package com.redhat.jhalliday.impl.javassist;

import com.redhat.jhalliday.impl.FinalMethodWrapper;
import javassist.CtMethod;

import java.util.Map;

public class CtMethodFinalWrapper extends FinalMethodWrapper<CtMethod> {

    private final Map<Integer, String> localVariables;
    private final Map<Integer, String> classNames;
    private final Map<Integer, String> fieldNames;
    private final Map<Integer, String> methodNames;
    private final Map<Integer, String> constants;

    public CtMethodFinalWrapper(CtMethod method) {

        ParameterExtractor exprExtractor = new ParameterExtractor(method, "\n");

        this.method = method;
        this.name = method.getLongName();

        this.localVariables = exprExtractor.getVariableNames();
        this.methodNames = exprExtractor.getMethodNames();
        this.classNames = exprExtractor.getClassNames();
        this.fieldNames = exprExtractor.getFieldNames();
        this.constants = exprExtractor.getConstants();

        this.toReplace.addAll(this.localVariables.values());
        this.toReplace.addAll(this.classNames.values());
        this.toReplace.addAll(this.fieldNames.values());
        this.toReplace.addAll(this.methodNames.values());
        this.toReplace.addAll(this.constants.values());

        this.methodBody = exprExtractor.getBody();
    }

    public Map<Integer, String> getLocalVariables() { return localVariables; }

    public Map<Integer, String> getClassNames() { return classNames; }

    public Map<Integer, String> getFieldNames() { return fieldNames; }

    public Map<Integer, String> getMethodNames() { return methodNames; }

    public Map<Integer, String> getConstants() { return constants; }
}
