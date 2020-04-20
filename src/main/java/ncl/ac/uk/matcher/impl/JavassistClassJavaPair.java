package ncl.ac.uk.matcher.impl;

import javassist.*;
import javassist.bytecode.*;
import ncl.ac.uk.matcher.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

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
    public List<BytecodeRepresentation> getBytecodeRepresentations(String dotJavaFilename) throws Exception {

        List<BytecodeRepresentation> bytecodeRepresentations = new LinkedList<>();

        // Checks if the parameter dotJavaFile matches a .java file in this object
        if (this._pairs.containsKey(dotJavaFilename))
            //sentences = javassistUtility.extractSentences(this._pairs.get(dotJavaFilename));
            bytecodeRepresentations = javassistUtility.extractSentences(this._pairs.get(dotJavaFilename));

        return bytecodeRepresentations;
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
            List<String> dotJavaFiles = JARUtility.ListDotJavaFiles(srcJarPath);

            // Read the JAR file that contains .class files
            JarFile jarFile = new JarFile(new File(binJarPath));

            // Create a default ClassPool object
            ClassPool classPool = ClassPool.getDefault();

            for (JarEntry jarEntry : Collections.list(jarFile.entries())) {

                String name = jarEntry.getName();

                if (name.endsWith(".class") &&
                        !name.contains("module-info")) {

                    // extracts the InputStream corresponding to the current .class
                    InputStream dotClassInputStream = jarFile.getInputStream(jarEntry);

                    // creates a CtClass object from the .class InputStream
                    CtClass ctClass = classPool.makeClass(dotClassInputStream);

                    // FIXED bug when source file is not available
                    // Filter interfaces (do not have implementation)
                    if (ctClass.getClassFile().getAttribute("SourceFile") != null) {

                        String packageName = ctClass.getPackageName().replaceAll("\\.", "/");

                        // Reads the .class source filename
                        String sourceFile = ((SourceFileAttribute) ctClass.getClassFile().getAttribute("SourceFile")).getFileName();

                        // Checks if there is a .java file matching the source filename
                        String fullReference = String.format("%s/%s", packageName, sourceFile);

                        if (dotJavaFiles.contains(fullReference)) {

                            // Updates or creates List of CtClass
                            List<CtClass> ctClasses = dotClassFiles.getOrDefault(fullReference, null);
                            if (ctClasses == null)
                                ctClasses = new LinkedList<>();

                            ctClasses.add(ctClass);

                            dotClassFiles.put(fullReference, ctClasses);
                        }
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
                for (CtMethod method : ctClass.getDeclaredMethods())
                    // If method is not empty
                    if (!method.isEmpty())
                        methodsNames.add(String.format("%s/%s", ctClass.getName(), method.getName()));

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
                //System.out.println(String.format("Class's name: %s", ctClass.getName()));
                // getDeclaredBehaviors returns constructors and methods of the CtClass object
                // getDeclaredMethods returns only methods of the CtClass object
                for (CtMethod method : ctClass.getDeclaredMethods()) {

                    // If method is not empty
                    if (!method.isEmpty()) {

                        // DEBUG
                        //System.out.println(String.format("Method's name: %s", method.getName()));
                        // extract a control flow graph
                        //ControlFlow controlFlow = new ControlFlow(ctClass, method.getMethodInfo());
                        //ControlFlow controlFlow = new ControlFlow(method);

                        //InstructionPrinterMod printer = new InstructionPrinterMod(System.out);

                        // Print all bytecode of the method
                        //printer.print(method);

                        //CodeAttribute codeAttribute = method.getMethodInfo().getCodeAttribute();
                        //CodeIterator ci = codeAttribute.iterator();
                        /*

                        Node[] nodes = controlFlow.dominatorTree();

                        for (Block block : controlFlow.basicBlocks()) {
                            int currentIndex = block.position();
                            int lineNumber = method.getMethodInfo().getLineNumber(currentIndex);
                            System.out.println(String.format("Line number: %s", lineNumber));

                            for (int i = 0; i < block.length(); i++) {

                                String instruction = InstructionPrinterMod.instructionString(ci, currentIndex + i, ctClass.getClassFile().getConstPool());
                                System.out.println(instruction);

                            }
                        }


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
            }

            return new LinkedList<>();
        }

        /**
         * This private method extracts Sentence(s) from a .class file
         *
         * @param ctClasses List of CtClass representing the .class files
         * @return List of Sentence from the .class file
         */
        private static List<BytecodeRepresentation> extractSentences(List<CtClass> ctClasses) throws Exception {

            List<BytecodeRepresentation> bytecodeRepresentations = new LinkedList<>();

            // For each class in the collection of classes in dotJavaFile
            for (CtClass ctClass : ctClasses)

                for (CtMethod method : ctClass.getDeclaredMethods()) {

                    // If method is not empty
                    if (!method.isEmpty()) {

                        // Here, the ByteArrayOutputStream and the UTF_8 are used to convert the
                        // PrintStream to a String. This conversion is needed because methods'
                        // bytecode need to be post-processed
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        String utf8 = StandardCharsets.UTF_8.name();

                        try (PrintStream ps = new PrintStream(baos, true, utf8)) {
                            // Javassist printer (modified)
                            InstructionPrinterMod printer = new InstructionPrinterMod(ps);
                            // Print all bytecode of the method to the PrintStream
                            printer.print(method);
                            // Convert the PrintStream to String
                            bytecodeRepresentations.add(new BytecodeRepresentationImpl(
                                    ctClass.getName(),
                                    method.getMethodInfo(),
                                    baos.toString(utf8)));
                        }
                    }
                }

            return bytecodeRepresentations;
        }

    }

}
