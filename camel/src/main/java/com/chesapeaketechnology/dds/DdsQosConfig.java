package com.chesapeaketechnology.dds;

import us.ihmc.pubsub.attributes.DurabilityKind;
import us.ihmc.pubsub.attributes.HistoryQosPolicy;
import us.ihmc.pubsub.attributes.OwnerShipPolicyKind;
import us.ihmc.pubsub.attributes.ReliabilityKind;
import us.ihmc.rtps.impl.fastRTPS.LivelinessQosPolicyKind;

/**
 * Quality of service wrapper type. Contains configurable options for both DDS publishing and subscribing.
 *
 * @author Copyright &#169; 2020 Chesapeake Technology International Corp.
 */
public class DdsQosConfig
{
    private final String name;
    private final ReliabilityKind reliability;
    private final DurabilityKind durability;
    private final OwnerShipPolicyKind ownerShipPolicy;
    private final HistoryQosPolicy.HistoryQosPolicyKind historyQosPolicyKind;
    private final LivelinessQosPolicyKind livelinessPolicyKind;

    private final boolean asynchronous;

    /**
     * Create a subscription config. Specifying {@code null} for a value will result in the defaults being used.
     *
     * @param name                 Config name.
     * @param reliability          Reliablity of config.
     * @param durability           Durability of config.
     * @param ownerShipPolicy      Ownership policy of config.
     * @param livelinessPolicyKind Liveliness policy of config.
     * @param historyQosPolicyKind History policy of config.
     */
    public DdsQosConfig(String name, ReliabilityKind reliability, DurabilityKind durability, OwnerShipPolicyKind ownerShipPolicy,
                        HistoryQosPolicy.HistoryQosPolicyKind historyQosPolicyKind, LivelinessQosPolicyKind livelinessPolicyKind)
    {
        this(name, reliability, durability, ownerShipPolicy, historyQosPolicyKind, livelinessPolicyKind, false);
    }

    /**
     * Create a publisher config. Specifying {@code null} for a value will result in the defaults being used.
     *
     * @param name                 Config name.
     * @param reliability          Reliability of config.
     * @param durability           Durability of config.
     * @param ownerShipPolicy      Ownership policy of config.
     * @param historyQosPolicyKind History policy of config.
     * @param livelinessPolicyKind liveliness policy of config.
     * @param asynchronous         {@code true} for asynchronous message sending. {@code false} for synchronous sending.
     */
    public DdsQosConfig(String name, ReliabilityKind reliability, DurabilityKind durability,
                        OwnerShipPolicyKind ownerShipPolicy, HistoryQosPolicy.HistoryQosPolicyKind historyQosPolicyKind,
                        LivelinessQosPolicyKind livelinessPolicyKind, boolean asynchronous)
    {
        this.name = name;
        this.reliability = reliability;
        this.durability = durability;
        this.ownerShipPolicy = ownerShipPolicy;
        this.historyQosPolicyKind = historyQosPolicyKind;
        this.livelinessPolicyKind = livelinessPolicyKind;
        this.asynchronous = asynchronous;
    }

    /**
     * @return Config name.
     */
    public String getName()
    {
        return name;
    }

    /**
     * @return Reliability of config.
     */
    public ReliabilityKind getReliability()
    {
        return reliability;
    }

    /**
     * @return Durability of config.
     */
    public DurabilityKind getDurability()
    {
        return durability;
    }

    /**
     * @return Ownership policy of config.
     */
    public OwnerShipPolicyKind getOwnerShipPolicy()
    {
        return ownerShipPolicy;
    }

    /**
     * @return History policy of config.
     */
    public HistoryQosPolicy.HistoryQosPolicyKind getHistoryQosPolicyKind()
    {
        return historyQosPolicyKind;
    }

    /**
     * @return Liveliness policy of config.
     */
    public LivelinessQosPolicyKind getLivelinessPolicyKind()
    {
        return livelinessPolicyKind;
    }

    /**
     * @return {@code true} for asynchronous message sending. {@code false} for synchronous sending.
     */
    public boolean isAsynchronous()
    {
        return asynchronous;
    }
}
