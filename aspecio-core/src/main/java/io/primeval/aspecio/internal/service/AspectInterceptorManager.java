package io.primeval.aspecio.internal.service;

import static io.primeval.aspecio.internal.AspecioUtils.asStringProperties;
import static io.primeval.aspecio.internal.AspecioUtils.asStringProperty;
import static io.primeval.aspecio.internal.AspecioUtils.copySet;
import static io.primeval.aspecio.internal.AspecioUtils.firstOrNull;
import static io.primeval.aspecio.internal.AspecioUtils.getIntValue;
import static io.primeval.aspecio.internal.AspecioUtils.getLongValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

import io.primeval.aspecio.AspecioConstants;
import io.primeval.aspecio.AspectDescription;
import io.primeval.aspecio.InterceptorDescription;
import io.primeval.aspecio.internal.logging.AspecioLogger;
import io.primeval.aspecio.internal.logging.AspecioLoggerFactory;
import io.primeval.aspecio.internal.service.AspectInterceptorListener.EventKind;
import io.primeval.reflect.proxy.Interceptor;
import io.primeval.reflect.proxy.composite.Interceptors;

public final class AspectInterceptorManager implements ServiceListener {

    private static final AspecioLogger LOGGER = AspecioLoggerFactory.getLogger(AspectInterceptorManager.class);

    private static final String SERVICE_FILTER = "(" + AspecioConstants.SERVICE_ASPECT + "=*)";

    private final BundleContext bundleContext;

    private final SortedMap<ServiceReference<?>, AspectInterceptor> aspectServiceByServiceRef = Collections
            .synchronizedSortedMap(new TreeMap<>());

    private final Map<String, SortedSet<AspectInterceptor>> aspectServicesByAspectName = new ConcurrentHashMap<>();

    private final List<AspectInterceptorListener> aspectInterceptorListeners = new CopyOnWriteArrayList<>();

    private volatile boolean closed = false;

    public AspectInterceptorManager(BundleContext bundleContext) {
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
            // This might happen if a service arrives between the listener registration and the initial
            // getServiceReferences call
            return;
        }

        String aspect = asStringProperty(reference.getProperty(AspecioConstants.SERVICE_ASPECT));
        Set<String> extraProperties = new LinkedHashSet<>(
                Arrays.asList(asStringProperties(reference.getProperty(AspecioConstants.SERVICE_ASPECT_EXTRAPROPERTIES))));
        int serviceRanking = getIntValue(reference.getProperty(Constants.SERVICE_RANKING), 0);

        Object service = bundleContext.getService(reference);
        if (!(service instanceof Interceptor)) {
            // Don't track aspects that don't implements Interceptor.
            bundleContext.ungetService(reference);
            return;
        }

        LOGGER.debug("Added aspect: {} (extraProps: {})", aspect, extraProperties);

        AspectInterceptor aspectService = new AspectInterceptor(aspect, (Interceptor) service, reference, serviceRanking, extraProperties);
        this.aspectServiceByServiceRef.put(reference, aspectService);

        // Deal with aspect map.
        {
            SortedSet<AspectInterceptor> as = this.aspectServicesByAspectName.computeIfAbsent(aspect, k -> new TreeSet<>());
            AspectInterceptor firstBefore = firstOrNull(as);
            // The trick here is that we use a SortedSet
            // with the right compareTo method on aspectService.
            as.add(aspectService);

            AspectInterceptor firstAfter = firstOrNull(as);

            if (firstAfter != firstBefore) {
                // still in lock, should we?
                fireEvent(EventKind.NEWMATCH, aspect, firstAfter);
            }
        }
    }

    public synchronized void onServiceUpdate(ServiceReference<?> reference) {
        AspectInterceptor aspectService = aspectServiceByServiceRef.get(reference);
        if (aspectService == null) {
            return;
        }

        String newAspect = asStringProperty(reference.getProperty(AspecioConstants.SERVICE_ASPECT));

        Set<String> extraProperties = new LinkedHashSet<>(
                Arrays.asList(asStringProperties(reference.getProperty(AspecioConstants.SERVICE_ASPECT_EXTRAPROPERTIES))));
        int serviceRanking = getIntValue(reference.getProperty(Constants.SERVICE_RANKING), 0);

        boolean rankingChanged = aspectService.serviceRanking != serviceRanking;
        boolean aspectChanged = !Objects.equals(aspectService.aspect, newAspect);
        boolean extraPropsChanged = !Objects.equals(aspectService.extraProperties, extraProperties);

        if (rankingChanged || aspectChanged || extraPropsChanged) {
            if (!aspectChanged) {
                LOGGER.debug("Updating aspect: {} (extraProps: {})", newAspect, extraProperties);
            } else {
                LOGGER.debug("Updating aspect: {} -> {} (extraProps: {})", aspectService.aspect, newAspect, extraProperties);

            }
            AspectInterceptor updatedService = new AspectInterceptor(newAspect, aspectService.interceptor, reference, serviceRanking,
                    extraProperties);
            this.aspectServiceByServiceRef.put(reference, updatedService);

            Iterator<String> aspectsToProcess = Stream.of(aspectService.aspect, newAspect).distinct().iterator();

            while (aspectsToProcess.hasNext()) {
                String aspect = aspectsToProcess.next();
                boolean toPublish = newAspect.equals(aspect);
                SortedSet<AspectInterceptor> as = this.aspectServicesByAspectName.computeIfAbsent(aspect, k -> new TreeSet<>());
                AspectInterceptor firstBefore = firstOrNull(as);

                if (toPublish) {
                    if (rankingChanged) {
                        // special case where we must force the re-ordering
                        // by cleanly removing from the set first
                        as.remove(aspectService);
                    }
                    // The trick here is that we use a SortedSet
                    // with the right compareTo method on aspectService.
                    // It will replace the pre-existing service that has
                    // a different entity, but compareTo() == 0.
                    as.add(updatedService);

                } else {
                    // here it is the *old* service we remove.
                    as.remove(aspectService);
                    // clean-up
                    if (as.isEmpty()) {
                        this.aspectServicesByAspectName.remove(aspect);
                    }
                }

                AspectInterceptor firstAfter = firstOrNull(as);

                if (firstAfter != firstBefore) {
                    // still in lock, should we?
                    fireEvent(firstAfter != null ? EventKind.NEWMATCH : EventKind.NOMATCH, aspect, firstAfter);
                }
            }
        }

    }

    public synchronized void onServiceDeparture(ServiceReference<?> reference) {
        AspectInterceptor aspectService = aspectServiceByServiceRef.get(reference);
        if (aspectService == null) {
            return;
        }
        String aspect = aspectService.aspect;
        LOGGER.debug("Removed aspect: {} (extraProps: {})", aspect, aspectService.extraProperties);
        this.aspectServiceByServiceRef.remove(reference);

        {
            SortedSet<AspectInterceptor> as = aspectServicesByAspectName.get(aspect);
            AspectInterceptor firstBefore = firstOrNull(as);

            if (as != null) {
                as.remove(aspectService);
                if (as.isEmpty()) {
                    aspectServicesByAspectName.remove(aspect);
                }
            }
            AspectInterceptor firstAfter = firstOrNull(as);

            if (firstAfter != firstBefore) {
                // still in lock, should we?
                fireEvent(firstAfter != null ? EventKind.NEWMATCH : EventKind.NOMATCH, aspect, firstAfter);
            }
        }
    }

    public synchronized AspectInterceptorContext getContext(List<String> requiredAspects, List<String> optionalAspects) {
        Set<AspectInterceptor> interceptors = new TreeSet<>(new Comparator<AspectInterceptor>() {

            @Override
            public int compare(AspectInterceptor o1, AspectInterceptor o2) {
                int res = o1.aspect.compareTo(o2.aspect);
                if (res != 0) {
                    return res;
                }
                int a = o1.serviceRanking;
                int b = o2.serviceRanking;
                return (a < b) ? -1 : ((a > b) ? 1 : 0);
            }
        });

        Set<String> satisfiedRequiredAspects = new LinkedHashSet<>();

        Set<String> unsatisfiedRequiredAspects = new LinkedHashSet<>();

        Set<String> satisfiedOptionalAspects = new LinkedHashSet<>();

        Set<String> unsatisfiedOptionalAspects = new LinkedHashSet<>();

        Set<String> extraProperties = new LinkedHashSet<>();

        for (String aspect : requiredAspects) {
            AspectInterceptor aspectInterceptor = getAspectInterceptor(aspect);
            if (aspectInterceptor != null) {
                satisfiedRequiredAspects.add(aspect);
                extraProperties.addAll(aspectInterceptor.extraProperties);
                interceptors.add(aspectInterceptor);
            } else {
                unsatisfiedRequiredAspects.add(aspect);
            }
        }

        for (String aspect : optionalAspects) {
            AspectInterceptor aspectInterceptor = getAspectInterceptor(aspect);
            if (aspectInterceptor != null) {
                satisfiedOptionalAspects.add(aspect);
                extraProperties.addAll(aspectInterceptor.extraProperties);
                interceptors.add(aspectInterceptor);
            } else {
                unsatisfiedOptionalAspects.add(aspect);
            }
        }

        Set<String> satisfiedAspects = new LinkedHashSet<>();
        satisfiedAspects.addAll(satisfiedRequiredAspects);
        satisfiedAspects.addAll(satisfiedOptionalAspects);
        Interceptor interceptor = Interceptors.compose(interceptors.stream().map(ai -> ai.interceptor).iterator());

        AspectInterceptorContext aspectInterceptorContext = new AspectInterceptorContext(interceptor, satisfiedAspects,
                satisfiedRequiredAspects, unsatisfiedRequiredAspects, satisfiedOptionalAspects,
                unsatisfiedOptionalAspects, extraProperties);

        return aspectInterceptorContext;
    }

    private void fireEvent(EventKind eventKind, String aspectName, AspectInterceptor aspectInterceptor) {
        for (AspectInterceptorListener l : aspectInterceptorListeners) {
            l.onAspectChange(eventKind, aspectName, aspectInterceptor);
        }
    }

    public void addListener(AspectInterceptorListener aspectInterceptorListener) {
        aspectInterceptorListeners.add(aspectInterceptorListener);
    }

    public void removeListener(AspectInterceptorListener aspectInterceptorListener) {
        aspectInterceptorListeners.remove(aspectInterceptorListener);
    }

    private AspectInterceptor getAspectInterceptor(String aspectName) {
        return firstOrNull(aspectServicesByAspectName.get(aspectName));
    }

    public synchronized Set<String> getRegisteredAspects() {
        return copySet(aspectServicesByAspectName.keySet());
    }

    public synchronized Optional<AspectDescription> getAspectDescription(String aspectName) {
        SortedSet<AspectInterceptor> ais = aspectServicesByAspectName.get(aspectName);
        if (ais == null || ais.isEmpty()) {
            return Optional.empty();
        }

        Iterator<AspectInterceptor> iterator = ais.iterator();

        AspectInterceptor interceptor = iterator.next();
        InterceptorDescription id = makeInterceptorDescription(interceptor);
        List<InterceptorDescription> backupIds = new ArrayList<>(ais.size() - 1);

        while (iterator.hasNext()) {
            interceptor = iterator.next();
            InterceptorDescription backupId = makeInterceptorDescription(interceptor);
            backupIds.add(backupId);
        }

        AspectDescription ad = new AspectDescription(aspectName, id, backupIds);

        return Optional.of(ad);
    }

    private InterceptorDescription makeInterceptorDescription(AspectInterceptor ai) {
        long serviceId = getLongValue(ai.serviceRef.getProperty(Constants.SERVICE_ID));
        long bundleId = getLongValue(ai.serviceRef.getProperty(Constants.SERVICE_BUNDLEID));

        return new InterceptorDescription(serviceId, bundleId, ai.serviceRanking, ai.interceptor.getClass(),
                copySet(ai.extraProperties));
    }



}