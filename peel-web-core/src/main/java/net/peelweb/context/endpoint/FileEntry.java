package net.peelweb.context.endpoint;

import net.peelweb.enums.HttpContentType;

import java.io.InputStream;

public interface FileEntry {

    String getName();

    long size();

    HttpContentType getContentType();

    InputStream getInputStream();

}
