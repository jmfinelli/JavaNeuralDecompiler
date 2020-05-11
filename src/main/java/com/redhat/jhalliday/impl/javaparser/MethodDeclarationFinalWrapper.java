package com.redhat.jhalliday.impl.javaparser;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.metamodel.FieldAccessExprMetaModel;
import com.redhat.jhalliday.impl.FinalMethodWrapper;

import java.util.LinkedHashSet;

public class MethodDeclarationFinalWrapper extends FinalMethodWrapper<MethodDeclaration> {

    public MethodDeclarationFinalWrapper(MethodDeclaration method) {

        this.method = method;
        this.name = method.getNameAsString();

        if (method.getBody().isPresent()) {

            method.getBody().get().findAll(SimpleName.class).forEach(x -> this.toReplace.add(x.asString()));

//            method.getBody().get().findAll(MethodCallExpr.class).forEach(x -> this.toReplace.add(x.getName().asString()));
//            method.getBody().get().findAll(FieldAccessExpr.class).forEach(x -> this.toReplace.add(x.getName().asString()));
//            method.getBody().get().findAll(ClassOrInterfaceType.class).forEach(x -> this.toReplace.add(x.getName().asString()));

//            System.out.println("EXPRESSION.CLASS");
//            method.getBody().get().findAll(Expression.class).forEach(x -> System.out.printf("%s: %s\n", x.getClass().toGenericString(), x.toString()));
//            System.out.println("SIMPLENAME.CLASS");
//            method.getBody().get().findAll(SimpleName.class).forEach(x -> System.out.printf("%s: %s\n", x.getClass().toGenericString(), x.asString()));
//            System.out.println("NODE.CLASS");
//            method.getBody().get().findAll(Node.class).forEach(x -> System.out.printf("%s: %s\n", x.getClass().toGenericString(), x.toString()));
//            System.out.println();
        }
    }
}