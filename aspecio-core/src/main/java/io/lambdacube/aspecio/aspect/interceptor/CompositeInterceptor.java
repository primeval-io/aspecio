package io.lambdacube.aspecio.aspect.interceptor;

import java.util.Iterator;
import java.util.stream.Stream;

public final class CompositeInterceptor implements Interceptor {

    private final Interceptor[] interceptors;
    private String repr;

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

    @Override
    public String toString() {
        if (repr == null) {
            repr = new StringBuilder().append("Composite{").append(String.join(",", new Iterable<String>() {
                public Iterator<String> iterator() {
                    return Stream.of(interceptors).map(Object::toString).iterator();
                }
            })).append('}').toString();
        }
        return repr;

    }

}
