package io.primeval.aspecio.examples.greetings.internal;

import org.osgi.service.component.annotations.Component;

import io.primeval.aspecio.aspect.annotations.Weave;
import io.primeval.aspecio.examples.aspect.counting.CountingAspect;
import io.primeval.aspecio.examples.aspect.metric.MetricAspect;
import io.primeval.aspecio.examples.greetings.Goodbye;
import io.primeval.aspecio.examples.greetings.Hello;

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
