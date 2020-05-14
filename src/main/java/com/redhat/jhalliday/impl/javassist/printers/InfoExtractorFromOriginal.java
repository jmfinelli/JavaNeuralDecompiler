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

/**
 * Simple utility class for printing the bytecode instructions of a method.
 *
 * @author Jason T. Greene
 */
public class InfoExtractorFromOriginal implements Opcode, LowInfoExtractor {

    private final static String opcodes[] = Mnemonic.OPCODE;

    private final static String DELIMITER = " ";
    private final List<String> body = new LinkedList<>();

    public InfoExtractorFromOriginal(CtMethod ctMethod) {

        List<String> body = new LinkedList<>();

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
    public Map<Integer, String> getMethodNames() { return new HashMap<>(); }

    @Override
    public Map<Integer, String> getClassNames() { return new HashMap<>(); }

    @Override
    public Map<Integer, String> getConstants() { return new HashMap<>(); }

    @Override
    public Map<Integer, String> getFieldNames() { return new HashMap<>(); }

    @Override
    public Map<Integer, String> getVariableNames() { return new HashMap<>(); }

    @Override
    public String getBody() { return String.join(DELIMITER, this.body); }

    /**
     * Gets a string representation of the bytecode instruction at the specified
     * position.
     */
    public static String instructionString(CodeIterator iter, int pos, ConstPool pool) {
        int opcode = iter.byteAt(pos);

        if (opcode > opcodes.length || opcode < 0)
            throw new IllegalArgumentException("Invalid opcode, opcode: " + opcode + " pos: "+ pos);

        String opstring = opcodes[opcode];
        return switch (opcode) {
            case BIPUSH -> opstring + " " + iter.byteAt(pos + 1);
            case SIPUSH -> opstring + " " + iter.s16bitAt(pos + 1);
            case LDC -> opstring + " " + ldc(pool, iter.byteAt(pos + 1));
            case LDC_W, LDC2_W -> opstring + " " + ldc(pool, iter.u16bitAt(pos + 1));
            case ILOAD, LLOAD, FLOAD, DLOAD, ALOAD, ISTORE, LSTORE, FSTORE, DSTORE, ASTORE -> opstring + " " + iter.byteAt(pos + 1);
            case IFEQ, IFGE, IFGT, IFLE, IFLT, IFNE, IFNONNULL, IFNULL, IF_ACMPEQ, IF_ACMPNE, IF_ICMPEQ, IF_ICMPGE, IF_ICMPGT, IF_ICMPLE, IF_ICMPLT, IF_ICMPNE -> opstring + " " + (iter.s16bitAt(pos + 1) + pos);
            case IINC -> opstring + " " + iter.byteAt(pos + 1) + " , " + iter.signedByteAt(pos + 2);
            case GOTO, JSR -> opstring + " " + (iter.s16bitAt(pos + 1) + pos);
            case RET -> opstring + " " + iter.byteAt(pos + 1);
            case TABLESWITCH -> tableSwitch(iter, pos);
            case LOOKUPSWITCH -> lookupSwitch(iter, pos);
            case GETSTATIC, PUTSTATIC, GETFIELD, PUTFIELD -> opstring + " " + fieldInfo(pool, iter.u16bitAt(pos + 1));
            case INVOKEVIRTUAL, INVOKESPECIAL, INVOKESTATIC -> opstring + " " + methodInfo(pool, iter.u16bitAt(pos + 1));
            case INVOKEINTERFACE -> opstring + " " + interfaceMethodInfo(pool, iter.u16bitAt(pos + 1));
            case INVOKEDYNAMIC -> opstring + " " + iter.u16bitAt(pos + 1);
            case NEW -> opstring + " " + classInfo(pool, iter.u16bitAt(pos + 1));
            case NEWARRAY -> opstring + " " + arrayInfo(iter.byteAt(pos + 1));
            case ANEWARRAY, CHECKCAST -> opstring + " " + classInfo(pool, iter.u16bitAt(pos + 1));
            case WIDE -> wide(iter, pos);
            case MULTIANEWARRAY -> opstring + " " + classInfo(pool, iter.u16bitAt(pos + 1));
            case GOTO_W, JSR_W -> opstring + " " + (iter.s32bitAt(pos + 1) + pos);
            default -> opstring;
        };
    }


    private static String wide(CodeIterator iter, int pos) {
        int opcode = iter.byteAt(pos + 1);
        int index = iter.u16bitAt(pos + 2);
        return switch (opcode) {
            case ILOAD, LLOAD, FLOAD, DLOAD, ALOAD, ISTORE, LSTORE, FSTORE, DSTORE, ASTORE, IINC, RET -> opcodes[opcode] + " " + index;
            default -> throw new RuntimeException("Invalid WIDE operand");
        };
    }


    private static String arrayInfo(int type) {
        return switch (type) {
            case T_BOOLEAN -> "boolean";
            case T_CHAR -> "char";
            case T_BYTE -> "byte";
            case T_SHORT -> "short";
            case T_INT -> "int";
            case T_LONG -> "long";
            case T_FLOAT -> "float";
            case T_DOUBLE -> "double";
            default -> throw new RuntimeException("Invalid array type");
        };
    }


    private static String classInfo(ConstPool pool, int index) {
        return "# " + index + " = Class " + pool.getClassInfo(index);
    }


    private static String interfaceMethodInfo(ConstPool pool, int index) {
        return "# " + index + " = Method "
                + pool.getInterfaceMethodrefClassName(index) + " . "
                + pool.getInterfaceMethodrefName(index) + " ( "
                + pool.getInterfaceMethodrefType(index) + " ) ";
    }

    private static String methodInfo(ConstPool pool, int index) {
        return "# " + index + " = Method "
                + pool.getMethodrefClassName(index) + " . "
                + pool.getMethodrefName(index) + " ( "
                + pool.getMethodrefType(index) + " ) ";
    }


    private static String fieldInfo(ConstPool pool, int index) {
        return "# " + index + " = Field "
                + pool.getFieldrefClassName(index) + " . "
                + pool.getFieldrefName(index) + " ( "
                + pool.getFieldrefType(index) + " ) ";
    }


    private static String lookupSwitch(CodeIterator iter, int pos) {
        StringBuilder buffer = new StringBuilder("lookupswitch { ");
        int index = (pos & ~3) + 4;
        // default
        buffer.append("default: ").append(pos + iter.s32bitAt(index));
        int npairs = iter.s32bitAt(index += 4);
        int end = npairs * 8 + (index += 4);

        for (; index < end; index += 8) {
            int match = iter.s32bitAt(index);
            int target = iter.s32bitAt(index + 4) + pos;
            buffer.append(" ").append(match).append(" : ").append(target).append(" ");
        }

        buffer.append('}');
        return buffer.toString();
    }


    private static String tableSwitch(CodeIterator iter, int pos) {
        StringBuilder buffer = new StringBuilder("tableswitch { ");
        int index = (pos & ~3) + 4;
        // default
        buffer.append("default: ").append(pos + iter.s32bitAt(index));
        int low = iter.s32bitAt(index += 4);
        int high = iter.s32bitAt(index += 4);
        int end = (high - low + 1) * 4 + (index += 4);

        // Offset table
        for (int key = low; index < end; index += 4, key++) {
            int target = iter.s32bitAt(index) + pos;
            buffer.append(" ").append(key).append(" : ").append(target).append(" ");
        }

        buffer.append('}');
        return buffer.toString();
    }


    private static String ldc(ConstPool pool, int index) {
        int tag = pool.getTag(index);
        return switch (tag) {
            case ConstPool.CONST_String -> "# " + index + " = \" " + pool.getStringInfo(index) + " \"";
            case ConstPool.CONST_Integer -> "# " + index + " = int " + pool.getIntegerInfo(index);
            case ConstPool.CONST_Float -> "# " + index + " = float " + pool.getFloatInfo(index);
            case ConstPool.CONST_Long -> "# " + index + " = long " + pool.getLongInfo(index);
            case ConstPool.CONST_Double -> "# " + index + " = double " + pool.getDoubleInfo(index);
            case ConstPool.CONST_Class -> classInfo(pool, index);
            default -> throw new RuntimeException("bad LDC: " + tag);
        };
    }
}
