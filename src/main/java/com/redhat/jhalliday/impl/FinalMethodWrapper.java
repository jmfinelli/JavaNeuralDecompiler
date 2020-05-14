package com.redhat.jhalliday.impl;

import java.util.*;
import java.util.function.Function;

public abstract class FinalMethodWrapper<T> {

    protected T method;
    protected String methodBody;
    protected final Set<String> toReplace = new HashSet<>();

    public final T getMethod() {
        return method;
    }

    public final String getMethodBody() {
        return methodBody;
    }

    public final List<String> getToReplace() { return new ArrayList<>(toReplace); }

    public final void replaceStringInBody(String target, String newValue) {
        this.methodBody = this.methodBody.replace(target, newValue);
    }
}
