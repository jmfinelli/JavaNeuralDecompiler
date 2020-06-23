package com.redhat.jhalliday.impl.javassist;

import com.redhat.jhalliday.InfoExtractor;
import javassist.CannotCompileException;
import javassist.CtMethod;
import javassist.bytecode.*;
import javassist.expr.*;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
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
        getConstants(methodInfo.getConstPool()).forEach(results::putIfAbsent);

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

    private static Map<String, InfoExtractor.InfoType> getConstants(ConstPool pool) {

        Map<String, InfoExtractor.InfoType> results = new HashMap<>();

        try {

            Field itemsField = pool.getClass().getDeclaredField("items");
            itemsField.setAccessible(true);
            Object items = itemsField.get(pool);

            Field objectsField = items.getClass().getDeclaredField("objects");
            objectsField.setAccessible(true);
            Object objects = objectsField.get(items);

            for (int index = 0; index < Array.getLength(objects); index++) {

                Object arrayElement = Array.get(objects, index);

                if (Objects.nonNull(arrayElement) && arrayElement.getClass().isArray()) {

                    for (int inner_index = 0; inner_index < Array.getLength(arrayElement); inner_index++) {

                        Object innerElement = Array.get(arrayElement, inner_index);

                        if (Objects.nonNull(innerElement) && innerElement.getClass().getSuperclass().toGenericString().contains("ConstInfo")) {

                            Field tagField = innerElement.getClass().getDeclaredField("tag");
                            tagField.setAccessible(true);
                            int tag = (int) tagField.get(innerElement);

                            Field indexField = innerElement.getClass().getSuperclass().getDeclaredField("index");
                            indexField.setAccessible(true);
                            int innerElementIndex = (int) indexField.get(innerElement);

                            switch (tag) {
                                case ConstPool.CONST_String:
                                    results.putIfAbsent(pool.getStringInfo(innerElementIndex), InfoType.CONST);
                                    break;
                                case ConstPool.CONST_Integer:
                                    results.putIfAbsent(String.valueOf(pool.getIntegerInfo(innerElementIndex)), InfoType.CONST);
                                    break;
                                case ConstPool.CONST_Float:
                                    results.putIfAbsent(String.valueOf(pool.getFloatInfo(innerElementIndex)), InfoType.CONST);
                                    break;
                                case ConstPool.CONST_Long:
                                    results.putIfAbsent(String.valueOf(pool.getLongInfo(innerElementIndex)), InfoType.CONST);
                                    break;
                                case ConstPool.CONST_Double:
                                    results.putIfAbsent(String.valueOf(pool.getDoubleInfo(innerElementIndex)), InfoType.CONST);
                                    break;
                                case ConstPool.CONST_Class:
                                    String clazz = pool.getClassInfo(innerElementIndex);
                                    results.putIfAbsent(clazz.substring(clazz.lastIndexOf(".") + 1), InfoType.CLASS);
                                    break;
                            }
                        }
                    }
                }
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
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
            String clazz = e.getClassName();
            this._storage.putIfAbsent(clazz.substring(clazz.lastIndexOf(".") + 1), InfoType.CLASS);
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
