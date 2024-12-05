package com.logicalbias.dropwizard.testing.dynamo;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.extension.ExtendWith;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@ExtendWith(DynamoDbTestExtension.class)
public @interface DynamoDbTest {

    /**
     * Control which environment the dynamo database service will run under; defaults to EMBEDDED.
     */
    Environment environment() default Environment.EMBEDDED;

    /**
     * Enabled if the clients should be injected into the DropwizardTest HK2 context (if applicable); defaults to true.
     * If this is set to false, the properties field should be set to ensure the correct URL and port are injected into
     * the application context.
     */
    boolean registerClients() default true;

    /**
     * <p>
     * An array of key=value property overrides to apply to the test environment. The following
     * variables will be replaced with the appropriate dynamo service value.
     * </p>
     * <ul>
     *     <li>${endpoint}</li>
     * </ul>
     */
    String[] properties() default {};

    /**
     * The port the docker container will expose for dynamoDb; ignored if running
     * in EMBEDDED mode. The default value is 0 which will pick a random open port.
     */
    int port() default 0;

    enum Environment {
        /**
         * Starts an embedded, in-memory dynamoDb database. The database will not be exposed on any port
         * and the service MUST use the clients provided from the {@link DynamoManager} which will be provided
         * via test constructor/method params. If the bindClients property is true, the test can also use the
         * injected clients directly.
         */
        EMBEDDED,

        /**
         * Starts a containerized dynamoDb database via docker and test containers.
         */
        DOCKER
    }
}
