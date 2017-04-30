package io.primeval.aspecio.examples.async;

import org.osgi.util.promise.Promise;

public interface SuperSlowService {

    Promise<Long> compute();
}
