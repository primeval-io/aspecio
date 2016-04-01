package io.lambdacube.aspecio.aspect.interceptor;

import java.lang.annotation.Annotation;

import io.lambdacube.aspecio.aspect.CallContext;


public interface Interceptor<A extends Annotation> {
  
    Advice intercept(A annotation, CallContext callContext);

    Class<A> annotation();
}
