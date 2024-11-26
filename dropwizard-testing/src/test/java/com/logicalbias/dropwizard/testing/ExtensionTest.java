package com.logicalbias.dropwizard.testing;

import jakarta.inject.Singleton;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.logicalbias.dropwizard.testing.application.DropwizardTestApplication;
import com.logicalbias.dropwizard.testing.application.WidgetService;
import com.logicalbias.dropwizard.testing.extension.annotation.Import;
import com.logicalbias.dropwizard.testing.extension.annotation.MockBean;
import com.logicalbias.dropwizard.testing.extension.client.TestClient;
import com.logicalbias.dropwizard.testing.extension.context.DropwizardTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DropwizardTest(value = DropwizardTestApplication.class, configFile = "config.yml", properties = "name=TestApp")
@RequiredArgsConstructor
@MockBean(WidgetService.class)
@Import(ExtensionTest.TestResource.class)
public class ExtensionTest {

    private final TestClient testClient;
    private final WidgetService widgetService;
    private final TestResource testResource;

    @Test
    void testMockWidgetCallsRealMethod() {
        Mockito.doCallRealMethod().when(widgetService).getWidget(Mockito.anyString());
        var id = "test-value";
        var response = testClient.get("widgets/{widgetId}", id)
                .expectStatus(Response.Status.OK)
                .invoke(String.class);

        assertEquals("Widget: " + id, response);
    }

    @Test
    void testMockWidgetReturnsMockedValue() {
        var id = UUID.randomUUID().toString();
        Mockito.doReturn(id).when(widgetService).getWidget(Mockito.anyString());
        var response = testClient.get("widgets/{widgetId}", "0")
                .expectStatus(Response.Status.OK)
                .invoke(String.class);

        assertEquals(id, response);
    }

    @Test
    void testImportedResourceCanBeAccessed() {
        var testValue = testResource.getTestValue();
        var response = testClient.get("tests")
                .expectStatus(Response.Status.OK)
                .invoke(String.class);
        assertEquals(testValue, response);

        // Test that we can interract with the imported resource and change the value returned
        testValue = UUID.randomUUID().toString();
        testResource.setTestValue(testValue);

        response = testClient.get("tests")
                .expectStatus(Response.Status.OK)
                .invoke(String.class);
        assertEquals(testValue, response);
    }

    @Data
    @Path("tests")
    @Singleton
    public static class TestResource {

        private String testValue = "test";

        @GET
        public String getTest() {
            return testValue;
        }
    }
}
