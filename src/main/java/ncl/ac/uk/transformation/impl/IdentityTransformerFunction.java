package ncl.ac.uk.transformation.impl;

import ncl.ac.uk.transformation.TransformerFunction;

import java.util.stream.Stream;

public class IdentityTransformerFunction<T> implements TransformerFunction<T, T> {

    @Override
    public Stream<T> apply(T t) {
        return Stream.of(t);
    }
}
