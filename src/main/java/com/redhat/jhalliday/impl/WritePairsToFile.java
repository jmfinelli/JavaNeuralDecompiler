package com.redhat.jhalliday.impl;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.redhat.jhalliday.DecompilationRecord;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.function.Consumer;

public class WritePairsToFile<LOW> implements Consumer<DecompilationRecord<MethodJuice<LOW>, MethodJuice<MethodDeclaration>>> {

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
    public void accept(DecompilationRecord<MethodJuice<LOW>, MethodJuice<MethodDeclaration>> finalDecompilationRecord) {

        try (
                PrintWriter lowRefPrintWriter = new PrintWriter(new FileWriter(this.lowLevelReferencesFile, true));
                PrintWriter highRefPrintWriter = new PrintWriter(new FileWriter(this.highLevelReferencesFile, true))
        ) {

            lowRefPrintWriter.write(String.format("%s\n", finalDecompilationRecord.getLowLevelRepresentation().getBody()));
            highRefPrintWriter.write(String.format("%s\n", finalDecompilationRecord.getHighLevelRepresentation().getBody()));


        } catch (IOException e) {
            e.printStackTrace();
        }

        if (finalDecompilationRecord.getHighLevelDecompiled() != null) {

            try (PrintWriter highCandPrintWriter = new PrintWriter(new FileWriter(this.highLevelCandidatesFile, true))) {

                highCandPrintWriter.write(String.format("%s\n", finalDecompilationRecord.getHighLevelDecompiled().getBody()));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
