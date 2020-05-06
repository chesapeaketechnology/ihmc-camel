# Camel

A camel component for DDS using IHMC pub-sub-group.

## Usage

The DDS component requires Camel version `3.0.0` or later.

### Dependency

Add the IHMC repository:
```groovy
 maven {
    url "https://dl.bintray.com/ihmcrobotics/maven-release/"
}
```
Add the camel component dependency, where `dds_camel_version` is the latest artifact version:
```groovy
implementation("com.chesapeaketechnology:dds:${dds_camel_version}")
```

### Camel Component

**Auto-discovery with Spring Boot**: 

Annotate your camel `RouteBuilder` implementation with `@Component` and follow the route conventions

**Manual**:

If you are not using a dependency injection framework, you can add the component manually like so:
```java
context = new DefaultCamelContext();
context.addComponent(DdsCamelComponent.SCHEME, new DdsCamelComponent());
context.addRoutes(new MyRouteBuilder());
context.start();
```

###Routes

Camel routes for DDS follow the given pattern: `dds:<topic>:<domain>/<typeName>`

| Section Title | Description                                   |
| ------------- | --------------------------------------------- |
| `topic`       | The topic name of the DDS endpoint, typically is the simple name of the data type being sent to the endpoint. <br> Example: `Message` |
| `domain`      | A number denoting the DDS domain. |
| `typeName`    | The fully qualified class name of the data type being sent to the endpoint. <br> Example: `com.example.messaging.Message` |

Additional options are passed as uri parameters. Multiple parameters are split by `&`

| Additional option(s)    | Pattern                  |
| ----------------------- | ------------------------ |
| Quality of Service      | `...?qos=<configName>`   |
| Reuse message instances | `...?reuse=<true/false>` |

These routes can be generated automatically using `com.chesapeaketechnology.dds.DdsUriBuilder`. 
The only required argument for the builder is the content type, as the domain and quality have default values.
```java
DdsUriBuilder.create()
    .domain(0)                                // Numeric domain identifier
    .content(Message.class)                   // Type of content for the endpoint
    .quality(DdsQoSConfigManager.CONFIG_HIGH) // Optional quality of service parameters (CONFIG_LOW/CONFIG_HIGH)
    .toString()
```