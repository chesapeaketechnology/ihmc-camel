package com.chesapeaketechnology.dds;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.RuntimeCamelException;
import org.apache.camel.support.DefaultConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.ihmc.pubsub.common.MatchingInfo;
import us.ihmc.pubsub.subscriber.Subscriber;
import us.ihmc.pubsub.subscriber.SubscriberListener;

/**
 * Listens to incoming messages from a DDS endpoint's subscriber and passes them along to Camel.
 *
 * @author Copyright &#169; 2020 Chesapeake Technology International Corp.
 */
@SuppressWarnings("rawtypes")
public class DdsConsumer extends DefaultConsumer implements SubscriberListener
{
    private static final Logger logger = LoggerFactory.getLogger(DdsConsumer.class);
    protected final DdsEndpoint endpoint;

    /**
     * Create the camel consumer.
     *
     * @param endpoint  Endpoint this consumer belongs to.
     * @param processor Processor to send received data to.
     */
    public DdsConsumer(DdsEndpoint endpoint, Processor processor)
    {
        super(endpoint, processor);
        this.endpoint = endpoint;
    }

    /**
     * Handle received idl type.
     *
     * @param data Received object.
     */
    protected void onReceive(Object data)
    {
        Exchange exchange = endpoint.toExchange(data);
        try
        {
            logger.trace("Processing for endpoint: '{}' - Value: {}",
                    endpoint.getEndpointUri(), data.toString());
            getProcessor().process(exchange);
            if (exchange.isFailed() && exchange.getException() != null)
            {
                throw new RuntimeCamelException(exchange.getException());
            }
        } catch (Exception ex)
        {
            logger.error("Error in processing DDS message", ex);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onNewDataMessage(Subscriber subscriber)
    {
        logger.debug("Receive new data message for endpoint '{}': Guid={}",
                endpoint.getEndpointUri(), subscriber.getGuid());
        onReceive(subscriber.takeNextData());
    }

    @Override
    public void onSubscriptionMatched(Subscriber subscriber, MatchingInfo info)
    {
        logger.debug("Subscription matched for endpoint '{}': Guid={} - Status={}",
                endpoint.getEndpointUri(), subscriber.getGuid(), info.getStatus().name());
    }

    @Override
    protected void doStart() throws Exception
    {
        super.doStart();
        // Ensure endpoint is active
        if (!endpoint.isStarted())
        {
            endpoint.start();
        }
        // Register this consumer as a subscriber listener
        endpoint.getSubscriber(this);
    }
}
