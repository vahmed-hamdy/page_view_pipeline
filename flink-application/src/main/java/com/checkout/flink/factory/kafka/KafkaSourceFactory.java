package com.checkout.flink.factory.kafka;

import com.checkout.flink.configuration.Configuration;
import com.checkout.flink.configuration.ConfigurationOption;
import com.checkout.flink.factory.SourceFactory;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;

@Slf4j
@AllArgsConstructor
public class KafkaSourceFactory implements SourceFactory {
  private static final ConfigurationOption<String> TOPIC =
      ConfigurationOption.stringOption("topic", "SOURCE_KAFKA_TOPIC", true, null);
  private static final ConfigurationOption<String> BOOTSTRAP_SERVERS =
      ConfigurationOption.stringOption(
          "bootstrap.servers", "SOURCE_KAFKA_BOOTSTRAP_SERVERS", true, null);
  private static final ConfigurationOption<String> GROUP_ID =
      ConfigurationOption.stringOption(
          "group.id",
          "SOURCE_KAFKA_GROUP_ID",
          false,
          "consumer-group" + System.currentTimeMillis());

  private final Configuration configuration;

  @Override
  public <T> KafkaSource<T> createSource(Class<T> type) {
    return createKafkaSource(type);
  }

  @Override
  public String getSourceType() {
    return "kafka";
  }

  private <T> KafkaSource<T> createKafkaSource(Class<T> type) {
    String topic = TOPIC.loadWithPrefix(configuration, "source.kafka.");
    String bootstrapServers = BOOTSTRAP_SERVERS.loadWithPrefix(configuration, "source.kafka.");
    String groupId = GROUP_ID.loadWithPrefix(configuration, "source.kafka.");
    log.info(
        "Creating Kafka source for topic: {} and bootstrap servers: {}", topic, bootstrapServers);
    return KafkaSource.<T>builder()
        .setBootstrapServers(bootstrapServers)
        .setTopics(topic)
        .setDeserializer(new KafkaJsonDeserialization<>(type))
        .setStartingOffsets(OffsetsInitializer.earliest())
        .setGroupId(groupId)
        .build();
  }
}
