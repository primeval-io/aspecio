package io.primeval.aspecio.internal.service;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class ServicePool<T> {

    private final Map<Object, T> originalToProxy = new IdentityHashMap<>();
    private final Map<T, Object> proxyToOriginal = new IdentityHashMap<>();
    private final Map<T, Integer> proxyToCount = new IdentityHashMap<>();

    public synchronized T get(Object originalService, Supplier<T> proxyFactory) {
        T proxy = originalToProxy.computeIfAbsent(originalService, k -> proxyFactory.get());
        proxyToOriginal.putIfAbsent(proxy, originalService);
        proxyToCount.compute(proxy, (k, v) -> {
            if (v == null) {
                return 1;
            } else {
                return v + 1;
            }
        });
        return proxy;
    }

    public synchronized boolean unget(T proxy) {
        Integer count = proxyToCount.compute(proxy, (k, v) -> {
            if (v == null) {
                return 0;
            } else {
                return v - 1;
            }
        });

        if (count > 0) {
            return false;
        }
        // clean-up
        proxyToCount.remove(proxy);
        Object original = proxyToOriginal.remove(proxy);
        T proxyX = originalToProxy.remove(original);

        assert proxy == proxyX;

        return true;
    }

}
