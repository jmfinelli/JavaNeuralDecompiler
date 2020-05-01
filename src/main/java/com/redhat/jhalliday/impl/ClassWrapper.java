package com.redhat.jhalliday.impl;

public class ClassWrapper<T> {

    protected String sourceFileName;
    protected String qualifiedName;
    protected T clazz;

    public String getSourceFileName() {
        return sourceFileName;
    }

    public String getQualifiedName() { return qualifiedName; }

    public T unwrap() {
        return clazz;
    }

}
