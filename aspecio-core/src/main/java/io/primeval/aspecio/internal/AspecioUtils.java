package io.primeval.aspecio.internal;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.Callable;

import io.primeval.aspecio.internal.logging.AspecioLogger;
import io.primeval.aspecio.internal.logging.AspecioLoggerFactory;

public final class AspecioUtils {

    private static final AspecioLogger LOGGER = AspecioLoggerFactory.getLogger(AspecioUtils.class);

    private AspecioUtils() {
    }

    // Run any block of code and propagate throwables as necessary
    public static <T> T trust(Callable<T> block) {
        try {
            return block.call();
        } catch (RuntimeException | Error e) {
            LOGGER.error("Error while running code", e);
            throw e;
        } catch (Exception e) {
            LOGGER.error("Exception while running code", e);
            throw new RuntimeException(e);
        }
    }

    public static String asStringProperty(Object propObj) {
        String res;
        if (propObj == null) {
            res = null;
        } else if (propObj instanceof String[] && ((String[]) propObj).length == 1) {
            res = ((String[]) propObj)[0];
        } else if (propObj instanceof String) {
            res = (String) propObj;
        } else {
            throw new IllegalArgumentException("Can only convert properties of type String or String[] of size 1");
        }
        return res;
    }

    public static String[] asStringProperties(Object propObj) {
        String[] res = null;
        if (propObj == null) {
            res = new String[0];
        } else if (propObj instanceof String[]) {
            res = (String[]) propObj;
        } else if (propObj instanceof String) {
            res = new String[] { (String) propObj };
        } else {
            throw new IllegalArgumentException("Can only convert properties of type String or String[]");
        }
        return res;
    }

    public static long getLongValue(Object propObj) {
        if (propObj instanceof Number) {
            return ((Number) propObj).longValue();
        } else {
            throw new IllegalArgumentException("Required number!");
        }
    }

    public static int getIntValue(Object propObj, int defaultValue) {
        if (propObj instanceof Integer) {
            return ((Integer) propObj).intValue();
        } else {
            return defaultValue;
        }
    }

    public static <T> T firstOrNull(SortedSet<T> set) {
        if (set == null || set.isEmpty()) {
            return null;
        }
        return set.first();
    }

    public static <T> Set<T> copySet(Collection<T> source) {
        Set<T> copy = new LinkedHashSet<>(source.size());
        copy.addAll(source);
        return copy;
    }

}
