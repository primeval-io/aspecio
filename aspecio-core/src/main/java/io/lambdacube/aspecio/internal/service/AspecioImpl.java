package io.lambdacube.aspecio.internal.service;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.hooks.service.EventListenerHook;
import org.osgi.framework.hooks.service.FindHook;
import org.osgi.framework.hooks.service.ListenerHook.ListenerInfo;

import io.lambdacube.aspecio.AspecioConstants;
import io.lambdacube.aspecio.internal.logging.AspecioLogger;
import io.lambdacube.aspecio.internal.logging.AspecioLoggerFactory;

public class AspecioImpl implements FindHook, EventListenerHook {

    private static final AspecioLogger LOGGER = AspecioLoggerFactory.getLogger(AspecioImpl.class);

    private long bundleId;

    private ServiceWeavingManager serviceWeavingManager;
    private AspectManager aspectsManager;

    public void activate(BundleContext bundleContext) throws InvalidSyntaxException {
        LOGGER.info("Activating Aspecio");
        this.bundleId = bundleContext.getBundle().getBundleId();

        aspectsManager = new AspectManager(bundleContext);

        serviceWeavingManager = new ServiceWeavingManager(bundleContext);

        aspectsManager.open();
        serviceWeavingManager.open();
        LOGGER.info("Aspecio activated");
    }

    public void deactivate() {
        aspectsManager.close();
        serviceWeavingManager.close();
        LOGGER.info("Aspecio deactivated");
    }

    @Override
    public void event(ServiceEvent event, Map<BundleContext, Collection<ListenerInfo>> listeners) {
        // Is it an event we want to filter out?
        if (event.getServiceReference().getProperty(AspecioConstants.SERVICE_ASPECT_WEAVE) == null) {
            return;
        }
        Iterator<BundleContext> iterator = listeners.keySet().iterator();
        while (iterator.hasNext()) {
            BundleContext consumingBc = iterator.next();
            long consumingBundleId = consumingBc.getBundle().getBundleId();

            if (consumingBundleId == bundleId || consumingBundleId == 0) {
                continue; // allow self and system bundle
            }
            iterator.remove();
        }
    }

    @Override
    public void find(BundleContext context, String name, String filter, boolean allServices, Collection<ServiceReference<?>> references) {
        long consumingBundleId = context.getBundle().getBundleId();
        if (consumingBundleId == bundleId || consumingBundleId == 0) {
            return;
        }

        Iterator<ServiceReference<?>> iterator = references.iterator();
        while (iterator.hasNext()) {
            ServiceReference<?> reference = iterator.next();
            if (reference.getProperty(AspecioConstants.SERVICE_ASPECT_WEAVE) == null) {
                continue;
            }
            iterator.remove();
        }
    }

}
