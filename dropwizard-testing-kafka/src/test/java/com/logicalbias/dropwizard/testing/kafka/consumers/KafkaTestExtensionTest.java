package com.logicalbias.dropwizard.testing.kafka.consumers;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.List;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import com.logicalbias.dropwizard.testing.kafka.KafkaTest;
import com.logicalbias.dropwizard.testing.kafka.factory.ConsumerFactory;
import com.logicalbias.dropwizard.testing.kafka.factory.ProducerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@KafkaTest
public class KafkaTestExtensionTest {

    public static final String TOPIC_NAME = "test-topic";

    static {
        var rootLogger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        ((Logger) rootLogger).setLevel(Level.INFO);
    }

    private final EmbeddedKafkaBroker broker;
    private final Consumer<String, String> consumer;
    private final Producer<String, String> producer;

    public KafkaTestExtensionTest(EmbeddedKafkaBroker broker,
            ConsumerFactory consumerFactory, ProducerFactory producerFactory) {
        this.broker = broker;
        this.consumer = consumerFactory.createConsumer("test-group");
        this.producer = producerFactory.createProducer();

        consumer.subscribe(List.of(TOPIC_NAME));
    }

    @AfterEach
    void afterEach() {
        consumer.close();
    }

    @Test
    void testEmbeddedKafka() {
        log.info("Brokers: {}", broker.getBrokersAsString());

        for (int i = 0; i < 5; i++) {
            var key = "test-key";
            var value = "Test Value: " + i;
            sendMessage(key, value);

            var record = KafkaTestUtils.getSingleRecord(consumer, TOPIC_NAME, Duration.ofSeconds(3));
            log.info("Received record: {}", record);
            assertEquals(key, record.key());
            assertEquals(value, record.value());
        }

        producer.close();
    }

    void sendMessage(String key, String value) {
        var record = new ProducerRecord<>(TOPIC_NAME, key, value);
        producer.send(record);
        producer.flush();
    }

}
