package io.lambdacube.aspecio.aspect;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public final class CallContext {
    public final Class<?> target;
    public final Method method;
    public final Parameter[] parameters;

    public CallContext(Class<?> target, Method method, Parameter[] parameters) {
        super();
        this.target = target;
        this.method = method;
        this.parameters = parameters;
    }
}
