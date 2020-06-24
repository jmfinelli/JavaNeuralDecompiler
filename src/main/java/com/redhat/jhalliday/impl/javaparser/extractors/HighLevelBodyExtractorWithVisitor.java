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

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitor;

import java.util.Map;
import java.util.function.BiFunction;

/**
 * Pretty printer for AST nodes.
 */
public class HighLevelBodyExtractorWithVisitor implements BiFunction<MethodDeclaration, Map<String, String>, String> {

//    private final PrettyPrinterConfigurationMod configuration;
    private final PrettyPrinterConfigurationExtraSpace configuration;

    public HighLevelBodyExtractorWithVisitor() {

        this.configuration = new PrettyPrinterConfigurationExtraSpace();
        this.configuration.setExtraSpace(true);
        this.configuration.setPrintComments(false);
        this.configuration.setEndOfLineCharacter(" ");
        this.configuration.setColumnAlignFirstMethodChain(false);
        this.configuration.setIndentCaseInSwitch(false);
        this.configuration.setIndentSize(0);
    }

    @Override
    public String apply(MethodDeclaration declaration, Map<String, String> placeholders) {

        VoidVisitor<Void> visitor = new PrettyPrintVisitorWithSubstitutions(this.configuration, placeholders);

        declaration.getBody().get().accept(visitor, null);

        return visitor.toString()
                // Remove Redundant Spaces
                .replaceAll("\\s+", " ")
                // Remove initial {
                .replaceAll("\\s+}\\s+$", "")
                // Remove final }
                .replaceAll("^\\s+\\{\\s+", "");
    }
}