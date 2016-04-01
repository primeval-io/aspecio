package io.lambdacube.aspecio.examples;

import java.io.PrintStream;

public interface DemoConsumer {

    void consumeTo(PrintStream out);
    
    Long getLongResult();
}
