package com.redhat.jhalliday.impl.javassist;

import com.redhat.jhalliday.impl.FinalLowLevelMethodWrapper;
import javassist.CtMethod;

public class CtMethodFinalWrapper extends FinalLowLevelMethodWrapper<CtMethod> {

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
}
