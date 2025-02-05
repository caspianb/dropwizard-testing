package com.logicalbias.dropwizard.testing.extension.context;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.support.AnnotationSupport;
import org.mockito.Mockito;
import org.mockito.internal.util.MockUtil;

import com.logicalbias.dropwizard.testing.extension.annotation.MockBean;

class MockContext {

    private final Map<MockDefinition, Object> mocks = new LinkedHashMap<>();

    MockContext(ExtensionContext context) {
        var testClass = context.getRequiredTestClass();

        loadMocksFromAnnotations(testClass);
        loadMocksFromProperties(testClass);
    }

    void resetMocks() {
        for (var mock : mocks.values()) {
            MockUtil.resetMock(mock);
        }
    }

    void forEach(BiConsumer<MockDefinition, Object> action) {
        mocks.forEach(action);
    }

    void injectTestInstanceMocks(Object testInstance, BiFunction<Class<?>, Type, Object> mockSupplier) {
        var testClass = testInstance.getClass();
        var mockFields = getMockFields(testClass).stream()
                .filter(field -> !Modifier.isFinal(field.getModifiers()))
                .collect(Collectors.toList());

        for (var field : mockFields) {
            try {
                var rawType = field.getType();
                var parameterizedType = field.getGenericType();
                var mock = mockSupplier.apply(rawType, parameterizedType);

                field.setAccessible(true);
                if (field.get(testInstance) == null) {
                    field.set(testInstance, mock);
                }
            }
            catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void loadMocksFromAnnotations(Class<?> testClass) {
        // Scan for @MockBean annotations and prep our test mocks
        var mockBeans = AnnotationSupport.findRepeatableAnnotations(testClass, MockBean.class);
        for (var mockBean : mockBeans) {
            if (mockBean.value().length == 0) {
                throw new IllegalStateException("@MockBean(value=?) is required at class level.");
            }

            for (var mockType : mockBean.value()) {
                // Only use bean name definition if exactly one mock type is specified on the @MockBean annotation.
                var name = mockBean.value().length <= 1 ? mockBean.name() : null;
                var mock = Mockito.mock(mockType);
                mocks.put(new MockDefinition(mockType, name), mock);
            }
        }
    }

    private void loadMocksFromProperties(Class<?> testClass) {
        var mockFields = getMockFields(testClass);

        for (var field : mockFields) {
            var mockBean = field.getAnnotation(MockBean.class);
            var name = mockBean.name();
            var mock = Mockito.mock(field.getType());
            mocks.put(new MockDefinition(field.getGenericType(), name), mock);
        }
    }

    private List<Field> getMockFields(Class<?> testClass) {
        return FieldUtils.getAllFieldsList(testClass).stream()
                .filter(field -> field.getAnnotation(MockBean.class) != null)
                .collect(Collectors.toList());
    }

    @Getter
    @Accessors(fluent = true)
    @EqualsAndHashCode
    @RequiredArgsConstructor
    static class MockDefinition {
        private final Type type;
        private final String name;
    }
}
