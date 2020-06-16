package com.redhat.jhalliday.impl;

import com.redhat.jhalliday.DecompilationRecord;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.function.Consumer;

public class WritePairsToFile<LOW> implements Consumer<DecompilationRecord<FinalLowLevelMethodWrapper<LOW>, FinalHighLevelMethodWrapper>> {

    private final File lowLevelReferencesFile;
    private final File highLevelReferencesFile;
    private final File highLevelCandidatesFile;

    public WritePairsToFile(File lowLevelReferencesFile, File highLevelReferencesFile, File highLevelCandidatesFile) {
        this.lowLevelReferencesFile = lowLevelReferencesFile;
        this.highLevelReferencesFile = highLevelReferencesFile;
        this.highLevelCandidatesFile = highLevelCandidatesFile;
    }

    public WritePairsToFile(File lowLevelReferencesFile, File highLevelReferencesFile) {
        this.lowLevelReferencesFile = lowLevelReferencesFile;
        this.highLevelReferencesFile = highLevelReferencesFile;
        this.highLevelCandidatesFile = null;
    }

    @Override
    public void accept(DecompilationRecord<FinalLowLevelMethodWrapper<LOW>, FinalHighLevelMethodWrapper> finalDecompilationRecord) {

        if (finalDecompilationRecord.getHighLevelDecompiled() != null) {

            try (
                    PrintWriter lowRefPrintWriter = new PrintWriter(new FileWriter(this.lowLevelReferencesFile, true));
                    PrintWriter highRefPrintWriter = new PrintWriter(new FileWriter(this.highLevelReferencesFile, true));
                    PrintWriter highCandPrintWriter = new PrintWriter(new FileWriter(this.highLevelCandidatesFile, true))
            ) {

                lowRefPrintWriter.write(String.format("%s\n", finalDecompilationRecord.getLowLevelRepresentation().getMethodBody()));
                highRefPrintWriter.write(String.format("%s\n", finalDecompilationRecord.getHighLevelRepresentation().getMethodBody()));
                highCandPrintWriter.write(String.format("%s\n", finalDecompilationRecord.getHighLevelDecompiled().getMethodBody()));

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try (
                    PrintWriter lowRefPrintWriter = new PrintWriter(new FileWriter(this.lowLevelReferencesFile, true));
                    PrintWriter highRefPrintWriter = new PrintWriter(new FileWriter(this.highLevelReferencesFile, true));
            ) {

                lowRefPrintWriter.write(String.format("%s\n", finalDecompilationRecord.getLowLevelRepresentation().getMethodBody()));
                highRefPrintWriter.write(String.format("%s\n", finalDecompilationRecord.getHighLevelRepresentation().getMethodBody()));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
