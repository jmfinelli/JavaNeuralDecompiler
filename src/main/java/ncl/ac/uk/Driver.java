package ncl.ac.uk;

import ncl.ac.uk.matcher.BytecodeSentence;
import ncl.ac.uk.matcher.ClassJavaPair;
import ncl.ac.uk.matcher.JARSReader;
import ncl.ac.uk.matcher.impl.JARSReaderImpl;

import org.apache.commons.math3.stat.descriptive.rank.*;

import java.util.*;

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
            List<Double> methodsLengths = new LinkedList<>();
            for (List<BytecodeSentence> bytecodeSentences : sentencesMap.values())
                for (BytecodeSentence bytecodeSentence : bytecodeSentences) {
                    methodsLengths.add((double) bytecodeSentence.getTokensNumber());
                    numberOfMethods++;
                }

            // Sort the list of lengths
            Collections.sort(methodsLengths);

            System.out.println("Number of methods: " + numberOfMethods);
            Set<String> dictionary = BytecodeSentence.getDictionary();
            System.out.println("Dictionary size: " + dictionary.size());
            System.out.println("Average method length: " + methodsLengths.stream().mapToInt(Double::intValue).sum()/numberOfMethods);

            Percentile percentile = new Percentile();
            percentile.setData(methodsLengths.stream().mapToDouble(Double::doubleValue).toArray());

            for(int p = 5; p <= 100; p += 5)
                System.out.println(String.format("%sth Percentile: %s", p, percentile.evaluate(p)));


        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
