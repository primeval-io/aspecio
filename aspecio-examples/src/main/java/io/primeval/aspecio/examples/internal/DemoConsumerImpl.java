package io.primeval.aspecio.examples.internal;

import java.io.PrintStream;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.util.promise.Deferred;
import org.osgi.util.promise.Promise;

import io.primeval.aspecio.examples.DemoConsumer;
import io.primeval.aspecio.examples.async.SuperSlowService;
import io.primeval.aspecio.examples.greetings.Goodbye;
import io.primeval.aspecio.examples.greetings.Hello;

@Component
public final class DemoConsumerImpl implements DemoConsumer {

    private Hello hello;
    private Goodbye goodbye;
    private SuperSlowService superSlowService;

    @Override
    public void consumeTo(PrintStream out) {
        try {
            out.println(hello.hello() + " " + goodbye.goodbye());
        } catch (Throwable e) {
        }
    }

    @Override
    public Promise<Long> getLongResult() {
        Deferred<Long> d = new Deferred<>();

        Promise<Long> promise = superSlowService.compute();
        promise.onResolve(() -> {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    d.resolveWith(promise);
                }
            }).start();
        });
        Promise<Long> promise2 = d.getPromise();

        return promise2;
    }

    @Reference
    public void setHello(Hello hello) {
        this.hello = hello;
    }

    @Reference
    public void setGoodbye(Goodbye goodbye) {
        this.goodbye = goodbye;
    }

    @Reference
    public void setSuperSlowService(SuperSlowService superSlowService) {
        this.superSlowService = superSlowService;
    }

}
