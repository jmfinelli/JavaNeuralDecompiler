package com.redhat.jhalliday.impl;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.redhat.jhalliday.DecompilationRecord;
import com.redhat.jhalliday.InfoExtractor;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JuicerRecordLowBasedTransformer<LOW_ITEM> implements Function<
        DecompilationRecord<LOW_ITEM, MethodDeclaration>,
        Stream<DecompilationRecord<MethodJuice<LOW_ITEM>, MethodJuice<MethodDeclaration>>>> {

    private final InfoExtractor<LOW_ITEM> lowLevelInfoExtractor;
    private final InfoExtractor<MethodDeclaration> highLevelInfoExtractor;
    private final BiFunction<LOW_ITEM, Map<String, String>, String> lowLevelBodyExtractor;
    private final BiFunction<MethodDeclaration, Map<String, String>, String> highLevelBodyExtractor;

    public JuicerRecordLowBasedTransformer(
            InfoExtractor<LOW_ITEM> lowLevelInfoExtractor,
            InfoExtractor<MethodDeclaration> highLevelInfoExtractor,
            BiFunction<LOW_ITEM, Map<String, String>, String> lowLevelBodyExtractor,
            BiFunction<MethodDeclaration, Map<String, String>, String> highLevelBodyExtractor) {
        this.lowLevelInfoExtractor = lowLevelInfoExtractor;
        this.highLevelInfoExtractor = highLevelInfoExtractor;
        this.lowLevelBodyExtractor = lowLevelBodyExtractor;
        this.highLevelBodyExtractor = highLevelBodyExtractor;
    }

    @Override
    public Stream<DecompilationRecord<MethodJuice<LOW_ITEM>, MethodJuice<MethodDeclaration>>> apply
            (DecompilationRecord<LOW_ITEM, MethodDeclaration> decompilationRecord) {

        Map<String, InfoExtractor.InfoType> lowLevelSet = lowLevelInfoExtractor.apply(decompilationRecord.getLowLevelRepresentation());

        Map<String, String> placeholders = new TreeMap<String, String>(
                new Comparator<String>() {
                    @Override
                    public int compare(String s1, String s2) {
                        if (s1.length() > s2.length()) {
                            return -1;
                        } else if (s1.length() < s2.length()) {
                            return 1;
                        } else {
                            return s1.compareTo(s2);
                        }
                    }}
        );

        for (Map.Entry<String, InfoExtractor.InfoType> entry : lowLevelSet.entrySet()) {
            int postfix = placeholders.entrySet().stream()
                    .filter(x -> x.getValue().startsWith(entry.getValue().toString()))
                    .collect(Collectors.toList()).size();

            placeholders.putIfAbsent(entry.getKey(), entry.getValue().toString() + postfix);
        }

        String LowLevelBody = lowLevelBodyExtractor.apply(decompilationRecord.getLowLevelRepresentation(), placeholders);
        String HighLevelBody = highLevelBodyExtractor.apply(decompilationRecord.getHighLevelRepresentation(), placeholders);

        DecompilationRecord<MethodJuice<LOW_ITEM>, MethodJuice<MethodDeclaration>> newRecord = new GenericDecompilationRecord<>(
                new MethodJuice<>(decompilationRecord.getLowLevelRepresentation(), placeholders, LowLevelBody),
                new MethodJuice<>(decompilationRecord.getHighLevelRepresentation(), placeholders, HighLevelBody),
                decompilationRecord
        );

        return Stream.of(newRecord);
    }
}
