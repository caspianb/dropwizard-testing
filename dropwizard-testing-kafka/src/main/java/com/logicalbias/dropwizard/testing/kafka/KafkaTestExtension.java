package com.logicalbias.dropwizard.testing.kafka;

import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.platform.commons.support.AnnotationSupport;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.condition.EmbeddedKafkaCondition;

import com.logicalbias.dropwizard.testing.extension.context.ExtensionHooks;
import com.logicalbias.dropwizard.testing.extension.utils.TestHelpers;
import com.logicalbias.dropwizard.testing.kafka.factory.ConsumerFactory;
import com.logicalbias.dropwizard.testing.kafka.factory.ProducerFactory;

@Slf4j
class KafkaTestExtension implements
        ParameterResolver,
        BeforeAllCallback {

    @Override
    public void beforeAll(ExtensionContext context) {
        var broker = getBroker();
        var embeddedKafka = getExtensionAnnotation(context);
        var extensions = ExtensionHooks.from(context);

        broker.addTopics(embeddedKafka.topics());

        Arrays.stream(embeddedKafka.properties())
                .map(property -> TestHelpers.splitPropertyWithVariable(property, "bootstrapServers", broker.getBrokersAsString()))
                .forEach(prop -> extensions.overrideProperty(prop[0], prop[1]));
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        var type = parameterContext.getParameter().getType();
        return type == ConsumerFactory.class || type == ProducerFactory.class;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        var type = parameterContext.getParameter().getType();

        if (type == ConsumerFactory.class) {
            return new ConsumerFactory(getBroker());
        }

        if (type == ProducerFactory.class) {
            return new ProducerFactory(getBroker());
        }

        return null;
    }

    private static KafkaTest getExtensionAnnotation(ExtensionContext context) {
        var testClass = context.getRequiredTestClass();
        return AnnotationSupport.findAnnotation(testClass, KafkaTest.class)
                .orElseThrow(() -> new IllegalStateException("@EmbeddedKafkaTest annotation was not located for " + testClass));
    }

    private static EmbeddedKafkaBroker getBroker() {
        return EmbeddedKafkaCondition.getBroker();
    }
}
