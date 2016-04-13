package io.lambdacube.aspecio.internal.weaving;

import java.util.function.Function;

public final class WovenClassHolder {

    public final Class<?> wovenClass;

    public final Function<Object, Woven> weavingFactory;

    public WovenClassHolder(Class<?> wovenClass, Function<Object, Woven> weavingFactory) {
        super();
        this.wovenClass = wovenClass;
        this.weavingFactory = weavingFactory;
    }

}
