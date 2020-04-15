package ncl.ac.uk.transformation.impl;

import ncl.ac.uk.transformation.TransformerFunction;
import ncl.ac.uk.utilities.InputStreamUtil;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

public class JarContentTransformerFunction implements TransformerFunction<File, Map<String, byte[]>> {

    private final Predicate<String> matchingName;

    public JarContentTransformerFunction(Predicate<String> matchingName) {
        this.matchingName = matchingName;
    }

    @Override
    public Stream<Map<String, byte[]>> apply(File jar) {

        Map<String, byte[]> results = new HashMap<>();

        try {
            JarFile jarFile = new JarFile(jar);
            for (JarEntry jarEntry : Collections.list(jarFile.entries())) {
                String name = jarEntry.getName();
                if (matchingName.test(name)) {
                    results.put(name, InputStreamUtil.convertInputStream(jarFile.getInputStream(jarEntry)).toByteArray());
                }
            }

            return Stream.of(results);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
