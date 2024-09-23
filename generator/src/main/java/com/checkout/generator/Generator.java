package com.checkout.generator;

import com.checkout.generator.model.KeyedRecord;
import com.checkout.generator.model.RandomDatumGenerator;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.Random;

@Slf4j
public class Generator< T extends KeyedRecord> implements Iterator<T> {
    private final long maxCapacity;
    private int returned = 0;

    private final int throughtput;

    private final Random random = new Random();


    private final RandomDatumGenerator<T> randomDatumGenerator;

    @Builder
    public Generator(long maxCapacity, RandomDatumGenerator<T> randomDatumGenerator) {
        this.maxCapacity = maxCapacity;
        this.randomDatumGenerator = randomDatumGenerator;
        this.throughtput = 100;
    }

    @Builder
    public Generator(long maxCapacity, int thoroughtput, RandomDatumGenerator<T> randomDatumGenerator) {
        this.maxCapacity = maxCapacity;
        this.randomDatumGenerator = randomDatumGenerator;
        if(thoroughtput <= 0) {
            log.warn("Throughput cannot be negative or zero, setting to 100");
            this.throughtput = 100;
        } else {
            this.throughtput = thoroughtput;
        }
    }

    @Override
    public boolean hasNext() {
        return returned < maxCapacity;
    }

    protected T getOne(Duration maximumLateness) {
        return randomDatumGenerator.generateRandom(random , getRandomInstantWithRandomLateness(maximumLateness));
    }


    @Override
    public T next() {
        returned++;
        try {
            Thread.sleep(1000 / throughtput);
        } catch (InterruptedException e) {
            log.warn("Error while sleeping", e);
        }

        return getOne(Duration.ofSeconds(20));
    }

    @Override
    public void remove() {
        Iterator.super.remove();
    }

    protected Instant getRandomInstantWithRandomLateness(Duration jitter) {
        return Instant.now().minus(jitter).plus(Duration.ofSeconds((long) (Math.random() * jitter.getSeconds())));
    }
}
