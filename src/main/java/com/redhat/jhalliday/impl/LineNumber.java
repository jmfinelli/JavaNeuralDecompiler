package com.redhat.jhalliday.impl;

public class LineNumber implements Comparable<LineNumber>{

    private final int index;
    private final int lineNumber;

    public LineNumber(int index, int lineNumber) {
        this.index = index;
        this.lineNumber = lineNumber;
    }

    @Override
    public int compareTo(LineNumber lineNumber) {
        if (index < lineNumber.getIndex()){
            return -1;
        } else if (index > lineNumber.getIndex()) {
            return 1;
        } else {
            return 0;
        }
    }

    public int getIndex() {
        return index;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + (int) index;
        hash = 31 * hash + lineNumber;
        return hash;
    }
}
