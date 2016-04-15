package io.lambdacube.aspecio.examples.greetings.internal;

import io.lambdacube.aspecio.aspect.interceptor.Interceptor;

public abstract class Woven {

    protected volatile Interceptor interceptor;

    public void setInterceptor(Interceptor interceptor) {
        this.interceptor = interceptor;
    }

}
