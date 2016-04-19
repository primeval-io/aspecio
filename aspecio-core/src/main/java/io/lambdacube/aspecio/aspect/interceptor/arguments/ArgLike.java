package io.lambdacube.aspecio.aspect.interceptor.arguments;

import java.lang.reflect.Parameter;
import java.util.List;

public interface ArgLike {
    
    List<Parameter> parameters();

    <T> T objectArg(String argName);

    @SuppressWarnings("unchecked")
    default <T> T objectArg(String argName, Class<T> clazz) {
        return (T) objectArg(argName);
    }

    int intArg(String argName);

    short shortArg(String argName);

    long longArg(String argName);

    byte byteArg(String argName);

    boolean booleanArg(String argName);

    float floatArg(String argName);

    double doubleArg(String argName);

    char charArg(String argName);

}