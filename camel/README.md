# Camel

A camel component for DDS using IHMC pub-sub-group.

## Usage

### Camel Component
**Auto-discovery**:
```java
// TODO: Fix component not being auto-discoverable then write usage here 
```

**Manual**:
```java
context = new DefaultCamelContext();
context.addComponent(DdsCamelComponent.SCHEME, new DdsCamelComponent());
context.addRoutes(new MyRouteBuilder());
context.start();
```

###Routes
Camel route URI's can be generated automatically using `DdsUriBuilder`. 
The only required argument is the content type, as the domain and quality have default behaviors.
```java
DdsUriBuilder.create()
    .domain(0)                                // Numeric domain identifier
    .content(Channel.class)                   // Type of content for the endpoint
    .quality(DdsQoSConfigManager.CONFIG_HIGH) // Optional quality of service parameters (CONFIG_LOW/CONFIG_HIGH)
    .toString()
```