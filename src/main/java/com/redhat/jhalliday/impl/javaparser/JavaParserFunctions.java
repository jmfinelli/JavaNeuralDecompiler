package com.redhat.jhalliday.impl.javaparser;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.redhat.jhalliday.impl.MethodWrapper;

import java.util.function.Function;

public class JavaParserFunctions {

    public static Function<MethodDeclaration, MethodWrapper<MethodDeclaration>> methodWrappingFunction = MethodDeclarationWrapper::new;
}
