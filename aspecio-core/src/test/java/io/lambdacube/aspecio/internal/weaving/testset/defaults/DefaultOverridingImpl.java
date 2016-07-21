package io.lambdacube.aspecio.internal.weaving.testset.defaults;

public final class DefaultOverridingImpl implements DefaultItf {

    @Override
    public Class<?> myDefault() {
        return this.getClass();
    }
}
