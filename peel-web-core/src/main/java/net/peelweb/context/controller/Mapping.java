package net.peelweb.context.controller;

import net.peelweb.enums.HttpMethod;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Mapping {

    String value() default "/";

    HttpMethod method() default HttpMethod.GET;

}
