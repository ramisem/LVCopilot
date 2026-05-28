/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.dynamicmaint.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.accessor.QueryProcessor;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class ExtraColumnUtil {
    private QueryProcessor qp;
    private Logger logger;

    public ExtraColumnUtil(QueryProcessor qp, Logger logger) {
        this.qp = qp;
        this.logger = logger;
    }

    public void fillExtraColumnData(List<String> extraColumns, Map<String, String> searchValues, String tableid, DataSet primary, String[] keyColumns) {
        if (primary == null || primary.getRowCount() == 0 || extraColumns == null || extraColumns.size() == 0) {
            return;
        }
        StringBuilder sqlSelect = new StringBuilder();
        for (String keycol : keyColumns) {
            sqlSelect.append(",").append(keycol);
        }
        for (String extracolumn : extraColumns) {
            sqlSelect.append(",").append(extracolumn);
        }
        StringBuilder queryWhere = new StringBuilder();
        ArrayList<String> params = new ArrayList<String>();
        for (Map.Entry<String, String> entry : searchValues.entrySet()) {
            String searchcolumn = entry.getKey();
            String value = entry.getValue();
            if (value == null || value.equals("")) continue;
            if (queryWhere.length() > 0) {
                queryWhere.append(" and ");
            }
            queryWhere.append(searchcolumn).append("=?");
            params.add(value);
        }
        String sql = "SELECT " + sqlSelect.substring(1) + " FROM " + tableid + " WHERE " + queryWhere;
        DataSet dsExtra = this.qp.getPreparedSqlDataSet(sql, params.toArray());
        if (dsExtra != null) {
            this.injectExtraColumnsToDataset(primary, dsExtra, new HashMap<String, String>(), keyColumns);
        } else {
            this.logger.error("Invalid query: " + sql);
        }
    }

    public void injectExtraColumnsToDataset(DataSet dsPrimary, DataSet dsExtra, Map<String, String> actualColumnIds, String[] keyColumns) {
        for (int col = 0; col < dsExtra.getColumnCount(); ++col) {
            String columnid = dsExtra.getColumnId(col);
            int coltype = dsExtra.getColumnType(columnid);
            if (Arrays.asList(keyColumns).contains(columnid)) continue;
            String actualcolumnid = actualColumnIds.get(columnid);
            if (actualcolumnid == null) {
                actualcolumnid = columnid;
            }
            dsPrimary.addColumn(actualcolumnid, coltype);
        }
        for (int prRow = 0; prRow < dsPrimary.getRowCount(); ++prRow) {
            for (int eRow = 0; eRow < dsExtra.getRowCount(); ++eRow) {
                boolean isMatch = true;
                for (String keyCol : keyColumns) {
                    if (dsPrimary.getValue(prRow, keyCol).equals(dsExtra.getValue(eRow, keyCol))) continue;
                    isMatch = false;
                    break;
                }
                if (!isMatch) continue;
                for (int col = 0; col < dsExtra.getColumnCount(); ++col) {
                    String columnid = dsExtra.getColumnId(col);
                    int coltype = dsExtra.getColumnType(columnid);
                    if (Arrays.asList(keyColumns).contains(columnid)) continue;
                    String actualcolumnid = actualColumnIds.get(columnid);
                    if (actualcolumnid == null) {
                        actualcolumnid = columnid;
                    }
                    if (coltype == 0) {
                        dsPrimary.setString(prRow, actualcolumnid, dsExtra.getString(eRow, columnid, ""));
                        continue;
                    }
                    if (coltype == 1) {
                        dsPrimary.setNumber(prRow, actualcolumnid, dsExtra.getBigDecimal(eRow, columnid));
                        continue;
                    }
                    if (coltype != 2) continue;
                    dsPrimary.setDate(prRow, actualcolumnid, dsExtra.getCalendar(eRow, columnid));
                }
            }
        }
    }

    public Map<String, List<String>> parseExtraColumns(Object oFormconfig) {
        HashMap<String, List<String>> extraColumns = new HashMap<String, List<String>>();
        if (oFormconfig == null) {
            return extraColumns;
        }
        if (oFormconfig instanceof PropertyList) {
            PropertyList pageConfig = (PropertyList)oFormconfig;
            for (Object key : pageConfig.keySet()) {
                PropertyListCollection extrafields;
                String elementId = (String)key;
                PropertyList elementConfig = pageConfig.getPropertyList(elementId);
                if (elementConfig == null || (extrafields = elementConfig.getCollection("extrafields")) == null) continue;
                for (int i = 0; i < extrafields.size(); ++i) {
                    ArrayList<String> list;
                    PropertyList extraFieldObj = extrafields.getPropertyList(i);
                    String extrafield = extraFieldObj.getProperty("columnid");
                    String datasource = extraFieldObj.getProperty("tableid");
                    if (datasource.equals("")) {
                        datasource = elementId;
                    }
                    if ((list = (ArrayList<String>)extraColumns.get(datasource)) == null) {
                        list = new ArrayList<String>();
                    }
                    list.add(extrafield);
                    extraColumns.put(datasource, list);
                }
            }
            return extraColumns;
        }
        return extraColumns;
    }

    public Map<String, List<String>> parseExtraColumns(Object oFormconfig, String elementid) {
        HashMap<String, List<String>> extraColumns = new HashMap<String, List<String>>();
        JSONObject jFormconfig = null;
        try {
            JSONObject elementConfig;
            if (oFormconfig instanceof PropertyList) {
                PropertyList pageConfig = (PropertyList)oFormconfig;
                PropertyList elementConfig2 = pageConfig.getPropertyList(elementid);
                if (elementConfig2 == null) {
                    return extraColumns;
                }
                PropertyListCollection extrafields = elementConfig2.getCollection("extrafields");
                if (extrafields != null) {
                    for (int i = 0; i < extrafields.size(); ++i) {
                        ArrayList<String> list;
                        PropertyList extraFieldObj = extrafields.getPropertyList(i);
                        String extrafield = extraFieldObj.getProperty("columnid");
                        String datasource = extraFieldObj.getProperty("tableid");
                        if (datasource.equals("")) {
                            datasource = elementid;
                        }
                        if ((list = (ArrayList<String>)extraColumns.get(datasource)) == null) {
                            list = new ArrayList<String>();
                        }
                        list.add(extrafield);
                        extraColumns.put(datasource, list);
                    }
                }
                return extraColumns;
            }
            if (oFormconfig instanceof JSONObject) {
                jFormconfig = (JSONObject)oFormconfig;
            } else if (oFormconfig instanceof String) {
                jFormconfig = new JSONObject((String)oFormconfig);
            }
            if (jFormconfig != null && (elementConfig = jFormconfig.optJSONObject(elementid)).has("extrafields")) {
                JSONObject extraFieldsArray = elementConfig.getJSONObject("extrafields");
                for (int i = 0; i < extraFieldsArray.length(); ++i) {
                    JSONObject extraFieldObj = extraFieldsArray.getJSONObject("" + i);
                    String extrafield = extraFieldObj.optString("0");
                    String datasource = extraFieldObj.optString("1");
                    ArrayList<String> list = (ArrayList<String>)extraColumns.get(datasource);
                    if (list == null) {
                        list = new ArrayList<String>();
                    }
                    list.add(extrafield);
                    extraColumns.put(datasource, list);
                }
            }
        }
        catch (JSONException e) {
            this.logger.error("Exception parsing JSON object " + oFormconfig, e);
        }
        return extraColumns;
    }

    public String[] getKeyColumns(String tableid) {
        String sql = "SELECT columnid FROM syscolumn WHERE pkflag='Y' AND tableid=?";
        Object[] params = new String[]{tableid};
        DataSet ds = this.qp.getPreparedSqlDataSet(sql, params);
        if (ds != null) {
            return ds.getColumnValues("columnid", ";").split(";");
        }
        return new String[0];
    }

    public void fillExtraColumnData(List<String> extracolumnList, DataSet dataset, String datasetname) {
        for (String extracolumn : extracolumnList) {
            if (!extracolumn.contains("(") || !extracolumn.contains(")")) continue;
            String extracolumnid = extracolumn.substring(extracolumn.lastIndexOf(")") + 1).trim();
            dataset.addColumn(extracolumnid, 0);
            for (int i = 0; i < dataset.getRowCount(); ++i) {
                String addressType;
                String addressId;
                Object[] params;
                DataSet ds;
                String sql = extracolumn.substring(extracolumn.indexOf(40) + 1, extracolumn.lastIndexOf(41)).trim();
                while (sql.contains(datasetname + ".")) {
                    int pos = sql.indexOf(datasetname + ".", 0);
                    int endpos = sql.length();
                    int endpos1 = sql.indexOf(" ", pos);
                    int endpos2 = sql.indexOf(")", pos);
                    if (endpos1 != -1 && (endpos1 < endpos2 || endpos2 == -1)) {
                        endpos = endpos1;
                    } else if (endpos2 != -1 && (endpos2 < endpos1 || endpos1 == -1)) {
                        endpos = endpos2;
                    }
                    String variable = sql.substring(pos, endpos);
                    String columnid = variable.substring(datasetname.length() + 1);
                    String value = dataset.getValue(i, columnid);
                    value = !value.equals("") ? "'" + value + "'" : "null";
                    sql = sql.replaceFirst(variable, value);
                }
                if (sql.toLowerCase().startsWith("select")) {
                    DataSet ds2 = this.qp.getSqlDataSet(sql);
                    if (ds2 == null) continue;
                    dataset.setValue(i, extracolumnid, ds2.getValue(0, ds2.getColumnId(0), ""));
                    continue;
                }
                if (!sql.toLowerCase().startsWith("case") || !datasetname.equals("sdiaddress") || (ds = this.qp.getPreparedSqlDataSet(sql = "SELECT " + sql + " FROM address WHERE addressid=? and addresstype=?", params = new String[]{addressId = dataset.getValue(i, "addressid", ""), addressType = dataset.getValue(i, "addresstype", "")})) == null) continue;
                dataset.setValue(i, extracolumnid, ds.getValue(0, ds.getColumnId(0), ""));
            }
        }
    }
}

