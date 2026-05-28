/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.cmt;

import com.labvantage.sapphire.actions.cmt.ExportSnapshot;
import com.labvantage.sapphire.cmt.CMTUtil;
import com.labvantage.sapphire.cmt.PropertyTreeSnapshot;
import com.labvantage.sapphire.cmt.PropertyTreeSnapshotItem;
import com.labvantage.sapphire.cmt.SDISnapshot;
import com.labvantage.sapphire.cmt.SDISnapshotItem;
import com.labvantage.sapphire.cmt.SnapshotFactory;
import com.labvantage.sapphire.cmt.SnapshotPackage;
import com.labvantage.sapphire.util.policy.CMTPolicy;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;
import sapphire.SapphireException;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.SDIProcessor;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.SDIRequest;
import sapphire.util.SafeHTML;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.cmt.Snapshot;
import sapphire.xml.cmt.SnapshotItem;

public class ExportValidation
extends BaseAction {
    private static HashMap<String, SoftReference<HashMap>> snapshotCache = new HashMap();

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        int i;
        boolean approvalRequired;
        String sdcid = properties.getProperty("sdcid");
        String keyid1 = properties.getProperty("keyid1");
        String keyid2 = properties.getProperty("keyid2");
        String keyid3 = properties.getProperty("keyid3");
        boolean verboseLog = "Y".equals(properties.getProperty("verboselog"));
        boolean checkVersionStatus = "Y".equals(properties.getProperty("versionstatuscheck", "N"));
        SDIProcessor sdiProcessor = this.getSDIProcessor();
        SDCProcessor sdcProcessor = this.getSDCProcessor();
        boolean isFromChangeControl = "LV_ChangeRequest".equals(sdcid) || "LV_ChangeLog".equals(sdcid);
        DataSet changeLogsDS = null;
        String[] columnids = new String[]{"sdcid", "keyid1", "keyid2", "keyid3", "keydisplay", "transfertype", "changecontrolledflag", "changelogstatus", "uuid", "changelogid", "changerequest"};
        String title_sdcid = "SDC ID";
        String title_sditoexport = "SDI To Export";
        String title_exportvalidation = "Export Validation";
        String title_fromchangelog = "Change Log (Change Request)";
        String[] columntitles = new String[]{title_sdcid, title_sditoexport, title_exportvalidation};
        if (isFromChangeControl) {
            ExportValidation.removeSnapshotPackageFromCache(this.getConnectionid());
            columntitles = new String[]{title_sdcid, title_sditoexport, title_fromchangelog, title_exportvalidation};
            String policynodeid = properties.getProperty("policynodeid");
            CMTPolicy cmtPolicy = CMTPolicy.getPolicy(this.getConnectionid(), sdcid, policynodeid);
            approvalRequired = cmtPolicy.isChangeRequestRequireApproval();
            SDIRequest sdiRequest = new SDIRequest();
            sdiRequest.setSDCid("LV_ChangeLog");
            StringBuilder changeLogIds = new StringBuilder();
            ExportSnapshot.determineExportableCLs(sdcid, keyid1, changeLogIds, new StringBuilder(), false, this.getDAMProcessor(), this.getQueryProcessor(), this.database.isOracle());
            sdiRequest.setKeyid1List(changeLogIds.toString());
            sdiRequest.setRequestItem("primary[changelogid, linksdcid sdcid,linkkeyid1 keyid1,linkkeyid2 keyid2,linkkeyid3 keyid3, propertytreenodeid, changelogstatus, changerequestid, changerequestid.changerequeststatus, 'Y' uuid]");
            changeLogsDS = sdiProcessor.getSDIData(sdiRequest).getDataset("primary");
            String incorrectstatuschangelogs = "";
            Object notapprovedchangelogs = "";
            for (int i2 = 0; i2 < changeLogsDS.getRowCount(); ++i2) {
                String sdcId = changeLogsDS.getValue(i2, "sdcid");
                if ("Checked Out".equals(changeLogsDS.getValue(i2, "changelogstatus")) || "CheckOut Aborted".equals(changeLogsDS.getValue(i2, "changelogstatus")) || "CheckOut Rolledback".equals(changeLogsDS.getValue(i2, "changelogstatus"))) {
                    incorrectstatuschangelogs = incorrectstatuschangelogs + "<br><br>" + changeLogsDS.getValue(i2, "changelogid") + "(" + changeLogsDS.getValue(i2, "sdcid") + "," + changeLogsDS.getValue(i2, "keyid1") + "," + changeLogsDS.getValue(i2, "keyid2") + "," + changeLogsDS.getValue(i2, "keyid3") + ("PropertyTree".equals(sdcId) ? ", " + changeLogsDS.getValue(i2, "propertytreenodeid", "__FULL") : "") + ")";
                }
                if (!approvalRequired || changeLogsDS.getString(i2, "changerequestid", "").length() <= 0 || "Approved".equals(changeLogsDS.getValue(i2, "changerequeststatus"))) continue;
                notapprovedchangelogs = notapprovedchangelogs + "<br><br>" + changeLogsDS.getValue(i2, "changelogid") + "(" + changeLogsDS.getValue(i2, "sdcid") + "," + changeLogsDS.getValue(i2, "keyid1") + "," + changeLogsDS.getValue(i2, "keyid2") + "," + changeLogsDS.getValue(i2, "keyid3") + ("PropertyTree".equals(sdcId) ? ", " + changeLogsDS.getValue(i2, "propertytreenodeid", "__FULL") : "") + ")";
            }
            if (isFromChangeControl && incorrectstatuschangelogs.length() > 1) {
                throw new SapphireException(this.getTranslationProcessor().translate("Export from Change Request or Change Log cannot proceed. The follow changes are in status Checked Out, CheckOut Aborted or Rolledback:") + incorrectstatuschangelogs);
            }
            if (isFromChangeControl && ((String)notapprovedchangelogs).length() > 1) {
                throw new SapphireException(this.getTranslationProcessor().translate("Export from Change Request or Change Log cannot proceed. The follow changes are not approved:") + (String)notapprovedchangelogs);
            }
            changeLogsDS.addColumn("__versionstatus", 0);
            ArrayList<DataSet> groupedChangeLogs = changeLogsDS.getGroupedDataSets("sdcid");
            for (DataSet sdcGroupCL : groupedChangeLogs) {
                String linkSDCId = sdcGroupCL.getValue(0, "sdcid");
                if ("Y".equals(sdcProcessor.getProperty(linkSDCId, "uuidflag"))) {
                    sdiRequest = new SDIRequest();
                    sdiRequest.setSDCid(sdcGroupCL.getValue(0, "sdcid"));
                    sdiRequest.setKeyid1List(sdcGroupCL.getColumnValues("keyid1", ";"));
                    sdiRequest.setKeyid2List(sdcGroupCL.getColumnValues("keyid2", ";"));
                    sdiRequest.setKeyid3List(sdcGroupCL.getColumnValues("keyid3", ";"));
                    sdiRequest.setQueryWhere("uuid is null OR uuid=''");
                    sdiRequest.setRequestItem("primary[uuid]");
                    DataSet emptyUUIDds = sdiProcessor.getSDIData(sdiRequest).getDataset("primary");
                    if (emptyUUIDds.getRowCount() > 0) {
                        String keycolid1 = sdcProcessor.getProperty(linkSDCId, "keycolid1");
                        String keycolid2 = sdcProcessor.getProperty(linkSDCId, "keycolid2");
                        String keycolid3 = sdcProcessor.getProperty(linkSDCId, "keycolid3");
                        HashMap<String, String> filterMap = new HashMap<String, String>();
                        for (i = 0; i < emptyUUIDds.getRowCount(); ++i) {
                            filterMap.put("keyid1", emptyUUIDds.getValue(i, keycolid1));
                            if (keycolid2.length() > 0) {
                                filterMap.put("keyid2", emptyUUIDds.getValue(i, keycolid2));
                                if (keycolid3.length() > 0) {
                                    filterMap.put("keyid3", emptyUUIDds.getValue(i, keycolid3));
                                }
                            }
                            int foundRow = changeLogsDS.findRow(filterMap);
                            changeLogsDS.setValue(foundRow, "uuid", "N");
                        }
                    }
                }
                if (!checkVersionStatus || !"Y".equals(sdcProcessor.getProperty(linkSDCId, "versionedflag"))) continue;
                String[] changeLogIdWithVers = StringUtil.split(sdcGroupCL.getColumnValues("changelogid", ";"), ";");
                ((Stream)Arrays.stream(changeLogIdWithVers).parallel()).forEach(changeLogId -> {
                    try {
                        CMTUtil.getChangeLogSnapshotItem(changeLogId, this.getConnectionId()).ifPresent(snapshotItems -> {
                            for (SnapshotItem snapshoItem : snapshotItems) {
                                String versionStatus = CMTUtil.getSnapshotVersionStatus(snapshoItem, this.getSDCProcessor());
                                if (versionStatus.length() <= 0) continue;
                                int findRow = sdcGroupCL.findRow("changelogid", (String)changeLogId);
                                sdcGroupCL.setString(findRow, "__versionstatus", versionStatus);
                            }
                        });
                    }
                    catch (SapphireException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        } else {
            SnapshotPackage snapshotPackage = null;
            SnapshotFactory snapshotUtil = new SnapshotFactory(this.getConnectionId());
            snapshotUtil.setVerbose(verboseLog);
            approvalRequired = false;
            if ("PropertyTree".equals(sdcid)) {
                String mode = properties.getProperty("cmt-propertytreemode");
                String nodeList = properties.getProperty("nodelist");
                int noOfItems = StringUtil.split(keyid1, ";").length;
                if ("__FULL".equals(mode)) {
                    nodeList = StringUtil.repeat("__FULL", noOfItems, ";");
                } else if ("__DEFINITION".equals(mode)) {
                    nodeList = StringUtil.repeat("__DEFINITION", noOfItems, ";");
                }
                snapshotPackage = snapshotUtil.packagePropertyTreeSnapshot(keyid1, nodeList);
                approvalRequired = true;
            } else {
                String policynodeid = properties.getProperty("policynodeid");
                boolean isFullExport = true;
                if (policynodeid.indexOf("_Minimum") > 0) {
                    policynodeid = policynodeid.substring(0, policynodeid.indexOf("_Minimum"));
                    isFullExport = false;
                }
                CMTPolicy cmtPolicy = CMTPolicy.getPolicy(this.getConnectionid(), sdcid, policynodeid);
                approvalRequired = cmtPolicy.isChangeRequestRequireApproval();
                snapshotPackage = snapshotUtil.packageSnapshot(sdcid, keyid1, keyid2, keyid3, policynodeid, isFullExport);
            }
            changeLogsDS = new DataSet();
            for (int c = 0; c < columnids.length; ++c) {
                changeLogsDS.addColumn(columnids[c], 0);
            }
            HashMap<String, SoftReference<HashMap>> c = snapshotCache;
            synchronized (c) {
                if (snapshotCache.get(this.getConnectionId()) == null) {
                    snapshotCache.put(this.getConnectionId(), new SoftReference(new HashMap()));
                }
                snapshotCache.get(this.getConnectionId()).get().put("snapshotPackage", snapshotPackage);
                snapshotCache.get(this.getConnectionId()).get().put("snapshotPackage_log", snapshotUtil.getLog());
            }
            List<SnapshotItem> itemList = snapshotPackage.getSnapshotItems();
            List<SnapshotItem> requestedItemList = snapshotPackage.getRequestedSnapshotItems();
            HashMap referencedMap = new HashMap();
            for (SnapshotItem item : itemList) {
                String itemsdcid = "";
                String itemkeyid1 = "";
                String itemkeyid2 = "";
                String itemkeyid3 = "";
                Object itemPropertyTreeNodeId = "";
                DataSet itemprimary = null;
                if (Snapshot.Type.PROPERTYTREE.equals((Object)item.getType())) {
                    PropertyTreeSnapshotItem propertyTreeSnapshotItem = (PropertyTreeSnapshotItem)item;
                    PropertyTreeSnapshot transferPTreesnapshot = (PropertyTreeSnapshot)snapshotPackage.getSnapshot(propertyTreeSnapshotItem);
                    itemprimary = transferPTreesnapshot.getSDIData().getDataset("primary");
                    itemsdcid = "PropertyTree";
                    itemkeyid1 = propertyTreeSnapshotItem.getPropertyTreeId();
                    itemkeyid2 = "";
                    itemkeyid3 = "";
                    itemPropertyTreeNodeId = propertyTreeSnapshotItem.getNodeId();
                } else {
                    SDISnapshot transferSDIsnapshot = (SDISnapshot)snapshotPackage.getSnapshot(item);
                    itemprimary = transferSDIsnapshot.getSDIData(transferSDIsnapshot.getSnapshotItem()).getDataset("primary");
                    itemsdcid = transferSDIsnapshot.getSDCId();
                    itemkeyid1 = transferSDIsnapshot.getKeyId1();
                    itemkeyid2 = transferSDIsnapshot.getKeyId2();
                    itemkeyid3 = transferSDIsnapshot.getKeyId3();
                }
                int row = changeLogsDS.addRow();
                changeLogsDS.setValue(row, "sdcid", itemsdcid);
                changeLogsDS.setValue(row, "keyid1", itemkeyid1);
                changeLogsDS.setValue(row, "keyid2", itemkeyid2);
                changeLogsDS.setValue(row, "keyid3", itemkeyid3);
                changeLogsDS.setString(row, "propertytreenodeid", (String)itemPropertyTreeNodeId);
                changeLogsDS.setValue(row, "transfertype", requestedItemList.indexOf(item) < 0 ? "Referenced" : "");
                changeLogsDS.setNumber(row, "attachmentsize", ((SDISnapshotItem)item).getAttachmentSize());
                if ("Y".equals(this.getSDCProcessor().getProperty(itemsdcid, "uuidflag")) && itemprimary.getValue(0, "uuid").length() == 0) {
                    changeLogsDS.setValue(row, "uuid", "(Missing)");
                }
                if (referencedMap.get(itemsdcid) == null) {
                    HashMap<String, StringBuilder> keyMap = new HashMap<String, StringBuilder>();
                    keyMap.put("keyid1", new StringBuilder());
                    keyMap.put("keyid2", new StringBuilder());
                    keyMap.put("keyid3", new StringBuilder());
                    keyMap.put("propertytreenodeid", new StringBuilder());
                    referencedMap.put(itemsdcid, keyMap);
                }
                ((StringBuilder)((HashMap)referencedMap.get(itemsdcid)).get("keyid1")).append(";" + itemkeyid1);
                ((StringBuilder)((HashMap)referencedMap.get(itemsdcid)).get("keyid2")).append(";" + itemkeyid2);
                ((StringBuilder)((HashMap)referencedMap.get(itemsdcid)).get("keyid3")).append(";" + itemkeyid3);
                ((StringBuilder)((HashMap)referencedMap.get(itemsdcid)).get("propertytreenodeid")).append(";" + (String)itemPropertyTreeNodeId);
            }
            for (String itemsdcid : referencedMap.keySet()) {
                String changeControlFlag = CMTPolicy.getPolicy(this.getConnectionid(), itemsdcid).getChangeControlledFlag();
                if (!"Y".equals(changeControlFlag)) continue;
                if ("PropertyTree".equals(itemsdcid)) {
                    DataSet referencedNodes = new DataSet();
                    referencedNodes.addColumnValues("propertytreeid", 0, ((StringBuilder)((HashMap)referencedMap.get(itemsdcid)).get("keyid1")).substring(1), ";");
                    referencedNodes.addColumnValues("propertytreenodeid", 0, ((StringBuilder)((HashMap)referencedMap.get(itemsdcid)).get("propertytreenodeid")).substring(1), ";");
                    DataSet rsetNodes = referencedNodes.copy();
                    ArrayList<DataSet> groups = rsetNodes.getGroupedDataSets("propertytreeid");
                    for (DataSet group : groups) {
                        if (group.findRow("propertytreenodeid", "__FULL") != -1) continue;
                        int newRow = rsetNodes.addRow();
                        rsetNodes.setString(newRow, "propertytreeid", group.getString(0, "propertytreeid"));
                        rsetNodes.setString(newRow, "propertytreenodeid", "__FULL");
                    }
                    String rsetid = this.getDAMProcessor().createRSet("PropertyTree", rsetNodes.getColumnValues("propertytreeid", ";"), "", "");
                    String sql = "SELECT mv.*, cr.changerequeststatus FROM rsetitems r, changerequest cr, (SELECT changelogid,changelogstatus,changerequestid,linksdcid,linkkeyid1,propertytreenodeid, Row_Number () OVER (PARTITION BY linksdcid,linkkeyid1,propertytreenodeid ORDER BY moddt DESC) row_num FROM changelog) mv WHERE mv.row_num = 1 AND r.rsetid = ? AND mv.linksdcid = 'PropertyTree' AND r.keyid1 = mv.linkkeyid1 AND mv.changerequestid=cr.changerequestid";
                    DataSet changelogds = this.getQueryProcessor().getPreparedSqlDataSet(sql, new Object[]{rsetid});
                    this.getDAMProcessor().clearRSet(rsetid);
                    HashMap<String, String> findMap = new HashMap<String, String>();
                    block10: for (i = 0; i < changeLogsDS.getRowCount(); ++i) {
                        boolean isNotApproved;
                        boolean isCheckedOut;
                        String sdcId = changeLogsDS.getString(i, "sdcid");
                        if (!"PropertyTree".equals(sdcId)) continue;
                        findMap.clear();
                        String propertyTreeId = changeLogsDS.getString(i, "keyid1");
                        String propertyTreeNodeId = changeLogsDS.getString(i, "propertytreenodeid");
                        findMap.put("linkkeyid1", propertyTreeId);
                        DataSet pTreeChangeLogDS = changelogds.getFilteredDataSet(findMap);
                        if ("__FULL".equals(propertyTreeNodeId)) {
                            findMap.put("propertytreenodeid", "__FULL");
                            int fullChangeLogRow = pTreeChangeLogDS.findRow(findMap);
                            isCheckedOut = "Checked Out".equals(pTreeChangeLogDS.getString(fullChangeLogRow, "changelogstatus"));
                            boolean bl = isNotApproved = !"Approved".equals(pTreeChangeLogDS.getString(fullChangeLogRow, "changerequeststatus", "Approved"));
                            if (isCheckedOut) {
                                changeLogsDS.setValue(i, "changelogstatus", "Checked Out");
                                continue;
                            }
                            if (isFromChangeControl && approvalRequired && isNotApproved) {
                                changeLogsDS.setValue(i, "changelogstatus", "Not Approved");
                                continue;
                            }
                            findMap.put("propertytreenodeid", "__FULL");
                            DataSet nonFullChangeLogRows = pTreeChangeLogDS.getFilteredDataSet(findMap, true);
                            for (int j = 0; j < nonFullChangeLogRows.getRowCount(); ++j) {
                                isCheckedOut = "Checked Out".equals(nonFullChangeLogRows.getString(j, "changelogstatus"));
                                boolean bl2 = isNotApproved = !"Approved".equals(nonFullChangeLogRows.getString(j, "changerequeststatus", "Approved"));
                                if (isCheckedOut) {
                                    changeLogsDS.setValue(i, "changelogstatus", "Checked Out Node");
                                    continue block10;
                                }
                                if (!isFromChangeControl || !approvalRequired || !isNotApproved) continue;
                                changeLogsDS.setValue(i, "changelogstatus", "Not Approved Node");
                                continue block10;
                            }
                            continue;
                        }
                        findMap.put("propertytreenodeid", propertyTreeNodeId);
                        int nodeChangeLogRow = pTreeChangeLogDS.findRow(findMap);
                        isCheckedOut = "Checked Out".equals(pTreeChangeLogDS.getString(nodeChangeLogRow, "changelogstatus"));
                        boolean bl = isNotApproved = !"Approved".equals(pTreeChangeLogDS.getString(nodeChangeLogRow, "changerequeststatus", "Approved"));
                        if (isCheckedOut) {
                            changeLogsDS.setValue(i, "changelogstatus", "Checked Out");
                            continue;
                        }
                        if (isFromChangeControl && approvalRequired && isNotApproved) {
                            changeLogsDS.setValue(i, "changelogstatus", "Not Approved");
                            continue;
                        }
                        findMap.put("propertytreenodeid", "__FULL");
                        int fullChangeLogRow = pTreeChangeLogDS.findRow(findMap);
                        isCheckedOut = "Checked Out".equals(pTreeChangeLogDS.getString(fullChangeLogRow, "changelogstatus"));
                        boolean bl3 = isNotApproved = !"Approved".equals(pTreeChangeLogDS.getString(fullChangeLogRow, "changerequeststatus", "Approved"));
                        if (isCheckedOut) {
                            changeLogsDS.setValue(i, "changelogstatus", "Checked Out Tree");
                            continue;
                        }
                        if (!isFromChangeControl || !approvalRequired || !isNotApproved) continue;
                        changeLogsDS.setValue(i, "changelogstatus", "Not Approved Tree");
                    }
                    continue;
                }
                String keycolumns = this.getSDCProcessor().getProperty(itemsdcid, "keycolumns");
                String keyid1list = ((StringBuilder)((HashMap)referencedMap.get(itemsdcid)).get("keyid1")).substring(1);
                String keyid2list = ((StringBuilder)((HashMap)referencedMap.get(itemsdcid)).get("keyid2")).substring(1);
                String keyid3list = ((StringBuilder)((HashMap)referencedMap.get(itemsdcid)).get("keyid3")).substring(1);
                String rsetid = this.getDAMProcessor().createRSet(itemsdcid, keyid1list, keyid2list, keyid3list);
                String sql = "SELECT mv.*,cr.changerequeststatus FROM rsetitems r, (  SELECT changelogid,changelogstatus,changerequestid,linksdcid,linkkeyid1,linkkeyid2,linkkeyid3,    Row_Number () OVER (PARTITION BY linksdcid,linkkeyid1,linkkeyid2,linkkeyid3 ORDER BY moddt DESC) row_num  FROM changelog) mv LEFT OUTER JOIN changerequest cr  ON mv.changerequestid = cr.changerequestid WHERE mv.row_num = 1 AND r.rsetid = ? AND r.sdcid = mv.linksdcid AND r.keyid1 = mv.linkkeyid1 AND r.keyid2 = mv.linkkeyid2 AND r.keyid3 = mv.linkkeyid3";
                DataSet changelogds = this.getQueryProcessor().getPreparedSqlDataSet(sql, new Object[]{rsetid});
                this.getDAMProcessor().clearRSet(rsetid);
                for (i = 0; i < changelogds.getRowCount(); ++i) {
                    int foundrow;
                    HashMap<String, String> findMap = new HashMap<String, String>();
                    findMap.put("keyid1", changelogds.getValue(i, "linkkeyid1"));
                    if ("2".equals(keycolumns) || "3".equals(keycolumns)) {
                        findMap.put("keyid2", changelogds.getValue(i, "linkkeyid2"));
                        if ("3".equals(keycolumns)) {
                            findMap.put("keyid3", changelogds.getValue(i, "linkkeyid3"));
                        }
                    }
                    if ("Checked Out".equals(changelogds.getValue(i, "changelogstatus"))) {
                        foundrow = changeLogsDS.findRow(findMap);
                        if (foundrow < 0) continue;
                        changeLogsDS.setValue(foundrow, "changelogstatus", "Checked Out");
                        continue;
                    }
                    if (!isFromChangeControl || !approvalRequired || changelogds.getString(i, "changerequestid", "").length() <= 0 || "Approved".equals(changelogds.getValue(i, "changerequeststatus")) || (foundrow = changeLogsDS.findRow(findMap)) < 0) continue;
                    changeLogsDS.setValue(foundrow, "changelogstatus", "Not Approved");
                }
            }
        }
        changeLogsDS.sort("transfertype, sdcid");
        StringBuilder sb = new StringBuilder();
        if (!isFromChangeControl) {
            sb.append("<div style=\"padding:5px\"><input type=\"checkbox\" id=\"ignorevalidationerrorcheckbox\" onclick=\"ignoreCheckedOutValidationErrors( this )\"/><label for='ignorevalidationerrorcheckbox'>" + this.getTranslationProcessor().translate("Ignore all checked out validation errors") + "</label></div>");
        }
        sb.append("<table id=\"exportvalidationtable\" style=\"cell-padding:5px;\" class=\"gridmaint_table\">");
        for (int row = 0; row < changeLogsDS.getRowCount(); ++row) {
            if (row == 0) {
                sb.append("<tr style=\"height:30px\">");
                for (int col = 0; col < columntitles.length; ++col) {
                    sb.append("<td style=\"padding:5px;white-space:nowrap\" class=\"maintform_fieldtitle\">" + this.getTranslationProcessor().translate(columntitles[col]) + "</td>");
                }
                sb.append("</tr>");
            }
            sb.append("<tr style=\"height:30px\">");
            boolean isNextSameSDC = changeLogsDS.getRowCount() > row && changeLogsDS.getValue(row + 1, "sdcid").equals(changeLogsDS.getValue(row, "sdcid"));
            boolean isPrevSameSDC = changeLogsDS.getRowCount() > 0 && changeLogsDS.getValue(row - 1, "sdcid").equals(changeLogsDS.getValue(row, "sdcid"));
            boolean isReferenced = "Referenced".equals(changeLogsDS.getValue(row, "transfertype"));
            for (int col = 0; col < columntitles.length; ++col) {
                if (title_sdcid.equals(columntitles[col])) {
                    sb.append("<td style=\"padding:5px;white-space:nowrap;" + (isNextSameSDC ? "border-bottom:1px solid white" : "") + "\" class=\"maintform_field\">");
                    sb.append("<div style=\"display:inline-block;width:120px\" title=\"" + (isReferenced ? this.getTranslationProcessor().translate("Referenced From Requested") : this.getTranslationProcessor().translate("Requested SDI")) + "\">" + (isPrevSameSDC ? "&nbsp;" : changeLogsDS.getValue(row, "sdcid") + (isReferenced ? "*" : "")) + "</div>");
                } else if (title_sditoexport.equals(columntitles[col])) {
                    sb.append("<td style=\"padding:5px;white-space:nowrap\" class=\"maintform_field\">");
                    if ("PropertyTree".equals(changeLogsDS.getString(row, "sdcid"))) {
                        if (changeLogsDS.getString(row, "propertytreenodeid", "").length() > 0) {
                            sb.append("<div style=\"display:inline-block;width:200px\"><a style=\"text-decoration:underline\" href=\"Javascript:viewSDISnapshot( '" + changeLogsDS.getValue(row, "sdcid") + "', '" + changeLogsDS.getValue(row, "keyid1") + "', '', '', 'N', '" + changeLogsDS.getValue(row, "propertytreenodeid") + "' )\">" + changeLogsDS.getValue(row, "keyid1") + "|" + changeLogsDS.getValue(row, "propertytreenodeid") + "</a></div>");
                        } else {
                            sb.append("<div style=\"display:inline-block;width:200px\"><a style=\"text-decoration:underline\" href=\"Javascript:viewSDISnapshot( '" + changeLogsDS.getValue(row, "sdcid") + "', '" + changeLogsDS.getValue(row, "keyid1") + "', '', '', 'N' )\">" + changeLogsDS.getValue(row, "keyid1") + "</a></div>");
                        }
                    } else {
                        sb.append("<div style=\"display:inline-block;width:300px\">");
                        sb.append("<a style=\"text-decoration:underline\" href=\"Javascript:viewSDISnapshot( '" + changeLogsDS.getValue(row, "sdcid") + "', '" + changeLogsDS.getValue(row, "keyid1") + "', '" + changeLogsDS.getValue(row, "keyid2") + "', '" + changeLogsDS.getValue(row, "keyid3") + "', 'N' )\">" + SafeHTML.encodeForHTML(changeLogsDS.getValue(row, "keyid1")) + (changeLogsDS.getValue(row, "keyid2").length() == 0 || "(null)".equals(changeLogsDS.getValue(row, "keyid2")) ? "" : "|" + changeLogsDS.getValue(row, "keyid2")) + (changeLogsDS.getValue(row, "keyid3").length() == 0 || "(null)".equals(changeLogsDS.getValue(row, "keyid3")) ? "" : "|" + changeLogsDS.getValue(row, "keyid3")) + "</a>");
                        long attSize = changeLogsDS.getLong(row, "attachmentsize");
                        sb.append(CMTUtil.getAttachmentIconHTML(attSize, this.getTranslationProcessor()));
                        sb.append("</div>");
                    }
                } else if (title_fromchangelog.equals(columntitles[col])) {
                    sb.append("<td style=\"padding:5px;white-space:nowrap\" class=\"maintform_field\">");
                    sb.append(changeLogsDS.getValue(row, "changelogid") + "(" + changeLogsDS.getValue(row, "changerequestid") + ")");
                } else if (title_exportvalidation.equals(columntitles[col])) {
                    sb.append("<td style=\"padding:5px;white-space:nowrap\" class=\"maintform_field\">");
                    if (changeLogsDS.getString(row, "changelogstatus", "").startsWith("Checked Out")) {
                        if ("PropertyTree".equals(changeLogsDS.getString(row, "sdcid"))) {
                            if ("Checked Out".equals(changeLogsDS.getString(row, "changelogstatus", ""))) {
                                sb.append("<div name=\"changelogstatus\" style=\"color:red;display:inline-block;width:100px\">" + this.getTranslationProcessor().translate("Checked Out"));
                                sb.append("</div><div style=\"display:inline-block;\">");
                                sb.append("<a style=\"text-decoration:underline\" href=\"Javascript:viewSDISnapshot( '" + changeLogsDS.getValue(row, "sdcid") + "', '" + changeLogsDS.getValue(row, "keyid1") + "', '', '', 'Y', '" + changeLogsDS.getValue(row, "propertytreenodeid") + "' )\">" + this.getTranslationProcessor().translate("View Changes") + "</a>");
                            } else if ("Checked Out Tree".equals(changeLogsDS.getString(row, "changelogstatus", ""))) {
                                sb.append("<div name=\"changelogstatus\" title=\"" + this.getTranslationProcessor().translate("Full PropertyTree is Checked Out") + "\" style=\"color:red;display:inline-block;width:180px\">" + this.getTranslationProcessor().translate("Checked Out") + "</div>");
                            } else if ("Checked Out Node".equals(changeLogsDS.getString(row, "changelogstatus", ""))) {
                                sb.append("<div name=\"changelogstatus\" title=\"" + this.getTranslationProcessor().translate("One or more Nodes are Checked Out") + "\" style=\"color:red;display:inline-block;width:180px\">" + this.getTranslationProcessor().translate("Checked Out") + "</div>");
                            }
                        } else {
                            sb.append("<div name=\"changelogstatus\" style=\"color:red;display:inline-block;width:100px\">" + changeLogsDS.getValue(row, "changelogstatus"));
                            sb.append("</div><div style=\"display:inline-block;\">");
                            sb.append("<a style=\"text-decoration:underline\" href=\"Javascript:viewSDISnapshot( '" + changeLogsDS.getValue(row, "sdcid") + "', '" + changeLogsDS.getValue(row, "keyid1") + "', '" + changeLogsDS.getValue(row, "keyid2") + "', '" + changeLogsDS.getValue(row, "keyid3") + "', 'Y' )\">" + this.getTranslationProcessor().translate("View Changes") + "</a>");
                        }
                        sb.append("</div>");
                    } else if (changeLogsDS.getString(row, "changelogstatus", "").startsWith("Not Approved")) {
                        if ("PropertyTree".equals(changeLogsDS.getString(row, "sdcid"))) {
                            if ("Not Approved".equals(changeLogsDS.getString(row, "changelogstatus", ""))) {
                                sb.append("<div name=\"changelogstatus\" title=\"" + this.getTranslationProcessor().translate("Change Request Approval Required.") + "\" style=\"color:red;display:inline-block;width:180px\">" + changeLogsDS.getValue(row, "changelogstatus") + "</div>");
                            } else if ("Not Approved Tree".equals(changeLogsDS.getString(row, "changelogstatus", ""))) {
                                sb.append("<div name=\"changelogstatus\" title=\"" + this.getTranslationProcessor().translate("Change Request Approval Required for Tree") + "\" style=\"color:red;display:inline-block;width:180px\">" + changeLogsDS.getValue(row, "changelogstatus") + "</div>");
                            } else if ("Not Approved Node".equals(changeLogsDS.getString(row, "changelogstatus", ""))) {
                                sb.append("<div name=\"changelogstatus\" title=\"" + this.getTranslationProcessor().translate("Change Request Approval Required for one or more Nodes") + "\" style=\"color:red;display:inline-block;width:180px\">" + changeLogsDS.getValue(row, "changelogstatus") + "</div>");
                            }
                        } else {
                            sb.append("<div name=\"changelogstatus\" title=\"" + this.getTranslationProcessor().translate("Change Request Approval Required.") + "\" style=\"color:red;display:inline-block;width:180px\">" + changeLogsDS.getValue(row, "changelogstatus") + "</div>");
                        }
                    } else if ("Provisional".equals(changeLogsDS.getString(row, "__versionstatus", ""))) {
                        sb.append("<div name=\"changelogstatus\" title=\"" + this.getTranslationProcessor().translate("Provisional SDI.") + "\" style=\"color:red;display:inline-block;width:180px\">" + changeLogsDS.getString(row, "__versionstatus") + "</div>");
                    }
                } else {
                    sb.append("<td style=\"padding:5px;white-space:nowrap\" class=\"maintform_field\">");
                    sb.append("Not Sure");
                }
                sb.append("</td>");
            }
            sb.append("</tr>");
        }
        sb.append("</table>");
        sb.append("\n<script>");
        sb.append("\nisFromChangeLog=" + isFromChangeControl + ";");
        sb.append("\nfunction ignoreCheckedOutValidationErrors( chx ) { if ( chx.checked ) { $(document.getElementById('exportvalidationtable')).find('div[name=changelogstatus]').each( function(){if($(this).html()=='Checked Out'){$(this).html('')}} )  } else { $(document.getElementById('exportvalidationtable')).find('div[name=changelogstatus]').each( function(){if($(this).html()==''){$(this).html('Checked Out')}} ) } }");
        sb.append("\nfunction viewSDISnapshot( sdcid, keyid1, keyid2, keyid3, snapshotdiff, propertyTreeNodeId ){ var url='rc?command=page&page=SDISnapshotViewer&layoutscrolling=N&snapshotdiff=' + snapshotdiff + '&sdcid=' + sdcid + '&keyid1=' + keyid1+ '&keyid2=' + keyid2 + '&keyid3=' + keyid3; if(typeof(propertyTreeNodeId) != 'undefined' && propertyTreeNodeId != ''){url = url + '&propertytreenodeid=' + propertyTreeNodeId} sapphire.ui.dialog.open( sapphire.translate( 'View SDI To Be Exported' ), url, 800, 450 );};");
        sb.append("\n</script>");
        properties.put("validationresults", sb.toString());
    }

    public static SnapshotPackage getCachedSnapshotPackage(String connectionid) {
        return snapshotCache.get(connectionid) != null ? (SnapshotPackage)snapshotCache.get(connectionid).get().get("snapshotPackage") : null;
    }

    public static String getCachedLog(String connectionid) {
        return snapshotCache.get(connectionid) != null ? (String)snapshotCache.get(connectionid).get().get("snapshotPackage_log") : "";
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void removeSnapshotPackageFromCache(String connectionid) {
        HashMap<String, SoftReference<HashMap>> hashMap = snapshotCache;
        synchronized (hashMap) {
            if (snapshotCache.get(connectionid) != null) {
                snapshotCache.get(connectionid).get().remove("snapshotPackage");
                snapshotCache.get(connectionid).get().remove("snapshotPackage_log");
            }
        }
    }
}

