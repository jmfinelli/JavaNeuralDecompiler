package com.redhat.jhalliday.impl;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.redhat.jhalliday.DecompilationRecord;

import java.util.function.Function;
import java.util.stream.Stream;

public class FinalWrapperRecordTransformer<LOW_INPUT> implements Function<
        DecompilationRecord<LOW_INPUT, MethodDeclaration>,
        Stream<DecompilationRecord<FinalLowLevelMethodWrapper<LOW_INPUT>, FinalHighLevelMethodWrapper>>> {

    private final Function<LOW_INPUT, FinalLowLevelMethodWrapper<LOW_INPUT>> lowMethodWrapper;
    private final Function<MethodDeclaration, FinalHighLevelMethodWrapper> highMethodWrapper;

    public FinalWrapperRecordTransformer(
            Function<LOW_INPUT, FinalLowLevelMethodWrapper<LOW_INPUT>> lowMethodWrapper,
            Function<MethodDeclaration, FinalHighLevelMethodWrapper> highMethodWrapper) {
        this.lowMethodWrapper = lowMethodWrapper;
        this.highMethodWrapper = highMethodWrapper;
    }

    @Override
    public Stream<DecompilationRecord<FinalLowLevelMethodWrapper<LOW_INPUT>, FinalHighLevelMethodWrapper>> apply(DecompilationRecord<LOW_INPUT, MethodDeclaration> decompilationRecord) {

        FinalLowLevelMethodWrapper<LOW_INPUT> wrappedLowLevelMethod = lowMethodWrapper.apply(decompilationRecord.getLowLevelRepresentation());
        FinalHighLevelMethodWrapper wrappedHighLevelMethod = highMethodWrapper.apply(decompilationRecord.getHighLevelRepresentation());
        FinalHighLevelMethodWrapper wrappedHighLevelDecompiledMethod = highMethodWrapper.apply(decompilationRecord.getHighLevelDecompiled());

        if (wrappedLowLevelMethod.getMethodBody().split(" ").length < 50) {
            DecompilationRecord<FinalLowLevelMethodWrapper<LOW_INPUT>, FinalHighLevelMethodWrapper> finalWrappedDecompilationRecord;

            if (wrappedHighLevelDecompiledMethod == null) {
                finalWrappedDecompilationRecord = new
                        GenericDecompilationRecord<>(wrappedLowLevelMethod, wrappedHighLevelMethod, decompilationRecord);
            } else {
                finalWrappedDecompilationRecord = new
                        GenericDecompilationRecord<>(wrappedLowLevelMethod, wrappedHighLevelMethod, wrappedHighLevelDecompiledMethod, decompilationRecord);
            }

            return Stream.of(finalWrappedDecompilationRecord);
        }

        return Stream.empty();
    }
}
