package com.redhat.jhalliday.impl;

import java.util.*;
import java.util.function.Function;

public abstract class FinalLowLevelMethodWrapper<T> extends FinalMethodWrapper<T> {

    private final Map<Integer, String> variableNames;
    private final Map<Integer, String> classNames;
    private final Map<Integer, String> fieldNames;
    private final Map<Integer, String> methodNames;
    private final Map<Integer, String> constants;

    public FinalLowLevelMethodWrapper(T method, Function<T, LowInfoExtractor> methodBodyExtractor) {
        this.method = method;
        LowInfoExtractor info = methodBodyExtractor.apply(method);

        variableNames = info.getVariableNames();
        classNames = info.getClassNames();
        fieldNames = info.getFieldNames();
        methodNames = info.getMethodNames();
        constants = info.getConstants();

        this.toReplace.addAll(this.variableNames.values());
        this.toReplace.addAll(this.classNames.values());
        this.toReplace.addAll(this.fieldNames.values());
        this.toReplace.addAll(this.methodNames.values());
        this.toReplace.addAll(this.constants.values());

        this.methodBody = info.getBody();
    }

    public Map<Integer, String> getVariableNames() { return variableNames; }

    public Map<Integer, String> getClassNames() { return classNames; }

    public Map<Integer, String> getFieldNames() { return fieldNames; }

    public Map<Integer, String> getMethodNames() { return methodNames; }

    public Map<Integer, String> getConstants() { return constants; }
}
