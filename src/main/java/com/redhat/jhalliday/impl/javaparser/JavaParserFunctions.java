package com.redhat.jhalliday.impl.javaparser;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.redhat.jhalliday.impl.ClassWrapper;
import com.redhat.jhalliday.impl.MethodWrapper;
import com.redhat.jhalliday.impl.MethodWrapperOnParameters;

import java.util.List;
import java.util.function.Function;

public class JavaParserFunctions {

    public static Function<CompilationUnit, List<? extends ClassWrapper<? extends TypeDeclaration>>> classWrapperFunction =
            TypeDeclarationWrapper::getTypeDeclarationWrappersFromCompilationUnit;

    public static Function<MethodDeclaration, MethodWrapper<MethodDeclaration>> methodWrappingFunction =
            MethodDeclarationWrapper::new;

    public static Function<MethodDeclaration, MethodWrapperOnParameters<MethodDeclaration>> methodWrappingOnParametersFunction =
            MethodDeclarationWrapperOnParameters::new;
}
