package com.logicalbias.dropwizard.testing.extension.context;

import io.dropwizard.core.Application;
import io.dropwizard.core.Configuration;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.servlet.ServletContainer;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.platform.commons.support.AnnotationSupport;

import com.logicalbias.dropwizard.testing.extension.annotation.TestProperties;
import com.logicalbias.dropwizard.testing.extension.utils.TestHelpers;

import static com.logicalbias.dropwizard.testing.extension.utils.TestHelpers.buildClassInheritanceTree;
import static com.logicalbias.dropwizard.testing.extension.utils.TestHelpers.findAnnotations;

@Slf4j
@Getter(AccessLevel.PACKAGE)
class TestContextManager {

    private static final Namespace NAMESPACE = Namespace.create(TestContextManager.class);

    private final ExtensionContext context;
    private final DependencyContext dependencyContext;
    private final MockContext mockContext;
    private final List<ConfigOverride> configOverrides;

    private DropwizardAppExtension<?> appExtension;

    static TestContextManager from(ExtensionContext context) {
        var testClass = context.getRequiredTestClass();
        return getStore(context)
                .getOrComputeIfAbsent(testClass, k -> new TestContextManager(context), TestContextManager.class);
    }

    private static ExtensionContext.Store getStore(ExtensionContext context) {
        return context.getRoot().getStore(NAMESPACE);
    }

    private TestContextManager(ExtensionContext context) {
        this.context = context;
        this.dependencyContext = new DependencyContext();
        this.mockContext = new MockContext(context);
        this.configOverrides = new ArrayList<>();
    }

    void overrideProperty(String key, String value) {
        configOverrides.add(ConfigOverride.config(key, value));
    }

    private DropwizardAppExtension<?> initialize() {
        if (appExtension != null) {
            return appExtension;
        }

        try {
            // This initializes the test application using any properties, dependencies, mocks, etc. that have
            // been registered with the TestContext at this point. We try to initialize as late as possible
            // to allow other extensions the ability to add/update this data instead of directly calling
            // beforeAll inside the junit beforeAll hook (typically called during parameter resolution
            // or in the first beforeEach block).
            appExtension = createDropwizardAppExtension(context, configOverrides)
                    .addListener(new TestServiceListener<>(this));

            appExtension.beforeAll(context);
            return appExtension;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    void beforeEach() throws Exception {
        initialize().before();
    }

    void afterEach() {
        mockContext.resetMocks();
        appExtension.after();
    }

    void afterAll() {
        try {
            appExtension.afterAll(context);
        }
        finally {
            getStore(context).remove(context.getRequiredTestClass());
        }
    }

    DropwizardAppExtension<?> getAppExtension() {
        return initialize();
    }

    /**
     * Retrieves the primary bean for the specified class type from the DI context.
     */
    @SuppressWarnings("unchecked")
    <T> T getBean(Class<T> classType) {
        var appExtension = initialize();

        if (classType == DropwizardAppExtension.class) {
            return (T) appExtension;
        }

        var environment = appExtension.getEnvironment();
        if (environment.getJerseyServletContainer() instanceof ServletContainer) {
            var container = (ServletContainer) environment.getJerseyServletContainer();
            var context = container.getApplicationHandler().getInjectionManager();
            var bean = context.getInstance(classType);
            if (bean != null) {
                return bean;
            }

            // If no type was registered against this specific interface, we're going to do something dropwizard
            // doesn't do. Scan up from the type and try to find some other superclass or interface it might've been
            // registered under.

            // TODO This still doesn't work quite correctly because context.getInstance() still requires a single type bound to the supertype ...
            // otherwise, only the first type will be returned... Might have to get more clever and return the list of types given the interface
            // and scan through and find an exact class match.
            return buildClassInheritanceTree(classType).stream()
                    .map(context::getInstance)
                    .filter(classType::isInstance)
                    .map(classType::cast)
                    .findFirst()
                    .orElse(null);
        }

        throw new IllegalStateException("Servlet container is not of type " + ServletContainer.class.getName());
    }

    // ********************************************************************************
    // Utility methods and classes defined below
    // ********************************************************************************

    static Optional<DropwizardTest> getDropwizardTestAnnotation(ExtensionContext context) {
        var testClass = context.getRequiredTestClass();
        return AnnotationSupport.findAnnotation(testClass, DropwizardTest.class);
    }

    @SuppressWarnings("unchecked")
    static <C extends Configuration> DropwizardAppExtension<C> createDropwizardAppExtension(ExtensionContext context, List<ConfigOverride> configOverrides) {
        var testClass = context.getRequiredTestClass();
        var dropwizardTest = getDropwizardTestAnnotation(context)
                .orElseThrow(() -> new IllegalStateException("@DropwizardTest annotation was not located for " + testClass.getName()));

        var applicationClass = (Class<? extends Application<C>>) dropwizardTest.value();
        var configFile = getConfigFile(dropwizardTest);
        log.info("Initializing @DropwizardTest application context [configFile={}].", configFile);

        if (dropwizardTest.webEnvironment() == DropwizardTest.WebEnvironment.RANDOM) {
            configOverrides.add(ConfigOverride.randomPorts());
        }

        configOverrides.addAll(getPropertyOverrides(testClass, dropwizardTest));
        return new DropwizardAppExtension<>(applicationClass, configFile, configOverrides.toArray(ConfigOverride[]::new));
    }

    static String getConfigFile(DropwizardTest dropwizardTest) {
        var configFile = dropwizardTest.configFile();
        return dropwizardTest.useResourceFilePath()
                ? ResourceHelpers.resourceFilePath(configFile)
                : configFile;
    }

    static List<ConfigOverride> getPropertyOverrides(Class<?> testClass, DropwizardTest dropwizardTest) {
        var properties = new LinkedHashMap<String, String>();

        // Scan for @TestProperties annotations first; these take precedence over other configured properties
        findAnnotations(testClass, TestProperties.class).stream()
                .flatMap(testProperties -> Arrays.stream(testProperties.properties()))
                .map(TestHelpers::splitProperty)
                .filter(props -> StringUtils.isNoneBlank(props[0], props[1]))
                .forEach(props -> properties.putIfAbsent(props[0], props[1]));

        // Now scan for properties on the DropwizardTest annotation
        Arrays.stream(dropwizardTest.properties())
                .map(TestHelpers::splitProperty)
                .filter(props -> StringUtils.isNoneBlank(props[0], props[1]))
                .forEach(props -> properties.putIfAbsent(props[0], props[1]));

        return properties.entrySet().stream()
                .map(props -> ConfigOverride.config(props.getKey(), props.getValue()))
                .collect(Collectors.toList());
    }

}
