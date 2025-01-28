package com.logicalbias.dropwizard.testing;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import com.logicalbias.dropwizard.testing.application.DropwizardTestApplication;
import com.logicalbias.dropwizard.testing.extension.annotation.Import;
import com.logicalbias.dropwizard.testing.extension.client.TestClient;
import com.logicalbias.dropwizard.testing.extension.context.DropwizardTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DropwizardTest(value = DropwizardTestApplication.class, configFile = "config.yml", properties = "name=TestApp")
@Import(ImportBeanExtensionTest.TestResource.class)
@RequiredArgsConstructor
public class ImportBeanExtensionTest {

    private final TestClient testClient;
    private final TestResource testResource;
    private final String testHeaderValue = UUID.randomUUID().toString();

    @BeforeEach
    void beforeEach() {
        testClient.defaultHeader("test-header", testHeaderValue);
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

    @RepeatedTest(5)
    void testClientHeadersAndResponse() {
        var value = UUID.randomUUID().toString();
        var response = testClient.get("tests")
                .header("another-header", value)
                .expectStatus(200)
                .andReturnResponse();

        var headers = response.getHeaders();
        var responseStr = response.readEntity(String.class);

        assertEquals(testResource.getTestValue(), responseStr);

        assertEquals(1, headers.get("test-header").size());
        assertEquals(testHeaderValue, headers.getFirst("test-header"));

        assertEquals(1, headers.get("another-header").size());
        assertEquals(value, headers.getFirst("another-header"));
    }

    @Data
    @Path("tests")
    @Singleton
    public static class TestResource {

        private String testValue = UUID.randomUUID().toString();

        @GET
        public Response getTest(@Context HttpHeaders headers) {
            var response = Response.ok(testValue);

            // Return all headers that were passed in
            headers.getRequestHeaders().forEach((name, values) -> {
                for (var value : values) {
                    response.header(name, value);
                }
            });

            return response.build();
        }
    }
}
