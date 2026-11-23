package net.peelweb.server;

import net.peelweb.PeelApp;
import net.peelweb.PeelAppBuilder;
import net.peelweb.context.controller.Controller;
import net.peelweb.context.controller.Mapping;
import net.peelweb.context.endpoint.Request;
import net.peelweb.context.endpoint.Response;
import net.peelweb.context.endpoint.Responses;
import net.peelweb.enums.HttpMethod;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes das virtual threads do servidor")
class VirtualThreadsTest {

    static final int CLIENTS = 64;

    @Controller("/api")
    public static class SlowController {
        final CountDownLatch allInside = new CountDownLatch(CLIENTS);
        final AtomicInteger running = new AtomicInteger();
        final AtomicInteger peak = new AtomicInteger();
        volatile boolean virtual = true;

        @Mapping(value = "/wait", method = HttpMethod.GET)
        public Response slow(Request request) throws InterruptedException {
            peak.accumulateAndGet(running.incrementAndGet(), Math::max);
            if (!Thread.currentThread().isVirtual()) virtual = false;
            allInside.countDown();
            allInside.await(10, TimeUnit.SECONDS);
            Thread.sleep(50);
            running.decrementAndGet();
            return Responses.ok(Map.of("ok", true));
        }
    }

    private static List<HttpResponse<String>> fire(PeelApp app) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        List<CompletableFuture<HttpResponse<String>>> futures = new ArrayList<>();
        for (int i = 0; i < CLIENTS; i++) {
            futures.add(client.sendAsync(HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + app.getPort() + "/api/wait")).build(),
                    HttpResponse.BodyHandlers.ofString()));
        }
        List<HttpResponse<String>> out = new ArrayList<>();
        for (var f : futures) out.add(f.get(30, TimeUnit.SECONDS));
        return out;
    }

    @Test
    @DisplayName("Sem limite, deve atender ao mesmo tempo mais requisições que núcleos, cada uma numa virtual thread")
    void shouldServeManyWaitingRequestsAtOnce() throws Exception {
        SlowController c = new SlowController();
        PeelApp app = PeelAppBuilder.run(b -> b.port(0).addController(c));
        app.start();
        try {
            for (HttpResponse<String> r : fire(app)) assertEquals(200, r.statusCode());
            assertEquals(CLIENTS, c.peak.get());
            assertTrue(c.virtual);
        } finally {
            app.stop();
        }
    }

    @Test
    @DisplayName("Com maxThreads, não deve passar do limite de requisições simultâneas")
    void shouldCapConcurrentRequests() throws Exception {
        SlowController c = new SlowController();
        PeelApp app = PeelAppBuilder.run(b -> b.port(0).maxThreads(4).addController(c));
        app.start();
        try {
            // com o limite, o latch não chega a zero: cada lote espera o tempo do await e sai
            c.allInside.countDown();
            for (int i = 0; i < CLIENTS - 5; i++) c.allInside.countDown();
            for (HttpResponse<String> r : fire(app)) assertEquals(200, r.statusCode());
            assertTrue(c.peak.get() <= 4, "pico " + c.peak.get());
        } finally {
            app.stop();
        }
    }
}
