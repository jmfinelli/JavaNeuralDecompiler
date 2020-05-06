package com.redhat.jhalliday.impl.javassist;

import com.redhat.jhalliday.TransformerFunction;
import javassist.CannotCompileException;
import javassist.CtMethod;
import javassist.bytecode.*;
import javassist.expr.Expr;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class CtMethodToBodyTransformerFunction implements TransformerFunction<CtMethod, String> {

    @Override
    public Stream<String> apply(CtMethod ctMethod) {

        List<String> tokens = new ArrayList<>();

        MethodInfo info = ctMethod.getMethodInfo2();
        MethodInfo methodInfo = ctMethod.getMethodInfo();
        ConstPool pool = info.getConstPool();
        CodeAttribute code = info.getCodeAttribute();

        LocalVariableAttribute localVariableTable = (LocalVariableAttribute)methodInfo.getCodeAttribute().getAttribute(LocalVariableAttribute.tag);
        if (localVariableTable != null) {
            for(int i = 0; i < localVariableTable.tableLength(); i++) {
                System.out.printf("%d) Variable name: %s\nStart: %d, Length: %d\nIndex in the Local Variables Array: %d\n",
                        i + 1,
                        pool.getUtf8Info(localVariableTable.nameIndex(i)),
                        localVariableTable.startPc(i),
                        localVariableTable.codeLength(i),
                        localVariableTable.index(i));
            }
            System.out.println();
        }

        CodeIterator iterator = code.iterator();
        while (iterator.hasNext()) {
            int pos;
            try {
                pos = iterator.next();
            } catch (BadBytecode e) {
                throw new RuntimeException(e);
            }

            tokens.add(InstructionPrinter.instructionString(iterator, pos, pool));
        }

        tokens.forEach(System.out::println);

        System.out.println();

        return tokens.stream();
    }
}
