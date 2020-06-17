package com.redhat.jhalliday.impl;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.redhat.jhalliday.DecompilationRecord;
import com.redhat.jhalliday.RecordTransformer;
import com.redhat.jhalliday.TransformerFunction;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MethodAssociatingRecordTransformer<LOW_AGGREGATE, HIGH_AGGREGATE, LOW_ITEM>
        implements RecordTransformer<LOW_AGGREGATE, HIGH_AGGREGATE, LOW_ITEM, MethodDeclaration> {

    private static final Set<String> GENERATED_METHOD_NAMES = Set.of("values", "valueOf");

    private final TransformerFunction<LOW_AGGREGATE, LOW_ITEM> lowShredder;
    private final Function<LOW_ITEM, MethodWrapper<LOW_ITEM>> lowItemWrappingFunction;
    private final TransformerFunction<HIGH_AGGREGATE, MethodDeclaration> highShredder;
    private final Function<MethodDeclaration, MethodWrapper<MethodDeclaration>> highItemWrappingFunction;

    public MethodAssociatingRecordTransformer(
            TransformerFunction<LOW_AGGREGATE, LOW_ITEM> lowShredder,
            Function<LOW_ITEM, MethodWrapper<LOW_ITEM>> lowItemWrappingFunction,
            TransformerFunction<HIGH_AGGREGATE, MethodDeclaration> highShredder,
            Function<MethodDeclaration, MethodWrapper<MethodDeclaration>> highItemWrappingFunction) {
        this.lowShredder = lowShredder;
        this.lowItemWrappingFunction = lowItemWrappingFunction;
        this.highShredder = highShredder;
        this.highItemWrappingFunction = highItemWrappingFunction;
    }

    @Override
    public Stream<DecompilationRecord<LOW_ITEM, MethodDeclaration>> apply(DecompilationRecord<LOW_AGGREGATE, HIGH_AGGREGATE> decompilationRecord) {

        Map<String, List<MethodWrapper<LOW_ITEM>>> lowItemsByName = new HashMap<>();
        Map<String, List<MethodWrapper<MethodDeclaration>>> highItemsByName = new HashMap<>();
        Map<String, List<MethodWrapper<MethodDeclaration>>> decItemsByName = new HashMap<>();

        List<LOW_ITEM> lowItems = lowShredder
                .apply(decompilationRecord.getLowLevelRepresentation()).collect(Collectors.toList());

        List<MethodDeclaration> highItems = highShredder
                .apply(decompilationRecord.getHighLevelRepresentation()).collect(Collectors.toList());

        List<MethodDeclaration> decItems = highShredder
                .apply(decompilationRecord.getHighLevelDecompiled()).collect(Collectors.toList());

        for (LOW_ITEM lowItem : lowItems) {
            MethodWrapper<LOW_ITEM> methodWrapper = lowItemWrappingFunction.apply(lowItem);
            lowItemsByName.computeIfAbsent(methodWrapper.getName(), s -> new ArrayList<>()).add(methodWrapper);
        }

        for (MethodDeclaration highItem : highItems) {
            MethodWrapper<MethodDeclaration> methodWrapper = highItemWrappingFunction.apply(highItem);
            highItemsByName.computeIfAbsent(methodWrapper.getName(), s -> new ArrayList<>()).add(methodWrapper);
        }

        for (MethodDeclaration decItem : decItems) {
            MethodWrapper<MethodDeclaration> methodWrapper = highItemWrappingFunction.apply(decItem);
            decItemsByName.computeIfAbsent(methodWrapper.getName(), s -> new ArrayList<>()).add(methodWrapper);
        }

        List<DecompilationRecord<LOW_ITEM, MethodDeclaration>> results = new ArrayList<>();

        for (Map.Entry<String, List<MethodWrapper<LOW_ITEM>>> entry : lowItemsByName.entrySet()) {
            String methodName = entry.getKey();
            List<MethodWrapper<MethodDeclaration>> candidateMatches = highItemsByName.get(methodName);
            if (candidateMatches == null) {
                if (!GENERATED_METHOD_NAMES.contains(methodName)) {
                    // suspicious
                }
                continue;
            }

            List<MethodWrapper<MethodDeclaration>> candidateDecompiledMatches = decItemsByName.get(methodName);

            // Check if there are candidates
            if (decompilationRecord.getHighLevelDecompiled() != null && candidateDecompiledMatches == null) {
                continue;
            }

            List<Pair<MethodWrapper<LOW_ITEM>, MethodWrapper<MethodDeclaration>>> matchedPairs = match(entry.getValue(), candidateMatches);

            for (Pair<MethodWrapper<LOW_ITEM>, MethodWrapper<MethodDeclaration>> matchedPair : matchedPairs) {

                if (decompilationRecord.getHighLevelDecompiled() != null) {
                    MethodDeclaration highLevelMethod = matchedPair.b.unwrap();
                    List<MethodWrapper<MethodDeclaration>> decompiledMethods = candidateDecompiledMatches.stream().filter(x -> matchMethodDeclaration(highLevelMethod, x.unwrap())).collect(Collectors.toList());
                    if (decompiledMethods.size() == 1)
                        results.add(new GenericDecompilationRecord<>(matchedPair.a.unwrap(), matchedPair.b.unwrap(), decompiledMethods.get(0).unwrap(), decompilationRecord));
                } else {
                    results.add(new GenericDecompilationRecord<>(matchedPair.a.unwrap(), matchedPair.b.unwrap(), decompilationRecord));
                }
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

    public boolean matchMethodDeclaration (MethodDeclaration base, MethodDeclaration target) {
        return (!target.isAbstract() && target.getBody().isPresent() &&
                base.getType().equals(target.getType()) &&
                base.getParameters().containsAll(target.getParameters()));
    }
}
