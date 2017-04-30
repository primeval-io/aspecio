package io.primeval.aspecio.internal.service;

public interface AspectInterceptorListener {
    enum EventKind {
        NEWMATCH, NOMATCH
    }
    void onAspectChange(EventKind eventKind, String aspectName, AspectInterceptor aspectInterceptor);
}
