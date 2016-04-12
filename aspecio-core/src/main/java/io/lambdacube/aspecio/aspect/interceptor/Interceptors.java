package io.lambdacube.aspecio.aspect.interceptor;

import java.util.List;

public final class Interceptors {

    private Interceptors() {
    }

    public static Interceptor compose(Interceptor interceptor) {
        return interceptor;
    }

    public static Interceptor compose(Interceptor... interceptors) {
        if (interceptors.length == 0) {
            return Interceptor.NOOP;
        }
        // no defensive copy, trust the client not to share or mutate this
        // array.
        return new CompositeInterceptor(interceptors);
    }

    public static Interceptor compose(List<Interceptor> interceptors) {
        // no defensive copy, trust the client not to share or mutate this
        // list.
        if (interceptors.isEmpty()) {
            return Interceptor.NOOP;
        } else if (interceptors.size() == 1) {
            return interceptors.get(0);
        } else {
            return new CompositeInterceptor(interceptors.toArray(new Interceptor[0]));
        }
    }

}
