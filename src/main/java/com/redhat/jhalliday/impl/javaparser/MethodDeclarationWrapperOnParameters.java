package com.redhat.jhalliday.impl.javaparser;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.type.ArrayType;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import com.redhat.jhalliday.impl.MethodWrapperOnParameters;

import java.util.List;

public class MethodDeclarationWrapperOnParameters extends MethodWrapperOnParameters<MethodDeclaration> {

    public MethodDeclarationWrapperOnParameters(MethodDeclaration declaration) {

        name = declaration.getNameAsString();
        method = declaration;

        declaration.getParameters().forEach(x -> parametersTypes.add(typeToCheck(x.getType())));

        returnParameterType = typeToCheck(declaration.getType());
    }

    private static String typeToCheck(Type type) {
        /*
         * This is to get parameters that are arrays or classes
         */
        String typeToCheck = "";
        if (type instanceof ArrayType)
            typeToCheck = type.asArrayType().asString();
        else if (type instanceof ClassOrInterfaceType)
            typeToCheck = type.asClassOrInterfaceType().getName().getIdentifier();
        else
            type.asString();

        return typeToCheck;
    }
}
