/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.actions;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.actions.sdidata.BaseSDIDataAction;
import com.labvantage.sapphire.xml.PropertyListUtil;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class SyncSDIWIStatus
extends BaseAction
implements sapphire.action.SyncSDIWIStatus {
    public static String LABVANTAGE_CVS_ID = "$Revision: 102163 $";
    public static final String DSSTATUSCOLUMN = "s_datasetstatus";
    public static final String SDIWORKITEMSTATUSCOLUMN = "workitemstatus";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String sdcId = properties.getProperty("sdcid");
        String keyId1 = properties.getProperty("keyid1");
        String keyId2 = properties.getProperty("keyid2");
        String keyId3 = properties.getProperty("keyid3");
        boolean syncGroupOnly = StringUtil.getYN(properties.getProperty("syncsdiworkitemgroupstatusonly"), "N").equals("Y");
        String workItemId = properties.getProperty("workitemid");
        String workItemInstance = properties.getProperty("workiteminstance");
        String groupId = properties.getProperty("groupid");
        String groupInstance = properties.getProperty("groupinstance");
        String[] workItemIdprop = null;
        String[] workItemInstanceprop = null;
        String[] groupIdprop = null;
        String[] groupInstanceprop = null;
        String auditreason = properties.getProperty("auditreason");
        String auditactivity = properties.getProperty("auditactivity");
        String auditsignedflag = properties.getProperty("auditsignedflag");
        boolean bypassWSCompletionCheck = "Y".equals(properties.getProperty("bypassworksheetcompletioncheck"));
        TranslationProcessor tp = this.getTranslationProcessor();
        if (sdcId.length() == 0 || keyId1.length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", tp.translate("No SDC/KeyId1 specified in action input."));
        }
        SDCProcessor sdcProcessor = this.getSDCProcessor();
        String keycolid2 = sdcProcessor.getProperty(sdcId, "keycolid2");
        String keycolid3 = sdcProcessor.getProperty(sdcId, "keycolid3");
        String[] keyid1prop = StringUtil.split(keyId1, ";");
        String[] keyid2prop = null;
        String[] keyid3prop = null;
        if (keycolid2.length() > 0) {
            if (keyId2.length() == 0) {
                throw new SapphireException("INVALID_PROPERTY", tp.translate("No keyid2 specified"));
            }
            keyid2prop = StringUtil.split(keyId2, ";");
            if (keyid1prop.length != keyid2prop.length) {
                throw new SapphireException("INVALID_PROPERTY", tp.translate("Count of keyid2 not matching with count of keyid1"));
            }
        } else {
            keyId2 = null;
        }
        if (keycolid3.length() > 0) {
            if (keyId3.length() == 0) {
                throw new SapphireException("INVALID_PROPERTY", tp.translate("No keyid3 specified"));
            }
            keyid3prop = StringUtil.split(keyId3, ";");
            if (keyid1prop.length != keyid3prop.length) {
                throw new SapphireException("INVALID_PROPERTY", tp.translate("Count of keyid3 not matching with count of keyid1"));
            }
        } else {
            keyId3 = null;
        }
        DataSet inputWorkItems = new DataSet();
        if (syncGroupOnly) {
            workItemIdprop = StringUtil.split(workItemId, ";");
            workItemInstanceprop = StringUtil.split(workItemInstance, ";");
            groupIdprop = StringUtil.split(groupId, ";");
            groupInstanceprop = StringUtil.split(groupInstance, ";");
            if (workItemId.length() == 0 || workItemInstance.length() == 0 || groupId.length() == 0 || groupInstance.length() == 0) {
                throw new SapphireException("INVALID_PROPERTY", " " + tp.translate("WorkItemId, WorkItemInstance, GroupId, GroupInstance are mandatory"));
            }
            if (keyid1prop.length != workItemIdprop.length || workItemIdprop.length != workItemInstanceprop.length || workItemIdprop.length != groupIdprop.length || workItemIdprop.length != groupInstanceprop.length) {
                throw new SapphireException("INVALID_PROPERTY", " " + tp.translate("WorkItemId, WorkItemInstance, GroupId or GroupInstance not specified for all."));
            }
        } else {
            workItemIdprop = StringUtil.split(workItemId, ";");
            workItemInstanceprop = StringUtil.split(workItemInstance, ";");
            if (workItemId.length() > 0 || workItemInstance.length() > 0) {
                if (keyid1prop.length != workItemIdprop.length || workItemIdprop.length != workItemInstanceprop.length) {
                    throw new SapphireException("INVALID_PROPERTY", " " + tp.translate("WorkItemId, WorkItemInstance not specified for all."));
                }
                inputWorkItems.addColumnValues("keyid1", 0, keyId1, ";");
                inputWorkItems.addColumnValues("keyid2", 0, keyId2, ";");
                inputWorkItems.addColumnValues("keyid3", 0, keyId3, ";");
                inputWorkItems.addColumnValues("workitemid", 0, workItemId, ";");
                inputWorkItems.addColumnValues("workiteminstance", 1, workItemInstance, ";");
            }
        }
        DataSet sdiWorkItemGrps = new DataSet();
        if (!syncGroupOnly) {
            DataSet modifiedSDIs = this.evaluateSDIWorkItemStatus(sdcId, keyId1, keyId2, keyId3, inputWorkItems, bypassWSCompletionCheck);
            if (modifiedSDIs.getRowCount() > 0) {
                DataSet dsEditSDIWI = new DataSet();
                for (int i = 0; i < modifiedSDIs.getRowCount(); ++i) {
                    if (modifiedSDIs.getValue(i, "groupid", "").length() > 0) {
                        sdiWorkItemGrps.copyRow(modifiedSDIs, i, 1);
                    }
                    if ("P".equals(modifiedSDIs.getValue(i, "workitemtypeflag"))) continue;
                    dsEditSDIWI.copyRow(modifiedSDIs, i, 1);
                }
                if (dsEditSDIWI.getRowCount() > 0) {
                    String sysuserid = "(system)".equals(this.connectionInfo.getSysuserId()) ? "" : this.connectionInfo.getSysuserId();
                    dsEditSDIWI.sort(SDIWORKITEMSTATUSCOLUMN);
                    ArrayList<DataSet> list = dsEditSDIWI.getGroupedDataSets(SDIWORKITEMSTATUSCOLUMN);
                    for (DataSet ds : list) {
                        if (ds.size() <= 0) continue;
                        PropertyList props = new PropertyList();
                        String workitemstatus = ds.getString(0, SDIWORKITEMSTATUSCOLUMN);
                        if ("Completed".equals(workitemstatus)) {
                            ds.setString(-1, "completeddt", "n");
                            ds.setString(-1, "completedby", sysuserid);
                            props.setProperty("completeddt", ds.getColumnValues("completeddt", ";"));
                            props.setProperty("completedby", ds.getColumnValues("completedby", ";"));
                        } else if ("Cancelled".equals(workitemstatus)) {
                            ds.setString(-1, "cancelleddt", "n");
                            ds.setString(-1, "cancelledby", sysuserid);
                            props.setProperty("cancelleddt", ds.getColumnValues("cancelleddt", ";"));
                            props.setProperty("cancelledby", ds.getColumnValues("cancelledby", ";"));
                        }
                        props.setProperty("sdcid", sdcId);
                        props.setProperty("keyid1", ds.getColumnValues("keyid1", ";"));
                        props.setProperty("keyid2", ds.getColumnValues("keyid2", ";"));
                        props.setProperty("keyid3", ds.getColumnValues("keyid3", ";"));
                        props.setProperty("workitemid", ds.getColumnValues("workitemid", ";"));
                        props.setProperty("workiteminstance", ds.getColumnValues("workiteminstance", ";"));
                        props.setProperty(SDIWORKITEMSTATUSCOLUMN, ds.getColumnValues(SDIWORKITEMSTATUSCOLUMN, ";"));
                        props.setProperty("propsmatch", "Y");
                        props.setProperty("auditactivity", auditactivity);
                        props.setProperty("auditreason", auditreason);
                        props.setProperty("auditsignedflag", auditsignedflag);
                        this.getActionProcessor().processAction("EditSDIWorkItem", "1", props);
                    }
                }
            }
        } else {
            sdiWorkItemGrps.addColumnValues("keyid1", 0, keyId1, ";");
            sdiWorkItemGrps.addColumnValues("keyid2", 0, keyId2, ";");
            sdiWorkItemGrps.addColumnValues("keyid3", 0, keyId3, ";");
            sdiWorkItemGrps.addColumnValues("workitemid", 0, workItemId, ";");
            sdiWorkItemGrps.addColumnValues("workiteminstance", 0, workItemInstance, ";");
            sdiWorkItemGrps.addColumnValues("groupid", 0, groupId, ";");
            sdiWorkItemGrps.addColumnValues("groupinstance", 0, groupInstance, ";");
            sdiWorkItemGrps.setString(-1, "sdcid", sdcId);
        }
        if (sdiWorkItemGrps.getRowCount() > 0) {
            this.syncSDIWorkItemGroupStatus(sdiWorkItemGrps, properties);
        }
    }

    private DataSet evaluateSDIWorkItemStatus(String sdcid, String keyid1, String keyid2, String keyid3, DataSet inputWorkItems, boolean bypassWSCompletionCheck) throws SapphireException {
        int i;
        String rsetId = BaseSDIDataAction.createRSet(sdcid, keyid1, keyid2, keyid3, this.database, this.connectionInfo, false);
        QueryProcessor qp = this.getQueryProcessor();
        StringBuffer sql = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        sql.append("SELECT swi.sdcid, swi.keyid1, swi.keyid2, swi.keyid3, swi.workitemid, swi.workiteminstance, swi.groupid, swi.groupinstance, swi.workitemstatus, swi.workitemtypeflag ").append(" FROM sdiworkitem swi, rsetitems rs ").append(" WHERE swi.sdcid = rs.sdcid and swi.keyid1 = rs.keyid1 and swi.keyid2 = rs.keyid2 and swi.keyid2 = rs.keyid2 and rs.rsetid=" + safeSQL.addVar(rsetId)).append(" AND  workitemtypeflag = 'P'");
        DataSet dsAllGrpWorkItems = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        DataSet dsGrpWorkItems = new DataSet();
        if (dsAllGrpWorkItems.getRowCount() > 0) {
            if (inputWorkItems.getRowCount() > 0) {
                for (int d = 0; d < dsAllGrpWorkItems.getRowCount(); ++d) {
                    HashMap<String, Object> find = new HashMap<String, Object>();
                    find.put("keyid1", dsAllGrpWorkItems.getValue(d, "keyid1"));
                    if (keyid2 != null && keyid2.length() > 0) {
                        find.put("keyid2", dsAllGrpWorkItems.getValue(d, "keyid2"));
                    }
                    if (keyid3 != null && keyid3.length() > 0) {
                        find.put("keyid3", dsAllGrpWorkItems.getValue(d, "keyid3"));
                    }
                    find.put("workitemid", dsAllGrpWorkItems.getValue(d, "workitemid"));
                    find.put("workiteminstance", dsAllGrpWorkItems.getBigDecimal(d, "workiteminstance"));
                    if (inputWorkItems.findRow(find) <= -1) continue;
                    dsGrpWorkItems.copyRow(dsAllGrpWorkItems, d, 1);
                }
            } else {
                dsGrpWorkItems.copyRow(dsAllGrpWorkItems, -1, 1);
            }
        }
        sql.setLength(0);
        safeSQL.reset();
        sql.append("SELECT swi.sdiworkitemid, swi.sdcid, swi.keyid1, swi.keyid2, swi.keyid3, swi.workitemid, swi.workiteminstance, swi.appliedflag, swii.workitemitemid,").append(" swi.workitemstatus, swi.groupid, swi.groupinstance, swi.workitemtypeflag, swii.mandatoryflag, swii.itemkeyid1, swii.itemkeyid2, swii.itemkeyid3, swii.iteminstance, ds.dataset, ds.s_datasetstatus, ds.availabilityflag ").append(" FROM sdiworkitem swi, rsetitems rs, sdiworkitemitem swii LEFT OUTER JOIN sdidata ds").append(" ON swii.sdcid = ds.sdcid").append(" and swii.keyid1 = ds.keyid1").append(" and swii.keyid2 = ds.keyid2").append(" and swii.keyid3 = ds.keyid3").append(" and swii.itemkeyid1 = ds.paramlistid").append(" and swii.itemkeyid2 = ds.paramlistversionid").append(" and swii.itemkeyid3 = ds.variantid").append(" and swii.iteminstance = ds.dataset").append(" WHERE swii.sdcid = swi.sdcid and swii.keyid1 = swi.keyid1 and swii.keyid2 = swi.keyid2 and swii.keyid3 = swi.keyid3").append(" and swii.workitemid = swi.workitemid and swii.workiteminstance = swi.workiteminstance ").append(" and swi.sdcid = rs.sdcid and swi.keyid1 = rs.keyid1 and swi.keyid2 = rs.keyid2 ").append(" and swi.keyid3 = rs.keyid3 and rs.rsetid=" + safeSQL.addVar(rsetId)).append(" order by swi.sdcid, swi.keyid1, swi.keyid2, swi.keyid3, swi.workitemid, swi.workiteminstance");
        DataSet dsAll = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        DataSet ds = new DataSet();
        if (inputWorkItems.getRowCount() > 0 && dsAll.getRowCount() > 0) {
            for (int d = 0; d < dsAll.getRowCount(); ++d) {
                HashMap<String, Object> find = new HashMap<String, Object>();
                find.put("keyid1", dsAll.getValue(d, "keyid1"));
                if (keyid2 != null && keyid2.length() > 0) {
                    find.put("keyid2", dsAllGrpWorkItems.getValue(d, "keyid2"));
                }
                if (keyid3 != null && keyid3.length() > 0) {
                    find.put("keyid3", dsAllGrpWorkItems.getValue(d, "keyid3"));
                }
                find.put("workitemid", dsAll.getValue(d, "workitemid"));
                find.put("workiteminstance", dsAll.getBigDecimal(d, "workiteminstance"));
                if (inputWorkItems.findRow(find) <= -1) continue;
                ds.copyRow(dsAll, d, 1);
            }
        } else {
            ds.copyRow(dsAll, -1, 1);
        }
        ds.sort("sdcid, keyid1, keyid2, keyid3, workitemid, workiteminstance");
        StringBuilder sqlToFetchWSBlockingSample = new StringBuilder();
        sqlToFetchWSBlockingSample.append("select wsdi.sdcid , wsdi.keyid1 from worksheet ws, worksheetsdi wsdi,  rsetitems rs").append(" where ws.worksheetid = wsdi.worksheetid  ").append(" AND ws.worksheetversionid  = wsdi.worksheetversionid  ").append(" AND ws.worksheetstatus <> 'Complete' ").append(" AND wsdi.sdcid = rs.sdcid ").append(" AND wsdi.keyid1 = rs.keyid1 ").append(" AND rs.rsetid = ?").append(" AND ").append(" ( ws.blockflag = 'Y' OR ws.blockflag is null OR ws.blockflag = '' ) ").append(" AND ").append(" ( ws.blocksdcid = 'Sample'  OR ws.blocksdcid is null   OR ws.blocksdcid = '' ) ");
        DataSet worksheetBlockingSampleDS = this.getQueryProcessor().getPreparedSqlDataSet(sqlToFetchWSBlockingSample.toString(), (Object[])new String[]{rsetId});
        DataSet worksheetBlockingSDIWIDS = null;
        if (OpalUtil.isNotEmpty(ds)) {
            ArrayList<String> keyid1s = new ArrayList<String>();
            for (int count = 0; count < ds.getRowCount(); ++count) {
                keyid1s.add(ds.getString(count, "sdiworkitemid"));
            }
            String rsetSDIWI = BaseSDIDataAction.createRSet("SDIWorkItem", String.join((CharSequence)";", keyid1s), null, null, this.database, this.connectionInfo, false);
            StringBuilder sqlToFetchWSBlockingSDIWI = new StringBuilder();
            sqlToFetchWSBlockingSDIWI.append("select wsdi.sdcid , wsdi.keyid1 from worksheet ws, worksheetsdi wsdi,  rsetitems rs").append(" where ws.worksheetid = wsdi.worksheetid  ").append(" AND ws.worksheetversionid  = wsdi.worksheetversionid  ").append(" AND ws.worksheetstatus <> 'Complete' ").append(" AND wsdi.sdcid = 'SDIWorkItem' ").append(" AND wsdi.keyid1 = rs.keyid1 ").append(" AND rs.rsetid = ?").append(" AND ").append(" ( ws.blockflag = 'Y' OR ws.blockflag is null OR ws.blockflag = '' ) ").append(" AND ").append(" ( ws.blocksdcid = 'SDIWorkItem'  OR ws.blocksdcid is null   OR ws.blocksdcid = '' ) ");
            worksheetBlockingSDIWIDS = this.getQueryProcessor().getPreparedSqlDataSet(sqlToFetchWSBlockingSDIWI.toString(), (Object[])new String[]{rsetSDIWI});
            this.getDAMProcessor().clearRSet(rsetSDIWI);
        }
        ArrayList<DataSet> list = ds.getGroupedDataSets("sdcid, keyid1, keyid2, keyid3, workitemid, workiteminstance");
        DataSet modifiedDS = new DataSet();
        modifiedDS.addColumn("sdcid", 0);
        modifiedDS.addColumn("keyid1", 0);
        modifiedDS.addColumn("keyid2", 0);
        modifiedDS.addColumn("keyid3", 0);
        modifiedDS.addColumn("workitemid", 0);
        modifiedDS.addColumn("workiteminstance", 1);
        modifiedDS.addColumn(SDIWORKITEMSTATUSCOLUMN, 0);
        modifiedDS.addColumn("groupid", 0);
        modifiedDS.addColumn("groupinstance", 1);
        modifiedDS.addColumn("workitemtypeflag", 0);
        for (i = 0; i < list.size(); ++i) {
            DataSet dsSWI = list.get(i);
            DataSet sdidatas = new DataSet();
            block4: for (int k = 0; k < dsSWI.getRowCount(); ++k) {
                if (dsSWI.getValue(k, "dataset").trim().length() > 0) {
                    sdidatas.copyRow(dsSWI, k, 1);
                    continue;
                }
                HashMap<String, Object> find = new HashMap<String, Object>();
                find.put("sdcid", dsSWI.getValue(k, "sdcid"));
                find.put("keyid1", dsSWI.getValue(k, "keyid1"));
                find.put("keyid2", dsSWI.getValue(k, "keyid2"));
                find.put("keyid3", dsSWI.getValue(k, "keyid3"));
                find.put("itemkeyid1", dsSWI.getValue(k, "itemkeyid1"));
                find.put("itemkeyid2", dsSWI.getValue(k, "itemkeyid2"));
                find.put("itemkeyid3", dsSWI.getValue(k, "itemkeyid3"));
                find.put("iteminstance", dsSWI.getBigDecimal(k, "iteminstance"));
                DataSet sharedDS = ds.getFilteredDataSet(find);
                for (int s = 0; s < sharedDS.getRowCount(); ++s) {
                    if (sharedDS.getValue(s, "dataset").trim().length() <= 0) continue;
                    sdidatas.copyRow(sharedDS, s, 1);
                    continue block4;
                }
            }
            String sdiwiStatus = dsSWI.getString(0, SDIWORKITEMSTATUSCOLUMN, "");
            String wiSdcid = dsSWI.getValue(0, "sdcid");
            String wikeyid1 = dsSWI.getValue(0, "keyid1");
            String wikeyid2 = dsSWI.getValue(0, "keyid2");
            String wikeyid3 = dsSWI.getValue(0, "keyid3");
            String workitemid = dsSWI.getValue(0, "workitemid");
            String workiteminstance = dsSWI.getValue(0, "workiteminstance");
            String sdiworkitemid = dsSWI.getValue(0, "sdiworkitemid");
            if ("Cancelled".equals(sdiwiStatus)) continue;
            String tempStatus = "Initial";
            boolean isWorkSheetComplete = false;
            if (!bypassWSCompletionCheck) {
                HashMap<String, String> findsdisample = new HashMap<String, String>();
                findsdisample.put("sdcid", "Sample");
                findsdisample.put("keyid1", wikeyid1);
                HashMap<String, String> findsdiworkitem = new HashMap<String, String>();
                findsdiworkitem.put("sdcid", "SDIWorkItem");
                findsdiworkitem.put("keyid1", sdiworkitemid);
                int count = (OpalUtil.isNotEmpty(worksheetBlockingSampleDS) ? worksheetBlockingSampleDS.getFilteredDataSet(findsdisample).getRowCount() : 0) + (OpalUtil.isNotEmpty(worksheetBlockingSDIWIDS) ? worksheetBlockingSDIWIDS.getFilteredDataSet(findsdiworkitem).getRowCount() : 0);
                if (count == 0) {
                    isWorkSheetComplete = true;
                }
            }
            tempStatus = (bypassWSCompletionCheck || isWorkSheetComplete) && this.isCompleted(sdidatas, DSSTATUSCOLUMN) && this.isMandatorySDIWorkItemRelationCompleted(wiSdcid, wikeyid1, wikeyid2, wikeyid3, workitemid, workiteminstance) && this.isMandatorySDIWorkItemAttributeCompleted(wiSdcid, wikeyid1, wikeyid2, wikeyid3, workitemid, workiteminstance) && !this.isDeferDataSetCreationExists(wiSdcid, wikeyid1, wikeyid2, wikeyid3, workitemid, workiteminstance) ? "Completed" : (this.isInitial(sdidatas, DSSTATUSCOLUMN) ? "Initial" : (this.isReleased(sdidatas, DSSTATUSCOLUMN) ? "Released" : (this.isDataEntered(sdidatas, DSSTATUSCOLUMN) ? "DataEntered" : "InProgress")));
            boolean isWiApplied = false;
            if (this.isGrpWiApplied(tempStatus, dsSWI, dsGrpWorkItems)) {
                isWiApplied = true;
            }
            if (sdiwiStatus.equals(tempStatus) && !isWiApplied) continue;
            int r = modifiedDS.addRow();
            modifiedDS.setString(r, "sdcid", wiSdcid);
            modifiedDS.setString(r, "keyid1", wikeyid1);
            modifiedDS.setString(r, "keyid2", wikeyid2);
            modifiedDS.setString(r, "keyid3", wikeyid3);
            modifiedDS.setString(r, "workitemid", workitemid);
            modifiedDS.setNumber(r, "workiteminstance", new Integer(workiteminstance));
            modifiedDS.setString(r, SDIWORKITEMSTATUSCOLUMN, tempStatus);
            modifiedDS.setString(r, "groupid", dsSWI.getString(0, "groupid"));
            modifiedDS.setNumber(r, "groupinstance", dsSWI.getInt(0, "groupinstance"));
            modifiedDS.setString(r, "workitemtypeflag", dsSWI.getString(0, "workitemtypeflag"));
        }
        for (i = 0; i < dsGrpWorkItems.getRowCount(); ++i) {
            HashMap<String, Object> filter = new HashMap<String, Object>();
            filter.put("sdcid", dsGrpWorkItems.getValue(i, "sdcid"));
            filter.put("keyid1", dsGrpWorkItems.getValue(i, "keyid1"));
            filter.put("keyid2", dsGrpWorkItems.getValue(i, "keyid2"));
            filter.put("keyid3", dsGrpWorkItems.getValue(i, "keyid3"));
            filter.put("workitemid", dsGrpWorkItems.getValue(i, "workitemid"));
            filter.put("workiteminstance", new BigDecimal(dsGrpWorkItems.getValue(i, "workiteminstance")));
            int findRow = ds.findRow(filter);
            if (findRow >= 0) continue;
            int r = modifiedDS.addRow();
            modifiedDS.setString(r, "sdcid", dsGrpWorkItems.getValue(i, "sdcid"));
            modifiedDS.setString(r, "keyid1", dsGrpWorkItems.getValue(i, "keyid1"));
            modifiedDS.setString(r, "keyid2", dsGrpWorkItems.getValue(i, "keyid2"));
            modifiedDS.setString(r, "keyid3", dsGrpWorkItems.getValue(i, "keyid3"));
            modifiedDS.setString(r, "workitemid", dsGrpWorkItems.getValue(i, "workitemid"));
            modifiedDS.setNumber(r, "workiteminstance", dsGrpWorkItems.getInt(i, "workiteminstance"));
            modifiedDS.setString(r, SDIWORKITEMSTATUSCOLUMN, dsGrpWorkItems.getString(i, SDIWORKITEMSTATUSCOLUMN));
            modifiedDS.setString(r, "groupid", dsGrpWorkItems.getString(i, "groupid"));
            modifiedDS.setNumber(r, "groupinstance", dsGrpWorkItems.getInt(i, "groupinstance"));
            modifiedDS.setString(r, "workitemtypeflag", dsGrpWorkItems.getString(i, "workitemtypeflag"));
        }
        this.getDAMProcessor().clearRSet(rsetId);
        return modifiedDS;
    }

    private boolean isCompleted(DataSet ds, String columnId) {
        int mandatoryComplete = 0;
        int nonMandatoryComplete = 0;
        int mandatory = 0;
        int cancelled = 0;
        for (int i = 0; i < ds.getRowCount(); ++i) {
            String dsStatus = ds.getString(i, columnId, "");
            String mandatoryWorkItemItem = ds.getString(i, "mandatoryflag", "N");
            String availableWorkItemItem = ds.getString(i, "availabilityflag", "");
            if ("Cancelled".equals(dsStatus)) {
                ++cancelled;
                continue;
            }
            if ("Y".equalsIgnoreCase(mandatoryWorkItemItem)) {
                ++mandatory;
                if (!"Completed".equals(dsStatus)) continue;
                ++mandatoryComplete;
                continue;
            }
            if ("S".equalsIgnoreCase(mandatoryWorkItemItem)) {
                if (!"Y".equalsIgnoreCase(availableWorkItemItem)) continue;
                ++mandatory;
                if (!"Completed".equals(dsStatus)) continue;
                ++mandatoryComplete;
                continue;
            }
            if (!"Completed".equals(dsStatus)) continue;
            ++nonMandatoryComplete;
        }
        boolean completed = false;
        if (ds.getRowCount() == cancelled) {
            completed = true;
        } else if (mandatory > 0 && mandatoryComplete == mandatory) {
            completed = true;
        } else if (mandatory == 0 && nonMandatoryComplete > 0) {
            completed = true;
        }
        if (completed) {
            StringBuilder sql = new StringBuilder();
            SafeSQL safeSQL = new SafeSQL();
            sql.append("select s_sampleid, sourcesdiworkitemid, samplestatus, storagestatus, sdiworkitemcompletionstatus");
            sql.append(" from s_sample");
            sql.append(" where sourcesdiworkitemid in (").append(safeSQL.addIn(ds.getColumnValues("sdiworkitemid", "','"))).append(")");
            sql.append(" and samplestatus not in ('Disposed', 'Cancelled')");
            DataSet wids = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            if (wids != null) {
                for (int i = 0; i < wids.size(); ++i) {
                    String sdiworkitemcompletionstatus = wids.getString(i, "sdiworkitemcompletionstatus", "");
                    if (!OpalUtil.isNotEmpty(sdiworkitemcompletionstatus) || sdiworkitemcompletionstatus.equals(wids.getString(i, "samplestatus", "")) || sdiworkitemcompletionstatus.equals(wids.getString(i, "storagestatus", ""))) continue;
                    completed = false;
                    break;
                }
            }
        }
        return completed;
    }

    private boolean isDeferDataSetCreationExists(String sdcid, String keyid1, String keyid2, String keyid3, String workitemid, String workiteminstance) throws SapphireException {
        StringBuffer sql = new StringBuffer("SELECT todolistid, actionid, propertyclob ").append(" FROM  todolist WHERE statusflag in( 'W', 'S' ) AND actionid = 'AddDataSet' AND ").append(this.database.isSqlServer() ? "CHARINDEX(?, propertyclob, 1 )" : "INSTR( propertyclob, ?, 1)").append(">0");
        String search_keyid1 = "keyid1=" + keyid1;
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("checkdefercreation", sql.toString(), new String[]{search_keyid1}, true);
        boolean exists = false;
        for (int i = 0; i < ds.getRowCount(); ++i) {
            PropertyList propertyList = new PropertyList();
            PropertyListUtil.setDelimiteredProps(propertyList, ds.getClob(i, "propertyclob"));
            boolean bl = exists = sdcid.equals(propertyList.getProperty("sdcid")) && keyid1.equals(propertyList.getProperty("keyid1")) && keyid2.equals(propertyList.getProperty("keyid2")) && keyid3.equals(propertyList.getProperty("keyid3")) && workitemid.equals(propertyList.getProperty("sourceworkitemid")) && workiteminstance.equals(propertyList.getProperty("sourceworkiteminstance"));
            if (exists) break;
        }
        return exists;
    }

    private boolean isDeferMemberWorkItemCreationExists(String sdcid, String keyid1, String keyid2, String keyid3, String groupworkitemid, String groupworkiteminstance) throws SapphireException {
        StringBuffer sql = new StringBuffer("SELECT todolistid, actionid, propertyclob ").append(" FROM  todolist WHERE statusflag in ( 'W', 'S' ) AND actionid in ('AddSDIWorkItem','AddDataSet') AND ").append(this.database.isSqlServer() ? "CHARINDEX(?, propertyclob, 1 )" : "INSTR( propertyclob, ?, 1)").append(">0");
        String search_keyid1 = "keyid1=" + keyid1;
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("checkdefercreation", sql.toString(), new String[]{search_keyid1}, true);
        boolean exists = false;
        for (int i = 0; i < ds.getRowCount(); ++i) {
            PropertyList propertyList = new PropertyList();
            PropertyListUtil.setDelimiteredProps(propertyList, ds.getClob(i, "propertyclob"));
            String actionId = ds.getValue(i, "actionid");
            if ("AddDataSet".equals(actionId)) {
                exists = sdcid.equals(propertyList.getProperty("sdcid")) && keyid1.equals(propertyList.getProperty("keyid1")) && keyid2.equals(propertyList.getProperty("keyid2")) && keyid3.equals(propertyList.getProperty("keyid3")) && groupworkitemid.equals(propertyList.getProperty("sourceworkitemid")) && groupworkiteminstance.equals(propertyList.getProperty("sourceworkiteminstance"));
            } else {
                boolean bl = exists = sdcid.equals(propertyList.getProperty("sdcid")) && keyid1.equals(propertyList.getProperty("keyid1")) && keyid2.equals(propertyList.getProperty("keyid2")) && keyid3.equals(propertyList.getProperty("keyid3")) && groupworkitemid.equals(propertyList.getProperty("groupid")) && groupworkiteminstance.equals(propertyList.getProperty("groupinstance"));
            }
            if (exists) break;
        }
        return exists;
    }

    private boolean isMandatorySDIWorkItemRelationCompleted(String sdcid, String keyid1, String keyid2, String keyid3, String workitemid, String workiteminstance) throws SapphireException {
        this.database.createPreparedResultSet("checkmandatorysdiworkitemrelation", "select 1 from sdiworkitemrelation where sdcid = ? and keyid1 = ? and keyid2 = ? and keyid3 = ? and workitemid = ? and workiteminstance = ?  and mandatoryflag = 'Y' and ( tokeyid1 is null or tokeyid1 = '')", new String[]{sdcid, keyid1, keyid2, keyid3, workitemid, workiteminstance});
        DataSet ds = new DataSet(this.database.getResultSet("checkmandatorysdiworkitemrelation"));
        return ds.getRowCount() == 0;
    }

    private boolean isGrpWiApplied(String tempStatus, DataSet dsSWI, DataSet dsGrpWorkItems) throws SapphireException {
        if (tempStatus.equals("Initial") && this.isGrpWiCompleted(dsSWI, dsGrpWorkItems)) {
            boolean sdiavailabilityflag = "Y".equalsIgnoreCase(dsSWI.getString(0, "availabilityflag", "N"));
            boolean sdiappliedflag = "Y".equalsIgnoreCase(dsSWI.getString(0, "appliedflag", "N"));
            if (sdiavailabilityflag && sdiappliedflag) {
                return true;
            }
        }
        return false;
    }

    private boolean isApplied(boolean sdiavailabilityflag, boolean sdiappliedflag) throws SapphireException {
        return sdiavailabilityflag && sdiappliedflag;
    }

    private boolean isGrpWiCompleted(DataSet dsSWI, DataSet ds) throws SapphireException {
        if (OpalUtil.isNotEmpty(dsSWI.getString(0, "groupid")) && OpalUtil.isNotEmpty(dsSWI.getValue(0, "groupinstance"))) {
            HashMap<String, Object> filter = new HashMap<String, Object>();
            filter.put("sdcid", dsSWI.getValue(0, "sdcid"));
            filter.put("keyid1", dsSWI.getValue(0, "keyid1"));
            filter.put("keyid2", dsSWI.getValue(0, "keyid2"));
            filter.put("keyid3", dsSWI.getValue(0, "keyid3"));
            filter.put("workitemid", dsSWI.getString(0, "groupid"));
            filter.put("workiteminstance", new BigDecimal(dsSWI.getValue(0, "groupinstance")));
            DataSet grpWi = ds.getFilteredDataSet(filter);
            return grpWi.size() == 1 && grpWi.getString(0, SDIWORKITEMSTATUSCOLUMN, "").equalsIgnoreCase("Completed");
        }
        return false;
    }

    private boolean isSampleCompleted(String sdcid, String keyid1) throws SapphireException {
        if (sdcid.equals("Sample")) {
            String sampleStatus;
            StringBuilder sql = new StringBuilder();
            SafeSQL safeSQL = new SafeSQL();
            sql.append("select samplestatus");
            sql.append(" from s_sample");
            sql.append(" where s_sampleid =" + safeSQL.addVar(keyid1));
            DataSet wss = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            if (wss != null && (sampleStatus = wss.getString(0, "samplestatus", "")).equals("Completed")) {
                return true;
            }
        }
        return false;
    }

    private boolean isMandatorySDIWorkItemAttributeCompleted(String sdcid, String keyid1, String keyid2, String keyid3, String workitemid, String workiteminstance) throws SapphireException {
        this.database.createPreparedResultSet("checkmandatorysdiattribute", "select a.* from sdiattribute a, sdiworkitem w where w.sdcid = ? and w.keyid1 = ? and w.keyid2 = ? and w.keyid3 = ? and w.workitemid = ? and w.workiteminstance = ?  and a.sdcid = 'SDIWorkItem' and a.keyid1 = w.sdiworkitemid and a.mandatoryflag = 'Y' ", new String[]{sdcid, keyid1, keyid2, keyid3, workitemid, workiteminstance});
        DataSet ds_mandatoryAttributes = new DataSet();
        ds_mandatoryAttributes.setResultSet(this.database.getResultSet("checkmandatorysdiattribute"), true, this.connectionInfo.getDbms());
        DataSet dsIncompleteAttribute = new DataSet();
        for (int i = 0; i < ds_mandatoryAttributes.getRowCount(); ++i) {
            String dataType = ds_mandatoryAttributes.getValue(i, "datatype");
            String attributeColumn = "";
            if ("D".equals(dataType) || "O".equals(dataType)) {
                attributeColumn = "datevalue";
            } else if ("S".equals(dataType)) {
                attributeColumn = "textvalue";
            } else if ("C".equals(dataType)) {
                attributeColumn = "clobvalue";
            } else if ("N".equals(dataType)) {
                attributeColumn = "numericvalue";
            }
            String attributeValue = ds_mandatoryAttributes.getValue(i, attributeColumn);
            if (attributeValue.length() > 0) continue;
            dsIncompleteAttribute.copyRow(ds_mandatoryAttributes, i, 1);
        }
        return dsIncompleteAttribute.getRowCount() == 0;
    }

    private boolean isCompletedMemberWorkItems(DataSet ds, String columnId, boolean mandatoryDirectDSExists) {
        int mandatoryComplete = 0;
        int nonMandatoryComplete = 0;
        int mandatory = 0;
        int mandatoryCancelled = 0;
        int nonMandatory = 0;
        String directDSStatus = "";
        int cancelled = 0;
        for (int i = 0; i < ds.getRowCount(); ++i) {
            String dsStatus = ds.getString(i, columnId, "");
            String workitemTypeFlag = ds.getString(i, "workitemtypeflag", "");
            String appliedFlag = ds.getString(i, "appliedflag", "");
            if ("P".equals(workitemTypeFlag)) {
                directDSStatus = ds.getString(i, columnId, "");
                continue;
            }
            String mandatoryTest = ds.getString(i, "mandatoryflag", "N");
            if ("Y".equalsIgnoreCase(mandatoryTest)) {
                ++mandatory;
                if ("Cancelled".equals(dsStatus)) {
                    ++cancelled;
                    ++mandatoryCancelled;
                    continue;
                }
                if (!"Completed".equals(dsStatus)) continue;
                ++mandatoryComplete;
                continue;
            }
            if ("S".equalsIgnoreCase(mandatoryTest)) {
                if (!"Y".equalsIgnoreCase(appliedFlag)) continue;
                ++mandatory;
                if ("Cancelled".equals(dsStatus)) {
                    ++cancelled;
                    ++mandatoryCancelled;
                    continue;
                }
                if (!"Completed".equals(dsStatus)) continue;
                ++mandatoryComplete;
                continue;
            }
            ++nonMandatory;
            if ("Cancelled".equals(dsStatus)) {
                ++cancelled;
                continue;
            }
            if (!"Completed".equals(dsStatus)) continue;
            ++nonMandatoryComplete;
        }
        boolean directDSCompleted = "Completed".equals(directDSStatus);
        if ((mandatory == 0 || mandatoryCancelled == mandatory) && directDSCompleted) {
            return true;
        }
        if ((directDSCompleted || !mandatoryDirectDSExists) && mandatory > 0 && mandatoryComplete > 0 && mandatoryComplete + mandatoryCancelled == mandatory) {
            return true;
        }
        return !mandatoryDirectDSExists && (mandatory == 0 || mandatory == mandatoryCancelled) && nonMandatoryComplete > 0;
    }

    private boolean isInitial(DataSet ds, String columnId) {
        for (int i = 0; i < ds.getRowCount(); ++i) {
            String dsStatus = ds.getString(i, columnId, "");
            if ("Initial".equals(dsStatus) || "Cancelled".equals(dsStatus)) continue;
            return false;
        }
        return true;
    }

    private boolean isReleased(DataSet ds, String columnId) {
        for (int i = 0; i < ds.getRowCount(); ++i) {
            String dsStatus = ds.getString(i, columnId, "");
            if ("Completed".equals(dsStatus) || "Cancelled".equals(dsStatus) || "Released".equals(dsStatus)) continue;
            return false;
        }
        return true;
    }

    private boolean isDataEntered(DataSet ds, String columnId) {
        for (int i = 0; i < ds.getRowCount(); ++i) {
            String dsStatus = ds.getString(i, columnId, "");
            if ("Completed".equals(dsStatus) || "Cancelled".equals(dsStatus) || "Released".equals(dsStatus) || "DataEntered".equals(dsStatus)) continue;
            return false;
        }
        return true;
    }

    private void syncSDIWorkItemGroupStatus(DataSet sdiWorkItemGrps, PropertyList properties) throws SapphireException {
        sdiWorkItemGrps.sort("sdcid,keyid1,keyid2,keyid3,groupid,groupinstance");
        ArrayList<DataSet> groupedDatas = sdiWorkItemGrps.getGroupedDataSets("sdcid,keyid1,keyid2,keyid3,groupid,groupinstance");
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT swi.sdcid, swi.keyid1, swi.keyid2, swi.keyid3, swi.appliedflag, swi.workitemid, swi.workiteminstance, swi.workitemstatus, swi.workitemtypeflag, swii.mandatoryflag ").append(" FROM sdiworkitem swi LEFT OUTER JOIN sdiworkitemitem swii ").append(" ON swi.sdcid = swii.sdcid AND swi.keyid1 = swii.keyid1 AND swi.keyid2 = swii.keyid2 AND swi.keyid3 = swii.keyid3 AND swi.groupid = swii.workitemid").append(" AND swi.groupinstance = swii.workiteminstance  and swii.itemsdcid = 'WorkItem' and swi.workitemid = swii.itemkeyid1 and swi.workitemversionid = swii.itemkeyid2 ").append(" WHERE swi.sdcid = ? AND swi.keyid1 = ? AND swi.keyid2 = ? AND swi.keyid3 = ? AND  swi.groupid = ? AND swi.groupinstance = ? ");
        PreparedStatement getMembersPsmt = this.database.prepareStatement("getmembers", sql.toString());
        sql.setLength(0);
        sql.append("SELECT ds.s_datasetstatus, swii.mandatoryflag FROM sdiworkitem swi, sdiworkitemitem swii, sdidata ds ").append(" where ds.sdcid = swii.sdcid").append(" and ds.keyid1 = swii.keyid1").append(" and ds.keyid2 = swii.keyid2").append(" and ds.keyid3 = swii.keyid3").append(" and ds.paramlistid = swii.itemkeyid1").append(" and ds.paramlistversionid = swii.itemkeyid2").append(" and ds.variantid = swii.itemkeyid3").append(" and ds.dataset = swii.iteminstance").append(" and ds.sourceworkitemid = swii.workitemid").append(" and ds.sourceworkiteminstance = swii.workiteminstance").append(" and swii.sdcid = swi.sdcid and swii.keyid1 = swi.keyid1 and swii.keyid2 = swi.keyid2 and swii.keyid3 = swi.keyid3").append(" and swii.workitemid = swi.workitemid and swii.workiteminstance = swi.workiteminstance ").append(" and swi.sdcid = ? and swi.keyid1 = ? and swi.keyid2 = ? and swi.keyid3 = ? and swi.workitemid = ? and swi.workiteminstance = ?");
        PreparedStatement directDSPsmt = this.database.prepareStatement("getdirectds", sql.toString());
        try {
            DataSet editSDIWorkItems = new DataSet();
            for (DataSet ds : groupedDatas) {
                getMembersPsmt.setString(1, ds.getValue(0, "sdcid"));
                getMembersPsmt.setString(2, ds.getValue(0, "keyid1"));
                getMembersPsmt.setString(3, ds.getValue(0, "keyid2", "(null)"));
                getMembersPsmt.setString(4, ds.getValue(0, "keyid3", "(null)"));
                getMembersPsmt.setString(5, ds.getValue(0, "groupid"));
                getMembersPsmt.setString(6, ds.getValue(0, "groupinstance"));
                DataSet dsMembers = new DataSet(getMembersPsmt.executeQuery());
                String tempGroupStatus = "Initial";
                String currentGroupStatus = "";
                int findRow = dsMembers.findRow("workitemtypeflag", "P");
                DataSet groupCopy = new DataSet();
                if (findRow <= -1) continue;
                currentGroupStatus = dsMembers.getValue(findRow, SDIWORKITEMSTATUSCOLUMN);
                groupCopy.copyRow(dsMembers, findRow, 1);
                directDSPsmt.setString(1, dsMembers.getValue(findRow, "sdcid"));
                directDSPsmt.setString(2, dsMembers.getValue(findRow, "keyid1"));
                directDSPsmt.setString(3, dsMembers.getValue(findRow, "keyid2"));
                directDSPsmt.setString(4, dsMembers.getValue(findRow, "keyid3"));
                directDSPsmt.setString(5, dsMembers.getValue(findRow, "workitemid"));
                directDSPsmt.setString(6, dsMembers.getValue(findRow, "workiteminstance"));
                DataSet directDSStatus = new DataSet(directDSPsmt.executeQuery());
                boolean mandatoryDirectDSExists = false;
                if (directDSStatus.getRowCount() > 0) {
                    String dsStatus = "";
                    mandatoryDirectDSExists = this.existsMandatoryIncomplete(directDSStatus);
                    dsStatus = this.isCompleted(directDSStatus, DSSTATUSCOLUMN) ? "Completed" : (this.isInitial(directDSStatus, DSSTATUSCOLUMN) ? "Initial" : (this.isReleased(directDSStatus, DSSTATUSCOLUMN) ? "Released" : (this.isDataEntered(directDSStatus, DSSTATUSCOLUMN) ? "DataEntered" : "InProgress")));
                    dsMembers.setString(findRow, SDIWORKITEMSTATUSCOLUMN, dsStatus);
                } else {
                    dsMembers.deleteRow(findRow);
                }
                if (currentGroupStatus.equals(tempGroupStatus = this.isCompletedMemberWorkItems(dsMembers, SDIWORKITEMSTATUSCOLUMN, mandatoryDirectDSExists) && !this.isDeferMemberWorkItemCreationExists(ds.getValue(0, "sdcid"), ds.getValue(0, "keyid1"), ds.getValue(0, "keyid2", "(null)"), ds.getValue(0, "keyid3", "(null)"), ds.getValue(0, "groupid"), ds.getValue(0, "groupinstance")) ? "Completed" : (this.isInitial(dsMembers, SDIWORKITEMSTATUSCOLUMN) ? "Initial" : (this.isReleased(dsMembers, SDIWORKITEMSTATUSCOLUMN) ? "Released" : (this.isDataEntered(dsMembers, SDIWORKITEMSTATUSCOLUMN) ? "DataEntered" : "InProgress")))) || groupCopy.getRowCount() != 1) continue;
                groupCopy.setString(0, SDIWORKITEMSTATUSCOLUMN, tempGroupStatus);
                editSDIWorkItems.copyRow(groupCopy, 0, 1);
            }
            if (editSDIWorkItems.getRowCount() > 0) {
                PropertyList props = new PropertyList();
                props.setProperty("sdcid", editSDIWorkItems.getValue(0, "sdcid"));
                props.setProperty("keyid1", editSDIWorkItems.getColumnValues("keyid1", ";"));
                props.setProperty("keyid2", editSDIWorkItems.getColumnValues("keyid2", ";"));
                props.setProperty("keyid3", editSDIWorkItems.getColumnValues("keyid3", ";"));
                props.setProperty("workitemid", editSDIWorkItems.getColumnValues("workitemid", ";"));
                props.setProperty("workiteminstance", editSDIWorkItems.getColumnValues("workiteminstance", ";"));
                props.setProperty(SDIWORKITEMSTATUSCOLUMN, editSDIWorkItems.getColumnValues(SDIWORKITEMSTATUSCOLUMN, ";"));
                props.setProperty("propsmatch", "Y");
                props.setProperty("auditactivity", properties.getProperty("auditactivity"));
                props.setProperty("auditreason", properties.getProperty("auditreason"));
                props.setProperty("auditsignedflag", properties.getProperty("auditsignedflag"));
                this.getActionProcessor().processAction("EditSDIWorkItem", "1", props);
            }
        }
        catch (Exception e) {
            throw new SapphireException(e);
        }
        this.database.closeStatement("getmembers");
        this.database.closeStatement("getdirectds");
    }

    private boolean existsMandatoryIncomplete(DataSet ds) {
        for (int i = 0; i < ds.getRowCount(); ++i) {
            String dsStatus = ds.getValue(i, DSSTATUSCOLUMN);
            boolean mandatoryflag = "Y".equalsIgnoreCase(ds.getString(i, "mandatoryflag", "N"));
            if (!mandatoryflag || "Cancelled".equals(dsStatus) || "Completed".equals(dsStatus)) continue;
            return true;
        }
        return false;
    }
}

