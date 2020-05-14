package com.redhat.jhalliday.impl;

import com.github.javaparser.ast.body.MethodDeclaration;

import java.util.*;
import java.util.function.Function;

public abstract class FinalHighLevelMethodWrapper extends FinalMethodWrapper<MethodDeclaration> {

    protected Set<String> NameExprNames;
    protected Set<String> ClassExprNames;
    protected Set<String> MethodExprNames;
    protected Set<String> LiteralExprNames;

    public FinalHighLevelMethodWrapper(MethodDeclaration method, Function<MethodDeclaration, HighInfoExtractor> methodBodyExtractor) {

        this.method = method;
        HighInfoExtractor info = methodBodyExtractor.apply(method);

        NameExprNames = info.getNameExprNames();
        ClassExprNames = info.getClassExprNames();
        MethodExprNames = info.getMethodExprNames();
        LiteralExprNames = info.getLiteralExprNames();

        this.toReplace.addAll(this.ClassExprNames);
        this.toReplace.addAll(this.LiteralExprNames);
        this.toReplace.addAll(this.MethodExprNames);
        this.toReplace.addAll(this.NameExprNames);

        this.methodBody = info.getBody();
    }

    public Set<String> getNameExprNames() { return NameExprNames; }

    public Set<String> getClassExprNames() { return ClassExprNames; }

    public Set<String> getMethodExprNames() { return MethodExprNames; }

    public Set<String> getLiteralExprNames() { return LiteralExprNames; }
}
