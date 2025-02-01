# Dropwizard DynamoDb Testing

# Getting Started

### Include in your project:

#### Maven

```yaml
<dependency>
  <groupId>com.logicalbias</groupId>
  <artifactId>dropwizard-testing-dynamo</artifactId>
  <version>0.2.7</version>
  <scope>test</scope>
</dependency>
```

#### Gradle

```groovy
dependencies {
    testImplementation 'com.logicalbias:dropwizard-testing-dynamo:0.2.7'
}
```

## Initializing the DynamoDb Test Service

Using this library is easy. Simply add `@DynamoDbTest` annotation to your class. This will (by default) spin up an in-memory dynamo database.
Alternatively, the annotation environment property is provided `@DynamoDbTest(environment = Environment.DOCKER)` allowing control to spin up a full
docker container (this requires the [Test Containers](https://testcontainers.com/) dependency to be added to your project.

```java
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import org.junit.jupiter.api.BeforeEach;

import com.logicalbias.dropwizard.testing.dynamo.DynamoManager;
import com.logicalbias.dropwizard.testing.dynamo.DynamoTestUtils;

@DynamoDbTest(environment = DynamoDbTest.Environment.DOCKER)
@DropwizardTest(value = MyApp.class, configFile = "my-app-config.yaml", useResourceFilePath = true)
class EmbeddedDynamoTest {

    /**
     * All standard awssdk dynamo clients are available. 
     */
    private final DynamoDbClient dynamoDbClient;

    /**
     * This library provides the DynamoTestUtils class to help with some common operations
     */
    private final DynamoTestUtils dynamoTestUtils;

    EmbeddedDynamoTest(
            DynamoDbClient dynamoDbClient,
            DynamoTestUtils dynamoTestUtils) {
        this.dynamoDbClient = dynamoDbClient;
        this.dynamoTestUtils = dynamoTestUtils;
    }

    @BeforeEach
    void beforeEach() {
        dynamoTestUtils.createTable("test", TestEntity.class);
        dynamoTestUtils.clearTable("test", TestEntity.class);
    }

    @Test
    void testApplication() {
        // Test application as normal via direct service injection or TestClient calls
    }
}
```

### @DynamoDbTest Annotation

| Attribute         | Default Value | Description                                                                                                                       |
|-------------------|---------------|-----------------------------------------------------------------------------------------------------------------------------------|
| `environment`     | EMBEDDED      | Which environment to run the dynamo databnase service under.                                                                      |                                                            
| `registerClients` | true          | Determines if the dynamoDb clients should be added to the `@DropwizardTestApplication` HK2 context (if detected).                 |
| `properties`      |               | List of properties to override for the life of the test. Should follow the standard properties file format: "property.name=value" |
| `port`            | 0             | The port exposed when running in docker; 0 indicates a random port.                                                           |

Note that registerClients gives control over whether the test extension will override DynamoDB clients configured in the HK2 context. If this behavior is disabled, the
`properties` property (or the `port` property) must also be configured to ensure the test application knows howto connect to the docker dynamo database.

> **_NOTE:_** It is not possible to run dynamoDb in EMBEDDED mode and allow the test application to create its own clients; the embedded database is not exposed on any ports.
