package net.peelweb.context;

import net.peelweb.context.endpoint.Endpoint;

import java.util.List;

public interface ResourceContext {

    List<Endpoint> getEndpoints();

    String getBaseMapping();

}
