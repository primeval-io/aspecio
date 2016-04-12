package io.lambdacube.aspecio.internal.service;

import java.util.List;

import org.osgi.framework.ServiceReference;

import io.lambdacube.aspecio.aspect.interceptor.Interceptor;

public final class AspectService implements Comparable<AspectService> {

    public final Interceptor interceptor;

    public final ServiceReference<?> serviceRef;

    public final int serviceRanking;

    public final List<String> aspects;

    public final List<String> extraProperties;

    public AspectService(Interceptor interceptor, ServiceReference<?> serviceRef, int serviceRanking, List<String> aspects,
            List<String> extraProperties) {
        super();
        this.interceptor = interceptor;
        this.serviceRef = serviceRef;
        this.serviceRanking = serviceRanking;
        this.aspects = aspects;
        this.extraProperties = extraProperties;
    }

    @Override
    public int compareTo(AspectService o) {
        return serviceRef.compareTo(o.serviceRef);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((serviceRef == null) ? 0 : serviceRef.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AspectService other = (AspectService) obj;
        if (serviceRef == null) {
            if (other.serviceRef != null)
                return false;
        } else if (!serviceRef.equals(other.serviceRef))
            return false;
        return true;
    }

}
