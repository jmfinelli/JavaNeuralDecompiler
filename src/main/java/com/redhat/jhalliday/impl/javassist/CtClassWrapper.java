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

        /*
         * Truncate the qualified name of CtClass in case the .java file
         * generated multiple .class files (which are numbered within
         * javassist with "$"+digit
         */
        qualifiedName = ctClass.getName();
        if (qualifiedName.matches(".+\\$\\d+$"))
            qualifiedName = qualifiedName.substring(0, qualifiedName.lastIndexOf("$"));

        /*
         * To match qualified names between javassist and javaparser, "/" and "$"
         * are replaced with "."
         */
        qualifiedName = qualifiedName.replaceAll("[/$]", ".");
    }
}
