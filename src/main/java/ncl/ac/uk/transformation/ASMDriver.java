package ncl.ac.uk.transformation;

import ncl.ac.uk.transformation.impl.*;
import ncl.ac.uk.transformation.impl.asm.*;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ASMDriver {

    public static void main(String[] args) throws Exception {

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
        CompositeRecordTransformer<Map<String, byte[]>, Map<String, byte[]>, Map<String, ClassNode>, Map<String, byte[]>>
                classNodeBuildingTransformer = new CompositeRecordTransformer<>(
                new ClassNodeCreationTransformerFunction(),
                new IdentityTransformerFunction<>()
        );
        List<DecompilationRecord<Map<String, ClassNode>, Map<String, byte[]>>> classNodes =
                rawFileContentRecords.stream().flatMap(classNodeBuildingTransformer).collect(Collectors.toList());

        /*
         * Now we can pair up the individual .java and .class files, using the source file name from the ClassNode
         */
        FileNameAssociatingRecordTransformer fileNameAssociatingRecordTransformer = new FileNameAssociatingRecordTransformer();
        List<DecompilationRecord<ClassNode, byte[]>> associatedFileRecords =
                classNodes.stream().flatMap(fileNameAssociatingRecordTransformer).collect(Collectors.toList());

        System.out.printf("Processed %d jar file pairs, yielding %d file pairs\n", jarRecords.size(), associatedFileRecords.size());

        /*
         * Decompose the ClassNode into a number of MethodModes, ignoring the ones that are not translatable
         */
        CompositeRecordTransformer<ClassNode, byte[], MethodNode, byte[]> methodSplitter = new CompositeRecordTransformer<>(
                new ClassNodeToMethodNodesTransformerFunction(),
                new IdentityTransformerFunction<>()
        );
        List<DecompilationRecord<MethodNode, byte[]>> methodRecords =
                associatedFileRecords.stream().flatMap(methodSplitter).collect(Collectors.toList());

        System.out.printf("Found %d potentially interesting binary methods in the .class files\n", methodRecords.size());
    }
}
