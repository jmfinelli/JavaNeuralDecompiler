package com.redhat.jhalliday.impl.javassist.extractors;

/*
 * Javassist, a Java-bytecode translator toolkit.
 * Copyright (C) 1999- Shigeru Chiba. All Rights Reserved.
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License.  Alternatively, the contents of this file may be used under
 * the terms of the GNU Lesser General Public License Version 2.1 or later,
 * or the Apache License Version 2.0.
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 */

import com.redhat.jhalliday.InfoExtractor;
import javassist.CtMethod;
import javassist.bytecode.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Simple utility class for printing the bytecode instructions of a method.
 *
 * @author Jason T. Greene
 */
public class LowLevelInfoExtractor implements Opcode, InfoExtractor<CtMethod> {

    private final static String opcodes[] = Mnemonic.OPCODE;

    @Override
    public Map<String, InfoType> apply(CtMethod ctMethod) {

        final Map<String, com.redhat.jhalliday.InfoExtractor.InfoType> infoTypeMap = new HashMap<>();

        final MethodInfo methodInfo = ctMethod.getMethodInfo();
        final ConstPool pool = methodInfo.getConstPool();
        final CodeAttribute code = methodInfo.getCodeAttribute();
        final CodeIterator iterator = code.iterator();

        getVariableName(iterator).forEach(x -> infoTypeMap.putIfAbsent(x, InfoType.VAR));

        while (iterator.hasNext()) {
            int pos;
            try {
                pos = iterator.next();
            } catch (BadBytecode e) {
                throw new RuntimeException(e);
            }

            Map.Entry<String, InfoType> entry = extractInfoFromInstruction(iterator, pos, pool);
            if (entry != null) {
                infoTypeMap.putIfAbsent(entry.getKey(), entry.getValue());
            }
        }

        return infoTypeMap;
    }

    private static Set<String> getVariableName(CodeIterator iterator) {

        Set<String> results = new HashSet<>();

        CodeAttribute ca = iterator.get();
        LocalVariableAttribute localVariableTable = (LocalVariableAttribute) ca.getAttribute(LocalVariableAttribute.tag);
        if (localVariableTable != null) {
            for (int index = 0; index < localVariableTable.tableLength(); index++) {
                int localVariableIndex = localVariableTable.index(index);
                if (!localVariableTable.variableName(index).equals("this")) {
                    results.add(localVariableTable.variableName(index));
                }
            }
        }

        return results;
    }

    /**
     * Gets a string representation of the bytecode instruction at the specified
     * position.
     */
    private Map.Entry<String, InfoType> extractInfoFromInstruction(CodeIterator iter, int pos, ConstPool pool) {
        int opcode = iter.byteAt(pos);

        if (opcode > opcodes.length || opcode < 0)
            throw new IllegalArgumentException("Invalid opcode, opcode: " + opcode + " pos: " + pos);

        switch (opcode) {
            case LDC:
                return ldc(pool, iter.byteAt(pos + 1));
            case LDC_W:
            case LDC2_W:
                return ldc(pool, iter.u16bitAt(pos + 1));
            case GETSTATIC:
            case PUTSTATIC:
            case GETFIELD:
            case PUTFIELD:
                return fieldInfo(pool, iter.u16bitAt(pos + 1));
            case INVOKEVIRTUAL:
            case INVOKESPECIAL:
            case INVOKESTATIC:
                return methodInfo(pool, iter.u16bitAt(pos + 1));
            case INVOKEINTERFACE:
                return interfaceMethodInfo(pool, iter.u16bitAt(pos + 1));
            case NEW:
            case ANEWARRAY:
            case CHECKCAST:
            case MULTIANEWARRAY:
                return classInfo(pool, iter.u16bitAt(pos + 1));
        }

        return null;
    }

    private static Map.Entry<Integer, String> getVariableName(CodeIterator iterator, int current, int index) {

        CodeAttribute ca = iterator.get();
        LocalVariableAttribute localVariableTable = (LocalVariableAttribute) ca.getAttribute(LocalVariableAttribute.tag);
        if (localVariableTable != null) {
            for (int i = 0; i < localVariableTable.tableLength(); i++) {
                int localVariableIndex = localVariableTable.index(i);
                if (localVariableIndex == index) {
                    int start = localVariableTable.startPc(i);
                    int end = start + localVariableTable.codeLength(i);
                    if (current >= start - 2 && current < end) {
                        String variableName = localVariableTable.variableName(i);
                        if (!variableName.equals("this")) {
                            return Map.entry(i, localVariableTable.variableName(i));
                        }
                    }
                }
            }
        }

        return Map.entry(index, "");
    }

    private static Map.Entry<String, InfoType> classInfo(ConstPool pool, int index) {
        String clazz = pool.getClassInfo(index);
        return Map.entry(clazz.substring(clazz.lastIndexOf(".") + 1), InfoType.CLASS);
    }

    private static Map.Entry<String, InfoType> interfaceMethodInfo(ConstPool pool, int index) {

        return Map.entry(pool.getInterfaceMethodrefName(index), InfoType.MET);
    }

    private static Map.Entry<String, InfoType> methodInfo(ConstPool pool, int index) {

        return Map.entry(pool.getMethodrefName(index), InfoType.MET);
    }

    private static Map.Entry<String, InfoType> fieldInfo(ConstPool pool, int index) {

        return Map.entry(pool.getFieldrefName(index), InfoType.FIELD);
    }

    private static Map.Entry<String, InfoType> ldc(ConstPool pool, int index) {

        int tag = pool.getTag(index);

        switch (tag) {
            case ConstPool.CONST_String:
                return Map.entry(pool.getStringInfo(index), InfoType.CONST);
            case ConstPool.CONST_Integer:
                return Map.entry(String.valueOf(pool.getIntegerInfo(index)), InfoType.CONST);
            case ConstPool.CONST_Float:
                return Map.entry(String.valueOf(pool.getFloatInfo(index)), InfoType.CONST);
            case ConstPool.CONST_Long:
                return Map.entry(String.valueOf(pool.getLongInfo(index)), InfoType.CONST);
            case ConstPool.CONST_Double:
                return Map.entry(String.valueOf(pool.getDoubleInfo(index)), InfoType.CONST);
            case ConstPool.CONST_Class:
                return classInfo(pool, index);
            default:
                throw new RuntimeException("bad LDC: " + tag);
        }
    }
}