package io.lambdacube.aspecio.internal.service;

import java.util.Map;
import java.util.function.Supplier;

import org.osgi.framework.wiring.BundleRevision;

import io.lambdacube.aspecio.internal.weaving.DynamicClassLoader;

public final class BundleRevPath {
    private DynamicClassLoader classLoader;
    private Map<BundleRevision, BundleRevPath> subMap;

    public synchronized DynamicClassLoader computeClassLoaderIfAbsent(Supplier<DynamicClassLoader> classLoaderSupplier) {
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
