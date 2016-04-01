package io.lambdacube.aspecio.internal.weaving.testset.simplest;

import io.lambdacube.aspecio.internal.weaving.testset.api.SimplestInterface;

public final class SimplestService implements SimplestInterface {

    public static final String PROP_NAME = "simplestFoo";

    @Override
    public void foo() {
        System.setProperty(PROP_NAME, "true");
    }

}
