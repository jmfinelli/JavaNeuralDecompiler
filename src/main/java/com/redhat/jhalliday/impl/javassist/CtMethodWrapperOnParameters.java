package com.redhat.jhalliday.impl.javassist;

import com.redhat.jhalliday.impl.MethodWrapperOnParameters;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

import java.util.Arrays;
import java.util.List;

public class CtMethodWrapperOnParameters extends MethodWrapperOnParameters<CtMethod> {

    public CtMethodWrapperOnParameters(CtMethod ctMethod) {

        name = ctMethod.getName();
        method = ctMethod;

        try {
            returnParameterType = ctMethod.getReturnType().getName();

            List<CtClass> parametersFromBytecode = Arrays.asList(ctMethod.getParameterTypes());

            parametersFromBytecode.forEach(x -> parametersTypes.add(x.getName()));

        } catch (NotFoundException ignore) {
        }
    }
}
