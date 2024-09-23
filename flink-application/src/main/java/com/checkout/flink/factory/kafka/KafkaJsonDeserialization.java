package com.checkout.flink.factory.kafka;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.Serializable;
import lombok.RequiredArgsConstructor;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.connector.kafka.source.reader.deserializer.KafkaRecordDeserializationSchema;
import org.apache.flink.util.Collector;
import org.apache.kafka.clients.consumer.ConsumerRecord;

@RequiredArgsConstructor
public class KafkaJsonDeserialization<T>
    implements Serializable, KafkaRecordDeserializationSchema<T> {
  private ObjectMapper objectMapper;
  private final Class<T> type;

  @Override
  public void open(DeserializationSchema.InitializationContext context) throws Exception {
    objectMapper = new ObjectMapper();
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  @Override
  public void deserialize(ConsumerRecord<byte[], byte[]> consumerRecord, Collector<T> collector)
      throws IOException {
    collector.collect(objectMapper.readValue(consumerRecord.value(), type));
  }

  @Override
  public TypeInformation<T> getProducedType() {
    return TypeInformation.of(type);
  }
}
