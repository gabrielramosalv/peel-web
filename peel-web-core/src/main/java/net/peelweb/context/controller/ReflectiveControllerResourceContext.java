package net.peelweb.context.controller;

import net.peelweb.context.ResourceContext;
import net.peelweb.context.endpoint.Endpoint;
import net.peelweb.context.endpoint.StandardRequest;
import net.peelweb.context.endpoint.Response;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static net.peelweb.context.endpoint.Endpoints.mount;

public class ReflectiveControllerResourceContext implements ResourceContext {

    private final Object controller;

    private final List<Endpoint> endpoints;

    private String mapping;

    public ReflectiveControllerResourceContext(Object controller) {
        this.controller = controller;
        this.mapping = "";
        this.endpoints = new ArrayList<>();
        this.load();
    }

    private void load() {
        Controller controllerAnnotation = this.controller.getClass().getAnnotation(Controller.class);

        if (controllerAnnotation == null) {
            throw new RuntimeException(String.format("Controller annotation is not defined on %s rest controller",
                    this.getClass().getName()));
        }

        this.mapping = controllerAnnotation.value();

        for (Method method : this.controller.getClass().getMethods()) {
            Mapping mappingAnnotation = method.getAnnotation(Mapping.class);
            if (mappingAnnotation == null) {
                continue;
            }

            if (!method.getReturnType().equals(Response.class)) {
                throw new RuntimeException(
                        String.format("Method %s from controller %s does not return %s type", method.getName(),
                                this.getClass().getName(), method.getReturnType()));
            }

            method.setAccessible(true);

            Class<?>[] parameterTypes = method.getParameterTypes();

            boolean hasNoneParameters;
            if (parameterTypes.length > 1) {
                throw new RuntimeException(
                        String.format("Method " + method.getName() + " must have just %s parameter or none",
                                StandardRequest.class.getName()));
            } else {
                hasNoneParameters = parameterTypes.length == 0;
            }

            endpoints.add(mount(mappingAnnotation.method(), mappingAnnotation.value(), request -> {
                try {
                    if (hasNoneParameters) {
                        return (Response) method.invoke(this.controller);
                    } else {
                        return (Response) method.invoke(this.controller, request);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }));
        }
    }

    @Override
    public List<Endpoint> getEndpoints() {
        return endpoints;
    }

    @Override
    public String getBaseMapping() {
        return this.mapping;
    }
}
