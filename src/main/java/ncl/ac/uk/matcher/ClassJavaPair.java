package ncl.ac.uk.matcher;

import jdk.internal.org.objectweb.asm.ClassReader;
import jdk.internal.org.objectweb.asm.tree.ClassNode;
import jdk.internal.org.objectweb.asm.tree.MethodNode;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

//TODO: modify this class to be immutable

public class ClassJavaPair implements ClassJavaInterface {

    /* ************************************************
     * ************************************************
     * *************** Class's Fields *****************
     * ************************************************
     * ************************************************/

    private final String _binJARFullPath;
    private final String _srcJARFullPath;
    private final Map<String, List<ClassNode>> _pairs;

    /* ************************************************
     * ************************************************
     * ***************** Constructors *****************
     * ************************************************
     * ************************************************/

    /*
    public ClassJavaPair(List<ClassNode> classNodes, String dotJavaFile) {


        if (!classNodes.stream().allMatch(x -> dotJavaFile.equals(x.sourceFile)))
            throw new IllegalArgumentException(String.format("%s is not the source file for all .class files in the list.", dotJavaFile));

        this._classNodes = classNodes;
        this._dotJavaFile = dotJavaFile;
    }
    */

    public ClassJavaPair(String binJARFullPath, String srcJARFullPath) throws IOException {

        String[] splittedArray = binJARFullPath.split("/");
        String binJARFilename = splittedArray[splittedArray.length - 1];

        // Check if the path binJARFullPath ends with a .jar file
        if (!binJARFilename.endsWith(".jar"))
            throw new IllegalArgumentException(String.format("%s is not a .jar file.", binJARFilename));

        // Check if the two paths (e.g. binJARFullPath and srcJARFullPath) are related to the same library name
        if (!srcJARFullPath.contains(binJARFilename.substring(0, binJARFilename.length() - ".jar".length())))
            throw new IllegalArgumentException("Wrong source library path.");

        this._binJARFullPath = binJARFullPath;
        this._srcJARFullPath = srcJARFullPath;

        this._pairs = asmUtility.extractClassNodes(binJARFullPath, srcJARFullPath);
    }

    /* ************************************************
     * ************************************************
     * **************** Public APIs *******************
     * ************************************************
     * ************************************************/

    @Override
    public Map<String, String> getMethodBytecodes(String methodName) {

        Map<String, String> bytecodePairs = new HashMap<>();

        return bytecodePairs;
    }

    @Override
    public Map<String, String> getMethodsBytecodes() {

        Map<String, String> bytecodePairs = new HashMap<>();

        return bytecodePairs;
    }

    @Override
    public List<String> getMethodsNames() {

        List<String> methodsNames = new LinkedList<>();

        for (Map.Entry<String, List<ClassNode>> classNodes : this._pairs.entrySet()) {
            for (ClassNode classNode : classNodes.getValue())
                for (MethodNode method : classNode.methods)
                    methodsNames.add(String.format("%s/%s", classNode.name, method.name));
        }

        return methodsNames;
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
    public String getDotJavaFile() {
        String[] splittedArray = this._srcJARFullPath.split("/");
        return splittedArray[splittedArray.length - 1];
    }

    /* ************************************************
     * ************************************************
     * *************** Utility Classes ****************
     * ************************************************
     * ************************************************/

    private static class asmUtility {

        /**
         * This private method creates a List of .class files and corresponding .java files
         *
         * @param binJarPath path of the JAR file that contains .class files
         * @return Map of String and InputStream corresponding, respectively, to .java and .class files
         * @throws IOException
         */
        private static Map<String, List<ClassNode>> extractClassNodes(String binJarPath, String srcJarPath) throws IOException {

            // HashMap to insert .class files that matches .java files
            Map<String, List<ClassNode>> dotClassFiles = new HashMap<>();

            // List all .java files in the source .jar file
            List<String> dotJavaFiles = JARUtility.ListDotJavaFiles(srcJarPath);

            // Read the JAR file that contains .class files
            JarFile jarFile = new JarFile(new File(binJarPath));

            for (JarEntry jarEntry : Collections.list(jarFile.entries())) {
                String name = jarEntry.getName();
                // The second condition could be excluded if module-info is removed from jar files
                // directly through the POM file. There are plugins (such as AntRun) that can do that but I
                // wasted too much time trying to understand how to configure the plugin to run in the package
                // phase. TODO: ask Jonathan to help with this.
                if (name.endsWith(".class") &&
                        !name.startsWith("module-info")) {
                    // extracts the InputStream corresponding to the current .class
                    InputStream dotClass = jarFile.getInputStream(jarEntry);
                    // fetches the .java file corresponding to the .class file (if there is one)
                    ClassNode classNode = extractClassNode(dotClass);

                    String sourceFile = classNode.sourceFile;
                    // Checks if there is a .java file matching the source filename

                    if (dotJavaFiles.contains(sourceFile)) {

                        // Update or create List of ClassNode
                        List<ClassNode> classNodes = dotClassFiles.getOrDefault(sourceFile, null);
                        if (classNodes == null)
                            classNodes = new LinkedList<>();

                        classNodes.add(classNode);

                        dotClassFiles.put(sourceFile, classNodes);
                    }
                }

            }

            return dotClassFiles;
        }

        /**
         * This private method convert an InputStream to a ClassNode.
         *
         * @param dotClassFile This input parameter is the InputStream containing the .class file
         * @return a new ClassNode object extracted from the .class file
         * @throws IOException
         */
        private static ClassNode extractClassNode(InputStream dotClassFile) throws IOException {

            ClassReader reader = new ClassReader(dotClassFile);
            ClassNode classNode = new ClassNode();
            reader.accept(classNode, 0);

            return classNode;
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
         * @throws IOException
         */
        private static List<String> ListDotJavaFiles(String jarPath) throws IOException {

            List<String> dotJavaFiles = ListFilesInJAR(jarPath, "java");

            // This little modification was added because ClassNode holds only the filename
            // of the .java file and not the entire package path.
            dotJavaFiles.replaceAll(x -> {
                String[] tempArray = x.split("/");
                return tempArray[tempArray.length - 1];
            });

            return dotJavaFiles;
        }

        /**
         * This private method reads all files in a JAR file
         *
         * @param jarFullPath path of the JAR file to analyse
         * @param extension   This is the extension the method will focus on
         * @return List of the files read
         * @throws IOException
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
    }

}
