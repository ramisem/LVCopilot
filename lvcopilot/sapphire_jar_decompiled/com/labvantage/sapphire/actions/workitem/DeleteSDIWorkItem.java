/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.workitem;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.opal.validation.data.ValidateDataEntered;
import com.labvantage.sapphire.SDI;
import com.labvantage.sapphire.actions.sdi.AddSDITraceLog;
import com.labvantage.sapphire.actions.sdi.DeleteSDI;
import com.labvantage.sapphire.actions.sdidata.BaseSDIDataAction;
import com.labvantage.sapphire.actions.wap.RemoveActivityWorkSDI;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.DAMProcessor;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class DeleteSDIWorkItem
extends BaseAction
implements sapphire.action.DeleteSDIWorkItem {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        boolean rc = true;
        boolean deleterset = false;
        String rsetid = properties.getProperty("rsetid");
        String sdcid = properties.getProperty("sdcid");
        String separator = properties.getProperty("separator", ";");
        DAMProcessor damProcessor = this.getDAMProcessor();
        DataSet deleteSDIWI = new DataSet();
        DataSet deleteActivityWorkSDIs = new DataSet();
        if (rsetid.length() == 0) {
            boolean applylock = properties.getProperty("applylock").equals("Y");
            rsetid = BaseSDIDataAction.createRSet(sdcid, properties.getProperty("keyid1"), properties.getProperty("keyid2"), properties.getProperty("keyid3"), this.database, this.connectionInfo, applylock);
            deleterset = true;
        }
        if (rsetid != null && rsetid.length() > 0) {
            StringBuffer deleteWorkitem = new StringBuffer("DELETE FROM sdiworkitem WHERE sdcid = ? AND keyid1 = ? AND ");
            deleteWorkitem.append("keyid2 = ? AND keyid3 = ? AND workitemid = ? AND workiteminstance = ?");
            StringBuffer deleteTaskQueueWorkitem = new StringBuffer("DELETE FROM taskqueue WHERE queuesdcid = 'SDIWorkItem' AND queuekeyid1 IN ");
            deleteTaskQueueWorkitem.append("(SELECT sdiworkitemid FROM sdiworkitem WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ? AND keyid3 = ? AND workitemid = ? AND workiteminstance = ?)");
            StringBuffer deleteWorkitemitem = new StringBuffer("DELETE FROM sdiworkitemitem WHERE sdcid = ? AND keyid1 = ? AND ");
            deleteWorkitemitem.append("keyid2 = ? AND keyid3 = ? AND workitemid = ? AND workiteminstance = ?");
            StringBuffer deleteSDIWorkitemRelation = new StringBuffer("DELETE FROM sdiworkitemrelation WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ? AND keyid3 = ? AND workitemid = ? AND workiteminstance = ?");
            StringBuffer selectActivityWorkSDI = new StringBuffer("select * from activityworksdi WHERE worksdcid = 'SDIWorkItem' AND workkeyid1 = ").append("(SELECT sdiworkitemid FROM sdiworkitem WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ? AND keyid3 = ? AND workitemid = ? AND workiteminstance = ?)");
            StringBuffer childWorkItem = new StringBuffer("SELECT c.workitemid, c.workiteminstance, c.workitemtypeflag FROM ").append("sdiworkitem wi INNER JOIN sdiworkitem c ").append(" ON wi.sdcid = c.sdcid AND wi.keyid1 = c.keyid1 AND wi.keyid2 = c.keyid2 AND wi.keyid3 = c.keyid3").append(" AND wi.groupid = c.groupid AND wi.groupinstance    = c.groupinstance").append(" WHERE ").append(" wi.sdcid = ? AND wi.keyid1 = ? AND ").append("wi.keyid2 = ? AND wi.keyid3 = ? AND wi.workitemid = ? AND wi.workiteminstance = ? AND wi.workitemtypeflag = 'P'");
            boolean cascadedeletes = StringUtil.getYN(properties.getProperty("cascadedeletes"), "N").equals("Y");
            String forcedelete = StringUtil.getYN(properties.getProperty("forcedelete"), "Y");
            PreparedStatement psWorkitem = this.database.prepareStatement("deleteworkitem", deleteWorkitem.toString());
            PreparedStatement psTaskQueueWorkitem = this.database.prepareStatement("deletetaskqueueworkitem", deleteTaskQueueWorkitem.toString());
            PreparedStatement psActivityWorkSDI = this.database.prepareStatement("selectactivityworksdis", selectActivityWorkSDI.toString());
            PreparedStatement psWorkitemitem = this.database.prepareStatement("deleteworkitemitem", deleteWorkitemitem.toString());
            PreparedStatement psChildWorkitem = this.database.prepareStatement("childWorkItem", childWorkItem.toString());
            PreparedStatement psDeleteSDIWorkitemRelation = this.database.prepareStatement("deletesdiworkitemrelation", deleteSDIWorkitemRelation.toString());
            StringBuffer parentWorkItem = new StringBuffer("SELECT p.workitemid, p.workiteminstance, wi.workitemversionid FROM sdiworkitem p, sdiworkitem wi ").append(" WHERE p.sdcid = wi.sdcid AND p.keyid1 = wi.keyid1 AND p.keyid2 = wi.keyid2 AND p.keyid3 = wi.keyid3").append(" AND p.groupid = wi.groupid AND p.groupinstance  = wi.groupinstance AND p.workitemtypeflag = 'P'").append(" AND  wi.sdcid = ? AND wi.keyid1 = ? AND wi.keyid2 = ? AND wi.keyid3 = ? AND wi.workitemid = ? AND wi.workiteminstance = ? ");
            PreparedStatement psParentWorkitem = this.database.prepareStatement("parentWorkItem", parentWorkItem.toString());
            StringBuffer deleteParentWorkitemitem = new StringBuffer("DELETE FROM sdiworkitemitem WHERE sdcid = ? AND keyid1 = ? AND ");
            deleteParentWorkitemitem.append("keyid2 = ? AND keyid3 = ? AND workitemid = ? AND workiteminstance = ? and itemsdcid ='WorkItem' and itemkeyid1 = ? and iteminstance = ?");
            PreparedStatement psDeleteParentWorkitemItem = this.database.prepareStatement("deleteParentWorkItemItem", deleteParentWorkitemitem.toString());
            DataSet dataset = new DataSet();
            dataset.addColumnValues("keyid1", 0, properties.getProperty("keyid1"), separator);
            dataset.addColumnValues("keyid2", 0, properties.getProperty("keyid2"), separator, "(null)");
            dataset.addColumnValues("keyid3", 0, properties.getProperty("keyid3"), separator, "(null)");
            dataset.addColumnValues("workitemid", 0, properties.getProperty("workitemid"), separator);
            dataset.addColumnValues("workiteminstance", 0, properties.getProperty("workiteminstance"), separator, "1");
            dataset.padColumns();
            String reason = properties.getProperty("auditreason");
            String activity = properties.getProperty("auditactivity");
            String signedFlag = properties.getProperty("auditsignedflag");
            String auditdt = properties.getProperty("auditdt");
            String tracelogid = null;
            if (properties.getProperty("tracelogid", "").trim().length() == 0) {
                if (reason.length() > 0) {
                    this.logger.info("Generate the tracelog record");
                    PropertyList tracelogprops = new PropertyList();
                    tracelogprops.setProperty("sdcid", sdcid);
                    tracelogprops.setProperty("keyid1", properties.getProperty("keyid1"));
                    tracelogprops.setProperty("keyid2", properties.getProperty("keyid2"));
                    tracelogprops.setProperty("keyid3", properties.getProperty("keyid3"));
                    tracelogprops.setProperty("description", "Deleted workitems");
                    tracelogprops.setProperty("auditreason", reason);
                    tracelogprops.setProperty("auditactivity", activity);
                    tracelogprops.setProperty("auditsignedflag", signedFlag);
                    tracelogprops.setProperty("auditdt", auditdt);
                    ActionProcessor ac = this.getActionProcessor();
                    try {
                        ac.processActionClass(AddSDITraceLog.class.getName(), tracelogprops);
                        tracelogid = tracelogprops.getProperty("tracelogid");
                        properties.setProperty("tracelogid", tracelogid);
                    }
                    catch (Exception e) {
                        if (deleterset) {
                            damProcessor.clearRSet(rsetid);
                        }
                        throw new SapphireException("PROCESSACTION_FAILED", "Error calling AddSDITracelog action. Exception:", e);
                    }
                }
            } else {
                tracelogid = properties.getProperty("tracelogid");
            }
            SafeSQL safeSQL = new SafeSQL();
            StringBuffer allWorkItemItemsql = new StringBuffer("SELECT swii.sdcid, swii.keyid1, swii.keyid2, swii.keyid3, workitemid, workiteminstance").append(", itemsdcid, itemkeyid1, itemkeyid2, itemkeyid3, iteminstance ").append(" FROM sdiworkitemitem swii, rsetitems r").append(" WHERE swii.sdcid=").append(safeSQL.addVar(sdcid)).append(" AND swii.keyid1=r.keyid1 AND swii.keyid2=r.keyid2 AND swii.keyid3=r.keyid3").append(" AND r.rsetid=").append(safeSQL.addVar(rsetid));
            DataSet allWorkItemItems = this.getQueryProcessor().getPreparedSqlDataSet(allWorkItemItemsql.toString(), safeSQL.getValues());
            for (int i = 0; i < dataset.size(); ++i) {
                String keyid1 = dataset.getValue(i, "keyid1");
                String keyid2 = dataset.getValue(i, "keyid2");
                String keyid3 = dataset.getValue(i, "keyid3");
                String workitemid = dataset.getValue(i, "workitemid");
                String workiteminstance = dataset.getValue(i, "workiteminstance");
                try {
                    psParentWorkitem.setString(1, sdcid);
                    psParentWorkitem.setString(2, keyid1);
                    psParentWorkitem.setString(3, keyid2);
                    psParentWorkitem.setString(4, keyid3);
                    psParentWorkitem.setString(5, workitemid);
                    psParentWorkitem.setString(6, workiteminstance);
                    DataSet parentWI = new DataSet(psParentWorkitem.executeQuery());
                    this.logger.info("Getting child sdiworkitem record of {" + sdcid + ";" + keyid1 + ";" + keyid2 + ";" + keyid3 + ";" + workitemid + ";" + workiteminstance + "}");
                    psChildWorkitem.setString(1, sdcid);
                    psChildWorkitem.setString(2, keyid1);
                    psChildWorkitem.setString(3, keyid2);
                    psChildWorkitem.setString(4, keyid3);
                    psChildWorkitem.setString(5, workitemid);
                    psChildWorkitem.setInt(6, Integer.parseInt(workiteminstance));
                    DataSet childds = new DataSet(psChildWorkitem.executeQuery());
                    for (int j = 0; j < childds.size(); ++j) {
                        HashMap<String, String> filterMap = new HashMap<String, String>();
                        filterMap.put("keyid1", keyid1);
                        filterMap.put("keyid2", keyid2);
                        filterMap.put("keyid3", keyid3);
                        filterMap.put("workitemid", childds.getString(j, "workitemid"));
                        filterMap.put("workiteminstance", childds.getValue(j, "workiteminstance"));
                        int index = dataset.findRow(filterMap);
                        if (index != -1) continue;
                        int childRow = dataset.size();
                        dataset.addRow();
                        dataset.setValue(childRow, "keyid1", keyid1);
                        dataset.setValue(childRow, "keyid2", keyid2);
                        dataset.setValue(childRow, "keyid3", keyid3);
                        dataset.setValue(childRow, "workitemid", childds.getString(j, "workitemid"));
                        dataset.setValue(childRow, "workiteminstance", childds.getValue(j, "workiteminstance"));
                    }
                    if (cascadedeletes) {
                        if ("Y".equals(StringUtil.getYN(properties.getProperty("validatedataentered", "N"), "N"))) {
                            SDI sdi = new SDI(sdcid, keyid1, keyid2, keyid3);
                            if (ValidateDataEntered.isWorkItemInProgress(this.getQueryProcessor(), sdi, workitemid, workiteminstance)) {
                                throw new SapphireException("PROCESSACTION_FAILED", this.getTranslationProcessor().translate("Atleast one of the Tests are in progress. Unable to Delete."));
                            }
                        }
                        HashMap<String, Object> filterMap = new HashMap<String, Object>();
                        filterMap.put("sdcid", sdcid);
                        filterMap.put("keyid1", keyid1);
                        filterMap.put("keyid2", keyid2);
                        filterMap.put("keyid3", keyid3);
                        filterMap.put("workitemid", workitemid);
                        filterMap.put("workiteminstance", new BigDecimal(workiteminstance));
                        DataSet workItemItems = allWorkItemItems.getFilteredDataSet(filterMap);
                        for (int workItemItemCount = 0; workItemItemCount < workItemItems.getRowCount(); ++workItemItemCount) {
                            String itemsdcid = workItemItems.getString(workItemItemCount, "itemsdcid");
                            String itemkeyid1 = workItemItems.getString(workItemItemCount, "itemkeyid1");
                            String itemkeyid2 = workItemItems.getString(workItemItemCount, "itemkeyid2");
                            String itemkeyid3 = workItemItems.getString(workItemItemCount, "itemkeyid3");
                            BigDecimal itemInstanceBD = workItemItems.getBigDecimal(workItemItemCount, "iteminstance");
                            if (itemInstanceBD == null) continue;
                            String iteminstance = itemInstanceBD.toString();
                            if (!this.isWorkItemItemInUse(allWorkItemItems, sdcid, keyid1, keyid2, keyid3, itemsdcid, itemkeyid1, itemkeyid2, itemkeyid3, iteminstance)) {
                                if (itemsdcid != null && itemsdcid.equals("ParamList")) {
                                    PropertyList tempprops = new PropertyList();
                                    tempprops.setProperty("sdcid", sdcid);
                                    tempprops.setProperty("keyid1", keyid1);
                                    tempprops.setProperty("keyid2", keyid2);
                                    tempprops.setProperty("keyid3", keyid3);
                                    tempprops.setProperty("paramlistid", itemkeyid1);
                                    tempprops.setProperty("paramlistversionid", itemkeyid2);
                                    tempprops.setProperty("variantid", itemkeyid3);
                                    tempprops.setProperty("dataset", iteminstance);
                                    tempprops.setProperty("forcedelete", forcedelete);
                                    tempprops.setProperty("tracelogid", tracelogid);
                                    if (reason.length() > 0) {
                                        tempprops.setProperty("auditreason", reason);
                                        tempprops.setProperty("auditactivity", activity);
                                        tempprops.setProperty("auditsignedflag", signedFlag);
                                    }
                                    ActionProcessor ac = this.getActionProcessor();
                                    try {
                                        ac.processAction("DeleteDataSet", "1", tempprops);
                                        continue;
                                    }
                                    catch (ActionException ae) {
                                        if (deleterset) {
                                            damProcessor.clearRSet(rsetid);
                                        }
                                        throw new SapphireException("PROCESSACTION_FAILED", "Error calling DeleteDataSet Action::", ae);
                                    }
                                }
                                if (itemsdcid == null || !itemsdcid.equals("SpecSDC")) continue;
                                ActionProcessor ac = this.getActionProcessor();
                                PropertyList tempprops = new PropertyList();
                                tempprops.setProperty("sdcid", sdcid);
                                tempprops.setProperty("keyid1", keyid1);
                                tempprops.setProperty("keyid2", keyid2);
                                tempprops.setProperty("keyid3", keyid3);
                                tempprops.setProperty("specid", itemkeyid1);
                                tempprops.setProperty("specversionid", itemkeyid2);
                                tempprops.setProperty("tracelogid", tracelogid);
                                ac.processAction("RemoveSDISpec", "1", tempprops);
                                continue;
                            }
                            this.logger.info("WorkItem item {" + sdcid + ";" + keyid1 + ";" + keyid2 + ";" + keyid3 + ";" + workitemid + ";" + workiteminstance + ";" + itemsdcid + ";" + itemkeyid1 + ";" + itemkeyid2 + ";" + itemkeyid3 + ";" + iteminstance + "} is in use by another workitem. Skipping Deletion.");
                        }
                        this.database.closeResultSet();
                    }
                    StringBuilder sql = new StringBuilder();
                    safeSQL.reset();
                    sql.append("select embedchildsampleplanid, embedchildsampleplanversionid from sdiworkitem");
                    sql.append(" where sdcid = ").append(safeSQL.addVar(sdcid));
                    sql.append(" and keyid1 = ").append(safeSQL.addVar(keyid1));
                    if (StringUtil.getLen(keyid2) > 0L && !"(null)".equals(keyid2)) {
                        sql.append(" and keyid2 = ").append(safeSQL.addVar(keyid2));
                        if (StringUtil.getLen(keyid3) > 0L && !"(null)".equals(keyid3)) {
                            sql.append(" and keyid3 = ").append(safeSQL.addVar(keyid3));
                        }
                    }
                    sql.append(" and workitemid = ").append(safeSQL.addVar(workitemid));
                    sql.append(" and workiteminstance = ").append(safeSQL.addVar(workiteminstance));
                    sql.append(" and embedchildsampleplanid is not null");
                    DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                    if (ds != null && ds.size() > 0) {
                        String embedchildsampleplanid = ds.getString(0, "embedchildsampleplanid", "").trim();
                        String embedchildsampleplanversionid = ds.getString(0, "embedchildsampleplanversionid", "").trim();
                        if (StringUtil.getLen(embedchildsampleplanid) > 0L && StringUtil.getLen(embedchildsampleplanversionid) > 0L) {
                            PropertyList props = new PropertyList();
                            props.setProperty("sdcid", "LV_ChildSamplePlan");
                            props.setProperty("keyid1", embedchildsampleplanid);
                            props.setProperty("keyid2", embedchildsampleplanversionid);
                            this.getActionProcessor().processActionClass(DeleteSDI.class.getName(), props);
                        }
                    }
                    this.logger.info("Deleting the sdiworkitem record of {" + sdcid + ";" + keyid1 + ";" + keyid2 + ";" + keyid3 + ";" + workitemid + ";" + workiteminstance + "}");
                    psWorkitemitem.setString(1, sdcid);
                    psWorkitemitem.setString(2, keyid1);
                    psWorkitemitem.setString(3, keyid2);
                    psWorkitemitem.setString(4, keyid3);
                    psWorkitemitem.setString(5, workitemid);
                    psWorkitemitem.setInt(6, Integer.parseInt(workiteminstance));
                    psWorkitemitem.executeUpdate();
                    this.syncAllWorkItemItemDS(allWorkItemItems, sdcid, keyid1, keyid2, keyid3, workitemid, workiteminstance);
                    psDeleteSDIWorkitemRelation.setString(1, sdcid);
                    psDeleteSDIWorkitemRelation.setString(2, keyid1);
                    psDeleteSDIWorkitemRelation.setString(3, keyid2);
                    psDeleteSDIWorkitemRelation.setString(4, keyid3);
                    psDeleteSDIWorkitemRelation.setString(5, workitemid);
                    psDeleteSDIWorkitemRelation.setInt(6, Integer.parseInt(workiteminstance));
                    psDeleteSDIWorkitemRelation.executeUpdate();
                    psTaskQueueWorkitem.setString(1, sdcid);
                    psTaskQueueWorkitem.setString(2, keyid1);
                    psTaskQueueWorkitem.setString(3, keyid2);
                    psTaskQueueWorkitem.setString(4, keyid3);
                    psTaskQueueWorkitem.setString(5, workitemid);
                    psTaskQueueWorkitem.setInt(6, Integer.parseInt(workiteminstance));
                    psTaskQueueWorkitem.executeUpdate();
                    psActivityWorkSDI.setString(1, sdcid);
                    psActivityWorkSDI.setString(2, keyid1);
                    psActivityWorkSDI.setString(3, keyid2);
                    psActivityWorkSDI.setString(4, keyid3);
                    psActivityWorkSDI.setString(5, workitemid);
                    psActivityWorkSDI.setInt(6, Integer.parseInt(workiteminstance));
                    DataSet activityWorkSDIS = new DataSet(psActivityWorkSDI.executeQuery());
                    if (activityWorkSDIS.getRowCount() > 0) {
                        deleteActivityWorkSDIs.copyRow(activityWorkSDIS, -1, 1);
                    }
                    psWorkitem.setString(1, sdcid);
                    psWorkitem.setString(2, keyid1);
                    psWorkitem.setString(3, keyid2);
                    psWorkitem.setString(4, keyid3);
                    psWorkitem.setString(5, workitemid);
                    psWorkitem.setInt(6, Integer.parseInt(workiteminstance));
                    psWorkitem.executeUpdate();
                    if (parentWI.getRowCount() > 0) {
                        psDeleteParentWorkitemItem.setString(1, sdcid);
                        psDeleteParentWorkitemItem.setString(2, keyid1);
                        psDeleteParentWorkitemItem.setString(3, keyid2);
                        psDeleteParentWorkitemItem.setString(4, keyid3);
                        psDeleteParentWorkitemItem.setString(5, parentWI.getString(0, "workitemid"));
                        psDeleteParentWorkitemItem.setInt(6, parentWI.getInt(0, "workiteminstance"));
                        psDeleteParentWorkitemItem.setString(7, workitemid);
                        psDeleteParentWorkitemItem.setInt(8, Integer.parseInt(workiteminstance));
                        psDeleteParentWorkitemItem.executeUpdate();
                        DataSet dsSDIWI = this.getQueryProcessor().getPreparedSqlDataSet(" SELECT sdcid, keyid1, keyid2, keyid3, workitemid, workiteminstance FROM sdiworkitem  WHERE sdcid = ? and keyid1 = ? and keyid2 = ? and keyid3 = ? and workitemid = ? AND workiteminstance = ? AND NOT EXISTS (SELECT 1 FROM sdiworkitemitem WHERE  sdiworkitemitem.sdcid = sdiworkitem.sdcid AND sdiworkitemitem.keyid1 = sdiworkitem.keyid1 AND sdiworkitemitem.keyid2 = sdiworkitem.keyid2 AND sdiworkitemitem.keyid3 = sdiworkitem.keyid3  AND sdiworkitemitem.workitemid = sdiworkitem.workitemid AND sdiworkitemitem.workiteminstance = sdiworkitem.workiteminstance  )", (Object[])new String[]{sdcid, keyid1, keyid2, keyid3, parentWI.getString(0, "workitemid"), parentWI.getValue(0, "workiteminstance")});
                        if (dsSDIWI.getRowCount() > 0) {
                            deleteSDIWI.copyRow(dsSDIWI, 0, 1);
                        }
                    }
                    if (tracelogid == null || tracelogid.length() <= 0) continue;
                    String auditUpdate = "UPDATE a_sdiworkitem SET tracelogid = ? WHERE tracelogid = 'DELETED' AND workitemid=? AND sdcid = ? AND keyid1 = ? AND keyid2 = ? AND keyid3 = ?";
                    this.database.executePreparedUpdate(auditUpdate, new Object[]{tracelogid, workitemid, sdcid, keyid1, keyid2, keyid3});
                    continue;
                }
                catch (Exception ex) {
                    if (deleterset) {
                        damProcessor.clearRSet(rsetid);
                    }
                    throw new SapphireException("PREPARE_STMT_FAILED", "Failed to set parameters the delete statement for: " + sdcid + ";" + keyid1 + ";" + keyid2 + ";" + keyid3 + " because: " + ErrorUtil.extractMessageFromException(ex, ErrorUtil.isUserAdmin(this.getConnectionId())), ex);
                }
            }
        } else {
            throw new SapphireException("CREATE_RSET_FAILURE", "Could not create");
        }
        if (deleteSDIWI.getRowCount() > 0) {
            PropertyList delSDIWI = new PropertyList();
            delSDIWI.setProperty("sdcid", deleteSDIWI.getValue(0, "sdcid"));
            delSDIWI.setProperty("keyid1", deleteSDIWI.getColumnValues("keyid1", ";"));
            delSDIWI.setProperty("keyid2", deleteSDIWI.getColumnValues("keyid2", ";"));
            delSDIWI.setProperty("keyid3", deleteSDIWI.getColumnValues("keyid2", ";"));
            delSDIWI.setProperty("workitemid", deleteSDIWI.getColumnValues("workitemid", ";"));
            delSDIWI.setProperty("workiteminstance", deleteSDIWI.getColumnValues("workiteminstance", ";"));
            this.getActionProcessor().processAction("DeleteSDIWorkItem", "1", delSDIWI);
        }
        if (deleteActivityWorkSDIs.getRowCount() > 0) {
            deleteActivityWorkSDIs.sort("activityid");
            ArrayList<DataSet> activityGrps = deleteActivityWorkSDIs.getGroupedDataSets("activityid");
            for (int g = 0; g < activityGrps.size(); ++g) {
                DataSet activityWorkSDIs = activityGrps.get(g);
                PropertyList props = new PropertyList();
                props.setProperty("activityid", activityWorkSDIs.getValue(0, "activityid"));
                props.setProperty("worksdcid", "SDIWorkItem");
                props.setProperty("workkeyid1", activityWorkSDIs.getColumnValues("workkeyid1", ";"));
                this.getActionProcessor().processActionClass(RemoveActivityWorkSDI.class.getName(), props);
            }
        }
    }

    public boolean isWorkItemItemInUse(DataSet allWorkItemItems, String sdcId, String keyId1, String keyId2, String keyId3, String itemSDCId, String itemKeyid1, String itemKeyid2, String itemKeyid3, String itemInstance) throws Exception {
        boolean flag = false;
        HashMap<String, Object> filterMap = new HashMap<String, Object>();
        filterMap.put("sdcid", sdcId);
        filterMap.put("keyid1", keyId1);
        filterMap.put("keyid2", keyId2);
        filterMap.put("keyid3", keyId3);
        filterMap.put("itemsdcid", itemSDCId);
        filterMap.put("itemkeyid1", itemKeyid1);
        filterMap.put("itemkeyid2", itemKeyid2);
        filterMap.put("itemkeyid3", itemKeyid3);
        filterMap.put("iteminstance", new BigDecimal(itemInstance));
        DataSet workItemItem = allWorkItemItems.getFilteredDataSet(filterMap);
        if (workItemItem.getRowCount() > 1) {
            flag = true;
        }
        return flag;
    }

    public void syncAllWorkItemItemDS(DataSet allWorkItemItems, String sdcId, String keyId1, String keyId2, String keyId3, String workItemId, String workItemInstance) {
        HashMap<String, Object> filterMap = new HashMap<String, Object>();
        filterMap.put("sdcid", sdcId);
        filterMap.put("keyid1", keyId1);
        filterMap.put("keyid2", keyId2);
        filterMap.put("keyid3", keyId3);
        filterMap.put("workitemid", workItemId);
        filterMap.put("workiteminstance", new BigDecimal(workItemInstance));
        int row = allWorkItemItems.findRow(filterMap);
        while (row > -1) {
            allWorkItemItems.deleteRow(row);
            row = allWorkItemItems.findRow(filterMap);
        }
    }
}

