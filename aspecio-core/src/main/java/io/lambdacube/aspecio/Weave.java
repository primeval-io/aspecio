package io.lambdacube.aspecio;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

import io.lambdacube.component.annotation.ComponentProperty;
import io.lambdacube.component.annotation.ComponentPropertyGroup;

@ComponentPropertyGroup
@Target(ElementType.TYPE)
public @interface Weave {
    
    @ComponentProperty(AspecioConstants.SERVICE_ASPECT_WEAVE)
    Class<?>[] required() default {};

    
    @ComponentProperty(AspecioConstants.SERVICE_ASPECT_WEAVE_OPTIONAL)
    Class<?>[] optional() default {};

}
