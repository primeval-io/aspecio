package io.lambdacube.aspecio.internal.weaving;

import io.lambdacube.aspecio.aspect.interceptor.Interceptor;

public abstract class Woven {

    protected volatile Interceptor interceptor = Interceptor.NOOP;

    public void setInterceptor(Interceptor interceptor) {
        this.interceptor = interceptor;
    }

}
