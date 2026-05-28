/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.util.evaluator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;

public class ExpressionParam {
    public static final int NUMBER = 0;
    public static final int DATE = 1;
    public static final int STRING = 2;
    private int type = 0;
    BigDecimal[] numberValues;
    Calendar[] dateValues;
    String[] stringValues;
    String paramName;

    public String toString() {
        if (this.length() == 1) {
            if (this.type == 0) {
                return this.numberValues[0].toString();
            }
            if (this.type == 1) {
                return this.dateValues[0].toString();
            }
            if (this.type == 2) {
                return this.stringValues[0].toString();
            }
        }
        return "";
    }

    public int length() {
        if (this.type == 0) {
            return this.numberValues.length;
        }
        if (this.type == 1) {
            return this.dateValues.length;
        }
        if (this.type == 2) {
            return this.stringValues.length;
        }
        return 0;
    }

    public ExpressionParam(String paramName, BigDecimal numberValue) {
        this.paramName = paramName;
        this.numberValues = new BigDecimal[1];
        this.numberValues[0] = numberValue;
        this.type = 0;
    }

    public ExpressionParam(String paramName, BigDecimal[] values) {
        int i;
        this.paramName = paramName;
        this.numberValues = values;
        this.type = 0;
        ArrayList<BigDecimal> arrayList = new ArrayList<BigDecimal>();
        for (i = 0; i < this.numberValues.length; ++i) {
            if (this.numberValues[i] == null) continue;
            arrayList.add(this.numberValues[i]);
        }
        this.numberValues = new BigDecimal[arrayList.size()];
        for (i = 0; i < this.numberValues.length; ++i) {
            this.numberValues[i] = (BigDecimal)arrayList.get(i);
        }
    }

    public ExpressionParam(String paramName, Calendar dateValue) {
        this.paramName = paramName;
        this.dateValues = new Calendar[1];
        this.dateValues[0] = dateValue;
        this.type = 1;
    }

    public ExpressionParam(String paramName, Calendar[] values) {
        int i;
        this.paramName = paramName;
        this.dateValues = values;
        this.type = 1;
        ArrayList<Calendar> arrayList = new ArrayList<Calendar>();
        for (i = 0; i < this.dateValues.length; ++i) {
            if (this.dateValues[i] == null) continue;
            arrayList.add(this.dateValues[i]);
        }
        this.dateValues = new Calendar[arrayList.size()];
        for (i = 0; i < this.dateValues.length; ++i) {
            this.dateValues[i] = (Calendar)arrayList.get(i);
        }
    }

    public ExpressionParam(String paramName, String stringValue) {
        this.paramName = paramName;
        this.stringValues = new String[1];
        this.stringValues[0] = stringValue;
        this.type = 2;
    }

    public ExpressionParam(String paramName, String[] values) {
        int i;
        this.paramName = paramName;
        this.stringValues = values;
        this.type = 2;
        ArrayList<String> arrayList = new ArrayList<String>();
        for (i = 0; i < this.stringValues.length; ++i) {
            if (this.stringValues[i] == null || this.stringValues[i].length() <= 0) continue;
            arrayList.add(this.stringValues[i]);
        }
        this.stringValues = new String[arrayList.size()];
        for (i = 0; i < this.stringValues.length; ++i) {
            this.stringValues[i] = (String)arrayList.get(i);
        }
    }

    public ExpressionParam(String paramName, int type, Object objValue) {
        this.buildExpressionParam(paramName, type, new Object[]{objValue});
    }

    public ExpressionParam(String paramName, int type, Object[] objValues) {
        this.buildExpressionParam(paramName, type, objValues);
    }

    private void buildExpressionParam(String paramName, int type, Object[] objValues) {
        block9: {
            int i;
            ArrayList<Object> arrayList;
            block10: {
                int i2;
                block8: {
                    int i3;
                    this.paramName = paramName;
                    this.type = type;
                    arrayList = new ArrayList<Object>();
                    if (type != 0) break block8;
                    for (i3 = 0; i3 < objValues.length; ++i3) {
                        try {
                            arrayList.add(new BigDecimal(objValues[i3].toString()));
                            continue;
                        }
                        catch (Exception e) {
                            if (objValues[i3] == null || objValues[i3].toString().length() <= 0) continue;
                            arrayList.add(new BigDecimal(0));
                        }
                    }
                    this.numberValues = new BigDecimal[arrayList.size()];
                    for (i3 = 0; i3 < this.numberValues.length; ++i3) {
                        this.numberValues[i3] = (BigDecimal)arrayList.get(i3);
                    }
                    break block9;
                }
                if (type != 2) break block10;
                for (i2 = 0; i2 < objValues.length; ++i2) {
                    if (objValues[i2] == null || !(objValues[i2] instanceof String) || objValues[i2].toString().equals("")) continue;
                    arrayList.add(objValues[i2]);
                }
                this.stringValues = new String[arrayList.size()];
                for (i2 = 0; i2 < this.stringValues.length; ++i2) {
                    this.stringValues[i2] = (String)arrayList.get(i2);
                }
                break block9;
            }
            if (type != 1) break block9;
            for (i = 0; i < objValues.length; ++i) {
                if (objValues[i] == null || !(objValues[i] instanceof Calendar)) continue;
                arrayList.add(objValues[i]);
            }
            this.dateValues = new Calendar[arrayList.size()];
            for (i = 0; i < this.dateValues.length; ++i) {
                this.dateValues[i] = (Calendar)arrayList.get(i);
            }
        }
    }

    public int getType() {
        return this.type;
    }

    public BigDecimal getNumberValue() {
        return this.numberValues.length > 0 ? this.numberValues[0] : null;
    }

    public BigDecimal[] getNumberValues() {
        return this.numberValues;
    }

    public Calendar getDateValue() {
        return this.dateValues.length > 0 ? this.dateValues[0] : null;
    }

    public Calendar[] getDateValues() {
        return this.dateValues;
    }

    public String getStringValue() {
        return this.stringValues.length > 0 ? this.stringValues[0] : null;
    }

    public String[] getStringValues() {
        return this.stringValues;
    }
}

