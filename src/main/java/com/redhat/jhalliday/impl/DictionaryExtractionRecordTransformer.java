package com.redhat.jhalliday.impl;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.redhat.jhalliday.DecompilationRecord;
import com.redhat.jhalliday.DecompilationRecordWithDic;
import com.redhat.jhalliday.impl.javassist.ParameterExtractor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

public class DictionaryExtractionRecordTransformer<LOW_INPUT> implements Function<
        DecompilationRecord<LOW_INPUT, MethodDeclaration>,
        Stream<DecompilationRecordWithDic<List<String>, List<String>, Map<String, String>>>> {

    private static final String VAR_PREFIX = "VAR";

    private final Function<LOW_INPUT, FinalLowLevelMethodWrapper<LOW_INPUT>> lowMethodWrapper;
    //private final TransformerFunction<LOW_INPUT, String> lowMethodExtraction;
    private final Function<MethodDeclaration, FinalHighLevelMethodWrapper> highMethodWrapper;
    //private final TransformerFunction<MethodDeclaration, String> highMethodExtraction;

    public DictionaryExtractionRecordTransformer(
            Function<LOW_INPUT, FinalLowLevelMethodWrapper<LOW_INPUT>> lowMethodWrapper,
            //TransformerFunction<LOW_INPUT, String> lowMethodExtraction,
            Function<MethodDeclaration, FinalHighLevelMethodWrapper> highMethodWrapper) {
            //TransformerFunction<MethodDeclaration, String> highMethodExtraction) {
        this.lowMethodWrapper = lowMethodWrapper;
        //this.lowMethodExtraction = lowMethodExtraction;
        this.highMethodWrapper = highMethodWrapper;
        //this.highMethodExtraction = highMethodExtraction;
    }

    @Override
    public Stream<DecompilationRecordWithDic<List<String>, List<String>, Map<String, String>>> apply(DecompilationRecord<LOW_INPUT, MethodDeclaration> decompilationRecord) {

        Map<String, String> dictionary = new HashMap<>();

        FinalLowLevelMethodWrapper<LOW_INPUT> wrappedLowLevelMethod = lowMethodWrapper.apply(decompilationRecord.getLowLevelRepresentation());
        //List<String> lowMethodBodies = lowMethodExtraction.apply(decompilationRecord.getLowLevelRepresentation()).collect(Collectors.toList());
        FinalHighLevelMethodWrapper wrappedHighLevelMethod = highMethodWrapper.apply(decompilationRecord.getHighLevelRepresentation());
        //List<String> highMethodBodies = highMethodExtraction.apply(decompilationRecord.getHighLevelRepresentation()).collect(Collectors.toList());

        List<Integer> indexes = new ArrayList<>(wrappedLowLevelMethod.getLocalVariables().keySet());
        for (int i = 0; i < indexes.size(); i++) {
            Integer variableIndex = indexes.get(i);
            String variableName = wrappedLowLevelMethod.getLocalVariables().get(variableIndex);
            if (wrappedHighLevelMethod.getNameExpr().contains(variableName)) {
                String placeHolder = String.format("%s_%d", VAR_PREFIX, i);
                dictionary.putIfAbsent(ParameterExtractor.LOC_VAR_SYMBOL + variableIndex, placeHolder);
                wrappedLowLevelMethod.replaceStringInBody(ParameterExtractor.LOC_VAR_SYMBOL + variableIndex, placeHolder);
                wrappedHighLevelMethod.replaceStringInBody(variableName, placeHolder);
            }
        }

        return null;
    }
}
