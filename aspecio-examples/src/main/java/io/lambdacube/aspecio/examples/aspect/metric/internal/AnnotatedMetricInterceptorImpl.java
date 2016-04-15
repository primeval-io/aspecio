package io.lambdacube.aspecio.examples.aspect.metric.internal;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;

import org.osgi.service.component.annotations.Component;
import org.osgi.util.promise.Promise;

import io.lambdacube.aspecio.aspect.Aspect;
import io.lambdacube.aspecio.aspect.interceptor.Advice;
import io.lambdacube.aspecio.aspect.interceptor.AdviceAdapter;
import io.lambdacube.aspecio.aspect.interceptor.AnnotationInterceptor;
import io.lambdacube.aspecio.aspect.interceptor.CallContext;
import io.lambdacube.aspecio.examples.aspect.metric.MetricAspect;
import io.lambdacube.aspecio.examples.aspect.metric.Timed;

@Component
@Aspect(provides = MetricAspect.AnnotatedOnly.class, extraProperties = "measured")
public final class AnnotatedMetricInterceptorImpl implements AnnotationInterceptor {

    private static final Set<Class<? extends Annotation>> ANNOTATIONS = Collections.singleton(Timed.class);

    @Override
    public <A extends Annotation> Advice onCall(A annotation, CallContext callContext) {
        return time((Timed) annotation, callContext);
    }

    @Override
    public Set<Class<? extends Annotation>> intercept() {
        return ANNOTATIONS;
    }

    private Advice time(Timed annotation, CallContext callContext) {
        Stopwatch started = Stopwatch.createStarted();
        String methodName = callContext.target.getName() + "::" + callContext.method.getName();

        boolean async = (callContext.method.getReturnType() == Promise.class);
        return new AdviceAdapter() {
            @Override
            public int afterPhases() {
                return Finally.PHASE + (async ? CallReturn.PHASE : 0);
            }

            @Override
            // We only get there if return type is Promise, so we know
            // other return (primitives) won't be called.
            public <T> T onObjectReturn(T result) {
                // cast is safe because of check before.
                Promise<?> p = (Promise<?>) result;
                p.onResolve(() -> System.out
                        .println("Async call to " + methodName + " took " + started.elapsed(TimeUnit.MICROSECONDS) + " µs"));
                return result;
            }

            @Override
            public void runFinally() {
                System.out.println("Sync call to " + methodName + " took " + started.elapsed(TimeUnit.MICROSECONDS) + " µs");
            }
        };
    }

    @Override
    public String toString() {
        return "MetricsInterceptor:ANNOTATED";
    }
}
