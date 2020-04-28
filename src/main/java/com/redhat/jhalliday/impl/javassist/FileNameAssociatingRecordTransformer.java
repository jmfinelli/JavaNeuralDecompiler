package com.redhat.jhalliday.impl.javassist;

import com.github.javaparser.ast.CompilationUnit;
import com.redhat.jhalliday.DecompilationRecord;
import javassist.CtClass;
import javassist.bytecode.SourceFileAttribute;
import com.redhat.jhalliday.RecordTransformer;
import com.redhat.jhalliday.impl.GenericDecompilationRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Deprecated
public class FileNameAssociatingRecordTransformer implements RecordTransformer<Map<String, CtClass>, Map<String, CompilationUnit>, CtClass, CompilationUnit> {

    @Override
    public Stream<DecompilationRecord<CtClass, CompilationUnit>> apply(DecompilationRecord<Map<String, CtClass>, Map<String, CompilationUnit>> decompilationRecord) {

        Map<String, CtClass> binMap = decompilationRecord.getLowLevelRepresentation();
        Map<String, CompilationUnit> srcMap = decompilationRecord.getHighLevelRepresentation();

        List<DecompilationRecord<CtClass, CompilationUnit>> results = new ArrayList<>();

        for (Map.Entry<String, CtClass> entry : binMap.entrySet()) {

            CtClass ctClass = entry.getValue();
            SourceFileAttribute attribute = (SourceFileAttribute) ctClass.getClassFile().getAttribute("SourceFile");
            if (attribute == null) {
                continue;
            }

            String prefix = entry.getKey();
            prefix = prefix.substring(0, prefix.lastIndexOf('/') + 1);
            String sourceFileName = prefix + attribute.getFileName();
            CompilationUnit sourceFile = srcMap.get(sourceFileName);
            if (sourceFile != null) {
                GenericDecompilationRecord<String, String> predecessor = new GenericDecompilationRecord<>(entry.getKey(), sourceFileName, decompilationRecord);
                results.add(new GenericDecompilationRecord<>(ctClass, sourceFile, predecessor));
            }
        }

        return results.stream();
    }
}
