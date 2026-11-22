package net.peelweb.context.endpoint;

import net.peelweb.enums.HttpMethod;

public class Endpoints {

    public static Endpoint get(String mapping, RequestHandler handler) {
        return mount(HttpMethod.GET, mapping, handler);
    }

    public static Endpoint put(String mapping, RequestHandler handler) {
        return mount(HttpMethod.PUT, mapping, handler);
    }

    public static Endpoint patch(String mapping, RequestHandler handler) {
        return mount(HttpMethod.PATCH, mapping, handler);
    }

    public static Endpoint post(String mapping, RequestHandler handler) {
        return mount(HttpMethod.POST, mapping, handler);
    }

    public static Endpoint mount(HttpMethod method, String mapping, RequestHandler requestHandler) {
        return new Endpoint() {
            @Override
            public RequestHandler getHandler() {
                return requestHandler;
            }

            @Override
            public HttpMethod getMethod() {
                return method;
            }

            @Override
            public String getMapping() {
                return mapping;
            }
        };
    }

}
