package com.chesapeaketechnology.dds.idl;

import us.ihmc.communication.packets.Packet;
import us.ihmc.euclid.interfaces.Settable;
import us.ihmc.euclid.interfaces.EpsilonComparable;

import java.util.function.Supplier;

import us.ihmc.idl.IDLTools;
import us.ihmc.pubsub.TopicDataType;

/**
 * Auto generated
 */
public class TestIDL extends Packet<TestIDL> implements Settable<TestIDL>, EpsilonComparable<TestIDL>
{
    public int id;

    public TestIDL()
    {
    }

    // Added for convenience
    public TestIDL(int id)
    {
        this.id = id;
    }

    public TestIDL(TestIDL other)
    {
        set(other);
    }

    public void set(TestIDL other)
    {
        id = other.id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public int getId()
    {
        return id;
    }

    public static Supplier<TestIDLPubSubType> getPubSubType()
    {
        return TestIDLPubSubType::new;
    }

    @Override
    public Supplier<TopicDataType> getPubSubTypePacket()
    {
        return TestIDLPubSubType::new;
    }

    @Override
    public boolean epsilonEquals(TestIDL other, double epsilon)
    {
        if (other == null) return false;
        if (other == this) return true;
        if (!IDLTools.epsilonEqualsPrimitive(this.id, other.id, epsilon)) return false;
        return true;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof TestIDL)) return false;
        TestIDL otherMyClass = (TestIDL) other;
        if (this.id != otherMyClass.id) return false;
        return true;
    }

    @Override
    public java.lang.String toString()
    {
        return getClass().getSimpleName() + " {id=" + this.id + "}";
    }
}
