package io.lambdacube.aspecio.examples.internal;

import org.osgi.service.component.annotations.Component;

import io.lambdacube.aspecio.Weave;
import io.lambdacube.aspecio.examples.Goodbye;
import io.lambdacube.aspecio.examples.Hello;

@Weave
@Component
public final class HelloGoodbyeImpl implements Hello, Goodbye {

    @Override
    public String hello() {
        return "hello";
    }
    
    @Override
    public String goodbye() {
        return "goodbye";
    }

}
