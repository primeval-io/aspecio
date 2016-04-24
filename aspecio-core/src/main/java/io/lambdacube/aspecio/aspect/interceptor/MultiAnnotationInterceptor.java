package io.lambdacube.aspecio.aspect.interceptor;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public interface MultiAnnotationInterceptor extends Interceptor {
    
    default public Advice onCall(CallContext callContext) {
        Set<Class<? extends Annotation>> intercept = intercept();
        List<Advice> advices = new ArrayList<>(intercept.size());
        for (Class<? extends Annotation> annClass : intercept) {
            for (Annotation ann : callContext.method.getAnnotationsByType(annClass)) {
                Advice advice = onCall(ann, callContext);
                advices.add(advice);
            }
        }
        return Advices.compose(advices);
    };

    <A extends Annotation> Advice onCall(A annotation, CallContext callContext);

    Set<Class<? extends Annotation>> intercept();
    
}
