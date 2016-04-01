package io.lambdacube.aspecio.examples.greetings.internal;

import io.lambdacube.aspecio.examples.greetings.Goodbye;
import io.lambdacube.aspecio.examples.greetings.Hello;

public final class TheoreticallyWovenComp implements Hello, Goodbye {

    private final HelloGoodbyeImpl delegate;

    public TheoreticallyWovenComp(HelloGoodbyeImpl delegate) {
        this.delegate = delegate;
    }

    @Override
    public String hello() {
        // PRE CODE
        try {
            return delegate.hello();
        } finally {
            // POST CODE
        }
    }

    @Override
    public String goodbye() {
        // PRE CODE
        try {
            return delegate.goodbye();
        } finally {
            // POST CODE
        }
    }

}
