package com.redhat.jhalliday.impl.javaparser;

import com.github.javaparser.Range;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.redhat.jhalliday.impl.MethodWrapper;

public class MethodDeclarationWrapper extends MethodWrapper<MethodDeclaration> {

    public MethodDeclarationWrapper(MethodDeclaration methodDeclaration) {
        name = methodDeclaration.getNameAsString();
        method = methodDeclaration;

        startLine = -1;
        endLine = -1;
        Range range;
        if (methodDeclaration.getBody().isPresent()) {
            range = methodDeclaration.getBody().get().getRange().get();
        } else {
            range = methodDeclaration.getRange().get();
        }

        startLine = range.begin.line;
        endLine = range.end.line;
    }
}
