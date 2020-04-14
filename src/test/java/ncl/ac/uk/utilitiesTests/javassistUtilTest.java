package ncl.ac.uk.utilitiesTests;

import javassist.CtClass;
import ncl.ac.uk.utilities.javassistUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class javassistUtilTest {

    private String SRCJARS_BASEPATH = "/home/jf/IdeaProjects/ReadJarFiles/data/srcjars";
    private String BINJARS_BASEPATH = "/home/jf/IdeaProjects/ReadJarFiles/data/binjars";
    private String LIBRARY = "classmate-1.5.1";

    @Test
    public void testReadDotJavaFiles(){

        String srcFilename = String.format("%s-sources.jar", this.LIBRARY);
        String binFilename = String.format("%s.jar", this.LIBRARY);

        String srcJarFullPath = String.format("%s/%s", this.SRCJARS_BASEPATH, srcFilename);
        String binJarFullPath = String.format("%s/%s", this.BINJARS_BASEPATH, binFilename);

        try {
            Map<String, List<CtClass>> ctClass = javassistUtil.extractClassNodesFromJAR(binJarFullPath, srcJarFullPath);

            Assert.assertNotEquals(ctClass.size(), 0);
        } catch (IOException ioEx) {
            ioEx.printStackTrace();
            //System.out.println(ioEx.getMessage());

            Assert.fail();
        }
    }


}
