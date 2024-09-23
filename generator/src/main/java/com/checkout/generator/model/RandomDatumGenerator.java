package com.checkout.generator.model;

import java.time.Instant;
import java.util.Random;

public interface RandomDatumGenerator<T> {
    T generateRandom(Random random, Instant timestamp);
}
