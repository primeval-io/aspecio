package io.lambdacube.aspecio.examples.internal;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.util.promise.Deferred;
import org.osgi.util.promise.Promise;

import io.lambdacube.aspecio.examples.DemoConsumer;
import io.lambdacube.aspecio.examples.async.SuperSlowService;
import io.lambdacube.aspecio.examples.greetings.Goodbye;
import io.lambdacube.aspecio.examples.greetings.Hello;

@Component
public final class DemoConsumerImpl implements DemoConsumer {

    private Hello hello;
    private Goodbye goodbye;
    private SuperSlowService superSlowService;

    @Override
    public void consumeTo(PrintStream out) {
        out.println(hello.hello() + " " + goodbye.goodbye());
    }

    @Override
    public Long getLongResult() {
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
        
        try {
            return promise2.getValue();
        } catch (InvocationTargetException e) {
        } catch (InterruptedException e) {
        }
        return null;
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
