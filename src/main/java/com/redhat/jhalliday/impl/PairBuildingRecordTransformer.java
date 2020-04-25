package com.redhat.jhalliday.impl;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.TypeDeclaration;
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

        if (decompilationRecord.getLowLevelRepresentation().getName().equals("net.bytebuddy.jar.asm.ClassReader"))
            System.out.println("Stop");

        for (CtMethod ctMethod : ctMethods) {
            List<MethodDeclaration> interestingMethods = interestingMethods(ctMethod, methodDeclarations);
            if (interestingMethods.size() == 1) {

                DecompilationRecord<CtMethod, MethodDeclaration> result =
                        new GenericDecompilationRecord<>(ctMethod, interestingMethods.get(0), decompilationRecord);

                results.add(result);
            }
        }

        if (results.size() != ctMethods.size()) {
            different++;
            System.out.printf("WARNING #%d! Methods in the .class file are %d but %d were(was) found!\n", different, ctMethods.size(), results.size());
            System.out.printf("DEBUG INFO! .class: %s\n\n", decompilationRecord.getLowLevelRepresentation().getName());
        }


        return results.stream();
    }

    static int different = 0;

    private List<MethodDeclaration> interestingMethods(CtMethod ctMethod, List<MethodDeclaration> methodDeclarations) {

        String methodName = ctMethod.getName();

        List<CtClass> parametersFromSource = new ArrayList<>();
        try {
            parametersFromSource = Arrays.asList(ctMethod.getParameterTypes());
        } catch (NotFoundException e) {
            // Should not be a problem as all CtClass instances were initialised with
            // the use of the default ClassPool
        }

        String className = ctMethod.getDeclaringClass().getSimpleName();

        final List<CtClass> finalParametersFromSource = new ArrayList<>(parametersFromSource);

        List<MethodDeclaration> possibleMethods = methodDeclarations.stream().filter(x ->
        {

            List<Parameter> parameters = x.getParameters();

            if (parameters.size() == finalParametersFromSource.size() && methodName.contains(x.getName().asString())) {

                /*
                 * Get the class where the MethodDeclaration was found
                 * TODO: There can be a method declared in a method, change this in something better
                 */
                String classNameFromdotJava = "";
                if (x.getParentNode().isPresent()) {
                    Node parent = x.getParentNode().get();
                    if (x.getParentNode().get() instanceof TypeDeclaration)
                        classNameFromdotJava = ((TypeDeclaration) parent).getNameAsString();
                }

                if (!classNameFromdotJava.isEmpty() && className.endsWith(classNameFromdotJava)) {
                    int checks = 0;
                    for (Parameter parameter : parameters) {
                        Type type = parameter.getType();
                        String typeToCheck = type instanceof ClassOrInterfaceType ?
                                type.asClassOrInterfaceType().getName().getIdentifier() :
                                type.asString();
                        if (finalParametersFromSource.stream().anyMatch(
                                p -> p.getName().endsWith(typeToCheck))
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
