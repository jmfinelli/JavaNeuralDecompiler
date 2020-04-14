package ncl.ac.uk.tests;

import jdk.internal.org.objectweb.asm.tree.ClassNode;
import org.testng.Assert;
import org.testng.annotations.Test;
import ncl.ac.uk.utilities.JarReaderUtil;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class jarReaderTest {

    private String SRCJARS_BASEPATH = "/home/jf/IdeaProjects/NeuralNetworksDecompilation/data/srcjars/";
    private String BINJARS_BASEPATH = "/home/jf/IdeaProjects/NeuralNetworksDecompilation/data/binjars/";

    @Test
    public void testReadDotJavaFiles(){

        String filename = String.format("%s-sources.jar", "classmate-1.5.1");

        try {
            List<String> result = JarReaderUtil.ListDotJavaFiles(this.SRCJARS_BASEPATH + filename);

            Assert.assertNotEquals(result.size(), 0);
        } catch (IOException ioEx) {
            ioEx.printStackTrace();
            //System.out.println(ioEx.getMessage());

            Assert.fail();
        }
    }

    @Test
    public void testReadDotClassFiles(){

        String filename = String.format("%s.jar", "classmate-1.5.1");

        try {
            List<String> result = JarReaderUtil.ListDotClassFiles(this.BINJARS_BASEPATH + filename);

            Assert.assertNotEquals(result.size(), 0);
        } catch (IOException ioEx) {
            ioEx.printStackTrace();
            //System.out.println(ioEx.getMessage());

            Assert.fail();
        }
    }

    @Test
    public void testExtractContentDotClassFiles(){

        String filename = String.format("%s.jar", "classmate-1.5.1");

        try {
            Map<String, byte[]> result = JarReaderUtil.ReadDotClassFiles(this.BINJARS_BASEPATH + filename);

            Assert.assertNotEquals(result.size(), 0);
        } catch (IOException ioEx) {
            ioEx.printStackTrace();
            //System.out.println(ioEx.getMessage());

            Assert.fail();
        }
    }

    @Test
    public void testExtractBytecodeDotClassFiles(){

        String filename = String.format("%s.jar", "classmate-1.5.1");

        try {
            Map<String, String> result = JarReaderUtil.ReadBytecodeDotClassFiles(this.BINJARS_BASEPATH + filename);

            Assert.assertNotEquals(result.size(), 0);
        } catch (IOException ioEx) {
            ioEx.printStackTrace();
            //System.out.println(ioEx.getMessage());

            Assert.fail();
        }
    }

    @Test
    public void testCreateDotClassDotJavaList() {

        String binFilename = String.format("%s.jar", "classmate-1.5.1");
        String srcFilename = String.format("%s-sources.jar", "classmate-1.5.1");

        try {
            Map<String, List<ClassNode>> result = JarReaderUtil.ListDotClassMatchingDotJava(
                    this.BINJARS_BASEPATH + binFilename,
                    this.SRCJARS_BASEPATH + srcFilename);

            Assert.assertNotEquals(result.size(), 0);
        } catch (IOException ioEx) {
            ioEx.printStackTrace();
            //System.out.println(ioEx.getMessage());

            Assert.fail();
        }

    }
}
