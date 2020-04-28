package com.chesapeaketechnology.dds;

import us.ihmc.pubsub.attributes.DurabilityKind;
import us.ihmc.pubsub.attributes.OwnerShipPolicyKind;
import us.ihmc.pubsub.attributes.ReliabilityKind;

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
    private final boolean asynchronous;

    /**
     * Create a subscription config.
     *
     * @param name            Config name.
     * @param reliability     Reliablity of config.
     * @param durability      Durability of config.
     * @param ownerShipPolicy Ownership policy of config.
     */
    public DdsQosConfig(String name, ReliabilityKind reliability, DurabilityKind durability, OwnerShipPolicyKind ownerShipPolicy)
    {
        this(name, reliability, durability, ownerShipPolicy, false);
    }

    /**
     * Create a publisher config.
     *
     * @param name            Config name.
     * @param reliability     Reliability of config.
     * @param durability      Durability of config.
     * @param ownerShipPolicy Ownership policy of config.
     * @param asynchronous    {@code true} for asynchronous message sending. {@code false} for synchronous sending.
     */
    public DdsQosConfig(String name, ReliabilityKind reliability, DurabilityKind durability,
                        OwnerShipPolicyKind ownerShipPolicy, boolean asynchronous)
    {
        this.name = name;
        this.reliability = reliability;
        this.durability = durability;
        this.ownerShipPolicy = ownerShipPolicy;
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
     * @return {@code true} for asynchronous message sending. {@code false} for synchronous sending.
     */
    public boolean isAsynchronous()
    {
        return asynchronous;
    }
}
