/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.array;

import com.labvantage.sapphire.array.ArraysPolicy;
import com.labvantage.sapphire.util.evaluator.ExpressionEvaluator;
import com.labvantage.sapphire.util.evaluator.ExpressionParam;
import com.labvantage.sapphire.util.evaluator.ParseException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.util.M18NUtil;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class PromoteArrayResults
extends BaseAction
implements sapphire.action.PromoteArrayResults {
    static final String LABVANTAGE_CVS_ID = "$Revision: 91470 $";
    private static final String LINKID = "Array Item Content";
    public static String AVG_TEXT = "Average_Repeats";
    public static String MAX_TEXT = "Max_Repeats";
    public static String MIN_TEXT = "Min_Repeats";
    public static String INDIV_REPLICATETEXT = "To_Individual_Replicate";
    public static String ARRAYITEM_DATASET_LAST = "Last_DataSet";
    public static String ARRAYITEM_DATASET_AGGREGATE = "Aggregate_Across_DataSets";
    public static String IGNORE_MISSING_TEXT = "Ignore_Missing_Value";
    public static String ARRAYITEM_PREFIX = "ArrayItems:";
    public static String MAX_DATASET = "Max";
    public static String MIN_DATASET = "Min";
    private boolean createNewIfReleased = false;

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String arrayIds = properties.getProperty("arrayid");
        String arrayMethodIds = properties.getProperty("arraymethodid");
        String arrayMethodVersionIds = properties.getProperty("arraymethodversionid");
        String arrayMethodInstances = properties.getProperty("arraymethodinstance");
        TranslationProcessor tp = this.getTranslationProcessor();
        ActionProcessor ap = this.getActionProcessor();
        if (arrayIds.length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", tp.translate("Array Id not passed into the action."));
        }
        String delimeter = properties.getProperty("separator", properties.getProperty("delimeter", ";"));
        DataSet dsProps = new DataSet();
        dsProps.addColumnValues("arrayid", 0, arrayIds, delimeter);
        if (properties.containsKey("arraymethodid")) {
            if (!properties.containsKey("arraymethodversionid")) {
                throw new SapphireException("INVALID_PROPERTY", tp.translate("Array Method Version Id is required with Array Method."));
            }
            dsProps.addColumnValues("arraymethodid", 0, arrayMethodIds, delimeter);
            dsProps.addColumnValues("arraymethodversionid", 0, arrayMethodVersionIds, delimeter);
            dsProps.addColumnValues("arraymethodinstance", 0, arrayMethodInstances, delimeter);
            HashMap<String, String> tokenMap = new HashMap<String, String>();
            for (int i = 0; i < dsProps.getRowCount(); ++i) {
                String arrayId = dsProps.getString(i, "arrayid", "");
                String arrayMethodId = dsProps.getString(i, "arraymethodid", "");
                String arrayMethodVerId = dsProps.getString(i, "arraymethodversionid", "");
                String arrayMethodInstance = dsProps.getString(i, "arraymethodinstance", "");
                tokenMap.put("arrayid", arrayId);
                tokenMap.put("arraymethodid", arrayMethodId);
                tokenMap.put("arraymethodversionid", arrayMethodVerId);
                tokenMap.put("arraymethodinstance", arrayMethodInstance);
                if (arrayMethodId.length() != 0 && arrayMethodVerId.length() != 0) continue;
                throw new SapphireException("INVALID_PROPERTY", tp.translate("Array Method Id, Version is missing in action input."));
            }
        }
        DataSet updateSamples = new DataSet();
        PropertyList policy = this.getConfigurationProcessor().getPolicy("ArraysPolicy", "Sapphire Custom");
        ArraysPolicy policyDef = new ArraysPolicy(policy);
        this.createNewIfReleased = "Y".equalsIgnoreCase(policyDef.getAddNewDataSetIfReleased());
        this.pullArrayItemDataToContentSample(properties, dsProps, ap, updateSamples);
        this.pushArrayItemDataToContentSample(properties, dsProps, ap, updateSamples);
        if (updateSamples.getRowCount() > 0) {
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", "Sample");
            props.setProperty("keyid1", updateSamples.getColumnValues("keyid1", ";"));
            props.setProperty("auditsignedflag", properties.getProperty("auditsignedflag"));
            props.setProperty("auditactivity", properties.getProperty("auditactivity"));
            props.setProperty("auditreason", properties.getProperty("auditreason"));
            this.getActionProcessor().processAction("UpdateDatasetStatus", "1", props);
            props.clear();
            props.setProperty("sdcid", "Sample");
            props.setProperty("keyid1", updateSamples.getColumnValues("keyid1", ";"));
            props.setProperty("statuscolid", "samplestatus");
            props.setProperty("auditsignedflag", properties.getProperty("auditsignedflag"));
            props.setProperty("auditactivity", properties.getProperty("auditactivity"));
            props.setProperty("auditreason", properties.getProperty("auditreason"));
            this.getActionProcessor().processAction("SyncSDIDataSetStatus", "1", props);
            props.clear();
            props.setProperty("sdcid", "Sample");
            props.setProperty("keyid1", updateSamples.getColumnValues("keyid1", ";"));
            props.setProperty("auditsignedflag", properties.getProperty("auditsignedflag"));
            props.setProperty("auditactivity", properties.getProperty("auditactivity"));
            props.setProperty("auditreason", properties.getProperty("auditreason"));
            this.getActionProcessor().processAction("SyncSDIWIStatus", "1", props);
        }
    }

    private void pullArrayItemDataToContentSample(PropertyList properties, DataSet dsProps, ActionProcessor ap, DataSet updateSamples) throws SapphireException {
        String sql = "SELECT DISTINCT azc.arraymethodid, azc.arraymethodversionid, aami.executecalcflag, azc.workitemid, azc.workitemversionid, aic.arrayitemid,  aic.arrayitemcontentid, aic.contentsdcid, aic.contentkeyid1, aic.contentworkitemid, az.arrayzoneid, azc.contentitem, azc.usersequence  FROM arrayzonecontent azc, arrayzone az, arrayitemarrayzone aiaz, arrayitemcontent aic, arrayarraymethoditem aami  WHERE aic.arrayitemid = aiaz.arrayitemid AND aiaz.arrayzoneid = az.arrayzoneid AND azc.arrayzoneid = az.arrayzoneid  AND aic.contentsdcid = 'Sample' AND  aami.arrayid = az.arrayid AND aami.arraymethodid = azc.arraymethodid AND aami.arraymethodversionid = azc.arraymethodversionid  AND az.arrayid = ? AND aic.contentkeyid1 is not null and azc.workitemid is not null ORDER BY azc.usersequence DESC";
        DataSet calculateSamples = new DataSet();
        DataSet arrayItemContents = new DataSet();
        HashMap<String, DataSet> dsMap = new HashMap<String, DataSet>();
        DataSet processedRows = new DataSet();
        QueryProcessor qp = this.getQueryProcessor();
        for (int idx = 0; idx < dsProps.getRowCount(); ++idx) {
            String arrayId = dsProps.getString(idx, "arrayid", "");
            String arrayMethodId = dsProps.getString(idx, "arraymethodid", "");
            String arrayMethodVersionId = dsProps.getString(idx, "arraymethodversionid", "");
            HashMap<String, String> findMap = new HashMap<String, String>();
            findMap.put("arrayid", arrayId);
            findMap.put("arraymethodid", arrayMethodId);
            findMap.put("arraymethodversionid", arrayMethodVersionId);
            if (processedRows.findRow(findMap) > -1) continue;
            DataSet ds = new DataSet();
            if (dsMap.containsKey(arrayId)) {
                ds = (DataSet)dsMap.get(arrayId);
            } else {
                this.database.createPreparedResultSet("contentsamples", sql, new Object[]{arrayId});
                ds.setResultSet(this.database.getResultSet("contentsamples"));
                if (ds.getRowCount() == 0) {
                    throw new SapphireException("Could not find workitem details to promote the results to. Please check the array method applied on the array");
                }
                dsMap.put(arrayId, ds);
            }
            if (arrayMethodId.length() > 0 && arrayMethodVersionId.length() > 0) {
                HashMap<String, String> filterMap = new HashMap<String, String>();
                filterMap.put("arraymethodid", arrayMethodId);
                filterMap.put("arraymethodversionid", arrayMethodVersionId);
                ds = ds.getFilteredDataSet(filterMap);
            }
            int rw = processedRows.addRow();
            processedRows.setString(rw, "arrayid", arrayId);
            processedRows.setString(rw, "arraymethodid", arrayMethodId);
            processedRows.setString(rw, "arraymethodversionid", arrayMethodVersionId);
            ArrayList<DataSet> zoneGrps = ds.getGroupedDataSets("arrayzoneid");
            for (int i = 0; i < zoneGrps.size(); ++i) {
                DataSet zoneGroup = zoneGrps.get(i);
                String zoneWorkItemId = "";
                String zoneWorkItemVersionId = "";
                for (int z = 0; z < zoneGroup.size(); ++z) {
                    if (zoneWorkItemId.length() > 0 && !zoneWorkItemId.equals(zoneGroup.getString(z, "workitemid", ""))) {
                        throw new SapphireException("Multiple matching test methods found in array method definition. Please review the definition.");
                    }
                    zoneWorkItemId = zoneGroup.getString(z, "workitemid", "");
                    zoneWorkItemVersionId = zoneGroup.getString(z, "workitemversionid", "1");
                }
                if (zoneWorkItemId.length() == 0) {
                    throw new SapphireException("Test Method not defined for content results to be promoted");
                }
                DataSet editArrayItemContents = new DataSet();
                DataSet zoneSamples = new DataSet();
                for (int j = 0; j < zoneGroup.getRowCount(); ++j) {
                    String sampleId = zoneGroup.getString(j, "contentkeyid1");
                    String executeCalcFlag = zoneGroup.getString(j, "executecalcflag", "N");
                    String contentItem = zoneGroup.getString(j, "contentitem", "");
                    if (("U".equalsIgnoreCase(executeCalcFlag) && "Unknown".equalsIgnoreCase(contentItem) || executeCalcFlag.equalsIgnoreCase("A")) && calculateSamples.findRow("keyid1", sampleId) < 0) {
                        int r = calculateSamples.addRow();
                        calculateSamples.setString(r, "keyid1", sampleId);
                    }
                    String contentWorkItemId = zoneGroup.getString(j, "contentworkitemid", "");
                    String arrayItemContentId = zoneGroup.getString(j, "arrayitemcontentid", "");
                    String arrayItemId = zoneGroup.getString(j, "arrayitemid", "");
                    editArrayItemContents.addColumn("workiteminstance", 0);
                    HashMap<String, String> findArrayItemContent = new HashMap<String, String>();
                    findArrayItemContent.put("arrayitemcontentid", arrayItemContentId);
                    findArrayItemContent.put("arrayitemid", arrayItemId);
                    SafeSQL safeSQL = new SafeSQL();
                    safeSQL.addVar(sampleId);
                    safeSQL.addVar(zoneWorkItemId);
                    safeSQL.addVar(zoneWorkItemVersionId);
                    DataSet dsSampleWorkItem = qp.getPreparedSqlDataSet("getsdiwi", "select workitemid, workiteminstance from sdiworkitem where sdcid='Sample' and keyid1=? and workitemid =? and workitemversionid=?", safeSQL.getValues());
                    if (zoneWorkItemId.length() <= 0 || contentWorkItemId.length() != 0) continue;
                    if (arrayItemContents.findRow(findArrayItemContent) < 0) {
                        int r = arrayItemContents.addRow();
                        arrayItemContents.setString(r, "arrayitemcontentid", arrayItemContentId);
                        arrayItemContents.setString(r, "arrayitemid", arrayItemId);
                        int r1 = editArrayItemContents.addRow();
                        editArrayItemContents.setString(r1, "arrayitemcontentid", arrayItemContentId);
                        editArrayItemContents.setString(r1, "arrayitemid", arrayItemId);
                        editArrayItemContents.setString(r1, "contentsdcid", "Sample");
                        editArrayItemContents.setString(r1, "contentworkitemid", zoneWorkItemId);
                        editArrayItemContents.setString(r1, "contentkeyid1", sampleId);
                    }
                    if (dsSampleWorkItem.getRowCount() == 0) {
                        if (zoneSamples.findRow("keyid1", sampleId) >= 0) continue;
                        int zr = zoneSamples.addRow();
                        zoneSamples.setString(zr, "keyid1", sampleId);
                        continue;
                    }
                    HashMap<String, String> findSample = new HashMap<String, String>();
                    findSample.put("contentkeyid1", sampleId);
                    DataSet sampleArrayItems = editArrayItemContents.getFilteredDataSet(findSample);
                    sampleArrayItems.setString(-1, "workiteminstance", dsSampleWorkItem.getValue(0, "workiteminstance"));
                }
                if (zoneSamples.getRowCount() > 0) {
                    PropertyList workitemProps = new PropertyList();
                    workitemProps.put("sdcid", "Sample");
                    workitemProps.put("keyid1", zoneSamples.getColumnValues("keyid1", ";"));
                    workitemProps.put("workitemid", zoneWorkItemId);
                    workitemProps.put("workitemversionid", zoneWorkItemVersionId);
                    workitemProps.put("applyworkitem", "Y");
                    workitemProps.put("propsmatch", "N");
                    workitemProps.put("forcenew", "Y");
                    workitemProps.setProperty("auditsignedflag", properties.getProperty("auditsignedflag"));
                    workitemProps.setProperty("auditactivity", properties.getProperty("auditactivity"));
                    workitemProps.setProperty("auditreason", properties.getProperty("auditreason"));
                    ap.processAction("AddSDIWorkItem", "1", workitemProps);
                    DataSet dsInstance = new DataSet(workitemProps.getProperty("newworkiteminstancexml"));
                    for (int d = 0; d < dsInstance.getRowCount(); ++d) {
                        String keyid1 = dsInstance.getString(d, "keyid1", "");
                        String wiInstance = dsInstance.getValue(d, "workiteminstance", "");
                        HashMap<String, String> findSample = new HashMap<String, String>();
                        findSample.put("contentkeyid1", keyid1);
                        DataSet sampleArrayItems = editArrayItemContents.getFilteredDataSet(findSample);
                        sampleArrayItems.setString(-1, "workiteminstance", wiInstance);
                    }
                }
                if (editArrayItemContents.getRowCount() <= 0) continue;
                PropertyList editprops = new PropertyList();
                editprops.setProperty("sdcid", "LV_ArrayItem");
                editprops.setProperty("linkid", LINKID);
                editprops.setProperty("arrayitemid", editArrayItemContents.getColumnValues("arrayitemid", ";"));
                editprops.setProperty("arrayitemcontentid", editArrayItemContents.getColumnValues("arrayitemcontentid", ";"));
                editprops.setProperty("contentworkitemid", editArrayItemContents.getColumnValues("contentworkitemid", ";"));
                editprops.setProperty("contentworkiteminstance", editArrayItemContents.getColumnValues("workiteminstance", ";"));
                editprops.setProperty("auditsignedflag", properties.getProperty("auditsignedflag"));
                editprops.setProperty("auditactivity", properties.getProperty("auditactivity"));
                editprops.setProperty("auditreason", properties.getProperty("auditreason"));
                ap.processAction("EditSDIDetail", "1", editprops);
            }
        }
        if (calculateSamples.getRowCount() > 0) {
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", "Sample");
            props.setProperty("keyid1", calculateSamples.getColumnValues("keyid1", ";"));
            props.setProperty("auditsignedflag", properties.getProperty("auditsignedflag"));
            props.setProperty("auditactivity", properties.getProperty("auditactivity"));
            props.setProperty("auditreason", properties.getProperty("auditreason"));
            ap.processAction("RedoCalculations", "1", props);
            updateSamples.copyRow(calculateSamples, -1, 1);
        }
    }

    private void pushArrayItemDataToContentSample(PropertyList properties, DataSet dsProps, ActionProcessor ap, DataSet updateSamples) throws SapphireException {
        String columnId;
        DataSet ds;
        PropertyList props;
        StringBuffer getPropagationRule = new StringBuffer();
        getPropagationRule.append("select ap.paramid, ap.paramtype, ap.datatypes, ap.arraymethodid, ap.arraymethodversionid, ap.arrayzoneid, ap.propagationrule proprule, az.zone, az.loadingpriorityhorizontal, az.loadingpriorityvertical ").append(" from arrayparamitem ap, arrayzone az ").append(" where ap.arrayid = ? and ap.propagationrule is not null and az.arrayzoneid = ap.arrayzoneid ").append(" and ( az.zone = '(FullArray)'  or exists (select 1 from arrayitemcontent aic, arrayitemarrayzone aiaz where aic.arrayitemid = aiaz.arrayitemid and aic.contentsdcid = 'Sample' and  aiaz.arrayzoneid = ap.arrayzoneid ))").append(" and ( az.loadingpriorityhorizontal is null or az.loadingpriorityhorizontal not like '%D%') and (az.loadingpriorityvertical is null or az.loadingpriorityvertical not like '%D%')");
        StringBuffer getArrayDataItems = new StringBuffer();
        getArrayDataItems.append("SELECT distinct aic.arrayitemid, di.transformvalue, di.enteredtext, aic.contentkeyid1, aic.contentworkitemid, aic.contentworkiteminstance, ds.arraymethodinstance, aic.repeatnum, ai.XPOS, ai.YPOS, smap.sourcesampleid").append(" FROM sdidataitem di, sdidata ds, arrayitemarrayzone aiaz, arrayitem ai, arrayarraymethoditem aami, arrayitemcontent aic ").append(" LEFT OUTER JOIN  s_samplemap smap ON aic.contentkeyid1 = smap.destsampleid").append(" WHERE di.sdcid = ds.sdcid AND di.keyid1 = ds.keyid1 AND di.keyid2 = ds.keyid2 AND di.keyid3 = ds.keyid3 AND di.paramlistid = ds.paramlistid ").append(" AND di.paramlistversionid = ds.paramlistversionid AND di.variantid = ds.variantid AND di.dataset = ds.dataset AND di.paramid = ? AND di.paramtype = ? ").append(" AND ds.sdcid = 'LV_ArrayItem' AND ds.keyid1 = aic.arrayitemid AND ds.arraymethodid = ? AND ds.arraymethodversionid = ? ").append(" AND ds.arraymethodid = aami.arraymethodid AND ds.arraymethodversionid = aami.arraymethodversionid AND ds.arraymethodinstance = aami.arraymethodinstance AND aami.arrayid = ai.arrayid ").append(" AND aic.arrayitemid = aiaz.arrayitemid AND aiaz.arrayzoneid = ? and aic.contentsdcid = 'Sample' and ai.arrayitemid = aic.arrayitemid and aami.arraymethoditemstatus != 'Cancelled' order by aic.contentkeyid1");
        ExpressionEvaluator expeval = new ExpressionEvaluator(new StringReader(""));
        ExpressionParam ep = null;
        DataSet enterDataItemPL = new DataSet();
        DataSet addReplicate = new DataSet();
        DataSet editTrackItem = new DataSet();
        DataSet editSample = new DataSet();
        QueryProcessor qp = this.getQueryProcessor();
        TranslationProcessor tp = this.getTranslationProcessor();
        ArrayList<String> samples = new ArrayList<String>();
        try {
            for (int idx = 0; idx < dsProps.getRowCount(); ++idx) {
                String arrayId = dsProps.getString(idx, "arrayid", "");
                String propMethodId = dsProps.getString(idx, "arraymethodid", "");
                String propMethodVersionId = dsProps.getString(idx, "arraymethodversionid", "");
                String propMethodInstance = dsProps.getString(idx, "arraymethodinstance", "");
                SafeSQL safeSQL = new SafeSQL();
                safeSQL.addVar(arrayId);
                DataSet ds2 = qp.getPreparedSqlDataSet(getPropagationRule.toString(), safeSQL.getValues(), true);
                if (propMethodId.length() > 0 && propMethodVersionId.length() > 0) {
                    HashMap<String, String> filterMap = new HashMap<String, String>();
                    filterMap.put("arraymethodid", propMethodId);
                    filterMap.put("arraymethodversionid", propMethodVersionId);
                    ds2 = ds2.getFilteredDataSet(filterMap);
                }
                block8: for (int i = 0; i < ds2.getRowCount(); ++i) {
                    String rule = ds2.getClob(i, "proprule", "");
                    if (rule.length() <= 0) continue;
                    String paramId = ds2.getString(i, "paramid", "");
                    String pType = ds2.getString(i, "paramtype", "");
                    String dataType = ds2.getString(i, "datatypes", "");
                    String loadH = ds2.getString(i, "loadingpriorityhorizontal", "");
                    String loadV = ds2.getString(i, "loadingpriorityvertical", "");
                    String zoneId = ds2.getString(i, "arrayzoneid", "");
                    boolean numericData = "N".equalsIgnoreCase(dataType) || "NC".equalsIgnoreCase(dataType);
                    HashMap<String, String> tokenMap = new HashMap<String, String>();
                    tokenMap.put("zone", zoneId);
                    tokenMap.put("paramid", paramId);
                    tokenMap.put("paramtype", pType);
                    String arraymethodId = ds2.getString(i, "arraymethodid", "");
                    String arraymethodVersionId = ds2.getString(i, "arraymethodversionid", "");
                    this.database.createPreparedResultSet("dataitems", getArrayDataItems.toString(), new Object[]{paramId, pType, arraymethodId, arraymethodVersionId, zoneId});
                    DataSet aiDataItems = new DataSet();
                    aiDataItems.setResultSet(this.database.getResultSet("dataitems"));
                    if (propMethodInstance.length() > 0) {
                        HashMap<String, BigDecimal> filterMap = new HashMap<String, BigDecimal>();
                        filterMap.put("arraymethodinstance", new BigDecimal(propMethodInstance));
                        aiDataItems = aiDataItems.getFilteredDataSet(filterMap);
                    }
                    String uploadTo = "";
                    String columnOf = "";
                    String paramListData = "";
                    String paramData = "";
                    String columnId2 = "";
                    String operation = "";
                    boolean ignoreMissing = false;
                    boolean toIndivReplicate = false;
                    boolean lastArrayItemDataSet = true;
                    if (rule.startsWith("Sample.") || rule.startsWith("ParentSample.") || rule.startsWith("Trackitem.")) {
                        String[] opArray;
                        uploadTo = "column";
                        columnOf = rule.substring(0, rule.indexOf("."));
                        String[] ruleArray = StringUtil.split(rule.substring(rule.indexOf(".") + 1), ";");
                        columnId2 = ruleArray[0];
                        if (loadH.contains("D") || loadV.contains("D")) {
                            tokenMap.put("columnid", columnId2);
                            tokenMap.put("columnof", columnOf);
                            Logger.logInfo("PromoteArrayResults: " + tp.translate("As dilution exists, Parameter [paramid] of Param Type [paramtype] in [zone] zone is not propagated to column [columnid] of [columnof]. It is propagated only to individual replicates of content Sample.", tokenMap));
                            continue;
                        }
                        if (ruleArray.length > 1 && (opArray = StringUtil.split(ruleArray[1], ":")).length > 1 && ARRAYITEM_DATASET_AGGREGATE.equalsIgnoreCase(opArray[1])) {
                            lastArrayItemDataSet = false;
                        }
                        if (ruleArray.length > 2) {
                            operation = ruleArray[2];
                        }
                        if (operation.length() == 0) {
                            operation = "Average";
                        }
                        if (ruleArray.length > 3 && ruleArray[3].equalsIgnoreCase(IGNORE_MISSING_TEXT)) {
                            ignoreMissing = true;
                        }
                    } else {
                        uploadTo = "dataitem";
                        String[] value = StringUtil.split(rule.substring(1, rule.length() - 1), "|");
                        paramListData = value[0];
                        paramData = value[1];
                        if (value.length > 2) {
                            String[] array = StringUtil.split(value[2], ";");
                            String[] opArray = StringUtil.split(array[0], ":");
                            if (opArray.length > 1 && ARRAYITEM_DATASET_AGGREGATE.equalsIgnoreCase(opArray[1])) {
                                lastArrayItemDataSet = false;
                            }
                            if (array.length > 1) {
                                operation = array[1];
                            }
                            if (array.length > 2 && array[2].equalsIgnoreCase(IGNORE_MISSING_TEXT)) {
                                ignoreMissing = true;
                            }
                        }
                        if (operation.length() == 0) {
                            operation = "Average";
                        }
                        if (operation.equalsIgnoreCase(INDIV_REPLICATETEXT)) {
                            toIndivReplicate = true;
                        }
                    }
                    if (!toIndivReplicate && (loadH.contains("D") || loadV.contains("D"))) {
                        Logger.logInfo("PromoteArrayResults: " + tp.translate("As dilution exists, Parameter [paramid] of Param Type [paramtype] in [zone] zone is propagated only if \"To Individual Replicate\" option is chosen in Push Rule.", tokenMap));
                        continue;
                    }
                    if (!numericData && !toIndivReplicate && (loadH.contains("R") || loadV.contains("R"))) {
                        Logger.logInfo("PromoteArrayResults: " + tp.translate("As repeat exists, non numeric Parameter [paramid] of Param Type [paramtype] in [zone] zone is propagated only if \"To Individual Replicate\" option is chosen in Push Rule.", tokenMap));
                        continue;
                    }
                    if (!operation.equalsIgnoreCase("Average") && !operation.equalsIgnoreCase(AVG_TEXT) || toIndivReplicate) {
                        ignoreMissing = true;
                    }
                    if (aiDataItems.getRowCount() <= 0) continue;
                    if (lastArrayItemDataSet) {
                        aiDataItems.sort("arraymethodinstance D");
                        HashMap<String, BigDecimal> filterLastDataset = new HashMap<String, BigDecimal>();
                        filterLastDataset.put("arraymethodinstance", aiDataItems.getBigDecimal(0, "arraymethodinstance"));
                        aiDataItems = aiDataItems.getFilteredDataSet(filterLastDataset);
                    }
                    ArrayList<Object> groups = new ArrayList();
                    if (rule.startsWith("ParentSample.")) {
                        aiDataItems.sort("sourcesampleid");
                        groups = aiDataItems.getGroupedDataSets("sourcesampleid");
                    } else {
                        aiDataItems.sort("contentkeyid1");
                        groups = aiDataItems.getGroupedDataSets("contentkeyid1");
                    }
                    M18NUtil m18NUtil = new M18NUtil();
                    M18NUtil userM18NUtil = new M18NUtil(this.connectionInfo);
                    for (int g = 0; g < groups.size(); ++g) {
                        DataSet sampleArrayitemDataItems = (DataSet)groups.get(g);
                        sampleArrayitemDataItems.sort("arraymethodinstance, repeatnum, XPOS, YPOS");
                        HashMap<String, ExpressionParam> values = new HashMap<String, ExpressionParam>();
                        String sampleId = sampleArrayitemDataItems.getString(0, "contentkeyid1", "");
                        String parentSampleId = sampleArrayitemDataItems.getString(0, "sourcesampleid", "");
                        DataSet dsSample = qp.getPreparedSqlDataSet("select samplestatus from s_sample where s_sampleid=?", (Object[])new String[]{sampleId});
                        String sampleStatus = dsSample.getValue(0, "samplestatus");
                        tokenMap.put("sampleid", sampleId);
                        String contentWorkItemId = sampleArrayitemDataItems.getString(0, "contentworkitemid", "");
                        String contentWorkItemInstance = sampleArrayitemDataItems.getValue(0, "contentworkiteminstance", "");
                        String result = "";
                        ArrayList<BigDecimal> dataArray = new ArrayList<BigDecimal>();
                        if (numericData) {
                            if (!toIndivReplicate) {
                                for (int d = 0; d < sampleArrayitemDataItems.getRowCount(); ++d) {
                                    if (ignoreMissing && (sampleArrayitemDataItems.getValue(d, "enteredtext").length() == 0 || sampleArrayitemDataItems.getValue(d, "transformvalue").length() == 0)) continue;
                                    dataArray.add(m18NUtil.parseBigDecimal(sampleArrayitemDataItems.getValue(d, "transformvalue", "0")));
                                }
                                if (dataArray.size() == 0) continue;
                                Object[] data = new BigDecimal[dataArray.size()];
                                for (int d = 0; d < data.length; ++d) {
                                    data[d] = (BigDecimal)dataArray.get(d);
                                }
                                ep = new ExpressionParam(paramId, 0, data);
                                values.put(paramId, ep);
                                try {
                                    String resultstr_insysclocale = "";
                                    resultstr_insysclocale = operation.equalsIgnoreCase(MIN_TEXT) ? expeval.evaluate("min([" + paramId + "])", values) : (operation.equalsIgnoreCase(MAX_TEXT) ? expeval.evaluate("max([" + paramId + "])", values) : expeval.evaluate("avg([" + paramId + "])", values));
                                    BigDecimal resultObject = m18NUtil.parseBigDecimal(resultstr_insysclocale);
                                    result = userM18NUtil.format(resultObject);
                                }
                                catch (ParseException e) {
                                    throw new SapphireException("Expression evaluation failed", e);
                                }
                            }
                        } else if (!toIndivReplicate) {
                            for (int s = 0; s < sampleArrayitemDataItems.getRowCount() && (result = sampleArrayitemDataItems.getValue(s, "enteredtext", "")).length() <= 0; ++s) {
                            }
                        }
                        if ("column".equals(uploadTo) && result.length() > 0) {
                            if ("sample".equalsIgnoreCase(columnOf)) {
                                int r = editSample.addRow();
                                editSample.setString(r, "keyid1", sampleId);
                                editSample.setString(r, "columnid", columnId2);
                                editSample.setString(r, "value", result);
                                continue;
                            }
                            if ("parentsample".equalsIgnoreCase(columnOf)) {
                                if (parentSampleId.length() > 0) {
                                    int r = editSample.addRow();
                                    editSample.setString(r, "keyid1", parentSampleId);
                                    editSample.setString(r, "columnid", columnId2);
                                    editSample.setString(r, "value", result);
                                    continue;
                                }
                                this.logger.info("PromoteArrayResults: " + tp.translate("Array DataItem result for the Parameter [paramid] of Param Type [paramtype] in [zone] zone could not be propagated. No parent sample exists for the content sample [sampleid]", tokenMap));
                                continue;
                            }
                            if (!"trackitem".equalsIgnoreCase(columnOf)) continue;
                            this.database.createPreparedResultSet("gettrackitem", "select trackitemid from trackitem where linksdcid = 'Sample' and linkkeyid1 = ?", new String[]{sampleId});
                            DataSet trackitem = new DataSet(this.database.getResultSet("gettrackitem"));
                            if (trackitem.getRowCount() > 0) {
                                String trackItemId = trackitem.getString(0, "trackitemid", "");
                                int r = editTrackItem.addRow();
                                editTrackItem.setString(r, "keyid1", trackItemId);
                                editTrackItem.setString(r, "columnid", columnId2);
                                editTrackItem.setString(r, "value", result);
                                continue;
                            }
                            this.logger.info("PromoteArrayResults: " + tp.translate("Array DataItem result for the Parameter [paramid] of Param Type [paramtype] in [zone] zone could not be propagated. No TrackItem exists for the content sample [sampleid]", tokenMap));
                            continue;
                        }
                        if (!"dataitem".equals(uploadTo)) continue;
                        String paramListId = "";
                        String paramListVersionId = "";
                        String variantId = "";
                        String dataset = "";
                        String param = "";
                        String paramType = "";
                        if (paramListData.length() > 0) {
                            tokenMap.put("paramlistdata", paramListData);
                            String[] plDataArray = StringUtil.split(paramListData, ";");
                            if (plDataArray.length >= 3) {
                                paramListId = plDataArray[0];
                                paramListVersionId = plDataArray[1];
                                variantId = plDataArray[2];
                                if (plDataArray.length > 3) {
                                    dataset = plDataArray[3];
                                }
                            } else {
                                this.logger.info("PromoteArrayResults: " + tp.translate("Invalid Paramlist data [paramlistdata] found in arrayparamitem propagation rule. Result for the Parameter [paramid] of Param Type [paramtype] in [zone] zone could not be propagated in the content sample [sampleid]", tokenMap));
                                continue block8;
                            }
                        }
                        if (paramData.length() > 0) {
                            String[] paramDataArray = StringUtil.split(paramData, ";");
                            if (paramDataArray.length == 2) {
                                param = paramDataArray[0];
                                paramType = paramDataArray[1];
                            } else {
                                tokenMap.put("paramdata", paramData);
                                this.logger.info("PromoteArrayResults: " + tp.translate("Invalid Param data [paramdata] found in arrayparamitem propagation rule. Result for the Parameter [paramid] of Param Type [paramtype] in [zone] zone could not be propagated in the content sample [sampleid]", tokenMap));
                                continue block8;
                            }
                        }
                        this.database.createPreparedResultSet("getdataitems", "select distinct di.sdcid, di.keyid1, di.keyid2, di.keyid3, di.paramlistid, di.paramlistversionid, di.variantid, di.dataset, di.paramid, di.paramtype, di.replicateid, di.releasedflag, ds.s_datasetstatus  from sdidataitem di, sdidata ds where di.sdcid = ds.sdcid and di.keyid1 = ds.keyid1 and di.keyid2 = ds.keyid2 and di.keyid3 = ds.keyid3  and di.paramlistid = ds.paramlistid and di.paramlistversionid = ds.paramlistversionid and di.dataset = ds.dataset and di.paramid = ? and di.paramtype = ?  and ds.sdcid ='Sample' and ds.keyid1 = ? and ds.paramlistid = ? and ds.paramlistversionid = ? and ds.variantid = ? and ds.sourceworkitemid = ? and ds.sourceworkiteminstance = ?", new String[]{param, paramType, sampleId, paramListId, paramListVersionId, variantId, contentWorkItemId, contentWorkItemInstance});
                        DataSet allDataitems = new DataSet(this.database.getResultSet("getdataitems"));
                        if (allDataitems.getRowCount() > 0) {
                            if (sampleStatus.equalsIgnoreCase("Completed") || sampleStatus.equalsIgnoreCase("Reviewed")) {
                                throw new SapphireException(tp.translate("ArrayItem result cannot be pushed to Sample") + ": " + sampleId + ". " + tp.translate("Status of Sample is") + ": " + sampleStatus);
                            }
                            DataSet dataitem = new DataSet();
                            HashMap<String, Object> filterDS = new HashMap<String, Object>();
                            filterDS.put("s_datasetstatus", "Completed");
                            DataSet notCompletedDataitems = allDataitems.getFilteredDataSet(filterDS, true);
                            filterDS = new HashMap();
                            filterDS.put("s_datasetstatus", "Released");
                            DataSet unreleasedDataitems = notCompletedDataitems.getFilteredDataSet(filterDS, true);
                            if (!MAX_DATASET.equalsIgnoreCase(dataset)) {
                                dataset = MIN_DATASET;
                            }
                            if (unreleasedDataitems.getRowCount() > 0) {
                                if (dataset.equals(MAX_DATASET)) {
                                    unreleasedDataitems.sort("dataset D");
                                } else {
                                    unreleasedDataitems.sort("dataset");
                                }
                                filterDS.clear();
                                filterDS.put("dataset", unreleasedDataitems.getBigDecimal(0, "dataset"));
                                dataitem = unreleasedDataitems.getFilteredDataSet(filterDS);
                            } else {
                                if (dataset.equals(MAX_DATASET)) {
                                    allDataitems.sort("dataset D");
                                } else {
                                    allDataitems.sort("dataset");
                                }
                                filterDS.clear();
                                filterDS.put("dataset", allDataitems.getBigDecimal(0, "dataset"));
                                dataitem = allDataitems.getFilteredDataSet(filterDS);
                            }
                            if (unreleasedDataitems.getRowCount() == 0) {
                                if (this.createNewIfReleased) {
                                    this.logger.info("PromoteArrayResults: " + tp.translate("DataSet found to be in " + dataitem.getValue(0, "s_datasetstatus") + " status. Creating a new dataset to push the ArrayItem results."));
                                    PropertyList props2 = new PropertyList();
                                    props2.setProperty("sdcid", "Sample");
                                    props2.setProperty("keyid1", dataitem.getValue(0, "keyid1"));
                                    props2.setProperty("keyid2", dataitem.getValue(0, "keyid2"));
                                    props2.setProperty("keyid3", dataitem.getValue(0, "keyid3"));
                                    props2.setProperty("paramlistid", dataitem.getValue(0, "paramlistid"));
                                    props2.setProperty("paramlistversionid", dataitem.getValue(0, "paramlistversionid"));
                                    props2.setProperty("variantid", dataitem.getValue(0, "variantid"));
                                    props2.setProperty("dataset", dataitem.getValue(0, "dataset"));
                                    props2.setProperty("auditreason", properties.getProperty("auditreason"));
                                    props2.setProperty("auditsignedflag", properties.getProperty("auditactivity"));
                                    props2.setProperty("auditactivity", properties.getProperty("auditsignedflag"));
                                    props2.put("newdsstatus", "Initial");
                                    ap.processAction("RemeasureDataSet", "1", props2);
                                    DataSet dsInstances = new DataSet(props2.getProperty("newdatasetinstancexml"));
                                    String maxInstance = dsInstances.getValue(0, "dataset");
                                    this.database.createPreparedResultSet("getnewdataitems", "select di.sdcid, di.keyid1, di.keyid2, di.keyid3, di.paramlistid, di.paramlistversionid, di.variantid, di.dataset, di.paramid, di.paramtype, di.replicateid, di.releasedflag, ds.s_datasetstatus  from sdidataitem di, sdidata ds where di.sdcid = ds.sdcid and di.keyid1 = ds.keyid1 and di.keyid2 = ds.keyid2 and di.keyid3 = ds.keyid3  and di.paramlistid = ds.paramlistid and di.paramlistversionid = ds.paramlistversionid and di.dataset = ds.dataset and di.paramid = ? and di.paramtype = ?  and ds.sdcid ='Sample' and ds.keyid1 = ? and ds.paramlistid = ? and ds.paramlistversionid = ? and ds.variantid = ? and ds.sourceworkitemid = ? and ds.sourceworkiteminstance = ? and ds.dataset = ?", new String[]{param, paramType, sampleId, paramListId, paramListVersionId, variantId, contentWorkItemId, contentWorkItemInstance, maxInstance});
                                    dataitem = new DataSet(this.database.getResultSet("getnewdataitems"));
                                } else {
                                    throw new SapphireException("PromoteArrayResults: " + tp.translate("ArrayItem result cannot be pushed to Sample ") + dataitem.getValue(0, "keyid1") + ", DataSet: " + dataitem.getValue(0, "paramlistid") + "( Ver:" + dataitem.getValue(0, "paramlistversionid") + ", Variant:" + dataitem.getValue(0, "variantid") + ") DS#:" + dataitem.getValue(0, "dataset") + " is in " + dataitem.getValue(0, "s_datasetstatus") + " status.");
                                }
                            }
                            int numReplicateToAdd = 0;
                            if (toIndivReplicate) {
                                dataitem.sort("replicateid D");
                                int maxExistingReplicate = dataitem.getInt(0, "replicateid");
                                String targetDataset = dataitem.getValue(0, "dataset");
                                sampleArrayitemDataItems.sort("arraymethodinstance, repeatnum, XPOS, YPOS");
                                int replicate = 1;
                                ArrayList<String> repeatnumList = new ArrayList<String>();
                                for (int d = 0; d < sampleArrayitemDataItems.getRowCount(); ++d) {
                                    boolean dataItemExists;
                                    result = sampleArrayitemDataItems.getValue(d, "enteredtext", "");
                                    String repeatnum = sampleArrayitemDataItems.getValue(d, "repeatnum");
                                    if (result.length() == 0) {
                                        if (repeatnum.length() <= 0) continue;
                                        ++replicate;
                                        continue;
                                    }
                                    int row = -1;
                                    HashMap<String, BigDecimal> find = new HashMap<String, BigDecimal>();
                                    int currentReplicate = 0;
                                    if (repeatnum.length() > 0 && !repeatnumList.contains(repeatnum)) {
                                        find.put("dataset", new BigDecimal(targetDataset));
                                        find.put("replicateid", new BigDecimal(repeatnum));
                                        currentReplicate = new Integer(repeatnum);
                                        if (currentReplicate == 0) {
                                            currentReplicate = 1;
                                        }
                                        repeatnumList.add(repeatnum);
                                    } else {
                                        find.put("dataset", new BigDecimal(targetDataset));
                                        find.put("replicateid", new BigDecimal(replicate));
                                        currentReplicate = new Integer(replicate);
                                    }
                                    row = dataitem.findRow(find);
                                    boolean bl = dataItemExists = row > -1;
                                    if (!dataItemExists) {
                                        if (currentReplicate > maxExistingReplicate + numReplicateToAdd) {
                                            numReplicateToAdd = currentReplicate - maxExistingReplicate;
                                        }
                                    } else if ("Y".equalsIgnoreCase(dataitem.getValue(row, "releasedflag"))) {
                                        throw new SapphireException("PromoteArrayResults: " + tp.translate("ArrayItem result cannot be pushed to DataItem: ") + dataitem.getValue(row, "keyid1") + ", " + dataitem.getValue(row, "paramlistid") + "( Ver:" + dataitem.getValue(row, "paramlistversionid") + ", Variant:" + dataitem.getValue(row, "variantid") + ") DataSet:" + dataitem.getValue(row, "dataset") + ", ParamId:" + dataitem.getValue(row, "paramid") + ", ParamType:" + dataitem.getValue(row, "paramtype") + ", Replicate:" + dataitem.getValue(row, "replicateid") + tp.translate(" is in \"Released\" status."));
                                    }
                                    int r = enterDataItemPL.addRow();
                                    enterDataItemPL.setString(r, "keyid1", sampleId);
                                    enterDataItemPL.setString(r, "paramlistid", paramListId);
                                    enterDataItemPL.setString(r, "paramlistversionid", paramListVersionId);
                                    enterDataItemPL.setString(r, "variantid", variantId);
                                    enterDataItemPL.setString(r, "dataset", targetDataset);
                                    enterDataItemPL.setString(r, "replicateid", "" + currentReplicate);
                                    enterDataItemPL.setString(r, "paramid", param);
                                    enterDataItemPL.setString(r, "paramtype", paramType);
                                    enterDataItemPL.setString(r, "enteredtext", result);
                                    if (!samples.contains(sampleId)) {
                                        samples.add(sampleId);
                                    }
                                    ++replicate;
                                }
                                if (numReplicateToAdd <= 0) continue;
                                int r = addReplicate.addRow();
                                addReplicate.setString(r, "keyid1", sampleId);
                                addReplicate.setString(r, "paramlistid", paramListId);
                                addReplicate.setString(r, "paramlistversionid", paramListVersionId);
                                addReplicate.setString(r, "variantid", variantId);
                                addReplicate.setString(r, "dataset", targetDataset);
                                addReplicate.setString(r, "paramid", param);
                                addReplicate.setString(r, "paramtype", paramType);
                                addReplicate.setString(r, "numreplicate", "" + numReplicateToAdd);
                                continue;
                            }
                            if (result.length() <= 0) continue;
                            for (int di = 0; di < dataitem.getRowCount(); ++di) {
                                if ("Y".equalsIgnoreCase(dataitem.getValue(di, "releasedflag"))) {
                                    Logger.logInfo("PromoteArrayResults: " + tp.translate("DataItem ") + dataitem.getValue(di, "keyid1") + ", " + dataitem.getValue(di, "paramlistid") + "( Ver:" + dataitem.getValue(di, "paramlistversionid") + ", Variant:" + dataitem.getValue(di, "variantid") + ") DataSet:" + dataitem.getValue(di, "dataset") + ", ParamId:" + dataitem.getValue(di, "paramid") + ", ParamType:" + dataitem.getValue(di, "paramtype") + ", Replicate:" + dataitem.getValue(di, "replicateid") + tp.translate(" is released. ArrayItem Data cannot be pushed."));
                                }
                                int r = enterDataItemPL.addRow();
                                enterDataItemPL.setString(r, "keyid1", sampleId);
                                enterDataItemPL.setString(r, "paramlistid", paramListId);
                                enterDataItemPL.setString(r, "paramlistversionid", dataitem.getValue(di, "paramlistversionid"));
                                enterDataItemPL.setString(r, "variantid", dataitem.getValue(di, "variantid"));
                                enterDataItemPL.setString(r, "dataset", dataitem.getValue(di, "dataset"));
                                enterDataItemPL.setString(r, "paramid", param);
                                enterDataItemPL.setString(r, "paramtype", dataitem.getValue(di, "paramtype"));
                                enterDataItemPL.setString(r, "replicateid", dataitem.getValue(di, "replicateid"));
                                enterDataItemPL.setString(r, "enteredtext", result);
                                if (samples.contains(sampleId)) continue;
                                samples.add(sampleId);
                            }
                            continue;
                        }
                        this.logger.info("PromoteArrayResults: " + tp.translate(" Array DataItem result for the Parameter [paramid] of Param Type [paramtype] in [zone] zone could not be propagated. Required dataitems not found in the content sample [sampleid]", tokenMap));
                    }
                }
            }
        }
        catch (Exception e) {
            throw new SapphireException(e);
        }
        finally {
            this.database.closeResultSet("dataitems");
            this.database.closeResultSet("getdataitems");
            this.database.closeResultSet("getnewdataitems");
            this.database.closeResultSet("getparentsample");
            this.database.closeResultSet("gettrackitem");
        }
        if (editSample.getRowCount() > 0) {
            editSample.sort("columnid");
            ArrayList<DataSet> colGroups = editSample.getGroupedDataSets("columnid");
            props = new PropertyList();
            for (int g = 0; g < colGroups.size(); ++g) {
                ds = colGroups.get(g);
                columnId = ds.getString(0, "columnid");
                props.setProperty("sdcid", "Sample");
                props.setProperty("keyid1", ds.getColumnValues("keyid1", ";"));
                props.setProperty(columnId, ds.getColumnValues("value", ";"));
                props.setProperty("auditsignedflag", properties.getProperty("auditsignedflag"));
                props.setProperty("auditactivity", properties.getProperty("auditactivity"));
                props.setProperty("auditreason", properties.getProperty("auditreason"));
                ap.processAction("EditSDI", "1", props);
                props.clear();
            }
        }
        if (editTrackItem.getRowCount() > 0) {
            editTrackItem.sort("columnid");
            ArrayList<DataSet> colGroups = editTrackItem.getGroupedDataSets("columnid");
            props = new PropertyList();
            for (int g = 0; g < colGroups.size(); ++g) {
                ds = colGroups.get(g);
                columnId = ds.getString(0, "columnid");
                props.setProperty("sdcid", "TrackItemSDC");
                props.setProperty("keyid1", ds.getColumnValues("keyid1", ";"));
                props.setProperty(columnId, ds.getColumnValues("value", ";"));
                props.setProperty("auditsignedflag", properties.getProperty("auditsignedflag"));
                props.setProperty("auditactivity", properties.getProperty("auditactivity"));
                props.setProperty("auditreason", properties.getProperty("auditreason"));
                ap.processAction("EditSDI", "1", props);
                props.clear();
            }
        }
        if (addReplicate.getRowCount() > 0) {
            PropertyList props3 = new PropertyList();
            props3.setProperty("sdcid", "Sample");
            props3.setProperty("keyid1", addReplicate.getColumnValues("keyid1", ";"));
            props3.setProperty("paramlistid", addReplicate.getColumnValues("paramlistid", ";"));
            props3.setProperty("paramlistversionid", addReplicate.getColumnValues("paramlistversionid", ";"));
            props3.setProperty("variantid", addReplicate.getColumnValues("variantid", ";"));
            props3.setProperty("dataset", addReplicate.getColumnValues("dataset", ";"));
            props3.setProperty("paramid", addReplicate.getColumnValues("paramid", ";"));
            props3.setProperty("paramtype", addReplicate.getColumnValues("paramtype", ";"));
            props3.setProperty("numreplicate", addReplicate.getColumnValues("numreplicate", ";"));
            props3.setProperty("propsmatch", "Y");
            props3.setProperty("auditactivity", properties.getProperty("auditactivity"));
            props3.setProperty("auditreason", properties.getProperty("auditreason"));
            props3.setProperty("auditsignedflag", properties.getProperty("auditsignedflag"));
            ap.processAction("AddReplicate", "1", props3);
        }
        if (enterDataItemPL.getRowCount() > 0) {
            PropertyList props4 = new PropertyList();
            props4.setProperty("sdcid", "Sample");
            props4.setProperty("keyid1", enterDataItemPL.getColumnValues("keyid1", ";"));
            props4.setProperty("paramlistid", enterDataItemPL.getColumnValues("paramlistid", ";"));
            props4.setProperty("paramlistversionid", enterDataItemPL.getColumnValues("paramlistversionid", ";"));
            props4.setProperty("variantid", enterDataItemPL.getColumnValues("variantid", ";"));
            props4.setProperty("dataset", enterDataItemPL.getColumnValues("dataset", ";"));
            props4.setProperty("paramid", enterDataItemPL.getColumnValues("paramid", ";"));
            props4.setProperty("paramtype", enterDataItemPL.getColumnValues("paramtype", ";"));
            props4.setProperty("replicateid", enterDataItemPL.getColumnValues("replicateid", ";"));
            props4.setProperty("enteredtext", enterDataItemPL.getColumnValues("enteredtext", ";"));
            props4.setProperty("propsmatch", "Y");
            props4.setProperty("auditactivity", properties.getProperty("auditactivity"));
            props4.setProperty("auditreason", properties.getProperty("auditreason"));
            props4.setProperty("auditsignedflag", properties.getProperty("auditsignedflag"));
            ap.processAction("EnterDataItem", "1", props4);
            for (int s = 0; s < samples.size(); ++s) {
                String sampleId = (String)samples.get(s);
                if (updateSamples.findRow("keyid1", sampleId) >= 0) continue;
                int r = updateSamples.addRow();
                updateSamples.setString(r, "keyid1", sampleId);
            }
        }
    }
}

