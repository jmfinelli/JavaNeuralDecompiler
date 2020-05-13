package com.redhat.jhalliday.impl.javaparser;

import com.github.javaparser.*;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import java.io.File;
import java.io.IOException;

public class ParserUtil {

    private ParserConfiguration.LanguageLevel[] levels = new ParserConfiguration.LanguageLevel[]{
            ParserConfiguration.LanguageLevel.JAVA_11,
            // fallback to 8 deals with '_' as a var name.
            ParserConfiguration.LanguageLevel.JAVA_8,
            // fallback to 1_4 deals with 'enum' as a var name.
            ParserConfiguration.LanguageLevel.JAVA_1_4
    };

    private ParserConfiguration[] configurations = new ParserConfiguration[6]; // 3 levels x 2 unicode escapes.

    public ParserUtil() {
        for (int i = 0; i < configurations.length; i++) {
            ParserConfiguration parserConfiguration = new ParserConfiguration();
            parserConfiguration.setLanguageLevel(levels[i >= 3 ? i - 3 : i]);
            parserConfiguration.setAttributeComments(false); // otherwise lexical preservation won't work.
            parserConfiguration.setPreprocessUnicodeEscapes(i >= 3);
            configurations[i] = parserConfiguration;
        }
    }

    public CompilationUnit parseWithFallback(byte[] inputBytes) {

        CompilationUnit compilationUnit = null;
        for (int i = 0; i < configurations.length && compilationUnit == null; i++) {
            compilationUnit = tryParseWithConfig(inputBytes, configurations[i]);
        }

        return compilationUnit;
    }

    public CompilationUnit parseWithFallback(byte[] inputBytes, File jarFile) {

        CompilationUnit compilationUnit = null;
        for (int i = 0; i < configurations.length && compilationUnit == null; i++) {
            compilationUnit = tryParseWithConfig(inputBytes, configurations[i], jarFile);
        }

        return compilationUnit;
    }

    private CompilationUnit tryParseWithConfig(byte[] inputBytes, ParserConfiguration parserConfiguration) {

        try {
            ParseResult<CompilationUnit> result =
                    new JavaParser(parserConfiguration).parse(new String(inputBytes));

            if (!result.isSuccessful()) {
                return null;
            } else {
                return result.getResult().get();
            }

        } catch (Throwable e) {
            return null;
        }
    }

    private CompilationUnit tryParseWithConfig(byte[] inputBytes, ParserConfiguration parserConfiguration, File jarFile) {

        try {

            TypeSolver reflectionTypeSolver = new ReflectionTypeSolver();
            TypeSolver jarTypeSolver = new JarTypeSolver(jarFile);

            CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
            combinedTypeSolver.add(jarTypeSolver);
            combinedTypeSolver.add(reflectionTypeSolver);
            JavaSymbolSolver symbolSolver = new JavaSymbolSolver(combinedTypeSolver);

            parserConfiguration.setSymbolResolver(symbolSolver);
            JavaParser javaParser = new JavaParser(parserConfiguration);

            ParseResult<CompilationUnit> result = javaParser.parse(new String(inputBytes));

            if (!result.isSuccessful()) {
                return null;
            } else {
                return result.getResult().get();
            }

        } catch (Throwable e) {
            return null;
        }
    }
}
