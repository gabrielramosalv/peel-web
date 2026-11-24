package net.peelweb.server;

/**
 * Network and safety options of the HTTP server.
 *
 * @param host          interface to listen on; "127.0.0.1" by default (only the local machine)
 * @param allowedOrigin value of Access-Control-Allow-Origin; null sends no CORS header
 * @param maxThreads    most requests served at once, each on its own virtual thread; 0 or less means no cap
 * @param maxBodyBytes  largest request body accepted (413 above it)
 */
public record ServerOptions(String host, String allowedOrigin, int maxThreads, int maxBodyBytes,
                            RequestObserver requestObserver) {

    public static final String LOCALHOST = "127.0.0.1";

    public ServerOptions(String host, String allowedOrigin, int maxThreads, int maxBodyBytes) {
        this(host, allowedOrigin, maxThreads, maxBodyBytes, null);
    }

    public static ServerOptions defaults() {
        return new ServerOptions(LOCALHOST, null, 0, 1 << 20);
    }
}
