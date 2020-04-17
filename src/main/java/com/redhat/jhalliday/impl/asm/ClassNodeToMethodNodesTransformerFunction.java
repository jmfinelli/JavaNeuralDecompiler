package com.redhat.jhalliday.impl.asm;

import com.redhat.jhalliday.TransformerFunction;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Stream;

public class ClassNodeToMethodNodesTransformerFunction implements TransformerFunction<ClassNode, MethodNode> {

    @Override
    public Stream<MethodNode> apply(ClassNode classNode) {

        final List<MethodNode> interestingMethods = new ArrayList<>();

        for (MethodNode methodNode : classNode.methods) {
            if (isInteresting(methodNode)) {
                interestingMethods.add(methodNode);
            }
        }

        return interestingMethods.stream();
    }

    public boolean isInteresting(MethodNode methodNode) {

        // ignore empty (mostly interface) methods as they have no bytecode to translate
        // ignore synthetic (compiler generated) methods as they have no sourcecode to translate
        if (methodNode.instructions.size() == 0 ||
                (methodNode.access & Opcodes.ACC_SYNTHETIC) != 0 ||
                (methodNode.access & Opcodes.ACC_ABSTRACT) != 0) {
            return false;
        }

        // match the behaviour of javassist, which don't include these in declaredMethods
        if (methodNode.name.equals("<init>") || methodNode.name.equals("<clinit>")) {
            return false;
        }

        // a non-zero size doesn't necessarily mean it has executable code,
        // as e.g. line numbers and labels are considered 'instructions'.

        boolean hasLineNumbers = false;
        List<AbstractInsnNode> executableNodes = new ArrayList<>();
        InsnList insnList = methodNode.instructions;
        ListIterator<AbstractInsnNode> iter = insnList.iterator();
        while (iter.hasNext()) {
            AbstractInsnNode node = iter.next();
            if (node instanceof LineNumberNode) {
                hasLineNumbers = true;
            } else if (node instanceof LabelNode) {
                // skip
            } else {
                executableNodes.add(node);
            }
        }

        if (executableNodes.size() == 1 && executableNodes.get(0).getOpcode() == Opcodes.RETURN) {
            return false; // mimics javassist's isEmpty behaviour
        }

        return hasLineNumbers;
    }
}
