package net.peelweb.server;

import net.peelweb.PeelApp;
import net.peelweb.PeelAppBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Observador de requisição: chamado uma vez por resposta, com método, caminho, status e tempos")
class RequestObserverTest {

    record Seen(String method, String path, int status, long start, long end) {
    }

    @Test
    @DisplayName("Deve chamar o observador depois de cada resposta")
    void shouldObserveEachResponse() throws Exception {
        List<Seen> seen = new CopyOnWriteArrayList<>();
        PeelApp app = PeelAppBuilder.run(b -> b.port(0)
                .addController(new ComSunNetHttpServerHolderTest.ExampleController())
                .requestObserver((m, p, s, t0, t1) -> seen.add(new Seen(m, p, s, t0, t1))));
        app.start();
        try {
            HttpClient client = HttpClient.newHttpClient();
            String base = "http://127.0.0.1:" + app.getPort();
            assertEquals(200, client.send(HttpRequest.newBuilder(URI.create(base + "/api/items/7?q=x")).GET().build(),
                    HttpResponse.BodyHandlers.ofString()).statusCode());
            assertEquals(404, client.send(HttpRequest.newBuilder(URI.create(base + "/api/items/none")).GET().build(),
                    HttpResponse.BodyHandlers.ofString()).statusCode());
            long deadline = System.currentTimeMillis() + 2000;
            while (seen.size() < 2 && System.currentTimeMillis() < deadline) {
                Thread.sleep(10);
            }
            assertEquals(2, seen.size());
            assertEquals(new Seen("GET", "/api/items/7", 200, 0, 0).path(), seen.get(0).path());
            assertEquals(200, seen.get(0).status());
            assertEquals(404, seen.get(1).status());
            for (Seen s : seen) {
                assertEquals("GET", s.method());
                assertTrue(s.end() >= s.start());
            }
        } finally {
            app.stop();
        }
    }
}
