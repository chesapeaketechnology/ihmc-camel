package com.chesapeaketechnology.dds;

/**
 * A utility for generating DDS uri's.
 *
 * @author Copyright &#169; 2020 Chesapeake Technology International Corp.
 */
public class DdsUriBuilder
{
    public static final String SCHEME = DdsCamelComponent.SCHEME;
    private DdsQosConfig config;
    private Class<?> type;
    private String domain = "0";
    private boolean reuse;

    // Deny constructor access
    private DdsUriBuilder()
    {
    }

    /**
     * @return A new DDS URI builder.
     */
    public static DdsUriBuilder create()
    {
        return new DdsUriBuilder();
    }

    /**
     * @param name Quality of service config, by name to use.
     * @return DDS URI builder.
     */
    public DdsUriBuilder quality(String name)
    {
        DdsQosConfig config = DdsQoSConfigManager.getConfig(name);
        // Validate the config was loaded
        if (config == null)
        {
            throw new IllegalStateException("No configuration with the name '" + name +
                    "' exists in the manager!");
        }
        return quality(config);
    }

    /**
     * @param config Quality of service config to use.
     * @return DDS URI builder.
     */
    public DdsUriBuilder quality(DdsQosConfig config)
    {
        this.config = config;
        // Register the config if it has not been registered.
        // This will allow the camel component to look up the config properly.
        if (DdsQoSConfigManager.getConfig(config.getName()) == null)
        {
            DdsQoSConfigManager.register(config);
        }
        return this;
    }

    /**
     * @param domain Domain to use.
     * @return DDS URI builder.
     */
    public DdsUriBuilder domain(int domain)
    {
        return domain(String.valueOf(domain));
    }

    /**
     * @param domain Domain to use.
     * @return DDS URI builder.
     */
    public DdsUriBuilder domain(String domain)
    {
        this.domain = domain;
        return this;
    }

    /**
     * @param type Type of content to be sent through the endpoint.
     * @return DDS URI builder.
     */
    public DdsUriBuilder content(Class<?> type)
    {
        this.type = type;
        return this;
    }

    /**
     * Activate message structure reuse.
     * This is useful for saving memory when the message type is not a dependency of other message types,
     * or if the message type will always have the same value.
     *
     * @return DDS URI builder.
     */
    public DdsUriBuilder reuseMessageStructures()
    {
        this.reuse = true;
        return this;
    }

    /**
     * @return Generated URI baseline. Does not contain parameters even if a {@link DdsQosConfig} is specified.
     */
    public String getBaseUri()
    {
        // Validate
        if (domain == null)
        {
            throw new IllegalStateException("Domain cannot be null!");
        }
        if (type == null)
        {
            throw new IllegalStateException("Content type cannot be null!");
        }
        // Create URI
        String topic = type.getSimpleName();
        String typeName = type.getName();
        return SCHEME + ":" + topic + ":" + domain + "/" + typeName;
    }

    /**
     * @return Full generated URI. Will contain parameters if {@link #reuseMessageStructures()} has been called
     * or if a {@link DdsQosConfig} is specified.
     */
    public String getUri()
    {
        String uri = getBaseUri();
        String next = "?";
        if (config != null)
        {
            uri += next + "qos=" + config.getName();
            next = "&";
        }
        if (reuse)
        {
            uri += next + "reuse=true";
        }
        return uri;
    }

    @Override
    public String toString()
    {
        return getUri();
    }
}
