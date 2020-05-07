package com.chesapeaketechnology.dds;

import org.apache.camel.Consumer;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.support.DefaultEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.ihmc.pubsub.Domain;
import us.ihmc.pubsub.DomainFactory;
import us.ihmc.pubsub.TopicDataType;
import us.ihmc.pubsub.attributes.ParticipantAttributes;
import us.ihmc.pubsub.attributes.PublisherAttributes;
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
    private static final Logger logger = LoggerFactory.getLogger(DdsConsumer.class);
    private final String topicName;
    private final String messageType;
    private final int domainId;
    private final TopicDataType topicDataType;
    private final DdsQosConfigurator configurator;

    private final boolean reuse;
    private Participant participant;
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
     * @param reuse         {@code true} when message structures will be reused.
     */
    public DdsEndpoint(String uri, DdsCamelComponent component, String topicName, String messageType,
                       int domainId, TopicDataType topicDataType, DdsQosConfig config, boolean reuse)
    {
        super(uri, component);
        this.topicName = topicName;
        this.messageType = messageType;
        this.domainId = domainId;
        this.topicDataType = topicDataType;
        this.reuse = reuse;
        configurator = new DdsQosConfigurator(config);
    }

    @Override
    public Producer createProducer() throws Exception
    {
        logger.trace("Create new producer for endpoint: {}", getEndpointUri());
        return new DdsProducer(this);
    }

    @Override
    public Consumer createConsumer(Processor processor) throws Exception
    {
        logger.trace("Create new consumer for endpoint: {}", getEndpointUri());
        // Determine consumer impl based on if we want to re-use message structures.
        if (reuse)
        {
            return new DdsReusingConsumer(this, processor);
        } else
        {
            return new DdsConsumer(this, processor);
        }
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
            Participant participant = getParticipant();
            PublisherAttributes publisherAttributes =
                    DOMAIN.createPublisherAttributes(participant, topicDataType, topicName, configurator.getReliability());
            configurator.configurePublisher(publisherAttributes);
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
            Participant participant = getParticipant();
            SubscriberAttributes subscriberAttributes =
                    DOMAIN.createSubscriberAttributes(participant, topicDataType, topicName, configurator.getReliability());
            configurator.configureSubscriber(subscriberAttributes);
            subscriber = DOMAIN.createSubscriber(participant, subscriberAttributes, consumer);
        }
        return subscriber;
    }

    /**
     * @return Participant for this endpoint.
     * @throws IOException When the participant cannot be created.
     */
    private Participant getParticipant() throws IOException
    {
        if (participant == null)
        {
            logger.trace("Creating participant for endpoint: {}", getEndpointUri());
            ParticipantAttributes attributes = DOMAIN.createParticipantAttributes(domainId, topicName);
            participant = DOMAIN.createParticipant(attributes);
            logger.trace(" - Participant created: {}", participant.getGuid());
        }
        return participant;
    }

    @Override
    protected void doStop() throws Exception
    {
        super.doStop();
        // Removes our participant
        logger.trace("Stopping endpoint({}). Removing participant({}) from domain.", getEndpointUri(), getParticipant().getGuid());
        DOMAIN.removeParticipant(getParticipant());
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

    /**
     * @return Fully {@link Class#getName() qualified name} of the data type of this endpoint.
     */
    public String getMessageType()
    {
        return messageType;
    }
}
