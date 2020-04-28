package com.chesapeaketechnology.dds;

import org.apache.camel.Exchange;
import org.apache.camel.support.DefaultMessage;

/**
 * Camel message for DDS content.
 *
 * @author Copyright &#169; 2020 Chesapeake Technology International Corp.
 */
public class DdsMessage extends DefaultMessage
{
    /**
     * Create the message.
     *
     * @param exchange Base exchange value.
     * @param value    Body of message.
     */
    public DdsMessage(Exchange exchange, Object value)
    {
        super(exchange);
        setBody(value);
    }
}
