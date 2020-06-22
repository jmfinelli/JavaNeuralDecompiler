package com.redhat.jhalliday.impl.javassist;

import com.redhat.jhalliday.impl.FinalLowLevelMethodWrapper;
import com.redhat.jhalliday.impl.javassist.printers.InfoExtractor;
import com.redhat.jhalliday.impl.javassist.printers.InfoExtractorWithoutIndex;
import javassist.CtMethod;

public class CtMethodFinalWrapper extends FinalLowLevelMethodWrapper<CtMethod> {

    public CtMethodFinalWrapper(CtMethod method) {
        super(method, InfoExtractor::new);
        //super(method, InfoExtractorFromOriginal::new);
        // super(method, InfoExtractorWithoutIndex::new);
    }
}