/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.util.evaluator;

import com.labvantage.sapphire.util.evaluator.ExpressionEvaluatorConstants;
import com.labvantage.sapphire.util.evaluator.SimpleCharStream;
import com.labvantage.sapphire.util.evaluator.Token;
import com.labvantage.sapphire.util.evaluator.TokenMgrError;
import java.io.IOException;
import java.io.PrintStream;

public class ExpressionEvaluatorTokenManager
implements ExpressionEvaluatorConstants {
    public PrintStream debugStream = System.out;
    static final long[] jjbitVec0 = new long[]{0L, 0L, -1L, -1L};
    static final int[] jjnextStates = new int[]{50, 51, 52, 5, 55, 56, 59, 60, 66, 67, 7, 8, 10, 1, 2, 5, 7, 8, 12, 10, 16, 17, 51, 52, 5, 61, 62, 5, 72, 73, 69, 70, 3, 4, 9, 11, 13, 53, 54, 57, 58, 63, 64};
    public static final String[] jjstrLiteralImages = new String[]{"", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, "\n", null, null, null, null, null, null, null, null, null, null, null, "+", "-", "*", "/", "%", "^", "(", ")", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, ","};
    public static final String[] lexStateNames = new String[]{"DEFAULT"};
    static final long[] jjtoToken = new long[]{-111L, 0x1FFFFFFFFFFL};
    static final long[] jjtoSkip = new long[]{14L, 0L};
    protected SimpleCharStream input_stream;
    private final int[] jjrounds = new int[74];
    private final int[] jjstateSet = new int[148];
    protected char curChar;
    int curLexState = 0;
    int defaultLexState = 0;
    int jjnewStateCnt;
    int jjround;
    int jjmatchedPos;
    int jjmatchedKind;

    public void setDebugStream(PrintStream ds) {
        this.debugStream = ds;
    }

    private final int jjStopStringLiteralDfa_0(int pos, long active0, long active1) {
        switch (pos) {
            case 0: {
                if ((active0 & 0x120100000000000L) != 0L || (active1 & 0x6480000L) != 0L) {
                    return 69;
                }
                if ((active1 & 0x1000L) != 0L) {
                    return 43;
                }
                if ((active0 & 0x14000000000000L) != 0L || (active1 & 0x4000220008L) != 0L) {
                    return 21;
                }
                if ((active0 & 0x80010000000000L) != 0L || (active1 & 0x1000000L) != 0L) {
                    return 25;
                }
                if ((active0 & 0x8001000000L) != 0L || (active1 & 0x8000000000L) != 0L) {
                    return 39;
                }
                if ((active0 & 0x24000000000L) != 0L || (active1 & 1L) != 0L) {
                    return 30;
                }
                return -1;
            }
            case 1: {
                if ((active0 & 0x10000000000L) != 0L) {
                    return 24;
                }
                if ((active1 & 0x2480000L) != 0L) {
                    this.jjmatchedKind = 16;
                    this.jjmatchedPos = 1;
                    return -1;
                }
                if ((active0 & 0x10000000000000L) != 0L || (active1 & 0x200000L) != 0L) {
                    return 20;
                }
                return -1;
            }
            case 2: {
                if ((active1 & 0x2480000L) != 0L) {
                    if (this.jjmatchedPos < 1) {
                        this.jjmatchedKind = 16;
                        this.jjmatchedPos = 1;
                    }
                    return -1;
                }
                if ((active0 & 0x10000000000000L) != 0L) {
                    return 19;
                }
                return -1;
            }
            case 3: {
                if ((active1 & 0x480000L) != 0L) {
                    if (this.jjmatchedPos < 1) {
                        this.jjmatchedKind = 16;
                        this.jjmatchedPos = 1;
                    }
                    return -1;
                }
                return -1;
            }
            case 4: {
                if ((active1 & 0x80000L) != 0L) {
                    if (this.jjmatchedPos < 1) {
                        this.jjmatchedKind = 16;
                        this.jjmatchedPos = 1;
                    }
                    return -1;
                }
                return -1;
            }
            case 5: {
                if ((active1 & 0x80000L) != 0L) {
                    if (this.jjmatchedPos < 1) {
                        this.jjmatchedKind = 16;
                        this.jjmatchedPos = 1;
                    }
                    return -1;
                }
                return -1;
            }
            case 6: {
                if ((active1 & 0x80000L) != 0L) {
                    if (this.jjmatchedPos < 1) {
                        this.jjmatchedKind = 16;
                        this.jjmatchedPos = 1;
                    }
                    return -1;
                }
                return -1;
            }
            case 7: {
                if ((active1 & 0x80000L) != 0L) {
                    if (this.jjmatchedPos < 1) {
                        this.jjmatchedKind = 16;
                        this.jjmatchedPos = 1;
                    }
                    return -1;
                }
                return -1;
            }
        }
        return -1;
    }

    private final int jjStartNfa_0(int pos, long active0, long active1) {
        return this.jjMoveNfa_0(this.jjStopStringLiteralDfa_0(pos, active0, active1), pos + 1);
    }

    private final int jjStopAtPos(int pos, int kind) {
        this.jjmatchedKind = kind;
        this.jjmatchedPos = pos;
        return pos + 1;
    }

    private final int jjStartNfaWithStates_0(int pos, int kind, int state) {
        this.jjmatchedKind = kind;
        this.jjmatchedPos = pos;
        try {
            this.curChar = this.input_stream.readChar();
        }
        catch (IOException e) {
            return pos + 1;
        }
        return this.jjMoveNfa_0(state, pos + 1);
    }

    private final int jjMoveStringLiteralDfa0_0() {
        switch (this.curChar) {
            case '\n': {
                return this.jjStopAtPos(0, 18);
            }
            case '%': {
                return this.jjStopAtPos(0, 34);
            }
            case '(': {
                return this.jjStopAtPos(0, 36);
            }
            case ')': {
                return this.jjStopAtPos(0, 37);
            }
            case '*': {
                return this.jjStopAtPos(0, 32);
            }
            case '+': {
                return this.jjStopAtPos(0, 30);
            }
            case ',': {
                return this.jjStopAtPos(0, 104);
            }
            case '-': {
                return this.jjStopAtPos(0, 31);
            }
            case '/': {
                return this.jjStopAtPos(0, 33);
            }
            case '^': {
                return this.jjStopAtPos(0, 35);
            }
            case 'A': 
            case 'a': {
                return this.jjMoveStringLiteralDfa1_0(0x24000000000L, 1L);
            }
            case 'C': 
            case 'c': {
                return this.jjMoveStringLiteralDfa1_0(-4593667221837316096L, 0x1000000000L);
            }
            case 'D': 
            case 'd': {
                return this.jjMoveStringLiteralDfa1_0(0L, 500L);
            }
            case 'E': 
            case 'e': {
                return this.jjMoveStringLiteralDfa1_0(0x8001000000L, 0x8000000000L);
            }
            case 'F': 
            case 'f': {
                return this.jjMoveStringLiteralDfa1_0(0x80010000000000L, 0x1000000L);
            }
            case 'H': 
            case 'h': {
                return this.jjMoveStringLiteralDfa1_0(0L, 512L);
            }
            case 'I': 
            case 'i': {
                return this.jjMoveStringLiteralDfa1_0(8797099655168L, 0x400000000L);
            }
            case 'L': 
            case 'l': {
                return this.jjMoveStringLiteralDfa1_0(0x120100000000000L, 105381888L);
            }
            case 'M': 
            case 'm': {
                return this.jjMoveStringLiteralDfa1_0(0x600200000600000L, 402656258L);
            }
            case 'N': 
            case 'n': {
                return this.jjMoveStringLiteralDfa1_0(0L, 4096L);
            }
            case 'P': 
            case 'p': {
                return this.jjMoveStringLiteralDfa1_0(0x400000000000L, 0x20000000L);
            }
            case 'R': 
            case 'r': {
                return this.jjMoveStringLiteralDfa1_0(0x1800000000000L, 1083203584L);
            }
            case 'S': 
            case 's': {
                return this.jjMoveStringLiteralDfa1_0(2885118511293661184L, 0x880018000L);
            }
            case 'T': 
            case 't': {
                return this.jjMoveStringLiteralDfa1_0(0x14000000000000L, 274880135176L);
            }
            case 'U': 
            case 'u': {
                return this.jjMoveStringLiteralDfa1_0(0L, 0x100000000L);
            }
            case 'V': 
            case 'v': {
                return this.jjMoveStringLiteralDfa1_0(0x1000000000000000L, 0L);
            }
            case 'W': 
            case 'w': {
                return this.jjMoveStringLiteralDfa1_0(0L, 0x2200000000L);
            }
            case 'Y': 
            case 'y': {
                return this.jjMoveStringLiteralDfa1_0(0L, 262144L);
            }
        }
        return this.jjMoveNfa_0(0, 0);
    }

    private final int jjMoveStringLiteralDfa1_0(long active0, long active1) {
        try {
            this.curChar = this.input_stream.readChar();
        }
        catch (IOException e) {
            this.jjStopStringLiteralDfa_0(0, active0, active1);
            return 1;
        }
        switch (this.curChar) {
            case 'A': 
            case 'a': {
                return this.jjMoveStringLiteralDfa2_0(active0, 1226246835553697792L, active1, 68853694966L);
            }
            case 'B': 
            case 'b': {
                return this.jjMoveStringLiteralDfa2_0(active0, 0x20000000000L, active1, 0L);
            }
            case 'E': 
            case 'e': {
                return this.jjMoveStringLiteralDfa2_0(active0, 0x200040000000000L, active1, 1112399872L);
            }
            case 'F': 
            case 'f': {
                if ((active1 & 0x400000000L) == 0L) break;
                return this.jjStopAtPos(1, 98);
            }
            case 'H': 
            case 'h': {
                return this.jjMoveStringLiteralDfa2_0(active0, 0L, active1, 0x6000000000L);
            }
            case 'I': 
            case 'i': {
                if ((active0 & 0x400000000000L) != 0L) {
                    return this.jjStopAtPos(1, 46);
                }
                return this.jjMoveStringLiteralDfa2_0(active0, 2342434756191322112L, active1, 294650888L);
            }
            case 'L': 
            case 'l': {
                return this.jjMoveStringLiteralDfa2_0(active0, -4611686018427387904L, active1, 0x8000000000L);
            }
            case 'N': 
            case 'n': {
                return this.jjMoveStringLiteralDfa2_0(active0, 0x80000000000L, active1, 0L);
            }
            case 'O': 
            case 'o': {
                return this.jjMoveStringLiteralDfa2_0(active0, 315586225484857344L, active1, 9194052096L);
            }
            case 'P': 
            case 'p': {
                return this.jjMoveStringLiteralDfa2_0(active0, 0L, active1, 0x180000000L);
            }
            case 'Q': 
            case 'q': {
                return this.jjMoveStringLiteralDfa2_0(active0, 0x8000000000000L, active1, 0L);
            }
            case 'R': 
            case 'r': {
                return this.jjMoveStringLiteralDfa2_0(active0, 0x10000000000000L, active1, 0x200000L);
            }
            case 'S': 
            case 's': {
                return this.jjMoveStringLiteralDfa2_0(active0, 0x3C000000L, active1, 1L);
            }
            case 'T': 
            case 't': {
                return this.jjMoveStringLiteralDfa2_0(active0, 0x800000000000000L, active1, 0x800000000L);
            }
            case 'U': 
            case 'u': {
                return this.jjMoveStringLiteralDfa2_0(active0, 0x800000L, active1, 0L);
            }
            case 'V': 
            case 'v': {
                return this.jjMoveStringLiteralDfa2_0(active0, 0x4000000000L, active1, 0L);
            }
            case 'X': 
            case 'x': {
                return this.jjMoveStringLiteralDfa2_0(active0, 0x8001000000L, active1, 0L);
            }
        }
        return this.jjStartNfa_0(0, active0, active1);
    }

    private final int jjMoveStringLiteralDfa2_0(long old0, long active0, long old1, long active1) {
        if (((active0 &= old0) | (active1 &= old1)) == 0L) {
            return this.jjStartNfa_0(0, old0, old1);
        }
        try {
            this.curChar = this.input_stream.readChar();
        }
        catch (IOException e) {
            this.jjStopStringLiteralDfa_0(1, active0, active1);
            return 2;
        }
        switch (this.curChar) {
            case 'A': 
            case 'a': {
                return this.jjMoveStringLiteralDfa3_0(active0, 0L, active1, 0x80040000L);
            }
            case 'C': 
            case 'c': {
                return this.jjMoveStringLiteralDfa3_0(active0, 0x10000000000L, active1, 98304L);
            }
            case 'D': 
            case 'd': {
                if ((active0 & 0x200000000000L) != 0L) {
                    this.jjmatchedKind = 45;
                    this.jjmatchedPos = 2;
                } else if ((active1 & 0x10000000L) != 0L) {
                    return this.jjStopAtPos(2, 92);
                }
                return this.jjMoveStringLiteralDfa3_0(active0, 0xE00000008000000L, active1, 131072L);
            }
            case 'E': 
            case 'e': {
                return this.jjMoveStringLiteralDfa3_0(active0, 0L, active1, 0x6000000000L);
            }
            case 'F': 
            case 'f': {
                return this.jjMoveStringLiteralDfa3_0(active0, 0L, active1, 0x480000L);
            }
            case 'G': 
            case 'g': {
                if ((active0 & 0x4000000000L) != 0L) {
                    return this.jjStopAtPos(2, 38);
                }
                if ((active0 & 0x100000000000L) != 0L) {
                    this.jjmatchedKind = 44;
                    this.jjmatchedPos = 2;
                }
                return this.jjMoveStringLiteralDfa3_0(active0, 0x2022000000000000L, active1, 0x900000L);
            }
            case 'I': 
            case 'i': {
                return this.jjMoveStringLiteralDfa3_0(active0, 0x40000000000L, active1, 0x200000L);
            }
            case 'L': 
            case 'l': {
                return this.jjMoveStringLiteralDfa3_0(active0, 0x2000000L, active1, 0x1006000L);
            }
            case 'M': 
            case 'm': {
                if ((active0 & 0x800000L) != 0L) {
                    return this.jjStopAtPos(2, 23);
                }
                return this.jjMoveStringLiteralDfa3_0(active0, 0L, active1, 8L);
            }
            case 'N': 
            case 'n': {
                if ((active0 & 0x100000L) != 0L) {
                    return this.jjStopAtPos(2, 20);
                }
                if ((active0 & 0x400000L) != 0L) {
                    this.jjmatchedKind = 22;
                    this.jjmatchedPos = 2;
                } else {
                    if ((active0 & 0x4000000000000L) != 0L) {
                        return this.jjStopAtPos(2, 50);
                    }
                    if ((active1 & 0x2000000L) != 0L) {
                        return this.jjStopAtPos(2, 89);
                    }
                }
                return this.jjMoveStringLiteralDfa3_0(active0, 140738092335104L, active1, 3072L);
            }
            case 'P': 
            case 'p': {
                if ((active0 & 0x8000000000L) != 0L) {
                    return this.jjStopAtPos(2, 39);
                }
                return this.jjMoveStringLiteralDfa3_0(active0, -4611686018427387904L, active1, 0x140000000L);
            }
            case 'R': 
            case 'r': {
                if ((active0 & 0x1000000000000000L) != 0L) {
                    return this.jjStopAtPos(2, 60);
                }
                return this.jjMoveStringLiteralDfa3_0(active0, 0x88000000000000L, active1, 0xA00000000L);
            }
            case 'S': 
            case 's': {
                if ((active0 & 0x80000L) != 0L) {
                    return this.jjStopAtPos(2, 19);
                }
                if ((active0 & 0x20000000000L) != 0L) {
                    return this.jjStopAtPos(2, 41);
                }
                if ((active1 & 0x20000000L) != 0L) {
                    return this.jjStopAtPos(2, 93);
                }
                return this.jjMoveStringLiteralDfa3_0(active0, 0x100000000000000L, active1, 0x9000000000L);
            }
            case 'T': 
            case 't': {
                if ((active0 & 0x80000000000L) != 0L) {
                    return this.jjStopAtPos(2, 43);
                }
                return this.jjMoveStringLiteralDfa3_0(active0, 0x11000000L, active1, 134217749L);
            }
            case 'U': 
            case 'u': {
                return this.jjMoveStringLiteralDfa3_0(active0, 0x51000000000000L, active1, 512L);
            }
            case 'W': 
            case 'w': {
                if ((active1 & 0x1000L) != 0L) {
                    return this.jjStopAtPos(2, 76);
                }
                return this.jjMoveStringLiteralDfa3_0(active0, 0L, active1, 0x4000000L);
            }
            case 'X': 
            case 'x': {
                if ((active0 & 0x200000L) != 0L) {
                    this.jjmatchedKind = 21;
                    this.jjmatchedPos = 2;
                }
                return this.jjMoveStringLiteralDfa3_0(active0, 0L, active1, 2L);
            }
            case 'Y': 
            case 'y': {
                if ((active1 & 0x20L) != 0L) {
                    this.jjmatchedKind = 69;
                    this.jjmatchedPos = 2;
                }
                return this.jjMoveStringLiteralDfa3_0(active0, 0L, active1, 448L);
            }
        }
        return this.jjStartNfa_0(1, active0, active1);
    }

    private final int jjMoveStringLiteralDfa3_0(long old0, long active0, long old1, long active1) {
        if (((active0 &= old0) | (active1 &= old1)) == 0L) {
            return this.jjStartNfa_0(1, old0, old1);
        }
        try {
            this.curChar = this.input_stream.readChar();
        }
        catch (IOException e) {
            this.jjStopStringLiteralDfa_0(2, active0, active1);
            return 3;
        }
        switch (this.curChar) {
            case 'A': 
            case 'a': {
                return this.jjMoveStringLiteralDfa4_0(active0, 0x8000000L, active1, 155648L);
            }
            case 'C': 
            case 'c': {
                return this.jjMoveStringLiteralDfa4_0(active0, 0L, active1, 0x88000000L);
            }
            case 'D': 
            case 'd': {
                if ((active0 & 0x800000000000L) != 0L) {
                    return this.jjStopAtPos(3, 47);
                }
                return this.jjMoveStringLiteralDfa4_0(active0, 0L, active1, 0x200000000L);
            }
            case 'E': 
            case 'e': {
                if ((active0 & 0x400000000000000L) != 0L) {
                    return this.jjStopAtPos(3, 58);
                }
                if ((active1 & 4L) != 0L) {
                    this.jjmatchedKind = 66;
                    this.jjmatchedPos = 3;
                } else {
                    if ((active1 & 8L) != 0L) {
                        return this.jjStopAtPos(3, 67);
                    }
                    if ((active1 & 0x1000000000L) != 0L) {
                        return this.jjStopAtPos(3, 100);
                    }
                    if ((active1 & 0x8000000000L) != 0L) {
                        return this.jjStopAtPos(3, 103);
                    }
                }
                return this.jjMoveStringLiteralDfa4_0(active0, 0x800000001000000L, active1, 0x104000010L);
            }
            case 'F': 
            case 'f': {
                return this.jjMoveStringLiteralDfa4_0(active0, 0x2000000000000000L, active1, 0L);
            }
            case 'H': 
            case 'h': {
                return this.jjMoveStringLiteralDfa4_0(active0, 0L, active1, 0x900000L);
            }
            case 'I': 
            case 'i': {
                return this.jjMoveStringLiteralDfa4_0(active0, -9079256848510484480L, active1, 0x800000000L);
            }
            case 'L': 
            case 'l': {
                if ((active1 & 0x1000000L) != 0L) {
                    return this.jjStopAtPos(3, 88);
                }
                return this.jjMoveStringLiteralDfa4_0(active0, 0x40000000000L, active1, 0x40000000L);
            }
            case 'M': 
            case 'm': {
                if ((active1 & 0x200000L) != 0L) {
                    return this.jjStopAtPos(3, 85);
                }
                return this.jjMoveStringLiteralDfa4_0(active0, 0L, active1, 1L);
            }
            case 'N': 
            case 'n': {
                if ((active0 & 0x2000000000000L) != 0L) {
                    return this.jjStopAtPos(3, 49);
                }
                if ((active1 & 0x2000000000L) != 0L) {
                    return this.jjStopAtPos(3, 101);
                }
                if ((active1 & 0x4000000000L) != 0L) {
                    return this.jjStopAtPos(3, 102);
                }
                return this.jjMoveStringLiteralDfa4_0(active0, 0x51000000000000L, active1, 192L);
            }
            case 'O': 
            case 'o': {
                return this.jjMoveStringLiteralDfa4_0(active0, 0x4000000002000000L, active1, 98304L);
            }
            case 'R': 
            case 'r': {
                if ((active1 & 0x200L) != 0L) {
                    return this.jjStopAtPos(3, 73);
                }
                if ((active1 & 0x40000L) == 0L) break;
                return this.jjStopAtPos(3, 82);
            }
            case 'S': 
            case 's': {
                return this.jjMoveStringLiteralDfa4_0(active0, 0x80000000000000L, active1, 258L);
            }
            case 'T': 
            case 't': {
                if ((active0 & 0x10000000000L) != 0L) {
                    return this.jjStopAtPos(3, 40);
                }
                if ((active0 & 0x8000000000000L) != 0L) {
                    return this.jjStopAtPos(3, 51);
                }
                if ((active0 & 0x100000000000000L) != 0L) {
                    return this.jjStopAtPos(3, 56);
                }
                if ((active1 & 0x400000L) != 0L) {
                    this.jjmatchedKind = 86;
                    this.jjmatchedPos = 3;
                }
                return this.jjMoveStringLiteralDfa4_0(active0, 0x20000000000000L, active1, 526336L);
            }
            case 'U': 
            case 'u': {
                return this.jjMoveStringLiteralDfa4_0(active0, 0x24000000L, active1, 1024L);
            }
        }
        return this.jjStartNfa_0(2, active0, active1);
    }

    private final int jjMoveStringLiteralDfa4_0(long old0, long active0, long old1, long active1) {
        if (((active0 &= old0) | (active1 &= old1)) == 0L) {
            return this.jjStartNfa_0(2, old0, old1);
        }
        try {
            this.curChar = this.input_stream.readChar();
        }
        catch (IOException e) {
            this.jjStopStringLiteralDfa_0(3, active0, active1);
            return 4;
        }
        switch (this.curChar) {
            case 'A': 
            case 'a': {
                return this.jjMoveStringLiteralDfa5_0(active0, 0x200000000000000L, active1, 0x40000140L);
            }
            case 'C': 
            case 'c': {
                return this.jjMoveStringLiteralDfa5_0(active0, 0x10000000000000L, active1, 0x200000000L);
            }
            case 'D': 
            case 'd': {
                if ((active0 & 0x1000000000000L) == 0L) break;
                return this.jjStopAtPos(4, 48);
            }
            case 'E': 
            case 'e': {
                if ((active1 & 0x80000000L) != 0L) {
                    return this.jjStopAtPos(4, 95);
                }
                return this.jjMoveStringLiteralDfa5_0(active0, 0x20000000000000L, active1, 0L);
            }
            case 'H': 
            case 'h': {
                if ((active1 & 0x800L) != 0L) {
                    return this.jjStopAtPos(4, 75);
                }
                if ((active1 & 0x8000000L) == 0L) break;
                return this.jjStopAtPos(4, 91);
            }
            case 'I': 
            case 'i': {
                return this.jjMoveStringLiteralDfa5_0(active0, 0x2000040000000000L, active1, 2L);
            }
            case 'L': 
            case 'l': {
                return this.jjMoveStringLiteralDfa5_0(active0, 0x4000000L, active1, 0L);
            }
            case 'M': 
            case 'm': {
                return this.jjMoveStringLiteralDfa5_0(active0, 0x30000000L, active1, 0L);
            }
            case 'N': 
            case 'n': {
                return this.jjMoveStringLiteralDfa5_0(active0, Long.MIN_VALUE, active1, 0x800018000L);
            }
            case 'R': 
            case 'r': {
                if ((active0 & 0x2000000L) != 0L) {
                    return this.jjStopAtPos(4, 25);
                }
                if ((active1 & 0x4000000L) != 0L) {
                    return this.jjStopAtPos(4, 90);
                }
                if ((active1 & 0x100000000L) != 0L) {
                    return this.jjStopAtPos(4, 96);
                }
                return this.jjMoveStringLiteralDfa5_0(active0, 0x4000000001000000L, active1, 1L);
            }
            case 'T': 
            case 't': {
                if ((active0 & 0x40000000000000L) != 0L) {
                    return this.jjStopAtPos(4, 54);
                }
                if ((active0 & 0x80000000000000L) != 0L) {
                    return this.jjStopAtPos(4, 55);
                }
                if ((active1 & 0x800000L) != 0L) {
                    this.jjmatchedKind = 87;
                    this.jjmatchedPos = 4;
                }
                return this.jjMoveStringLiteralDfa5_0(active0, 0x8000000L, active1, 1598480L);
            }
            case 'U': 
            case 'u': {
                return this.jjMoveStringLiteralDfa5_0(active0, 0L, active1, 128L);
            }
            case 'V': 
            case 'v': {
                if ((active0 & 0x800000000000000L) == 0L) break;
                return this.jjStopAtPos(4, 59);
            }
            case 'Y': 
            case 'y': {
                if ((active1 & 0x20000L) == 0L) break;
                return this.jjStopAtPos(4, 81);
            }
        }
        return this.jjStartNfa_0(3, active0, active1);
    }

    private final int jjMoveStringLiteralDfa5_0(long old0, long active0, long old1, long active1) {
        if (((active0 &= old0) | (active1 &= old1)) == 0L) {
            return this.jjStartNfa_0(3, old0, old1);
        }
        try {
            this.curChar = this.input_stream.readChar();
        }
        catch (IOException e) {
            this.jjStopStringLiteralDfa_0(4, active0, active1);
            return 5;
        }
        switch (this.curChar) {
            case 'A': 
            case 'a': {
                return this.jjMoveStringLiteralDfa6_0(active0, 0x10000000000000L, active1, 0x200000000L);
            }
            case 'B': 
            case 'b': {
                return this.jjMoveStringLiteralDfa6_0(active0, 0x20000000L, active1, 0L);
            }
            case 'C': 
            case 'c': {
                return this.jjMoveStringLiteralDfa6_0(active0, 0L, active1, 0x40000000L);
            }
            case 'D': 
            case 'd': {
                if ((active1 & 0x8000L) != 0L) {
                    this.jjmatchedKind = 79;
                    this.jjmatchedPos = 5;
                }
                return this.jjMoveStringLiteralDfa6_0(active0, 0L, active1, 65536L);
            }
            case 'E': 
            case 'e': {
                if ((active0 & 0x8000000L) != 0L) {
                    return this.jjStopAtPos(5, 27);
                }
                if ((active0 & 0x10000000L) != 0L) {
                    return this.jjStopAtPos(5, 28);
                }
                if ((active1 & 0x400L) == 0L) break;
                return this.jjStopAtPos(5, 74);
            }
            case 'F': 
            case 'f': {
                return this.jjMoveStringLiteralDfa6_0(active0, 0L, active1, 256L);
            }
            case 'G': 
            case 'g': {
                if ((active0 & 0x2000000000000000L) != 0L) {
                    return this.jjStopAtPos(5, 61);
                }
                if ((active1 & 0x800000000L) != 0L) {
                    return this.jjStopAtPos(5, 99);
                }
                return this.jjMoveStringLiteralDfa6_0(active0, 0x4000000000000000L, active1, 2L);
            }
            case 'I': 
            case 'i': {
                return this.jjMoveStringLiteralDfa6_0(active0, 0L, active1, 24592L);
            }
            case 'L': 
            case 'l': {
                if ((active0 & 0x4000000L) == 0L) break;
                return this.jjStopAtPos(5, 26);
            }
            case 'M': 
            case 'm': {
                return this.jjMoveStringLiteralDfa6_0(active0, 0L, active1, 192L);
            }
            case 'N': 
            case 'n': {
                if ((active0 & 0x20000000000000L) != 0L) {
                    return this.jjStopAtPos(5, 53);
                }
                if ((active0 & 0x200000000000000L) != 0L) {
                    return this.jjStopAtPos(5, 57);
                }
                return this.jjMoveStringLiteralDfa6_0(active0, 0x40001000000L, active1, 0L);
            }
            case 'O': 
            case 'o': {
                return this.jjMoveStringLiteralDfa6_0(active0, Long.MIN_VALUE, active1, 1L);
            }
            case 'R': 
            case 'r': {
                return this.jjMoveStringLiteralDfa6_0(active0, 0L, active1, 524288L);
            }
            case 'T': 
            case 't': {
                return this.jjMoveStringLiteralDfa6_0(active0, 0L, active1, 0x100000L);
            }
        }
        return this.jjStartNfa_0(4, active0, active1);
    }

    private final int jjMoveStringLiteralDfa6_0(long old0, long active0, long old1, long active1) {
        if (((active0 &= old0) | (active1 &= old1)) == 0L) {
            return this.jjStartNfa_0(4, old0, old1);
        }
        try {
            this.curChar = this.input_stream.readChar();
        }
        catch (IOException e) {
            this.jjStopStringLiteralDfa_0(5, active0, active1);
            return 6;
        }
        switch (this.curChar) {
            case 'A': 
            case 'a': {
                return this.jjMoveStringLiteralDfa7_0(active0, 0x1000000L, active1, 0L);
            }
            case 'B': 
            case 'b': {
                return this.jjMoveStringLiteralDfa7_0(active0, 0L, active1, 128L);
            }
            case 'E': 
            case 'e': {
                if ((active1 & 0x40L) != 0L) {
                    return this.jjStopAtPos(6, 70);
                }
                if ((active1 & 0x40000000L) != 0L) {
                    return this.jjStopAtPos(6, 94);
                }
                return this.jjMoveStringLiteralDfa7_0(active0, 0x20000000L, active1, 0L);
            }
            case 'F': 
            case 'f': {
                return this.jjMoveStringLiteralDfa7_0(active0, 0L, active1, 2L);
            }
            case 'G': 
            case 'g': {
                if ((active0 & 0x40000000000L) == 0L) break;
                return this.jjStopAtPos(6, 42);
            }
            case 'I': 
            case 'i': {
                return this.jjMoveStringLiteralDfa7_0(active0, 0L, active1, 524288L);
            }
            case 'M': 
            case 'm': {
                return this.jjMoveStringLiteralDfa7_0(active0, 0L, active1, 16L);
            }
            case 'P': 
            case 'p': {
                if ((active1 & 0x200000000L) == 0L) break;
                return this.jjStopAtPos(6, 97);
            }
            case 'R': 
            case 'r': {
                return this.jjMoveStringLiteralDfa7_0(active0, Long.MIN_VALUE, active1, 0x100000L);
            }
            case 'S': 
            case 's': {
                return this.jjMoveStringLiteralDfa7_0(active0, 0x4000000000000000L, active1, 65536L);
            }
            case 'T': 
            case 't': {
                return this.jjMoveStringLiteralDfa7_0(active0, 0x10000000000000L, active1, 256L);
            }
            case 'U': 
            case 'u': {
                return this.jjMoveStringLiteralDfa7_0(active0, 0L, active1, 1L);
            }
            case 'V': 
            case 'v': {
                return this.jjMoveStringLiteralDfa7_0(active0, 0L, active1, 24576L);
            }
        }
        return this.jjStartNfa_0(5, active0, active1);
    }

    private final int jjMoveStringLiteralDfa7_0(long old0, long active0, long old1, long active1) {
        if (((active0 &= old0) | (active1 &= old1)) == 0L) {
            return this.jjStartNfa_0(5, old0, old1);
        }
        try {
            this.curChar = this.input_stream.readChar();
        }
        catch (IOException e) {
            this.jjStopStringLiteralDfa_0(6, active0, active1);
            return 7;
        }
        switch (this.curChar) {
            case 'A': 
            case 'a': {
                return this.jjMoveStringLiteralDfa8_0(active0, 0L, active1, 65536L);
            }
            case 'E': 
            case 'e': {
                if ((active0 & 0x10000000000000L) != 0L) {
                    return this.jjStopAtPos(7, 52);
                }
                if ((active1 & 0x10L) != 0L) {
                    return this.jjStopAtPos(7, 68);
                }
                return this.jjMoveStringLiteralDfa8_0(active0, 0L, active1, 24960L);
            }
            case 'G': 
            case 'g': {
                return this.jjMoveStringLiteralDfa8_0(active0, Long.MIN_VALUE, active1, 0L);
            }
            case 'I': 
            case 'i': {
                return this.jjMoveStringLiteralDfa8_0(active0, 0x4000000000000000L, active1, 0x100002L);
            }
            case 'L': 
            case 'l': {
                if ((active0 & 0x1000000L) == 0L) break;
                return this.jjStopAtPos(7, 24);
            }
            case 'M': 
            case 'm': {
                if ((active1 & 0x80000L) == 0L) break;
                return this.jjStopAtPos(7, 83);
            }
            case 'N': 
            case 'n': {
                return this.jjMoveStringLiteralDfa8_0(active0, 0L, active1, 1L);
            }
            case 'R': 
            case 'r': {
                if ((active0 & 0x20000000L) == 0L) break;
                return this.jjStopAtPos(7, 29);
            }
        }
        return this.jjStartNfa_0(6, active0, active1);
    }

    private final int jjMoveStringLiteralDfa8_0(long old0, long active0, long old1, long active1) {
        if (((active0 &= old0) | (active1 &= old1)) == 0L) {
            return this.jjStartNfa_0(6, old0, old1);
        }
        try {
            this.curChar = this.input_stream.readChar();
        }
        catch (IOException e) {
            this.jjStopStringLiteralDfa_0(7, active0, active1);
            return 8;
        }
        switch (this.curChar) {
            case 'D': 
            case 'd': {
                if ((active1 & 1L) != 0L) {
                    return this.jjStopAtPos(8, 64);
                }
                return this.jjMoveStringLiteralDfa9_0(active0, 0L, active1, 8192L);
            }
            case 'F': 
            case 'f': {
                return this.jjMoveStringLiteralDfa9_0(active0, 0L, active1, 65536L);
            }
            case 'G': 
            case 'g': {
                return this.jjMoveStringLiteralDfa9_0(active0, 0x4000000000000000L, active1, 2L);
            }
            case 'M': 
            case 'm': {
                if ((active1 & 0x100000L) == 0L) break;
                return this.jjStopAtPos(8, 84);
            }
            case 'R': 
            case 'r': {
                if ((active1 & 0x80L) != 0L) {
                    return this.jjStopAtPos(8, 71);
                }
                if ((active1 & 0x100L) == 0L) break;
                return this.jjStopAtPos(8, 72);
            }
            case 'S': 
            case 's': {
                return this.jjMoveStringLiteralDfa9_0(active0, Long.MIN_VALUE, active1, 0L);
            }
            case 'T': 
            case 't': {
                return this.jjMoveStringLiteralDfa9_0(active0, 0L, active1, 16384L);
            }
        }
        return this.jjStartNfa_0(7, active0, active1);
    }

    private final int jjMoveStringLiteralDfa9_0(long old0, long active0, long old1, long active1) {
        if (((active0 &= old0) | (active1 &= old1)) == 0L) {
            return this.jjStartNfa_0(7, old0, old1);
        }
        try {
            this.curChar = this.input_stream.readChar();
        }
        catch (IOException e) {
            this.jjStopStringLiteralDfa_0(8, active0, active1);
            return 9;
        }
        switch (this.curChar) {
            case 'A': 
            case 'a': {
                return this.jjMoveStringLiteralDfa10_0(active0, 0L, active1, 8192L);
            }
            case 'D': 
            case 'd': {
                return this.jjMoveStringLiteralDfa10_0(active0, 0L, active1, 2L);
            }
            case 'F': 
            case 'f': {
                return this.jjMoveStringLiteralDfa10_0(active0, 0x4000000000000000L, active1, 0L);
            }
            case 'I': 
            case 'i': {
                return this.jjMoveStringLiteralDfa10_0(active0, Long.MIN_VALUE, active1, 16384L);
            }
            case 'T': 
            case 't': {
                return this.jjMoveStringLiteralDfa10_0(active0, 0L, active1, 65536L);
            }
        }
        return this.jjStartNfa_0(8, active0, active1);
    }

    private final int jjMoveStringLiteralDfa10_0(long old0, long active0, long old1, long active1) {
        if (((active0 &= old0) | (active1 &= old1)) == 0L) {
            return this.jjStartNfa_0(8, old0, old1);
        }
        try {
            this.curChar = this.input_stream.readChar();
        }
        catch (IOException e) {
            this.jjStopStringLiteralDfa_0(9, active0, active1);
            return 10;
        }
        switch (this.curChar) {
            case 'E': 
            case 'e': {
                return this.jjMoveStringLiteralDfa11_0(active0, 0L, active1, 65536L);
            }
            case 'G': 
            case 'g': {
                return this.jjMoveStringLiteralDfa11_0(active0, Long.MIN_VALUE, active1, 0L);
            }
            case 'I': 
            case 'i': {
                return this.jjMoveStringLiteralDfa11_0(active0, 0x4000000000000000L, active1, 0L);
            }
            case 'M': 
            case 'm': {
                return this.jjMoveStringLiteralDfa11_0(active0, 0L, active1, 16384L);
            }
            case 'P': 
            case 'p': {
                if ((active1 & 2L) == 0L) break;
                return this.jjStopAtPos(10, 65);
            }
            case 'T': 
            case 't': {
                return this.jjMoveStringLiteralDfa11_0(active0, 0L, active1, 8192L);
            }
        }
        return this.jjStartNfa_0(9, active0, active1);
    }

    private final int jjMoveStringLiteralDfa11_0(long old0, long active0, long old1, long active1) {
        if (((active0 &= old0) | (active1 &= old1)) == 0L) {
            return this.jjStartNfa_0(9, old0, old1);
        }
        try {
            this.curChar = this.input_stream.readChar();
        }
        catch (IOException e) {
            this.jjStopStringLiteralDfa_0(10, active0, active1);
            return 11;
        }
        switch (this.curChar) {
            case 'E': 
            case 'e': {
                if ((active1 & 0x2000L) != 0L) {
                    return this.jjStopAtPos(11, 77);
                }
                if ((active1 & 0x4000L) == 0L) break;
                return this.jjStopAtPos(11, 78);
            }
            case 'F': 
            case 'f': {
                return this.jjMoveStringLiteralDfa12_0(active0, Long.MIN_VALUE, active1, 0L);
            }
            case 'G': 
            case 'g': {
                if ((active0 & 0x4000000000000000L) == 0L) break;
                return this.jjStopAtPos(11, 62);
            }
            case 'R': 
            case 'r': {
                if ((active1 & 0x10000L) == 0L) break;
                return this.jjStopAtPos(11, 80);
            }
        }
        return this.jjStartNfa_0(10, active0, active1);
    }

    private final int jjMoveStringLiteralDfa12_0(long old0, long active0, long old1, long active1) {
        if (((active0 &= old0) | (active1 &= old1)) == 0L) {
            return this.jjStartNfa_0(10, old0, old1);
        }
        try {
            this.curChar = this.input_stream.readChar();
        }
        catch (IOException e) {
            this.jjStopStringLiteralDfa_0(11, active0, 0L);
            return 12;
        }
        switch (this.curChar) {
            case 'I': 
            case 'i': {
                return this.jjMoveStringLiteralDfa13_0(active0, Long.MIN_VALUE);
            }
        }
        return this.jjStartNfa_0(11, active0, 0L);
    }

    private final int jjMoveStringLiteralDfa13_0(long old0, long active0) {
        if ((active0 &= old0) == 0L) {
            return this.jjStartNfa_0(11, old0, 0L);
        }
        try {
            this.curChar = this.input_stream.readChar();
        }
        catch (IOException e) {
            this.jjStopStringLiteralDfa_0(12, active0, 0L);
            return 13;
        }
        switch (this.curChar) {
            case 'G': 
            case 'g': {
                if ((active0 & Long.MIN_VALUE) == 0L) break;
                return this.jjStopAtPos(13, 63);
            }
        }
        return this.jjStartNfa_0(12, active0, 0L);
    }

    private final void jjCheckNAdd(int state) {
        if (this.jjrounds[state] != this.jjround) {
            this.jjstateSet[this.jjnewStateCnt++] = state;
            this.jjrounds[state] = this.jjround;
        }
    }

    private final void jjAddStates(int start, int end) {
        do {
            this.jjstateSet[this.jjnewStateCnt++] = jjnextStates[start];
        } while (start++ != end);
    }

    private final void jjCheckNAddTwoStates(int state1, int state2) {
        this.jjCheckNAdd(state1);
        this.jjCheckNAdd(state2);
    }

    private final void jjCheckNAddStates(int start, int end) {
        do {
            this.jjCheckNAdd(jjnextStates[start]);
        } while (start++ != end);
    }

    private final void jjCheckNAddStates(int start) {
        this.jjCheckNAdd(jjnextStates[start]);
        this.jjCheckNAdd(jjnextStates[start + 1]);
    }

    private final int jjMoveNfa_0(int startState, int curPos) {
        int startsAt = 0;
        this.jjnewStateCnt = 74;
        int i = 1;
        this.jjstateSet[0] = startState;
        int kind = Integer.MAX_VALUE;
        while (true) {
            if (++this.jjround == Integer.MAX_VALUE) {
                this.ReInitRounds();
            }
            if (this.curChar < '@') {
                long l = 1L << this.curChar;
                block87: do {
                    switch (this.jjstateSet[--i]) {
                        case 0: {
                            if ((0x3FF000000000000L & l) != 0L) {
                                if (kind > 4) {
                                    kind = 4;
                                }
                                this.jjCheckNAddStates(0, 7);
                            } else if (this.curChar == '<') {
                                this.jjAddStates(8, 9);
                            } else if (this.curChar == '>') {
                                this.jjstateSet[this.jjnewStateCnt++] = 47;
                            } else if (this.curChar == '!') {
                                this.jjstateSet[this.jjnewStateCnt++] = 41;
                            } else if (this.curChar == '=') {
                                if (kind > 12) {
                                    kind = 12;
                                }
                            } else if (this.curChar == '&') {
                                this.jjstateSet[this.jjnewStateCnt++] = 27;
                            } else if (this.curChar == '\"') {
                                this.jjCheckNAddStates(10, 12);
                            } else if (this.curChar == '.') {
                                this.jjCheckNAdd(1);
                            }
                            if (this.curChar == '>') {
                                if (kind <= 15) break;
                                kind = 15;
                                break;
                            }
                            if (this.curChar == '<') {
                                if (kind <= 14) break;
                                kind = 14;
                                break;
                            }
                            if (this.curChar != '=') break;
                            this.jjstateSet[this.jjnewStateCnt++] = 36;
                            break;
                        }
                        case 1: {
                            if ((0x3FF000000000000L & l) == 0L) continue block87;
                            if (kind > 4) {
                                kind = 4;
                            }
                            this.jjCheckNAddStates(13, 15);
                            break;
                        }
                        case 3: {
                            if ((0x280000000000L & l) == 0L) break;
                            this.jjCheckNAdd(4);
                            break;
                        }
                        case 4: {
                            if ((0x3FF000000000000L & l) == 0L) continue block87;
                            if (kind > 4) {
                                kind = 4;
                            }
                            this.jjCheckNAddTwoStates(4, 5);
                            break;
                        }
                        case 6: {
                            if (this.curChar != '\"') break;
                            this.jjCheckNAddStates(10, 12);
                            break;
                        }
                        case 7: {
                            if ((0xFFFFFFFBFFFFDBFFL & l) == 0L) break;
                            this.jjCheckNAddStates(10, 12);
                            break;
                        }
                        case 9: {
                            if ((0x8400000000L & l) == 0L) break;
                            this.jjCheckNAddStates(10, 12);
                            break;
                        }
                        case 10: {
                            if (this.curChar != '\"' || kind <= 7) continue block87;
                            kind = 7;
                            break;
                        }
                        case 11: {
                            if ((0xFF000000000000L & l) == 0L) break;
                            this.jjCheckNAddStates(16, 19);
                            break;
                        }
                        case 12: {
                            if ((0xFF000000000000L & l) == 0L) break;
                            this.jjCheckNAddStates(10, 12);
                            break;
                        }
                        case 13: {
                            if ((0xF000000000000L & l) == 0L) break;
                            this.jjstateSet[this.jjnewStateCnt++] = 14;
                            break;
                        }
                        case 14: {
                            if ((0xFF000000000000L & l) == 0L) break;
                            this.jjCheckNAdd(12);
                            break;
                        }
                        case 16: {
                            if ((0x3FF000000000000L & l) == 0L) break;
                            this.jjAddStates(20, 21);
                            break;
                        }
                        case 27: {
                            if (this.curChar != '&' || kind <= 10) continue block87;
                            kind = 10;
                            break;
                        }
                        case 28: {
                            if (this.curChar != '&') break;
                            this.jjstateSet[this.jjnewStateCnt++] = 27;
                            break;
                        }
                        case 36: {
                            if (this.curChar != '=' || kind <= 12) continue block87;
                            kind = 12;
                            break;
                        }
                        case 37: {
                            if (this.curChar != '=') break;
                            this.jjstateSet[this.jjnewStateCnt++] = 36;
                            break;
                        }
                        case 38: {
                            if (this.curChar != '=' || kind <= 12) continue block87;
                            kind = 12;
                            break;
                        }
                        case 41: {
                            if (this.curChar != '=' || kind <= 13) continue block87;
                            kind = 13;
                            break;
                        }
                        case 42: {
                            if (this.curChar != '!') break;
                            this.jjstateSet[this.jjnewStateCnt++] = 41;
                            break;
                        }
                        case 45: {
                            if (this.curChar != '<' || kind <= 14) continue block87;
                            kind = 14;
                            break;
                        }
                        case 46: {
                            if (this.curChar != '>' || kind <= 15) continue block87;
                            kind = 15;
                            break;
                        }
                        case 47: {
                            if (this.curChar != '=' || kind <= 17) continue block87;
                            kind = 17;
                            break;
                        }
                        case 48: {
                            if (this.curChar != '>') break;
                            this.jjstateSet[this.jjnewStateCnt++] = 47;
                            break;
                        }
                        case 49: {
                            if ((0x3FF000000000000L & l) == 0L) continue block87;
                            if (kind > 4) {
                                kind = 4;
                            }
                            this.jjCheckNAddStates(0, 7);
                            break;
                        }
                        case 50: {
                            if ((0x3FF000000000000L & l) == 0L) continue block87;
                            if (kind > 4) {
                                kind = 4;
                            }
                            this.jjCheckNAdd(50);
                            break;
                        }
                        case 51: {
                            if ((0x3FF000000000000L & l) == 0L) break;
                            this.jjCheckNAddStates(22, 24);
                            break;
                        }
                        case 53: {
                            if ((0x280000000000L & l) == 0L) break;
                            this.jjCheckNAdd(54);
                            break;
                        }
                        case 54: {
                            if ((0x3FF000000000000L & l) == 0L) break;
                            this.jjCheckNAddTwoStates(54, 5);
                            break;
                        }
                        case 55: {
                            if ((0x3FF000000000000L & l) == 0L) break;
                            this.jjCheckNAddTwoStates(55, 56);
                            break;
                        }
                        case 57: {
                            if ((0x280000000000L & l) == 0L) break;
                            this.jjCheckNAdd(58);
                            break;
                        }
                        case 58: {
                            if ((0x3FF000000000000L & l) == 0L) continue block87;
                            if (kind > 4) {
                                kind = 4;
                            }
                            this.jjCheckNAddTwoStates(58, 5);
                            break;
                        }
                        case 59: {
                            if ((0x3FF000000000000L & l) == 0L) break;
                            this.jjCheckNAddTwoStates(59, 60);
                            break;
                        }
                        case 60: {
                            if (this.curChar != '.') continue block87;
                            if (kind > 4) {
                                kind = 4;
                            }
                            this.jjCheckNAddStates(25, 27);
                            break;
                        }
                        case 61: {
                            if ((0x3FF000000000000L & l) == 0L) continue block87;
                            if (kind > 4) {
                                kind = 4;
                            }
                            this.jjCheckNAddStates(25, 27);
                            break;
                        }
                        case 63: {
                            if ((0x280000000000L & l) == 0L) break;
                            this.jjCheckNAdd(64);
                            break;
                        }
                        case 64: {
                            if ((0x3FF000000000000L & l) == 0L) continue block87;
                            if (kind > 4) {
                                kind = 4;
                            }
                            this.jjCheckNAddTwoStates(64, 5);
                            break;
                        }
                        case 65: {
                            if (this.curChar != '<') break;
                            this.jjAddStates(8, 9);
                            break;
                        }
                        case 66: {
                            if (this.curChar != '>' || kind <= 13) continue block87;
                            kind = 13;
                            break;
                        }
                        case 67: {
                            if (this.curChar != '=' || kind <= 16) continue block87;
                            kind = 16;
                            break;
                        }
                    }
                } while (i != startsAt);
            } else if (this.curChar < '\u0080') {
                long l = 1L << (this.curChar & 0x3F);
                block88: do {
                    switch (this.jjstateSet[--i]) {
                        case 0: {
                            if ((0x8000000080L & l) != 0L) {
                                this.jjAddStates(28, 29);
                                break;
                            }
                            if ((0x100000001000L & l) != 0L) {
                                this.jjAddStates(30, 31);
                                break;
                            }
                            if ((0x400000004000L & l) != 0L) {
                                this.jjstateSet[this.jjnewStateCnt++] = 43;
                                break;
                            }
                            if ((0x2000000020L & l) != 0L) {
                                this.jjstateSet[this.jjnewStateCnt++] = 39;
                                break;
                            }
                            if ((0x800000008000L & l) != 0L) {
                                this.jjstateSet[this.jjnewStateCnt++] = 34;
                                break;
                            }
                            if ((0x200000002L & l) != 0L) {
                                this.jjstateSet[this.jjnewStateCnt++] = 30;
                                break;
                            }
                            if ((0x4000000040L & l) != 0L) {
                                this.jjstateSet[this.jjnewStateCnt++] = 25;
                                break;
                            }
                            if ((0x10000000100000L & l) != 0L) {
                                this.jjstateSet[this.jjnewStateCnt++] = 21;
                                break;
                            }
                            if (this.curChar == '|') {
                                this.jjstateSet[this.jjnewStateCnt++] = 32;
                                break;
                            }
                            if (this.curChar != '[') break;
                            this.jjstateSet[this.jjnewStateCnt++] = 15;
                            break;
                        }
                        case 69: {
                            if ((0x2000000020L & l) != 0L) {
                                if (kind <= 16) break;
                                kind = 16;
                                break;
                            }
                            if ((0x10000000100000L & l) == 0L || kind <= 14) continue block88;
                            kind = 14;
                            break;
                        }
                        case 2: {
                            if ((0x2000000020L & l) == 0L) break;
                            this.jjAddStates(32, 33);
                            break;
                        }
                        case 5: {
                            if ((0x5000000050L & l) == 0L || kind <= 4) continue block88;
                            kind = 4;
                            break;
                        }
                        case 7: {
                            if ((0xFFFFFFFFEFFFFFFFL & l) == 0L) break;
                            this.jjCheckNAddStates(10, 12);
                            break;
                        }
                        case 8: {
                            if (this.curChar != '\\') break;
                            this.jjAddStates(34, 36);
                            break;
                        }
                        case 9: {
                            if ((0x14404410144044L & l) == 0L) break;
                            this.jjCheckNAddStates(10, 12);
                            break;
                        }
                        case 15: {
                            if ((0x1000000010000L & l) == 0L) break;
                            this.jjAddStates(20, 21);
                            break;
                        }
                        case 17: {
                            if (this.curChar != ']' || kind <= 8) continue block88;
                            kind = 8;
                            break;
                        }
                        case 18: {
                            if (this.curChar != '[') break;
                            this.jjstateSet[this.jjnewStateCnt++] = 15;
                            break;
                        }
                        case 19: {
                            if ((0x2000000020L & l) == 0L || kind <= 9) continue block88;
                            kind = 9;
                            break;
                        }
                        case 20: {
                            if ((0x20000000200000L & l) == 0L) break;
                            this.jjCheckNAdd(19);
                            break;
                        }
                        case 21: {
                            if ((0x4000000040000L & l) == 0L) break;
                            this.jjstateSet[this.jjnewStateCnt++] = 20;
                            break;
                        }
                        case 22: {
                            if ((0x10000000100000L & l) == 0L) break;
                            this.jjstateSet[this.jjnewStateCnt++] = 21;
                            break;
                        }
                        case 23: {
                            if ((0x8000000080000L & l) == 0L) break;
                            this.jjCheckNAdd(19);
                            break;
                        }
                        case 24: {
                            if ((0x100000001000L & l) == 0L) break;
                            this.jjstateSet[this.jjnewStateCnt++] = 23;
                            break;
                        }
                        case 25: {
                            if ((0x200000002L & l) == 0L) break;
                            this.jjstateSet[this.jjnewStateCnt++] = 24;
                            break;
                        }
                        case 26: {
                            if ((0x4000000040L & l) == 0L) break;
                            this.jjstateSet[this.jjnewStateCnt++] = 25;
                            break;
                        }
                        case 29: {
                            if ((0x1000000010L & l) == 0L || kind <= 10) continue block88;
                            kind = 10;
                            break;
                        }
                        case 30: {
                            if ((0x400000004000L & l) == 0L) break;
                            this.jjstateSet[this.jjnewStateCnt++] = 29;
                            break;
                        }
                        case 31: {
                            if ((0x200000002L & l) == 0L) break;
                            this.jjstateSet[this.jjnewStateCnt++] = 30;
                            break;
                        }
                        case 32: {
                            if (this.curChar != '|' || kind <= 11) continue block88;
                            kind = 11;
                            break;
                        }
                        case 33: {
                            if (this.curChar != '|') break;
                            this.jjstateSet[this.jjnewStateCnt++] = 32;
                            break;
                        }
                        case 34: {
                            if ((0x4000000040000L & l) == 0L || kind <= 11) continue block88;
                            kind = 11;
                            break;
                        }
                        case 35: {
                            if ((0x800000008000L & l) == 0L) break;
                            this.jjstateSet[this.jjnewStateCnt++] = 34;
                            break;
                        }
                        case 39: {
                            if ((0x2000000020000L & l) == 0L || kind <= 12) continue block88;
                            kind = 12;
                            break;
                        }
                        case 40: {
                            if ((0x2000000020L & l) == 0L) break;
                            this.jjstateSet[this.jjnewStateCnt++] = 39;
                            break;
                        }
                        case 43: {
                            if ((0x2000000020L & l) == 0L || kind <= 13) continue block88;
                            kind = 13;
                            break;
                        }
                        case 44: {
                            if ((0x400000004000L & l) == 0L) break;
                            this.jjstateSet[this.jjnewStateCnt++] = 43;
                            break;
                        }
                        case 52: {
                            if ((0x2000000020L & l) == 0L) break;
                            this.jjAddStates(37, 38);
                            break;
                        }
                        case 56: {
                            if ((0x2000000020L & l) == 0L) break;
                            this.jjAddStates(39, 40);
                            break;
                        }
                        case 62: {
                            if ((0x2000000020L & l) == 0L) break;
                            this.jjAddStates(41, 42);
                            break;
                        }
                        case 68: {
                            if ((0x100000001000L & l) == 0L) break;
                            this.jjAddStates(30, 31);
                            break;
                        }
                        case 70: {
                            if ((0x2000000020L & l) == 0L || kind <= 16) continue block88;
                            kind = 16;
                            break;
                        }
                        case 71: {
                            if ((0x8000000080L & l) == 0L) break;
                            this.jjAddStates(28, 29);
                            break;
                        }
                        case 72: {
                            if ((0x10000000100000L & l) == 0L || kind <= 15) continue block88;
                            kind = 15;
                            break;
                        }
                        case 73: {
                            if ((0x2000000020L & l) == 0L || kind <= 17) continue block88;
                            kind = 17;
                            break;
                        }
                    }
                } while (i != startsAt);
            } else {
                int i2 = (this.curChar & 0xFF) >> 6;
                long l2 = 1L << (this.curChar & 0x3F);
                do {
                    switch (this.jjstateSet[--i]) {
                        case 7: {
                            if ((jjbitVec0[i2] & l2) == 0L) break;
                            this.jjAddStates(10, 12);
                            break;
                        }
                    }
                } while (i != startsAt);
            }
            if (kind != Integer.MAX_VALUE) {
                this.jjmatchedKind = kind;
                this.jjmatchedPos = curPos;
                kind = Integer.MAX_VALUE;
            }
            ++curPos;
            i = this.jjnewStateCnt;
            this.jjnewStateCnt = startsAt;
            if (i == (startsAt = 74 - this.jjnewStateCnt)) {
                return curPos;
            }
            try {
                this.curChar = this.input_stream.readChar();
            }
            catch (IOException e) {
                return curPos;
            }
        }
    }

    public ExpressionEvaluatorTokenManager(SimpleCharStream stream) {
        this.input_stream = stream;
    }

    public ExpressionEvaluatorTokenManager(SimpleCharStream stream, int lexState) {
        this(stream);
        this.SwitchTo(lexState);
    }

    public void ReInit(SimpleCharStream stream) {
        this.jjnewStateCnt = 0;
        this.jjmatchedPos = 0;
        this.curLexState = this.defaultLexState;
        this.input_stream = stream;
        this.ReInitRounds();
    }

    private final void ReInitRounds() {
        this.jjround = -2147483647;
        int i = 74;
        while (i-- > 0) {
            this.jjrounds[i] = Integer.MIN_VALUE;
        }
    }

    public void ReInit(SimpleCharStream stream, int lexState) {
        this.ReInit(stream);
        this.SwitchTo(lexState);
    }

    public void SwitchTo(int lexState) {
        if (lexState >= 1 || lexState < 0) {
            throw new TokenMgrError("Error: Ignoring invalid lexical state : " + lexState + ". State unchanged.", 2);
        }
        this.curLexState = lexState;
    }

    protected Token jjFillToken() {
        Token t = Token.newToken(this.jjmatchedKind);
        t.kind = this.jjmatchedKind;
        String im = jjstrLiteralImages[this.jjmatchedKind];
        t.image = im == null ? this.input_stream.GetImage() : im;
        t.beginLine = this.input_stream.getBeginLine();
        t.beginColumn = this.input_stream.getBeginColumn();
        t.endLine = this.input_stream.getEndLine();
        t.endColumn = this.input_stream.getEndColumn();
        return t;
    }

    public Token getNextToken() {
        int curPos;
        block11: {
            Object specialToken = null;
            curPos = 0;
            while (true) {
                try {
                    this.curChar = this.input_stream.BeginToken();
                }
                catch (IOException e) {
                    this.jjmatchedKind = 0;
                    Token matchedToken = this.jjFillToken();
                    return matchedToken;
                }
                try {
                    this.input_stream.backup(0);
                    while (this.curChar <= ' ' && (0x100002200L & 1L << this.curChar) != 0L) {
                        this.curChar = this.input_stream.BeginToken();
                    }
                }
                catch (IOException e1) {
                    continue;
                }
                this.jjmatchedKind = Integer.MAX_VALUE;
                this.jjmatchedPos = 0;
                curPos = this.jjMoveStringLiteralDfa0_0();
                if (this.jjmatchedKind == Integer.MAX_VALUE) break block11;
                if (this.jjmatchedPos + 1 < curPos) {
                    this.input_stream.backup(curPos - this.jjmatchedPos - 1);
                }
                if ((jjtoToken[this.jjmatchedKind >> 6] & 1L << (this.jjmatchedKind & 0x3F)) != 0L) break;
            }
            Token matchedToken = this.jjFillToken();
            return matchedToken;
        }
        int error_line = this.input_stream.getEndLine();
        int error_column = this.input_stream.getEndColumn();
        String error_after = null;
        boolean EOFSeen = false;
        try {
            this.input_stream.readChar();
            this.input_stream.backup(1);
        }
        catch (IOException e1) {
            EOFSeen = true;
            String string = error_after = curPos <= 1 ? "" : this.input_stream.GetImage();
            if (this.curChar == '\n' || this.curChar == '\r') {
                ++error_line;
                error_column = 0;
            }
            ++error_column;
        }
        if (!EOFSeen) {
            this.input_stream.backup(1);
            error_after = curPos <= 1 ? "" : this.input_stream.GetImage();
        }
        throw new TokenMgrError(EOFSeen, this.curLexState, error_line, error_column, error_after, this.curChar, 0);
    }
}

