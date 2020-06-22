package com.redhat.jhalliday.impl.javassist;

import com.redhat.jhalliday.InfoExtractor;
import javassist.CannotCompileException;
import javassist.CtMethod;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.MethodInfo;
import javassist.expr.*;

import java.util.*;

public class CtMethodInfoExtractor implements InfoExtractor<CtMethod> {

    @Override
    public Map<String, InfoType> apply(CtMethod ctMethod) {

        Map<String, InfoExtractor.InfoType> results = new HashMap<>();

        try {
            ctMethod.instrument(new visitor(results));
        } catch (CannotCompileException e) {
            e.printStackTrace();
        }

        final MethodInfo methodInfo = ctMethod.getMethodInfo();
        final CodeAttribute code = methodInfo.getCodeAttribute();
        final CodeIterator iterator = code.iterator();
        getVariableName(iterator).forEach(x -> results.putIfAbsent(x, InfoType.VAR));

        return results;
    }

    private static Set<String> getVariableName(CodeIterator iterator) {

        Set<String> results = new HashSet<>();

        CodeAttribute ca = iterator.get();
        LocalVariableAttribute localVariableTable = (LocalVariableAttribute) ca.getAttribute(LocalVariableAttribute.tag);
        if (localVariableTable != null) {
            for (int i = 0; i < localVariableTable.tableLength(); i++) {
                int localVariableIndex = localVariableTable.index(i);
                results.add(localVariableTable.variableName(i));
            }
        }

        return results;
    }

    private class visitor extends ExprEditor {

        private final Map<String, InfoExtractor.InfoType> _storage;

        public visitor(Map<String, InfoExtractor.InfoType> storage) {
            this._storage = storage;
        }

        @Override
        public void edit(MethodCall m) throws CannotCompileException {
            this._storage.putIfAbsent(m.getMethodName(), InfoType.MET);
        }

        @Override
        public void edit(NewExpr e) throws CannotCompileException {
            this._storage.putIfAbsent(e.getClassName(), InfoType.CLASS);
        }

        @Override
        public void edit(ConstructorCall c) throws CannotCompileException {
            this._storage.putIfAbsent(c.getMethodName(), InfoType.MET);
        }

        @Override
        public void edit(FieldAccess f) throws CannotCompileException {
            this._storage.putIfAbsent(f.getFieldName(), InfoType.FIELD);
        }
    }
}
