package io.lambdacube.aspecio.aspect.interceptor;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collections;
import java.util.List;

public final class CallContext {
    public final Class<?> target;
    public final Method method;
    public final List<Parameter> parameters;

    public CallContext(Class<?> target, Method method, List<Parameter> parameters) {
        super();
        this.target = target;
        this.method = method;
        this.parameters = Collections.unmodifiableList(parameters);
    }
}
