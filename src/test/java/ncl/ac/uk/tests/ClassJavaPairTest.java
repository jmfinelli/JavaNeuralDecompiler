package ncl.ac.uk.tests;

import ncl.ac.uk.matcher.*;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ClassJavaPairTest {

    private String SRCJARS_BASEPATH = "/home/jf/IdeaProjects/ReadJarFiles/data/srcjars";
    private String BINJARS_BASEPATH = "/home/jf/IdeaProjects/ReadJarFiles/data/binjars";

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
    public void testGetSentences() {

        JARSReader reader = new JARSReaderImpl(this.SRCJARS_BASEPATH, this.BINJARS_BASEPATH);

        try {

            List<String> librariesNames = reader.getLibrariesNames();

            Map<String, List<Sentence>> sentencesMap = new HashMap<>();

            for (String library : librariesNames) {

                ClassJavaPair pair = reader.getClassJavaPair(library);

                for (String dotJavaFile : pair.getDotJavaFiles())
                    sentencesMap.put(String.format("%s$%s", library, dotJavaFile), pair.getSentences(dotJavaFile));
            }

            Assert.assertNotEquals(sentencesMap.size(), 0);

        } catch (Exception ex) {
            ex.printStackTrace();

            Assert.fail();
        }

    }

}
