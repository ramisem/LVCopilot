/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.util.calculations;

import com.labvantage.sapphire.services.ConnectionInfo;
import com.labvantage.sapphire.util.UnitsUtil;
import com.labvantage.sapphire.util.cache.CacheNames;
import com.labvantage.sapphire.util.cache.CacheUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import sapphire.accessor.QueryProcessor;
import sapphire.util.DataSet;

public class ExpressionPrefix
implements CacheNames {
    public static String prependCalcDefs(ConnectionInfo connectionInfo, QueryProcessor queryProcessor, String expression) {
        int i;
        if (expression.indexOf("$G{") == 0 && (i = (expression = expression.substring(3)).lastIndexOf("}")) > -1) {
            expression = expression.substring(0, i);
        }
        String imports = ExpressionPrefix.getExpressionImports(queryProcessor, expression, connectionInfo.isOracle());
        String defs = ExpressionPrefix.getExpressionDefs(queryProcessor, connectionInfo.getDatabaseId(), expression);
        String prefix = "//startinsert\n" + imports + defs + "\n//endinsert\n";
        String databaseid = connectionInfo.getDatabaseId();
        if (CacheUtil.getCacheSize(databaseid, "UnitConversion") == 0) {
            UnitsUtil.popupulateUnitConversationCache(queryProcessor, databaseid);
        }
        expression = prefix + "\n" + expression;
        return expression;
    }

    private static String getExpressionImports(QueryProcessor queryProcessor, String script, boolean isOracle) {
        String ret = "";
        String sql = "";
        sql = isOracle ? "SELECT distinct namespace, expression expr, dbms_lob.substr(expressionimplementation, 10000, 1) expressionimplementation FROM expression WHERE typeflag = 'J' order by expressionimplementation" : "SELECT distinct namespace, expression expr, expressionimplementation FROM expression WHERE typeflag = 'J' order by expressionimplementation";
        DataSet ds = queryProcessor.getSqlDataSet(sql);
        HashSet<String> s = new HashSet<String>();
        if (ds != null) {
            for (int i = 0; i < ds.getRowCount(); ++i) {
                String expressionimplementation;
                String expression = ds.getValue(i, "expr");
                String patternInScript = ds.getValue(i, "namespace") + "." + (expression.indexOf("(") > 0 ? ds.getValue(i, "expr").substring(0, expression.indexOf("(") + 1) : expression);
                if (script.indexOf(patternInScript) < 0 || (expressionimplementation = ds.getString(i, "expressionimplementation")) == null || expressionimplementation.length() <= 0 || expressionimplementation.indexOf(".") <= 0) continue;
                String packagename = expressionimplementation.substring(0, expressionimplementation.lastIndexOf("."));
                s.add(packagename);
            }
            if (s.size() > 0) {
                Object[] list = s.toArray();
                for (int i = 0; i < list.length; ++i) {
                    ret = ret + "import " + list[i] + ".*;\n";
                }
            }
        }
        return ret;
    }

    private static String getExpressionDefs(QueryProcessor queryProcessor, String databaseid, String script) {
        String sql = "SELECT namespace, typeflag, expressionimplementation, expression FROM expression order by namespace";
        DataSet ds = queryProcessor.getSqlDataSet(sql, true);
        ArrayList<DataSet> groupedList = ds.getGroupedDataSets("namespace");
        ArrayList<String> nameSpaceList = new ArrayList<String>();
        HashSet<String> handledSet = new HashSet<String>();
        HashMap<String, StringBuilder> nameSpaceFunctionsMap = new HashMap<String, StringBuilder>();
        ExpressionPrefix.getExpressionDefsRecursive(handledSet, nameSpaceList, nameSpaceFunctionsMap, groupedList, databaseid, script);
        StringBuilder ret = new StringBuilder();
        for (String namespace : nameSpaceList) {
            if (nameSpaceFunctionsMap.get(namespace) == null || nameSpaceFunctionsMap.get(namespace).length() <= 0) continue;
            ret.append("class " + namespace + "{\n").append((CharSequence)nameSpaceFunctionsMap.get(namespace)).append("};\n");
        }
        return ret.toString();
    }

    private static void getExpressionDefsRecursive(HashSet<String> handledSet, ArrayList<String> nameSpaceList, HashMap<String, StringBuilder> nameSpaceFunctionsMap, ArrayList<DataSet> groupedList, String databaseid, String script) {
        boolean newdefadded = false;
        StringBuilder ret = new StringBuilder();
        for (DataSet ds2 : groupedList) {
            for (int i = 0; i < ds2.getRowCount(); ++i) {
                String namespace = ds2.getString(i, "namespace");
                if (namespace == null || namespace.length() <= 0) continue;
                if (nameSpaceFunctionsMap.get(namespace) == null) {
                    nameSpaceFunctionsMap.put(namespace, new StringBuilder());
                    nameSpaceList.add(0, namespace);
                }
                StringBuilder currdefs = nameSpaceFunctionsMap.get(namespace);
                String typeflag = ds2.getString(i, "typeflag");
                String calculatonclass = ds2.getString(i, "expressionimplementation");
                String expression = ds2.getClob(i, "expression");
                String expressiondef = namespace + "." + expression;
                String findPatternInScript = namespace + "." + (expression.indexOf("(") > 0 ? expression.substring(0, expression.indexOf("(") + 1) : expression);
                if (!handledSet.contains(expressiondef) && script.contains(findPatternInScript) && typeflag != null && typeflag.length() > 0) {
                    if (typeflag.equals("J") && calculatonclass != null && calculatonclass.indexOf(".") > 0) {
                        currdefs.append("def static " + expression);
                        currdefs.append("{" + calculatonclass + "." + expression + " };\n");
                        newdefadded = true;
                        handledSet.add(findPatternInScript);
                    } else if (typeflag.equals("G") && calculatonclass != null && calculatonclass.length() > 0) {
                        if (namespace.equals("LV") && expression.startsWith("unitConv(")) {
                            currdefs.append("def static unitConv( value, fromunit, tounit ) {\n\tcom.labvantage.sapphire.util.UnitsUtil.unitConv( '" + databaseid + "', value, fromunit, tounit );\n};\n");
                        } else if (namespace.equals("LV") && expression.startsWith("unitConvDataItem(")) {
                            currdefs.append("def static unitConvDataItem( dataitem, tounit ) {\n\tcom.labvantage.sapphire.util.UnitsUtil.unitConvDataItem( '" + databaseid + "', dataitem, tounit );\n};\n");
                        } else if (namespace.equals("LV") && expression.startsWith("unitConvDataItemWithDensity(")) {
                            currdefs.append("def static unitConvDataItemWithDensity( dataitem, tounit, densityvalue, densityunit ) {\n\tcom.labvantage.sapphire.util.UnitsUtil.unitConvDataItemWithDensity( '" + databaseid + "', dataitem, tounit, densityvalue, densityunit );\n};\n");
                        } else if (namespace.equals("LV") && expression.startsWith("unitConvWithDensity(")) {
                            currdefs.append("def static unitConvWithDensity( value, fromunit, tounit, densityvalue, densityunit ) {\n\tcom.labvantage.sapphire.util.UnitsUtil.unitConvWithDensity( '" + databaseid + "', value, fromunit, tounit, densityvalue, densityunit );\n};\n");
                        } else {
                            currdefs.append("def static " + expression + " ");
                            if (!calculatonclass.startsWith("{")) {
                                currdefs.append("{");
                            }
                            currdefs.append(calculatonclass);
                            if (!calculatonclass.endsWith("}")) {
                                currdefs.append("}");
                            }
                            currdefs.append(";\n");
                        }
                        newdefadded = true;
                        handledSet.add(expressiondef);
                    }
                }
                if (currdefs.length() <= 0 || !newdefadded) continue;
                ret.append("class " + namespace + "{\n").append((CharSequence)currdefs).append("};\n");
            }
        }
        if (newdefadded) {
            ExpressionPrefix.getExpressionDefsRecursive(handledSet, nameSpaceList, nameSpaceFunctionsMap, groupedList, databaseid, ret.toString());
        }
    }

    public static String stripExpressionPrefix(String text) {
        if (text != null && text.indexOf("//startinsert") > -1 && text.indexOf("//endinsert") > -1) {
            String stripped = text.substring(0, text.indexOf("//startinsert")) + "\n" + text.substring(text.indexOf("//endinsert") + 11);
            stripped = stripped.trim();
            return ExpressionPrefix.stripExpressionPrefix(stripped);
        }
        return text;
    }
}

