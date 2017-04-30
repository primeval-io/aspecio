package io.primeval.aspecio.internal.service;

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

    public static ServiceScope fromString(String s) {
        if (s == null) {
            return SINGLETON;
        }
        switch (s) {
        case "bundle":
            return BUNDLE;
        case "prototype":
            return PROTOTYPE;
        default:
            return SINGLETON;
        }
    }
}