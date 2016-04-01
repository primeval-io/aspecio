package io.lambdacube.aspecio;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

import io.lambdacube.component.annotation.ComponentProperty;

@ComponentProperty(AspecioConstants.SERVICE_ASPECT_WEAVE)
@Target(ElementType.TYPE)
public @interface Weave {
    Class<? extends Annotation>[] value();
}
