package com.redhat.jhalliday.impl.fernflower;

import com.redhat.jhalliday.Decompiler;

import java.io.File;
import java.io.IOException;

public class CLIFernFlower implements Decompiler<File, File> {

    @Override
    public File apply(File binJar) {

        if (!binJar.getName().endsWith(".jar")){
            return null;
        }

        String libraryName = binJar.getAbsolutePath()
                .substring(binJar.getAbsolutePath().lastIndexOf(File.separator) + 1)
                .replace(".jar", "");

        // Go to the parent folder
        String newPath = binJar.getPath()
                .substring(0, binJar.getPath().lastIndexOf(File.separator));
        newPath = newPath
                .substring(0, newPath.lastIndexOf(File.separator))
                + File.separator + "decJars";

        File outputFolder = new File(newPath);
        if (!outputFolder.exists())
            outputFolder.mkdir();

        File check = new File(outputFolder + File.separator + binJar.getName());

        if (check.exists())
            return check;

        try {
            return decompileWithCLI(binJar, outputFolder);
        } catch (IOException e) {
            return null;
        }
    }

    private File decompileWithCLI(File binJar, File outputFolder) throws IOException {

        ProcessBuilder processBuilder = new ProcessBuilder();
        File redirection = new File("/dev/null");
        processBuilder.redirectError(redirection);
        processBuilder.redirectOutput(redirection);
        processBuilder.redirectInput(redirection);

        String binariesPath = binJar.getPath().substring(0, binJar.getPath().lastIndexOf("/"));

        File fernflowerJar = new File(binariesPath + File.separator + "fernflower.jar");

        if (!fernflowerJar.exists())
            throw new IOException("FernFlower JAR is not present in " + binariesPath);

        processBuilder.command("bash", "-c", String.format(
                "java -jar %s -dsg=true %s %s",
                fernflowerJar.getPath(),
                binJar.getPath(),
                outputFolder));

        try {

            Process process = processBuilder.start();

            int exitVal = process.waitFor();
            if (exitVal == 0) {
                System.out.println("Success!");
                return new File(outputFolder + File.separator + binJar.getName().replace(".jar", ""));
            } else {
                System.out.println("Failure!");
                return null;
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

}
