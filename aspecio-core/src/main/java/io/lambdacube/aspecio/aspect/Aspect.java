package io.lambdacube.aspecio.aspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.lambdacube.aspecio.AspecioConstants;
import io.lambdacube.component.annotation.ComponentProperty;
import io.lambdacube.component.annotation.ComponentPropertyGroup;

@ComponentPropertyGroup
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.TYPE_USE })
public @interface Aspect {

    @ComponentProperty(AspecioConstants.SERVICE_ASPECT)
    Class<?>provides();

    @ComponentProperty(AspecioConstants.SERVICE_ASPECT_EXTRAPROPERTIES)
    String[]extraProperties() default {};
}
