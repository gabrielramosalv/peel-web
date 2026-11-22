package net.peelweb.resp;

import lombok.RequiredArgsConstructor;
import net.peelweb.json.JsonConverter;

@RequiredArgsConstructor
public class JsonResponseAssembler implements ResponseAssembler {

    private final JsonConverter converter;

    @Override
    public String assembly(Object obj) {
        return this.converter.toJson(obj);
    }
}
