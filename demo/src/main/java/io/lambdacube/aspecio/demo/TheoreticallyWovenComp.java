package io.lambdacube.aspecio.demo;

public final class TheoreticallyWovenComp implements Hello, Goodbye {

    private final WovenCompDemo delegate;

    public TheoreticallyWovenComp(WovenCompDemo delegate) {
        this.delegate = delegate;
    }

    @Override
    public void hello() {
        // PRE CODE
        try {
            delegate.hello();
        } finally {
            // POST CODE
        }
    }

    @Override
    public void goodbye() {
        // PRE CODE
        try {
            delegate.goodbye();
        } finally {
            // POST CODE
        }
    }

}
