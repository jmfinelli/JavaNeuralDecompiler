package com.redhat.jhalliday.impl;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.redhat.jhalliday.DecompilationRecord;
import com.redhat.jhalliday.DecompilationRecordWithDic;
import com.redhat.jhalliday.impl.javassist.ParameterExtractor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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

        DecompilationRecord previous = decompilationRecord.getPredecessor();
        while (!(previous.getHighLevelRepresentation() instanceof File)) {
            previous = previous.getPredecessor();
        }

        TypeSolver jarTypeSolver = null;
        TypeSolver reflectionTypeSolver = new ReflectionTypeSolver();
        reflectionTypeSolver.setParent(reflectionTypeSolver);
        try {
            jarTypeSolver = new JarTypeSolver((File)previous.getHighLevelRepresentation());
        } catch (IOException e) {
            e.printStackTrace();
        }

        CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
        if (jarTypeSolver != null)
            combinedTypeSolver.add(jarTypeSolver);
        combinedTypeSolver.add(reflectionTypeSolver);

        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(combinedTypeSolver);

        FinalLowLevelMethodWrapper<LOW_INPUT> wrappedLowLevelMethod = decompilationRecord.getLowLevelRepresentation();
        FinalHighLevelMethodWrapper wrappedHighLevelMethod = decompilationRecord.getHighLevelRepresentation();

        ParserConfiguration configuration = new ParserConfiguration();
        configuration.setSymbolResolver(symbolSolver);
        StaticJavaParser.setConfiguration(configuration);

        StaticJavaParser.parse(wrappedHighLevelMethod.methodBody);

        // TODO: use the functional-programming paradigm here to try out different logics.

        List<Integer> indexes = new ArrayList<>(wrappedLowLevelMethod.getLocalVariables().keySet());
        for (Integer variableIndex : indexes) {
            String variableName = wrappedLowLevelMethod.getLocalVariables().get(variableIndex);
            if (wrappedHighLevelMethod.getNameExpr().contains(variableName)) {
                String placeHolder = String.format("%s_%d", VAR_PREFIX, variableIndex);
                dictionary.putIfAbsent(ParameterExtractor.LOC_VAR_SYMBOL + variableIndex, placeHolder);
                wrappedLowLevelMethod.replaceStringInBody(ParameterExtractor.LOC_VAR_SYMBOL + variableIndex, placeHolder);
                wrappedHighLevelMethod.replaceStringInBody(variableName, placeHolder);
            }
        }

        indexes = new ArrayList<>(wrappedLowLevelMethod.getMethodNames().keySet());
        for (int i = 0; i < indexes.size(); i++) {
            Integer methodIndex = indexes.get(i);
            String methodName = wrappedLowLevelMethod.getMethodNames().get(methodIndex);
            if (wrappedHighLevelMethod.getMethodExpr().contains(methodName)) {
                String placeHolder = String.format("%s_%d", METHOD_PREFIX, i);
                dictionary.putIfAbsent(ParameterExtractor.POOL_SYMBOL + methodIndex, placeHolder);
                wrappedLowLevelMethod.replaceStringInBody(ParameterExtractor.POOL_SYMBOL + methodIndex, placeHolder);
                wrappedHighLevelMethod.replaceStringInBody(methodName, placeHolder);
            }
        }

        return null;
    }
}
