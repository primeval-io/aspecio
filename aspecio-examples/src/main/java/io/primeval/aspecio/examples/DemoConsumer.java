package io.primeval.aspecio.examples;

import java.io.PrintStream;

import org.osgi.util.promise.Promise;

public interface DemoConsumer {

    void consumeTo(PrintStream out);
    
    Promise<Long> getLongResult();
}
