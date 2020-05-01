package com.redhat.jhalliday.impl.javaparser;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.redhat.jhalliday.impl.ClassWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class TypeDeclarationWrapper extends ClassWrapper<TypeDeclaration> {

    public static List<TypeDeclarationWrapper> getTypeDeclarationWrappersFromCompilationUnit(CompilationUnit compilationUnit) {

        List<TypeDeclarationWrapper> results = new ArrayList<>();

        String filename = "";
        if (compilationUnit.getStorage().isPresent())
            filename = compilationUnit.getStorage().get().getFileName();

        List<TypeDeclaration> declarationList = new ArrayList<>();
        Stream.of(
                compilationUnit.findAll(ClassOrInterfaceDeclaration.class),
                compilationUnit.findAll(EnumDeclaration.class))
                .forEach(declarationList::addAll);

        for(TypeDeclaration declaration : declarationList){
            TypeDeclarationWrapper temp = new TypeDeclarationWrapper(declaration, filename);
            if (!results.contains(temp))
                results.add(new TypeDeclarationWrapper(declaration, filename));
        }

        return results;

    }

    public TypeDeclarationWrapper(TypeDeclaration typeDeclaration, String sourceFile) {

        sourceFileName = sourceFile;
        clazz = typeDeclaration;

        if (typeDeclaration.getFullyQualifiedName().isPresent())
            qualifiedName = typeDeclaration.getFullyQualifiedName().get().toString();
    }

}
