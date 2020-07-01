package com.redhat.jhalliday.impl;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.redhat.jhalliday.DecompilationRecord;
import com.redhat.jhalliday.RecordTransformer;
import javassist.CtMethod;

import java.util.stream.Stream;

public class IdentityRecordTransformer<LOW, HIGH> implements RecordTransformer<LOW, HIGH, LOW, HIGH> {

    @Override
    public Stream<DecompilationRecord<LOW, HIGH>> apply(DecompilationRecord<LOW, HIGH> decompilationRecord) {
        return Stream.of(decompilationRecord);
    }
}
