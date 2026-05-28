/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.sdisecurity;

import com.labvantage.opal.util.SdiInfo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.SDIProcessor;
import sapphire.util.DBAccess;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class SDISecurity {
    static final String LABVANTAGE_CVS_ID = "1: 1.1 $";
    private static final String SDC = "LV_SecuritySet";
    private static final String SECURITYSET = "securityset";
    private static final String SECURITYSETSDC = "securitysetsdc";
    private static final String SDCIDCOL = "securitysetsdcid";
    private static final String SECURITYSETID = "securitysetid";
    private static HashMap<String, String> filterrmap = new HashMap();

    public static void copyDownSecuritySet(PropertyList sdcProps, DataSet primaryData, DataSet beforeEditPrimaryData, ArrayList<PropertyList> copyDownPolicy, SDCProcessor sdcProcessor, SDIProcessor sdiProcessor, QueryProcessor qp, ActionProcessor ap) throws SapphireException {
        String sdcid = sdcProps.getProperty("sdcid");
        String toaccesscontrolledflag = sdcProps.getProperty("accesscontrolledflag");
        HashMap<Integer, Boolean> sdiEditedMap = new HashMap<Integer, Boolean>();
        SDISecurity.populateMap(primaryData, sdiEditedMap);
        if (toaccesscontrolledflag.equals("S") || toaccesscontrolledflag.equals("D")) {
            for (PropertyList copyFrom : copyDownPolicy) {
                String fromSdc = copyFrom.getProperty("sdcid");
                String fkcolumnid = copyFrom.getProperty("fkcolumnid", "");
                String fkcolumnid2 = copyFrom.getProperty("fkcolumnid2", "");
                String fkcolumnid3 = copyFrom.getProperty("fkcolumnid3", "");
                if (fkcolumnid.length() <= 0) continue;
                SDIRequest request = new SDIRequest();
                request.setSDIList(fromSdc, primaryData.getColumnValues(fkcolumnid, ";"), fkcolumnid2.length() > 0 ? primaryData.getColumnValues(fkcolumnid2, ";") : "", fkcolumnid3.length() > 0 ? primaryData.getColumnValues(fkcolumnid3, ";") : "");
                request.setRequestItem("primary");
                SDIData fromSDCSDIData = sdiProcessor.getSDIData(request);
                DataSet fromSdcPrimary = fromSDCSDIData.getDataset("primary");
                if (fromSdcPrimary.getRowCount() <= 0) continue;
                HashMap fromSDCProps = sdcProcessor.getSDCProperties(fromSdc);
                SDIRequest ssRequest = new SDIRequest();
                ssRequest.setSDIList(SDC, fromSdcPrimary.getColumnValues(SECURITYSET, ";"), "", "");
                ssRequest.setRequestItem("primary");
                ssRequest.setRequestItem(SECURITYSETSDC);
                SDIData securitysetData = sdiProcessor.getSDIData(ssRequest);
                DataSet securitysetSdcs = securitysetData.getDataset(SECURITYSETSDC);
                SDISecurity.populatePrimary(sdcid, primaryData, fkcolumnid, fkcolumnid2, fkcolumnid3, fromSdcPrimary, fromSDCProps, securitysetSdcs, sdiEditedMap, beforeEditPrimaryData, sdcProps, qp, ap);
            }
        }
    }

    private static void populateMap(DataSet primaryData, Map<Integer, Boolean> sdiEditedMap) {
        int count = primaryData.size();
        for (int i = 0; i < count; ++i) {
            sdiEditedMap.put(i, false);
        }
    }

    private static void populatePrimary(String sdcid, DataSet primaryData, String fkcolumnid, String fkcolumnid2, String fkcolumnid3, DataSet fromSdcPrimary, HashMap<String, String> fromSDCProps, DataSet securitysetsdcs, Map<Integer, Boolean> sdiEditedMap, DataSet beforeEditPrimaryData, PropertyList sdcProps, QueryProcessor qp, ActionProcessor ap) throws SapphireException {
        int count = primaryData.getRowCount();
        String keycolid1 = sdcProps.getProperty("keycolid1");
        String keycolid2 = sdcProps.getProperty("keycolid2");
        String keycolid3 = sdcProps.getProperty("keycolid3");
        HashMap<Integer, String> oldSecuritySetMap = new HashMap<Integer, String>();
        HashMap<Integer, String> oldSecurityDeptMap = new HashMap<Integer, String>();
        DataSet addSDISecuritySet = new DataSet();
        DataSet addSDISecurityDepartment = new DataSet();
        String toaccesscontrolledflag = sdcProps.getProperty("accesscontrolledflag");
        for (int i = 0; i < count; ++i) {
            StringBuffer sdisecuritySQL;
            SafeSQL safeSQL;
            String newSecuritySet = primaryData.getValue(i, SECURITYSET);
            String newSecurityDept = primaryData.getValue(i, "securitydepartment");
            String keyid1 = primaryData.getValue(i, keycolid1, "");
            String keyid2 = keycolid2.length() > 0 ? primaryData.getValue(i, keycolid2, "") : "";
            String keyid3 = keycolid3.length() > 0 ? primaryData.getValue(i, keycolid3, "") : "";
            String fkvalue = primaryData.getValue(i, fkcolumnid, "");
            String fkvalue2 = primaryData.getValue(i, fkcolumnid2, "");
            String fkvalue3 = primaryData.getValue(i, fkcolumnid3, "");
            if (beforeEditPrimaryData != null && beforeEditPrimaryData.getRowCount() > 0) {
                boolean fkvalueChanged = false;
                if (fkvalue.length() > 0) {
                    boolean bl = fkvalueChanged = !fkvalue.equals(SdiInfo.getOldPrimaryValue(sdcProps, keycolid1, keycolid2, keycolid3, primaryData, beforeEditPrimaryData, i, fkcolumnid, ""));
                    if (!fkvalueChanged && fkcolumnid2.length() > 0) {
                        boolean bl2 = fkvalueChanged = !fkvalue2.equals(SdiInfo.getOldPrimaryValue(sdcProps, keycolid1, keycolid2, keycolid3, primaryData, beforeEditPrimaryData, i, fkcolumnid2, ""));
                    }
                    if (!fkvalueChanged && fkcolumnid3.length() > 0) {
                        boolean bl3 = fkvalueChanged = !fkvalue3.equals(SdiInfo.getOldPrimaryValue(sdcProps, keycolid1, keycolid2, keycolid3, primaryData, beforeEditPrimaryData, i, fkcolumnid3, ""));
                    }
                }
                if (!fkvalueChanged) {
                    String oldSecurityDepartment;
                    String oldSecuritySet = SdiInfo.getOldPrimaryValue(sdcProps, keycolid1, keycolid2, keycolid3, primaryData, beforeEditPrimaryData, i, SECURITYSET, "");
                    if (oldSecuritySet.length() > 0) {
                        oldSecuritySetMap.put(i, oldSecuritySet);
                    }
                    if ((oldSecurityDepartment = SdiInfo.getOldPrimaryValue(sdcProps, keycolid1, keycolid2, keycolid3, primaryData, beforeEditPrimaryData, i, "securitydepartment", "")).length() <= 0) continue;
                    oldSecurityDeptMap.put(i, oldSecurityDepartment);
                    continue;
                }
            }
            if (fkvalue.length() <= 0) continue;
            String securitySetId = "";
            String securityDepartmentId = "";
            filterrmap.clear();
            filterrmap.put(fromSDCProps.get("keycolid1"), fkvalue);
            if (fkvalue2.length() > 0) {
                filterrmap.put(fromSDCProps.get("keycolid2"), fkvalue2);
            }
            if (fkvalue3.length() > 0) {
                filterrmap.put(fromSDCProps.get("keycolid3"), fkvalue3);
            }
            DataSet fromSDI = fromSdcPrimary.getFilteredDataSet(filterrmap);
            if (toaccesscontrolledflag.equals("S") && newSecuritySet.length() == 0 && (securitySetId = fromSDI.getValue(0, SECURITYSET, "")).length() > 0) {
                if (!sdiEditedMap.get(i).booleanValue()) {
                    filterrmap.clear();
                    filterrmap.put(SECURITYSETID, securitySetId);
                    DataSet filteredSDCs = securitysetsdcs.getFilteredDataSet(filterrmap);
                    filterrmap.clear();
                    filterrmap.put(SDCIDCOL, sdcid);
                    if (filteredSDCs.findRow(filterrmap) != -1) {
                        primaryData.setString(i, SECURITYSET, securitySetId);
                        sdiEditedMap.put(i, true);
                    }
                } else {
                    String conflictMessage = "Cannot set " + fromSDCProps.get("sdcid") + "." + SECURITYSET + ". " + sdcid + "." + SECURITYSET + " already set";
                    Logger.logInfo("CONFLICTING_POLICY_CONFIGURATION", conflictMessage);
                }
            }
            if (toaccesscontrolledflag.equals("D") && newSecurityDept.length() == 0 && (securityDepartmentId = fromSDI.getValue(0, "securitydepartment", "")).length() > 0) {
                primaryData.setString(i, "securitydepartment", securityDepartmentId);
            }
            if (toaccesscontrolledflag.equals("S")) {
                safeSQL = new SafeSQL();
                sdisecuritySQL = new StringBuffer("SELECT ss.securityset, ss.operationid FROM sdisecurityset ss, securitysetsdc ssdc  WHERE ss.sdcid = ").append(safeSQL.addVar(fromSDCProps.get("sdcid"))).append(" AND ss.keyid1 =  ").append(safeSQL.addVar(fkvalue));
                if (fkvalue2.length() > 0) {
                    sdisecuritySQL.append(" AND ss.keyid2 = ").append(safeSQL.addVar(fkvalue2));
                }
                if (fkvalue3.length() > 0) {
                    sdisecuritySQL.append(" AND ss.keyid3 = ").append(safeSQL.addVar(fkvalue3));
                }
                sdisecuritySQL.append(" AND ss.securityset = ssdc.securitysetid and ssdc.securitysetsdcid = ").append(safeSQL.addVar(sdcid));
                sdisecuritySQL.append(" AND NOT EXISTS ( SELECT 1 FROM sdisecurityset WHERE sdcid = ").append(safeSQL.addVar(sdcid)).append(" AND keyid1 = ").append(safeSQL.addVar(keyid1));
                if (keycolid2.length() > 0) {
                    sdisecuritySQL.append(" AND keyid2 = ").append(safeSQL.addVar(keyid2));
                }
                if (keycolid3.length() > 0) {
                    sdisecuritySQL.append(" AND keyid3 = ").append(safeSQL.addVar(keyid3));
                }
                sdisecuritySQL.append(" AND  securityset = ss.securityset AND operationid = ss.operationid )");
                DataSet fromsdisecuritySet = qp.getPreparedSqlDataSet(sdisecuritySQL.toString(), safeSQL.getValues());
                if (fromsdisecuritySet.getRowCount() <= 0) continue;
                fromsdisecuritySet.setString(-1, "keyid1", keyid1);
                if (keyid2.length() > 0) {
                    fromsdisecuritySet.setString(-1, "keyid2", keyid2);
                }
                if (keyid3.length() > 0) {
                    fromsdisecuritySet.setString(-1, "keyid3", keyid3);
                }
                addSDISecuritySet.copyRow(fromsdisecuritySet, -1, 1);
                continue;
            }
            if (!toaccesscontrolledflag.equals("D")) continue;
            safeSQL = new SafeSQL();
            sdisecuritySQL = new StringBuffer("SELECT ss.securitydepartment, ss.operationid FROM sdisecuritydepartment ss  WHERE ss.sdcid = ").append(safeSQL.addVar(fromSDCProps.get("sdcid"))).append(" AND ss.keyid1 =  ").append(safeSQL.addVar(fkvalue));
            if (fkvalue2.length() > 0) {
                sdisecuritySQL.append(" AND ss.keyid2 = ").append(safeSQL.addVar(fkvalue2));
            }
            if (fkvalue3.length() > 0) {
                sdisecuritySQL.append(" AND ss.keyid3 = ").append(safeSQL.addVar(fkvalue3));
            }
            sdisecuritySQL.append(" AND NOT EXISTS ( SELECT 1 FROM sdisecuritydepartment WHERE sdcid = ").append(safeSQL.addVar(sdcid)).append(" AND keyid1 = ").append(safeSQL.addVar(keyid1));
            if (keycolid2.length() > 0) {
                sdisecuritySQL.append(" AND keyid2 = ").append(safeSQL.addVar(keyid2));
            }
            if (keycolid3.length() > 0) {
                sdisecuritySQL.append(" AND keyid3 = ").append(safeSQL.addVar(keyid3));
            }
            sdisecuritySQL.append(" AND  securitydepartment = ss.securitydepartment AND operationid = ss.operationid )");
            DataSet fromsdisecurityDepartment = qp.getPreparedSqlDataSet(sdisecuritySQL.toString(), safeSQL.getValues());
            if (fromsdisecurityDepartment.getRowCount() <= 0) continue;
            fromsdisecurityDepartment.setString(-1, "keyid1", keyid1);
            if (keyid2.length() > 0) {
                fromsdisecurityDepartment.setString(-1, "keyid2", keyid2);
            }
            if (keyid3.length() > 0) {
                fromsdisecurityDepartment.setString(-1, "keyid3", keyid3);
            }
            addSDISecurityDepartment.copyRow(fromsdisecurityDepartment, -1, 1);
        }
        if (oldSecuritySetMap.size() > 0 && primaryData.isValidColumn(SECURITYSET)) {
            for (Integer key : oldSecuritySetMap.keySet()) {
                String oldSecuritySet = (String)oldSecuritySetMap.get(key);
                primaryData.setString(key, SECURITYSET, oldSecuritySet);
            }
        }
        if (oldSecurityDeptMap.size() > 0 && primaryData.isValidColumn("securitydepartment")) {
            for (Integer key : oldSecurityDeptMap.keySet()) {
                String oldSecurityDepartment = (String)oldSecurityDeptMap.get(key);
                primaryData.setString(key, "securitydepartment", oldSecurityDepartment);
            }
        }
        if (addSDISecuritySet.getRowCount() > 0) {
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", sdcid);
            props.setProperty("keyid1", addSDISecuritySet.getColumnValues("keyid1", ";"));
            if (keycolid2.length() > 0) {
                props.setProperty("keyid2", addSDISecuritySet.getColumnValues("keyid2", ";"));
            }
            if (keycolid3.length() > 0) {
                props.setProperty("keyid3", addSDISecuritySet.getColumnValues("keyid3", ";"));
            }
            props.setProperty(SECURITYSET, addSDISecuritySet.getColumnValues(SECURITYSET, ";"));
            props.setProperty("operationid", addSDISecuritySet.getColumnValues("operationid", ";"));
            props.setProperty("propsmatch", "Y");
            ap.processAction("AddSDISecuritySet", "1", props);
        }
        if (addSDISecurityDepartment.getRowCount() > 0) {
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", sdcid);
            props.setProperty("keyid1", addSDISecurityDepartment.getColumnValues("keyid1", ";"));
            if (keycolid2.length() > 0) {
                props.setProperty("keyid2", addSDISecurityDepartment.getColumnValues("keyid2", ";"));
            }
            if (keycolid3.length() > 0) {
                props.setProperty("keyid3", addSDISecurityDepartment.getColumnValues("keyid3", ";"));
            }
            props.setProperty("departmentid", addSDISecurityDepartment.getColumnValues("securitydepartment", ";"));
            props.setProperty("operationid", addSDISecurityDepartment.getColumnValues("operationid", ";"));
            props.setProperty("propsmatch", "Y");
            ap.processAction("AddSDISecurityDept", "1", props);
        }
    }

    public static void setDefaultSecuritySet(DataSet dsPrimary, DBAccess db, String sdcId) throws SapphireException {
        String defaultSecuritySet;
        db.createPreparedResultSet("getdefaultsecurityset", "select defaultsecurityset from sdc where sdcid = ?", new Object[]{sdcId});
        if (db.getNext("getdefaultsecurityset") && (defaultSecuritySet = db.getString("getdefaultsecurityset", "defaultsecurityset")) != null && defaultSecuritySet.length() > 0) {
            for (int i = 0; i < dsPrimary.getRowCount(); ++i) {
                if (dsPrimary.getString(i, SECURITYSET, "").length() != 0) continue;
                dsPrimary.setString(i, SECURITYSET, defaultSecuritySet);
            }
        }
    }
}

