package io.lambdacube.aspecio.internal.weaving;

import java.util.HashSet;
import java.util.Set;

public final class BridgingClassLoader extends ClassLoader {
    private static final Set<String> ASPECIO_PACKAGES = new HashSet<>();
    static {
        ASPECIO_PACKAGES.add("io.lambdacube.aspecio.aspect.interceptor");
        ASPECIO_PACKAGES.add("io.lambdacube.aspecio.aspect.interceptor.arguments");
        ASPECIO_PACKAGES.add("io.lambdacube.aspecio.internal.weaving.shared");
    }
    
    private final ClassLoader aspecioClassLoader;

    public BridgingClassLoader(ClassLoader parent, ClassLoader aspecioClassLoader) {
        super(parent);
        this.aspecioClassLoader = aspecioClassLoader;
    }

    @Override
    protected Class<?> findClass(String className) throws ClassNotFoundException {
        int lastDot = className.lastIndexOf('.');
        String packageName = className.substring(0, lastDot);
        if (ASPECIO_PACKAGES.contains(packageName)) {
            return aspecioClassLoader.loadClass(className);
        }
        return super.findClass(className);
    }
}
