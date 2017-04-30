package io.lambdacube.aspecio.internal.service;

public interface WovenServiceListener {

    void onWovenServiceEvent(WovenServiceEvent event, WovenService wovenService);
}
