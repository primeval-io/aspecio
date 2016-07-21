package io.lambdacube.aspecio.internal.weaving.testset.defaults;

public interface DefaultItf {

    default Class<?> myDefault() {
        return this.getClass();
    }
}
