package com.logicalbias.dropwizard.testing;

import lombok.RequiredArgsConstructor;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.logicalbias.dropwizard.testing.application.ApplicationConfiguration;
import com.logicalbias.dropwizard.testing.application.DropwizardTestApplication;
import com.logicalbias.dropwizard.testing.extension.annotation.TestProperties;
import com.logicalbias.dropwizard.testing.extension.context.DropwizardTest;

@RequiredArgsConstructor
@TestProperties(properties = "name=test-child")
class TestPropertiesOverrideTest implements DeclaredParent {

    private final ApplicationConfiguration configuration;

    @Test
    void testPropertyOverridesIsInheritedAtClassLevel() {
        Assertions.assertEquals("test-child", configuration.getName());
        Assertions.assertEquals("test-application", configuration.getDescription());
    }
}

@DropwizardTest(value = DropwizardTestApplication.class, configFile = "config.yml", properties = "name=TestApp")
@TestProperties(properties = {
        "name=parent",
        "description=test-application"
})
interface DeclaredParent {
}
