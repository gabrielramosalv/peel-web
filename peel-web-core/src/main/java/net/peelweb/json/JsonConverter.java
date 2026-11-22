package net.peelweb.json;

public interface JsonConverter {

    String toJson(Object o);

    <T> T fromJson(String json, Class<T> clazz);

}
