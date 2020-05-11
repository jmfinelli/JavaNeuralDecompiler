package com.redhat.jhalliday.impl;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.redhat.jhalliday.DecompilationRecord;
import com.redhat.jhalliday.DecompilationRecordWithDic;
import com.redhat.jhalliday.TransformerFunction;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

public class DictionaryExtractionRecordTransformer<LOW_INPUT> implements Function<
        DecompilationRecord<LOW_INPUT, MethodDeclaration>,
        Stream<DecompilationRecordWithDic<List<String>, List<String>, Map<String, String>>>> {

    private final Function<LOW_INPUT, FinalMethodWrapper<LOW_INPUT>> lowMethodWrapper;
    //private final TransformerFunction<LOW_INPUT, String> lowMethodExtraction;
    private final Function<MethodDeclaration, FinalMethodWrapper<MethodDeclaration>> highMethodWrapper;
    //private final TransformerFunction<MethodDeclaration, String> highMethodExtraction;

    public DictionaryExtractionRecordTransformer(
            Function<LOW_INPUT, FinalMethodWrapper<LOW_INPUT>> lowMethodWrapper,
            //TransformerFunction<LOW_INPUT, String> lowMethodExtraction,
            Function<MethodDeclaration, FinalMethodWrapper<MethodDeclaration>> highMethodWrapper) {
            //TransformerFunction<MethodDeclaration, String> highMethodExtraction) {
        this.lowMethodWrapper = lowMethodWrapper;
        //this.lowMethodExtraction = lowMethodExtraction;
        this.highMethodWrapper = highMethodWrapper;
        //this.highMethodExtraction = highMethodExtraction;
    }

    @Override
    public Stream<DecompilationRecordWithDic<List<String>, List<String>, Map<String, String>>> apply(DecompilationRecord<LOW_INPUT, MethodDeclaration> decompilationRecord) {

        FinalMethodWrapper<LOW_INPUT> wrappedLowLevelMethod = lowMethodWrapper.apply(decompilationRecord.getLowLevelRepresentation());
        //List<String> lowMethodBodies = lowMethodExtraction.apply(decompilationRecord.getLowLevelRepresentation()).collect(Collectors.toList());
        FinalMethodWrapper<MethodDeclaration> wrappedHighLevelMethod = highMethodWrapper.apply(decompilationRecord.getHighLevelRepresentation());
        //List<String> highMethodBodies = highMethodExtraction.apply(decompilationRecord.getHighLevelRepresentation()).collect(Collectors.toList());

        return null;
    }
}
