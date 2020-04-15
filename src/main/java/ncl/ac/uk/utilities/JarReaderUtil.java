package ncl.ac.uk.utilities;

import org.objectweb.asm.tree.ClassNode;

import java.io.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;

/**
 * This class is a utility class to read .java and .class files from a jar file
 */
public final class JarReaderUtil {

    // Blocks the instantiation of this class
    private JarReaderUtil() {}

    /**
     * This method lists all .java files in a JAR file
     * @param jarPath path of the JAR file to analyse
     * @return A list of Strings that contains all .java files found in the JAR
     */
    public static List<String> ListDotJavaFiles(String jarPath) throws IOException {

        List<String> dotJavaFiles = ListFilesNew(jarPath, "java");

        // This little modification was added because ClassNode holds only the filename
        // of the .java file and not the entire package path.
        dotJavaFiles.replaceAll(x -> {
            String[] tempArray = x.split("/");
            return tempArray[tempArray.length - 1];
        });

        return dotJavaFiles;
    }

    /**
     * This method lists all .class files in a JAR file
     * @param jarPath path of the JAR file to analyse
     * @return A list of Strings that contains all .class files found in the JAR
     */
    public static List<String> ListDotClassFiles(String jarPath) throws IOException {

        return ListFilesNew(jarPath, "class");
    }

    /**
     * This method creates a Map of .class files and corresponding .java files
     * @param binJarPat path of the JAR file that contains .class files
     * @param srcJarPath path of the JAR file that contains .java files
     */
    public static Map<String, List<ClassNode>> ListDotClassMatchingDotJava(String binJarPat, String srcJarPath) throws IOException {

        return createDotClassMatchingDotJava(binJarPat, srcJarPath, ListDotJavaFiles(srcJarPath));
    }

    public static Map<String, byte[]> ReadDotClassFiles(String jarPath) throws IOException {

        return extractContentDotClassFiles(jarPath, "class");
    }

    public static Map<String, String> ReadBytecodeDotClassFiles(String jarPath) throws IOException {

        return extractBytecodeDotClassFiles(jarPath, "class");
    }

    /***********************************************/
    /*************** Private Methods ***************/
    /***********************************************/

    @Deprecated
    private static List<String> ListFiles(String jarPath, String extension) throws IOException {

        List<String> dotClassFiles = new LinkedList<>();

        JarInputStream jarInputStream = new JarInputStream(new FileInputStream(jarPath));

        JarEntry jarClass;
        while((jarClass = jarInputStream.getNextJarEntry()) != null) {
            String name = jarClass.getName();
            if(name.endsWith(String.format(".%s", extension)))
                dotClassFiles.add(name);
        }

        return dotClassFiles;
    }

    private static List<String> ListFilesNew(String jarPath, String extension) throws IOException {

        List<String> dotClassFiles = new LinkedList<>();

        JarFile jarFile = new JarFile(new File(jarPath));

        for(JarEntry jarEntry : Collections.list(jarFile.entries())) {
            String name = jarEntry.getName();
            if(name.endsWith(String.format(".%s", extension)))
                dotClassFiles.add(name);
        }

        return dotClassFiles;
    }

    private static Map<String, byte[]> extractContentDotClassFiles(String jarPath, String extension) throws IOException {

        Map<String, byte[]> dotClassFiles = new HashMap<>();

        JarFile jarFile = new JarFile(new File(jarPath));

        for(JarEntry jarEntry : Collections.list(jarFile.entries())) {
            String name = jarEntry.getName();
            // The second condition could be excluded in the moment module-info is removed from jar files
            // directly through the POM file. There are plugins (such as AntRun) that can do that but I
            // wasted too much time trying to understand how to configure the plugin to run in the package
            // phase. TODO: ask Jonathan to help with this.
            if(name.endsWith(String.format(".%s", extension)) &&
            !name.startsWith("module-info")) {
                dotClassFiles.put(name, InputStreamUtil.convertInputStream(jarFile.getInputStream(jarEntry)).toByteArray());
            }
        }

        return dotClassFiles;
    }

    private static Map<String, String> extractBytecodeDotClassFiles(String jarPath, String extension) throws IOException {

        Map<String, String> dotClassFiles = new HashMap<>();

        JarFile jarFile = new JarFile(new File(jarPath));

        for(JarEntry jarEntry : Collections.list(jarFile.entries())) {
            String name = jarEntry.getName();
            // The second condition could be excluded in the moment module-info is removed from jar files
            // directly through the POM file. There are plugins (such as AntRun) that can do that but I
            // wasted too much time trying to understand how to configure the plugin to run in the package
            // phase. TODO: ask Jonathan to help with this.
            if(name.endsWith(String.format(".%s", extension)) &&
                    !name.startsWith("module-info")) {
                dotClassFiles.put(name, asmUtil.readMethodsByteCodes(jarFile.getInputStream(jarEntry)));
            }
        }

        return dotClassFiles;
    }

    /**
     * This private method creates a Map of .class files and corresponding .java files
     * @param binJarPat path of the JAR file that contains .class files
     * @param srcJarPath path of the JAR file that contains .java files
     * @return Map of String and InputStream corresponding, respectively, to .java and .class files
     */
    private static Map<String, List<ClassNode>> createDotClassMatchingDotJava(String binJarPat, String srcJarPath, List<String> dotJavaFiles) throws IOException {

        // List to insert .class files that matches .java files
        Map<String, List<ClassNode>> dotClassFiles = new HashMap<>();

        // Read the JAR file that contains .class files
        JarFile jarFile = new JarFile(new File(binJarPat));

        for (JarEntry jarEntry : Collections.list(jarFile.entries())) {
            String name = jarEntry.getName();
            // The second condition could be excluded if module-info is removed from jar files
            // directly through the POM file. There are plugins (such as AntRun) that can do that but I
            // wasted too much time trying to understand how to configure the plugin to run in the package
            // phase. TODO: ask Jonathan to help with this.
            if (name.endsWith(".class") &&
                    !name.startsWith("module-info")){
                // extracts the InputStream corresponding to the current .class
                InputStream dotClass = jarFile.getInputStream(jarEntry);
                // fetches the .java file corresponding to the .class file (if there is one)
                Map.Entry<String, ClassNode> dotJavaDotClassPair = asmUtil.checkDotClassMatchesDotJava(dotClass, dotJavaFiles);

                // Checks if there is a .java file that matches the .class file
                if (!dotJavaDotClassPair.equals(null)) {
                    String dotJava = dotJavaDotClassPair.getKey();
                    List<ClassNode> tempISList = dotClassFiles.getOrDefault(dotJava, null);
                    if (tempISList == null) tempISList = new LinkedList<>();
                    // Add .class name to the List
                    tempISList.add(dotJavaDotClassPair.getValue());
                    dotClassFiles.put(dotJava, tempISList);
                }
            }

        }

        return dotClassFiles;
    }

}
