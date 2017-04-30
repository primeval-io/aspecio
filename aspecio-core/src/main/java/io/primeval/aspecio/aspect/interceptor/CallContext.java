package io.lambdacube.aspecio.aspect.interceptor;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * The call context for an interception.
 * </p>
 * <p>
 * It provides information about the class and method being intercepted, as well as the method parameters (cached and
 * thus cheaper than calling {@link Method#getParameters()} each time).
 * </p>
 * 
 * <p>
 * It is <b>immutable</b> and thus <b>thread-safe</b>.
 * </p>
 *
 */
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
