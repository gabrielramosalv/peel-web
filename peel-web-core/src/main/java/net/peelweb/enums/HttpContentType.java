package net.peelweb.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Getter
public enum HttpContentType {

    JSON("application/json"),

    HTML("text/html"),

    CSS("text/css"),

    JAVASCRIPT("application/javascript"),

    PNG("image/png"),

    JPEG("image/jpeg"),

    JPG("image/jpeg"),

    GIF("image/gif"),

    SVG("image/svg+xml"),

    ICO("image/x-icon"),

    WOFF("font/woff"),

    WOFF2("font/woff2"),

    TTF("font/ttf"),

    OTF("font/otf"),

    MAP("application/json"),

    TEXT("text/plain");

    private static final Map<String, HttpContentType> EXTENSION_MAP = new HashMap<>();

    private final String name;

    static {
        EXTENSION_MAP.put("json", JSON);
        EXTENSION_MAP.put("html", HTML);
        EXTENSION_MAP.put("htm", HTML);
        EXTENSION_MAP.put("css", CSS);
        EXTENSION_MAP.put("js", JAVASCRIPT);
        EXTENSION_MAP.put("png", PNG);
        EXTENSION_MAP.put("jpeg", JPEG);
        EXTENSION_MAP.put("jpg", JPG);
        EXTENSION_MAP.put("gif", GIF);
        EXTENSION_MAP.put("svg", SVG);
        EXTENSION_MAP.put("ico", ICO);
        EXTENSION_MAP.put("woff", WOFF);
        EXTENSION_MAP.put("woff2", WOFF2);
        EXTENSION_MAP.put("ttf", TTF);
        EXTENSION_MAP.put("otf", OTF);
        EXTENSION_MAP.put("map", MAP);
        EXTENSION_MAP.put("txt", TEXT);
    }

    public static HttpContentType get(String contentType) {
        for (HttpContentType type : HttpContentType.values()) {
            if (type.getName().equalsIgnoreCase(contentType)) {
                return type;
            }
        }
        return null;
    }

    public static HttpContentType fromExtension(String ext) {
        if (ext == null) {
            return null;
        }
        return EXTENSION_MAP.get(ext.toLowerCase());
    }

    public static HttpContentType fromUri(String uri) {
        if (uri == null || !uri.contains(".")) {
            return null;
        }

        String path = uri.split("\\?")[0]; // Remove query string
        int lastDot = path.lastIndexOf(".");
        if (lastDot == -1 || lastDot == path.length() - 1) {
            return null;
        }

        String ext = path.substring(lastDot + 1);
        return fromExtension(ext);
    }
}
