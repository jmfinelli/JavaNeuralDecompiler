package com.redhat.jhalliday.impl;

import java.util.Map;

public interface LowInfoExtractor {

    Map<Integer, String> getMethodNames();

    Map<Integer, String> getClassNames();

    Map<Integer, String> getConstants();

    Map<Integer, String> getFieldNames();

    Map<Integer, String> getVariableNames();

    String getBody();
}
