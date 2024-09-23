package com.checkout.generator.config;

import lombok.Builder;
import lombok.Getter;

import java.util.function.Function;

@Getter
@Builder
public class ConfigurationOption<T> {
    private final String key;
    private final String envKey;
    private T value;
    private final boolean required;
    private final T defaultValue;
    private final Function<String, T> extractor;

    public static ConfigurationOption<String> stringOption(String key, String envKey, boolean required, String defaultValue) {
        return ConfigurationOption.<String>builder()
                .key(key)
                .envKey(envKey)
                .required(required)
                .defaultValue(defaultValue)
                .extractor(Function.identity())
                .build();
    }

    public static ConfigurationOption<Integer> intOption(String key, String envKey, boolean required, Integer defaultValue) {
        return ConfigurationOption.<Integer>builder()
                .key(key)
                .envKey(envKey)
                .required(required)
                .defaultValue(defaultValue)
                .extractor(Integer::parseInt)
                .build();
    }

    public static ConfigurationOption<Long> longOption(String key, String envKey, boolean required, Long defaultValue) {
        return ConfigurationOption.<Long>builder()
                .key(key)
                .envKey(envKey)
                .required(required)
                .defaultValue(defaultValue)
                .extractor(Long::parseLong)
                .build();
    }

    private T load(Configuration config, String keyOverride) {
        String value = config.get(keyOverride);
        value = value == null ? System.getenv(envKey) : value;
        return extract(value, keyOverride);
    }

    public T extract(String value, String keyOverride) {
        if (value == null) {
            if (required) {
                throw new IllegalArgumentException("Missing required configuration: " + keyOverride);
            }
            this.value = defaultValue;
            return defaultValue;
        }

        this.value = extractor.apply(value);
        return this.value;
    }

    public T load(Configuration config) {
        return load(config, key);
    }

    public T loadFromEnv() {
        return extract(System.getenv(envKey), envKey);
    }
}
