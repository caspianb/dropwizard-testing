package com.logicalbias.dropwizard.testing.extension.client;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import lombok.AccessLevel;
import lombok.Getter;

import java.util.List;

@Getter
public class TestClient {

    @Getter(AccessLevel.NONE)
    private final MultivaluedMap<String, Object> defaultHeaders = new MultivaluedHashMap<>();
    private final Client client;
    private final int localPort;

    public TestClient(Client client, int localPort) {
        this.client = client;
        this.localPort = localPort;
    }

    /**
     * Helper method to create a {@link GenericType} for a list of the specified type.
     */
    public static <T> GenericType<List<T>> listOf(Class<T> classType) {
        return new GenericType<>() {
        };
    }

    /**
     * Add a (non-null) header which will be automatically added to every request.
     */
    public TestClient defaultHeader(String key, Object value) {
        if (value != null) {
            defaultHeaders.add(key, value);
        }
        return this;
    }

    public RequestBuilder get(String path, Object... args) {
        return request(RequestBuilder.HttpMethod.GET, path, args);
    }

    public RequestBuilder post(String path, Object... args) {
        return request(RequestBuilder.HttpMethod.POST, path, args);
    }

    public RequestBuilder patch(String path, Object... args) {
        return request(RequestBuilder.HttpMethod.PATCH, path, args);
    }

    public RequestBuilder put(String path, Object... args) {
        return request(RequestBuilder.HttpMethod.PUT, path, args);
    }

    public RequestBuilder delete(String path, Object... args) {
        return request(RequestBuilder.HttpMethod.DELETE, path, args);
    }

    private RequestBuilder request(RequestBuilder.HttpMethod method, String path, Object... args) {
        var url = resolveUrl(path, args);
        return new RequestBuilder(client, method, url)
                .headers(defaultHeaders);
    }

    private String resolveUrl(String url, Object... args) {
        if (url.startsWith("/")) {
            url = url.substring(1);
        }

        // Replace {...} in URL with args
        for (var arg : args) {
            url = url.replaceFirst("\\{.*?}", String.valueOf(arg));
        }

        return String.format("http://localhost:%s/%s", localPort, url);
    }

}
