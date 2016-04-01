package io.lambdacube.aspecio.examples.aspect.count;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface CountCalls {
    
    // How much to increment each time
    int inc() default 1;

}
