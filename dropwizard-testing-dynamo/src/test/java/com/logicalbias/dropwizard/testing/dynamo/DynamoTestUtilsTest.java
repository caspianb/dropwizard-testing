package com.logicalbias.dropwizard.testing.dynamo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.extensions.annotations.DynamoDbVersionAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;

import org.junit.jupiter.api.Test;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DynamoDbTest
@RequiredArgsConstructor
public class DynamoTestUtilsTest {

    private final DynamoTestUtils dynamoTestUtils;

    @Test
    void testTableIndexesCreated() {
        var dynamoTable = dynamoTestUtils.createTable(TestItem.class);
        var description = dynamoTable.describeTable();
        var tableDescription = description.table();

        assertTrue(tableDescription.hasKeySchema());
        assertTrue(tableDescription.hasGlobalSecondaryIndexes());
        assertEquals(1, tableDescription.globalSecondaryIndexes().size());
        assertEquals("idx_external_id", tableDescription.globalSecondaryIndexes().get(0).indexName());
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @DynamoDbBean
    @DynamoDBTable(tableName = "test")
    public static class TestItem {
        @Getter(onMethod_ = @DynamoDbPartitionKey)
        private String id;

        @Getter(onMethod_ = @DynamoDbSecondaryPartitionKey(indexNames = "idx_external_id"))
        private String externalId;

        private String description;

        @Getter(onMethod_ = @DynamoDbVersionAttribute)
        private Long version;
    }
}
