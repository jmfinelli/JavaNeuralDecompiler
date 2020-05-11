package com.redhat.jhalliday.impl.javassist;

import com.redhat.jhalliday.impl.FinalMethodWrapper;
import javassist.CtMethod;

public class CtMethodFinalWrapper extends FinalMethodWrapper<CtMethod> {

    public CtMethodFinalWrapper(CtMethod method) {

        ParameterExtractor exprExtractor = new ParameterExtractor(method, "\n");

        this.method = method;
        this.name = method.getLongName();

        this.localVariables = exprExtractor.getVariableNames();
        this.methodNames = exprExtractor.getMethodNames();
        this.classNames = exprExtractor.getClassNames();
        this.fieldNames = exprExtractor.getFieldNames();
        this.constants = exprExtractor.getConstants();

        this.methodBody = exprExtractor.getBody();
    }
}
