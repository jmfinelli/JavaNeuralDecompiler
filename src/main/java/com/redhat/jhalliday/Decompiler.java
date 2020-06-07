package com.redhat.jhalliday;

import java.util.function.Function;

public interface Decompiler<LOW, HIGH> extends Function<LOW, HIGH> {
}
