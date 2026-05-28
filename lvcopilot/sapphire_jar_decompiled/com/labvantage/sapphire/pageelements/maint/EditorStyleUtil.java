/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.maint;

import com.labvantage.sapphire.BaseCustom;
import java.io.File;
import java.util.HashMap;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class EditorStyleUtil
extends BaseCustom {
    private HashMap<String, String> refValueCache = new HashMap();
    private HashMap<String, DataSet> sqlDataSetCache = new HashMap();
    private HashMap<String, String> sqlValueColumnCache = new HashMap();
    private HashMap<String, String> requestKeydi1Cache = new HashMap();

    public EditorStyleUtil(String connectionid) {
        this.setConnectionId(connectionid);
    }

    public EditorStyleUtil(File rakFile, String connectionid) {
        this.setRakFile(rakFile);
        this.setConnectionId(connectionid);
    }

    public String getStringDisplayValue(String value, PropertyList def) {
        boolean translate;
        String formattedValue;
        block27: {
            if (value == null || value.length() == 0) {
                return "";
            }
            QueryProcessor qp = this.getQueryProcessor();
            formattedValue = value;
            translate = def.getProperty("translatevalue").equals("Y");
            String displayValue = def.getProperty("displayvalue");
            String reftypeid = def.getProperty("reftypeid");
            String sql = def.getProperty("sql");
            String sdcid = def.getProperty("sdcid");
            try {
                if (displayValue.length() > 0) {
                    String[] parts = StringUtil.split(displayValue, ";");
                    for (int i = 0; i < parts.length; ++i) {
                        String part = parts[i];
                        if (!part.startsWith(value + "=")) continue;
                        formattedValue = part.equals(value + "=") ? "" : part.substring(part.indexOf("=") + 1);
                    }
                    break block27;
                }
                if (reftypeid.length() > 0) {
                    String key = reftypeid + ";" + value;
                    if (this.refValueCache.keySet().contains(key)) {
                        formattedValue = this.refValueCache.get(key);
                    } else {
                        SafeSQL safeSql = new SafeSQL();
                        DataSet ds = qp.getPreparedSqlDataSet("SELECT refvaluedesc, refdisplayvalue FROM refvalue WHERE reftypeid=" + safeSql.addVar(reftypeid) + " AND refvalueid=" + safeSql.addVar(value), safeSql.getValues());
                        if (ds.size() > 0) {
                            formattedValue = ds.getValue(0, "refvaluedesc", ds.getValue(0, "refdisplayvalue"));
                        }
                        this.refValueCache.put(key, formattedValue);
                    }
                    break block27;
                }
                if (sql.length() > 0) {
                    boolean revertToDataSet = true;
                    String defValueColumn = def.getProperty("valuecolumn");
                    if (defValueColumn.length() > 0) {
                        try {
                            String cacheValue = this.sqlValueColumnCache.get(sql + ";" + value);
                            if (cacheValue == null) {
                                String tempsql = sql;
                                int i = tempsql.toLowerCase().indexOf(" where ");
                                if (i > 0) {
                                    tempsql = tempsql.substring(i);
                                }
                                SafeSQL safeSQL = new SafeSQL();
                                DataSet ds = qp.getPreparedSqlDataSet(tempsql = tempsql + " where " + defValueColumn + "=" + safeSQL.addVar(value), safeSQL.getValues());
                                if (ds != null && ds.size() > 0) {
                                    String displayColumn = def.getProperty("displaycolumn", ds.getColumnId(1));
                                    formattedValue = ds.getValue(0, displayColumn);
                                    this.sqlValueColumnCache.put(sql + ";" + value, formattedValue);
                                    revertToDataSet = false;
                                } else {
                                    this.sqlValueColumnCache.put(sql + ";" + value, "[[[FAILED_SINGLY]]]");
                                }
                            } else if (!cacheValue.equals("[[[FAILED_SINGLY]]]")) {
                                formattedValue = cacheValue;
                                revertToDataSet = false;
                            }
                        }
                        catch (Exception e) {
                            this.sqlValueColumnCache.put(sql + ";" + value, "[[[FAILED_SINGLY]]]");
                        }
                    }
                    if (revertToDataSet) {
                        DataSet ds = this.sqlDataSetCache.get(sql);
                        if (ds == null) {
                            ds = qp.getSqlDataSet(sql);
                            this.sqlDataSetCache.put(sql, ds);
                        }
                        if (ds != null && ds.getColumnCount() > 1) {
                            String valueColumn = def.getProperty("valuecolumn", ds.getColumnId(0));
                            String displayColumn = def.getProperty("displaycolumn", ds.getColumnId(1));
                            int row = ds.findRow(valueColumn, value);
                            if (row >= 0) {
                                formattedValue = ds.getValue(row, displayColumn);
                            }
                        }
                    }
                    break block27;
                }
                if (sdcid.length() > 0) {
                    String key;
                    SDCProcessor sdcProcessor = this.getSDCProcessor();
                    String keyColid1 = sdcProcessor.getProperty(sdcid, "keycolid1");
                    String defValueColumn = def.getProperty("valuecolumn");
                    if ((defValueColumn.length() == 0 || defValueColumn.equals(keyColid1)) && (formattedValue = this.requestKeydi1Cache.get(key = sdcid + ";" + value)) == null) {
                        String desccol = sdcProcessor.getProperty(sdcid, "desccol");
                        String table = sdcProcessor.getProperty(sdcid, "tableid");
                        String displayColumn = def.getProperty("displaycolumn", desccol);
                        String valueColumn = def.getProperty("valecolumn", keyColid1);
                        SafeSQL safeSQL = new SafeSQL();
                        String tempsql = "SELECT " + displayColumn + " FROM " + table + " WHERE " + valueColumn + "=" + safeSQL.addVar(value);
                        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(tempsql, safeSQL.getValues());
                        formattedValue = ds.getValue(0, displayColumn, value);
                        this.requestKeydi1Cache.put(key, formattedValue);
                    }
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        if (formattedValue == null || formattedValue.length() == 0) {
            formattedValue = value;
        }
        if (translate && formattedValue.length() > 0) {
            formattedValue = this.getTranslationProcessor().translate(formattedValue);
        }
        return formattedValue;
    }
}

