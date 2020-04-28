package com.redhat.jhalliday;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.redhat.jhalliday.impl.*;
import com.redhat.jhalliday.impl.javaparser.CompilationUnitCreationTransformerFunction;
import com.redhat.jhalliday.impl.javaparser.MethodDeclarationToTextTransformerFunction;
import javassist.CtClass;
import javassist.CtMethod;

import com.redhat.jhalliday.impl.javassist.CtClassCreationTransformerFunction;
import com.redhat.jhalliday.impl.javassist.CtMethodToTextTransformerFunction;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JavassistDriver {

    public static void main(String[] args) {

        /*
         * We start with a pair of directories, one containing binary (.class) jar files
         * and the other containing the corresponding source (.java) jar files.
         * To populate these directories from maven central, see gather-sample-corpus.xml
         */
        DecompilationRecord<File, File> dirRecord = new GenericDecompilationRecord<>(
                new File("./data/binjars"), new File("./data/srcjars"));

        /*
         * Conversion step to change the directories into jar files pairs using name matching
         */
        DirectoryToJarsRecordTransformer dir2jarsTransformer = new DirectoryToJarsRecordTransformer();
        List<DecompilationRecord<File, File>> jarRecords = dir2jarsTransformer.apply(dirRecord).collect(Collectors.toList());

        /*
         * jar pairs are a good start, but next we need to extract the contents.
         * Note we extract to memory not disk, so it's byte[] not File result
         */
        JarContentRecordTransformer jarContentTransformer = new JarContentRecordTransformer(
                new JarContentTransformerFunction(s -> s.endsWith(".class")),
                new JarContentTransformerFunction(s -> s.endsWith(".java"))
        );
        List<DecompilationRecord<Map<String, byte[]>, Map<String, byte[]>>> rawFileContentRecords =
                jarRecords.stream().flatMap(jarContentTransformer).collect(Collectors.toList());

        /*
         * From this point we are decoupled from the filesystem.
         * .class files and related .java files are converted, respectively, to CtClass
         * and CompilationUnit.
         */
        CompositeRecordTransformer<Map<String, byte[]>, Map<String, byte[]>, Map<String, CtClass>, Map<String, CompilationUnit>>
                CtClassBuildingTransformer = new CompositeRecordTransformer<>(
                new CtClassCreationTransformerFunction(),
                new CompilationUnitCreationTransformerFunction()
        );
        List<DecompilationRecord<Map<String, CtClass>, Map<String, CompilationUnit>>> ctClasses =
                rawFileContentRecords.stream().flatMap(CtClassBuildingTransformer).collect(Collectors.toList());

        /*
         * Now we can pair up individual Classes and Enums with related .class files.
         */
        ClassAssociatingRecordTransformer classAssociatingRecordTransformer = new ClassAssociatingRecordTransformer();
        List<DecompilationRecord<CtClass, TypeDeclaration>> associatedClassRecords =
                ctClasses.stream().flatMap(classAssociatingRecordTransformer).collect(Collectors.toList());

        System.out.printf("Processed %d jar file pairs, yielding %d file pairs\n", jarRecords.size(), associatedClassRecords.size());

        /*
         * Decompose the CtClass into a number of MethodModes, ignoring the ones that are not translatable
         */
        PairMethodsBuildingRecordTransformer pairBuldingRecordTransformer = new PairMethodsBuildingRecordTransformer();
        List<DecompilationRecord<CtMethod, MethodDeclaration>> methodsRecords =
                associatedClassRecords.stream().flatMap(pairBuldingRecordTransformer).collect(Collectors.toList());

        System.out.printf("Found %d potentially interesting binary methods in the .class files\n", methodsRecords.size());

        /*
         * Convert each CtMethod into a list of string tokens representing opcodes, operands, etc.
         * One List<String> per CtMethod, containing one String per instruction, having space-separated tokens.
         */
        CompositeRecordTransformer<CtMethod, MethodDeclaration, List<String>, List<String>> methodPrinter = new CompositeRecordTransformer<>(
                new CtMethodToTextTransformerFunction(),
                new MethodDeclarationToTextTransformerFunction()
        );
        List<DecompilationRecord<List<String>, List<String>>> textRecords = methodsRecords.stream().flatMap(methodPrinter).collect(Collectors.toList());

        Integer totalTokens = textRecords.stream() .map(listDecompilationRecord -> listDecompilationRecord.getLowLevelRepresentation().size()) .reduce(0, Integer::sum);

        System.out.printf("Total instructions %d, average instructions per method %f", totalTokens, totalTokens * 1.0 / textRecords.size());
    }
}
