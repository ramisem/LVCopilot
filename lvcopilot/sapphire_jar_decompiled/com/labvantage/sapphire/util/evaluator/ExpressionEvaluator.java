/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.util.evaluator;

import com.labvantage.sapphire.util.evaluator.CalcFunctions;
import com.labvantage.sapphire.util.evaluator.DateFunctions;
import com.labvantage.sapphire.util.evaluator.ExpressionEvaluatorConstants;
import com.labvantage.sapphire.util.evaluator.ExpressionEvaluatorTokenManager;
import com.labvantage.sapphire.util.evaluator.ExpressionParam;
import com.labvantage.sapphire.util.evaluator.IndustryFunctions;
import com.labvantage.sapphire.util.evaluator.ParseException;
import com.labvantage.sapphire.util.evaluator.SimpleCharStream;
import com.labvantage.sapphire.util.evaluator.StringFunctions;
import com.labvantage.sapphire.util.evaluator.Token;
import com.labvantage.sapphire.util.format.DateFormatter;
import com.labvantage.sapphire.util.format.NumericFormatter;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.MathContext;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;
import java.util.Vector;
import sapphire.util.StringUtil;

public class ExpressionEvaluator
implements ExpressionEvaluatorConstants {
    HashMap params = new HashMap();
    PrintStream ps = null;
    boolean lookaheadFlag;
    boolean isBooleanInside;
    boolean initialStage;
    boolean rtypeString = false;
    boolean errorFlag = false;
    boolean errorInIF = false;
    boolean nullInArithmetic = false;
    String errorMessage = "";
    boolean honorNullInRelationalExpressions = false;
    boolean honorNullInANDORConditionals = false;
    int noOfBooleanFragment = 0;
    String objType = null;
    public ExpressionEvaluatorTokenManager token_source;
    SimpleCharStream jj_input_stream;
    public Token token;
    public Token jj_nt;
    private int jj_ntk;
    private Token jj_scanpos;
    private Token jj_lastpos;
    private int jj_la;
    public boolean lookingAhead = false;
    private boolean jj_semLA;
    private int jj_gen;
    private final int[] jj_la1 = new int[57];
    private static int[] jj_la1_0;
    private static int[] jj_la1_1;
    private static int[] jj_la1_2;
    private static int[] jj_la1_3;
    private final JJCalls[] jj_2_rtns = new JJCalls[10];
    private boolean jj_rescan = false;
    private int jj_gc = 0;
    private final LookaheadSuccess jj_ls = new LookaheadSuccess();
    private Vector jj_expentries = new Vector();
    private int[] jj_expentry;
    private int jj_kind = -1;
    private int[] jj_lasttokens = new int[100];
    private int jj_endpos;

    void setError(String message) throws ParseException {
        if (!this.errorFlag) {
            this.errorMessage = message;
        }
        this.errorFlag = true;
    }

    void setHonorNullInRelationalExpressions(boolean honorNullInRelationalExpressions) throws ParseException {
        this.honorNullInRelationalExpressions = honorNullInRelationalExpressions;
    }

    void setHonorNullInANDORConditionals(boolean honorNullInANDORConditionals) throws ParseException {
        this.honorNullInANDORConditionals = honorNullInANDORConditionals;
    }

    boolean lookaheadConditionalOperator() throws ParseException {
        int ifTokenCount = 0;
        int conditionalTokenCount = 0;
        int booleanTokenCount = 0;
        boolean ifFound = false;
        int tk = 1;
        while (this.getToken((int)tk).kind != 0) {
            if (this.getToken((int)tk).kind == 98 || this.getToken((int)tk).kind == 10 || this.getToken((int)tk).kind == 11) {
                ++ifTokenCount;
                ifFound = true;
            } else {
                if (!this.initialStage && (this.getToken((int)tk).image.equals(",") || this.getToken((int)tk).image.equals(")"))) break;
                if (this.getToken((int)tk).kind == 12 || this.getToken((int)tk).kind == 13 || this.getToken((int)tk).kind == 14 || this.getToken((int)tk).kind == 15 || this.getToken((int)tk).kind == 16 || this.getToken((int)tk).kind == 17) {
                    ++conditionalTokenCount;
                } else if (this.getToken((int)tk).kind == 26 || this.getToken((int)tk).kind == 27 || this.getToken((int)tk).kind == 29 || this.getToken((int)tk).kind == 28 || this.getToken((int)tk).kind == 9) {
                    ++booleanTokenCount;
                }
            }
            ++tk;
        }
        if (ifFound && ifTokenCount < conditionalTokenCount + booleanTokenCount) {
            return true;
        }
        return !ifFound && conditionalTokenCount > 0;
    }

    boolean isBooleanInside() throws ParseException {
        int ifTokenCount = 0;
        int parCount = 0;
        int conditionalTokenCount = 0;
        int booleanTokenCount = 0;
        boolean ifFound = false;
        this.noOfBooleanFragment = 0;
        if (!this.getToken((int)1).image.equals("(")) {
            return false;
        }
        ++parCount;
        int tk = 2;
        while (!(this.getToken((int)tk).kind == 0 || this.getToken((int)tk).image.equals(",") && parCount == 0)) {
            if (this.getToken((int)tk).image.equals("(")) {
                ++parCount;
            } else if (this.getToken((int)tk).image.equals(")")) {
                if (--parCount == 0) {
                    if (ifFound && ifTokenCount < conditionalTokenCount + booleanTokenCount) {
                        ++this.noOfBooleanFragment;
                    } else {
                        if (ifFound || conditionalTokenCount <= 0 && booleanTokenCount <= 0) break;
                        ++this.noOfBooleanFragment;
                    }
                }
            } else if (parCount != 0) {
                if (this.getToken((int)tk).kind == 98 || this.getToken((int)tk).kind == 10 || this.getToken((int)tk).kind == 11) {
                    ++ifTokenCount;
                    ifFound = true;
                } else if (this.getToken((int)tk).kind == 12 || this.getToken((int)tk).kind == 13 || this.getToken((int)tk).kind == 14 || this.getToken((int)tk).kind == 15 || this.getToken((int)tk).kind == 16 || this.getToken((int)tk).kind == 17) {
                    ++conditionalTokenCount;
                } else if (this.getToken((int)tk).kind == 26 || this.getToken((int)tk).kind == 27 || this.getToken((int)tk).kind == 29 || this.getToken((int)tk).kind == 28 || this.getToken((int)tk).kind == 9) {
                    ++booleanTokenCount;
                }
            }
            ++tk;
        }
        if (this.noOfBooleanFragment > 1) {
            return false;
        }
        if (ifFound && ifTokenCount < conditionalTokenCount + booleanTokenCount) {
            return true;
        }
        return !ifFound && (conditionalTokenCount > 0 || booleanTokenCount > 0);
    }

    public final String evaluate(String expr, HashMap params) throws ParseException {
        Set keys = params.keySet();
        HashMap newParams = new HashMap();
        int paramCount = 0;
        String[] tokens = StringUtil.getTokens(expr);
        for (String paramid : keys) {
            String newParamid = "p" + paramCount++;
            expr = StringUtil.replaceAll(expr, "[" + paramid + "]", "[" + newParamid + "]");
            newParams.put(newParamid, params.get(paramid));
        }
        for (int i = 0; i < tokens.length; ++i) {
            if (expr.indexOf(tokens[i]) < 0 || params.keySet().contains(tokens[i])) continue;
            String newParamid = "p" + paramCount++;
            expr = StringUtil.replaceAll(expr, "[" + tokens[i] + "]", "[" + newParamid + "]");
        }
        expr = StringUtil.replaceAll(expr, "\n", " ");
        expr = StringUtil.replaceAll(expr, "\r", " ");
        this.params = newParams;
        this.lookaheadFlag = false;
        this.isBooleanInside = false;
        this.initialStage = true;
        this.rtypeString = false;
        this.errorFlag = false;
        this.errorInIF = false;
        this.nullInArithmetic = false;
        this.errorMessage = "";
        Object a = null;
        boolean isNumber = false;
        this.ps = System.out;
        this.ReInit(new StringReader(expr));
        String s = this.Expression();
        try {
            if (this.getNextToken().kind == 0) {
                if (!this.rtypeString) {
                    try {
                        BigDecimal sigfigVal = new BigDecimal(NumericFormatter.formatNumber(new BigDecimal(s), "[sigfig;13]"));
                        s = sigfigVal.doubleValue() == 0.0 ? "0" : sigfigVal.stripTrailingZeros().toPlainString();
                    }
                    catch (Exception e) {
                        this.setError("Error while formatting the result: " + e.getMessage());
                    }
                }
                return s;
            }
            return "";
        }
        catch (Throwable e) {
            this.setError("Error in evaluate(): " + e.getMessage());
            return "";
        }
    }

    public final String Expression() throws ParseException {
        String s = "";
        Boolean bObj = null;
        try {
            this.lookaheadFlag = this.lookaheadConditionalOperator();
            if (this.initialStage) {
                this.initialStage = false;
            }
        }
        catch (Throwable e) {
            this.setError("Error in Expression(): " + e.getMessage());
            return "";
        }
        try {
            switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                case 0: {
                    this.jj_consume_token(0);
                    return "";
                }
            }
            this.jj_la1[1] = this.jj_gen;
            if (this.jj_2_3(1)) {
                if (!this.lookaheadFlag) {
                    switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                        case 4: 
                        case 7: 
                        case 8: 
                        case 19: 
                        case 20: 
                        case 21: 
                        case 22: 
                        case 23: 
                        case 24: 
                        case 25: 
                        case 31: 
                        case 36: 
                        case 38: 
                        case 39: 
                        case 40: 
                        case 41: 
                        case 42: 
                        case 43: 
                        case 44: 
                        case 45: 
                        case 46: 
                        case 47: 
                        case 48: 
                        case 49: 
                        case 50: 
                        case 51: 
                        case 52: 
                        case 53: 
                        case 54: 
                        case 55: 
                        case 56: 
                        case 57: 
                        case 58: 
                        case 59: 
                        case 60: 
                        case 61: 
                        case 62: 
                        case 63: 
                        case 64: 
                        case 65: 
                        case 66: 
                        case 67: 
                        case 68: 
                        case 69: 
                        case 70: 
                        case 71: 
                        case 72: 
                        case 73: 
                        case 74: 
                        case 75: 
                        case 76: 
                        case 77: 
                        case 78: 
                        case 79: 
                        case 80: 
                        case 81: 
                        case 82: 
                        case 83: 
                        case 84: 
                        case 85: 
                        case 86: 
                        case 87: 
                        case 88: 
                        case 89: 
                        case 90: 
                        case 91: 
                        case 92: 
                        case 93: 
                        case 94: 
                        case 95: 
                        case 96: 
                        case 97: 
                        case 98: 
                        case 99: 
                        case 100: {
                            s = this.StringExpression();
                            return s;
                        }
                    }
                    this.jj_la1[0] = this.jj_gen;
                    if (this.jj_2_1(1)) {
                        bObj = this.BooleanExpression();
                        return bObj == null ? "" : bObj.toString();
                    }
                    this.jj_consume_token(-1);
                    throw new ParseException();
                }
                if (this.jj_2_2(1)) {
                    bObj = this.RelationalExpression();
                    return bObj == null ? "" : bObj.toString();
                }
                this.jj_consume_token(-1);
                throw new ParseException();
            }
            return "";
        }
        catch (Throwable e) {
            this.setError("Error in Expression(): " + e.getMessage());
            return "";
        }
    }

    public final Boolean BooleanExpression() throws ParseException {
        Boolean bObj = null;
        try {
            this.isBooleanInside = this.isBooleanInside();
        }
        catch (Exception e) {
            this.isBooleanInside = false;
        }
        if (this.isBooleanInside) {
            this.jj_consume_token(36);
            bObj = this.BooleanExpression();
            this.jj_consume_token(37);
            return bObj;
        }
        if (this.jj_2_4(1)) {
            bObj = this.ConditionalOrExpression();
            return bObj;
        }
        this.jj_consume_token(-1);
        throw new ParseException();
    }

    public final boolean BooleanFunction() throws ParseException {
        if (this.jj_2_5(3)) {
            this.jj_consume_token(26);
            this.jj_consume_token(36);
            String s = this.StringExpression();
            this.jj_consume_token(37);
            s = s.equals("") ? null : s;
            return s == null;
        }
        switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
            case 26: {
                this.jj_consume_token(26);
                Double a = this.NumericExpression();
                return a == null;
            }
            case 27: {
                this.jj_consume_token(27);
                this.jj_consume_token(36);
                String s = this.StringExpression();
                this.jj_consume_token(104);
                String s2 = this.StringExpression();
                this.jj_consume_token(37);
                return DateFunctions.isDate(s, s2);
            }
            case 28: {
                this.jj_consume_token(28);
                this.jj_consume_token(36);
                String s = this.StringExpression();
                this.jj_consume_token(104);
                String s2 = this.StringExpression();
                this.jj_consume_token(37);
                return DateFunctions.isTime(s, s2);
            }
            case 29: {
                this.jj_consume_token(29);
                this.jj_consume_token(36);
                Double a = this.NumericExpression();
                this.jj_consume_token(37);
                return a != null && !a.isNaN();
            }
            case 9: {
                Token t = this.jj_consume_token(9);
                return t.toString().equals("true");
            }
        }
        this.jj_la1[2] = this.jj_gen;
        this.jj_consume_token(-1);
        throw new ParseException();
    }

    public final Boolean ConditionalOrExpression() throws ParseException {
        Boolean aObj = null;
        Boolean bObj = null;
        aObj = this.ConditionalAndExpression();
        block3: while (true) {
            switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                case 11: {
                    break;
                }
                default: {
                    this.jj_la1[3] = this.jj_gen;
                    break block3;
                }
            }
            this.jj_consume_token(11);
            bObj = this.BooleanExpression();
            if (this.honorNullInRelationalExpressions) {
                if (this.honorNullInANDORConditionals) {
                    aObj = aObj == null || bObj == null ? null : Boolean.valueOf(aObj != false || bObj != false);
                    continue;
                }
                aObj = aObj == null && bObj == null ? null : (aObj != null && aObj != false || bObj != null && bObj != false ? Boolean.valueOf(true) : Boolean.valueOf(false));
                continue;
            }
            aObj = aObj != false || bObj != false;
        }
        return aObj;
    }

    public final Boolean ConditionalAndExpression() throws ParseException {
        Boolean aObj = null;
        Boolean bObj = null;
        if (this.jj_2_6(1)) {
            aObj = this.RelationalExpression();
        } else {
            switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                case 9: 
                case 26: 
                case 27: 
                case 28: 
                case 29: {
                    boolean a = this.BooleanFunction();
                    aObj = a;
                    break;
                }
                default: {
                    this.jj_la1[4] = this.jj_gen;
                    this.jj_consume_token(-1);
                    throw new ParseException();
                }
            }
        }
        block6: while (true) {
            switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                case 10: {
                    break;
                }
                default: {
                    this.jj_la1[5] = this.jj_gen;
                    break block6;
                }
            }
            this.jj_consume_token(10);
            bObj = this.BooleanExpression();
            if (this.honorNullInRelationalExpressions) {
                if (this.honorNullInANDORConditionals) {
                    aObj = aObj == null || bObj == null ? null : Boolean.valueOf(aObj != false && bObj != false);
                    continue;
                }
                aObj = aObj == null && bObj == null ? null : (aObj != null && aObj != false && bObj != null && bObj != false ? Boolean.valueOf(true) : Boolean.valueOf(false));
                continue;
            }
            aObj = aObj != false && bObj != false;
        }
        return aObj;
    }

    public final Boolean RelationalExpression() throws ParseException {
        Boolean bObj = null;
        if (this.noOfBooleanFragment > 1) {
            this.jj_consume_token(36);
            bObj = this.BooleanExpression();
            this.jj_consume_token(37);
            return bObj;
        }
        switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
            case 4: 
            case 7: 
            case 8: 
            case 19: 
            case 20: 
            case 21: 
            case 22: 
            case 23: 
            case 24: 
            case 25: 
            case 31: 
            case 36: 
            case 38: 
            case 39: 
            case 40: 
            case 41: 
            case 42: 
            case 43: 
            case 44: 
            case 45: 
            case 46: 
            case 47: 
            case 48: 
            case 49: 
            case 50: 
            case 51: 
            case 52: 
            case 53: 
            case 54: 
            case 55: 
            case 56: 
            case 57: 
            case 58: 
            case 59: 
            case 60: 
            case 61: 
            case 62: 
            case 63: 
            case 64: 
            case 65: 
            case 66: 
            case 67: 
            case 68: 
            case 69: 
            case 70: 
            case 71: 
            case 72: 
            case 73: 
            case 74: 
            case 75: 
            case 76: 
            case 77: 
            case 78: 
            case 79: 
            case 80: 
            case 81: 
            case 82: 
            case 83: 
            case 84: 
            case 85: 
            case 86: 
            case 87: 
            case 88: 
            case 89: 
            case 90: 
            case 91: 
            case 92: 
            case 93: 
            case 94: 
            case 95: 
            case 96: 
            case 97: 
            case 98: 
            case 99: 
            case 100: {
                block3 : switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                    case 4: 
                    case 8: 
                    case 19: 
                    case 20: 
                    case 21: 
                    case 22: 
                    case 23: 
                    case 31: 
                    case 36: 
                    case 38: 
                    case 39: 
                    case 40: 
                    case 41: 
                    case 42: 
                    case 43: 
                    case 44: 
                    case 45: 
                    case 46: 
                    case 47: 
                    case 48: 
                    case 49: 
                    case 50: 
                    case 51: 
                    case 52: 
                    case 53: 
                    case 54: 
                    case 55: 
                    case 56: 
                    case 57: 
                    case 58: 
                    case 59: 
                    case 60: 
                    case 61: 
                    case 62: 
                    case 63: 
                    case 64: 
                    case 65: 
                    case 98: {
                        Double n1 = this.NumericExpression();
                        switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                            case 12: {
                                this.jj_consume_token(12);
                                Double n2 = this.NumericExpression();
                                if (this.honorNullInRelationalExpressions) {
                                    bObj = n1 == null || n2 == null ? null : Boolean.valueOf(n1.equals(n2));
                                    break block3;
                                }
                                bObj = n1 == null && n2 == null ? true : (n1 == null || n2 == null ? false : n1.equals(n2));
                                break block3;
                            }
                            case 13: {
                                this.jj_consume_token(13);
                                Double n2 = this.NumericExpression();
                                if (this.honorNullInRelationalExpressions) {
                                    bObj = n1 == null || n2 == null ? null : Boolean.valueOf(!n1.equals(n2));
                                    break block3;
                                }
                                bObj = n1 == null && n2 == null ? false : (n1 == null || n2 == null ? true : !n1.equals(n2));
                                break block3;
                            }
                            case 14: {
                                this.jj_consume_token(14);
                                Double n2 = this.NumericExpression();
                                if (this.honorNullInRelationalExpressions) {
                                    bObj = n1 == null || n2 == null ? null : Boolean.valueOf(n1.compareTo(n2) == -1);
                                    break block3;
                                }
                                bObj = n1 == null || n2 == null ? false : n1.compareTo(n2) == -1;
                                break block3;
                            }
                            case 15: {
                                this.jj_consume_token(15);
                                Double n2 = this.NumericExpression();
                                if (this.honorNullInRelationalExpressions) {
                                    bObj = n1 == null || n2 == null ? null : Boolean.valueOf(n1.compareTo(n2) == 1);
                                    break block3;
                                }
                                bObj = n1 == null || n2 == null ? false : n1.compareTo(n2) == 1;
                                break block3;
                            }
                            case 16: {
                                this.jj_consume_token(16);
                                Double n2 = this.NumericExpression();
                                if (this.honorNullInRelationalExpressions) {
                                    bObj = n1 == null || n2 == null ? null : Boolean.valueOf(n1.compareTo(n2) == -1 || n1.compareTo(n2) == 0);
                                    break block3;
                                }
                                bObj = n1 == null && n2 == null ? true : (n1 == null || n2 == null ? false : n1.compareTo(n2) == -1 || n1.compareTo(n2) == 0);
                                break block3;
                            }
                            case 17: {
                                this.jj_consume_token(17);
                                Double n2 = this.NumericExpression();
                                if (this.honorNullInRelationalExpressions) {
                                    bObj = n1 == null || n2 == null ? null : Boolean.valueOf(n1.compareTo(n2) == 1 || n1.compareTo(n2) == 0);
                                    break block3;
                                }
                                bObj = n1 == null && n2 == null ? true : (n1 == null || n2 == null ? false : n1.compareTo(n2) == 1 || n1.compareTo(n2) == 0);
                                break block3;
                            }
                        }
                        this.jj_la1[6] = this.jj_gen;
                        this.jj_consume_token(-1);
                        throw new ParseException();
                    }
                    case 7: 
                    case 24: 
                    case 25: 
                    case 66: 
                    case 67: 
                    case 68: 
                    case 69: 
                    case 70: 
                    case 71: 
                    case 72: 
                    case 73: 
                    case 74: 
                    case 75: 
                    case 76: 
                    case 77: 
                    case 78: 
                    case 79: 
                    case 80: 
                    case 81: 
                    case 82: 
                    case 83: 
                    case 84: 
                    case 85: 
                    case 86: 
                    case 87: 
                    case 88: 
                    case 89: 
                    case 90: 
                    case 91: 
                    case 92: 
                    case 93: 
                    case 94: 
                    case 95: 
                    case 96: 
                    case 97: 
                    case 99: 
                    case 100: {
                        String s1 = this.StringExpression();
                        switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                            case 12: {
                                this.jj_consume_token(12);
                                String s2 = this.StringExpression();
                                if (this.honorNullInRelationalExpressions) {
                                    bObj = s1 == null || s2 == null ? null : Boolean.valueOf(s1.equals(s2));
                                    break block3;
                                }
                                bObj = s1 == null && s2 == null ? true : (s1 == null || s2 == null ? false : s1.equals(s2));
                                break block3;
                            }
                            case 13: {
                                this.jj_consume_token(13);
                                String s2 = this.StringExpression();
                                if (this.honorNullInRelationalExpressions) {
                                    bObj = s1 == null || s2 == null ? null : Boolean.valueOf(!s1.equals(s2));
                                    break block3;
                                }
                                bObj = s1 == null && s2 == null ? false : (s1 == null || s2 == null ? true : !s1.equals(s2));
                                break block3;
                            }
                            case 14: {
                                this.jj_consume_token(14);
                                String s2 = this.StringExpression();
                                if (this.honorNullInRelationalExpressions) {
                                    bObj = s1 == null || s2 == null ? null : Boolean.valueOf(s1.compareTo(s2) == -1);
                                    break block3;
                                }
                                bObj = s1 == null || s2 == null ? false : s1.compareTo(s2) == -1;
                                break block3;
                            }
                            case 15: {
                                this.jj_consume_token(15);
                                String s2 = this.StringExpression();
                                if (this.honorNullInRelationalExpressions) {
                                    bObj = s1 == null || s2 == null ? null : Boolean.valueOf(s1.compareTo(s2) == 1);
                                    break block3;
                                }
                                bObj = s1 == null || s2 == null ? false : s1.compareTo(s2) == 1;
                                break block3;
                            }
                            case 16: {
                                this.jj_consume_token(16);
                                String s2 = this.StringExpression();
                                if (this.honorNullInRelationalExpressions) {
                                    bObj = s1 == null || s2 == null ? null : Boolean.valueOf(s1.compareTo(s2) == -1 || s1.compareTo(s2) == 0);
                                    break block3;
                                }
                                bObj = s1 == null && s2 == null ? true : (s1 == null || s2 == null ? false : s1.compareTo(s2) == -1 || s1.compareTo(s2) == 0);
                                break block3;
                            }
                            case 17: {
                                this.jj_consume_token(17);
                                String s2 = this.StringExpression();
                                if (this.honorNullInRelationalExpressions) {
                                    bObj = s1 == null || s2 == null ? null : Boolean.valueOf(s1.compareTo(s2) == 1 || s1.compareTo(s2) == 0);
                                    break block3;
                                }
                                bObj = s1 == null && s2 == null ? true : (s1 == null || s2 == null ? false : s1.compareTo(s2) == 1 || s1.compareTo(s2) == 0);
                                break block3;
                            }
                        }
                        this.jj_la1[7] = this.jj_gen;
                        this.jj_consume_token(-1);
                        throw new ParseException();
                    }
                    default: {
                        this.jj_la1[8] = this.jj_gen;
                        this.jj_consume_token(-1);
                        throw new ParseException();
                    }
                }
                return bObj;
            }
        }
        this.jj_la1[9] = this.jj_gen;
        this.jj_consume_token(-1);
        throw new ParseException();
    }

    public final Object MultiObjectListExpression() throws ParseException {
        Object a;
        try {
            a = this.ObjectFunction();
        }
        catch (Exception e) {
            this.setError("Error in MultiObjectListExpression: " + e.getMessage());
            a = null;
        }
        return a;
    }

    public final Double NumericExpression() throws ParseException {
        Double a;
        try {
            a = this.AdditiveExpression();
            if (a == 0.0) {
                a = new Double(0.0);
            }
        }
        catch (Exception e) {
            a = null;
        }
        return a;
    }

    public final Double AdditiveExpression() throws ParseException {
        Double a;
        block10: {
            int prea = -1;
            int preb = -1;
            try {
                a = this.MultiplicativeExpression();
                block9: while (true) {
                    switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                        case 30: 
                        case 31: {
                            break;
                        }
                        default: {
                            this.jj_la1[10] = this.jj_gen;
                            break block10;
                        }
                    }
                    switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                        case 30: {
                            this.jj_consume_token(30);
                            Double b = this.MultiplicativeExpression();
                            a = new Double(BigDecimal.valueOf(a).add(BigDecimal.valueOf(b)).doubleValue());
                            continue block9;
                        }
                        case 31: {
                            this.jj_consume_token(31);
                            Double b = this.MultiplicativeExpression();
                            a = new Double(BigDecimal.valueOf(a).subtract(BigDecimal.valueOf(b)).doubleValue());
                            continue block9;
                        }
                    }
                    break;
                }
                this.jj_la1[11] = this.jj_gen;
                this.jj_consume_token(-1);
                throw new ParseException();
            }
            catch (Exception e) {
                this.setError("Error in AdditiveExpression: " + e.getMessage());
                a = null;
            }
        }
        return a;
    }

    public final Double MultiplicativeExpression() throws ParseException {
        Double a;
        block11: {
            try {
                a = this.PowerExpression();
                block10: while (true) {
                    switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                        case 32: 
                        case 33: 
                        case 34: {
                            break;
                        }
                        default: {
                            this.jj_la1[12] = this.jj_gen;
                            break block11;
                        }
                    }
                    switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                        case 32: {
                            this.jj_consume_token(32);
                            Double b = this.PowerExpression();
                            a = new Double(BigDecimal.valueOf(a).multiply(BigDecimal.valueOf(b)).doubleValue());
                            continue block10;
                        }
                        case 33: {
                            this.jj_consume_token(33);
                            Double b = this.PowerExpression();
                            a = new Double(BigDecimal.valueOf(a).divide(BigDecimal.valueOf(b), MathContext.DECIMAL128).doubleValue());
                            continue block10;
                        }
                        case 34: {
                            this.jj_consume_token(34);
                            Double b = this.PowerExpression();
                            a = new Double(BigDecimal.valueOf(a).remainder(BigDecimal.valueOf(b)).doubleValue());
                            continue block10;
                        }
                    }
                    break;
                }
                this.jj_la1[13] = this.jj_gen;
                this.jj_consume_token(-1);
                throw new ParseException();
            }
            catch (Exception e) {
                this.setError("Error in MultiplicativeExpression: " + e.getMessage());
                a = null;
            }
        }
        return a;
    }

    public final Double PowerExpression() throws ParseException {
        Double a;
        block6: {
            try {
                a = this.UnaryExpression();
                while (true) {
                    switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                        case 35: {
                            break;
                        }
                        default: {
                            this.jj_la1[14] = this.jj_gen;
                            break block6;
                        }
                    }
                    this.jj_consume_token(35);
                    Double b = this.UnaryExpression();
                    a = new Double(Math.pow(BigDecimal.valueOf(a).doubleValue(), BigDecimal.valueOf(b).doubleValue()));
                }
            }
            catch (Exception e) {
                this.setError("Error in PowerExpression: " + e.getMessage());
                a = null;
            }
        }
        return a;
    }

    public final Double UnaryExpression() throws ParseException {
        Double a;
        try {
            switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                case 31: {
                    this.jj_consume_token(31);
                    Double a2 = this.NumericElement();
                    return new Double(BigDecimal.valueOf(a2).multiply(new BigDecimal("-1")).doubleValue());
                }
                case 4: 
                case 8: 
                case 19: 
                case 20: 
                case 21: 
                case 22: 
                case 23: 
                case 36: 
                case 38: 
                case 39: 
                case 40: 
                case 41: 
                case 42: 
                case 43: 
                case 44: 
                case 45: 
                case 46: 
                case 47: 
                case 48: 
                case 49: 
                case 50: 
                case 51: 
                case 52: 
                case 53: 
                case 54: 
                case 55: 
                case 56: 
                case 57: 
                case 58: 
                case 59: 
                case 60: 
                case 61: 
                case 62: 
                case 63: 
                case 64: 
                case 65: 
                case 98: {
                    a = this.NumericElement();
                    break;
                }
                default: {
                    this.jj_la1[15] = this.jj_gen;
                    this.jj_consume_token(-1);
                    throw new ParseException();
                }
            }
        }
        catch (Exception e) {
            this.setError("Error in UnaryExpression: " + e.getMessage());
            a = null;
        }
        return a;
    }

    public final Double NumericElement() throws ParseException {
        Double a;
        switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
            case 4: {
                Token t = this.jj_consume_token(4);
                a = new Double(t.toString());
                break;
            }
            case 36: {
                this.jj_consume_token(36);
                a = this.NumericExpression();
                this.jj_consume_token(37);
                break;
            }
            case 19: 
            case 20: 
            case 39: 
            case 40: 
            case 41: 
            case 42: 
            case 43: 
            case 44: 
            case 45: 
            case 46: 
            case 47: 
            case 48: 
            case 49: 
            case 50: 
            case 51: 
            case 52: 
            case 53: 
            case 61: 
            case 62: 
            case 63: 
            case 64: 
            case 65: {
                a = this.NumericFunction();
                break;
            }
            case 8: {
                Token t = this.jj_consume_token(8);
                String s = t.toString();
                try {
                    s = this.params.get(s.substring(1, s.length() - 1)).toString();
                    a = new Double(s);
                }
                catch (Exception e) {
                    a = null;
                }
                break;
            }
            case 98: {
                String s = this.IfStatement();
                try {
                    a = new Double(s);
                }
                catch (Exception e) {
                    this.setError("Unexpected non-numeric value: " + e.getMessage());
                    a = null;
                }
                break;
            }
            case 21: 
            case 22: 
            case 23: 
            case 38: 
            case 54: 
            case 55: 
            case 56: 
            case 57: 
            case 58: 
            case 59: 
            case 60: {
                Object o = this.MultiObjectListExpression();
                String s = "";
                SimpleDateFormat df = new SimpleDateFormat(DateFormatter.getJavaCompatibleFormatString(""));
                if (o instanceof Calendar) {
                    Calendar c = (Calendar)o;
                    s = df.format(c.getTime());
                }
                try {
                    int idx;
                    s = o.toString();
                    if (s.indexOf("E") >= 0) {
                        s = new BigDecimal(s).stripTrailingZeros().toPlainString();
                    }
                    if ((idx = s.indexOf(".")) != -1 && !(new Double(s.substring(idx)) > 0.0)) {
                        s = s.substring(0, idx);
                    }
                }
                catch (Exception e) {
                    this.setError("Error in NumericElement: " + e.getMessage());
                }
                try {
                    a = new Double(s);
                }
                catch (Exception e) {
                    this.setError("Unexpected non-numeric value: " + e.getMessage());
                    a = null;
                }
                break;
            }
            default: {
                this.jj_la1[16] = this.jj_gen;
                this.jj_consume_token(-1);
                throw new ParseException();
            }
        }
        return a;
    }

    public final Double[] NumericList() throws ParseException {
        int i;
        String[] s = this.StringList();
        ArrayList<Double> lst = new ArrayList<Double>();
        for (i = 0; i < s.length; ++i) {
            try {
                lst.add(new Double(s[i]));
                continue;
            }
            catch (Exception e) {
                this.setError("Unexpected non-numeric value: " + e.getMessage());
                if (s[i] == null || s[i].equals("")) continue;
                lst.add(new Double(0.0));
            }
        }
        lst.trimToSize();
        Double[] doublelist = new Double[lst.size()];
        for (i = 0; i < lst.size(); ++i) {
            doublelist[i] = (Double)lst.get(i);
        }
        return doublelist;
    }

    /*
     * Unable to fully structure code
     */
    public final Object ObjectFunction() throws ParseException {
        al = null;
        a2 = new ArrayList<Double>();
        ep = null;
        switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
            case 55: {
                this.jj_consume_token(55);
                this.jj_consume_token(36);
                a = this.getObject1();
                if (a instanceof ExpressionParam) {
                    ep = (ExpressionParam)a;
                } else {
                    al = this.createArray(a);
                }
                block100: while (true) {
                    switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                        case 4: 
                        case 8: 
                        case 19: 
                        case 20: 
                        case 21: 
                        case 22: 
                        case 23: 
                        case 31: 
                        case 36: 
                        case 38: 
                        case 39: 
                        case 40: 
                        case 41: 
                        case 42: 
                        case 43: 
                        case 44: 
                        case 45: 
                        case 46: 
                        case 47: 
                        case 48: 
                        case 49: 
                        case 50: 
                        case 51: 
                        case 52: 
                        case 53: 
                        case 54: 
                        case 55: 
                        case 56: 
                        case 57: 
                        case 58: 
                        case 59: 
                        case 60: 
                        case 61: 
                        case 62: 
                        case 63: 
                        case 64: 
                        case 65: 
                        case 98: 
                        case 104: {
                            break;
                        }
                        default: {
                            this.jj_la1[17] = this.jj_gen;
                            ** GOTO lbl47
                        }
                    }
                    switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                        case 104: {
                            this.jj_consume_token(104);
                            s = this.Primary();
                            if (s == null || s.length() <= 0) continue block100;
                            try {
                                a = new Double(s);
                                al.add(a);
                            }
                            catch (Exception e) {
                                this.setError("Unexpected non-numeric value: " + e.getMessage());
                                al.add(new Double(0.0));
                            }
                            continue block100;
                        }
                        case 4: 
                        case 8: 
                        case 19: 
                        case 20: 
                        case 21: 
                        case 22: 
                        case 23: 
                        case 31: 
                        case 36: 
                        case 38: 
                        case 39: 
                        case 40: 
                        case 41: 
                        case 42: 
                        case 43: 
                        case 44: 
                        case 45: 
                        case 46: 
                        case 47: 
                        case 48: 
                        case 49: 
                        case 50: 
                        case 51: 
                        case 52: 
                        case 53: 
                        case 54: 
                        case 55: 
                        case 56: 
                        case 57: 
                        case 58: 
                        case 59: 
                        case 60: 
                        case 61: 
                        case 62: 
                        case 63: 
                        case 64: 
                        case 65: 
                        case 98: {
                            d = this.NumericExpression();
                            al.add(d);
                            continue block100;
                        }
                    }
                    break;
                }
                this.jj_la1[18] = this.jj_gen;
                this.jj_consume_token(-1);
                throw new ParseException();
lbl47:
                // 1 sources

                this.jj_consume_token(37);
                if (ep == null) {
                    ep = this.buildExpParam(al);
                }
                return CalcFunctions.first(ep);
            }
            case 56: {
                this.jj_consume_token(56);
                this.jj_consume_token(36);
                a = this.getObject1();
                if (a instanceof ExpressionParam) {
                    ep = (ExpressionParam)a;
                } else {
                    al = this.createArray(a);
                }
                block101: while (true) {
                    switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                        case 4: 
                        case 8: 
                        case 19: 
                        case 20: 
                        case 21: 
                        case 22: 
                        case 23: 
                        case 31: 
                        case 36: 
                        case 38: 
                        case 39: 
                        case 40: 
                        case 41: 
                        case 42: 
                        case 43: 
                        case 44: 
                        case 45: 
                        case 46: 
                        case 47: 
                        case 48: 
                        case 49: 
                        case 50: 
                        case 51: 
                        case 52: 
                        case 53: 
                        case 54: 
                        case 55: 
                        case 56: 
                        case 57: 
                        case 58: 
                        case 59: 
                        case 60: 
                        case 61: 
                        case 62: 
                        case 63: 
                        case 64: 
                        case 65: 
                        case 98: 
                        case 104: {
                            break;
                        }
                        default: {
                            this.jj_la1[19] = this.jj_gen;
                            ** GOTO lbl94
                        }
                    }
                    switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                        case 104: {
                            this.jj_consume_token(104);
                            s = this.Primary();
                            if (s == null || s.length() <= 0) continue block101;
                            try {
                                a = new Double(s);
                                al.add(a);
                            }
                            catch (Exception e) {
                                this.setError("Unexpected non-numeric value: " + e.getMessage());
                                al.add(new Double(0.0));
                            }
                            continue block101;
                        }
                        case 4: 
                        case 8: 
                        case 19: 
                        case 20: 
                        case 21: 
                        case 22: 
                        case 23: 
                        case 31: 
                        case 36: 
                        case 38: 
                        case 39: 
                        case 40: 
                        case 41: 
                        case 42: 
                        case 43: 
                        case 44: 
                        case 45: 
                        case 46: 
                        case 47: 
                        case 48: 
                        case 49: 
                        case 50: 
                        case 51: 
                        case 52: 
                        case 53: 
                        case 54: 
                        case 55: 
                        case 56: 
                        case 57: 
                        case 58: 
                        case 59: 
                        case 60: 
                        case 61: 
                        case 62: 
                        case 63: 
                        case 64: 
                        case 65: 
                        case 98: {
                            d = this.NumericExpression();
                            al.add(d);
                            continue block101;
                        }
                    }
                    break;
                }
                this.jj_la1[20] = this.jj_gen;
                this.jj_consume_token(-1);
                throw new ParseException();
lbl94:
                // 1 sources

                this.jj_consume_token(37);
                if (ep == null) {
                    ep = this.buildExpParam(al);
                }
                return CalcFunctions.last(ep);
            }
            case 54: {
                this.jj_consume_token(54);
                this.jj_consume_token(36);
                a = this.getObject1();
                if (a instanceof ExpressionParam) {
                    ep = (ExpressionParam)a;
                } else {
                    al = this.createArray(a);
                }
                block102: while (true) {
                    switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                        case 4: 
                        case 8: 
                        case 19: 
                        case 20: 
                        case 21: 
                        case 22: 
                        case 23: 
                        case 31: 
                        case 36: 
                        case 38: 
                        case 39: 
                        case 40: 
                        case 41: 
                        case 42: 
                        case 43: 
                        case 44: 
                        case 45: 
                        case 46: 
                        case 47: 
                        case 48: 
                        case 49: 
                        case 50: 
                        case 51: 
                        case 52: 
                        case 53: 
                        case 54: 
                        case 55: 
                        case 56: 
                        case 57: 
                        case 58: 
                        case 59: 
                        case 60: 
                        case 61: 
                        case 62: 
                        case 63: 
                        case 64: 
                        case 65: 
                        case 98: 
                        case 104: {
                            break;
                        }
                        default: {
                            this.jj_la1[21] = this.jj_gen;
                            ** GOTO lbl141
                        }
                    }
                    switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                        case 104: {
                            this.jj_consume_token(104);
                            s = this.Primary();
                            if (s == null || s.length() <= 0) continue block102;
                            try {
                                a = new Double(s);
                                al.add(a);
                            }
                            catch (Exception e) {
                                this.setError("Unexpected non-numeric value: " + e.getMessage());
                                al.add(new Double(0.0));
                            }
                            continue block102;
                        }
                        case 4: 
                        case 8: 
                        case 19: 
                        case 20: 
                        case 21: 
                        case 22: 
                        case 23: 
                        case 31: 
                        case 36: 
                        case 38: 
                        case 39: 
                        case 40: 
                        case 41: 
                        case 42: 
                        case 43: 
                        case 44: 
                        case 45: 
                        case 46: 
                        case 47: 
                        case 48: 
                        case 49: 
                        case 50: 
                        case 51: 
                        case 52: 
                        case 53: 
                        case 54: 
                        case 55: 
                        case 56: 
                        case 57: 
                        case 58: 
                        case 59: 
                        case 60: 
                        case 61: 
                        case 62: 
                        case 63: 
                        case 64: 
                        case 65: 
                        case 98: {
                            d = this.NumericExpression();
                            al.add(d);
                            continue block102;
                        }
                    }
                    break;
                }
                this.jj_la1[22] = this.jj_gen;
                this.jj_consume_token(-1);
                throw new ParseException();
lbl141:
                // 1 sources

                this.jj_consume_token(37);
                if (ep == null) {
                    ep = this.buildExpParam(al);
                }
                return new Double(result = CalcFunctions.count(ep)).isNaN() != false ? "" : new Double(result).toString();
            }
            case 23: {
                this.jj_consume_token(23);
                this.jj_consume_token(36);
                block103: while (this.jj_2_7(1)) {
                    this.objType = this.getObjectType();
                    switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                        case 104: {
                            this.jj_consume_token(104);
                            continue block103;
                        }
                    }
                    this.jj_la1[23] = this.jj_gen;
                    if (this.objType != null && (this.objType.equals("ExpressionParam") || this.objType.equals("ObjectArray"))) {
                        a = this.getObject();
                        if (a instanceof ExpressionParam) {
                            ep = (ExpressionParam)a;
                            continue;
                        }
                        a2.addAll(this.getParsedArray(a));
                        continue;
                    }
                    switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                        case 4: 
                        case 8: 
                        case 19: 
                        case 20: 
                        case 21: 
                        case 22: 
                        case 23: 
                        case 31: 
                        case 36: 
                        case 38: 
                        case 39: 
                        case 40: 
                        case 41: 
                        case 42: 
                        case 43: 
                        case 44: 
                        case 45: 
                        case 46: 
                        case 47: 
                        case 48: 
                        case 49: 
                        case 50: 
                        case 51: 
                        case 52: 
                        case 53: 
                        case 54: 
                        case 55: 
                        case 56: 
                        case 57: 
                        case 58: 
                        case 59: 
                        case 60: 
                        case 61: 
                        case 62: 
                        case 63: 
                        case 64: 
                        case 65: 
                        case 98: {
                            d = this.NumericExpression();
                            a2.add(d);
                            continue block103;
                        }
                    }
                    this.jj_la1[24] = this.jj_gen;
                    this.jj_consume_token(-1);
                    throw new ParseException();
                }
                this.jj_consume_token(37);
                if (ep == null) {
                    ep = this.buildExpParam(a2);
                }
                return new Double(result = CalcFunctions.sum(ep)).isNaN() != false ? "" : new Double(result).toString();
            }
            case 38: {
                this.jj_consume_token(38);
                this.jj_consume_token(36);
                block104: while (this.jj_2_8(1)) {
                    this.objType = this.getObjectType();
                    switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                        case 104: {
                            this.jj_consume_token(104);
                            continue block104;
                        }
                    }
                    this.jj_la1[25] = this.jj_gen;
                    if (this.objType != null && (this.objType.equals("ExpressionParam") || this.objType.equals("ObjectArray"))) {
                        a = this.getObject();
                        if (a instanceof ExpressionParam) {
                            ep = (ExpressionParam)a;
                            continue;
                        }
                        a2.addAll(this.getParsedArray(a));
                        continue;
                    }
                    switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                        case 4: 
                        case 8: 
                        case 19: 
                        case 20: 
                        case 21: 
                        case 22: 
                        case 23: 
                        case 31: 
                        case 36: 
                        case 38: 
                        case 39: 
                        case 40: 
                        case 41: 
                        case 42: 
                        case 43: 
                        case 44: 
                        case 45: 
                        case 46: 
                        case 47: 
                        case 48: 
                        case 49: 
                        case 50: 
                        case 51: 
                        case 52: 
                        case 53: 
                        case 54: 
                        case 55: 
                        case 56: 
                        case 57: 
                        case 58: 
                        case 59: 
                        case 60: 
                        case 61: 
                        case 62: 
                        case 63: 
                        case 64: 
                        case 65: 
                        case 98: {
                            d = this.NumericExpression();
                            a2.add(d);
                            continue block104;
                        }
                    }
                    this.jj_la1[26] = this.jj_gen;
                    this.jj_consume_token(-1);
                    throw new ParseException();
                }
                this.jj_consume_token(37);
                if (ep == null) {
                    ep = this.buildExpParam(a2);
                }
                return new Double(result = CalcFunctions.avg(ep)).isNaN() != false ? "" : new Double(result).toString();
            }
            case 57: {
                this.jj_consume_token(57);
                this.jj_consume_token(36);
                a = this.getObject1();
                if (a instanceof ExpressionParam) {
                    ep = (ExpressionParam)a;
                } else {
                    al = this.createArray(a);
                }
                block105: while (true) {
                    switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                        case 4: 
                        case 8: 
                        case 19: 
                        case 20: 
                        case 21: 
                        case 22: 
                        case 23: 
                        case 31: 
                        case 36: 
                        case 38: 
                        case 39: 
                        case 40: 
                        case 41: 
                        case 42: 
                        case 43: 
                        case 44: 
                        case 45: 
                        case 46: 
                        case 47: 
                        case 48: 
                        case 49: 
                        case 50: 
                        case 51: 
                        case 52: 
                        case 53: 
                        case 54: 
                        case 55: 
                        case 56: 
                        case 57: 
                        case 58: 
                        case 59: 
                        case 60: 
                        case 61: 
                        case 62: 
                        case 63: 
                        case 64: 
                        case 65: 
                        case 98: 
                        case 104: {
                            break;
                        }
                        default: {
                            this.jj_la1[27] = this.jj_gen;
                            ** GOTO lbl260
                        }
                    }
                    switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                        case 104: {
                            this.jj_consume_token(104);
                            s = this.Primary();
                            if (s == null || s.length() <= 0) continue block105;
                            try {
                                a = new Double(s);
                                al.add(a);
                            }
                            catch (Exception e) {
                                this.setError("Unexpected non-numeric value: " + e.getMessage());
                                al.add(new Double(0.0));
                            }
                            continue block105;
                        }
                        case 4: 
                        case 8: 
                        case 19: 
                        case 20: 
                        case 21: 
                        case 22: 
                        case 23: 
                        case 31: 
                        case 36: 
                        case 38: 
                        case 39: 
                        case 40: 
                        case 41: 
                        case 42: 
                        case 43: 
                        case 44: 
                        case 45: 
                        case 46: 
                        case 47: 
                        case 48: 
                        case 49: 
                        case 50: 
                        case 51: 
                        case 52: 
                        case 53: 
                        case 54: 
                        case 55: 
                        case 56: 
                        case 57: 
                        case 58: 
                        case 59: 
                        case 60: 
                        case 61: 
                        case 62: 
                        case 63: 
                        case 64: 
                        case 65: 
                        case 98: {
                            d = this.NumericExpression();
                            al.add(d);
                            continue block105;
                        }
                    }
                    break;
                }
                this.jj_la1[28] = this.jj_gen;
                this.jj_consume_token(-1);
                throw new ParseException();
lbl260:
                // 1 sources

                this.jj_consume_token(37);
                if (ep == null) {
                    ep = this.buildExpParam(al);
                }
                return new Double(result = CalcFunctions.median(ep)).isNaN() != false ? "" : new Double(result).toString();
            }
            case 58: {
                this.jj_consume_token(58);
                this.jj_consume_token(36);
                a = this.getObject1();
                if (a instanceof ExpressionParam) {
                    ep = (ExpressionParam)a;
                } else {
                    al = this.createArray(a);
                }
                block106: while (true) {
                    switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                        case 4: 
                        case 8: 
                        case 19: 
                        case 20: 
                        case 21: 
                        case 22: 
                        case 23: 
                        case 31: 
                        case 36: 
                        case 38: 
                        case 39: 
                        case 40: 
                        case 41: 
                        case 42: 
                        case 43: 
                        case 44: 
                        case 45: 
                        case 46: 
                        case 47: 
                        case 48: 
                        case 49: 
                        case 50: 
                        case 51: 
                        case 52: 
                        case 53: 
                        case 54: 
                        case 55: 
                        case 56: 
                        case 57: 
                        case 58: 
                        case 59: 
                        case 60: 
                        case 61: 
                        case 62: 
                        case 63: 
                        case 64: 
                        case 65: 
                        case 98: 
                        case 104: {
                            break;
                        }
                        default: {
                            this.jj_la1[29] = this.jj_gen;
                            ** GOTO lbl307
                        }
                    }
                    switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                        case 104: {
                            this.jj_consume_token(104);
                            s = this.Primary();
                            if (s == null || s.length() <= 0) continue block106;
                            try {
                                a = new Double(s);
                                al.add(a);
                            }
                            catch (Exception e) {
                                this.setError("Unexpected non-numeric value: " + e.getMessage());
                                al.add(new Double(0.0));
                            }
                            continue block106;
                        }
                        case 4: 
                        case 8: 
                        case 19: 
                        case 20: 
                        case 21: 
                        case 22: 
                        case 23: 
                        case 31: 
                        case 36: 
                        case 38: 
                        case 39: 
                        case 40: 
                        case 41: 
                        case 42: 
                        case 43: 
                        case 44: 
                        case 45: 
                        case 46: 
                        case 47: 
                        case 48: 
                        case 49: 
                        case 50: 
                        case 51: 
                        case 52: 
                        case 53: 
                        case 54: 
                        case 55: 
                        case 56: 
                        case 57: 
                        case 58: 
                        case 59: 
                        case 60: 
                        case 61: 
                        case 62: 
                        case 63: 
                        case 64: 
                        case 65: 
                        case 98: {
                            d = this.NumericExpression();
                            al.add(d);
                            continue block106;
                        }
                    }
                    break;
                }
                this.jj_la1[30] = this.jj_gen;
                this.jj_consume_token(-1);
                throw new ParseException();
lbl307:
                // 1 sources

                this.jj_consume_token(37);
                if (ep == null) {
                    ep = this.buildExpParam(al);
                }
                return new Double(result = CalcFunctions.mode(ep)).isNaN() != false ? "" : new Double(result).toString();
            }
            case 59: {
                this.jj_consume_token(59);
                this.jj_consume_token(36);
                block107: while (this.jj_2_9(1)) {
                    this.objType = this.getObjectType();
                    switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                        case 104: {
                            this.jj_consume_token(104);
                            continue block107;
                        }
                    }
                    this.jj_la1[31] = this.jj_gen;
                    if (this.objType != null && (this.objType.equals("ExpressionParam") || this.objType.equals("ObjectArray"))) {
                        a = this.getObject();
                        if (a instanceof ExpressionParam) {
                            ep = (ExpressionParam)a;
                            continue;
                        }
                        a2.addAll(this.getParsedArray(a));
                        continue;
                    }
                    switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                        case 4: 
                        case 8: 
                        case 19: 
                        case 20: 
                        case 21: 
                        case 22: 
                        case 23: 
                        case 31: 
                        case 36: 
                        case 38: 
                        case 39: 
                        case 40: 
                        case 41: 
                        case 42: 
                        case 43: 
                        case 44: 
                        case 45: 
                        case 46: 
                        case 47: 
                        case 48: 
                        case 49: 
                        case 50: 
                        case 51: 
                        case 52: 
                        case 53: 
                        case 54: 
                        case 55: 
                        case 56: 
                        case 57: 
                        case 58: 
                        case 59: 
                        case 60: 
                        case 61: 
                        case 62: 
                        case 63: 
                        case 64: 
                        case 65: 
                        case 98: {
                            d = this.NumericExpression();
                            a2.add(d);
                            continue block107;
                        }
                    }
                    this.jj_la1[32] = this.jj_gen;
                    this.jj_consume_token(-1);
                    throw new ParseException();
                }
                this.jj_consume_token(37);
                if (ep == null) {
                    ep = this.buildExpParam(a2);
                }
                return new Double(result = CalcFunctions.stdev(ep)).isNaN() != false ? "" : new Double(result).toString();
            }
            case 60: {
                this.jj_consume_token(60);
                this.jj_consume_token(36);
                block108: while (this.jj_2_10(1)) {
                    this.objType = this.getObjectType();
                    switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                        case 104: {
                            this.jj_consume_token(104);
                            continue block108;
                        }
                    }
                    this.jj_la1[33] = this.jj_gen;
                    if (this.objType != null && (this.objType.equals("ExpressionParam") || this.objType.equals("ObjectArray"))) {
                        a = this.getObject();
                        if (a instanceof ExpressionParam) {
                            ep = (ExpressionParam)a;
                            continue;
                        }
                        a2.addAll(this.getParsedArray(a));
                        continue;
                    }
                    switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                        case 4: 
                        case 8: 
                        case 19: 
                        case 20: 
                        case 21: 
                        case 22: 
                        case 23: 
                        case 31: 
                        case 36: 
                        case 38: 
                        case 39: 
                        case 40: 
                        case 41: 
                        case 42: 
                        case 43: 
                        case 44: 
                        case 45: 
                        case 46: 
                        case 47: 
                        case 48: 
                        case 49: 
                        case 50: 
                        case 51: 
                        case 52: 
                        case 53: 
                        case 54: 
                        case 55: 
                        case 56: 
                        case 57: 
                        case 58: 
                        case 59: 
                        case 60: 
                        case 61: 
                        case 62: 
                        case 63: 
                        case 64: 
                        case 65: 
                        case 98: {
                            d = this.NumericExpression();
                            a2.add(d);
                            continue block108;
                        }
                    }
                    this.jj_la1[34] = this.jj_gen;
                    this.jj_consume_token(-1);
                    throw new ParseException();
                }
                this.jj_consume_token(37);
                if (ep == null) {
                    ep = this.buildExpParam(a2);
                }
                return new Double(result = CalcFunctions.var(ep)).isNaN() != false ? "" : new Double(result).toString();
            }
            case 22: {
                this.jj_consume_token(22);
                this.jj_consume_token(36);
                a = this.getObject1();
                if (a instanceof ExpressionParam) {
                    ep = (ExpressionParam)a;
                } else {
                    al = this.createArray(a);
                }
                block109: while (true) {
                    switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                        case 4: 
                        case 8: 
                        case 19: 
                        case 20: 
                        case 21: 
                        case 22: 
                        case 23: 
                        case 31: 
                        case 36: 
                        case 38: 
                        case 39: 
                        case 40: 
                        case 41: 
                        case 42: 
                        case 43: 
                        case 44: 
                        case 45: 
                        case 46: 
                        case 47: 
                        case 48: 
                        case 49: 
                        case 50: 
                        case 51: 
                        case 52: 
                        case 53: 
                        case 54: 
                        case 55: 
                        case 56: 
                        case 57: 
                        case 58: 
                        case 59: 
                        case 60: 
                        case 61: 
                        case 62: 
                        case 63: 
                        case 64: 
                        case 65: 
                        case 98: 
                        case 104: {
                            break;
                        }
                        default: {
                            this.jj_la1[35] = this.jj_gen;
                            ** GOTO lbl426
                        }
                    }
                    switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                        case 104: {
                            this.jj_consume_token(104);
                            s = this.Primary();
                            if (s == null || s.length() <= 0) continue block109;
                            try {
                                a = new Double(s);
                                al.add(a);
                            }
                            catch (Exception e) {
                                this.setError("Unexpected non-numeric value: " + e.getMessage());
                                al.add(new Double(0.0));
                            }
                            continue block109;
                        }
                        case 4: 
                        case 8: 
                        case 19: 
                        case 20: 
                        case 21: 
                        case 22: 
                        case 23: 
                        case 31: 
                        case 36: 
                        case 38: 
                        case 39: 
                        case 40: 
                        case 41: 
                        case 42: 
                        case 43: 
                        case 44: 
                        case 45: 
                        case 46: 
                        case 47: 
                        case 48: 
                        case 49: 
                        case 50: 
                        case 51: 
                        case 52: 
                        case 53: 
                        case 54: 
                        case 55: 
                        case 56: 
                        case 57: 
                        case 58: 
                        case 59: 
                        case 60: 
                        case 61: 
                        case 62: 
                        case 63: 
                        case 64: 
                        case 65: 
                        case 98: {
                            d = this.NumericExpression();
                            al.add(d);
                            continue block109;
                        }
                    }
                    break;
                }
                this.jj_la1[36] = this.jj_gen;
                this.jj_consume_token(-1);
                throw new ParseException();
lbl426:
                // 1 sources

                this.jj_consume_token(37);
                if (ep == null) {
                    ep = this.buildExpParam(al);
                }
                return CalcFunctions.min(ep);
            }
            case 21: {
                this.jj_consume_token(21);
                this.jj_consume_token(36);
                a = this.getObject1();
                if (a instanceof ExpressionParam) {
                    ep = (ExpressionParam)a;
                } else {
                    al = this.createArray(a);
                }
                block110: while (true) {
                    switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                        case 4: 
                        case 8: 
                        case 19: 
                        case 20: 
                        case 21: 
                        case 22: 
                        case 23: 
                        case 31: 
                        case 36: 
                        case 38: 
                        case 39: 
                        case 40: 
                        case 41: 
                        case 42: 
                        case 43: 
                        case 44: 
                        case 45: 
                        case 46: 
                        case 47: 
                        case 48: 
                        case 49: 
                        case 50: 
                        case 51: 
                        case 52: 
                        case 53: 
                        case 54: 
                        case 55: 
                        case 56: 
                        case 57: 
                        case 58: 
                        case 59: 
                        case 60: 
                        case 61: 
                        case 62: 
                        case 63: 
                        case 64: 
                        case 65: 
                        case 98: 
                        case 104: {
                            break;
                        }
                        default: {
                            this.jj_la1[37] = this.jj_gen;
                            ** GOTO lbl473
                        }
                    }
                    switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                        case 104: {
                            this.jj_consume_token(104);
                            s = this.Primary();
                            if (s == null || s.length() <= 0) continue block110;
                            try {
                                a = new Double(s);
                                al.add(a);
                            }
                            catch (Exception e) {
                                this.setError("Unexpected non-numeric value: " + e.getMessage());
                                al.add(new Double(0.0));
                            }
                            continue block110;
                        }
                        case 4: 
                        case 8: 
                        case 19: 
                        case 20: 
                        case 21: 
                        case 22: 
                        case 23: 
                        case 31: 
                        case 36: 
                        case 38: 
                        case 39: 
                        case 40: 
                        case 41: 
                        case 42: 
                        case 43: 
                        case 44: 
                        case 45: 
                        case 46: 
                        case 47: 
                        case 48: 
                        case 49: 
                        case 50: 
                        case 51: 
                        case 52: 
                        case 53: 
                        case 54: 
                        case 55: 
                        case 56: 
                        case 57: 
                        case 58: 
                        case 59: 
                        case 60: 
                        case 61: 
                        case 62: 
                        case 63: 
                        case 64: 
                        case 65: 
                        case 98: {
                            d = this.NumericExpression();
                            al.add(d);
                            continue block110;
                        }
                    }
                    break;
                }
                this.jj_la1[38] = this.jj_gen;
                this.jj_consume_token(-1);
                throw new ParseException();
lbl473:
                // 1 sources

                this.jj_consume_token(37);
                if (ep == null) {
                    ep = this.buildExpParam(al);
                }
                return CalcFunctions.max(ep);
            }
        }
        this.jj_la1[39] = this.jj_gen;
        this.jj_consume_token(-1);
        throw new ParseException();
    }

    public final ArrayList createArray(Object a) throws ParseException {
        ArrayList<Object> al = new ArrayList<Object>();
        Object[] l = a != null && a.getClass().isArray() ? (Object[])a : new Object[]{a};
        if (l != null) {
            for (int i = 0; i < l.length; ++i) {
                if (l[i] == null || l[i].toString().equals("")) continue;
                al.add(l[i]);
            }
        }
        return al;
    }

    public final ExpressionParam buildExpParam(ArrayList al) throws ParseException {
        Object[] l = new Object[al.size()];
        for (int i = 0; i < al.size(); ++i) {
            l[i] = al.get(i);
        }
        ExpressionParam ep = new ExpressionParam("param1", 0, l);
        return ep;
    }

    public final Double NumericFunction() throws ParseException {
        double value = 0.0;
        ArrayList al = new ArrayList();
        try {
            switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                case 19: {
                    this.jj_consume_token(19);
                    this.jj_consume_token(36);
                    Double a = this.NumericExpression();
                    this.jj_consume_token(37);
                    return new Double(Math.cos(BigDecimal.valueOf(a).doubleValue()));
                }
                case 20: {
                    this.jj_consume_token(20);
                    this.jj_consume_token(36);
                    Double a = this.NumericExpression();
                    this.jj_consume_token(37);
                    return new Double(Math.sin(BigDecimal.valueOf(a).doubleValue()));
                }
                case 50: {
                    this.jj_consume_token(50);
                    this.jj_consume_token(36);
                    Double a = this.NumericExpression();
                    this.jj_consume_token(37);
                    return new Double(Math.tan(BigDecimal.valueOf(a).doubleValue()));
                }
                case 39: {
                    this.jj_consume_token(39);
                    this.jj_consume_token(36);
                    Double a = this.NumericExpression();
                    this.jj_consume_token(37);
                    return new Double(CalcFunctions.exp(a));
                }
                case 40: {
                    this.jj_consume_token(40);
                    this.jj_consume_token(36);
                    Double a = this.NumericExpression();
                    this.jj_consume_token(37);
                    return new Double(CalcFunctions.factorial(a));
                }
                case 41: {
                    this.jj_consume_token(41);
                    this.jj_consume_token(36);
                    Double a = this.NumericExpression();
                    this.jj_consume_token(37);
                    return new Double(Math.abs(BigDecimal.valueOf(a).doubleValue()));
                }
                case 42: {
                    this.jj_consume_token(42);
                    this.jj_consume_token(36);
                    Double a = this.NumericExpression();
                    this.jj_consume_token(37);
                    return new Double(Math.ceil(BigDecimal.valueOf(a).doubleValue()));
                }
                case 43: {
                    this.jj_consume_token(43);
                    this.jj_consume_token(36);
                    Double a = this.NumericExpression();
                    this.jj_consume_token(37);
                    return new Double(a.intValue());
                }
                case 44: {
                    this.jj_consume_token(44);
                    this.jj_consume_token(36);
                    Double a = this.NumericExpression();
                    this.jj_consume_token(37);
                    return new Double(CalcFunctions.log(a));
                }
                case 45: {
                    this.jj_consume_token(45);
                    this.jj_consume_token(36);
                    Double a = this.NumericExpression();
                    this.jj_consume_token(104);
                    Double b = this.NumericExpression();
                    this.jj_consume_token(37);
                    return new Double(CalcFunctions.mod(a, b));
                }
                case 46: {
                    this.jj_consume_token(46);
                    this.jj_consume_token(36);
                    Double a = this.NumericExpression();
                    this.jj_consume_token(37);
                    return new Double(Math.PI * BigDecimal.valueOf(a).doubleValue());
                }
                case 47: {
                    this.jj_consume_token(47);
                    this.jj_consume_token(36);
                    Double a = this.NumericExpression();
                    this.jj_consume_token(37);
                    return new Double(CalcFunctions.random(a));
                }
                case 48: {
                    this.jj_consume_token(48);
                    this.jj_consume_token(36);
                    Double a = this.NumericExpression();
                    this.jj_consume_token(104);
                    Double b = this.NumericExpression();
                    this.jj_consume_token(37);
                    return new Double(CalcFunctions.round(a, b));
                }
                case 49: {
                    this.jj_consume_token(49);
                    this.jj_consume_token(36);
                    Double a = this.NumericExpression();
                    this.jj_consume_token(37);
                    return new Double(CalcFunctions.sign(a));
                }
                case 51: {
                    this.jj_consume_token(51);
                    this.jj_consume_token(36);
                    Double a = this.NumericExpression();
                    this.jj_consume_token(37);
                    a = BigDecimal.valueOf(a).doubleValue() <= 0.0 ? new Double(0.0) : new Double(Math.sqrt(BigDecimal.valueOf(a).doubleValue()));
                    return a;
                }
                case 52: {
                    this.jj_consume_token(52);
                    this.jj_consume_token(36);
                    Double a = this.NumericExpression();
                    this.jj_consume_token(104);
                    Double b = this.NumericExpression();
                    this.jj_consume_token(37);
                    return new Double(CalcFunctions.truncate(a, b));
                }
                case 53: {
                    this.jj_consume_token(53);
                    this.jj_consume_token(36);
                    Double a = this.NumericExpression();
                    this.jj_consume_token(37);
                    return new Double(CalcFunctions.logten(a));
                }
                case 61: {
                    this.jj_consume_token(61);
                    this.jj_consume_token(36);
                    Double a = this.NumericExpression();
                    this.jj_consume_token(104);
                    Double b = this.NumericExpression();
                    this.jj_consume_token(37);
                    return new Double(IndustryFunctions.sigfig(BigDecimal.valueOf(a).doubleValue(), b.intValue()));
                }
                case 62: {
                    this.jj_consume_token(62);
                    this.jj_consume_token(36);
                    Double a = this.NumericExpression();
                    this.jj_consume_token(104);
                    Double b = this.NumericExpression();
                    this.jj_consume_token(37);
                    return new Double(IndustryFunctions.clporgsigfig(BigDecimal.valueOf(a).doubleValue(), b.intValue()));
                }
                case 63: {
                    this.jj_consume_token(63);
                    this.jj_consume_token(36);
                    Double a = this.NumericExpression();
                    this.jj_consume_token(104);
                    Double b = this.NumericExpression();
                    this.jj_consume_token(37);
                    return new Double(IndustryFunctions.clpinorgsigfig(BigDecimal.valueOf(a).doubleValue(), b.intValue()));
                }
                case 64: {
                    this.jj_consume_token(64);
                    this.jj_consume_token(36);
                    Double a = this.NumericExpression();
                    this.jj_consume_token(104);
                    Double b = this.NumericExpression();
                    this.jj_consume_token(37);
                    return new Double(IndustryFunctions.astmround(BigDecimal.valueOf(a).doubleValue(), b.intValue()));
                }
                case 65: {
                    this.jj_consume_token(65);
                    this.jj_consume_token(36);
                    Double a = this.NumericExpression();
                    this.jj_consume_token(104);
                    Double b = this.NumericExpression();
                    this.jj_consume_token(37);
                    return new Double(IndustryFunctions.maxsigfigdp(BigDecimal.valueOf(a).doubleValue(), b.intValue()));
                }
            }
            this.jj_la1[40] = this.jj_gen;
            this.jj_consume_token(-1);
            throw new ParseException();
        }
        catch (Exception e) {
            this.setError("Error in NumericFunction: " + e.getMessage());
            return null;
        }
    }

    /*
     * Unable to fully structure code
     */
    public final String StringExpression() throws ParseException {
        switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
            case 7: 
            case 24: 
            case 25: 
            case 66: 
            case 67: 
            case 68: 
            case 69: 
            case 70: 
            case 71: 
            case 72: 
            case 73: 
            case 74: 
            case 75: 
            case 76: 
            case 77: 
            case 78: 
            case 79: 
            case 80: 
            case 81: 
            case 82: 
            case 83: 
            case 84: 
            case 85: 
            case 86: 
            case 87: 
            case 88: 
            case 89: 
            case 90: 
            case 91: 
            case 92: 
            case 93: 
            case 94: 
            case 95: 
            case 96: 
            case 97: 
            case 99: {
                switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                    case 7: {
                        t = this.jj_consume_token(7);
                        returnStr = t.toString();
                        returnStr = returnStr.substring(1, returnStr.length() - 1);
                        break;
                    }
                    case 24: 
                    case 25: 
                    case 83: 
                    case 84: 
                    case 85: 
                    case 86: 
                    case 87: 
                    case 88: 
                    case 89: 
                    case 90: 
                    case 91: 
                    case 92: 
                    case 93: 
                    case 94: 
                    case 95: 
                    case 96: 
                    case 97: 
                    case 99: {
                        returnStr = this.StringFunction();
                        break;
                    }
                    case 66: 
                    case 67: 
                    case 68: 
                    case 69: 
                    case 70: 
                    case 71: 
                    case 72: 
                    case 73: 
                    case 74: 
                    case 75: 
                    case 76: 
                    case 77: 
                    case 78: 
                    case 79: 
                    case 80: 
                    case 81: 
                    case 82: {
                        returnStr = this.DateTimeFunction();
                        break;
                    }
                    default: {
                        this.jj_la1[41] = this.jj_gen;
                        this.jj_consume_token(-1);
                        throw new ParseException();
                    }
                }
                block25: while (true) {
                    switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                        case 30: {
                            break;
                        }
                        default: {
                            this.jj_la1[42] = this.jj_gen;
                            break block25;
                        }
                    }
                    this.jj_consume_token(30);
                    s = this.StringExpression();
                    returnStr = returnStr + s;
                }
                return returnStr;
            }
            case 4: 
            case 8: 
            case 19: 
            case 20: 
            case 21: 
            case 22: 
            case 23: 
            case 31: 
            case 36: 
            case 38: 
            case 39: 
            case 40: 
            case 41: 
            case 42: 
            case 43: 
            case 44: 
            case 45: 
            case 46: 
            case 47: 
            case 48: 
            case 49: 
            case 50: 
            case 51: 
            case 52: 
            case 53: 
            case 54: 
            case 55: 
            case 56: 
            case 57: 
            case 58: 
            case 59: 
            case 60: 
            case 61: 
            case 62: 
            case 63: 
            case 64: 
            case 65: 
            case 98: 
            case 100: {
                value = 0.0;
                nfmt = NumberFormat.getNumberInstance(Locale.US);
                frdigits = 0;
                decimalpos = 0;
                doFormat = false;
                rstring = this.Term();
                try {
                    value = new BigDecimal(rstring).doubleValue();
                }
                catch (Exception var11_11) {
                    // empty catch block
                }
                decimalpos = rstring.indexOf(".");
                if (decimalpos != -1) {
                    frdigits = rstring.length() - ++decimalpos;
                }
                block26: while (true) {
                    switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                        case 30: 
                        case 31: {
                            break;
                        }
                        default: {
                            this.jj_la1[43] = this.jj_gen;
                            ** GOTO lbl104
                        }
                    }
                    switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                        case 30: {
                            this.jj_consume_token(30);
                            s = this.Term();
                            if (!rstring.equals("")) {
                                try {
                                    value = BigDecimal.valueOf(value).add(new BigDecimal(s)).doubleValue();
                                    if (Math.abs(value) == 0.0) {
                                        value = 0.0;
                                    }
                                    rstring = new Double(value).toString();
                                    decimalpos = s.indexOf(".");
                                    if (decimalpos != -1 && s.length() - ++decimalpos > frdigits) {
                                        frdigits = s.length() - decimalpos;
                                    }
                                    doFormat = true;
                                }
                                catch (Exception e) {
                                    this.setError("Unexpected non-numeric value: " + e.getMessage());
                                    if (!s.equals("")) continue block26;
                                    rstring = "";
                                }
                                continue block26;
                            }
                            this.nullInArithmetic = true;
                            continue block26;
                        }
                        case 31: {
                            this.jj_consume_token(31);
                            s = this.Term();
                            if (!rstring.equals("")) {
                                try {
                                    value = BigDecimal.valueOf(value).subtract(new BigDecimal(s)).doubleValue();
                                    if (Math.abs(value) == 0.0) {
                                        value = 0.0;
                                    }
                                    rstring = new Double(value).toString();
                                    decimalpos = s.indexOf(".");
                                    if (decimalpos != -1 && s.length() - ++decimalpos > frdigits) {
                                        frdigits = s.length() - decimalpos;
                                    }
                                    doFormat = true;
                                }
                                catch (Exception e) {
                                    this.setError("Unexpected non-numeric value: " + e.getMessage());
                                    if (!s.equals("")) continue block26;
                                    rstring = "";
                                }
                                continue block26;
                            }
                            this.nullInArithmetic = true;
                            continue block26;
                        }
                    }
                    break;
                }
                this.jj_la1[44] = this.jj_gen;
                this.jj_consume_token(-1);
                throw new ParseException();
lbl104:
                // 1 sources

                if (doFormat) {
                    if (rstring.indexOf("E") >= 0) {
                        rstring = new BigDecimal(rstring).stripTrailingZeros().toPlainString();
                    }
                    if ((idx = rstring.indexOf(".")) != -1) {
                        if (!(new Double(rstring.substring(idx)) > 0.0)) {
                            rstring = rstring.substring(0, idx);
                        } else {
                            nfmt.setMinimumFractionDigits(0);
                            nfmt.setMaximumFractionDigits(frdigits);
                            nfmt.setGroupingUsed(false);
                            rstring = nfmt.format(new Double(rstring));
                        }
                    }
                }
                return rstring;
            }
        }
        this.jj_la1[45] = this.jj_gen;
        this.jj_consume_token(-1);
        throw new ParseException();
    }

    public final String Term() throws ParseException {
        boolean doFormat;
        String rstring;
        block30: {
            rstring = "";
            String s = "";
            double value = 0.0;
            boolean frdigits = false;
            boolean decimalpos = false;
            doFormat = false;
            rstring = this.Power();
            try {
                value = new BigDecimal(rstring).doubleValue();
            }
            catch (Exception exception) {
                // empty catch block
            }
            block16: while (true) {
                switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                    case 32: 
                    case 33: 
                    case 34: {
                        break;
                    }
                    default: {
                        this.jj_la1[46] = this.jj_gen;
                        break block30;
                    }
                }
                switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                    case 32: {
                        this.jj_consume_token(32);
                        s = this.Power();
                        if (!rstring.equals("")) {
                            try {
                                value = BigDecimal.valueOf(value).multiply(new BigDecimal(s)).doubleValue();
                                if (Math.abs(value) == 0.0) {
                                    value = 0.0;
                                }
                                rstring = new Double(value).toString();
                                doFormat = true;
                            }
                            catch (Exception e) {
                                this.setError("Unexpected non-numeric value: " + e.getMessage());
                                if (s.equals("")) {
                                    rstring = "";
                                    continue block16;
                                }
                                rstring = "0";
                            }
                            continue block16;
                        }
                        this.nullInArithmetic = true;
                        continue block16;
                    }
                    case 33: {
                        this.jj_consume_token(33);
                        s = this.Power();
                        if (!rstring.equals("")) {
                            try {
                                if (new Double(s) == 0.0) {
                                    rstring = "";
                                    continue block16;
                                }
                                if (Math.abs(value = BigDecimal.valueOf(value).divide(new BigDecimal(s), MathContext.DECIMAL128).doubleValue()) == 0.0) {
                                    value = 0.0;
                                }
                                rstring = new Double(value).toString();
                                doFormat = true;
                            }
                            catch (Exception e) {
                                this.setError("Unexpected non-numeric value: " + e.getMessage());
                                if (s.equals("")) {
                                    rstring = "";
                                    continue block16;
                                }
                                rstring = "0";
                            }
                            continue block16;
                        }
                        this.nullInArithmetic = true;
                        continue block16;
                    }
                    case 34: {
                        this.jj_consume_token(34);
                        s = this.Power();
                        if (!rstring.equals("")) {
                            try {
                                value = BigDecimal.valueOf(value).remainder(new BigDecimal(s)).doubleValue();
                                if (Math.abs(value) == 0.0) {
                                    value = 0.0;
                                }
                                rstring = new Double(value).toString();
                                doFormat = true;
                            }
                            catch (Exception e) {
                                this.setError("Unexpected non-numeric value: " + e.getMessage());
                                if (s.equals("")) {
                                    rstring = "";
                                    continue block16;
                                }
                                rstring = "0";
                            }
                            continue block16;
                        }
                        this.nullInArithmetic = true;
                        continue block16;
                    }
                }
                break;
            }
            this.jj_la1[47] = this.jj_gen;
            this.jj_consume_token(-1);
            throw new ParseException();
        }
        if (doFormat) {
            int idx;
            if (rstring.indexOf("E") >= 0) {
                rstring = new BigDecimal(rstring).stripTrailingZeros().toPlainString();
            }
            if ((idx = rstring.indexOf(".")) != -1 && !(new Double(rstring.substring(idx)) > 0.0)) {
                rstring = rstring.substring(0, idx);
            }
        }
        return rstring;
    }

    public final String Power() throws ParseException {
        String rstring = "";
        String s = "";
        double value = 0.0;
        boolean frdigits = false;
        boolean decimalpos = false;
        boolean doFormat = false;
        rstring = this.Unary();
        try {
            value = new BigDecimal(rstring).doubleValue();
        }
        catch (Exception exception) {
            // empty catch block
        }
        block7: while (true) {
            switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                case 35: {
                    break;
                }
                default: {
                    this.jj_la1[48] = this.jj_gen;
                    break block7;
                }
            }
            this.jj_consume_token(35);
            s = this.Unary();
            if (!rstring.equals("")) {
                try {
                    value = Math.pow(value, new BigDecimal(s).doubleValue());
                    if (Math.abs(value) == 0.0) {
                        value = 0.0;
                    }
                    rstring = new Double(value).toString();
                    doFormat = true;
                }
                catch (Exception e) {
                    this.setError("Unexpected non-numeric value: " + e.getMessage());
                    if (s.equals("")) {
                        rstring = "";
                        continue;
                    }
                    rstring = "0";
                }
                continue;
            }
            this.nullInArithmetic = true;
        }
        if (doFormat) {
            int idx;
            if (rstring.indexOf("E") >= 0) {
                rstring = new BigDecimal(rstring).stripTrailingZeros().toPlainString();
            }
            if ((idx = rstring.indexOf(".")) != -1 && !(new Double(rstring.substring(idx)) > 0.0)) {
                rstring = rstring.substring(0, idx);
            }
        }
        return rstring;
    }

    public final String Unary() throws ParseException {
        String rstring = "";
        String s = "";
        double value = 0.0;
        boolean frdigits = false;
        boolean decimalpos = false;
        boolean doFormat = false;
        switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
            case 31: {
                this.jj_consume_token(31);
                s = this.Primary();
                try {
                    value = new BigDecimal(s).multiply(new BigDecimal("-1")).doubleValue();
                    if (Math.abs(value) == 0.0) {
                        value = 0.0;
                    }
                    rstring = new Double(value).toString();
                    doFormat = true;
                }
                catch (Exception e) {
                    this.setError("Unexpected non-numeric value: " + e.getMessage());
                    if (s.equals("")) {
                        rstring = "";
                        break;
                    }
                    rstring = "0";
                }
                break;
            }
            case 4: 
            case 7: 
            case 8: 
            case 19: 
            case 20: 
            case 21: 
            case 22: 
            case 23: 
            case 36: 
            case 38: 
            case 39: 
            case 40: 
            case 41: 
            case 42: 
            case 43: 
            case 44: 
            case 45: 
            case 46: 
            case 47: 
            case 48: 
            case 49: 
            case 50: 
            case 51: 
            case 52: 
            case 53: 
            case 54: 
            case 55: 
            case 56: 
            case 57: 
            case 58: 
            case 59: 
            case 60: 
            case 61: 
            case 62: 
            case 63: 
            case 64: 
            case 65: 
            case 98: 
            case 100: {
                rstring = s = this.Primary();
                break;
            }
            default: {
                this.jj_la1[49] = this.jj_gen;
                this.jj_consume_token(-1);
                throw new ParseException();
            }
        }
        if (doFormat) {
            int idx;
            if (rstring.indexOf("E") >= 0) {
                rstring = new BigDecimal(rstring).stripTrailingZeros().toPlainString();
            }
            if ((idx = rstring.indexOf(".")) != -1 && !(new Double(rstring.substring(idx)) > 0.0)) {
                rstring = rstring.substring(0, idx);
            }
        }
        return rstring;
    }

    public final String Primary() throws ParseException {
        String s = "";
        String rstring = "";
        int idx = 0;
        SimpleDateFormat df = new SimpleDateFormat(DateFormatter.getJavaCompatibleFormatString(""));
        switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
            case 7: {
                Token t = this.jj_consume_token(7);
                s = t.toString();
                s = s.substring(1, s.length() - 1);
                return s;
            }
            case 4: {
                Token t = this.jj_consume_token(4);
                s = t.toString();
                return s;
            }
            case 19: 
            case 20: 
            case 39: 
            case 40: 
            case 41: 
            case 42: 
            case 43: 
            case 44: 
            case 45: 
            case 46: 
            case 47: 
            case 48: 
            case 49: 
            case 50: 
            case 51: 
            case 52: 
            case 53: 
            case 61: 
            case 62: 
            case 63: 
            case 64: 
            case 65: {
                Double a = this.NumericFunction();
                try {
                    rstring = a.toString();
                    if (rstring.indexOf("E") >= 0) {
                        rstring = new BigDecimal(rstring).stripTrailingZeros().toPlainString();
                    }
                    if ((idx = rstring.indexOf(".")) != -1 && !(new Double(rstring.substring(idx)) > 0.0)) {
                        rstring = rstring.substring(0, idx);
                    }
                }
                catch (Exception e) {
                    this.setError("Error in Primary: " + e.getMessage());
                    rstring = "";
                }
                return rstring;
            }
            case 98: {
                s = this.IfStatement();
                if (!this.rtypeString) {
                    try {
                        double value = new BigDecimal(s).doubleValue();
                        if (Math.abs(value) == 0.0) {
                            value = 0.0;
                        }
                        if ((rstring = new Double(value).toString()).indexOf("E") >= 0) {
                            rstring = new BigDecimal(rstring).stripTrailingZeros().toPlainString();
                        }
                        if ((idx = rstring.indexOf(".")) != -1 && !(new Double(rstring.substring(idx)) > 0.0)) {
                            rstring = rstring.substring(0, idx);
                        }
                        s = rstring;
                    }
                    catch (Exception e) {
                        // empty catch block
                    }
                }
                return s;
            }
            case 100: {
                s = this.CaseStatement();
                try {
                    double value = new BigDecimal(s).doubleValue();
                    if (Math.abs(value) == 0.0) {
                        value = 0.0;
                    }
                    if ((rstring = new Double(value).toString()).indexOf("E") >= 0) {
                        rstring = new BigDecimal(rstring).stripTrailingZeros().toPlainString();
                    }
                    if ((idx = rstring.indexOf(".")) != -1 && !(new Double(rstring.substring(idx)) > 0.0)) {
                        rstring = rstring.substring(0, idx);
                    }
                    s = rstring;
                }
                catch (Exception e) {
                    // empty catch block
                }
                return s;
            }
            case 21: 
            case 22: 
            case 23: 
            case 38: 
            case 54: 
            case 55: 
            case 56: 
            case 57: 
            case 58: 
            case 59: 
            case 60: {
                Object o = this.MultiObjectListExpression();
                if (o instanceof Calendar) {
                    Calendar c = (Calendar)o;
                    s = df.format(c.getTime());
                    return s;
                }
                try {
                    s = o.toString();
                    if (s.indexOf("E") >= 0) {
                        s = new BigDecimal(s).stripTrailingZeros().toPlainString();
                    }
                    if ((idx = s.indexOf(".")) != -1 && !(new Double(s.substring(idx)) > 0.0)) {
                        s = s.substring(0, idx);
                    }
                }
                catch (Exception c) {
                    // empty catch block
                }
                return s;
            }
            case 8: {
                Token t = this.jj_consume_token(8);
                s = t.toString();
                s = s.substring(1, s.length() - 1);
                Object obj = this.params.get(s);
                if (this.params.get(s) instanceof BigDecimal) {
                    rstring = ((BigDecimal)this.params.get(s)).toPlainString();
                } else if (obj instanceof Object[]) {
                    rstring = "";
                } else {
                    try {
                        rstring = obj.toString();
                    }
                    catch (Exception e) {
                        rstring = "";
                    }
                }
                return rstring;
            }
            case 36: {
                this.jj_consume_token(36);
                rstring = this.StringExpression();
                this.jj_consume_token(37);
                return rstring;
            }
        }
        this.jj_la1[50] = this.jj_gen;
        this.jj_consume_token(-1);
        throw new ParseException();
    }

    public final String[] StringList() throws ParseException {
        String[] rstring = null;
        Token t = this.jj_consume_token(8);
        String s = t.toString();
        if (this.params.get(s = s.substring(1, s.length() - 1)) instanceof ExpressionParam) {
            ExpressionParam ep = (ExpressionParam)this.params.get(s);
            String[] stringArray = ep.getStringValues();
        } else {
            try {
                Object[] obj = (Object[])this.params.get(s);
                rstring = new String[obj.length];
                try {
                    for (int i = 0; i < obj.length; ++i) {
                        rstring[i] = obj[i].toString();
                    }
                }
                catch (Exception i) {
                }
            }
            catch (Exception e) {
                rstring = new String[1];
                try {
                    rstring[0] = this.params.get(s).toString();
                }
                catch (Exception ee) {
                    rstring[0] = "";
                }
            }
        }
        return rstring;
    }

    public final String DateTimeFunction() throws ParseException {
        switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
            case 66: {
                this.jj_consume_token(66);
                this.jj_consume_token(36);
                String dt1 = this.Primary();
                this.jj_consume_token(104);
                String f = this.Primary();
                this.jj_consume_token(37);
                return DateFunctions.getDate(dt1, f);
            }
            case 67: {
                this.jj_consume_token(67);
                this.jj_consume_token(36);
                String dt1 = this.Primary();
                this.jj_consume_token(104);
                String f = this.Primary();
                this.jj_consume_token(37);
                return DateFunctions.getTime(dt1, f);
            }
            case 68: {
                this.jj_consume_token(68);
                this.jj_consume_token(36);
                String dt1 = this.Primary();
                this.jj_consume_token(104);
                String f = this.Primary();
                this.jj_consume_token(37);
                return DateFunctions.getDateTime(dt1, f);
            }
            case 69: {
                this.jj_consume_token(69);
                this.jj_consume_token(36);
                String dt1 = this.Primary();
                this.jj_consume_token(104);
                String f = this.Primary();
                this.jj_consume_token(37);
                return DateFunctions.getDay(dt1, f);
            }
            case 70: {
                this.jj_consume_token(70);
                this.jj_consume_token(36);
                String dt1 = this.Primary();
                this.jj_consume_token(104);
                String f = this.Primary();
                this.jj_consume_token(37);
                return DateFunctions.getDayName(dt1, f);
            }
            case 71: {
                this.jj_consume_token(71);
                this.jj_consume_token(36);
                String dt1 = this.Primary();
                this.jj_consume_token(104);
                String f = this.Primary();
                this.jj_consume_token(37);
                return DateFunctions.getDayNumber(dt1, f);
            }
            case 72: {
                this.jj_consume_token(72);
                this.jj_consume_token(36);
                String dt1 = this.Primary();
                this.jj_consume_token(104);
                String dt2 = this.Primary();
                this.jj_consume_token(104);
                String f = this.Primary();
                this.jj_consume_token(37);
                return DateFunctions.getDaysAfter(dt1, dt2, f);
            }
            case 73: {
                this.jj_consume_token(73);
                this.jj_consume_token(36);
                String dt1 = this.Primary();
                this.jj_consume_token(104);
                String f = this.Primary();
                this.jj_consume_token(37);
                return DateFunctions.getHour(dt1, f);
            }
            case 74: {
                this.jj_consume_token(74);
                this.jj_consume_token(36);
                String dt1 = this.Primary();
                this.jj_consume_token(104);
                String f = this.Primary();
                this.jj_consume_token(37);
                return DateFunctions.getMinute(dt1, f);
            }
            case 75: {
                this.jj_consume_token(75);
                this.jj_consume_token(36);
                String dt1 = this.Primary();
                this.jj_consume_token(104);
                String f = this.Primary();
                this.jj_consume_token(37);
                return DateFunctions.getMonth(dt1, f);
            }
            case 76: {
                this.jj_consume_token(76);
                this.jj_consume_token(36);
                String f = this.Primary();
                this.jj_consume_token(37);
                return DateFunctions.getCurrentTime(f);
            }
            case 77: {
                this.jj_consume_token(77);
                this.jj_consume_token(36);
                String dt1 = this.Primary();
                this.jj_consume_token(104);
                String num = this.Primary();
                this.jj_consume_token(104);
                String f = this.Primary();
                this.jj_consume_token(37);
                return DateFunctions.getRelativeDate(dt1, num, f);
            }
            case 78: {
                this.jj_consume_token(78);
                this.jj_consume_token(36);
                String dt1 = this.Primary();
                this.jj_consume_token(104);
                String num = this.Primary();
                this.jj_consume_token(104);
                String f = this.Primary();
                this.jj_consume_token(37);
                return DateFunctions.getRelativeTime(dt1, num, f);
            }
            case 79: {
                this.jj_consume_token(79);
                this.jj_consume_token(36);
                String dt1 = this.Primary();
                this.jj_consume_token(104);
                String f = this.Primary();
                this.jj_consume_token(37);
                return DateFunctions.getSecond(dt1, f);
            }
            case 80: {
                this.jj_consume_token(80);
                this.jj_consume_token(36);
                String dt1 = this.Primary();
                this.jj_consume_token(104);
                String dt2 = this.Primary();
                this.jj_consume_token(104);
                String f = this.Primary();
                this.jj_consume_token(37);
                return DateFunctions.getSecondsAfter(dt1, dt2, f);
            }
            case 81: {
                this.jj_consume_token(81);
                this.jj_consume_token(36);
                String f = this.Primary();
                this.jj_consume_token(37);
                return DateFunctions.getCurrentDate(f);
            }
            case 82: {
                this.jj_consume_token(82);
                this.jj_consume_token(36);
                String dt1 = this.Primary();
                this.jj_consume_token(104);
                String f = this.Primary();
                this.jj_consume_token(37);
                return DateFunctions.getYear(dt1, f);
            }
        }
        this.jj_la1[51] = this.jj_gen;
        this.jj_consume_token(-1);
        throw new ParseException();
    }

    public final String StringFunction() throws ParseException {
        switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
            case 25: {
                this.jj_consume_token(25);
                this.jj_consume_token(36);
                Double a = this.NumericExpression();
                this.jj_consume_token(37);
                return CalcFunctions.color(BigDecimal.valueOf(a).doubleValue());
            }
            case 83: {
                this.jj_consume_token(83);
                this.jj_consume_token(36);
                String s = this.StringExpression();
                this.jj_consume_token(37);
                return StringFunctions.leftTrim(s);
            }
            case 84: {
                this.jj_consume_token(84);
                this.jj_consume_token(36);
                String s = this.StringExpression();
                this.jj_consume_token(37);
                return StringFunctions.rightTrim(s);
            }
            case 85: {
                this.jj_consume_token(85);
                this.jj_consume_token(36);
                String s = this.StringExpression();
                this.jj_consume_token(37);
                return StringFunctions.trim(s);
            }
            case 86: {
                this.jj_consume_token(86);
                this.jj_consume_token(36);
                String s = this.StringExpression();
                this.jj_consume_token(104);
                Double a = this.NumericExpression();
                this.jj_consume_token(37);
                return StringFunctions.left(s, a.intValue());
            }
            case 87: {
                this.jj_consume_token(87);
                this.jj_consume_token(36);
                String s = this.StringExpression();
                this.jj_consume_token(104);
                Double a = this.NumericExpression();
                this.jj_consume_token(37);
                return StringFunctions.right(s, a.intValue());
            }
            case 88: {
                this.jj_consume_token(88);
                this.jj_consume_token(36);
                String s = this.StringExpression();
                this.jj_consume_token(104);
                Double a = this.NumericExpression();
                this.jj_consume_token(37);
                return StringFunctions.fill(s, a.intValue());
            }
            case 89: {
                this.jj_consume_token(89);
                this.jj_consume_token(36);
                String s = this.StringExpression();
                this.jj_consume_token(37);
                return new Integer(StringFunctions.len(s)).toString();
            }
            case 90: {
                this.jj_consume_token(90);
                this.jj_consume_token(36);
                String s = this.StringExpression();
                this.jj_consume_token(37);
                return StringFunctions.lower(s);
            }
            case 91: {
                this.jj_consume_token(91);
                this.jj_consume_token(36);
                String s = this.StringExpression();
                this.jj_consume_token(104);
                String p = this.StringExpression();
                this.jj_consume_token(37);
                return new Boolean(StringFunctions.match(s, p)).toString();
            }
            case 92: {
                Double m = null;
                Double n = null;
                this.jj_consume_token(92);
                this.jj_consume_token(36);
                String s = this.StringExpression();
                this.jj_consume_token(104);
                m = this.NumericExpression();
                switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                    case 104: {
                        this.jj_consume_token(104);
                        n = this.NumericExpression();
                        break;
                    }
                    default: {
                        this.jj_la1[52] = this.jj_gen;
                    }
                }
                this.jj_consume_token(37);
                if (n == null) {
                    return StringFunctions.mid(s, m.intValue());
                }
                return StringFunctions.mid(s, m.intValue(), n.intValue());
            }
            case 93: {
                this.jj_consume_token(93);
                this.jj_consume_token(36);
                String s = this.StringExpression();
                this.jj_consume_token(104);
                String s2 = this.StringExpression();
                this.jj_consume_token(37);
                return new Integer(StringFunctions.pos(s, s2)).toString();
            }
            case 94: {
                this.jj_consume_token(94);
                this.jj_consume_token(36);
                String s = this.StringExpression();
                this.jj_consume_token(104);
                String s2 = this.StringExpression();
                this.jj_consume_token(104);
                String s3 = this.StringExpression();
                this.jj_consume_token(37);
                return StringFunctions.replace(s, s2, s3);
            }
            case 95: {
                this.jj_consume_token(95);
                this.jj_consume_token(36);
                Double a = this.NumericExpression();
                this.jj_consume_token(37);
                return StringFunctions.space(a.intValue());
            }
            case 96: {
                this.jj_consume_token(96);
                this.jj_consume_token(36);
                String s = this.StringExpression();
                this.jj_consume_token(37);
                return StringFunctions.upper(s);
            }
            case 97: {
                this.jj_consume_token(97);
                this.jj_consume_token(36);
                String s = this.StringExpression();
                this.jj_consume_token(37);
                return StringFunctions.wordcap(s);
            }
            case 99: {
                this.jj_consume_token(99);
                this.jj_consume_token(36);
                String b = this.Expression();
                this.jj_consume_token(37);
                return StringFunctions.getString(b);
            }
            case 24: {
                ArrayList<Object> al = new ArrayList<Object>();
                this.jj_consume_token(24);
                this.jj_consume_token(36);
                String className = this.Primary();
                this.jj_consume_token(104);
                String methodName = this.Primary();
                block28: while (true) {
                    switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                        case 104: {
                            break;
                        }
                        default: {
                            this.jj_la1[53] = this.jj_gen;
                            break block28;
                        }
                    }
                    this.jj_consume_token(104);
                    Object obj = this.getObject();
                    if (obj == null) continue;
                    al.add(obj);
                }
                this.jj_consume_token(37);
                Object[] objArray = al.toArray();
                String s = CalcFunctions.external(className, methodName, objArray);
                if (s.indexOf("E") >= 0) {
                    s = new BigDecimal(s).stripTrailingZeros().toPlainString();
                }
                int idx = s.indexOf(".0");
                try {
                    if (idx != -1 && !(new Double(s.substring(idx)) > 0.0)) {
                        s = s.substring(0, idx);
                    }
                }
                catch (Exception exception) {
                    // empty catch block
                }
                return s;
            }
        }
        this.jj_la1[54] = this.jj_gen;
        this.jj_consume_token(-1);
        throw new ParseException();
    }

    public final String IfStatement() throws ParseException {
        Boolean bObj = null;
        boolean tnum = true;
        boolean fnum = true;
        boolean initError = false;
        String initMessage = "";
        boolean truePartError = false;
        String truePartMessage = "";
        boolean falsePartError = false;
        String falsePartMessage = "";
        this.rtypeString = false;
        try {
            this.jj_consume_token(98);
            this.jj_consume_token(36);
            bObj = this.BooleanExpression();
            this.jj_consume_token(104);
            if (this.getToken((int)1).image.startsWith("\"") || this.getToken((int)1).image.startsWith("string")) {
                tnum = false;
            }
            initError = this.errorFlag;
            initMessage = this.errorMessage;
            this.errorFlag = false;
            this.errorMessage = "";
            String t = this.Expression();
            this.jj_consume_token(104);
            truePartError = this.errorFlag;
            truePartMessage = this.errorMessage;
            this.errorFlag = false;
            this.errorMessage = "";
            if (this.rtypeString) {
                tnum = false;
                this.rtypeString = false;
            }
            if (this.getToken((int)1).image.startsWith("\"") || this.getToken((int)1).image.startsWith("string")) {
                fnum = false;
            }
            String f = this.Expression();
            this.jj_consume_token(37);
            falsePartError = this.errorFlag;
            falsePartMessage = this.errorMessage;
            this.errorFlag = false;
            this.errorMessage = "";
            if (this.rtypeString) {
                fnum = false;
            }
            if (this.errorInIF || this.honorNullInRelationalExpressions && (truePartError || falsePartError || this.nullInArithmetic)) {
                this.errorFlag = true;
                this.errorMessage = truePartError ? truePartMessage : falsePartMessage;
                return "";
            }
            if (bObj == null) {
                this.setError("Error in IF Statement: Null encountered in Boolean Expression.");
                this.errorInIF = true;
                return "";
            }
            if (this.honorNullInRelationalExpressions) {
                if (this.honorNullInANDORConditionals) {
                    Double a;
                    if (tnum) {
                        try {
                            a = new Double(t);
                        }
                        catch (Exception e) {
                            tnum = false;
                        }
                    }
                    if (fnum) {
                        try {
                            a = new Double(f);
                        }
                        catch (Exception e) {
                            fnum = false;
                        }
                    }
                }
            } else {
                Double a;
                if (tnum) {
                    try {
                        a = new Double(t);
                    }
                    catch (Exception e) {
                        tnum = false;
                    }
                }
                if (fnum) {
                    try {
                        a = new Double(f);
                    }
                    catch (Exception e) {
                        fnum = false;
                    }
                }
            }
            if (tnum != fnum && t != null && f != null && t.length() > 0 && f.length() > 0) {
                throw new ParseException();
            }
            if (bObj.booleanValue()) {
                boolean bl = this.rtypeString = !tnum;
                if (truePartError) {
                    this.errorFlag = truePartError;
                    this.errorMessage = truePartMessage;
                } else {
                    this.errorFlag = initError;
                    this.errorMessage = initMessage;
                }
            } else {
                boolean bl = this.rtypeString = !fnum;
                if (falsePartError) {
                    this.errorFlag = falsePartError;
                    this.errorMessage = falsePartMessage;
                } else {
                    this.errorFlag = initError;
                    this.errorMessage = initMessage;
                }
            }
            return bObj != false ? t : f;
        }
        catch (Exception e) {
            this.setError("Error in If Statement (possibly the result of different data types for the true and false clauses): " + e.getMessage());
            this.errorInIF = true;
            return "";
        }
    }

    public final String CaseStatement() throws ParseException {
        Double switchVal = null;
        String returnVal = "";
        boolean returnValFound = false;
        try {
            Double a;
            this.jj_consume_token(100);
            this.jj_consume_token(36);
            Token t = this.jj_consume_token(8);
            String s = t.toString();
            s = this.params.get(s.substring(1, s.length() - 1)).toString();
            try {
                switchVal = new Double(s);
            }
            catch (Exception e) {
                throw new ParseException();
            }
            block7: while (true) {
                switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                    case 101: {
                        break;
                    }
                    default: {
                        this.jj_la1[55] = this.jj_gen;
                        break block7;
                    }
                }
                this.jj_consume_token(101);
                Double caseVal = this.NumericExpression();
                this.jj_consume_token(102);
                a = this.NumericExpression();
                if (!switchVal.equals(caseVal) || returnValFound) continue;
                returnVal = a.toString();
                returnValFound = true;
            }
            this.jj_consume_token(103);
            a = this.NumericExpression();
            if (!returnValFound) {
                returnVal = a.toString();
            }
            this.jj_consume_token(37);
            return returnVal;
        }
        catch (Exception e) {
            this.setError("Error in CaseStatement: " + e.getMessage());
            return "";
        }
    }

    public final Object getObject() throws ParseException {
        Token t = this.jj_consume_token(8);
        String s = t.toString();
        s = s.substring(1, s.length() - 1);
        Object obj = this.params.get(s);
        return obj;
    }

    public final Object getObject1() throws ParseException {
        try {
            Token t = this.getToken(1);
            if (t.kind == 8) {
                String s = t.toString();
                Object obj = this.params.get(s = s.substring(1, s.length() - 1));
                if (obj instanceof ExpressionParam || obj instanceof Object[]) {
                    t = this.getNextToken();
                    return obj;
                }
                return null;
            }
            return null;
        }
        catch (Exception exception) {
            throw new Error("Missing return statement in function");
        }
    }

    public final String getObjectType() throws ParseException {
        try {
            Token t = this.getToken(1);
            if (t.kind == 8) {
                String s = t.toString();
                Object obj = this.params.get(s = s.substring(1, s.length() - 1));
                if (obj instanceof ExpressionParam) {
                    return "ExpressionParam";
                }
                if (obj instanceof Object[]) {
                    return "ObjectArray";
                }
                return "SimpleParam";
            }
            return "Other";
        }
        catch (Exception exception) {
            throw new Error("Missing return statement in function");
        }
    }

    public final ArrayList getParsedArray(Object a) throws ParseException {
        ArrayList<Object> a2 = new ArrayList<Object>();
        switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
            case 30: {
                this.jj_consume_token(30);
                Double d = this.NumericExpression();
                Object[] l = (Object[])a;
                for (int i = 0; i < l.length; ++i) {
                    if (l[i] == null || l[i].toString().equals("")) continue;
                    a2.add(new Double(((BigDecimal)l[i]).add(BigDecimal.valueOf(d)).doubleValue()));
                }
                break;
            }
            case 31: {
                this.jj_consume_token(31);
                Double d = this.NumericExpression();
                Object[] l = (Object[])a;
                for (int i = 0; i < l.length; ++i) {
                    if (l[i] == null || l[i].toString().equals("")) continue;
                    a2.add(new Double(((BigDecimal)l[i]).subtract(BigDecimal.valueOf(d)).doubleValue()));
                }
                break;
            }
            case 32: {
                this.jj_consume_token(32);
                Double d = this.NumericExpression();
                Object[] l = (Object[])a;
                for (int i = 0; i < l.length; ++i) {
                    if (l[i] == null || l[i].toString().equals("")) continue;
                    a2.add(new Double(((BigDecimal)l[i]).multiply(BigDecimal.valueOf(d)).doubleValue()));
                }
                break;
            }
            case 33: {
                this.jj_consume_token(33);
                Double d = this.NumericExpression();
                Object[] l = (Object[])a;
                for (int i = 0; i < l.length; ++i) {
                    if (l[i] == null || l[i].toString().equals("")) continue;
                    a2.add(new Double(((BigDecimal)l[i]).divide(BigDecimal.valueOf(d), MathContext.DECIMAL128).doubleValue()));
                }
                break;
            }
            case 34: {
                this.jj_consume_token(34);
                Double d = this.NumericExpression();
                Object[] l = (Object[])a;
                for (int i = 0; i < l.length; ++i) {
                    if (l[i] == null || l[i].toString().equals("")) continue;
                    a2.add(new Double(((BigDecimal)l[i]).remainder(BigDecimal.valueOf(d)).doubleValue()));
                }
                break;
            }
            case 35: {
                this.jj_consume_token(35);
                Double d = this.NumericExpression();
                Object[] l = (Object[])a;
                for (int i = 0; i < l.length; ++i) {
                    if (l[i] == null || l[i].toString().equals("")) continue;
                    a2.add(new Double(Math.pow(((BigDecimal)l[i]).doubleValue(), BigDecimal.valueOf(d).doubleValue())));
                }
                break;
            }
            default: {
                this.jj_la1[56] = this.jj_gen;
                Object[] l = (Object[])a;
                for (int i = 0; i < l.length; ++i) {
                    if (l[i] == null || l[i].toString().equals("")) continue;
                    a2.add(l[i]);
                }
            }
        }
        return a2;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private final boolean jj_2_1(int xla) {
        this.jj_la = xla;
        this.jj_lastpos = this.jj_scanpos = this.token;
        try {
            boolean bl = !this.jj_3_1();
            return bl;
        }
        catch (LookaheadSuccess ls) {
            boolean bl = true;
            return bl;
        }
        finally {
            this.jj_save(0, xla);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private final boolean jj_2_2(int xla) {
        this.jj_la = xla;
        this.jj_lastpos = this.jj_scanpos = this.token;
        try {
            boolean bl = !this.jj_3_2();
            return bl;
        }
        catch (LookaheadSuccess ls) {
            boolean bl = true;
            return bl;
        }
        finally {
            this.jj_save(1, xla);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private final boolean jj_2_3(int xla) {
        this.jj_la = xla;
        this.jj_lastpos = this.jj_scanpos = this.token;
        try {
            boolean bl = !this.jj_3_3();
            return bl;
        }
        catch (LookaheadSuccess ls) {
            boolean bl = true;
            return bl;
        }
        finally {
            this.jj_save(2, xla);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private final boolean jj_2_4(int xla) {
        this.jj_la = xla;
        this.jj_lastpos = this.jj_scanpos = this.token;
        try {
            boolean bl = !this.jj_3_4();
            return bl;
        }
        catch (LookaheadSuccess ls) {
            boolean bl = true;
            return bl;
        }
        finally {
            this.jj_save(3, xla);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private final boolean jj_2_5(int xla) {
        this.jj_la = xla;
        this.jj_lastpos = this.jj_scanpos = this.token;
        try {
            boolean bl = !this.jj_3_5();
            return bl;
        }
        catch (LookaheadSuccess ls) {
            boolean bl = true;
            return bl;
        }
        finally {
            this.jj_save(4, xla);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private final boolean jj_2_6(int xla) {
        this.jj_la = xla;
        this.jj_lastpos = this.jj_scanpos = this.token;
        try {
            boolean bl = !this.jj_3_6();
            return bl;
        }
        catch (LookaheadSuccess ls) {
            boolean bl = true;
            return bl;
        }
        finally {
            this.jj_save(5, xla);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private final boolean jj_2_7(int xla) {
        this.jj_la = xla;
        this.jj_lastpos = this.jj_scanpos = this.token;
        try {
            boolean bl = !this.jj_3_7();
            return bl;
        }
        catch (LookaheadSuccess ls) {
            boolean bl = true;
            return bl;
        }
        finally {
            this.jj_save(6, xla);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private final boolean jj_2_8(int xla) {
        this.jj_la = xla;
        this.jj_lastpos = this.jj_scanpos = this.token;
        try {
            boolean bl = !this.jj_3_8();
            return bl;
        }
        catch (LookaheadSuccess ls) {
            boolean bl = true;
            return bl;
        }
        finally {
            this.jj_save(7, xla);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private final boolean jj_2_9(int xla) {
        this.jj_la = xla;
        this.jj_lastpos = this.jj_scanpos = this.token;
        try {
            boolean bl = !this.jj_3_9();
            return bl;
        }
        catch (LookaheadSuccess ls) {
            boolean bl = true;
            return bl;
        }
        finally {
            this.jj_save(8, xla);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private final boolean jj_2_10(int xla) {
        this.jj_la = xla;
        this.jj_lastpos = this.jj_scanpos = this.token;
        try {
            boolean bl = !this.jj_3_10();
            return bl;
        }
        catch (LookaheadSuccess ls) {
            boolean bl = true;
            return bl;
        }
        finally {
            this.jj_save(9, xla);
        }
    }

    private final boolean jj_3R_46() {
        return this.jj_3R_45();
    }

    private final boolean jj_3_9() {
        if (this.jj_3R_28()) {
            return true;
        }
        Token xsp = this.jj_scanpos;
        if (this.jj_scan_token(104)) {
            this.jj_scanpos = xsp;
            this.lookingAhead = true;
            this.jj_semLA = this.objType != null && (this.objType.equals("ExpressionParam") || this.objType.equals("ObjectArray"));
            this.lookingAhead = false;
            if (!this.jj_semLA || this.jj_3R_33()) {
                this.jj_scanpos = xsp;
                if (this.jj_3R_34()) {
                    return true;
                }
            }
        }
        return false;
    }

    private final boolean jj_3R_39() {
        Token xsp = this.jj_scanpos;
        if (this.jj_3R_46()) {
            this.jj_scanpos = xsp;
            if (this.jj_3R_47()) {
                return true;
            }
        }
        return false;
    }

    private final boolean jj_3R_104() {
        Token xsp = this.jj_scanpos;
        if (this.jj_3R_107()) {
            this.jj_scanpos = xsp;
            if (this.jj_3R_108()) {
                this.jj_scanpos = xsp;
                if (this.jj_3R_109()) {
                    this.jj_scanpos = xsp;
                    if (this.jj_3R_110()) {
                        this.jj_scanpos = xsp;
                        if (this.jj_3R_111()) {
                            this.jj_scanpos = xsp;
                            if (this.jj_3R_112()) {
                                this.jj_scanpos = xsp;
                                if (this.jj_3R_113()) {
                                    this.jj_scanpos = xsp;
                                    if (this.jj_3R_114()) {
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private final boolean jj_3R_107() {
        return this.jj_scan_token(7);
    }

    private final boolean jj_3R_156() {
        return this.jj_scan_token(59);
    }

    private final boolean jj_3R_24() {
        Token xsp = this.jj_scanpos;
        this.lookingAhead = true;
        this.jj_semLA = this.noOfBooleanFragment > 1;
        this.lookingAhead = false;
        if (!this.jj_semLA || this.jj_3R_38()) {
            this.jj_scanpos = xsp;
            if (this.jj_3R_39()) {
                return true;
            }
        }
        return false;
    }

    private final boolean jj_3R_38() {
        return this.jj_scan_token(36);
    }

    private final boolean jj_3R_125() {
        return this.jj_3R_119();
    }

    private final boolean jj_3R_81() {
        return this.jj_scan_token(24);
    }

    private final boolean jj_3R_124() {
        return this.jj_3R_117();
    }

    private final boolean jj_3R_80() {
        return this.jj_scan_token(99);
    }

    private final boolean jj_3R_79() {
        return this.jj_scan_token(97);
    }

    private final boolean jj_3R_78() {
        return this.jj_scan_token(96);
    }

    private final boolean jj_3R_77() {
        return this.jj_scan_token(95);
    }

    private final boolean jj_3R_76() {
        return this.jj_scan_token(94);
    }

    private final boolean jj_3R_123() {
        return this.jj_scan_token(8);
    }

    private final boolean jj_3R_75() {
        return this.jj_scan_token(93);
    }

    private final boolean jj_3R_43() {
        return this.jj_3R_52();
    }

    private final boolean jj_3R_122() {
        return this.jj_3R_116();
    }

    private final boolean jj_3R_121() {
        return this.jj_scan_token(36);
    }

    private final boolean jj_3R_48() {
        return this.jj_3R_54();
    }

    private final boolean jj_3R_102() {
        return this.jj_3R_104();
    }

    private final boolean jj_3R_155() {
        return this.jj_scan_token(58);
    }

    private final boolean jj_3R_51() {
        return this.jj_3R_56();
    }

    private final boolean jj_3_6() {
        return this.jj_3R_24();
    }

    private final boolean jj_3R_120() {
        return this.jj_scan_token(4);
    }

    private final boolean jj_3R_41() {
        Token xsp = this.jj_scanpos;
        if (this.jj_3_6()) {
            this.jj_scanpos = xsp;
            if (this.jj_3R_48()) {
                return true;
            }
        }
        return false;
    }

    private final boolean jj_3R_50() {
        return this.jj_3R_55();
    }

    private final boolean jj_3R_74() {
        return this.jj_scan_token(92);
    }

    private final boolean jj_3R_73() {
        return this.jj_scan_token(91);
    }

    private final boolean jj_3R_115() {
        Token xsp = this.jj_scanpos;
        if (this.jj_3R_120()) {
            this.jj_scanpos = xsp;
            if (this.jj_3R_121()) {
                this.jj_scanpos = xsp;
                if (this.jj_3R_122()) {
                    this.jj_scanpos = xsp;
                    if (this.jj_3R_123()) {
                        this.jj_scanpos = xsp;
                        if (this.jj_3R_124()) {
                            this.jj_scanpos = xsp;
                            if (this.jj_3R_125()) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private final boolean jj_3R_72() {
        return this.jj_scan_token(90);
    }

    private final boolean jj_3R_49() {
        return this.jj_scan_token(7);
    }

    private final boolean jj_3R_71() {
        return this.jj_scan_token(89);
    }

    private final boolean jj_3R_70() {
        return this.jj_scan_token(88);
    }

    private final boolean jj_3R_106() {
        return this.jj_3R_115();
    }

    private final boolean jj_3R_69() {
        return this.jj_scan_token(87);
    }

    private final boolean jj_3R_28() {
        return false;
    }

    private final boolean jj_3R_101() {
        return this.jj_scan_token(31);
    }

    private final boolean jj_3R_105() {
        return this.jj_scan_token(31);
    }

    private final boolean jj_3R_68() {
        return this.jj_scan_token(86);
    }

    private final boolean jj_3R_27() {
        Token xsp = this.jj_scanpos;
        if (this.jj_3R_42()) {
            this.jj_scanpos = xsp;
            if (this.jj_3R_43()) {
                return true;
            }
        }
        return false;
    }

    private final boolean jj_3R_42() {
        Token xsp = this.jj_scanpos;
        if (this.jj_3R_49()) {
            this.jj_scanpos = xsp;
            if (this.jj_3R_50()) {
                this.jj_scanpos = xsp;
                if (this.jj_3R_51()) {
                    return true;
                }
            }
        }
        return false;
    }

    private final boolean jj_3R_99() {
        Token xsp = this.jj_scanpos;
        if (this.jj_3R_101()) {
            this.jj_scanpos = xsp;
            if (this.jj_3R_102()) {
                return true;
            }
        }
        return false;
    }

    private final boolean jj_3R_67() {
        return this.jj_scan_token(85);
    }

    private final boolean jj_3R_66() {
        return this.jj_scan_token(84);
    }

    private final boolean jj_3R_103() {
        Token xsp = this.jj_scanpos;
        if (this.jj_3R_105()) {
            this.jj_scanpos = xsp;
            if (this.jj_3R_106()) {
                return true;
            }
        }
        return false;
    }

    private final boolean jj_3R_65() {
        return this.jj_scan_token(83);
    }

    private final boolean jj_3R_32() {
        return this.jj_3R_45();
    }

    private final boolean jj_3R_64() {
        return this.jj_scan_token(25);
    }

    private final boolean jj_3R_55() {
        Token xsp = this.jj_scanpos;
        if (this.jj_3R_64()) {
            this.jj_scanpos = xsp;
            if (this.jj_3R_65()) {
                this.jj_scanpos = xsp;
                if (this.jj_3R_66()) {
                    this.jj_scanpos = xsp;
                    if (this.jj_3R_67()) {
                        this.jj_scanpos = xsp;
                        if (this.jj_3R_68()) {
                            this.jj_scanpos = xsp;
                            if (this.jj_3R_69()) {
                                this.jj_scanpos = xsp;
                                if (this.jj_3R_70()) {
                                    this.jj_scanpos = xsp;
                                    if (this.jj_3R_71()) {
                                        this.jj_scanpos = xsp;
                                        if (this.jj_3R_72()) {
                                            this.jj_scanpos = xsp;
                                            if (this.jj_3R_73()) {
                                                this.jj_scanpos = xsp;
                                                if (this.jj_3R_74()) {
                                                    this.jj_scanpos = xsp;
                                                    if (this.jj_3R_75()) {
                                                        this.jj_scanpos = xsp;
                                                        if (this.jj_3R_76()) {
                                                            this.jj_scanpos = xsp;
                                                            if (this.jj_3R_77()) {
                                                                this.jj_scanpos = xsp;
                                                                if (this.jj_3R_78()) {
                                                                    this.jj_scanpos = xsp;
                                                                    if (this.jj_3R_79()) {
                                                                        this.jj_scanpos = xsp;
                                                                        if (this.jj_3R_80()) {
                                                                            this.jj_scanpos = xsp;
                                                                            if (this.jj_3R_81()) {
                                                                                return true;
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private final boolean jj_3R_26() {
        return this.jj_3R_41();
    }

    private final boolean jj_3R_147() {
        return this.jj_scan_token(65);
    }

    private final boolean jj_3R_154() {
        return this.jj_scan_token(57);
    }

    private final boolean jj_3R_146() {
        return this.jj_scan_token(64);
    }

    private final boolean jj_3R_145() {
        return this.jj_scan_token(63);
    }

    private final boolean jj_3R_98() {
        return this.jj_scan_token(82);
    }

    private final boolean jj_3R_144() {
        return this.jj_scan_token(62);
    }

    private final boolean jj_3R_97() {
        return this.jj_scan_token(81);
    }

    private final boolean jj_3R_143() {
        return this.jj_scan_token(61);
    }

    private final boolean jj_3R_96() {
        return this.jj_scan_token(80);
    }

    private final boolean jj_3R_63() {
        return this.jj_scan_token(9);
    }

    private final boolean jj_3R_31() {
        return this.jj_3R_44();
    }

    private final boolean jj_3R_142() {
        return this.jj_scan_token(53);
    }

    private final boolean jj_3R_95() {
        return this.jj_scan_token(79);
    }

    private final boolean jj_3R_100() {
        return this.jj_3R_103();
    }

    private final boolean jj_3R_62() {
        return this.jj_scan_token(29);
    }

    private final boolean jj_3R_141() {
        return this.jj_scan_token(52);
    }

    private final boolean jj_3R_94() {
        return this.jj_scan_token(78);
    }

    private final boolean jj_3R_61() {
        return this.jj_scan_token(28);
    }

    private final boolean jj_3R_93() {
        return this.jj_scan_token(77);
    }

    private final boolean jj_3R_140() {
        return this.jj_scan_token(51);
    }

    private final boolean jj_3R_92() {
        return this.jj_scan_token(76);
    }

    private final boolean jj_3R_60() {
        return this.jj_scan_token(27);
    }

    private final boolean jj_3R_139() {
        return this.jj_scan_token(49);
    }

    private final boolean jj_3_8() {
        if (this.jj_3R_28()) {
            return true;
        }
        Token xsp = this.jj_scanpos;
        if (this.jj_scan_token(104)) {
            this.jj_scanpos = xsp;
            this.lookingAhead = true;
            this.jj_semLA = this.objType != null && (this.objType.equals("ExpressionParam") || this.objType.equals("ObjectArray"));
            this.lookingAhead = false;
            if (!this.jj_semLA || this.jj_3R_31()) {
                this.jj_scanpos = xsp;
                if (this.jj_3R_32()) {
                    return true;
                }
            }
        }
        return false;
    }

    private final boolean jj_3R_91() {
        return this.jj_scan_token(75);
    }

    private final boolean jj_3R_138() {
        return this.jj_scan_token(48);
    }

    private final boolean jj_3R_59() {
        return this.jj_scan_token(26);
    }

    private final boolean jj_3R_30() {
        return this.jj_3R_45();
    }

    private final boolean jj_3R_90() {
        return this.jj_scan_token(74);
    }

    private final boolean jj_3R_137() {
        return this.jj_scan_token(47);
    }

    private final boolean jj_3R_54() {
        Token xsp = this.jj_scanpos;
        if (this.jj_3_5()) {
            this.jj_scanpos = xsp;
            if (this.jj_3R_59()) {
                this.jj_scanpos = xsp;
                if (this.jj_3R_60()) {
                    this.jj_scanpos = xsp;
                    if (this.jj_3R_61()) {
                        this.jj_scanpos = xsp;
                        if (this.jj_3R_62()) {
                            this.jj_scanpos = xsp;
                            if (this.jj_3R_63()) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private final boolean jj_3_5() {
        if (this.jj_scan_token(26)) {
            return true;
        }
        if (this.jj_scan_token(36)) {
            return true;
        }
        return this.jj_3R_27();
    }

    private final boolean jj_3R_89() {
        return this.jj_scan_token(73);
    }

    private final boolean jj_3R_136() {
        return this.jj_scan_token(46);
    }

    private final boolean jj_3R_44() {
        return this.jj_scan_token(8);
    }

    private final boolean jj_3R_153() {
        return this.jj_scan_token(38);
    }

    private final boolean jj_3R_88() {
        return this.jj_scan_token(72);
    }

    private final boolean jj_3R_135() {
        return this.jj_scan_token(45);
    }

    private final boolean jj_3R_87() {
        return this.jj_scan_token(71);
    }

    private final boolean jj_3R_134() {
        return this.jj_scan_token(44);
    }

    private final boolean jj_3R_86() {
        return this.jj_scan_token(70);
    }

    private final boolean jj_3R_133() {
        return this.jj_scan_token(43);
    }

    private final boolean jj_3R_85() {
        return this.jj_scan_token(69);
    }

    private final boolean jj_3R_132() {
        return this.jj_scan_token(42);
    }

    private final boolean jj_3R_84() {
        return this.jj_scan_token(68);
    }

    private final boolean jj_3R_58() {
        return this.jj_3R_100();
    }

    private final boolean jj_3R_131() {
        return this.jj_scan_token(41);
    }

    private final boolean jj_3R_83() {
        return this.jj_scan_token(67);
    }

    private final boolean jj_3_4() {
        return this.jj_3R_26();
    }

    private final boolean jj_3R_29() {
        return this.jj_3R_44();
    }

    private final boolean jj_3R_82() {
        return this.jj_scan_token(66);
    }

    private final boolean jj_3R_56() {
        Token xsp = this.jj_scanpos;
        if (this.jj_3R_82()) {
            this.jj_scanpos = xsp;
            if (this.jj_3R_83()) {
                this.jj_scanpos = xsp;
                if (this.jj_3R_84()) {
                    this.jj_scanpos = xsp;
                    if (this.jj_3R_85()) {
                        this.jj_scanpos = xsp;
                        if (this.jj_3R_86()) {
                            this.jj_scanpos = xsp;
                            if (this.jj_3R_87()) {
                                this.jj_scanpos = xsp;
                                if (this.jj_3R_88()) {
                                    this.jj_scanpos = xsp;
                                    if (this.jj_3R_89()) {
                                        this.jj_scanpos = xsp;
                                        if (this.jj_3R_90()) {
                                            this.jj_scanpos = xsp;
                                            if (this.jj_3R_91()) {
                                                this.jj_scanpos = xsp;
                                                if (this.jj_3R_92()) {
                                                    this.jj_scanpos = xsp;
                                                    if (this.jj_3R_93()) {
                                                        this.jj_scanpos = xsp;
                                                        if (this.jj_3R_94()) {
                                                            this.jj_scanpos = xsp;
                                                            if (this.jj_3R_95()) {
                                                                this.jj_scanpos = xsp;
                                                                if (this.jj_3R_96()) {
                                                                    this.jj_scanpos = xsp;
                                                                    if (this.jj_3R_97()) {
                                                                        this.jj_scanpos = xsp;
                                                                        if (this.jj_3R_98()) {
                                                                            return true;
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private final boolean jj_3R_130() {
        return this.jj_scan_token(40);
    }

    private final boolean jj_3R_23() {
        Token xsp = this.jj_scanpos;
        this.lookingAhead = true;
        this.jj_semLA = this.isBooleanInside;
        this.lookingAhead = false;
        if (!this.jj_semLA || this.jj_3R_37()) {
            this.jj_scanpos = xsp;
            if (this.jj_3_4()) {
                return true;
            }
        }
        return false;
    }

    private final boolean jj_3R_37() {
        return this.jj_scan_token(36);
    }

    private final boolean jj_3R_129() {
        return this.jj_scan_token(39);
    }

    private final boolean jj_3R_128() {
        return this.jj_scan_token(50);
    }

    private final boolean jj_3R_57() {
        return this.jj_3R_99();
    }

    private final boolean jj_3R_127() {
        return this.jj_scan_token(20);
    }

    private final boolean jj_3_7() {
        if (this.jj_3R_28()) {
            return true;
        }
        Token xsp = this.jj_scanpos;
        if (this.jj_scan_token(104)) {
            this.jj_scanpos = xsp;
            this.lookingAhead = true;
            this.jj_semLA = this.objType != null && (this.objType.equals("ExpressionParam") || this.objType.equals("ObjectArray"));
            this.lookingAhead = false;
            if (!this.jj_semLA || this.jj_3R_29()) {
                this.jj_scanpos = xsp;
                if (this.jj_3R_30()) {
                    return true;
                }
            }
        }
        return false;
    }

    private final boolean jj_3R_126() {
        return this.jj_scan_token(19);
    }

    private final boolean jj_3R_152() {
        return this.jj_scan_token(23);
    }

    private final boolean jj_3R_116() {
        Token xsp = this.jj_scanpos;
        if (this.jj_3R_126()) {
            this.jj_scanpos = xsp;
            if (this.jj_3R_127()) {
                this.jj_scanpos = xsp;
                if (this.jj_3R_128()) {
                    this.jj_scanpos = xsp;
                    if (this.jj_3R_129()) {
                        this.jj_scanpos = xsp;
                        if (this.jj_3R_130()) {
                            this.jj_scanpos = xsp;
                            if (this.jj_3R_131()) {
                                this.jj_scanpos = xsp;
                                if (this.jj_3R_132()) {
                                    this.jj_scanpos = xsp;
                                    if (this.jj_3R_133()) {
                                        this.jj_scanpos = xsp;
                                        if (this.jj_3R_134()) {
                                            this.jj_scanpos = xsp;
                                            if (this.jj_3R_135()) {
                                                this.jj_scanpos = xsp;
                                                if (this.jj_3R_136()) {
                                                    this.jj_scanpos = xsp;
                                                    if (this.jj_3R_137()) {
                                                        this.jj_scanpos = xsp;
                                                        if (this.jj_3R_138()) {
                                                            this.jj_scanpos = xsp;
                                                            if (this.jj_3R_139()) {
                                                                this.jj_scanpos = xsp;
                                                                if (this.jj_3R_140()) {
                                                                    this.jj_scanpos = xsp;
                                                                    if (this.jj_3R_141()) {
                                                                        this.jj_scanpos = xsp;
                                                                        if (this.jj_3R_142()) {
                                                                            this.jj_scanpos = xsp;
                                                                            if (this.jj_3R_143()) {
                                                                                this.jj_scanpos = xsp;
                                                                                if (this.jj_3R_144()) {
                                                                                    this.jj_scanpos = xsp;
                                                                                    if (this.jj_3R_145()) {
                                                                                        this.jj_scanpos = xsp;
                                                                                        if (this.jj_3R_146()) {
                                                                                            this.jj_scanpos = xsp;
                                                                                            if (this.jj_3R_147()) {
                                                                                                return true;
                                                                                            }
                                                                                        }
                                                                                    }
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private final boolean jj_3_2() {
        return this.jj_3R_24();
    }

    private final boolean jj_3_1() {
        return this.jj_3R_23();
    }

    private final boolean jj_3R_40() {
        return this.jj_3R_27();
    }

    private final boolean jj_3R_25() {
        Token xsp = this.jj_scanpos;
        if (this.jj_3R_40()) {
            this.jj_scanpos = xsp;
            if (this.jj_3_1()) {
                return true;
            }
        }
        return false;
    }

    private final boolean jj_3_3() {
        Token xsp = this.jj_scanpos;
        this.lookingAhead = true;
        this.jj_semLA = !this.lookaheadFlag;
        this.lookingAhead = false;
        if (!this.jj_semLA || this.jj_3R_25()) {
            this.jj_scanpos = xsp;
            if (this.jj_3_2()) {
                return true;
            }
        }
        return false;
    }

    private final boolean jj_3R_53() {
        return this.jj_3R_58();
    }

    private final boolean jj_3R_118() {
        return this.jj_scan_token(100);
    }

    private final boolean jj_3R_151() {
        return this.jj_scan_token(54);
    }

    private final boolean jj_3R_45() {
        return this.jj_3R_53();
    }

    private final boolean jj_3R_114() {
        return this.jj_scan_token(36);
    }

    private final boolean jj_3R_119() {
        return this.jj_3R_148();
    }

    private final boolean jj_3R_150() {
        return this.jj_scan_token(56);
    }

    private final boolean jj_3R_113() {
        return this.jj_scan_token(8);
    }

    private final boolean jj_3R_159() {
        return this.jj_scan_token(21);
    }

    private final boolean jj_3R_112() {
        return this.jj_3R_119();
    }

    private final boolean jj_3R_148() {
        Token xsp = this.jj_scanpos;
        if (this.jj_3R_149()) {
            this.jj_scanpos = xsp;
            if (this.jj_3R_150()) {
                this.jj_scanpos = xsp;
                if (this.jj_3R_151()) {
                    this.jj_scanpos = xsp;
                    if (this.jj_3R_152()) {
                        this.jj_scanpos = xsp;
                        if (this.jj_3R_153()) {
                            this.jj_scanpos = xsp;
                            if (this.jj_3R_154()) {
                                this.jj_scanpos = xsp;
                                if (this.jj_3R_155()) {
                                    this.jj_scanpos = xsp;
                                    if (this.jj_3R_156()) {
                                        this.jj_scanpos = xsp;
                                        if (this.jj_3R_157()) {
                                            this.jj_scanpos = xsp;
                                            if (this.jj_3R_158()) {
                                                this.jj_scanpos = xsp;
                                                if (this.jj_3R_159()) {
                                                    return true;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private final boolean jj_3R_149() {
        return this.jj_scan_token(55);
    }

    private final boolean jj_3R_52() {
        return this.jj_3R_57();
    }

    private final boolean jj_3R_36() {
        return this.jj_3R_45();
    }

    private final boolean jj_3R_158() {
        return this.jj_scan_token(22);
    }

    private final boolean jj_3R_47() {
        return this.jj_3R_27();
    }

    private final boolean jj_3R_111() {
        return this.jj_3R_118();
    }

    private final boolean jj_3R_35() {
        return this.jj_3R_44();
    }

    private final boolean jj_3_10() {
        if (this.jj_3R_28()) {
            return true;
        }
        Token xsp = this.jj_scanpos;
        if (this.jj_scan_token(104)) {
            this.jj_scanpos = xsp;
            this.lookingAhead = true;
            this.jj_semLA = this.objType != null && (this.objType.equals("ExpressionParam") || this.objType.equals("ObjectArray"));
            this.lookingAhead = false;
            if (!this.jj_semLA || this.jj_3R_35()) {
                this.jj_scanpos = xsp;
                if (this.jj_3R_36()) {
                    return true;
                }
            }
        }
        return false;
    }

    private final boolean jj_3R_117() {
        return this.jj_scan_token(98);
    }

    private final boolean jj_3R_34() {
        return this.jj_3R_45();
    }

    private final boolean jj_3R_110() {
        return this.jj_3R_117();
    }

    private final boolean jj_3R_157() {
        return this.jj_scan_token(60);
    }

    private final boolean jj_3R_33() {
        return this.jj_3R_44();
    }

    private final boolean jj_3R_109() {
        return this.jj_3R_116();
    }

    private final boolean jj_3R_108() {
        return this.jj_scan_token(4);
    }

    private static void jj_la1_0() {
        jj_la1_0 = new int[]{-2080898672, 1, 1006633472, 2048, 1006633472, 1024, 258048, 258048, -2080898672, -2080898672, -1073741824, -1073741824, 0, 0, 0, -2131230448, 16253200, -2131230448, -2131230448, -2131230448, -2131230448, -2131230448, -2131230448, 0, -2131230448, 0, -2131230448, -2131230448, -2131230448, -2131230448, -2131230448, 0, -2131230448, 0, -2131230448, -2131230448, -2131230448, -2131230448, -2131230448, 0xE00000, 0x180000, 0x3000080, 0x40000000, -1073741824, -1073741824, -2080898672, 0, 0, 0, -2131230320, 16253328, 0, 0, 0, 0x3000000, 0, -1073741824};
    }

    private static void jj_la1_1() {
        jj_la1_1 = new int[]{-48, 0, 0, 0, 0, 0, 0, 0, -48, -48, 0, 0, 7, 7, 8, -48, -48, -48, -48, -48, -48, -48, -48, 0, -48, 0, -48, -48, -48, -48, -48, 0, -48, 0, -48, -48, -48, -48, -48, 532676672, -532676736, 0, 0, 0, 0, -48, 7, 7, 8, -48, -48, 0, 0, 0, 0, 0, 15};
    }

    private static void jj_la1_2() {
        jj_la1_2 = new int[]{-1, 0, 0, 0, 0, 0, 0, 0, -1, -1, 0, 0, 0, 0, 0, 3, 3, 3, 3, 3, 3, 3, 3, 0, 3, 0, 3, 3, 3, 3, 3, 0, 3, 0, 3, 3, 3, 3, 3, 0, 3, -4, 0, 0, 0, -1, 0, 0, 0, 3, 3, 524284, 0, 0, -524288, 0, 0};
    }

    private static void jj_la1_3() {
        jj_la1_3 = new int[]{31, 0, 0, 0, 0, 0, 0, 0, 31, 31, 0, 0, 0, 0, 0, 4, 4, 260, 260, 260, 260, 260, 260, 256, 4, 256, 4, 260, 260, 260, 260, 256, 4, 256, 4, 260, 260, 260, 260, 0, 0, 11, 0, 0, 0, 31, 0, 0, 0, 20, 20, 0, 256, 256, 11, 32, 0};
    }

    public ExpressionEvaluator(InputStream stream) {
        int i;
        this.jj_input_stream = new SimpleCharStream(stream, 1, 1);
        this.token_source = new ExpressionEvaluatorTokenManager(this.jj_input_stream);
        this.token = new Token();
        this.jj_ntk = -1;
        this.jj_gen = 0;
        for (i = 0; i < 57; ++i) {
            this.jj_la1[i] = -1;
        }
        for (i = 0; i < this.jj_2_rtns.length; ++i) {
            this.jj_2_rtns[i] = new JJCalls();
        }
    }

    public void ReInit(InputStream stream) {
        int i;
        this.jj_input_stream.ReInit(stream, 1, 1);
        this.token_source.ReInit(this.jj_input_stream);
        this.token = new Token();
        this.jj_ntk = -1;
        this.jj_gen = 0;
        for (i = 0; i < 57; ++i) {
            this.jj_la1[i] = -1;
        }
        for (i = 0; i < this.jj_2_rtns.length; ++i) {
            this.jj_2_rtns[i] = new JJCalls();
        }
    }

    public ExpressionEvaluator(Reader stream) {
        int i;
        this.jj_input_stream = new SimpleCharStream(stream, 1, 1);
        this.token_source = new ExpressionEvaluatorTokenManager(this.jj_input_stream);
        this.token = new Token();
        this.jj_ntk = -1;
        this.jj_gen = 0;
        for (i = 0; i < 57; ++i) {
            this.jj_la1[i] = -1;
        }
        for (i = 0; i < this.jj_2_rtns.length; ++i) {
            this.jj_2_rtns[i] = new JJCalls();
        }
    }

    public void ReInit(Reader stream) {
        int i;
        this.jj_input_stream.ReInit(stream, 1, 1);
        this.token_source.ReInit(this.jj_input_stream);
        this.token = new Token();
        this.jj_ntk = -1;
        this.jj_gen = 0;
        for (i = 0; i < 57; ++i) {
            this.jj_la1[i] = -1;
        }
        for (i = 0; i < this.jj_2_rtns.length; ++i) {
            this.jj_2_rtns[i] = new JJCalls();
        }
    }

    public ExpressionEvaluator(ExpressionEvaluatorTokenManager tm) {
        int i;
        this.token_source = tm;
        this.token = new Token();
        this.jj_ntk = -1;
        this.jj_gen = 0;
        for (i = 0; i < 57; ++i) {
            this.jj_la1[i] = -1;
        }
        for (i = 0; i < this.jj_2_rtns.length; ++i) {
            this.jj_2_rtns[i] = new JJCalls();
        }
    }

    public void ReInit(ExpressionEvaluatorTokenManager tm) {
        int i;
        this.token_source = tm;
        this.token = new Token();
        this.jj_ntk = -1;
        this.jj_gen = 0;
        for (i = 0; i < 57; ++i) {
            this.jj_la1[i] = -1;
        }
        for (i = 0; i < this.jj_2_rtns.length; ++i) {
            this.jj_2_rtns[i] = new JJCalls();
        }
    }

    private final Token jj_consume_token(int kind) throws ParseException {
        Token oldToken = this.token;
        this.token = oldToken.next != null ? this.token.next : (this.token.next = this.token_source.getNextToken());
        this.jj_ntk = -1;
        if (this.token.kind == kind) {
            ++this.jj_gen;
            if (++this.jj_gc > 100) {
                this.jj_gc = 0;
                for (int i = 0; i < this.jj_2_rtns.length; ++i) {
                    JJCalls c = this.jj_2_rtns[i];
                    while (c != null) {
                        if (c.gen < this.jj_gen) {
                            c.first = null;
                        }
                        c = c.next;
                    }
                }
            }
            return this.token;
        }
        this.token = oldToken;
        this.jj_kind = kind;
        throw this.generateParseException();
    }

    private final boolean jj_scan_token(int kind) {
        if (this.jj_scanpos == this.jj_lastpos) {
            --this.jj_la;
            if (this.jj_scanpos.next == null) {
                this.jj_scanpos = this.jj_scanpos.next = this.token_source.getNextToken();
                this.jj_lastpos = this.jj_scanpos.next;
            } else {
                this.jj_lastpos = this.jj_scanpos = this.jj_scanpos.next;
            }
        } else {
            this.jj_scanpos = this.jj_scanpos.next;
        }
        if (this.jj_rescan) {
            int i = 0;
            Token tok = this.token;
            while (tok != null && tok != this.jj_scanpos) {
                ++i;
                tok = tok.next;
            }
            if (tok != null) {
                this.jj_add_error_token(kind, i);
            }
        }
        if (this.jj_scanpos.kind != kind) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            throw this.jj_ls;
        }
        return false;
    }

    public final Token getNextToken() {
        this.token = this.token.next != null ? this.token.next : (this.token.next = this.token_source.getNextToken());
        this.jj_ntk = -1;
        ++this.jj_gen;
        return this.token;
    }

    public final Token getToken(int index) {
        Token t = this.lookingAhead ? this.jj_scanpos : this.token;
        for (int i = 0; i < index; ++i) {
            t = t.next != null ? t.next : (t.next = this.token_source.getNextToken());
        }
        return t;
    }

    private final int jj_ntk() {
        this.jj_nt = this.token.next;
        if (this.jj_nt == null) {
            this.token.next = this.token_source.getNextToken();
            this.jj_ntk = this.token.next.kind;
            return this.jj_ntk;
        }
        this.jj_ntk = this.jj_nt.kind;
        return this.jj_ntk;
    }

    private void jj_add_error_token(int kind, int pos) {
        if (pos >= 100) {
            return;
        }
        if (pos == this.jj_endpos + 1) {
            this.jj_lasttokens[this.jj_endpos++] = kind;
        } else if (this.jj_endpos != 0) {
            this.jj_expentry = new int[this.jj_endpos];
            for (int i = 0; i < this.jj_endpos; ++i) {
                this.jj_expentry[i] = this.jj_lasttokens[i];
            }
            boolean exists = false;
            Enumeration e = this.jj_expentries.elements();
            while (e.hasMoreElements()) {
                int[] oldentry = (int[])e.nextElement();
                if (oldentry.length != this.jj_expentry.length) continue;
                exists = true;
                for (int i = 0; i < this.jj_expentry.length; ++i) {
                    if (oldentry[i] == this.jj_expentry[i]) continue;
                    exists = false;
                    break;
                }
                if (!exists) continue;
                break;
            }
            if (!exists) {
                this.jj_expentries.addElement(this.jj_expentry);
            }
            if (pos != 0) {
                this.jj_endpos = pos;
                this.jj_lasttokens[this.jj_endpos - 1] = kind;
            }
        }
    }

    public ParseException generateParseException() {
        int i;
        this.jj_expentries.removeAllElements();
        boolean[] la1tokens = new boolean[105];
        for (i = 0; i < 105; ++i) {
            la1tokens[i] = false;
        }
        if (this.jj_kind >= 0) {
            la1tokens[this.jj_kind] = true;
            this.jj_kind = -1;
        }
        for (i = 0; i < 57; ++i) {
            if (this.jj_la1[i] != this.jj_gen) continue;
            for (int j = 0; j < 32; ++j) {
                if ((jj_la1_0[i] & 1 << j) != 0) {
                    la1tokens[j] = true;
                }
                if ((jj_la1_1[i] & 1 << j) != 0) {
                    la1tokens[32 + j] = true;
                }
                if ((jj_la1_2[i] & 1 << j) != 0) {
                    la1tokens[64 + j] = true;
                }
                if ((jj_la1_3[i] & 1 << j) == 0) continue;
                la1tokens[96 + j] = true;
            }
        }
        for (i = 0; i < 105; ++i) {
            if (!la1tokens[i]) continue;
            this.jj_expentry = new int[1];
            this.jj_expentry[0] = i;
            this.jj_expentries.addElement(this.jj_expentry);
        }
        this.jj_endpos = 0;
        this.jj_rescan_token();
        this.jj_add_error_token(0, 0);
        int[][] exptokseq = new int[this.jj_expentries.size()][];
        for (int i2 = 0; i2 < this.jj_expentries.size(); ++i2) {
            exptokseq[i2] = (int[])this.jj_expentries.elementAt(i2);
        }
        return new ParseException(this.token, exptokseq, tokenImage);
    }

    public final void enable_tracing() {
    }

    public final void disable_tracing() {
    }

    private final void jj_rescan_token() {
        this.jj_rescan = true;
        for (int i = 0; i < 10; ++i) {
            JJCalls p = this.jj_2_rtns[i];
            do {
                if (p.gen <= this.jj_gen) continue;
                this.jj_la = p.arg;
                this.jj_lastpos = this.jj_scanpos = p.first;
                switch (i) {
                    case 0: {
                        this.jj_3_1();
                        break;
                    }
                    case 1: {
                        this.jj_3_2();
                        break;
                    }
                    case 2: {
                        this.jj_3_3();
                        break;
                    }
                    case 3: {
                        this.jj_3_4();
                        break;
                    }
                    case 4: {
                        this.jj_3_5();
                        break;
                    }
                    case 5: {
                        this.jj_3_6();
                        break;
                    }
                    case 6: {
                        this.jj_3_7();
                        break;
                    }
                    case 7: {
                        this.jj_3_8();
                        break;
                    }
                    case 8: {
                        this.jj_3_9();
                        break;
                    }
                    case 9: {
                        this.jj_3_10();
                    }
                }
            } while ((p = p.next) != null);
        }
        this.jj_rescan = false;
    }

    private final void jj_save(int index, int xla) {
        JJCalls p = this.jj_2_rtns[index];
        while (p.gen > this.jj_gen) {
            if (p.next == null) {
                p = p.next = new JJCalls();
                break;
            }
            p = p.next;
        }
        p.gen = this.jj_gen + xla - this.jj_la;
        p.first = this.token;
        p.arg = xla;
    }

    static {
        ExpressionEvaluator.jj_la1_0();
        ExpressionEvaluator.jj_la1_1();
        ExpressionEvaluator.jj_la1_2();
        ExpressionEvaluator.jj_la1_3();
    }

    static final class JJCalls {
        int gen;
        Token first;
        int arg;
        JJCalls next;

        JJCalls() {
        }
    }

    private static final class LookaheadSuccess
    extends Error {
        private LookaheadSuccess() {
        }
    }
}

