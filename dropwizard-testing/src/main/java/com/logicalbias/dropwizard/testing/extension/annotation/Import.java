package com.logicalbias.dropwizard.testing.extension.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.glassfish.jersey.Beta;

/**
 * <p>
 * Defines a class which will be automatically added to the dropwizard HK2 context.
 * The class should utilize {@link org.jvnet.hk2.annotations.Service} and
 * {@link org.jvnet.hk2.annotations.ContractsProvided} annotations to define
 * the bean names and additional contracts. All imported beans will be automatically
 * bound to themselves.
 * </p>
 */
@Beta
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Imports.class)
@Documented
@Inherited
public @interface Import {

    /**
     * The class or set of classes to inject into the HK2 context.
     */
    Class<?>[] value();

    /**
     * The name of the mock. This is ignored if {@link Import#value} contains more than one value.
     * If set, this will override any @Service annotations placed on the imported class.
     */
    String name() default "";
}
