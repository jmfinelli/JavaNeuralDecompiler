package ncl.ac.uk.transformation.impl;

import ncl.ac.uk.transformation.DecompilationRecord;
import ncl.ac.uk.transformation.RecordTransformer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class DirectoryToJarsRecordTransformer implements RecordTransformer<File, File, File, File> {

    @Override
    public Stream<DecompilationRecord<File, File>> apply(DecompilationRecord<File, File> decompilationRecord) {

        File srcFolder = decompilationRecord.getHighLevelRepresentation();
        File binFolder = decompilationRecord.getLowLevelRepresentation();

        if (!srcFolder.exists() || !binFolder.exists()) {
            throw new IllegalArgumentException("Please, check the provided path(s)!");
        }

        List<DecompilationRecord<File, File>> results = new ArrayList<>();

        for (File binJar : Objects.requireNonNull(binFolder.listFiles())) {
            String libraryName = binJar.getName();
            if (libraryName.endsWith(".jar")) {
                libraryName = libraryName.substring(0, libraryName.length() - ".jar".length());

                File srcJar = new File(srcFolder, String.format("%s-sources.jar", libraryName));
                if (srcJar.exists()) {
                    DecompilationRecord<File, File> result = new GenericDecompilationRecord<>(binJar, srcJar, decompilationRecord);
                    results.add(result);
                }
            }
        }

        return results.stream();
    }
}
