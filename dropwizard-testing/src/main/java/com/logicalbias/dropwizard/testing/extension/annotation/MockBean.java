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
 * application context. The mocks will be automatically generated and injected into the HK2 context for duration of the
 * test class.
 * </p>
 * <p>
 * This annotation can be placed at either the class or field level. When placed at the class level (or if the annotated field
 * is marked final) the user must inject the mocks into the test constructor.
 * If the @MockBean annotation is placed on a field that is not final, the test extension will automatically set the field after
 * the test class is instantiated.
 * </p>
 */
@Target({ ElementType.TYPE, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(MockBeans.class)
@Documented
@Inherited
public @interface MockBean {

    /**
     * The type of mock to inject. This is required when used at the class level and
     * ignored when used at the field level; field type is used directly.
     */
    Class<?>[] value() default {};

    /**
     * The name of the mock. This is ignored if {@link MockBean#value} contains more than one value.
     */
    String name() default "";
}
