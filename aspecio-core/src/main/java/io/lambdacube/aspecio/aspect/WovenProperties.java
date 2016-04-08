package io.lambdacube.aspecio.aspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.lambdacube.component.annotation.ComponentProperty;

@ComponentProperty("service.aspect.extraproperties")
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface WovenProperties {
    String value();
}
