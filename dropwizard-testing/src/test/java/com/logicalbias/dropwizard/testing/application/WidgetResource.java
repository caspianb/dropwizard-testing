package com.logicalbias.dropwizard.testing.application;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;

@Singleton
@Path("widgets")
@Produces(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class WidgetResource {

    private final WidgetService widgetService;

    @GET
    @Path("{id}")
    public String getWidget(@PathParam("id") String id) {
        return widgetService.getWidget(id);
    }
}
