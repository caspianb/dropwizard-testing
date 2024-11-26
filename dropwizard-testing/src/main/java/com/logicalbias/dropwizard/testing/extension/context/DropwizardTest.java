package com.logicalbias.dropwizard.testing.extension.context;

import io.dropwizard.core.Application;
import io.dropwizard.core.Configuration;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.extension.ExtendWith;

/**
 * <p>Defines a test that will run within a Dropwizard test application context.</p>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@ExtendWith(DropwizardTestExtension.class)
public @interface DropwizardTest {

    /**
     * The main {@link Application} class of the dropwizard service.
     */
    Class<? extends Application<? extends Configuration>> value();

    /**
     * <p>The path to the the configuration file to load.</p>
     */
    String configFile();

    /**
     * Set to true if dropwizard {@link io.dropwizard.testing.ResourceHelpers#resourceFilePath(String)} should be
     * applied to the configFile before passing its path to the test application. Defaults to false.
     */
    boolean useResourceFilePath() default false;

    /**
     * An array of key=value property overrides to apply to the test environment.
     */
    String[] properties() default {};

    WebEnvironment webEnvironment() default WebEnvironment.DEFAULT;

    enum WebEnvironment {
        /**
         * Starts the dropwizard test application running on the configured port.
         */
        DEFAULT,

        /**
         * Starts the dropwizard test application running on a random port.
         */
        RANDOM
    }

}
