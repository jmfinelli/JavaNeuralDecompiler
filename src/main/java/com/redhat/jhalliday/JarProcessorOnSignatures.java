package com.redhat.jhalliday;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.redhat.jhalliday.impl.*;
import com.redhat.jhalliday.impl.javaparser.CompilationUnitCreationTransformerFunction;
import com.redhat.jhalliday.impl.javaparser.TypeDeclarationExtractionTransformerFunction;
import com.redhat.jhalliday.impl.javassist.ClassesAssociatingRecordTransformer;
import com.redhat.jhalliday.impl.javassist.CtClassAssociatingRecordTransformer;
import javassist.CtClass;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JarProcessorOnSignatures<LOW_AGGREGATE, LOW_ITEM> {

    private final TransformerFunction<Map<String, byte[]>, Map<String, ClassWrapper<LOW_AGGREGATE>>> classParsingFunction;

    private final MethodPairingRecordTransformer<ClassWrapper<LOW_AGGREGATE>, ClassWrapper<TypeDeclaration>, LOW_ITEM, MethodDeclaration> methodAssociatingRecordTransformer;

    public JarProcessorOnSignatures(
            TransformerFunction<
                    Map<String, byte[]>,
                    Map<String, ClassWrapper<LOW_AGGREGATE>>>
                    classParsingFunction,
            MethodPairingRecordTransformer<
                    ClassWrapper<LOW_AGGREGATE>,
                    ClassWrapper<TypeDeclaration>,
                    LOW_ITEM,
                    MethodDeclaration>
                    methodAssociatingRecordTransformer) {

        this.classParsingFunction = classParsingFunction;
        this.methodAssociatingRecordTransformer = methodAssociatingRecordTransformer;
    }

    public List<DecompilationRecord<ClassWrapper<LOW_AGGREGATE>, ClassWrapper<TypeDeclaration>>> associateFiles(
            DecompilationRecord<File, File> decompilationRecord) {

        List<DecompilationRecord<File, File>> jarRecords = List.of(decompilationRecord);

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
         * From this point we are decoupled from the filesystem
         * Next we need to convert the raw bytes to something more useful
         * Start with the low level (.class) side
         */
        CompositeRecordTransformer<
                // INPUT
                Map<String, byte[]>,
                Map<String, byte[]>,
                // OUTPUT
                Map<String, ClassWrapper<LOW_AGGREGATE>>,
                Map<String, byte[]>>
                ctClassBuildingTransformer = new CompositeRecordTransformer<>(
                        classParsingFunction, new IdentityTransformerFunction<>());

        List<DecompilationRecord<Map<String, ClassWrapper<LOW_AGGREGATE>>, Map<String, byte[]>>> semiParsed =
                rawFileContentRecords.stream().flatMap(ctClassBuildingTransformer).collect(Collectors.toList());

        /*
         * Now the high level side.
         * Note that this ordering will parse some .java files unnecessarily, as they won't have matching .class files in the next step.
         * However, it's cheaper than re-parsing the same .java file multiple times if it contains multiple classes.
         */
        CompositeRecordTransformer<
                // INPUT
                Map<String, ClassWrapper<LOW_AGGREGATE>>,
                Map<String, byte[]>,
                // OUTPUT
                Map<String, ClassWrapper<LOW_AGGREGATE>>,
                Map<String, ClassWrapper<TypeDeclaration>>>
                compilationUnitBuildingTransformer = new CompositeRecordTransformer<>(
                new IdentityTransformerFunction<>(),
                new TypeDeclarationExtractionTransformerFunction()
        );
        List<DecompilationRecord<Map<String, ClassWrapper<LOW_AGGREGATE>>, Map<String, ClassWrapper<TypeDeclaration>>>> fullyParsed =
                semiParsed.stream().flatMap(compilationUnitBuildingTransformer).collect(Collectors.toList());

        /*
         * Now we can pair up individual Classes and Enums with related .class files.
         */
        ClassesAssociatingRecordTransformer<ClassWrapper<LOW_AGGREGATE>, ClassWrapper<TypeDeclaration>> classesAssociatingRecordTransformer =
                new ClassesAssociatingRecordTransformer();

        List<DecompilationRecord<ClassWrapper<LOW_AGGREGATE>, ClassWrapper<TypeDeclaration>>> associatedClassRecords =
                fullyParsed.stream().flatMap(classesAssociatingRecordTransformer).collect(Collectors.toList());

        return associatedClassRecords;
    }

    /*
     * Decompose the file pairs into a number of Method pairs, ignoring the ones that are not translatable
     */
    public List<DecompilationRecord<LOW_ITEM, MethodDeclaration>> associateMethods(
            List<DecompilationRecord<ClassWrapper<LOW_AGGREGATE>, ClassWrapper<TypeDeclaration>>> associatedFileRecords) {

        List<DecompilationRecord<LOW_ITEM, MethodDeclaration>> methodRecords =
                associatedFileRecords.stream().flatMap(methodAssociatingRecordTransformer).collect(Collectors.toList());

        return methodRecords;
    }
}
