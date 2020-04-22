package com.redhat.jhalliday.impl.javassist;

import com.redhat.jhalliday.DecompilationRecord;
import javassist.CtClass;
import javassist.bytecode.SourceFileAttribute;
import com.redhat.jhalliday.RecordTransformer;
import com.redhat.jhalliday.impl.GenericDecompilationRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class FileNameAssociatingRecordTransformer implements RecordTransformer<Map<String, CtClass>, Map<String, byte[]>, CtClass, byte[]> {

    @Override
    public Stream<DecompilationRecord<CtClass, byte[]>> apply(DecompilationRecord<Map<String, CtClass>, Map<String, byte[]>> decompilationRecord) {

        Map<String, CtClass> binMap = decompilationRecord.getLowLevelRepresentation();
        Map<String, byte[]> srcMap = decompilationRecord.getHighLevelRepresentation();

        List<DecompilationRecord<CtClass, byte[]>> results = new ArrayList<>();

        for (Map.Entry<String, CtClass> entry : binMap.entrySet()) {

            CtClass ctClass = entry.getValue();
            SourceFileAttribute attribute = (SourceFileAttribute) ctClass.getClassFile().getAttribute("SourceFile");
            if (attribute == null) {
                continue;
            }

            String prefix = entry.getKey();
            prefix = prefix.substring(0, prefix.lastIndexOf('/') + 1);
            String sourceFileName = prefix + attribute.getFileName();
            byte[] sourceFile = srcMap.get(sourceFileName);
            if (sourceFile != null) {
                GenericDecompilationRecord<String, String> predecessor = new GenericDecompilationRecord<>(entry.getKey(), sourceFileName, decompilationRecord);
                results.add(new GenericDecompilationRecord<>(ctClass, sourceFile, predecessor));
            }
        }

        return results.stream();
    }
}
