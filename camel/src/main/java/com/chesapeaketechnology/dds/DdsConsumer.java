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
    private static final Logger logger = LoggerFactory.getLogger(DdsProducer.class);
    private final DdsEndpoint endpoint;

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

    private void onReceive(Object object)
    {
        Exchange exchange = endpoint.toExchange(object);
        try
        {
            getProcessor().process(exchange);
            if (exchange.isFailed() && exchange.getException() != null)
            {
                throw new RuntimeCamelException(exchange.getException());
            }
        } catch (Exception ex)
        {
            logger.error("Error in processing DDSI message", ex);
        }
    }

    @Override
    public void onNewDataMessage(Subscriber subscriber)
    {
        // Handle received data
        onReceive(subscriber.takeNextData());
    }

    @Override
    public void onSubscriptionMatched(Subscriber subscriber, MatchingInfo info)
    {
        // Do nothing
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
