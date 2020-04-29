package com.chesapeaketechnology.dds.idl;

import us.ihmc.idl.CDR;
import us.ihmc.idl.InterchangeSerializer;
import us.ihmc.pubsub.TopicDataType;
import us.ihmc.pubsub.common.SerializedPayload;

/**
 * Auto generated
 */
public class TestIDLPubSubType implements TopicDataType<TestIDL>
{
    public static final java.lang.String name = "messaging::" + TestIDL.class.getSimpleName();

    private final CDR serializeCDR = new CDR();
    private final CDR deserializeCDR = new CDR();

    @Override
    public void serialize(TestIDL data, SerializedPayload serializedPayload) throws java.io.IOException
    {
        serializeCDR.serialize(serializedPayload);
        write(data, serializeCDR);
        serializeCDR.finishSerialize();
    }

    @Override
    public void deserialize(SerializedPayload serializedPayload, TestIDL data) throws java.io.IOException
    {
        deserializeCDR.deserialize(serializedPayload);
        read(data, deserializeCDR);
        deserializeCDR.finishDeserialize();
    }

    public static int getMaxCdrSerializedSize()
    {
        return getMaxCdrSerializedSize(0);
    }

    public static int getMaxCdrSerializedSize(int current_alignment)
    {
        int initial_alignment = current_alignment;
        current_alignment += 4 + CDR.alignment(current_alignment, 4);
        current_alignment += 4 + CDR.alignment(current_alignment, 4) + 255 + 1;
        return current_alignment - initial_alignment;
    }

    public final static int getCdrSerializedSize(TestIDL data)
    {
        return getCdrSerializedSize(data, 0);
    }

    public final static int getCdrSerializedSize(TestIDL data, int current_alignment)
    {
        int initial_alignment = current_alignment;
        current_alignment += 4 + CDR.alignment(current_alignment, 4);
        return current_alignment - initial_alignment;
    }

    public static void write(TestIDL data, CDR cdr)
    {
        cdr.write_type_2(data.getId());
    }

    public static void read(TestIDL data, CDR cdr)
    {
        data.setId(cdr.read_type_2());
    }

    @Override
    public final void serialize(TestIDL data, InterchangeSerializer ser)
    {
        ser.write_type_2("id", data.getId());
    }

    @Override
    public final void deserialize(InterchangeSerializer ser, TestIDL data)
    {
        data.setId(ser.read_type_2("id"));
    }

    public static void staticCopy(TestIDL src, TestIDL dest)
    {
        dest.set(src);
    }

    @Override
    public TestIDL createData()
    {
        return new TestIDL();
    }

    @Override
    public int getTypeSize()
    {
        return CDR.getTypeSize(getMaxCdrSerializedSize());
    }

    @Override
    public java.lang.String getName()
    {
        return name;
    }

    public void serialize(TestIDL data, CDR cdr)
    {
        write(data, cdr);
    }

    public void deserialize(TestIDL data, CDR cdr)
    {
        read(data, cdr);
    }

    public void copy(TestIDL src, TestIDL dest)
    {
        staticCopy(src, dest);
    }

    @Override
    public TestIDLPubSubType newInstance()
    {
        return new TestIDLPubSubType();
    }
}
