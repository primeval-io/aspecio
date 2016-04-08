package io.lambdacube.aspecio.aspect.interceptor;

public interface Interceptor {

    Advice onCall(CallContext callContext);
    
}
