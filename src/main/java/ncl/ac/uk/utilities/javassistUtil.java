package ncl.ac.uk.utilities;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.bytecode.SourceFileAttribute;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class javassistUtil {

    /**
     * This private method creates a List of .class files and corresponding .java files
     *
     * @param binJarPath path of the JAR file that contains .class files
     * @return Map of String and InputStream corresponding, respectively, to .java and .class files
     */
    public static Map<String, List<CtClass>> extractClassNodesFromJAR(String binJarPath, String srcJarPath) throws IOException {

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
                InputStream dotClass = jarFile.getInputStream(jarEntry);
                // fetches the .java file corresponding to the .class file (if there is one)
                CtClass ctClass = classPool.makeClass(dotClass);

                String sourceFile = ((SourceFileAttribute)ctClass.getClassFile().getAttribute("SourceFile")).getFileName();
                // Checks if there is a .java file matching the source filename
                if (dotJavaFiles.stream().anyMatch(x -> x.contains(sourceFile))) {

                    // Update or create List of CtClass
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

    public static CtClass readDotClassFile(InputStream dotClassInputStream) throws IOException {

        ClassPool classPool = ClassPool.getDefault();
        return classPool.makeClass(dotClassInputStream);
    }
}
