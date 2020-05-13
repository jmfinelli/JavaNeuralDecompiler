package com.redhat.jhalliday.impl.javaparser;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.*;
import com.redhat.jhalliday.impl.javaparser.printer.PrettyPrinterMod;
import com.redhat.jhalliday.impl.javaparser.printer.PrettyPrinterConfigurationMod;
import com.redhat.jhalliday.impl.FinalHighLevelMethodWrapper;

public class MethodDeclarationFinalWrapper extends FinalHighLevelMethodWrapper {

    public MethodDeclarationFinalWrapper(MethodDeclaration method) {

        this.method = method;
        this.name = method.getNameAsString();

        if (method.getBody().isPresent()) {

//            System.out.println("NameExpr");
//            method.getBody().get().findAll(NameExpr.class).forEach(x -> System.out.printf("%s: %s\n", x.getClass().toGenericString(), x.getNameAsString()));
            method.getBody().get().findAll(NameExpr.class).forEach(x -> this.NameExpr.add(x.getNameAsString()));

//            System.out.println("MethodCallExpr");
//            method.getBody().get().findAll(MethodCallExpr.class).forEach(x -> System.out.printf("%s: %s\n", x.getClass().toGenericString(), x.getName().getIdentifier()));
            method.getBody().get().findAll(MethodCallExpr.class).forEach(x -> this.MethodExpr.add(x.getName().getIdentifier()));

//            System.out.println("LiteralExpr");
//            method.getBody().get().findAll(LiteralExpr.class).forEach(x -> System.out.printf("%s: %s\n", x.getClass().toGenericString(), x.toString()));
            method.getBody().get().findAll(LiteralExpr.class).forEach(x -> this.LiteralExpr.add(x.toString()));

//            System.out.println("ClassExpr");
//            method.getBody().get().findAll(ClassExpr.class).forEach(x -> System.out.printf("%s: %s\n", x.getClass().toGenericString(), x.toString().replace(".class", "")));
            method.getBody().get().findAll(ClassExpr.class).forEach(x -> this.ClassExpr.add(x.toString().replace(".class", "")));

            this.toReplace.addAll(this.ClassExpr);
            this.toReplace.addAll(this.LiteralExpr);
            this.toReplace.addAll(this.MethodExpr);
            this.toReplace.addAll(this.NameExpr);

            PrettyPrinterConfigurationMod conf = new PrettyPrinterConfigurationMod();
            conf.setPrintComments(false);
            conf.setEndOfLineCharacter(" ");
            conf.setColumnAlignFirstMethodChain(false);
            conf.setIndentCaseInSwitch(false);
            conf.setIndentSize(0);

            PrettyPrinterMod prettyPrinter = new PrettyPrinterMod(conf);
            this.methodBody = prettyPrinter.print(method.getBody().get());

//            System.out.println("EXPRESSION.CLASS");
//            method.getBody().get().findAll(Expression.class).forEach(x -> System.out.printf("%s: %s\n", x.getClass().toGenericString(), x.toString()));
        }
    }
}