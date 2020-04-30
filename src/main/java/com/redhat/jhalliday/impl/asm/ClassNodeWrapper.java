package com.redhat.jhalliday.impl.asm;

import com.redhat.jhalliday.impl.ClassWrapper;
import org.objectweb.asm.tree.ClassNode;

public class ClassNodeWrapper extends ClassWrapper<ClassNode> {

    public ClassNodeWrapper(ClassNode classNode) {
        clazz = classNode;
        sourceFileName = classNode.sourceFile;
    }
}
