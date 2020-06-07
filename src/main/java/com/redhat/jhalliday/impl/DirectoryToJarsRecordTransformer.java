package com.redhat.jhalliday.impl;

import com.redhat.jhalliday.DecompilationRecord;
import com.redhat.jhalliday.Decompiler;
import com.redhat.jhalliday.RecordTransformer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class DirectoryToJarsRecordTransformer implements RecordTransformer<File, File, File, File> {

    private final Decompiler<File, File> _decompiler;

    public DirectoryToJarsRecordTransformer(Decompiler<File, File> _decompiler) {
        this._decompiler = _decompiler;
    }

    public DirectoryToJarsRecordTransformer() {
        this._decompiler = null;
    }

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
                    File decompiledJar = this._decompiler == null ? null : this._decompiler.apply(binJar);
                    DecompilationRecord<File, File> result = new GenericDecompilationRecord<>(binJar, srcJar, decompiledJar, decompilationRecord);
                    results.add(result);
                }
            }
        }

        return results.stream();
    }
}
