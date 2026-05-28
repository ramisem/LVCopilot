/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.cmt;

import com.labvantage.opal.ajax.cmt.OperationRequireCheckOut;
import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.admin.webadmin.WebAdminProcessor;
import com.labvantage.sapphire.cmt.SDISnapshotItem;
import com.labvantage.sapphire.cmt.SnapshotFactory;
import com.labvantage.sapphire.cmt.SnapshotPackage;
import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.platform.SapphireDatabase;
import com.labvantage.sapphire.util.jndi.ServiceLocator;
import com.labvantage.sapphire.util.junit.ConnectionDetails;
import com.labvantage.sapphire.xml.Node;
import com.labvantage.sapphire.xml.NodeList;
import com.labvantage.sapphire.xml.PropertyTree;
import com.labvantage.sapphire.xml.PropertyTreeUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.ConnectionInfo;
import sapphire.util.DBAccess;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;
import sapphire.xml.cmt.Snapshot;
import sapphire.xml.cmt.SnapshotItem;

public class CMTUtil {
    static final String LABVANTAGE_CVS_ID = "$Revision: 1.1 $";
    public static final ArrayList<String> OOB_CHANGE_CONTROLLED_YES_SDC_LIST = new ArrayList();
    public static final ArrayList<String> OOB_CHANGE_CONTROLLED_TEMPLATE_SDC_LIST = new ArrayList();
    public static final ArrayList<String> OOB_CHANGE_CONTROLLED_PARENT_SDC_LIST = new ArrayList();
    public static final HashSet<String> EXCLUDED_ATTACHMENT_TYPES = new HashSet();

    public static String getChangeLockIconHTML(String sdcId, String keyId1, String keyId2, String keyId3, String propertyTreeNodeId, String connectionId) {
        DataSet changeLogInfo = CMTUtil.getChangeLogDS(sdcId, keyId1, keyId2, keyId3, "", connectionId);
        return CMTUtil.getChangeLockIconHTML(changeLogInfo, sdcId, keyId1, keyId2, keyId3, propertyTreeNodeId, connectionId);
    }

    public static String getChangeLockIconHTML(DataSet changeLogInfo, String sdcId, String keyId1, String keyId2, String keyId3, String propertyTreeNodeId, String connectionId) {
        DataSet filterDS;
        StringBuffer html = new StringBuffer();
        ConnectionProcessor connectionProcessor = new ConnectionProcessor(connectionId);
        TranslationProcessor tp = new TranslationProcessor(connectionId);
        SDCProcessor sdcProcessor = new SDCProcessor(connectionId);
        int keyColumns = Integer.parseInt(sdcProcessor.getProperty(sdcId, "keycolumns"));
        HashMap<String, String> filterMap = new HashMap<String, String>();
        filterMap.put("linksdcid", sdcId);
        filterMap.put("linkkeyid1", keyId1);
        if (keyColumns > 1) {
            filterMap.put("linkkeyid2", keyId2);
        }
        if (keyColumns > 2) {
            filterMap.put("linkkeyid3", keyId3);
        }
        if ("PropertyTree".equals(sdcId)) {
            filterMap.put("propertytreenodeid", propertyTreeNodeId);
        }
        if ((filterDS = changeLogInfo.getFilteredDataSet(filterMap)).getRowCount() > 0) {
            int findRow = -1;
            if ("PropertyTree".equals(sdcId) && propertyTreeNodeId.length() == 0) {
                for (int i = 0; i < filterDS.getRowCount(); ++i) {
                    if (filterDS.getString(i, "propertytreenodeid", "").length() != 0) continue;
                    findRow = i;
                    break;
                }
            } else {
                findRow = 0;
            }
            if (findRow > -1) {
                String checkedOutByUserId = filterDS.getString(findRow, "checkedoutbyuserid", "");
                String checkedOutByDeptId = filterDS.getString(findRow, "checkedoutbydepartmentid", "");
                String currUserId = connectionProcessor.getSapphireConnection().getSysuserId();
                String currUserDeptList = ";" + connectionProcessor.getSapphireConnection().getDepartmentList() + ";";
                String iconDimension = "16";
                if (checkedOutByDeptId.length() > 0) {
                    if (currUserDeptList.contains(";" + checkedOutByDeptId + ";")) {
                        html.append("&nbsp;<span title = '" + tp.translate("Checked out to Department: ") + checkedOutByDeptId + "'><img src='" + "WEB-CORE/images/svg/checkout_dept.svg" + "' width=" + iconDimension + " height=" + iconDimension + "/></span>");
                    } else {
                        html.append("&nbsp;<span title = '" + tp.translate("Checked out to Department: ") + checkedOutByDeptId + "'><img src='" + "WEB-CORE/images/svg/checkout_others_dept.svg" + "' width=" + iconDimension + " height=" + iconDimension + "/></span>");
                    }
                } else if (checkedOutByUserId.equals(currUserId)) {
                    html.append("&nbsp;<span title = '" + tp.translate("Checked out by: ") + checkedOutByUserId + "'><img src='" + "WEB-CORE/images/svg/checkout_user.svg" + "' width=" + iconDimension + " height=" + iconDimension + "/></span>");
                } else {
                    html.append("&nbsp;<span title='" + tp.translate("Checked out by: ") + checkedOutByUserId + tp.translate(". Click to send message.") + "'><a href=\"javascript:sapphire.notification.send('" + checkedOutByUserId + "', '', 'Send Message to " + checkedOutByUserId + "', 'Enter message')\"><img src='" + "WEB-CORE/images/svg/checkout_others.svg" + "' width=" + iconDimension + " height=" + iconDimension + "/></a></span>");
                }
            }
        }
        return html.toString();
    }

    public static boolean isChangeControlLockOk(String sdcId, String keyId1, String keyId2, String keyId3, String propertyTreeNodeId, String connectionId) {
        DataSet changeLogInfo = CMTUtil.getChangeLogDS(sdcId, keyId1, keyId2, keyId3, "", connectionId);
        return CMTUtil.isChangeControlLockOk(changeLogInfo, sdcId, keyId1, keyId2, keyId3, propertyTreeNodeId, connectionId);
    }

    public static boolean isChangeControlLockOk(DataSet allChangeLogInfo, String sdcId, String keyId1, String keyId2, String keyId3, String propertyTreeNodeId, String connectionId) {
        int findRow;
        DataSet filterDS;
        boolean isChangeControlLockOk = true;
        ConnectionProcessor connectionProcessor = new ConnectionProcessor(connectionId);
        HashMap<String, String> filterMap = new HashMap<String, String>();
        filterMap.put("linksdcid", sdcId);
        filterMap.put("linkkeyid1", keyId1);
        if (keyId2 != null && keyId2.length() > 0) {
            filterMap.put("linkkeyid2", keyId2);
        }
        if (keyId3 != null && keyId3.length() > 0) {
            filterMap.put("linkkeyid3", keyId3);
        }
        filterMap.put("changelogstatus", "Checked Out");
        if (propertyTreeNodeId != null && propertyTreeNodeId.length() > 0 && "PropertyTree".equals(sdcId)) {
            filterMap.put("propertytreenodeid", propertyTreeNodeId);
        }
        if ((filterDS = allChangeLogInfo.getFilteredDataSet(filterMap)).getRowCount() > 0 && (findRow = 0) > -1) {
            String checkedOutByUserId = filterDS.getString(findRow, "checkedoutbyuserid", "");
            String checkedOutByDeptId = filterDS.getString(findRow, "checkedoutbydepartmentid", "");
            String currUserId = connectionProcessor.getSapphireConnection().getSysuserId();
            String currUserDeptList = ";" + connectionProcessor.getSapphireConnection().getDepartmentList() + ";";
            if (checkedOutByDeptId.length() > 0) {
                if (!currUserDeptList.contains(";" + checkedOutByDeptId + ";")) {
                    isChangeControlLockOk = false;
                }
            } else if (!checkedOutByUserId.equals(currUserId)) {
                isChangeControlLockOk = false;
            }
        }
        return isChangeControlLockOk;
    }

    public static boolean isPropertyTreeCheckedOut(String propertyTreeId, String connectionId) {
        QueryProcessor qp = new QueryProcessor(connectionId);
        String sql = "SELECT changelogid FROM changelog WHERE linksdcid = 'PropertyTree' AND linkkeyid1 = ? AND propertytreenodeid = '__FULL' AND changelogstatus = 'Checked Out'";
        DataSet ds = qp.getPreparedSqlDataSet(sql, new Object[]{propertyTreeId});
        return ds.getRowCount() > 0;
    }

    public static DataSet getChangeLogDS(String sdcId, String keyId1, String keyId2, String keyId3, String propertyTreeNodeId, String connectionId) {
        QueryProcessor qp = new QueryProcessor(connectionId);
        SDCProcessor sdcProcessor = new SDCProcessor(connectionId);
        int keyCols = Integer.parseInt(sdcProcessor.getProperty(sdcId, "keycolumns"));
        DataSet changeLogInfo = new DataSet();
        changeLogInfo = qp.getSqlDataSet("SELECT changerequestid, changelogid, linksdcid, linkkeyid1, linkkeyid2, linkkeyid3, propertytreenodeid, changelogstatus, checkedoutbyuserid, checkedoutbydepartmentid FROM changelog WHERE linksdcid = '" + sdcId + "' AND linkkeyid1 = '" + keyId1 + "'" + (keyCols > 1 ? " AND linkkeyid2 = '" + keyId2 + "'" : "") + (keyCols > 2 ? " AND linkkeyid3 = '" + keyId3 + "'" : "") + (propertyTreeNodeId != null && propertyTreeNodeId.length() > 0 ? " AND propertytreenodeid = '" + propertyTreeNodeId + "'" : "") + " AND changelogstatus = '" + "Checked Out" + "'");
        return changeLogInfo;
    }

    public static void updateNodeChangeLogs(String pTreeId, String nodeId, QueryProcessor queryProcessor, ActionProcessor actionProcessor, PropertyList extraProps) throws ActionException {
        String sql = "SELECT changelogid FROM changelog WHERE linksdcid = 'PropertyTree' AND linkkeyid1 = ? AND propertytreenodeid = ? AND changelogstatus = 'Checked Out'";
        DataSet nodeChangeLogs = queryProcessor.getPreparedSqlDataSet(sql, new Object[]{pTreeId, nodeId});
        if (nodeChangeLogs.getRowCount() > 0) {
            PropertyList props = new PropertyList();
            if (extraProps != null) {
                props.putAll(extraProps);
            }
            if ("Deleted".equals(props.getProperty("changelogstatus")) || "Renamed".equals(props.getProperty("changelogstatus"))) {
                PropertyList checkInProps = new PropertyList();
                checkInProps.setProperty("changelogid", nodeChangeLogs.getColumnValues("changelogid", ";"));
                checkInProps.setProperty("deleteflag", "Y");
                actionProcessor.processAction("CheckInSDI", "1", checkInProps);
            }
            props.setProperty("sdcid", "LV_ChangeLog");
            props.setProperty("keyid1", nodeChangeLogs.getColumnValues("changelogid", ";"));
            if ("Renamed".equals(props.getProperty("changelogstatus"))) {
                props.setProperty("notes", extraProps.getProperty("auditreason"));
            }
            actionProcessor.processAction("EditSDI", "1", props);
        }
    }

    public static PropertyList checkOutSDI(String connectionId, String sdcId, String keyId1, String keyId2, String keyId3, String propertytreeNodeId, String changeRequestId, String checkedOutToDeptId, String mode, PropertyList extraProps) throws ActionException {
        PropertyList props = new PropertyList();
        if (extraProps != null) {
            props.putAll(extraProps);
        }
        props.setProperty("sdcid", sdcId);
        props.setProperty("keyid1", keyId1);
        props.setProperty("keyid2", keyId2);
        props.setProperty("keyid3", keyId3);
        if ("PropertyTree".equals(sdcId)) {
            props.setProperty("propertytreenodeid", propertytreeNodeId);
        }
        if ("add".equals(mode)) {
            props.setProperty("mode", mode);
        }
        if (changeRequestId != null && changeRequestId.length() > 0) {
            props.setProperty("changerequestid", changeRequestId);
        }
        if (checkedOutToDeptId != null && checkedOutToDeptId.length() > 0) {
            props.setProperty("departmentid", checkedOutToDeptId);
        }
        ActionProcessor ap = new ActionProcessor(connectionId);
        ap.processAction("CheckOutSDI", "1", props);
        return props;
    }

    public static void deletePropertyTreeNode(String propertyTreeId, String nodeId, boolean renameLinkedSDIs, DBAccess database, WebAdminProcessor wp) throws SapphireException {
        PropertyTree dbPropertyTree = PropertyTreeUtil.getPropertyTree(database, propertyTreeId, false);
        Node deleteMe = dbPropertyTree.getNode(nodeId);
        Node deleteMeParent = deleteMe.getParent();
        NodeList children = deleteMe.getNodeList();
        for (int i = 0; i < children.size(); ++i) {
            Node child = (Node)children.get(i);
            dbPropertyTree.moveNode(child.getNodeId(), deleteMeParent == null ? "" : deleteMeParent.getNodeId());
        }
        if (renameLinkedSDIs) {
            try {
                wp.renameWebPagePropertyTreeNode(propertyTreeId, deleteMe.getNodeId(), "__root");
            }
            catch (Exception e) {
                throw new SapphireException(e);
            }
        }
        dbPropertyTree.deleteNode(deleteMe.getNodeId());
        PropertyTreeUtil.setPropertyTreeValue(database, "(import)", propertyTreeId, dbPropertyTree.toXMLString());
    }

    public static String getAttachmentIconHTML(long attSize, TranslationProcessor tp) {
        String html = "";
        if (attSize > 0L) {
            String attSizeText = "";
            attSizeText = attSize < 1024L ? attSize + " B." : (attSize < 0x100000L ? attSize / 1024L + " KB." : (attSize < 0x40000000L ? attSize / 0x100000L + " MB." : attSize / 0x40000000L + " GB."));
            String attImgSrc = "";
            attImgSrc = attSize > 0x500000L ? "<img height='16' width='16' title='" + tp.translate("Total Attachment Size: ") + attSizeText + "' src='rc?command=image&image=FlatBlackPaperclipRotated&color=red&size=48'/>" : "<img height='16' width='16' title='" + tp.translate("Total Attachment Size: ") + attSizeText + "' src='rc?command=image&image=FlatBlackPaperclipRotated&color=black&size=48'/>";
            if (attImgSrc.length() > 0) {
                html = "&nbsp;'" + attImgSrc;
            }
        }
        return html;
    }

    public static DBAccess getDBAccess(String connectionId, File rakFile) throws SapphireException {
        DBUtil dbu = new DBUtil();
        try {
            if (rakFile == null) {
                ConnectionProcessor connProcessor = rakFile == null ? new ConnectionProcessor(connectionId) : new ConnectionProcessor(rakFile, connectionId);
                ConnectionInfo connectionInfo = connProcessor.getConnectionInfo(connectionId);
                SapphireDatabase database = Configuration.getInstance().getSapphireDatabase(connectionInfo.getDatabaseId());
                DataSource dataSource = ServiceLocator.getInstance().getDataSource(database.getJndiname());
                dbu.setConnection(database.getDbms(), dataSource.getConnection());
            } else {
                if (!"ORA".equalsIgnoreCase(ConnectionDetails.TESTDB_DBMS)) {
                    dbu.setDatabase(ConnectionDetails.TESTDB_INSTANCENAME, ConnectionDetails.TESTDB_SERVERNAME, ConnectionDetails.TESTDB_PORT, ConnectionDetails.TESTDB_SQLDATABASE, ConnectionDetails.TESTDB_USERNAME, ConnectionDetails.TESTDB_PASSWORD);
                } else {
                    dbu.setDatabase(ConnectionDetails.TESTDB_SERVERNAME, ConnectionDetails.TESTDB_PORT, ConnectionDetails.TESTDB_SID, ConnectionDetails.TESTDB_USERNAME, ConnectionDetails.TESTDB_PASSWORD);
                }
                dbu.getConnection().setAutoCommit(true);
            }
        }
        catch (Exception e) {
            throw new SapphireException("DB_Access_Error", "FAILURE", "Failed to instantiate DBAccess.");
        }
        return dbu;
    }

    public static DataSet getSDIsLinkedToNode(String propertytreeid, String nodeId, String connectionId) {
        QueryProcessor qp = new QueryProcessor(connectionId);
        DataSet resultSDIs = new DataSet();
        SafeSQL safeSQL = new SafeSQL();
        String sql = "SELECT propertytreetype FROM propertytree WHERE propertytreeid = " + safeSQL.addVar(propertytreeid);
        DataSet ds = qp.getPreparedSqlDataSet(sql, safeSQL.getValues());
        if (ds.getRowCount() > 0) {
            String propertyTreeType = ds.getString(0, "propertytreetype", "");
            safeSQL = new SafeSQL();
            if ("Element".equals(propertyTreeType) || "Layout".equals(propertyTreeType) || "Page Type".equals(propertyTreeType)) {
                sql = "SELECT DISTINCT 'WebPage' sdcid, webpageid keyid1, productedition keyid2, '' keyid3 FROM webpagepropertytree WHERE propertytreeid = " + safeSQL.addVar(propertytreeid) + " AND extendnodeid = " + safeSQL.addVar(nodeId);
                resultSDIs = qp.getPreparedSqlDataSet(sql, safeSQL.getValues());
            } else if ("Gizmo".equals(propertyTreeType)) {
                sql = "SELECT 'LV_GizmoDef' sdcid, gizmodefid keyid1, '' keyid2, '' keyid3 FROM gizmodef  WHERE propertytreeid = " + safeSQL.addVar(propertytreeid) + " AND extendnodeid = " + safeSQL.addVar(nodeId);
                resultSDIs = qp.getPreparedSqlDataSet(sql, safeSQL.getValues());
            } else if ("Step".equals(propertyTreeType)) {
                sql = "SELECT DISTINCT 'LV_TaskDef' sdcid, taskdefid keyid1, taskdefversionid keyid2, '' keyid3 FROM taskdefstep WHERE propertytreeid = " + safeSQL.addVar(propertytreeid) + " AND extendnodeid = " + safeSQL.addVar(nodeId);
                resultSDIs = qp.getPreparedSqlDataSet(sql, safeSQL.getValues());
            } else if ("CollectorType".equals(propertyTreeType)) {
                sql = "SELECT 'Instrument' sdcid, instrumentid keyid1, '' keyid2, '' keyid3 FROM instrument WHERE collectorpropertytreeid = " + safeSQL.addVar(propertytreeid) + " AND collectorextendnodeid = " + safeSQL.addVar(nodeId) + " UNION  SELECT 'LV_InstrumentModel' sdcid, instrumentmodel keyid1, '' keyid2, '' keyid3 FROM instrumentmodel WHERE collectorpropertytreeid = " + safeSQL.addVar(propertytreeid) + " AND collectorextendnodeid = " + safeSQL.addVar(nodeId);
                resultSDIs = qp.getPreparedSqlDataSet(sql, safeSQL.getValues());
            } else if ("WorksheetItem".equals(propertyTreeType)) {
                sql = "SELECT DISTINCT 'LV_Worksheet' sdcid, worksheetid keyid1, worksheetversionid keyid2, '' keyid3 FROM worksheetitem WHERE propertytreeid = " + safeSQL.addVar(propertytreeid) + " AND sourcenodeid = " + safeSQL.addVar(nodeId);
                resultSDIs = qp.getPreparedSqlDataSet(sql, safeSQL.getValues());
            } else if ("FileRepository".equals(propertyTreeType)) {
                sql = "SELECT DISTINCT sdcid, keyid1, keyid2, keyid3 FROM sdiattachment WHERE attachmentrepositoryid = " + safeSQL.addVar(propertytreeid) + " AND attachmentrepositorynodeid = " + safeSQL.addVar(nodeId);
                resultSDIs = qp.getPreparedSqlDataSet(sql, safeSQL.getValues());
                for (int i = 0; i < resultSDIs.getRowCount(); ++i) {
                    if ("(null)".equalsIgnoreCase(resultSDIs.getString(i, "keyid2", ""))) {
                        resultSDIs.getString(i, "keyid2", "");
                    }
                    if (!"(null)".equalsIgnoreCase(resultSDIs.getString(i, "keyid3", ""))) continue;
                    resultSDIs.getString(i, "keyid3", "");
                }
            } else if ("ScheduleTask".equals(propertyTreeType)) {
                sql = "SELECT DISTINCT scheduleplanid FROM scheduleplanitem WHERE propertytreeid = " + safeSQL.addVar(propertytreeid) + " AND scheduletasknodeid = " + safeSQL.addVar(nodeId);
                resultSDIs = qp.getPreparedSqlDataSet(sql, safeSQL.getValues());
            }
        }
        return resultSDIs;
    }

    public static void validatePreNodeRename(String propertyTreeId, String nodeId, String connectionId) throws SapphireException {
        DataSet linkedSDIs = CMTUtil.getSDIsLinkedToNode(propertyTreeId, nodeId, connectionId);
        ArrayList<DataSet> groupedSDIs = linkedSDIs.getGroupedDataSets("sdcid");
        for (int i = 0; i < groupedSDIs.size(); ++i) {
            DataSet linkedSDIsSDC = groupedSDIs.get(i);
            String groupSDCId = linkedSDIsSDC.getString(0, "sdcid");
            OperationRequireCheckOut.Result result = OperationRequireCheckOut.doOperationRequireCheckout(groupSDCId, linkedSDIsSDC.getColumnValues("keyid1", ";"), linkedSDIsSDC.getString(0, "keyid2", "").length() == 0 ? "" : linkedSDIsSDC.getColumnValues("keyid2", ";"), linkedSDIsSDC.getString(0, "keyid3", "").length() == 0 ? "" : linkedSDIsSDC.getColumnValues("keyid3", ";"), "", connectionId, null);
            if (result.checkedOutToOthers.getRowCount() > 0) {
                throw new SapphireException("SDIs linked to this node are not Checked out to current user. Cannot proceed.");
            }
            if (result.checkedOutToMe.getRowCount() > 0) {
                // empty if block
            }
            if (result.checkOutAble.getRowCount() <= 0) continue;
            throw new SapphireException("SDIs linked to this node are not Checked out to current user. Cannot proceed.");
        }
    }

    public static Optional<List<SnapshotItem>> getChangeLogSnapshotItem(String changeLogId, String connectionId) throws SapphireException {
        SnapshotFactory snapshotFactory = new SnapshotFactory(connectionId);
        SnapshotPackage pkg = snapshotFactory.packageFromChangeLog(changeLogId, "");
        if (pkg == null || pkg.getRequestedSnapshots().size() == 0) {
            throw new SapphireException("Unable to package Snapshot from ChangeLog.");
        }
        List<SnapshotItem> snapshotItems = pkg.getRequestedSnapshotItems();
        Optional<List<SnapshotItem>> snapshotItemsOpt = Optional.empty();
        if (snapshotItems != null && snapshotItems.size() > 0) {
            snapshotItemsOpt = Optional.of(snapshotItems);
        }
        return snapshotItemsOpt;
    }

    public static String getSnapshotVersionStatus(SnapshotItem snapshotItem, SDCProcessor sdcProcessor) {
        String versionStatus = "";
        SDISnapshotItem sdiSnapshotItem = (SDISnapshotItem)snapshotItem;
        if (!Snapshot.Type.PROPERTYTREE.equals((Object)snapshotItem.getType()) && !sdiSnapshotItem.isDeleted() && "Y".equals(sdcProcessor.getProperty(snapshotItem.getSDCId(), "versionedflag"))) {
            SDIData sdiData = snapshotItem.getSDIData();
            DataSet primary = sdiData.getDataset("primary");
            switch (versionStatus = primary.getString(0, "versionstatus", "")) {
                case "P": {
                    versionStatus = "Provisional";
                    break;
                }
                case "A": {
                    versionStatus = "Active";
                    break;
                }
                case "E": {
                    versionStatus = "Expired";
                    break;
                }
                case "C": {
                    versionStatus = "Current";
                }
            }
        }
        return versionStatus;
    }

    static {
        EXCLUDED_ATTACHMENT_TYPES.add("P");
        EXCLUDED_ATTACHMENT_TYPES.add("M");
        EXCLUDED_ATTACHMENT_TYPES.add("L");
        OOB_CHANGE_CONTROLLED_YES_SDC_LIST.add("LV_Phrase");
        OOB_CHANGE_CONTROLLED_YES_SDC_LIST.add("ApprovalType");
        OOB_CHANGE_CONTROLLED_YES_SDC_LIST.add("LimitRule");
        OOB_CHANGE_CONTROLLED_YES_SDC_LIST.add("LimitType");
        OOB_CHANGE_CONTROLLED_YES_SDC_LIST.add("Units");
        OOB_CHANGE_CONTROLLED_YES_SDC_LIST.add("Param");
        OOB_CHANGE_CONTROLLED_YES_SDC_LIST.add("ParamList");
        OOB_CHANGE_CONTROLLED_YES_SDC_LIST.add("SpecSDC");
        OOB_CHANGE_CONTROLLED_YES_SDC_LIST.add("WorkItem");
        OOB_CHANGE_CONTROLLED_YES_SDC_LIST.add("LV_ChildSamplePlan");
        OOB_CHANGE_CONTROLLED_YES_SDC_LIST.add("LV_AssayType");
        OOB_CHANGE_CONTROLLED_PARENT_SDC_LIST.add("LV_Strain");
        OOB_CHANGE_CONTROLLED_YES_SDC_LIST.add("LV_Species");
        OOB_CHANGE_CONTROLLED_YES_SDC_LIST.add("LV_Treatment");
        OOB_CHANGE_CONTROLLED_YES_SDC_LIST.add("LV_PrepType");
        OOB_CHANGE_CONTROLLED_YES_SDC_LIST.add("LV_CollectMeth");
        OOB_CHANGE_CONTROLLED_YES_SDC_LIST.add("LV_Metastasis");
        OOB_CHANGE_CONTROLLED_YES_SDC_LIST.add("LV_Tissue");
        OOB_CHANGE_CONTROLLED_YES_SDC_LIST.add("LV_ClinicalDiag");
        OOB_CHANGE_CONTROLLED_YES_SDC_LIST.add("LV_Disease");
        OOB_CHANGE_CONTROLLED_YES_SDC_LIST.add("Location");
        OOB_CHANGE_CONTROLLED_YES_SDC_LIST.add("LV_CountRule");
        OOB_CHANGE_CONTROLLED_YES_SDC_LIST.add("LV_ProdVariantRule");
        OOB_CHANGE_CONTROLLED_YES_SDC_LIST.add("LV_SamplingPlan");
        OOB_CHANGE_CONTROLLED_PARENT_SDC_LIST.add("LV_VendorItem");
        OOB_CHANGE_CONTROLLED_YES_SDC_LIST.add("LV_ReagentType");
        OOB_CHANGE_CONTROLLED_PARENT_SDC_LIST.add("LV_ProdVariant");
        OOB_CHANGE_CONTROLLED_PARENT_SDC_LIST.add("LV_ProductIngredient");
        OOB_CHANGE_CONTROLLED_PARENT_SDC_LIST.add("LV_ProductStage");
        OOB_CHANGE_CONTROLLED_PARENT_SDC_LIST.add("LV_ProductInstrument");
        OOB_CHANGE_CONTROLLED_YES_SDC_LIST.add("Product");
        OOB_CHANGE_CONTROLLED_YES_SDC_LIST.add("SampleType");
        OOB_CHANGE_CONTROLLED_YES_SDC_LIST.add("SamplePoint");
        OOB_CHANGE_CONTROLLED_YES_SDC_LIST.add("Component");
        OOB_CHANGE_CONTROLLED_YES_SDC_LIST.add("Material");
        OOB_CHANGE_CONTROLLED_YES_SDC_LIST.add("ContainerType");
        OOB_CHANGE_CONTROLLED_YES_SDC_LIST.add("StorageEnvSDC");
        OOB_CHANGE_CONTROLLED_YES_SDC_LIST.add("StorageCondTypeSDC");
        OOB_CHANGE_CONTROLLED_YES_SDC_LIST.add("PhysicalStore");
        OOB_CHANGE_CONTROLLED_YES_SDC_LIST.add("LV_Box");
        OOB_CHANGE_CONTROLLED_PARENT_SDC_LIST.add("LV_RequestItem");
        OOB_CHANGE_CONTROLLED_PARENT_SDC_LIST.add("LV_RequestItemDetail");
        OOB_CHANGE_CONTROLLED_TEMPLATE_SDC_LIST.add("Request");
        OOB_CHANGE_CONTROLLED_TEMPLATE_SDC_LIST.add("LV_BatchStage");
        OOB_CHANGE_CONTROLLED_TEMPLATE_SDC_LIST.add("Batch");
        OOB_CHANGE_CONTROLLED_PARENT_SDC_LIST.add("WorkOrderSDC");
        OOB_CHANGE_CONTROLLED_PARENT_SDC_LIST.add("LV_ActionPlan");
        OOB_CHANGE_CONTROLLED_PARENT_SDC_LIST.add("LV_IncdtFind");
        OOB_CHANGE_CONTROLLED_TEMPLATE_SDC_LIST.add("LV_Incdt");
        OOB_CHANGE_CONTROLLED_PARENT_SDC_LIST.add("LV_RestClass");
        OOB_CHANGE_CONTROLLED_PARENT_SDC_LIST.add("LV_StudySite");
        OOB_CHANGE_CONTROLLED_PARENT_SDC_LIST.add("LV_ClinicalEvnt");
        OOB_CHANGE_CONTROLLED_PARENT_SDC_LIST.add("LV_EventDef");
        OOB_CHANGE_CONTROLLED_PARENT_SDC_LIST.add("LV_ClinicalProtocol");
        OOB_CHANGE_CONTROLLED_YES_SDC_LIST.add("Study");
        OOB_CHANGE_CONTROLLED_TEMPLATE_SDC_LIST.add("Sample");
        OOB_CHANGE_CONTROLLED_YES_SDC_LIST.add("Instrument");
        OOB_CHANGE_CONTROLLED_YES_SDC_LIST.add("LV_InstrumentModel");
        OOB_CHANGE_CONTROLLED_YES_SDC_LIST.add("LV_InstrumentType");
        OOB_CHANGE_CONTROLLED_YES_SDC_LIST.add("Address");
        OOB_CHANGE_CONTROLLED_YES_SDC_LIST.add("LV_Field");
        OOB_CHANGE_CONTROLLED_YES_SDC_LIST.add("LV_Formlet");
        OOB_CHANGE_CONTROLLED_YES_SDC_LIST.add("LV_Form");
        OOB_CHANGE_CONTROLLED_YES_SDC_LIST.add("LV_ArrayType");
        OOB_CHANGE_CONTROLLED_YES_SDC_LIST.add("LV_ArrayLayout");
        OOB_CHANGE_CONTROLLED_YES_SDC_LIST.add("LV_ArrayMethod");
        OOB_CHANGE_CONTROLLED_YES_SDC_LIST.add("LV_ArrayTransferMethod");
        OOB_CHANGE_CONTROLLED_PARENT_SDC_LIST.add("LV_WorksheetSection");
        OOB_CHANGE_CONTROLLED_PARENT_SDC_LIST.add("LV_WorksheetItem");
        OOB_CHANGE_CONTROLLED_TEMPLATE_SDC_LIST.add("LV_Worksheet");
        OOB_CHANGE_CONTROLLED_PARENT_SDC_LIST.add("ModuleSDC");
        OOB_CHANGE_CONTROLLED_TEMPLATE_SDC_LIST.add("User");
        OOB_CHANGE_CONTROLLED_YES_SDC_LIST.add("Role");
        OOB_CHANGE_CONTROLLED_YES_SDC_LIST.add("Department");
        OOB_CHANGE_CONTROLLED_YES_SDC_LIST.add("LV_JobType");
        OOB_CHANGE_CONTROLLED_YES_SDC_LIST.add("LV_SecuritySet");
        OOB_CHANGE_CONTROLLED_YES_SDC_LIST.add("LV_ExternalApp");
        OOB_CHANGE_CONTROLLED_YES_SDC_LIST.add("LV_Statement");
        OOB_CHANGE_CONTROLLED_YES_SDC_LIST.add("SDC");
        OOB_CHANGE_CONTROLLED_YES_SDC_LIST.add("Action");
        OOB_CHANGE_CONTROLLED_YES_SDC_LIST.add("RefType");
        OOB_CHANGE_CONTROLLED_YES_SDC_LIST.add("Category");
        OOB_CHANGE_CONTROLLED_YES_SDC_LIST.add("Query");
        OOB_CHANGE_CONTROLLED_YES_SDC_LIST.add("Language");
        OOB_CHANGE_CONTROLLED_YES_SDC_LIST.add("LV_EditorStyle");
        OOB_CHANGE_CONTROLLED_YES_SDC_LIST.add("ExpressionSDC");
        OOB_CHANGE_CONTROLLED_YES_SDC_LIST.add("WebPage");
        OOB_CHANGE_CONTROLLED_YES_SDC_LIST.add("PropertyTree");
        OOB_CHANGE_CONTROLLED_YES_SDC_LIST.add("LV_GizmoDef");
        OOB_CHANGE_CONTROLLED_YES_SDC_LIST.add("LV_ImageRef");
        OOB_CHANGE_CONTROLLED_YES_SDC_LIST.add("Report");
        OOB_CHANGE_CONTROLLED_YES_SDC_LIST.add("LV_LabelMethod");
        OOB_CHANGE_CONTROLLED_YES_SDC_LIST.add("SchedulePlan");
        OOB_CHANGE_CONTROLLED_YES_SDC_LIST.add("SchedulePlanItem");
        OOB_CHANGE_CONTROLLED_YES_SDC_LIST.add("Task");
        OOB_CHANGE_CONTROLLED_YES_SDC_LIST.add("LV_EventType");
        OOB_CHANGE_CONTROLLED_YES_SDC_LIST.add("LV_EventPlan");
        OOB_CHANGE_CONTROLLED_YES_SDC_LIST.add("LV_Calendar");
        OOB_CHANGE_CONTROLLED_YES_SDC_LIST.add("LV_ConnectorType");
        OOB_CHANGE_CONTROLLED_YES_SDC_LIST.add("LV_TaskDef");
        OOB_CHANGE_CONTROLLED_YES_SDC_LIST.add("LV_WorkflowDef");
        OOB_CHANGE_CONTROLLED_YES_SDC_LIST.add("LV_DataFileDef");
        OOB_CHANGE_CONTROLLED_YES_SDC_LIST.add("LV_SAPMsgType");
        OOB_CHANGE_CONTROLLED_YES_SDC_LIST.add("LV_MessageType");
        OOB_CHANGE_CONTROLLED_YES_SDC_LIST.add("LV_FormulationMethod");
        OOB_CHANGE_CONTROLLED_PARENT_SDC_LIST.add("QCMethodSampleType");
        OOB_CHANGE_CONTROLLED_YES_SDC_LIST.add("QCMethod");
        OOB_CHANGE_CONTROLLED_YES_SDC_LIST.add("QCEvalRule");
        OOB_CHANGE_CONTROLLED_YES_SDC_LIST.add("ProtocolSDC");
        OOB_CHANGE_CONTROLLED_YES_SDC_LIST.add("LV_ScheduleGroup");
        OOB_CHANGE_CONTROLLED_YES_SDC_LIST.add("LV_AttachmentHandler");
        OOB_CHANGE_CONTROLLED_YES_SDC_LIST.add("LV_Portal");
        OOB_CHANGE_CONTROLLED_YES_SDC_LIST.add("LV_App");
    }
}

