package io.primeval.aspecio.examples.aspect.metric.internal;

import java.util.concurrent.TimeUnit;

import org.osgi.service.component.annotations.Component;
import org.osgi.util.promise.Promise;

import com.google.common.base.Stopwatch;

import io.primeval.aspecio.aspect.annotations.Aspect;
import io.primeval.aspecio.examples.aspect.metric.MetricAspect;
import io.primeval.reflex.proxy.CallContext;
import io.primeval.reflex.proxy.Interceptor;
import io.primeval.reflex.proxy.handler.InterceptionHandler;

@Component
@Aspect(provides = MetricAspect.All.class, extraProperties = "measured")
public final class AllMetricInterceptorImpl implements Interceptor {

    @Override
    public <T, E extends Throwable> T onCall(CallContext callContext, InterceptionHandler<T> handler) throws E {
        Stopwatch started = Stopwatch.createStarted();
        String methodName = callContext.target.getName() + "::" + callContext.method.getName();

        boolean async = (callContext.method.getReturnType() == Promise.class);

        try {
            T result = handler.invoke();
            if (async) {
                Promise<?> p = (Promise<?>) result;
                p.onResolve(() -> System.out
                        .println("Async call to " + methodName + " took " + started.elapsed(TimeUnit.MICROSECONDS)
                                + " µs"));
            }
            return result;
        } finally {
            System.out.println(
                    "Sync call to " + methodName + " took " + started.elapsed(TimeUnit.MICROSECONDS) + " µs");
        }
    }

    @Override
    public String toString() {
        return "MetricsInterceptor:ALL";
    }
}
