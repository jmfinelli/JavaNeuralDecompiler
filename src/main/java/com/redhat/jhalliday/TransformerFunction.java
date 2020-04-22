package com.redhat.jhalliday;

import java.util.function.Function;
import java.util.stream.Stream;

/**
 * A flatmap style function that transforms a T into a stream of zero or more R.
 *
 * @param <T> The input type.
 * @param <R> The output type.
 */
public interface TransformerFunction<T, R> extends Function<T, Stream<R>> {
}
