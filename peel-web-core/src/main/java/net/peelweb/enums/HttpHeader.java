package net.peelweb.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum HttpHeader {

    // Requisição
    ACCEPT("Accept"),
    ACCEPT_ENCODING("Accept-Encoding"),
    ACCEPT_LANGUAGE("Accept-Language"),
    AUTHORIZATION("Authorization"),
    CACHE_CONTROL("Cache-Control"),
    CONNECTION("Connection"),
    CONTENT_LENGTH("Content-Length"),
    CONTENT_TYPE("Content-Type"),
    COOKIE("Cookie"),
    HOST("Host"),
    ORIGIN("Origin"),
    REFERER("Referer"),
    USER_AGENT("User-Agent"),

    // Resposta
    ACCESS_CONTROL_ALLOW_ORIGIN("Access-Control-Allow-Origin"),
    CONTENT_DISPOSITION("Content-Disposition"),
    CONTENT_ENCODING("Content-Encoding"),
    CONTENT_LANGUAGE("Content-Language"),
    DATE("Date"),
    ETAG("ETag"),
    EXPIRES("Expires"),
    LAST_MODIFIED("Last-Modified"),
    LOCATION("Location"),
    SERVER("Server"),
    SET_COOKIE("Set-Cookie"),
    VARY("Vary"),
    WWW_AUTHENTICATE("WWW-Authenticate");

    private final String name;

}
