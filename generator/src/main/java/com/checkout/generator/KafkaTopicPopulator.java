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
import java.util.Set;
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
            true, "ec2-18-197-31-16.eu-central-1.compute.amazonaws.com:9092");
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
//        adminMain();
        produceMain();
    }

    public static void produceMain() throws ExecutionException, InterruptedException {
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

    public static void adminMain() throws ExecutionException, InterruptedException {
//        cleanup.policy=compact
        List<String> topicsToCreate = List.of("Orders", "Products", "Customers", "Shipments", "Payments", "ExchangeRates");
        List<String> topicsToDelete = List.of("ExchangeRates");
        createTopic(List.of("ExchangeRates"), Collections.emptyList(),
                "ec2-18-197-31-16.eu-central-1.compute.amazonaws.com:9092");
    }

    public static void createTopic(List<String> topicsToCreate, List<String> topicsToDelete, String bootstrapServers) throws ExecutionException, InterruptedException {
        Properties props = getCommonProperties(bootstrapServers);
//        props.put("cleanup.policy", "compact");
        try (AdminClient adminClient = AdminClient.create(props)) {
            final Set<String> topics = adminClient.listTopics().names().get();
            List<NewTopic> newTopics = topicsToCreate.stream()
                    .filter(topic -> !topics.contains(topic))
                    .map(topic -> new NewTopic(topic, 1, (short) 1))
                    .collect(Collectors.toList());
            List<String> topicsToDeleteFiltered = topicsToDelete.stream()
                    .filter(topics::contains)
                    .collect(Collectors.toList());
            if (!newTopics.isEmpty()) {
                adminClient.createTopics(newTopics).all().get();
            }
            if (!topicsToDeleteFiltered.isEmpty()) {
                adminClient.deleteTopics(topicsToDeleteFiltered).all().get();
            }
            Set<String> updatedTopics = adminClient.listTopics().names().get();
            log.info("Updated topics: ");
            updatedTopics.forEach(t -> log.info("Topic: {}", t));
        }
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