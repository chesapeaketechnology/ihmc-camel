package com.chesapeaketechnology.dds.client;

import com.chesapeaketechnology.dds.ChatManager;
import com.example.idl.messaging.Channel;
import com.example.idl.messaging.Message;
import com.example.idl.messaging.User;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Message processor that ties into the chat manager. Client-side.
 *
 * @author Copyright &#169; 2020 Chesapeake Technology International Corp.
 */
public class ClientProcessor extends ChatManager implements Processor
{
    private static final Logger logger = LoggerFactory.getLogger(ChatManager.class);

    @Override
    public void process(Exchange exchange) throws Exception
    {
        Object body = exchange.getMessage().getBody();
        if (body instanceof Message)
        {
            Message message = (Message) body;
            logger.info(messageToString(message));
        } else if (body instanceof Channel)
        {
            Channel channel = (Channel) body;
            registerChannel(channel);
            logger.info("Channel created: " + channel.getNameAsString() + " - " + channel.getDescriptionAsString());
        } else if (body instanceof User)
        {
            User user = (User) body;
            registerUser(user);
            logger.info("User joined: " + user.getNameAsString());
        }
    }
}
