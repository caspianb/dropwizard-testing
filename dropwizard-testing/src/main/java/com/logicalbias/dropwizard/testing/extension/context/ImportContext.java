package com.logicalbias.dropwizard.testing.extension.context;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.support.AnnotationSupport;

import com.logicalbias.dropwizard.testing.extension.annotation.Import;

class ImportContext {

    static List<Class<?>> getImportClassTypes(ExtensionContext context) {
        var testClass = context.getRequiredTestClass();
        var imports = AnnotationSupport.findRepeatableAnnotations(testClass, Import.class);
        return imports.stream()
                .flatMap(imp -> Arrays.stream(imp.value()))
                .collect(Collectors.toList());

    }
}
