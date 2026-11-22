package net.peelweb.context.controller;

import net.peelweb.context.ResourceContext;

public class Controllers {

    public static ResourceContext from(Object controller) {
        return new ReflectiveControllerResourceContext(controller);
    }

}
