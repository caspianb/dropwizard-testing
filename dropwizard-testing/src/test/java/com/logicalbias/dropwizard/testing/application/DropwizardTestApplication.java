package com.logicalbias.dropwizard.testing.application;

import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.core.Application;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import jakarta.inject.Singleton;

import org.glassfish.hk2.api.TypeLiteral;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import com.codahale.metrics.health.HealthCheck;
import com.logicalbias.dropwizard.testing.application.generic.GenericService;
import com.logicalbias.dropwizard.testing.application.generic.NumberService;
import com.logicalbias.dropwizard.testing.application.generic.StringService;
import com.logicalbias.dropwizard.testing.application.widgets.WidgetResource;
import com.logicalbias.dropwizard.testing.application.widgets.WidgetService;

/**
 * Integration test application.
 */
public class DropwizardTestApplication extends Application<ApplicationConfiguration> {

    private String name;

    public static void main(String[] args) throws Exception {
        new DropwizardTestApplication().run("server", "config.yml");
    }

    @Override
    public String getName() {
        if (name == null) {
            return super.getName();
        }
        return name;
    }

    @Override
    public void initialize(Bootstrap<ApplicationConfiguration> bootstrap) {
        super.initialize(bootstrap);

        // Load configuration file directly from resources
        bootstrap.setConfigurationSourceProvider(new ResourceConfigurationSourceProvider());
    }

    @Override
    public void run(ApplicationConfiguration configuration, Environment environment) {
        this.name = configuration.getName();

        environment.healthChecks().register("health-check", new ApplicationHealthCheck());

        environment.jersey().register(WidgetResource.class);
        environment.jersey().register(ApplicationBinder.class);
    }

    static class ApplicationBinder extends AbstractBinder {

        @Override
        protected void configure() {
            bindAsContract(WidgetService.class).in(Singleton.class);

            bindGenericServices();
        }

        void bindGenericServices() {
            bindAsContract(StringService.class)
                    .in(Singleton.class)
                    .to(new TypeLiteral<GenericService<String>>() {
                    });

            bindAsContract(NumberService.class)
                    .in(Singleton.class)
                    .to(new TypeLiteral<GenericService<Number>>() {
                    });
        }
    }

    static class ApplicationHealthCheck extends HealthCheck {

        @Override
        protected Result check() throws Exception {
            return Result.healthy();
        }
    }
}
