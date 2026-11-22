package net.peelweb.context.endpoint;

import net.peelweb.enums.HttpMethod;

public interface Endpoint {

    RequestHandler getHandler();

    HttpMethod getMethod();

    String getMapping();

}
