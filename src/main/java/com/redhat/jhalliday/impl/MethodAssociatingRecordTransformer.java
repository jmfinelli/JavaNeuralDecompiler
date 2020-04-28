package com.redhat.jhalliday.impl;

import com.redhat.jhalliday.DecompilationRecord;
import com.redhat.jhalliday.RecordTransformer;
import com.redhat.jhalliday.TransformerFunction;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MethodAssociatingRecordTransformer<LOW_AGGREGATE, HIGH_AGGREGATE, LOW_ITEM, HIGH_ITEM>
        implements RecordTransformer<LOW_AGGREGATE, HIGH_AGGREGATE, LOW_ITEM, HIGH_ITEM> {

    private static final Set<String> GENERATED_METHOD_NAMES = Set.of("values", "valueOf");

    private final TransformerFunction<LOW_AGGREGATE, LOW_ITEM> lowShredder;
    private final Function<LOW_ITEM, MethodWrapper<LOW_ITEM>> lowItemWrappingFunction;
    private final TransformerFunction<HIGH_AGGREGATE, HIGH_ITEM> highShredder;
    private final Function<HIGH_ITEM, MethodWrapper<HIGH_ITEM>> highItemWrappingFunction;

    public MethodAssociatingRecordTransformer(
            TransformerFunction<LOW_AGGREGATE, LOW_ITEM> lowShredder,
            Function<LOW_ITEM, MethodWrapper<LOW_ITEM>> lowItemWrappingFunction,
            TransformerFunction<HIGH_AGGREGATE, HIGH_ITEM> highShredder,
            Function<HIGH_ITEM, MethodWrapper<HIGH_ITEM>> highItemWrappingFunction) {
        this.lowShredder = lowShredder;
        this.lowItemWrappingFunction = lowItemWrappingFunction;
        this.highShredder = highShredder;
        this.highItemWrappingFunction = highItemWrappingFunction;
    }

    @Override
    public Stream<DecompilationRecord<LOW_ITEM, HIGH_ITEM>> apply(DecompilationRecord<LOW_AGGREGATE, HIGH_AGGREGATE> decompilationRecord) {

        List<LOW_ITEM> lowItems = lowShredder
                .apply(decompilationRecord.getLowLevelRepresentation()).collect(Collectors.toList());
        Map<String, List<MethodWrapper<LOW_ITEM>>> lowItemsByName = new HashMap<>();

        List<HIGH_ITEM> highItems = highShredder
                .apply(decompilationRecord.getHighLevelRepresentation()).collect(Collectors.toList());
        Map<String, List<MethodWrapper<HIGH_ITEM>>> highItemsByName = new HashMap<>();

        for (LOW_ITEM lowItem : lowItems) {
            MethodWrapper<LOW_ITEM> methodWrapper = lowItemWrappingFunction.apply(lowItem);
            lowItemsByName.computeIfAbsent(methodWrapper.getName(), s -> new ArrayList<>()).add(methodWrapper);
        }

        for (HIGH_ITEM highItem : highItems) {
            MethodWrapper<HIGH_ITEM> methodWrapper = highItemWrappingFunction.apply(highItem);
            highItemsByName.computeIfAbsent(methodWrapper.getName(), s -> new ArrayList<>()).add(methodWrapper);
        }

        List<DecompilationRecord<LOW_ITEM, HIGH_ITEM>> results = new ArrayList<>();

        for (Map.Entry<String, List<MethodWrapper<LOW_ITEM>>> entry : lowItemsByName.entrySet()) {
            String methodName = entry.getKey();
            List<MethodWrapper<HIGH_ITEM>> candidateMatches = highItemsByName.get(methodName);
            if (candidateMatches == null) {
                if (!GENERATED_METHOD_NAMES.contains(methodName)) {
                    // suspicious
                }
                continue;
            }

            List<Pair<MethodWrapper<LOW_ITEM>, MethodWrapper<HIGH_ITEM>>> matchedPairs = match(entry.getValue(), candidateMatches);
            for (Pair<MethodWrapper<LOW_ITEM>, MethodWrapper<HIGH_ITEM>> matchedPair : matchedPairs) {
                results.add(new GenericDecompilationRecord<>(matchedPair.a.unwrap(), matchedPair.b.unwrap(), decompilationRecord));
            }
        }

        return results.stream();
    }

    public <A, B> List<Pair<MethodWrapper<A>, MethodWrapper<B>>> match(List<MethodWrapper<A>> listA, List<MethodWrapper<B>> listB) {

        List<Pair<MethodWrapper<A>, MethodWrapper<B>>> successfulMatches = new ArrayList<>();

        for (MethodWrapper<A> itemA : listA) {
            List<Pair<MethodWrapper<A>, MethodWrapper<B>>> candidateMatches = new ArrayList<>();
            for (MethodWrapper<B> itemB : listB) {
                if (itemA.enclosedBy(itemB)) {
                    candidateMatches.add(new Pair<>(itemA, itemB));
                }
            }

            if (candidateMatches.size() > 1) {
                candidateMatches.sort(Comparator.comparingInt(value -> value.a.nonOverlapSize(value.b)));
            }

            if (candidateMatches.size() == 1) {
                successfulMatches.add(candidateMatches.get(0));
                continue;
            }

            if (!GENERATED_METHOD_NAMES.contains(itemA.getName())) {
                // suspicious
            }
        }

        Set<MethodWrapper<A>> setA = new HashSet<>();
        Set<MethodWrapper<B>> setB = new HashSet<>();
        successfulMatches.forEach(pair -> {
            setA.add(pair.a);
            setB.add(pair.b);
        });
        if (setA.size() != successfulMatches.size() || setB.size() != successfulMatches.size()) {
            // oops, we mismatched
            throw new IllegalStateException();
        }

        return successfulMatches;
    }
}
