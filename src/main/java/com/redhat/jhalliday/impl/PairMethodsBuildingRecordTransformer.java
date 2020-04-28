package com.redhat.jhalliday.impl;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.type.ArrayType;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import com.redhat.jhalliday.DecompilationRecord;
import com.redhat.jhalliday.RecordTransformer;
import com.redhat.jhalliday.impl.javassist.CtClassToCtMethodsTransformerFunction;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PairMethodsBuildingRecordTransformer implements RecordTransformer<CtClass, TypeDeclaration, CtMethod, MethodDeclaration> {

    final boolean isDebug = java.lang.management.ManagementFactory.getRuntimeMXBean().
            getInputArguments().toString().contains("jdwp");

    @Override
    public Stream<DecompilationRecord<CtMethod, MethodDeclaration>> apply(DecompilationRecord<CtClass, TypeDeclaration> decompilationRecord) {

        CtClassToCtMethodsTransformerFunction ctMethodsTransformerFunction = new CtClassToCtMethodsTransformerFunction();

        /*
         * Transform decompilationRecord to CtMethod and MethodDeclaration objects
         */

        List<CtMethod> ctMethods = ctMethodsTransformerFunction
                .apply(decompilationRecord.getLowLevelRepresentation()).collect(Collectors.toList());
        List<MethodDeclaration> methodDeclarations = decompilationRecord.getHighLevelRepresentation().getMethods();

        /*
         * Pairs will be stored in the "results" List
         */
        List<DecompilationRecord<CtMethod, MethodDeclaration>> results = new ArrayList<>();

        for (CtMethod ctMethod : ctMethods) {
            List<MethodDeclaration> interestingMethods = new ArrayList<>();

            try {
                interestingMethods = interestingMethods(ctMethod, methodDeclarations);
            } catch (NotFoundException e) {
                // Nothing can happen
            }

            // DEBUG
            if (interestingMethods.size() > 1 && isDebug)
                System.out.println("Found 2 candidates for the same method!");

            if (interestingMethods.size() == 1) {

                // This is a method to check line numbers between CtMethod and MethodDeclaration
                // WARNING! There are differences between javassist and javaparser. Run this tool in debug mode
                //checkLineNumbers(ctMethod, interestingMethods.get(0));

                DecompilationRecord<CtMethod, MethodDeclaration> result =
                        new GenericDecompilationRecord<>(ctMethod, interestingMethods.get(0), decompilationRecord);

                results.add(result);

            }
        }

        // DEBUG
        if (results.size() != ctMethods.size() && isDebug) {
            different++;
            System.out.printf("WARNING #%d! Methods in the .class file are %d but %d were(was) found!\n", different, ctMethods.size(), results.size());
            System.out.printf("DEBUG INFO! .class: %s\n\n", decompilationRecord.getLowLevelRepresentation().getName());
        }

        return results.stream();
    }

    static int different = 0;

    /**
     * This method creates a List of MethodDeclaration that are eligible to be paired up with the CtMethod
     * passed as parameter. It is crucial to return all possible matches as more than one match would mean
     * that something went wrong
     * @param ctMethod The bytecode method to match
     * @param methodDeclarations A List of MethodDeclaration to pick matches from
     * @return A List of possible MethodDeclaration
     */
    private List<MethodDeclaration> interestingMethods(CtMethod ctMethod, List<MethodDeclaration> methodDeclarations)
            throws NotFoundException {

        /*
         * Fetch the list of Parameters' Types and the Return Type of the CtMethod
         */
        List<CtClass> parametersFromBytecode = Arrays.asList(ctMethod.getParameterTypes());
        final CtClass returnFromBytecode = ctMethod.getReturnType();

        List<MethodDeclaration> possibleMethods = methodDeclarations.stream().filter(x ->
        {

            List<Parameter> parameters = x.getParameters();

            String returnType = typeToCheck(x.getType());

            if (parameters.size() == parametersFromBytecode.size() &&
                    ctMethod.getName().equals(x.getName().asString()) &&
                    returnFromBytecode.getName().endsWith(returnType)) {

                /*
                 * Loop until a Class containing the ctMethod is found.
                 * The instanceof PackageDeclaration is used as upper limit
                 */
                Node parent = x;
                while (!(parent instanceof ClassOrInterfaceDeclaration) && !(parent instanceof PackageDeclaration)) {

                    if (parent.getParentNode().isEmpty()) break;

                    parent = parent.getParentNode().get();
                }

                int checks = 0;
                for (Parameter parameter : parameters) {

                    final String typeToCheck = typeToCheck(parameter.getType());
                    if (parametersFromBytecode.stream().anyMatch(
                            p -> p.getName().endsWith(typeToCheck))
                    )
                        checks++;
                }

                return checks == parameters.size();
            }

            return false;

        }).collect(Collectors.toList());

        return possibleMethods;
    }

    private String typeToCheck(Type type) {
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

    private boolean checkLineNumbers(CtMethod ctMethod, MethodDeclaration declaration) {

        int lineNumberFromBytecode = ctMethod.getMethodInfo().getLineNumber(0);
        int lineNumberFromSource = lineNumberFromBytecode;
        if (declaration.getBody().isPresent())
            if (declaration.getBody().get().getBegin().isPresent())
                lineNumberFromSource = declaration.getBody().get().getBegin().get().line;

            if (Math.abs(lineNumberFromBytecode - lineNumberFromSource) > 1 && isDebug)
                System.out.printf("Method %s in %s has been found in two different locations!" +
                        "\nJavaparser gives: %d, while Javassist gives: %d\n\n",
                        ctMethod.getName(),
                        ctMethod.getDeclaringClass().getName(),
                        lineNumberFromSource,
                        lineNumberFromBytecode);

        return lineNumberFromBytecode - 1 == lineNumberFromSource;
    }

    private boolean doubleCheckWithLineNumber() {
        //        String methodSignature = ctMethod.getSignature();
//        String parameterString = methodSignature.substring(methodSignature.indexOf('('), methodSignature.indexOf(')'));

//        I was not able to find a class in javassist to summarise the type of method's parameters.
//        The only solution is to use the constant pool which returns a String anyway.

//        CodeAttribute codeAttribute = ctMethod.getMethodInfo().getCodeAttribute();
//        if (codeAttribute != null) {
//            LocalVariableTypeAttribute table = (LocalVariableTypeAttribute) codeAttribute.getAttribute(LocalVariableAttribute.typeTag);
//            if (table != null)
//                for (int i = 0; i < table.tableLength(); i++) {
//                    System.out.printf("Parameter %d\n", i);
//                    System.out.printf("Parameter Type: %s\n", ctMethod.getMethodInfo().getConstPool().getUtf8Info(table.nameIndex(i)));
//                }
//        }

        return true;
    }
}