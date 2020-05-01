package com.redhat.jhalliday.impl.javassist;

import com.github.javaparser.ast.body.TypeDeclaration;
import com.redhat.jhalliday.DecompilationRecord;
import com.redhat.jhalliday.RecordTransformer;
import com.redhat.jhalliday.impl.ClassWrapper;
import com.redhat.jhalliday.impl.GenericDecompilationRecord;
import com.redhat.jhalliday.impl.javaparser.TypeDeclarationWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ClassesAssociatingRecordTransformer<LOW_ITEM extends ClassWrapper<?>, HIGH_ITEM extends ClassWrapper<?>> implements
        RecordTransformer<
                Map<String, LOW_ITEM>,
                Map<String, HIGH_ITEM>,
                LOW_ITEM,
                HIGH_ITEM> {

    @Override
    public Stream<DecompilationRecord<LOW_ITEM, HIGH_ITEM>> apply(DecompilationRecord<Map<String, LOW_ITEM>, Map<String, HIGH_ITEM>> decompilationRecord) {

        Map<String, LOW_ITEM> binMap = decompilationRecord.getLowLevelRepresentation();
        Map<String, HIGH_ITEM> srcMap = decompilationRecord.getHighLevelRepresentation();

        List<DecompilationRecord<LOW_ITEM, HIGH_ITEM>> results = new ArrayList<>();

        List<String> keys = binMap.keySet().stream().filter(x -> srcMap.keySet().contains(x)).collect(Collectors.toList());

        for(String key : keys) {
            GenericDecompilationRecord<String, String> predecessor = new GenericDecompilationRecord<>(key, srcMap.get(key).getSourceFileName(), decompilationRecord);
            results.add(new GenericDecompilationRecord<>(binMap.get(key), srcMap.get(key), predecessor));
        }

        return results.stream();
    }
}
