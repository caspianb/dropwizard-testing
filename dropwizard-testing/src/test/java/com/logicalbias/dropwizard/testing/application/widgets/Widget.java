package com.logicalbias.dropwizard.testing.application.widgets;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Widget {
    private Long id;
    private String name;
    private String description;
}
