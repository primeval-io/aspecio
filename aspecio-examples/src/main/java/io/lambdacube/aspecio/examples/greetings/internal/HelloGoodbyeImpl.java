package io.lambdacube.aspecio.examples.greetings.internal;

import org.osgi.service.component.annotations.Component;

import io.lambdacube.aspecio.Weave;
import io.lambdacube.aspecio.examples.aspect.count.CountAspect;
import io.lambdacube.aspecio.examples.aspect.count.CountCalls;
import io.lambdacube.aspecio.examples.greetings.Goodbye;
import io.lambdacube.aspecio.examples.greetings.Hello;

@Component
@Weave(CountAspect.class)
public final class HelloGoodbyeImpl implements Hello, Goodbye {

    @Override
    @CountCalls(inc = 2)
    public String hello() {
        return "hello";
    }
    
    @Override
    public String goodbye() {
        return "goodbye";
    }

}
