package com.redhat.jhalliday.impl;

import java.util.*;

public abstract class FinalLowLevelMethodWrapper<T> extends FinalMethodWrapper<T> {

    protected Map<Integer, String> localVariables;
    protected Map<Integer, String> classNames;
    protected Map<Integer, String> fieldNames;
    protected Map<Integer, String> methodNames;
    protected Map<Integer, String> constants;

    public Map<Integer, String> getLocalVariables() { return localVariables; }

    public Map<Integer, String> getClassNames() { return classNames; }

    public Map<Integer, String> getFieldNames() { return fieldNames; }

    public Map<Integer, String> getMethodNames() { return methodNames; }

    public Map<Integer, String> getConstants() { return constants; }
}
