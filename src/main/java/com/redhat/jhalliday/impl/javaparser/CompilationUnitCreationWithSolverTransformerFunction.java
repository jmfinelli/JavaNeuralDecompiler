package com.redhat.jhalliday.impl.javaparser;

import com.github.javaparser.ast.CompilationUnit;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

public class CompilationUnitCreationWithSolverTransformerFunction implements
        Function<Map<String, byte[]>, Stream<Map<String, CompilationUnit>>> {

    private final ParserUtil parserUtil;

    public CompilationUnitCreationWithSolverTransformerFunction(File binJarFolder) {
        this.parserUtil = new ParserUtil(binJarFolder);
    }

    @Override
    public Stream<Map<String, CompilationUnit>> apply(Map<String, byte[]> map) {

        Map<String, CompilationUnit> result = new HashMap<>();

        for (Map.Entry<String, byte[]> entry : map.entrySet()) {
            CompilationUnit compilationUnit = parserUtil.parseWithFallback(entry.getValue());
            if (compilationUnit != null) {
                result.put(entry.getKey(), compilationUnit);
            } else {
                System.out.println("javaparser failed for " + entry.getKey());
            }
        }

        return Stream.of(result);
    }
}

