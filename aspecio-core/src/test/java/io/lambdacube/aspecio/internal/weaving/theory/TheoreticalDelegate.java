package io.lambdacube.aspecio.internal.weaving.theory;

import java.io.PrintStream;

public final class TheoreticalDelegate implements Goodbye, Hello, Stuff {

    @Override
    public void test(PrintStream ps, int i, byte b, String s) {
    }

    @Override
    public double foo(double a, int[] b) {
        return 0;
    }

    @Override
    public String hello() {
        return null;
    }

    @Override
    public String goodbye() {
        return null;
    }

}
