package com.logicalbias.dropwizard.testing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.logicalbias.dropwizard.testing.application.DropwizardTestApplication;
import com.logicalbias.dropwizard.testing.extension.client.TestClient;
import com.logicalbias.dropwizard.testing.extension.context.DropwizardTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@DropwizardTest(value = DropwizardTestApplication.class, configFile = "config.yml")
@RequiredArgsConstructor
public class ApplicationTestClientTest {

    private final TestClient client;

    @Test
    void testQueryParams() {
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

}
