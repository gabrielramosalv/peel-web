package net.peelweb.context.endpoint;

import lombok.RequiredArgsConstructor;
import net.peelweb.enums.HttpContentType;

import java.io.InputStream;

@RequiredArgsConstructor
public class StandardFileEntry implements FileEntry {

    private final String name;

    private final InputStream inputStream;

    private final long size;

    private final HttpContentType httpContentType;

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public long size() {
        return this.size;
    }

    @Override
    public HttpContentType getContentType() {
        return this.httpContentType;
    }

    @Override
    public InputStream getInputStream() {
        return this.inputStream;
    }
}
