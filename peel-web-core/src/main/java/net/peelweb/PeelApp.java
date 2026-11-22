package net.peelweb;

import lombok.RequiredArgsConstructor;
import net.peelweb.server.HttpServerHolder;

@RequiredArgsConstructor
public class PeelApp {

    private final HttpServerHolder serverHolder;

    public void start() {
        this.serverHolder.start();
    }

    /** Port actually bound; with port(0) the system picks a free one. */
    public int getPort() {
        return this.serverHolder.getBoundPort();
    }

    public void stop() {
        this.serverHolder.stop();
    }

}
