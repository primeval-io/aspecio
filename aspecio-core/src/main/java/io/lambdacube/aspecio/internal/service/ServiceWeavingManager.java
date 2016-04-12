package io.lambdacube.aspecio.internal.service;

import static io.lambdacube.aspecio.AspecioConstants.SERVICE_ASPECT_WEAVE;
import static io.lambdacube.aspecio.AspecioConstants.SERVICE_ASPECT_WEAVE_OPTIONAL;
import static io.lambdacube.aspecio.AspecioConstants._SERVICE_ASPECT_WOVEN;
import static io.lambdacube.aspecio.internal.AspecioUtils.asStringProperties;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.osgi.framework.AllServiceListener;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.PrototypeServiceFactory;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

import io.lambdacube.aspecio.AspecioConstants;
import io.lambdacube.aspecio.internal.logging.AspecioLogger;
import io.lambdacube.aspecio.internal.logging.AspecioLoggerFactory;
import io.lambdacube.aspecio.internal.weaving.AspectWeaver;
import io.lambdacube.aspecio.internal.weaving.WovenClassHolder;

public final class ServiceWeavingManager implements AllServiceListener {
    private static final AspecioLogger LOGGER = AspecioLoggerFactory.getLogger(ServiceWeavingManager.class);

    private static final String SERVICE_FILTER = MessageFormat.format("(&(|({0}=*)({1}=*))(!({2}=*)))", SERVICE_ASPECT_WEAVE,
            SERVICE_ASPECT_WEAVE_OPTIONAL, _SERVICE_ASPECT_WOVEN);

    private Map<ServiceReference<?>, ServiceRegistration<?>> wovenServices = Collections.synchronizedSortedMap(new TreeMap<>());

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
            for (ServiceReference<?> sr : wovenServices.keySet()) {
                this.bundleContext.ungetService(sr);
            }
            wovenServices.clear();
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
        BundleContext registeringBc = reference.getBundle().getBundleContext();

        Object delegateToWeave = registeringBc.getService(reference);

        LOGGER.debug("Weaving service {} of class {}", reference.getProperty(Constants.SERVICE_ID),
                delegateToWeave.getClass().getName());

        Dictionary<String, Object> newProps = new Hashtable<>();
        String[] requiredAspectsToWeave = new String[0];
        String[] optionalAspectsToWeave = new String[0];

        String[] objectClass = null;
        Integer ranking = 0;
        ServiceScope serviceScope = ServiceScope.SINGLETON;

        for (String key : reference.getPropertyKeys()) {
            Object val = reference.getProperty(key);
            switch (key) {
            case Constants.SERVICE_ID:
            case Constants.SERVICE_PID:
            case Constants.SERVICE_BUNDLEID:
                break;
            case AspecioConstants.SERVICE_ASPECT_WEAVE:
                requiredAspectsToWeave = asStringProperties(val);
                break;
            case AspecioConstants.SERVICE_ASPECT_WEAVE_OPTIONAL:
                optionalAspectsToWeave = asStringProperties(val);
                break;
            case Constants.SERVICE_RANKING:
                if (val instanceof Integer)
                    ranking = (Integer) val;
                break;
            case Constants.SERVICE_SCOPE:
                if (val instanceof String) {
                    String strVal = (String) val;
                    if (Constants.SCOPE_PROTOTYPE.equals(strVal)) {
                        serviceScope = ServiceScope.PROTOTYPE;
                    } else if (Constants.SCOPE_BUNDLE.equals(strVal)) {
                        serviceScope = ServiceScope.BUNDLE;
                    }
                }
                newProps.put(Constants.SERVICE_SCOPE, val);
                break;
            case Constants.OBJECTCLASS:
                objectClass = asStringProperties(val);
                break;
            default:
                newProps.put(key, reference.getProperty(key));
                break;
            }
        }
        ranking++;

        List<Class<?>> interfaces = new ArrayList<>();
        for (String intf : objectClass) {
            try {
                Class<?> cls = reference.getBundle().loadClass(intf);
                if (!cls.isInterface()) {
                    // Cannot weave!
                    LOGGER.warn(
                            "Cannot weave service class {} because it provides service that are not interfaces, such as {}",
                            delegateToWeave.getClass().getName(), cls.getName());
                    bundleContext.ungetService(reference);
                    return;
                }
                interfaces.add(cls);
            } catch (ClassNotFoundException e) {
                // Should not happen
                LOGGER.error("Could not find class, not weaving service of class {}", delegateToWeave.getClass().getName(), e);
                bundleContext.ungetService(reference);
                return;
            }
        }

        newProps.put(Constants.SERVICE_RANKING, ranking);

        // This property is set to recognize the registration as a proxy, so it's not proxied again
        newProps.put(_SERVICE_ASPECT_WOVEN, requiredAspectsToWeave);

        Object toRegister = null;
        switch (serviceScope) {
        case PROTOTYPE:
            toRegister = new PrototypeServiceFactory<Object>() {
                @Override
                public Object getService(Bundle bundle, ServiceRegistration<Object> registration) {
                    return weave(bundle, interfaces, delegateToWeave);
                }

                @Override
                public void ungetService(Bundle bundle, ServiceRegistration<Object> registration, Object service) {
                    //
                }
            };
            break;

        case BUNDLE:
            toRegister = new ServiceFactory<Object>() {

                @Override
                public Object getService(Bundle bundle, ServiceRegistration<Object> registration) {
                    return weave(bundle, interfaces, delegateToWeave);
                }

                @Override
                public void ungetService(Bundle bundle, ServiceRegistration<Object> registration, Object service) {
                    //
                }
            };
            break;
        default:
            toRegister = weave(reference.getBundle(), interfaces, delegateToWeave);

            break;
        }

        ServiceRegistration<?> serviceRegistration = registeringBc.registerService(objectClass, toRegister, newProps);
        wovenServices.put(reference, serviceRegistration);

    }

    private synchronized void onServiceUpdate(ServiceReference<?> reference) {
        ServiceRegistration<?> wovenServiceRegistration = wovenServices.get(reference);
        if (wovenServiceRegistration == null) {
            LOGGER.warn("Got notified about a service of class we don't know about, but should...");
            return;
        }

        // Don't wanna weave anymore..?
        if (reference.getProperty(AspecioConstants.SERVICE_ASPECT_WEAVE) == null) {
            wovenServiceRegistration.unregister();
            wovenServices.remove(reference);
            return;
        }

        Dictionary<String, Object> newProps = new Hashtable<>();

        String[] objectClass = null;
        Integer ranking = 0;

        for (String key : reference.getPropertyKeys()) {
            Object val = reference.getProperty(key);
            switch (key) {
            case Constants.SERVICE_ID:
            case Constants.SERVICE_PID:
            case Constants.SERVICE_BUNDLEID:
            case AspecioConstants.SERVICE_ASPECT_WEAVE:
                break;
            case Constants.SERVICE_RANKING:
                if (val instanceof Integer)
                    ranking = (Integer) val;
                break;
            case Constants.OBJECTCLASS:
                if (val instanceof String[]) {
                    objectClass = (String[]) val;
                } else if (val instanceof String) {
                    objectClass = new String[] { (String) val };
                }
                break;
            default:
                newProps.put(key, val);
                break;
            }
        }
        ranking++;

        List<Class<?>> interfaces = new ArrayList<>();
        for (String intf : objectClass) {
            try {
                Class<?> cls = reference.getBundle().loadClass(intf);
                if (!cls.isInterface()) {
                    return;
                }
                interfaces.add(cls);
            } catch (ClassNotFoundException e) {
                // Should not happen
                LOGGER.error("Could not find class", e);
            }
        }

        newProps.put(Constants.SERVICE_RANKING, ranking);

        newProps.put(_SERVICE_ASPECT_WOVEN, Boolean.TRUE);

        wovenServiceRegistration.setProperties(newProps);

    }

    private synchronized void onServiceDeparture(ServiceReference<?> reference) {
        ServiceRegistration<?> wovenServiceRegistration = wovenServices.get(reference);
        if (wovenServiceRegistration == null) {
            LOGGER.warn("Got notified about a service of class we don't know about, but should...");
            return;
        }

        // Don't wanna weave anymore..?
        if (reference.getProperty(AspecioConstants.SERVICE_ASPECT_WEAVE) == null) {
            wovenServiceRegistration.unregister();
            wovenServices.remove(reference);
            return;
        }
    }

    private Object weave(Bundle bundle, List<Class<?>> interfaces, Object delegateToWeave) {
        // BundleWiring bw = bundle.adapt(BundleWiring.class);
        // ClassLoader cl = bw.getClassLoader();
        WovenClassHolder wovenClassHolder = AspectWeaver.weave(delegateToWeave.getClass(), interfaces.toArray(new Class<?>[0]));
        return wovenClassHolder.weavingFactory.apply(delegateToWeave);
    }
}
