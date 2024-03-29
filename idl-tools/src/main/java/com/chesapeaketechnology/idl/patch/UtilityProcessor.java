package com.chesapeaketechnology.idl.patch;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.type.Type;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A source processor that adds helpful methods such as {@link #hashCode()} and {@code copy()}
 * that are not generated by default by the IHMC source code generator.
 *
 * @author Copyright &#169; 2020 Chesapeake Technology International Corp.
 */
public class UtilityProcessor extends AbstractProcessor
{
    private final boolean hash;
    private final boolean copy;
    private final boolean constructor;

    /**
     * Create the method generator.
     *
     * @param code        Initial source code.
     * @param hash        Add hashcode method
     * @param copy        Add copy method.
     * @param constructor Add field-setting constructor.
     */
    public UtilityProcessor(String code, boolean hash, boolean copy, boolean constructor)
    {
        super(code);
        this.hash = hash;
        this.copy = copy;
        this.constructor = constructor;
    }

    @Override
    public String apply()
    {
        // Do not update non-idl types (idl-specified enums get skipped with this)
        if (!isIdlType())
        {
            return getCode();
        }
        // Check if no changes were specified
        if (!hash && !copy && !constructor)
        {
            return getCode();
        }
        CompilationUnit cu = createCompilationUnit();
        if (constructor)
        {
            visitConstructor(cu);
        }
        if (copy)
        {
            visitCopy(cu);
        }
        if (hash)
        {
            visitHashcode(cu);
        }
        return cu.toString();
    }

    /**
     * <pre>return Objects.hash(one, two...)</pre>
     *
     * @param cu Unit to modify.
     */
    private void visitHashcode(CompilationUnit cu)
    {
        BlockStmt block = new BlockStmt();
        MethodCallExpr call = new MethodCallExpr(new NameExpr("Objects"), "hash");
        for (FieldDeclaration field : cu.findAll(FieldDeclaration.class))
        {
            if (field.getCommonType().asString().endsWith("StringBuilder"))
            {
                call.addArgument(new NameExpr(field.getVariable(0).getName() + ".toString()"));
            } else
            {
                call.addArgument(new NameExpr(field.getVariable(0).getName()));
            }
        }
        block.addStatement(new ReturnStmt(call));
        //
        MethodDeclaration method = new MethodDeclaration();
        method.addAnnotation(Override.class);
        method.setName("hashCode");
        method.setModifiers(Modifier.Keyword.PUBLIC);
        method.setType(int.class);
        method.setBody(block);
        // Add import and method definition
        cu.getImports().add(new ImportDeclaration(Objects.class.getName(), false, false));
        cu.getClassByName(getPrimaryType(cu)).get().addMember(method);
    }

    /**
     * <pre>
     * Type t = new Type()
     * t.set(this)
     * return t
     * </pre>
     *
     * @param cu Unit to modify.
     */
    private void visitCopy(CompilationUnit cu)
    {
        Type selfType = StaticJavaParser.parseType(getPrimaryType(cu));
        BlockStmt block = new BlockStmt();
        VariableDeclarationExpr v = new VariableDeclarationExpr();
        v.addVariable(new VariableDeclarator(selfType, "copy"));
        v.getVariable(0).setInitializer("new " + selfType + "()");
        MethodCallExpr call = new MethodCallExpr(v.getVariable(0).getNameAsExpression(), "set", new NodeList<>(new NameExpr("this")));
        block.addStatement(v);
        block.addStatement(call);
        block.addStatement(new ReturnStmt(v.getVariable(0).getNameAsExpression()));
        //
        MethodDeclaration method = new MethodDeclaration();
        method.setName("copy");
        method.setModifiers(Modifier.Keyword.PUBLIC);
        method.setType(selfType);
        method.setBody(block);
        // Add right after the "set" method
        insertAfter(cu, method,
                (before, next) -> before instanceof MethodDeclaration && ((MethodDeclaration) before).getNameAsString().equals("set"));
    }

    /**
     * <pre>
     * Type(t one, t two) {
     * this.one = one;
     * this.two = two
     * }
     * </pre>
     *
     * @param cu Unit to modify.
     */
    private void visitConstructor(CompilationUnit cu)
    {
        // Generate constructor parameters
        List<Parameter> params = cu.findAll(FieldDeclaration.class).stream()
                .map(field -> new Parameter(field.getCommonType(), field.getVariable(0).getNameAsString()))
                .peek(param -> {
                    // Change StringBuilder to String for constructor parameters
                    if (param.getType().asString().endsWith("StringBuilder"))
                    {
                        param.setType("String");
                    }
                }).collect(Collectors.toList());
        ConstructorDeclaration constructor = new ConstructorDeclaration(getPrimaryType(cu));
        constructor.setParameters(new NodeList<>(params));
        constructor.setPublic(true);
        BlockStmt block = new BlockStmt();
        for (FieldDeclaration field : cu.findAll(FieldDeclaration.class))
        {
            if (field.getCommonType().asString().endsWith("StringBuilder")) {
                String name = field.getVariable(0).getNameAsString();
                AssignExpr assign = new AssignExpr(new FieldAccessExpr(
                        new NameExpr("this"), name), new NameExpr("new StringBuilder(" + name + ")"), AssignExpr.Operator.ASSIGN);
                block.addStatement(assign);
            }
            else
            {
                String name = field.getVariable(0).getNameAsString();
                AssignExpr assign = new AssignExpr(new FieldAccessExpr(new NameExpr("this"), name), new NameExpr(name), AssignExpr.Operator.ASSIGN);
                block.addStatement(assign);
            }
        }
        constructor.setBody(block);
        // Add after other constructors
        insertAfter(cu, constructor,
                (before, next) -> before instanceof ConstructorDeclaration && next instanceof MethodDeclaration);
    }
}
