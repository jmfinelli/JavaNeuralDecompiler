package com.redhat.jhalliday.impl;

import com.redhat.jhalliday.DecompilationRecord;
import com.redhat.jhalliday.RecordTransformer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class FileNameAssociatingRecordTransformer<LOW_ITEM extends ClassWrapper<?>, HIGH_ITEM>
        implements RecordTransformer<Map<String, LOW_ITEM>, Map<String, HIGH_ITEM>, LOW_ITEM, HIGH_ITEM> {

    @Override
    public Stream<DecompilationRecord<LOW_ITEM, HIGH_ITEM>> apply(
            DecompilationRecord<Map<String, LOW_ITEM>, Map<String, HIGH_ITEM>> decompilationRecord) {

        Map<String, LOW_ITEM> binMap = decompilationRecord.getLowLevelRepresentation();
        Map<String, HIGH_ITEM> srcMap = decompilationRecord.getHighLevelRepresentation();
        Map<String, HIGH_ITEM> decMap = decompilationRecord.getHighLevelDecompiled();

        List<DecompilationRecord<LOW_ITEM, HIGH_ITEM>> results = new ArrayList<>();

        for (Map.Entry<String, LOW_ITEM> entry : binMap.entrySet()) {

            LOW_ITEM lowItem = entry.getValue();
            if (lowItem.getSourceFileName() == null) {
                continue;
            }

            String prefix = entry.getKey();
            prefix = prefix.substring(0, prefix.lastIndexOf('/') + 1);
            String sourceFileName = prefix + lowItem.getSourceFileName();
            HIGH_ITEM referenceSourceFile = srcMap.get(sourceFileName);
            if (referenceSourceFile != null) {
                // Checks if a decompilation record is available
                if (decMap == null) {
                    GenericDecompilationRecord<String, String> predecessor = new GenericDecompilationRecord<>(entry.getKey(), sourceFileName, decompilationRecord);
                    results.add(new GenericDecompilationRecord<>(lowItem, referenceSourceFile, predecessor));
                } else {
                    HIGH_ITEM decompilerSourceFile = decMap.get(sourceFileName);
                    if (decompilerSourceFile != null) {
                        GenericDecompilationRecord<String, String> predecessor = new GenericDecompilationRecord<>(entry.getKey(), sourceFileName, decompilationRecord);
                        results.add(new GenericDecompilationRecord<>(lowItem, referenceSourceFile, decompilerSourceFile, predecessor));
                    }
                }
            }
        }

        return results.stream();
    }
}
