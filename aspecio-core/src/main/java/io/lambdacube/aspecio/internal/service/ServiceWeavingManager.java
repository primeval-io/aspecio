package io.lambdacube.aspecio.internal.service;

import static io.lambdacube.aspecio.AspecioConstants.SERVICE_ASPECT_WEAVE;
import static io.lambdacube.aspecio.AspecioConstants.SERVICE_ASPECT_WEAVE_OPTIONAL;
import static io.lambdacube.aspecio.AspecioConstants._SERVICE_ASPECT_WOVEN;
import static io.lambdacube.aspecio.internal.AspecioUtils.asStringProperties;
import static io.lambdacube.aspecio.internal.AspecioUtils.getIntValue;
import static io.lambdacube.aspecio.internal.AspecioUtils.getLongValue;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

import org.osgi.framework.AllServiceListener;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceReference;

import io.lambdacube.aspecio.AspecioConstants;
import io.lambdacube.aspecio.internal.AspecioUtils;
import io.lambdacube.aspecio.internal.logging.AspecioLogger;
import io.lambdacube.aspecio.internal.logging.AspecioLoggerFactory;
import io.lambdacube.aspecio.internal.weaving.AspectWeaver;
import io.lambdacube.aspecio.internal.weaving.Woven;
import io.lambdacube.aspecio.internal.weaving.WovenClassHolder;

public final class ServiceWeavingManager implements AllServiceListener {
    private static final AspecioLogger LOGGER = AspecioLoggerFactory.getLogger(ServiceWeavingManager.class);

    private static final String SERVICE_FILTER = MessageFormat.format("(&(|({0}=*)({1}=*))(!({2}=*)))", SERVICE_ASPECT_WEAVE,
            SERVICE_ASPECT_WEAVE_OPTIONAL, _SERVICE_ASPECT_WOVEN);

    private final Map<ServiceReference<?>, WovenService> wovenServiceByServiceRef = Collections.synchronizedSortedMap(new TreeMap<>());
    private final Map<String, List<WovenService>> wovenServicesByAspect = new ConcurrentHashMap<>();
    public final List<WovenServiceListener> wovenServiceListeners = new CopyOnWriteArrayList<>();

    private final BundleContext bundleContext;

    private volatile boolean closed = false;

    public ServiceWeavingManager(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public void open() {
        try {
            this.bundleContext.addServiceListener(this, SERVICE_FILTER);

            ServiceReference<?>[] serviceReferences = this.bundleContext.getAllServiceReferences((String) null, SERVICE_FILTER);

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
            for (ServiceReference<?> sr : wovenServiceByServiceRef.keySet()) {
                this.bundleContext.ungetService(sr);
            }
            wovenServiceByServiceRef.clear();
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

    private synchronized void onServiceRegistration(ServiceReference<?> reference) {
        if (wovenServiceByServiceRef.containsKey(reference)) {
            // This might happen if a service arrives between the listener registration and the initial getAllServiceReferences call
            return;
        }

        long originalServiceId = getLongValue(reference.getProperty(Constants.SERVICE_ID));

        LOGGER.debug("Preparing the weaving service id {} provided by {}", originalServiceId, reference.getBundle().getSymbolicName());

        List<String> requiredAspectsToWeave = new ArrayList<>(
                Arrays.asList(asStringProperties(reference.getProperty(AspecioConstants.SERVICE_ASPECT_WEAVE))));
        List<String> optionalAspectsToWeave = new ArrayList<>(
                Arrays.asList(asStringProperties(reference.getProperty(AspecioConstants.SERVICE_ASPECT_WEAVE_OPTIONAL))));
        List<String> objectClass = new ArrayList<>(Arrays.asList(asStringProperties(reference.getProperty(Constants.OBJECTCLASS))));
        int serviceRanking = getIntValue(reference.getProperty(Constants.SERVICE_RANKING), 0);
        ServiceScope serviceScope = ServiceScope.fromString(AspecioUtils.asStringProperty(reference.getProperty(Constants.SERVICE_SCOPE)));

        // Keep original properties, except for managed ones.
        Hashtable<String, Object> serviceProperties = new Hashtable<>();
        for (String key : reference.getPropertyKeys()) {
            Object val = reference.getProperty(key);
            switch (key) {
            case Constants.SERVICE_ID:
            case Constants.SERVICE_PID:
            case Constants.SERVICE_BUNDLEID:
            case Constants.SERVICE_RANKING:
            case Constants.OBJECTCLASS:
            case Constants.SERVICE_SCOPE:
            case AspecioConstants.SERVICE_ASPECT_WEAVE:
            case AspecioConstants.SERVICE_ASPECT_WEAVE_OPTIONAL:
                continue;
            default:
                serviceProperties.put(key, val);
            }
        }
        serviceRanking++;

        // Check if we can weave it
        List<Class<?>> interfaces = new ArrayList<>();
        for (String intf : objectClass) {
            try {
                Class<?> cls = reference.getBundle().loadClass(intf);
                if (!cls.isInterface()) {
                    // Cannot weave!
                    LOGGER.warn("Cannot weave service id {} because it provides service that are not interfaces, such as {}",
                            originalServiceId, cls.getName());
                    bundleContext.ungetService(reference);
                    return;
                }
                interfaces.add(cls);
            } catch (ClassNotFoundException e) {
                // Should not happen
                LOGGER.error("Could not find class, not weaving service id {}", originalServiceId, e);
                bundleContext.ungetService(reference);
                return;
            }
        }

        serviceProperties.put(Constants.SERVICE_RANKING, serviceRanking);

        // This property is set to recognize the registration as a proxy, so it's not proxied again
        // TODO add on publish
        // serviceProperties.put(_SERVICE_ASPECT_WOVEN, requiredAspectsToWeave.toArray(new String[0]));

        AspecioServiceObject aspecioServiceObject = new AspecioServiceObject(serviceScope, reference,
                originalService -> weave(interfaces, originalService));

        WovenService wovenService = new WovenService(originalServiceId, requiredAspectsToWeave, optionalAspectsToWeave, reference,
                objectClass, serviceProperties, aspecioServiceObject);
        wovenServiceByServiceRef.put(reference, wovenService);

        Iterator<String> aspectIt = Stream.concat(requiredAspectsToWeave.stream(), optionalAspectsToWeave.stream()).distinct().iterator();
        while (aspectIt.hasNext()) {
            String aspect = aspectIt.next();
            List<WovenService> wovenServices = wovenServicesByAspect.computeIfAbsent(aspect, k -> new ArrayList<>());
            wovenServices.add(wovenService);
        }

        fireEvent(WovenServiceEvent.SERVICE_REGISTRATION, wovenService);
    }

    private synchronized void onServiceUpdate(ServiceReference<?> reference) {
        WovenService wovenService = wovenServiceByServiceRef.get(reference);
        if (wovenService == null) {
            return;
        }

        List<String> requiredAspectsToWeave = new ArrayList<>(
                Arrays.asList(asStringProperties(reference.getProperty(AspecioConstants.SERVICE_ASPECT_WEAVE))));
        List<String> optionalAspectsToWeave = new ArrayList<>(
                Arrays.asList(asStringProperties(reference.getProperty(AspecioConstants.SERVICE_ASPECT_WEAVE_OPTIONAL))));
        int serviceRanking = getIntValue(reference.getProperty(Constants.SERVICE_RANKING), 0);

        // Keep original properties, except for managed ones.
        Hashtable<String, Object> serviceProperties = new Hashtable<>();
        for (String key : reference.getPropertyKeys()) {
            Object val = reference.getProperty(key);
            switch (key) {
            case Constants.SERVICE_ID:
            case Constants.SERVICE_PID:
            case Constants.SERVICE_BUNDLEID:
            case Constants.SERVICE_RANKING:
            case Constants.OBJECTCLASS:
            case Constants.SERVICE_SCOPE:
            case AspecioConstants.SERVICE_ASPECT_WEAVE:
            case AspecioConstants.SERVICE_ASPECT_WEAVE_OPTIONAL:
                continue;
            default:
                serviceProperties.put(key, val);
            }
        }
        serviceRanking++;
        serviceProperties.put(Constants.SERVICE_RANKING, serviceRanking);

        boolean requiredAspectsChanged = !Objects.equals(requiredAspectsToWeave, wovenService.requiredAspects);
        boolean optionalAspectsChanged = !Objects.equals(optionalAspectsToWeave, wovenService.optionalAspects);
        boolean servicePropertiesChanged = !Objects.equals(serviceProperties, wovenService.serviceProperties);

        WovenService updatedWovenService = wovenService.update(requiredAspectsToWeave, optionalAspectsToWeave, serviceProperties);

        int mask = requiredAspectsChanged ? WovenServiceEvent.REQUIRED_ASPECT_CHANGE : 0;
        mask |= optionalAspectsChanged ? WovenServiceEvent.OPTIONAL_ASPECT_CHANGE : 0;
        mask |= servicePropertiesChanged ? WovenServiceEvent.SERVICE_PROPERTIES_CHANGE : 0;

        if (mask != 0) {
            fireEvent(new WovenServiceEvent(WovenServiceEvent.EventKind.SERVICE_UPDATE, mask), updatedWovenService);
        }

    }

    private synchronized void onServiceDeparture(ServiceReference<?> reference) {
        WovenService wovenService = wovenServiceByServiceRef.get(reference);
        if (wovenService == null) {
            return;
        }

        fireEvent(WovenServiceEvent.SERVICE_DEPARTURE, wovenService);

    }

    private Woven weave(List<Class<?>> interfaces, Object delegateToWeave) {
        WovenClassHolder wovenClassHolder = AspectWeaver.weave(delegateToWeave.getClass(), interfaces.toArray(new Class<?>[0]));
        return wovenClassHolder.weavingFactory.apply(delegateToWeave);
    }

    private void fireEvent(WovenServiceEvent event, WovenService wovenService) {
        wovenServiceListeners.forEach(l -> l.onWovenServiceEvent(event, wovenService));
    }

    public void addListener(WovenServiceListener wovenServiceListener) {
        wovenServiceListeners.add(wovenServiceListener);
    }

    public void removeListener(WovenServiceListener wovenServiceListener) {
        wovenServiceListeners.remove(wovenServiceListener);
    }
}
