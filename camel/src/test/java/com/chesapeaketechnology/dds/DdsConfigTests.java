package com.chesapeaketechnology.dds;

import com.chesapeaketechnology.dds.idl.TestIDL;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import us.ihmc.pubsub.attributes.ReaderQosHolder;
import us.ihmc.pubsub.attributes.WriterQosHolder;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DDS camel component tests to validate configuration usage.
 *
 * @author Copyright &#169; 2020 Chesapeake Technology International Corp.
 */
public class DdsConfigTests extends CamelTestSupport
{
    @Test
    public void testUseExistingConfigForSubscriber() throws Exception
    {
        for (DdsQosConfig qosConfig : DdsQoSConfigManager.configs())
        {
            // Create URI
            String uri = DdsUriBuilder.create().content(TestIDL.class).quality(qosConfig.getName()).getUri();
            assertTrue(uri.endsWith("?qos=" + qosConfig.getName()));
            // Create endpoint
            DdsCamelComponent component = (DdsCamelComponent) context.getComponent("dds");
            DdsEndpoint endpoint = (DdsEndpoint) component.createEndpoint(uri);
            // Assert attributes match
            DdsConsumer ddsConsumer = Mockito.mock(DdsConsumer.class);
            ReaderQosHolder qos = endpoint.getSubscriber(ddsConsumer).getAttributes().getQos();
            assertEquals(qosConfig.getReliability(), qos.getReliabilityKind());
            assertEquals(qosConfig.getDurability(), qos.getDurabilityKind());
            assertEquals(qosConfig.getOwnerShipPolicy(), qos.getOwnershipPolicyKind());
        }
    }

    @Test
    public void testUseExistingConfigForPublisher() throws Exception
    {
        for (DdsQosConfig qosConfig : DdsQoSConfigManager.configs()) {
            // Create URI
            String uri = DdsUriBuilder.create().content(TestIDL.class).quality(qosConfig.getName()).getUri();
            assertTrue(uri.endsWith("?qos=" + qosConfig.getName()));
            // Create endpoint
            DdsCamelComponent component = (DdsCamelComponent) context.getComponent("dds");
            DdsEndpoint endpoint = (DdsEndpoint) component.createEndpoint(uri);
            // Assert attributes match
            WriterQosHolder qos = endpoint.getPublisher().getAttributes().getQos();
            assertEquals(qosConfig.getReliability(), qos.getReliabilityKind());
            assertEquals(qosConfig.getDurability(), qos.getDurabilityKind());
            assertEquals(qosConfig.getOwnerShipPolicy(), qos.getOwnershipPolicyKind());
        }
    }
}
