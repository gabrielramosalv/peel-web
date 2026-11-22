package net.peelweb.resp;

import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
public class ClasspathContentAssembler implements ResponseAssembler {

    private final String baseResourcePath;

    private String sanitize(String p) {
        if (p == null) {
            return "";
        }

        try {
            p = URLDecoder.decode(p, StandardCharsets.UTF_8);
        } catch (Exception ignored) {
        }

        p = p.replace('\\', '/');
        p = p.replaceAll("^/+", "");        // sem barra inicial
        if (p.contains("..")) {
            throw new RuntimeException("Path traversal not allowed: " + p);
        }
        return p;
    }

    private String join(String base, String rel) {
        if (base == null || base.isEmpty()) {
            return rel;
        }
        if (base.endsWith("/")) {
            return base + rel;
        }
        return base + "/" + rel;
    }

    @Override
    public String assembly(Object object) {
        String resourcePath = this.sanitize((String) object);
        String fullPath = this.join(this.baseResourcePath, resourcePath);

        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(fullPath);

        if (in == null) {
            int lastSlash = resourcePath.lastIndexOf('/');
            if (lastSlash >= 0) {
                String fileName = resourcePath.substring(lastSlash + 1);
                String fallback = join(this.baseResourcePath, fileName);
                in = Thread.currentThread().getContextClassLoader().getResourceAsStream(fallback);
            }
        }

        if (in == null) {
            throw new RuntimeException("Resource not found on classpath: " + fullPath);
        }

        byte[] bytes;
        try {
            bytes = in.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return new String(bytes, StandardCharsets.UTF_8);
    }
}
