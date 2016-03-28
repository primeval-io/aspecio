package io.lambdacube.aspecio.demo;

import java.util.stream.Stream;

import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component
public final class DemoConsumer {

    private Hello hello;
    private Goodbye goodbye;

    @Activate
    public void activate() {
        hello.hello();
        goodbye.goodbye();
    }

    @Reference
    public void setHello(Hello hello, ServiceReference<Hello> helloRef) {
        System.out.println("HELLO arrived!");
        this.hello = hello;
        Stream.of(helloRef.getPropertyKeys()).forEach(p -> {
            System.out.println(p + " ---> " + propAsString(helloRef.getProperty(p)));
        });

    }

    @Reference
    public void setGoodbye(Goodbye goodbye, ServiceReference<Goodbye> goodbyeRef) {
        System.out.println("GOODBYE arrived!");
        this.goodbye = goodbye;
        Stream.of(goodbyeRef.getPropertyKeys()).forEach(p -> {
            System.out.println(p + " ---> " + propAsString(goodbyeRef.getProperty(p)));
        });
    }

    private String propAsString(Object prop) {
        if (prop instanceof String[]) {
            String[] objects = (String[]) prop;
            return String.join(", ", objects);
        }
        return prop.toString();
    }

}
