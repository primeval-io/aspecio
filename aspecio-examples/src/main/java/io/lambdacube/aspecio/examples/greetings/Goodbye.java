package io.lambdacube.aspecio.examples.greetings;

import java.io.PrintStream;

public interface Goodbye {
    String goodbye() throws Throwable;
    
    void test(PrintStream ps, int i, byte b, String s) throws Throwable;
    
    double foo(double a, int[] b) throws Throwable;
}
