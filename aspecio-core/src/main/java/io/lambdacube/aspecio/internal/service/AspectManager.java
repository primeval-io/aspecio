package io.lambdacube.aspecio.internal.service;

import static io.lambdacube.aspecio.internal.AspecioUtils.asStringProperties;
import static io.lambdacube.aspecio.internal.AspecioUtils.firstOrNull;
import static io.lambdacube.aspecio.internal.AspecioUtils.getIntValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
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

    private final SortedMap<ServiceReference<?>, AspectService> aspectServiceByServiceRef = Collections
            .synchronizedSortedMap(new TreeMap<>());

    private final Map<String, SortedSet<AspectService>> aspectServicesByAspectName = new ConcurrentHashMap<>();

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
            for (ServiceReference<?> sr : aspectServiceByServiceRef.keySet()) {
                this.bundleContext.ungetService(sr);
            }
            aspectServiceByServiceRef.clear();
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
        if (aspectServiceByServiceRef.containsKey(reference)) {
            // This might happen if a service arrives between the listener registration and the initial getServiceReferences call
            return;
        }

        List<String> aspects = new ArrayList<>(Arrays.asList(asStringProperties(reference.getProperty(AspecioConstants.SERVICE_ASPECT))));
        List<String> extraProperties = new ArrayList<>(
                Arrays.asList(asStringProperties(reference.getProperty(AspecioConstants.SERVICE_ASPECT_EXTRAPROPERTIES))));
        int serviceRanking = getIntValue(reference.getProperty(Constants.SERVICE_RANKING), 0);

        Object service = bundleContext.getService(reference);
        if (!(service instanceof Interceptor)) {
            // Don't track aspects that don't implements Interceptor.
            bundleContext.ungetService(reference);
            return;
        }

        LOGGER.debug("Added aspect: {} (extraProps: {})", aspects, extraProperties);

        AspectService aspectService = new AspectService((Interceptor) service, reference, serviceRanking, aspects, extraProperties);
        this.aspectServiceByServiceRef.put(reference, aspectService);

        // Deal with aspects map.
        for (String aspect : aspects) {
            boolean mustNotify = false;

            SortedSet<AspectService> as = this.aspectServicesByAspectName.get(aspect);
            if (as == null) {
                as = new TreeSet<>();
                this.aspectServicesByAspectName.put(aspect, as);
                mustNotify = true;
                as.add(aspectService);

            } else {
                AspectService firstBefore = firstOrNull(as);

                // The trick here is that we use a SortedSet
                // with the right compareTo method on aspectService.
                as.add(aspectService);
                AspectService firstAfter = firstOrNull(as);

                mustNotify = firstAfter != firstBefore;
            }

            if (mustNotify) {
                // TODO notify
            }
        }
    }

    public synchronized void onServiceUpdate(ServiceReference<?> reference) {
        AspectService aspectService = aspectServiceByServiceRef.get(reference);
        if (aspectService == null) {
            return;
        }

        List<String> newAspects = new ArrayList<>(
                Arrays.asList(asStringProperties(reference.getProperty(AspecioConstants.SERVICE_ASPECT))));
        List<String> extraProperties = new ArrayList<>(
                Arrays.asList(asStringProperties(reference.getProperty(AspecioConstants.SERVICE_ASPECT_EXTRAPROPERTIES))));
        int serviceRanking = getIntValue(reference.getProperty(Constants.SERVICE_RANKING), 0);

        boolean rankingChanged = aspectService.serviceRanking != serviceRanking;
        boolean aspectsChanged = !Objects.equals(aspectService.aspects, newAspects);
        boolean extraPropsChanged = !Objects.equals(aspectService.extraProperties, extraProperties);
        
        if (rankingChanged || aspectsChanged || extraPropsChanged) {
            LOGGER.debug("Updating aspect: {} (extraProps: {})", newAspects, extraProperties);
            AspectService updatedService = new AspectService(aspectService.interceptor, reference, serviceRanking, newAspects,
                    extraProperties);
            this.aspectServiceByServiceRef.put(reference, updatedService);

            Iterator<String> aspectsToProcess = Stream.concat(newAspects.stream(), aspectService.aspects.stream()).distinct().iterator();

            while (aspectsToProcess.hasNext()) {
                boolean mustNotify = false;
                String aspect = aspectsToProcess.next();
                boolean toPublish = newAspects.contains(aspect);
                SortedSet<AspectService> as = this.aspectServicesByAspectName.get(aspect);

                // Brand new aspect
                if (as == null) {
                    if (toPublish) {
                        as = new TreeSet<>();
                        this.aspectServicesByAspectName.put(aspect, as);
                        as.add(updatedService);
                        mustNotify = true;
                    } else {
                        // otherwise we have an aspect we wanted to remove,
                        // and somehow it's been already removed?
                        // trace it because it would be a bug
                        // However it's harmless.
                        LOGGER.debug("Weird situation #4242, check code & comments");
                    }
                } else {
                    AspectService firstBefore = firstOrNull(as);

                    if (toPublish) {
                        if (rankingChanged) {
                            // special case where we must force the re-ordering
                            // by cleanly removing from the set first
                            as.remove(aspectService);
                        }
                        // The trick here is that we use a SortedSet
                        // with the right compareTo method on aspectService.
                        as.add(updatedService);

                    } else {
                        // here it is the *old* service we remove.
                        as.remove(aspectService);
                    }

                    AspectService firstAfter = firstOrNull(as);

                    mustNotify = firstAfter != firstBefore;

                    // clean-up
                    if (as.isEmpty()) {
                        this.aspectServicesByAspectName.remove(aspect);
                    }

                }
                if (mustNotify) {
                    // TODO notify
                }
            }
        }

    }

    public synchronized void onServiceDeparture(ServiceReference<?> reference) {
        AspectService aspectService = aspectServiceByServiceRef.get(reference);
        if (aspectService == null) {
            return;
        }
        LOGGER.debug("Removed aspect: {} (extraProps: {})", aspectService.aspects, aspectService.extraProperties);
        this.aspectServiceByServiceRef.remove(reference);

        for (String aspect : aspectService.aspects) {
            boolean mustNotify = false;
            SortedSet<AspectService> as = aspectServicesByAspectName.get(aspect);
            if (as != null) {
                Iterator<AspectService> asIt = as.iterator();
                while (asIt.hasNext()) {
                    AspectService a = asIt.next();
                    if (aspectService.equals(a)) {
                        asIt.remove();
                        mustNotify = true;
                    }

                }
            }
            if (mustNotify) {
                // TODO
            }
        }
    }

}