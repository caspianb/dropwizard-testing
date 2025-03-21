package com.logicalbias.dropwizard.testing.extension.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface TestProperties {

    /**
     * <p>
     * An array of key=value property overrides to apply to the test environment. These
     * values will override any properties configured in the loaded configuration files.
     * </p>
     */
    String[] properties();
}
