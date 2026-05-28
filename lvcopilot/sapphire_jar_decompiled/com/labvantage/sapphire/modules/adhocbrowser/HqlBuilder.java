/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.hibernate.query.Query
 */
package com.labvantage.sapphire.modules.adhocbrowser;

import com.labvantage.sapphire.RequestParser;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.services.SapphireConnection;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Set;
import java.util.TimeZone;
import org.hibernate.query.Query;
import sapphire.util.FormatUtil;
import sapphire.util.M18NUtil;
import sapphire.util.StringUtil;

public class HqlBuilder {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";
    private SapphireConnection sapphireConnection;
    private int paramnum = 0;
    private HashMap paramMap = new HashMap();

    public HqlBuilder(SapphireConnection sapphireConnection) {
        this.sapphireConnection = sapphireConnection;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    String getOperatorValueClause(String operator, String value, String columntype, String columnid, boolean isTimeZonesensitive) {
        String clause = "";
        String paramname = "";
        String paramname1 = "";
        boolean isMSS = "MSS".equals(this.sapphireConnection.getDbms());
        if ("is null".equals(operator)) {
            return " (" + columnid + " is null " + (isMSS && ("string".equals(columntype) || "C".equals(columntype)) ? " or " + columnid + "=''" : "") + " ) ";
        }
        if ("is not null".equals(operator)) {
            return " (" + columnid + " is not null " + (isMSS && ("string".equals(columntype) || "C".equals(columntype)) ? " and " + columnid + "!=''" : "") + " ) ";
        }
        if ("timestamp".equals(columntype) || "D".equals(columntype)) {
            M18NUtil m18n = new M18NUtil(this.sapphireConnection);
            if ("between".equals(operator)) {
                String[] values = StringUtil.split(value, "|");
                if (values.length == 2) {
                    if (values[0].length() > 0 && values[1].length() > 0) {
                        Calendar c = m18n.parseCalendar(values[0], isTimeZonesensitive);
                        c.setTimeZone(TimeZone.getDefault());
                        Calendar c1 = m18n.parseCalendar(values[1], isTimeZonesensitive);
                        c1.setTimeZone(TimeZone.getDefault());
                        paramname = "timestamp" + this.paramnum++;
                        paramname1 = "timestamp" + this.paramnum++;
                        clause = " " + operator + " :" + paramname + " and :" + paramname1;
                        this.paramMap.put(paramname, c);
                        this.paramMap.put(paramname1, c1);
                    } else if (values[0].length() > 0) {
                        operator = ">=";
                        value = values[0];
                    } else {
                        if (values[1].length() <= 0) return " is not null";
                        operator = "<=";
                        value = values[1];
                    }
                } else {
                    Trace.logError("Operator between with only one value.");
                }
            }
            if ("between".equals(operator)) return columnid + clause;
            Calendar c = m18n.parseCalendar(value, isTimeZonesensitive);
            boolean isDateOnly = false;
            if (c.get(10) == 0 && c.get(12) == 0 && c.get(13) == 0) {
                isDateOnly = true;
            }
            c.setTimeZone(TimeZone.getDefault());
            if ("=".equals(operator) || "!=".equals(operator) || ">=".equals(operator) || "<=".equals(operator)) {
                boolean isNestSelect;
                if (isDateOnly) {
                    c.set(9, 0);
                    c.set(10, 0);
                    c.set(12, 0);
                }
                c.set(13, 0);
                boolean bl = isNestSelect = columnid.toLowerCase().indexOf("select ") >= 0;
                if (!isNestSelect) {
                    columnid = isMSS ? (isDateOnly ? "CONVERT(datetime, CONVERT(char," + columnid + ", 102))" : "CONVERT(datetime, CONVERT(char(16)," + columnid + ", 120))") : (isDateOnly ? "trunc(" + columnid + ")" : "trunc(" + columnid + ",'MI')");
                }
            }
            paramname = "timestamp" + this.paramnum++;
            clause = operator + ":" + paramname;
            this.paramMap.put(paramname, c);
            return columnid + clause;
        } else if ("big_decimal".equals(columntype) || "long".equals(columntype) || "int".equals(columntype) || "double".equals(columntype) || "N".equals(columntype) || "R".equals(columntype)) {
            String[] values;
            FormatUtil fmu = FormatUtil.getInstance(this.sapphireConnection);
            if ("between".equals(operator) && (values = StringUtil.split(value, "|")).length == 2) {
                if (values[0].length() > 0 && values[1].length() > 0) {
                    BigDecimal b = fmu.parseBigDecimal(values[0]);
                    BigDecimal b1 = fmu.parseBigDecimal(values[1]);
                    paramname = "big_decimal" + this.paramnum++;
                    paramname1 = "big_decimal" + this.paramnum++;
                    clause = " " + operator + " :" + paramname + " and :" + paramname1;
                    this.paramMap.put(paramname, b);
                    this.paramMap.put(paramname1, b1);
                } else if (values[0].length() > 0) {
                    operator = ">=";
                    value = values[0];
                } else {
                    if (values[1].length() <= 0) return " is not null";
                    operator = "<=";
                    value = values[1];
                }
            }
            if ("between".equals(operator)) return columnid + clause;
            BigDecimal b = fmu.parseBigDecimal(value);
            paramname = "big_decimal" + this.paramnum++;
            clause = operator + ":" + paramname;
            this.paramMap.put(paramname, b);
            return columnid + clause;
        } else {
            if (!"string".equals(columntype) && !"C".equals(columntype)) return columnid + clause;
            if ("in".equals(operator) || "not in".equals(operator)) {
                String[] values = StringUtil.split(value, "|");
                StringBuffer sb = new StringBuffer(" " + operator + " (");
                for (int i = 0; i < values.length; ++i) {
                    paramname = "string" + this.paramnum++;
                    if (i == 0) {
                        sb.append(":" + paramname);
                    } else {
                        sb.append(",:" + paramname);
                    }
                    this.paramMap.put(paramname, values[i]);
                }
                clause = sb.toString() + ")";
                return columnid + clause;
            } else if ("like".equals(operator) || "start with".equals(operator) || "end with".equals(operator)) {
                paramname = "string" + this.paramnum++;
                clause = " like :" + paramname;
                if (!RequestParser.isSelect(columnid)) {
                    value = "like".equals(operator) ? "%" + value.toLowerCase() + "%" : ("start with".equals(operator) ? value.toLowerCase() + "%" : "%" + value.toLowerCase());
                    columnid = "lower(" + columnid + ")";
                } else {
                    value = "like".equals(operator) ? "%" + value + "%" : ("start with".equals(operator) ? value + "%" : "%" + value);
                }
                this.paramMap.put(paramname, value);
                return columnid + clause;
            } else {
                if ("between".equals(operator)) return columnid + clause;
                paramname = "string" + this.paramnum++;
                clause = operator + ":" + paramname;
                this.paramMap.put(paramname, value);
            }
        }
        return columnid + clause;
    }

    String addStringTypeToParamMap(String value) {
        String paramname = "string" + this.paramnum++;
        this.paramMap.put(paramname, value);
        return paramname;
    }

    void setNamedParameters(Query q) {
        Set names = q.getParameterMetadata().getNamedParameterNames();
        names.forEach(name -> {
            Object bindvalue = this.paramMap.get(name);
            if (name.indexOf("timestamp") == 0) {
                q.setParameter(name, (Object)((Calendar)bindvalue).getTime());
            } else if (name.indexOf("big_decimal") == 0) {
                q.setParameter(name, bindvalue);
            } else if (name.indexOf("string") == 0) {
                q.setParameter(name, bindvalue);
            }
        });
    }
}

