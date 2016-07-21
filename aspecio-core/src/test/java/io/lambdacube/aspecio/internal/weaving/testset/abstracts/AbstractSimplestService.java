package io.lambdacube.aspecio.internal.weaving.testset.abstracts;

import io.lambdacube.aspecio.internal.weaving.testset.api.SimplestInterface;

public abstract class AbstractSimplestService implements SimplestInterface {
    public static final String PROP_NAME = "simplestAbstractedFoo";

    @Override
    public void foo() {
        System.setProperty(PROP_NAME, "true");
    }
}
