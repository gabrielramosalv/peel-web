package net.peelweb.context.endpoint;

import lombok.Getter;
import net.peelweb.enums.HttpMethod;
import net.peelweb.exception.InvalidJsonFormatException;
import net.peelweb.json.JsonConverterManager;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class StandardRequest implements Request {

    @Getter
    private final HttpMethod method;

    @Getter
    private final String uri;

    private final Object body;

    private final Map<String, String> queryParameters;

    private final Map<String, Object> formData;

    private Map<String, String> pathVariables;

    private final Map<String, String> headers;

    public StandardRequest(HttpMethod method, String uri, Object body, Map<String, String> queryParameters,
                           Map<String, Object> formData) {
        this(method, uri, body, queryParameters, formData, Map.of());
    }

    public StandardRequest(HttpMethod method, String uri, Object body, Map<String, String> queryParameters,
                           Map<String, Object> formData, Map<String, String> headers) {
        this.method = method;
        this.uri = uri;
        this.body = body;
        this.queryParameters = queryParameters;
        this.formData = formData;
        this.pathVariables = new HashMap<>();
        this.headers = new HashMap<>();
        headers.forEach((k, v) -> this.headers.put(k.toLowerCase(java.util.Locale.ROOT), v));
    }

    @Override
    public String getHeader(String name) {
        return name == null ? null : this.headers.get(name.toLowerCase(java.util.Locale.ROOT));
    }

    public boolean parametersIsEmpty() {
        return this.queryParameters.isEmpty();
    }

    protected void setPathVariables(Map<String, String> pathVariables) {
        this.pathVariables = pathVariables;
    }

    @Override
    public <T> T getBodyAs(Class<T> tClass) {
        if (this.body == null) {
            return null;
        }
        try {
            if (this.body instanceof String) {
                return JsonConverterManager.getInstance().getConverter().fromJson(this.body.toString(), tClass);
            }

            throw new RuntimeException();
        } catch (InvalidJsonFormatException e) {
            return null;
        }
    }

    @Override
    public String getParameter(String name) {
        if (this.parametersIsEmpty()) {
            return null;
        }
        return this.queryParameters.get(name);
    }

    @Override
    public Integer getParameterAsInteger(String name) {
        String parameter = this.getParameter(name);
        if (parameter != null) {
            return Integer.parseInt(parameter);
        }
        return null;
    }

    @Override
    public <T> T getParameterAs(String name, Function<String, T> converter) {
        String parameter = this.getParameter(name);
        if (parameter == null) {
            return null;
        }
        try {
            return converter.apply(parameter);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String getPathVariable(String name) {
        return this.pathVariables.get(name);
    }

    @Override
    public Integer getPathVariableAsInteger(String name) {
        if (this.pathVariables.isEmpty()) {
            return null;
        }
        String pathVariable = this.getPathVariable(name);
        if (pathVariable != null) {
            return Integer.parseInt(pathVariable);
        }
        return null;
    }

    @Override
    public <T> T getPathVariableAs(String name, Function<String, T> converter) {
        if (this.pathVariables.isEmpty()) {
            return null;
        }
        try {
            return converter.apply(this.pathVariables.get(name));
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public FileEntry getFileEntry(String name) {
        if (this.formData.isEmpty()) {
            return null;
        }
        Object o = this.formData.get(name);
        if (!(o instanceof FileEntry)) {
            return null;
        }
        return (FileEntry) o;
    }

}
