package com.redhat.jhalliday.impl.javaparser;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.redhat.jhalliday.TransformerFunction;

import java.util.List;
import java.util.stream.Stream;

public class CompilationUnitToMethodDeclarationsTransformerFunction implements TransformerFunction<CompilationUnit, MethodDeclaration> {

    @Override
    public Stream<MethodDeclaration> apply(CompilationUnit compilationUnit) {

        List<MethodDeclaration> methodDeclarations = compilationUnit.findAll(MethodDeclaration.class);

        return methodDeclarations.stream();
    }
}
