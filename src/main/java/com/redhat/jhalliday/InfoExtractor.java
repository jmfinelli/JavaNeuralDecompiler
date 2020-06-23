package com.redhat.jhalliday;

import java.util.Map;
import java.util.function.Function;

public interface InfoExtractor<T> extends Function<T, Map<String, InfoExtractor.InfoType>> {

    enum InfoType {
        CLASS,
        VAR,
        FIELD,
        MET,
        CONST;

    }
}
