package com.redhat.jhalliday;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;

import com.redhat.jhalliday.impl.*;
import com.redhat.jhalliday.impl.MethodAssociatingRecordTransformer;

import com.redhat.jhalliday.impl.asm.AsmFunctions;
import com.redhat.jhalliday.impl.javaparser.*;
import com.redhat.jhalliday.impl.javassist.JavassistFunctions;

import javassist.CtClass;
import javassist.CtMethod;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public class Driver {

    public static void main(String[] args) throws Exception {

        long start = System.currentTimeMillis();

        /*
         * We start with a pair of directories, one containing binary (.class) jar files
         * and the other containing the corresponding source (.java) jar files.
         * To populate these directories from maven central, see gather-sample-corpus.xml
         */
        DecompilationRecord<File, File> dirRecord = new GenericDecompilationRecord<>(
                new File("../data/binjars"), new File("../data/srcjars"));

        /*
         * Conversion step to change the directories into jar files pairs using name matching
         */
        DirectoryToJarsRecordTransformer dir2jarsTransformer = new DirectoryToJarsRecordTransformer();
        List<DecompilationRecord<File, File>> jarRecords = dir2jarsTransformer.apply(dirRecord).collect(Collectors.toList());

//        JarProcessor<ClassNode, MethodNode> jarProcessor = new JarProcessor<>(
//                new ClassWrapperCreationTransformerFunction<>(
//                        AsmFunctions.classCreationFunction,
//                        AsmFunctions.classWrappingFunction),
//                new MethodAssociatingRecordTransformer<>(
//                        AsmFunctions.classShreddingFunction,
//                        AsmFunctions.methodWrappingFunction,
//                        new CompilationUnitToMethodDeclarationsTransformerFunction(),
//                        JavaParserFunctions.methodWrappingFunction)
//        );

        JarProcessor<CtClass,CtMethod> jarProcessor = new JarProcessor<>(
                new ClassWrapperCreationTransformerFunction<>(
                        JavassistFunctions.classCreationFunction,
                        JavassistFunctions.classWrappingFunction),
                new MethodAssociatingRecordTransformer<>(
                        JavassistFunctions.classShreddingFunction,
                        JavassistFunctions.methodWrappingFunction,
                        new CompilationUnitToMethodDeclarationsTransformerFunction(),
                        JavaParserFunctions.methodWrappingFunction)
        );

        int files = 0;
        int methods = 0;
        for (DecompilationRecord<File, File> decompilationRecord : jarRecords) {
            //System.out.println(decompilationRecord.getHighLevelRepresentation().getAbsolutePath());

            List<DecompilationRecord<ClassWrapper<CtClass>, CompilationUnit>> filePairs =
                    jarProcessor.associateFiles(decompilationRecord);
            files += filePairs.size();

            List<DecompilationRecord<CtMethod, MethodDeclaration>> methodRecords =
                    jarProcessor.associateMethods(filePairs);
            methods += methodRecords.size();
        }

        System.out.printf("Processed %d jar file pairs, yielding %d file pairs\n", jarRecords.size(), files);
        System.out.printf("Found %d method pairs\n", methods);

        long end = System.currentTimeMillis();
        System.out.printf("Runtime %d ms", end-start);
    }
}
