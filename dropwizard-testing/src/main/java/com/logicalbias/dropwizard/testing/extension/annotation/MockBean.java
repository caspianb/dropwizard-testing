package com.logicalbias.dropwizard.testing.extension.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * Defines what classes will be mocked within the {@link com.logicalbias.dropwizard.testing.extension.context.DropwizardTest}
 * application context. The mocks will be automatically generated and injected into the hk2 context for duration of the
 * test class.
 * </p>
 * <p>
 * Currently, all mocks must be defined at the class level. The mock will be injected into the test constructor.
 * </p>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(MockBeans.class)
@Documented
@Inherited
public @interface MockBean {

    /**
     * The name of the mock. This is ignored if more than one type is mocked in a single {@link MockBean} declaration.
     */
    String name() default "";

    Class<?>[] value();
}
