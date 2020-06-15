package com.redhat.jhalliday.impl;

import com.redhat.jhalliday.DecompilationRecord;

public class GenericDecompilationRecord<LOW, HIGH> implements DecompilationRecord<LOW, HIGH> {

    private final LOW lowLevelRepresentation;
    private final HIGH highLevelRepresentation;
    private final HIGH highLevelDecompiled;

    private final DecompilationRecord predecessor;

    public GenericDecompilationRecord(LOW lowLevelRepresentation, HIGH highLevelRepresentation) {
        this(lowLevelRepresentation, highLevelRepresentation, null);
    }

    public GenericDecompilationRecord(LOW lowLevelRepresentation, HIGH highLevelRepresentation, DecompilationRecord predecessor) {
        this(lowLevelRepresentation, highLevelRepresentation, null, predecessor);
    }

    public GenericDecompilationRecord(LOW lowLevelRepresentation, HIGH highLevelRepresentation, HIGH highLevelDecompiled, DecompilationRecord predecessor) {
        this.lowLevelRepresentation = lowLevelRepresentation;
        this.highLevelRepresentation = highLevelRepresentation;
        this.highLevelDecompiled = highLevelDecompiled;
        this.predecessor = predecessor;
    }

    @Override
    public LOW getLowLevelRepresentation() {
        return lowLevelRepresentation;
    }

    @Override
    public HIGH getHighLevelRepresentation() {
        return highLevelRepresentation;
    }

    @Override
    public HIGH getHighLevelDecompiled() { return highLevelDecompiled; }

    public DecompilationRecord getPredecessor() {
        return predecessor;
    }
}
