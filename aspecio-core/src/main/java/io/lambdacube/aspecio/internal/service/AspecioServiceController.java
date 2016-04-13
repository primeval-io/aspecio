package io.lambdacube.aspecio.internal.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.osgi.framework.ServiceReference;

import io.lambdacube.aspecio.internal.logging.AspecioLogger;
import io.lambdacube.aspecio.internal.logging.AspecioLoggerFactory;

public final class AspecioServiceController implements AspectInterceptorListener, WovenServiceListener {
    private static final AspecioLogger LOGGER = AspecioLoggerFactory.getLogger(AspecioServiceController.class);

    private final AspectInterceptorManager aspectInterceptorManager;
    private final ServiceWeavingManager serviceWeavingManager;

    private final Map<ServiceReference<?>, ManagedWovenService> managedServices = new ConcurrentHashMap<>();

    public AspecioServiceController(AspectInterceptorManager aspectInterceptorManager,
            ServiceWeavingManager serviceWeavingManager) {
        this.aspectInterceptorManager = aspectInterceptorManager;
        this.serviceWeavingManager = serviceWeavingManager;
    }

    public void open() {
        aspectInterceptorManager.addListener(this);
        serviceWeavingManager.addListener(this);
        aspectInterceptorManager.open();
        serviceWeavingManager.open();
    }

    public void close() {
        serviceWeavingManager.close();
        aspectInterceptorManager.close();
        aspectInterceptorManager.removeListener(this);
        serviceWeavingManager.removeListener(this);
    }

    @Override
    public void onAspectChange(AspectInterceptorListener.EventKind eventKind, String aspectName, AspectInterceptor aspectInterceptor) {
    }

    @Override
    public void onWovenServiceEvent(WovenServiceEvent event, WovenService wovenService) {
        switch (event.kind) {
        case SERVICE_ARRIVAL:
            handleServiceArrival(wovenService);
            return;

        case SERVICE_UPDATE:
            handleServiceUpdate(wovenService, event.matchesCause(WovenServiceEvent.REQUIRED_ASPECT_CHANGE),
                    event.matchesCause(WovenServiceEvent.OPTIONAL_ASPECT_CHANGE),
                    event.matchesCause(WovenServiceEvent.SERVICE_PROPERTIES_CHANGE));
            return;

        case SERVICE_DEPARTURE:
            handleServiceDeparture(wovenService);
            return;
        }
    }

    private synchronized void handleServiceArrival(WovenService wovenService) {

        ManagedWovenService managedWovenService = new ManagedWovenService();
        ManagedWovenService old = managedServices.put(wovenService.originalReference, managedWovenService);
        if (old != null) {
            LOGGER.warn("Got an old service that we shouldn't for service id {}", wovenService.originalServiceId);
            old.unregister();
        }

        AspectInterceptorContext context = aspectInterceptorManager.getContext(wovenService.requiredAspects,
                wovenService.optionalAspects);

        managedWovenService.wovenService = wovenService;
        managedWovenService.aspectContext = context;

        boolean satisfied = context.unsatisfiedRequiredAspects.isEmpty();
        if (satisfied) {
            managedWovenService.register();
        }

    }

    private synchronized void handleServiceUpdate(WovenService wovenService, boolean requiredAspectsChanged, boolean optionalAspectsChanged,
            boolean servicePropertiesChanged) {
        ManagedWovenService managed = managedServices.get(wovenService.originalReference);
        if (managed == null) {
            LOGGER.warn("Couldn't find an old service while we should for service id {}, treating the update as a new service...(?)",
                    wovenService.originalServiceId);
            handleServiceArrival(wovenService);
            return;
        }

        if (requiredAspectsChanged || optionalAspectsChanged) {
            AspectInterceptorContext context = aspectInterceptorManager.getContext(wovenService.requiredAspects,
                    wovenService.optionalAspects);
            managed.wovenService = wovenService;
            managed.aspectContext = context;
            managed.wovenService.aspecioServiceObject.setInterceptor(context.interceptor);

            boolean satisfied = context.unsatisfiedRequiredAspects.isEmpty();

            if (satisfied) {
                if (managed.registration == null) {
                    // newly satisfied!
                    managed.register();
                }
            } else {
                if (managed.registration != null) {
                    // was satisfied before, but not anymore...!
                    managed.unregister();
                }
            }
        } else if (servicePropertiesChanged) {
            managed.wovenService = wovenService;
            managed.registration.setProperties(managed.getProperties());
        }
    }

    private synchronized void handleServiceDeparture(WovenService wovenService) {
        ManagedWovenService managed = managedServices.remove(wovenService.originalReference);
        if (managed == null) {
            LOGGER.warn("Notified of the departure of a service we couldn't find with service id {}",
                    wovenService.originalServiceId);
            return;
        }
        managed.unregister();
    }

}
