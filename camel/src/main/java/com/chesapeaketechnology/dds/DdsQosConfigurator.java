package com.chesapeaketechnology.dds;

import us.ihmc.pubsub.attributes.PublishModeKind;
import us.ihmc.pubsub.attributes.PublisherAttributes;
import us.ihmc.pubsub.attributes.QosInterface;
import us.ihmc.pubsub.attributes.ReliabilityKind;
import us.ihmc.pubsub.attributes.SubscriberAttributes;
import us.ihmc.rtps.impl.fastRTPS.FastRTPSSubscriberAttributes;
import us.ihmc.rtps.impl.fastRTPS.LivelinessQosPolicy;
import us.ihmc.rtps.impl.fastRTPS.ReaderQos;

/**
 * Applies QoS configs.
 *
 * @author Copyright &#169; 2020 Chesapeake Technology International Corp.
 */
public class DdsQosConfigurator
{
    private final DdsQosConfig config;

    /**
     * Create the configurator.
     *
     * @param config Config to pull values from.
     */
    public DdsQosConfigurator(DdsQosConfig config)
    {
        if (config == null) throw new IllegalStateException("QoS config cannot be null!");
        this.config = config;
    }

    /**
     * Configure a subscriber's attributes with the current config.
     *
     * @param attributes subscriber's attributes to configure.
     */
    public void configureSubscriber(SubscriberAttributes attributes)
    {
        configureCommon(attributes.getQos());
        if (attributes instanceof FastRTPSSubscriberAttributes) configureFastRTPSSubscriber(attributes);
    }

    /**
     * Configure a FastRTPS subscriber's attributes with the current config.
     *
     * @param attributes subscriber's attributes to configure.
     */
    private void configureFastRTPSSubscriber(SubscriberAttributes attributes)
    {
        if (config.getLivelinessPolicyKind() != null)
        {
            LivelinessQosPolicy live = new LivelinessQosPolicy();
            live.setKind(config.getLivelinessPolicyKind());
            ReaderQos readerQos = attributes.getQos().getReaderQos();
            readerQos.setM_liveliness(live);
        }
    }

    /**
     * Configure a publisher's attributes with the current config.
     *
     * @param attributes publisher's attributes to configure.
     */
    public void configurePublisher(PublisherAttributes attributes)
    {
        configureCommon(attributes.getQos());
        if (config.getHistoryQosPolicyKind() != null)
        {
            attributes.getTopic().getHistoryQos().setKind(config.getHistoryQosPolicyKind());
        }
        attributes.getQos().setPublishMode(
                config.isAsynchronous() ? PublishModeKind.ASYNCHRONOUS_PUBLISH_MODE : PublishModeKind.SYNCHRONOUS_PUBLISH_MODE);
    }

    /**
     * Configure common qos values with the current config.
     *
     * @param qos common publisher/subscriber qos interface to configure.
     */
    private void configureCommon(QosInterface qos)
    {
        if (config.getReliability() != null) qos.setReliabilityKind(config.getReliability());
        if (config.getDurability() != null) qos.setDurabilityKind(config.getDurability());
        if (config.getOwnerShipPolicy() != null) qos.setOwnershipPolicyKind(config.getOwnerShipPolicy());
    }

    /**
     * @return Reliability of config.
     */
    public ReliabilityKind getReliability()
    {
        return config.getReliability();
    }
}
