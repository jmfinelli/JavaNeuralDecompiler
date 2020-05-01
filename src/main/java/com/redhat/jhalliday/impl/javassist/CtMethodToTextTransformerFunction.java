package com.redhat.jhalliday.impl.javassist;

import com.redhat.jhalliday.TransformerFunction;
import javassist.CtMethod;
import javassist.bytecode.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class CtMethodToTextTransformerFunction implements TransformerFunction<CtMethod, List<String>> {

    @Override
    public Stream<List<String>> apply(CtMethod ctMethod) {

        MethodInfo info = ctMethod.getMethodInfo2();
        ConstPool pool = info.getConstPool();
        CodeAttribute code = info.getCodeAttribute();
        if (code == null) {
            return Stream.of(Collections.emptyList());
        }

        List<String> tokens = new ArrayList<>();

        CodeIterator iterator = code.iterator();
        while (iterator.hasNext()) {
            int pos;
            try {
                pos = iterator.next();
            } catch (BadBytecode e) {
                throw new RuntimeException(e);
            }

            tokens.add(InstructionPrinterMod.instructionString(iterator, pos, pool));
        }

        return Stream.of(tokens);
    }
}
