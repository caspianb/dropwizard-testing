package com.logicalbias.dropwizard.testing.application.generic;

import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.logicalbias.dropwizard.testing.application.widgets.WidgetService;

@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class StringService implements GenericService<String> {

    private final WidgetService widgetService;

    @Override
    public String process(String value) {
        log.info("Processed {}", value);
        return value;
    }
}
