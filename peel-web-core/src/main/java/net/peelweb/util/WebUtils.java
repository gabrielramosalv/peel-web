package net.peelweb.util;

import net.peelweb.context.endpoint.FileEntry;
import net.peelweb.context.endpoint.StandardFileEntry;
import net.peelweb.enums.HttpContentType;
import net.peelweb.enums.HttpHeader;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebUtils {

    public static Map<String, Object> getFormData(InputStream input, String contentType) {
        Map<String, Object> formData = new HashMap<>();
        String boundary = extractBoundary(contentType);
        if (boundary == null) {
            return formData;
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
        String line;
        String name = null;
        String filename = null;
        HttpContentType type = null;
        boolean readingContent = false;

        ByteArrayOutputStream fileOutput = null;
        StringBuilder fieldBuilder = new StringBuilder();

        try {
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("--" + boundary)) {
                    if (name != null) {
                        if (filename != null && fileOutput != null) {
                            byte[] fileBytes = fileOutput.toByteArray();
                            InputStream fileStream = new ByteArrayInputStream(fileBytes);
                            FileEntry uploadedFile = new StandardFileEntry(filename, fileStream, fileBytes.length,
                                    type);
                            formData.put(name, uploadedFile);
                        } else {
                            formData.put(name, fieldBuilder.toString().trim());
                        }
                    }

                    // Reset para o próximo campo
                    name = null;
                    filename = null;
                    type = null;
                    fieldBuilder.setLength(0);
                    fileOutput = null;
                    readingContent = false;
                } else if (!readingContent && line.toLowerCase().startsWith(
                        HttpHeader.CONTENT_DISPOSITION.getName().toLowerCase())) {
                    name = extractNameFromContentDisposition(line);
                    filename = extractFilenameFromContentDisposition(line);
                } else if (!readingContent && line.toLowerCase().startsWith(
                        HttpHeader.CONTENT_TYPE.getName().toLowerCase())) {
                    String ct = line.split(":")[1].trim();
                    type = HttpContentType.get(ct);
                } else if (!readingContent && line.isEmpty()) {
                    readingContent = true;
                    if (filename != null) {
                        fileOutput = new ByteArrayOutputStream();
                    }
                } else if (readingContent) {
                    if (filename != null && fileOutput != null) {
                        fileOutput.write(line.getBytes(StandardCharsets.UTF_8));
                        fileOutput.write('\n');
                    } else {
                        fieldBuilder.append(line).append('\n');
                    }
                }
            }

            if (name != null) {
                if (filename != null && fileOutput != null) {
                    byte[] fileBytes = fileOutput.toByteArray();
                    InputStream fileStream = new ByteArrayInputStream(fileBytes);
                    FileEntry uploadedFile = new StandardFileEntry(filename, fileStream, fileBytes.length, type);
                    formData.put(name, uploadedFile);
                } else {
                    formData.put(name, fieldBuilder.toString().trim());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error parsing multipart form-data", e);
        }

        return formData;
    }

    /** Query parameters of a raw URI, URL-decoded (UTF-8). A repeated name keeps the last value. */
    public static Map<String, String> getQueryParameters(String URI) {
        int q = URI.indexOf('?');
        if (q < 0 || q == URI.length() - 1) {
            return new HashMap<>();
        }

        Map<String, String> parameters = new HashMap<>();
        for (String param : URI.substring(q + 1).split("&")) {
            if (param.isEmpty()) {
                continue;
            }
            int eq = param.indexOf('=');
            String name = eq < 0 ? param : param.substring(0, eq);
            String value = eq < 0 ? "" : param.substring(eq + 1);
            parameters.put(decode(name), decode(value));
        }

        return parameters;
    }

    private static String decode(String s) {
        try {
            return java.net.URLDecoder.decode(s, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            return s; // malformed escape: keep the raw text
        }
    }

    public static String getPureResourceMapping(String uri) {
        if (uri.isEmpty()) {
            return uri;
        }
        if (uri.contains("?")) {
            uri = uri.substring(0, uri.indexOf("?"));
        }
        if (uri.charAt(uri.length() - 1) == '/') {
            uri = uri.substring(0, uri.length() - 1);
        }
        return uri;
    }

    public static String getPathVariable(String component) {
        Pattern pathVariablePattern = Pattern.compile("\\{([a-zA-Z]+)}");
        Matcher matcher = pathVariablePattern.matcher(component);

        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }

    private static String extractFilenameFromContentDisposition(String contentDispositionLine) {
        for (String part : contentDispositionLine.split(";")) {
            part = part.trim();
            if (part.startsWith("filename=")) {
                return part.split("=")[1].replace("\"", "");
            }
        }
        return null;
    }

    private static String extractBoundary(String contentType) {
        if (contentType == null || !contentType.contains("multipart/form-data")) {
            return null;
        }
        String[] parts = contentType.split(";");
        for (String part : parts) {
            part = part.trim();
            if (part.startsWith("boundary=")) {
                return part.substring("boundary=".length());
            }
        }
        return null;
    }

    private static String extractNameFromContentDisposition(String contentDispositionLine) {
        for (String part : contentDispositionLine.split(";")) {
            part = part.trim();
            if (part.startsWith("name=")) {
                return part.split("=")[1].replace("\"", "");
            }
        }
        return null;
    }

}
