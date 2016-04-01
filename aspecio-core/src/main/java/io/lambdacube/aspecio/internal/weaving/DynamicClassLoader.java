package io.lambdacube.aspecio.internal.weaving;

public class DynamicClassLoader extends ClassLoader {
    private final String className;
    private final byte[] bytecode;

    public DynamicClassLoader(ClassLoader parent, String className, byte[] bytecode) {
        super(parent);
        this.className = className;
        this.bytecode = bytecode;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        if (bytecode != null && name.equals(className)) {
            return defineClass(name, bytecode, 0, bytecode.length);
        } else {
            return super.findClass(name);
        }
    }
}