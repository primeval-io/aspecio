package io.primeval.aspecio.internal.weaving.testset.bounds;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public final class BoundsImpl implements BoundsItf {

    @Override
    public List<?> someList1() {
        return Collections.emptyList();
    }

    @Override
    public List<? extends Number> someList2() {
        return Collections.emptyList();
    }

    @Override
    public List<? super Number> someList3() {
        return Collections.emptyList();

    }

    @Override
    public <V extends Number & Runnable> List<V> someList4() {
        return Collections.emptyList();
    }
    
    @Override
    public <V extends Number & Runnable> List<V> singleton(V v) {
        return Collections.singletonList(v);
    }
    
    @Override
    public <V extends Number & Runnable> void foo(V v) {
    }
    
    @Override
    public <V extends Number & Runnable, K extends Supplier<V>> V makeFoo(K k) {
        return k.get();
    }

}
