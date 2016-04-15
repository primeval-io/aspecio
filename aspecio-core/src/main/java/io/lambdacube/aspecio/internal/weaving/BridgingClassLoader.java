package io.lambdacube.aspecio.internal.weaving;

public final class BridgingClassLoader extends ClassLoader {
//    private static final String WOVEN_CLASSNAME = Woven.class.getName();
//    private static final String WOVENUTILS_CLASSNAME = WovenUtils.class.getName();
    private final ClassLoader aspecioClassLoader;

    public BridgingClassLoader(ClassLoader parent, ClassLoader aspecioClassLoader) {
        super(parent);
        this.aspecioClassLoader = aspecioClassLoader;
    }

    @Override
    protected Class<?> findClass(String className) throws ClassNotFoundException {
        if (className.startsWith("io.lambdacube.aspecio")) {
            return aspecioClassLoader.loadClass(className);
        }
        return super.findClass(className);
    }
}
