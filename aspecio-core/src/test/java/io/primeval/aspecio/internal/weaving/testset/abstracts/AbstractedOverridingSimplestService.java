package io.primeval.aspecio.internal.weaving.testset.abstracts;

public final class AbstractedOverridingSimplestService extends AbstractSimplestService {
    public static final String PROP_NAME = "simplestOverridingFoo";

    @Override
    public void foo() {
        System.setProperty(PROP_NAME, "true");
    }
}
