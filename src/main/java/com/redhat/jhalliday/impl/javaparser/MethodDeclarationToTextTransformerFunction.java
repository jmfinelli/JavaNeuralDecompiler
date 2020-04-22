package com.redhat.jhalliday.impl.javaparser;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.printer.PrettyPrinter;
import com.github.javaparser.printer.PrettyPrinterConfiguration;
import com.redhat.jhalliday.TransformerFunction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

public class MethodDeclarationToTextTransformerFunction implements TransformerFunction<MethodDeclaration, List<String>> {

    @Override
    public Stream<List<String>> apply(MethodDeclaration methodDeclaration) {

        List<String> tokens = new LinkedList<>();

        PrettyPrinterConfiguration configuration = new PrettyPrinterConfiguration();
        configuration.setPrintComments(false);

        PrettyPrinter prettyPrinter = new PrettyPrinter(configuration);

        if (methodDeclaration.getBody().isPresent())
            tokens.addAll(Arrays.asList(prettyPrinter.print(methodDeclaration.getBody().get()).split("\\n")));

        return Stream.of(tokens);
    }
}
