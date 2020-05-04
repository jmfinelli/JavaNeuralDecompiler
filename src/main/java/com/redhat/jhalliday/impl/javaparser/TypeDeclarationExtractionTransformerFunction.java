package com.redhat.jhalliday.impl.javaparser;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.redhat.jhalliday.TransformerFunction;
import com.redhat.jhalliday.impl.ClassWrapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class TypeDeclarationExtractionTransformerFunction
        implements TransformerFunction<Map<String, byte[]>, Map<String, ClassWrapper<TypeDeclaration>>> {

    private final ParserUtil parserUtil = new ParserUtil();

    @Override
    public Stream<Map<String, ClassWrapper<TypeDeclaration>>> apply(Map<String, byte[]> map) {

        Map<String, ClassWrapper<TypeDeclaration>> results = new HashMap<>();

        for (Map.Entry<String, byte[]> entry : map.entrySet()) {
            CompilationUnit compilationUnit = parserUtil.parseWithFallback(entry.getValue());
            if (compilationUnit != null) {

                TypeDeclarationWrapper.getTypeDeclarationWrappersFromCompilationUnit(compilationUnit, entry.getKey())
                .forEach(x -> {
                    if (!x.getQualifiedName().isEmpty())
                        results.put(x.getQualifiedName(), x);
                        });
            } else {
                System.out.println("javaparser failed for " + entry.getKey());
            }
        }

        return Stream.of(results);
    }
}

