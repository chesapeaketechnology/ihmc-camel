package com.chesapeaketechnology.idl.patch;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;

import java.util.Arrays;
import java.util.function.BiPredicate;

/**
 * Common source processing.
 *
 * @author Copyright &#169; 2020 Chesapeake Technology International Corp.
 */
@SuppressWarnings("OptionalGetWithoutIsPresent")
public abstract class AbstractProcessor
{
    private final boolean isIdlType;
    private String code;

    protected AbstractProcessor(String code)
    {
        this.code = code;
        this.isIdlType = code.contains(" extends Packet<");
    }

    /**
     * @return Modified source.
     */
    public abstract String apply();

    protected CompilationUnit createCompilationUnit()
    {
        ParseResult<CompilationUnit> result = new JavaParser().parse(getCode());
        if (result.isSuccessful())
        {
            return result.getResult().get();
        } else
        {
            throw new IllegalStateException("Errors in source code: " + Arrays.toString(result.getProblems().toArray()));
        }
    }

    protected boolean isIdlType()
    {
        return isIdlType;
    }

    protected void setCode(String code)
    {
        this.code = code;
    }

    protected String getCode()
    {
        return code;
    }

    /// ==================== STATIC UTILITIES ==================== ///

    protected static String getPrimaryType(CompilationUnit cu)
    {
        return cu.findAll(TypeDeclaration.class).get(0).getNameAsString();
    }

    protected static void insertAfter(CompilationUnit cu, BodyDeclaration<?> body, BiPredicate<Node, Node> predicate) {
        ClassOrInterfaceDeclaration dec = cu.getClassByName(getPrimaryType(cu)).get();
        for (int i = 0; i < dec.getMembers().size(); i++) {
            Node before = i == 0 ? null : dec.getMember(i - 1);
            Node next = dec.getMember(i);
            if (predicate.test(before, next)) {
                dec.getMembers().add(i, body);
                return;
            }
        }
    }
}
