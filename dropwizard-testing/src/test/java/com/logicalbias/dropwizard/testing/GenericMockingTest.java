package com.logicalbias.dropwizard.testing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.internal.util.MockUtil;

import com.logicalbias.dropwizard.testing.application.DropwizardTestApplication;
import com.logicalbias.dropwizard.testing.application.generic.GenericService;
import com.logicalbias.dropwizard.testing.extension.annotation.MockBean;
import com.logicalbias.dropwizard.testing.extension.context.DropwizardTest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public class GenericMockingTest {

    @Nested
    @RequiredArgsConstructor
    @DropwizardTest(value = DropwizardTestApplication.class, configFile = "config.yml", properties = "name=TestApp")
    class FinalGenericFieldInjectionTest {

        @MockBean
        private final GenericService<String> stringService;
        private final GenericService<Number> numberService;

        @Test
        void testGenericTypes() {
            assertTrue(MockUtil.isMock(stringService));
            assertFalse(MockUtil.isMock(numberService));
        }
    }

    @Nested
    @RequiredArgsConstructor
    @DropwizardTest(value = DropwizardTestApplication.class, configFile = "config.yml", properties = "name=TestApp")
    class NonFinalGenericFieldInjectionTest {

        private GenericService<String> stringService;
        @MockBean
        private GenericService<Number> numberService;

        @Test
        void testGenericTypes() {
            assertFalse(MockUtil.isMock(stringService));
            assertTrue(MockUtil.isMock(numberService));
        }
    }

}
