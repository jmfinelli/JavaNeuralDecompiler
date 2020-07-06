package com.redhat.jhalliday.impl.javaparser.extractors;

/*
 * Copyright (C) 2007-2010 Júlio Vilmar Gesser.
 * Copyright (C) 2011, 2013-2020 The JavaParser Team.
 *
 * This file is part of JavaParser.
 *
 * JavaParser can be used either under the terms of
 * a) the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * b) the terms of the Apache License
 *
 * You should have received a copy of both licenses in LICENCE.LGPL and
 * LICENCE.APACHE. Please refer to those files for details.
 *
 * JavaParser is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 */

import com.github.javaparser.Range;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.redhat.jhalliday.impl.LineNumber;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Pretty printer for AST nodes.
 */
public class HighLevelBodyExtractorWithLineNumber implements BiFunction<MethodDeclaration, Map<String, String>, Map<LineNumber, String>> {

//    private final PrettyPrinterConfigurationMod configuration;
    private final PrettyPrinterConfigurationExtraSpace configuration;

    public HighLevelBodyExtractorWithLineNumber() {

        this.configuration = new PrettyPrinterConfigurationExtraSpace();
        this.configuration.setExtraSpace(true);
        this.configuration.setPrintComments(false);
        this.configuration.setEndOfLineCharacter("\n");
        this.configuration.setColumnAlignFirstMethodChain(false);
        this.configuration.setIndentCaseInSwitch(false);
        this.configuration.setIndentSize(0);
    }

    @Override
    public Map<LineNumber, String> apply(MethodDeclaration declaration, Map<String, String> placeholders) {

        VoidVisitor<Void> visitor = new PrettyPrintVisitorWithSubstitutions(this.configuration, placeholders);

        declaration.getBody().get().accept(visitor, null);

        String body = visitor.toString()
                .replaceAll("\\s+", " ")
                // Remove initial {
                .replaceAll("\\s+}\\s+$", "")
                // Remove final }
                .replaceAll("^\\s+\\{\\s+", "");

        List<String> lines = Arrays.asList(body.split(".+\n"));

        int startLine = -1;
        int endLine = -1;
        Range range;
        if (declaration.getBody().isPresent()) {
            range = declaration.getBody().get().getRange().get();
        } else {
            range = declaration.getRange().get();
        }

        startLine = range.begin.line;
        endLine = range.end.line;

        if (!(endLine - startLine == lines.size())) {
            System.out.println("Something wrong!");
        }

        Map<LineNumber, String> results = new HashMap<>();
        for (String line : lines) {
            results.put(new LineNumber(startLine), line);
        }

        return results;
    }
}