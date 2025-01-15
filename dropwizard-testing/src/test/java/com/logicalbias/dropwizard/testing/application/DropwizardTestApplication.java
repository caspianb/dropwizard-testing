package com.logicalbias.dropwizard.testing.application;

import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.core.Application;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import jakarta.inject.Singleton;

import org.glassfish.hk2.utilities.binding.AbstractBinder;

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

        environment.jersey().register(WidgetResource.class);
        environment.jersey().register(new AbstractBinder() {
            @Override
            protected void configure() {
                bindAsContract(WidgetService.class).in(Singleton.class);
            }
        });
    }
}
