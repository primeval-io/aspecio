package io.lambdacube.aspecio.demo;

import org.osgi.service.component.annotations.Component;

import io.lambdacube.aspecio.Weave;

@Weave
@Component
public final class WovenCompDemo implements Hello, Goodbye {

    @Override
    public void hello() {
        System.out.println("hello");
    }
    
    @Override
    public void goodbye() {
        System.out.println("goodbye");
    }

}
