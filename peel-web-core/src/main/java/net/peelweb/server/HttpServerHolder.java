package net.peelweb.server;

public interface HttpServerHolder {

    void start();

    void stop();

    /** Port actually bound, or -1 before start. */
    default int getBoundPort() {
        return -1;
    }

}
