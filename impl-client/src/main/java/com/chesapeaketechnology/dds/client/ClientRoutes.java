package com.chesapeaketechnology.dds.client;

import com.chesapeaketechnology.dds.DdsQoSConfigManager;
import com.example.idl.messaging.Channel;
import com.example.idl.messaging.Message;
import com.example.idl.messaging.User;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import static com.chesapeaketechnology.dds.DdsUriBuilder.create;

/**
 * Camel routes for the client.
 *
 * @author Copyright &#169; 2020 Chesapeake Technology International Corp.
 */
@Component
public class ClientRoutes extends RouteBuilder
{
    public static final String CHANNEL_OUT = create().domain(0)
            .content(Channel.class).quality(DdsQoSConfigManager.CONFIG_HIGH).toString();
    public static final String USER_OUT = create().domain(0)
            .content(User.class).quality(DdsQoSConfigManager.CONFIG_HIGH).toString();
    public static final String MESSAGE_OUT = create().domain(0)
            .content(Message.class).quality(DdsQoSConfigManager.CONFIG_HIGH).reuseMessageStructures().toString();
    public static final String MESSAGE_IN = create().domain(1)
            .content(Message.class).quality(DdsQoSConfigManager.CONFIG_HIGH).reuseMessageStructures().toString();

    @Override
    public void configure() throws Exception
    {
        Processor clientProcessor = new ClientProcessor();
        from(CHANNEL_OUT).process(clientProcessor);
        from(USER_OUT).process(clientProcessor);
        from(MESSAGE_OUT).process(clientProcessor);
        from(MESSAGE_IN).process(clientProcessor);
    }
}
