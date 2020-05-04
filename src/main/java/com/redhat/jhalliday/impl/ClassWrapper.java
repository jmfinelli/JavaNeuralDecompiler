package com.redhat.jhalliday.impl;

public class ClassWrapper<T> {

    protected String sourceFileName;
    protected T clazz;

    public String getSourceFileName() {
        return sourceFileName;
    }

    public T unwrap() {
        return clazz;
    }
}
