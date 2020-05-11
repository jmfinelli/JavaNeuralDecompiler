package com.redhat.jhalliday.impl;

import java.util.*;

public abstract class FinalMethodWrapper<T> {

    protected T method;
    protected String methodBody;
    protected String name;

    protected final Set<String> toReplace = new LinkedHashSet<>();

    public T getMethod() {
        return method;
    }

    public String getName() { return name; }

    public String getMethodBody() {
        return methodBody;
    }

    public List<String> getToReplace() { return new ArrayList<>(toReplace); }


}
