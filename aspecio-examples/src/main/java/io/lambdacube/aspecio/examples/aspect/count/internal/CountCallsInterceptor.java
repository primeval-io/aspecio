package io.lambdacube.aspecio.examples.aspect.count.internal;

import java.lang.reflect.Method;
import java.util.Map;

import com.google.common.collect.Maps;

import org.osgi.service.component.annotations.Component;

import io.lambdacube.aspecio.aspect.Aspect;
import io.lambdacube.aspecio.aspect.CallContext;
import io.lambdacube.aspecio.aspect.interceptor.Advice;
import io.lambdacube.aspecio.aspect.interceptor.Interceptor;
import io.lambdacube.aspecio.examples.aspect.count.CountAspect;
import io.lambdacube.aspecio.examples.aspect.count.CountCalls;

@Component
@Aspect(CountAspect.class)
public final class CountCallsInterceptor implements Interceptor<CountCalls> {

    private final Map<Method, Integer> methodCallCount = Maps.newConcurrentMap();

    @Override
    public Advice intercept(CountCalls annotation, CallContext callContext) {
        methodCallCount.compute(callContext.method, (k, v) -> v == null ? 1 : v++);
        return Advice.NOOP;
    }

    @Override
    public Class<CountCalls> annotation() {
        return CountCalls.class;
    }

}
