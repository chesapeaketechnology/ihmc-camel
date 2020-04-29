package com.chesapeaketechnology.idl.asm;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.HashMap;
import java.util.Map;

/**
 * A class visitor that adds a {@link #hashCode()} implementation to all classes.
 *
 * @author Copyright &#169; 2020 Chesapeake Technology International Corp.
 */
public class HashCodeGenerator extends ClassVisitor implements Opcodes
{
    private final Map<String, String> fields = new HashMap<>();
    private String definingType;

    /**
     * Create the hashcode generator.
     * @param vistor Sub visitor.
     */
    public HashCodeGenerator(ClassVisitor vistor)
    {
        super(ASM8, vistor);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces)
    {
        this.definingType = name;
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value)
    {
        // Record instance fields
        if (isInstance(access))
        {
            fields.put(name, descriptor);
        }
        return super.visitField(access, name, descriptor, signature, value);
    }

    @Override
    public void visitEnd()
    {
        // Create hashcode
        MethodVisitor mv = super.visitMethod(ACC_PUBLIC, "hashCode", "()I", null, null);
        Label lblStart = new Label();
        Label lblEnd = new Label();
        // Start label
        mv.visitInsn(NOP);
        mv.visitLabel(lblStart);
        // Create array
        int[] index = {0};
        visitInt(mv, fields.size());
        mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
        // Insert each field reference into the array
        fields.forEach((name, desc) -> {
            // Keep array reference
            mv.visitInsn(DUP);
            // Index to store value
            visitInt(mv, index[0]++);
            // Load "this"
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, definingType, name, desc);
            visitConvertToObject(mv, desc);
            mv.visitInsn(AASTORE);
        });
        mv.visitMethodInsn(INVOKESTATIC, "java/util/Objects", "hash", "([Ljava/lang/Object;)I", false);
        mv.visitInsn(IRETURN);
        // End label
        mv.visitLabel(lblEnd);
        // Visit max stack/max locals
        mv.visitMaxs(fields.size() + 1, 1);
        // Add "this" local
        mv.visitLocalVariable("this", "L" + definingType + ";", null, lblStart, lblEnd, 0);
        // Done
        mv.visitEnd();
        super.visitEnd();
    }

    /**
     * Visit method, converting the type on the top of the stack to an object if necessary.
     *
     * @param mv   Method visitor.
     * @param desc Descriptor of type on stack.
     */
    private static void visitConvertToObject(MethodVisitor mv, String desc)
    {
        // Convert StringBuilder to String
        if (desc.equals("Ljava/lang/StringBuilder;"))
        {
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
        }
        // Primitives are only one char long, skip other non-primitives
        else if (desc.length() > 1)
        {
            return;
        }
        // Use "<PrimBoxType>.valueOf(<Prim>)"
        switch (desc)
        {
            case "Z":
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;", false);
                break;
            case "C":
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Character", "valueOf", "(C)Ljava/lang/Character;", false);
                break;
            case "B":
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;", false);
                break;
            case "S":
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;", false);
                break;
            case "I":
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
                break;
            case "F":
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;", false);
                break;
            case "J":
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;", false);
                break;
            case "D":
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;", false);
                break;
        }
    }

    /**
     * Visit method with a specified number.
     *
     * @param mv   Method visitor.
     * @param size Integer to add to the instructions.
     */
    private static void visitInt(MethodVisitor mv, int size)
    {
        if (size <= 5)
        {
            switch (size)
            {
                case 0:
                    mv.visitInsn(ICONST_0);
                    break;
                case 1:
                    mv.visitInsn(ICONST_1);
                    break;
                case 2:
                    mv.visitInsn(ICONST_2);
                    break;
                case 3:
                    mv.visitInsn(ICONST_3);
                    break;
                case 4:
                    mv.visitInsn(ICONST_4);
                    break;
                case 5:
                    mv.visitInsn(ICONST_5);
                    break;
                default:
                    throw new IllegalStateException("Did not visit with value: " + size);
            }
        } else if (size <= 127)
        {
            mv.visitIntInsn(BIPUSH, size);
        } else if (size <= 32767)
        {
            mv.visitIntInsn(SIPUSH, size);
        } else
        {
            mv.visitLdcInsn(size);
        }
    }

    /**
     * @param access Field access modifiers.
     * @return {@code true} when the modifiers do not inclue static.
     */
    private static boolean isInstance(int access)
    {
        return !isStatic(access);
    }

    /**
     * @param access Field access modifiers.
     * @return {@code true} when the modifiers include static.
     */
    private static boolean isStatic(int access)
    {
        return (access & ACC_STATIC) == ACC_STATIC;
    }
}
