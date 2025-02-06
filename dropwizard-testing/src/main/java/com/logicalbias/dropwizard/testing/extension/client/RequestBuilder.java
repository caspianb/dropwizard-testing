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
import java.util.Map;
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

    /**
     * Sets the body of the request.
     */
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

    /**
     * Appends passed in headers to the request.
     */
    public RequestBuilder headers(MultivaluedMap<String, Object> headers) {
        headers.forEach(this.headers::addAll);
        return this;
    }

    /**
     * Appends the specified query params to the request. Repeated calls
     * with the same parameter name will append to the parameter.
     */
    public RequestBuilder queryParam(String name, Object... values) {
        params.addAll(name, values);
        return this;
    }

    /**
     * Sets the query params to [key=value] from the passed in map.
     * Any existing parameters sharing the same key will be removed.
     * Note that this method will only allow for a single value for the
     * query parameter. If a list of values is desired, see {@link RequestBuilder#queryParams(MultivaluedMap)}.
     */
    public RequestBuilder queryParams(Map<String, Object> queryParams) {
        queryParams.forEach(params::putSingle);
        return this;
    }

    /**
     * Adds the query params to [key=value] from the passed in map.
     * Any existing parameters sharing the same key will be removed.
     */
    public RequestBuilder queryParams(MultivaluedMap<String, Object> queryParams) {
        params.putAll(queryParams);
        return this;
    }

    /**
     * Sets the expected response status which will be asserted on when after the request is processed.
     * The assertion is disabled if this value is <= 0.
     */
    public RequestBuilder expectStatus(Response.Status status) {
        return expectStatus(status.getStatusCode());
    }

    /**
     * Sets the expected response status which will be asserted on when after the request is processed.
     * The assertion is disabled if this value is <= 0.
     */
    public RequestBuilder expectStatus(int status) {
        this.expectedStatus = status;
        return this;
    }

    /**
     * Invokes the request; no return value.
     */
    public void andReturn() {
        try (var ignored = callAndGetResponse()) {
            // Nothing to do here
        }
    }

    /**
     * @return The response body of the request. If direct access to the
     * client {@link Response} object is desired, pass in Response.class.
     */
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

    /**
     * @return The generic response body of the request.
     */
    public <T> T andReturn(GenericType<T> responseType) {
        try (var response = callAndGetResponse()) {
            return response.readEntity(responseType);
        }
    }

    /**
     * @return The value from applying the {@link Response}
     * object to the given function.
     */
    public <T> T andReturn(Function<Response, T> function) {
        try (var response = callAndGetResponse()) {
            return function.apply(response);
        }
    }

    /**
     * Invokes Invokes the request and passes the {@link Response}
     * object into the supplied consumer.
     */
    public void andConsumeResponse(Consumer<Response> consumer) {
        try (var response = callAndGetResponse()) {
            consumer.accept(response);
        }
    }

    /**
     * @return {@link Response} from the request.
     */
    public Response andReturnResponse() {
        return callAndGetResponse();
    }

    private Response callAndGetResponse() {
        var target = client.target(url);

        // attach query params
        for (var param : params.entrySet()) {
            var paramKey = param.getKey();
            var paramValue = param.getValue();

            // Convert all param values to strings; otherwise, the URI Builder will wrap the values in brackets
            // Which fails to work for dropwizard (e.g. queryParam: ["error", true] will result in ...?error=[true]
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
