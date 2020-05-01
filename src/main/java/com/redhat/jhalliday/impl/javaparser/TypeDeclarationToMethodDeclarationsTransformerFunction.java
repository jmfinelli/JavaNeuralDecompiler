package com.redhat.jhalliday.impl.javaparser;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.redhat.jhalliday.TransformerFunction;
import com.redhat.jhalliday.impl.ClassWrapper;

import java.util.List;
import java.util.stream.Stream;

public class TypeDeclarationToMethodDeclarationsTransformerFunction implements TransformerFunction<ClassWrapper<TypeDeclaration>, MethodDeclaration> {

    @Override
    public Stream<MethodDeclaration> apply(ClassWrapper<TypeDeclaration> wrapper) {

        TypeDeclaration typeDeclaration = wrapper.unwrap();

        List<MethodDeclaration> methodDeclarations = typeDeclaration.findAll(MethodDeclaration.class);

        return methodDeclarations.stream();
    }
}
