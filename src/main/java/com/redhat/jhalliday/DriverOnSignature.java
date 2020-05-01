package com.redhat.jhalliday;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.redhat.jhalliday.impl.*;
import com.redhat.jhalliday.impl.javaparser.CompilationUnitToMethodDeclarationsTransformerFunction;
import com.redhat.jhalliday.impl.javaparser.JavaParserFunctions;
import com.redhat.jhalliday.impl.javaparser.TypeDeclarationToMethodDeclarationsTransformerFunction;
import com.redhat.jhalliday.impl.javassist.JavassistFunctions;
import javassist.CtClass;
import javassist.CtMethod;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public class DriverOnSignature {

    public static void main(String[] args) throws Exception {

        long start = System.currentTimeMillis();

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

        JarProcessorOnSignatures<CtClass,CtMethod> jarProcessor = new JarProcessorOnSignatures<>(
                new ClassWrapperTransformerFunction<>(
                        JavassistFunctions.classCreationFunction,
                        JavassistFunctions.classWrappingFunction),
                new MethodPairingRecordTransformer<>(
                        JavassistFunctions.classShreddingFunction,
                        JavassistFunctions.methodWrappingOnParametersFunction,
                        new TypeDeclarationToMethodDeclarationsTransformerFunction(),
                        JavaParserFunctions.methodWrappingOnParametersFunction)
        );

        int files = 0;
        int methods = 0;
        for (DecompilationRecord<File, File> decompilationRecord : jarRecords) {
            //System.out.println(decompilationRecord.getHighLevelRepresentation().getAbsolutePath());

            List<DecompilationRecord<ClassWrapper<CtClass>, ClassWrapper<TypeDeclaration>>> classPairs =
                    jarProcessor.associateFiles(decompilationRecord);
            files += classPairs.size();

            List<DecompilationRecord<CtMethod, MethodDeclaration>> methodRecords =
                    jarProcessor.associateMethods(classPairs);
            methods += methodRecords.size();
        }

        System.out.printf("Processed %d jar file pairs, yielding %d file pairs\n", jarRecords.size(), files);
        System.out.printf("Found %d method pairs\n", methods);

        long end = System.currentTimeMillis();
        System.out.printf("Runtime %d ms", end-start);
    }
}
