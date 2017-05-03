package io.primeval.aspecio.examples.aspect.counting.internal;

import java.lang.reflect.Method;
import java.util.Map;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;

import com.google.common.collect.Maps;

import io.primeval.aspecio.aspect.annotations.Aspect;
import io.primeval.aspecio.examples.aspect.counting.CountingAspect;
import io.primeval.reflect.proxy.CallContext;
import io.primeval.reflect.proxy.Interceptor;
import io.primeval.reflect.proxy.handler.InterceptionHandler;

@Component
@Aspect(provides = CountingAspect.class)
public final class CountingAspectImpl implements Interceptor, CountingAspect {

    private final Map<Method, Integer> methodCallCount = Maps.newConcurrentMap();

    private volatile boolean countOnlySuccessful = false;

    @Activate
    public void activate(CountAspectConfig config) {
        countOnlySuccessful = config.countOnlySuccessful();
    }

    @Deactivate
    public void deactivate() {
        methodCallCount.clear();
    }

    @Modified
    public void modified(CountAspectConfig config) {
        countOnlySuccessful = config.countOnlySuccessful();
    }

    @Override
    public <T, E extends Throwable> T onCall(CallContext context, InterceptionHandler<T> handler) throws E {
        if (countOnlySuccessful) {
            T res = handler.invoke();
            methodCallCount.compute(context.method, (k, v) -> v == null ? 1 : (v += 1));
            return res;
        } else {
            methodCallCount.compute(context.method, (k, v) -> v == null ? 1 : (v += 1));
            return handler.invoke();
        }
    }

    @Override
    public void printCounts() {
        methodCallCount.forEach((m, count) -> System.out
                .println(m.getDeclaringClass().getName() + "::" + m.getName() + " -> " + count));
    }

    @Override
    public String toString() {
        return "Counting";
    }

}
