package com.redhat.jhalliday.impl.asm;

import com.redhat.jhalliday.TransformerFunction;
import com.redhat.jhalliday.impl.ClassWrapper;
import com.redhat.jhalliday.impl.MethodWrapper;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.function.Function;

public class AsmFunctions {

    public static Function<byte[], ClassNode> classCreationFunction =
            bytes -> {
                ClassReader reader = new ClassReader(bytes);
                ClassNode classNode = new ClassNode();
                reader.accept(classNode, 0);
                return classNode;
            };

    public static Function<ClassNode, ClassWrapper<ClassNode>> classWrappingFunction = ClassNodeWrapper::new;

    public static TransformerFunction<ClassWrapper<ClassNode>, MethodNode> classShreddingFunction =
            new ClassNodeToMethodNodesTransformerFunction();

    public static Function<MethodNode, MethodWrapper<MethodNode>> methodWrappingFunction = MethodNodeWrapper::new;
}
