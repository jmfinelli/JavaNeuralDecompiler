package com.redhat.jhalliday.impl.javassist;

import com.redhat.jhalliday.TransformerFunction;
import com.redhat.jhalliday.impl.ClassWrapper;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.bytecode.SignatureAttribute;
import javassist.bytecode.SyntheticAttribute;

import java.util.*;
import java.util.stream.Stream;

public class CtClassToCtMethodsOldTransformerFunction implements TransformerFunction<CtClass, CtMethod> {

    @Override
    public Stream<CtMethod> apply(CtClass ctClass) {

        //final List<CtMethod> interestingMethods = new ArrayList<>();
        final Map<String, List<CtMethod>> interestingMethods = new HashMap<>();

        // unlike asm, this does not include constructors or static initializers.
        for (CtMethod ctMethod : ctClass.getDeclaredMethods()) {
            if (isInteresting(ctMethod)) {
                //interestingMethods.add(ctMethod);
                try {
                    /*
                     * CtClass.getDeclaredMethods() does not return overloaded methods.
                     * On the other side, CtClass.getDeclaredMethods(String) give all overloaded methods
                     * for a particular one. Combining the two methods, gives the complete collection
                     * of methods of the class.
                     */
                    interestingMethods.putIfAbsent(ctMethod.getName(), Arrays.asList(ctClass.getDeclaredMethods(ctMethod.getName())));
                } catch (javassist.NotFoundException ex) {
                    /*
                     * Nothing to do as NotFoundException cannot be thrown
                     */
                }
            }
        }

        List<CtMethod> results = new ArrayList<>();
        interestingMethods.entrySet().forEach(x -> results.addAll(x.getValue()));

        return results.stream();
    }

    static final int SYNTHETIC = 0x00001000;

    public boolean isInteresting(CtMethod ctMethod) {

        // ignore empty (mostly interface) methods as they have no bytecode to translate
        // ignore synthetic (compiler generated) methods as they have no sourcecode to translate
        if (ctMethod.isEmpty() ||
                ctMethod.getMethodInfo().getAttribute(SignatureAttribute.tag) != null ||
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
