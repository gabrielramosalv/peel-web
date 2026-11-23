package net.peelweb.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import net.peelweb.context.ResourceContext;
import net.peelweb.context.endpoint.*;
import net.peelweb.enums.HttpContentType;
import net.peelweb.enums.HttpHeader;
import net.peelweb.enums.HttpMethod;
import net.peelweb.enums.HttpResponseCode;
import net.peelweb.resp.ResponseAssembler;
import net.peelweb.resp.ResponseAssemblerFactory;
import net.peelweb.util.WebUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ComSunNetHttpServerHolder implements HttpServerHolder {

    private static final Logger LOGGER = Logger.getLogger(ComSunNetHttpServerHolder.class.getName());

    private final List<ResourceContext> resourceContexts;

    private final int port;

    private final String contextPath;

    private final String staticContentPath;

    private final boolean isExternalStaticContent;

    private final ServerOptions options;

    private HttpServer httpServer;

    private ExecutorService executorService;

    public ComSunNetHttpServerHolder(List<ResourceContext> resourceContexts, int port, String contextPath,
                                     String staticContentPath, boolean isExternalStaticContent) {
        this(resourceContexts, port, contextPath, staticContentPath, isExternalStaticContent, ServerOptions.defaults());
    }

    public ComSunNetHttpServerHolder(List<ResourceContext> resourceContexts, int port, String contextPath,
                                     String staticContentPath, boolean isExternalStaticContent, ServerOptions options) {
        this.resourceContexts = resourceContexts;
        this.port = port;
        this.contextPath = contextPath;
        this.staticContentPath = staticContentPath;
        this.isExternalStaticContent = isExternalStaticContent;
        this.options = options == null ? ServerOptions.defaults() : options;
    }

    /** Port actually bound (useful when the configured port is 0). */
    @Override
    public int getBoundPort() {
        return this.httpServer == null ? -1 : this.httpServer.getAddress().getPort();
    }

    private String getRequestBody(HttpExchange exchange) throws IOException {
        try (ByteArrayOutputStream buffer = new ByteArrayOutputStream();
             InputStream input = exchange.getRequestBody()) {
            byte[] data = new byte[4096];
            int bytesRead;
            int total = 0;
            while ((bytesRead = input.read(data)) != -1) {
                total += bytesRead;
                if (total > this.options.maxBodyBytes()) {
                    throw new BodyTooLargeException();
                }
                buffer.write(data, 0, bytesRead);
            }
            String body = buffer.toString(StandardCharsets.UTF_8);
            return body.isEmpty() ? null : body;
        }
    }

    private static final class BodyTooLargeException extends IOException {
    }

    private static boolean hasBody(HttpMethod method) {
        return method == HttpMethod.POST || method == HttpMethod.PUT || method == HttpMethod.PATCH;
    }

    private void setDefaultHeaders(ResourceContext resourceContext, HttpExchange exchange) {
        if (this.options.allowedOrigin() == null) {
            return;
        }
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", this.options.allowedOrigin());
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods",
                resourceContext.getEndpoints().stream().map(e -> e.getMethod().toString()).distinct()
                        .collect(Collectors.joining(",")));
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers",
                "Origin, Accept, X-Requested-With, Content-Type, If-None-Match, "
                        + "Access-Control-Request-Method, Access-Control-Request-Headers, Authorization");
        exchange.getResponseHeaders().set("Access-Control-Expose-Headers", "ETag");
    }

    private String mountResourceContextMapping(ResourceContext resourceContext) {
        if (this.contextPath == null || this.contextPath.isEmpty()) {
            return resourceContext.getBaseMapping();
        }
        return this.contextPath + resourceContext.getBaseMapping();
    }

    private static Map<String, String> headersOf(HttpExchange exchange) {
        Map<String, String> headers = new HashMap<>();
        exchange.getRequestHeaders().forEach((k, v) -> {
            if (v != null && !v.isEmpty()) headers.put(k, v.get(0));
        });
        return headers;
    }

    @Override
    public void start() {
        if (this.httpServer != null) {
            return;
        }

        try {
            this.httpServer = HttpServer.create(new InetSocketAddress(this.options.host(), this.port), 0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // one virtual thread per request: a request that waits (a long poll, a stream) holds no platform thread;
        // maxThreads > 0 caps how many requests run at once, and 0 or less means no cap
        java.util.concurrent.ThreadFactory factory = Thread.ofVirtual().name("peel-web-http-", 0).factory();
        this.executorService = this.options.maxThreads() > 0
                ? new BoundedExecutor(Executors.newThreadPerTaskExecutor(factory), this.options.maxThreads())
                : Executors.newThreadPerTaskExecutor(factory);
        this.httpServer.setExecutor(this.executorService);

        final ResponseAssemblerFactory responseAssemblerFactory = new ResponseAssemblerFactory(this.staticContentPath,
                this.isExternalStaticContent);

        // controllers that share a base mapping share one server context (the JDK server refuses two
        // contexts with the same path); each request is matched against all of them, in registration order
        Map<String, List<ResourceContext>> byMapping = new java.util.LinkedHashMap<>();
        for (ResourceContext resourceContext : this.resourceContexts) {
            byMapping.computeIfAbsent(this.mountResourceContextMapping(resourceContext), k -> new java.util.ArrayList<>())
                    .add(resourceContext);
        }

        for (Map.Entry<String, List<ResourceContext>> entry : byMapping.entrySet()) {
            List<ResourceContext> contexts = entry.getValue();
            List<EndpointTracker> trackers = new java.util.ArrayList<>();
            for (ResourceContext resourceContext : contexts) {
                trackers.add(new EndpointTracker(resourceContext, this.contextPath));
            }
            this.httpServer.createContext(entry.getKey(), exchange -> {
                try {
                    this.handle(contexts.get(0), trackers, responseAssemblerFactory, exchange);
                } catch (Throwable t) {
                    LOGGER.log(Level.SEVERE, "Error handling " + exchange.getRequestMethod() + " " + exchange.getRequestURI(), t);
                    try {
                        this.send(exchange, Responses.error(HttpResponseCode.INTERNAL_SERVER_ERROR, "internal error"),
                                responseAssemblerFactory);
                    } catch (Throwable ignored) {
                        // the response may already be committed
                    }
                } finally {
                    exchange.close();
                }
            });
        }
        this.httpServer.start();
    }

    private void handle(ResourceContext resourceContext, List<EndpointTracker> endpointTrackers,
                        ResponseAssemblerFactory responseAssemblerFactory, HttpExchange exchange) throws IOException {
        String requestMethod = exchange.getRequestMethod();

        this.setDefaultHeaders(resourceContext, exchange);

        HttpMethod method = HttpMethod.get(requestMethod);
        if (method == HttpMethod.OPTIONS) {
            exchange.sendResponseHeaders(HttpResponseCode.NO_CONTENT.getNumber(), -1);
            return;
        }
        if (method == null) {
            this.send(exchange, Responses.error(HttpResponseCode.METHOD_NOT_ALLOWED, "method not supported"),
                    responseAssemblerFactory);
            return;
        }

        String uri = exchange.getRequestURI().toString();
        Map<String, String> queryParameters = WebUtils.getQueryParameters(uri);
        List<String> contentType = exchange.getRequestHeaders().get(HttpHeader.CONTENT_TYPE.getName());
        Map<String, Object> formData = new HashMap<>();
        String body = null;
        if (hasBody(method)) {
            try {
                if (contentType != null && !contentType.isEmpty() && contentType.get(0).startsWith("multipart/")) {
                    formData = WebUtils.getFormData(exchange.getRequestBody(), contentType.get(0));
                } else {
                    body = this.getRequestBody(exchange);
                }
            } catch (BodyTooLargeException e) {
                this.send(exchange, Responses.error(HttpResponseCode.PAYLOAD_TOO_LARGE, "request body too large"),
                        responseAssemblerFactory);
                return;
            }
        }

        StandardRequest standardRequest = new StandardRequest(method, uri, body, queryParameters, formData,
                headersOf(exchange));
        Endpoint endpoint = null;
        boolean otherMethod = false;
        for (EndpointTracker endpointTracker : endpointTrackers) {
            endpoint = endpointTracker.find(standardRequest);
            if (endpoint != null) {
                break;
            }
            otherMethod = otherMethod || endpointTracker.matchesAnyMethod(standardRequest);
        }

        Response response;
        if (endpoint != null) {
            try {
                response = endpoint.getHandler().execute(standardRequest);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error in endpoint " + method + " " + uri, e);
                response = Responses.error(HttpResponseCode.INTERNAL_SERVER_ERROR, "internal error");
            }

            if (response == null) {
                response = Responses.mount(HttpResponseCode.NOT_IMPLEMENTED);
            }
        } else if (otherMethod) {
            response = Responses.error(HttpResponseCode.METHOD_NOT_ALLOWED, "method not allowed");
        } else {
            HttpContentType httpContentType = HttpContentType.fromUri(uri);
            if (httpContentType != null) {
                response = Responses.mount(HttpResponseCode.OK, uri, httpContentType);
            } else {
                response = Responses.notFound("not found");
            }
        }

        this.send(exchange, response, responseAssemblerFactory);
    }

    private void send(HttpExchange exchange, Response response, ResponseAssemblerFactory responseAssemblerFactory)
            throws IOException {
        response.getHeaders().forEach((k, v) -> exchange.getResponseHeaders().set(k, v));
        int code = response.getResponseCode().getNumber();

        // 204, 304 and responses without body carry no content
        if (response.getBody() == null || code == 204 || code == 304) {
            exchange.sendResponseHeaders(code, -1);
            return;
        }

        HttpContentType contentType = response.getHttpContentType() == null ? HttpContentType.JSON
                : response.getHttpContentType();
        ResponseAssembler responseAssembler = responseAssemblerFactory.get(contentType);
        byte[] bytes = responseAssembler.assembly(response.getBody()).getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set(HttpHeader.CONTENT_TYPE.getName(), contentType.getName() + ";charset=utf-8");
        exchange.sendResponseHeaders(code, bytes.length);
        try (OutputStream outputBody = exchange.getResponseBody()) {
            outputBody.write(bytes);
        }
    }

    @Override
    public void stop() {
        if (this.httpServer != null) {
            this.httpServer.stop(0);
            this.httpServer = null;
        }
        if (this.executorService != null) {
            this.executorService.shutdownNow();
            this.executorService = null;
        }
    }
}
