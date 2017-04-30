package io.lambdacube.aspecio.internal.service;

import java.util.Set;

import io.lambdacube.aspecio.aspect.interceptor.Interceptor;

public final class AspectInterceptorContext {

    public final Interceptor interceptor;

    public final Set<String> satisfiedAspects;

    public final Set<String> satisfiedRequiredAspects;

    public final Set<String> unsatisfiedRequiredAspects;

    public final Set<String> satisfiedOptionalAspects;

    public final Set<String> unsatisfiedOptionalAspects;

    public final Set<String> extraProperties;

    public AspectInterceptorContext(Interceptor interceptor, Set<String> satisfiedAspects, Set<String> satisfiedRequiredAspects,
            Set<String> unsatisfiedRequiredAspects, Set<String> satisfiedOptionalAspects, Set<String> unsatisfiedOptionalAspects,
            Set<String> extraProperties) {
        super();
        this.interceptor = interceptor;
        this.satisfiedAspects = satisfiedAspects;
        this.satisfiedRequiredAspects = satisfiedRequiredAspects;
        this.unsatisfiedRequiredAspects = unsatisfiedRequiredAspects;
        this.satisfiedOptionalAspects = satisfiedOptionalAspects;
        this.unsatisfiedOptionalAspects = unsatisfiedOptionalAspects;
        this.extraProperties = extraProperties;
    }

}
