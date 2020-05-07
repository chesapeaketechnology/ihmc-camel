package com.chesapeaketechnology.dds;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.ihmc.pubsub.attributes.DurabilityKind;
import us.ihmc.pubsub.attributes.HistoryQosPolicy;
import us.ihmc.pubsub.attributes.OwnerShipPolicyKind;
import us.ihmc.pubsub.attributes.ReliabilityKind;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Manager of quality-of-service configurations. Comes with two default configurations:
 * <ul>
 *     <li>{@link #CONFIG_LOW} - For low priority exchanges</li>
 *     <li>{@link #CONFIG_HIGH} - For higher priority exchanges</li>
 * </ul>
 *
 * @author Copyright &#169; 2020 Chesapeake Technology International Corp.
 */
public class DdsQoSConfigManager
{
    public static final String CONFIG_LOW = "low";
    public static final String CONFIG_HIGH = "high";
    private static final Map<String, DdsQosConfig> schemeConfigurations = new HashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(DdsQoSConfigManager.class);

    /**
     * @param config Config to register.
     */
    public static void register(DdsQosConfig config)
    {
        logger.trace("Register QoS config: {}", config.getName());
        schemeConfigurations.put(config.getName(), config);
    }

    /**
     * @param name Name of config.
     * @return Saved config, or {@code null} if no matching config exists.
     */
    public static DdsQosConfig getConfig(String name)
    {
        if ("null".equals(name) || name == null)
        {
            return null;
        }
        return schemeConfigurations.get(name);
    }

    /**
     * @return Collection of all stored configs.
     */
    public static Collection<DdsQosConfig> configs()
    {
        return schemeConfigurations.values();
    }

    static
    {
        // Setup low priority config
        register(new DdsQosConfig(
                CONFIG_LOW,
                ReliabilityKind.BEST_EFFORT,
                DurabilityKind.VOLATILE_DURABILITY_QOS,
                OwnerShipPolicyKind.SHARED_OWNERSHIP_QOS,
                HistoryQosPolicy.HistoryQosPolicyKind.KEEP_LAST_HISTORY_QOS,
                null // LivelinessQosPolicyKind
        ));
        // Setup high priority config
        register(new DdsQosConfig(
                CONFIG_HIGH,
                ReliabilityKind.RELIABLE,
                DurabilityKind.TRANSIENT_LOCAL_DURABILITY_QOS,
                OwnerShipPolicyKind.EXCLUSIVE_OWNERSHIP_QOS,
                HistoryQosPolicy.HistoryQosPolicyKind.KEEP_ALL_HISTORY_QOS,
                null // LivelinessQosPolicyKind
        ));
    }
}
