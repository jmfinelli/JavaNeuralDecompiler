package com.redhat.jhalliday.impl;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.redhat.jhalliday.DecompilationRecord;
import com.redhat.jhalliday.RecordTransformer;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

public class FilterDuplicatesOutRecordTransformer<LOW_ITEM> implements
        RecordTransformer<
                MethodJuice<LOW_ITEM>, MethodJuice<MethodDeclaration>,
                MethodJuice<LOW_ITEM>, MethodJuice<MethodDeclaration>> {

    final private Set<String> lowLevelBodies;
    final private Set<String> highLevelBodies;

    public FilterDuplicatesOutRecordTransformer() {
        this.lowLevelBodies = new HashSet<>();
        this.highLevelBodies = new HashSet<>();
    }

    @Override
    public Stream<DecompilationRecord<MethodJuice<LOW_ITEM>, MethodJuice<MethodDeclaration>>> apply
            (DecompilationRecord<MethodJuice<LOW_ITEM>, MethodJuice<MethodDeclaration>> decompilationRecord) {

        if (this.lowLevelBodies.contains(decompilationRecord.getLowLevelRepresentation().getBody()) &&
            this.highLevelBodies.contains(decompilationRecord.getHighLevelRepresentation().getBody())) {
            return Stream.empty();
        }

        this.lowLevelBodies.add(decompilationRecord.getLowLevelRepresentation().getBody());
        this.highLevelBodies.add(decompilationRecord.getHighLevelRepresentation().getBody());

        return Stream.of(decompilationRecord);
    }
}
