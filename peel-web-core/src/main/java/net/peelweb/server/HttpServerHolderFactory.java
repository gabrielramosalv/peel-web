package net.peelweb.server;

import net.peelweb.context.ResourceContext;

import java.util.List;

public class HttpServerHolderFactory {

    public static HttpServerHolder release(List<ResourceContext> resourceContexts, String staticContentPath, boolean isExternalStaticContent,
                                           String contextPath, int port) {
        return release(resourceContexts, staticContentPath, isExternalStaticContent, contextPath, port, ServerOptions.defaults());
    }

    public static HttpServerHolder release(List<ResourceContext> resourceContexts, String staticContentPath, boolean isExternalStaticContent,
                                           String contextPath, int port, ServerOptions options) {
        return new ComSunNetHttpServerHolder(resourceContexts, port, contextPath, staticContentPath, isExternalStaticContent, options);
    }

}
