package net.peelweb.server;

/**
 * Observer called once per request, after the response was written and the exchange closed.
 * Meant for measurement: {@code startNanos} is taken when the server hands the request to Peel Web (headers already
 * read) and {@code endNanos} after the exchange is closed, both from {@link System#nanoTime()}. It runs on the thread
 * that handled the request, so a caller can correlate it with thread-local data set by its own controllers.
 */
@FunctionalInterface
public interface RequestObserver {

    void requestCompleted(String method, String path, int status, long startNanos, long endNanos);
}
