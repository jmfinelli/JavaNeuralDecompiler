package com.redhat.jhalliday.impl.javaparser;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.redhat.jhalliday.impl.javaparser.printer.PrettyPrinterMod;
import com.redhat.jhalliday.impl.FinalHighLevelMethodWrapper;
import com.redhat.jhalliday.impl.javaparser.printer.PrettyPrinterWithoutSolver;

public class MethodDeclarationFinalWrapper extends FinalHighLevelMethodWrapper {

    public MethodDeclarationFinalWrapper(MethodDeclaration method) {
        //super(method, PrettyPrinterMod::new);
        super(method, PrettyPrinterWithoutSolver::new);

        //method.getBody().get().findAll(NameExpr.class).forEach(x -> this.NameExprNames.add(x.getNameAsString()));

        //method.getBody().get().findAll(MethodCallExpr.class).forEach(x -> this.MethodExprNames.add(x.getName().getIdentifier()));

        //method.getBody().get().findAll(LiteralExpr.class).forEach(x -> this.LiteralExprNames.add(x.toString()));

        //method.getBody().get().findAll(ClassExpr.class).forEach(x -> this.ClassExprNames.add(x.toString().replace(".class", "")));

    }
}