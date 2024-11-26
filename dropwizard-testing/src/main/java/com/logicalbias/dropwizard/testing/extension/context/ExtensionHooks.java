package com.logicalbias.dropwizard.testing.extension.context;

import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * <p>
 * This class is provided to be used by other dropwizard test extensions.
 * It provides hook points allowing dropwizard test extensions to:
 * </p>
 * <ul>
 * <li>extensions to add or override dependencies in the hk2 context</li>
 * <li>extensions to add or override properties in the running application</li>
 * </ul>
 *
 * <pre>
 *     // This should be executed as soon as possible in the extension (prior to the test constructor)
 *     ExtensionHooks.from(extensionContext)
 *        .register(SomeClass.class, someClassInstance)
 *        .register(AnotherClass.class, anotherClassInstance);
 * </pre>
 *
 * <p>
 * NOTE: If the extension implements the ParameterResolver callback, then it should also avoid resolving any parameters if
 * the context is active for any dependencies it has registered with this class.
 * </p>
 *
 * <pre>
 * boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
 *     // If active, return false (unless other parameters not added to the registry still need to be supported)
 *     if (ExtensionHooks.isActive())
 *         return false;
 * }
 * </pre>
 */
public interface ExtensionHooks {

    static boolean isActive(ExtensionContext context) {
        return TestContextManager.getDropwizardTestAnnotation(context).isPresent();
    }

    static ExtensionHooks from(ExtensionContext context) {
        var testContext = isActive(context) ? TestContextManager.from(context) : null;

        return new ExtensionHooks() {
            @Override
            public <T> ExtensionHooks register(Class<T> classType, String name, T object) {
                if (testContext != null) {
                    testContext.getDependencyContext().add(classType, name, object);
                }

                return this;
            }

            @Override
            public ExtensionHooks overrideProperty(String key, String value) {
                if (testContext != null) {
                    testContext.overrideProperty(key, value);
                }
                return this;
            }
        };
    }

    default <T> ExtensionHooks register(Class<T> classType, T object) {
        return register(classType, null, object);
    }

    <T> ExtensionHooks register(Class<T> classType, String name, T object);

    ExtensionHooks overrideProperty(String key, String value);

}
