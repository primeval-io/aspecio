package io.lambdacube.aspecio.internal.weaving.testset.api;

public interface GenericInterface<A, B extends A> {

    B makeB();
    
    void consumeA(A boo);
    
    default void doSome() {
        consumeA(makeB());
    }
}
