package com.redhat.jhalliday.impl;

import com.redhat.jhalliday.DecompilationRecord;
import com.redhat.jhalliday.DecompilationRecordWithDic;
import com.redhat.jhalliday.impl.javassist.printers.LowLevelPrinter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

public class DictionaryExtractionRecordTransformer<LOW_INPUT> implements Function<
        DecompilationRecord<FinalLowLevelMethodWrapper<LOW_INPUT>, FinalHighLevelMethodWrapper>,
        Stream<DecompilationRecordWithDic<FinalLowLevelMethodWrapper<LOW_INPUT>, FinalHighLevelMethodWrapper, Map<String, String>>>> {

    private static final String VAR_PREFIX = "VAR";
    private static final String METHOD_PREFIX = "MET";

    @Override
    public Stream<DecompilationRecordWithDic<FinalLowLevelMethodWrapper<LOW_INPUT>, FinalHighLevelMethodWrapper, Map<String, String>>> apply(
            DecompilationRecord<FinalLowLevelMethodWrapper<LOW_INPUT>, FinalHighLevelMethodWrapper> decompilationRecord) {

        Map<String, String> dictionary = new HashMap<>();

        // Set up the environment

        FinalLowLevelMethodWrapper<LOW_INPUT> wrappedLowLevelMethod = decompilationRecord.getLowLevelRepresentation();
        FinalHighLevelMethodWrapper wrappedHighLevelMethod = decompilationRecord.getHighLevelRepresentation();

        // TODO: use the functional-programming paradigm here to try out different logics.

        List<Integer> indexes = new ArrayList<>(wrappedLowLevelMethod.getVariableNames().keySet());
        for (Integer variableIndex : indexes) {
            String variableName = wrappedLowLevelMethod.getVariableNames().get(variableIndex);
            if (wrappedHighLevelMethod.getNameExprNames().contains(variableName)) {
                String placeHolder = String.format("%s_%d", VAR_PREFIX, variableIndex);
                dictionary.putIfAbsent(LowLevelPrinter.LOC_VAR_SYMBOL + variableIndex, placeHolder);
                wrappedLowLevelMethod.replaceStringInBody(LowLevelPrinter.LOC_VAR_SYMBOL + variableIndex, placeHolder);
                wrappedHighLevelMethod.replaceStringInBody(variableName, placeHolder);
            }
        }

        indexes = new ArrayList<>(wrappedLowLevelMethod.getMethodNames().keySet());
        for (int i = 0; i < indexes.size(); i++) {
            Integer methodIndex = indexes.get(i);
            String methodName = wrappedLowLevelMethod.getMethodNames().get(methodIndex);
            if (wrappedHighLevelMethod.getMethodExprNames().contains(methodName)) {
                String placeHolder = String.format("%s_%d", METHOD_PREFIX, i);
                dictionary.putIfAbsent(LowLevelPrinter.POOL_SYMBOL + methodIndex, placeHolder);
                wrappedLowLevelMethod.replaceStringInBody(LowLevelPrinter.POOL_SYMBOL + methodIndex, placeHolder);
                wrappedHighLevelMethod.replaceStringInBody(methodName, placeHolder);
            }
        }

        return null;
    }
}
