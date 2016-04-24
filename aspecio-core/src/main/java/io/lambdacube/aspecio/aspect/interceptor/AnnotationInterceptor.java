package io.lambdacube.aspecio.aspect.interceptor;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

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
