package com.redhat.jhalliday.impl.javassist;

import com.redhat.jhalliday.TransformerFunction;
import com.redhat.jhalliday.impl.ClassWrapper;
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
            ClassPool classPool = new ClassPool();
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
}
