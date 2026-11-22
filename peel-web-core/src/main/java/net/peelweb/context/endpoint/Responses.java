package net.peelweb.context.endpoint;

import net.peelweb.enums.HttpContentType;
import net.peelweb.enums.HttpResponseCode;

import java.util.Map;

public class Responses {

    public static Response ok(Object body) {
        return new Response(HttpResponseCode.OK, body, HttpContentType.JSON);
    }

    public static Response page(String filePath) {
        return new Response(HttpResponseCode.OK, filePath, HttpContentType.HTML);
    }

    public static Response created(Object body) {
        return new Response(HttpResponseCode.CREATED, body, HttpContentType.JSON);
    }

    public static Response badRequest() {
        return new Response(HttpResponseCode.BAD_REQUEST, HttpContentType.JSON);
    }

    /** 400 with a JSON body {"error": message}. */
    public static Response badRequest(String message) {
        return error(HttpResponseCode.BAD_REQUEST, message);
    }

    public static Response notFound() {
        return new Response(HttpResponseCode.NOT_FOUND, HttpContentType.JSON);
    }

    /** 404 with a JSON body {"error": message}. */
    public static Response notFound(String message) {
        return error(HttpResponseCode.NOT_FOUND, message);
    }

    /** 410: the resource existed and is gone for good. */
    public static Response gone(String message) {
        return error(HttpResponseCode.GONE, message);
    }

    /** 304 without body; the caller should add the ETag header. */
    public static Response notModified() {
        return new Response(HttpResponseCode.NOT_MODIFIED, HttpContentType.JSON);
    }

    public static Response internalServerError() {
        return new Response(HttpResponseCode.INTERNAL_SERVER_ERROR, HttpContentType.JSON);
    }

    public static Response error(HttpResponseCode code, String message) {
        return new Response(code, Map.of("error", message == null ? code.getReason() : message), HttpContentType.JSON);
    }

    public static Response mount(HttpResponseCode code, Object body, HttpContentType httpContentType) {
        return new Response(code, body, httpContentType);
    }

    public static Response mount(HttpResponseCode code) {
        return new Response(code, null, null);
    }

}
