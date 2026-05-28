/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.dynamicmaint.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.action.ActionConstants;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public final class Utils
implements ActionConstants {
    public static final String DYM_PREFIX = "DYM_";
    private static Pattern intPattern = Pattern.compile("[0-9*]+");

    private Utils() {
    }

    public static void appendToPL(PropertyList map, String key, String value) {
        Utils.appendToPL(map, key, value, true);
    }

    public static void appendToPL(PropertyList map, String key, String value, boolean useLvNull) {
        String valueToBeSaved;
        if (value == null) {
            value = "";
        }
        if ((valueToBeSaved = value.equals("") && useLvNull ? "(null)" : value).contains(";")) {
            valueToBeSaved = valueToBeSaved.replaceAll(";", "#semicolon#");
        }
        if (!map.containsKey(key)) {
            map.setProperty(key, valueToBeSaved);
        } else {
            String s = map.getProperty(key);
            map.setProperty(key, s + ";" + valueToBeSaved);
        }
    }

    public static void appendToPL(PropertyList map, String key, String value, int numtimes) {
        if (numtimes < 1) {
            return;
        }
        if (value == null) {
            value = "";
        }
        if (value.contains(";")) {
            value = value.replaceAll(";", " ");
        }
        StringBuilder valueArray = new StringBuilder();
        for (int i = 0; i < numtimes; ++i) {
            if (i > 0) {
                valueArray.append(";");
            }
            valueArray.append(value);
        }
        if (!map.containsKey(key)) {
            map.setProperty(key, valueArray.toString());
        } else {
            String s = map.getProperty(key);
            map.setProperty(key, s + ";" + valueArray.toString());
        }
    }

    public static String notNull(String s) {
        return Utils.notNull(s, "");
    }

    public static String notNull(String s, String defaultvalue) {
        if (s == null) {
            return Utils.notNull(defaultvalue);
        }
        return s;
    }

    public static boolean isInt(String i) {
        Matcher m = intPattern.matcher(i);
        return m.matches();
    }

    public static int s2i(String s) {
        return Utils.s2i(s, 0);
    }

    public static int s2i(String s, int defaultvalue) {
        int retval = s == null ? defaultvalue : (Utils.isInt(s) ? Integer.parseInt(s) : defaultvalue);
        return retval;
    }

    public static void multiplyItems(PropertyList map, int cnt, boolean ignoreKeys) {
        for (Map.Entry entry : map.entrySet()) {
            String[] valueList;
            String key = (String)entry.getKey();
            String value = (String)entry.getValue();
            if (ignoreKeys && (key.equals("keyid1") || key.equals("keyid2") || key.equals("keyid3"))) continue;
            StringBuilder finalValue = new StringBuilder();
            for (String aValueList : valueList = value.split(";")) {
                for (int i = 0; i < cnt; ++i) {
                    if (finalValue.length() > 0) {
                        finalValue.append(";");
                    }
                    finalValue.append(aValueList);
                }
            }
            map.setProperty(key, finalValue.toString());
        }
    }

    public static PropertyListCollection dsToCollection(DataSet ds) {
        PropertyListCollection retval = new PropertyListCollection();
        for (int i = 0; i < ds.getRowCount(); ++i) {
            PropertyList collection = new PropertyList();
            for (int j = 0; j < ds.getColumnCount(); ++j) {
                String columnid = ds.getColumnId(j);
                String value = ds.getValue(i, columnid);
                if (value.equals("")) continue;
                collection.setProperty(columnid, ds.getValue(i, columnid));
            }
            retval.add(i, collection);
        }
        return retval;
    }

    public static void fillKeyids(DataSet ds, String keyid1, String keyid2, String keyid3) {
        if (ds == null) {
            return;
        }
        ds.addColumn("keyid1", 0);
        ds.addColumn("keyid2", 0);
        ds.addColumn("keyid3", 0);
        String[] arrKeyid1 = keyid1.split(";");
        String[] arrKeyid2 = keyid2.split(";");
        String[] arrKeyid3 = keyid3.split(";");
        int datasetRowCount = ds.getRowCount();
        for (int i = 0; i < arrKeyid1.length; ++i) {
            for (int j = 0; j < datasetRowCount; ++j) {
                if (i == 0) {
                    ds.setString(j, "keyid1", arrKeyid1[0]);
                    ds.setString(j, "keyid2", arrKeyid2[0]);
                    ds.setString(j, "keyid3", arrKeyid3[0]);
                    continue;
                }
                int newrow = Utils.copyRow(ds, j);
                ds.setString(newrow, "keyid1", arrKeyid1[i]);
                ds.setString(newrow, "keyid2", arrKeyid2.length > i ? arrKeyid2[i] : "(null)");
                ds.setString(newrow, "keyid3", arrKeyid3.length > i ? arrKeyid3[i] : "(null)");
            }
        }
    }

    private static int copyRow(DataSet ds, int rownum) {
        int newrow = ds.addRow();
        for (int i = 0; i < ds.getColumnCount(); ++i) {
            String columnid = ds.getColumnId(i);
            ds.setValue(newrow, columnid, ds.getValue(rownum, columnid));
        }
        return newrow;
    }

    public static String replaceVariables(String str, PropertyList globalValues) {
        StringBuilder retval = new StringBuilder();
        char[] chars = str.toCharArray();
        boolean isVariable = false;
        StringBuilder variable = new StringBuilder();
        for (char c : chars) {
            if (c == '[') {
                isVariable = true;
                continue;
            }
            if (c == ']') {
                isVariable = false;
                retval.append(globalValues.get(variable.toString()));
                variable = new StringBuilder();
                continue;
            }
            if (isVariable) {
                variable.append(c);
                continue;
            }
            retval.append(c);
        }
        return retval.toString();
    }

    public static String getLatestVersion(String keyid1, PropertyList sdcProps, QueryProcessor qp, boolean isOra) {
        Object[] params;
        String sql;
        DataSet ds;
        String retVal = "";
        String tableId = sdcProps.getProperty("tableid");
        String primaryKeyField = sdcProps.getProperty("keycolid1");
        String versionidField = sdcProps.getProperty("keycolid2");
        if (versionidField != null && (retVal = (ds = qp.getPreparedSqlDataSet(sql = isOra ? "SELECT max(to_number(" + versionidField + ")) AS keyid2 FROM " + tableId + " WHERE " + primaryKeyField + "=? AND versionstatus='C' " : "SELECT max(convert(int, " + versionidField + ")) AS keyid2 FROM " + tableId + " WHERE " + primaryKeyField + "=? AND versionstatus='C' ", params = new String[]{keyid1})).getValue(0, "keyid2", "")).equals("")) {
            sql = isOra ? "SELECT max(to_number(" + versionidField + ")) AS keyid2 FROM " + tableId + " WHERE " + primaryKeyField + "=? " : "SELECT max(convert(int, " + versionidField + ")) AS keyid2 FROM " + tableId + " WHERE " + primaryKeyField + "=? ";
            ds = qp.getPreparedSqlDataSet(sql, params);
            retVal = ds.getValue(0, "keyid2", "");
        }
        return retVal;
    }

    public static Map<String, PropertyListCollection> extractNotifyColumns(PropertyListCollection columns) {
        HashMap<String, PropertyListCollection> notifyColumns = new HashMap<String, PropertyListCollection>();
        for (int i = 0; i < columns.size(); ++i) {
            PropertyList col = columns.getPropertyList(i);
            String columnid = col.getProperty("columnid", "");
            String hideRule = col.getProperty("hiderule", "");
            String readOnlyRule = col.getProperty("readonlyrule", "");
            String dropDownSql = col.getProperty("dropdownsql", "");
            Utils.populateNotifyColumns(notifyColumns, columnid, Utils.extractKeys(hideRule), "hiderule", hideRule);
            Utils.populateNotifyColumns(notifyColumns, columnid, Utils.extractKeys(readOnlyRule), "readonlyrule", readOnlyRule);
            Utils.populateNotifyColumns(notifyColumns, columnid, Utils.extractKeys(dropDownSql), "refreshdropdown", dropDownSql);
        }
        return notifyColumns;
    }

    private static void populateNotifyColumns(Map<String, PropertyListCollection> notifyColumns, String columnid, String[] keys, String function, String expression) {
        for (String key : keys) {
            PropertyListCollection coll = notifyColumns.get(key);
            if (coll == null) {
                coll = new PropertyListCollection();
            }
            if (columnid.equals("") || expression.equals("")) continue;
            PropertyList pl = new PropertyList();
            pl.setProperty("columnid", columnid);
            pl.setProperty("func", function);
            pl.setProperty("expression", expression);
            coll.add(pl);
            notifyColumns.put(key, coll);
        }
    }

    private static String[] extractKeys(String hideRule) {
        ArrayList<String> retVal = new ArrayList<String>();
        boolean isKey = false;
        StringBuilder key = new StringBuilder();
        for (char c : hideRule.toCharArray()) {
            if (c == '[') {
                isKey = true;
                continue;
            }
            if (c == ']') {
                isKey = false;
                retVal.add(key.toString());
                key = new StringBuilder();
                continue;
            }
            if (!isKey) continue;
            key.append(c);
        }
        return retVal.toArray(new String[retVal.size()]);
    }

    public static PropertyList formatDropDownSql(String dropDownSql, String simplecolumnid, String elementId) {
        String sqlId;
        String sqlIdAndVariables = sqlId = "sql_" + elementId + "_" + simplecolumnid;
        char[] chars = dropDownSql.toCharArray();
        boolean isVariable = false;
        String variable = "";
        for (char c : chars) {
            if (c == '[') {
                isVariable = true;
                continue;
            }
            if (c == ']') {
                isVariable = false;
                sqlIdAndVariables = sqlIdAndVariables + ";[" + variable + "]";
                variable = "";
                continue;
            }
            if (!isVariable) continue;
            variable = variable + c;
        }
        PropertyList pl = new PropertyList();
        pl.setProperty("sqlid", sqlId);
        pl.setProperty("sql", dropDownSql);
        pl.setProperty("sqlAndVariables", sqlIdAndVariables);
        return pl;
    }

    public static String setToString(Set<String> set, String separator) {
        StringBuilder retVal = new StringBuilder();
        for (String item : set) {
            if (retVal.length() > 0) {
                retVal.append(separator);
            }
            retVal.append(item);
        }
        return retVal.toString();
    }

    public static void getEditorStyleConfig(String editorStyle, PropertyList columnProps, QueryProcessor qp, Logger logger) {
        String sql = "SELECT editordefinition FROM editorstyle WHERE editorstyleid=? ";
        Object[] params = new String[]{editorStyle};
        DataSet ds = qp.getPreparedSqlDataSet(sql, params, true);
        String sEditorDefinition = ds.getValue(0, "editordefinition", "");
        if (!sEditorDefinition.equals("")) {
            String[] validationArr;
            PropertyList lookupProps;
            PropertyList editorDefinitionProps = new PropertyList();
            try {
                editorDefinitionProps.setPropertyList(sEditorDefinition);
            }
            catch (SapphireException e) {
                logger.error("Invalid Editor Style property list ", e);
            }
            String columnType = editorDefinitionProps.getProperty("mode", "");
            if (columnType.equals("lookup")) {
                columnProps.setProperty("columntype", "lookup");
                lookupProps = columnProps.getPropertyListNotNull("lookup");
                lookupProps.setProperty("lookupsdcid", editorDefinitionProps.getPropertyListNotNull("lookuplink").getProperty("sdcid"));
                lookupProps.setProperty("dynamiclookup", editorDefinitionProps.getPropertyListNotNull("lookuplink").getProperty("enablesuggest").equals("Yes") ? "Y" : "N");
                lookupProps.setProperty("lookuppage", editorDefinitionProps.getPropertyListNotNull("lookuplink").getProperty("href"));
                lookupProps.setProperty("extendedwhere", editorDefinitionProps.getPropertyListNotNull("lookuplink").getProperty("restrictivewhere"));
                lookupProps.setProperty("selectortype", editorDefinitionProps.getPropertyListNotNull("lookuplink").getProperty("selectortype"));
                columnProps.setProperty("reftypeid", editorDefinitionProps.getPropertyListNotNull("lookuplink").getProperty("reftypeid"));
                PropertyListCollection lookupColumns = editorDefinitionProps.getPropertyListNotNull("lookuplink").getCollectionNotNull("columns");
                lookupProps.setProperty("lookupcolumns", lookupColumns);
                lookupProps.setProperty("tooltip", editorDefinitionProps.getPropertyListNotNull("lookuplink").getProperty("tip"));
                columnProps.setProperty("lookup", lookupProps);
            } else if (columnType.equals("dropdownlist") || columnType.equals("dropdowncombo")) {
                columnProps.setProperty("columntype", columnType);
                columnProps.setProperty("reftypeid", editorDefinitionProps.getProperty("reftypeid", ""));
                columnProps.setProperty("dropdownsql", editorDefinitionProps.getProperty("sql", ""));
                if (!editorDefinitionProps.getProperty("sdcid").equals("")) {
                    lookupProps = columnProps.getPropertyListNotNull("lookup");
                    lookupProps.setProperty("lookupsdcid", editorDefinitionProps.getProperty("sdcid"));
                    columnProps.setProperty("lookup", lookupProps);
                    columnProps.setProperty("queryfrom", editorDefinitionProps.getProperty("queryfrom"));
                    columnProps.setProperty("querywhere", editorDefinitionProps.getProperty("querywhere"));
                    columnProps.setProperty("queryorderby", editorDefinitionProps.getProperty("queryorderby"));
                    columnProps.setProperty("valuecolumn", editorDefinitionProps.getProperty("valuecolumn"));
                    columnProps.setProperty("displaycolumn", editorDefinitionProps.getProperty("displaycolumn"));
                }
                if (columnType.equals("dropdowncombo")) {
                    columnProps.setProperty("iseditable", "Y");
                }
            } else if (columnType.equals("datelookup")) {
                columnProps.setProperty("columntype", "datelookup");
            } else if (columnType.equals("checkbox")) {
                columnProps.setProperty("columntype", "checkbox");
                columnProps.setProperty("checkboxY", editorDefinitionProps.getProperty("checkedvalue", "Y"));
                columnProps.setProperty("checkboxN", editorDefinitionProps.getProperty("uncheckedvalue", "N"));
            }
            String validations = editorDefinitionProps.getProperty("validation");
            if (!validations.equals("")) {
                columnProps.setProperty("validation", validations);
            }
            for (String validation : validationArr = validations.split(";")) {
                if (!validation.startsWith("Length") || validation.contains("Length([]<") || validation.contains("Length( to") || validation.equals("Length([]==0)")) continue;
                columnProps.setProperty("mandatory", "Y");
            }
            columnProps.setProperty("pseudo", editorDefinitionProps.getProperty("pseudocolumn", ""));
            columnProps.setProperty("displayvalue", editorDefinitionProps.getProperty("displayvalue", ""));
            columnProps.setProperty("translate", editorDefinitionProps.getProperty("translatevalue", ""));
        }
    }

    public static PropertyListCollection getColumnConfig(PropertyListCollection columns, String dataSource) {
        PropertyListCollection simpleColumnConfig = new PropertyListCollection();
        for (int i = 0; i < columns.size(); ++i) {
            PropertyList columnProps = new PropertyList();
            String simpleColumnId = columns.getPropertyList(i).getProperty("columnid");
            if (simpleColumnId.contains(")")) {
                simpleColumnId = simpleColumnId.substring(simpleColumnId.lastIndexOf(41) + 1).trim();
            } else if (simpleColumnId.contains(".")) {
                simpleColumnId = simpleColumnId.replaceAll("\\.", "_");
            }
            if (columnProps.getProperty("isparentcolumn").equals("Y")) {
                dataSource = "parent";
            } else if (!columns.getPropertyList(i).getProperty("tableid", "").equals("")) {
                dataSource = columns.getPropertyList(i).getProperty("tableid");
            }
            if (simpleColumnId.startsWith(dataSource + "_")) {
                simpleColumnId = simpleColumnId.substring(dataSource.length() + 1);
            }
            columnProps.setProperty("columnid", simpleColumnId);
            columnProps.setProperty("datasource", dataSource);
            if (columns.getPropertyList(i).getProperty("translate").equals("Y")) {
                columnProps.setProperty("translate", "Y");
            }
            simpleColumnConfig.add(columnProps);
        }
        return simpleColumnConfig;
    }

    public static void translateRefDisplayValues(DataSet ds, TranslationProcessor tp, String language, String context) {
        for (int i = 0; i < ds.getRowCount(); ++i) {
            String transValue = tp.translate(ds.getString(i, "refdisplayvalue", ""), language, context);
            ds.setValue(i, "refdisplayvalue", transValue);
        }
    }

    public static Map<String, List<String>> getTranslateColumns(PropertyList pageConfig) {
        HashMap<String, List<String>> translateColumns = new HashMap<String, List<String>>();
        for (Object key : pageConfig.keySet()) {
            List<String> columnList;
            String columnId;
            int i;
            String elementId = (String)key;
            PropertyList elementConfig = pageConfig.getPropertyList(elementId);
            if (elementConfig == null) continue;
            PropertyListCollection columnCollection = elementConfig.getCollectionNotNull("columns");
            for (int i2 = 0; i2 < columnCollection.size(); ++i2) {
                PropertyList columnProps = columnCollection.getPropertyList(i2);
                String columnId2 = columnProps.getProperty("columnid");
                boolean translate = columnProps.getProperty("translate", "N").equals("Y");
                if (!translate) continue;
                String dataSource = columnProps.getProperty("datasource");
                ArrayList<String> columnList2 = (ArrayList<String>)translateColumns.get(dataSource);
                if (columnList2 == null) {
                    columnList2 = new ArrayList<String>();
                }
                columnList2.add(columnId2);
                translateColumns.put(dataSource, columnList2);
            }
            String elementType = elementConfig.getProperty("type", "");
            if (!elementType.equals("multimaint")) continue;
            columnCollection = elementConfig.getPropertyListNotNull("detailcollection").getCollectionNotNull("secondarycolumns");
            String dataSource = "secondary";
            for (i = 0; i < columnCollection.size(); ++i) {
                PropertyList columnProps = columnCollection.getPropertyList(i);
                columnId = columnProps.getProperty("columnid");
                boolean translate = columnProps.getProperty("translate", "N").equals("Y");
                if (!translate) continue;
                columnList = (ArrayList<String>)translateColumns.get(dataSource);
                if (columnList == null) {
                    columnList = new ArrayList<String>();
                }
                columnList.add(columnId);
                translateColumns.put(dataSource, columnList);
            }
            columnCollection = elementConfig.getPropertyListNotNull("detailcollection").getCollectionNotNull("detailcolumns");
            dataSource = elementConfig.getPropertyListNotNull("detailcollection").getProperty("detailcollectionitem");
            if (elementConfig.getPropertyListNotNull("detailcollection").getProperty("detailcollectiontype").equals("many-to-many")) {
                dataSource = elementId + "m2m";
            }
            for (i = 0; i < columnCollection.size(); ++i) {
                PropertyList columnProps = columnCollection.getPropertyList(i);
                columnId = columnProps.getProperty("columnid");
                boolean translate = columnProps.getProperty("translate", "N").equals("Y");
                elementConfig.getPropertyListNotNull("detailcollection").getProperty("detailcollectionitem");
                if (!translate) continue;
                columnList = (List)translateColumns.get(dataSource);
                if (columnList == null) {
                    columnList = new ArrayList();
                }
                columnList.add(columnId);
                translateColumns.put(dataSource, columnList);
            }
        }
        return translateColumns;
    }

    public static void translateData(DataSet ds, List<String> columnIds, String language, String context, TranslationProcessor tp) {
        if (columnIds == null) {
            return;
        }
        for (int i = 0; i < ds.getRowCount(); ++i) {
            for (String columnId : columnIds) {
                String value;
                if (!ds.isValidColumn(columnId + "__trans")) {
                    ds.addColumn(columnId + "__trans", 0);
                }
                if ((value = ds.getValue(i, columnId, "")).equals("")) continue;
                String transValue = tp.translate(value, language, context);
                ds.setValue(i, columnId + "__trans", transValue);
            }
        }
    }
}

