package io.lambdacube.aspecio.internal.weaving.testset.simpleservice;

import java.io.PrintStream;

import io.lambdacube.aspecio.internal.weaving.testset.api.SimpleInterface;

public final class DelegatingSimpleService implements SimpleInterface {

    private final SimpleService delegate;

    public DelegatingSimpleService(SimpleService delegate) {
        this.delegate = delegate;
    }

    @Override
    public void sayHello(PrintStream ps) {
        delegate.sayHello(ps);
    }

    @Override
    public String hello() {
        return delegate.hello();
    }

    @Override
    public int times() {
        return delegate.times();
    }
    
    @Override
    public int increase(int a) {
        return delegate.increase(a);
    }

    @Override
    public int reduce(int[] arr) {
        return delegate.reduce(arr);
    };

}
