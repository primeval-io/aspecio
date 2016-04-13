package io.lambdacube.aspecio.internal.service;

public final class WovenServiceEvent {
    enum EventKind {
        SERVICE_ARRIVAL, SERVICE_UPDATE, SERVICE_DEPARTURE
    }
    
    public static final WovenServiceEvent SERVICE_REGISTRATION = new WovenServiceEvent(EventKind.SERVICE_ARRIVAL, 0);

    public static final WovenServiceEvent SERVICE_DEPARTURE = new WovenServiceEvent(EventKind.SERVICE_DEPARTURE, 0);

    public static final int REQUIRED_ASPECT_CHANGE = 1;
    public static final int OPTIONAL_ASPECT_CHANGE = 2;
    public static final int SERVICE_PROPERTIES_CHANGE = 4;

    public final EventKind kind;
    public final int mask;

    public WovenServiceEvent(EventKind kind, int mask) {
        this.kind = kind;
        this.mask = mask;
    }
    
    public boolean matchesCause(int causeMask) {
        return (mask & causeMask) != 0;
    }

}
