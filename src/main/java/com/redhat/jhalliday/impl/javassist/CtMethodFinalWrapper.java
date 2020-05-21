package com.redhat.jhalliday.impl.javassist;

import com.redhat.jhalliday.impl.FinalLowLevelMethodWrapper;
import com.redhat.jhalliday.impl.javassist.printers.InfoExtractorFromOriginal;
import com.redhat.jhalliday.impl.javassist.printers.InfoExtractorWithoutIndex;
import com.redhat.jhalliday.impl.javassist.printers.MyInfoExtractor;
import javassist.CtMethod;

public class CtMethodFinalWrapper extends FinalLowLevelMethodWrapper<CtMethod> {

    public CtMethodFinalWrapper(CtMethod method) {
        //super(method, MyInfoExtractor::new);
        //super(method, InfoExtractorFromOriginal::new);
        super(method, InfoExtractorWithoutIndex::new);
    }
}
