/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.scheduler;

import com.labvantage.sapphire.BaseCustom;
import com.labvantage.sapphire.gwt.shared.util.StringUtil;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.util.cache.CacheUtil;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.util.ActionBlock;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class SchedulerUtil
extends BaseCustom {
    public static final String MANAGED_PLAN = "M";
    private static final String SEQUENCE_ID = "embeddedtemplate";

    public SchedulerUtil(String connectionId) {
        this.setConnectionId(connectionId);
    }

    public void performSchedulerUpdates(String sdcid, DataSet primaryData, DataSet beforeEditData, PropertyList actionProps) throws SapphireException {
        int i;
        if (actionProps.getProperty("__noloop").equals("Y")) {
            return;
        }
        SapphireConnection sapphireConnection = this.getConnectionProcessor().getSapphireConnection();
        HashSet<String> scheduleSources = (HashSet<String>)CacheUtil.get(sapphireConnection.getDatabaseId(), "Scheduler", "schedulesources");
        HashSet<String> scheduleSdcs = (HashSet<String>)CacheUtil.get(sapphireConnection.getDatabaseId(), "Scheduler", "schedulesdcs");
        if (scheduleSources == null || scheduleSdcs == null) {
            scheduleSources = new HashSet<String>();
            scheduleSdcs = new HashSet<String>();
            String sql = "select distinct policynodeid from scheduleplan where scheduleplantypeflag='M'";
            DataSet ds = this.getQueryProcessor().getSqlDataSet(sql);
            if (ds != null) {
                for (i = 0; i < ds.getRowCount(); ++i) {
                    String policyNodeId = ds.getString(i, "policynodeid", "Sapphire Custom");
                    PropertyList schedulerPolicy = this.getConfigurationProcessor().getPolicy("SchedulePlanPolicy", policyNodeId);
                    if (schedulerPolicy == null) continue;
                    PropertyListCollection nodeHierarchy = schedulerPolicy.getCollectionNotNull("nodehierarchy");
                    for (int j = 0; j < nodeHierarchy.size(); ++j) {
                        PropertyList node = nodeHierarchy.getPropertyList(j);
                        String sourceSdcid = node.getProperty("sdcid", "");
                        scheduleSources.add(sourceSdcid);
                    }
                    PropertyListCollection scheduleSdcColl = schedulerPolicy.getCollectionNotNull("schedulesdc");
                    for (int j = 0; j < scheduleSdcColl.size(); ++j) {
                        PropertyList node = scheduleSdcColl.getPropertyList(j);
                        String sourceSdcid = node.getProperty("sdcid", "");
                        scheduleSdcs.add(sourceSdcid);
                    }
                }
            }
            CacheUtil.put(sapphireConnection.getDatabaseId(), "Scheduler", "schedulesources", scheduleSources);
            CacheUtil.put(sapphireConnection.getDatabaseId(), "Scheduler", "schedulesdcs", scheduleSdcs);
        }
        for (String scheduleSource : scheduleSources) {
            if (!scheduleSource.equals(sdcid)) continue;
            String keyCol1 = this.getSDCProcessor().getProperty(sdcid, "keycolid1", "");
            String keyCol2 = this.getSDCProcessor().getProperty(sdcid, "keycolid2", "");
            String keyCol3 = this.getSDCProcessor().getProperty(sdcid, "keycolid3", "");
            for (int i2 = 0; i2 < primaryData.getRowCount(); ++i2) {
                String sql = "SELECT scheduleplan.scheduleplanid, scheduleplan.policynodeid, scheduleplannode.scheduleplannodeid, scheduleplannode.parentnodeid, scheduleplannode.scheduleplannodedesc FROM scheduleplannode JOIN scheduleplan on scheduleplan.scheduleplanid=scheduleplannode.scheduleplanid WHERE scheduleplan.scheduleplantypeflag='M' AND scheduleplannode.refsdcid=? AND scheduleplannode.refkeyid1=? ";
                Object[] params = new String[]{sdcid, primaryData.getString(i2, keyCol1)};
                if (!keyCol2.equals("")) {
                    sql = sql + "AND scheduleplannode.refkeyid2=? ";
                    params = new String[]{sdcid, primaryData.getString(i2, keyCol1), primaryData.getString(i2, keyCol2)};
                }
                if (!keyCol3.equals("")) {
                    sql = sql + "AND scheduleplannode.refkeyid3=? ";
                    params = new String[]{sdcid, primaryData.getString(i2, keyCol1), primaryData.getString(i2, keyCol2), primaryData.getString(i2, keyCol3)};
                }
                DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, params);
                for (int j = 0; j < ds.getRowCount(); ++j) {
                    String schedulePlanId = ds.getString(j, "scheduleplanid", "");
                    String schedulePlanNodeId = ds.getString(j, "scheduleplannodeid", "");
                    String policyNodeId = ds.getString(j, "policynodeid", "Sapphire Custom");
                    String parentNodeId = ds.getString(j, "parentnodeid", "root");
                    PropertyList schedulerPolicy = this.getConfigurationProcessor().getPolicy("SchedulePlanPolicy", policyNodeId);
                    if (schedulerPolicy == null) continue;
                    PropertyListCollection nodeHierarchy = schedulerPolicy.getCollectionNotNull("nodehierarchy");
                    for (int k = 0; k < nodeHierarchy.size(); ++k) {
                        String parentItem;
                        String newParentNodeId;
                        PropertyList node = nodeHierarchy.getPropertyList(k);
                        String sourceSdcid = node.getProperty("sdcid", "");
                        if (!sourceSdcid.equals(sdcid)) continue;
                        String parentLinkField1 = node.getProperty("parentlinkfield1", "");
                        String parentLinkField2 = node.getProperty("parentlinkfield2", "");
                        if (!actionProps.containsKey(parentLinkField1) && !actionProps.containsKey(parentLinkField2) || primaryData.getValue(i2, parentLinkField1, "").equals(this.getOldPrimaryValue(sdcid, primaryData, beforeEditData, i2, parentLinkField1)) && primaryData.getValue(i2, parentLinkField2, "").equals(this.getOldPrimaryValue(sdcid, primaryData, beforeEditData, i2, parentLinkField2)) || (newParentNodeId = this.generateParentNode(schedulerPolicy, schedulePlanId, parentItem = node.getProperty("parentitem"), parentLinkField1, parentLinkField2, sdcid, primaryData.getString(i2, keyCol1, ""), primaryData.getString(i2, keyCol2, ""), primaryData.getString(i2, keyCol3, ""), true)).equals(parentNodeId)) continue;
                        if (schedulePlanNodeId.equals(newParentNodeId)) {
                            this.moveNode(schedulePlanId, schedulePlanNodeId, "root");
                            continue;
                        }
                        this.moveNode(schedulePlanId, schedulePlanNodeId, newParentNodeId);
                    }
                }
            }
        }
        for (String scheduleSdc : scheduleSdcs) {
            if (!scheduleSdc.equals(sdcid)) continue;
            for (i = 0; i < primaryData.getRowCount(); ++i) {
                if (!primaryData.getString(i, "templateflag", this.getOldPrimaryValue(sdcid, primaryData, beforeEditData, i, "templateflag")).equals("Y")) continue;
                this.updateSchedulePlanNodes(sdcid, primaryData, beforeEditData, actionProps, i);
            }
        }
    }

    private void moveNode(String schedulePlanId, String schedulePlanNodeId, String newParentNodeId) throws SapphireException {
        PropertyList actionProps = new PropertyList();
        actionProps.setProperty("sdcid", "SchedulePlan");
        actionProps.setProperty("keyid1", schedulePlanId);
        actionProps.setProperty("linkid", "PlanNodes");
        actionProps.setProperty("scheduleplannodeid", schedulePlanNodeId);
        actionProps.setProperty("parentnodeid", newParentNodeId);
        this.getActionProcessor().processAction("EditSDIDetail", "1", actionProps);
        this.cleanupEmptyNodes(schedulePlanId);
    }

    private void updateSchedulePlanNodes(String sdcid, DataSet primaryData, DataSet beforeEditData, PropertyList actionProps, int primaryDataRow) throws SapphireException {
        String keyCol1 = this.getSDCProcessor().getProperty(sdcid, "keycolid1");
        String keyCol2 = this.getSDCProcessor().getProperty(sdcid, "keycolid2");
        String keyCol3 = this.getSDCProcessor().getProperty(sdcid, "keycolid3");
        String sql = "SELECT scheduleplan.policynodeid, scheduleplan.scheduleplanid, scheduleplanitem.scheduleplanitemid, scheduleplanitem.linksdcid, scheduleplanitem.scheduleplannodeid FROM scheduleplanitem JOIN scheduleplan ON scheduleplan.scheduleplanid=scheduleplanitem.scheduleplanid WHERE scheduleplan.scheduleplantypeflag='M' AND scheduleplanitem.scheduletemplatesdcid=? AND scheduleplanitem.scheduletemplatekeyid1=? ";
        Object[] params = new String[]{sdcid, primaryData.getString(primaryDataRow, keyCol1)};
        if (!keyCol2.equals("")) {
            sql = sql + "AND scheduleplanitem.scheduletemplatekeyid2=? ";
            params = new String[]{sdcid, primaryData.getString(primaryDataRow, keyCol1), primaryData.getString(primaryDataRow, keyCol2)};
        }
        if (!keyCol3.equals("")) {
            sql = sql + "AND scheduleplanitem.scheduletemplatekeyid3=? ";
            params = new String[]{sdcid, primaryData.getString(primaryDataRow, keyCol1), primaryData.getString(primaryDataRow, keyCol2), primaryData.getString(primaryDataRow, keyCol3)};
        }
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, params);
        String schedulePlanId = ds.getString(0, "scheduleplanid", "");
        String schedulePlanItemId = ds.getString(0, "scheduleplanitemid", "");
        String schedulePlanNodeId = ds.getString(0, "scheduleplannodeid", "");
        String policyNodeId = ds.getString(0, "policynodeid", "Sapphire Custom");
        String currentSourceSdc = ds.getString(0, "linksdcid", "");
        PropertyList schedulerPolicy = this.getConfigurationProcessor().getPolicy("SchedulePlanPolicy", policyNodeId);
        if (schedulerPolicy != null) {
            PropertyListCollection scheduleSdcColl = schedulerPolicy.getCollectionNotNull("schedulesdc");
            for (int k = 0; k < scheduleSdcColl.size(); ++k) {
                String sourceKeyid3;
                String sourceKeyid2;
                String sourceKeyid1;
                PropertyList node = scheduleSdcColl.getPropertyList(k);
                String sourceSdcid = node.getProperty("sdcid", "");
                if (!sourceSdcid.equals(sdcid)) continue;
                PropertyListCollection linkFieldColl = node.getCollection("nodefields");
                boolean linkFieldChanged = false;
                for (int l = 0; l < linkFieldColl.size(); ++l) {
                    String linkFieldId1 = linkFieldColl.getPropertyList(l).getProperty("linkfield1");
                    String linkFieldId2 = linkFieldColl.getPropertyList(l).getProperty("linkfield2");
                    if (!primaryData.isValidColumn(linkFieldId1) || !linkFieldId2.isEmpty() && !primaryData.isValidColumn(linkFieldId2)) continue;
                    String oldFieldId1Value = this.getOldPrimaryValue(sdcid, primaryData, beforeEditData, primaryDataRow, linkFieldId1);
                    String oldFieldId2Value = this.getOldPrimaryValue(sdcid, primaryData, beforeEditData, primaryDataRow, linkFieldId2);
                    String linkId1Value = primaryData.getString(primaryDataRow, linkFieldId1, "");
                    String linkId2Value = primaryData.getString(primaryDataRow, linkFieldId2, "");
                    if (linkId1Value.equals(oldFieldId1Value) && linkId2Value.equals(oldFieldId2Value)) continue;
                    linkFieldChanged = true;
                }
                if (!linkFieldChanged) continue;
                if (ds.getRowCount() > 1) {
                    throw new SapphireException(this.getTranslationProcessor().translate("Template is located under multiple Schedule Plan Items."));
                }
                PropertyList newSource = this.getTemplateSource(sdcid, primaryData.getString(primaryDataRow, keyCol1), primaryData.getString(primaryDataRow, keyCol2), primaryData.getString(primaryDataRow, keyCol3), currentSourceSdc, schedulerPolicy);
                String sourceSdc = newSource.getProperty("sdcid", "");
                String newNodeId = this.generateNodes(schedulerPolicy, schedulePlanId, sourceSdc, sourceKeyid1 = newSource.getProperty("keyid1", ""), sourceKeyid2 = newSource.getProperty("keyid2", ""), sourceKeyid3 = newSource.getProperty("keyid3", ""));
                if (newNodeId.equals(schedulePlanNodeId)) continue;
                this.moveSchedulePlanItem(schedulePlanId, schedulePlanItemId, newNodeId, sourceSdc, sourceKeyid1, sourceKeyid2, sourceKeyid3);
            }
        }
    }

    private void moveSchedulePlanItem(String schedulePlanId, String schedulePlanItemId, String newNodeId, String sourceSdc, String sourceKeyid1, String sourceKeyid2, String sourceKeyid3) throws ActionException {
        PropertyList actionProps = new PropertyList();
        actionProps.setProperty("sdcid", "SchedulePlanItem");
        actionProps.setProperty("keyid1", schedulePlanId);
        actionProps.setProperty("keyid2", schedulePlanItemId);
        actionProps.setProperty("scheduleplannodeid", newNodeId);
        actionProps.setProperty("linksdcid", sourceSdc);
        actionProps.setProperty("linkkeyid1", sourceKeyid1);
        actionProps.setProperty("linkkeyid2", sourceKeyid2);
        actionProps.setProperty("linkkeyid3", sourceKeyid3);
        this.getActionProcessor().processAction("EditSDI", "1", actionProps);
    }

    private String getOldPrimaryValue(String sdcid, DataSet newPrimary, DataSet oldPrimary, int primaryRow, String columnId) {
        int oldRow;
        String oldValue = "";
        String keyid1 = newPrimary.getValue(primaryRow, this.getSDCProcessor().getProperty(sdcid, "keycolid1"));
        String keyid2 = newPrimary.getValue(primaryRow, this.getSDCProcessor().getProperty(sdcid, "keycolid2"));
        String keyid3 = newPrimary.getValue(primaryRow, this.getSDCProcessor().getProperty(sdcid, "keycolid3"));
        HashMap<String, String> findMap = new HashMap<String, String>();
        findMap.put(this.getSDCProcessor().getProperty(sdcid, "keycolid1"), keyid1);
        if (keyid2 != null && keyid2.length() > 0) {
            findMap.put(this.getSDCProcessor().getProperty(sdcid, "keycolid2"), keyid2);
        }
        if (keyid3 != null && keyid3.length() > 0) {
            findMap.put(this.getSDCProcessor().getProperty(sdcid, "keycolid3"), keyid3);
        }
        if (oldPrimary != null && (oldRow = oldPrimary.findRow(findMap)) >= 0 && oldRow < oldPrimary.size()) {
            oldValue = oldPrimary.getValue(oldRow, columnId);
        }
        return oldValue;
    }

    public PropertyList getTemplateSource(String templateSdcId, String templateKeyId1, String templateKeyId2, String templateKeyId3, String sourceSdc, PropertyList schedulerPolicy) {
        PropertyList retVal = new PropertyList();
        HashMap<String, String> nodeSdcs = new HashMap<String, String>();
        PropertyListCollection nodeHierarchy = schedulerPolicy.getCollectionNotNull("nodehierarchy");
        for (int i = 0; i < nodeHierarchy.size(); ++i) {
            PropertyList node = nodeHierarchy.getPropertyList(i);
            nodeSdcs.put(node.getProperty("id"), node.getProperty("sdcid"));
        }
        PropertyListCollection scheduleSdcs = schedulerPolicy.getCollectionNotNull("schedulesdc");
        for (int i = 0; i < scheduleSdcs.size(); ++i) {
            PropertyList scheduleSdcProps = scheduleSdcs.getPropertyList(i);
            if (!scheduleSdcProps.getProperty("sdcid").equals(templateSdcId)) continue;
            PropertyListCollection linkFieldCollection = scheduleSdcProps.getCollection("nodefields");
            for (int j = 0; j < linkFieldCollection.size(); ++j) {
                DataSet ds;
                PropertyList linkColumnProps = linkFieldCollection.getPropertyList(j);
                String linkSdc = (String)nodeSdcs.get(linkColumnProps.getProperty("linkitem"));
                if (linkSdc == null || !linkSdc.equals(sourceSdc) && !sourceSdc.equals("")) continue;
                String linkField1 = linkColumnProps.getProperty("linkfield1");
                String linkField2 = linkColumnProps.getProperty("linkfield2");
                String table = this.getSDCProcessor().getProperty(templateSdcId, "tableid");
                String keyCol1 = this.getSDCProcessor().getProperty(templateSdcId, "keycolid1");
                String keyCol2 = this.getSDCProcessor().getProperty(templateSdcId, "keycolid2");
                String keyCol3 = this.getSDCProcessor().getProperty(templateSdcId, "keycolid3");
                SafeSQL safeSQL = new SafeSQL();
                String sql = "SELECT * FROM " + table + " WHERE " + keyCol1 + "=" + safeSQL.addVar(templateKeyId1) + " ";
                if (!keyCol2.equals("") && !templateKeyId2.equals("")) {
                    sql = sql + " AND " + keyCol2 + "=" + safeSQL.addVar(templateKeyId2) + " ";
                }
                if (!keyCol3.equals("") && !templateKeyId3.equals("")) {
                    sql = sql + " AND " + keyCol3 + "=" + safeSQL.addVar(templateKeyId3) + " ";
                }
                if (!(ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues())).getString(0, linkField1, "").equals("")) {
                    retVal.setProperty("sdcid", linkSdc);
                    retVal.setProperty("keyid1", ds.getString(0, linkField1, ""));
                    retVal.setProperty("keyid2", ds.getString(0, linkField2, ""));
                }
                retVal.setProperty("linkfield1", linkField1);
                retVal.setProperty("linkfield2", linkField2);
            }
        }
        return retVal;
    }

    public String generateNodes(PropertyList schedulerPolicy, String schedulePlanId, String sdcId, String keyid1, String keyid2, String keyid3) throws SapphireException {
        String nodeId = "";
        PropertyListCollection nodeHierarchy = schedulerPolicy.getCollectionNotNull("nodehierarchy");
        for (int i = 0; i < nodeHierarchy.size(); ++i) {
            PropertyList node = nodeHierarchy.getPropertyList(i);
            String nodeSdcid = node.getProperty("sdcid", "");
            String parentItem = node.getProperty("parentitem", "");
            if (!nodeSdcid.equals(sdcId)) continue;
            if (parentItem.equals("")) {
                nodeId = this.generateNode(schedulePlanId, "root", sdcId, keyid1, keyid2, keyid3);
                continue;
            }
            String parentLinkField1 = node.getProperty("parentlinkfield1", "");
            String parentLinkField2 = node.getProperty("parentlinkfield2", "");
            String parentNodeId = this.generateParentNode(schedulerPolicy, schedulePlanId, parentItem, parentLinkField1, parentLinkField2, sdcId, keyid1, keyid2, keyid3, false);
            nodeId = this.generateNode(schedulePlanId, parentNodeId, sdcId, keyid1, keyid2, keyid3);
        }
        return nodeId;
    }

    private String generateParentNode(PropertyList schedulerPolicy, String schedulePlanId, String parentItem, String parentLinkField1, String parentLinkField2, String childSdcId, String childKeyid1, String childKeyid2, String childKeyid3, boolean checkForEmptyParent) throws SapphireException {
        String nodeId = "";
        PropertyListCollection nodeHierarchy = schedulerPolicy.getCollectionNotNull("nodehierarchy");
        for (int i = 0; i < nodeHierarchy.size(); ++i) {
            PropertyList node = nodeHierarchy.getPropertyList(i);
            String nodeParentItem = node.getProperty("id", "");
            if (!nodeParentItem.equals(parentItem) || node.getProperty("sdcid", "").equals("")) continue;
            String parentSdcId = node.getProperty("sdcid", "");
            if (parentLinkField1.equals("")) {
                DataSet primaryLinks = this.getSDCProcessor().getLinksData(childSdcId);
                for (int j = 0; j < primaryLinks.getRowCount(); ++j) {
                    if (!primaryLinks.getString(j, "linksdcid", "").equals(parentSdcId)) continue;
                    parentLinkField1 = primaryLinks.getString(j, "sdccolumnid", "");
                    parentLinkField2 = primaryLinks.getString(j, "sdccolumnid2", "");
                    break;
                }
            }
            if (parentLinkField1.equals("")) continue;
            String table = this.getSDCProcessor().getProperty(childSdcId, "tableid");
            String keyCol1 = this.getSDCProcessor().getProperty(childSdcId, "keycolid1");
            String keyCol2 = this.getSDCProcessor().getProperty(childSdcId, "keycolid2");
            String keyCol3 = this.getSDCProcessor().getProperty(childSdcId, "keycolid3");
            SafeSQL safeSQL = new SafeSQL();
            String sql = parentLinkField2.equals("") ? "SELECT " + parentLinkField1 + " FROM " + table + " WHERE " + keyCol1 + "=" + safeSQL.addVar(childKeyid1) + " " : "SELECT " + parentLinkField1 + ", " + parentLinkField2 + " FROM " + table + " WHERE " + keyCol1 + "=" + safeSQL.addVar(childKeyid1) + " ";
            if (!keyCol2.equals("") && !childKeyid2.equals("")) {
                sql = sql + " AND " + keyCol2 + "=" + safeSQL.addVar(childKeyid2) + " ";
            }
            if (!keyCol3.equals("") && !childKeyid3.equals("")) {
                sql = sql + " AND " + keyCol3 + "='" + safeSQL.addVar(childKeyid3) + " ";
            }
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
            String parentKeyid1 = ds.getString(0, parentLinkField1, "");
            String parentKeyid2 = ds.getString(0, parentLinkField2, "");
            if (!parentKeyid1.equals("")) {
                nodeId = this.generateNodes(schedulerPolicy, schedulePlanId, parentSdcId, parentKeyid1, parentKeyid2, "");
                continue;
            }
            if (!parentKeyid1.equals("") || !checkForEmptyParent) continue;
            nodeId = this.generateNode(schedulePlanId, "root", childSdcId, childKeyid1, childKeyid2, childKeyid3);
        }
        return nodeId;
    }

    private String generateNode(String schedulePlanId, String parentNodeId, String sdcId, String keyid1, String keyid2, String keyid3) throws ActionException {
        DataSet ds;
        String sql = "SELECT scheduleplannodeid FROM scheduleplannode WHERE scheduleplanid=? AND refsdcid=? AND refkeyid1=? ";
        ArrayList<String> params = new ArrayList<String>();
        params.add(schedulePlanId);
        params.add(sdcId);
        params.add(keyid1);
        if (!keyid2.equals("") && !keyid2.equals("(null)")) {
            sql = sql + " AND refkeyid2=?";
            params.add(keyid2);
        }
        if (!keyid3.equals("") && !keyid3.equals("(null)")) {
            sql = sql + " AND refkeyid3=?";
            params.add(keyid3);
        }
        if ((ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])params.toArray(new String[params.size()]))).getRowCount() > 0) {
            return ds.getString(0, "scheduleplannodeid", "");
        }
        String nodeId = this.generateNodeId();
        String nodeDesc = this.getSdiDesc(sdcId, keyid1, keyid2, keyid3);
        if (parentNodeId.equals("")) {
            parentNodeId = "root";
        }
        PropertyList actionProps = new PropertyList();
        actionProps.setProperty("sdcid", "SchedulePlan");
        actionProps.setProperty("linkid", "PlanNodes");
        actionProps.setProperty("keyid1", schedulePlanId);
        actionProps.setProperty("scheduleplannodeid", nodeId);
        actionProps.setProperty("scheduleplannodedesc", nodeDesc);
        actionProps.setProperty("parentnodeid", parentNodeId);
        actionProps.setProperty("refsdcid", sdcId);
        actionProps.setProperty("refkeyid1", keyid1);
        actionProps.setProperty("refkeyid2", keyid2);
        actionProps.setProperty("refkeyid3", keyid3);
        this.getActionProcessor().processAction("AddSDIDetail", "1", actionProps);
        return nodeId;
    }

    private String getSdiDesc(String sdcId, String keyid1, String keyid2, String keyid3) {
        String description = "";
        HashMap sdcProps = this.getSDCProcessor().getSDCProperties(sdcId);
        String tableId = (String)sdcProps.get("tableid");
        String keyCol1 = (String)sdcProps.get("keycolid1");
        String keyCol2 = (String)sdcProps.get("keycolid2");
        String keyCol3 = (String)sdcProps.get("keycolid3");
        String descCol = (String)sdcProps.get("desccol");
        String sql = "SELECT " + descCol + " FROM " + tableId + " WHERE " + keyCol1 + "=? ";
        Object[] params = new String[]{keyid1};
        if (keyCol2 != null) {
            sql = sql + "AND " + keyCol2 + "=? ";
            params = new String[]{keyid1, keyid2};
        }
        if (keyCol3 != null) {
            sql = sql + "AND " + keyCol3 + "=? ";
            params = new String[]{keyid1, keyid2, keyid3};
        }
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, params);
        description = ds.getString(0, descCol, keyid1);
        return description;
    }

    private String generateNodeId() {
        int sequence = this.getSequenceProcessor().getSequence("SchedulePlanNode", "mainkey", 1, 1);
        String sSequence = new DecimalFormat("000000").format(sequence);
        return "N" + sSequence;
    }

    public void cleanupEmptyNodes(String schedulePlanId) throws SapphireException {
        String sql = "SELECT sp.scheduleplanid, spn.scheduleplannodeid FROM scheduleplannode spn JOIN scheduleplan sp ON sp.scheduleplanid=spn.scheduleplanid WHERE sp.scheduleplanid=? AND sp.scheduleplantypeflag='M' AND NOT EXISTS (   SELECT 1 FROM scheduleplanitem   WHERE scheduleplanitem.scheduleplanid=spn.scheduleplanid   AND scheduleplanitem.scheduleplannodeid=spn.scheduleplannodeid) AND NOT EXISTS (   SELECT 1 FROM scheduleplannode n2   WHERE n2.scheduleplanid=spn.scheduleplanid    AND n2.parentnodeid=spn.scheduleplannodeid) ";
        Object[] params = new String[]{schedulePlanId};
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, params);
        while (ds.getRowCount() > 0) {
            for (int i = 0; i < ds.getRowCount(); ++i) {
                PropertyList actionProps = new PropertyList();
                actionProps.setProperty("sdcid", "SchedulePlan");
                actionProps.setProperty("linkid", "PlanNodes");
                actionProps.setProperty("keyid1", ds.getValue(i, "scheduleplanid", ""));
                actionProps.setProperty("scheduleplannodeid", ds.getValue(i, "scheduleplannodeid", ""));
                this.getActionProcessor().processAction("DeleteSDIDetail", "1", actionProps);
            }
            ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, params);
        }
    }

    public void editTemplateSource(String templateSdcId, String templateKeyId1, String templateKeyId2, String templateKeyId3, String sourceField1, String sourceKeyid1, String sourceField2, String sourceKeyid2) throws ActionException {
        PropertyList actionProps = new PropertyList();
        actionProps.setProperty("sdcid", templateSdcId);
        actionProps.setProperty("keyid1", templateKeyId1);
        if (!templateKeyId2.equals("")) {
            actionProps.setProperty("keyid2", templateKeyId2);
        }
        if (!templateKeyId3.equals("")) {
            actionProps.setProperty("keyid3", templateKeyId3);
        }
        actionProps.setProperty(sourceField1, sourceKeyid1);
        if (!sourceField2.equals("")) {
            actionProps.setProperty(sourceField2, sourceKeyid2);
        }
        actionProps.setProperty("__noloop", "Y");
        this.getActionProcessor().processAction("EditSDI", "1", actionProps);
    }

    public boolean isSingleItemTemplate(String templateSdcId, String templateKeyId1, String templateKeyId2, String templateKeyId3) {
        DataSet ds;
        String sql = "SELECT DISTINCT scheduleplanitemid FROM scheduleplanitem WHERE scheduletemplatesdcid=? AND scheduletemplatekeyid1=? ";
        Object[] params = new String[]{templateSdcId, templateKeyId1};
        if (!templateKeyId2.equals("")) {
            sql = sql + "AND scheduletemplatekeyid2=? ";
            params = new String[]{templateSdcId, templateKeyId1, templateKeyId2};
        }
        if (!templateKeyId3.equals("")) {
            sql = sql + "AND scheduletemplatekeyid3=? ";
            params = new String[]{templateSdcId, templateKeyId1, templateKeyId2, templateKeyId3};
        }
        return (ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, params)).getRowCount() == 1;
    }

    public String getSchedulePlanTypeFlag(String schedulePlanId) {
        String sql = "select scheduleplantypeflag from scheduleplan where scheduleplanid=?";
        Object[] params = new String[]{schedulePlanId};
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, params);
        return ds.getString(0, "scheduleplantypeflag", "");
    }

    public String getPolicyNode(String schedulePlanId) {
        String sql = "select policynodeid from scheduleplan where scheduleplanid=?";
        Object[] params = new String[]{schedulePlanId};
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, params);
        return ds.getString(0, "policynodeid", "Sapphire Custom");
    }

    public void deleteEmbeddedScheduleTemplate(List<String> schedulePlanIdList, List<String> schedulePlanItemIdList) {
        if (schedulePlanIdList == null) {
            throw new IllegalArgumentException("Schedule plan ID list is null");
        }
        if (schedulePlanItemIdList == null) {
            throw new IllegalArgumentException("Schedule plan item ID list is null");
        }
        if (schedulePlanIdList.size() != schedulePlanItemIdList.size()) {
            throw new IllegalArgumentException("Inconsistent number plan schedule plans and plan items.");
        }
        Set<String> sdcIdSet = this.getSdcIdSet(schedulePlanIdList, schedulePlanItemIdList);
        Map<String, String> scheduleTemplateFlagColumnMap = this.getScheduleTemplateFlagColumn(new HashSet<String>(schedulePlanIdList), sdcIdSet);
        for (String sdcId : sdcIdSet) {
            String scheduleTemplateFlag = scheduleTemplateFlagColumnMap.get(sdcId);
            if (scheduleTemplateFlag == null || scheduleTemplateFlag.isEmpty()) {
                throw new IllegalArgumentException("Schedule template flag not defined for SDC: " + sdcId);
            }
            PropertyList sdcProps = this.getSDCProcessor().getProperties(sdcId);
            String tableId = sdcProps.getProperty("tableid");
            String keyColumn1 = sdcProps.getProperty("keycolid1");
            String keyColumn2 = sdcProps.getProperty("keycolid2");
            String keyColumn3 = sdcProps.getProperty("keycolid3");
            boolean templateTable = sdcProps.getProperty("templatableflag", "N").toLowerCase().startsWith("y");
            ArrayList<String> sqlParams = new ArrayList<String>();
            StringBuilder joinFragment = new StringBuilder();
            StringBuilder selectFragment = new StringBuilder();
            joinFragment.append(" AND spi.scheduletemplatekeyid1 = p.").append(keyColumn1);
            selectFragment.append("p.").append(keyColumn1);
            if (!keyColumn2.isEmpty()) {
                joinFragment.append(" AND spi.scheduletemplatekeyid2 = p.").append(keyColumn2);
                selectFragment.append(", p.").append(keyColumn2);
                if (!keyColumn3.isEmpty()) {
                    joinFragment.append(" AND spi.scheduletemplatekeyid3 = p.").append(keyColumn3);
                    selectFragment.append(", p.").append(keyColumn3);
                }
            }
            joinFragment.append(" AND NOT (");
            StringBuilder joinSubFragment = new StringBuilder();
            for (int i = 0; i < schedulePlanIdList.size(); ++i) {
                joinSubFragment.append(" OR (spi.scheduleplanid = ? AND spi.scheduleplanitemid = ?)");
                sqlParams.add(schedulePlanIdList.get(i));
                sqlParams.add(schedulePlanItemIdList.get(i));
            }
            if (joinSubFragment.length() <= 0) continue;
            joinFragment.append(joinSubFragment.substring(4)).append(")");
            if (selectFragment.length() <= 0) continue;
            String sql = "SELECT " + selectFragment + " FROM " + tableId + " p LEFT JOIN scheduleplanitem spi ON spi.scheduletemplatesdcid = '" + sdcId + "'" + joinFragment + " WHERE p." + scheduleTemplateFlag + " = 'E' AND spi.scheduleplanitemid IS NULL " + (templateTable ? " AND p.templateflag = 'Y'" : "");
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, sqlParams.toArray());
            StringBuilder keyId1s = new StringBuilder();
            StringBuilder keyId2s = new StringBuilder();
            StringBuilder keyId3s = new StringBuilder();
            for (int i = 0; i < ds.getRowCount(); ++i) {
                keyId1s.append(";").append(ds.getString(i, keyColumn1, ""));
                if (keyColumn2.isEmpty()) continue;
                keyId2s.append(";").append(ds.getString(i, keyColumn2, ""));
                if (keyColumn3.isEmpty()) continue;
                keyId3s.append(";").append(ds.getString(i, keyColumn3, ""));
            }
            if (keyId1s.length() <= 0) continue;
            PropertyList deleteSDIProps = new PropertyList();
            deleteSDIProps.setProperty("sdcid", sdcId);
            deleteSDIProps.setProperty("keyid1", keyId1s.substring(1));
            if (keyId2s.length() > 0) {
                deleteSDIProps.setProperty("keyid2", keyId2s.substring(1));
                if (keyId3s.length() > 0) {
                    deleteSDIProps.setProperty("keyid3", keyId3s.substring(1));
                }
            }
            deleteSDIProps.setProperty("auditreason", "Schedule template deleted automatically due to schedule plan item being deleted");
            try {
                this.getActionProcessor().processAction("DeleteSDI", "1", deleteSDIProps);
            }
            catch (ActionException e) {
                this.logger.error("Cannot delete embedded schedule templates", e);
            }
        }
    }

    private Set<String> getSdcIdSet(List<String> schedulePlanIdList, List<String> schedulePlanItemIdList) {
        ArrayList<String> sqlParams = new ArrayList<String>();
        StringBuilder whereFragment = new StringBuilder();
        HashSet<String> sdcIdSet = new HashSet<String>();
        for (int i = 0; i < schedulePlanIdList.size(); ++i) {
            whereFragment.append(" OR (scheduleplanid = ? AND scheduleplanitemid = ?)");
            sqlParams.add(schedulePlanIdList.get(i));
            sqlParams.add(schedulePlanItemIdList.get(i));
        }
        if (whereFragment.length() > 0) {
            String sql = "SELECT scheduletemplatesdcid FROM scheduleplanitem WHERE " + whereFragment.substring(4);
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, sqlParams.toArray());
            for (int i = 0; i < ds.getRowCount(); ++i) {
                String sdcId = ds.getString(i, "scheduletemplatesdcid", "");
                if (sdcId.isEmpty()) continue;
                sdcIdSet.add(sdcId);
            }
        }
        return sdcIdSet;
    }

    private Map<String, String> getScheduleTemplateFlagColumn(Set<String> schedulePlanIdSet, Set<String> sdcIdSet) {
        HashMap<String, String> scheduleTemplateFlagColumnMap = new HashMap<String, String>();
        for (String sdcId : sdcIdSet) {
            ArrayList<String> sqlParams = new ArrayList<String>();
            StringBuilder whereFragment = new StringBuilder();
            for (String schedulePlanId : schedulePlanIdSet) {
                whereFragment.append(" OR scheduleplanid = ?");
                sqlParams.add(schedulePlanId);
            }
            if (whereFragment.length() <= 0) continue;
            String sql = "SELECT DISTINCT policynodeid FROM scheduleplan WHERE " + whereFragment.substring(4);
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, sqlParams.toArray());
            block4: for (int i = 0; i < ds.getRowCount(); ++i) {
                String policyNodeId = ds.getString(i, "policynodeid", "");
                PropertyList schedulerPolicy = null;
                try {
                    schedulerPolicy = this.getConfigurationProcessor().getPolicy("SchedulePlanPolicy", policyNodeId);
                }
                catch (SapphireException e) {
                    this.logger.warn("Cannot get scheduler policy for node: " + policyNodeId);
                }
                if (schedulerPolicy == null) continue;
                PropertyListCollection scheduleSdcCollection = schedulerPolicy.getCollectionNotNull("schedulesdc");
                for (int j = 0; j < scheduleSdcCollection.size(); ++j) {
                    PropertyList node = scheduleSdcCollection.getPropertyList(j);
                    String sourceSdcId = node.getProperty("sdcid", "");
                    if (!sourceSdcId.equals(sdcId) || node.getProperty("scheduletemplatefield", "").isEmpty()) continue;
                    scheduleTemplateFlagColumnMap.put(sdcId, node.getProperty("scheduletemplatefield", ""));
                    continue block4;
                }
            }
        }
        return scheduleTemplateFlagColumnMap;
    }

    public void copySchedulePlanItemsFromSchedulePlan(String scheduleplanid, String newSchedulePlanId, boolean setAsInactive) throws SapphireException {
        String[] toKeyId1Arr = StringUtil.split(newSchedulePlanId, ";");
        int copies = toKeyId1Arr.length;
        ArrayList<String> params = new ArrayList<String>();
        params.add(scheduleplanid);
        StringBuffer planItemSql = new StringBuffer("SELECT spi.scheduleplanid, spi.scheduleplanitemid ");
        planItemSql.append(" FROM scheduleplanitem spi WHERE spi.scheduleplanid = ? ");
        planItemSql.append(" ORDER BY spi.scheduleplanid, spi.scheduleplanitemid");
        DataSet planItems = this.getQueryProcessor().getPreparedSqlDataSet(planItemSql.toString(), params.toArray());
        planItems.sort("scheduleplanid");
        ActionBlock actionBlock = new ActionBlock();
        for (int j = 0; j < planItems.getRowCount(); ++j) {
            StringBuffer schedulePlanItemId = new StringBuffer();
            for (int i = 0; i < copies; ++i) {
                schedulePlanItemId.append(";").append(new DecimalFormat("00000").format(this.getSequenceProcessor().getSequence("SchedulePlanItem", newSchedulePlanId)));
            }
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", "SchedulePlanItem");
            props.setProperty("templatekeyid1", scheduleplanid);
            props.setProperty("templatekeyid2", planItems.getValue(j, "scheduleplanitemid"));
            props.setProperty("copies", String.valueOf(copies));
            if (setAsInactive) {
                props.setProperty("planitemstatus", "X");
            }
            props.setProperty("copyplanitems", "Y");
            props.setProperty("keyid1", newSchedulePlanId);
            props.setProperty("keyid2", schedulePlanItemId.substring(1));
            actionBlock.setAction(newSchedulePlanId + "_" + j, "AddSDI", "1", props);
        }
        this.getActionProcessor().processActionBlock(actionBlock);
    }

    public void copySchedulePlanItemsOnSource(String fromSdcId, String fromKeyId1, String fromKeyid2, String fromKeyid3, String toSdcId, String toKeyId1, String toKeyId2, String toKeyId3, boolean setAsInactive) throws SapphireException {
        String[] toKeyId1Arr = StringUtil.split(toKeyId1, ";");
        String[] toKeyId2Arr = StringUtil.split(toKeyId2, ";");
        String[] toKeyId3Arr = StringUtil.split(toKeyId3, ";");
        boolean hasToSdcKeyid2 = !this.getSDCProcessor().getProperty(toSdcId, "keycolid2").isEmpty();
        boolean hasToSdcKeyid3 = !this.getSDCProcessor().getProperty(toSdcId, "keycolid3").isEmpty();
        int copies = toKeyId1Arr.length;
        if (hasToSdcKeyid2 && (toKeyId2 == null || toKeyId2Arr.length != copies)) {
            throw new SapphireException("Number of to keyid1 and keyid2 parameters does not match!");
        }
        if (hasToSdcKeyid3 && (toKeyId3 == null || toKeyId3Arr.length != copies)) {
            throw new SapphireException("Number of to keyid1 and keyid3 parameters does not match!");
        }
        ArrayList<String> params = new ArrayList<String>();
        params.add(fromSdcId);
        params.add(fromKeyId1);
        StringBuffer planItemSql = new StringBuffer("SELECT spi.scheduleplanid, spi.scheduleplanitemid ");
        planItemSql.append(" FROM scheduleplanitem spi WHERE spi.linksdcid = ? AND spi.linkkeyid1 = ? ");
        if (!this.getSDCProcessor().getProperty(fromSdcId, "keycolid2").isEmpty()) {
            planItemSql.append("  AND spi.linkkeyid2 = ? ");
            params.add(fromKeyid2);
        }
        if (!this.getSDCProcessor().getProperty(fromSdcId, "keycolid3").isEmpty()) {
            planItemSql.append("  AND spi.linkkeyid3 = ? ");
            params.add(fromKeyid3);
        }
        planItemSql.append(" ORDER BY spi.scheduleplanid, spi.scheduleplanitemid");
        DataSet planItems = this.getQueryProcessor().getPreparedSqlDataSet(planItemSql.toString(), params.toArray());
        planItems.sort("scheduleplanid");
        ArrayList<DataSet> groupedPlanItems = planItems.getGroupedDataSets("scheduleplanid");
        ActionBlock actionBlock = new ActionBlock();
        for (DataSet planitems : groupedPlanItems) {
            for (int j = 0; j < planitems.getRowCount(); ++j) {
                String planId = planitems.getValue(j, "scheduleplanid");
                StringBuffer schedulePlanItemId = new StringBuffer();
                for (int i = 0; i < copies; ++i) {
                    schedulePlanItemId.append(";").append(new DecimalFormat("00000").format(this.getSequenceProcessor().getSequence("SchedulePlanItem", planId)));
                }
                PropertyList props = new PropertyList();
                props.setProperty("sdcid", "SchedulePlanItem");
                props.setProperty("templatekeyid1", planId);
                props.setProperty("templatekeyid2", planitems.getValue(j, "scheduleplanitemid"));
                props.setProperty("copies", String.valueOf(copies));
                if (setAsInactive) {
                    props.setProperty("planitemstatus", "X");
                }
                props.setProperty("copyplanitems", "Y");
                props.setProperty("keyid1", planId);
                props.setProperty("keyid2", schedulePlanItemId.substring(1));
                props.setProperty("linksdcid", toSdcId);
                props.setProperty("linkkeyid1", toKeyId1);
                if (hasToSdcKeyid2 && !toKeyId2.isEmpty()) {
                    props.put("linkkeyid2", toKeyId2);
                } else {
                    props.put("linkkeyid2", "(null)");
                }
                if (hasToSdcKeyid3 && !toKeyId3.isEmpty()) {
                    props.put("linkkeyid3", toKeyId3);
                } else {
                    props.put("linkkeyid3", "(null)");
                }
                actionBlock.setAction(planId + "_" + j, "AddSDI", "1", props);
            }
        }
        this.getActionProcessor().processActionBlock(actionBlock);
    }

    public void getNewEmbeddedTemplateId(PropertyList props) throws SapphireException {
        String sdcId = props.getProperty("sdcid", "");
        if (sdcId.isEmpty()) {
            throw new SapphireException("SDC id is empty");
        }
        PropertyList sdcProps = new PropertyList(this.getSDCProcessor().getSDCProperties(sdcId));
        String keyColumn1 = sdcProps.getProperty("keycolid1");
        String keyColumn2 = sdcProps.getProperty("keycolid2");
        int sequence = this.getSequenceProcessor().getSequence(sdcId, SEQUENCE_ID);
        String newKeyId1 = "ScheduleTmpl-" + String.format("%06d", sequence);
        String newKeyId2 = "";
        if (!keyColumn2.isEmpty()) {
            newKeyId2 = "1";
        }
        props.setProperty("keycolumn1", keyColumn1);
        props.setProperty("keycolumn2", keyColumn2);
        props.setProperty("newkeyid1", newKeyId1);
        props.setProperty("newkeyid2", newKeyId2);
    }

    public void checkForExistingSchedulePlanItemOnSource(String sdcid, String keyid1) throws SapphireException {
        String sql = "select scheduleplanid, scheduleplanitemid, scheduleplanitemdesc from scheduleplanitem where linksdcid = ? and linkkeyid1 = ?";
        List<String> keyid1list = Arrays.asList(keyid1.split(";"));
        StringBuilder msg = new StringBuilder();
        boolean found = false;
        for (String sourceKeyid1 : keyid1list) {
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])new String[]{sdcid, sourceKeyid1});
            if (ds.getRowCount() <= 0) continue;
            msg.append(this.getTranslationProcessor().translate("Schedule Plan Items found on")).append(" ").append(sourceKeyid1).append(":\n");
            for (int i = 0; i < ds.getRowCount(); ++i) {
                found = true;
                String scheduleplanid = ds.getValue(i, "scheduleplanid", "");
                String scheduleplanitemid = ds.getValue(i, "scheduleplanitemid", "");
                String scheduleplanitemdesc = ds.getValue(i, "scheduleplanitemdesc", "");
                msg.append(this.getTranslationProcessor().translate("Schedule Plan: ")).append(scheduleplanid).append(" ").append(this.getTranslationProcessor().translate("item")).append(": ").append(scheduleplanitemid);
                if (!scheduleplanitemdesc.isEmpty()) {
                    msg.append(" (").append(scheduleplanitemdesc).append(")");
                }
                msg.append("\n");
            }
        }
        if (found) {
            throw new SapphireException(this.getTranslationProcessor().translate("Delete not allowed.") + "\n" + msg.toString());
        }
    }

    public static int getTimeZoneDiffForDate(TimeZone sTimeZone, TimeZone cTimeZone, Calendar date) {
        int cTZoffset = cTimeZone.getOffset(date.getTimeInMillis());
        int sTZoffset = sTimeZone.getOffset(date.getTimeInMillis());
        int diffTimezone = cTZoffset - sTZoffset;
        return diffTimezone;
    }
}

