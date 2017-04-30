package io.primeval.aspecio.internal.weaving.testset.annotated;

import io.primeval.aspecio.internal.weaving.testset.api.annotation.CompileAnn;
import io.primeval.aspecio.internal.weaving.testset.api.annotation.MyChoices;
import io.primeval.aspecio.internal.weaving.testset.api.annotation.NestedAnn;
import io.primeval.aspecio.internal.weaving.testset.api.annotation.NestingAnn;
import io.primeval.aspecio.internal.weaving.testset.api.annotation.RuntimeMethodAnn;
import io.primeval.aspecio.internal.weaving.testset.api.annotation.RuntimeTypeAnn;
import io.primeval.aspecio.internal.weaving.testset.api.annotation.RuntimeTypeEnumAnn;
import io.primeval.aspecio.internal.weaving.testset.api.annotation.WebTag;

@CompileAnn
@RuntimeTypeAnn(someArray = { "foo", "bar" }, who = "him")
@RuntimeTypeEnumAnn(value = MyChoices.THAT)
@NestingAnn({ @NestedAnn("A"), @NestedAnn("B") })
public final class AnnotatedService {

    @RuntimeMethodAnn(path = "/test")
    @WebTag(name = "foo") @WebTag(name = "bar")
    public String someMethod() {
        return "foo";
    }
    
    
}
