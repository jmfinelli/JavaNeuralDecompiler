package ncl.ac.uk.transformation.impl.asm;

import ncl.ac.uk.transformation.DecompilationRecord;
import ncl.ac.uk.transformation.impl.GenericDecompilationRecord;
import ncl.ac.uk.transformation.RecordTransformer;

import org.objectweb.asm.tree.ClassNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class FileNameAssociatingRecordTransformer implements RecordTransformer<Map<String, ClassNode>, Map<String, byte[]>, ClassNode, byte[]> {

    @Override
    public Stream<DecompilationRecord<ClassNode, byte[]>> apply(DecompilationRecord<Map<String, ClassNode>, Map<String, byte[]>> decompilationRecord) {

        Map<String, ClassNode> binMap = decompilationRecord.getLowLevelRepresentation();
        Map<String, byte[]> srcMap = decompilationRecord.getHighLevelRepresentation();

        List<DecompilationRecord<ClassNode, byte[]>> results = new ArrayList<>();

        for (Map.Entry<String, ClassNode> entry : binMap.entrySet()) {
            String path = entry.getKey();
            path = path.substring(0, path.lastIndexOf('/') + 1);
            ClassNode classNode = entry.getValue();
            String sourceFileName = path + classNode.sourceFile;

            byte[] srcFile = srcMap.get(sourceFileName);
            if (srcFile != null) {
                results.add(new GenericDecompilationRecord<>(classNode, srcFile, decompilationRecord));
            }
        }

        return results.stream();
    }
}
