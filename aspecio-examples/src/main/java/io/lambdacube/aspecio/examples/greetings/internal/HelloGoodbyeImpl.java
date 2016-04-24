package io.lambdacube.aspecio.examples.greetings.internal;

import org.osgi.service.component.annotations.Component;

import io.lambdacube.aspecio.aspect.annotations.Weave;
import io.lambdacube.aspecio.examples.aspect.counting.CountingAspect;
import io.lambdacube.aspecio.examples.aspect.metric.MetricAspect;
import io.lambdacube.aspecio.examples.greetings.Goodbye;
import io.lambdacube.aspecio.examples.greetings.Hello;

@Component
@Weave(required = CountingAspect.class, optional = MetricAspect.All.class)
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
