package io.primeval.aspecio.aspect.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.lambdacube.component.annotation.ComponentProperty;
import io.lambdacube.component.annotation.ComponentPropertyGroup;
import io.primeval.aspecio.AspecioConstants;

/**
 * Use this property annotation, along with Bnd' Declarative Services properties annotation plugin, to define Aspects.
 * Your component class must be assignable to Interceptor to be recognized as an Aspect.
 */
@ComponentPropertyGroup
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.TYPE_USE })
public @interface Aspect {

    /**
     * The name of the aspect to provide. A class is used here to piggyback on Java's namespacing and avoid conflitcs in
     * aspect names.
     * @return The aspect class to provide
     */
    @ComponentProperty(AspecioConstants.SERVICE_ASPECT)
    Class<?>provides();

    /**
     * The extra properties that will be published, with value {@link Boolean#TRUE} to services woven with this aspect.
     * Defaults to the empty array.
     * @return the extra properties
     */
    @ComponentProperty(AspecioConstants.SERVICE_ASPECT_EXTRAPROPERTIES)
    String[]extraProperties() default {};
}
