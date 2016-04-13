package io.lambdacube.aspecio.examples.greetings.internal;

import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

import io.lambdacube.aspecio.Weave;
import io.lambdacube.aspecio.examples.aspect.counting.CountingAspect;
import io.lambdacube.aspecio.examples.aspect.timed.Timed;
import io.lambdacube.aspecio.examples.greetings.Goodbye;
import io.lambdacube.aspecio.examples.greetings.Hello;

@Component(scope = ServiceScope.SINGLETON, immediate = true)
@Weave(required = CountingAspect.class)
public final class HelloGoodbyeImpl implements Hello, Goodbye {

    @Reference
    private ConfigurationAdmin ca;

    @Override
    @Timed
    public String hello() throws Throwable {
        return "hello";
    }

    @Override
    public String goodbye() {
        return "goodbye";
    }

}
