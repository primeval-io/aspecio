package io.lambdacube.aspecio.internal;

import java.util.concurrent.Callable;

public final class AspecioUtils {

    private AspecioUtils() {
    }

    // Run any block of code and propagate throwables as necessary
    public static <T> T trust(Callable<T> block) {
        try {
            return block.call();
        } catch (RuntimeException | Error e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
