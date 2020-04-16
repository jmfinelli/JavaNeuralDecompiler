package ncl.ac.uk;

import ncl.ac.uk.matcher.BytecodeSentence;
import ncl.ac.uk.matcher.ClassJavaPair;
import ncl.ac.uk.matcher.JARSReader;
import ncl.ac.uk.matcher.JARSReaderImpl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Driver {

    private static String SRCJARS_BASEPATH = "/home/jf/IdeaProjects/ReadJarFiles/data/srcjars";
    private static String BINJARS_BASEPATH = "/home/jf/IdeaProjects/ReadJarFiles/data/binjars";

    public static void main(String[] args) {

        JARSReader reader = new JARSReaderImpl(Driver.SRCJARS_BASEPATH, Driver.BINJARS_BASEPATH);

        try {

            List<String> librariesNames = reader.getLibrariesNames();

            Map<String, List<BytecodeSentence>> sentencesMap = new HashMap<>();

            for (String library : librariesNames) {

                ClassJavaPair pair = reader.getClassJavaPair(library);

                for (String dotJavaFile : pair.getDotJavaFiles())
                    sentencesMap.put(String.format("%s$%s", library, dotJavaFile), pair.getSentences(dotJavaFile));
            }

            int numberOfMethods = 0;
            for (List<BytecodeSentence> bytecodeSentences : sentencesMap.values())
                for (BytecodeSentence bytecodeSentence : bytecodeSentences)
                    numberOfMethods++;

            System.out.println("Number of methods: " + numberOfMethods);
            Set<String> dictionary = BytecodeSentence.getDictionary();
            System.out.println("Dictionary size: " + dictionary.size());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
