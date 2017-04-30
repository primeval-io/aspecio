package io.primeval.aspecio.aspect.interceptor;

/**
 * An Action in the Before phase.
 * 
 * @see Advice
 */
public enum BeforeAction {
    // Order matters.
    /**
     * <p>
     * Skip the method call and return a value.
     * </p>
     * 
     * @see Advice
     * @see Advice.SkipCall
     */
    SKIP_AND_RETURN,

    /**
     * <p>
     * Request the intercepted method call's arguments.
     * </p>
     * 
     * @see Advice
     * @see Advice.ArgumentHook
     */
    REQUEST_ARGUMENTS,

    /**
     * <p>
     * Request a chance to update the method call's arguments, then proceed to the method call.
     * </p>
     * 
     * @see Advice
     * @see Advice.ArgumentHook
     */
    UPDATE_ARGUMENTS_AND_PROCEED,

    /**
     * <p>
     * Proceed to the intercepted method call.
     * </p>
     * 
     * @see Advice
     */
    PROCEED
}