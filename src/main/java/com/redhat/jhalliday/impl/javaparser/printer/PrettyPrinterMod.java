package com.redhat.jhalliday.impl.javaparser.printer;

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

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.redhat.jhalliday.impl.HighInfoExtractor;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

/**
 * Pretty printer for AST nodes.
 */
public class PrettyPrinterMod implements HighInfoExtractor {

    private final PrettyPrinterConfigurationMod configuration;
    private final String body;

    public PrettyPrinterMod(MethodDeclaration methodDeclaration) {

        this.configuration = new PrettyPrinterConfigurationMod();
        this.configuration.setExtraWhiteSpace(true);
        this.configuration.setPrintComments(false);
        this.configuration.setEndOfLineCharacter(" ");
        this.configuration.setColumnAlignFirstMethodChain(false);
        this.configuration.setIndentCaseInSwitch(false);
        this.configuration.setIndentSize(0);

        final VoidVisitor<Void> visitor = configuration.getVisitorFactory().apply(this.configuration);
        methodDeclaration.accept(visitor, null);

        body = visitor.toString().replaceAll("\\s+", " ");
    }

    @Override
    public Set<String> getMethodExprNames() { return new HashSet<>(); }

    @Override
    public Set<String> getClassExprNames() { return new HashSet<>(); }

    @Override
    public Set<String> getLiteralExprNames() { return new HashSet<>(); }

    @Override
    public Set<String> getNameExprNames() { return new HashSet<>(); }

    @Override
    public String getBody() { return this.body; }

}