package com.redhat.jhalliday.impl.javaparser;

import com.github.javaparser.ast.CompilationUnit;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Stream;

public class CompilationUnitCreationWithSolverTransformerFunction implements
        BiFunction<Map<String, byte[]>, File, Stream<Map<String, CompilationUnit>>> {

    private final ParserUtil parserUtil = new ParserUtil();

    @Override
    public Stream<Map<String, CompilationUnit>> apply(Map<String, byte[]> map, File jarFile) {

        Map<String, CompilationUnit> result = new HashMap<>();

        for (Map.Entry<String, byte[]> entry : map.entrySet()) {
            CompilationUnit compilationUnit = parserUtil.parseWithFallback(entry.getValue(), jarFile);
            if (compilationUnit != null) {
                result.put(entry.getKey(), compilationUnit);
            } else {
                System.out.println("javaparser failed for " + entry.getKey());
            }
        }

        return Stream.of(result);
    }
}

