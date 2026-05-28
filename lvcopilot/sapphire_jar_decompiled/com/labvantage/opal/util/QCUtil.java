/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.util;

import com.labvantage.opal.actions.QCBatchReagentSync;
import com.labvantage.opal.qcbatch.QCBatch;
import com.labvantage.opal.qcbatch.QCBatchItem;
import com.labvantage.opal.sql.SQLGenerator;
import com.labvantage.opal.sql.common.Aqc;
import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.Trace;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.DAMProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SequenceProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.DBAccess;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class QCUtil {
    private static String fieldName_QCBATCHID = "s_qcbatchid";
    private static String fieldName_QCBATCHREAGENTID = "s_qcbatchreagentid";
    private static String fieldName_REAGENTLOTID = "reagentlotid";
    private static String fieldName_TRACKITEMID = "trackitemid";

    public static void addQCBatchSampleTypeParamSet(DBAccess db, QueryProcessor qp, SQLGenerator sqlGenerator, SequenceProcessor sq, String qcBatchId) throws SapphireException {
        try {
            int dsSize;
            SafeSQL safeSQL = new SafeSQL();
            DataSet ds = qp.getPreparedSqlDataSet(Aqc.getParamsNotAddedInBatchParamSet(qcBatchId, safeSQL), safeSQL.getValues());
            if (ds != null && (dsSize = ds.size()) > 0) {
                DataSet dsInsert = new DataSet();
                GregorianCalendar calendar = new GregorianCalendar();
                StringBuffer sb1 = new StringBuffer();
                StringBuffer sb2 = new StringBuffer();
                StringBuffer sb3 = new StringBuffer();
                int sequence = sq.getSequence("QCBATCHPARAMSET", Integer.toString(calendar.get(1)), dsSize);
                for (int i = 0; i < dsSize; ++i) {
                    String qcBatchSampleTypeId = ds.getValue(i, "QCBATCHSAMPLETYPEID");
                    String paramId = ds.getValue(i, "PARAMID");
                    sb1.append(qcBatchSampleTypeId).append(";");
                    sb2.append("QCBPS-").append(sequence + i).append(";");
                    sb3.append(paramId).append(";");
                }
                if (sb1.length() > 0) {
                    sb1.setLength(sb1.length() - 1);
                    sb2.setLength(sb2.length() - 1);
                    sb3.setLength(sb3.length() - 1);
                    dsInsert.addColumnValues("s_qcbatchsampletypeid", 0, sb1.toString(), ";");
                    dsInsert.addColumnValues("s_qcbatchparamsetid", 0, sb2.toString(), ";");
                    dsInsert.addColumnValues("paramid", 0, sb3.toString(), ";");
                    DataSetUtil.insert(db, dsInsert, "s_qcbatchparamset");
                }
                safeSQL.reset();
                ds = qp.getPreparedSqlDataSet(Aqc.getEvalRuleParamToAddForQCBatch(qcBatchId, safeSQL), safeSQL.getValues());
                StringBuffer sbevalruleids = new StringBuffer();
                StringBuffer sbparamsetids = new StringBuffer();
                StringBuffer sbsampletypeids = new StringBuffer();
                if (ds != null && ds.size() > 0) {
                    dsSize = ds.size();
                    for (int j = 0; j < dsSize; ++j) {
                        String evalRuleId = ds.getValue(j, "S_QCBATCHEVALRULEID");
                        String qcBatchSampleTypeId = ds.getValue(j, "S_QCBATCHSAMPLETYPEID");
                        String qcBatchParamSetId = ds.getValue(j, "S_QCBATCHPARAMSETID");
                        sbevalruleids.append(evalRuleId + ";");
                        sbparamsetids.append(qcBatchParamSetId + ";");
                        sbsampletypeids.append(qcBatchSampleTypeId + ";");
                    }
                    if (sbevalruleids.length() > 0) {
                        dsInsert = null;
                        dsInsert = new DataSet();
                        sbsampletypeids.setLength(sbsampletypeids.length() - 1);
                        sbparamsetids.setLength(sbparamsetids.length() - 1);
                        sbevalruleids.setLength(sbevalruleids.length() - 1);
                        dsInsert.addColumnValues("s_qcbatchsampletypeid", 0, sbsampletypeids.toString(), ";");
                        dsInsert.addColumnValues("s_qcbatchparamsetid", 0, sbparamsetids.toString(), ";");
                        dsInsert.addColumnValues("s_qcbatchevalruleid", 0, sbevalruleids.toString(), ";");
                        DataSetUtil.insert(db, dsInsert, "s_qcbatchevalruleparam");
                    }
                }
            }
        }
        catch (Exception e) {
            throw new SapphireException("Failed to save data ", e);
        }
    }

    public static String getDistinctUnknownDatasetsSQL(String paramListType, boolean matchWorkItemItem) {
        StringBuffer sql = new StringBuffer();
        if (paramListType == null) {
            paramListType = "";
        }
        sql.append(" select distinct ");
        sql.append(" T1.sdcid, T1.keyid1, ");
        sql.append(" T1.keyid2, T1.keyid3, ");
        sql.append(" T1.paramlistid, ");
        sql.append(" T1.paramlistversionid, ");
        sql.append(" T1.variantid, T1.dataset, T1.sdidataid").append(matchWorkItemItem ? ", sdiworkitem.sdiworkitemid" : "");
        sql.append(" from  s_qcmethod_workitem, workitemitem, sdidata T1");
        if (matchWorkItemItem) {
            sql.append(", sdiworkitemitem, sdiworkitem");
        }
        if (paramListType.trim().length() > 0) {
            sql.append(", paramlist");
        }
        sql.append(" where s_qcmethod_workitem.workitemid = workitemitem.workitemid and coalesce(s_qcmethod_workitem.workitemversionid,workitemitem.workitemversionid) = workitemitem.workitemversionid ");
        sql.append(" and workitemitem.keyid1 = T1.paramlistid and ");
        sql.append(" (workitemitem.keyid2 ='C' or workitemitem.keyid2 = T1.paramlistversionid)");
        sql.append(" and workitemitem.keyid3=T1.variantid and T1.sdcid = ? ");
        if (paramListType.trim().length() > 0) {
            sql.append(" and T1.paramlistid = paramlist.paramlistid and T1.paramlistversionid = paramlist.paramlistversionid ");
            sql.append(" and T1.variantid = paramlist.variantid and  paramlist.s_paramlisttype = ? ");
        }
        sql.append(" and T1.keyid1 = ? ");
        sql.append(" and T1.s_datasetstatus='Initial'");
        sql.append(" and ( T1.s_qcbatchitemid is null or T1.s_qcbatchitemid = '' )");
        sql.append(" and s_qcmethod_workitem.s_qcmethodid = ? and s_qcmethod_workitem.s_qcmethodversionid = ?");
        if (matchWorkItemItem) {
            sql.append(" and  sdiworkitemitem.sdcid = T1.sdcid and  sdiworkitemitem.keyid1 = T1.keyid1 and sdiworkitemitem.keyid2 = T1.keyid2 ").append(" and sdiworkitemitem.keyid3 = T1.keyid3 and sdiworkitemitem.workitemid = s_qcmethod_workitem.workitemid ").append(" and sdiworkitemitem.itemkeyid1 = T1.paramlistid and sdiworkitemitem.itemkeyid2 = T1.paramlistversionid and sdiworkitemitem.itemkeyid3 = T1.variantid ").append(" and sdiworkitemitem.iteminstance = T1.dataset").append(" and sdiworkitem.sdcid = sdiworkitemitem.sdcid and sdiworkitem.keyid1 = sdiworkitemitem.keyid1 and sdiworkitem.keyid2 = sdiworkitemitem.keyid2 and sdiworkitem.keyid3 = sdiworkitemitem.keyid3").append(" and sdiworkitem.workitemid =  sdiworkitemitem.workitemid and sdiworkitem.workiteminstance = sdiworkitemitem.workiteminstance");
        }
        sql.append(" order by T1.dataset ");
        return sql.toString();
    }

    public static String getUnknownbasedOnSDIWorkItems(String paramListType) {
        StringBuffer sql = new StringBuffer();
        if (paramListType == null) {
            paramListType = "";
        }
        sql.append(" select distinct ");
        sql.append(" T1.sdcid, T1.keyid1, ");
        sql.append(" T1.keyid2, T1.keyid3, ");
        sql.append(" T1.paramlistid, ");
        sql.append(" T1.paramlistversionid, ");
        sql.append(" T1.variantid, T1.dataset, T1.sdidataid, sdiworkitem.sdiworkitemid");
        sql.append(" from  s_qcmethod_workitem, workitemitem, sdidata T1, sdiworkitemitem, sdiworkitem");
        if (paramListType.trim().length() > 0) {
            sql.append(", paramlist");
        }
        sql.append(" where s_qcmethod_workitem.workitemid = workitemitem.workitemid and coalesce(s_qcmethod_workitem.workitemversionid,workitemitem.workitemversionid) = workitemitem.workitemversionid ");
        sql.append(" and workitemitem.keyid1 = T1.paramlistid and ");
        sql.append(" (workitemitem.keyid2 ='C' or workitemitem.keyid2 = T1.paramlistversionid)");
        sql.append(" and workitemitem.keyid3 = T1.variantid and T1.sdcid = ? ");
        if (paramListType.trim().length() > 0) {
            sql.append(" and T1.paramlistid = paramlist.paramlistid and T1.paramlistversionid = paramlist.paramlistversionid ");
            sql.append(" and T1.variantid = paramlist.variantid and  paramlist.s_paramlisttype = ? ");
        }
        sql.append(" and sdiworkitem.sdiworkitemid = ? ");
        sql.append(" and T1.s_datasetstatus='Initial'");
        sql.append(" and ( T1.s_qcbatchitemid is null or T1.s_qcbatchitemid = '' )");
        sql.append(" and s_qcmethod_workitem.s_qcmethodid = ? and s_qcmethod_workitem.s_qcmethodversionid = ?");
        sql.append(" and  sdiworkitemitem.sdcid = T1.sdcid and  sdiworkitemitem.keyid1 = T1.keyid1 and sdiworkitemitem.keyid2 = T1.keyid2 ").append(" and sdiworkitemitem.keyid3 = T1.keyid3 and sdiworkitemitem.workitemid = s_qcmethod_workitem.workitemid ").append(" and sdiworkitemitem.itemkeyid1 = T1.paramlistid and sdiworkitemitem.itemkeyid2 = T1.paramlistversionid and sdiworkitemitem.itemkeyid3 = T1.variantid ").append(" and sdiworkitemitem.iteminstance = T1.dataset").append(" and sdiworkitem.sdcid = sdiworkitemitem.sdcid and sdiworkitem.keyid1 = sdiworkitemitem.keyid1 and sdiworkitem.keyid2 = sdiworkitemitem.keyid2 and sdiworkitem.keyid3 = sdiworkitemitem.keyid3").append(" and sdiworkitem.workitemid =  sdiworkitemitem.workitemid and sdiworkitem.workiteminstance = sdiworkitemitem.workiteminstance");
        sql.append(" order by T1.dataset ");
        return sql.toString();
    }

    public static String getUnknownbasedOnSDIDatas(String paramListType, boolean matchWorkItemItem) {
        StringBuffer sql = new StringBuffer();
        if (paramListType == null) {
            paramListType = "";
        }
        sql.append(" select distinct ");
        sql.append(" T1.sdcid, T1.keyid1, ");
        sql.append(" T1.keyid2, T1.keyid3, ");
        sql.append(" T1.paramlistid, ");
        sql.append(" T1.paramlistversionid, ");
        sql.append(" T1.variantid, T1.dataset, T1.sdidataid ");
        sql.append(" from  s_qcmethod_workitem, workitemitem, sdidata T1");
        if (matchWorkItemItem) {
            sql.append(", sdiworkitemitem, sdiworkitem");
        }
        if (paramListType.trim().length() > 0) {
            sql.append(", paramlist");
        }
        sql.append(" where s_qcmethod_workitem.workitemid = workitemitem.workitemid and coalesce(s_qcmethod_workitem.workitemversionid,workitemitem.workitemversionid) = workitemitem.workitemversionid ");
        sql.append(" and workitemitem.keyid1 = T1.paramlistid and ");
        sql.append(" (workitemitem.keyid2 ='C' or workitemitem.keyid2 = T1.paramlistversionid)");
        sql.append(" and workitemitem.keyid3 = T1.variantid and T1.sdcid = ? ");
        if (paramListType.trim().length() > 0) {
            sql.append(" and T1.paramlistid = paramlist.paramlistid and T1.paramlistversionid = paramlist.paramlistversionid ");
            sql.append(" and T1.variantid = paramlist.variantid and  paramlist.s_paramlisttype = ? ");
        }
        sql.append(" and T1.sdidataid = ? ");
        sql.append(" and T1.s_datasetstatus='Initial'");
        sql.append(" and ( T1.s_qcbatchitemid is null or T1.s_qcbatchitemid = '' )");
        sql.append(" and s_qcmethod_workitem.s_qcmethodid = ? and s_qcmethod_workitem.s_qcmethodversionid = ?");
        if (matchWorkItemItem) {
            sql.append(" and  sdiworkitemitem.sdcid = T1.sdcid and  sdiworkitemitem.keyid1 = T1.keyid1 and sdiworkitemitem.keyid2 = T1.keyid2 ").append(" and sdiworkitemitem.keyid3 = T1.keyid3 and sdiworkitemitem.workitemid = s_qcmethod_workitem.workitemid ").append(" and sdiworkitemitem.itemkeyid1 = T1.paramlistid and sdiworkitemitem.itemkeyid2 = T1.paramlistversionid and sdiworkitemitem.itemkeyid3 = T1.variantid ").append(" and sdiworkitemitem.iteminstance = T1.dataset").append(" and sdiworkitem.sdcid = sdiworkitemitem.sdcid and sdiworkitem.keyid1 = sdiworkitemitem.keyid1 and sdiworkitem.keyid2 = sdiworkitemitem.keyid2 and sdiworkitem.keyid3 = sdiworkitemitem.keyid3").append(" and sdiworkitem.workitemid =  sdiworkitemitem.workitemid and sdiworkitem.workiteminstance = sdiworkitemitem.workiteminstance");
        }
        sql.append(" order by T1.dataset ");
        return sql.toString();
    }

    public static DataSet filterBySDIWorkItem(DataSet dsUnknown, ArrayList sdiwiList, TranslationProcessor tp) throws SapphireException {
        DataSet selectedDataSets = new DataSet();
        for (int i = 0; i < dsUnknown.getRowCount(); ++i) {
            String sdiworkItemId = dsUnknown.getValue(i, "sdiworkitemid");
            if (!sdiwiList.contains(sdiworkItemId)) continue;
            HashMap<String, String> filterDatasets = new HashMap<String, String>();
            filterDatasets.put("sdiworkitemid", sdiworkItemId);
            DataSet filteredDS = dsUnknown.getFilteredDataSet(filterDatasets);
            selectedDataSets.copyRow(filteredDS, -1, 1);
            sdiwiList.remove(sdiworkItemId);
            break;
        }
        return selectedDataSets;
    }

    public static DataSet filterBySDIData(DataSet dsUnknown, ArrayList sdidsList, TranslationProcessor tp) throws SapphireException {
        DataSet selectedDataSets = new DataSet();
        for (int i = 0; i < dsUnknown.getRowCount(); ++i) {
            String sdidataId = dsUnknown.getValue(i, "sdidataid");
            if (!sdidsList.contains(sdidataId)) continue;
            selectedDataSets.copyRow(dsUnknown, i, 1);
            sdidsList.remove(sdidataId);
            break;
        }
        return selectedDataSets;
    }

    public static DataSet getCalcDataItems(QueryProcessor qp, String qcBatchId, String batchItemIds, String paramType) {
        StringBuffer sql = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        if (batchItemIds.length() > 0 && batchItemIds.startsWith("'") && batchItemIds.endsWith("'")) {
            batchItemIds = batchItemIds.substring(1, batchItemIds.length() - 1);
        }
        sql.append(" SELECT DISTINCT DI.SDCID, DI.KEYID1, DI.KEYID2, DI.KEYID3, ").append(" DI.PARAMLISTID, DI.PARAMLISTVERSIONID, DI.VARIANTID, DI.DATASET, DI.PARAMTYPE, DI.PARAMID, DI.REPLICATEID,").append(" DI.DISPLAYFORMAT, DI.CALCRULE, DI.DATATYPES, DI.USERSEQUENCE, DS.S_QCBATCHITEMID  ").append(" FROM SDIDATAITEM DI, SDIDATA DS, PARAMLIST PL ").append(" WHERE ").append(" DS.PARAMLISTID = PL.PARAMLISTID AND DS.PARAMLISTVERSIONID = PL.PARAMLISTVERSIONID ").append(" AND DS.VARIANTID = PL.VARIANTID AND  PL.S_PARAMLISTTYPE != 'Preparation' ").append(" AND DI.SDCID = DS.SDCID AND DI.KEYID1 = DS.KEYID1 ").append(" AND DI.KEYID2 = DS.KEYID2 AND DI.KEYID3 = DS.KEYID3 AND DI.PARAMLISTID = DS.PARAMLISTID ").append(" AND DI.PARAMLISTVERSIONID = DS.PARAMLISTVERSIONID AND DI.VARIANTID = DS.VARIANTID ").append(" AND DI.DATASET = DS.DATASET AND DS.S_QCBATCHID = " + safeSQL.addVar(qcBatchId)).append(" AND DS.S_QCBATCHITEMID IN ( " + safeSQL.addIn(batchItemIds) + " ) ").append(" AND ( DI.PARAMTYPE = " + safeSQL.addVar(paramType) + " OR ( DI.PARAMTYPE = 'Standard' AND DI.datatypes = 'N' ) )");
        DataSet ds = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (ds != null && ds.getRowCount() > 0) {
            int r;
            ds.addColumn("operation", 0);
            ds.setString(-1, "operation", "Add");
            for (r = 0; r < ds.getRowCount(); ++r) {
                if (!ds.getString(r, "paramtype", "").equalsIgnoreCase(paramType)) continue;
                ds.setString(r, "operation", "Edit");
                HashMap<String, Object> map = new HashMap<String, Object>();
                map.put("sdcid", ds.getString(r, "sdcid"));
                map.put("keyid1", ds.getString(r, "keyid1"));
                map.put("keyid2", ds.getString(r, "keyid2"));
                map.put("keyid3", ds.getString(r, "keyid3"));
                map.put("paramlistid", ds.getString(r, "paramlistid"));
                map.put("paramlistversionid", ds.getString(r, "paramlistversionid"));
                map.put("variantid", ds.getString(r, "variantid"));
                map.put("dataset", ds.getBigDecimal(r, "dataset"));
                map.put("paramid", ds.getString(r, "paramid"));
                map.put("replicateid", ds.getBigDecimal(r, "replicateid"));
                map.put("paramtype", "Standard");
                int findRow = ds.findRow(map);
                if (findRow <= -1) continue;
                ds.setString(findRow, "operation", "");
            }
            for (r = 0; r < ds.getRowCount(); ++r) {
                if (!ds.getString(r, "operation", "").equals("")) continue;
                ds.remove(r);
            }
        }
        ds.setString(-1, "paramtype", paramType);
        return ds;
    }

    public static void setCalcRule(DataSet calcDataItems, String calcRule, QCBatch qcBatch, String[] batchItemIds) {
        HashMap<String, String> ruleBIMap = new HashMap<String, String>();
        for (int i = 0; i < batchItemIds.length; ++i) {
            QCBatchItem batchItem = qcBatch.getQCBatchItem(batchItemIds[i]);
            if (calcRule.indexOf("CURRENTITEM") > -1) {
                QCBatchItem blankParent = QCUtil.getBlankParent(qcBatch, batchItem);
                calcRule = blankParent != null && "QCCalcBlankSubtract".equalsIgnoreCase(blankParent.getActionCalc()) ? calcRule.replaceAll("CURRENTITEM", "BlankCorrected") : calcRule.replaceAll("CURRENTITEM", "Standard");
            }
            ruleBIMap.put(batchItemIds[i], calcRule);
        }
        for (int r = 0; r < calcDataItems.getRowCount(); ++r) {
            String batchItemId = calcDataItems.getString(r, "s_qcbatchitemid");
            String calculationRule = (String)ruleBIMap.get(batchItemId);
            String calcRuleBefore = calcDataItems.getString(r, "calcrule", "");
            if (calcRuleBefore.equals(calculationRule)) {
                calcDataItems.setString(r, "operation", "");
                continue;
            }
            calcDataItems.setString(r, "calcrule", calculationRule);
        }
    }

    public static void setCalcRule(DataSet calcDataItems, String calcRule, QCBatch qcBatch, String[] batchItemIds, String[] linkedItemIds) {
        HashMap<String, String> ruleBIMap = new HashMap<String, String>();
        for (int i = 0; i < batchItemIds.length; ++i) {
            QCBatchItem blankParent;
            QCBatchItem batchItem = qcBatch.getQCBatchItem(batchItemIds[i]);
            QCBatchItem linkedBatchItem = qcBatch.getQCBatchItem(linkedItemIds[i]);
            if (calcRule.indexOf("CURRENTITEM") > -1) {
                blankParent = QCUtil.getBlankParent(qcBatch, batchItem);
                calcRule = blankParent != null && "QCCalcBlankSubtract".equalsIgnoreCase(blankParent.getActionCalc()) ? calcRule.replaceAll("CURRENTITEM", "BlankCorrected") : calcRule.replaceAll("CURRENTITEM", "Standard");
            }
            if (calcRule.indexOf("LINKEDITEM") > -1) {
                blankParent = QCUtil.getBlankParent(qcBatch, linkedBatchItem);
                calcRule = blankParent != null && "QCCalcBlankSubtract".equalsIgnoreCase(blankParent.getActionCalc()) ? calcRule.replaceAll("LINKEDITEM", "BlankCorrected") : calcRule.replaceAll("LINKEDITEM", "Standard");
            }
            ruleBIMap.put(batchItemIds[i], calcRule);
        }
        for (int r = 0; r < calcDataItems.getRowCount(); ++r) {
            String batchItemId = calcDataItems.getString(r, "s_qcbatchitemid");
            String calculationRule = (String)ruleBIMap.get(batchItemId);
            String calcRuleBefore = calcDataItems.getString(r, "calcrule", "");
            if (calcRuleBefore.equals(calculationRule)) {
                calcDataItems.setString(r, "operation", "");
                continue;
            }
            calcDataItems.setString(r, "calcrule", calculationRule);
        }
    }

    public static DataSet getQCBatchItems(String batchSamplTypeId, QueryProcessor qp) {
        DataSet ds = qp.getPreparedSqlDataSet("qcBatchDetails", "SELECT  s_qcbatchitemid, linktoqcbatchitemid FROM s_qcbatchitem WHERE qcbatchsampletypeid = ? ORDER BY usersequence", new Object[]{batchSamplTypeId});
        return ds;
    }

    public static boolean inBlankBracket(QCBatch qcBatch, QCBatchItem batchItem) {
        QCBatchItem prevBatchItem = qcBatch.getPreviousBatchItem(batchItem);
        while (prevBatchItem != null) {
            if ("Blank".equalsIgnoreCase(prevBatchItem.getQCSampleType())) {
                return true;
            }
            prevBatchItem = qcBatch.getPreviousBatchItem(prevBatchItem);
        }
        return false;
    }

    public static QCBatchItem getBlankParent(QCBatch qcBatch, QCBatchItem batchItem) {
        QCBatchItem prevBatchItem = qcBatch.getPreviousBatchItem(batchItem);
        while (prevBatchItem != null) {
            if ("Blank".equalsIgnoreCase(prevBatchItem.getQCSampleType())) {
                return prevBatchItem;
            }
            prevBatchItem = qcBatch.getPreviousBatchItem(prevBatchItem);
        }
        return null;
    }

    public static List getBatchItems(QCBatch qcBatch, QCBatchItem qcBatchItem, String sampleType, String mode, String level) {
        boolean levelProvided;
        List qcBatchItems = qcBatch.getQCBatchItems();
        int currIndex = qcBatchItems.indexOf(qcBatchItem);
        ArrayList<QCBatchItem> returnObjects = new ArrayList<QCBatchItem>();
        boolean bl = levelProvided = level.length() > 0;
        if ("All".equalsIgnoreCase(mode)) {
            for (int i = 0; i < qcBatchItems.size(); ++i) {
                QCBatchItem item = (QCBatchItem)qcBatchItems.get(i);
                if (!sampleType.equalsIgnoreCase(item.getQCSampleType())) continue;
                if (levelProvided) {
                    if (!level.equalsIgnoreCase(item.getStandardLevel())) continue;
                    returnObjects.add(item);
                    continue;
                }
                returnObjects.add(item);
            }
        } else if ("First".equalsIgnoreCase(mode)) {
            for (int i = 0; i < qcBatchItems.size(); ++i) {
                QCBatchItem item = (QCBatchItem)qcBatchItems.get(i);
                if (sampleType.equalsIgnoreCase(item.getQCSampleType())) {
                    if (levelProvided) {
                        if (!level.equalsIgnoreCase(item.getStandardLevel())) continue;
                        returnObjects.add(item);
                    } else {
                        returnObjects.add(item);
                    }
                } else if (returnObjects.size() <= 0) {
                    continue;
                }
                break;
            }
        } else if ("Last".equalsIgnoreCase(mode)) {
            for (int i = qcBatchItems.size() - 1; i >= 0; --i) {
                QCBatchItem item = (QCBatchItem)qcBatchItems.get(i);
                if (sampleType.equalsIgnoreCase(item.getQCSampleType())) {
                    if (levelProvided) {
                        if (!level.equalsIgnoreCase(item.getStandardLevel())) continue;
                        returnObjects.add(item);
                    } else {
                        returnObjects.add(item);
                    }
                } else if (returnObjects.size() <= 0) {
                    continue;
                }
                break;
            }
        } else if ("Previous".equalsIgnoreCase(mode)) {
            int start = currIndex - 1;
            int end = 0;
            for (int k = start; k >= end; --k) {
                QCBatchItem item = (QCBatchItem)qcBatchItems.get(k);
                if (sampleType.equalsIgnoreCase(item.getQCSampleType())) {
                    if (levelProvided) {
                        if (!level.equalsIgnoreCase(item.getStandardLevel())) continue;
                        returnObjects.add(item);
                        continue;
                    }
                    returnObjects.add(item);
                    continue;
                }
                if (returnObjects.size() <= 0) {
                    continue;
                }
                break;
            }
        } else if ("Next".equalsIgnoreCase(mode)) {
            int start = currIndex + 1;
            int end = qcBatchItems.size() - 1;
            for (int k = start; k <= end; ++k) {
                QCBatchItem item = (QCBatchItem)qcBatchItems.get(k);
                if (sampleType.equalsIgnoreCase(item.getQCSampleType())) {
                    if (levelProvided) {
                        if (!level.equalsIgnoreCase(item.getStandardLevel())) continue;
                        returnObjects.add(item);
                        continue;
                    }
                    returnObjects.add(item);
                    continue;
                }
                if (returnObjects.size() <= 0) {
                    continue;
                }
                break;
            }
        } else if ("Leading".equalsIgnoreCase(mode)) {
            int start = currIndex - 1;
            int end = 0;
            for (int k = start; k >= end; --k) {
                QCBatchItem item = (QCBatchItem)qcBatchItems.get(k);
                if (!sampleType.equalsIgnoreCase(item.getQCSampleType())) continue;
                if (levelProvided) {
                    if (!level.equalsIgnoreCase(item.getStandardLevel())) continue;
                    returnObjects.add(item);
                    continue;
                }
                returnObjects.add(item);
            }
        } else if ("Trailing".equalsIgnoreCase(mode)) {
            int start = currIndex + 1;
            int end = qcBatchItems.size() - 1;
            for (int k = start; k <= end; ++k) {
                QCBatchItem item = (QCBatchItem)qcBatchItems.get(k);
                if (!sampleType.equalsIgnoreCase(item.getQCSampleType())) continue;
                if (levelProvided) {
                    if (!level.equalsIgnoreCase(item.getStandardLevel())) continue;
                    returnObjects.add(item);
                    continue;
                }
                returnObjects.add(item);
            }
        } else if ("Bracket".equalsIgnoreCase(mode)) {
            QCBatchItem item;
            int k;
            int start = currIndex - 1;
            int end = 0;
            ArrayList<QCBatchItem> objs = new ArrayList<QCBatchItem>();
            for (k = start; k >= end; --k) {
                item = (QCBatchItem)qcBatchItems.get(k);
                if (sampleType.equalsIgnoreCase(item.getQCSampleType())) {
                    if (levelProvided) {
                        if (!level.equalsIgnoreCase(item.getStandardLevel())) continue;
                        objs.add(item);
                        continue;
                    }
                    objs.add(item);
                    continue;
                }
                if (objs.size() > 0) break;
            }
            if (objs.size() > 0) {
                returnObjects.addAll(objs);
                objs = new ArrayList();
            }
            start = currIndex + 1;
            end = qcBatchItems.size() - 1;
            for (k = start; k <= end; ++k) {
                item = (QCBatchItem)qcBatchItems.get(k);
                if (sampleType.equalsIgnoreCase(item.getQCSampleType())) {
                    if (levelProvided) {
                        if (!level.equalsIgnoreCase(item.getStandardLevel())) continue;
                        objs.add(item);
                        continue;
                    }
                    objs.add(item);
                    continue;
                }
                if (objs.size() > 0) break;
            }
            if (objs.size() > 0) {
                returnObjects.addAll(objs);
            }
        }
        return returnObjects;
    }

    public static DataSet getLinkedDataitems(DataSet sdidata, DataSet sdidataitems, String qcBatchId, String linkedBatchItemId) {
        DataSet linkedDataItems = new DataSet();
        HashMap<String, Object> filterMap = new HashMap<String, Object>();
        filterMap.put("s_qcbatchid", qcBatchId);
        filterMap.put("s_qcbatchitemid", linkedBatchItemId);
        DataSet sdidataFiltered = sdidata.getFilteredDataSet(filterMap);
        if (sdidataFiltered.getRowCount() > 0) {
            for (int i = 0; i < sdidataFiltered.getRowCount(); ++i) {
                filterMap.clear();
                filterMap.put("sdcid", sdidataFiltered.getString(i, "sdcid"));
                filterMap.put("keyid1", sdidataFiltered.getString(i, "keyid1"));
                filterMap.put("keyid2", sdidataFiltered.getString(i, "keyid2"));
                filterMap.put("keyid3", sdidataFiltered.getString(i, "keyid3"));
                filterMap.put("paramlistid", sdidataFiltered.getString(i, "paramlistid"));
                filterMap.put("paramlistversionid", sdidataFiltered.getString(i, "paramlistversionid"));
                filterMap.put("variantid", sdidataFiltered.getString(i, "variantid"));
                filterMap.put("dataset", sdidataFiltered.getBigDecimal(i, "dataset"));
                DataSet dsItems = sdidataitems.getFilteredDataSet(filterMap);
                if (dsItems == null || dsItems.getRowCount() <= 0) continue;
                linkedDataItems.addAll(dsItems);
            }
        }
        return linkedDataItems;
    }

    public static DataSet getLinkedDataSets(DataSet sdidata, String qcBatchId, String linkedBatchItemId) {
        HashMap<String, String> filterMap = new HashMap<String, String>();
        filterMap.put("s_qcbatchid", qcBatchId);
        filterMap.put("s_qcbatchitemid", linkedBatchItemId);
        DataSet linkedDataSets = sdidata.getFilteredDataSet(filterMap);
        return linkedDataSets;
    }

    public static void addCalcDataItems(String qcBatchId, QueryProcessor qp, ActionProcessor ap) throws SapphireException {
        DataSet dsGrp;
        DataSet dsAddDataItem = new DataSet();
        DataSet dsEditDataItem = new DataSet();
        HashMap sequenceMap = new HashMap();
        SafeSQL safeSQL = new SafeSQL();
        DataSet dsBSTDetails = qp.getPreparedSqlDataSet("SELECT DISTINCT qbst.s_qcbatchsampletypeid, qbst.actioncalc, a.objectname FROM s_qcbatchsampletype qbst, action a WHERE qbst.qcbatchid = " + safeSQL.addVar(qcBatchId) + " AND a.actionid = qbst.actioncalc", safeSQL.getValues());
        for (int i = 0; i < dsBSTDetails.getRowCount(); ++i) {
            String calcActionId = dsBSTDetails.getValue(i, "actioncalc", "");
            String bstId = dsBSTDetails.getValue(i, "s_qcbatchsampletypeid", "");
            String actionClass = dsBSTDetails.getValue(i, "objectname");
            try {
                Class<?> c = Class.forName(actionClass);
                Class[] paramtypes = new Class[]{String.class, String.class, String.class, QueryProcessor.class};
                Method meth = c.getMethod("addQCCalcDataItems", paramtypes);
                DataSet dsDataItems = (DataSet)meth.invoke(c.newInstance(), qcBatchId, bstId, calcActionId, qp);
                for (int r = 0; r < dsDataItems.getRowCount(); ++r) {
                    if (dsDataItems.getString(r, "operation", "").equalsIgnoreCase("Add")) {
                        dsAddDataItem.copyRow(dsDataItems, r, 1);
                        continue;
                    }
                    if (!dsDataItems.getString(r, "operation", "").equalsIgnoreCase("Edit")) continue;
                    dsEditDataItem.copyRow(dsDataItems, r, 1);
                }
                continue;
            }
            catch (Exception c) {
                // empty catch block
            }
        }
        DataSet dsSetUserSequence = new DataSet();
        if (dsAddDataItem.getRowCount() > 0) {
            dsAddDataItem.sort("sdcid,keyid1,keyid2,keyid3,paramlistid,paramlistversionid,variantid,dataset,paramid,paramtype");
            ArrayList<DataSet> grpList = dsAddDataItem.getGroupedDataSets("sdcid,keyid1,keyid2,keyid3,paramlistid,paramlistversionid,variantid,dataset,paramid,paramtype");
            DataSet dsExtendDataSet = new DataSet();
            DataSet dsAddReplicate = new DataSet();
            for (int i = 0; i < grpList.size(); ++i) {
                dsGrp = grpList.get(i);
                dsGrp.sort("replicateid");
                DataSet copyDSGrp = dsGrp.copy();
                int copyRow = dsGrp.getRowCount() - 1;
                dsGrp.setNumber(copyRow, "replicateid", dsGrp.getRowCount());
                HashMap<String, Object> findMap = new HashMap<String, Object>();
                findMap.put("sdcid", dsGrp.getString(copyRow, "sdcid"));
                findMap.put("keyid1", dsGrp.getString(copyRow, "keyid1"));
                findMap.put("keyid2", dsGrp.getString(copyRow, "keyid2"));
                findMap.put("keyid3", dsGrp.getString(copyRow, "keyid3"));
                findMap.put("paramlistid", dsGrp.getString(copyRow, "paramlistid"));
                findMap.put("paramlistversionid", dsGrp.getString(copyRow, "paramlistversionid"));
                findMap.put("variantid", dsGrp.getString(copyRow, "variantid"));
                findMap.put("dataset", dsGrp.getBigDecimal(copyRow, "dataset"));
                findMap.put("paramid", dsGrp.getString(copyRow, "paramid"));
                findMap.put("paramtype", dsGrp.getString(copyRow, "paramtype"));
                int findRow = dsEditDataItem.findRow(findMap);
                if (findRow > -1) {
                    dsAddReplicate.copyRow(dsGrp, copyRow, 1);
                } else {
                    dsExtendDataSet.copyRow(dsGrp, copyRow, 1);
                }
                dsSetUserSequence.copyRow(copyDSGrp, -1, 1);
            }
            PropertyList actionProps = new PropertyList();
            if (dsExtendDataSet.getRowCount() > 0) {
                actionProps.setProperty("sdcid", dsExtendDataSet.getString(0, "sdcid"));
                actionProps.setProperty("keyid1", dsExtendDataSet.getColumnValues("keyid1", ";"));
                actionProps.setProperty("keyid2", dsExtendDataSet.getColumnValues("keyid2", ";"));
                actionProps.setProperty("keyid3", dsExtendDataSet.getColumnValues("keyid3", ";"));
                actionProps.setProperty("paramlistid", dsExtendDataSet.getColumnValues("paramlistid", ";"));
                actionProps.setProperty("paramlistversionid", dsExtendDataSet.getColumnValues("paramlistversionid", ";"));
                actionProps.setProperty("variantid", dsExtendDataSet.getColumnValues("variantid", ";"));
                actionProps.setProperty("dataset", dsExtendDataSet.getColumnValues("dataset", ";"));
                actionProps.setProperty("paramid", dsExtendDataSet.getColumnValues("paramid", ";"));
                actionProps.setProperty("paramtype", dsExtendDataSet.getColumnValues("paramtype", ";"));
                actionProps.setProperty("numreplicate", dsExtendDataSet.getColumnValues("replicateid", ";"));
                actionProps.setProperty("displayformat", dsExtendDataSet.getColumnValues("displayformat", ";"));
                String calcRules = dsExtendDataSet.getColumnValues("calcrule", "~");
                calcRules = calcRules.replaceAll(";", "#semicolon#");
                calcRules = calcRules.replaceAll("~", ";");
                actionProps.setProperty("calcrule", calcRules);
                actionProps.setProperty("propsmatch", "Y");
                actionProps.setProperty("paramlistcheck", "N");
                actionProps.setProperty("datatypes", "NC");
                ap.processAction("ExtendDataSet", "1", actionProps);
            }
            if (dsAddReplicate.getRowCount() > 0) {
                actionProps.clear();
                actionProps.setProperty("sdcid", dsAddReplicate.getString(0, "sdcid"));
                actionProps.put("keyid1", dsAddReplicate.getColumnValues("keyid1", ";"));
                actionProps.put("keyid2", dsAddReplicate.getColumnValues("keyid2", ";"));
                actionProps.put("keyid3", dsAddReplicate.getColumnValues("keyid3", ";"));
                actionProps.put("paramlistid", dsAddReplicate.getColumnValues("paramlistid", ";"));
                actionProps.put("paramlistversionid", dsAddReplicate.getColumnValues("paramlistversionid", ";"));
                actionProps.put("variantid", dsAddReplicate.getColumnValues("variantid", ";"));
                actionProps.put("dataset", dsAddReplicate.getColumnValues("dataset", ";"));
                actionProps.put("paramid", dsAddReplicate.getColumnValues("paramid", ";"));
                actionProps.put("paramtype", dsAddReplicate.getColumnValues("paramtype", ";"));
                actionProps.put("numreplicate", dsAddReplicate.getColumnValues("replicateid", ";"));
                actionProps.put("propsmatch", "Y");
                ap.processAction("AddReplicate", "1", actionProps);
            }
        }
        if (dsSetUserSequence.getRowCount() > 0) {
            DataSet finalSequenceDS = new DataSet();
            safeSQL.reset();
            DataSet dsMaxSeq = qp.getPreparedSqlDataSet("select di.sdcid, di.keyid1, di.keyid2, di.keyid3, di.paramlistid, di.paramlistversionid, di.variantid, di.dataset, max(di.usersequence) maxseq from sdidataitem di, sdidata ds where di.sdcid=ds.sdcid and di.keyid1=ds.keyid1 and di.keyid2=ds.keyid2 and di.keyid3=ds.keyid3  and di.paramlistid=ds.paramlistid and di.paramlistversionid=ds.paramlistversionid and di.variantid=ds.variantid and di.dataset=ds.dataset  and ds.s_qcbatchid = " + safeSQL.addVar(qcBatchId) + " group by di.sdcid, di.keyid1, di.keyid2, di.keyid3, di.paramlistid, di.paramlistversionid, di.variantid, di.dataset", safeSQL.getValues());
            dsSetUserSequence.sort("sdcid,keyid1,keyid2,keyid3,paramlistid,paramlistversionid,variantid,dataset");
            ArrayList<DataSet> grpList = dsSetUserSequence.getGroupedDataSets("sdcid,keyid1,keyid2,keyid3,paramlistid,paramlistversionid,variantid,dataset");
            for (int lst = 0; lst < grpList.size(); ++lst) {
                int c;
                dsGrp = grpList.get(lst);
                HashMap<String, Object> findmap = new HashMap<String, Object>();
                findmap.put("sdcid", dsGrp.getString(0, "sdcid"));
                findmap.put("keyid1", dsGrp.getString(0, "keyid1"));
                findmap.put("keyid2", dsGrp.getString(0, "keyid2"));
                findmap.put("keyid3", dsGrp.getString(0, "keyid3"));
                findmap.put("paramlistid", dsGrp.getString(0, "paramlistid"));
                findmap.put("paramlistversionid", dsGrp.getString(0, "paramlistversionid"));
                findmap.put("variantid", dsGrp.getString(0, "variantid"));
                findmap.put("dataset", dsGrp.getBigDecimal(0, "dataset"));
                int row = dsMaxSeq.findRow(findmap);
                int maxSeq = 0;
                if (row > -1) {
                    maxSeq = dsMaxSeq.getInt(row, "maxseq");
                }
                dsGrp.sort("paramtype,usersequence");
                findmap.clear();
                findmap.put("paramtype", "BlankCorrected");
                DataSet blankCorrectDS = dsGrp.getFilteredDataSet(findmap);
                blankCorrectDS.sort("usersequence");
                for (c = 0; c < blankCorrectDS.getRowCount(); ++c) {
                    blankCorrectDS.setNumber(c, "usersequence", ++maxSeq);
                }
                finalSequenceDS.copyRow(blankCorrectDS, -1, 1);
                for (c = 0; c < dsGrp.getRowCount(); ++c) {
                    if ("BlankCorrected".equalsIgnoreCase(dsGrp.getString(c, "paramtype"))) continue;
                    dsGrp.setNumber(c, "usersequence", ++maxSeq);
                    finalSequenceDS.copyRow(dsGrp, c, 1);
                }
            }
            dsEditDataItem.copyRow(finalSequenceDS, -1, 1);
        }
        if (dsEditDataItem.getRowCount() > 0) {
            PropertyList actionProps = new PropertyList();
            actionProps.setProperty("sdcid", dsEditDataItem.getString(0, "sdcid"));
            actionProps.setProperty("keyid1", dsEditDataItem.getColumnValues("keyid1", ";"));
            actionProps.setProperty("keyid2", dsEditDataItem.getColumnValues("keyid2", ";"));
            actionProps.setProperty("keyid3", dsEditDataItem.getColumnValues("keyid3", ";"));
            actionProps.setProperty("paramlistid", dsEditDataItem.getColumnValues("paramlistid", ";"));
            actionProps.setProperty("paramlistversionid", dsEditDataItem.getColumnValues("paramlistversionid", ";"));
            actionProps.setProperty("variantid", dsEditDataItem.getColumnValues("variantid", ";"));
            actionProps.setProperty("dataset", dsEditDataItem.getColumnValues("dataset", ";"));
            actionProps.setProperty("paramid", dsEditDataItem.getColumnValues("paramid", ";"));
            actionProps.setProperty("paramtype", dsEditDataItem.getColumnValues("paramtype", ";"));
            actionProps.setProperty("replicateid", dsEditDataItem.getColumnValues("replicateid", ";"));
            actionProps.setProperty("usersequence", dsEditDataItem.getColumnValues("usersequence", ";"));
            actionProps.setProperty("displayformat", dsEditDataItem.getColumnValues("displayformat", ";"));
            String calcRules = dsEditDataItem.getColumnValues("calcrule", "~");
            calcRules = calcRules.replaceAll(";", "#semicolon#");
            calcRules = calcRules.replaceAll("~", ";");
            actionProps.setProperty("calcrule", calcRules);
            actionProps.setProperty("propsmatch", "Y");
            ap.processAction("EditDataItem", "1", actionProps);
        }
    }

    public static boolean isQCBItemCalcRuleDefined(String qcBatchId, String qcBatchItemId, String calcActionName, QueryProcessor qp, ConnectionProcessor cp) {
        try {
            DataSet ds = qp.getPreparedSqlDataSet("SELECT objectname FROM action WHERE actionid = ?", (Object[])new String[]{calcActionName});
            Class<?> c = Class.forName(ds.getValue(0, "objectname"));
            Class[] paramtypes = new Class[]{String.class, String.class, QueryProcessor.class, ConnectionProcessor.class};
            Method meth = c.getMethod("isCalcRuleAdded", paramtypes);
            Boolean calcRulePresent = (Boolean)meth.invoke(c.newInstance(), qcBatchId, qcBatchItemId, qp, cp);
            return calcRulePresent;
        }
        catch (Exception e) {
            Trace.logInfo("ProcessQCBatch: ", e.getMessage() + " Project specific calculation action may not have the method  addQCCalcDataItems.");
            return false;
        }
    }

    public static DataSet getDataItemsWithCalcRuleDefined(String qcBatchId, String batchItemIds, String paramType, QueryProcessor qp, boolean isOra) {
        StringBuffer sql = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        if (batchItemIds.length() > 0 && batchItemIds.startsWith("'") && batchItemIds.endsWith("'")) {
            batchItemIds = batchItemIds.substring(1, batchItemIds.length() - 1);
        }
        sql.append(" SELECT 1 FROM SDIDATAITEM DI, SDIDATA DS WHERE DI.SDCID = DS.SDCID AND DI.KEYID1 = DS.KEYID1 ").append(" AND DI.KEYID2 = DS.KEYID2 AND DI.KEYID3 = DS.KEYID3 AND DI.PARAMLISTID = DS.PARAMLISTID ").append(" AND DI.PARAMLISTVERSIONID = DS.PARAMLISTVERSIONID AND DI.VARIANTID = DS.VARIANTID ").append(" AND DI.DATASET = DS.DATASET AND DS.S_QCBATCHID = " + safeSQL.addVar(qcBatchId)).append(" AND DS.S_QCBATCHITEMID IN ( " + safeSQL.addIn(batchItemIds) + " ) ").append(" AND  DI.PARAMTYPE = " + safeSQL.addVar(paramType) + " AND ").append(isOra ? " nvl( length( DI.CALCRULE ), 0 ) > 0 " : " coalesce( len( DI.CALCRULE), 0) > 0");
        DataSet ds = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        return ds;
    }

    public static boolean matchSDIWorkItemItem(ConfigurationProcessor cp) throws SapphireException {
        PropertyList policy = cp.getPolicy("AQCPolicy", "Sapphire Custom");
        if (policy != null) {
            return "Y".equalsIgnoreCase(policy.getPropertyListNotNull("datasetselectionmode").getProperty("honorsdiworkitemitem", "Y"));
        }
        return true;
    }

    public static boolean copySecurityDeptToSample(ConfigurationProcessor cp) throws SapphireException {
        PropertyList policy = cp.getPolicy("AQCPolicy", "Sapphire Custom");
        if (policy != null) {
            return "Y".equalsIgnoreCase(policy.getProperty("copyqcbatchsecuritydepartmenttoqcsamples", "N"));
        }
        return true;
    }

    public static boolean linkDataSetAddedByReflexRuleToQCBatch(ConfigurationProcessor cp) throws SapphireException {
        PropertyList policy = cp.getPolicy("AQCPolicy", "Sapphire Custom");
        if (policy != null) {
            return "Y".equalsIgnoreCase(policy.getProperty("linkdatasetaddedbyreflexruletoqcbatch", "Y"));
        }
        return false;
    }

    public static String getBatchItemCalcDataItemsSQL() {
        StringBuffer sdidataitemSQL = new StringBuffer();
        sdidataitemSQL.append("SELECT DISTINCT DI.SDCID, DI.KEYID1, DI.KEYID2, DI.KEYID3, DI.PARAMLISTID, DI.PARAMLISTVERSIONID,").append(" DI.VARIANTID, DI.DATASET, DI.PARAMID, DI.PARAMTYPE, DI.REPLICATEID, DI.CALCRULE, DI.ENTEREDTEXT FROM SDIDATAITEM DI, SDIDATA DS ").append(" WHERE DI.SDCID = DS.SDCID AND DI.KEYID1 = DS.KEYID1  AND DI.KEYID2 = DS.KEYID2 AND DI.KEYID3 = DS.KEYID3 AND ").append("DI.PARAMLISTID = DS.PARAMLISTID AND DI.PARAMLISTVERSIONID = DS.PARAMLISTVERSIONID AND DI.VARIANTID = DS.VARIANTID ").append(" AND DI.DATASET = DS.DATASET AND DI.DATATYPES = 'NC' AND DS.SDCID = ? AND DS.S_QCBATCHID = ?").append(" AND DS.S_QCBATCHITEMID = ?");
        return sdidataitemSQL.toString();
    }

    public static String getLinkedQCBatchIds(String sdcId, String keyIds, String paramListIds, String paramListVersionIds, String variantIds, String dataSets, QueryProcessor qp) throws SapphireException {
        DataSet dsProcessed = new DataSet();
        dsProcessed.addColumnValues("keyid1", 0, keyIds, ";");
        dsProcessed.addColumnValues("paramlistid", 0, paramListIds, ";");
        dsProcessed.addColumnValues("paramlistversionid", 0, paramListVersionIds, ";");
        dsProcessed.addColumnValues("variantid", 0, variantIds, ";");
        dsProcessed.addColumnValues("dataset", 1, dataSets, ";");
        DAMProcessor dam = new DAMProcessor(qp.getConnectionid());
        String rsetId = dam.createRSet(sdcId, keyIds, null, null);
        StringBuffer sql = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        sql.append("SELECT S.SDCID, S.KEYID1, S.KEYID2, S.KEYID3, S.PARAMLISTID, S.PARAMLISTVERSIONID, S.VARIANTID, S.DATASET, S.S_QCBATCHID ");
        sql.append(" FROM SDIDATA S, RSETITEMS R");
        sql.append(" WHERE S.SDCID = R.SDCID  AND S.KEYID1 = R.KEYID1 AND R.RSETID = " + safeSQL.addVar(rsetId) + " AND R.SDCID = " + safeSQL.addVar(sdcId));
        if (!"QCBatch".equals(sdcId)) {
            sql.append(" AND S.S_QCBATCHID IS NOT NULL");
        }
        DataSet ds = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        dam.clearRSet(rsetId);
        ArrayList<String> qcBatches = new ArrayList<String>();
        StringBuffer qcBatchIds = new StringBuffer();
        HashMap<String, Object> findMap = new HashMap<String, Object>();
        for (int i = 0; i < dsProcessed.getRowCount(); ++i) {
            String qcBatchId;
            findMap.put("sdcid", sdcId);
            findMap.put("keyid1", dsProcessed.getString(i, "keyid1"));
            findMap.put("paramlistid", dsProcessed.getString(i, "paramlistid"));
            findMap.put("paramlistversionid", dsProcessed.getString(i, "paramlistversionid"));
            findMap.put("variantid", dsProcessed.getString(i, "variantid"));
            findMap.put("dataset", dsProcessed.getBigDecimal(i, "dataset"));
            int findRow = ds.findRow(findMap);
            if (findRow <= -1) continue;
            String string = qcBatchId = "QCBatch".equals(sdcId) ? ds.getString(findRow, "keyid1", "") : ds.getString(findRow, "s_qcbatchid", "");
            if (qcBatches.contains(qcBatchId)) continue;
            qcBatches.add(qcBatchId);
            qcBatchIds.append(";").append(qcBatchId);
        }
        if (qcBatchIds.length() > 0) {
            return qcBatchIds.substring(1);
        }
        return "";
    }

    public static void updateReagent(QueryProcessor qp, DBAccess database, DataSet dsQCBatchitem) throws SapphireException {
        if (dsQCBatchitem != null && dsQCBatchitem.size() > 0) {
            for (int i = 0; i < dsQCBatchitem.size(); ++i) {
                String qcbatchid = dsQCBatchitem.getString(i, fieldName_QCBATCHID, "");
                DataSet dsQCBatchReagent = QCUtil.getQCBatchReagent(qp, qcbatchid);
                if (dsQCBatchReagent == null || dsQCBatchReagent.size() <= 0) continue;
                for (int r = 0; r < dsQCBatchReagent.size(); ++r) {
                    String qcbatchreagentid = dsQCBatchReagent.getString(r, fieldName_QCBATCHREAGENTID, "");
                    String reagentlotid = dsQCBatchReagent.getString(r, fieldName_REAGENTLOTID, "");
                    String trackitemid = dsQCBatchReagent.getString(r, fieldName_TRACKITEMID, "");
                    if (StringUtil.getLen(reagentlotid) <= 0L || StringUtil.getLen(trackitemid) <= 0L) continue;
                    QCUtil.syncSDIDataRelation(qp, database, qcbatchreagentid, reagentlotid, trackitemid);
                }
            }
        }
    }

    private static void syncSDIDataRelation(QueryProcessor qp, DBAccess database, String qcbatchreagentid, String reagentlotid, String trackitemid) throws SapphireException {
        StringBuffer sqlSDIdatarelation = new StringBuffer();
        sqlSDIdatarelation.setLength(0);
        sqlSDIdatarelation.append("select sdr.relationid ");
        sqlSDIdatarelation.append(" from sdidata sd,sdidatarelation sdr,s_qcbatchreagent qcbr");
        sqlSDIdatarelation.append(" where sd.sdcid =sdr.sdcid ");
        sqlSDIdatarelation.append(" and sd.keyid1 = sdr.keyid1 ");
        sqlSDIdatarelation.append(" and sd.keyid2 = sdr.keyid2 ");
        sqlSDIdatarelation.append(" and sd.keyid3 = sdr.keyid3 ");
        sqlSDIdatarelation.append(" and sd.paramlistid = sdr.paramlistid");
        sqlSDIdatarelation.append(" and sd.paramlistversionid = sdr.paramlistversionid");
        sqlSDIdatarelation.append(" and sd.variantid = sdr.variantid");
        sqlSDIdatarelation.append(" and sd.dataset = sdr.dataset");
        sqlSDIdatarelation.append(" and sdr.relationfunction = 'Reagent'");
        sqlSDIdatarelation.append(" and sd.s_qcbatchid = qcbr.qcbatchid");
        sqlSDIdatarelation.append(" and sdr.relationtype = qcbr.reagenttypeid");
        sqlSDIdatarelation.append(" and qcbr.s_qcbatchreagentid = '").append(qcbatchreagentid).append("'");
        DataSet dslSDIdatarelation = qp.getSqlDataSet(sqlSDIdatarelation.toString());
        if (dslSDIdatarelation != null && dslSDIdatarelation.size() > 0) {
            sqlSDIdatarelation.setLength(0);
            sqlSDIdatarelation.append("update sdidatarelation set tosdcid = 'LV_ReagentLot'");
            sqlSDIdatarelation.append(",tokeyid1 = '").append(reagentlotid).append("'");
            sqlSDIdatarelation.append(",refsdcid = 'TrackItemSDC'");
            sqlSDIdatarelation.append(",refkeyid1 = '").append(trackitemid).append("'");
            sqlSDIdatarelation.append(" where relationid in ('").append(dslSDIdatarelation.getColumnValues("relationid", "','")).append("')");
            database.executeSQL(sqlSDIdatarelation.toString());
        }
    }

    private static DataSet getQCBatchReagent(QueryProcessor qp, String qcbatchid) {
        return qp.getPreparedSqlDataSet("select * FROM s_qcbatchreagent WHERE qcbatchid = ?", (Object[])new String[]{qcbatchid});
    }

    public static void syncQCBatchReagentInstrument(DataSet qcBatchDS, ActionProcessor ap) throws SapphireException {
        PropertyList actionProps = new PropertyList();
        actionProps.setProperty("qcbatchid", qcBatchDS.getColumnValues("s_qcbatchid", ";"));
        actionProps.setProperty("keyid1", qcBatchDS.getColumnValues("keyid1", ";"));
        actionProps.setProperty("paramlistid", qcBatchDS.getColumnValues("paramlistid", ";"));
        actionProps.setProperty("paramlistversionid", qcBatchDS.getColumnValues("paramlistversionid", ";"));
        actionProps.setProperty("variantid", qcBatchDS.getColumnValues("variantid", ";"));
        actionProps.setProperty("dataset", qcBatchDS.getColumnValues("dataset", ";"));
        ap.processActionClass(QCBatchReagentSync.class.getName(), actionProps);
    }

    public static void postAddDataSetSyncQCBatchReagentInstrument(String datasetXml, ActionProcessor ap, QueryProcessor qp) throws SapphireException {
        DataSet dsInstances = new DataSet(datasetXml);
        DataSet qcBatchDS = new DataSet();
        for (int d = 0; d < dsInstances.getRowCount(); ++d) {
            String sdidataid = dsInstances.getValue(d, "sdidataid");
            DataSet ds = qp.getPreparedSqlDataSet("select * from sdidata where sdidataid = ? and s_qcbatchid is not null", (Object[])new String[]{sdidataid});
            if (ds.getRowCount() <= 0) continue;
            qcBatchDS.copyRow(ds, -1, 1);
        }
        if (qcBatchDS.getRowCount() > 0) {
            PropertyList actionProps = new PropertyList();
            actionProps.setProperty("qcbatchid", qcBatchDS.getColumnValues("s_qcbatchid", ";"));
            actionProps.setProperty("keyid1", qcBatchDS.getColumnValues("keyid1", ";"));
            actionProps.setProperty("paramlistid", qcBatchDS.getColumnValues("paramlistid", ";"));
            actionProps.setProperty("paramlistversionid", qcBatchDS.getColumnValues("paramlistversionid", ";"));
            actionProps.setProperty("variantid", qcBatchDS.getColumnValues("variantid", ";"));
            actionProps.setProperty("dataset", qcBatchDS.getColumnValues("dataset", ";"));
            ap.processActionClass(QCBatchReagentSync.class.getName(), actionProps);
        }
    }

    public static void setOriginalReagent(DataSet primary, DataSet oldPrimary) {
        if (QCUtil.isOriginalReagentTypeBlank(oldPrimary)) {
            for (int i = 0; i < primary.getRowCount(); ++i) {
                String originalreagenttypeid = oldPrimary.getString(i, "originalreagenttypeid", "");
                String originalreagenttypeversionid = oldPrimary.getString(i, "originalreagenttypeversionid", "");
                if (originalreagenttypeid.length() == 0) {
                    originalreagenttypeid = oldPrimary.getString(i, "reagenttypeid", "");
                    originalreagenttypeversionid = oldPrimary.getString(i, "reagenttypeversionid", "");
                }
                primary.setString(i, "originalreagenttypeid", originalreagenttypeid);
                primary.setString(i, "originalreagenttypeversionid", originalreagenttypeversionid);
            }
        }
    }

    public static boolean isOriginalReagentTypeBlank(DataSet oldPrimary) {
        boolean flag = false;
        for (int i = 0; i < oldPrimary.getRowCount(); ++i) {
            String originalreagenttypeid = oldPrimary.getString(i, "originalreagenttypeid", "");
            if (originalreagenttypeid.length() != 0) continue;
            flag = true;
            break;
        }
        return flag;
    }
}

