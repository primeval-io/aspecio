package io.lambdacube.aspecio.internal.service;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.PrototypeServiceFactory;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

import io.lambdacube.aspecio.aspect.interceptor.Interceptor;
import io.lambdacube.aspecio.internal.weaving.shared.Woven;

public final class AspecioServiceObject {
    private final ServiceScope serviceScope;
    private final ServiceReference<?> originalRef;
    private final Function<Object, Woven> proxyFunction;
    private final List<Woven> instances = new CopyOnWriteArrayList<>();
    // Try to de-duplicate for servicefactories that are just lazy singletons.
    private final ServicePool<Woven> servicePool = new ServicePool<>();
    private Object serviceToRegister;
    private volatile Interceptor interceptor = Interceptor.NOOP;

    public AspecioServiceObject(ServiceScope serviceScope, ServiceReference<?> originalRef,
            Function<Object, Woven> proxyFunction) {
        this.serviceScope = serviceScope;
        this.originalRef = originalRef;
        this.proxyFunction = proxyFunction;
    }

    public void setInterceptor(Interceptor interceptor) {
        this.interceptor = interceptor;
        for (Woven w : instances) {
            w.setInterceptor(interceptor);
        }
    }

    public synchronized Object getServiceObjectToRegister() {
        if (serviceToRegister == null) {
            serviceToRegister = makeServiceObjectToRegister();
        }
        return serviceToRegister;
    }

    private Object makeServiceObjectToRegister() {
        switch (serviceScope) {
        case PROTOTYPE:
            return new PrototypeServiceFactory<Woven>() {
                @Override
                public Woven getService(Bundle bundle, ServiceRegistration<Woven> registration) {
                    Object originalService = bundle.getBundleContext().getService(originalRef);
                    Woven instance = proxyFunction.apply(originalService);
                    instance.setInterceptor(interceptor);
                    instances.add(instance);
                    return instance;
                }

                @Override
                public void ungetService(Bundle bundle, ServiceRegistration<Woven> registration, Woven service) {
                    instances.remove(service);
                    BundleContext bundleContext = bundle.getBundleContext();
                    // If bundle is still there, let's unget, otherwise ignore.
                    if (bundleContext != null) {
                        bundleContext.ungetService(originalRef);
                    }
                }
            };

        case BUNDLE:
            return new ServiceFactory<Woven>() {
                @Override
                public Woven getService(Bundle bundle, ServiceRegistration<Woven> registration) {
                    Object originalService = bundle.getBundleContext().getService(originalRef);
                    return servicePool.get(originalService, () -> {
                        Woven woven = proxyFunction.apply(originalService);
                        woven.setInterceptor(interceptor);
                        instances.add(woven);
                        return woven;
                    });
                }

                @Override
                public void ungetService(Bundle bundle, ServiceRegistration<Woven> registration, Woven service) {
                    boolean empty = servicePool.unget(service);
                    if (empty) {
                        instances.remove(service);
                    }
                    BundleContext bundleContext = bundle.getBundleContext();
                    // If bundle is still there, let's unget, otherwise ignore.
                    if (bundleContext != null) {
                        bundleContext.ungetService(originalRef);
                    }
                }
            };
        default:
            Object originalService = originalRef.getBundle().getBundleContext().getService(originalRef);
            Woven instance = proxyFunction.apply(originalService);
            instance.setInterceptor(interceptor);
            instances.add(instance);
            return instance;
        }

    }
}
