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

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.ClassExpr;
import com.github.javaparser.ast.expr.LiteralExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.redhat.jhalliday.impl.HighInfoExtractor;

import java.util.HashSet;
import java.util.Set;

/**
 * Pretty printer for AST nodes.
 */
public class PrettyPrinterWithoutSolver implements HighInfoExtractor {

    private final PrettyPrinterConfigurationMod configuration;
    private final String body;

    private final Set<String> nameExprNames = new HashSet<>();
    private final Set<String> methodExprNames = new HashSet<>();
    private final Set<String> literalExprNames = new HashSet<>();
    private final Set<String> classExprNames = new HashSet<>();

    public PrettyPrinterWithoutSolver(MethodDeclaration methodDeclaration) {

        this.configuration = new PrettyPrinterConfigurationMod();
        this.configuration.setExtraWhiteSpace(true);
        this.configuration.setPrintComments(false);
        this.configuration.setEndOfLineCharacter(" ");
        this.configuration.setColumnAlignFirstMethodChain(false);
        this.configuration.setIndentCaseInSwitch(false);
        this.configuration.setIndentSize(0);

        final VoidVisitor<Void> visitor = configuration.getVisitorFactory().apply(this.configuration);
        // it is not needed to check if there is a body because
        // all previous operations (in the Driver.java) make sure that a body is present
        methodDeclaration.getBody().get().accept(visitor, null);

        String tempBody = visitor.toString();
        body = tempBody.replaceAll("\\s+", " ");
    }

    @Override
    public Set<String> getMethodExprNames() { return this.methodExprNames; }

    @Override
    public Set<String> getClassExprNames() { return this.classExprNames; }

    @Override
    public Set<String> getLiteralExprNames() { return this.literalExprNames; }

    @Override
    public Set<String> getNameExprNames() { return this.nameExprNames; }

    @Override
    public String getBody() { return this.body; }

}