package ncl.ac.uk.transformation.impl.asm;

import ncl.ac.uk.transformation.TransformerFunction;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class ClassNodeCreationTransformerFunction implements TransformerFunction<Map<String, byte[]>, Map<String, ClassNode>> {

    @Override
    public Stream<Map<String, ClassNode>> apply(Map<String, byte[]> map) {

        Map<String, ClassNode> result = new HashMap<>();

        map.forEach((key, bytes) -> {
            ClassNode classNode = apply(bytes);
            result.put(key, classNode);
        });

        return Stream.of(result);
    }

    public ClassNode apply(byte[] bytes) {

        ClassReader reader = new ClassReader(bytes);
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, 0);

        return classNode;
    }
}
