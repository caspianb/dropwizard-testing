package com.logicalbias.dropwizard.testing.application.widgets;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

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
