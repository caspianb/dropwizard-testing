package com.logicalbias.dropwizard.testing.application;

import io.dropwizard.core.Configuration;
import lombok.Getter;

import com.fasterxml.jackson.annotation.JsonProperty;

@Getter
public class ApplicationConfiguration extends Configuration {
    @JsonProperty("name")
    private String name;
}
