package net.peelweb.context.endpoint;

import net.peelweb.context.ResourceContext;
import net.peelweb.enums.HttpMethod;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

@DisplayName("Testes do rastreamento de endpoints (EndpointTracker)")
class EndpointTrackerTest {

    @Test
    @DisplayName("Deve localizar o endpoint e extrair sua variável de rota")
    void shouldFindEndpointAndExtractPathVariable() {
        Endpoint endpoint = Endpoints.get("/{id}", request -> Responses.ok("found"));
        EndpointTracker tracker = new EndpointTracker(resourceContext(endpoint), "/api");
        StandardRequest request = requestFor("/api/users/42");

        Endpoint result = tracker.find(request);

        assertSame(endpoint, result);
        assertEquals("42", request.getPathVariable("id"));
    }

    @Test
    @DisplayName("Não deve reutilizar variáveis de rota de uma busca anterior")
    void shouldNotReusePathVariablesFromPreviousSearch() {
        Endpoint endpoint = Endpoints.get("/{id}", request -> Responses.ok("found"));
        EndpointTracker tracker = new EndpointTracker(resourceContext(endpoint), "/api");
        StandardRequest firstRequest = requestFor("/api/users/42");
        StandardRequest secondRequest = requestFor("/api/users/not-found/details");

        tracker.find(firstRequest);
        Endpoint result = tracker.find(secondRequest);

        assertNull(result);
        assertNull(secondRequest.getPathVariable("id"));
    }

    private ResourceContext resourceContext(Endpoint endpoint) {
        return new ResourceContext() {
            @Override
            public List<Endpoint> getEndpoints() {
                return List.of(endpoint);
            }

            @Override
            public String getBaseMapping() {
                return "/users";
            }
        };
    }

    private StandardRequest requestFor(String uri) {
        return new StandardRequest(HttpMethod.GET, uri, null, Map.of(), Map.of());
    }
}
