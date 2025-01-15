package com.logicalbias.dropwizard.testing;

import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.logicalbias.dropwizard.testing.application.DropwizardTestApplication;
import com.logicalbias.dropwizard.testing.application.widgets.WidgetService;
import com.logicalbias.dropwizard.testing.extension.annotation.MockBean;
import com.logicalbias.dropwizard.testing.extension.client.TestClient;
import com.logicalbias.dropwizard.testing.extension.context.DropwizardTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DropwizardTest(value = DropwizardTestApplication.class, configFile = "config.yml", properties = "name=TestApp")
@MockBean(WidgetService.class)
@RequiredArgsConstructor
public class MockBeanExtensionTest {

    private final TestClient testClient;
    private final WidgetService widgetService;
    private final String testHeaderValue = UUID.randomUUID().toString();

    @BeforeEach
    void beforeEach() {
        testClient.defaultHeader("test-header", testHeaderValue);
    }

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
}
