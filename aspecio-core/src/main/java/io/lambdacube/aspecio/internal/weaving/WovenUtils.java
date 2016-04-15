package io.lambdacube.aspecio.internal.weaving;

import java.lang.reflect.Method;

public final class WovenUtils {

    private WovenUtils() {
    }
    

    public static Method getMethodUnchecked(Class<?> clazz, String methodName, Class<?>... params) {
        try {
            return clazz.getMethod(methodName, params);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new AssertionError("Inconsistent weaving");
        }
    }
}
