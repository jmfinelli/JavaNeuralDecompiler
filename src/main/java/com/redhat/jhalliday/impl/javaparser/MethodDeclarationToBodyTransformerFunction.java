package com.redhat.jhalliday.impl.javaparser;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.redhat.jhalliday.TransformerFunction;

import java.util.List;
import java.util.stream.Stream;

public class MethodDeclarationToBodyTransformerFunction implements TransformerFunction<MethodDeclaration, String> {

    @Override
    public Stream<String> apply(MethodDeclaration declaration) {
        return null;
    }
}
