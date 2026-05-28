/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.util.samplingplan;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.util.evaluator.ExpressionEvaluator;
import com.labvantage.sapphire.util.evaluator.ParseException;
import com.labvantage.sapphire.util.groovy.GroovyUtil;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.DataSet;
import sapphire.util.FormatUtil;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class SamplingPlanUtil {
    static final String LABVANTAGE_CVS_ID = "$Revision: 98365 $";
    public static final String PROPERTY_COPYFROMSAMPLINGPLANLEVELID = "copyfromsamplingplanlevelid";
    public static final String PROPERTY_COPYFROMSAMPLINGPLANID = "copyfromsamplingplanid";
    public static final String PROPERTY_COPYFROMSAMPLINGPLANVERSIONID = "copyfromsamplingplanversionid";

    public static String getNewRowEditor(QueryProcessor qp, String rowIndex, TranslationProcessor tp) {
        DataSet levelds = qp.getRefTypeDataSet("ProdVariantRuleLevel");
        DataSet levelruletypesds = qp.getRefTypeDataSet("LevelRuleTypes");
        StringBuffer rowEditor = new StringBuffer();
        rowEditor.append(tp.translate("Set Level to "));
        int rowid = Integer.parseInt(rowIndex);
        String levelid = "level_" + rowid;
        rowEditor.append("<select name=\"level\" id=\"" + levelid + "\">");
        for (int i = 0; i < levelds.size(); ++i) {
            rowEditor.append("<option value=\"").append(levelds.getValue(i, "refvalueid")).append("\">");
            if (levelds.getValue(i, "refdisplayvalue").length() > 0) {
                rowEditor.append(tp.translate(levelds.getValue(i, "refdisplayvalue")));
            } else {
                rowEditor.append(tp.translate(levelds.getValue(i, "refvalueid")));
            }
            rowEditor.append("</option>");
        }
        rowEditor.append("</select>").append("&nbsp;");
        String ruletypeid = "ruletype_" + rowid;
        rowEditor.append("<select name=\"ruletype\"  id=\"" + ruletypeid + "\" onchange=\"prodvariantruleeditor.getEdiorforRuleType(this)\">");
        for (int j = 0; j < levelruletypesds.size(); ++j) {
            rowEditor.append("<option value=\"").append(levelruletypesds.getValue(j, "refvalueid")).append("\">");
            if (levelruletypesds.getValue(j, "refdisplayvalue").length() > 0) {
                rowEditor.append(tp.translate(levelruletypesds.getValue(j, "refdisplayvalue")));
            } else {
                rowEditor.append(tp.translate(levelruletypesds.getValue(j, "refvalueid")));
            }
            rowEditor.append("</option>");
        }
        rowEditor.append("</select> ");
        return rowEditor.toString();
    }

    public static DataSet getRefTypeValues(QueryProcessor qp, String reftypeid) {
        return qp.getRefTypeDataSet(reftypeid);
    }

    public static String displayRulesForAGivenState(String ruledef, String rule, QueryProcessor qp) {
        StringBuffer ruleStr = new StringBuffer();
        if (rule.equalsIgnoreCase("levelrule")) {
            try {
                JSONObject ruledefObj = new JSONObject(ruledef);
                JSONObject rules = ruledefObj.getJSONObject("rules");
                Iterator it = rules.keys();
                while (it.hasNext()) {
                    JSONObject ruleObj = rules.getJSONObject(it.next().toString());
                    String level = ruleObj.getString("level");
                    String ruleno = ruleObj.getString("ruleno");
                    String ruletype = ruleObj.getString("ruletype");
                    JSONObject data = ruleObj.getJSONObject("data");
                    ruleStr.append("<tr>");
                    ruleStr.append("<td><input type=\"checkbox\" name=\"rowcb\"></td>");
                    ruleStr.append("<td>");
                    ruleStr.append("</td>");
                    ruleStr.append("</tr>");
                }
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return ruleStr.toString();
    }

    public static String getNewEditorForTransitionRules(QueryProcessor qp, String rowIndex, String pvrstate, TranslationProcessor tp) {
        DataSet statesDS = qp.getRefTypeDataSet("ProdVariantState");
        DataSet transitionRTDS = qp.getRefTypeDataSet("TransitionRuleTypes");
        DataSet transitionFlagDS = qp.getRefTypeDataSet("StateTransitionFlag");
        DataSet batchStatusDS = qp.getRefTypeDataSet("Disposition");
        StringBuffer rowEditor = new StringBuffer();
        rowEditor.append(tp.translate("Set new state to "));
        int rowid = Integer.parseInt(rowIndex);
        String stateid = "state_" + rowid;
        rowEditor.append("<select name=\"state\" id=\"" + stateid + "\">");
        for (int i = 0; i < statesDS.size(); ++i) {
            if (statesDS.getValue(i, "refvalueid").equals(pvrstate)) continue;
            rowEditor.append("<option value=\"").append(statesDS.getValue(i, "refvalueid")).append("\">");
            if (statesDS.getValue(i, "refdisplayvalue").length() > 0) {
                rowEditor.append(tp.translate(statesDS.getValue(i, "refdisplayvalue")));
            } else {
                rowEditor.append(tp.translate(statesDS.getValue(i, "refvalueid")));
            }
            rowEditor.append("</option>");
        }
        rowEditor.append("</select>").append("&nbsp;");
        String autoflagid = "autoflag_" + rowid;
        rowEditor.append("<select name=\"autoflag\" id=\"" + autoflagid + "\">");
        for (int i = 0; i < transitionFlagDS.size(); ++i) {
            rowEditor.append("<option value=\"").append(transitionFlagDS.getValue(i, "refvalueid")).append("\">");
            if (transitionFlagDS.getValue(i, "refdisplayvalue").length() > 0) {
                if (transitionFlagDS.getValue(i, "refdisplayvalue").equalsIgnoreCase("Automatically")) {
                    rowEditor.append("");
                } else {
                    rowEditor.append(tp.translate(transitionFlagDS.getValue(i, "refdisplayvalue")));
                }
            } else {
                rowEditor.append(tp.translate(transitionFlagDS.getValue(i, "refvalueid")));
            }
            rowEditor.append("</option>");
        }
        rowEditor.append("</select>").append(tp.translate(" on "));
        String batchCountId = "batchCount_" + rowIndex;
        rowEditor.append("<input type=\"text\" name=\"batchCount\" id=\"" + batchCountId + "\"size=\"3\" > ");
        String batchConsecutiveId = "batchConsecutive_" + rowIndex;
        rowEditor.append("&nbsp;<select name=\"consecutiveItems\" id=\"" + batchConsecutiveId + "\">").append("<option value=\"N\"></option>").append("<option value=\"Y\">" + tp.translate("consecutively") + "</option>").append("</select>&nbsp;");
        String batchStatusId = "batchStatus_" + rowid;
        rowEditor.append("<select name=\"batchStatus\" id=\"" + batchStatusId + "\">");
        for (int i = 0; i < batchStatusDS.size(); ++i) {
            rowEditor.append("<option value=\"").append(batchStatusDS.getValue(i, "refvalueid")).append("\">").append(tp.translate(batchStatusDS.getValue(i, "refvalueid"))).append("</option>");
        }
        rowEditor.append("</select>").append(" ").append(tp.translate(" item(s)  "));
        String ruletypeid = "ruletype_" + rowid;
        rowEditor.append("<select name=\"ruletype\"  id=\"" + ruletypeid + "\" onchange=\"prodvariantruleeditor.getEdiorforRuleType(this)\">");
        rowEditor.append("<option value=\"Blank\"></option>");
        for (int j = 0; j < transitionRTDS.size(); ++j) {
            rowEditor.append("<option value=\"").append(transitionRTDS.getValue(j, "refvalueid")).append("\">");
            if (transitionRTDS.getValue(j, "refdisplayvalue").length() > 0) {
                rowEditor.append(tp.translate(transitionRTDS.getValue(j, "refdisplayvalue")));
            } else {
                rowEditor.append(tp.translate(transitionRTDS.getValue(j, "refvalueid")));
            }
            rowEditor.append("</option>");
        }
        rowEditor.append("</select> ");
        return rowEditor.toString();
    }

    public static PropertyList getPolicyPropertyList(String sdc, ConfigurationProcessor cfg) {
        String policyId = "SamplingPlanPolicy";
        String nodeId = "Sapphire Custom";
        String propertyId = "samplingplans";
        PropertyList policy = null;
        PropertyList pl = new PropertyList();
        try {
            policy = cfg.getPolicy(policyId, nodeId);
            PropertyListCollection plc = policy.getCollectionNotNull(propertyId);
            for (int i = 0; i < plc.size(); ++i) {
                String sdcId;
                pl = plc.getPropertyList(i);
                if (pl == null || !(sdcId = pl.getProperty("sdcid", "")).equals(sdc)) continue;
                return pl;
            }
        }
        catch (Exception e) {
            Trace.logError("Failed to retrieve policy.", e.getMessage());
        }
        return pl;
    }

    public static DataSet getSamplingPlanColumnMapFromPolicy(String sdcId, String prodVariantType, ConfigurationProcessor cfg) {
        DataSet columnMap = new DataSet();
        columnMap.addColumn("sdccolumnid", 0);
        columnMap.addColumn("prodvariantcolumnid", 0);
        PropertyList prodVarPropList = SamplingPlanUtil.getProdVariantPropertyList(sdcId, prodVariantType, cfg);
        if (prodVarPropList != null) {
            PropertyListCollection columnCols = prodVarPropList.getCollectionNotNull("columns");
            for (int j = 0; j < columnCols.size(); ++j) {
                PropertyList plColumns = columnCols.getPropertyList(j);
                String sdcColumn = plColumns.getProperty("sdccolumn", "");
                String prodVariantColumn = plColumns.getProperty("prodvariantcolumn", "");
                if (sdcColumn.trim().length() <= 0 || prodVariantColumn.trim().length() <= 0) continue;
                int row = columnMap.addRow();
                columnMap.setString(row, "sdccolumnid", sdcColumn);
                columnMap.setString(row, "prodvariantcolumnid", prodVariantColumn);
            }
        }
        return columnMap;
    }

    public static String getSamplingPlanSDIToCreateFromPolicy(String sdcId, ConfigurationProcessor cfg) {
        PropertyList pl = SamplingPlanUtil.getPolicyPropertyList(sdcId, cfg);
        String SDIToCreate = pl.getProperty("sdicreatedforsdc", "");
        return SDIToCreate;
    }

    public static String getBaseSDCFromPolicy(String sdcId, ConfigurationProcessor cfg) {
        PropertyList pl = SamplingPlanUtil.getPolicyPropertyList(sdcId, cfg);
        String basesdc = pl.getProperty("basedonsdc", "");
        return basesdc;
    }

    public static String getIntermediateStageSDCFromPolicy(String sdcId, ConfigurationProcessor cfg) {
        PropertyList pl = SamplingPlanUtil.getPolicyPropertyList(sdcId, cfg);
        String stageSdc = pl.getProperty("intermediatestagesdc", "");
        return stageSdc;
    }

    public static DataSet getSamplingPlanUpdateColumnMapFromPolicy(String sdcId, ConfigurationProcessor cfg, String propertyName) {
        DataSet updateColMap = new DataSet();
        PropertyList pl = SamplingPlanUtil.getPolicyPropertyList(sdcId, cfg);
        if (pl != null) {
            PropertyListCollection populateColumnCols = pl.getCollectionNotNull(propertyName);
            for (int j = 0; j < populateColumnCols.size(); ++j) {
                PropertyList plColumns = populateColumnCols.getPropertyList(j);
                String column = plColumns.getProperty("column", "");
                String sdiColumn = plColumns.getProperty("sdicolumn", "");
                if (column.trim().length() <= 0 || sdiColumn.trim().length() <= 0) continue;
                int row = updateColMap.addRow();
                updateColMap.setString(row, "column", column);
                updateColMap.setString(row, "sdicolumn", sdiColumn);
            }
        }
        return updateColMap;
    }

    public static String getSamplingPlanSizeColumnId(String sdcId, ConfigurationProcessor cfg) {
        String columnId = "";
        PropertyList pl = SamplingPlanUtil.getPolicyPropertyList(sdcId, cfg);
        if (pl != null) {
            columnId = pl.getProperty("sizecolumnid", "");
        }
        return columnId;
    }

    public static boolean evaluateLevelRule(String levelid, String levelruletype, String levelrule, String sdcid, String tableid, HashMap prodVarColValMap, ConfigurationProcessor cp, QueryProcessor qp, TranslationProcessor tp, Calendar lastTransactiondate, String dbms) throws SapphireException {
        boolean rulePassed = true;
        String className = levelruletype + "LevelRuleProcessor";
        HashMap hm = SamplingPlanUtil.getSummaryMethod(className, levelruletype, cp, "Level");
        Method summaryMethod = (Method)hm.get("evaluateRule");
        try {
            Object retObj = summaryMethod.invoke(hm.get("object"), levelid, levelrule, SamplingPlanUtil.getSortingColumnFromPolicy(sdcid, cp), tableid, qp, tp, prodVarColValMap, lastTransactiondate, dbms);
            rulePassed = (Boolean)retObj;
        }
        catch (Exception e) {
            throw new SapphireException(e);
        }
        return rulePassed;
    }

    public static boolean evaluateTransitionRule(String levelid, String levelruletype, String levelrule, String sdcid, String tableid, ConfigurationProcessor cp, QueryProcessor qp, TranslationProcessor tp, HashMap prodVarColValMap, Calendar lastTransitionDate, String dbms) throws SapphireException {
        boolean rulePassed = true;
        String className = levelruletype + "TransitionRuleProcessor";
        HashMap hm = SamplingPlanUtil.getSummaryMethod(className, levelruletype, cp, "Transition");
        Method summaryMethod = (Method)hm.get("evaluateRule");
        try {
            Object retObj = summaryMethod.invoke(hm.get("object"), levelid, levelrule, SamplingPlanUtil.getSortingColumnFromPolicy(sdcid, cp), tableid, qp, tp, prodVarColValMap, lastTransitionDate, dbms);
            rulePassed = (Boolean)retObj;
        }
        catch (Exception e) {
            throw new SapphireException(e);
        }
        return rulePassed;
    }

    public static HashMap getSummaryMethod(String className, String ruletype, ConfigurationProcessor cp, String rule) throws SapphireException {
        Class<?> processorclass = null;
        HashMap<String, Method> summaryMethodObjHM = new HashMap<String, Method>();
        try {
            try {
                processorclass = Class.forName("com.labvantage.sapphire.pages.prodvariantruleeditor." + className);
            }
            catch (ClassNotFoundException e) {
                try {
                    processorclass = Class.forName(SamplingPlanUtil.getCustomRuleClassName(rule, ruletype, cp));
                }
                catch (ClassNotFoundException e1) {
                    throw new SapphireException(e1);
                }
                catch (SapphireException e1) {
                    throw e1;
                }
            }
            Constructor<?> c = processorclass.getConstructor(new Class[0]);
            Object o = c.newInstance(new Object[0]);
            Class[] partypes = new Class[]{String.class, String.class, String.class, String.class, QueryProcessor.class, TranslationProcessor.class, HashMap.class, Calendar.class, String.class};
            summaryMethodObjHM.put("evaluateRule", processorclass.getMethod("evaluateRule", partypes));
            summaryMethodObjHM.put("object", (Method)o);
        }
        catch (Exception e) {
            throw new SapphireException(e);
        }
        return summaryMethodObjHM;
    }

    public static String getCustomRuleClassName(String rule, String ruletype, ConfigurationProcessor cp) throws SapphireException {
        String className = "";
        PropertyList policy = cp.getPolicy("SamplingPlanPolicy", "Sapphire Custom");
        if (policy != null) {
            PropertyListCollection customRules = policy.getCollectionNotNull("customrules");
            for (int i = 0; i < customRules.size(); ++i) {
                PropertyList ruleDef = customRules.getPropertyList(i);
                String ruleTypeValueFromPolicy = ruleDef.getProperty("ruletype");
                String ruleFromPolicy = ruleDef.getProperty("rule");
                if (!ruleTypeValueFromPolicy.equals(ruletype) || ruleFromPolicy.indexOf(rule) == -1) continue;
                className = ruleDef.getProperty("classname");
                break;
            }
        }
        return className;
    }

    public static String getSortingColumnFromPolicy(String sdcid, ConfigurationProcessor cp) throws SapphireException {
        String evaluationsortby = "";
        PropertyList policy = cp.getPolicy("SamplingPlanPolicy", "Sapphire Custom");
        if (policy != null) {
            PropertyListCollection samplingplans = policy.getCollectionNotNull("samplingplans");
            for (int i = 0; i < samplingplans.size(); ++i) {
                PropertyList pl = samplingplans.getPropertyList(i);
                if (!pl.getProperty("sdcid").equals(sdcid)) continue;
                evaluationsortby = pl.getProperty("evaluationsortby");
                break;
            }
        }
        if (evaluationsortby.length() == 0) {
            evaluationsortby = "createdt";
        }
        return evaluationsortby;
    }

    public static int evaluateCountRule(QueryProcessor qp, String countRuleValue, String countRuleType, String compareValue2, String compareUnit2, ConfigurationProcessor configurationProcessor, SDCProcessor sdcProcessor, String prodVarId, PropertyList properties, String keyid1, String keyid2, String keyid3) {
        int countedVal = 0;
        compareValue2 = compareValue2.replace(Character.toString(FormatUtil.getInstance().getDecimalSeparator()), ".");
        try {
            if ("Number".equalsIgnoreCase(countRuleType)) {
                countedVal = Integer.parseInt(countRuleValue);
            } else if ("Groovy".equalsIgnoreCase(countRuleType)) {
                countedVal = new Double(GroovyUtil.getInstance(new ConnectionProcessor(qp.getConnectionid()).getConnectionInfo(qp.getConnectionid())).evaluateSecure(countRuleValue, SamplingPlanUtil.getGroovyMap(configurationProcessor, qp, sdcProcessor, prodVarId, properties, keyid1, keyid2, keyid3))).intValue();
            } else if ("Range".equalsIgnoreCase(countRuleType)) {
                countedVal = SamplingPlanUtil.evaluateRangeExpression(countRuleValue, compareValue2);
            } else if ("CountRule".equalsIgnoreCase(countRuleType)) {
                SafeSQL safeSQL = new SafeSQL();
                String sql = "SELECT countrule, countruletype from s_countrule where s_countruleid = " + safeSQL.addVar(countRuleValue);
                DataSet ds = qp.getPreparedSqlDataSet(sql, safeSQL.getValues());
                if (ds.getRowCount() > 0) {
                    countRuleValue = ds.getValue(0, "countrule", "");
                    countRuleType = ds.getValue(0, "countruletype", "");
                    if (countRuleValue.length() > 0) {
                        countedVal = "Groovy".equalsIgnoreCase(countRuleType) ? SamplingPlanUtil.evaluateCountRule(qp, countRuleValue, "Groovy", compareValue2, compareUnit2, configurationProcessor, sdcProcessor, prodVarId, properties, keyid1, keyid2, keyid3) : SamplingPlanUtil.evaluateRangeExpression(countRuleValue, compareValue2);
                    }
                }
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return countedVal;
    }

    private static int evaluateRangeExpression(String countRuleValue, String compareValue2) throws JSONException {
        int countedVal = 0;
        DataSet rangeData = SamplingPlanUtil.getRangeDataFromJSONString(countRuleValue);
        String defaultNumberOfSDI = SamplingPlanUtil.getValueFromJSONStringByKey(countRuleValue, "defaultnumberofsdi");
        if (defaultNumberOfSDI == null || defaultNumberOfSDI.length() == 0) {
            defaultNumberOfSDI = "0";
        }
        ExpressionEvaluator expeval = new ExpressionEvaluator(new StringReader(""));
        HashMap<String, BigDecimal> expVals = new HashMap<String, BigDecimal>();
        if (compareValue2 != null && compareValue2.length() > 0) {
            expVals.put("param1", new BigDecimal(compareValue2));
        }
        if (expVals.size() > 0) {
            StringBuffer expression = new StringBuffer();
            StringBuffer closingExpression = new StringBuffer();
            String evalVal = "";
            for (int rdi = 0; rdi < rangeData.getRowCount(); ++rdi) {
                String operator = rangeData.getValue(rdi, "operator", "");
                String value1 = rangeData.getValue(rdi, "batchsize", "");
                String noOfSDI = rangeData.getValue(rdi, "noofsdi", "0");
                expression.append("if([param1]").append(operator).append(value1).append(",").append(noOfSDI).append(",");
                closingExpression.append(")");
            }
            if (expression.length() > 0) {
                expression.append(defaultNumberOfSDI).append(closingExpression);
            }
            try {
                evalVal = expeval.evaluate(expression.toString(), expVals);
            }
            catch (ParseException e) {
                evalVal = "0";
            }
            countedVal = Integer.parseInt(evalVal);
        } else {
            countedVal = Integer.parseInt(defaultNumberOfSDI);
        }
        return countedVal;
    }

    private static DataSet getRangeDataFromJSONString(String jsonString) throws JSONException {
        DataSet ds = new DataSet();
        ds.addColumn("noofsdi", 0);
        ds.addColumn("operator", 0);
        ds.addColumn("batchsize", 0);
        ds.addColumn("batchunits", 0);
        ds.addColumn("usersequence", 1);
        JSONObject jsonObject = new JSONObject(jsonString);
        JSONObject rangeArrayObject = jsonObject.getJSONObject("rangearray");
        JSONArray rangeArrayNames = rangeArrayObject.names();
        if (rangeArrayNames != null) {
            for (int k = 0; k < rangeArrayNames.length(); ++k) {
                JSONObject jsonAssayTypeObject = rangeArrayObject.getJSONObject(rangeArrayNames.getString(k));
                int newRow = ds.addRow();
                for (int j = 0; j < ds.getColumnCount(); ++j) {
                    String columnId = ds.getColumnId(j);
                    if ("usersequence".equalsIgnoreCase(columnId)) {
                        ds.setNumber(newRow, columnId, rangeArrayNames.getString(k));
                        continue;
                    }
                    ds.setValue(newRow, columnId, jsonAssayTypeObject.getString(columnId));
                }
            }
        }
        ds.sort("usersequence");
        return ds;
    }

    private static String getValueFromJSONStringByKey(String jsonString, String key) throws JSONException {
        JSONObject jsonObject = new JSONObject(jsonString);
        return jsonObject.getString(key);
    }

    public static PropertyListCollection getProdVariantPropertyList(String prodVariantType, ConfigurationProcessor cfg) {
        String policyId = "SamplingPlanPolicy";
        String nodeId = "Sapphire Custom";
        String propertyId = "prodvarianttype";
        PropertyList policy = null;
        PropertyList pl = null;
        try {
            policy = cfg.getPolicy(policyId, nodeId);
            PropertyListCollection plc = policy.getCollectionNotNull(propertyId);
            for (int i = 0; i < plc.size(); ++i) {
                String type;
                pl = plc.getPropertyList(i);
                if (pl == null || !(type = pl.getProperty("type", "")).equals(prodVariantType)) continue;
                return pl.getCollectionNotNull("columns");
            }
        }
        catch (Exception e) {
            Trace.logError("Failed to retrieve policy.", e.getMessage());
        }
        return null;
    }

    public static PropertyList getProdVariantPropertyList(String sdcId, String prodVariantType, ConfigurationProcessor cfg) {
        String policyId = "SamplingPlanPolicy";
        String nodeId = "Sapphire Custom";
        String propertyId = "prodvarianttype";
        PropertyList policy = null;
        PropertyList pl = new PropertyList();
        try {
            policy = cfg.getPolicy(policyId, nodeId);
            if (policy != null) {
                PropertyListCollection plc = policy.getCollectionNotNull(propertyId);
                for (int i = 0; i < plc.size(); ++i) {
                    pl = plc.getPropertyList(i);
                    if (pl == null) continue;
                    String type = pl.getProperty("type", "");
                    String sdcid = pl.getProperty("sdcid", "");
                    if (!type.equals(prodVariantType) || !sdcid.equals(sdcId)) continue;
                    return pl;
                }
            }
        }
        catch (Exception e) {
            Trace.logError("Failed to retrieve policy.", e.getMessage());
        }
        return pl;
    }

    private static HashMap getGroovyMap(ConfigurationProcessor cfg, QueryProcessor qp, SDCProcessor sdcp, String prodVarId, PropertyList properties, String keyid1, String keyid2, String keyid3) {
        DataSet ds;
        String sdcid = properties.getProperty("sdcid");
        String tableid = sdcp.getProperty(sdcid, "tableid");
        String keycolid1 = sdcp.getProperty(sdcid, "keycolid1");
        String keycolid2 = sdcp.getProperty(sdcid, "keycolid2");
        String keycolid3 = sdcp.getProperty(sdcid, "keycolid3");
        HashMap<String, HashMap> groovyMap = new HashMap<String, HashMap>();
        StringBuffer sql = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        sql.append("select * from ").append(tableid).append(" where ").append(keycolid1).append(" = ").append(safeSQL.addVar(keyid1));
        if (keycolid2.length() > 0) {
            sql.append(" AND ").append(keycolid2).append(" = ").append(safeSQL.addVar(keyid2));
        }
        if (keycolid3.length() > 0) {
            sql.append(" AND ").append(keycolid3).append(" = ").append(safeSQL.addVar(keyid3));
        }
        if ((ds = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues())) != null && ds.size() > 0) {
            groovyMap.put(sdcid, SamplingPlanUtil.setNullColumnsToBlank((HashMap)ds.get(0)));
        } else {
            groovyMap.put(sdcid, SamplingPlanUtil.getSDCColumnMap(sdcid, sdcp));
        }
        DataSet linkedTablesDS = SamplingPlanUtil.getSDCForGroovyFromPolicy(cfg, sdcp);
        if (linkedTablesDS.size() > 0) {
            for (int i = 0; i < linkedTablesDS.size(); ++i) {
                if (!linkedTablesDS.getValue(i, "sdcid").equals(sdcid)) continue;
                String prodVariantCol = linkedTablesDS.getValue(i, "prodvariantcolumn");
                String linkTableId = linkedTablesDS.getValue(i, "linktableid");
                String linkSDCId = linkedTablesDS.getValue(i, "linksdcid");
                String linkKeyColId1 = linkedTablesDS.getValue(i, "linkkeycolid1");
                String linkKeyColId2 = linkedTablesDS.getValue(i, "linkkeycolid2", "");
                String linkKeyColId3 = linkedTablesDS.getValue(i, "linkkeycolid3", "");
                HashMap<String, String> multiKeysMap = new HashMap<String, String>();
                multiKeysMap.put("linkkeycolid1", linkKeyColId1);
                multiKeysMap.put("linkkeycolid2", linkKeyColId2);
                multiKeysMap.put("linkkeycolid3", linkKeyColId3);
                multiKeysMap.put("sdccolumnid2", linkedTablesDS.getValue(i, "sdccolumnid2"));
                multiKeysMap.put("sdccolumnid3", linkedTablesDS.getValue(i, "sdccolumnid3"));
                SamplingPlanUtil.setLinkedTableInGroovyMap(groovyMap, prodVariantCol, linkTableId, multiKeysMap, linkSDCId, tableid, keycolid1, keyid1, keycolid2, keyid2, keycolid3, keyid3, sdcp, qp);
            }
        } else {
            Trace.logError("SamplingPlan policy is missing columns mapping for the sdc in the Prod Variant Type collection");
        }
        sql.setLength(0);
        safeSQL.reset();
        sql.append("select * from s_prodvariant WHERE s_prodvariantid  = ").append(safeSQL.addVar(prodVarId));
        ds = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (ds != null && ds.size() > 0) {
            groovyMap.put("LV_ProdVariant", SamplingPlanUtil.setNullColumnsToBlank((HashMap)ds.get(0)));
        } else {
            groovyMap.put("LV_ProdVariant", SamplingPlanUtil.getSDCColumnMap(sdcid, sdcp));
        }
        return groovyMap;
    }

    private static void setLinkedTableInGroovyMap(HashMap groovyMap, String prodVariantCol, String linkTableId, HashMap multiKeysMap, String linksdcid, String tableid, String keycolid1, String keyid1, String keycolid2, String keyid2, String keycolid3, String keyid3, SDCProcessor sdcp, QueryProcessor qp) {
        DataSet ds;
        StringBuffer sql = new StringBuffer();
        String linkkeycolid2 = multiKeysMap.get("linkkeycolid2").toString();
        String linkkeycolid3 = multiKeysMap.get("linkkeycolid3").toString();
        String sdccolumnid2 = multiKeysMap.get("sdccolumnid2").toString();
        String sdccolumnid3 = multiKeysMap.get("sdccolumnid3").toString();
        SafeSQL safeSQL = new SafeSQL();
        sql.append("SELECT * from ").append(linkTableId).append(" p, ").append(tableid).append(" b ");
        sql.append(" WHERE p.").append(multiKeysMap.get("linkkeycolid1")).append(" = ").append("b.").append(prodVariantCol);
        if (linkkeycolid2 != null && linkkeycolid2 != "") {
            sql.append(" and p.").append(linkkeycolid2).append(" = ").append("b.").append(sdccolumnid2);
        }
        if (linkkeycolid3 != null && linkkeycolid3 != "") {
            sql.append(" and p.").append(linkkeycolid3).append(" = ").append("b.").append(sdccolumnid3);
        }
        sql.append(" and b.").append(keycolid1).append(" = ").append(safeSQL.addVar(keyid1));
        if (keycolid2 != null && keycolid2.length() > 0) {
            sql.append(" and b.").append(keycolid2).append(" = ").append(safeSQL.addVar(keyid2));
        }
        if (keycolid3 != null && keycolid3.length() > 0) {
            sql.append(" and b.").append(keycolid3).append(" = ").append(safeSQL.addVar(keyid3));
        }
        if ((ds = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues())) != null && ds.size() > 0) {
            groovyMap.put(linksdcid, SamplingPlanUtil.setNullColumnsToBlank((HashMap)ds.get(0)));
        } else {
            groovyMap.put(linksdcid, SamplingPlanUtil.getSDCColumnMap(linksdcid, sdcp));
        }
    }

    private static HashMap getSDCColumnMap(String sdcid, SDCProcessor sdcProcessor) {
        HashMap columns = new HashMap();
        DataSet ds = sdcProcessor.getColumnData(sdcid);
        if (ds != null) {
            for (int i = 0; i < ds.size(); ++i) {
                HashMap map = (HashMap)ds.get(i);
                columns.put(map.get("columnid"), "");
            }
        }
        return columns;
    }

    private static HashMap setNullColumnsToBlank(HashMap map) {
        if (map != null) {
            for (String key : map.keySet()) {
                if (!String.valueOf(map.get(key)).equals("null")) continue;
                map.put(key, "");
            }
        }
        return map;
    }

    public static DataSet getSDCForGroovyFromPolicy(ConfigurationProcessor cfg, SDCProcessor sdcProcessor) {
        DataSet linkedColDS = new DataSet();
        String policyId = "SamplingPlanPolicy";
        String nodeId = "Sapphire Custom";
        String propertyId = "prodvarianttype";
        PropertyList policy = null;
        PropertyList pl = null;
        linkedColDS.addColumn("prodvariantcolumn", 0);
        linkedColDS.addColumn("linktableid", 0);
        linkedColDS.addColumn("linksdcid", 0);
        linkedColDS.addColumn("linkkeycolid1", 0);
        linkedColDS.addColumn("sdcid", 0);
        try {
            policy = cfg.getPolicy(policyId, nodeId);
            PropertyListCollection plc = policy.getCollectionNotNull(propertyId);
            for (int i = 0; i < plc.size(); ++i) {
                pl = plc.getPropertyList(i);
                if (pl == null) continue;
                String sdc = pl.getProperty("sdcid", "");
                PropertyListCollection columnsColl = pl.getCollectionNotNull("columns");
                DataSet reverseLinks = sdcProcessor.getLinksData(sdc);
                for (int j = 0; j < columnsColl.size(); ++j) {
                    String prodVariantCol;
                    int row;
                    if (columnsColl.getPropertyList(j) == null || (row = reverseLinks.findRow("sdccolumnid", prodVariantCol = columnsColl.getPropertyList(j).getProperty("prodvariantcolumn", ""))) == -1) continue;
                    linkedColDS.addRow();
                    linkedColDS.setValue(linkedColDS.size() - 1, "prodvariantcolumn", prodVariantCol);
                    String tableid = reverseLinks.getValue(row, "tableid");
                    linkedColDS.setValue(linkedColDS.size() - 1, "linktableid", tableid);
                    String linksdcid = reverseLinks.getValue(row, "linksdcid");
                    linkedColDS.setValue(linkedColDS.size() - 1, "linksdcid", linksdcid);
                    String keycolid1 = sdcProcessor.getProperty(linksdcid, "keycolid1");
                    String keycolid2 = sdcProcessor.getProperty(linksdcid, "keycolid2");
                    String keycolid3 = sdcProcessor.getProperty(linksdcid, "keycolid3");
                    linkedColDS.setValue(linkedColDS.size() - 1, "linkkeycolid1", keycolid1);
                    if (keycolid2 != null && keycolid2.length() > 0) {
                        linkedColDS.setString(linkedColDS.size() - 1, "sdccolumnid2", reverseLinks.getValue(row, "sdccolumnid2"));
                        linkedColDS.setValue(linkedColDS.size() - 1, "linkkeycolid2", keycolid2);
                    }
                    if (keycolid3 != null && keycolid3.length() > 0) {
                        linkedColDS.setString(linkedColDS.size() - 1, "sdccolumnid3", reverseLinks.getValue(row, "sdccolumnid3"));
                        linkedColDS.setValue(linkedColDS.size() - 1, "linkkeycolid3", keycolid3);
                    }
                    linkedColDS.setValue(linkedColDS.size() - 1, "sdcid", sdc);
                }
            }
        }
        catch (Exception e) {
            Trace.logError("Failed to retrieve policy.", e.getMessage());
        }
        return linkedColDS;
    }

    public static HashMap createProdVariantColValueMap(DataSet columnMap, DataSet dsProdVars) {
        HashMap<String, String> pvColHM = new HashMap<String, String>();
        String[] prodVariantCols = columnMap.getColumnValues("prodvariantcolumnid", ";").split(";");
        for (int i = 0; i < prodVariantCols.length; ++i) {
            String colVal = dsProdVars.getValue(0, prodVariantCols[i]);
            pvColHM.put(prodVariantCols[i], colVal);
        }
        return pvColHM;
    }

    public static HashMap createSDCColIdProdVariantValueMap(DataSet columnMap, DataSet dsProdVars) {
        HashMap<String, String> sdcColHM = new HashMap<String, String>();
        String[] prodVariantCols = columnMap.getColumnValues("prodvariantcolumnid", ";").split(";");
        String[] sdcCols = columnMap.getColumnValues("sdccolumnid", ";").split(";");
        for (int i = 0; i < sdcCols.length; ++i) {
            String colVal = dsProdVars.getValue(0, prodVariantCols[i]);
            sdcColHM.put(sdcCols[i], colVal);
        }
        return sdcColHM;
    }

    public static String getCurrentOrMaxPVersion(String spId, QueryProcessor qp) throws SapphireException {
        String sql = "SELECT s_samplingplanversionid FROM s_samplingplan  WHERE s_samplingplanid=? and ( versionstatus='P' or versionstatus='C' ) order by versionstatus, cast (s_samplingplanversionid as numeric) desc";
        DataSet ds = qp.getPreparedSqlDataSet("CurrentVersion", sql, new Object[]{spId});
        return ds.getValue(0, "s_samplingplanversionid");
    }

    public static void checkSPItemLinks(String sdcId, String rsetId, QueryProcessor qp, TranslationProcessor tp, ActionProcessor ap, SDCProcessor sdcProcessor) throws SapphireException {
        DataSet ds = SamplingPlanUtil.getSPItemLinks(qp, sdcId, rsetId);
        if (ds.getRowCount() > 0) {
            String tableid = sdcProcessor.getProperty(sdcId, "tableid");
            String keycolid1 = sdcProcessor.getProperty(sdcId, "keycolid1");
            String keycolid2 = sdcProcessor.getProperty(sdcId, "keycolid2");
            DataSet dsRefences = new DataSet();
            dsRefences.addColumn("SamplingPlan", 0);
            dsRefences.addColumn("SamplingPlan Version", 0);
            dsRefences.addColumn(keycolid1, 0);
            dsRefences.addColumn(keycolid2, 0);
            for (int i = 0; i < ds.getRowCount(); ++i) {
                DataSet dsVers;
                String itemId = ds.getValue(i, "keyid1");
                String itemVerId = ds.getValue(i, "keyid2");
                String spVerId = ds.getValue(i, "spversionid", "C");
                String samplingPlanId = ds.getValue(i, "s_samplingplanid");
                String samplingPlanVersionId = ds.getValue(i, "s_samplingplanversionid");
                if (itemVerId.equals(spVerId)) {
                    int r = dsRefences.addRow();
                    dsRefences.setString(r, "SamplingPlan", samplingPlanId);
                    dsRefences.setString(r, "SamplingPlan Version", samplingPlanVersionId);
                    dsRefences.setString(r, keycolid1, itemId);
                    dsRefences.setString(r, keycolid2, itemVerId);
                    continue;
                }
                if (!"C".equalsIgnoreCase(spVerId) || (dsVers = qp.getPreparedSqlDataSet("SELECT " + keycolid1 + " itemid, " + keycolid2 + " itemversion FROM " + tableid + "  WHERE " + keycolid1 + " = ? and ( versionstatus='P' or versionstatus='C' ) order by versionstatus, cast (" + keycolid2 + " as numeric) desc", (Object[])new String[]{itemId})).getRowCount() <= 0 || !itemVerId.equals(dsVers.getValue(0, "itemversion"))) continue;
                if (dsVers.getRowCount() < 2) {
                    int r = dsRefences.addRow();
                    dsRefences.setString(r, "SamplingPlan", samplingPlanId);
                    dsRefences.setString(r, "SamplingPlan Version", samplingPlanVersionId);
                    dsRefences.setString(r, keycolid1, itemId);
                    dsRefences.setString(r, keycolid2, spVerId);
                    continue;
                }
                int toDeleteVersions = 0;
                HashMap<String, String> findmap = new HashMap<String, String>();
                findmap.put("keyid1", itemId);
                for (int k = 1; k < dsVers.getRowCount(); ++k) {
                    findmap.put("keyid2", dsVers.getValue(k, "itemversion"));
                    if (ds.findRow(findmap) <= -1) continue;
                    ++toDeleteVersions;
                }
                if (toDeleteVersions != dsVers.getRowCount() - 1) continue;
                int r = dsRefences.addRow();
                dsRefences.setString(r, "SamplingPlan", samplingPlanId);
                dsRefences.setString(r, "SamplingPlan Version", samplingPlanVersionId);
                dsRefences.setString(r, keycolid1, itemId);
                dsRefences.setString(r, keycolid2, spVerId);
            }
            if (dsRefences.getRowCount() > 0) {
                throw new SapphireException("Referencing SamplingPlan", "CONFIRM", tp.translate("Referencing SamplingPlan exists. Continue?") + "<br>" + dsRefences.toHTML());
            }
        }
    }

    private static DataSet getSPItemLinks(QueryProcessor qp, String sdcId, String rsetId) {
        SafeSQL safeSQL = new SafeSQL();
        return qp.getPreparedSqlDataSet("SELECT DISTINCT r.keyid1, r.keyid2, s.itemkeyid2 spversionid, s.s_samplingplanid, s.s_samplingplanversionid, s.s_samplingplanitemno  FROM rsetitems r, s_spitem s WHERE r.rsetid = " + safeSQL.addVar(rsetId) + " AND s.itemsdcid = " + safeSQL.addVar(sdcId) + " AND s.itemkeyid1 = r.keyid1", safeSQL.getValues());
    }

    public static void deleteSPItemLinks(String sdcId, String rsetId, QueryProcessor qp, ActionProcessor ap, SDCProcessor sdcProcessor) throws SapphireException {
        DataSet ds = SamplingPlanUtil.getSPItemLinks(qp, sdcId, rsetId);
        String tableid = sdcProcessor.getProperty(sdcId, "tableid");
        String keycolid1 = sdcProcessor.getProperty(sdcId, "keycolid1");
        DataSet dsActionProps = new DataSet();
        dsActionProps.addColumn("s_samplingplanid", 0);
        dsActionProps.addColumn("s_samplingplanversionid", 0);
        dsActionProps.addColumn("s_samplingplanitemno", 0);
        for (int i = 0; i < ds.getRowCount(); ++i) {
            DataSet dsCurrent;
            String itemId = ds.getValue(i, "keyid1");
            String itemVerId = ds.getValue(i, "keyid2");
            String spVerId = ds.getValue(i, "spversionid", "C");
            String samplingPlanId = ds.getValue(i, "s_samplingplanid");
            String samplingPlanVersionId = ds.getValue(i, "s_samplingplanversionid");
            String spItemNo = ds.getValue(i, "s_samplingplanitemno");
            if (itemVerId.equals(spVerId)) {
                int r = dsActionProps.addRow();
                dsActionProps.setString(r, "s_samplingplanid", samplingPlanId);
                dsActionProps.setString(r, "s_samplingplanversionid", samplingPlanVersionId);
                dsActionProps.setString(r, "s_samplingplanitemno", spItemNo);
                continue;
            }
            if (!"C".equalsIgnoreCase(spVerId) || (dsCurrent = qp.getPreparedSqlDataSet("SELECT 1 FROM " + tableid + "  WHERE " + keycolid1 + " = ? AND  ( versionstatus = 'P' or versionstatus = 'C' )", (Object[])new String[]{itemId})).getRowCount() != 0) continue;
            int r = dsActionProps.addRow();
            dsActionProps.setString(r, "s_samplingplanid", samplingPlanId);
            dsActionProps.setString(r, "s_samplingplanversionid", samplingPlanVersionId);
            dsActionProps.setString(r, "s_samplingplanitemno", spItemNo);
        }
        if (dsActionProps.getRowCount() > 0) {
            PropertyList deleteProps = new PropertyList();
            deleteProps.setProperty("sdcid", "LV_SamplingPlan");
            deleteProps.setProperty("linkid", "item");
            deleteProps.setProperty("s_samplingplanid", dsActionProps.getColumnValues("s_samplingplanid", ";"));
            deleteProps.setProperty("s_samplingplanversionid", dsActionProps.getColumnValues("s_samplingplanversionid", ";"));
            deleteProps.setProperty("s_samplingplanitemno", dsActionProps.getColumnValues("s_samplingplanitemno", ";"));
            deleteProps.setProperty("separator", ";");
            ap.processAction("DeleteSDIDetail", "1", deleteProps);
        }
    }

    private static class UserMessages {
        static final String SETLEVELTO = "Set Level to ";
        static final String BATCHES = " item(s)  ";
        static final String SETNEWSTATETO = "Set new state to ";
        static final String ON = " on ";
        static final String CONSECUTIVE = "consecutively";

        private UserMessages() {
        }
    }
}

