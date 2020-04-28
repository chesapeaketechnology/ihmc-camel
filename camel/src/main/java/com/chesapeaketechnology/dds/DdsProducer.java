package com.chesapeaketechnology.dds;

import org.apache.camel.Exchange;
import org.apache.camel.support.DefaultProducer;
import us.ihmc.pubsub.publisher.Publisher;

/**
 * Directs Camel exchanges into a DDS endpoint's publisher.
 *
 * @author Copyright &#169; 2020 Chesapeake Technology International Corp.
 */
public class DdsProducer extends DefaultProducer
{
    private final DdsEndpoint endpoint;
    private Publisher publisher;

    /**
     * Create the camel producer.
     *
     * @param endpoint Endpoint this producer belongs to.
     */
    public DdsProducer(DdsEndpoint endpoint)
    {
        super(endpoint);
        this.endpoint = endpoint;
    }

    @Override
    public void process(Exchange exchange) throws Exception
    {
        // Send message content to publisher
        publisher.write(exchange.getIn().getBody());
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
        // Get dds publisher for endpoint
        publisher = endpoint.getPublisher();
    }
}
