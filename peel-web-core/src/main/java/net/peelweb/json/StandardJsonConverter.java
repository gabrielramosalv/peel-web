package net.peelweb.json;

import net.peelweb.exception.IncompatibleJsonFormatException;
import net.peelweb.exception.InvalidJsonFormatException;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class StandardJsonConverter implements JsonConverter {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_INSTANT;

    private Object parseValue(String value, Class<?> fieldType) {
        if (fieldType == String.class) {
            return value.replaceAll("^\"|\"$", "").replaceAll("\\\\", "");
        } else if (fieldType == int.class || fieldType == Integer.class) {
            return Integer.parseInt(value);
        } else if (fieldType == long.class || fieldType == Long.class) {
            return Long.parseLong(value);
        } else if (fieldType == double.class || fieldType == Double.class) {
            return Double.parseDouble(value);
        } else if (fieldType == boolean.class || fieldType == Boolean.class) {
            return Boolean.parseBoolean(value);
        } else if (fieldType.isArray()) {
            return parseArray(value, fieldType.getComponentType());
        } else if (List.class.isAssignableFrom(fieldType)) {
            return parseList(value);
        } else {
            return this.fromJson(value, fieldType);
        }
    }

    private Object parseArray(String value, Class<?> componentType) {
        if (!value.startsWith("[") || !value.endsWith("]")) {
            throw new InvalidJsonFormatException("Invalid JSON array format");
        }

        value = value.substring(1, value.length() - 1).trim();
        List<String> elements = splitArrayElements(value);

        Object array = Array.newInstance(componentType, elements.size());
        for (int i = 0; i < elements.size(); i++) {
            Array.set(array, i, parseValue(elements.get(i).trim(), componentType));
        }

        return array;
    }

    private List<Object> parseList(String value) {
        if (!value.startsWith("[") || !value.endsWith("]")) {
            throw new InvalidJsonFormatException("Invalid JSON array format");
        }

        value = value.substring(1, value.length() - 1).trim();
        List<String> elements = splitArrayElements(value);

        List<Object> list = new ArrayList<>();
        for (String element : elements) {
            list.add(parseValue(element.trim(), String.class));
        }

        return list;
    }

    private List<String> splitKeyValuePairs(String json) {
        List<String> pairs = new ArrayList<>();
        int braceLevel = 0;
        int bracketLevel = 0;
        boolean inQuotes = false;
        boolean escapeNext = false;
        StringBuilder currentPair = new StringBuilder();

        for (char c : json.toCharArray()) {
            if (escapeNext) {
                // Adiciona o caractere escapado e ignora seu efeito
                currentPair.append(c);
                escapeNext = false;
                continue;
            }

            if (c == '\\') {
                // Indica que o próximo caractere será escapado
                escapeNext = true;
                currentPair.append(c);
                continue;
            }

            if (c == '"') {
                // Alterna o estado de `inQuotes` apenas se não estiver escapado
                inQuotes = !inQuotes;
            }

            if (!inQuotes) {
                switch (c) {
                    case '{':
                        braceLevel++;
                        break;
                    case '}':
                        braceLevel--;
                        break;
                    case '[':
                        bracketLevel++;
                        break;
                    case ']':
                        bracketLevel--;
                        break;
                    case ',':
                        // Apenas adiciona o par se fora de chaves, colchetes e strings
                        if (braceLevel == 0 && bracketLevel == 0) {
                            pairs.add(currentPair.toString().trim());
                            currentPair.setLength(0);
                            continue;
                        }
                        break;
                }
            }

            currentPair.append(c);
        }

        if (!currentPair.toString().isEmpty()) {
            pairs.add(currentPair.toString().trim());
        }

        return pairs;
    }

    private List<String> splitArrayElements(String array) {
        List<String> elements = new ArrayList<>();
        int bracketLevel = 0;
        boolean inQuotes = false;
        boolean escapeNext = false;
        StringBuilder currentElement = new StringBuilder();

        for (char c : array.toCharArray()) {
            if (escapeNext) {
                // Adiciona o caractere escapado e ignora seu efeito
                currentElement.append(c);
                escapeNext = false;
                continue;
            }

            if (c == '\\') {
                // Indica que o próximo caractere será escapado
                escapeNext = true;
                currentElement.append(c);
                continue;
            }

            if (c == '"') {
                // Alterna o estado de `inQuotes` apenas se não estiver escapado
                inQuotes = !inQuotes;
            }

            if (!inQuotes) {
                if (c == ',' && bracketLevel == 0) {
                    // Adiciona o elemento atual e inicia um novo
                    elements.add(currentElement.toString().trim());
                    currentElement.setLength(0);
                    continue;
                } else if (c == '[') {
                    bracketLevel++;
                } else if (c == ']') {
                    bracketLevel--;
                }
            }

            // Adiciona o caractere ao elemento atual
            currentElement.append(c);
        }

        if (!currentElement.toString().isEmpty()) {
            // Adiciona o último elemento restante
            elements.add(currentElement.toString().trim());
        }

        return elements;
    }

    private static final java.util.concurrent.ConcurrentMap<Class<?>, Field[]> FIELD_CACHE = new java.util.concurrent.ConcurrentHashMap<>();

    /** Instance fields to serialize: no static, transient or synthetic fields; superclass fields after the class's own. */
    private static Field[] fieldsOf(Class<?> type) {
        return FIELD_CACHE.computeIfAbsent(type, t -> {
            List<Field> fields = new ArrayList<>();
            Class<?> current = t;
            while (current != null && current != Object.class && current != Record.class) {
                for (Field field : current.getDeclaredFields()) {
                    int mod = field.getModifiers();
                    if (java.lang.reflect.Modifier.isStatic(mod) || java.lang.reflect.Modifier.isTransient(mod)
                            || field.isSynthetic()) {
                        continue;
                    }
                    field.setAccessible(true);
                    fields.add(field);
                }
                current = current.getSuperclass();
            }
            return fields.toArray(new Field[0]);
        });
    }

    private void serializeObject(Object obj, StringBuilder json) {
        json.append('{');
        boolean firstField = true;
        for (Field field : fieldsOf(obj.getClass())) {
            Object value;
            try {
                value = field.get(obj);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            if (!firstField) {
                json.append(',');
            }
            json.append('"');
            escapeString(field.getName(), json);
            json.append("\":");
            serializeValue(value, json);
            firstField = false;
        }
        json.append('}');
    }

    private void serializeArray(Object array, StringBuilder json) {
        json.append('[');
        int length = Array.getLength(array);
        for (int i = 0; i < length; i++) {
            if (i > 0) {
                json.append(',');
            }
            serializeValue(Array.get(array, i), json);
        }
        json.append(']');
    }

    private void serializeIterable(Iterable<?> iterable, StringBuilder json) {
        json.append('[');
        boolean first = true;
        for (Object o : iterable) {
            if (!first) {
                json.append(',');
            }
            serializeValue(o, json);
            first = false;
        }
        json.append(']');
    }

    private void serializeMap(Map<?, ?> map, StringBuilder json) {
        json.append('{');
        boolean firstEntry = true;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (!firstEntry) {
                json.append(',');
            }
            json.append('"');
            escapeString(String.valueOf(entry.getKey()), json);
            json.append("\":");
            serializeValue(entry.getValue(), json);
            firstEntry = false;
        }
        json.append('}');
    }

    private void serializeValue(Object value, StringBuilder json) {
        if (value == null) {
            json.append("null");
        } else if (value instanceof CharSequence || value instanceof Character) {
            json.append('"');
            escapeString(value.toString(), json);
            json.append('"');
        } else if (value instanceof Double d && (d.isNaN() || d.isInfinite())) {
            json.append("null");
        } else if (value instanceof Float f && (f.isNaN() || f.isInfinite())) {
            json.append("null");
        } else if (value instanceof Number || value instanceof Boolean) {
            json.append(value);
        } else if (value instanceof Enum<?> e) {
            json.append('"');
            escapeString(e.name(), json);
            json.append('"');
        } else if (value instanceof Iterable<?> it) {
            serializeIterable(it, json);
        } else if (value instanceof Map<?, ?> m) {
            serializeMap(m, json);
        } else if (value instanceof java.util.Optional<?> o) {
            serializeValue(o.orElse(null), json);
        } else if (value.getClass().isArray()) {
            serializeArray(value, json);
        } else if (value instanceof Date d) {
            json.append('"').append(DATE_FORMAT.format(d.toInstant())).append('"');
        } else if (value instanceof java.time.temporal.TemporalAccessor) {
            json.append('"');
            escapeString(value.toString(), json);
            json.append('"');
        } else {
            serializeObject(value, json);
        }
    }

    /** Escapes a string as RFC 8259 asks: quote, backslash and every control character. */
    private static void escapeString(String str, StringBuilder json) {
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            switch (c) {
                case '"' -> json.append("\\\"");
                case '\\' -> json.append("\\\\");
                case '\n' -> json.append("\\n");
                case '\r' -> json.append("\\r");
                case '\t' -> json.append("\\t");
                case '\b' -> json.append("\\b");
                case '\f' -> json.append("\\f");
                default -> {
                    if (c < 0x20) {
                        json.append(String.format("\\u%04x", (int) c));
                    } else {
                        json.append(c);
                    }
                }
            }
        }
    }

    @Override
    public String toJson(Object o) {
        StringBuilder json = new StringBuilder(256);
        serializeValue(o, json);
        return json.toString();
    }

    @Override
    public <T> T fromJson(String json, Class<T> clazz) {
        T instance;
        try {
            instance = clazz.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        json = json.trim();
        if (!json.startsWith("{") || !json.endsWith("}")) {
            throw new InvalidJsonFormatException("Invalid JSON format");
        }

        json = json.substring(1, json.length() - 1).trim();

        List<String> keyValuePairs = splitKeyValuePairs(json);

        for (String pair : keyValuePairs) {
            String[] keyValue = pair.split(":", 2);
            if (keyValue.length != 2) {
                continue;
            }

            String key = keyValue[0].trim().replaceAll("^\"|\"$", "");
            String value = keyValue[1].trim();

            Field field;
            try {
                field = clazz.getDeclaredField(key);
            } catch (NoSuchFieldException e) {
                throw new IncompatibleJsonFormatException();
            }

            field.setAccessible(true);
            Object parsedValue = parseValue(value, field.getType());
            try {
                field.set(instance, parsedValue);
            } catch (IllegalArgumentException e) {
                throw new IncompatibleJsonFormatException();
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        return instance;
    }

}
