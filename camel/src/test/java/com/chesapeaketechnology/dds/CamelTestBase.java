package com.chesapeaketechnology.dds;

import com.chesapeaketechnology.dds.idl.TestIDL;
import org.apache.camel.test.junit5.CamelTestSupport;

/**
 * Common camel test constants.
 *
 * @author Copyright &#169; 2020 Chesapeake Technology International Corp.
 */
public class CamelTestBase extends CamelTestSupport
{
    protected static final int MESSAGE_COUNT = 20;
    protected static final int REPEAT_COUNT = 5;
    protected static final String URI_OUT_HIGH = DdsUriBuilder.create()
            .content(TestIDL.class)
            .domain(0)
            .quality(DdsQoSConfigManager.CONFIG_HIGH)
            .getUri();
    protected static final String URI_OUT_LOW = DdsUriBuilder.create()
            .content(TestIDL.class)
            .domain(0)
            .quality(DdsQoSConfigManager.CONFIG_LOW)
            .getUri();
    protected static final String URI_IN_HIGH = DdsUriBuilder.create()
            .content(TestIDL.class)
            .domain(0)
            .quality(DdsQoSConfigManager.CONFIG_HIGH)
            .getUri();
    protected static final String URI_IN_HIGH_REUSE = DdsUriBuilder.create()
            .content(TestIDL.class)
            .domain(0)
            .quality(DdsQoSConfigManager.CONFIG_HIGH)
            .reuseMessageStructures()
            .getUri();
    protected static final String URI_MOCK = "mock:result";
    protected static final String URI_DIRECT = "direct:test";
    protected static final int MOCK_TIMEOUT_MS = 250;

    protected void sendTestMessages(String out) throws InterruptedException
    {
        for (int i = 0; i < MESSAGE_COUNT; i++)
        {
            TestIDL body = new TestIDL(i);
            sendBody(out, body);
        }
    }
}
