package com.redhat.jhalliday.impl.javassist;

import com.redhat.jhalliday.TransformerFunction;
import com.redhat.jhalliday.impl.ClassWrapper;
import com.redhat.jhalliday.impl.FinalLowLevelMethodWrapper;
import com.redhat.jhalliday.impl.MethodWrapper;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.function.Function;

public class JavassistFunctions {

    public static Function<byte[], CtClass> classCreationFunction =
     bytes -> {
        try {
            /*
             * Using ClassPool.getDefault() to save all the possible Types in the ClassPool.
             * This is fundamental when it comes to fetching information about methods's parameters.
             */
            ClassPool classPool = ClassPool.getDefault();
            CtClass ctClass = classPool.makeClass(new ByteArrayInputStream(bytes));
            return ctClass;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    };

    public static Function<CtClass, ClassWrapper<CtClass>> classWrappingFunction = CtClassWrapper::new;

    public static TransformerFunction<ClassWrapper<CtClass>, CtMethod> classShreddingFunction =
            new CtClassToCtMethodsTransformerFunction();

    public static Function<CtMethod, MethodWrapper<CtMethod>> methodWrappingFunction = CtMethodWrapper::new;

    public static Function<CtMethod, FinalLowLevelMethodWrapper<CtMethod>> finalMethodWrapperFunction = CtMethodFinalWrapper::new;
}
