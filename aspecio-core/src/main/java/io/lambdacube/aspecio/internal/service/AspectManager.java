package io.lambdacube.aspecio.internal.service;

import static io.lambdacube.aspecio.internal.AspecioUtils.asStringProperties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

import io.lambdacube.aspecio.AspecioConstants;
import io.lambdacube.aspecio.aspect.interceptor.Interceptor;
import io.lambdacube.aspecio.internal.logging.AspecioLogger;
import io.lambdacube.aspecio.internal.logging.AspecioLoggerFactory;

public final class AspectManager implements ServiceListener {

    private static final AspecioLogger LOGGER = AspecioLoggerFactory.getLogger(AspectManager.class);

    private static final String SERVICE_FILTER = "(" + AspecioConstants.SERVICE_ASPECT + "=*)";

    private final BundleContext bundleContext;

    private final SortedMap<ServiceReference<?>, AspectService> aspectServices = Collections.synchronizedSortedMap(new TreeMap<>());

    private volatile boolean closed = false;

    public AspectManager(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public void open() {
        try {
            this.bundleContext.addServiceListener(this, SERVICE_FILTER);

            ServiceReference<?>[] serviceReferences = this.bundleContext.getServiceReferences((String) null, SERVICE_FILTER);

            if (serviceReferences != null) {
                synchronized (this) {
                    for (ServiceReference<?> sr : serviceReferences) {
                        onServiceRegistration(sr);
                    }
                }
            }

        } catch (InvalidSyntaxException e) {
            throw new AssertionError("Could not create filter?!", e);
        }
    }

    public void close() {
        this.closed = true;
        this.bundleContext.removeServiceListener(this);
        synchronized (this) {
            for (ServiceReference<?> sr : aspectServices.keySet()) {
                this.bundleContext.ungetService(sr);
            }
            aspectServices.clear();
        }
    }

    @Override
    public void serviceChanged(ServiceEvent event) {
        if (closed) {
            return;
        }

        ServiceReference<?> sr = event.getServiceReference();

        switch (event.getType()) {
        case ServiceEvent.REGISTERED:
            onServiceRegistration(sr);
            break;

        case ServiceEvent.MODIFIED:
            onServiceUpdate(sr);
            break;

        case ServiceEvent.MODIFIED_ENDMATCH:
        case ServiceEvent.UNREGISTERING:
            onServiceDeparture(sr);
            break;
        }
    }

    public synchronized void onServiceRegistration(ServiceReference<?> reference) {
        if (aspectServices.containsKey(reference)) {
            // This might happen if a service arrives between the listener registration and the initial getServiceReferences call
            return;
        }

        List<String> aspects = new ArrayList<>(Arrays.asList(asStringProperties(reference.getProperty(AspecioConstants.SERVICE_ASPECT))));
        List<String> extraProperties = new ArrayList<>(
                Arrays.asList(asStringProperties(reference.getProperty(AspecioConstants.SERVICE_ASPECT_EXTRAPROPERTIES))));

        Object service = bundleContext.getService(reference);
        if (!(service instanceof Interceptor)) {
            // Don't track aspects that don't implements Interceptor.
            bundleContext.ungetService(reference);
            return;
        }

        LOGGER.debug("Added aspect: {} (extraProps: {})", aspects, extraProperties);

        AspectService aspectService = new AspectService((Interceptor) service, aspects, extraProperties);
        this.aspectServices.put(reference, aspectService);
    }

    public synchronized void onServiceUpdate(ServiceReference<?> reference) {
        AspectService aspectService = aspectServices.get(reference);
        if (aspectService == null) {
            return;
        }

        List<String> aspects = new ArrayList<>(Arrays.asList(asStringProperties(reference.getProperty(AspecioConstants.SERVICE_ASPECT))));
        List<String> extraProperties = new ArrayList<>(
                Arrays.asList(asStringProperties(reference.getProperty(AspecioConstants.SERVICE_ASPECT_EXTRAPROPERTIES))));

        if (!Objects.equals(aspectService.aspects, aspects) || !Objects.equals(aspectService.extraProperties, extraProperties)) {
            LOGGER.debug("Updating aspect: {} (extraProps: {})", aspects, extraProperties);
            AspectService updatedService = new AspectService(aspectService.interceptor, aspects, extraProperties);
            this.aspectServices.put(reference, updatedService);
        }

    }

    public synchronized void onServiceDeparture(ServiceReference<?> reference) {
        AspectService aspectService = aspectServices.get(reference);
        if (aspectService == null) {
            return;
        }
        LOGGER.debug("Removed aspect: {} (extraProps: {})", aspectService.aspects, aspectService.extraProperties);
        this.aspectServices.remove(reference);
    }

}