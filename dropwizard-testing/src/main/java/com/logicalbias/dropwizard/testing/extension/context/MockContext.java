package com.logicalbias.dropwizard.testing.extension.context;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.support.AnnotationSupport;
import org.mockito.Mockito;
import org.mockito.internal.util.MockUtil;

import com.logicalbias.dropwizard.testing.extension.annotation.MockBean;

class MockContext {

    private final Map<MockDefinition, Object> mocks = new LinkedHashMap<>();

    MockContext(ExtensionContext context) {
        var testClass = context.getTestClass();

        // Scan for @MockBean annotations and prep our test mocks
        var mockBeans = AnnotationSupport.findRepeatableAnnotations(testClass, MockBean.class);
        for (var mockBean : mockBeans) {
            for (var mockType : mockBean.value()) {
                // Only use bean name definition if exactly one mock type is specified on the @MockBean annotation.
                var name = mockBean.value().length == 1 ? mockBean.name() : null;
                var mock = Mockito.mock(mockType);
                mocks.put(new MockDefinition(mockType, name), mock);
            }
        }
    }

    void resetMocks() {
        for (var mock : mocks.values()) {
            MockUtil.resetMock(mock);
        }
    }

    void forEach(BiConsumer<MockDefinition, Object> action) {
        mocks.forEach(action);
    }

    @Getter
    @Accessors(fluent = true)
    @RequiredArgsConstructor
    static class MockDefinition {
        private final Class<?> type;
        private final String name;
    }
}
