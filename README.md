[![Build Status](https://api.travis-ci.org/Lambdacube/aspecio.png)](https://api.travis-ci.org/Lambdacube/aspecio/)

# Aspecio

Aspecio is a Java/OSGi R6 'micro-framework' that brings a mix of component-oriented and aspect-oriented programming to your application. Aspecio lets you define _Aspects_ that you can later pick to add behavior to your components and avoid duplicating boilerplate dealing with cross-cutting concerns.


**Disclaimer**

This documentation is a work-in-progress. Aspecio is ready for a first release, but do not hesitate to contact me for questions. I will update the documentation accordingly.

## Documentation

Aside from this page, [Aspecio's Javadoc](http://lambdacube.github.io/aspecio/javadoc/) is complete and provides a good overview. 

## Overview


### Why Aspects?

In general, aspects allow you to intercept code and alter its execution. There are a number of downsides to using aspects:
* Scattering behavior across the code base ;
* Making the execution model opaque by having aspects intercept any random piece of code, including internal code that might have implicit invariants that aspects break ;
* Not knowing **which** aspects are being _woven_ on a piece of code at a given time ;
* Having some aspect framework implementations _weave_ aspects into one big bytecode muddy-ball, making debugging difficult when line numbers are desynchronized, adding synthetic methods in the bytecode.
* Sometimes aspects are implemented using JDK Proxies, which can break consuming code, for example code relying on reflection such as annotation-driven frameworks.

However there are _cross-cutting concerns_ for which aspects can be very useful, for example:
* Security: ensuring some conditions are met before being allowed into a function ; 
* Metrics: having live metrics on key components (e.g using Coda Hale's Metrics library) ;
* Ensuring a piece of code takes place in a transaction ;
* And more :-)

Aspecio aims to make aspects predictable and bridges them with the OSGi service model.


### Aspecio and OSGi

While Aspecio's internal weaving code could be interesting to plug to other Dependency Injection frameworks, it currently supports exclusively OSGi R6 out of the box.

As it is, Aspecio works with OSGi Services and can weave almost any _willing_ OSGi service (only OSGi services registered as a class and not an interface, which a bad practice, cannot be woven using Aspecio). 

Aspecio works with any service scope, `singleton`, `bundle` and `prototype` and will only create as many instances as expected. In case of service frameworks using the `bundle` scope to make service creation lazy (such as Declarative Services), but still having effectively `singleton` services, Aspecio will make sure each service instance has exactly one proxy instance.

Thanks to relying on OSGi's low-level service primitives, Aspecio can work with any OSGi service component framework, including any compliant implementation of Declarative Services, Blueprint, Guice + Peaberry, Apache Felix iPojo or Apache Felix DependencyManager.

Aspecio has been tested on Felix 5.4.0 but should work with any framework compliant with OSGi R6.

In the following examples, Declarative Services will be used.


### Aspecio's weaving

Aspecio picks service objects that ask for certain aspects in their service properties, hiding (by default) the original service from all bundles except the system bundle and Aspecio itself.

Aspecio dynamically generates a proxy implementing all the interfaces and public methods of the same service using the ASM bytecode generation library, that have extra logic for call interception, and naturally delegates to the original service object.

The Aspecio proxy is designed to expose most of the woven service object's metadata: 
* Same method parameter names (will work with Java 8's `-parameters` compile switch) ;
* Same runtime annotations on the types, methods and method parameters ;
* Same generic method signatures on types and methods.

This way, any piece of code consuming a service proxied by Aspecio rather than the original instance should be clueless about it and work exactly the same, including reflection code.

Finally, Aspecio proxies are meant to be as efficient as can be:
* No primitive boxing whatsoever ;
* Lazy weaving of Aspects when a class is loaded (for instance, to obtain an instance of the arguments of a specific method call) ;
* Interception has different levels of detail, which are all "opt-in", so no one pays for what they don't use.


### Aspecio's dependencies

Aspecio only depends on Java 8 and OSGi R6. It has an optional dependency on SLF4J so that it will be used if it is `RESOLVED` before Aspecio is (or you can issue a `refresh` to rewire it). Aspecio will use JUL loggers if SLF4J is not found.


### Installing Aspecio in an OSGi Framework

It is better to install Aspecio early, before bundles making use of it are installed, because OSGi's R6 service hook do not allow "breaking" existing service bindings. 


## Defining an Aspect with Aspecio

In Aspecio, we use Java to declare an Aspect.

Here is a simple Aspect counting how many times a method has been called. Depending on its configuration, it may count only successful calls (e.g, methods that did not throw an exception) or all methods indiscriminately. 

```java
@Component
@Aspect(provides = CountingAspect.class)
public final class CountingAspectImpl implements Interceptor {

    private final Map<Method, Integer> methodCallCount = Maps.newConcurrentMap();
    
    // This could be dynamically configured using Declarative Service + ConfigAdmin
    private volatile boolean countOnlySuccessful = false;

    @Override
    public Advice onCall(CallContext callContext) {
        if (countOnlySuccessful) {
            return new AdviceAdapter() {
                @Override
                public int afterPhases() {
                    return CallReturn.PHASE;
                }

                @Override
                public void onSuccessfulReturn() {
                    methodCallCount.compute(callContext.method, (k, v) -> v == null ? 1 : (v += 1));
                }
            };
        } else {
            methodCallCount.compute(callContext.method, (k, v) -> v == null ? 1 : (v += 1));
            return Advice.DEFAULT;
        }
    }
}

```

Aspecio finds Aspects by:
* Looking for OSGi Services ; in the example above, provided using the `@Component` Declarative Service annotation)
* That provide the OSGi service String property `AspecioConstants.SERVICE_ASPECT` (`"service.aspect"`) ; in the example above, declared using the `@Aspect` annotation using the [BND Declarative Services Annotation Property Plugin](https://github.com/lambdacube/bnd-dsap-plugin).
* That implement the interface `io.lambdacube.aspecio.aspect.interceptor.Interceptor` (it need not be provided as the service's `"objectClass"`).
* If several services provide an aspect, Aspecio will pick the one with the highest-service ranking ; in case of equal service rankings, Aspecio will pick the one with the lowest service id. Aspecio supports OSGi's service dynamics and will happily replace or update Aspects live. Aspecio is always 'greedy': if a "better" interceptor is registered for a given aspect, all the services using it will have it updated immediately. 

In the example above, our component `CountingAspectImpl` provides the aspect named `"io.lambdacube.aspecio.examples.aspect.counting.CountingAspect"` (a Java String). You can name your aspects with any String, but it is practical to use Java classes to piggyback on the namespaces. 

Interceptors define the method `Advice onCall(CallContext callContext)` that will be called anytime a method from a woven service is called. 


### Advanced interception: Advices in Aspecio

TODO advice state automaton to describe how Aspecio processes advices.

[Javadoc](http://lambdacube.github.io/aspecio/javadoc/) is complete on Advices.

### Composing Aspects

TODO write this :)


## Aspect Weaving with Aspecio

In Aspecio, we can only weave OSGi services that opt-in to one or several aspects. This is because services have a well-defined contract and make it the perfect entry point for aspects.

Services must declare the OSGi service `String` or `String[]` property `"service.aspect.weave"` (for required aspects) or `"service.aspect.weave.optional"` (for optional aspects), with the aspect names as value, to be candidate for weaving.

### A simple example

Here is a Declarative Services component that will be woven by Aspecio using our `CountingAspect` declared earlier. Again, you can see that we use a custom service property annotation, `@Weave`, that is picked-up by the [BND Declarative Services Annotation Property Plugin](https://github.com/lambdacube/bnd-dsap-plugin).


```java
@Component
@Weave(required = CountingAspect.class, optional = AnotherOptionalAspect.class)
public final class HelloGoodbyeImpl implements Hello, Goodbye {

    @Override
    public String hello() {
        return "hello";
    }

    @Override
    public String goodbye() {
        return "goodbye";
    }

}
```

That's all! Now any aspect woven will be notified with the calls of methods `hello()` or `goodbye()` and may interact by returning other values, throwing exceptions, catching exceptions, accessing the arguments of each call (or just some) or even update the arguments before the call takes place.

Also, because `"i.l.a.e.a.c.CountingAspect.class"` is `required` by `HelloGoodbyeImpl`, the service will **not** be visible until a service providing Aspect `"i.l.a.e.a.c.CountingAspect.class"` is available. All the kinds of OSGi dynamism can happen here: the aspect can be registered after a service requiring it or later. 

Having `"i.l.a.e.a.a.AnotherOptionalAspect.class"` as an optional aspect will not prevent Aspecio's proxy of `HelloGoodbyeImpl` of being registered even in case `"i.l.a.e.a.a.AnotherOptionalAspect.class"` is missing ; however if it becomes available during `HelloGoodbyImpl`'s lifetime, it will start intercepting its methods as well.



## Aspect patterns

### Annotation-based interception

* When you want to intercept only certain annotated methods, and you can use the annotation to pass configuration to the interceptor ;
* When you annotate certain method parameters to guide your aspect.

```java
@Component
@Aspect(provides = MyAnnotationDrivenAspect.class)
public final class MyAnnotationDrivenAspectImpl implements AnnotationInterceptor<MyAnnotation> {

    @Override
    public Advice onCall(MyAnnotation myAnn, CallContext callContext) {
         // myAnn may contain previous info on how to use the aspect.
         ...
    }
    
    @Override
    public Class<MyAnnotation> intercept() {
        return MyAnnotation.class;
    }
}
```


See `AnnotationInterceptor`.


### Aspects that bridge services

Because we rarely want the actual cross-cutting behavior to reside in our interceptor, it is a better approach to use your favorite component framework to make your aspects merely bring a functionality provided elsewhere:


```java
@Component
@Aspect(provides = MyFeatureAspect.class)
public final class MyFeatureAspectImpl implements Interceptor {

    @Reference
    private MyFeature; // logic is in another service

    @Override
    public Advice onCall(CallContext callContext) {
         // notify MyFeature appropriately  
         ...
    }
}

```

### Interceptors that register extra service properties


```java
@Component
@Aspect(provides = MySecurityAspect.class, extraProperties = "secured")
public final class MySecurityAspectImpl implements Interceptor {

    @Override
    public Advice onCall(CallContext callContext) {
         ...
    }
}

```

The proxy service object registered by Aspecio will have the OSGi service Boolean property `"secured"` set to `Boolean.TRUE`. Now consuming code can check for that property to know if a service is secure, on only select secured services using a target filter. The consuming code doesn't need to know whether a service was secured manually or using an aspect, and this enables just that.


## Debugging Aspecio

Aspecio provides a service aptly named `Aspecio` that can show you what Aspecio sees at any time:
* which aspects are present ;
* what services are woven.

Aspecio provides two Gogo commands to get the same information in the Gogo shell, `aspect:aspects` and `aspect:woven`.

Here's a sample output of the two commands:

```
g! aspects
* io.lambdacube.aspecio.examples.aspect.metric.MetricAspect$All
  [ --- active --- ] Service id 25, class io.lambdacube.aspecio.examples.aspect.metric.internal.AllMetricInterceptorImpl, extra properties: [measured]
                     Provided by: io.lambdacube.aspecio.examples 0.9.0.SNAPSHOT [10]
* io.lambdacube.aspecio.examples.aspect.counting.CountingAspect
  [ --- active --- ] Service id 24, class io.lambdacube.aspecio.examples.aspect.counting.internal.CountingAspectImpl, extra properties: []
                     Provided by: io.lambdacube.aspecio.examples 0.9.0.SNAPSHOT [10]
* io.lambdacube.aspecio.examples.aspect.metric.MetricAspect$AnnotatedOnly
  [ --- active --- ] Service id 26, class io.lambdacube.aspecio.examples.aspect.metric.internal.AnnotatedMetricInterceptorImpl, extra properties: [measured]
                     Provided by: io.lambdacube.aspecio.examples 0.9.0.SNAPSHOT [10]
g! woven
[0] Service id: 27, objectClass: [io.lambdacube.aspecio.examples.async.SuperSlowService]
    Required aspects: [io.lambdacube.aspecio.examples.aspect.metric.MetricAspect$AnnotatedOnly], Optional aspects: [io.lambdacube.aspecio.examples.aspect.counting.CountingAspect]
    Provided by: io.lambdacube.aspecio.examples 0.9.0.SNAPSHOT [10]
    Satisfied: true
    Active aspects: [io.lambdacube.aspecio.examples.aspect.metric.MetricAspect$AnnotatedOnly, io.lambdacube.aspecio.examples.aspect.counting.CountingAspect]
[1] Service id: 29, objectClass: [io.lambdacube.aspecio.examples.greetings.Hello, io.lambdacube.aspecio.examples.greetings.Goodbye]
    Required aspects: [io.lambdacube.aspecio.examples.aspect.counting.CountingAspect], Optional aspects: [io.lambdacube.aspecio.examples.aspect.metric.MetricAspect$All]
    Provided by: io.lambdacube.aspecio.examples 0.9.0.SNAPSHOT [10]
    Satisfied: true
    Active aspects: [io.lambdacube.aspecio.examples.aspect.counting.CountingAspect, io.lambdacube.aspecio.examples.aspect.metric.MetricAspect$All]
[2] Service id: 32, objectClass: [io.lambdacube.aspecio.examples.misc.Stuff]
    Required aspects: [io.lambdacube.aspecio.examples.aspect.metric.Timed], Optional aspects: []
    Provided by: io.lambdacube.aspecio.examples 0.9.0.SNAPSHOT [10]
    Satisfied: false
    Missing required aspects: [io.lambdacube.aspecio.examples.aspect.metric.Timed]
g!        

```


## Maven coordinates

```xml
<dependency>
	<groupId>io.lambdacube.aspecio</groupId>
	<artifactId>aspecio-core</artifactId>
	<version>1.0</version>
</dependency>
```

## Credits / Contact

Author: Simon Chemouil. 

(c) Simon Chemouil & Lambdacube

Ask questions directly on Twitter `@simach`

Open bugs on Github issues.

