package com.logicalbias.dropwizard.testing.dynamo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.mapper.BeanTableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.CreateTableEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;

import java.util.function.Consumer;
import java.util.function.Function;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

@Slf4j
@RequiredArgsConstructor
public class DynamoTestUtils {

    private final DynamoDbClient client;
    private final DynamoDbEnhancedClient enhancedClient;

    /**
     * Creates a new table for the given dynamoDbBean type.
     */
    public void createTable(Class<?> itemType) {
        var tableName = getTableName(itemType);
        createTable(tableName, itemType);
    }

    /**
     * Creates a new table for the given dynamoDbBean type.
     */
    public void createTable(Class<?> itemType, Consumer<CreateTableEnhancedRequest.Builder> requestConsumer) {
        var tableName = getTableName(itemType);
        createTable(tableName, itemType, requestConsumer);
    }

    /**
     * Creates a new table for the given dynamoDbBean type with the specified name.
     */
    public void createTable(String tableName, Class<?> itemType) {
        createTable(tableName, itemType, Function.identity()::apply);
    }

    /**
     * Creates a new table for the given dynamoDbBean type with the specified name.
     */
    public void createTable(String tableName, Class<?> itemType, Consumer<CreateTableEnhancedRequest.Builder> requestConsumer) {
        if (tableExists(tableName)) {
            log.info("Table '{}' already exists.", tableName);
            return;
        }

        log.info("Creating table={} for item={}...", tableName, itemType.getName());
        getTable(tableName, itemType).createTable(requestConsumer);
        try (var dbWaiter = client.waiter()) {
            dbWaiter.waitUntilTableExists(DescribeTableRequest.builder()
                    .tableName(tableName)
                    .build());
        }

        log.info(" - Table '{}' created successfully.", tableName);
    }

    public void deleteTable(Class<?> itemType) {
        var tableName = getTableName(itemType);
        deleteTable(tableName, itemType);
    }

    public void deleteTable(String tableName, Class<?> itemType) {
        if (!tableExists(tableName)) {
            return;
        }

        log.info("Deleting table={} for item={}...", tableName, itemType.getName());
        getTable(tableName, itemType).deleteTable();
        try (var dbWaiter = client.waiter()) {
            dbWaiter.waitUntilTableNotExists(DescribeTableRequest.builder().tableName(tableName).build());
        }
    }

    public void recreateTable(Class<?> itemType) {
        recreateTable(getTableName(itemType), itemType);
    }

    public void recreateTable(String tableName, Class<?> itemType) {
        deleteTable(tableName, itemType);
        createTable(tableName, itemType);
    }

    public <T> void clearTable(Class<T> itemType) {
        var tableName = getTableName(itemType);
        clearTable(tableName, itemType);
    }

    public <T> void clearTable(String tableName, Class<T> itemType) {
        if (!tableExists(tableName)) {
            return;
        }

        log.info("Deleting all records in {} for item={}", tableName, itemType);
        var table = getTable(tableName, itemType);
        table.scan().forEach(page -> page.items().forEach(table::deleteItem));
    }

    public <T> DynamoDbTable<T> getTable(Class<T> itemType) {
        var tableName = getTableName(itemType);
        return getTable(tableName, itemType);
    }

    public <T> DynamoDbTable<T> getTable(String tableName, Class<T> itemType) {
        var tableSchema = BeanTableSchema.create(itemType);
        return enhancedClient.table(tableName, tableSchema);
    }

    private String getTableName(Class<?> classType) {
        var annotation = classType.getAnnotation(DynamoDBTable.class);
        if (annotation == null) {
            throw new IllegalStateException(classType + " must have @DynamoDBTable annotation to determine table name.");
        }

        return annotation.tableName();
    }

    private boolean tableExists(String tableName) {
        var existingTables = client.listTables().tableNames();
        return existingTables.contains(tableName);
    }

}
