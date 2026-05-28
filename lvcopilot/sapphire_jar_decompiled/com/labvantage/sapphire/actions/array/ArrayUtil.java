/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.array;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.services.ConnectionInfo;
import com.labvantage.sapphire.util.UnitsUtil;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.DAMProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.util.DataSet;
import sapphire.util.M18NUtil;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class ArrayUtil {
    static final String LABVANTAGE_CVS_ID = "1: 1.1 $";

    public static void updateArrayItemsInZone(String arrayzoneid, String zoneitemvolumes, String zoneitemvolumeunits, QueryProcessor queryProcessor, ActionProcessor actionProcessor) throws ActionException {
        String sql = "SELECT arrayitemid FROM arrayitemarrayzone WHERE arrayzoneid=? ";
        SafeSQL safeSQL = new SafeSQL();
        safeSQL.addVar(arrayzoneid);
        String arrayitemids = queryProcessor.getPreparedSqlDataSet(sql, safeSQL.getValues()).getColumnValues("arrayitemid", ";");
        if (arrayitemids.length() > 0) {
            HashMap<String, String> arrayitemmap = new HashMap<String, String>();
            arrayitemmap.put("arrayitemid", arrayitemids);
            arrayitemmap.put("totalvol", zoneitemvolumes);
            arrayitemmap.put("totalvolunits", zoneitemvolumeunits);
            PropertyList arrayitemprops = new PropertyList(arrayitemmap);
            actionProcessor.processAction("EditArrayItem", "1", arrayitemprops);
        }
    }

    public static void updateArrayItem(String arrayid, String arrayitemid, String arrayitemidVols, String quantityUnits, ActionProcessor actionProcessor) throws ActionException {
        String[] arrayitemidVol = StringUtil.split(arrayitemidVols, ";");
        String[] arrayitemids = StringUtil.split(arrayitemid, ";");
        String[] quantityUnit = StringUtil.split(quantityUnits, ";");
        String totalquan = "";
        String totalquantityUnits = "";
        for (int i = 0; i < arrayitemids.length; ++i) {
            totalquan = totalquan + ";" + arrayitemidVol[i];
            totalquantityUnits = totalquantityUnits + ";" + quantityUnit[i];
        }
        if (arrayitemids.length > 0) {
            totalquan = totalquan.substring(1);
            totalquantityUnits = totalquantityUnits.substring(1);
            HashMap<String, String> arrayitemmap = new HashMap<String, String>();
            arrayitemmap.put("arrayitemid", arrayitemid);
            arrayitemmap.put("totalvol", totalquan);
            arrayitemmap.put("totalvolunits", totalquantityUnits);
            PropertyList arrayitemprops = new PropertyList(arrayitemmap);
            actionProcessor.processAction("EditArrayItem", "1", arrayitemprops);
        }
    }

    public static String getArrayMethodCurrentVersion(QueryProcessor qp, String arrayMethodId) {
        String versionStatus;
        int i;
        String sql = "SELECT arraymethodversionid, versionstatus FROM arraymethod WHERE arraymethodid=? order by arraymethodversionid desc";
        SafeSQL safeSQL = new SafeSQL();
        safeSQL.addVar(arrayMethodId);
        DataSet ds = qp.getPreparedSqlDataSet(sql, safeSQL.getValues());
        for (i = 0; i < ds.getRowCount(); ++i) {
            versionStatus = ds.getString(i, "versionstatus");
            if (!versionStatus.equals("C")) continue;
            return ds.getString(i, "arraymethodversionid");
        }
        for (i = 0; i < ds.getRowCount(); ++i) {
            versionStatus = ds.getString(i, "versionstatus");
            if (!versionStatus.equals("P")) continue;
            return ds.getString(i, "arraymethodversionid");
        }
        return "";
    }

    public static DataSet getDefaultsFromArrayMethod(QueryProcessor queryProcessor, String arraymethodid, String arraymethodversionid, String level) {
        String sql = "select sampletypeid, createchildsampleflag, zone, contentitem, volumetarget, volumetargetunits, concentrationtarget, concentrationtargetunits, amounttarget, amounttargetunits from arraymethod, arraymethodcontent where arraymethodcontent.arraymethodid = arraymethod.arraymethodid and arraymethodcontent.arraymethodversionid = arraymethod.arraymethodversionid and arraymethod.arraymethodid = ? and arraymethod.arraymethodversionid = ? and levelflag = ?";
        SafeSQL safeSQL = new SafeSQL();
        safeSQL.addVar(arraymethodid);
        safeSQL.addVar(arraymethodversionid);
        safeSQL.addVar("" + level.toUpperCase().charAt(0));
        return queryProcessor.getPreparedSqlDataSet(sql, safeSQL.getValues());
    }

    public static DataSet getDefaultsFromArrayMethod(QueryProcessor queryProcessor, String arraymethodid, String arraymethodversionid, String zone, String contentitem, String level) {
        String sql = "SELECT sampletypeid, createchildsampleflag, zone, contentitem, volumetarget, volumetargetunits, concentrationtarget, concentrationtargetunits, amounttarget, amounttargetunits  FROM arraymethod, arraymethodcontent  WHERE arraymethod.arraymethodid = arraymethodcontent.arraymethodid  AND arraymethod.arraymethodversionid = arraymethodcontent.arraymethodversionid  AND arraymethod.arraymethodid = ? AND arraymethod.arraymethodversionid = ?  AND zone = ? AND contentitem = ? AND levelflag = ?  ";
        SafeSQL safeSQL = new SafeSQL();
        safeSQL.addVar(arraymethodid);
        safeSQL.addVar(arraymethodversionid);
        safeSQL.addVar(zone);
        safeSQL.addVar(contentitem);
        safeSQL.addVar("" + level.toUpperCase().charAt(0));
        DataSet ds = queryProcessor.getPreparedSqlDataSet(sql, safeSQL.getValues());
        if (ds.size() == 0) {
            sql = "SELECT sampletypeid, createchildsampleflag, zone, contentitem, volumetarget, volumetargetunits, concentrationtarget, concentrationtargetunits, amounttarget, amounttargetunits  FROM arraymethod, arraymethodcontent  WHERE arraymethod.arraymethodid = arraymethodcontent.arraymethodid  AND arraymethod.arraymethodversionid = arraymethodcontent.arraymethodversionid  AND arraymethod.arraymethodid = ? AND arraymethod.arraymethodversionid = ?  AND arraymethodcontent.zone = ? AND arraymethodcontent.levelflag = ?  ";
            safeSQL.reset();
            safeSQL.addVar(arraymethodid);
            safeSQL.addVar(arraymethodversionid);
            safeSQL.addVar(zone);
            safeSQL.addVar("" + level.toUpperCase().charAt(0));
            ds = queryProcessor.getPreparedSqlDataSet(sql, safeSQL.getValues());
        }
        return ds;
    }

    public static String getArrayLayoutCurrentVersion(QueryProcessor qp, String arrayLayoutId) {
        String versionStatus;
        int i;
        String sql = "SELECT arraylayoutversionid, versionstatus FROM arraylayout WHERE arraylayoutid=? order by arraylayoutversionid desc";
        SafeSQL safeSQL = new SafeSQL();
        safeSQL.addVar(arrayLayoutId);
        DataSet ds = qp.getPreparedSqlDataSet(sql, safeSQL.getValues());
        for (i = 0; i < ds.getRowCount(); ++i) {
            versionStatus = ds.getString(i, "versionstatus");
            if (!versionStatus.equals("C")) continue;
            return ds.getString(i, "arraylayoutversionid");
        }
        for (i = 0; i < ds.getRowCount(); ++i) {
            versionStatus = ds.getString(i, "versionstatus");
            if (!versionStatus.equals("P")) continue;
            return ds.getString(i, "arraylayoutversionid");
        }
        return "";
    }

    public static DataSet getArrayTypeDetails(QueryProcessor qp, String arrayTypeId, String arrayTypeVersionId) {
        String sql = "SELECT * FROM arraytype WHERE arraytypeid = ? and arraytypeversionid = ?";
        SafeSQL safeSQL = new SafeSQL();
        safeSQL.addVar(arrayTypeId);
        safeSQL.addVar(arrayTypeVersionId);
        return qp.getPreparedSqlDataSet(sql, safeSQL.getValues());
    }

    public static String getArrayTypeCurrentVersion(QueryProcessor qp, String arrayTypeId) {
        String versionStatus;
        int i;
        String sql = "SELECT arraytypeversionid, versionstatus FROM arraytype WHERE arraytypeid=? order by arraytypeversionid desc";
        SafeSQL safeSQL = new SafeSQL();
        safeSQL.addVar(arrayTypeId);
        DataSet ds = qp.getPreparedSqlDataSet(sql, safeSQL.getValues());
        for (i = 0; i < ds.getRowCount(); ++i) {
            versionStatus = ds.getString(i, "versionstatus");
            if (!versionStatus.equals("C")) continue;
            return ds.getString(i, "arraytypeversionid");
        }
        for (i = 0; i < ds.getRowCount(); ++i) {
            versionStatus = ds.getString(i, "versionstatus");
            if (!versionStatus.equals("P")) continue;
            return ds.getString(i, "arraytypeversionid");
        }
        return "";
    }

    public static DataSet getArrayTypeDimensions(QueryProcessor qp, String arrayTypeId, String arrayTypeVersionId) {
        return com.labvantage.sapphire.util.array.ArrayUtil.getArrayTypeDimensions(qp, arrayTypeId, arrayTypeVersionId);
    }

    public static final String getLastArrayMethodItem(QueryProcessor queryProcessor, String arrayid) {
        return ArrayUtil.getLastArrayMethodItem(queryProcessor, arrayid, "");
    }

    public static final String getArrayType(QueryProcessor queryProcessor, String arrayid) {
        String sql = "SELECT arraytypeid, arraytypeversionid FROM array where arrayid = ?";
        SafeSQL safeSQL = new SafeSQL();
        safeSQL.addVar(arrayid);
        DataSet ds = queryProcessor.getPreparedSqlDataSet(sql, safeSQL.getValues());
        if (ds != null && ds.getRowCount() > 0) {
            return ds.getValue(0, "arraytypeid", "") + "|" + ds.getValue(0, "arraytypeversionid", "");
        }
        return "";
    }

    public static String getCurrentArrayMethodItemDetails(QueryProcessor queryProcessor, String arrayid, String arraymethodid, String arraymethodinstance) {
        if (arraymethodid == null || arraymethodid.length() == 0 || arraymethodinstance == null || arraymethodinstance.length() == 0) {
            return ArrayUtil.getLastArrayMethodItem(queryProcessor, arrayid, arraymethodid);
        }
        String sql = "SELECT arraymethodid, arraymethodversionid, arraymethodinstance, arraymethoditemstatus, promoteresultsflag from arrayarraymethoditem where arrayid = ? and arraymethodid = ? and arraymethodinstance = ? ORDER BY usersequence desc";
        SafeSQL safeSQL = new SafeSQL();
        safeSQL.addVar(arrayid);
        safeSQL.addVar(arraymethodid);
        safeSQL.addVar(arraymethodinstance);
        DataSet ds = queryProcessor.getPreparedSqlDataSet(sql, safeSQL.getValues());
        if (ds != null && ds.getRowCount() > 0) {
            return ds.getString(0, "arraymethodid") + "|" + ds.getValue(0, "arraymethodinstance") + "|" + ds.getString(0, "arraymethoditemstatus") + "|" + ds.getString(0, "promoteresultsflag") + "|" + ds.getString(0, "arraymethodversionid");
        }
        return "";
    }

    public static String getLastArrayMethodItem(QueryProcessor queryProcessor, String arrayid, String arraymethodid) {
        if (arraymethodid == null || arraymethodid.length() == 0) {
            String sql = "SELECT arraymethodid, arraymethodversionid, arraymethodinstance, arraymethoditemstatus, promoteresultsflag from arrayarraymethoditem where arrayid = ? ORDER BY usersequence desc";
            DataSet ds = queryProcessor.getPreparedSqlDataSet(sql, new Object[]{arrayid});
            if (ds != null && ds.getRowCount() > 0) {
                return ds.getString(0, "arraymethodid") + "|" + ds.getValue(0, "arraymethodinstance") + "|" + ds.getString(0, "arraymethoditemstatus") + "|" + ds.getString(0, "promoteresultsflag") + "|" + ds.getString(0, "arraymethodversionid");
            }
        } else {
            String sql = "SELECT arraymethodid, arraymethodversionid, arraymethodinstance, arraymethoditemstatus, promoteresultsflag from arrayarraymethoditem where arrayid = ? and arraymethodid = ? ORDER BY usersequence desc";
            DataSet ds = queryProcessor.getPreparedSqlDataSet(sql, new Object[]{arrayid, arraymethodid});
            if (ds != null && ds.getRowCount() > 0) {
                return ds.getString(0, "arraymethodid") + "|" + ds.getValue(0, "arraymethodinstance") + "|" + ds.getString(0, "arraymethoditemstatus") + "|" + ds.getString(0, "promoteresultsflag") + "|" + ds.getString(0, "arraymethodversionid");
            }
        }
        return "";
    }

    public static String getPreviousArrayMethodItem(QueryProcessor queryProcessor, String arrayid) {
        String sql = "SELECT arraymethodid, arraymethodversionid, arraymethodinstance, arraymethoditemstatus, promoteresultsflag from arrayarraymethoditem where arrayid = ? ORDER BY usersequence desc";
        DataSet ds = queryProcessor.getPreparedSqlDataSet(sql, new Object[]{arrayid});
        if (ds != null && ds.getRowCount() > 0) {
            if (ds.getRowCount() == 1) {
                return "";
            }
            return ds.getString(1, "arraymethodid") + "|" + ds.getValue(1, "arraymethodinstance") + "|" + ds.getString(1, "arraymethoditemstatus") + "|" + ds.getString(1, "promoteresultsflag") + "|" + ds.getString(1, "arraymethodversionid");
        }
        return "";
    }

    public static String getArrayStatus(QueryProcessor qp, String arrayid) throws SapphireException {
        return com.labvantage.sapphire.util.array.ArrayUtil.getArrayStatus(qp, arrayid);
    }

    public static boolean validateArrayOperation(QueryProcessor queryProcessor, String arrayid, String operation) throws SapphireException {
        String arrayStatus = ArrayUtil.getArrayStatus(queryProcessor, arrayid);
        String ami = ArrayUtil.getCurrentArrayMethodItemDetails(queryProcessor, arrayid, "", "");
        if (ami != null && ami.length() > 0) {
            String[] tokens = StringUtil.split(ami, "|");
            return ArrayUtil.validateStatus(arrayStatus, tokens[2], operation, tokens[3]);
        }
        return ArrayUtil.validateStatus(arrayStatus, "Initial", operation, "");
    }

    public static boolean validateStatus(String arrayStatus, String arrayMethodItemStatus, String operation, String promoteresultsflag) {
        Trace.logDebug("Validation operation:" + operation + " Status is:" + arrayMethodItemStatus);
        if (arrayStatus.equals("Disposed")) {
            return false;
        }
        if (arrayMethodItemStatus.equals("Initial")) {
            return operation.equals("ApplyArrayMethod") || operation.equals("LoadArray") || operation.equals("LoadArrayZone") || operation.equals("CancelArray") || operation.equals("SendToInstrument") || operation.equals("ArrayDataEntry");
        }
        if (arrayMethodItemStatus.equals("Loaded")) {
            return operation.equals("ApplyArrayMethod") || operation.equals("LoadArray") || operation.equals("LoadArrayZone") || operation.equals("CancelArray") || operation.equals("SendToInstrument") || operation.equals("ArrayDataEntry") || operation.equals("TransferArray");
        }
        if (arrayMethodItemStatus.equals("SentToInstrument")) {
            return operation.equals("ApplyArrayMethod") || operation.equals("CancelArray") || operation.equals("SendToInstrument") || operation.equals("ArrayDataEntry") || operation.equals("TransferArray");
        }
        if (arrayMethodItemStatus.equals("DataEntered")) {
            if (operation.equals("CancelArray") || operation.equals("ArrayDataEntry") || operation.equals("ReviewArray") || operation.equals("TransferArray")) {
                return true;
            }
            return operation.equals("ApplyArrayMethod") || operation.equals("RemeasureArray");
        }
        if (arrayMethodItemStatus.equals("Completed")) {
            if (operation.equals("ArrayDataEntry") || operation.equals("ApplyArrayMethod") || operation.equals("RemeasureArray") || operation.equals("ReviewArray")) {
                return true;
            }
        } else {
            if (arrayMethodItemStatus.equals("Cancelled")) {
                return operation.equals("ApplyArrayMethod");
            }
            if (arrayMethodItemStatus.equals("Reviewed")) {
                return operation.equals("ReviewArray") || operation.equals("ApplyArrayMethod") || operation.equals("RemeasureArray") || operation.equals("TransferArray");
            }
        }
        return false;
    }

    public static double getDilutentVolume(ConnectionInfo connectionInfo, M18NUtil m18NUtil, String sourceconc, String sourceconcunits, double sourcevolumetodecr, String targetconc, String targetconcunits, double targetvolume) throws SapphireException {
        try {
            BigDecimal conv;
            if (targetconcunits == null) {
                targetconcunits = "";
            }
            if (sourceconcunits == null || sourceconcunits.length() == 0) {
                sourceconcunits = targetconcunits;
            }
            if (!sourceconcunits.equals(targetconcunits) && OpalUtil.isNotEmpty(sourceconc) && (conv = UnitsUtil.basicUnitConv(connectionInfo.getDatabaseId(), m18NUtil.parseBigDecimal(sourceconc), sourceconcunits, targetconcunits)) != null) {
                sourceconc = m18NUtil.format(conv);
            }
            double dilutentvolume = 0.0;
            if (sourceconc == null || sourceconc.length() == 0) {
                return -1.0;
            }
            if (sourceconc != null && targetconc != null && sourceconc.length() > 0 && targetconc.length() > 0) {
                double sourceconcN;
                double targetconcN = m18NUtil.parseBigDecimal(targetconc).doubleValue();
                dilutentvolume = targetconcN < (sourceconcN = m18NUtil.parseBigDecimal(sourceconc).doubleValue()) ? targetvolume - sourcevolumetodecr : targetvolume - targetvolume * targetconcN / sourceconcN;
            }
            return dilutentvolume;
        }
        catch (SapphireException e) {
            throw new SapphireException("Cannot determine diluent volume." + e.getMessage());
        }
    }

    public static DataSet processChildSamples(String connectionid, String createChild, String childSampleType, DataSet arrayitemcontentDS, String loadingDirection, int rowCount, int colCount) throws SapphireException {
        block8: {
            block7: {
                if (!"Y".equalsIgnoreCase(createChild)) break block7;
                ArrayList<String> parentSampleIdList = new ArrayList<String>();
                ArrayList<Integer> childCount = new ArrayList<Integer>();
                for (int contentnum = 0; contentnum < arrayitemcontentDS.getRowCount(); ++contentnum) {
                    int currpos;
                    if (!arrayitemcontentDS.getString(contentnum, "contentsdcid").equals("Sample")) continue;
                    String currentParentSampleId = arrayitemcontentDS.getValue(contentnum, "contentkeyid1");
                    if (parentSampleIdList.contains(currentParentSampleId)) {
                        currpos = parentSampleIdList.indexOf(currentParentSampleId);
                        int currentcount = (Integer)childCount.get(currpos);
                        childCount.add(currpos, currentcount + 1);
                        continue;
                    }
                    parentSampleIdList.add(currentParentSampleId);
                    currpos = parentSampleIdList.indexOf(currentParentSampleId);
                    childCount.add(currpos, new Integer(1));
                }
                List parentchildlists = com.labvantage.sapphire.util.array.ArrayUtil.createDistinctChildSamples(parentSampleIdList, childCount, childSampleType, new DAMProcessor(connectionid), new QueryProcessor(connectionid), new ActionProcessor(connectionid), new ConfigurationProcessor(connectionid));
                ArrayList parentList = (ArrayList)parentchildlists.get(0);
                ArrayList childList = (ArrayList)parentchildlists.get(1);
                if (parentchildlists == null || parentchildlists.isEmpty()) break block8;
                arrayitemcontentDS = com.labvantage.sapphire.util.array.ArrayUtil.addOrdinalNumber(arrayitemcontentDS, loadingDirection, rowCount, colCount);
                arrayitemcontentDS.sort("ordinalnum");
                for (int i = 0; i < arrayitemcontentDS.size(); ++i) {
                    if (!arrayitemcontentDS.getValue(i, "contentsdcid").equals("Sample")) continue;
                    String parentid = arrayitemcontentDS.getValue(i, "contentkeyid1");
                    int itempos = parentList.indexOf(parentid);
                    String childid = (String)childList.get(itempos);
                    arrayitemcontentDS.setValue(i, "contentkeyid1", childid != null ? childid : parentid);
                    arrayitemcontentDS.setValue(i, "contentlabel", childid != null ? childid : parentid);
                    parentList.remove(itempos);
                    childList.remove(itempos);
                }
                break block8;
            }
            if ("S".equalsIgnoreCase(createChild)) {
                ArrayList<String> parentSampleIdList = new ArrayList<String>();
                ArrayList<Integer> childCount = new ArrayList<Integer>();
                for (int contentnum = 0; contentnum < arrayitemcontentDS.getRowCount(); ++contentnum) {
                    String currentParentSampleId;
                    if (!arrayitemcontentDS.getString(contentnum, "contentsdcid").equals("Sample") || parentSampleIdList.contains(currentParentSampleId = arrayitemcontentDS.getValue(contentnum, "contentkeyid1"))) continue;
                    parentSampleIdList.add(currentParentSampleId);
                    int currpos = parentSampleIdList.indexOf(currentParentSampleId);
                    childCount.add(currpos, new Integer(1));
                }
                List parentchildlists = com.labvantage.sapphire.util.array.ArrayUtil.createDistinctChildSamples(parentSampleIdList, childCount, childSampleType, new DAMProcessor(connectionid), new QueryProcessor(connectionid), new ActionProcessor(connectionid), new ConfigurationProcessor(connectionid));
                ArrayList parentList = (ArrayList)parentchildlists.get(0);
                ArrayList childList = (ArrayList)parentchildlists.get(1);
                if (parentchildlists != null && !parentchildlists.isEmpty()) {
                    arrayitemcontentDS = com.labvantage.sapphire.util.array.ArrayUtil.addOrdinalNumber(arrayitemcontentDS, loadingDirection, rowCount, colCount);
                    arrayitemcontentDS.sort("ordinalnum");
                    for (int i = 0; i < arrayitemcontentDS.size(); ++i) {
                        if (!arrayitemcontentDS.getValue(i, "contentsdcid").equals("Sample")) continue;
                        String parentid = arrayitemcontentDS.getValue(i, "contentkeyid1");
                        int itempos = parentList.indexOf(parentid);
                        String childid = (String)childList.get(itempos);
                        arrayitemcontentDS.setValue(i, "contentkeyid1", childid != null ? childid : parentid);
                        arrayitemcontentDS.setValue(i, "contentlabel", childid != null ? childid : parentid);
                    }
                }
            }
        }
        return arrayitemcontentDS;
    }

    public static String findArrayZoneId(QueryProcessor queryProcessor, String arrayid, String zone) {
        String sql = "select  arrayzoneid from arrayzone where arrayid =  ? and zone = ?";
        SafeSQL safeSQL = new SafeSQL();
        safeSQL.addVar(arrayid);
        safeSQL.addVar(zone);
        DataSet ds = queryProcessor.getPreparedSqlDataSet(sql, safeSQL.getValues());
        if (ds != null && ds.getRowCount() > 0) {
            return ds.getValue(0, "arrayzoneid");
        }
        return "";
    }

    public static boolean isAdhocZone(QueryProcessor queryProcessor, String arrayid, String arrayzoneid) {
        String sql = "select adhocmodeflag from arrayzone where arrayid = ? and arrayzoneid =  ?";
        SafeSQL safeSQL = new SafeSQL();
        safeSQL.addVar(arrayid);
        safeSQL.addVar(arrayzoneid);
        DataSet ds = queryProcessor.getPreparedSqlDataSet(sql, safeSQL.getValues());
        if (ds != null && ds.getRowCount() > 0) {
            return ds.getValue(0, "adhocmodeflag", "").equals("Y");
        }
        return false;
    }

    public static boolean checkInZone(QueryProcessor queryProcessor, String arrayitemid, String arrayzoneid) {
        String sql = "select arrayitemid from arrayitemarrayzone where arrayitemid = ? and arrayzoneid =  ?";
        SafeSQL safeSQL = new SafeSQL();
        safeSQL.addVar(arrayitemid);
        safeSQL.addVar(arrayzoneid);
        return queryProcessor.getPreparedSqlDataSet(sql, safeSQL.getValues()).getRowCount() != 0;
    }

    public static String getArrayItemIdsByLabels(QueryProcessor queryProcessor, String arrayid, String itemlabels) throws SapphireException {
        String[] itemLabelList = StringUtil.split(itemlabels, ";");
        String arrayitemids = "";
        String sql = "SELECT arrayitemid, itemlabel FROM arrayitem WHERE arrayid=?  ORDER by itemlabel";
        SafeSQL safeSQL = new SafeSQL();
        safeSQL.addVar(arrayid);
        DataSet ds = queryProcessor.getPreparedSqlDataSet(sql, safeSQL.getValues());
        if (ds != null && ds.getRowCount() > 0) {
            block0: for (int l = 0; l < itemLabelList.length; ++l) {
                for (int i = 0; i < ds.getRowCount(); ++i) {
                    if (!ds.getString(i, "itemlabel", "").equals(itemLabelList[l])) continue;
                    if (l > 0) {
                        arrayitemids = arrayitemids + ";";
                    }
                    arrayitemids = arrayitemids + ds.getString(i, "arrayitemid");
                    continue block0;
                }
            }
        }
        return arrayitemids;
    }
}

