package ncl.ac.uk.transformation.impl.javassist;

import javassist.ClassPool;
import javassist.CtClass;
import ncl.ac.uk.transformation.TransformerFunction;

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
            ClassPool classPool = new ClassPool();
            CtClass ctClass = classPool.makeClass(new ByteArrayInputStream(bytes));
            return ctClass;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
