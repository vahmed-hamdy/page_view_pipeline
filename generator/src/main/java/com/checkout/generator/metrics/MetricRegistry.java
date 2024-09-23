package com.checkout.generator.metrics;

import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;

public class MetricRegistry {
    private static final MetricRegistry INSTANCE = new MetricRegistry();

    private PrometheusMeterRegistry prometheusRegistry;
    private CollectorRegistry collectorRegistry;
    private MetricRegistry() {
        // create a PrometheusMeterRegistry
        prometheusRegistry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
        collectorRegistry = prometheusRegistry.getPrometheusRegistry();
        prometheusRegistry.scrape();
    }

    public Counter createCounter(String name) {
        return Counter.build()
                .name(name)
                .namespace("checkout.generator")
                .register(collectorRegistry);
    }

    public static MetricRegistry getInstance() {
        return INSTANCE;
    }


}
