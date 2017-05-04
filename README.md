# Aspecio, AOP Proxies for OSGi [![Build Status](https://travis-ci.org/primeval-io/aspecio.svg?branch=master)](https://travis-ci.org/primeval-io/aspecio) [![Gitter primeval-io/Lobby](https://badges.gitter.im/primeval-io/Lobby.svg)](https://gitter.im/primeval-io/Lobby)

Aspecio is a 'micro-framework' that provide AOP Proxies to OSGi R6. It brings a mix of component-oriented and aspect-oriented programming to your application. Aspecio lets you define _Aspects_ that you can later pick to add behavior to your components and avoid duplicating boilerplate dealing with cross-cutting concerns.


## Aspecio 2.0.0, now at Primeval!

Aspecio 2.0.0 is a complete overhaul with a new proxy model that is simpler, completely unrestricted and faster. See [Primeval Reflex](http://github.com/primeval-io/primeval-reflex) for proxy capabilities. 

## Maven coordinates

```xml

	<groupId>io.primeval.aspecio</groupId>
	<artifactId>aspecio-core</artifactId>
	<version>2.0.0-SNAPSHOT</version>
```

Until a stable release (soon), the snapshot version is available in the Sonatype OSS Snapshots repository.

# Dependencies

Aspecio requires Java 8 and depends on primeval-reflect. 

```xml
	<dependency>
		<groupId>io.primeval</groupId>
		<artifactId>primeval-reflex</artifactId>
		<version>1.0.0-SNAPSHOT</version>
	</dependency>
```	

## Documentation

Aside from this page, Javadoc is complete and provides a good overview. 



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

Aspecio uses [Primeval Reflex](http://github.com/primeval-io/primeval-reflex) to proxy services requesting weaving. All interfaces and public methods are proxied.

See [Primeval Reflex](http://github.com/primeval-io/primeval-reflex) for documentation on the proxies and writing interceptors.


### Installing Aspecio in an OSGi Framework

Just start Aspecio in your OSGi framework and it will work right away.

If there are already registered services with the weaving property, Aspecio will restart their bundles to make sure it has the opportunity to apply its service hook.

Aspecio first collects the set of bundles providing services to weave, sorts them using the natural `Bundle` comparator (based on bundleIds). It stops them all in that order, then starts them all. The ordering should allow to keep the original installation order, and stopping and starting them by batch is aiming to minimize service level interactions between these bundles (such as re-creating components with static references too many times).
  

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
    public <T, E extends Throwable> T onCall(CallContext context, InterceptionHandler<T> handler) throws E {
        if (countOnlySuccessful) {
            T res = handler.invoke();
            methodCallCount.compute(context.method, (k, v) -> v == null ? 1 : (v += 1));
            return res;
        } else {
            methodCallCount.compute(context.method, (k, v) -> v == null ? 1 : (v += 1));
            return handler.invoke();
        }
    }    
}

```

Aspecio finds Aspects by:
* Looking for OSGi Services ; in the example above, provided using the `@Component` Declarative Service annotation)
* That provide the OSGi service String property `AspecioConstants.SERVICE_ASPECT` (`"service.aspect"`) ; in the example above, declared using the `@Aspect` annotation using the [BND Declarative Services Annotation Property Plugin](https://github.com/lambdacube/bnd-dsap-plugin).
* That implement the interface `io.primeval.reflect.proxy.Interceptor` (it need not be provided as the service's `"objectClass"`).
* If several services provide an aspect, Aspecio will pick the one with the highest-service ranking ; in case of equal service rankings, Aspecio will pick the one with the lowest service id. Aspecio supports OSGi's service dynamics and will happily replace or update Aspects live. Aspecio is always 'greedy': if a "better" interceptor is registered for a given aspect, all the services using it will have it updated immediately. 

In the example above, our component `CountingAspectImpl` provides the aspect named `"io.primeval.aspecio.examples.aspect.counting.CountingAspect"` (a Java String). You can name your aspects with any String, but it is practical to use Java classes to piggyback on the namespaces. 

For documentation on Interceptors, see [Primeval Reflect](http://github.com/primeval-io/primeval-reflect).



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

Also, because `"i.p.a.e.a.c.CountingAspect.class"` is `required` by `HelloGoodbyeImpl`, the service will **not** be visible until a service providing Aspect `"i.l.a.e.a.c.CountingAspect.class"` is available. All the kinds of OSGi dynamics can happen here: the aspect can be registered after a service requiring it or later. 

Having `"i.p.a.e.a.a.AnotherOptionalAspect.class"` as an optional aspect will not prevent Aspecio's proxy of `HelloGoodbyeImpl` of being registered even in case `"i.l.a.e.a.a.AnotherOptionalAspect.class"` is missing ; however if it becomes available during `HelloGoodbyImpl`'s lifetime, it will start intercepting its methods as well.



## Aspect patterns

### Annotation-based interception

* When you want to intercept only certain annotated methods, and you can use the annotation to pass configuration to the interceptor ;
* When you annotate certain method parameters to guide your aspect.

```java
@Component
@Aspect(provides = MyAnnotationDrivenAspect.class)
public final class MyAnnotationDrivenAspectImpl implements AnnotationInterceptor<MyAnnotation> {

    @Override
    public <T, E extends Throwable> T onCall(MyAnnotationDrivenAspect annotation, CallContext context,
                                                              InterceptionHandler<T> handler) throws E {
        // myAnn may contain previous info on how to use the aspect.
        ...
    }
    
    @Override
    public Class<MyAnnotation> intercept() {
        return MyAnnotation.class;
    }
}
```


See `AnnotationInterceptor` in [Primeval Reflex](http://github.com/primeval-io/primeval-reflex).


### Aspects that bridge services

Because we rarely want the actual cross-cutting behavior to reside in our interceptor, it is a better approach to use your favorite component framework to make your aspects merely bring a functionality provided elsewhere:


```java
@Component
@Aspect(provides = MyFeatureAspect.class)
public final class MyFeatureAspectImpl implements Interceptor {

    @Reference
    private MyFeature myFeatureService; // logic is in another service

    @Override
    public <T, E extends Throwable> T onCall(MyAnnotationDrivenAspect annotation, CallContext context,
                                                              InterceptionHandler<T> handler) throws E {
       // use MyFeature service
       ....
    }                                                              
}

```

### Interceptors that register extra service properties


```java
@Component
@Aspect(provides = MySecurityAspect.class, extraProperties = "secured")
public final class MySecurityAspectImpl implements Interceptor {

    @Reference private Auth auth;
    @Override
    public <T, E extends Throwable> T onCall(MyAnnotationDrivenAspect annotation, CallContext context,
                                                              InterceptionHandler<T> handler) throws E {
	     auth.checkPermissions(...);
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
* io.primeval.aspecio.examples.aspect.metric.MetricAspect$All
  [ --- active --- ] Service id 25, class io.primeval.aspecio.examples.aspect.metric.internal.AllMetricInterceptorImpl, extra properties: [measured]
                     Provided by: io.primeval.aspecio.examples 0.9.0.SNAPSHOT [10]
* io.primeval.aspecio.examples.aspect.counting.CountingAspect
  [ --- active --- ] Service id 24, class io.primeval.aspecio.examples.aspect.counting.internal.CountingAspectImpl, extra properties: []
                     Provided by: io.primeval.aspecio.examples 0.9.0.SNAPSHOT [10]
* io.primeval.aspecio.examples.aspect.metric.MetricAspect$AnnotatedOnly
  [ --- active --- ] Service id 26, class io.primeval.aspecio.examples.aspect.metric.internal.AnnotatedMetricInterceptorImpl, extra properties: [measured]
                     Provided by: io.primeval.aspecio.examples 0.9.0.SNAPSHOT [10]
g! woven
[0] Service id: 27, objectClass: [io.primeval.aspecio.examples.async.SuperSlowService]
    Required aspects: [io.primeval.aspecio.examples.aspect.metric.MetricAspect$AnnotatedOnly], Optional aspects: [io.primeval.aspecio.examples.aspect.counting.CountingAspect]
    Provided by: io.primeval.aspecio.examples 0.9.0.SNAPSHOT [10]
    Satisfied: true
    Active aspects: [io.primeval.aspecio.examples.aspect.metric.MetricAspect$AnnotatedOnly, io.primeval.aspecio.examples.aspect.counting.CountingAspect]
[1] Service id: 29, objectClass: [io.primeval.aspecio.examples.greetings.Hello, io.primeval.aspecio.examples.greetings.Goodbye]
    Required aspects: [io.primeval.aspecio.examples.aspect.counting.CountingAspect], Optional aspects: [io.primeval.aspecio.examples.aspect.metric.MetricAspect$All]
    Provided by: io.primeval.aspecio.examples 0.9.0.SNAPSHOT [10]
    Satisfied: true
    Active aspects: [io.primeval.aspecio.examples.aspect.counting.CountingAspect, io.primeval.aspecio.examples.aspect.metric.MetricAspect$All]
[2] Service id: 32, objectClass: [io.primeval.aspecio.examples.misc.Stuff]
    Required aspects: [io.primeval.aspecio.examples.aspect.metric.Timed], Optional aspects: []
    Provided by: io.primeval.aspecio.examples 0.9.0.SNAPSHOT [10]
    Satisfied: false
    Missing required aspects: [io.primeval.aspecio.examples.aspect.metric.Timed]
g!        

```



# Getting help

Post a new GitHub issue or join on [Gitter](https://gitter.im/primeval-io/Lobby).
 

# Author

Aspecio was developed by Simon Chemouil.

# Copyright

(c) 2016-2017, Simon Chemouil, Lambdacube

Aspecio is part of the Primeval project.
