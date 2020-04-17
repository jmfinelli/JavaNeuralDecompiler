package com.redhat.jhalliday;

import java.util.function.Function;
import java.util.stream.Stream;

/**
 * A flatMap style function that transforms a Decompilation record into
 * a stream of zero or more DecompilationRecords of different types.
 *
 * @param <T_LOW>  The low level language type of the provided DecompilationRecord.
 * @param <T_HIGH> The high level language type of the provided DecompilationRecord.
 * @param <R_LOW>  The low level language type of the resulting DecompilationRecord(s).
 * @param <R_HIGH> The high level language type of the resulting DecompilationRecord(s).
 */
public interface RecordTransformer<T_LOW, T_HIGH, R_LOW, R_HIGH>
        extends Function<DecompilationRecord<T_LOW, T_HIGH>, Stream<DecompilationRecord<R_LOW, R_HIGH>>> {

    @Override
    Stream<DecompilationRecord<R_LOW, R_HIGH>> apply(DecompilationRecord<T_LOW, T_HIGH> decompilationRecord);
}
