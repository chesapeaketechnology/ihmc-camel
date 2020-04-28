package com.chesapeaketechnology.dds.server;

import com.chesapeaketechnology.dds.DdsQoSConfigManager;
import com.example.idl.messaging.Channel;
import com.example.idl.messaging.Message;
import com.example.idl.messaging.User;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import static com.chesapeaketechnology.dds.DdsUriBuilder.create;

/**
 * Camel routes for the server.
 *
 * @author Copyright &#169; 2020 Chesapeake Technology International Corp.
 */
@Component
public class ServerRoutes extends RouteBuilder
{
    public static final String CHANNEL_IN = create().domain(0).content(Channel.class).quality(DdsQoSConfigManager.CONFIG_HIGH).toString();
    public static final String USER_IN = create().domain(0).content(User.class).quality(DdsQoSConfigManager.CONFIG_HIGH).toString();
    public static final String MESSAGE_IN = create().domain(0).content(Message.class).quality(DdsQoSConfigManager.CONFIG_HIGH).toString();
    public static final String MESSAGE_OUT = create().domain(1).content(Message.class).quality(DdsQoSConfigManager.CONFIG_HIGH).toString();

    @Override
    public void configure() throws Exception
    {
        Processor serverProcessor = new ServerProcessor();
        from(CHANNEL_IN).process(serverProcessor);
        from(USER_IN).process(serverProcessor);
        from(MESSAGE_IN).process(serverProcessor);
        from(MESSAGE_OUT).process(serverProcessor);
    }
}
