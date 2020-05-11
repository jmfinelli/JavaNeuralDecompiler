package com.redhat.jhalliday.impl;

import com.github.javaparser.ast.body.MethodDeclaration;

import java.util.*;

public abstract class FinalHighLevelMethodWrapper extends FinalMethodWrapper<MethodDeclaration> {

    protected Set<String> NameExpr = new HashSet<>();
    protected Set<String> ClassExpr = new HashSet<>();
    protected Set<String> MethodExpr = new HashSet<>();
    protected Set<String> LiteralExpr = new HashSet<>();

    public Set<String> getNameExpr() { return NameExpr; }

    public Set<String> getClassExpr() { return ClassExpr; }

    public Set<String> getMethodExpr() { return MethodExpr; }

    public Set<String> getLiteralExpr() { return LiteralExpr; }
}
