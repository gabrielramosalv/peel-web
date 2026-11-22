package net.peelweb.json;

public class JsonConverterManager {

    private static final JsonConverterManager INSTANCE = new JsonConverterManager();

    private volatile JsonConverter converter;

    private JsonConverterManager() {
        this.converter = new StandardJsonConverter();
    }

    public static JsonConverterManager getInstance() {
        return INSTANCE;
    }

    public JsonConverter getConverter() {
        return this.converter;
    }

    /** Replaces the converter (for example, a faster one written for the application's types). */
    public void setConverter(JsonConverter converter) {
        this.converter = converter == null ? new StandardJsonConverter() : converter;
    }

}
