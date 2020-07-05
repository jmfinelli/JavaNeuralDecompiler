package com.redhat.jhalliday.impl;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.redhat.jhalliday.DecompilationRecord;
import com.redhat.jhalliday.InfoExtractor;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

public class BlockJuicerRecordTransformer <LOW_ITEM> implements Function<
        DecompilationRecord<LOW_ITEM, MethodDeclaration>,
        Stream<DecompilationRecord<MethodJuice<LOW_ITEM>, MethodJuice<MethodDeclaration>>>> {

    private final InfoExtractor<LOW_ITEM> lowLevelInfoExtractor;
    private final BiFunction<LOW_ITEM, Map<String, String>, List<Map<LineNumber, String>>> lwoLevelControlFlowBlockExtractor;
    private final BiFunction<MethodDeclaration, Map<String, String>, Map<LineNumber, String>> highLevelBodyExtractor;

    public BlockJuicerRecordTransformer(
            InfoExtractor<LOW_ITEM> lowLevelInfoExtractor,
            BiFunction<LOW_ITEM, Map<String, String>, List<Map<LineNumber, String>>> lwoLevelControlFlowBlockExtractor,
            BiFunction<MethodDeclaration, Map<String, String>, Map<LineNumber, String>> highLevelBodyExtractor) {
        this.lowLevelInfoExtractor = lowLevelInfoExtractor;
        this.lwoLevelControlFlowBlockExtractor = lwoLevelControlFlowBlockExtractor;
        this.highLevelBodyExtractor = highLevelBodyExtractor;
    }

    @Override
    public Stream<DecompilationRecord<MethodJuice<LOW_ITEM>, MethodJuice<MethodDeclaration>>> apply(DecompilationRecord<LOW_ITEM, MethodDeclaration> decompilationRecord) {

        Map<String, InfoExtractor.InfoType> lowLevelSet = lowLevelInfoExtractor.apply(decompilationRecord.getLowLevelRepresentation());

        Map<String, String> placeholders = new TreeMap<>(
                (s1, s2) -> {
                    if (s1.length() > s2.length()) {
                        return -1;
                    } else if (s1.length() < s2.length()) {
                        return 1;
                    } else {
                        return s1.compareTo(s2);
                    }
                }
        );

        for (Map.Entry<String, InfoExtractor.InfoType> entry : lowLevelSet.entrySet()) {
            int postfix = (int) placeholders.entrySet().stream()
                    .filter(x -> x.getValue().startsWith(entry.getValue().toString())).count();

            placeholders.putIfAbsent(entry.getKey(), entry.getValue().toString() + postfix);
        }

        List<Map<LineNumber, String>> CFGBlocks = lwoLevelControlFlowBlockExtractor.apply(decompilationRecord.getLowLevelRepresentation(), placeholders);

        return null;
    }
}
