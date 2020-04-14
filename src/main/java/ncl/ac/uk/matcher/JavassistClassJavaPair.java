package ncl.ac.uk.matcher;

import com.sun.org.apache.bcel.internal.generic.Instruction;
import io.disassemble.javanalysis.CtMethodExtensionKt;
import io.disassemble.javanalysis.flow.ControlFlowGraph;
import io.disassemble.javanalysis.util.CodeParser;
import io.disassemble.javanalysis.util.insn.InsnUtil;
import javassist.*;
import javassist.bytecode.*;
import javassist.bytecode.analysis.ControlFlow;
import javassist.bytecode.analysis.ControlFlow.*;
import ncl.ac.uk.utilities.JarReaderUtil;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import io.disassemble.javanalysis.insn.*;

import java.util.regex.*;

//TODO: modify this class to be immutable

/**
 * This class handles the connection between a library's source codes and related bytecode.
 * Basically, this class creates the connection between .class files and related .java files.
 */
public class JavassistClassJavaPair implements ClassJavaPair {

    /* ************************************************
     * ************************************************
     * *************** Class's Fields *****************
     * ************************************************
     * ************************************************/

    // Path of the JAR that contains .class files
    private final String _binJARFullPath;
    // Path of the JAR that contains .java files
    private final String _srcJARFullPath;
    // Map that stores connections between .class and .java files
    private final Map<String, List<CtClass>> _pairs;

    /* ************************************************
     * ************************************************
     * ***************** Constructors *****************
     * ************************************************
     * ************************************************/

    public JavassistClassJavaPair(String binJARFullPath, String srcJARFullPath) throws IOException {

        String[] splitArray = binJARFullPath.split("/");
        String binJARFilename = splitArray[splitArray.length - 1];

        // Check if the path binJARFullPath ends with a .jar file
        if (!binJARFilename.endsWith(".jar"))
            throw new IllegalArgumentException(String.format("%s is not a .jar file.", binJARFilename));

        // Check if the two paths (e.g. binJARFullPath and srcJARFullPath) are related to the same library name
        if (!srcJARFullPath.contains(binJARFilename.substring(0, binJARFilename.length() - ".jar".length())))
            throw new IllegalArgumentException("Wrong source library path.");

        this._binJARFullPath = binJARFullPath;
        this._srcJARFullPath = srcJARFullPath;

        this._pairs = javassistUtility.extractCtClassesFromJAR(binJARFullPath, srcJARFullPath);
    }

    /* ************************************************
     * ************************************************
     * **************** Public APIs *******************
     * ************************************************
     * ************************************************/

    /**
     * This method creates a List of DecompilationRecord where each record is a pair of
     * Bytecode and related source code.
     *
     * @param dotJavaFilename .java file to analyse
     * @return List of DecompilationRecord
     */
    @Override
    public List<DecompilationRecord> getDecompilationRecords(String dotJavaFilename) throws Exception {

        List<DecompilationRecord> records = new LinkedList<>();

        // Checks if the parameter dotJavaFile matches a .java file in this object
        if (this._pairs.containsKey(dotJavaFilename)) {

            String sourceCode = JARUtility.extractSourceCode(this._srcJARFullPath, dotJavaFilename);

            // TODO: substitute dotJavaFile with the actual file.
            records = javassistUtility.extractDecompilationRecordsFromDotClass(sourceCode, this._pairs.get(dotJavaFilename));
        }

        return records;
    }

    @Override
    public List<String> getMethodsNames(String dotJavaFile) {

        return javassistUtility.extractMethodsNames(this._pairs.get(dotJavaFile));
    }

    @Override
    public List<Sentence> getSentences(String dotJavaFilename) throws Exception {

        List<Sentence> sentences = new LinkedList<>();

        // Checks if the parameter dotJavaFile matches a .java file in this object
        if (this._pairs.containsKey(dotJavaFilename)) {

            String sourceCode = JARUtility.extractSourceCode(this._srcJARFullPath, dotJavaFilename);

            sentences = javassistUtility.extractSentences(this._pairs.get(dotJavaFilename));


        }

        return sentences;
    }

    /* ************************************************
     * ************************************************
     * **************** GETTER/SETTER *****************
     * ************************************************
     * ************************************************/

    @Override
    public String getSrcPath() { return this._srcJARFullPath; }

    @Override
    public String getBinPath() { return this._binJARFullPath; }

    @Override
    public String getSourceJARFile() {
        String[] splitArray = this._srcJARFullPath.split("/");
        return splitArray[splitArray.length - 1];
    }

    @Override
    public List<String> getDotJavaFiles() { return new LinkedList<>(this._pairs.keySet()); }

    /* ************************************************
     * ************************************************
     * ******************* Private ********************
     * ************************************************
     * ************************************************/

    /* ************************************************
     * ************************************************
     * *************** Utility Classes ****************
     * ************************************************
     * ************************************************/

    private static class javassistUtility {

        /**
         * This private method creates a List of .class files and corresponding .java files
         *
         * @param binJarPath path of the JAR file that contains .class files
         * @return Map where entries are identified with the .java's filename while .class are represented with CtClass
         */
        private static Map<String, List<CtClass>> extractCtClassesFromJAR(String binJarPath, String srcJarPath) throws IOException {

            // HashMap to insert .class files that matches .java files
            Map<String, List<CtClass>> dotClassFiles = new HashMap<>();

            // List all .java files in the source .jar file
            List<String> dotJavaFiles = JarReaderUtil.ListDotJavaFiles(srcJarPath);

            // Read the JAR file that contains .class files
            JarFile jarFile = new JarFile(new File(binJarPath));

            // Create a default ClassPool object
            ClassPool classPool = ClassPool.getDefault();

            for (JarEntry jarEntry : Collections.list(jarFile.entries())) {

                String name = jarEntry.getName();

                // The second condition could be excluded if module-info is removed from jar files
                // directly through the POM file. There are plugins (such as AntRun) that can do that but I
                // wasted too much time trying to understand how to configure the plugin to run in the package
                // phase. TODO: ask Jonathan to help with this.
                if (name.endsWith(".class") &&
                        !name.startsWith("module-info")) {

                    // extracts the InputStream corresponding to the current .class
                    InputStream dotClassInputStream = jarFile.getInputStream(jarEntry);

                    // creates a CtClass object from the .class InputStream
                    CtClass ctClass = classPool.makeClass(dotClassInputStream);

                    // Reads the .class source filename
                    String sourceFile = ((SourceFileAttribute)ctClass.getClassFile().getAttribute("SourceFile")).getFileName();

                    // Checks if there is a .java file matching the source filename
                    if (dotJavaFiles.stream().anyMatch(x -> x.contains(sourceFile))) {

                        // Updates or creates List of CtClass
                        List<CtClass> ctClasses = dotClassFiles.getOrDefault(sourceFile, null);
                        if (ctClasses == null)
                            ctClasses = new LinkedList<>();

                        ctClasses.add(ctClass);

                        dotClassFiles.put(sourceFile, ctClasses);
                    }
                }

            }

            return dotClassFiles;
        }

        /**
         * This private method extracts names of methods in the .class files
         *
         * @param ctClasses List of CtClass representing the .class files
         * @return List of String
         */
        private static List<String> extractMethodsNames(List<CtClass> ctClasses) {

            List<String> methodsNames = new LinkedList<>();

            // For each class in the collection of classes in dotJavaFile
            for (CtClass ctClass : ctClasses)
                // For each method in the class
                for (CtBehavior behavior : ctClass.getDeclaredBehaviors())
                    methodsNames.add(String.format("%s/%s", ctClass.getName(), behavior.getName()));

            return methodsNames;
        }

        /**
         * This private method extracts DecompilationRecord(s) from a .class file
         *
         * @param sourceFile .java source code
         * @param ctClasses List of CtClass representing the .class files
         * @return List of DecompilationRecord
         */
        private static List<DecompilationRecord> extractDecompilationRecordsFromDotClass(String sourceFile, List<CtClass> ctClasses) throws Exception {

            // For each class in the collection of classes in dotJavaFile
            for (CtClass ctClass : ctClasses) {
                // DEBUG
                System.out.println(String.format("Class's name: %s", ctClass.getName()));
                // getDeclaredBehaviors returns constructors and methods of the CtClass object
                // getDeclaredMethods returns only methods of the CtClass object
                for (CtMethod method : ctClass.getDeclaredMethods()) {

                    // DEBUG
                    System.out.println(String.format("Method's name: %s", method.getName()));
                    // extract a control flow graph
                    //ControlFlow controlFlow = new ControlFlow(ctClass, method.getMethodInfo());
                    ControlFlow controlFlow = new ControlFlow(method);

                    List<CtInsn> ctInsns = CtMethodExtensionKt.getInstructions(method);

                    InstructionPrinter printer = new InstructionPrinter(System.out);

                    // Print all bytecode of the method
                    printer.print(method);

                    CodeAttribute codeAttribute = method.getMethodInfo().getCodeAttribute();
                    CodeIterator ci = codeAttribute.iterator();

                    Node[] nodes = controlFlow.dominatorTree();

                    for(Block block : controlFlow.basicBlocks()) {
                        int currentIndex = block.position();
                        int lineNumber = method.getMethodInfo().getLineNumber(currentIndex);
                        System.out.println(String.format("Line number: %s", lineNumber));

                        for(int i = 0; i < block.length(); i++) {

                            String instruction = InstructionPrinter.instructionString(ci, currentIndex + i, ctClass.getClassFile().getConstPool());
                            System.out.println(instruction);

                        }
                    }

                    /*
                    CodeAttribute codeAttribute = method.getMethodInfo().getCodeAttribute();
                    CodeIterator ci = codeAttribute.iterator();

                    for(Block block : controlFlow.basicBlocks()) {
                        int currentIndex = block.position();
                        int lineNumber = method.getMethodInfo().getLineNumber(currentIndex);
                        System.out.println(String.format("Line number: %s", lineNumber));

                        for(int i = 0; i < block.length(); i++) {

                            int op = ci.byteAt(currentIndex + i);
                            System.out.println(Mnemonic.OPCODE[op]);
                        }
                    }

                    System.out.println("BYTECODE");
                    while (ci.hasNext()) {
                        int index = ci.next();
                        int op = ci.byteAt(index);
                        System.out.println(Mnemonic.OPCODE[op]);
                    }

                    for (Block block : controlFlow.basicBlocks()) {
                        int position = block.position();
                        int lineNumber = behavior.getMethodInfo().getLineNumber(position);
                        System.out.println(lineNumber);

                    }
                    */
                }
            }

            return new LinkedList<>();
        }

        /**
         * This private method extracts Sentence(s) from a .class file
         *
         * @param ctClasses List of CtClass representing the .class files
         * @return List of Sentence from the .class file
         */
        private static List<Sentence> extractSentences(List<CtClass> ctClasses) throws Exception {

            List<Sentence> sentences = new LinkedList<>();

            // For each class in the collection of classes in dotJavaFile
            for (CtClass ctClass : ctClasses)

                for (CtMethod method : ctClass.getDeclaredMethods()) {

                    // Here, the ByteArrayOutputStream and the UTF_8 are used to convert the
                    // PrintStream to a String. This conversion is needed because methods'
                    // bytecode need to be post-processed
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    String utf8 = StandardCharsets.UTF_8.name();

                    try (PrintStream ps = new PrintStream(baos, true, utf8)) {
                        // Javassist printer
                        InstructionPrinter printer = new InstructionPrinter(ps);
                        // Print all bytecode of the method to the PrintStream
                        printer.print(method);
                        // Convert the PrintStream to String
                        sentences.add(
                                sentencePostProcessing(
                                ctClass.getName(),
                                method.getName(),
                                baos.toString(utf8)));
                    }
                }

            return sentences;
        }

        /**
         * This private method post-processes the String passed as parameter
         * @param className Name of the class
         * @param methodName Name of the method
         * @param sentence Sentence to post process
         * @return post processed String
         */
        private static Sentence sentencePostProcessing(String className, String methodName, String sentence) {

            Pattern pattern = Pattern.compile("(#\\d+)(\\s=\\s\\w+\\s)([^\\s]+)");
            Pattern costantPattern = Pattern.compile("(#\\d+)(\\s=\\s)\"(.*)\"");
            Pattern tokenPattern = Pattern.compile("(\\w+.+\\n)");

            Map<String, String> constantPool = new HashMap<>();

            String sentenceWithoutLineNumber = sentence.replaceAll("\\d+:\\s", "");
            Matcher matcher = pattern.matcher(sentenceWithoutLineNumber);
            Matcher costantMatcher = costantPattern.matcher(sentenceWithoutLineNumber);

            while (matcher.find())
                constantPool.put(matcher.group(1), matcher.group(3));
            while (costantMatcher.find())
                constantPool.put(costantMatcher.group(1), costantMatcher.group(3));

            sentenceWithoutLineNumber = sentenceWithoutLineNumber.replaceAll(pattern.pattern(), "$1");
            sentenceWithoutLineNumber = sentenceWithoutLineNumber.replaceAll(costantPattern.pattern(), "$1");

            Matcher tokenMatcher = tokenPattern.matcher(sentenceWithoutLineNumber);

            int tokens = 0;
            while(tokenMatcher.find())
                tokens++;

            return new BytecodeSentence(
                    className,
                    methodName,
                    sentenceWithoutLineNumber,
                    constantPool,
                    tokens
            );
        }
    }

    /**
     * Utility class used by the outer-class to analyse JAR files. The inner-class is declared
     * private because ClassJavaPair is designed to be the only class to interact with JAR files.
     */
    private static class JARUtility {

        /**
         * This method lists all .java files in a JAR file
         *
         * @param jarPath path of the JAR file to analyse
         * @return A list of Strings that contains all .java filenames found in the JAR
         */
        private static List<String> ListDotJavaFiles(String jarPath) throws IOException {

            //List<String> dotJavaFiles = ListFilesInJAR(jarPath, "java");

            /**
             * This was commented out on the advice of Jonathan.
             * TODO: ask Jonathan if in the same JAR there can be same .java files
             *
            // This little modification was added because ClassNode holds only the filename
            // of the .java file and not the entire package path.
            dotJavaFiles.replaceAll(x -> {
                String[] tempArray = x.split("/");
                return tempArray[tempArray.length - 1];
            });
            */

            //return dotJavaFiles;
            return ListFilesInJAR(jarPath, "java");
        }

        /**
         * This private method reads all files in a JAR file
         *
         * @param jarFullPath path of the JAR file to analyse
         * @param extension   This is the extension the method will focus on
         * @return List of the files read
         */
        private static List<String> ListFilesInJAR(String jarFullPath, String extension) throws IOException {

            List<String> dotClassFiles = new LinkedList<>();

            JarFile jarFile = new JarFile(new File(jarFullPath));

            for (JarEntry jarEntry : Collections.list(jarFile.entries())) {
                String name = jarEntry.getName();
                if (name.endsWith(String.format(".%s", extension)))
                    dotClassFiles.add(name);
            }

            return dotClassFiles;
        }

        private static String extractSourceCode(String srcJarPath, String dotJavaFilename) throws IOException {

            // Read the JAR file that contains .class files
            JarFile jarFile = new JarFile(new File(srcJarPath));

            List<String> dotJavaFiles = ListDotJavaFiles(srcJarPath);
            String completeDotJavaFilename = dotJavaFiles.stream().filter(x -> x.contains(dotJavaFilename)).collect(toSingleton());
            JarEntry jarEntry = jarFile.getJarEntry(completeDotJavaFilename);

            InputStream dotJavaFile = jarFile.getInputStream(jarEntry);

            return "";
        }

        /**
         * https://stackoverflow.com/questions/22694884/filter-java-stream-to-1-and-only-1-element
         * @param <T> Generic
         * @return a collector to use in a Stream
         */
        private static <T> Collector<T, ?, T> toSingleton() {
            return Collectors.collectingAndThen(
                    Collectors.toList(),
                    list -> {
                        if (list.size() != 1) {
                            throw new IllegalStateException();
                        }
                        return list.get(0);
                    }
            );
        }
    }

}
