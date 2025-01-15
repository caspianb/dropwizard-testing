package com.logicalbias.dropwizard.testing.application.widgets;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Singleton
@Path("widgets")
@Produces(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class WidgetResource {

    private final WidgetService widgetService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @GET
    @Path("{id}")
    public String getWidget(@PathParam("id") String id) {
        return widgetService.getWidget(id);
    }

    @GET
    @Path("params")
    public String getParams(
            @QueryParam("stringValue") String stringValue,
            @QueryParam("intValue") Integer intValue,
            @QueryParam("listValue") List<String> listValue) throws JsonProcessingException {
        var response = Map.of(
                "stringValue", String.valueOf(stringValue),
                "intValue", String.valueOf(intValue),
                "listValue", listValue
        );

        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);
    }
}
