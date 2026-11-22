package net.peelweb.enums;

public enum HttpMethod {

    GET, POST, PATCH, PUT, DELETE, OPTIONS;

    public static HttpMethod get(String method) {
        for (HttpMethod httpMethod : values()) {
            if (httpMethod.name().equalsIgnoreCase(method)) {
                return httpMethod;
            }
        }
        return null;
    }

}
