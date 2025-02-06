package com.logicalbias.dropwizard.testing;

import io.dropwizard.testing.junit5.DropwizardAppExtension;
import jakarta.ws.rs.core.MultivaluedHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.logicalbias.dropwizard.testing.application.ApplicationConfiguration;
import com.logicalbias.dropwizard.testing.application.DropwizardTestApplication;
import com.logicalbias.dropwizard.testing.extension.client.TestClient;
import com.logicalbias.dropwizard.testing.extension.context.DropwizardTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@DropwizardTest(value = DropwizardTestApplication.class, configFile = "config.yml")
@RequiredArgsConstructor
public class TestClientTest {

    private final DropwizardAppExtension<ApplicationConfiguration> appExtension;
    private final ApplicationConfiguration configuration;
    private final TestClient client;

    @Test
    void testExtensionSupportedParameters() {
        assertNotNull(appExtension);
        assertNotNull(configuration);
        assertNotNull(client);
    }

    @Test
    void testAddQueryParams() {
        var response = client.get("widgets/params")
                .queryParam("stringValue", "Test String")
                .queryParam("listValue", "Value1", "Value2")
                .queryParam("intValue", 27)
                .andReturn(JsonNode.class);
        log.info("{}", response);

        assertEquals("Test String", response.path("stringValue").asText());
        assertTrue(response.path("listValue").isArray());
        var listValue = StreamSupport.stream(response.path("listValue").spliterator(), false)
                .map(JsonNode::asText)
                .collect(Collectors.toList());

        assertEquals(List.of("Value1", "Value2"), listValue);
        assertEquals(27, response.path("intValue").asInt());
    }

    @Test
    void testQueryMap() {
        // Can't use a standard map to send in a list of values; must use the multimap api for that
        var response = client.get("widgets/params")
                .queryParams(Map.of(
                        "stringValue", "Test String",
                        "listValue", "Value1",
                        "intValue", 27
                ))
                .andReturn(JsonNode.class);
        log.info("{}", response);

        assertEquals("Test String", response.path("stringValue").asText());
        assertTrue(response.path("listValue").isArray());
        var listValue = StreamSupport.stream(response.path("listValue").spliterator(), false)
                .map(JsonNode::asText)
                .collect(Collectors.toList());

        assertEquals(List.of("Value1"), listValue);
        assertEquals(27, response.path("intValue").asInt());
    }

    @Test
    void testQueryParamMultiMap() {
        var multiMap = new MultivaluedHashMap<String, Object>(Map.of(
                "stringValue", "Test String",
                "intValue", 27
        ));
        multiMap.put("listValue", List.of("Value1", "Value2"));

        var response = client.get("widgets/params")
                .queryParams(multiMap)
                .andReturn(JsonNode.class);
        log.info("{}", response);

        assertEquals("Test String", response.path("stringValue").asText());
        assertTrue(response.path("listValue").isArray());
        var listValue = StreamSupport.stream(response.path("listValue").spliterator(), false)
                .map(JsonNode::asText)
                .collect(Collectors.toList());

        assertEquals(List.of("Value1", "Value2"), listValue);
        assertEquals(27, response.path("intValue").asInt());
    }

}
