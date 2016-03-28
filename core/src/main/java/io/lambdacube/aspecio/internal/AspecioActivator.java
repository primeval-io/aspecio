package io.lambdacube.aspecio.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.hooks.service.EventListenerHook;
import org.osgi.framework.hooks.service.FindHook;

public final class AspecioActivator implements BundleActivator {

    private AspecioServiceHook aspecio;

    @Override
    public void start(BundleContext context) throws Exception {
        aspecio = new AspecioServiceHook();
        aspecio.activate(context);
        context.registerService(new String[] { FindHook.class.getName(), EventListenerHook.class.getName() }, aspecio, null);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        if (aspecio != null) {
            aspecio.deactivate();
        }
    }

}
