package io.lambdacube.aspecio.examples.aspect.counting.internal;

public @interface CountAspectConfig {
    boolean countOnlySuccessful() default false;
}