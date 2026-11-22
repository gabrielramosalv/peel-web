package net.peelweb;

import net.peelweb.context.ResourceContext;
import net.peelweb.context.controller.Controllers;
import net.peelweb.server.HttpServerHolder;
import net.peelweb.server.HttpServerHolderFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class PeelAppBuilder {

    private final List<ResourceContext> resourceContexts;

    private String staticContentPath;

    private String contextPath;

    private int port;

    private boolean isExternalStaticContent;

    private String host = net.peelweb.server.ServerOptions.LOCALHOST;

    private String allowedOrigin;

    private int maxThreads = net.peelweb.server.ServerOptions.defaults().maxThreads();

    private int maxBodyBytes = net.peelweb.server.ServerOptions.defaults().maxBodyBytes();

    public PeelAppBuilder() {
        this.resourceContexts = new ArrayList<>();
    }

    public static PeelApp run(Consumer<PeelAppBuilder> config) {
        PeelAppBuilder builder = new PeelAppBuilder();
        config.accept(builder);
        return builder.build();
    }

    public PeelAppBuilder context(String context) {
        this.contextPath = context;
        return this;
    }

    public PeelAppBuilder port(int port) {
        this.port = port;
        return this;
    }

    public PeelAppBuilder addResourceContext(ResourceContext resourceContext) {
        this.resourceContexts.add(resourceContext);
        return this;
    }

    public PeelAppBuilder addController(Object controller) {
        this.resourceContexts.add(Controllers.from(controller));
        return this;
    }

    /** Interface to listen on. Default 127.0.0.1; use "0.0.0.0" to accept other machines. */
    public PeelAppBuilder host(String host) {
        this.host = host;
        return this;
    }

    /** Value of Access-Control-Allow-Origin. Default: no CORS header (same origin only). */
    public PeelAppBuilder allowedOrigin(String origin) {
        this.allowedOrigin = origin;
        return this;
    }

    public PeelAppBuilder maxThreads(int maxThreads) {
        this.maxThreads = maxThreads;
        return this;
    }

    public PeelAppBuilder maxBodyBytes(int maxBodyBytes) {
        this.maxBodyBytes = maxBodyBytes;
        return this;
    }

    /** Replaces the JSON converter used by every PeelApp of this JVM. */
    public PeelAppBuilder jsonConverter(net.peelweb.json.JsonConverter converter) {
        net.peelweb.json.JsonConverterManager.getInstance().setConverter(converter);
        return this;
    }

    public PeelAppBuilder staticContentPath(String staticContentPath) {
        this.staticContentPath = staticContentPath;
        return this;
    }

    public PeelAppBuilder isExternalStaticContent() {
        this.isExternalStaticContent = true;
        return this;
    }

    private PeelApp build() {
        HttpServerHolder httpServerHolder = HttpServerHolderFactory.release(this.resourceContexts,
                this.staticContentPath, this.isExternalStaticContent, this.contextPath, this.port,
                new net.peelweb.server.ServerOptions(this.host, this.allowedOrigin, this.maxThreads, this.maxBodyBytes));
        return new PeelApp(httpServerHolder);
    }

}
