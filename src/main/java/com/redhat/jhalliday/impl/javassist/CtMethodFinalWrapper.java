package com.redhat.jhalliday.impl.javassist;

import com.redhat.jhalliday.impl.FinalLowLevelMethodWrapper;
import com.redhat.jhalliday.impl.javassist.printers.LowLevelPrinter;
import javassist.CtMethod;

public class CtMethodFinalWrapper extends FinalLowLevelMethodWrapper<CtMethod> {

    public CtMethodFinalWrapper(CtMethod method) {
        super(method, LowLevelPrinter::new);
        //super(method, InfoExtractorFromOriginal::new);
        // super(method, InfoExtractorWithoutIndex::new);
    }
}