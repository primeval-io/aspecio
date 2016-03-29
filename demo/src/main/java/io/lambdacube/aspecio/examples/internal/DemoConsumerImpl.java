package io.lambdacube.aspecio.examples.internal;

import java.io.PrintStream;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import io.lambdacube.aspecio.examples.DemoConsumer;
import io.lambdacube.aspecio.examples.Goodbye;
import io.lambdacube.aspecio.examples.Hello;

@Component
public final class DemoConsumerImpl implements DemoConsumer {

    private Hello hello;
    private Goodbye goodbye;

    @Override
    public void consumeTo(PrintStream out) {
        out.println(hello.hello() + " " + goodbye.goodbye());
    }

    @Reference
    public void setHello(Hello hello) {
        this.hello = hello;
    }

    @Reference
    public void setGoodbye(Goodbye goodbye) {
        this.goodbye = goodbye;
    }

}
