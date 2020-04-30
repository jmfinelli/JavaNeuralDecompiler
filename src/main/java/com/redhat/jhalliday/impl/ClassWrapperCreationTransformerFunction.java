package com.redhat.jhalliday.impl;

import com.redhat.jhalliday.TransformerFunction;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

public class ClassWrapperCreationTransformerFunction<T>
        implements TransformerFunction<Map<String, byte[]>, Map<String, ClassWrapper<T>>> {

    private final Function<byte[], T> parsingFunction;
    private final Function<T, ClassWrapper<T>> wrappingFunction;

    public ClassWrapperCreationTransformerFunction(
            Function<byte[], T> parsingFunction, Function<T, ClassWrapper<T>> wrappingFunction) {
        this.parsingFunction = parsingFunction;
        this.wrappingFunction = wrappingFunction;
    }

    @Override
    public Stream<Map<String, ClassWrapper<T>>> apply(Map<String, byte[]> map) {

        Map<String, ClassWrapper<T>> result = new HashMap<>();

        map.forEach((key, bytes) -> {
            T t = parsingFunction.apply(bytes);
            result.put(key, wrappingFunction.apply(t));
        });

        return Stream.of(result);
    }

}
