package com.chesapeaketechnology.dds.server;

import com.chesapeaketechnology.dds.DdsCamelComponent;
import org.apache.camel.impl.DefaultCamelContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;


/**
 * Sample chat application, server-side boot application.
 *
 * @author Copyright &#169; 2020 Chesapeake Technology International Corp.
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.chesapeaketechnology.dds.server")
public class ServerApp implements CommandLineRunner
{
    private static final Logger logger = LoggerFactory.getLogger(ServerApp.class);

    /**
     * Launch the server.
     *
     * @param args Server arguments.
     * @throws InterruptedException n/a
     */
    public static void main(String[] args) throws InterruptedException
    {
        SpringApplication.run(ServerApp.class, args);
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
        context.addRoutes(new ServerRoutes());
        context.start();
    }
}