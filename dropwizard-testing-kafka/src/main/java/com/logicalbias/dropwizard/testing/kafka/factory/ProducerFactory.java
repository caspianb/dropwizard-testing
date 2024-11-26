package com.logicalbias.dropwizard.testing.kafka.factory;

import lombok.RequiredArgsConstructor;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.utils.KafkaTestUtils;

@RequiredArgsConstructor
public class ProducerFactory {

    private final EmbeddedKafkaBroker broker;

    public Producer<String, String> createProducer() {
        return createProducer(StringSerializer.class, StringSerializer.class);
    }

    public <K, KD extends Serializer<K>,
            V, VD extends Serializer<V>> Producer<K, V> createProducer(Class<KD> keySerializer, Class<VD> valueSerializer) {
        var producerProps = KafkaTestUtils.producerProps(broker);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, keySerializer.getName());
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, valueSerializer.getName());

        return new KafkaProducer<>(producerProps);
    }
}
