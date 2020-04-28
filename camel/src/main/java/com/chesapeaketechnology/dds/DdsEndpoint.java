package com.chesapeaketechnology.dds;

import org.apache.camel.Consumer;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.support.DefaultEndpoint;
import us.ihmc.pubsub.Domain;
import us.ihmc.pubsub.DomainFactory;
import us.ihmc.pubsub.TopicDataType;
import us.ihmc.pubsub.attributes.ParticipantAttributes;
import us.ihmc.pubsub.attributes.PublishModeKind;
import us.ihmc.pubsub.attributes.PublisherAttributes;
import us.ihmc.pubsub.attributes.ReliabilityKind;
import us.ihmc.pubsub.attributes.SubscriberAttributes;
import us.ihmc.pubsub.participant.Participant;
import us.ihmc.pubsub.publisher.Publisher;
import us.ihmc.pubsub.subscriber.Subscriber;

import java.io.IOException;

/**
 * Send and receive messages to/from DDS using topics and quality-of-service filtering.
 *
 * @author Copyright &#169; 2020 Chesapeake Technology International Corp.
 */
@SuppressWarnings("rawtypes")
@UriEndpoint(scheme = DdsCamelComponent.SCHEME, syntax = "dds:topic:domain/content_class_name", title = "DDS-IHMC", label = "DDS")
public class DdsEndpoint extends DefaultEndpoint
{
    private static final Domain DOMAIN = DomainFactory.getDomain(DomainFactory.PubSubImplementation.FAST_RTPS);
    private final String topicName;
    private final String messageType;
    private final int domainId;
    private final TopicDataType topicDataType;
    private final DdsQosConfig config;
    private Publisher publisher;
    private Subscriber subscriber;

    /**
     * Create a DDS endpoint.
     *
     * @param uri           DDS uri.
     * @param component     Component this endpoint was created by.
     * @param topicName     Topic name.
     * @param messageType   Fully {@link Class#getName() qualified name} of the data type of this endpoint.
     * @param domainId      Domain identifier.
     * @param topicDataType Serializer/deserializer for the data type of this endpoint.
     * @param config        Quality of service configuration. May be {@code null} for defaults.
     */
    public DdsEndpoint(String uri, DdsCamelComponent component, String topicName, String messageType,
                       int domainId, TopicDataType topicDataType, DdsQosConfig config)
    {
        super(uri, component);
        this.topicName = topicName;
        this.messageType = messageType;
        this.domainId = domainId;
        this.topicDataType = topicDataType;
        this.config = config;
    }

    @Override
    public Producer createProducer() throws Exception
    {
        return new DdsProducer(this);
    }

    @Override
    public Consumer createConsumer(Processor processor) throws Exception
    {
        return new DdsConsumer(this, processor);
    }

    /**
     * Get <i>(or create if necessary)</i> the DDS publisher, used for sending data.
     *
     * @return DDS publisher.
     * @throws IOException When the participant cannot be created.
     */
    public Publisher getPublisher() throws IOException
    {
        if (publisher == null)
        {
            ParticipantAttributes attributes = DOMAIN.createParticipantAttributes(domainId, topicName);
            Participant participant = DOMAIN.createParticipant(attributes);
            PublisherAttributes publisherAttributes =
                    DOMAIN.createPublisherAttributes(participant, topicDataType, topicName, ReliabilityKind.RELIABLE);
            if (config != null)
            {
                publisherAttributes.getQos().setPublishMode(
                        config.isAsynchronous() ? PublishModeKind.ASYNCHRONOUS_PUBLISH_MODE : PublishModeKind.SYNCHRONOUS_PUBLISH_MODE);
                publisherAttributes.getQos().setReliabilityKind(config.getReliability());
                publisherAttributes.getQos().setDurabilityKind(config.getDurability());
                publisherAttributes.getQos().setOwnershipPolicyKind(config.getOwnerShipPolicy());
            }
            publisher = DOMAIN.createPublisher(participant, publisherAttributes);
        }
        return publisher;
    }

    /**
     * Get <i>(or create if necessary)</i> the DDS subsciber, used for listening for incoming data.
     *
     * @param consumer Consumer to use as a subscriber listener.
     * @return DDS subscriber.
     * @throws IOException When the participant cannot be created.
     */
    public Subscriber getSubscriber(DdsConsumer consumer) throws IOException
    {
        if (subscriber == null)
        {
            ParticipantAttributes attributes = DOMAIN.createParticipantAttributes(domainId, topicName);
            Participant participant = DOMAIN.createParticipant(attributes);
            SubscriberAttributes subscriberAttributes =
                    DOMAIN.createSubscriberAttributes(participant, topicDataType, topicName, ReliabilityKind.RELIABLE);
            if (config != null)
            {
                subscriberAttributes.getQos().setReliabilityKind(config.getReliability());
                subscriberAttributes.getQos().setDurabilityKind(config.getDurability());
                subscriberAttributes.getQos().setOwnershipPolicyKind(config.getOwnerShipPolicy());
            }
            subscriber = DOMAIN.createSubscriber(participant, subscriberAttributes, consumer);
        }
        return subscriber;
    }

    @Override
    protected void doStop() throws Exception
    {
        super.doStop();
        // Removes all participants
        DOMAIN.stopAll();
    }

    /**
     * Convert some object into a Camel {@link Exchange} by wrapping it, then setting the body contents.
     *
     * @param object Some object to convert.
     * @return Exchange of object.
     */
    public Exchange toExchange(Object object)
    {
        Exchange exchange = createExchange(ExchangePattern.InOnly);
        exchange.setIn(new DdsMessage(exchange, object));
        return exchange;
    }
}
