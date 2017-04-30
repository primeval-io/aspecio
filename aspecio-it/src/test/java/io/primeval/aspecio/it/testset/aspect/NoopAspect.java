package io.lambdacube.aspecio.it.testset.aspect;

import io.lambdacube.aspecio.aspect.interceptor.Advice;
import io.lambdacube.aspecio.aspect.interceptor.CallContext;
import io.lambdacube.aspecio.aspect.interceptor.Interceptor;

public final class NoopAspect implements Interceptor {

    @Override
    public Advice onCall(CallContext callContext) {
        return Advice.DEFAULT;
    }

}
