package com.redhat.jhalliday.impl.javaparser;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.redhat.jhalliday.impl.ClassWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class TypeDeclarationWrapper extends ClassWrapper<TypeDeclaration> {

    public static List<TypeDeclarationWrapper> getTypeDeclarationWrappersFromCompilationUnit(CompilationUnit compilationUnit, String sourceQualifiedName) {

        List<TypeDeclarationWrapper> results = new ArrayList<>();

        String filename = sourceQualifiedName.substring(sourceQualifiedName.lastIndexOf("/") + 1);

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
        else {
            qualifiedName = typeDeclaration.getName().getIdentifier();
        }
    }

    private static TypeDeclaration returnClassOrInterfaceDeclaration(Node node) {
        Node parent = node;
        while (!(parent instanceof TypeDeclaration) && !(parent instanceof PackageDeclaration)) {

            if (parent.getParentNode().isEmpty()) break;

            parent = parent.getParentNode().get();
        }

        if (!(parent instanceof TypeDeclaration)) return null;

        return (TypeDeclaration) parent;
    }

}
