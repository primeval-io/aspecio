package io.lambdacube.aspecio.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.hooks.service.EventListenerHook;
import org.osgi.framework.hooks.service.FindHook;

import io.lambdacube.aspecio.internal.service.AspecioImpl;

public final class AspecioActivator implements BundleActivator {

    private AspecioImpl aspecio;

    @Override
    public void start(BundleContext context) {
        aspecio = new AspecioImpl(context);
        aspecio.activate();
        context.registerService(new String[] { FindHook.class.getName(), EventListenerHook.class.getName() }, aspecio, null);
    }

    @Override
    public void stop(BundleContext context) {
        if (aspecio != null) {
            aspecio.deactivate();
        }
    }

}
