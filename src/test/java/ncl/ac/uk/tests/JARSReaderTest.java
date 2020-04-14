package ncl.ac.uk.tests;

import ncl.ac.uk.matcher.JARSReader;
import ncl.ac.uk.matcher.JARSReaderImpl;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

public class JARSReaderTest {

    private String SRCJARS_BASEPATH = "/home/jf/IdeaProjects/NeuralNetworksDecompilation/data/srcjars";
    private String BINJARS_BASEPATH = "/home/jf/IdeaProjects/NeuralNetworksDecompilation/data/binjars";

    @Test
    public void testJARInterpreterInstantiation() {

        JARSReader result = new JARSReaderImpl(this.SRCJARS_BASEPATH, this.BINJARS_BASEPATH);

        Assert.assertNotNull(result);

    }

    @Test
    public void testNoMatchingPaths() {

        String srcPath = "/home/jf/IdeaProjects/NeuralNetworksDecompilation/data";

        try {
            JARSReader result = new JARSReaderImpl(srcPath, this.BINJARS_BASEPATH);

            Assert.fail();
        } catch (IllegalArgumentException ex) { }

    }

    @Test
    public void testSamePaths1() {

        try {
            JARSReader result = new JARSReaderImpl(this.BINJARS_BASEPATH, this.BINJARS_BASEPATH);

            Assert.fail();
        } catch (IllegalArgumentException ex) { }

    }

    @Test
    public void testSamePaths2() {

        try {
            JARSReader result = new JARSReaderImpl(this.SRCJARS_BASEPATH, this.SRCJARS_BASEPATH);

            Assert.fail();
        } catch (IllegalArgumentException ex) { }
    }

    @Test
    public void testJARInterpreterLibraries() {

        List<String> result = new JARSReaderImpl(this.SRCJARS_BASEPATH, this.BINJARS_BASEPATH).getLibrariesNames();

        Assert.assertNotEquals(result.size(), 0);
    }
}
