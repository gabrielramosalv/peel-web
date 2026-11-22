package net.peelweb.resp;

import lombok.RequiredArgsConstructor;
import net.peelweb.enums.HttpContentType;
import net.peelweb.json.JsonConverterManager;

@RequiredArgsConstructor
public class ResponseAssemblerFactory {

    private final String staticContentPath;

    private final boolean isExternalStaticContent;

    public ResponseAssembler get(HttpContentType httpContentType) {
        if (httpContentType.equals(HttpContentType.JSON)) {
            return new JsonResponseAssembler(JsonConverterManager.getInstance().getConverter());
        } else {
            if (this.isExternalStaticContent) {
                return new StaticContentAssembler(this.staticContentPath);
            }
            return new ClasspathContentAssembler(this.staticContentPath);
        }
    }

}
