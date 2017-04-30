package io.primeval.aspecio.internal.logging;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;

// Allow people to use something else than SLF4J, including nothing but JUL
public final class AspecioLoggerFactory {

    public static final AspecioLogger getLogger(Class<?> cls) {
        try {
            Class<?> loggerFactory = cls.getClassLoader().loadClass("org.slf4j.LoggerFactory");
            Method loggerCreator = loggerFactory.getMethod("getLogger", Class.class);
            Object logger = loggerCreator.invoke(null, cls);
            return new SLF4JLogger((org.slf4j.Logger) logger);

        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            java.util.logging.Logger julLogger = java.util.logging.Logger.getLogger(cls.getName());
            julLogger.log(Level.FINE, "SLF4J is not available, defaulting to JUL");
            return new JULLogger(julLogger);
        }
    }

}
