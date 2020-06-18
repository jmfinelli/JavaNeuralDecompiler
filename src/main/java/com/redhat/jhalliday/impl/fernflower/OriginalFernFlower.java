package com.redhat.jhalliday.impl.fernflower;

import com.redhat.jhalliday.Decompiler;
import org.jetbrains.java.decompiler.main.decompiler.PrintStreamLogger;
import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Map;

public class OriginalFernFlower implements Decompiler<File, File> {

    private final File _outputFolder;
    private final PrintStream _stream;
    private final Map<String, Object> _options;

    public OriginalFernFlower(File outputFolder, PrintStream stream, Map<String, Object> options) {
        if (!outputFolder.exists()) {
            outputFolder.mkdir();
        }
        this._outputFolder = outputFolder;

        if (options == null ) {
            throw new IllegalArgumentException("You must provide options for FernFlower");
        }
        this._options = options;
        this._stream = stream;
    }

    public OriginalFernFlower(File outputFolder) throws FileNotFoundException {
        this(outputFolder, new PrintStream(new FileOutputStream(new File("./decompilation.log"))), IFernflowerPreferences.getDefaults());
    }

    @Override
    public File apply(File file) {

        File outputFile = new File(this._outputFolder + File.separator + file.getName());
        if (outputFile.exists()) {
            return outputFile;
        }

        JetbrainsFernFlower engine = new JetbrainsFernFlower(this._outputFolder, this._options, new PrintStreamLogger(this._stream));

        engine.addSource(file);

        engine.decompileContext();

        if (outputFile.exists()) {
            return outputFile;
        } else {
            return null;
        }
    }
}
