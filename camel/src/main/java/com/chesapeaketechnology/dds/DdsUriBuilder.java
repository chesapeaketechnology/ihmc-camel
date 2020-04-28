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
        return quality(DdsQoSConfigManager.getConfig(name));
    }

    /**
     * @param config Quality of service config to use.
     * @return DDS URI builder.
     */
    public DdsUriBuilder quality(DdsQosConfig config)
    {
        this.config = config;
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

    @Override
    public String toString()
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
        String uri = SCHEME + ":" + topic + ":" + domain + "/" + typeName;
        if (config != null)
        {
            uri += "?qos=" + config.getName();
        }
        return uri;
    }
}
