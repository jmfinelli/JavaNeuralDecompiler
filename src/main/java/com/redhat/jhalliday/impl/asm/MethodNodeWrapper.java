package com.redhat.jhalliday.impl.asm;

import com.redhat.jhalliday.impl.MethodWrapper;
import org.objectweb.asm.tree.*;

public class MethodNodeWrapper extends MethodWrapper<MethodNode> {

    public MethodNodeWrapper(MethodNode methodNode) {
        name = methodNode.name;
        method = methodNode;

        startLine = Integer.MAX_VALUE;
        endLine = Integer.MIN_VALUE;
        InsnList insnList = methodNode.instructions;
        for (AbstractInsnNode node : insnList) {
            if (node instanceof LineNumberNode) {
                int line = ((LineNumberNode) node).line;
                startLine = Math.min(startLine, line);
                endLine = Math.max(endLine, line);
            }
        }
    }
}
