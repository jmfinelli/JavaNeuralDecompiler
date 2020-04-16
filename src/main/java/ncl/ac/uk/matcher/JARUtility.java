package ncl.ac.uk.matcher;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Utility class used by the outer-class to analyse JAR files. The inner-class is declared
 * private because ClassJavaPair is designed to be the only class to interact with JAR files.
 */
public class JARUtility {

    /**
     * This method lists all .java files in a JAR file
     *
     * @param jarPath path of the JAR file to analyse
     * @return A list of Strings that contains all .java filenames found in the JAR
     */
    public static List<String> ListDotJavaFiles(String jarPath) throws IOException {

        return ListFilesInJAR(jarPath, "java");
    }

    /**
     * This private method reads all files in a JAR file
     *
     * @param jarFullPath path of the JAR file to analyse
     * @param extension   This is the extension the method will focus on
     * @return List of the files read
     */
    public static List<String> ListFilesInJAR(String jarFullPath, String extension) throws IOException {

        List<String> dotClassFiles = new LinkedList<>();

        JarFile jarFile = new JarFile(new File(jarFullPath));

        for (JarEntry jarEntry : Collections.list(jarFile.entries())) {
            String name = jarEntry.getName();
            if (name.endsWith(String.format(".%s", extension)))
                dotClassFiles.add(name);
        }

        return dotClassFiles;
    }

    public static String extractSourceCode(String srcJarPath, String dotJavaFilename) throws IOException {

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
