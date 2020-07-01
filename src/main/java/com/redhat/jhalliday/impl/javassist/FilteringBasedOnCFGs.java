package com.redhat.jhalliday.impl.javassist;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.redhat.jhalliday.DecompilationRecord;
import com.redhat.jhalliday.RecordTransformer;
import com.redhat.jhalliday.impl.MethodJuice;
import javassist.CtMethod;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.analysis.ControlFlow;

import java.util.stream.Stream;

public class FilteringBasedOnCFGs implements RecordTransformer<
        MethodJuice<CtMethod>,
        MethodJuice<MethodDeclaration>,
        MethodJuice<CtMethod>,
        MethodJuice<MethodDeclaration>> {

    private final int _maxMethodLength;

    public FilteringBasedOnCFGs(int maxMethodLength) {
        this._maxMethodLength = maxMethodLength;
    }

    @Override
    public Stream<DecompilationRecord<MethodJuice<CtMethod>, MethodJuice<MethodDeclaration>>> apply(DecompilationRecord<MethodJuice<CtMethod>, MethodJuice<MethodDeclaration>> decompilationRecord) {

        CtMethod method = decompilationRecord.getLowLevelRepresentation().getMethod();
        ControlFlow CFGs = null;
        try {
            CFGs = new ControlFlow(method);
        } catch (BadBytecode ex) {
            // impossible
        }

        if (CFGs == null) return Stream.empty();

        ControlFlow.Block[] blocks = CFGs.basicBlocks();
        if (blocks.length <= this._maxMethodLength) {
            return Stream.of(decompilationRecord);
        }

        return Stream.empty();
    }
}