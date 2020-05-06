package com.redhat.jhalliday.impl;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.redhat.jhalliday.DecompilationRecord;
import com.redhat.jhalliday.DecompilationRecordWithDic;
import com.redhat.jhalliday.TransformerFunction;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DictionaryExtractionRecordTransformer<LOW_INPUT> implements Function<
        DecompilationRecord<LOW_INPUT, MethodDeclaration>,
        Stream<DecompilationRecordWithDic<List<String>, List<String>, Map<String, String>>>> {

    private final TransformerFunction<LOW_INPUT, String> lowMethodExtractor;
    private final TransformerFunction<MethodDeclaration, String> highMethodExtraction;

    public DictionaryExtractionRecordTransformer(
            TransformerFunction<LOW_INPUT, String> lowMethodExtractor,
            TransformerFunction<MethodDeclaration, String> highMethodExtraction) {
        this.lowMethodExtractor = lowMethodExtractor;
        this.highMethodExtraction = highMethodExtraction;
    }

    @Override
    public Stream<DecompilationRecordWithDic<List<String>, List<String>, Map<String, String>>> apply(DecompilationRecord<LOW_INPUT, MethodDeclaration> decompilationRecord) {

        List<String> lowMethodBodies = lowMethodExtractor.apply(decompilationRecord.getLowLevelRepresentation()).collect(Collectors.toList());
        //List<String> highMethodBodies = highMethodExtraction.apply(decompilationRecord.getHighLevelRepresentation()).collect(Collectors.toList());

        return null;
    }
}
