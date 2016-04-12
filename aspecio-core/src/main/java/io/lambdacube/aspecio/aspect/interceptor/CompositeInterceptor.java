package io.lambdacube.aspecio.aspect.interceptor;

public final class CompositeInterceptor implements Interceptor {

    private final Interceptor[] interceptors;

    public CompositeInterceptor(Interceptor[] interceptors) {
        this.interceptors = interceptors;
    }

    @Override
    public Advice onCall(CallContext callContext) {
        Advice advice = Advice.DEFAULT;

        int size = interceptors.length;
        if (size == 1) {
            advice = interceptors[0].onCall(callContext);
        } else if (size > 1) {
            Advice[] advices = new Advice[size];
            for (int i = 0; i < size; i++) {
                Advice adv = interceptors[i].onCall(callContext);
                advices[i] = adv;
            }
            advice = Advices.compose(advices);
        }

        return advice;
    }

}
