package com.redhat.jhalliday.impl.javaparser.extractors;

import com.github.javaparser.printer.PrettyPrinterConfiguration;

public class PrettyPrinterConfigurationExtraSpace extends PrettyPrinterConfiguration {
    private boolean extraSpace = true;

    public boolean isExtraSpace() {
        return extraSpace;
    }

    public void setExtraSpace(boolean extraSpace) {
        this.extraSpace = extraSpace;
    }
}