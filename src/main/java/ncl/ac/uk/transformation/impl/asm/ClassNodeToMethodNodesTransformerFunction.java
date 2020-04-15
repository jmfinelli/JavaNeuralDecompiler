package ncl.ac.uk.transformation.impl.asm;

import ncl.ac.uk.transformation.TransformerFunction;
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

    private boolean isInteresting(MethodNode methodNode) {
        // ignore empty (mostly interface) methods as they have no bytecode to translate
        // ignore synthetic (compiler generated) methods as they have no sourcecode to translate
        if (methodNode.instructions.size() == 0 || (methodNode.access & Opcodes.ACC_SYNTHETIC) != 0) {
            return false;
        }

        boolean hasLineNumbers = false;
        InsnList insnList = methodNode.instructions;
        ListIterator<AbstractInsnNode> iter = insnList.iterator();
        while (iter.hasNext()) {
            AbstractInsnNode node = iter.next();
            if (node instanceof LineNumberNode) {
                hasLineNumbers = true;
                break;
            }
        }

        return hasLineNumbers;
    }
}
