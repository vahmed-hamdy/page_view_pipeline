package com.checkout.flink.factory;

import com.checkout.flink.configuration.Configuration;
import com.checkout.flink.configuration.ConfigurationOption;
import com.checkout.flink.factory.filesystem.FileSystemSinkFactory;
import com.checkout.flink.factory.kafka.KafkaSourceFactory;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.connector.sink2.Sink;
import org.apache.flink.api.connector.source.Source;

import java.util.Properties;

@Builder
@Slf4j
public class ConfigurationSelectorFactory implements SourceFactory, SinkFactory {
  private static final ConfigurationOption<String> SOURCE_TYPE =
      ConfigurationOption.stringOption("source.type", "SOURCE_TYPE", true, null);
  private static final ConfigurationOption<String> SINK_TYPE =
      ConfigurationOption.stringOption("sink.type", "SINK_TYPE", true, null);

  private final Configuration configuration;

  public <T> Source<T, ?, ?> createSource(Class<T> type) {
    String sourceType = SOURCE_TYPE.load(configuration);
    switch (sourceType) {
      case "kafka":
        return new KafkaSourceFactory(configuration).createSource(type);
      default:
        throw new IllegalArgumentException("Unknown source type: " + sourceType);
    }
  }

  @Override
  public String getSourceType() {
    return "configurationSelector";
  }

  @Override
  public <T> Sink<T> createSink(Class<T> type, Properties args) {
    String sinkType = SINK_TYPE.load(configuration);
    switch (sinkType) {
      case "file":
        return new FileSystemSinkFactory().createSink(type, args);
      default:
        throw new IllegalArgumentException("Unknown sink type: " + sinkType);
    }
  }

  @Override
  public String getSinkType() {
    return "configurationSelector";
  }
}
