package net.peelweb.context.endpoint;

import net.peelweb.context.ResourceContext;

import java.util.HashMap;
import java.util.Map;

import static net.peelweb.util.WebUtils.getPathVariable;
import static net.peelweb.util.WebUtils.getPureResourceMapping;

public class EndpointTracker {

    private final ResourceContext resourceContext;

    private final String contextPath;

    public EndpointTracker(ResourceContext resourceContext, String contextPath) {
        this.resourceContext = resourceContext;
        this.contextPath = contextPath;
    }

    public Endpoint find(StandardRequest standardRequest) {
        for (Endpoint endpoint : this.resourceContext.getEndpoints()) {
            if (!standardRequest.getMethod().equals(endpoint.getMethod())) {
                continue;
            }

            Map<String, String> pathVariables = new HashMap<>();
            boolean mappingMatches = this.matches(endpoint, standardRequest.getUri(), pathVariables);
            if (!mappingMatches) {
                continue;
            }

            standardRequest.setPathVariables(pathVariables);

            return endpoint;
        }

        return null;
    }

    /** True when some endpoint has this path with another HTTP method (to answer 405 instead of 404). */
    public boolean matchesAnyMethod(StandardRequest standardRequest) {
        for (Endpoint endpoint : this.resourceContext.getEndpoints()) {
            if (this.matches(endpoint, standardRequest.getUri(), new HashMap<>())) {
                return true;
            }
        }
        return false;
    }

    private boolean matches(Endpoint endpoint, String requestUri, Map<String, String> pathVariables) {
        if (this.contextPath != null && !this.contextPath.isEmpty()) {
            requestUri = requestUri.substring(requestUri.indexOf(this.contextPath) + this.contextPath.length());
        }

        int query = requestUri.indexOf('?');
        if (query >= 0) {
            requestUri = requestUri.substring(0, query);
        }
        requestUri = getPureResourceMapping(requestUri);
        String completeMapping = getPureResourceMapping(this.resourceContext.getBaseMapping()) + getPureResourceMapping(
                endpoint.getMapping());

        if (getPathVariable(endpoint.getMapping()) == null && completeMapping.equals(requestUri)) {
            return true;
        }

        String[] requestUriComponents = requestUri.split("/");
        String[] completeMappingComponents = completeMapping.split("/");

        if (requestUriComponents.length != completeMappingComponents.length) {
            return false;
        }

        for (int i = 0; i < requestUriComponents.length; i++) {
            String completeMappingComponent = completeMappingComponents[i];
            String requestUriComponent = requestUriComponents[i];

            String pathVariable = getPathVariable(completeMappingComponent);
            if (pathVariable == null) {
                if (!requestUriComponent.equals(completeMappingComponent)) {
                    return false;
                }
            } else {
                pathVariables.put(pathVariable, requestUriComponent);
            }
        }

        return true;
    }

}
