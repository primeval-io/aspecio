package io.primeval.aspecio.internal.service;

import java.util.Map;
import java.util.function.Supplier;

import org.osgi.framework.wiring.BundleRevision;

import io.primeval.reflect.proxy.bytecode.ProxyClassLoader;

public final class BundleRevPath {
    private ProxyClassLoader classLoader;
    private Map<BundleRevision, BundleRevPath> subMap;

    public synchronized ProxyClassLoader computeClassLoaderIfAbsent(Supplier<ProxyClassLoader> classLoaderSupplier) {
        if (classLoader == null) {
            classLoader = classLoaderSupplier.get();
        }
        return classLoader;
    }

    public synchronized Map<BundleRevision, BundleRevPath> computeSubMapIfAbsent(
            Supplier<Map<BundleRevision, BundleRevPath>> subMapSupplier) {
        if (subMap == null) {
            subMap = subMapSupplier.get();
        }
        return subMap;
    }

}
