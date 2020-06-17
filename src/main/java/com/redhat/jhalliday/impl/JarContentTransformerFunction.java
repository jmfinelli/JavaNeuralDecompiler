package com.redhat.jhalliday.impl;

import com.redhat.jhalliday.TransformerFunction;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class JarContentTransformerFunction implements TransformerFunction<File, Map<String, byte[]>> {

    private final Predicate<String> matchingName;
    private final List<String> exclusions;

    public JarContentTransformerFunction(Predicate<String> matchingName) {
        this.matchingName = matchingName;
        exclusions = new ArrayList<>();
    }

    public JarContentTransformerFunction(Predicate<String> matchingName, List<String> exclusions) {
        this.matchingName = matchingName;
        this.exclusions = exclusions;
    }

    @Override
    public Stream<Map<String, byte[]>> apply(File jar) {

        // This handles when a null reference is passed
        if (jar == null) return Stream.empty();

        Map<String, byte[]> results = new HashMap<>();

        try {

            ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(jar));
            ZipEntry zipEntry = zipInputStream.getNextEntry();
            while (zipEntry != null) {
                String name = zipEntry.getName();

                if (matchingName.test(name)) {
                    if (exclusions.isEmpty()) {
                        byte[] fileBytes = zipInputStream.readAllBytes();
                        results.put(name, fileBytes);
                    } else if (exclusions.stream().noneMatch(name::matches)) {
                        byte[] fileBytes = zipInputStream.readAllBytes();
                        results.put(name, fileBytes);
                    }
                }
                zipEntry = zipInputStream.getNextEntry();
            }
            zipInputStream.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return Stream.of(results);
    }
}
