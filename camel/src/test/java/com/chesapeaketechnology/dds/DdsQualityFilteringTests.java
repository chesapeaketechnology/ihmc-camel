package com.chesapeaketechnology.dds;

import org.apache.camel.RoutesBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;

import java.util.concurrent.TimeUnit;

/**
 * DDS camel component tests to validate QoS filtering.
 *
 * @author Copyright &#169; 2020 Chesapeake Technology International Corp.
 */
public class DdsQualityFilteringTests
{
    /**
     * When sending messages through an endpoint with any quality-of-service,
     * any endpoint on the same uri with a matching quality-of-service will
     * handle the messages, since the quality requirements are met.
     */
    @Nested
    class HighToHigh extends CamelTestBase
    {
        @RepeatedTest(REPEAT_COUNT)
        void testSameConfigThroughput() throws Exception
        {
            MockEndpoint result = getMockEndpoint(URI_MOCK);
            result.expectedMessageCount(MESSAGE_COUNT);
            sendTestMessages(URI_OUT_HIGH);
            assertMockEndpointsSatisfied(MOCK_TIMEOUT_MS, TimeUnit.MILLISECONDS);
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
                    from(URI_IN_HIGH).to(URI_MOCK);
                }
            };
        }
    }

    /**
     * When sending messages through an endpoint with a low quality-of-service,
     * any endpoint on the same uri with a higher quality-of-service will not
     * handle the messages, as they do not meet the quality requirements.
     */
    @Nested
    class LowToHigh extends CamelTestBase
    {
        @RepeatedTest(REPEAT_COUNT)
        void testFilterFromLowQuality() throws Exception
        {
            MockEndpoint result = getMockEndpoint(URI_MOCK);
            result.expectedMessageCount(0);
            sendTestMessages(URI_DIRECT);
            assertMockEndpointsSatisfied(MOCK_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        }

        @Override
        protected RoutesBuilder createRouteBuilder()
        {
            return new RouteBuilder()
            {
                @Override
                public void configure()
                {
                    from(URI_DIRECT).to(URI_OUT_LOW);
                    from(URI_IN_HIGH).to(URI_MOCK);
                }
            };
        }
    }
}
