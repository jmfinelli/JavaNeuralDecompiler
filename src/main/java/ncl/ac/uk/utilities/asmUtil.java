package ncl.ac.uk.utilities;

import jdk.internal.org.objectweb.asm.ClassReader;
import jdk.internal.org.objectweb.asm.tree.AbstractInsnNode;
import jdk.internal.org.objectweb.asm.tree.ClassNode;
import jdk.internal.org.objectweb.asm.tree.InsnList;
import jdk.internal.org.objectweb.asm.tree.MethodNode;
import jdk.internal.org.objectweb.asm.util.Printer;
import jdk.internal.org.objectweb.asm.util.Textifier;
import jdk.internal.org.objectweb.asm.util.TraceMethodVisitor;

import java.io.*;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;

public class asmUtil {

    private static Printer printer = new Textifier();
    private static TraceMethodVisitor mp = new TraceMethodVisitor(printer);

    /**
     * From https://stackoverflow.com/questions/19152526/asm-outputting-java-bytecode-and-opcode
     * @param dotClassFile InputStream that represents the .class file
     * @return 
     */
    public static String readMethodsByteCodes(InputStream dotClassFile) throws IOException {

        ClassNode classNode = extractClassNode(dotClassFile);

        StringBuilder builder = new StringBuilder();
        final List<MethodNode> methods = classNode.methods;
        for (MethodNode m : methods) {
            InsnList inList = m.instructions;
            builder.append(m.name);
            for (int i = 0; i < inList.size(); i++) {
                builder.append(insnToString(inList.get(i)));
            }
        }

        return builder.toString();
    }

    /**
     * This method verifies that the .class file passed as InputStream is contained in the list of .java files
     * @param dotClassFile .class file as InputStream
     * @param dotJavaFiles List of .java files
     * @return True if dotClassFile is in dotJavaFiles
     * @throws IOException
     */
    public static Map.Entry<String, ClassNode> checkDotClassMatchesDotJava(InputStream dotClassFile, List<String> dotJavaFiles) throws IOException {

        ClassNode classNode = extractClassNode(dotClassFile);

        return (dotJavaFiles.contains(classNode.sourceFile)?
                new AbstractMap.SimpleEntry(dotJavaFiles.get(dotJavaFiles.indexOf(classNode.sourceFile)), classNode):
                null);
    }

    private static String insnToString(AbstractInsnNode insn){
        insn.accept(mp);
        StringWriter sw = new StringWriter();
        printer.print(new PrintWriter(sw));
        printer.getText().clear();
        return sw.toString();
    }

    private static ClassNode extractClassNode(InputStream dotClassFile) throws IOException {

        ClassReader reader = new ClassReader(dotClassFile);
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, 0);

        return classNode;
    }
}
