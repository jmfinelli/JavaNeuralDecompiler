package com.redhat.jhalliday.impl;

import com.redhat.jhalliday.DecompilationRecord;

public class GenericDecompilationRecord<LOW, HIGH> implements DecompilationRecord<LOW, HIGH> {

    private final LOW lowLevelRepresentation;
    private final HIGH highLevelRepresentation;
    private final HIGH highLevelReference;

    private final DecompilationRecord predecessor;

    public GenericDecompilationRecord(LOW lowLevelRepresentation, HIGH highLevelRepresentation) {
        this(lowLevelRepresentation, highLevelRepresentation, null);
    }

    public GenericDecompilationRecord(LOW lowLevelRepresentation, HIGH highLevelRepresentation, DecompilationRecord predecessor) {
        this(lowLevelRepresentation, highLevelRepresentation, null, predecessor);
    }

    public GenericDecompilationRecord(LOW lowLevelRepresentation, HIGH highLevelRepresentation, HIGH highLevelReference, DecompilationRecord predecessor) {
        this.lowLevelRepresentation = lowLevelRepresentation;
        this.highLevelRepresentation = highLevelRepresentation;
        this.highLevelReference = highLevelReference;
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
    public HIGH getHighLevelReference() { return highLevelReference; }

    public DecompilationRecord getPredecessor() {
        return predecessor;
    }
}
