package io.primeval.aspecio.internal;

import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.hooks.service.EventListenerHook;
import org.osgi.framework.hooks.service.FindHook;

import io.primeval.aspecio.Aspecio;
import io.primeval.aspecio.AspecioConstants;
import io.primeval.aspecio.internal.service.AspecioImpl;
import io.primeval.aspecio.internal.service.command.AspecioGogoCommand;

public final class AspecioActivator implements BundleActivator {

    private AspecioImpl aspecio;

    @Override
    public void start(BundleContext context) {
        aspecio = new AspecioImpl(context);
        aspecio.activate();

        boolean filterServices = shouldFilterServices(context);

        if (filterServices) {
            context.registerService(new String[] { Aspecio.class.getName(), FindHook.class.getName(), EventListenerHook.class.getName() },
                    aspecio, null);
        } else {
            context.registerService(Aspecio.class, aspecio, null);
        }
        Hashtable<String, Object> props = new Hashtable<>();
        props.put("osgi.command.scope", AspecioGogoCommand.ASPECIO_GOGO_COMMAND_SCOPE);
        props.put("osgi.command.function", AspecioGogoCommand.ASPECIO_GOGO_COMMANDS);

        AspecioGogoCommand gogoCommand = new AspecioGogoCommand(context, aspecio);
        context.registerService(Object.class, gogoCommand, props);
    }

    @Override
    public void stop(BundleContext context) {
        if (aspecio != null) {
            aspecio.deactivate();
        }
    }

    private boolean shouldFilterServices(BundleContext bundleContext) {
        String filterProp = bundleContext.getProperty(AspecioConstants.ASPECIO_FILTER_SERVICES);
        if (filterProp == null) {
            return true; // default to true
        } else {
            return Boolean.valueOf(filterProp.toLowerCase());
        }
    }

}
