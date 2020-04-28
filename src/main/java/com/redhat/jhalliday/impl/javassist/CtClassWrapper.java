package com.redhat.jhalliday.impl.javassist;

import com.redhat.jhalliday.impl.ClassWrapper;
import javassist.CtClass;
import javassist.bytecode.SourceFileAttribute;

public class CtClassWrapper extends ClassWrapper<CtClass> {

    public CtClassWrapper(CtClass ctClass) {
        clazz = ctClass;
        sourceFileName = null;

        SourceFileAttribute attribute = (SourceFileAttribute) ctClass.getClassFile().getAttribute("SourceFile");
        if (attribute != null) {
            sourceFileName = attribute.getFileName();
        }
    }
}
