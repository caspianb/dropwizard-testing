package com.logicalbias.dropwizard.testing.dynamo;

import lombok.AccessLevel;
import lombok.Getter;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.streams.DynamoDbStreamsAsyncClient;
import software.amazon.awssdk.services.dynamodb.streams.DynamoDbStreamsClient;

import java.net.URI;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;

@Getter
public class DynamoManager {

    @Getter(AccessLevel.NONE)
    private final DynamoService dynamoService;

    private final AmazonDynamoDB amazonDynamoDb;
    private final DynamoDbClient client;
    private final DynamoDbEnhancedClient enhancedClient;
    private final DynamoDbAsyncClient asyncClient;
    private final DynamoDbEnhancedAsyncClient enhancedAsyncClient;
    private final DynamoDbStreamsClient streamsClient;
    private final DynamoDbStreamsAsyncClient streamsAsyncClient;
    private final DynamoTestUtils dynamoTestUtils;
    private final URI endpoint;

    static DynamoManager startEmbedded() {
        return new DynamoManager(new DynamoService.DynamoEmbedded());
    }

    static DynamoManager startContainer(int port) {
        return new DynamoManager(new DynamoService.DynamoContainer(port));
    }

    private DynamoManager(DynamoService dynamoService) {
        this.dynamoService = dynamoService;

        this.amazonDynamoDb = dynamoService.amazonDynamoDb();
        this.client = dynamoService.dynamoDbClient();
        this.enhancedClient = DynamoDbEnhancedClient.builder().dynamoDbClient(client).build();
        this.asyncClient = dynamoService.dynamoDbAsyncClient();
        this.enhancedAsyncClient = DynamoDbEnhancedAsyncClient.builder().dynamoDbClient(asyncClient).build();
        this.streamsClient = dynamoService.dynamoDbStreamsClient();
        this.streamsAsyncClient = dynamoService.dynamoDbStreamsAsyncClient();
        this.endpoint = dynamoService.getEndpoint();

        this.dynamoTestUtils = new DynamoTestUtils(client, enhancedClient);
    }

    void shutdown() {
        dynamoService.shutdown();
    }

}
