package com.redhat.jhalliday.impl.javassist;

import com.redhat.jhalliday.TransformerFunction;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.bytecode.SyntheticAttribute;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class CtClassToCtMethodsTransformerFunction implements TransformerFunction<CtClass, CtMethod> {

    @Override
    public Stream<CtMethod> apply(CtClass ctClass) {

        final List<CtMethod> interestingMethods = new ArrayList<>();

        // unlike asm, this does not include constructors or static initializers.
        for (CtMethod ctMethod : ctClass.getDeclaredMethods()) {
            if (isInteresting(ctMethod)) {
                interestingMethods.add(ctMethod);
            }
        }

        return interestingMethods.stream();
    }

    static final int SYNTHETIC = 0x00001000;

    public boolean isInteresting(CtMethod ctMethod) {

        // ignore empty (mostly interface) methods as they have no bytecode to translate
        // ignore synthetic (compiler generated) methods as they have no sourcecode to translate
        if (ctMethod.isEmpty() ||
                ctMethod.getMethodInfo().getAttribute(SyntheticAttribute.tag) != null ||
                (ctMethod.getMethodInfo().getAccessFlags() & SYNTHETIC) != 0 ||
                Modifier.isAbstract(ctMethod.getMethodInfo().getAccessFlags())
        ) {
            return false;
        }

        // asm considers method bodies containing only a return statement to be empty, even though they are not.

        boolean hasLineNumbers = ctMethod.getMethodInfo().getLineNumber(0) != -1;

        return hasLineNumbers;
    }
}
