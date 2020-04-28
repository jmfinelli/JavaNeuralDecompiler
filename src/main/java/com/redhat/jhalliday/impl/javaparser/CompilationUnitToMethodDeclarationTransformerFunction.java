package com.redhat.jhalliday.impl.javaparser;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.redhat.jhalliday.TransformerFunction;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Deprecated
public class CompilationUnitToMethodDeclarationTransformerFunction
        implements TransformerFunction<CompilationUnit, MethodDeclaration> {

    @Override
    public Stream<MethodDeclaration> apply(CompilationUnit compilationUnit) {

        final List<MethodDeclaration> result = new ArrayList<>();

        MethodCollect methodCollect = new MethodCollect();

        methodCollect.visit(compilationUnit, result);

        return result.stream();
    }

    private static class MethodCollect extends VoidVisitorAdapter<List<MethodDeclaration>> {

        @Override
        public void visit(MethodDeclaration md, List<MethodDeclaration> collector) {
            super.visit(md, collector);
            md.getBody().ifPresent(x -> {
                /*
                 * Exclude empty methods
                 */
                if (!x.isEmptyStmt() || !x.isEmpty())
                    collector.add(md);
            });
        }
    }
}
