package net.peelweb.demo;

import net.peelweb.PeelApp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Testes de integração da aplicação demonstrativa (DemoApplication)")
class DemoApplicationIT {

    private PeelApp application;

    private HttpClient client;

    private int port;

    @BeforeEach
    void setUp() throws IOException {
        this.port = availablePort();
        this.client = HttpClient.newHttpClient();
        this.application = DemoApplication.create(this.port);
        this.application.start();
    }

    @AfterEach
    void tearDown() {
        this.application.stop();
    }

    @Test
    @DisplayName("Deve responder ao endpoint de saúde pelo servidor HTTP real")
    void shouldRespondToHealthEndpointThroughHttpServer() throws IOException, InterruptedException {
        HttpResponse<String> response = this.client.send(requestFor("/peel/health"),
                HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals("{\"status\":\"UP\"}", response.body());
        assertTrue(response.headers().firstValue("Content-Type").orElse("").startsWith("application/json"));
    }

    @Test
    @DisplayName("Deve servir a página estática empacotada no JAR")
    void shouldServePackagedStaticPage() throws IOException, InterruptedException {
        HttpResponse<String> response = this.client.send(requestFor("/peel/demo/static"),
                HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("<title>Peel Web Demo</title>"));
        assertTrue(response.headers().firstValue("Content-Type").orElse("").startsWith("text/html"));
    }

    private int availablePort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    private HttpRequest requestFor(String path) {
        return HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + this.port + path)).GET().build();
    }
}
