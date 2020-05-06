package com.redhat.jhalliday;

public interface DecompilationRecordWithDic<LOW, HIGH, DIC> extends DecompilationRecord<LOW, HIGH> {

    DIC getDictionary();
}
