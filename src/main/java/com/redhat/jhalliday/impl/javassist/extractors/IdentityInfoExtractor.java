package com.redhat.jhalliday.impl.javassist.extractors;

import com.redhat.jhalliday.InfoExtractor;
import javassist.CtMethod;

import java.util.HashMap;
import java.util.Map;

public class IdentityInfoExtractor implements InfoExtractor<CtMethod> {

    @Override
    public Map<String, InfoType> apply(CtMethod ctMethod) {
        return new HashMap<>();
    }
}
