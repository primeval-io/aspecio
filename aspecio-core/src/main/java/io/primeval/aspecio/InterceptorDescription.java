package io.lambdacube.aspecio;

import java.util.Set;

/**
 * A class describing an interceptor as seen by Aspecio.
 */
public final class InterceptorDescription {

    public final long serviceId;
    public final long bundleId;
    public final int serviceRanking;
    public final Class<?> interceptorClass;
    public final Set<String> extraProperties;

    public InterceptorDescription(long serviceId, long bundleId, int serviceRanking, Class<?> interceptorClass,
            Set<String> extraProperties) {
        super();
        this.serviceId = serviceId;
        this.bundleId = bundleId;
        this.serviceRanking = serviceRanking;
        this.interceptorClass = interceptorClass;
        this.extraProperties = extraProperties;
    }
}
