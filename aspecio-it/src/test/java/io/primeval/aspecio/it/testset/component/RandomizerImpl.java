package io.primeval.aspecio.it.testset.component;

import io.primeval.aspecio.it.testset.api.Randomizer;

public class RandomizerImpl implements Randomizer {

    public int randomInt(int max) {
        // very random!
        return Math.max(max, 42);
    };
}
