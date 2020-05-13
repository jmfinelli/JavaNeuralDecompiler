package com.redhat.jhalliday.impl;

import com.redhat.jhalliday.DecompilationRecord;
import com.redhat.jhalliday.RecordTransformer;
import com.redhat.jhalliday.TransformerFunction;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CompositeBiFunctionTransformer<T_LOW, T_HIGH, R_LOW, R_HIGH> implements RecordTransformer<T_LOW, T_HIGH, R_LOW, R_HIGH> {

    private final TransformerFunction<T_LOW, R_LOW> lowTransform;
    private final BiFunction<T_HIGH, File, Stream<R_HIGH>> highTransform;

    public CompositeBiFunctionTransformer(TransformerFunction<T_LOW, R_LOW> lowTransform,
                                          BiFunction<T_HIGH, File, Stream<R_HIGH>> highTransform) {
        this.lowTransform = lowTransform;
        this.highTransform = highTransform;
    }

    @Override
    public Stream<DecompilationRecord<R_LOW, R_HIGH>> apply(DecompilationRecord<T_LOW, T_HIGH> decompilationRecord) {

        DecompilationRecord previous = decompilationRecord.getPredecessor();
        while (!(previous.getHighLevelRepresentation() instanceof File)){ previous = previous.getPredecessor(); }

        Stream<R_LOW> rLowStream = lowTransform.apply(decompilationRecord.getLowLevelRepresentation());
        List<R_HIGH> rHighStream = highTransform.apply(
                decompilationRecord.getHighLevelRepresentation(),
                (File)previous.getHighLevelRepresentation())
                .collect(Collectors.toList());

        List<DecompilationRecord<R_LOW, R_HIGH>> result = new ArrayList<>();

        rLowStream.forEach(r_low -> {
            rHighStream.forEach(r_high -> {
                result.add(new GenericDecompilationRecord<>(r_low, r_high, decompilationRecord));
            });
        });

        return result.stream();
    }
}
