package io.lambdacube.aspecio.aspect.interceptor;

interface Arguments {
    Object objectAt(int position);

    Object objectNamed(String argName);
}

public enum BeforeAction {
    PROCEED, SKIP_AND_RETURN, REQUEST_ARGUMENTS, UPDATE_ARGUMENTS_AND_PROCEED
}