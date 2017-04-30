package io.lambdacube.aspecio.aspect.interceptor;

import java.lang.annotation.Annotation;
import java.lang.annotation.Repeatable;
import java.util.ArrayList;
import java.util.List;

import io.lambdacube.aspecio.aspect.interceptor.composite.Advices;

/**
 * <p>
 * An {@link Interceptor} that only intercepts methods that have a specific runtime annotation {@literal A}.
 * </p>
 * <p>
 * This interface make it easy to intercept annotated methods in a typesafe way.
 * </p>
 * <p>
 * If the annotation {@literal A} is {@link Repeatable}, then the Advice will be called for each instance of that
 * annotation on the intercepted method.
 * </p>
 * 
 * @param <A>
 *            the runtime annotation to intercept.
 */
public interface AnnotationInterceptor<A extends Annotation> extends Interceptor {

    default public Advice onCall(CallContext callContext) {
        Class<A> annClass = intercept();
        // there might be several advices in case of repeatable annotations.
        List<Advice> advices = new ArrayList<>(1);
        for (A ann : callContext.method.getAnnotationsByType(annClass)) {
            Advice advice = onCall(ann, callContext);
            advices.add(advice);
        }
        return Advices.compose(advices);
    };

    Advice onCall(A annotation, CallContext callContext);

    Class<A> intercept();

}
