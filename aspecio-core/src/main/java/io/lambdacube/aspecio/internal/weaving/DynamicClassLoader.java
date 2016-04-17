package io.lambdacube.aspecio.internal.weaving;

import java.util.Map;

public class DynamicClassLoader extends ClassLoader {
    private final Map<String, byte[]> byteCodes;

    public DynamicClassLoader(ClassLoader parent, Map<String, byte[]> byteCodes) {
        super(parent);
        this.byteCodes = byteCodes;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        byte[] bytecode = byteCodes.remove(name); // a class cannot be defined twice.
        if (bytecode != null) {
            return defineClass(name, bytecode, 0, bytecode.length);
        } else {
            return super.findClass(name);
        }
    }
}