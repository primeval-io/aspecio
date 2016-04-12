package io.lambdacube.aspecio.internal.service;

import java.util.List;

import io.lambdacube.aspecio.aspect.interceptor.Interceptor;

public final class AspectService {

    public final Interceptor interceptor;

    public final List<String> aspects;

    public final List<String> extraProperties;

    public AspectService(Interceptor interceptor, List<String> aspects, List<String> extraProperties) {
        super();
        this.interceptor = interceptor;
        this.aspects = aspects;
        this.extraProperties = extraProperties;
    }

}
