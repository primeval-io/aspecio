package io.primeval.aspecio.it.testset.aspect;

import io.primeval.reflect.proxy.CallContext;
import io.primeval.reflect.proxy.Interceptor;
import io.primeval.reflect.proxy.handler.InterceptionHandler;

public final class NoopAspect implements Interceptor {

    @Override
    public <T, E extends Throwable> T onCall(CallContext context, InterceptionHandler<T> handler) throws E {
        return handler.invoke();
    }
    
}
