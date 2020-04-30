package com.redhat.jhalliday.impl;

import java.util.Objects;

public class MethodWrapper<T> {

    protected String name;
    protected int startLine;
    protected int endLine;
    protected T method;

    public String getName() {
        return name;
    }

    public T unwrap() {
        return method;
    }

    public boolean enclosedBy(MethodWrapper<?> other) {
        return startLine >= other.startLine && endLine <= other.endLine;
    }

    public int nonOverlapSize(MethodWrapper<?> enclosing) {
        return (startLine - enclosing.startLine) + (enclosing.endLine - endLine);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MethodWrapper<?> that = (MethodWrapper<?>) o;
        return startLine == that.startLine &&
                endLine == that.endLine &&
                name.equals(that.name) &&
                method.equals(that.method);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, startLine, endLine, method);
    }
}
