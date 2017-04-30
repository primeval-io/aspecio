package io.primeval.aspecio.aspect.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

import io.primeval.aspecio.AspecioConstants;
import io.primeval.component.annotation.properties.ComponentProperty;
import io.primeval.component.annotation.properties.ComponentPropertyGroup;

/**
 * Use this property annotation, along with Bnd' Declarative Services properties annotation plugin, to request Aspects
 * to be woven for this {@literal @Component}.<br>
 */
@ComponentPropertyGroup
@Target(ElementType.TYPE)
public @interface Weave {

    /**
     * The required aspects to weave. The woven service will not be published unless all of the required aspects are
     * present. <br>
     * The unregistration of any required aspect will also cause the woven service to be unregistered.
     * 
     * @return the required aspects.
     */
    @ComponentProperty(AspecioConstants.SERVICE_ASPECT_WEAVE)
    Class<?>[]required() default {};

    /**
     * The optional aspects to weave. The woven service will be published even if the optional aspects are absent. <br>
     * The registration of an optional aspect will allow these aspects to intercept the service methods, even if the
     * service was previously published without that aspect.
     * 
     * @return the optional aspects.
     */
    @ComponentProperty(AspecioConstants.SERVICE_ASPECT_WEAVE_OPTIONAL)
    Class<?>[]optional() default {};

}
