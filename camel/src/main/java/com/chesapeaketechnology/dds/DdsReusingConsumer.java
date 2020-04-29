package com.chesapeaketechnology.dds;

import com.chesapeaketechnology.dds.util.ReflectionUtil;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.ihmc.pubsub.common.SampleInfo;
import us.ihmc.pubsub.subscriber.Subscriber;

/**
 * Listens to incoming messages from a DDS endpoint's subscriber and passes them along to Camel. Received message instances are reused.
 * This is useful for saving memory when the message type is not a dependency of other message types,
 * or if the message type will always have the same value.
 *
 * @author Copyright &#169; 2020 Chesapeake Technology International Corp.
 */
public class DdsReusingConsumer extends DdsConsumer
{
    private static final Logger logger = LoggerFactory.getLogger(DdsConsumer.class);
    private final SampleInfo sampleInfo = new SampleInfo();
    private final Object data;

    /**
     * Create the camel consumer.
     *
     * @param endpoint  Endpoint this consumer belongs to.
     * @param processor Processor to send received data to.
     */
    public DdsReusingConsumer(DdsEndpoint endpoint, Processor processor)
    {
        super(endpoint, processor);
        this.data = ReflectionUtil.createIdlType(endpoint.getMessageType());
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onNewDataMessage(Subscriber subscriber)
    {
        logger.debug("Receive new data message for endpoint '{}': Guid={}", endpoint.getEndpointUri(), subscriber.getGuid());
        if (subscriber.takeNextData(data, sampleInfo))
        {
            onReceive(data);
        }
    }
}
