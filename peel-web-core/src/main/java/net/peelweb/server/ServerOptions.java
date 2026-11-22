package net.peelweb.server;

/**
 * Network and safety options of the HTTP server.
 *
 * @param host          interface to listen on; "127.0.0.1" by default (only the local machine)
 * @param allowedOrigin value of Access-Control-Allow-Origin; null sends no CORS header
 * @param maxThreads    size of the request thread pool
 * @param maxBodyBytes  largest request body accepted (413 above it)
 */
public record ServerOptions(String host, String allowedOrigin, int maxThreads, int maxBodyBytes) {

    public static final String LOCALHOST = "127.0.0.1";

    public static ServerOptions defaults() {
        return new ServerOptions(LOCALHOST, null, 16, 1 << 20);
    }
}
