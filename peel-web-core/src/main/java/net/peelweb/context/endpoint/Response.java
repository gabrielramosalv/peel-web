package net.peelweb.context.endpoint;

import lombok.Getter;
import net.peelweb.enums.HttpContentType;
import net.peelweb.enums.HttpResponseCode;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@Getter
public class Response {

    private HttpResponseCode responseCode;

    private Object body;

    private HttpContentType httpContentType;

    private final Map<String, String> headers = new LinkedHashMap<>();

    public Response(HttpResponseCode responseCode, Object body, HttpContentType httpContentType) {
        this.responseCode = responseCode;
        this.body = body;
        this.httpContentType = httpContentType;
    }

    protected Response(HttpResponseCode responseCode, HttpContentType httpContentType) {
        this.responseCode = responseCode;
        this.httpContentType = httpContentType;
    }

    /** Adds (or replaces) a response header. Returns this response, for chaining. */
    public Response header(String name, String value) {
        this.headers.put(name, value);
        return this;
    }

    public Map<String, String> getHeaders() {
        return Collections.unmodifiableMap(this.headers);
    }

}
