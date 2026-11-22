package net.peelweb.json;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Testes da conversão JSON padrão (StandardJsonConverter)")
class StandardJsonConverterTest {

    private final StandardJsonConverter converter = new StandardJsonConverter();

    @Test
    @DisplayName("Deve serializar mapas preservando chaves, valores e nulos")
    void shouldSerializeMapIncludingNullValues() {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("name", "Peel Web");
        values.put("description", null);

        String json = this.converter.toJson(values);

        assertEquals("{\"name\":\"Peel Web\",\"description\":null}", json);
    }

    @Test
    @DisplayName("Deve serializar campos nulos de objetos")
    void shouldSerializeObjectIncludingNullFields() {
        ExamplePayload payload = new ExamplePayload("Peel Web", null);

        String json = this.converter.toJson(payload);

        assertEquals("{\"name\":\"Peel Web\",\"description\":null}", json);
    }

    @Test
    @DisplayName("Deve serializar records e enums pelo nome")
    void shouldSerializeRecordAndEnum() {
        assertEquals("{\"kind\":\"BELIEF\",\"values\":[1,2]}",
                this.converter.toJson(new Item(Kind.BELIEF, List.of(1, 2))));
    }

    @Test
    @DisplayName("Deve ignorar campos estáticos e transientes")
    void shouldSkipStaticAndTransientFields() {
        assertEquals("{\"kept\":1}", this.converter.toJson(new WithSkipped()));
    }

    @Test
    @DisplayName("Deve escapar caracteres de controle, aspas e barras")
    void shouldEscapeStrings() {
        assertEquals("\"a\\\\b \\\"q\\\" \\t \\u0001\"", this.converter.toJson("a\\b \"q\" \t \u0001"));
    }

    @Test
    @DisplayName("Deve serializar NaN como null")
    void shouldSerializeNaNAsNull() {
        assertEquals("[null,1.5]", this.converter.toJson(List.of(Double.NaN, 1.5)));
    }

    private enum Kind { BELIEF }

    private record Item(Kind kind, List<Integer> values) {}

    @SuppressWarnings("unused")
    private static final class WithSkipped {
        private static final int IGNORED = 2;
        private transient int alsoIgnored = 3;
        private final int kept = 1;
    }

    private static final class ExamplePayload {

        private final String name;

        private final String description;

        private ExamplePayload(String name, String description) {
            this.name = name;
            this.description = description;
        }
    }
}
