package com.redhat.jhalliday.impl;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.type.ArrayType;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import com.redhat.jhalliday.DecompilationRecord;
import com.redhat.jhalliday.RecordTransformer;
import com.redhat.jhalliday.impl.javaparser.CompilationUnitToMethodDeclarationTransformerFunction;
import com.redhat.jhalliday.impl.javassist.CtClassToCtMethodsTransformerFunction;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PairBuildingRecordTransformer implements RecordTransformer<CtClass, CompilationUnit, CtMethod, MethodDeclaration> {

    // TODO: convert CtClass and CompilationUnit before pairing methods up. This would help improving performances

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

//        if (results.size() != ctMethods.size()) {
//            different++;
//            System.out.printf("WARNING #%d! Methods in the .class file are %d but %d were(was) found!\n", different, ctMethods.size(), results.size());
//            System.out.printf("DEBUG INFO! .class: %s\n\n", decompilationRecord.getLowLevelRepresentation().getName());
//        }

        return results.stream();
    }

    static int different = 0;

    private List<MethodDeclaration> interestingMethods(CtMethod ctMethod, List<MethodDeclaration> methodDeclarations) {

        List<CtClass> parametersFromBytecode = new ArrayList<>();
        try {
            parametersFromBytecode = Arrays.asList(ctMethod.getParameterTypes());
        } catch (NotFoundException e) {
            // Should not be a problem as all CtClass instances were initialised with
            // the use of the default ClassPool
        }

        String className = ctMethod.getDeclaringClass().getPackageName() +
                "." + ctMethod.getDeclaringClass().getSimpleName().replaceAll("\\$", ".");

        final List<CtClass> finalParametersFromSource = new ArrayList<>(parametersFromBytecode);

        List<MethodDeclaration> possibleMethods = methodDeclarations.stream().filter(x ->
        {

            List<Parameter> parameters = x.getParameters();

            if (parameters.size() == finalParametersFromSource.size() && ctMethod.getName().equals(x.getName().asString())) {

                Node parent = x;
                while (!(parent instanceof ClassOrInterfaceDeclaration) && !(parent instanceof PackageDeclaration)) {

                    if (!parent.getParentNode().isPresent()) break;

                    parent = parent.getParentNode().get();
                }

                String classNameFromdotJava = "";
                if (parent instanceof ClassOrInterfaceDeclaration) {
                    if (((ClassOrInterfaceDeclaration) parent).getFullyQualifiedName().isPresent())
                        classNameFromdotJava = ((ClassOrInterfaceDeclaration) parent).getFullyQualifiedName().get();
                }

                if (!classNameFromdotJava.isEmpty() && className.equals(classNameFromdotJava)) {
                    int checks = 0;
                    for (Parameter parameter : parameters) {

                        Type type = parameter.getType();

                        String typeToCheck = "";
                        if (type instanceof ArrayType)
                            typeToCheck = ((ArrayType)type).asString();
                        else if (type instanceof ClassOrInterfaceType)
                            typeToCheck = type.asClassOrInterfaceType().getName().getIdentifier();
                        else
                            type.asString();

                        final String finalTypeToCheck = typeToCheck;
                        if (finalParametersFromSource.stream().anyMatch(
                                p -> p.getName().endsWith(finalTypeToCheck))
                        )
                            checks++;
                    }

                    return checks == parameters.size();
                }
            }

            return false;

        }).collect(Collectors.toList());

        return possibleMethods;
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
