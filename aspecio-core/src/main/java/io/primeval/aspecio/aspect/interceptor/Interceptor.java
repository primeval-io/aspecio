package io.primeval.aspecio.aspect.interceptor;

import io.primeval.aspecio.AspecioConstants;

/**
 * <p>
 * Interceptors <i>intercept</i> method calls for a certain Aspect.
 * </p>
 * <p>
 * To publish an Aspect, you must register an OSGi service implementing {@link Interceptor} (or one of its derivatives)
 * with the String property {@link AspecioConstants#SERVICE_ASPECT} containing the name of the Aspect. Optionally, it is
 * possible to define the property {@link AspecioConstants#SERVICE_ASPECT_EXTRAPROPERTIES} to define extra Boolean
 * service properties that will be registered with services woven with that Aspect.
 * </p>
 * <p>
 * Interceptors can intercept any <b>public</b> method the service has, as defined by {@link Class#getMethods()}.
 * </p>
 * 
 *
 */
public interface Interceptor {

    /**
     * The default Interceptor. It doesn't alter the method execution.
     */
    public final static Interceptor NOOP = new Interceptor() {
        @Override
        public Advice onCall(CallContext callContext) {
            return Advice.DEFAULT;
        }

        public String toString() {
            return "NOOP Interceptor";
        };
    };

    /**
     * Callback when a method call is intercepted. <br>
     * You should return a non-null {@link Advice}. After returning from this method, Aspecio will call
     * {@link Advice#initialAction()} to determine the next action it has to take.
     * 
     * @param callContext
     *            The context of that method call.
     * @return The advice to execute.
     */
    Advice onCall(CallContext callContext);

}
