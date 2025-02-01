package com.logicalbias.dropwizard.testing;

import jakarta.inject.Named;
import lombok.RequiredArgsConstructor;

import org.junit.jupiter.api.Test;
import org.jvnet.hk2.annotations.ContractsProvided;
import org.jvnet.hk2.annotations.Service;

import com.logicalbias.dropwizard.testing.application.DropwizardTestApplication;
import com.logicalbias.dropwizard.testing.extension.annotation.Import;
import com.logicalbias.dropwizard.testing.extension.context.DropwizardTest;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@DropwizardTest(value = DropwizardTestApplication.class, configFile = "config.yml", properties = "name=TestApp")
@Import(NamedBeanExtensionTest.TestBean2.class)
@Import(value = NamedBeanExtensionTest.TestBean1.class, name = "testBean1")
@Import(value = NamedBeanExtensionTest.TestBean3.class, name = "testBean3")
@RequiredArgsConstructor
public class NamedBeanExtensionTest {

    private final TestInterface defaultTestInterface;

    @Named("testBean1")
    private final TestInterface testInterface1;

    @Named("testBean2")
    private final TestInterface testInterface2;

    @Named("testBean3")
    private final TestInterface testInterface3;

    private final TestBean1 testBean1;
    private final TestBean2 testBean2;
    private final TestBean3 testBean3;

    @Test
    void testNamedBeansInjected() {

        // TestBean2 defined first; it should be the default
        assertInstanceOf(TestBean2.class, defaultTestInterface);

        assertInstanceOf(TestBean1.class, testInterface1);
        assertInstanceOf(TestBean2.class, testInterface2);
        assertInstanceOf(TestBean3.class, testInterface3);
        assertInstanceOf(TestBean1.class, testBean1);
        assertInstanceOf(TestBean2.class, testBean2);
        assertInstanceOf(TestBean3.class, testBean3);
    }

    public interface TestInterface {
    }

    @Service(name = "should-be-ignored")
    @ContractsProvided(TestInterface.class)
    public static class TestBean1 implements TestInterface {
    }

    @Service(name = "testBean2")
    @ContractsProvided(TestInterface.class)
    public static class TestBean2 implements TestInterface {
    }

    @ContractsProvided(TestInterface.class)
    public static class TestBean3 implements TestInterface {
    }
}
