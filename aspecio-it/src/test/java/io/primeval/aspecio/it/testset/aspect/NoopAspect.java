package io.primeval.aspecio.it.testset.aspect;

import io.primeval.aspecio.aspect.interceptor.Advice;
import io.primeval.aspecio.aspect.interceptor.CallContext;
import io.primeval.aspecio.aspect.interceptor.Interceptor;

public final class NoopAspect implements Interceptor {

    @Override
    public Advice onCall(CallContext callContext) {
        return Advice.DEFAULT;
    }

}
