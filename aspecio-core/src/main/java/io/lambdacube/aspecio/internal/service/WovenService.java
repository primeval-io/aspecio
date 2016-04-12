package io.lambdacube.aspecio.internal.service;

import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

public final class WovenService {

    public final ServiceReference<?> originalReference;

    public final ServiceRegistration<?> wovenRegistration;

    public final Object original;

    public final Object woven;

    public WovenService(ServiceReference<?> originalReference, ServiceRegistration<?> wovenRegistration, Object original, Object woven) {
        super();
        this.originalReference = originalReference;
        this.wovenRegistration = wovenRegistration;
        this.original = original;
        this.woven = woven;
    }

}
