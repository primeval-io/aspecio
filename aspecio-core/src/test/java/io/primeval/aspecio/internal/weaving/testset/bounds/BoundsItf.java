package io.primeval.aspecio.internal.weaving.testset.bounds;

import java.util.List;
import java.util.function.Supplier;

public interface BoundsItf {

    List<?> someList1();

    List<? extends Number> someList2();

    List<? super Number> someList3();

    <V extends Number & Runnable> List<V> someList4();

    <V extends Number & Runnable> List<V> singleton(V v);

    <V extends Number & Runnable> void foo(V v);

    <V extends Number & Runnable, K extends Supplier<V>> V makeFoo(K k);

}
