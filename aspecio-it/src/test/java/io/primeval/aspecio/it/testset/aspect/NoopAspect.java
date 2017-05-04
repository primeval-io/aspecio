package io.primeval.aspecio.it.testset.aspect;

import io.primeval.reflex.proxy.CallContext;
import io.primeval.reflex.proxy.Interceptor;
import io.primeval.reflex.proxy.handler.InterceptionHandler;

public final class NoopAspect implements Interceptor {

    @Override
    public <T, E extends Throwable> T onCall(CallContext context, InterceptionHandler<T> handler) throws E {
        return handler.invoke();
    }
    
}
