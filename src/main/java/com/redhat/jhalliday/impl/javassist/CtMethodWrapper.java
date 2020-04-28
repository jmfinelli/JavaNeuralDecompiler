package com.redhat.jhalliday.impl.javassist;

import com.redhat.jhalliday.impl.MethodWrapper;
import javassist.CtMethod;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.MethodInfo;

public class CtMethodWrapper extends MethodWrapper<CtMethod> {

    public CtMethodWrapper(CtMethod ctMethod) {
        name = ctMethod.getName();
        method = ctMethod;

        MethodInfo methodInfo = ctMethod.getMethodInfo();
        CodeAttribute ca = methodInfo.getCodeAttribute();
        startLine = Integer.MAX_VALUE;
        endLine = Integer.MIN_VALUE;
        for (int i = 0; i < ca.getCodeLength(); i++) {
            int line = ctMethod.getMethodInfo().getLineNumber(i);
            if (line != -1) {
                startLine = Math.min(startLine, line);
                endLine = Math.max(endLine, line);
            }
        }
    }
}
