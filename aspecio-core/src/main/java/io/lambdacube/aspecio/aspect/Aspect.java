package io.lambdacube.aspecio.aspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.lambdacube.component.annotation.ComponentProperty;
import io.lambdacube.component.annotation.ComponentPropertyGroup;

@ComponentPropertyGroup
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.TYPE_USE })
public @interface Aspect {

    @ComponentProperty("service.aspect")
    Class<?>provides();

    @ComponentProperty("service.aspect.extraProperties")
    String[]extraProperties() default {};
}
