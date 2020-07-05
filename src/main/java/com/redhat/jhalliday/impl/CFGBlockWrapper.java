package com.redhat.jhalliday.impl;

import com.redhat.jhalliday.impl.javassist.extractors.LowLevelBodyExtractor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CFGBlockWrapper<T> {

    protected int index;
    protected T basicBlock;
    protected int firstInstrPosition;
    protected int length;
    protected Map<LineNumber, String> blockBody;

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

    public String getBlockBody() {

        List<String> bytecodes = new ArrayList<>();
        blockBody.forEach((x, y) -> bytecodes.add(y));

        return String.join(LowLevelBodyExtractor.DELIMITER, bytecodes);
    }
}
