package io.lambdacube.aspecio.aspect.interceptor;

public interface Arguments {
    Object objectAt(int position);

    Object objectNamed(String argName);
}