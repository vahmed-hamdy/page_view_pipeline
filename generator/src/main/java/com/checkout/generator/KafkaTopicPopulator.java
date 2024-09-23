package com.checkout.generator;

import com.checkout.generator.config.ConfigurationOption;
import com.checkout.generator.model.KeyedRecord;
import com.checkout.generator.model.PageView;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.TopicListing;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.TopicPartitionInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Slf4j
public class KafkaTopicPopulator {
    private static final ConfigurationOption<String> BOOTSTRAP_SERVERS
            = ConfigurationOption.stringOption("bootstrap.servers",
            "KAFKA_BOOTSTRAP_SERVER",
            true, "localhost:9092");
    private static final ConfigurationOption<String> TOPIC
            = ConfigurationOption.stringOption("topic",
            "KAFKA_TOPIC",
            true, null);

    private static final ConfigurationOption<Long> LIMIT
            = ConfigurationOption.longOption("limit",
            "LIMIT",
            false, 1000_000L);

    private static final ConfigurationOption<Integer> THROUGHPUT
            = ConfigurationOption.intOption("throughput",
            "THROUGHPUT",
            false, 100);

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        log.info("Starting to produce to topic: {}", TOPIC.loadFromEnv());
        produceToTopic(
                new KafkaProducer<>(getProducerProperties(BOOTSTRAP_SERVERS.loadFromEnv())),
                TOPIC.loadFromEnv(),
                LIMIT.loadFromEnv(),
                new Generator<>(LIMIT.loadFromEnv(),
                        THROUGHPUT.loadFromEnv(),
                        new PageView.PageViewGenerator()));

        log.info("Finished producing to topic: {}", TOPIC.loadFromEnv());
    }

    public static <T extends KeyedRecord> void produceToTopic(Producer<byte[], byte[]> producer, String topic, long limit, Generator<T> generator) throws ExecutionException, InterruptedException {
        log.info("Producing to topic: {}", topic);
        ObjectMapper objectMapper = new ObjectMapper();
        List<String> keys = new ArrayList<>();
        for (int i = 0; i < limit && generator.hasNext(); i++) {
            T datum =  generator.next();
            try {
                var sentBytes = producer.send(new ProducerRecord<>(topic, objectMapper.writeValueAsBytes(datum))).get().serializedValueSize();
                keys.add(datum.getKey());
            } catch (JsonProcessingException ignored) {}
        }
    }

    private static Properties getProducerProperties(String bootstrapServers) {
        var props = (Properties) getCommonProperties(bootstrapServers).clone();
        props.put(
                "key.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");
        props.put(
                "value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");
        props.put("acks", "all");
        props.put("retries", 0);
        return props;
    }

    private static Properties getCommonProperties(String bootstrapServers) {
        var adminClientProps = new Properties();
        adminClientProps.put(
                "bootstrap.servers", bootstrapServers);
        adminClientProps.put("enable.auto.commit", true);
        adminClientProps.put("auto.commit.interval.ms", 1000);
        adminClientProps.put(
                "key.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer");
        adminClientProps.put(
                "value.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer");
        return adminClientProps;
    }
}