package com.redhat.jhalliday.impl.javassist.printers;

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

import com.redhat.jhalliday.impl.LowInfoExtractor;
import javassist.CtMethod;
import javassist.bytecode.*;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Simple utility class for printing the bytecode instructions of a method.
 *
 * @author Jason T. Greene
 */
public class LowLevelPrinter implements Opcode, LowInfoExtractor {

    public final static String LABEL_SYMBOL = "L";
    public final static String POOL_SYMBOL = "#";
    public final static String LOC_VAR_SYMBOL = "$";

    private final static String SIMPLE_PATTERN = "%s %s";
    private final static String LABEL_PATTERN = String.format("%%s %s%%s", LABEL_SYMBOL);
    private final static String POOL_PATTERN = String.format("%%s %s%%s", POOL_SYMBOL);
    private final static String LOC_VAR_PATTERN = String.format("%%s %s%%s", LOC_VAR_SYMBOL);

    private final static String DELIMITER = " ";

    private final static String opcodes[] = Mnemonic.OPCODE;

    private final Map<Integer, String> methodNames = new HashMap<>();
    private final Map<Integer, String> classNames = new HashMap<>();
    private final Map<Integer, String> constants = new HashMap<>();
    private final Map<Integer, String> fieldNames = new HashMap<>();
    private final Map<Integer, String> variableNames = new HashMap<>();
    private final List<String> body = new LinkedList<>();

    public LowLevelPrinter(CtMethod ctMethod) {

        final MethodInfo methodInfo = ctMethod.getMethodInfo();
        final ConstPool pool = methodInfo.getConstPool();
        final CodeAttribute code = methodInfo.getCodeAttribute();
        final CodeIterator iterator = code.iterator();

        while (iterator.hasNext()) {
            int pos;
            try {
                pos = iterator.next();
            } catch (BadBytecode e) {
                throw new RuntimeException(e);
            }

            body.add(instructionString(iterator, pos, pool));
        }
    }

    @Override
    public Map<Integer, String> getMethodNames() {
        return methodNames;
    }

    @Override
    public Map<Integer, String> getClassNames() {
        return classNames;
    }

    @Override
    public Map<Integer, String> getConstants() {
        return constants;
    }

    @Override
    public Map<Integer, String> getFieldNames() {
        return fieldNames;
    }

    @Override
    public Map<Integer, String> getVariableNames() {
        return variableNames;
    }

    @Override
    public String getBody() {
        return String.join(DELIMITER, body);
    }

    /**
     * Gets a string representation of the bytecode instruction at the specified
     * position.
     */
    private String instructionString(CodeIterator iter, int pos, ConstPool pool) {
        int opcode = iter.byteAt(pos);

        if (opcode > opcodes.length || opcode < 0)
            throw new IllegalArgumentException("Invalid opcode, opcode: " + opcode + " pos: " + pos);

        String opstring = opcodes[opcode];
        Map.Entry<Integer, String> entry;
        switch (opcode) {
            case BIPUSH:
                return String.format(SIMPLE_PATTERN, opstring, iter.byteAt(pos + 1));
            case SIPUSH:
                return String.format(SIMPLE_PATTERN, opstring, iter.s16bitAt(pos + 1));
            case LDC:
                entry = ldc(pool, iter.byteAt(pos + 1));
                constants.putIfAbsent(entry.getKey(), entry.getValue());
                return String.format(POOL_PATTERN, opstring, iter.byteAt(pos + 1));
            case LDC_W:
            case LDC2_W:
                entry = ldc(pool, iter.u16bitAt(pos + 1));
                constants.putIfAbsent(entry.getKey(), entry.getValue());
                return String.format(POOL_PATTERN, opstring, iter.u16bitAt(pos + 1));
            case ILOAD:
            case LLOAD:
            case FLOAD:
            case DLOAD:
            case ALOAD:
            case ISTORE:
            case LSTORE:
            case FSTORE:
            case DSTORE:
            case ASTORE:
                entry = getVariableName(iter, pos, iter.byteAt(pos + 1));
                if (!entry.getValue().isEmpty())
                    this.variableNames.putIfAbsent(entry.getKey(), entry.getValue());
                return String.format(LOC_VAR_PATTERN, opstring, entry.getKey());
            case IFEQ:
            case IFGE:
            case IFGT:
            case IFLE:
            case IFLT:
            case IFNE:
            case IFNONNULL:
            case IFNULL:
            case IF_ACMPEQ:
            case IF_ACMPNE:
            case IF_ICMPEQ:
            case IF_ICMPGE:
            case IF_ICMPGT:
            case IF_ICMPLE:
            case IF_ICMPLT:
            case IF_ICMPNE:
                return String.format(LABEL_PATTERN, opstring, (iter.s16bitAt(pos + 1) + pos));
            case IINC:
                entry = getVariableName(iter, pos, iter.byteAt(pos + 1));
                if (!entry.getValue().isEmpty())
                    this.variableNames.putIfAbsent(entry.getKey(), entry.getValue());
                return String.format("%s %s%d,%d", opstring, LOC_VAR_SYMBOL, entry.getKey(), iter.signedByteAt(pos + 2));
            case GOTO:
            case JSR:
                return String.format(LABEL_PATTERN, opstring, (iter.s16bitAt(pos + 1) + pos));
            case RET:
                return String.format(SIMPLE_PATTERN, opstring, iter.byteAt(pos + 1));
            case TABLESWITCH:
                return tableSwitch(iter, pos);
            case LOOKUPSWITCH:
                return lookupSwitch(iter, pos);
            case GETSTATIC:
            case PUTSTATIC:
            case GETFIELD:
            case PUTFIELD:
                entry = fieldInfo(pool, iter.u16bitAt(pos + 1));
                fieldNames.putIfAbsent(entry.getKey(), entry.getValue());
                return String.format(POOL_PATTERN, opstring, iter.u16bitAt(pos + 1));
            case INVOKEVIRTUAL:
            case INVOKESPECIAL:
            case INVOKESTATIC:
                entry = methodInfo(pool, iter.u16bitAt(pos + 1));
                methodNames.putIfAbsent(entry.getKey(), entry.getValue());
                return String.format(POOL_PATTERN, opstring, iter.u16bitAt(pos + 1));
            case INVOKEINTERFACE:
                entry = interfaceMethodInfo(pool, iter.u16bitAt(pos + 1));
                methodNames.putIfAbsent(entry.getKey(), entry.getValue());
                return String.format(POOL_PATTERN, opstring, iter.u16bitAt(pos + 1));
            case INVOKEDYNAMIC:
                return String.format(SIMPLE_PATTERN, opstring, iter.u16bitAt(pos + 1));
            case NEWARRAY:
                // TODO: is it worth to replace primitive types?
                return String.format(SIMPLE_PATTERN, opstring, iter.byteAt(pos + 1));
            // return pos + ":" + opstring + " " + arrayInfo(iter.byteAt(pos + 1));
            case NEW:
            case ANEWARRAY:
            case CHECKCAST:
            case MULTIANEWARRAY:
                entry = classInfo(pool, iter.u16bitAt(pos + 1));
                classNames.putIfAbsent(entry.getKey(), entry.getValue());
                return String.format(POOL_PATTERN, opstring, iter.u16bitAt(pos + 1));
            case WIDE:
                return wide(iter, pos);
            case GOTO_W:
            case JSR_W:
                return String.format(LABEL_PATTERN, opstring, (iter.s32bitAt(pos + 1) + pos));
//            case Opcode.ALOAD_0:
//            case Opcode.ASTORE_0:
//            case Opcode.ILOAD_0:
//            case Opcode.ISTORE_0:
//            case Opcode.FLOAD_0:
//            case Opcode.FSTORE_0:
//            case Opcode.LLOAD_0:
//            case Opcode.LSTORE_0:
//            case Opcode.DLOAD_0:
//            case Opcode.DSTORE_0:
//            case Opcode.ALOAD_1:
//            case Opcode.ASTORE_1:
//            case Opcode.ILOAD_1:
//            case Opcode.ISTORE_1:
//            case Opcode.FLOAD_1:
//            case Opcode.FSTORE_1:
//            case Opcode.LLOAD_1:
//            case Opcode.LSTORE_1:
//            case Opcode.DLOAD_1:
//            case Opcode.DSTORE_1:
//            case Opcode.ALOAD_2:
//            case Opcode.ASTORE_2:
//            case Opcode.ILOAD_2:
//            case Opcode.ISTORE_2:
//            case Opcode.FLOAD_2:
//            case Opcode.FSTORE_2:
//            case Opcode.LLOAD_2:
//            case Opcode.LSTORE_2:
//            case Opcode.DLOAD_2:
//            case Opcode.DSTORE_2:
//            case Opcode.ALOAD_3:
//            case Opcode.ASTORE_3:
//            case Opcode.ILOAD_3:
//            case Opcode.ISTORE_3:
//            case Opcode.FLOAD_3:
//            case Opcode.FSTORE_3:
//            case Opcode.LLOAD_3:
//            case Opcode.LSTORE_3:
//            case Opcode.DLOAD_3:
//            case Opcode.DSTORE_3:
//                int index = Integer.parseInt(opstring.substring(opstring.lastIndexOf("_") + 1));
//                Map.Entry<Integer, String> variable = getVariableName(iter, pos, index);
//                this.variableNames.putIfAbsent(variable.getKey(), variable.getValue());
//                return String.format(LOC_VAR_PATTERN, opstring.substring(0, opstring.lastIndexOf("_")), index);
            default:
                return opstring;

        }
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

    private static String getVariableType(CodeIterator iterator, int index) {

        CodeAttribute ca = iterator.get();
        ConstPool pool = ca.getConstPool();
        LocalVariableTypeAttribute localVariableTypeAttribute = (LocalVariableTypeAttribute) ca.getAttribute(LocalVariableTypeAttribute.tag);
        if (localVariableTypeAttribute != null) {
            for (int i = 0; i < localVariableTypeAttribute.tableLength(); i++) {
                if (localVariableTypeAttribute.index(i) == index)
                    return pool.getUtf8Info(localVariableTypeAttribute.signatureIndex(i));
            }
        }
        return "";
    }

    private static String wide(CodeIterator iter, int pos) {
        int opcode = iter.byteAt(pos + 1);
        int index = iter.u16bitAt(pos + 2);
        switch (opcode) {
            case ILOAD:
            case LLOAD:
            case FLOAD:
            case DLOAD:
            case ALOAD:
            case ISTORE:
            case LSTORE:
            case FSTORE:
            case DSTORE:
            case ASTORE:
            case IINC:
            case RET:
                return String.format(SIMPLE_PATTERN, opcodes[opcode], index);
            default:
                throw new RuntimeException("Invalid WIDE operand");
        }
    }

    private static String arrayInfo(int type) {
        switch (type) {
            case T_BOOLEAN:
                return "boolean";
            case T_CHAR:
                return "char";
            case T_BYTE:
                return "byte";
            case T_SHORT:
                return "short";
            case T_INT:
                return "int";
            case T_LONG:
                return "long";
            case T_FLOAT:
                return "float";
            case T_DOUBLE:
                return "double";
            default:
                throw new RuntimeException("Invalid array type");
        }
    }

    private static Map.Entry<Integer, String> classInfo(ConstPool pool, int index) {
        return Map.entry(index, pool.getClassInfo(index));
    }

    private static Map.Entry<Integer, String> interfaceMethodInfo(ConstPool pool, int index) {
        return Map.entry(index, String.format("%s.%s",
                pool.getInterfaceMethodrefClassName(index),
                pool.getInterfaceMethodrefName(index)));

//        return Map.entry(index, String.format("%s.%s(%s)",
//                pool.getInterfaceMethodrefClassName(index),
//                pool.getInterfaceMethodrefName(index),
//                pool.getInterfaceMethodrefType(index)));

//        return Map.entry(index, pool.getInterfaceMethodrefName(index));
    }

    private static Map.Entry<Integer, String> methodInfo(ConstPool pool, int index) {
        return Map.entry(index, String.format("%s.%s",
                pool.getMethodrefClassName(index),
                pool.getMethodrefName(index)));

//        return Map.entry(index, String.format("%s.%s(%s)",
//                pool.getMethodrefClassName(index),
//                pool.getMethodrefName(index),
//                pool.getMethodrefType(index)));

//        return Map.entry(index, pool.getMethodrefName(index));
    }

    private static Map.Entry<Integer, String> fieldInfo(ConstPool pool, int index) {
        return Map.entry(index, String.format("%s.%s",
                pool.getFieldrefClassName(index),
                pool.getFieldrefName(index)));

//        return Map.entry(index, String.format("%s.%s(%s)",
//                pool.getFieldrefClassName(index),
//                pool.getFieldrefName(index),
//                pool.getFieldrefType(index)));

//        return Map.entry(index, pool.getFieldrefName(index));
    }

    private static String lookupSwitch(CodeIterator iter, int pos) {
        StringBuilder buffer = new StringBuilder("lookupswitch {\n");
        int index = (pos & ~3) + 4;
        // default
        buffer.append("\t\tdefault:").append(pos + iter.s32bitAt(index)).append("\n");
        int npairs = iter.s32bitAt(index += 4);
        int end = npairs * 8 + (index += 4);

        for (; index < end; index += 8) {
            int match = iter.s32bitAt(index);
            int target = iter.s32bitAt(index + 4) + pos;
            buffer.append(match).append(LABEL_SYMBOL).append(target).append(";");
        }

        buffer.setCharAt(buffer.length() - 1, '}');
        return buffer.toString();
    }

    private static String tableSwitch(CodeIterator iter, int pos) {
        StringBuilder buffer = new StringBuilder("tableswitch {\n");
        int index = (pos & ~3) + 4;
        // default
        buffer.append("\t\tdefault:").append(pos + iter.s32bitAt(index)).append("\n");
        int low = iter.s32bitAt(index += 4);
        int high = iter.s32bitAt(index += 4);
        int end = (high - low + 1) * 4 + (index += 4);

        // Offset table
        for (int key = low; index < end; index += 4, key++) {
            int target = iter.s32bitAt(index) + pos;
            buffer.append(key).append(LABEL_SYMBOL).append(target).append(";");
        }

        buffer.setCharAt(buffer.length() - 1, '}');
        return buffer.toString();
    }

    private static Map.Entry<Integer, String> ldc(ConstPool pool, int index) {

        int tag = pool.getTag(index);

        switch (tag) {
            case ConstPool.CONST_String:
                return Map.entry(index, pool.getStringInfo(index));
            case ConstPool.CONST_Integer:
                return Map.entry(index, String.valueOf(pool.getIntegerInfo(index)));
            case ConstPool.CONST_Float:
                return Map.entry(index, String.valueOf(pool.getFloatInfo(index)));
            case ConstPool.CONST_Long:
                return Map.entry(index, String.valueOf(pool.getLongInfo(index)));
            case ConstPool.CONST_Double:
                return Map.entry(index, String.valueOf(pool.getDoubleInfo(index)));
            case ConstPool.CONST_Class:
                return classInfo(pool, index);
            default:
                throw new RuntimeException("bad LDC: " + tag);
        }
    }
}