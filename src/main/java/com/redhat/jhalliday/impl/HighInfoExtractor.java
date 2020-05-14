package com.redhat.jhalliday.impl;

import java.util.Set;

public interface HighInfoExtractor {

    Set<String> getMethodExprNames();

    Set<String> getClassExprNames();

    Set<String> getLiteralExprNames();

    Set<String> getNameExprNames();

    String getBody();
}
