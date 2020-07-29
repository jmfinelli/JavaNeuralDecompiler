package com.redhat.jhalliday;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;

import com.redhat.jhalliday.impl.*;
import com.redhat.jhalliday.impl.MethodAssociatingRecordTransformer;

import com.redhat.jhalliday.impl.fernflower.OriginalFernFlower;
import com.redhat.jhalliday.impl.javaparser.*;
import com.redhat.jhalliday.impl.javaparser.extractors.HighLevelBodyExtractorWithLineNumber;
import com.redhat.jhalliday.impl.javaparser.extractors.HighLevelBodyExtractorWithVisitor;
import com.redhat.jhalliday.impl.javassist.CountCFGs;
import com.redhat.jhalliday.impl.javassist.FilteringBasedOnCFGs;
import com.redhat.jhalliday.impl.javassist.JavassistFunctions;

import com.redhat.jhalliday.impl.javassist.extractors.*;
import javassist.CtClass;
import javassist.CtMethod;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class Driver {

    //////////////////////////////////////////
    //////////////// SWITCHES ////////////////
    //////////////////////////////////////////

    private static boolean USE_DECOMPILER = false;

    //////////////////////////////////////////
    //////////////////////////////////////////
    //////////////////////////////////////////

    private static final File LOW_FILENAME = new File("./bytecode.output");
    private static final File HIGH_FILENAME = new File("./references.output");
    private static final File HIGH_DEC_FILENAME = new File("./candidates.output");

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

        Decompiler<File,File> decompiler = null;
        if (USE_DECOMPILER) {
//            decompiler = new CLIFernFlower(dirRecord.getHighLevelDecompiled());
            try {
                decompiler = new OriginalFernFlower(dirRecord.getHighLevelDecompiled());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        DirectoryToJarsRecordTransformer dir2jarsTransformer = USE_DECOMPILER ?
                new DirectoryToJarsRecordTransformer(decompiler) :
                new DirectoryToJarsRecordTransformer();

        List<DecompilationRecord<File, File>> jarRecords = dir2jarsTransformer.apply(dirRecord).collect(Collectors.toList());

        CountCFGs BasicBlockCounter = new CountCFGs();

//        CreatePairsFromMainFolder createPairsFromMainFolder = new CreatePairsFromMainFolder("binjars", "srcjars");
//        List<DecompilationRecord<File, File>> jarRecords = createPairsFromMainFolder.apply(new File("./lib/")).collect(Collectors.toList());

        JarProcessor<CtClass, CtMethod> jarProcessor = new JarProcessor<>(
                new ClassWrapperCreationTransformerFunction<>(
                        JavassistFunctions.classCreationFunction,
                        JavassistFunctions.classWrappingFunction),

                new MethodAssociatingRecordTransformer<>(
                        JavassistFunctions.classShreddingFunction,
                        JavassistFunctions.methodWrappingFunction,
                        new CompilationUnitToMethodDeclarationsTransformerFunction(),
                        JavaParserFunctions.methodWrappingFunction),

                new BodyJuicerRecordTransformer<>(
                        // Low-Level Info Extractor
                        //new IdentityInfoExtractor(),
                        //new LowLevelInfoExtractor(),
                        new CtMethodInfoExtractor(),

                        // Low-Level Body Extractor
                        //new OriginalLowLevelPrinter(),
                        //new OriginalLowLevelBodyExtractorWithoutIndex(),
                        new LowLevelBodyExtractor(),

                        // High-Level Body Extractor
                        new HighLevelBodyExtractorWithVisitor()),

//                new BlockJuicerRecordTransformer<>(
//                        // Low-Level Info Extractor
//                        //new IdentityInfoExtractor(),
//                        //new LowLevelInfoExtractor(),
//                        new CtMethodInfoExtractor(),
//
//                        // Low-Level Block Extractor
//                        new CFGBlockExtractor(new LowLevelBodyExtractor()),
//                        // High-Level Body Extractor
//                        new HighLevelBodyExtractorWithLineNumber()),

                new ArrayList<>() {{
                    //add(new IdentityRecordTransformer<>());
                    //add(new FilterDuplicatesOutRecordTransformer());
                    //add(BasicBlockCounter);
                    add(new FilteringBasedOnCFGs(3));
                }});

        int files = 0;
        int methods = 0;
        int filteredMethods = 0;

        Set<String> lowLevelDictionary = new HashSet<>();
        Set<String> highLevelDictionary = new HashSet<>();

        int count = 0;

        for (DecompilationRecord<File, File> decompilationRecord : jarRecords) {

            System.out.printf("%d: %s\n", count + 1, decompilationRecord.getLowLevelRepresentation().getName());

            List<DecompilationRecord<ClassWrapper<CtClass>, CompilationUnit>> filePairs =
                    jarProcessor.associateFiles(decompilationRecord);
            files += filePairs.size();

            List<DecompilationRecord<CtMethod, MethodDeclaration>> methodRecords =
                    jarProcessor.associateMethods(filePairs);
            methods += methodRecords.size();

            List<DecompilationRecord<MethodJuice<CtMethod>, MethodJuice<MethodDeclaration>>> juicedMethods =
                    jarProcessor.juicer(methodRecords);

            for (DecompilationRecord<MethodJuice<CtMethod>, MethodJuice<MethodDeclaration>> record : juicedMethods) {
                lowLevelDictionary.addAll(Arrays.asList(record.getLowLevelRepresentation().getBody().split(" ")));
                highLevelDictionary.addAll(Arrays.asList(record.getHighLevelRepresentation().getBody().split(" ")));
            }

            List<DecompilationRecord<MethodJuice<CtMethod>, MethodJuice<MethodDeclaration>>> finalResults =
                    jarProcessor.filterMethods(juicedMethods);

            // Using FilterDuplicatesOutRecordTransformer here, results in the removal of duplicates
            // within the scope of each library. Duplicates still exist between libraries,
            // which is useful when training the model.
            FilterDuplicatesOutRecordTransformer<CtMethod> filterDuplicates = new FilterDuplicatesOutRecordTransformer();
            finalResults = finalResults.stream().flatMap(filterDuplicates).collect(Collectors.toList());

            filteredMethods += finalResults.size();

            finalResults.forEach(writePairsToFile);

            count++;

        }

        for (Map.Entry<Integer, Integer> entry : BasicBlockCounter.getBasicBlockMap().entrySet()) {
            System.out.printf("Number of Basic Blocks: %d; Number of methods: %d\n", entry.getKey(), entry.getValue());
        }

        System.out.printf("Processed %d jar file pairs, yielding %d file pairs\n", jarRecords.size(), files);
        System.out.printf("Found %d method pairs\n", methods);
        System.out.printf("Filtered %d method pairs\n", filteredMethods);

        System.out.printf("The low level dictionary is composed by %d words\n", lowLevelDictionary.size());
        System.out.printf("The high level dictionary is composed by %d words\n", highLevelDictionary.size());

        long end = System.currentTimeMillis();
        System.out.printf("Runtime %d ms", end - start);
    }
}
