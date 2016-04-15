package io.lambdacube.aspecio.internal.weaving.testset.simpleservice;

import java.io.PrintStream;
import java.util.stream.IntStream;

import io.lambdacube.aspecio.internal.weaving.testset.api.SimpleInterface;

public final class SimpleService implements SimpleInterface {

    @Override
    public void sayHello(PrintStream ps) {
        for (int i = 0; i < times(); i++) {
            ps.println(hello());
        }
    }

    @Override
    public String hello() {
        return "hello!";
    }

    @Override
    public int times() {
        return 4;
    }

    @Override
    public int increase(int a) {
        return a * 2;
    }
    
    @Override
    public int reduce(int[] arr) {
        return IntStream.of(arr).sum();
    }
}
