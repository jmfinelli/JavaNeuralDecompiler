package com.redhat.jhalliday.impl;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.redhat.jhalliday.DecompilationRecord;
import com.redhat.jhalliday.RecordTransformer;
import com.redhat.jhalliday.impl.javaparser.CompilationUnitToMethodDeclarationTransformerFunction;
import com.redhat.jhalliday.impl.javassist.CtClassToCtMethodsTransformerFunction;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PairBuldingRecordTransformer implements RecordTransformer<CtClass, CompilationUnit, CtMethod, MethodDeclaration> {

    @Override
    public Stream<DecompilationRecord<CtMethod, MethodDeclaration>> apply(DecompilationRecord<CtClass, CompilationUnit> decompilationRecord) {

        CtClassToCtMethodsTransformerFunction ctMethodsTransformerFunction = new CtClassToCtMethodsTransformerFunction();
        CompilationUnitToMethodDeclarationTransformerFunction methodDeclarationTransformerFunction = new CompilationUnitToMethodDeclarationTransformerFunction();

        List<CtMethod> ctMethods = ctMethodsTransformerFunction
                .apply(decompilationRecord.getLowLevelRepresentation()).collect(Collectors.toList());
        List<MethodDeclaration> methodDeclarations = methodDeclarationTransformerFunction
                .apply(decompilationRecord.getHighLevelRepresentation()).collect(Collectors.toList());

        List<DecompilationRecord<CtMethod, MethodDeclaration>> results = new LinkedList<>();

        for (CtMethod ctMethod : ctMethods) {
            List<MethodDeclaration> interestingMethods = interestingMethods(ctMethod, methodDeclarations);
            if (interestingMethods.size() == 1) {

                DecompilationRecord<CtMethod, MethodDeclaration> result =
                        new GenericDecompilationRecord<>(ctMethod, interestingMethods.get(0), decompilationRecord);

                results.add(result);
            }
        }

        if (results.size() != ctMethods.size()){
            different++;
            System.out.println("Again: " + different);
        }

        return results.stream();
    }

    static private Pattern extractParametersType = Pattern.compile("(\\w+);");
    static int different = 0;

    private List<MethodDeclaration> interestingMethods(CtMethod ctMethod, List<MethodDeclaration> methodDeclarations) {

        String methodName = ctMethod.getName();
        String methodSignature = ctMethod.getSignature();
        String className = ctMethod.getDeclaringClass().getSimpleName();
        Matcher matcher = extractParametersType.matcher(methodSignature);

        List<String> parameterTypes = matcher.results().map(x -> x.group(1)).collect(Collectors.toList());

        List<MethodDeclaration> possibleMethods = methodDeclarations.stream().filter(x ->
        {

            List<Parameter> parameters = x.getParameters();

            if (parameters.size() == parameterTypes.size() && methodName.contains(x.getName().asString())) {

                /*
                 * Get the class where the MethodDeclaration was found
                 */
                String classNameFromdotJava = "";
                if (x.getParentNode().isPresent()) {
                    Node parent = x.getParentNode().get();
                    if (x.getParentNode().get() instanceof TypeDeclaration)
                        classNameFromdotJava = ((TypeDeclaration) parent).getNameAsString();
                }

                if (!classNameFromdotJava.isEmpty() && className.contains(classNameFromdotJava)) {
                    int checks = 0;
                    for (Parameter parameter : parameters) {
                        if (parameterTypes.contains(parameter.getType().asString()))
                            checks++;
                    }

                    return checks == parameters.size();
                }
            }

            return false;

        }).collect(Collectors.toList());

        return possibleMethods;
    }
}
