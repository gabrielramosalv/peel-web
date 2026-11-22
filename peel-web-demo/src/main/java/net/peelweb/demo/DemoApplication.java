package net.peelweb.demo;

import net.peelweb.PeelApp;
import net.peelweb.PeelAppBuilder;

public class DemoApplication {

    private static final int DEFAULT_PORT = 8080;

    public static void main(String[] args) {
        PeelApp app = create(resolvePort());
        app.start();
    }

    public static PeelApp create(int port) {
        return PeelAppBuilder.run(builder -> builder
                .context("/peel")
                .port(port)
                .staticContentPath("net/peelweb/demo/static")
                .addController(new DemoController())
        );
    }

    private static int resolvePort() {
        String configuredPort = System.getenv("PORT");
        if (configuredPort == null || configuredPort.isBlank()) {
            return DEFAULT_PORT;
        }
        return Integer.parseInt(configuredPort);
    }

}
