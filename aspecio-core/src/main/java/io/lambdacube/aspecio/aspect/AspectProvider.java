package io.lambdacube.aspecio.aspect;

import io.lambdacube.aspecio.aspect.interceptor.Interceptor;

public interface AspectProvider {

    Aspect aspect();

    Interceptor<?>[] advices();
}
