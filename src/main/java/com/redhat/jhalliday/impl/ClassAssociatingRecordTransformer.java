package com.redhat.jhalliday.impl;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.redhat.jhalliday.DecompilationRecord;
import com.redhat.jhalliday.RecordTransformer;
import javassist.CtClass;
import javassist.bytecode.SourceFileAttribute;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class ClassAssociatingRecordTransformer implements
        RecordTransformer<Map<String, CtClass>, Map<String, CompilationUnit>, CtClass, TypeDeclaration> {

    @Override
    public Stream<DecompilationRecord<CtClass, TypeDeclaration>> apply(DecompilationRecord<Map<String, CtClass>, Map<String, CompilationUnit>> decompilationRecord) {

        Map<String, CtClass> binMap = decompilationRecord.getLowLevelRepresentation();
        Map<String, CompilationUnit> srcMap = decompilationRecord.getHighLevelRepresentation();

        List<DecompilationRecord<CtClass, TypeDeclaration>> results = new ArrayList<>();

        for (Map.Entry<String, CtClass> entry : binMap.entrySet()) {

            /*
             * Extract the source filename from the CtClass
             */
            CtClass ctClass = entry.getValue();
            SourceFileAttribute attribute = (SourceFileAttribute) ctClass.getClassFile().getAttribute("SourceFile");
            if (attribute == null) {
                continue;
            }

            String prefix = entry.getKey();
            prefix = prefix.substring(0, prefix.lastIndexOf('/') + 1);
            String sourceFileName = prefix + attribute.getFileName();

            /*
             * Truncate the qualified name of CtClass in case the .java file
             * generated multiple .class files (which are numbered within
             * javassist with "$"+digit
             */
            String qualifiedName = ctClass.getName();
            if (qualifiedName.matches(".+\\$\\d+$"))
                qualifiedName = qualifiedName.substring(0, qualifiedName.lastIndexOf("$"));

            /*
             * To match qualified names between javassist and javaparser, "/" and "$"
             * are replaced with "."
             */
            qualifiedName = qualifiedName.replaceAll("[/$]", ".");

            CompilationUnit sourceFile = srcMap.get(sourceFileName);

            if (sourceFile != null) {

                /*
                 * Visit the CompilationUnit with two visitors: one for ClassOrInterfaceDeclaration
                 * and the other one for EnumDeclaration. AnnotationDeclaration is not of interest.
                 */
                Map<String, ClassOrInterfaceDeclaration> classOrInterfaceDeclarationMap = new HashMap<>();
                ClassCollect classCollect = new ClassCollect();
                classCollect.visit(sourceFile, classOrInterfaceDeclarationMap);

                Map<String, EnumDeclaration> enumDeclarationMap = new HashMap<>();
                EnumCollect enumCollect = new EnumCollect();
                enumCollect.visit(sourceFile, enumDeclarationMap);

                if (classOrInterfaceDeclarationMap.containsKey(qualifiedName)) {
                    ClassOrInterfaceDeclaration targetClass = classOrInterfaceDeclarationMap.get(qualifiedName);

                    GenericDecompilationRecord<String, String> predecessor = new GenericDecompilationRecord<>(entry.getKey(), sourceFileName, decompilationRecord);
                    results.add(new GenericDecompilationRecord<>(ctClass, targetClass, predecessor));
                } else if (enumDeclarationMap.containsKey(qualifiedName)) {
                    EnumDeclaration targetEnum = enumDeclarationMap.get(qualifiedName);

                    GenericDecompilationRecord<String, String> predecessor = new GenericDecompilationRecord<>(entry.getKey(), sourceFileName, decompilationRecord);
                    results.add(new GenericDecompilationRecord<>(ctClass, targetEnum, predecessor));
                }
            }
        }

        return results.stream();
    }

    private static class ClassCollect extends VoidVisitorAdapter<Map<String, ClassOrInterfaceDeclaration>> {

        @Override
        public void visit(ClassOrInterfaceDeclaration cd, Map<String, ClassOrInterfaceDeclaration> collector) {
            super.visit(cd, collector);
            if (cd.getFullyQualifiedName().isPresent())
                collector.put(cd.getFullyQualifiedName().get(), cd);
        }
    }

    private static class EnumCollect extends VoidVisitorAdapter<Map<String, EnumDeclaration>> {

        @Override
        public void visit(EnumDeclaration ed, Map<String, EnumDeclaration> collector) {
            super.visit(ed, collector);
            if (ed.getFullyQualifiedName().isPresent())
                collector.put(ed.getFullyQualifiedName().get(), ed);
        }
    }
}
