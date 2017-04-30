package io.primeval.aspecio.aspect.interceptor.composite;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.primeval.aspecio.aspect.interceptor.Interceptor;

/**
 * Utilities for {@link Interceptor}.
 */
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

    public static Interceptor compose(Iterable<Interceptor> interceptors) {
        Iterator<Interceptor> iterator = interceptors.iterator();
        return compose(iterator);
    }

    public static Interceptor compose(Iterator<Interceptor> interceptors) {
        if (!interceptors.hasNext()) {
            return Interceptor.NOOP;
        } else {
            Interceptor first = interceptors.next();

            if (!interceptors.hasNext()) {
                return first;
            }
            List<Interceptor> list = new ArrayList<>();
            list.add(first);
            do {
                list.add(interceptors.next());
            } while (interceptors.hasNext());

            return new CompositeInterceptor(list.toArray(new Interceptor[0]));
        }
    }

    public static Interceptor compose(List<Interceptor> interceptors) {
        if (interceptors.isEmpty()) {
            return Interceptor.NOOP;
        } else if (interceptors.size() == 1) {
            return interceptors.get(0);
        } else {
            return new CompositeInterceptor(interceptors.toArray(new Interceptor[0]));
        }
    }

}
