package ncl.ac.uk.tests;

import ncl.ac.uk.matcher.*;
import ncl.ac.uk.matcher.impl.JARSReaderImpl;
import ncl.ac.uk.matcher.impl.JavassistClassJavaPair;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.*;

public class ClassJavaPairTest {

    private String SRCJARS_BASEPATH = "./data/srcjars";
    private String BINJARS_BASEPATH = "./data/binjars";

    @Test
    public void testDuplicateFilename() throws Exception {
        ClassJavaPair pair = new JavassistClassJavaPair(BINJARS_BASEPATH+"/testng-7.1.0.jar", SRCJARS_BASEPATH+"/testng-7.1.0-sources.jar");

        /*
        $ jar -tf testng-7.1.0-sources.jar | grep Model
        org/testng/mustache/Model.java
        org/testng/reporters/jq/Model.java
         */
        int seenCount = 0;
        for(String name : pair.getDotJavaFiles()) {
            if(name.equals("org/testng/mustache/Model.java") || name.equals("org/testng/reporters/jq/Model.java")) {
                seenCount++;
            }
        }

        Assert.assertEquals(seenCount, 2);
    }

    @Test
    public void testClassJavaPairInstantiation() {

        JARSReader reader = new JARSReaderImpl(this.SRCJARS_BASEPATH, this.BINJARS_BASEPATH);

        List<String> libraryNames = reader.getLibrariesNames();
        try {
            ClassJavaPair pair = reader.getClassJavaPair(libraryNames.get(0));

            Assert.assertNotNull(pair);
        } catch (IOException ex) {
            ex.printStackTrace();

            Assert.fail();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testGetMethods() {

        JARSReader reader = new JARSReaderImpl(this.SRCJARS_BASEPATH, this.BINJARS_BASEPATH);

        try {

            List<String> libraryNames = reader.getLibrariesNames();

            ClassJavaPair pair = reader.getClassJavaPair(libraryNames.get(11));

            List<String> methods = new LinkedList<>();

            for (String dotJavaFile : pair.getDotJavaFiles())
                methods.addAll(pair.getMethodsNames(dotJavaFile));

            Assert.assertNotNull(pair);
        } catch (IOException ex) {
            ex.printStackTrace();

            Assert.fail();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testGetDecompilationRecords() {

        JARSReader reader = new JARSReaderImpl(this.SRCJARS_BASEPATH, this.BINJARS_BASEPATH);

        try {

            List<String> librariesNames = reader.getLibrariesNames();

            ClassJavaPair pair = reader.getClassJavaPair(librariesNames.get(11));

            String dotJavaFile = pair.getDotJavaFiles().get(3);

            List<DecompilationRecord> result = pair.getDecompilationRecords(dotJavaFile);

            Assert.assertNotNull(result);
        } catch (Exception ex) {
            ex.printStackTrace();

            Assert.fail();
        }

    }

    @Test
    public void testGetSentencesFromOneLibrary() {

        JARSReader jarsReader = new JARSReaderImpl(this.SRCJARS_BASEPATH, this.BINJARS_BASEPATH);

        try {

            // Classmate library
            String library = jarsReader.getLibrariesNames().get(11);

            Map<String, List<BytecodeRepresentation>> sentencesMap = new HashMap<>();

            ClassJavaPair pair = jarsReader.getClassJavaPair(library);

            for (String dotJavaFile : pair.getDotJavaFiles())
                sentencesMap.put(String.format("%s$%s", library, dotJavaFile), pair.getBytecodeRepresentations(dotJavaFile));

            Assert.assertNotEquals(sentencesMap.size(), 0);

        } catch (Exception ex) {
            ex.printStackTrace();

            Assert.fail();
        }

    }

    @Test
    public void testGetASTMethodsFromOneLibrary() {

        JARSReader jarsReader = new JARSReaderImpl(this.SRCJARS_BASEPATH, this.BINJARS_BASEPATH);

        try {

            // Classmate library
            String library = jarsReader.getLibrariesNames().get(0);

            Map<String, List<ASTRepresentation>> sentencesMap = new HashMap<>();

            ClassJavaPair pair = jarsReader.getClassJavaPair(library);

            for (String dotJavaFile : pair.getDotJavaFiles())
                sentencesMap.put(dotJavaFile, pair.getASTRepresentations(dotJavaFile));

            Assert.assertNotEquals(sentencesMap.size(), 0);

        } catch (Exception ex) {
            ex.printStackTrace();

            Assert.fail();
        }

    }

    @Test
    public void testGetSentencesFromAllLibraries() {

        JARSReader reader = new JARSReaderImpl(this.SRCJARS_BASEPATH, this.BINJARS_BASEPATH);

        try {

            List<String> librariesNames = reader.getLibrariesNames();

            Map<String, List<BytecodeRepresentation>> sentencesMap = new HashMap<>();

            for (String library : librariesNames) {

                ClassJavaPair pair = reader.getClassJavaPair(library);

                for (String dotJavaFile : pair.getDotJavaFiles())
                    sentencesMap.put(String.format("%s$%s", library, dotJavaFile), pair.getBytecodeRepresentations(dotJavaFile));
            }

            int numberOfMethods = 0;
            for (List<BytecodeRepresentation> bytecodeRepresentations : sentencesMap.values())
                for (BytecodeRepresentation bytecodeRepresentation : bytecodeRepresentations)
                    numberOfMethods++;

            System.out.println("Number of methods: " + numberOfMethods);
            Set<String> dictionary = BytecodeRepresentation.getDictionary();
            System.out.println("Dictionary size: " + dictionary.size());

            Assert.assertNotEquals(sentencesMap.size(), 0);

        } catch (Exception ex) {
            ex.printStackTrace();

            Assert.fail();
        }

    }

}
