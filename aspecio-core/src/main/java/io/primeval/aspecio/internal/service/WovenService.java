package io.primeval.aspecio.internal.service;

import java.util.Hashtable;
import java.util.List;

import org.osgi.framework.ServiceReference;

public final class WovenService {

    public final long originalServiceId;

    public final List<String> requiredAspects;

    public final List<String> optionalAspects;

    public final ServiceReference<?> originalReference;

    public final List<String> objectClass;

    public final Hashtable<String, Object> serviceProperties;

    public final AspecioServiceObject aspecioServiceObject;

    public WovenService(long originalServiceId, List<String> requiredAspectsToWeave, List<String> optionalAspectsToWeave,
            ServiceReference<?> originalReference, List<String> objectClass, Hashtable<String, Object> serviceProperties,
            AspecioServiceObject aspecioServiceObject) {
        this.requiredAspects = requiredAspectsToWeave;
        this.optionalAspects = optionalAspectsToWeave;
        this.originalReference = originalReference;
        this.originalServiceId = originalServiceId;
        this.objectClass = objectClass;
        this.serviceProperties = serviceProperties;
        this.aspecioServiceObject = aspecioServiceObject;
    }

    public WovenService update(List<String> requiredAspects, List<String> optionalAspects, Hashtable<String, Object> serviceProperties) {
        return new WovenService(this.originalServiceId, requiredAspects, optionalAspects, this.originalReference, this.objectClass,
                serviceProperties, this.aspecioServiceObject);
    }
}
