package net.peelweb.context.endpoint;

import net.peelweb.enums.HttpMethod;

import java.util.function.Function;

public interface Request {

    <T> T getBodyAs(Class<T> tClass);

    String getParameter(String name);

    Integer getParameterAsInteger(String name);

    <T> T getParameterAs(String name, Function<String, T> converter);

    String getPathVariable(String name);

    Integer getPathVariableAsInteger(String name);

    <T> T getPathVariableAs(String name, Function<String, T> converter);

    FileEntry getFileEntry(String name);

    HttpMethod getMethod();

    String getUri();

    /** Value of a request header (case-insensitive name), or null. */
    String getHeader(String name);

}
