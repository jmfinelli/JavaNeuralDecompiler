package ncl.ac.uk.transformation.impl;

import ncl.ac.uk.transformation.DecompilationRecord;
import ncl.ac.uk.transformation.RecordTransformer;
import ncl.ac.uk.transformation.TransformerFunction;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CompositeRecordTransformer<T_LOW, T_HIGH, R_LOW, R_HIGH> implements RecordTransformer<T_LOW, T_HIGH, R_LOW, R_HIGH> {

    private final TransformerFunction<T_LOW, R_LOW> lowTransform;
    private final TransformerFunction<T_HIGH, R_HIGH> highTransform;

    public CompositeRecordTransformer(TransformerFunction<T_LOW, R_LOW> lowTransform, TransformerFunction<T_HIGH, R_HIGH> highTransform) {
        this.lowTransform = lowTransform;
        this.highTransform = highTransform;
    }

    @Override
    public Stream<DecompilationRecord<R_LOW, R_HIGH>> apply(DecompilationRecord<T_LOW, T_HIGH> decompilationRecord) {

        Stream<R_LOW> rLowStream = lowTransform.apply(decompilationRecord.getLowLevelRepresentation());
        List<R_HIGH> rHighStream = highTransform.apply(decompilationRecord.getHighLevelRepresentation()).collect(Collectors.toList());

        List<DecompilationRecord<R_LOW, R_HIGH>> result = new ArrayList<>();

        rLowStream.forEach(r_low -> {
            rHighStream.forEach(r_high -> {
                result.add(new GenericDecompilationRecord<>(r_low, r_high, decompilationRecord));
            });
        });

        return result.stream();
    }
}
