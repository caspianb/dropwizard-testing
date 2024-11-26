package com.logicalbias.dropwizard.testing.kafka.factory;

import lombok.RequiredArgsConstructor;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.utils.KafkaTestUtils;

@RequiredArgsConstructor
public class ConsumerFactory {

    private final EmbeddedKafkaBroker broker;

    public Consumer<String, String> createConsumer(String groupId) {
        return createConsumer(groupId, StringDeserializer.class, StringDeserializer.class);
    }

    public <K, KD extends Deserializer<K>,
            V, VD extends Deserializer<V>> Consumer<K, V> createConsumer(String groupId, Class<KD> keyDeserializer, Class<VD> valueDeserializer) {
        var consumerProps = KafkaTestUtils.consumerProps(groupId, "false", broker);
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, keyDeserializer.getName());
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, valueDeserializer.getName());

        return new KafkaConsumer<>(consumerProps);
    }

}
