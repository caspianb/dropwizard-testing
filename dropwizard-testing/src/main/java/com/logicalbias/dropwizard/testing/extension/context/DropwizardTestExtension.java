package com.logicalbias.dropwizard.testing.extension.context;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;

import com.logicalbias.dropwizard.testing.extension.client.TestClient;

@Slf4j
class DropwizardTestExtension implements
        ParameterResolver,
        BeforeEachCallback,
        AfterEachCallback,
        AfterAllCallback {

    static final Namespace NAMESPACE = Namespace.create(DropwizardTestExtension.class);

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        testContextManager(context).beforeEach();
    }

    @Override
    public void afterEach(ExtensionContext context) {
        testContextManager(context).afterEach();
    }

    @Override
    public void afterAll(ExtensionContext context) {
        testContextManager(context).afterAll();
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        var testContext = testContextManager(extensionContext);
        var paramType = parameterContext.getParameter().getType();

        if (paramType == TestClient.class) {
            return true;
        }

        return testContext.getBean(paramType) != null;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        var testContext = testContextManager(extensionContext);
        var paramType = parameterContext.getParameter().getType();

        if (paramType == TestClient.class) {
            return getTestClient(extensionContext);
        }

        return testContext.getBean(paramType);
    }

    private static TestClient getTestClient(ExtensionContext context) {
        var testContext = testContextManager(context);
        var appExtension = testContext.getAppExtension();
        return getStore(context)
                .getOrComputeIfAbsent(TestClient.class, key -> {
                    var client = appExtension.client();
                    var port = appExtension.getLocalPort();
                    return new TestClient(client, port);
                }, TestClient.class);
    }

    private static TestContextManager testContextManager(ExtensionContext context) {
        return TestContextManager.from(context);
    }

    private static ExtensionContext.Store getStore(ExtensionContext context) {
        return context.getStore(NAMESPACE);
    }
}
