package com.redhat.jhalliday.impl;

import com.redhat.jhalliday.DecompilationRecord;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CreatePairsFromMainFolder implements Function<File, Stream<DecompilationRecord<File, File>>> {

    private final String binFolder;
    private final String srcFolder;

    public CreatePairsFromMainFolder(String binFolder, String srcFolder) {
        this.binFolder = binFolder;
        this.srcFolder = srcFolder;
    }

    @Override
    public Stream<DecompilationRecord<File, File>> apply(File mainFolder) {

        List<DecompilationRecord<File, File>> results = new LinkedList<>();

        if (mainFolder.isDirectory()) {

            for (File folder : mainFolder.listFiles()) {

                if (folder.isDirectory()) {

                    List<File> binJars = new LinkedList<>();
                    List<File> srcJars = new LinkedList<>();

                    File binJarFolder = folder.listFiles(x -> x.getName().equals(binFolder))[0];
                    File srcJarFolder = folder.listFiles(x -> x.getName().equals(srcFolder))[0];

                    try {
                        binJars = listFilesForFolder(binJarFolder);
                        srcJars = listFilesForFolder(srcJarFolder);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    for (File binJar : binJars) {
                        String binJarPath = binJar.getPath();
                        String binFilename = binJarPath.substring(binJarPath.lastIndexOf("/") + 1);

                        List<File> srcMatches = srcJars.stream().filter(x -> x.getPath().contains(binFilename.replace(".jar", ""))).collect(Collectors.toList());
                        if (srcMatches.size() == 1) {
                            DecompilationRecord<File, File> predecessor = new GenericDecompilationRecord<>(binJarFolder, srcJarFolder);
                            results.add(new GenericDecompilationRecord<>(binJar, srcMatches.get(0), predecessor));
                        }
                    }
                }
            }
        }

        return results.stream();
    }

    public static List<File> listFilesForFolder(final File folder) {

        List<File> results = new LinkedList<>();

        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                results.addAll(listFilesForFolder(fileEntry));
            } else {
                if (fileEntry.getName().endsWith(".jar"))
                    results.add(fileEntry);
            }
        }

        return results;
    }
}
