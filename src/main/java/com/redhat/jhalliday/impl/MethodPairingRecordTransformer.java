package com.redhat.jhalliday.impl;

import com.redhat.jhalliday.DecompilationRecord;
import com.redhat.jhalliday.RecordTransformer;
import com.redhat.jhalliday.TransformerFunction;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MethodPairingRecordTransformer<
        LOW_AGGREGATE extends ClassWrapper<?>,
        HIGH_AGGREGATE extends ClassWrapper<?>,
        LOW_ITEM,
        HIGH_ITEM>
        implements RecordTransformer<LOW_AGGREGATE, HIGH_AGGREGATE, LOW_ITEM, HIGH_ITEM> {

    private static final Set<String> GENERATED_METHOD_NAMES = Set.of("values", "valueOf");

    private final TransformerFunction<LOW_AGGREGATE, LOW_ITEM> lowShredder;
    private final Function<LOW_ITEM, MethodWrapperOnParameters<LOW_ITEM>> lowItemWrappingOnParametersFunction;
    private final TransformerFunction<HIGH_AGGREGATE, HIGH_ITEM> highShredder;
    private final Function<HIGH_ITEM, MethodWrapperOnParameters<HIGH_ITEM>> highItemWrappingOnParametersFunction;

    public MethodPairingRecordTransformer(
            TransformerFunction<LOW_AGGREGATE, LOW_ITEM> lowShredder,
            Function<LOW_ITEM, MethodWrapperOnParameters<LOW_ITEM>> lowItemWrappingOnParametersFunction,
            TransformerFunction<HIGH_AGGREGATE, HIGH_ITEM> highShredder,
            Function<HIGH_ITEM, MethodWrapperOnParameters<HIGH_ITEM>> highItemWrappingOnParametersFunction) {
        this.lowShredder = lowShredder;
        this.lowItemWrappingOnParametersFunction = lowItemWrappingOnParametersFunction;
        this.highShredder = highShredder;
        this.highItemWrappingOnParametersFunction = highItemWrappingOnParametersFunction;
    }

    @Override
    public Stream<DecompilationRecord<LOW_ITEM, HIGH_ITEM>> apply(DecompilationRecord<LOW_AGGREGATE, HIGH_AGGREGATE> decompilationRecord) {

        List<LOW_ITEM> lowItems = lowShredder
                .apply(decompilationRecord.getLowLevelRepresentation()).collect(Collectors.toList());
        Map<String, List<MethodWrapperOnParameters<LOW_ITEM>>> lowItemsByName = new HashMap<>();

        List<HIGH_ITEM> highItems = highShredder
                .apply(decompilationRecord.getHighLevelRepresentation()).collect(Collectors.toList());
        Map<String, List<MethodWrapperOnParameters<HIGH_ITEM>>> highItemsByName = new HashMap<>();

        for (LOW_ITEM lowItem : lowItems) {
            MethodWrapperOnParameters<LOW_ITEM> methodWrapper = lowItemWrappingOnParametersFunction.apply(lowItem);
            lowItemsByName.computeIfAbsent(methodWrapper.getName(), s -> new ArrayList<>()).add(methodWrapper);
        }

        for (HIGH_ITEM highItem : highItems) {
            MethodWrapperOnParameters<HIGH_ITEM> methodWrapper = highItemWrappingOnParametersFunction.apply(highItem);
            highItemsByName.computeIfAbsent(methodWrapper.getName(), s -> new ArrayList<>()).add(methodWrapper);
        }

        List<DecompilationRecord<LOW_ITEM, HIGH_ITEM>> results = new ArrayList<>();

        for (Map.Entry<String, List<MethodWrapperOnParameters<LOW_ITEM>>> entry : lowItemsByName.entrySet()) {
            String methodName = entry.getKey();
            List<MethodWrapperOnParameters<HIGH_ITEM>> candidateMatches = highItemsByName.get(methodName);
            if (candidateMatches == null) {
                if (!GENERATED_METHOD_NAMES.contains(methodName)) {
                    // suspicious
                }
                continue;
            }

            List<Pair<MethodWrapperOnParameters<LOW_ITEM>, MethodWrapperOnParameters<HIGH_ITEM>>> matchedPairs = match(entry.getValue(), candidateMatches);
            for (Pair<MethodWrapperOnParameters<LOW_ITEM>, MethodWrapperOnParameters<HIGH_ITEM>> matchedPair : matchedPairs) {
                results.add(new GenericDecompilationRecord<>(matchedPair.a.unwrap(), matchedPair.b.unwrap(), decompilationRecord));
            }
        }

        return results.stream();
    }

    public <A, B> List<Pair<MethodWrapperOnParameters<A>, MethodWrapperOnParameters<B>>> match(List<MethodWrapperOnParameters<A>> listA, List<MethodWrapperOnParameters<B>> listB) {

        List<Pair<MethodWrapperOnParameters<A>, MethodWrapperOnParameters<B>>> successfulMatches = new ArrayList<>();

        for (MethodWrapperOnParameters<A> itemA : listA) {
            List<Pair<MethodWrapperOnParameters<A>, MethodWrapperOnParameters<B>>> candidateMatches = new ArrayList<>();
            for (MethodWrapperOnParameters<B> itemB : listB) {
                if (itemA.equals(itemB)) {
                    candidateMatches.add(new Pair<>(itemA, itemB));
                }
            }

            if (candidateMatches.size() > 1) {
                // Check on return type
            }

            if (candidateMatches.size() == 1) {
                successfulMatches.add(candidateMatches.get(0));
                continue;
            }

            if (!GENERATED_METHOD_NAMES.contains(itemA.getName())) {
                // suspicious
            }
        }

        Set<MethodWrapperOnParameters<A>> setA = new HashSet<>();
        Set<MethodWrapperOnParameters<B>> setB = new HashSet<>();
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
