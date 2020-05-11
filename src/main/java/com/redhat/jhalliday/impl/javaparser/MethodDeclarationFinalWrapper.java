package com.redhat.jhalliday.impl.javaparser;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.redhat.jhalliday.impl.FinalMethodWrapper;
import org.w3c.dom.ls.LSOutput;

import java.util.HashMap;

public class MethodDeclarationFinalWrapper extends FinalMethodWrapper<MethodDeclaration> {

    public MethodDeclarationFinalWrapper(MethodDeclaration method) {

        this.method = method;
        this.name = method.getNameAsString();

        this.methodNames = new HashMap<>();
        method.findAll(MethodCallExpr.class).forEach(x -> this.methodNames.putIfAbsent(this.methodNames.size(), x.getName().asString()));
        this.localVariables = new HashMap<>();
        if (method.getBody().isPresent()) {

            System.out.println("EXPRESSION.CLASS");
            method.getBody().get().findAll(Expression.class).forEach(x -> System.out.printf("%s: %s\n", x.getClass().toGenericString(), x.toString()));
            System.out.println("NODE.CLASS");
            method.getBody().get().findAll(Node.class).forEach(x -> System.out.printf("%s: %s\n", x.getClass().toGenericString(), x.toString()));
            System.out.println();
        }
    }
}