package io.primeval.aspecio.internal.weaving;

import java.util.HashSet;
import java.util.Set;

public final class BridgingClassLoader extends ClassLoader {
    private static final Set<String> ASPECIO_PACKAGES = new HashSet<>();
    static {
        ASPECIO_PACKAGES.add("io.primeval.aspecio.aspect.interceptor");
        ASPECIO_PACKAGES.add("io.primeval.aspecio.aspect.interceptor.arguments");
        ASPECIO_PACKAGES.add("io.primeval.aspecio.internal.weaving.shared");
    }

    private final ClassLoader aspecioClassLoader;
    private final ClassLoader[] classLoaders;

    public BridgingClassLoader(ClassLoader[] classLoaders, ClassLoader aspecioClassLoader) {
        this.classLoaders = classLoaders;
        this.aspecioClassLoader = aspecioClassLoader;
    }

    @Override
    protected Class<?> loadClass(String className, boolean resolve) throws ClassNotFoundException {
        int lastDot = className.lastIndexOf('.');
        String packageName = className.substring(0, lastDot);
        if (ASPECIO_PACKAGES.contains(packageName)) {
            return aspecioClassLoader.loadClass(className);
        }

        for (int i = 0; i < classLoaders.length; i++) {
            try {
                return classLoaders[i].loadClass(className);
            } catch (ClassNotFoundException cnfe) {
                // continue
            }
        }

        throw new ClassNotFoundException(className);
    }

   
}
