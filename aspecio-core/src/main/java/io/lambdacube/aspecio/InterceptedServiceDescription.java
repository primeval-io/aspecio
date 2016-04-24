package io.lambdacube.aspecio;

import java.util.List;
import java.util.Set;

public final class InterceptedServiceDescription {

    public final long serviceId;
    public final long bundleId;
    public final List<String> objectClass;
    public final boolean published;
    public final Set<String> satisfiedAspects;
    public final Set<String> unsatisfiedRequiredAspects;
    public final Set<String> requiredAspects;
    public final Set<String> optionalAspects;

    public InterceptedServiceDescription(long serviceId, long bundleId, List<String> objectClass, boolean published,
            Set<String> satisfiedAspects, Set<String> unsatisfiedRequiredAspects, Set<String> requiredAspects,
            Set<String> optionalAspects) {
        super();
        this.serviceId = serviceId;
        this.bundleId = bundleId;
        this.objectClass = objectClass;
        this.published = published;
        this.satisfiedAspects = satisfiedAspects;
        this.unsatisfiedRequiredAspects = unsatisfiedRequiredAspects;
        this.requiredAspects = requiredAspects;
        this.optionalAspects = optionalAspects;
    }

}
