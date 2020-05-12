package com.chesapeaketechnology.idl.patch;

import com.chesapeaketechnology.idl.CompatibilityType;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.Statement;

import java.util.ArrayList;
import java.util.List;

import static com.github.javaparser.StaticJavaParser.parseName;

/**
 * A source processor that manipulates the source to be compliant with given DDS implementations.
 *
 * @author Copyright &#169; 2020 Chesapeake Technology International Corp.
 */
public class CompatibilityProcessor extends AbstractProcessor
{
    public static final String CAFE_KEY_COMMENT = "@key";
    private static final CharSequence IHMC_COPY = "PubSubType.staticCopy(";
    private final CompatibilityType type;

    /**
     * @param code Initial source code.
     * @param type Compatibility type.
     */
    public CompatibilityProcessor(String code, CompatibilityType type)
    {
        super(code);
        this.type = type;
    }

    @Override
    public String apply()
    {
        // Do not update non-idl types (idl-specified enums get skipped with this)
        if (!isIdlType())
        {
            return getCode();
        }
        // Apply changes
        switch (type)
        {
            case CAFE:
                CompilationUnit cu = createCompilationUnit();
                // Add KeyList annotation
                visitCafeAnnotation(cu);
                // Add "this" qualifier to expressions
                visitFixIhmcSetterScope(cu);
                // IHMC adds an extra "_" to fields.
                // This removes it from all references. This works since we qualify field access with "this" beforehand
                setCode(cu.toString().replaceAll("(?<=\\w)_(?=\\b)", ""));
                break;
            default:
               break;
        }
        return getCode();
    }

    private void visitCafeAnnotation(CompilationUnit cu)
    {
        // If no key annotation exists in the comments, no action is needed.
        if (!cu.toString().contains(CAFE_KEY_COMMENT))
        {
            return;
        }
        String type = getPrimaryType(cu);
        List<String> keys = new ArrayList<>();
        for (Comment comment : cu.getAllComments())
        {
            Node node = comment.getCommentedNode().orElse(null);
            if (node instanceof FieldDeclaration)
            {
                // IHMC's fields have extra "_", so substring that from the key name.
                VariableDeclarator field = ((FieldDeclaration) node).getVariable(0);
                keys.add(field.getNameAsString().substring(0, field.getNameAsString().length() - 1));
                node.removeComment();
            }
        }
        // @com.prismtech.cafe.dcps.keys.KeyList(
        //    topicType = "TypeName",
        //    keys = {"key1", "key2"}
        //)
        NodeList<MemberValuePair> nodes = new NodeList<>();
        nodes.add(new MemberValuePair("topicType", new NameExpr("\"" + type.substring(type.lastIndexOf('.') + 1) + "\"")));
        nodes.add(new MemberValuePair("keys", new NameExpr("{\"" + String.join("\",\"", keys) + "\"}")));
        NormalAnnotationExpr annotation = new NormalAnnotationExpr(
                parseName("com.prismtech.cafe.dcps.keys.KeyList"), nodes);
        cu.getClassByName(type).get().addAnnotation(annotation);
    }

    private void visitFixIhmcSetterScope(CompilationUnit cu)
    {
        List<MethodDeclaration> methods = cu.findAll(MethodDeclaration.class);
        for (MethodDeclaration dec : methods)
        {
            // Only apply to setters
            if (!dec.getNameAsString().startsWith("set"))
            {
                continue;
            }
            // Update all statements to include "this" qualifier
            for (Statement n : dec.getBody().get().getStatements())
            {
                if (n instanceof ExpressionStmt) {
                    ExpressionStmt curStmt = (ExpressionStmt) n;
                    String curStmtText = curStmt.getExpression().toString();
                    if (!curStmtText.contains(IHMC_COPY))
                    {
                        curStmt.setExpression("this." + curStmtText);
                    }
                }
            }
        }
    }
}
