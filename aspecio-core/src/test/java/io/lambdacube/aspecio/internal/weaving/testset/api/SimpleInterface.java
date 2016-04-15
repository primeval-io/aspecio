package io.lambdacube.aspecio.internal.weaving.testset.api;

import java.io.PrintStream;

public interface SimpleInterface {

    void sayHello(PrintStream ps);
    
    String hello();
    
    int times();
    
    int increase(int a);
    
    int reduce(int[] arr);
}
