package io.lambdacube.aspecio.internal.logging;

import java.util.logging.Level;

public abstract class AspecioLogger {

    public abstract void info(String msg);

    public abstract void info(String format, Object... arguments);

    public abstract void debug(String format, Object... arguments);

    public abstract void warn(String format, Object... arguments);

    public abstract void error(String format, Object... arguments);

    public abstract void error(String msg, Throwable error);

}

final class SLF4JLogger extends AspecioLogger {
    public final org.slf4j.Logger logger;

    public SLF4JLogger(org.slf4j.Logger logger) {
        super();
        this.logger = logger;
    }

    @Override
    public void info(String msg) {
        logger.info(msg);
    }

    @Override
    public void info(String format, Object... arguments) {
        logger.info(format, arguments);
    }

    @Override
    public void warn(String format, Object... arguments) {
        logger.warn(format, arguments);
    }

    @Override
    public void debug(String format, Object... arguments) {
        logger.debug(format, arguments);
    }

    @Override
    public void error(String format, Object... arguments) {
        logger.error(format, arguments);
    }

    @Override
    public void error(String msg, Throwable error) {
        logger.error(msg, error);
    }

}

final class JULLogger extends AspecioLogger {
    private final java.util.logging.Logger logger;

    public JULLogger(java.util.logging.Logger logger) {
        this.logger = logger;
    }

    @Override
    public void info(String msg) {
        logger.info(msg);
    }

    @Override
    public void info(String format, Object... arguments) {
        logger.info(() -> sl4jFormatToJul(format, arguments));
    }

    @Override
    public void warn(String format, Object... arguments) {
        logger.log(Level.WARNING, () -> sl4jFormatToJul(format, arguments));
    }

    @Override
    public void debug(String format, Object... arguments) {
        logger.log(Level.FINER, () -> sl4jFormatToJul(format, arguments));
    }

    @Override
    public void error(String format, Object... arguments) {
        logger.log(Level.SEVERE, () -> sl4jFormatToJul(format, arguments));
    }

    @Override
    public void error(String msg, Throwable error) {
        logger.log(Level.SEVERE, msg, error);
    }

    private static String sl4jFormatToJul(String format, Object... arguments) {
        return String.format(format.replaceAll("\\{\\}", "%s"), arrayToString(arguments));
    }

    // Ugly method that ensure everything in the Object[] is indeed a string,
    // while remaining typed as Object[]..
    private static Object[] arrayToString(Object[] array) {
        Object[] res = new String[array.length];
        for (int i = 0; i < array.length; i++) {
            res[i] = array[i].toString();
        }
        return res;
    }
}