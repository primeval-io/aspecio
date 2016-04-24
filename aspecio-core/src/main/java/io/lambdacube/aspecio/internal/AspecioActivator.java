package io.lambdacube.aspecio.internal;

import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.hooks.service.EventListenerHook;
import org.osgi.framework.hooks.service.FindHook;

import io.lambdacube.aspecio.Aspecio;
import io.lambdacube.aspecio.internal.service.AspecioImpl;
import io.lambdacube.aspecio.internal.service.command.AspecioGogoCommand;

public final class AspecioActivator implements BundleActivator {

    
    private AspecioImpl aspecio;

    @Override
    public void start(BundleContext context) {
        aspecio = new AspecioImpl(context);
        aspecio.activate();

    
        context.registerService(new String[] { Aspecio.class.getName(), FindHook.class.getName(), EventListenerHook.class.getName() }, aspecio, null);
        
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

}
