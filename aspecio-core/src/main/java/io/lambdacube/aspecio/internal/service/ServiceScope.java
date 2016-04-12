package io.lambdacube.aspecio.internal.service;

public enum ServiceScope {
    SINGLETON("singleton"), BUNDLE("bundle"), PROTOTYPE("prototype");

    private final String value;

    ServiceScope(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}