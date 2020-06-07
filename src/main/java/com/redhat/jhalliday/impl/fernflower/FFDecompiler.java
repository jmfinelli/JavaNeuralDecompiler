package com.redhat.jhalliday.impl.fernflower;

import org.jboss.windup.decompiler.fernflower.*;
import com.redhat.jhalliday.Decompiler;
import org.jetbrains.java.decompiler.main.decompiler.BaseDecompiler;

import java.io.File;
import java.nio.file.Path;

public class FFDecompiler implements Decompiler<File, File> {

    @Override
    public File apply(File file) {

        File output = null;

        if (file.getName().endsWith(".jar")) {

            FernflowerDecompiler fernflowerDecompiler = new FernflowerDecompiler();
            fernflowerDecompiler.decompileArchiveImpl(file.toPath(), output.toPath(), null, null);

        }

        return output;
    }
}
