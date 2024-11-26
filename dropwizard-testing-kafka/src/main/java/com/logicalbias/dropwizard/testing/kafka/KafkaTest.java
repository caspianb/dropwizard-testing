package com.logicalbias.dropwizard.testing.kafka;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.kafka.test.context.EmbeddedKafka;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@EmbeddedKafka
@ExtendWith(KafkaTestExtension.class)
public @interface KafkaTest {

    /**
     * Topics to automatically create.
     */
    String[] topics() default {};

    /**
     * <p>
     * An array of key=value property overrides to apply to the test environment. The following
     * variables will be replaced with the running kafka broker properties.
     * </p>
     * <ul>
     *     <li>${bootstrapServers}</li>
     * </ul>
     */
    String[] properties() default {};

}
