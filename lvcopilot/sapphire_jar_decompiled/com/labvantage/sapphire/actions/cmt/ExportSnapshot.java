/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.cmt;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.actions.cmt.ExportValidation;
import com.labvantage.sapphire.cmt.SnapshotFactory;
import com.labvantage.sapphire.cmt.SnapshotPackage;
import com.labvantage.sapphire.util.file.FileManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import sapphire.SapphireException;
import sapphire.accessor.AttachmentProcessor;
import sapphire.accessor.DAMProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.action.BaseAction;
import sapphire.attachment.Attachment;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class ExportSnapshot
extends BaseAction
implements sapphire.action.ExportSnapshot {
    static final String LABVANTAGE_CVS_ID = "$Revision: 1.1 $";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        boolean isFromChangeControl;
        String sdcId = properties.getProperty("sdcid");
        String keyId1 = properties.getProperty("keyid1");
        String keyId2 = properties.getProperty("keyid2");
        String keyId3 = properties.getProperty("keyid3");
        String policyNodeId = properties.getProperty("policynodeid");
        String mode = properties.getProperty("cmt-propertytreemode");
        boolean verboseLog = "Y".equals(properties.getProperty("verboselog"));
        SnapshotPackage snapshotPackageCache = ExportValidation.getCachedSnapshotPackage(this.getConnectionid());
        String snapshotLog = "";
        SnapshotFactory snapshotUtil = null;
        String fileName = properties.getProperty("filename").length() > 0 ? properties.getProperty("filename") : ("PropertyTree".equals(sdcId) ? sdcId + "_" + keyId1 + "_" + mode : sdcId + "_" + StringUtil.escapeXMLAttributeValue(keyId1) + "_" + StringUtil.escapeXMLAttributeValue(keyId2) + "_" + StringUtil.escapeXMLAttributeValue(keyId3) + "_" + StringUtil.escapeXMLAttributeValue(policyNodeId));
        boolean bl = isFromChangeControl = "LV_ChangeRequest".equals(sdcId) || "LV_ChangeLog".equals(sdcId);
        if (snapshotPackageCache == null || isFromChangeControl) {
            snapshotUtil = new SnapshotFactory(this.getConnectionId());
            snapshotUtil.setVerbose(verboseLog);
            if (isFromChangeControl) {
                boolean isExportPreImage = "Y".equals(properties.getProperty("exportpreimage", "N"));
                StringBuilder postImageCLIds = new StringBuilder();
                StringBuilder preImageCLIds = new StringBuilder();
                ExportSnapshot.determineExportableCLs(sdcId, keyId1, postImageCLIds, preImageCLIds, isExportPreImage, this.getDAMProcessor(), this.getQueryProcessor(), this.database.isOracle());
                if (postImageCLIds.length() < 1) {
                    throw new SapphireException(this.getTranslationProcessor().translate("No Change logs found under the selected Change Requests:") + keyId1);
                }
                snapshotPackageCache = snapshotUtil.packageFromChangeLog(postImageCLIds.substring(1), preImageCLIds.length() > 1 ? preImageCLIds.substring(1) : "");
            } else {
                boolean isAdhocTransfer;
                boolean bl2 = isAdhocTransfer = !isFromChangeControl && "Y".equals(properties.getProperty("istransfer", "Y"));
                if ("PropertyTree".equals(sdcId)) {
                    String nodeList = properties.getProperty("nodelist");
                    int noOfItems = StringUtil.split(keyId1, ";").length;
                    if ("__FULL".equals(mode)) {
                        nodeList = StringUtil.repeat("__FULL", noOfItems, ";");
                    } else if ("__DEFINITION".equals(mode)) {
                        nodeList = StringUtil.repeat("__DEFINITION", noOfItems, ";");
                    }
                    snapshotPackageCache = snapshotUtil.packagePropertyTreeSnapshot(keyId1, nodeList);
                } else {
                    snapshotPackageCache = snapshotUtil.packageSnapshot(sdcId, keyId1, keyId2, keyId3, policyNodeId, isAdhocTransfer);
                }
            }
            snapshotLog = snapshotUtil.getLog();
            properties.setProperty("exportlog", snapshotLog);
        } else {
            snapshotLog = ExportValidation.getCachedLog(this.getConnectionid());
            properties.setProperty("exportlog", snapshotLog);
            ExportValidation.removeSnapshotPackageFromCache(this.getConnectionid());
        }
        if (!fileName.toLowerCase().endsWith(".zip")) {
            fileName = fileName + ".zip";
        }
        FileManager.TempFile tempZipFile = new FileManager.TempFile(fileName, FileManager.TempSource.DOWNLOAD, this.getConnectionId());
        File file = tempZipFile.getData().getFile().toFile();
        file.getParentFile().mkdirs();
        String tempid = tempZipFile.setTempFile("Sapphire Custom", this.getConnectionId(), this.getActionProcessor());
        properties.setProperty("tempid", tempid);
        snapshotPackageCache.toFile(file.getParent(), file.getName(), this.getConnectionId(), this.getRakFile());
        properties.setProperty("zipfilepath", file.getAbsolutePath());
        String transferuuid = snapshotPackageCache.getUUID();
        PropertyList actionProps = new PropertyList();
        actionProps.setProperty("sdcid", "TransferLog");
        actionProps.setProperty("transferlogid", "(auto)");
        String transferlogdesc = properties.getProperty("transferlogdesc");
        actionProps.setProperty("transferlogdesc", transferlogdesc);
        actionProps.setProperty("notes", properties.getProperty("transferlognotes"));
        actionProps.setProperty("transferlog", snapshotLog);
        actionProps.setProperty("transferstatus", "Completed");
        actionProps.setProperty("lasttransferreddt", "n");
        actionProps.setProperty("lasttransferredby", this.connectionInfo.getSysuserId());
        actionProps.setProperty("transferuuid", transferuuid);
        actionProps.setProperty("transfertype", "Export");
        this.getActionProcessor().processAction("AddSDI", "1", actionProps);
        String transferlogid = actionProps.getProperty("newkeyid1");
        Attachment attachment = Attachment.getAttachment("TransferLog", transferlogid, null, null);
        attachment.setDescription(properties.getProperty("filename") + ".zip");
        try {
            attachment.setInputStream(new FileInputStream(file));
        }
        catch (FileNotFoundException e) {
            throw new SapphireException(ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())));
        }
        attachment.setSourceFilename(properties.getProperty("filename") + ".zip");
        attachment.setAttachmentType(Attachment.AttachmentType.FILE);
        AttachmentProcessor ap = new AttachmentProcessor(this.getConnectionid());
        ap.addSDIAttachment(attachment, false, false, "Sapphire Custom");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void determineExportableCLs(String sdcId, String keyId1, StringBuilder returnPostImageCLIds, StringBuilder returnPreImageCLIds, boolean isExportPreImage, DAMProcessor damProcessor, QueryProcessor queryProcessor, boolean isOracle) throws SapphireException {
        String changeLogRSetId = "";
        DataSet changelogsDS = null;
        try {
            if ("LV_ChangeLog".equals(sdcId)) {
                changeLogRSetId = damProcessor.createRSet("LV_ChangeLog", keyId1, "", "");
            }
            SafeSQL safeSQL = new SafeSQL();
            String sqlSelectClause = "SELECT changelogid, linksdcid, linkkeyid1, linkkeyid2, linkkeyid3" + (isOracle ? ", NVL(propertytreenodeid, '__FULL')" : ", ISNULL(propertytreenodeid, '__FULL')") + " propertytreenodeid" + (isOracle ? ", NVL2(originalsnapshot, '1', '0')" : ", IIF(originalsnapshot IS NULL, '0', '1')") + " originalsnapshotexists, changerequestid, changelogstatus, checkedindt FROM changelog";
            String sqlWhereClauseAlways = " AND changelogstatus NOT IN ( 'Checked Out', 'CheckOut Aborted', 'CheckOut Rolledback' )";
            String sqlOrderClause = " ORDER BY linksdcid, linkkeyid1, linkkeyid2, linkkeyid3, propertytreenodeid, checkedindt DESC";
            String sql = "";
            sql = "LV_ChangeRequest".equals(sdcId) ? sqlSelectClause + " WHERE changerequestid IN (" + safeSQL.addIn(keyId1, ";") + ")" + sqlWhereClauseAlways + sqlOrderClause : (isExportPreImage ? sqlSelectClause + " WHERE changerequestid IN (  SELECT DISTINCT changerequestid FROM changelog WHERE changelogid IN (SELECT keyid1 FROM rsetitems WHERE rsetid = " + safeSQL.addVar(changeLogRSetId) + ")) " + sqlWhereClauseAlways + " UNION " + sqlSelectClause + " WHERE changelogid IN (SELECT keyid1 FROM rsetitems WHERE rsetid = " + safeSQL.addVar(changeLogRSetId) + ")" + sqlWhereClauseAlways + sqlOrderClause : sqlSelectClause + " WHERE changelogid IN ( SELECT keyid1 FROM rsetitems WHERE rsetid = " + safeSQL.addVar(changeLogRSetId) + ")" + sqlWhereClauseAlways + sqlOrderClause);
            changelogsDS = queryProcessor.getPreparedSqlDataSet(sql, safeSQL.getValues());
        }
        finally {
            if (changeLogRSetId.length() > 0) {
                damProcessor.clearRSet(changeLogRSetId);
            }
        }
        List<String> requestedChangeLogIds = null;
        if ("LV_ChangeLog".equals(sdcId)) {
            int i;
            requestedChangeLogIds = Arrays.asList(StringUtil.split(keyId1, ";"));
            DataSet requestedUniqueSDIs = new DataSet();
            HashMap<String, String> findMap = new HashMap<String, String>();
            for (i = 0; i < changelogsDS.getRowCount(); ++i) {
                if (!requestedChangeLogIds.contains(changelogsDS.getString(i, "changelogid"))) continue;
                findMap.clear();
                findMap.put("linksdcid", changelogsDS.getString(i, "linksdcid"));
                findMap.put("linkkeyid1", changelogsDS.getString(i, "linkkeyid1"));
                findMap.put("linkkeyid2", changelogsDS.getString(i, "linkkeyid2"));
                findMap.put("linkkeyid3", changelogsDS.getString(i, "linkkeyid3"));
                findMap.put("propertytreenodeid", changelogsDS.getString(i, "propertytreenodeid"));
                if (requestedUniqueSDIs.findRow(findMap) != -1) continue;
                requestedUniqueSDIs.copyRow(changelogsDS, i, 1);
            }
            for (i = changelogsDS.getRowCount() - 1; i >= 0; --i) {
                findMap.clear();
                findMap.put("linksdcid", changelogsDS.getString(i, "linksdcid"));
                findMap.put("linkkeyid1", changelogsDS.getString(i, "linkkeyid1"));
                findMap.put("linkkeyid2", changelogsDS.getString(i, "linkkeyid2"));
                findMap.put("linkkeyid3", changelogsDS.getString(i, "linkkeyid3"));
                findMap.put("propertytreenodeid", changelogsDS.getString(i, "propertytreenodeid"));
                if (requestedUniqueSDIs.findRow(findMap) != -1) continue;
                changelogsDS.deleteRow(i);
            }
        }
        ArrayList<DataSet> uniqueSDIList = changelogsDS.getGroupedDataSets("linksdcid,linkkeyid1,linkkeyid2,linkkeyid3");
        String[] changelogids = StringUtil.split(keyId1, ";");
        for (DataSet groupedsdichangeDS : uniqueSDIList) {
            String preImagechangelogid;
            String changeLogStatus;
            String linkSDCId = groupedsdichangeDS.getString(0, "linksdcid", "");
            if ("PropertyTree".equals(linkSDCId)) {
                String nodeId;
                ArrayList<DataSet> nodeGroups = groupedsdichangeDS.getGroupedDataSets("propertytreenodeid");
                Calendar lastFullCheckInCal = null;
                boolean isPTreeDeleted = false;
                for (DataSet nodeGroup : nodeGroups) {
                    String changelogid;
                    nodeId = nodeGroup.getString(0, "propertytreenodeid");
                    if (!"__FULL".equals(nodeId)) continue;
                    String changeLogStatus2 = nodeGroup.getValue(0, "changelogstatus");
                    if ("Deleted".equals(changeLogStatus2)) {
                        isPTreeDeleted = true;
                        changelogid = nodeGroup.getValue(0, "changelogid");
                        returnPostImageCLIds.append(";" + changelogid);
                        returnPreImageCLIds.append(";");
                        break;
                    }
                    lastFullCheckInCal = nodeGroup.getCalendar(0, "checkedindt");
                    changelogid = nodeGroup.getValue(0, "changelogid");
                    String preImagechangelogid2 = nodeGroup.getValue(nodeGroup.getRowCount() - 1, "changelogid");
                    returnPostImageCLIds.append(";" + changelogid);
                    if (isExportPreImage) {
                        returnPreImageCLIds.append(";" + preImagechangelogid2);
                        break;
                    }
                    returnPreImageCLIds.append(";");
                    break;
                }
                if (isPTreeDeleted) continue;
                for (DataSet nodeGroup : nodeGroups) {
                    nodeId = nodeGroup.getString(0, "propertytreenodeid");
                    if ("__FULL".equals(nodeId)) continue;
                    Calendar nodeCheckInCal = nodeGroup.getCalendar(0, "checkedindt");
                    String changeLogStatus3 = nodeGroup.getValue(0, "changelogstatus");
                    if (lastFullCheckInCal != null && (nodeCheckInCal == null || nodeCheckInCal.compareTo(lastFullCheckInCal) <= 0) && !"Deleted".equals(changeLogStatus3) && !"Renamed".equals(changeLogStatus3)) continue;
                    String changelogid = nodeGroup.getValue(0, "changelogid");
                    returnPostImageCLIds.append(";" + changelogid);
                    if (!"Deleted".equals(changeLogStatus3) && !"Renamed".equals(changeLogStatus3) && isExportPreImage) {
                        String preImagechangelogid3 = nodeGroup.getValue(nodeGroup.getRowCount() - 1, "changelogid");
                        returnPreImageCLIds.append(";" + preImagechangelogid3);
                        continue;
                    }
                    returnPreImageCLIds.append(";");
                }
                continue;
            }
            int row = 0;
            if ("LV_ChangeLog".equals(sdcId) && requestedChangeLogIds != null) {
                for (int selectedrow = 0; selectedrow < groupedsdichangeDS.getRowCount(); ++selectedrow) {
                    String currentchangelogid = groupedsdichangeDS.getValue(selectedrow, "changelogid");
                    if (!requestedChangeLogIds.contains(currentchangelogid)) continue;
                    row = selectedrow;
                    break;
                }
            }
            if ("Deleted".equals(changeLogStatus = groupedsdichangeDS.getValue(row, "changelogstatus"))) {
                String changelogid = groupedsdichangeDS.getValue(row, "changelogid");
                preImagechangelogid = "";
                preImagechangelogid = "1".equals(groupedsdichangeDS.getString(groupedsdichangeDS.getRowCount() - 1, "originalsnapshotexists", "")) ? groupedsdichangeDS.getString(groupedsdichangeDS.getRowCount() - 1, "changelogid") : groupedsdichangeDS.getString(row, "changelogid");
                returnPostImageCLIds.append(";" + changelogid);
                returnPreImageCLIds.append(";" + preImagechangelogid);
                continue;
            }
            String changelogid = groupedsdichangeDS.getValue(row, "changelogid");
            preImagechangelogid = groupedsdichangeDS.getValue(groupedsdichangeDS.getRowCount() - 1, "changelogid");
            returnPostImageCLIds.append(";" + changelogid);
            if (!isExportPreImage) {
                returnPreImageCLIds.append(";");
                continue;
            }
            returnPreImageCLIds.append(";" + preImagechangelogid);
        }
    }
}

