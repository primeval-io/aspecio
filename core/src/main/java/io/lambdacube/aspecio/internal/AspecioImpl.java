package io.lambdacube.aspecio.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.PrototypeServiceFactory;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.hooks.service.EventListenerHook;
import org.osgi.framework.hooks.service.FindHook;
import org.osgi.framework.hooks.service.ListenerHook.ListenerInfo;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import io.lambdacube.aspecio.AspecioConstants;
import io.lambdacube.aspecio.internal.logging.AspecioLogger;
import io.lambdacube.aspecio.internal.logging.AspecioLoggerFactory;
import io.lambdacube.aspecio.internal.weaving.AspectWeaver;
import io.lambdacube.aspecio.internal.weaving.WovenClassHolder;

public class AspecioImpl implements FindHook, EventListenerHook {

    private static final AspecioLogger LOGGER = AspecioLoggerFactory.getLogger(AspecioImpl.class);

    private static final String SERVICE_ASPECT_WOVEN = ".service.aspect.woven";

    private long bundleId;
    private ServiceTracker<Object, Object> aspectWeavingServiceTracker;

    private Map<ServiceReference<?>, ServiceRegistration<?>> wovenServices = Collections.synchronizedSortedMap(new TreeMap<>());

    public void activate(BundleContext bundleContext) throws InvalidSyntaxException {
        LOGGER.info("Activating Aspecio");
        this.bundleId = bundleContext.getBundle().getBundleId();
        aspectWeavingServiceTracker = new ServiceTracker<>(bundleContext,
                bundleContext.createFilter("(" + AspecioConstants.SERVICE_ASPECT_WEAVE + "=*)"),
                new ServiceTrackerCustomizer<Object, Object>() {

                    @Override
                    public Object addingService(ServiceReference<Object> reference) {
                        BundleContext registeringBc = reference.getBundle().getBundleContext();

                        Object delegateToWeave = registeringBc.getService(reference);

                        LOGGER.debug("Weaving service {} of class {}", reference.getProperty(Constants.SERVICE_ID),
                                delegateToWeave.getClass().getName());

                        Dictionary<String, Object> newProps = new Hashtable<>();

                        String[] objectClass = null;
                        Integer ranking = 0;
                        boolean prototype = false;

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
                            case Constants.SERVICE_SCOPE:
                                if (val instanceof String) {
                                    String strVal = (String) val;
                                    if (Constants.SCOPE_PROTOTYPE.equals(strVal)) {
                                        prototype = true;
                                    }
                                }
                                newProps.put(Constants.SERVICE_SCOPE, val);
                                break;
                            case Constants.OBJECTCLASS:
                                if (val instanceof String[]) {
                                    objectClass = (String[]) val;
                                } else if (val instanceof String) {
                                    objectClass = new String[] { (String) val };
                                }
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
                                            "Cannot weave service reference {} of class {} because it provides service that are not interfaces!",
                                            reference, delegateToWeave.getClass());
                                    return null;
                                }
                                interfaces.add(cls);
                            } catch (ClassNotFoundException e) {
                                // Should not happen
                                LOGGER.error("Could not find class", e);
                            }
                        }

                        newProps.put(Constants.SERVICE_RANKING, ranking);

                        // This property is set to recognize the registration as
                        // a proxy, so it's not
                        // proxied again
                        newProps.put(SERVICE_ASPECT_WOVEN, Boolean.TRUE);

                        if (prototype) {

                            PrototypeServiceFactory<Object> psf = new PrototypeServiceFactory<Object>() {
                                @Override
                                public Object getService(Bundle bundle, ServiceRegistration<Object> registration) {
                                    return weave(bundle, interfaces, delegateToWeave);
                                }

                                @Override
                                public void ungetService(Bundle bundle, ServiceRegistration<Object> registration, Object service) {
                                }
                            };
                            ServiceRegistration<?> serviceRegistration = registeringBc.registerService(objectClass, psf, newProps);
                            wovenServices.put(reference, serviceRegistration);
                        } else {
                            Object proxy = weave(reference.getBundle(), interfaces, delegateToWeave);

                            ServiceRegistration<?> serviceRegistration = registeringBc.registerService(objectClass, proxy, newProps);
                            wovenServices.put(reference, serviceRegistration);
                        }

                        return delegateToWeave;
                    }

                    @Override
                    public void modifiedService(ServiceReference<Object> reference, Object service) {
                        ServiceRegistration<?> wovenServiceRegistration = wovenServices.get(reference);
                        if (wovenServiceRegistration == null) {
                            LOGGER.warn("Got notified about a service of class we don't know about, but should...", service.getClass());
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

                        // This property is set to recognize the registration as
                        // a proxy, so it's not
                        // proxied again
                        newProps.put(SERVICE_ASPECT_WOVEN, Boolean.TRUE);

                        wovenServiceRegistration.setProperties(newProps);

                    }

                    @Override
                    public void removedService(ServiceReference<Object> reference, Object service) {
                        ServiceRegistration<?> wovenServiceRegistration = wovenServices.get(reference);
                        if (wovenServiceRegistration == null) {
                            LOGGER.warn("Got notified about a service of class we don't know about, but should...", service.getClass());
                            return;
                        }
                        
                        // Don't wanna weave anymore..?
                        if (reference.getProperty(AspecioConstants.SERVICE_ASPECT_WEAVE) == null) {
                            wovenServiceRegistration.unregister();
                            wovenServices.remove(reference);
                            return;
                        }
                    }
                });
        aspectWeavingServiceTracker.open(true);
        LOGGER.info("Aspecio activated");

    }

    public void deactivate() {
        aspectWeavingServiceTracker.close();
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

    private Object weave(Bundle bundle, List<Class<?>> interfaces, Object delegateToWeave) {
//        BundleWiring bw = bundle.adapt(BundleWiring.class);
//        ClassLoader cl = bw.getClassLoader();
        WovenClassHolder wovenClassHolder = AspectWeaver.weave(delegateToWeave.getClass(), interfaces.toArray(new Class<?>[0]));
        return wovenClassHolder.weavingFactory.apply(delegateToWeave);
    }
}
