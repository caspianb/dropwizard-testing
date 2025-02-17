package com.logicalbias.dropwizard.testing.dynamo;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.streams.DynamoDbStreamsAsyncClient;
import software.amazon.awssdk.services.dynamodb.streams.DynamoDbStreamsClient;

import java.net.URI;

import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.local.embedded.DynamoDBEmbedded;
import com.amazonaws.services.dynamodbv2.local.shared.access.AmazonDynamoDBLocal;

interface DynamoService {
    AmazonDynamoDB amazonDynamoDb();

    DynamoDbClient dynamoDbClient();

    DynamoDbAsyncClient dynamoDbAsyncClient();

    DynamoDbStreamsClient dynamoDbStreamsClient();

    DynamoDbStreamsAsyncClient dynamoDbStreamsAsyncClient();

    URI getEndpoint();

    void shutdown();

    @Slf4j
    class DynamoEmbedded implements DynamoService {
        private final AmazonDynamoDBLocal embeddedDb;

        DynamoEmbedded() {
            log.info("Starting embedded dynamoDb for testing.");
            this.embeddedDb = DynamoDBEmbedded.create();
        }

        @Override
        public AmazonDynamoDB amazonDynamoDb() {
            return embeddedDb.amazonDynamoDB();
        }

        @Override
        public DynamoDbClient dynamoDbClient() {
            return embeddedDb.dynamoDbClient();
        }

        @Override
        public DynamoDbAsyncClient dynamoDbAsyncClient() {
            return embeddedDb.dynamoDbAsyncClient();
        }

        @Override
        public DynamoDbStreamsClient dynamoDbStreamsClient() {
            return embeddedDb.dynamoDbStreamsClient();
        }

        @Override
        public DynamoDbStreamsAsyncClient dynamoDbStreamsAsyncClient() {
            return embeddedDb.dynamoDbStreamsAsyncClient();
        }

        @Override
        public URI getEndpoint() {
            return null;
        }

        @Override
        public void shutdown() {
            log.info("Shutting down embedded dynamoDb.");
            embeddedDb.shutdown();
        }
    }

    @Slf4j
    class DynamoContainer implements DynamoService {
        private static final String DYNAMO_IMAGE = "amazon/dynamodb-local";
        private static final int DEFAULT_PORT = 8000;

        private final GenericContainer<?> container;
        private final URI endpoint;
        private final AwsCredentialsProvider credentials;
        private final Region region;

        DynamoContainer(int hostPort) {
            log.info("Starting dynamoDb via docker for testing.");

            this.container = createContainer(hostPort);
            this.container
                    .withCommand("-jar DynamoDBLocal.jar -sharedDb -inMemory -disableTelemetry")
                    .waitingFor(Wait.forListeningPorts(DEFAULT_PORT));

            container.start();

            // Retrieve the port the docker container is actually listening on
            int port = container.getMappedPort(DEFAULT_PORT);
            log.info("DynamoDb container listening on port={}", port);

            this.endpoint = URI.create(String.format("http://%s:%s", container.getHost(), port));
            this.credentials = StaticCredentialsProvider
                    .create(AwsBasicCredentials.create("key", "secret"));
            this.region = Region.of("localhost");
        }

        @SuppressWarnings({ "deprecation", "resource" })
        private static GenericContainer<?> createContainer(int port) {
            if (port > 0) {
                return new FixedHostPortGenericContainer<>(DYNAMO_IMAGE)
                        .withFixedExposedPort(port, DEFAULT_PORT);
            }

            return new GenericContainer<>(DYNAMO_IMAGE)
                    .withExposedPorts(DEFAULT_PORT);
        }

        @Override
        public AmazonDynamoDB amazonDynamoDb() {
            return AmazonDynamoDBClientBuilder.standard()
                    .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpoint.toString(), "localhost"))
                    .build();
        }

        @Override
        public DynamoDbClient dynamoDbClient() {
            return DynamoDbClient.builder()
                    .endpointOverride(endpoint)
                    .credentialsProvider(credentials)
                    .region(region)
                    .build();
        }

        @Override
        public DynamoDbAsyncClient dynamoDbAsyncClient() {
            return DynamoDbAsyncClient.builder()
                    .endpointOverride(endpoint)
                    .credentialsProvider(credentials)
                    .region(region)
                    .build();
        }

        @Override
        public DynamoDbStreamsClient dynamoDbStreamsClient() {
            return DynamoDbStreamsClient.builder()
                    .endpointOverride(endpoint)
                    .credentialsProvider(credentials)
                    .region(region)
                    .build();
        }

        @Override
        public DynamoDbStreamsAsyncClient dynamoDbStreamsAsyncClient() {
            return DynamoDbStreamsAsyncClient.builder()
                    .endpointOverride(endpoint)
                    .credentialsProvider(credentials)
                    .region(region)
                    .build();
        }

        @Override
        public URI getEndpoint() {
            return endpoint;
        }

        @Override
        public void shutdown() {
            log.info("Shutting down dynamoDb container.");
            container.stop();
        }
    }
}
