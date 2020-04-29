package com.chesapeaketechnology.dds;

import com.chesapeaketechnology.dds.idl.TestIDL;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * DDS camel component tests to validate QoS filtering.
 *
 * @author Copyright &#169; 2020 Chesapeake Technology International Corp.
 */
public class DdsMessageReuseTests
{
    /**
     * A set using a "IdentityHashMap" backing is so that we use "==" instead of "equals".
     * We want to check literal instances, not value equality here.
     */
    private Set<TestIDL> receivedInstances = Collections.newSetFromMap(new IdentityHashMap<>());

    /**
     * Run test with processes message instances <i>not being reused</i>.
     * <br>
     * Each incoming message should be it's own instance. Since the {@link #receivedInstances} set is
     * backed by identity checking, its size will match the number of messages we send.
     */
    @Nested
    class NoReuse extends CamelTestBase
    {
        @Test
        void testWithoutReuse() throws Exception
        {
            sendTestMessages(URI_DIRECT);
        }

        @Override
        public void tearDown() throws Exception
        {
            super.tearDown();
            assertEquals(MESSAGE_COUNT, receivedInstances.size());
        }

        @Override
        protected RoutesBuilder createRouteBuilder()
        {
            return new RouteBuilder()
            {
                @Override
                public void configure()
                {
                    from(URI_DIRECT).to(URI_OUT_HIGH);
                    from(URI_IN_HIGH)
                            .process(exchange -> receivedInstances.add((TestIDL) exchange.getIn().getBody()));
                }
            };
        }
    }

    /**
     * Run test with processes message instances <i>being reused</i>.
     * <br>
     * Each incoming message will share one instance. Since the {@link #receivedInstances} set is
     * backed by identity checking, its size will be 1 since we only have one receiving endpoint.
     */
    @Nested
    class WithReuse extends CamelTestBase
    {
        @Test
        void testWithReuse() throws Exception
        {
            sendTestMessages(URI_DIRECT);
        }

        @Override
        public void tearDown() throws Exception
        {
            super.tearDown();
            assertEquals(1, receivedInstances.size());
        }

        @Override
        protected RoutesBuilder createRouteBuilder()
        {
            return new RouteBuilder()
            {
                @Override
                public void configure()
                {
                    from(URI_DIRECT)
                            .to(URI_OUT_HIGH);
                    from(URI_IN_HIGH_REUSE)
                            .process(exchange -> receivedInstances.add((TestIDL) exchange.getIn().getBody()));
                }
            };
        }
    }
}
