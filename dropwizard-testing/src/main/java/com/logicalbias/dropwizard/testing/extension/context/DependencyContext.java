package com.logicalbias.dropwizard.testing.extension.context;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
class DependencyContext {
    private final Set<DependencyInfo<?>> dependencies = new LinkedHashSet<>();

    <T> void add(Class<T> classType, String name, T instance) {
        dependencies.add(new DependencyInfo<>(classType, name, instance));
    }

    void forEach(Consumer<DependencyInfo<?>> action) {
        dependencies.forEach(action);
    }

    @Getter
    @Accessors(fluent = true)
    @RequiredArgsConstructor
    static class DependencyInfo<T> {
        private final Class<T> classType;
        private final String name;
        private final T instance;
    }
}
