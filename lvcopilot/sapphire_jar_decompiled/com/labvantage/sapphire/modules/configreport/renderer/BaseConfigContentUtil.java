/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.configreport.renderer;

import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;

public class BaseConfigContentUtil {
    protected StringBuffer content;
    protected boolean foundDiff = false;
    public DataSet diffInfo;
    protected String context;

    public StringBuffer append(StringBuffer buffer) {
        if (buffer.indexOf("<diffpoint") > -1) {
            this.foundDiff = true;
        }
        this.content.append(buffer);
        return this.content;
    }

    public StringBuffer append(String string) {
        if (string.contains("<diffpoint")) {
            this.foundDiff = true;
        }
        this.content.append(string);
        return this.content;
    }

    public String toString() {
        return this.content.toString();
    }

    public int length() {
        return this.content.length();
    }

    public boolean getFoundDiff() {
        return this.foundDiff | this.content.indexOf("class=\"diffreport") > -1;
    }

    public void clearContent() {
        this.foundDiff = false;
        this.content = new StringBuffer();
        this.diffInfo = new DataSet();
    }

    public static String getModifiedString(String orig) {
        if (orig == null || orig.length() == 0) {
            orig = "<blank>";
        }
        String ret = "<diffpoint/><font class=\"diffreportmodifieditem\">";
        ret = ret + orig;
        ret = ret + "</font>";
        return ret;
    }

    public static String getDeletedString(String orig) {
        if (orig == null || orig.length() == 0) {
            orig = "<blank>";
        }
        String ret = "<diffpoint/><font class=\"diffreportdeleteditem\">";
        ret = ret + orig;
        ret = ret + "</font>";
        return ret;
    }

    public static String getNewString(String orig) {
        if (orig == null || orig.length() == 0) {
            orig = "<blank>";
        }
        String ret = "<diffpoint/><font class=\"diffreportnewitem\">";
        ret = ret + orig;
        ret = ret + "</font>";
        return ret;
    }

    public static DataSet removeAuditColumns(DataSet orig) {
        if (orig == null) {
            return orig;
        }
        DataSet clean = new DataSet();
        String[] cols = orig.getColumns();
        for (int i = 0; i < cols.length; ++i) {
            if (cols[i].equals("createdt") || cols[i].equals("moddt") || cols[i].equals("modby") || cols[i].equals("createby") || cols[i].equals("createtool") || cols[i].equals("modtool") || cols[i].equals("auditsequence") || cols[i].equals("tracelogid")) continue;
            String values = orig.getColumnValues(cols[i], "|!|");
            clean.addColumn(cols[i], orig.getColumnType(cols[i]));
            clean.addColumnValues(cols[i], orig.getColumnType(cols[i]), values, "|!|");
        }
        return clean;
    }

    public static DataSet parseDisplayValues(String displayValue) throws SapphireException {
        DataSet p = new DataSet();
        p.addColumn("value", 0);
        p.addColumn("displayvalue", 0);
        String[] options = StringUtil.split(displayValue, ";");
        for (int i = 0; i < options.length; ++i) {
            if (options[i].indexOf("=") > -1) {
                String lhs = "Others";
                if (options[i].indexOf("=") != 0) {
                    lhs = options[i].substring(0, options[i].indexOf("="));
                }
                String rhs = options[i].substring(options[i].indexOf("=") + 1);
                int r = p.addRow();
                p.setValue(r, "value", lhs);
                p.setValue(r, "displayvalue", rhs);
                continue;
            }
            if (options[i].length() <= 0) continue;
            int r = p.addRow();
            p.setValue(r, "value", "Default");
            p.setValue(r, "displayvalue", options[i]);
        }
        return p;
    }

    public static String getRefTypeValue(QueryProcessor queryProcessor, String reftypeid, String value) {
        SafeSQL safeSQL = new SafeSQL();
        String sql = "select refvalueid, refdisplayvalue from refvalue where reftypeid = " + safeSQL.addVar(reftypeid) + " and refvalueid=" + safeSQL.addVar(value);
        DataSet ds = queryProcessor.getPreparedSqlDataSet(sql, safeSQL.getValues());
        if (ds.getRowCount() > 0) {
            return ds.getValue(0, "refdisplayvalue", value);
        }
        return value;
    }
}

