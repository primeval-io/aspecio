package io.lambdacube.aspecio.internal.weaving;

import static io.lambdacube.aspecio.internal.AspecioUtils.trust;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public final class DynamicClassLoader extends ClassLoader {
    private final Set<Class<?>> managedClasses = Collections.synchronizedSet(new HashSet<>());
    private final Map<String, Supplier<byte[]>> classesToWeave = new ConcurrentHashMap<>();

    public DynamicClassLoader(ClassLoader parent) {
        super(parent);
    }

    // We can add because we keep 1 classloader per bundle.
    // Removing is meaningless.
    public void declareClassToWeave(Class<?> clazzToWeave, Class<?>[] interfaces, Method[] methods) {
        if (managedClasses.add(clazzToWeave)) {
            String wovenClassGen = WovenClassGenerator.getName(clazzToWeave);
            classesToWeave.put(wovenClassGen, () -> trust(() -> WovenClassGenerator.weave(clazzToWeave, interfaces, methods)));
            for (int methodId = 0; methodId < methods.length; methodId++) {
                Method m = methods[methodId];
                int methId = methodId;
                String methodArgName = WovenMethodArgsGenerator.getName(clazzToWeave, m, methodId);
                classesToWeave.put(methodArgName, () -> trust(() -> WovenMethodArgsGenerator.generateMethodArgs(clazzToWeave, m, methId)));

                String methodArgUpdaterName = WovenMethodArgsUpdaterGenerator.getName(clazzToWeave, m, methodId);
                classesToWeave.put(methodArgUpdaterName,
                        () -> trust(() -> WovenMethodArgsUpdaterGenerator.generateMethodArgsUpdater(clazzToWeave, m, methId)));
            }
        }
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        // We remove, since classes are loaded only once.
        Supplier<byte[]> bytecodeSupplier = classesToWeave.remove(name);
        if (bytecodeSupplier != null) {
            byte[] bytecode = bytecodeSupplier.get();
            return defineClass(name, bytecode, 0, bytecode.length);
        } else {
            return super.findClass(name);
        }
    }
}