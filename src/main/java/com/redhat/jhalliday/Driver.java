package com.redhat.jhalliday;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;

import com.redhat.jhalliday.impl.*;
import com.redhat.jhalliday.impl.MethodAssociatingRecordTransformer;

import com.redhat.jhalliday.impl.fernflower.CLIFernFlower;
import com.redhat.jhalliday.impl.fernflower.OriginalFernFlower;
import com.redhat.jhalliday.impl.javaparser.*;
import com.redhat.jhalliday.impl.javassist.JavassistFunctions;

import javassist.CtClass;
import javassist.CtMethod;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class Driver {

    private static final File LOW_FILENAME = new File("./bytecode.output");
    private static final File HIGH_FILENAME = new File("./references.output");
    private static final File HIGH_DEC_FILENAME = new File("./candidates.output");

    private static boolean USE_DECOMPILER = true;

    public static void main(String[] args) {

        File[] fileArray = {LOW_FILENAME, HIGH_FILENAME, HIGH_DEC_FILENAME};

        for(File file : fileArray) {
            if (file.exists()) {
                file.delete();
            }
        }

        WritePairsToFile<CtMethod> writePairsToFile = USE_DECOMPILER ?
                new WritePairsToFile<>(LOW_FILENAME, HIGH_FILENAME, HIGH_DEC_FILENAME) :
                new WritePairsToFile<>(LOW_FILENAME, HIGH_FILENAME);

        long start = System.currentTimeMillis();

        /*
         * We start with a pair of directories, one containing binary (.class) jar files
         * and the other containing the corresponding source (.java) jar files.
         * To populate these directories from maven central, see gather-sample-corpus.xml
         */
        DecompilationRecord<File, File> dirRecord = USE_DECOMPILER ?
                new GenericDecompilationRecord<>(
                        new File("./data/binjars"), new File("./data/srcjars"), new File("./data/decjars"), null) :
                new GenericDecompilationRecord<>(
                        new File("./data/binjars"), new File("./data/srcjars"));

        /*
         * Conversion step to change the directories into jar files pairs using name matching
         */

        Decompiler<File,File> decompiler;
        try {
            decompiler = new OriginalFernFlower(dirRecord.getHighLevelDecompiled());
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            decompiler = new CLIFernFlower(dirRecord.getHighLevelDecompiled());
        }

        DirectoryToJarsRecordTransformer dir2jarsTransformer = USE_DECOMPILER ?
                //new DirectoryToJarsRecordTransformer(new CLIFernFlower(dirRecord.getHighLevelDecompiled())) :
                new DirectoryToJarsRecordTransformer(decompiler) :
                new DirectoryToJarsRecordTransformer();

        List<DecompilationRecord<File, File>> jarRecords = dir2jarsTransformer.apply(dirRecord).collect(Collectors.toList());

//        CreatePairsFromMainFolder createPairsFromMainFolder = new CreatePairsFromMainFolder("binjars", "srcjars");
//        List<DecompilationRecord<File, File>> jarRecords = createPairsFromMainFolder.apply(new File("./lib/")).collect(Collectors.toList());

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

        JarProcessor<CtClass, CtMethod, DictionaryExtractionRecordTransformer<CtMethod>> jarProcessor = new JarProcessor<>(
                new ClassWrapperCreationTransformerFunction<>(
                        JavassistFunctions.classCreationFunction,
                        JavassistFunctions.classWrappingFunction),
                new MethodAssociatingRecordTransformer<>(
                        JavassistFunctions.classShreddingFunction,
                        JavassistFunctions.methodWrappingFunction,
                        new CompilationUnitToMethodDeclarationsTransformerFunction(),
                        JavaParserFunctions.methodWrappingFunction),
                new FinalWrapperRecordTransformer<>(
                        JavassistFunctions.finalMethodWrapperFunction,
                        JavaParserFunctions.finalMethodWrapperFunction),
                new DictionaryExtractionRecordTransformer<>()
        );

        int files = 0;
        int methods = 0;
        int filteredMethods = 0;

        Set<String> lowLevelDictionary = new HashSet<>();
        Set<String> highLevelDictionary = new HashSet<>();

        float tempPasses = 0;
        float tempFails = 0;
        int count = 0;

        for (DecompilationRecord<File, File> decompilationRecord : jarRecords) {

            System.out.printf("%d: %s\n", count + 1, decompilationRecord.getLowLevelRepresentation().getName());

            long localStart = System.currentTimeMillis();

            List<DecompilationRecord<ClassWrapper<CtClass>, CompilationUnit>> filePairs =
                    jarProcessor.associateFiles(decompilationRecord);
            files += filePairs.size();

            List<DecompilationRecord<CtMethod, MethodDeclaration>> methodRecords =
                    jarProcessor.associateMethods(filePairs);
            methods += methodRecords.size();

            List<DecompilationRecord<FinalLowLevelMethodWrapper<CtMethod>, FinalHighLevelMethodWrapper>> finalWrappedMethods =
                    jarProcessor.finalWrapper(methodRecords);

            filteredMethods += finalWrappedMethods.size();

            for (DecompilationRecord<FinalLowLevelMethodWrapper<CtMethod>, FinalHighLevelMethodWrapper> record : finalWrappedMethods) {
                lowLevelDictionary.addAll(Arrays.asList(record.getLowLevelRepresentation().getMethodBody().split(" ")));
                highLevelDictionary.addAll(Arrays.asList(record.getHighLevelRepresentation().getMethodBody().split(" ")));
            }

//            List<DecompilationRecordWithDic<FinalLowLevelMethodWrapper<CtMethod>, FinalHighLevelMethodWrapper, Map<String, String>>> finalResults =
//                    jarProcessor.dictionaryExtraction(finalWrappedMethods);

            finalWrappedMethods.forEach(writePairsToFile);

//            System.out.printf("Successful resolutions: %.0f\n", PrettyPrinterMod.passes - tempPasses);
//            System.out.printf("Failed resolutions: %.0f\n", PrettyPrinterMod.fails - tempFails);
//            float percentage = 100f * (PrettyPrinterMod.passes - tempPasses)/((PrettyPrinterMod.passes - tempPasses) + (PrettyPrinterMod.fails - tempFails));
//            System.out.printf("Passes rate: %.2f%%\n", percentage);
//            long localEnd = System.currentTimeMillis();
//            System.out.printf("Runtime %d ms\n", localEnd-localStart);
//
//            System.out.println();
//
//            tempFails = PrettyPrinterMod.fails;
//            tempPasses = PrettyPrinterMod.passes;
//
            count++;

        }
//
//        System.out.printf("Overall successful resolutions: %.0f\n", PrettyPrinterMod.passes);
//        System.out.printf("Overall failed resolutions: %.0f\n", PrettyPrinterMod.fails);

        System.out.printf("Processed %d jar file pairs, yielding %d file pairs\n", jarRecords.size(), files);
        System.out.printf("Found %d method pairs\n", methods);
        System.out.printf("Filtered %d method pairs\n", filteredMethods);

        System.out.printf("The low level dictionary is composed by %d words\n", lowLevelDictionary.size());
        System.out.printf("The high level dictionary is composed by %d words\n", highLevelDictionary.size());

        long end = System.currentTimeMillis();
        System.out.printf("Runtime %d ms", end - start);
    }
}
