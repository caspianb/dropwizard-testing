# Dropwizard Integration Testing

A simple integration test framework built around [Dropwizard Testing](https://github.com/dropwizard/dropwizard/tree/release/4.0.x/dropwizard-testing).

* [Getting Started](#getting-started)
* [Initializing the Test Application](#initializing-the-test-application)
    * [@DropwizardTest](#dropwizardtest-annotation)
    * [@TestProperties](#testproperties-annotation)
* [TestClient](#testclient)
* [Mocking Dependencies](#mocking-dependencies)
* Additional Modules
    * [DynamoDb Testing](dropwizard-testing-dynamo/README.md)

# Getting Started

The basic functionality simply wraps the existing [integration testing](https://www.dropwizard.io/en/stable/manual/testing.html#junit-5) logic already provided
by the official dropwizard-testing library. The goal of this library is to make it quicker and easier to get the unit test up and running with minimal boilerplate
overhead.

> **_NOTE:_** This library currently supports Dropwizard 4.x. (though it may work with Dropwizard 3+).
> If demand exists, a new version of the library can be provided for Dropwizard1.x.
> This library is still considered "beta" (especially the kafka submodule - the spring-kafka-test dependency may be
> removed in a latter version); some APIs may be changed if necessary.

### Include in your project:

#### Maven

```yaml
<dependency>
<groupId>com.logicalbias</groupId>
<artifactId>dropwizard-testing</artifactId>
<version>0.2.6</version>
<scope>test</scope>
</dependency>
```

#### Gradle

```groovy
dependencies {
    testImplementation 'com.logicalbias:dropwizard-testing:0.2.6'
}
```

---

## Initializing the Test Application

Take this example from the dropwizard documentation linked above. This will start the dropwizard application at the start of the test and shuts it down when all tests have finished running.

```java

@ExtendWith(DropwizardExtensionsSupport.class)
class LoginAcceptanceTest {

    private static DropwizardAppExtension<TestConfiguration> EXT = new DropwizardAppExtension<>(
            MyApp.class,
            ResourceHelpers.resourceFilePath("my-app-config.yaml")
    );

    @Test
    void loginHandlerRedirectsAfterPost() {
        Client client = EXT.client();

        Response response = client.target(
                        String.format("http://localhost:%d/login", EXT.getLocalPort()))
                .request()
                .post(Entity.json(loginForm()));

        assertThat(response.getStatus()).isEqualTo(302);
    }
}
```

Using this library, the above can be accomplished in a similar way through an annotation. This library also provides the TestClient helper tool which will know how to connect
to the test application (which port to connect to).

```java

@DropwizardTest(value = MyApp.class, configFile = "my-app-config.yaml", useResourceFilePath = true)
class LoginAcceptanceTest {

    private final DropwizardAppExtension<?> dropwizardAppExtension;
    private final TestClient testClient;

    LoginAcceptanceTest(DropwizardAppExtension dropwizardAppExtension, TestClient testClient) {
        this.dropwizardAppExtension = dropwizardAppExtension;
        this.testClient = testClient;
    }

    @Test
    void loginHandlerRedirectsAfterPost() {
        testClient.post("login")
                .body(loginForm())
                .expectStatus(302)
                .andReturn();
    }
}
```

This library's jupiter extension will allow you to inject any dependency defined in the HK2 context into the test application (resource classes, services, etc).
It provides the DropwizardAppExtension instance that would have been defined in the static block above as well as the [TestClient](#testclient) instance for this test.

---

### @DropwizardTest Annotation

| Attribute             | Default Value | Description                                                                                                                                |
|-----------------------|---------------|--------------------------------------------------------------------------------------------------------------------------------------------|
| `value`               | required      | The Dropwizard Application class containing the application's entry point.                                                                 |
| `configFile`          | required      | The configPath passed to the DropwizardAppExtension.                                                                                       |
| `useResourceFilePath` | false         | Set to true if the test should use `ResourceHelpers.resourceFilePath` to detect the location of the configuration file.                    |
| `properties`          |               | List of properties to override for the life of the test. Should follow the standard properties file format: "property.name=value"          |
| `webEnvironment`      | DEFAULT       | DEFAULT will start the application on the port as configured in the configuration file. RANDOM will use a random port for each test class. |

> **_NOTE:_** All test annotations are discoverable via inheritance. This allows you to create a base test class or interface using these annotations and
> extend/implement to run standard jupiter hook points (e.g. `@Before` methods) generate required test state (e.g. auth tokens), initialize TestClient headers, create
> database tables, or whatever else the application needs prepared.

---

### @TestProperties Annotation

This annotation overrides properties in an equivalent manner to `@DropwizardTest` properties attribute. If duplicate properties are detected across multiple levels, the
annotations defined on child interfaces/classes will take priority over those defined at higher levels.

---

## TestClient

A TestClient utility is provided which wraps the DropwizardAppExtension::client. This utility is meant to make it easier to make web requests to the test application.

The TestClient provides a fluent builder pattern to make a request, assert the response, and retrieve the result.

```java
class TestClientExample {

    private final TestClient testClient;

    TestClientExample(TestClient testClient) {
        var bearerToken = createAuthToken();

        // This header will be applied to every call made by this testClient instance.
        testClient.defaultHeader("Authorization", "Bearer: " + bearerToken);
    }

    ResourceDto updateResource(String resourceId, ResourceDto body) {
        return testClient.put("resources/{resourceId}", resourceId)
                .header("customHeader", "customValue")
                .body(body)
                .expectStatus(Response.Status.OK)
                .andReturn(ResourceDto.class);
    }

}
```

## Mocking Dependencies

The enhanced extension also makes it easy to inject mocks into the test application.

Assume you have this resource in the application:

```java

@Singleton
@Path("orders")
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class OrderResource {

    private final OrderService orderService;

    @GET
    @Path("{orderId}")
    public OrderDto getOrder(@PathParam("orderId") String orderId) {
        var order = orderService.getOrder(orderId);
        return OrderMapper.map(order);
    }
}
```

You can mock out any services in the application by using the provided `@MockBean` annotation.

> **_NOTE:_** This requires usage of HK2 dependency injection. If you manually 'new' the OrderResource and register the instance with jersey this will not work.

```java

@MockBean(OrderService.class)
@DropwizardTest(value = MyApp.class, configFile = "my-app-config.yaml", useResourceFilePath = true)
@RequiredArgsConstructor
class OrderResourceTest {

    private final TestClient testClient;
    private final OrderService orderService;

    @Test
    void OrderResourceTest() {
        var testOrder = createTestOrder();

        Mockito.doReturn(testOrder)
                .when(orderService)
                .getOrder(testOrder.getOrderId());

        var orderDto = testClient.get("orders/{orderId}", testOrder.getOrderId())
                .expectStatus(200)
                .andReturn(OrderDto.class);

        // Assert against orderDto returned
    }
}
```

You can also use `@MockBean` at the field level. This is required when mocking generic types. All `@MockBean` fields will be provided via the test constructor,
but non-final fields that remain `null` will be set immediately after the test constructor is invoked by the test extension.

```java

@DropwizardTest(value = MyApp.class, configFile = "my-app-config.yaml", useResourceFilePath = true)
@RequiredArgsConstructor
class OrderResourceTest {

    /**
     * The test must set this field via the test constructor.
     */
    @MockBean
    private final GenericService<SomeTypeA> genericServiceA;

    /**
     * The test may set this field via the test constructor; if it is not set
     * after the test instance is initialized, it will be automatically populated.
     */
    @MockBean
    private GenericService<SomeTypeB> genericServiceB;

    @Test
    void OrderResourceTest() {
        Mockito.doReturn(someTypeA)
                .when(genericServiceA)
                .process();
    }
}
```

---

## Importing Dependencies

If mocking isn't flexible enough, you can also create test classes and import then into the test application. This can be useful to override classes that would otherwise
need to be commonly mocked out throughout the test code.

In the following example, assume you have an ItemClient in your service which makes a web request to some other service. Instead of mocking this out in every test, you could
create a single TestItemClient that extends/overrides the web-request calls and `@Import` this into every test.

> To avoid copy-pasting `@Import` blocks throughout your test suite, you can create an interface or base class which holds the `@DropwizardTest` annotation along
> with any common `@Import` annotations. Every test could simply implement such an interface and get the same behavior.

```java

@Import(TestItemClient.class)
@DropwizardTest(value = MyApp.class, configFile = "my-app-config.yaml", useResourceFilePath = true)
@RequiredArgsConstructor
class OrderResourceTest {

    @Test
    void someTest() {
        // Test that calls an endpoint which ultimately uses the ItemClient
    }
}

// You can optionally provide a name for this imported class
// You should always use ContractsProvided to explicitly declare which parent types/interfaces to bind against
@Service(name = "itemClient")
@ContractsProvided(ItemClient.class)
class TestItemClient extends ItemClient {
    @Override
    public Item getItem(String itemId) {
        return ItemDto.builder()
                .itemId(itemId)
                .description("test-item-" + itemId)
                .build();
    }
}
```
