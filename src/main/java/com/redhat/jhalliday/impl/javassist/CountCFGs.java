package com.redhat.jhalliday.impl.javassist;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.redhat.jhalliday.DecompilationRecord;
import com.redhat.jhalliday.RecordTransformer;
import com.redhat.jhalliday.impl.MethodJuice;
import javassist.CtMethod;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.analysis.ControlFlow;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class CountCFGs implements RecordTransformer<
        MethodJuice<CtMethod>,
        MethodJuice<MethodDeclaration>,
        MethodJuice<CtMethod>,
        MethodJuice<MethodDeclaration>> {

    private final Map<Integer, Integer> _BasicBlockMap;
    private final int _maxNumberOfBlocks;

    public CountCFGs() {
        _maxNumberOfBlocks = 0;
        _BasicBlockMap = new HashMap<>();
    }

    public CountCFGs(int maxNumberOfBlocks) {
        _maxNumberOfBlocks = maxNumberOfBlocks;
        _BasicBlockMap = new HashMap<>();
    }

    public Map<Integer, Integer> getBasicBlockMap() {
        return _BasicBlockMap;
    }

    @Override
    public Stream<DecompilationRecord<MethodJuice<CtMethod>, MethodJuice<MethodDeclaration>>> apply(
            DecompilationRecord<MethodJuice<CtMethod>, MethodJuice<MethodDeclaration>> decompilationRecord) {

        CtMethod method = decompilationRecord.getLowLevelRepresentation().getMethod();
        ControlFlow CFGs = null;
        try {
            CFGs = new ControlFlow(method);
        } catch (BadBytecode ex) {
            // impossible
        }

        if (CFGs == null) {
            return Stream.empty();
        }

        ControlFlow.Block[] blocks = CFGs.basicBlocks();

        if (_maxNumberOfBlocks > 0 && blocks.length > _maxNumberOfBlocks) {
            return Stream.empty();
        }

        this._BasicBlockMap.computeIfPresent(blocks.length, (key, value) -> value + 1);
        this._BasicBlockMap.putIfAbsent(blocks.length, 1);

        return Stream.of(decompilationRecord);
    }
}