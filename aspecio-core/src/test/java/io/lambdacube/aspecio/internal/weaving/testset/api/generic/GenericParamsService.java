package io.lambdacube.aspecio.internal.weaving.testset.api.generic;

import java.util.function.Consumer;
import java.util.function.Supplier;

import io.lambdacube.aspecio.internal.weaving.testset.api.GenericInterface;
import io.lambdacube.aspecio.internal.weaving.testset.api.annotation.RuntimeMethodAnn;
import io.lambdacube.aspecio.internal.weaving.testset.api.annotation.RuntimeMethodParamAnn;

public final class GenericParamsService<T extends IllegalArgumentException & Runnable> implements GenericInterface<Object, T[]> {

    @RuntimeMethodAnn(path = "/foo")
    public void myMethod(@RuntimeMethodParamAnn(defaultVal = "foo") Consumer<String> containerParam) {

    }

    public Supplier<String> fooMaker() {
        return () -> "foo";
    }

    public void unsafe() throws IllegalStateException, InterruptedException {
    }

    public void unsafeGeneric() throws T {

    }

    @Override
    public T[] makeB() {
        return null;
    }

    @Override
    public void consumeA(Object boo) {
        
    }

}
