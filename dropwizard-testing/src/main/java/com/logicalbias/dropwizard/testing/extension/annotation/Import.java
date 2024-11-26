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
 * Defines a class which will be automatically added to the dropwizard hk2 context.
 * </p>
 */
@Beta
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Imports.class)
@Documented
@Inherited
public @interface Import {
    Class<?>[] value();
}
