/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.codehaus.groovy.syntax.Types
 */
package org.kohsuke.groovy.sandbox.impl;

import java.util.HashMap;
import java.util.Map;
import org.codehaus.groovy.syntax.Types;

public class Ops {
    private static final Map<Integer, Integer> compoundAssignmentToBinaryOperator = new HashMap<Integer, Integer>();
    private static final Map<Integer, String> binaryOperatorMethods = new HashMap<Integer, String>();

    public static int compoundAssignmentToBinaryOperator(int type) {
        Integer o = compoundAssignmentToBinaryOperator.get(type);
        if (o == null) {
            throw new IllegalArgumentException("" + type);
        }
        return o;
    }

    public static String binaryOperatorMethods(int type) {
        String v = binaryOperatorMethods.get(type);
        if (v == null) {
            throw new IllegalArgumentException("" + type);
        }
        return v;
    }

    public static boolean isComparisionOperator(int type) {
        return Types.ofType((int)type, (int)1101);
    }

    public static boolean isRegexpComparisonOperator(int type) {
        return Types.ofType((int)type, (int)1105);
    }

    public static boolean isLogicalOperator(int type) {
        return Types.ofType((int)type, (int)1103);
    }

    static {
        Map<Integer, Integer> c = compoundAssignmentToBinaryOperator;
        c.put(210, 200);
        c.put(211, 201);
        c.put(212, 202);
        c.put(213, 203);
        c.put(214, 204);
        c.put(215, 205);
        c.put(216, 206);
        c.put(285, 280);
        c.put(286, 281);
        c.put(287, 282);
        c.put(350, 340);
        c.put(351, 341);
        c.put(352, 342);
        Map<Integer, String> b = binaryOperatorMethods;
        b.put(200, "plus");
        b.put(201, "minus");
        b.put(202, "multiply");
        b.put(206, "power");
        b.put(203, "div");
        b.put(205, "mod");
        b.put(340, "or");
        b.put(341, "and");
        b.put(342, "xor");
        b.put(280, "leftShift");
        b.put(281, "rightShift");
        b.put(282, "rightShiftUnsigned");
        b.put(123, "compareEqual");
        b.put(120, "compareNotEqual");
        b.put(124, "compareLessThan");
        b.put(125, "compareLessThanEqual");
        b.put(126, "compareGreaterThan");
        b.put(127, "compareGreaterThanEqual");
        b.put(128, "compareTo");
        b.put(90, "findRegex");
        b.put(94, "matchRegex");
    }
}

