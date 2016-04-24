package io.lambdacube.aspecio.examples.misc.internal;

import java.io.PrintStream;
import java.util.stream.IntStream;

import org.osgi.service.component.annotations.Component;

import io.lambdacube.aspecio.aspect.Weave;
import io.lambdacube.aspecio.examples.aspect.metric.Timed;
import io.lambdacube.aspecio.examples.misc.Stuff;

@Component
@Weave(required = Timed.class)
public final class StuffImpl implements Stuff {
    @Override
    public void test(PrintStream ps, int i, byte b, String s) {
        ps.println(s + " " + i + " b" + b);
    }

    @Override
    public double foo(double a, int[] b) {
        return a + IntStream.of(b).sum();
    }
}
