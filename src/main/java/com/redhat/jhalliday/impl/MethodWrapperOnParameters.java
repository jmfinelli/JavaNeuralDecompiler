package com.redhat.jhalliday.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MethodWrapperOnParameters<T> {

    protected String name;
    protected T method;
    protected List<String> parametersTypes = new ArrayList<>();
    protected String returnParameterType;

    public String getName() {
        return name;
    }

    public String getReturnParameterType() { return returnParameterType; }

    public T unwrap() {
        return method;
    }

    public List<String> getParametersTypes() {
        return new ArrayList<>(parametersTypes);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MethodWrapperOnParameters<?> that = (MethodWrapperOnParameters<?>) o;

        boolean matchedParamsType = true;

        matchedParamsType &= parametersTypes.stream().allMatch(
                x -> that.getParametersTypes().stream().anyMatch(
                        y -> y.endsWith(x)));

        return matchedParamsType &&
                name.equals(that.name) &&
                method.equals(that.method);
    }

    @Override
    public int hashCode() {

        int result = 1;

        for (String item : parametersTypes) {
            result = 31*result + item.hashCode();
        }

        return Objects.hash(name, result, method);
    }

}
