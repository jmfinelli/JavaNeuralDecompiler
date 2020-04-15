package ncl.ac.uk.transformation.impl;

import ncl.ac.uk.transformation.DecompilationRecord;

public class GenericDecompilationRecord<LOW, HIGH> implements DecompilationRecord<LOW, HIGH> {

    private final LOW lowLevelRepresentation;
    private final HIGH highLevelRepresentation;

    private final DecompilationRecord predecessor;

    public GenericDecompilationRecord(LOW lowLevelRepresentation, HIGH highLevelRepresentation) {
        this(lowLevelRepresentation, highLevelRepresentation, null);
    }

    public GenericDecompilationRecord(LOW lowLevelRepresentation, HIGH highLevelRepresentation, DecompilationRecord predecessor) {
        this.lowLevelRepresentation = lowLevelRepresentation;
        this.highLevelRepresentation = highLevelRepresentation;
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

    public DecompilationRecord getPredecessor() {
        return predecessor;
    }
}
