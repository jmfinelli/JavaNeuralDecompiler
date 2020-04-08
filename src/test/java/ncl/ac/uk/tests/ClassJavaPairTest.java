package ncl.ac.uk.tests;

import ncl.ac.uk.matcher.ClassJavaInterface;
import ncl.ac.uk.matcher.JARSReaderImpl;
import ncl.ac.uk.matcher.JARSReader;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;

public class ClassJavaPairTest {

    private String SRCJARS_BASEPATH = "/home/jf/IdeaProjects/NeuralNetworksDecompilation/data/srcjars";
    private String BINJARS_BASEPATH = "/home/jf/IdeaProjects/NeuralNetworksDecompilation/data/binjars";
    private String LIBRARY = "classmate-1.5.1";

    @Test
    public void testClassJavaPairInstantiation() {

        JARSReader reader = new JARSReaderImpl(this.SRCJARS_BASEPATH, this.BINJARS_BASEPATH);

        List<String> libraryNames = reader.getLibraryNames();
        try {
            ClassJavaInterface pair = reader.getClassJavaPair(libraryNames.get(0));

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

            List<String> libraryNames = reader.getLibraryNames();

            ClassJavaInterface pair = reader.getClassJavaPair(libraryNames.get(0));

            List<String> methods = pair.getMethodsNames();

            Assert.assertNotNull(pair);
        } catch (IOException ex) {
            ex.printStackTrace();

            Assert.fail();
        }

    }

}
