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
                .andReturn(String.class);

        assertEquals("Widget: " + id, response);
    }

    @Test
    void testMockWidgetReturnsMockedValue() {
        var id = UUID.randomUUID().toString();
        Mockito.doReturn(id).when(widgetService).getWidget(Mockito.anyString());
        var response = testClient.get("widgets/{widgetId}", "0")
                .expectStatus(Response.Status.OK)
                .andReturn(String.class);

        assertEquals(id, response);
    }

    @Test
    void testImportedResourceCanBeAccessed() {
        var testValue = testResource.getTestValue();
        var response = testClient.get("tests")
                .expectStatus(Response.Status.OK)
                .andReturn(String.class);
        assertEquals(testValue, response);

        // Test that we can interact with the imported resource and change the value returned
        testValue = UUID.randomUUID().toString();
        testResource.setTestValue(testValue);

        response = testClient.get("tests")
                .expectStatus(Response.Status.OK)
                .andReturn(String.class);
        assertEquals(testValue, response);
    }

    @Test
    void testClientResponse() {
        try (var response = testClient.get("tests")
                .expectStatus(200)
                .andReturnResponse()) {

            var headers = response.getHeaders();
            var responseStr = response.readEntity(String.class);
            assertEquals(testResource.getTestValue(), responseStr);
            assertEquals(testResource.getTestHeader(), headers.getFirst(TestResource.TEST_HEADER_NAME));
        }
    }

    @Data
    @Path("tests")
    @Singleton
    public static class TestResource {

        public static String TEST_HEADER_NAME = "test-header";

        private String testValue = UUID.randomUUID().toString();
        private String testHeader = UUID.randomUUID().toString();

        @GET
        public Response getTest() {
            return Response
                    .ok(testValue)
                    .header(TEST_HEADER_NAME, testHeader)
                    .build();
        }
    }
}
