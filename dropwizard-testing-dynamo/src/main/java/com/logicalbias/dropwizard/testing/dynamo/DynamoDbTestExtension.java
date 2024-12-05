package com.logicalbias.dropwizard.testing.dynamo;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.streams.DynamoDbStreamsAsyncClient;
import software.amazon.awssdk.services.dynamodb.streams.DynamoDbStreamsClient;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.platform.commons.support.AnnotationSupport;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.logicalbias.dropwizard.testing.extension.context.ExtensionHooks;
import com.logicalbias.dropwizard.testing.extension.utils.TestHelpers;

@Slf4j
class DynamoDbTestExtension implements ParameterResolver,
        ExecutionCondition,
        AfterAllCallback {

    private static final Namespace NAMESPACE = Namespace.create(DynamoDbTestExtension.class);

    /**
     * Extension provided utility classes. Always register these as injectable via test parameters.
     */
    private static final Map<Class<?>, Function<DynamoManager, Object>> EXTENSION_UTILS = Map.of(
            DynamoManager.class, dm -> dm,
            DynamoTestUtils.class, DynamoManager::getDynamoTestUtils
    );

    /**
     * Amazon / Dynamo library specific clients. We will register all of these against the HK2 context
     * if registerClients is enabled (default behavior).
     */
    private static final Map<Class<?>, Function<DynamoManager, Object>> DYNAMO_CLIENTS = Map.of(
            AmazonDynamoDB.class, DynamoManager::getAmazonDynamoDb,
            DynamoDbClient.class, DynamoManager::getClient,
            DynamoDbEnhancedClient.class, DynamoManager::getEnhancedClient,
            DynamoDbAsyncClient.class, DynamoManager::getAsyncClient,
            DynamoDbEnhancedAsyncClient.class, DynamoManager::getEnhancedAsyncClient,
            DynamoDbStreamsClient.class, DynamoManager::getStreamsClient,
            DynamoDbStreamsAsyncClient.class, DynamoManager::getStreamsAsyncClient
    );

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        getOrInitializeDynamoManager(context);
        return ConditionEvaluationResult.enabled("");
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext context) {
        var paramType = parameterContext.getParameter().getType();
        if (EXTENSION_UTILS.containsKey(paramType)) {
            return true;
        }

        // If a DependencyRegistry is detected, then do not support any client types
        // It will handle the client parameter resolution for us
        if (ExtensionHooks.isActive(context)) {
            return false;
        }

        return DYNAMO_CLIENTS.containsKey(paramType);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext context) {
        var dynamoManager = getOrInitializeDynamoManager(context);
        var paramType = parameterContext.getParameter().getType();

        if (EXTENSION_UTILS.containsKey(paramType)) {
            return EXTENSION_UTILS.get(paramType).apply(dynamoManager);
        }

        return DYNAMO_CLIENTS.get(paramType).apply(dynamoManager);
    }

    @Override
    public void afterAll(ExtensionContext context) {
        shutdown(context);
    }

    private static void shutdown(ExtensionContext context) {
        getStore(context)
                .remove(DynamoManager.class, DynamoManager.class)
                .shutdown();
    }

    @SuppressWarnings("unchecked")
    private static DynamoManager getOrInitializeDynamoManager(ExtensionContext context) {
        return getStore(context)
                .getOrComputeIfAbsent(DynamoManager.class, m -> {
                    var dynamoDbTest = dynamoAnnotation(context);
                    var dynamoManager = startDynamoManager(context);

                    // Add dynamoManager clients to the dropwizard test context if registerClients is enabled
                    if (dynamoDbTest.registerClients()) {
                        var dependencyRegistry = ExtensionHooks.from(context);
                        DYNAMO_CLIENTS.forEach((type, f) -> {
                            var obj = f.apply(dynamoManager);
                            dependencyRegistry.register((Class<Object>) type, obj);
                        });
                    }

                    return dynamoManager;
                }, DynamoManager.class);
    }

    private static DynamoManager startDynamoManager(ExtensionContext context) {
        var dynamoDbTest = dynamoAnnotation(context);
        var environment = dynamoDbTest.environment();

        if (environment == DynamoDbTest.Environment.EMBEDDED) {
            return DynamoManager.startEmbedded();
        }

        if (environment == DynamoDbTest.Environment.DOCKER) {
            var manager = DynamoManager.startContainer(dynamoDbTest.port());
            var extensions = ExtensionHooks.from(context);
            var endpoint = manager.getEndpoint().toString();

            Arrays.stream(dynamoDbTest.properties())
                    .map(property -> TestHelpers.splitPropertyWithVariable(property, "endpoint", endpoint))
                    .forEach(prop -> extensions.overrideProperty(prop[0], prop[1]));

            return manager;
        }

        throw new IllegalStateException("Unknown environment set on @DynamoDbTest: " + environment);
    }

    private static DynamoDbTest dynamoAnnotation(ExtensionContext context) {
        var testClass = context.getRequiredTestClass();
        return AnnotationSupport.findAnnotation(testClass, DynamoDbTest.class)
                .orElseThrow(() -> new IllegalStateException("@DynamoDbTest annotation was not located for " + testClass));
    }

    private static ExtensionContext.Store getStore(ExtensionContext context) {
        return context.getStore(NAMESPACE);
    }
}
