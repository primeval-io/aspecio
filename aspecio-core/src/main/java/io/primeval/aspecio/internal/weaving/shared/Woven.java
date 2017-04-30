package io.primeval.aspecio.internal.weaving.shared;

import io.primeval.aspecio.aspect.interceptor.Interceptor;

public abstract class Woven {

    protected volatile Interceptor interceptor = Interceptor.NOOP;

    public void setInterceptor(Interceptor interceptor) {
        this.interceptor = interceptor;
    }

}
