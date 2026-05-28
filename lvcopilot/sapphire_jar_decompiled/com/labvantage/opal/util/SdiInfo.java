/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.opal.util;

import com.labvantage.opal.util.Logger;
import com.labvantage.sapphire.BaseCustom;
import com.labvantage.sapphire.services.SapphireConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import javax.servlet.jsp.PageContext;
import sapphire.SapphireException;
import sapphire.accessor.DAMProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.SDIProcessor;
import sapphire.util.DataSet;
import sapphire.util.HttpUtil;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class SdiInfo
extends BaseCustom {
    static final String LABVANTAGE_CVS_ID = "$Revision: 77364 $";
    static final String SECURITYSET = "securityset";
    private static HashSet versionedSDCSet = null;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static boolean isVersionedSDC(String sdcid, PageContext pageContext) {
        if (versionedSDCSet == null) {
            versionedSDCSet = new HashSet();
            HashSet hashSet = versionedSDCSet;
            synchronized (hashSet) {
                DataSet ds = new QueryProcessor(pageContext).getSqlDataSet("Select sdcid from sdc where versionedflag='Y'");
                for (int i = 0; i < ds.getRowCount(); ++i) {
                    versionedSDCSet.add(ds.getValue(i, "sdcid"));
                }
            }
        }
        return versionedSDCSet.contains(sdcid);
    }

    public static String getCurrentVersion(String sdcid, String keyid1List, String keyid3List, SapphireConnection sc) throws SapphireException {
        return SdiInfo.getCurrentVersion(sdcid, keyid1List, keyid3List, sc.getConnectionId());
    }

    public static String getCurrentVersion(String sdcid, String keyid1List, String keyid3List, PageContext context) throws SapphireException {
        return SdiInfo.getCurrentVersion(sdcid, keyid1List, keyid3List, HttpUtil.getConnectionId(context));
    }

    public static String getCurrentVersion(String sdcid, String keyid1List, String keyid3List, String connectionId) throws SapphireException {
        SdiInfo me = new SdiInfo();
        me.setConnectionId(connectionId);
        return me.getCurrentVersion(sdcid, keyid1List, keyid3List);
    }

    public String getCurrentVersion(String sdcid, String keyid1List, String keyid3List) throws SapphireException {
        if (sdcid == null || sdcid.length() == 0) {
            throw new SapphireException("Sdcid is not supplied.");
        }
        if (keyid1List == null || keyid1List.length() == 0) {
            throw new SapphireException("Keyid1 must be supplied.");
        }
        String[] keyid1s = StringUtil.split(keyid1List, ";");
        String[] keyid3s = null;
        if (keyid3List != null && keyid3List.trim().length() > 0) {
            keyid3s = StringUtil.split(keyid3List, ";");
        }
        if (keyid3s != null && keyid1s.length != keyid3s.length) {
            throw new SapphireException("Number of params for keyid1 & keyid3 should be same.");
        }
        String rsetId = "";
        String versionIds = "";
        DAMProcessor damProcessor = this.getDAMProcessor();
        rsetId = damProcessor.createRSet(sdcid, keyid1List, keyid1List, keyid3s != null ? keyid3List : "");
        versionIds = this.resolveCurrentVersion(sdcid, keyid1s, keyid3s, rsetId);
        if (rsetId.length() > 0) {
            damProcessor.clearRSet(rsetId);
        }
        return versionIds;
    }

    private String resolveCurrentVersion(String sdcid, String[] keyid1s, String[] keyid3s, String rsetId) throws SapphireException {
        boolean keycolid3exists;
        String currentVersion = "";
        SDCProcessor sdcProcessor = this.getSDCProcessor();
        String tableid = sdcProcessor.getProperty(sdcid, "tableid");
        String keycolid1 = sdcProcessor.getProperty(sdcid, "keycolid1");
        String keycolid2 = sdcProcessor.getProperty(sdcid, "keycolid2");
        String keycolid3 = sdcProcessor.getProperty(sdcid, "keycolid3");
        boolean bl = keycolid3exists = keycolid3.length() > 0;
        if (keycolid3exists && keyid3s == null) {
            throw new SapphireException("Keyid3 must be supplied.");
        }
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT " + keycolid1 + "," + keycolid2 + " version,");
        sql.append(keycolid3exists ? keycolid3 + "," : "");
        sql.append(" versionstatus ");
        sql.append(" FROM " + tableid + ", rsetitems ri");
        sql.append(" WHERE " + keycolid1 + " = ri.keyid1");
        sql.append(keycolid3exists ? " AND " + keycolid3 + " = ri.keyid3 " : "");
        sql.append(" AND (versionstatus='P' OR versionstatus='C') AND ri.rsetid = ?");
        sql.append(" ORDER BY " + keycolid1 + ",");
        sql.append(keycolid3exists ? keycolid3 + "," : "");
        sql.append(" versionstatus,cast (" + keycolid2 + " as numeric) desc");
        Object[] p = new Object[]{rsetId};
        DataSet allData = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), p);
        if (allData.getRowCount() > 0) {
            StringBuffer errMsg = new StringBuffer();
            HashMap<String, String> filterMap = new HashMap<String, String>();
            for (int i = 0; i < keyid1s.length; ++i) {
                int rowNum;
                filterMap.clear();
                filterMap.put(keycolid1, keyid1s[i]);
                if (keycolid3exists) {
                    filterMap.put(keycolid3, keyid3s[i]);
                }
                if ((rowNum = allData.findRow(filterMap)) == -1) {
                    errMsg.append("No current version exists with " + keycolid1 + " '" + keyid1s[i] + "'");
                    errMsg.append(keycolid3exists ? " and " + keycolid3 + " '" + keyid3s[i] + "'" : "");
                    throw new SapphireException(errMsg.toString());
                }
                currentVersion = currentVersion + ";" + allData.getString(rowNum, "version", "");
            }
            currentVersion = currentVersion.substring(1);
        }
        return currentVersion;
    }

    public static void copyDownColumns(PropertyList sdcProps, DataSet primaryData, DataSet beforeEditPrimaryData, ArrayList<PropertyList> copyDownPolicy, SDCProcessor sdcProcessor, SDIProcessor sdiProcessor) {
        HashMap updateMap = new HashMap();
        for (PropertyList copyFrom : copyDownPolicy) {
            PropertyListCollection copyColumns = copyFrom.getCollection("columns");
            String fromSdc = copyFrom.getProperty("sdcid");
            String fkcolumnid = copyFrom.getProperty("fkcolumnid", "");
            String fkcolumnid2 = copyFrom.getProperty("fkcolumnid2", "");
            String fkcolumnid3 = copyFrom.getProperty("fkcolumnid3", "");
            if (fkcolumnid.length() <= 0 || copyColumns.size() <= 0) continue;
            PropertyList fromSDCPL = sdcProcessor.getPropertyList(fromSdc);
            boolean fromsdc_versioned = "Y".equalsIgnoreCase(fromSDCPL.getProperty("versionedflag"));
            if (fromsdc_versioned) {
                for (int i = 0; i < primaryData.size(); ++i) {
                    String fromKeyId1 = primaryData.getValue(i, fkcolumnid);
                    String fromKeyId2 = primaryData.getValue(i, fkcolumnid2, "C");
                    String fromKeyId3 = primaryData.getValue(i, fkcolumnid3);
                    if (fromKeyId1.length() <= 0 || !fromKeyId2.equalsIgnoreCase("C")) continue;
                    try {
                        fromKeyId2 = SdiInfo.getCurrentVersion(fromSdc, fromKeyId1, fromKeyId3, sdcProcessor.getConnectionid());
                        primaryData.setString(i, fkcolumnid2, fromKeyId2);
                        continue;
                    }
                    catch (SapphireException e) {
                        Logger.logTrace(2, "Coluld not fetch current version of " + fromSdc + ": " + fromKeyId1 + "," + fromKeyId3);
                    }
                }
            }
            SdiInfo.copyDownColumnFromFK(fromSdc, fkcolumnid, fkcolumnid2, fkcolumnid3, primaryData, beforeEditPrimaryData, copyColumns, sdcProps, updateMap, sdiProcessor, sdcProcessor);
        }
    }

    public static void copyDownColumnFromFK(String fromSdc, String fkcolumnid, String fkcolumnid2, String fkcolumnid3, DataSet primaryData, DataSet beforeEditPrimaryData, PropertyListCollection copyColumns, PropertyList sdcProps, HashMap updateMap, SDIProcessor sdiProcessor, SDCProcessor sdcProcessor) {
        String sdcid = sdcProps.getProperty("sdcid");
        String keycolid1 = sdcProps.getProperty("keycolid1");
        String keycolid2 = sdcProps.getProperty("keycolid2");
        String keycolid3 = sdcProps.getProperty("keycolid3");
        String accesscontrolledflag = sdcProps.getProperty("accesscontrolledflag");
        SDIRequest request = new SDIRequest();
        request.setSDIList(fromSdc, primaryData.getColumnValues(fkcolumnid, ";"), fkcolumnid2.length() > 0 ? primaryData.getColumnValues(fkcolumnid2, ";") : "", fkcolumnid3.length() > 0 ? primaryData.getColumnValues(fkcolumnid3, ";") : "");
        request.setRequestItem("primary");
        SDIData fromSDCSDIData = sdiProcessor.getSDIData(request);
        DataSet fromSdcPrimary = fromSDCSDIData.getDataset("primary");
        HashMap fromSDCProps = sdcProcessor.getSDCProperties(fromSdc);
        for (int j = 0; j < copyColumns.size(); ++j) {
            String fromcol = copyColumns.getPropertyList(j).getProperty("fromcolumnid", "");
            String tocol = copyColumns.getPropertyList(j).getProperty("tocolumnid", "");
            String copyOnlyIfNull = copyColumns.getPropertyList(j).getProperty("copyonlyifnull", "Y");
            String copyDownNullValue = copyColumns.getPropertyList(j).getProperty("copydownnull", "N");
            if (tocol.length() == 0 || fromcol.length() == 0 || tocol.equals(SECURITYSET) && accesscontrolledflag.equals("S")) continue;
            if (!primaryData.isValidColumn(tocol)) {
                PropertyListCollection columns = sdcProps.getCollection("columns");
                PropertyList column = columns.getPropertyList(tocol);
                if (column == null) continue;
                String datatype = column.getProperty("datatype");
                if (datatype.equals("C")) {
                    primaryData.addColumn(tocol, 0);
                } else if (datatype.equals("T") || datatype.equals("B")) {
                    primaryData.addColumn(tocol, 3);
                } else if (datatype.equals("N") || datatype.equals("R")) {
                    primaryData.addColumn(tocol, 1);
                } else if (datatype.equals("D")) {
                    primaryData.addColumn(tocol, 2);
                    if ("Y".equals(column.getProperty("timezoneindependent"))) {
                        primaryData.setTimeZoneInsensitive(tocol);
                    }
                }
                for (int k = 0; k < primaryData.getRowCount(); ++k) {
                    primaryData.setValue(k, tocol, SdiInfo.getOldPrimaryValue(sdcProps, keycolid1, keycolid2, keycolid3, primaryData, beforeEditPrimaryData, k, tocol, ""));
                }
            }
            for (int k = 0; k < primaryData.getRowCount(); ++k) {
                String oldValue;
                boolean copy = true;
                if (beforeEditPrimaryData != null) {
                    if (SdiInfo.getOldPrimaryValue(sdcProps, keycolid1, keycolid2, keycolid3, primaryData, beforeEditPrimaryData, k, "templateflag", "N").equals("Y")) {
                        copy = false;
                    } else {
                        String newValue;
                        oldValue = SdiInfo.getOldPrimaryValue(sdcProps, keycolid1, keycolid2, keycolid3, primaryData, beforeEditPrimaryData, k, fkcolumnid, "");
                        if (oldValue.equals(newValue = primaryData.getValue(k, fkcolumnid, ""))) {
                            if (fkcolumnid.length() > 0 && fkcolumnid2.length() > 0 && primaryData.isValidColumn(fkcolumnid2)) {
                                oldValue = SdiInfo.getOldPrimaryValue(sdcProps, keycolid1, keycolid2, keycolid3, primaryData, beforeEditPrimaryData, k, fkcolumnid2, "");
                                if (oldValue.equals(newValue = primaryData.getValue(k, fkcolumnid2, ""))) {
                                    if (fkcolumnid.length() > 0 && fkcolumnid3.length() > 0 && primaryData.isValidColumn(fkcolumnid3)) {
                                        oldValue = SdiInfo.getOldPrimaryValue(sdcProps, keycolid1, keycolid2, keycolid3, primaryData, beforeEditPrimaryData, k, fkcolumnid3, "");
                                        if (oldValue.equals(newValue = primaryData.getValue(k, fkcolumnid3, ""))) {
                                            copy = false;
                                        }
                                    } else {
                                        copy = false;
                                    }
                                }
                            } else {
                                copy = false;
                            }
                        }
                    }
                }
                if (copy && StringUtil.getYN(copyOnlyIfNull, "Y").equals("Y") && (oldValue = primaryData.getValue(k, tocol, "")).length() > 0) {
                    copy = false;
                }
                if (!copy) continue;
                HashMap map = new HashMap();
                map.put(fromSDCProps.get("keycolid1"), primaryData.getValue(k, fkcolumnid));
                if (fkcolumnid2.length() > 0 && primaryData.isValidColumn(fkcolumnid2)) {
                    map.put(fromSDCProps.get("keycolid2"), primaryData.getValue(k, fkcolumnid2));
                }
                if (fkcolumnid3.length() > 0 && primaryData.isValidColumn(fkcolumnid3)) {
                    map.put(fromSDCProps.get("keycolid3"), primaryData.getValue(k, fkcolumnid3));
                }
                String valueToBeCopied = "";
                int fromRow = fromSdcPrimary.findRow(map);
                if (fromRow > -1) {
                    valueToBeCopied = fromSdcPrimary.getValue(fromRow, fromcol, "");
                    if (valueToBeCopied.length() == 0 && StringUtil.getYN(copyDownNullValue, "N").equals("N")) {
                        copy = false;
                    }
                } else {
                    copy = false;
                }
                if (!copy) continue;
                if (updateMap.containsKey(tocol + ":" + k)) {
                    String conflictMessage = "Cannot set " + fromSdc + "." + fromcol + ". " + sdcid + "." + tocol + " already set using " + updateMap.get(tocol + ":" + k) + ". Please check the CopyDownPolicy for conflicting configuration for " + sdcid + " sdc.";
                    sapphire.util.Logger.logInfo("CONFLICTING_POLICY_CONFIGURATION", conflictMessage);
                    continue;
                }
                updateMap.put(tocol + ":" + k, fromSdc + "." + fromcol);
                primaryData.setValue(k, tocol, valueToBeCopied);
            }
        }
    }

    private static void setColumnValue(DataSet primaryData, DataSet beforeEditPrimaryData, String tocol, int type) {
        if (beforeEditPrimaryData != null) {
            primaryData.addColumnValues(tocol, type, beforeEditPrimaryData.getColumnValues(tocol, ";"), ";");
        }
    }

    public static String getOldPrimaryValue(PropertyList sdcProps, String keycol1, String keycol2, String keycol3, DataSet newPrimary, DataSet oldPrimary, int primaryRow, String columnId, String defaultValue) {
        int oldRow;
        String oldValue = defaultValue;
        String keyid1 = newPrimary.getValue(primaryRow, keycol1);
        String keyid2 = newPrimary.getValue(primaryRow, keycol2);
        String keyid3 = newPrimary.getValue(primaryRow, keycol3);
        HashMap<String, String> findMap = new HashMap<String, String>();
        findMap.put(sdcProps.getProperty("keycolid1"), keyid1);
        if (keyid2 != null && keyid2.length() > 0) {
            findMap.put(sdcProps.getProperty("keycolid2"), keyid2);
        }
        if (keyid3 != null && keyid3.length() > 0) {
            findMap.put(sdcProps.getProperty("keycolid3"), keyid3);
        }
        if (oldPrimary != null && (oldRow = oldPrimary.findRow(findMap)) >= 0 && oldRow < oldPrimary.size()) {
            oldValue = oldPrimary.getValue(oldRow, columnId, defaultValue);
        }
        return oldValue;
    }
}

