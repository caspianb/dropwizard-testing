package com.logicalbias.dropwizard.testing.extension.context;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.support.AnnotationSupport;
import org.jvnet.hk2.annotations.ContractsProvided;
import org.jvnet.hk2.annotations.Service;

import com.logicalbias.dropwizard.testing.extension.annotation.Import;

class ImportContext {

    private final List<ImportDefinition> imports = new ArrayList<>();

    ImportContext(ExtensionContext context) {
        var testClass = context.getRequiredTestClass();

        loadImportsFromAnnotations(testClass);
    }

    void forEach(Consumer<ImportDefinition> action) {
        imports.forEach(action);
    }

    private void loadImportsFromAnnotations(Class<?> testClass) {
        // Scan for @Import annotations and store their information
        var importBeans = AnnotationSupport.findRepeatableAnnotations(testClass, Import.class);
        for (var importBean : importBeans) {
            for (var importType : importBean.value()) {
                // Only use bean name definition if exactly one mock type is specified on the @MockBean annotation.
                var name = importBean.value().length <= 1 ? importBean.name() : null;
                imports.add(new ImportDefinition(importType, name));
            }
        }
    }

    @Getter
    @Accessors(fluent = true)
    @EqualsAndHashCode
    @RequiredArgsConstructor
    static class ImportDefinition {
        private final Class<?> type;
        private final String name;
        private final Class<?>[] contractsProvided;

        ImportDefinition(Class<?> type, String name) {
            this.type = type;
            this.name = getServiceName(type, name);
            this.contractsProvided = getContractsProvided(type);
        }

        private String getServiceName(Class<?> type, String name) {
            return StringUtils.getIfBlank(name, () -> AnnotationSupport.findAnnotation(type, Service.class)
                    .map(Service::name)
                    .orElse(null)
            );
        }

        private Class<?>[] getContractsProvided(Class<?> type) {
            return AnnotationSupport.findAnnotation(type, ContractsProvided.class)
                    .map(ContractsProvided::value)
                    .orElseGet(() -> new Class[0]);

        }
    }
}
