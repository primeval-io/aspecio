package io.lambdacube.aspecio.internal.weaving;

import static io.lambdacube.aspecio.internal.AspecioUtils.trust;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.lambdacube.aspecio.internal.logging.AspecioLogger;
import io.lambdacube.aspecio.internal.logging.AspecioLoggerFactory;
import io.lambdacube.aspecio.internal.service.AspecioImpl;
import io.lambdacube.aspecio.internal.weaving.shared.Woven;

/**
 * Entry point to weave a class and specify which interfaces to implement
 * 
 * @author Simon Chemouil
 *
 */
public final class AspectWeaver {
    private static final AspecioLogger LOGGER = AspecioLoggerFactory.getLogger(AspecioImpl.class);

    public static WovenClassHolder weave(DynamicClassLoader dynamicClassLoader, Class<?> clazzToWeave, Class<?>[] interfaces) {
        try {
            Method[] methods = getMethods(clazzToWeave);
            
            dynamicClassLoader.declareClassToWeave(clazzToWeave, interfaces, methods);
            
            String className = WovenClassGenerator.getName(clazzToWeave);
            Class<?> wovenClass = dynamicClassLoader.loadClass(className);

            return new WovenClassHolder(wovenClass,
                    o -> trust(() -> (Woven) wovenClass.getConstructor(clazzToWeave).newInstance(o)));
        } catch (Exception e) {
            LOGGER.error("Could not weave class {}", clazzToWeave.getName());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /* @VisibleForTesting */
    static Method[] getMethods(Class<?> clazzToWeave) {
        Method[] methods = clazzToWeave.getMethods();

        // group methods by name and parameters (unique dispatch)
        Map<MethodIdentifier, List<Method>> uniqueMethods = Stream.of(methods).filter(m -> m.getDeclaringClass() == clazzToWeave)
                .collect(Collectors.groupingBy(m -> new MethodIdentifier(m.getName(), m.getParameterTypes())));

        return uniqueMethods.values().stream()
                // Get array of return types for each unique method
                .map(lm -> lm.toArray(new Method[0]))
                // Choose the narrowest each time
                .map(AspectWeaver::getNarrowest).toArray(Method[]::new);

    }

    /* @VisibleForTesting */
    static Method getNarrowest(Method[] sameDispatchMethods) {
        assert sameDispatchMethods.length != 0;
        // this take care of void + all primitive types
        if (sameDispatchMethods.length == 1) {
            return sameDispatchMethods[0];
        }
        // this should take care of erased type narrowing
        Method res = null;
        Class<?> current = Object.class;
        for (Method m : sameDispatchMethods) {
            Class<?> cls = m.getReturnType();
            if (cls.isAssignableFrom(current)) {
                current = cls;
                res = m;
            }
        }
        assert res != null;
        return res;
    }
}
