package io.lambdacube.aspecio.examples.aspect.counting.internal;

import java.lang.reflect.Method;
import java.util.Map;

import com.google.common.collect.Maps;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;

import io.lambdacube.aspecio.aspect.Aspect;
import io.lambdacube.aspecio.aspect.interceptor.Advice;
import io.lambdacube.aspecio.aspect.interceptor.AdviceAdapter;
import io.lambdacube.aspecio.aspect.interceptor.CallContext;
import io.lambdacube.aspecio.aspect.interceptor.Interceptor;
import io.lambdacube.aspecio.examples.aspect.counting.CountingAspect;

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
    public Advice onCall(CallContext callContext) {
        if (countOnlySuccessful) {
            return new AdviceAdapter() {
                @Override
                public int afterPhases() {
                    return CallReturn.PHASE;
                }

                @Override
                public void onSuccessfulReturn() {
                    methodCallCount.compute(callContext.method, (k, v) -> v == null ? 1 : (v += 1));
                }
            };
        } else {
            methodCallCount.compute(callContext.method, (k, v) -> v == null ? 1 : (v += 1));
            return Advice.DEFAULT;
        }
    }

    @Override
    public void printCounts() {
        methodCallCount.forEach((m, count) -> System.out.println(m.getDeclaringClass().getName() + "::" + m.getName() + " -> " + count));
    }

}
