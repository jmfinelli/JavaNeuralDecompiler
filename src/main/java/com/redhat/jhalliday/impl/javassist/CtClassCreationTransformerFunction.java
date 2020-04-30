package com.redhat.jhalliday.impl.javassist;

import com.redhat.jhalliday.TransformerFunction;
import javassist.ClassPool;
import javassist.CtClass;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class CtClassCreationTransformerFunction implements TransformerFunction<Map<String, byte[]>, Map<String, CtClass>> {

    @Override
    public Stream<Map<String, CtClass>> apply(Map<String, byte[]> map) {

        Map<String, CtClass> result = new HashMap<>();

        map.forEach((key, bytes) -> {
            CtClass classNode = apply(bytes);
            result.put(key, classNode);
        });

        return Stream.of(result);
    }

    public CtClass apply(byte[] bytes) {

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
    }
}
