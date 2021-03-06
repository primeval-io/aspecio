package io.primeval.aspecio.examples.async.internal;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.google.common.util.concurrent.Uninterruptibles;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.util.promise.Deferred;
import org.osgi.util.promise.Promise;

import io.primeval.aspecio.aspect.annotations.Weave;
import io.primeval.aspecio.examples.aspect.counting.CountingAspect;
import io.primeval.aspecio.examples.aspect.metric.MetricAspect;
import io.primeval.aspecio.examples.aspect.metric.Timed;
import io.primeval.aspecio.examples.async.SuperSlowService;

@Component
@Weave(required = MetricAspect.AnnotatedOnly.class, optional = CountingAspect.class)
public final class SuperSlowServiceImpl implements SuperSlowService {

    private ExecutorService executor;
    
    @Activate
    public void activate() {
        this.executor = Executors.newFixedThreadPool(3);
    }
    
    @Deactivate
    public void deactivate() {
        executor.shutdown();
    }
    
    @Override
    @Timed
    public Promise<Long> compute() {
        Deferred<Long> deferred = new Deferred<>();
        executor.submit(() -> {
            Uninterruptibles.sleepUninterruptibly(3, TimeUnit.SECONDS);
            deferred.resolve(42L);
        });
        return deferred.getPromise();
    }

}
