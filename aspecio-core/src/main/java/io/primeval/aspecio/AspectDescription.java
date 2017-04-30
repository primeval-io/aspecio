package io.primeval.aspecio;

import java.util.List;

/**
 * A class describing an Aspect and the interceptors providing it.
 */
public final class AspectDescription {

    /**
     * The aspect name.
     */
    public final String aspectName;

    /**
     * The currently chosen interceptor for that aspect
     */
    public final InterceptorDescription interceptor;

    /**
     * The sorted list of interceptors that will replace the current interceptor if the chosen interceptor is
     * unregistered.<br>
     * Interceptors are chosen using by comparing their ServiceReference, so a higher service ranking is preferred, and
     * in the case of equal service rankings a lower service id will be chosen.
     */
    public final List<InterceptorDescription> backupInterceptors;

    public AspectDescription(String aspectName, InterceptorDescription interceptor, List<InterceptorDescription> backupInterceptors) {
        super();
        this.aspectName = aspectName;
        this.interceptor = interceptor;
        this.backupInterceptors = backupInterceptors;
    }

}
