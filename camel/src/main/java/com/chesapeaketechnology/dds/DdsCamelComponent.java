package com.chesapeaketechnology.dds;

import org.apache.camel.Endpoint;
import org.apache.camel.RuntimeCamelException;
import org.apache.camel.support.DefaultComponent;
import org.apache.camel.spi.annotations.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.ihmc.pubsub.TopicDataType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Defines a component for DDS endpoint handling.
 *
 * @author Copyright &#169; 2020 Chesapeake Technology International Corp.
 */
@Component(DdsCamelComponent.SCHEME)
public class DdsCamelComponent extends DefaultComponent
{
    public static final String SCHEME = "dds";
    private static final transient Pattern URI_REMAINING_PATTERN = Pattern.compile("(\\w+):(\\d+)/([\\w.:]+)");
    private static final Logger logger = LoggerFactory.getLogger(DdsCamelComponent.class);
    private final Map<String, DdsEndpoint> endpointMap = new ConcurrentHashMap<>();

    @Override
    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception
    {
        Matcher matcher = URI_REMAINING_PATTERN.matcher(remaining);
        if (matcher.matches())
        {
            // Automatically look up data generated type handler on classpath
            TopicDataType<?> topicDataType = null;
            String dataClassName = matcher.group(3);
            String dataClassNameSerializer = dataClassName + "PubSubType";
            try
            {
                topicDataType = (TopicDataType<?>) Class.forName(dataClassNameSerializer).getConstructors()[0].newInstance();
            } catch (ReflectiveOperationException ex)
            {
                logger.error("Failed to fetch serializer for {}, please check your classpath", dataClassName);
            }
            // Get the QoS, may be null
            String configKey = String.valueOf(parameters.get("qos"));
            DdsQosConfig config = DdsQoSConfigManager.getConfig(configKey);
            // Substring off parameters
            if (uri.contains("?"))
            {
                uri = uri.substring(0, uri.indexOf('?'));
            }
            // Create the endpoint
            DdsEndpoint endpoint = new DdsEndpoint(uri,
                    this,
                    matcher.group(1), // Topic name
                    dataClassName, // Fully qualified name of the topic's data class.
                    Integer.parseInt(matcher.group(2)), // Domain
                    topicDataType, // Type serializer
                    config // QoS
            );
            endpointMap.put(uri, endpoint);
            logger.debug("Created DDS endpoint '{}'", uri);
            return endpoint;
        }
        throw new RuntimeCamelException("Invalid DDS URI: " + uri);
    }

    @Override
    protected void validateParameters(String uri, Map<String, Object> parameters, String optionPrefix)
    {
        // Do not validate, the default implementation throws an exception when parameters are found
    }

    @Override
    protected void doStop() throws Exception
    {
        for (DdsEndpoint endpoint : endpointMap.values())
        {
            endpoint.close();
        }
    }
}
