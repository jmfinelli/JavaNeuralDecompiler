package com.redhat.jhalliday.impl.javaparser;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.redhat.jhalliday.InfoExtractor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MethodDeclarationInfoExtractor implements InfoExtractor<MethodDeclaration> {

    @Override
    public Map<String, InfoType> apply(MethodDeclaration methodDeclaration) {

        Map<String, InfoType> results = new HashMap<>();

        BlockStmt bodyStmt = methodDeclaration.getBody().get();

        List<Expression> list = bodyStmt.findAll(Expression.class);

        bodyStmt.findAll(MethodCallExpr.class)
                .forEach(x -> results.putIfAbsent(x.getName().asString(), InfoType.MET));

        bodyStmt.findAll(NameExpr.class)
                .forEach(x -> results.putIfAbsent(x.getName().asString(), InfoType.VAR));

        bodyStmt.findAll(FieldAccessExpr.class)
                .forEach(x -> results.putIfAbsent(x.getName().asString(), InfoType.FIELD));

        bodyStmt.findAll(ClassExpr.class)
                .forEach(x -> results.putIfAbsent(x.getTypeAsString(), InfoType.CLASS));

        bodyStmt.findAll(ObjectCreationExpr.class)
                .forEach(x -> results.putIfAbsent(x.getTypeAsString(), InfoType.CLASS));

        return results;

    }
}
