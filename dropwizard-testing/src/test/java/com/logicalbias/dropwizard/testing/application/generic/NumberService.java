package com.logicalbias.dropwizard.testing.application.generic;

import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.logicalbias.dropwizard.testing.application.widgets.WidgetService;

@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class NumberService implements GenericService<Number> {

    private final WidgetService widgetService;

    @Override
    public Number process(Number value) {
        log.info("Processed {}", value);
        return value;
    }
}
