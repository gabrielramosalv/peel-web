package net.peelweb.server;

import net.peelweb.PeelApp;
import net.peelweb.PeelAppBuilder;
import net.peelweb.context.controller.Controller;
import net.peelweb.context.controller.Mapping;
import net.peelweb.context.endpoint.Request;
import net.peelweb.context.endpoint.Response;
import net.peelweb.context.endpoint.Responses;
import net.peelweb.enums.HttpMethod;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes de integração do servidor: cabeçalhos, ETag, códigos de erro e segurança")
class ComSunNetHttpServerHolderTest {

    @Controller("/api")
    public static class ExampleController {

        @Mapping(value = "/items/{id}", method = HttpMethod.GET)
        public Response item(Request request) {
            String id = request.getPathVariable("id");
            if ("old".equals(id)) {
                return Responses.gone("item removed");
            }
            if ("none".equals(id)) {
                return Responses.notFound("no item");
            }
            String etag = "\"v1\"";
            if (etag.equals(request.getHeader("If-None-Match"))) {
                return Responses.notModified().header("ETag", etag);
            }
            return Responses.ok(Map.of("id", id, "q", String.valueOf(request.getParameter("q")))).header("ETag", etag);
        }

        @Mapping(value = "/items/{id}", method = HttpMethod.PUT)
        public Response update(Request request) {
            return Responses.ok(Map.of("body", String.valueOf(request.getBodyAs(Body.class).value)));
        }

        @Mapping(value = "/boom", method = HttpMethod.GET)
        public Response boom(Request request) {
            throw new IllegalStateException("boom");
        }
    }

    @Controller("/api")
    public static class OtherController {

        @Mapping(value = "/items/{id}/history", method = HttpMethod.GET)
        public Response history(Request request) {
            return Responses.ok(Map.of("history", request.getPathVariable("id")));
        }
    }

    public static class Body {
        private int value;
    }

    private PeelApp app;
    private HttpClient client;

    @BeforeEach
    void setUp() {
        app = PeelAppBuilder.run(b -> b.port(0).addController(new ExampleController()).addController(new OtherController()));
        app.start();
        client = HttpClient.newHttpClient();
    }

    @AfterEach
    void tearDown() {
        app.stop();
    }

    private HttpResponse<String> get(String path, String... headers) throws Exception {
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + app.getPort() + path));
        for (int i = 0; i < headers.length; i += 2) b.header(headers[i], headers[i + 1]);
        return client.send(b.GET().build(), HttpResponse.BodyHandlers.ofString());
    }

    @Test
    @DisplayName("Deve enviar o ETag e responder 304 sem corpo quando If-None-Match bate")
    void shouldAnswerNotModifiedWithEtag() throws Exception {
        HttpResponse<String> first = get("/api/items/a");
        assertEquals(200, first.statusCode());
        assertEquals("\"v1\"", first.headers().firstValue("ETag").orElse(null));

        HttpResponse<String> second = get("/api/items/a", "If-None-Match", "\"v1\"");
        assertEquals(304, second.statusCode());
        assertEquals("", second.body());
        assertEquals("\"v1\"", second.headers().firstValue("ETag").orElse(null));
    }

    @Test
    @DisplayName("Deve decodificar a query string (%20, + e =)")
    void shouldDecodeQueryParameters() throws Exception {
        HttpResponse<String> r = get("/api/items/a?q=a%20b+c%3Dd");
        assertEquals(200, r.statusCode());
        assertTrue(r.body().contains("\"q\":\"a b c=d\""), r.body());
    }

    @Test
    @DisplayName("Deve responder 404 e 410 com corpo de erro em JSON")
    void shouldAnswerNotFoundAndGoneWithBody() throws Exception {
        HttpResponse<String> nf = get("/api/items/none");
        assertEquals(404, nf.statusCode());
        assertEquals("{\"error\":\"no item\"}", nf.body());

        HttpResponse<String> gone = get("/api/items/old");
        assertEquals(410, gone.statusCode());
        assertEquals("{\"error\":\"item removed\"}", gone.body());
    }

    @Test
    @DisplayName("Deve responder 404 para rota desconhecida e 405 para método não mapeado")
    void shouldAnswerUnknownRouteAndMethod() throws Exception {
        assertEquals(404, get("/api/unknown").statusCode());
        HttpResponse<String> r = client.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + app.getPort() + "/api/items/a"))
                .DELETE().build(), HttpResponse.BodyHandlers.ofString());
        assertEquals(405, r.statusCode());
    }

    @Test
    @DisplayName("Deve registrar a exceção do endpoint e responder 500 com corpo")
    void shouldAnswerInternalErrorWithBody() throws Exception {
        HttpResponse<String> r = get("/api/boom");
        assertEquals(500, r.statusCode());
        assertEquals("{\"error\":\"internal error\"}", r.body());
    }

    @Test
    @DisplayName("Deve ler o corpo do PUT")
    void shouldReadPutBody() throws Exception {
        HttpResponse<String> r = client.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + app.getPort() + "/api/items/a"))
                .PUT(HttpRequest.BodyPublishers.ofString("{\"value\": 42}")).header("Content-Type", "application/json").build(),
                HttpResponse.BodyHandlers.ofString());
        assertEquals(200, r.statusCode());
        assertEquals("{\"body\":\"42\"}", r.body());
    }

    @Test
    @DisplayName("Sem allowedOrigin, não envia cabeçalho de CORS; com ele, envia e expõe o ETag")
    void shouldSendCorsOnlyWhenConfigured() throws Exception {
        assertTrue(get("/api/items/a").headers().firstValue("Access-Control-Allow-Origin").isEmpty());
        app.stop();
        app = PeelAppBuilder.run(b -> b.port(0).allowedOrigin("*").addController(new ExampleController()));
        app.start();
        HttpResponse<String> r = get("/api/items/a");
        assertEquals("*", r.headers().firstValue("Access-Control-Allow-Origin").orElse(null));
        assertEquals("ETag", r.headers().firstValue("Access-Control-Expose-Headers").orElse(null));
    }

    @Test
    @DisplayName("Deve atender dois controllers com o mesmo prefixo, cada rota no seu")
    void shouldServeControllersSharingTheBaseMapping() throws Exception {
        HttpResponse<String> r = get("/api/items/a/history");
        assertEquals(200, r.statusCode());
        assertEquals("{\"history\":\"a\"}", r.body());
        assertEquals(200, get("/api/items/a").statusCode());
        assertEquals(404, get("/api/items/a/other").statusCode());
    }
}
