package com.redhat.jhalliday.impl.javaparser;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.redhat.jhalliday.TransformerFunction;


import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class CompilationUnitCreationTransformerFunction implements TransformerFunction<Map<String, byte[]>, Map<String, CompilationUnit>> {

    @Override
    public Stream<Map<String, CompilationUnit>> apply(Map<String, byte[]> map) {

        Map<String, CompilationUnit> result = new HashMap<>();

        ParserConfiguration configuration = new ParserConfiguration();
        configuration
                .setStoreTokens(false)
                .setAttributeComments(false)
                .setDoNotAssignCommentsPrecedingEmptyLines(true)
                .setIgnoreAnnotationsWhenAttributingComments(true)
                .setLexicalPreservationEnabled(false)
                .setPreprocessUnicodeEscapes(false);

        JavaParser javaParser = new JavaParser(configuration);

        map.forEach((key, bytes) ->
            javaParser.parse(new ByteArrayInputStream(bytes))
                    .ifSuccessful(cu -> result.put(key, cu)));

        return Stream.of(result);
    }
}
