package com.logicalbias.dropwizard.testing.application.generic;

public interface GenericService<T> {

    T process(T value);
}
