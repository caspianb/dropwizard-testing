package com.logicalbias.dropwizard.testing.extension.context;

import io.dropwizard.core.Configuration;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import jakarta.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Type;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.junit.platform.commons.support.AnnotationSupport;
import org.jvnet.hk2.annotations.ContractsProvided;
import org.jvnet.hk2.annotations.Service;

import static com.logicalbias.dropwizard.testing.extension.utils.TestHelpers.buildTypeLiteral;

/**
 * ServiceListener will run before the target application 'run' method is invoked on service startup. This listener
 * will allow us to hook into the environment context and inject / override any test dependencies as defined within
 * the dependency or mock context.
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class TestServiceListener<C extends Configuration> extends DropwizardAppExtension.ServiceListener<C> {

    private final TestContextManager testContextManager;

    @Override
    public void onRun(C configuration, Environment environment, DropwizardAppExtension<C> rule) {
        // First look for imported classes and register them directly as a jersey component
        var context = testContextManager.getContext();
        var importedClasses = ImportContext.getImportClassTypes(context);
        importedClasses.forEach(environment.jersey()::register);

        environment.jersey().register(new TestBinder(testContextManager));
    }

    @RequiredArgsConstructor
    private static class TestBinder extends AbstractBinder {

        private final TestContextManager testContextManager;

        @Override
        protected void configure() {
            // Bind mocks first; this ensures mocks for dependencies being provided
            // from other extensions will be the primary dependency in the DI context.
            testContextManager.getMockContext().forEach(this::bind);
            testContextManager.getDependencyContext().forEach(this::bind);

            bindImportedTypes();
        }

        private void bindImportedTypes() {
            // Bind our imported classes into the DI context as well
            var importedClasses = ImportContext.getImportClassTypes(testContextManager.getContext());
            importedClasses.forEach(type -> {
                var name = AnnotationSupport.findAnnotation(type, Service.class)
                        .map(Service::name)
                        .orElse(null);

                var contractTypes = AnnotationSupport.findAnnotation(type, ContractsProvided.class)
                        .map(ContractsProvided::value)
                        .orElseGet(() -> new Class[0]);

                // Bind type to itself as well as all additional provided contracts
                var binding = bindAsContract(type);
                for (var contractType : contractTypes) {
                    binding = binding.to(contractType);
                }
                binding.named(name)
                        .in(Singleton.class)
                        .ranked(Integer.MAX_VALUE);
            });
        }

        @SuppressWarnings("unchecked")
        private <T> void bind(MockContext.MockDefinition mockDefinition, T mock) {
            bind((Class<T>) mockDefinition.type(), mockDefinition.typeArguments(), mockDefinition.name(), mock);
        }

        private <T> void bind(DependencyContext.DependencyInfo<T> dependencyInfo) {
            bind(dependencyInfo.classType(), null, dependencyInfo.name(), dependencyInfo.instance());
        }

        private <T> void bind(Class<T> type, Type[] typeArguments, String name, T instance) {
            if (typeArguments == null || typeArguments.length == 0) {
                bind(instance)
                        .to(type)
                        .named(name == null || name.isBlank() ? null : name)
                        .ranked(Integer.MAX_VALUE);
            }
            else {
                bind(instance)
                        .to(buildTypeLiteral(type, typeArguments))
                        .named(name == null || name.isBlank() ? null : name)
                        .ranked(Integer.MAX_VALUE);
            }
        }
    }
}
