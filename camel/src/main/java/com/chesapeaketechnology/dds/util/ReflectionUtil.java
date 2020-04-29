package com.chesapeaketechnology.dds.util;

import us.ihmc.pubsub.TopicDataType;

import java.lang.reflect.Constructor;

/**
 * Reflection utilities pertaining to generated idl types.
 *
 * @author Copyright &#169; 2020 Chesapeake Technology International Corp.
 */
public class ReflectionUtil
{
    private static final String PUB_SUB_EXT = "PubSubType";

    /**
     * @param idlTypeName Fully {@link Class#getName() qualified name} of the an idl type class.
     * @return Instance of the type's class.
     */
    public static Object createIdlType(String idlTypeName)
    {
        try
        {
            // Find and invoke the default constructor
            for (Constructor<?> constructor : Class.forName(idlTypeName).getConstructors())
            {
                if (constructor.getParameterCount() == 0)
                {
                    return constructor.newInstance();
                }
            }
            throw new IllegalStateException("No default constructor in idl type '" + idlTypeName + "'");
        } catch (ReflectiveOperationException ex)
        {
            throw new IllegalStateException("Failed to fetch idl type '" + idlTypeName + "', please check your classpath");
        }
    }

    /**
     * @param idlTypeName Fully {@link Class#getName() qualified name} of the an idl type class.
     * @return Instance of the type's pub-sub class.
     */
    public static TopicDataType<?> createIdlPubSubType(String idlTypeName)
    {
        String idlPubSubTypeName = idlTypeName + PUB_SUB_EXT;
        try
        {
            return (TopicDataType<?>) Class.forName(idlPubSubTypeName).getConstructors()[0].newInstance();
        } catch (ReflectiveOperationException ex)
        {
            throw new IllegalStateException("Failed to fetch serializer for '" + idlTypeName + "', please check your classpath");
        }
    }
}
