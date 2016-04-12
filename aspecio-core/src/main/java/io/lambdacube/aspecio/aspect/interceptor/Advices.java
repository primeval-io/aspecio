package io.lambdacube.aspecio.aspect.interceptor;

import java.util.List;

public final class Advices {

    private Advices() {
    }

    public static Advice compose(Advice advice) {
        return advice;
    }

    public static Advice compose(Advice... advices) {
        if (advices.length == 0) {
            return Advice.DEFAULT;
        }
        // no defensive copy, trust the client not to share or mutate this
        // array.
        return new CompositeAdvice(advices);
    }

    public static Advice compose(List<Advice> advices) {
        // no defensive copy, trust the client not to share or mutate this
        // list.
        if (advices.isEmpty()) {
            return Advice.DEFAULT;
        } else if (advices.size() == 1) {
            return advices.get(0);
        } else {
            return new CompositeAdvice(advices.toArray(new Advice[0]));
        }
    }

}
