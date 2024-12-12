package com.logicalbias.dropwizard.testing.extension.client;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class RequestBuilder {

    private final Client client;
    private final HttpMethod httpMethod;
    private final String url;
    private final MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
    private final MultivaluedMap<String, Object> params = new MultivaluedHashMap<>();
    private Object body;
    private int expectedStatus;

    enum HttpMethod {
        GET,
        POST,
        PATCH,
        PUT,
        DELETE
    }

    public RequestBuilder body(Object body) {
        this.body = body;
        return this;
    }

    /**
     * Adds the specified value to the request headers. If null, the key is removed.
     */
    public RequestBuilder header(String name, Object value) {
        if (value == null) {
            headers.remove(name);
        }
        else {
            headers.add(name, value);
        }

        return this;
    }

    public RequestBuilder headers(MultivaluedMap<String, Object> headers) {
        headers.forEach(this.headers::addAll);
        return this;
    }

    public RequestBuilder queryParam(String name, Object... values) {
        params.addAll(name, values);
        return this;
    }

    public RequestBuilder expectStatus(Response.Status status) {
        return expectStatus(status.getStatusCode());
    }

    public RequestBuilder expectStatus(int status) {
        this.expectedStatus = status;
        return this;
    }

    public void andReturn() {
        try (var response = callAndGetResponse()) {
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T andReturn(Class<T> responseType) {
        // Special case if a user requests Response.class directly
        if (responseType == Response.class) {
            return (T) callAndGetResponse();
        }

        try (var response = callAndGetResponse()) {
            return response.readEntity(responseType);
        }
    }

    public <T> T andReturn(GenericType<T> responseType) {
        try (var response = callAndGetResponse()) {
            return response.readEntity(responseType);
        }
    }

    public <T> T andReturn(Function<Response, T> function) {
        try (var response = callAndGetResponse()) {
            return function.apply(response);
        }
    }

    public void andConsumeResponse(Consumer<Response> consumer) {
        try (var response = callAndGetResponse()) {
            consumer.accept(response);
        }
    }

    public Response andReturnResponse() {
        return callAndGetResponse();
    }

    @Deprecated
    public void invoke() {
        andReturn();
    }

    @Deprecated
    public void invoke(Consumer<Response> consumer) {
        andConsumeResponse(consumer);
    }

    @Deprecated
    public <T> T invoke(Function<Response, T> function) {
        return andReturn(function);
    }

    @Deprecated
    public <T> T invoke(GenericType<T> responseType) {
        return andReturn(responseType);
    }

    @Deprecated
    public <T> T invoke(Class<T> responseType) {
        return andReturn(responseType);
    }

    private Response callAndGetResponse() {
        var target = client.target(url);

        // attach query params
        for (var param : params.entrySet()) {
            var paramKey = param.getKey();
            var paramValue = param.getValue();

            // Convert all param values to strings; otherwise, the URI Builder will wrap the values in brackets
            // Which fails to work for dropwizard (e.g. queryParam: ["error"", true] will result in ...?error=[true]
            var stringValues = Stream.ofNullable(paramValue)
                    .flatMap(Collection::stream)
                    .map(String::valueOf)
                    .toArray(String[]::new);
            target = target.queryParam(paramKey, (Object[]) stringValues);
        }

        var request = target.request()
                .headers(headers);

        var response = body == null
                ? request.method(httpMethod.name())
                : request.method(httpMethod.name(), Entity.json(body));

        assertResponse(response);
        return response;
    }

    private void assertResponse(Response response) {
        if (expectedStatus == 0) {
            return;
        }

        Assertions.assertEquals(expectedStatus, response.getStatus(), () -> {
            // When this occurs, we usually want to debug why the response is different, so add the actual response, formatted
            var responseBody = response.readEntity(String.class);
            return String.format("Returned responseStatus=%s does match expected responseStatus=%s; responseBody=%s",
                    response.getStatus(), expectedStatus, responseBody);
        });
    }

}
