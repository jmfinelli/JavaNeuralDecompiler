package com.redhat.jhalliday.impl;

import com.redhat.jhalliday.DecompilationRecord;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.function.Consumer;

public class WritePairsToFile<LOW> implements Consumer<DecompilationRecord<FinalLowLevelMethodWrapper<LOW>, FinalHighLevelMethodWrapper>> {

    private final File outputFile;

    public WritePairsToFile(File outputFile) {
        this.outputFile = outputFile;
    }

    @Override
    public void accept(DecompilationRecord<FinalLowLevelMethodWrapper<LOW>, FinalHighLevelMethodWrapper> finalDecompilationRecord) {

        try (PrintWriter printWriter = new PrintWriter(new FileWriter(this.outputFile, true))) {
            printWriter.write(String.format("%s\t%s\n",
                    finalDecompilationRecord.getLowLevelRepresentation().getMethodBody(),
                    finalDecompilationRecord.getHighLevelRepresentation().getMethodBody()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
