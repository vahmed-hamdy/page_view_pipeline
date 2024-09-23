package com.checkout.flink.configuration;

import java.util.function.Function;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ConfigurationOption<T> {
  private final String key;
  private final String envKey;
  private T value;
  private final boolean required;
  private final T defaultValue;
  private final Function<String, T> extractor;

  public static ConfigurationOption<String> stringOption(
      String key, String envKey, boolean required, String defaultValue) {
    return ConfigurationOption.<String>builder()
        .key(key)
        .envKey(envKey)
        .required(required)
        .defaultValue(defaultValue)
        .extractor(Function.identity())
        .build();
  }

  public static ConfigurationOption<Integer> intOption(
      String key, String envKey, boolean required, Integer defaultValue) {
    return ConfigurationOption.<Integer>builder()
        .key(key)
        .envKey(envKey)
        .required(required)
        .defaultValue(defaultValue)
        .extractor(Integer::parseInt)
        .build();
  }

  private T load(Configuration config, String keyOverride) {
    String value = config.get(keyOverride);
    value = value == null ? System.getenv(envKey) : value;
    if (value == null) {
      if (required) {
        throw new IllegalArgumentException("Missing required configuration: " + keyOverride);
      }
      this.value = defaultValue;
      return defaultValue;
    }

    this.value = extractor.apply(value);
    ;
    return this.value;
  }

  public T loadWithPrefix(Configuration config, String prefix) {
    return load(config, prefix + key);
  }

  public T load(Configuration config) {
    return load(config, key);
  }
}
