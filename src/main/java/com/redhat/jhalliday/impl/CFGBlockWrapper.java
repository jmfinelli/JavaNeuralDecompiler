package com.redhat.jhalliday.impl;

public class CFGBlockWrapper<T> {

    protected int index;
    protected T basicBlock;
    protected int firstInstrPosition;
    protected int length;
    protected String blockBody;

    public int getIndex() {
        return index;
    }

    public T getBasicBlock() {
        return basicBlock;
    }

    public int getFirstInstrPosition() {
        return firstInstrPosition;
    }

    public int getLength() {
        return length;
    }

    public String getBlockBody() { return blockBody; }
}
