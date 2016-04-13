package io.lambdacube.aspecio.internal.weaving;

public final class BridgingClassLoader extends ClassLoader {
    private static final String WOVEN_CLASSNAME = Woven.class.getName();
    private final ClassLoader aspecioClassLoader;

    public BridgingClassLoader(ClassLoader parent, ClassLoader aspecioClassLoader) {
        super(parent);
        this.aspecioClassLoader = aspecioClassLoader;
    }

    @Override
    protected Class<?> findClass(String className) throws ClassNotFoundException {
        if (WOVEN_CLASSNAME.equals(className)) {
            return aspecioClassLoader.loadClass(className);
        }
        return super.findClass(className);
    }
}
