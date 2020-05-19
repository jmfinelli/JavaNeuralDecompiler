package com.redhat.jhalliday.impl.javaparser;

import com.github.javaparser.ast.CompilationUnit;
import com.redhat.jhalliday.TransformerFunction;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public class CompilationUnitCreationTransformerFunction
        implements TransformerFunction<Map<String, byte[]>, Map<String, CompilationUnit>> {

    private final ParserUtil parserUtil;

    public CompilationUnitCreationTransformerFunction(File binJarsFolder) {

        if (!binJarsFolder.isDirectory())
            throw new IllegalArgumentException("Parameter must be a folder containing jars!");

        this.parserUtil = new ParserUtil(binJarsFolder);
    }

    public CompilationUnitCreationTransformerFunction() {
        this.parserUtil = new ParserUtil();
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