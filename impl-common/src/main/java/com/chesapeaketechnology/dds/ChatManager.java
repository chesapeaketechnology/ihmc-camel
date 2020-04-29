package com.chesapeaketechnology.dds;

import com.example.idl.messaging.Channel;
import com.example.idl.messaging.Message;
import com.example.idl.messaging.User;

import java.util.HashMap;
import java.util.Map;

/**
 * Basic chat manager for an example chat application.
 *
 * @author Copyright &#169; 2020 Chesapeake Technology International Corp.
 */
public abstract class ChatManager
{
    private final Map<Integer, Channel> channelMap = new HashMap<>();
    private final Map<Integer, User> userMap = new HashMap<>();

    /**
     * @param channel Channel to register.
     */
    public void registerChannel(Channel channel) {
        channelMap.put(channel.getId(), channel);
    }

    /**
     * @param user User to register.
     */
    public void registerUser(User user) {
        userMap.put(user.getId(), user);
    }

    /**
     * Convert a message to a string representation.
     *
     * @param message Message type.
     * @return String representation of type.
     * @throws IllegalStateException When a message was sent to a channel that doesn't exist, or by a user that doesn't exist.
     */
    public String messageToString(Message message) throws IllegalStateException {
        // Validate
        if (!channelMap.containsKey(message.getChannelId()))
        {
            throw new IllegalStateException("Message was sent to channel that does not exist: " + message.getChannelId());
        } else if (!userMap.containsKey(message.getUserId()))
        {
            throw new IllegalStateException("Message was sent by user that does not exist: " + message.getUserId());
        }
        // Convert to string
        String channel = channelMap.get(message.getChannelId()).getNameAsString();
        String user = userMap.get(message.getUserId()).getNameAsString();
        String content = message.getTextAsString();
        return String.format("[%s] %s: %s", channel, user, content);
    }
}
