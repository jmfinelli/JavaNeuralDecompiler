package com.redhat.jhalliday.impl;

import com.redhat.jhalliday.DecompilationRecord;
import com.redhat.jhalliday.RecordTransformer;
import com.redhat.jhalliday.TransformerFunction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ByteArrayRecordTransformer<R_LOW, R_HIGH> implements RecordTransformer<Map<String, byte[]>, Map<String, byte[]>, R_LOW, R_HIGH> {

    private final TransformerFunction<Map<String, byte[]>, R_LOW> lowTransform;
    private final TransformerFunction<Map<String, byte[]>, R_HIGH> highTransform;

    public ByteArrayRecordTransformer(TransformerFunction<Map<String, byte[]>, R_LOW> lowTransform,
                                      TransformerFunction<Map<String, byte[]>, R_HIGH> highTransform) {
        this.lowTransform = lowTransform;
        this.highTransform = highTransform;
    }

    @Override
    public Stream<DecompilationRecord<R_LOW, R_HIGH>> apply(DecompilationRecord<Map<String, byte[]>, Map<String, byte[]>> decompilationRecord) {

        Map<String, byte[]> lowLevelRecord = decompilationRecord.getLowLevelRepresentation();
        Map<String, byte[]> highLevelRecord = decompilationRecord.getHighLevelRepresentation();
        Map<String, byte[]> decompiledRecord = decompilationRecord.getHighLevelDecompiled();

//        if (decompiledRecord != null && highLevelRecord.size() != decompiledRecord.size()) {
//            // Retain only the keys in the highLevelRecord Map
//            decompiledRecord.keySet().retainAll(highLevelRecord.keySet());
//        }

        List<R_LOW> rLowList = lowTransform.apply(lowLevelRecord).collect(Collectors.toList());
        List<R_HIGH> rHighList = highTransform.apply(highLevelRecord).collect(Collectors.toList());
        List<R_HIGH> rDecompiledList = decompiledRecord != null ?
                highTransform.apply(decompiledRecord).collect(Collectors.toList()) :
                null;

        List<DecompilationRecord<R_LOW, R_HIGH>> result = new ArrayList<>();

        if (rDecompiledList == null) {
            rLowList.forEach(r_low -> {
                rHighList.forEach(r_high -> {
                    result.add(new GenericDecompilationRecord<>(r_low, r_high, decompilationRecord));
                });
            });
        } else if (!rDecompiledList.isEmpty()) {
            rLowList.forEach(r_low -> {
                rHighList.forEach(r_high -> {
                    rDecompiledList.forEach(r_decomp -> {
                        result.add(new GenericDecompilationRecord<>(r_low, r_high, r_decomp, decompilationRecord));
                    });
                });
            });
        }

        return result.stream();
    }
}
