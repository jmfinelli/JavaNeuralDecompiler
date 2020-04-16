package ncl.ac.uk.matcher.impl;

import ncl.ac.uk.matcher.ClassJavaPair;
import ncl.ac.uk.matcher.JARSReader;

import java.io.File;
import java.io.IOException;
import java.util.*;

public final class JARSReaderImpl implements JARSReader {

    /* ************************************************
     * ************************************************
     * *************** Class's Fields *****************
     * ************************************************
     * ************************************************/

    private final String _srcJARSFullPath;
    private final String _binJARSFullPath;
    private final static String _SOURCE_SUFFIX = "-sources";

    private final List<String> _libraryNames = new LinkedList<>();

    private final Map<String, ClassJavaPair> _classJavaPairs = new HashMap<>();

    /* ************************************************
     * ************************************************
     * ***************** Constructors *****************
     * ************************************************
     * ************************************************/

    public JARSReaderImpl(String srcPath, String binPath) {

        File srcFolder = new File(srcPath);
        File binFolder = new File(binPath);

        if (!srcFolder.exists() || !binFolder.exists())
            throw new IllegalArgumentException("Please, check the provided path(s)!");

        for (File binJar : Objects.requireNonNull(binFolder.listFiles())) {
            String libraryName = binJar.getName();
            if (libraryName.endsWith(".jar")) {
                libraryName = libraryName.substring(0, libraryName.length() - ".jar".length());

                File srcJar = new File(String.format("%s/%s%s.jar", srcPath, libraryName, JARSReaderImpl._SOURCE_SUFFIX));
                if (srcJar.exists())
                    this._libraryNames.add(libraryName);
            }
        }

        if (this._libraryNames.size() == 0)
            throw new IllegalArgumentException("Please, check the provided path(s)!");

        Collections.sort(this._libraryNames);
        this._srcJARSFullPath = srcPath;
        this._binJARSFullPath = binPath;

        // Fetches all files in the source JAR file
        //this._dotJavaFiles = JarReaderUtil.ListDotJavaFiles(_srcJarFullPath);

        // Reads all .class files in the bin JAR file and connects them with the related .java file
        //this._classNodes = JARUtiil.createDotClassMatchingDotJava(this._binJarFullPath, this._srcJarFullPath, this._dotJavaFiles);

    }

    /* ************************************************
     * ************************************************
     * **************** Public APIs *******************
     * ************************************************
     * ************************************************/

    @Override
    public ClassJavaPair getClassJavaPair(String libraryName) throws IOException {

        if (this._classJavaPairs.containsKey(libraryName)) {
            return this._classJavaPairs.get(libraryName);
        }

        if (!this._libraryNames.contains(libraryName))
            throw new IllegalArgumentException(
                    String.format("Library %s does not exist in this JARS Reader object!", libraryName));

        String srcJarPath = String.format("%s/%s%s.jar", this._srcJARSFullPath, libraryName, JARSReaderImpl._SOURCE_SUFFIX);
        String binJarPath = String.format("%s/%s.jar", this._binJARSFullPath, libraryName);

        // TODO: add CDI management
        //ClassJavaPair pair = new ClassJavaPairASMImpl(binJarPath, srcJarPath);
        ClassJavaPair pair = new JavassistClassJavaPair(binJarPath, srcJarPath);
        this._classJavaPairs.put(libraryName, pair);

        return pair;
    }

    /* ************************************************
     * ************************************************
     * **************** GETTER/SETTER *****************
     * ************************************************
     * ************************************************/

    @Override
    public List<String> getLibrariesNames() {

        return new LinkedList<>(this._libraryNames);
    }

    /* ************************************************
     * ************************************************
     * *************** Utility Classes ****************
     * ************************************************
     * ************************************************/
}
