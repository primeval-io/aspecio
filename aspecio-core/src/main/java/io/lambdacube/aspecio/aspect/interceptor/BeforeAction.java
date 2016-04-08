package io.lambdacube.aspecio.aspect.interceptor;



public enum BeforeAction {
    // Order matters.
    SKIP_AND_RETURN, REQUEST_ARGUMENTS, UPDATE_ARGUMENTS_AND_PROCEED, PROCEED
}