package com.chesapeaketechnology.dds.client;

import com.chesapeaketechnology.dds.DdsCamelComponent;
import com.example.idl.messaging.Channel;
import com.example.idl.messaging.Message;
import com.example.idl.messaging.User;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultCamelContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Sample chat application, client-side boot application.
 *
 * @author Copyright &#169; 2020 Chesapeake Technology International Corp.
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.chesapeaketechnology.dds.client")
public class ClientApp implements CommandLineRunner
{
    private static final String[] names = { "Alex", "Bob", "Carson", "Dylan", "Eric", "Daniel"};
    private static final Logger logger = LoggerFactory.getLogger(ClientApp.class);

    /**
     * Launch the client.
     *
     * @param args client arguments.
     * @throws InterruptedException n/a
     */
    public static void main(String[] args) throws InterruptedException
    {
        SpringApplication.run(ClientApp.class, args);
        // Stop application from pre-mature exit
        Thread.currentThread().join();
    }

    @Override
    public void run(String[] args) throws Exception
    {
        DefaultCamelContext context = new DefaultCamelContext();
        // TODO: This should auto-discover...
        //  - Is the service setup wrong?
        //  - Is boot being dumb?
        context.addComponent("dds", new DdsCamelComponent());
        context.addRoutes(new ClientRoutes());
        context.start();
        // Start clients to send dummy chat messages
        startDummyCommunications(context);
    }

    /**
     * Spins up some dummy users that spam messages in a dummy channel.
     *
     * @param ctx Camel context.
     */
    private void startDummyCommunications(DefaultCamelContext ctx)
    {
        ProducerTemplate template = ctx.createProducerTemplate();
        // Setup channel
        Channel channel = new Channel();
        channel.setId(0);
        channel.setName("The only channel");
        channel.setDescription("Dummy message demo");
        template.sendBody(ClientRoutes.CHANNEL_OUT, channel);
        logger.info("Setup channel");
        // Users
        AtomicInteger messageId = new AtomicInteger(0);
        ExecutorService service = Executors.newFixedThreadPool(names.length);
        for (int i = 0; i < names.length; i++)
        {
            int userId = i;
            service.submit(() -> {
                User user = new User();
                user.setId(userId);
                user.setName(names[userId]);
                template.sendBody(ClientRoutes.USER_OUT, user);
                logger.info("Setup user '{}'", user.getName());
                sleep(250);
                while (true)
                {
                    Message message = new Message();
                    message.setUserId(user.getId());
                    message.setChannelId(channel.getId());
                    message.setMessageId(messageId.getAndIncrement());
                    message.setText(UUID.randomUUID().toString());
                    template.sendBody(ClientRoutes.MESSAGE_OUT, message);
                    sleep(500);
                }
            });
            sleep(250);
        }
    }

    /**
     * Sleep wrapper.
     *
     * @param ms Millis to sleep for.
     */
    private void sleep(int ms)
    {
        try
        {
            Thread.sleep(250);
        } catch (InterruptedException ex)
        {
            logger.error("Failed to wait: " + ex.getMessage());
        }
    }
}