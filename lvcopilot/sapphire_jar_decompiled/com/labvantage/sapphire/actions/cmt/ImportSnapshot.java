/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.cmt;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.Build;
import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.RequestParser;
import com.labvantage.sapphire.admin.webadmin.WebAdminProcessor;
import com.labvantage.sapphire.cmt.CMTUtil;
import com.labvantage.sapphire.cmt.PropertyTreeSnapshot;
import com.labvantage.sapphire.cmt.PropertyTreeSnapshotItem;
import com.labvantage.sapphire.cmt.SDISnapshot;
import com.labvantage.sapphire.cmt.SDISnapshotItem;
import com.labvantage.sapphire.cmt.SnapshotPackage;
import com.labvantage.sapphire.services.AuditService;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.ServiceException;
import com.labvantage.sapphire.services.WebAdminService;
import com.labvantage.sapphire.util.cache.CacheUtil;
import com.labvantage.sapphire.util.file.FileManager;
import com.labvantage.sapphire.util.policy.CMTPolicy;
import com.labvantage.sapphire.util.policy.ConfigTransferOption;
import com.labvantage.sapphire.xml.Node;
import com.labvantage.sapphire.xml.NodeList;
import com.labvantage.sapphire.xml.PropertyListTransfer;
import com.labvantage.sapphire.xml.PropertyTree;
import com.labvantage.sapphire.xml.PropertyTreeUtil;
import com.labvantage.sapphire.xml.StringLogger;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.CallableStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.AttachmentProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.action.BaseAction;
import sapphire.attachment.Attachment;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;
import sapphire.xml.cmt.Snapshot;
import sapphire.xml.cmt.SnapshotItem;

public class ImportSnapshot
extends BaseAction {
    public static final String PROPERTY_FILENAME = "filename";
    public static final String PROPERTY_TEMPID = "tempid";
    public static final String PROPERTY_IMPORTLOG = "importlog";
    public static final String PROPERTY_EXCEPTION = "exception";
    public static final String PROPERTY_REMOTE = "remote";
    public static final String PROPERTY_UNDOCHECKOUTFLAG = "undocheckoutflag";
    private boolean isFromRemoteCheckOut_Slave = false;
    private boolean isFromRemoteCheckIn_Master = false;
    private boolean alwaysIgnoreIfExists = false;
    private boolean isUndoCheckoutOperation = false;
    private String changerequestid = "";
    private HashMap<String, DataSet> updTableRows = new HashMap();
    public static final String IMPORT_SRC_UNDOCHECKOUT = "UNDOCHECKOUT";
    public static final String IMPORT_SRC_WIZARD = "WIZARD";
    public static final String IMPORT_SRC_REMOTE_SLAVE = "REMOTE_SLAVE";
    public static final String IMPORT_SRC_REMOTE_MASTER = "REMOTE_MASTER";
    private String importSource = "";
    private Map<String, Boolean> SDC_VERSIONEDFLAG = new HashMap<String, Boolean>();

    /*
     * Loose catch block
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    public void processAction(PropertyList props) throws SapphireException {
        DataSet forsdcDataSets;
        StringLogger outputLogger;
        File file;
        boolean createtransferlog;
        block129: {
            String filename;
            block130: {
                String transferoptions = props.getProperty("transferoptions");
                boolean verboseLog = "Y".equals(props.getProperty("verboselog"));
                createtransferlog = !"N".equals(props.getProperty("createtransferlog"));
                this.alwaysIgnoreIfExists = "Y".equals(props.getProperty("ignoreifexist"));
                this.changerequestid = props.getProperty("changerequestid");
                this.isFromRemoteCheckOut_Slave = "Y".equals(props.getProperty("remotecheckout"));
                if (this.isFromRemoteCheckOut_Slave) {
                    this.importSource = IMPORT_SRC_REMOTE_SLAVE;
                }
                if (!this.isFromRemoteCheckOut_Slave) {
                    this.isFromRemoteCheckIn_Master = "Y".equals(props.getProperty("remotecheckin"));
                    if (this.isFromRemoteCheckIn_Master) {
                        this.importSource = IMPORT_SRC_REMOTE_MASTER;
                    }
                }
                this.isUndoCheckoutOperation = "Y".equals(props.getProperty(PROPERTY_UNDOCHECKOUTFLAG, "N"));
                if (this.isUndoCheckoutOperation) {
                    if (this.importSource.length() > 0) {
                        throw new SapphireException("Multiple Import Source detected. Invalid scenario: " + this.importSource + " & " + IMPORT_SRC_UNDOCHECKOUT);
                    }
                    this.importSource = IMPORT_SRC_UNDOCHECKOUT;
                }
                if (this.importSource.length() == 0) {
                    this.importSource = IMPORT_SRC_WIZARD;
                }
                if (transferoptions.length() > 0) {
                    PropertyList saveoptionsPL = new PropertyList();
                    saveoptionsPL.setProperty("sdcid", "TransferLog");
                    saveoptionsPL.setProperty("keyid1", props.getProperty("transferlogid"));
                    saveoptionsPL.setProperty("transferoptions", transferoptions);
                    this.getActionProcessor().processAction("EditSDI", "1", saveoptionsPL);
                    return;
                }
                filename = props.getProperty(PROPERTY_FILENAME);
                String tempid = props.getProperty(PROPERTY_TEMPID);
                file = null;
                if (tempid.length() <= 0) break block130;
                FileManager.TempFile tempFile = FileManager.TempFile.getTempFile(tempid, false, this.getQueryProcessor(), this.getConnectionId());
                if (tempFile != null) {
                    FileManager.FileData fileData = tempFile.getData();
                    if (fileData != null) {
                        file = fileData.getFile().toFile();
                        break block129;
                    } else {
                        this.logger.warn("Failed to obtain file data.");
                    }
                    break block129;
                } else {
                    this.logger.warn("Failed to obtain temp file.");
                }
                break block129;
            }
            file = props.get("tempFile") != null && props.get("tempFile") instanceof File ? (File)props.get("tempFile") : new File(filename);
        }
        if (file == null) {
            throw new SapphireException("Failed to find import file.");
        }
        String exportPackageXML = props.getProperty("exportpackagexml");
        if (exportPackageXML.length() > 0 && !file.exists()) {
            try {
                file = File.createTempFile("exportpackage", ".xml");
                file.deleteOnExit();
                Files.write(Paths.get(file.toURI()), exportPackageXML.getBytes(StandardCharsets.UTF_8), StandardOpenOption.SYNC);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        if ((outputLogger = (StringLogger)props.get("outputLogger")) == null) {
            outputLogger = new StringLogger();
            props.put("outputLogger", outputLogger);
        }
        long start = System.currentTimeMillis();
        outputLogger.log("Start parsing CMT export package");
        ArrayList<SnapshotPackage> snapshotPackageList = new ArrayList<SnapshotPackage>();
        snapshotPackageList.add(SnapshotPackage.fromFile(file, this.getConnectionId()));
        outputLogger.log(snapshotPackageList.size() + " CMT export package parsed in " + (System.currentTimeMillis() - start) + "ms");
        ImportInstructions importInstructions = null;
        if (props.get("importInstructions") instanceof String) {
            try {
                importInstructions = new ImportInstructions((String)props.get("importInstructions"));
            }
            catch (JSONException e) {
                throw new SapphireException("importInstructions not a well formed JSON String");
            }
        } else if (props.get("importInstructions") instanceof ImportInstructions) {
            importInstructions = (ImportInstructions)props.get("importInstructions");
        }
        if (importInstructions == null) {
            importInstructions = new ImportInstructions();
            if (this.isFromRemoteCheckOut_Slave) {
                importInstructions.setIgnoreMissingObjects(true);
            }
        }
        importInstructions.setValidateFK(!"N".equals(props.getProperty("validateFK")));
        DataSet importedSDIList = new DataSet();
        HashMap<String, HashMap<String, String>> oldNewKeyMap = new HashMap<String, HashMap<String, String>>();
        HashMap<String, HashMap<String, StringBuilder>> fkToValidateMap = new HashMap<String, HashMap<String, StringBuilder>>();
        boolean importSuccess = true;
        boolean fkValidationPass = true;
        AuditService audit = new AuditService(new SapphireConnection(this.database.getConnection(), this.connectionInfo));
        String traceLogId = "";
        try {
            ArrayList<SnapshotItem> importItemList;
            traceLogId = audit.addTraceLogEntry("", "Import", "N", "", "Auto created by CMT ImportSnapshot" + (props.getProperty("transferlogid").length() > 0 ? " for TransferLog: " + props.getProperty("transferlogid") : ""), false);
            audit.setTracelogIdInDBSession(traceLogId);
            outputLogger.log("New Tracelog Id generated and set to Session: " + traceLogId);
            StringBuffer importedSDCSDIIds = new StringBuffer();
            for (SnapshotPackage snapshotPackage : snapshotPackageList) {
                SDISnapshotItem item;
                int i;
                SnapshotItem snapshotItem;
                int i2;
                outputLogger.log("SnapshotPackage source: " + (snapshotPackage.isFromChangeLog() ? "Change Control" : "Direct"));
                List<SnapshotItem> requestedItemList = snapshotPackage.getRequestedSnapshotItems();
                importItemList = new ArrayList();
                ArrayList<SnapshotItem> importSDCItemList = new ArrayList<SnapshotItem>();
                ArrayList<SnapshotItem> importPTreeItemList = new ArrayList<SnapshotItem>();
                this.logger.debug("Sorting the requestedItemList.");
                ArrayList<SnapshotItem> newRequestedItemList = new ArrayList<SnapshotItem>();
                ArrayList<SnapshotItem> expiredItemList = new ArrayList<SnapshotItem>();
                for (i2 = 0; i2 < requestedItemList.size(); ++i2) {
                    snapshotItem = requestedItemList.get(i2);
                    String sdcid = snapshotItem.getSDCId();
                    if (Snapshot.Type.SDI.equals((Object)snapshotItem.getType()) && this.isVersionedSDC(sdcid)) {
                        SDISnapshotItem sdiSnapshotItem = (SDISnapshotItem)snapshotItem;
                        if (!sdiSnapshotItem.isDeleted() && snapshotItem.getSDIData() != null && snapshotItem.getSDIData().getDataset("primary") != null && "E".equals(snapshotItem.getSDIData().getDataset("primary").getString(0, "versionstatus", ""))) {
                            expiredItemList.add(snapshotItem);
                            continue;
                        }
                        this.sortDescending(snapshotItem, newRequestedItemList);
                        continue;
                    }
                    newRequestedItemList.add(snapshotItem);
                }
                expiredItemList.forEach(el -> newRequestedItemList.add(0, (SnapshotItem)el));
                requestedItemList = newRequestedItemList;
                this.logger.debug("Flatten the heirarchy of links.");
                for (i2 = 0; i2 < requestedItemList.size(); ++i2) {
                    snapshotItem = (SDISnapshotItem)requestedItemList.get(i2);
                    this.collectionReferencedItems(snapshotItem, importItemList, importPTreeItemList, importSDCItemList, snapshotPackage, true);
                }
                this.logger.debug("Import importsdcitems: " + importSDCItemList.size());
                if (importSDCItemList.size() > 0) {
                    outputLogger.log("SDC definitions found in Package. Initiate Data Model Changes...");
                    this.flushUPDTables(outputLogger);
                    for (i2 = 0; i2 < importSDCItemList.size(); ++i2) {
                        SDISnapshotItem item2 = (SDISnapshotItem)importSDCItemList.get(i2);
                        SDISnapshot sdiSnapshot = (SDISnapshot)snapshotPackage.getSnapshot(item2);
                        if ((this.isFromRemoteCheckOut_Slave || this.isFromRemoteCheckIn_Master) && (sdiSnapshot = (SDISnapshot)snapshotPackage.getPreSnapshot(item2)) == null) {
                            sdiSnapshot = (SDISnapshot)snapshotPackage.getSnapshot(item2);
                        }
                        outputLogger.log("***************" + (i2 + 1) + " of " + importSDCItemList.size() + ". Importing SDC:" + item2.getKeyId1());
                        if (!this.importSDCSnapshot(sdiSnapshot, true, outputLogger, importInstructions, importedSDIList)) continue;
                        importedSDCSDIIds.append(";").append(sdiSnapshot.getKeyId1());
                    }
                    if (importedSDCSDIIds.length() > 0) {
                        importedSDCSDIIds.deleteCharAt(0);
                        this.triggerSyncUpdProc(outputLogger);
                    }
                    outputLogger.log("***************  Finish Data Model Changes for SDCs.");
                }
                this.logger.debug("Import importPTreeItemList: " + importPTreeItemList.size());
                if (importPTreeItemList.size() > 0) {
                    outputLogger.log("Determine PropertyTree items import sequence.");
                }
                importPTreeItemList = this.determinePTreeImportSequence(importPTreeItemList, importInstructions, snapshotPackage);
                int totalCount = importItemList.size();
                int currentNo = 1;
                for (int i3 = 0; i3 < importPTreeItemList.size(); ++i3) {
                    SnapshotItem item3 = importPTreeItemList.get(i3);
                    outputLogger.log("***************" + currentNo++ + " of " + totalCount + ". Importing Property Tree:" + item3.getKeyId1() + ", Node:" + ((PropertyTreeSnapshotItem)item3).getNodeId());
                    this.importPropertyTreeSnapshot(item3, snapshotPackage, outputLogger, importInstructions, importedSDIList);
                }
                this.logger.debug("Import importItemList: " + importItemList.size());
                ArrayList<SDISnapshotItem> list = new ArrayList<SDISnapshotItem>();
                ArrayList<String> addList = new ArrayList<String>();
                DataSet ds = new DataSet();
                for (i = 0; i < importItemList.size(); ++i) {
                    item = (SDISnapshotItem)importItemList.get(i);
                    if (!this.isVersionedSDC(item.getSDCId())) {
                        list.add(item);
                        continue;
                    }
                    String sdcid = item.getSDCId();
                    String keyid1 = item.getKeyId1();
                    String keyid3 = item.getKeyId3();
                    String itemkey = sdcid + ";" + keyid1 + ";" + keyid3;
                    if (addList.contains(itemkey)) continue;
                    addList.add(itemkey);
                    ds.reset();
                    for (int index = i; index < importItemList.size(); ++index) {
                        SDISnapshotItem currentitem = (SDISnapshotItem)importItemList.get(index);
                        if (!currentitem.getSDCId().equals(sdcid) || !currentitem.getKeyId1().equals(keyid1) || !currentitem.getKeyId3().equals(keyid3)) continue;
                        int row = ds.addRow();
                        ds.setNumber(row, "version", currentitem.getKeyId2());
                        ds.setObject(row, "item", currentitem);
                    }
                    if (ds.size() > 0) {
                        ds.sort("version D");
                        for (int row = 0; row < ds.size(); ++row) {
                            list.add((SDISnapshotItem)ds.getObject(row, "item"));
                        }
                        continue;
                    }
                    list.add(item);
                }
                if (list.size() == importItemList.size()) {
                    importItemList.clear();
                    importItemList.addAll(list);
                }
                for (i = importItemList.size() - 1; i >= 0; --i) {
                    SDISnapshot preSnapshot;
                    item = (SDISnapshotItem)importItemList.get(i);
                    Snapshot snapshot = null;
                    snapshot = this.isFromRemoteCheckOut_Slave ? ((preSnapshot = (SDISnapshot)snapshotPackage.getPreSnapshot(item)) != null ? preSnapshot : snapshotPackage.getSnapshot(item)) : (item.isDeleted() ? snapshotPackage.getPreSnapshot(item) : snapshotPackage.getSnapshot(item));
                    if (Snapshot.Type.SDI.equals((Object)item.getType())) {
                        if (snapshot != null) {
                            item = (SDISnapshotItem)snapshot.getSnapshotItem();
                        }
                        SDISnapshot sdiSnapshot = (SDISnapshot)snapshot;
                        outputLogger.log("*************** " + currentNo++ + " of " + totalCount + ". Importing SDI:" + item.getSDCId() + "," + item.getKeyId1() + "," + item.getKeyId2() + "," + item.getKeyId3());
                        if ("SDC".equals(item.getSDCId())) {
                            this.importSDCSnapshot(sdiSnapshot, false, outputLogger, importInstructions, importedSDIList);
                            continue;
                        }
                        this.importSDISnapshot(sdiSnapshot, false, outputLogger, importInstructions, item, importedSDIList, oldNewKeyMap, fkToValidateMap);
                        continue;
                    }
                    if (Snapshot.Type.PROPERTYTREE.equals((Object)item.getType())) continue;
                    outputLogger.log("***************" + currentNo++ + " of " + totalCount + ". Unknown Snapshot type, ignored:" + (Object)((Object)snapshot.getType()));
                }
                outputLogger.log("*************** Finished Importing all Snapshot Items.");
            }
            if (importInstructions.isValidateFK() && importedSDIList.getRowCount() > 0) {
                Set sdcSet = fkToValidateMap.keySet();
                for (String sdcid : sdcSet) {
                    importItemList = (HashMap)fkToValidateMap.get(sdcid);
                }
                DataSet dataSet = new DataSet();
                dataSet.addColumnValues("sdcid", 0, importedSDIList.getColumnValues("sdcid", ";"), ";");
                dataSet.addColumnValues("keyid1", 0, importedSDIList.getColumnValues("keyid1", ";"), ";");
                dataSet.addColumnValues("keyid2", 0, importedSDIList.getColumnValues("keyid2", ";"), ";");
                dataSet.addColumnValues("keyid3", 0, importedSDIList.getColumnValues("keyid3", ";"), ";");
                HashMap<String, String> filterMap = new HashMap<String, String>();
                filterMap.put("sdcid", "Deleted");
                DataSet dataSet2 = dataSet.getFilteredDataSet(filterMap, true);
                dataSet2.sort("sdcid");
                ArrayList<DataSet> importedSDIBySDCGroup = dataSet2.getGroupedDataSets("sdcid");
                StringBuilder missingSDIs = new StringBuilder();
                outputLogger.log("*************** Start validate FK links...");
                for (int i = 0; i < importedSDIBySDCGroup.size(); ++i) {
                    String[] sdixxxTables;
                    String linksdckeycolid3;
                    String linksdckeycolid2;
                    String linksdckeycolid1;
                    String sdccolumnid3;
                    String sdccolumnid2;
                    String sdccolumnid;
                    forsdcDataSets = importedSDIBySDCGroup.get(i);
                    String sdcid = forsdcDataSets.getValue(0, "sdcid");
                    outputLogger.log("*************** Validating FK links for all SDIs of SDC: " + sdcid);
                    String keyid1list = forsdcDataSets.getColumnValues("keyid1", ";");
                    String keyid2list = forsdcDataSets.getColumnValues("keyid2", ";");
                    String keyid3list = forsdcDataSets.getColumnValues("keyid3", ";");
                    String keycolumns = this.getSDCProcessor().getProperty(sdcid, "keycolumns");
                    String keycolid1 = this.getSDCProcessor().getProperty(sdcid, "keycolid1");
                    String keycolid2 = this.getSDCProcessor().getProperty(sdcid, "keycolid2");
                    String keycolid3 = this.getSDCProcessor().getProperty(sdcid, "keycolid3");
                    String tableid = this.getSDCProcessor().getProperty(sdcid, "tableid");
                    String rsetid = "";
                    PropertyListCollection links = this.getSDCProcessor().getLinks(sdcid);
                    for (int linkno = 0; linkno < links.size(); ++linkno) {
                        PropertyList linkPL = links.getPropertyList(linkno);
                        String linktype = linkPL.getProperty("linktype");
                        if ("F".equals(linktype) || "M".equals(linktype)) {
                            String validationWhere;
                            String linkfromtableid;
                            String linksdcid = linkPL.getProperty("linksdcid");
                            HashMap fkkeyidMap = (HashMap)fkToValidateMap.get(linksdcid);
                            String linkmtableid = linkPL.getProperty("linktableid");
                            if ("SDC".equals(sdcid) && ("systable".equals(linkmtableid) || "syscolumn".equals(linkmtableid) || "sysrefcolumn".equals(linkmtableid) || "sysref".equals(linkmtableid))) continue;
                            sdccolumnid = linkPL.getProperty("sdccolumnid");
                            sdccolumnid2 = linkPL.getProperty("sdccolumnid2");
                            sdccolumnid3 = linkPL.getProperty("sdccolumnid3");
                            String linkid = linkPL.getProperty("linkid");
                            linksdckeycolid1 = this.getSDCProcessor().getProperty(linksdcid, "keycolid1");
                            linksdckeycolid2 = this.getSDCProcessor().getProperty(linksdcid, "keycolid2");
                            linksdckeycolid3 = this.getSDCProcessor().getProperty(linksdcid, "keycolid3");
                            String linksdctableid = this.getSDCProcessor().getProperty(linksdcid, "tableid");
                            boolean isVersioned = "Y".equals(this.getSDCProcessor().getProperty(linksdcid, "versionedflag"));
                            SafeSQL safeSQL = new SafeSQL();
                            String linkfrom = "";
                            String string = linkfromtableid = "M".equals(linktype) ? linkmtableid : tableid;
                            linkfrom = "M".equals(linktype) ? linkfromtableid + "." + linksdckeycolid1 + (linksdckeycolid2.length() > 0 ? "," + linkfromtableid + "." + linksdckeycolid2 : "") + (linksdckeycolid3.length() > 0 ? "," + linkfromtableid + "." + linksdckeycolid3 : "") : linkfromtableid + "." + sdccolumnid + (sdccolumnid2.length() > 0 ? "," + linkfromtableid + "." + sdccolumnid2 : "") + (sdccolumnid3.length() > 0 ? "," + linkfromtableid + "." + sdccolumnid3 : "");
                            outputLogger.log("Link Id : " + linkid + ": " + linkfrom + " to " + linksdctableid + "." + linksdckeycolid1 + (linksdckeycolid2.length() > 0 ? ", " + linksdctableid + "." + linksdckeycolid2 : (linksdckeycolid3.length() > 0 ? ", " + linksdctableid + "." + linksdckeycolid3 : "")));
                            if (fkkeyidMap != null) {
                                outputLogger.log("Validate SDI Exist " + linksdcid + ": keyid1=" + ((StringBuilder)fkkeyidMap.get("keyid1")).substring(1) + ",keyid2=" + ((StringBuilder)fkkeyidMap.get("keyid2")).substring(1) + ",keyid3=" + ((StringBuilder)fkkeyidMap.get("keyid3")).substring(1));
                            }
                            String primarySelectWhere = "SELECT " + linkfrom;
                            String string2 = validationWhere = "M".equals(linktype) ? "" : " AND NullIf(" + linkfromtableid + "." + sdccolumnid + ", '') is not null ";
                            if (rsetid.length() == 0) {
                                rsetid = this.getDAMProcessor().createRSet(sdcid, keyid1list, keyid2list, keyid3list);
                            }
                            primarySelectWhere = primarySelectWhere + " FROM " + linkfromtableid + ", rsetitems WHERE rsetitems.sdcid=" + safeSQL.addVar(sdcid) + " AND rsetitems.keyid1=" + linkfromtableid + "." + keycolid1 + (keycolid2.length() > 0 ? " AND rsetitems.keyid2=" + linkfromtableid + "." + keycolid2 : "") + (keycolid3.length() > 0 ? " AND rsetitems.keyid3=" + linkfromtableid + "." + keycolid3 : "") + " AND rsetitems.rsetid=" + safeSQL.addVar(rsetid);
                            validationWhere = validationWhere + " AND NOT EXISTS ( SELECT null FROM " + linksdctableid + " fktable WHERE fktable." + linksdckeycolid1 + "=" + linkfromtableid + "." + ("M".equals(linktype) ? linksdckeycolid1 : sdccolumnid) + (linksdckeycolid2.length() > 0 ? " AND (fktable." + linksdckeycolid2 + "=" + linkfromtableid + "." + ("M".equals(linktype) ? linksdckeycolid2 : sdccolumnid2) + (isVersioned ? " OR NullIf(" + linkfromtableid + "." + ("M".equals(linktype) ? linksdckeycolid2 : sdccolumnid2) + ",'') is null" : "") + ")" : "") + (linksdckeycolid3.length() > 0 ? " AND fktable." + linksdckeycolid3 + "=" + linkfromtableid + "." + ("M".equals(linktype) ? linksdckeycolid3 : sdccolumnid3) : "") + ")";
                            String finalSQL = primarySelectWhere + validationWhere;
                            Object[] vals = safeSQL.getValues();
                            DataSet missingRefSDIs = this.getQueryProcessor().getPreparedSqlDataSet(finalSQL, vals);
                            if (missingRefSDIs.getRowCount() <= 0) continue;
                            StringBuilder missingSDIDesc = null;
                            for (int r = 0; r < missingRefSDIs.getRowCount(); ++r) {
                                String linkkeyid3value;
                                String linkkeyid1value = missingRefSDIs.getValue(r, missingRefSDIs.getColumnId(0));
                                String linkkeyid2value = linksdckeycolid2.length() > 0 ? missingRefSDIs.getValue(r, missingRefSDIs.getColumnId(1)) : "";
                                String string3 = linkkeyid3value = linksdckeycolid3.length() > 0 ? missingRefSDIs.getValue(r, missingRefSDIs.getColumnId(2)) : "";
                                if (!(linkkeyid1value.length() > 0 && linksdckeycolid2.length() == 0 || linksdckeycolid1.length() > 0 && linksdckeycolid2.length() > 0 && linksdckeycolid3.length() == 0 && linkkeyid2value.length() > 0 || linksdckeycolid1.length() > 0 && linksdckeycolid2.length() > 0 && linksdckeycolid3.length() > 0 && linkkeyid3value.length() > 0) && (linksdckeycolid1.length() <= 0 || linksdckeycolid2.length() <= 0 || linksdckeycolid3.length() != 0 || linkkeyid2value.length() != 0)) continue;
                                if (missingSDIDesc == null) {
                                    missingSDIDesc = new StringBuilder();
                                }
                                missingSDIDesc.append("<br/>&nbsp;&nbsp;&nbsp;&nbsp;" + linkkeyid1value + (linksdckeycolid2.length() > 0 ? ", " + linkkeyid2value : "") + (linksdckeycolid3.length() > 0 ? ", " + linkkeyid3value : ""));
                                fkValidationPass = false;
                            }
                            if (missingSDIDesc == null) continue;
                            missingSDIs.append("<br/><br/>" + linksdcid + "(" + linksdckeycolid1 + (linksdckeycolid2.length() > 0 ? ", " + linksdckeycolid2 : "") + (linksdckeycolid3.length() > 0 ? ", " + linksdckeycolid3 : "") + ") Referenced from " + linkfromtableid + " of " + sdcid + " SDC: <br/>" + missingSDIDesc);
                            outputLogger.log("<span style=\"color:red\">ERROR: Referenced SDI Missing: " + linksdcid + "(Referenced from " + linkfromtableid + "):" + missingSDIDesc + "</span>");
                            outputLogger.log("Found with SQL:" + finalSQL);
                            continue;
                        }
                        if (!"V".equals(linktype)) continue;
                    }
                    PropertyListCollection detaillinks = this.getSDCProcessor().getDetailLinks(sdcid);
                    for (int linkno = 0; linkno < detaillinks.size(); ++linkno) {
                        PropertyList linkPL = detaillinks.getPropertyList(linkno);
                        String linktype = linkPL.getProperty("linktype");
                        if (!"F".equals(linktype)) continue;
                        String linksdcid = linkPL.getProperty("linksdcid");
                        HashMap fkkeyidMap = (HashMap)fkToValidateMap.get(linksdcid);
                        sdccolumnid = linkPL.getProperty("sdccolumnid");
                        sdccolumnid2 = linkPL.getProperty("sdccolumnid2");
                        sdccolumnid3 = linkPL.getProperty("sdccolumnid3");
                        String linkfromtableid = linkPL.getProperty("linktableid");
                        linksdckeycolid1 = this.getSDCProcessor().getProperty(linksdcid, "keycolid1");
                        linksdckeycolid2 = this.getSDCProcessor().getProperty(linksdcid, "keycolid2");
                        linksdckeycolid3 = this.getSDCProcessor().getProperty(linksdcid, "keycolid3");
                        boolean isVersioned = "Y".equals(this.getSDCProcessor().getProperty(linksdcid, "versionedflag"));
                        String linksdctableid = linkPL.getProperty("tableid");
                        String linkid = linkPL.getProperty("detaillinkid");
                        outputLogger.log("Detail FK linkid: " + linkid + ":" + linkfromtableid + " to " + linksdctableid);
                        if (fkkeyidMap != null) {
                            outputLogger.log("Validate SDI Exist " + linksdcid + ": keyid1=" + ((StringBuilder)fkkeyidMap.get("keyid1")).substring(1) + ",keyid2=" + ((StringBuilder)fkkeyidMap.get("keyid2")).substring(1) + ",keyid3=" + ((StringBuilder)fkkeyidMap.get("keyid3")).substring(1));
                        }
                        SafeSQL safeSQL = new SafeSQL();
                        if (rsetid.length() == 0) {
                            rsetid = this.getDAMProcessor().createRSet(sdcid, keyid1list, keyid2list, keyid3list);
                        }
                        String primarySelectWhere = "SELECT " + linkfromtableid + "." + sdccolumnid + (sdccolumnid2.length() > 0 ? ", " + linkfromtableid + "." + sdccolumnid2 : "") + (sdccolumnid3.length() > 0 ? ", " + linkfromtableid + "." + sdccolumnid3 : "") + " FROM " + linkfromtableid + ", rsetitems WHERE rsetitems.sdcid=" + safeSQL.addVar(sdcid) + " AND rsetitems.keyid1=" + linkfromtableid + "." + keycolid1 + (keycolid2.length() > 0 ? " AND rsetitems.keyid2=" + linkfromtableid + "." + keycolid2 : "") + (keycolid3.length() > 0 ? " AND rsetitems.keyid3=" + linkfromtableid + "." + keycolid3 : "") + " AND rsetitems.rsetid=" + safeSQL.addVar(rsetid);
                        String validationWhere = " AND NullIf(" + linkfromtableid + "." + sdccolumnid + ", '') is not null AND NOT EXISTS ( SELECT null FROM " + linksdctableid + " fktable WHERE fktable." + linksdckeycolid1 + "=" + linkfromtableid + "." + sdccolumnid + (sdccolumnid2.length() > 0 ? " AND ( fktable." + linksdckeycolid2 + "=" + linkfromtableid + "." + sdccolumnid2 + (isVersioned ? " OR NullIf(" + linkfromtableid + "." + sdccolumnid2 + ",'') is null" : "") + ")" : "") + (sdccolumnid3.length() > 0 ? " AND fktable." + linksdckeycolid3 + "=" + linkfromtableid + "." + sdccolumnid3 : "") + ")";
                        String finalSQL = primarySelectWhere + validationWhere;
                        Object[] vals = safeSQL.getValues();
                        DataSet missingRefSDIs = this.getQueryProcessor().getPreparedSqlDataSet(finalSQL, vals);
                        if (missingRefSDIs.getRowCount() <= 0) continue;
                        StringBuilder missingSDIDesc = null;
                        for (int r = 0; r < missingRefSDIs.getRowCount(); ++r) {
                            String linkkeyid3value;
                            String linkkeyid1value = missingRefSDIs.getValue(r, missingRefSDIs.getColumnId(0));
                            String linkkeyid2value = linksdckeycolid2.length() > 0 ? missingRefSDIs.getValue(r, missingRefSDIs.getColumnId(1)) : "";
                            String string = linkkeyid3value = linksdckeycolid3.length() > 0 ? missingRefSDIs.getValue(r, missingRefSDIs.getColumnId(2)) : "";
                            if (!(linkkeyid1value.length() > 0 && linksdckeycolid2.length() == 0 || linksdckeycolid1.length() > 0 && linksdckeycolid2.length() > 0 && linksdckeycolid3.length() == 0 && linkkeyid2value.length() > 0) && (linksdckeycolid1.length() <= 0 || linksdckeycolid2.length() <= 0 || linksdckeycolid3.length() <= 0 || linkkeyid3value.length() <= 0)) continue;
                            if (missingSDIDesc == null) {
                                missingSDIDesc = new StringBuilder();
                            }
                            missingSDIDesc.append("<br/>&nbsp;&nbsp;&nbsp;&nbsp;" + linkkeyid1value + (linksdckeycolid2.length() > 0 ? ", " + linkkeyid2value : "") + (linksdckeycolid3.length() > 0 ? ", " + linkkeyid3value : ""));
                            fkValidationPass = false;
                        }
                        if (missingSDIDesc == null) continue;
                        missingSDIs.append("<br/><br/>" + linksdcid + "(" + linksdckeycolid1 + (linksdckeycolid2.length() > 0 ? ", " + linksdckeycolid2 : "") + (linksdckeycolid3.length() > 0 ? ", " + linksdckeycolid3 : "") + ") Referenced from " + linkfromtableid + " of " + sdcid + " SDC: <br/>" + missingSDIDesc);
                        outputLogger.log("<span style=\"color:red\">ERROR: Referenced SDI Missing: " + linksdcid + "(Referenced from " + linkfromtableid + "):" + missingSDIDesc + "</span>");
                        outputLogger.log("Found with SQL:" + finalSQL);
                    }
                    String paramlistitemcheckSQL = null;
                    if (sdcid.equals("ParamList")) {
                        paramlistitemcheckSQL = "SELECT spi.specid,  spi.specversionid, spi.paramlistid, spi.paramlistversionid, spi.variantid, spi.paramid, spi.paramtype FROM specparamitems spi, rsetitems WHERE rsetitems.sdcid='ParamList' AND spi.paramlistid = rsetitems.keyid1 AND spi.paramlistversionid = rsetitems.keyid2 AND spi.variantid = rsetitems.keyid3 AND rsetitems.rsetid=?  AND NOT EXISTS (SELECT 1 FROM paramlistitem pli where spi.paramlistid =pli.paramlistid AND spi.paramlistversionid = pli.paramlistversionid AND spi.variantid = pli.variantid AND spi.paramid = pli.paramid AND spi.paramtype= pli.paramtype)";
                    } else if (sdcid.equals("SpecSDC")) {
                        paramlistitemcheckSQL = "SELECT spi.specid,  spi.specversionid, spi.paramlistid, spi.paramlistversionid, spi.variantid, spi.paramid, spi.paramtype FROM specparamitems spi, rsetitems WHERE rsetitems.sdcid='SpecSDC' AND spi.specid = rsetitems.keyid1 AND spi.specversionid = rsetitems.keyid2 AND rsetitems.rsetid=?  AND NOT EXISTS (SELECT 1 FROM paramlistitem pli WHERE spi.paramlistid =pli.paramlistid AND spi.paramlistversionid = pli.paramlistversionid AND spi.variantid = pli.variantid AND spi.paramid = pli.paramid AND spi.paramtype= pli.paramtype)";
                    }
                    if (paramlistitemcheckSQL != null) {
                        DataSet missingPli;
                        outputLogger.log("Special Detail to Detail FK link: specparamitem to paramlistitem");
                        if (rsetid.length() == 0) {
                            rsetid = this.getDAMProcessor().createRSet(sdcid, keyid1list, keyid2list, keyid3list);
                        }
                        if ((missingPli = this.getQueryProcessor().getPreparedSqlDataSet(paramlistitemcheckSQL, new Object[]{rsetid})).getRowCount() > 0) {
                            fkValidationPass = false;
                            StringBuilder missingPliSB = new StringBuilder();
                            for (int row = 0; row < missingPli.getRowCount(); ++row) {
                                missingPliSB.append("<br/>&nbsp;&nbsp;&nbsp;&nbsp;" + missingPli.getValue(row, "paramlistid") + ", " + missingPli.getValue(row, "paramlistversionid") + ", " + missingPli.getValue(row, "variantid") + ", " + missingPli.getValue(row, "paramid") + ", " + missingPli.getValue(row, "paramtype"));
                            }
                            missingSDIs.append("<br/><br/> Paramlistitem (Referenced from specparamitem of SpecSDC) missing: <br/>" + missingPliSB);
                            outputLogger.log("<span style=\"color:red\">ERROR: Referenced Paramlistitem Missing: (Referenced from specparamitem of SpecSDC):" + missingPliSB + ".</span>");
                        }
                    }
                    for (String sdixxxTableId : sdixxxTables = SDIData.getDatasetTables()) {
                        if ("primary".equals(sdixxxTableId) || !"categoryitem".equals(sdixxxTableId)) continue;
                        if (rsetid.length() == 0) {
                            rsetid = this.getDAMProcessor().createRSet(sdcid, keyid1list, keyid2list, keyid3list);
                        }
                        String sql = "SELECT DISTINCT ci.categoryid categoryitemcategory, ci.sdcid" + (this.database.isOracle() ? ", NVL(c.categoryid, '(null)')" : ", ISNULL(c.categoryid, '(null)')") + " categorycategory FROM categoryitem ci JOIN rsetitems r ON ci.keyid1 = r.keyid1 AND ci.sdcid = r.sdcid LEFT OUTER JOIN category c ON c.sdcid = ci.sdcid AND c.categoryid = ci.categoryid  WHERE r.rsetid = ?";
                        DataSet categories = this.getQueryProcessor().getPreparedSqlDataSet(sql, new Object[]{rsetid});
                        filterMap.clear();
                        filterMap.put("categorycategory", "(null)");
                        DataSet notFoundCategories = categories.getFilteredDataSet(filterMap);
                        if (notFoundCategories.getRowCount() <= 0) continue;
                        fkValidationPass = false;
                        missingSDIs.append("<br/><br/> Category (Referenced from " + sdixxxTableId + " of " + sdcid + " SDC): " + notFoundCategories.getColumnValues("categoryitemcategory", ";"));
                        outputLogger.log("<span style=\"color:red\">ERROR: Referenced SDI Missing: Category (Referenced from " + sdixxxTableId + "):" + notFoundCategories.getColumnValues("categoryitemcategory", ";") + ".</span>");
                    }
                    if (rsetid.length() <= 0) continue;
                    this.getDAMProcessor().clearRSet(rsetid);
                }
                outputLogger.log("*************** Done validate FK links.");
                if (!fkValidationPass) {
                    throw new SapphireException(this.getTranslationProcessor().translate("Missing referenced SDIs: ") + missingSDIs);
                }
                props.put("oldnewkeymap", oldNewKeyMap);
            } else {
                outputLogger.log("DO NOT validate FK links.");
            }
            if (importedSDCSDIIds.length() > 0) {
                outputLogger.log("Re-enable Audit Tables/triggers for Imported SDCs, if required.");
                this.triggerSDCAuditTablesProc(importedSDCSDIIds.toString());
            }
            if (this.isFromRemoteCheckIn_Master) {
                if (!importSuccess) return;
            }
        }
        catch (SapphireException se) {
            try {
                props.setProperty(PROPERTY_EXCEPTION, se.getMessage());
                outputLogger.log("<span style=\"color:red\">IMPORT FAILED</span>");
                this.logger.error("IMPORT FAILED.", se);
                importSuccess = false;
                throw se;
                catch (Throwable t) {
                    props.setProperty(PROPERTY_EXCEPTION, t.getMessage());
                    outputLogger.log("IMPORT FAILED:" + t.getMessage());
                    this.logger.error("Import Failed Unexpectedly.", t);
                    importSuccess = false;
                    throw new SapphireException(t);
                }
            }
            catch (Throwable throwable) {
                if (this.isFromRemoteCheckIn_Master) {
                    if (!importSuccess) throw throwable;
                }
                try {
                    if (props.getProperty(PROPERTY_EXCEPTION).length() > 0) {
                        outputLogger.log("Error importing in " + (System.currentTimeMillis() - start) + "ms");
                    } else {
                        outputLogger.log("Done importing in " + (System.currentTimeMillis() - start) + "ms");
                    }
                    String transferlogid = "";
                    String importlog = outputLogger.getLog();
                    PropertyList actionProps = new PropertyList();
                    actionProps.setProperty("sdcid", "TransferLog");
                    actionProps.setProperty("notes", props.getProperty("lognotesentered"));
                    actionProps.setProperty("transferlogdesc", props.getProperty("transferlogdesc"));
                    actionProps.setProperty("reason", props.getProperty("esigreasonentered"));
                    actionProps.setProperty("transferlog", importlog);
                    actionProps.setProperty("lasttransferreddt", "n");
                    actionProps.setProperty("lasttransferredby", this.connectionInfo.getSysuserId());
                    if (importSuccess) {
                        actionProps.setProperty("transferstatus", "Completed");
                    } else {
                        actionProps.setProperty("transferstatus", "Error");
                    }
                    if (props.getProperty("transferlogid").length() > 0) {
                        transferlogid = props.getProperty("transferlogid");
                        actionProps.setProperty("keyid1", transferlogid);
                        this.getActionProcessor().processAction("EditSDI", "1", actionProps, true);
                    } else if (createtransferlog) {
                        actionProps.setProperty("transferlogid", "(auto)");
                        actionProps.setProperty("transferlogdesc", "Auto created by ImportSnapshot action");
                        actionProps.setProperty("transfertype", this.isFromRemoteCheckIn_Master ? "Remote Check In" : "Import");
                        this.getActionProcessor().processAction("AddSDI", "1", actionProps);
                        transferlogid = actionProps.getProperty("newkeyid1");
                        Attachment attachment = Attachment.getAttachment("TransferLog", transferlogid, null, null);
                        attachment.setDescription(props.getProperty(PROPERTY_FILENAME) + ".zip");
                        try {
                            attachment.setInputStream(new FileInputStream(file));
                        }
                        catch (FileNotFoundException e) {
                            throw new SapphireException(e);
                        }
                        attachment.setSourceFilename(props.getProperty(PROPERTY_FILENAME) + ".zip");
                        attachment.setAttachmentType(Attachment.AttachmentType.FILE);
                        AttachmentProcessor ap = new AttachmentProcessor(this.getConnectionid());
                        ap.addSDIAttachment(attachment, false, false, "Sapphire Custom");
                    }
                    if (!importSuccess) throw throwable;
                    if (this.isFromRemoteCheckOut_Slave) throw throwable;
                    if (importedSDIList.getRowCount() <= 0) throw throwable;
                    DataSet checkInDs = new DataSet();
                    checkInDs.addColumnValues("sdcid", 0, importedSDIList.getColumnValues("sdcid", ";"), ";");
                    checkInDs.addColumnValues("keyid1", 0, importedSDIList.getColumnValues("keyid1", ";"), ";");
                    checkInDs.addColumnValues("keyid2", 0, importedSDIList.getColumnValues("keyid2", ";"), ";");
                    checkInDs.addColumnValues("keyid3", 0, importedSDIList.getColumnValues("keyid3", ";"), ";");
                    HashMap<String, String> excludefilterMap = new HashMap<String, String>();
                    excludefilterMap.put("sdcid", "Deleted");
                    checkInDs = checkInDs.getFilteredDataSet(excludefilterMap, true);
                    checkInDs.sort("sdcid");
                    ArrayList<DataSet> datasets = checkInDs.getGroupedDataSets("sdcid");
                    for (int i = 0; i < datasets.size(); ++i) {
                        DataSet forsdcDataSets2 = datasets.get(i);
                        PropertyList changelogprops = new PropertyList();
                        String sdcid = forsdcDataSets2.getValue(0, "sdcid");
                        String changeControlledFlag = CMTPolicy.getPolicy(this.getConnectionid(), sdcid).getChangeControlledFlag();
                        if (!"Y".equals(changeControlledFlag) && !"T".equals(changeControlledFlag)) continue;
                        changelogprops.setProperty("sdcid", sdcid);
                        if ("T".equals(changeControlledFlag)) {
                            outputLogger.log("The SDC template is under changecontrol:" + sdcid);
                        } else {
                            outputLogger.log("The SDC is under changecontrol:" + sdcid);
                        }
                        if (forsdcDataSets2.getRowCount() <= 0) continue;
                        String keyid1 = "";
                        String keyid2 = "";
                        String keyid3 = "";
                        String nodeId = "";
                        if ("PropertyTree".equals(sdcid)) {
                            keyid1 = forsdcDataSets2.getColumnValues("keyid1", ";");
                            keyid2 = "";
                            keyid3 = "";
                            nodeId = forsdcDataSets2.getColumnValues("keyid2", ";");
                        } else {
                            keyid1 = forsdcDataSets2.getColumnValues("keyid1", ";");
                            keyid2 = forsdcDataSets2.getColumnValues("keyid2", ";");
                            keyid3 = forsdcDataSets2.getColumnValues("keyid3", ";");
                        }
                        changelogprops.setProperty("keyid1", keyid1);
                        changelogprops.setProperty("keyid2", keyid2);
                        changelogprops.setProperty("keyid3", keyid3);
                        if ("PropertyTree".equals(sdcid)) {
                            changelogprops.setProperty("propertytreenodeid", nodeId);
                            outputLogger.log("Check In the Imported SDIs:" + sdcid + ": keyid1=" + keyid1 + ". Node id: " + nodeId);
                        } else {
                            outputLogger.log("Check In the Imported SDIs:" + sdcid + ": keyid1=" + keyid1 + ": keyid2=" + keyid2 + ": keyid3=" + keyid3);
                        }
                        changelogprops.setProperty("transferlogid", transferlogid);
                        changelogprops.setProperty("notes", props.getProperty("notes"));
                        this.getActionProcessor().processAction("CheckInSDI", "1", changelogprops);
                    }
                    outputLogger.log("Import Complete.");
                    this.logger.info("***********Import log" + outputLogger.getLog());
                    props.setProperty("transferlogid", transferlogid);
                    throw throwable;
                }
                catch (Exception e) {
                    this.logger.error("Import final tasks failed: " + e.getMessage());
                    throw new SapphireException(e);
                }
                finally {
                    if (traceLogId != null) {
                        try {
                            this.logger.info("Remove TracelogId from Session.");
                            outputLogger.log("Remove TracelogId from Session.");
                            audit.setTracelogIdInDBSession("");
                        }
                        catch (ServiceException e) {
                            outputLogger.log("Error occurred when trying to remove TracelogId from Session.");
                            this.logger.error("Error occurred when trying to remove TracelogId from Session.");
                        }
                    }
                }
            }
        }
        try {
            if (props.getProperty(PROPERTY_EXCEPTION).length() > 0) {
                outputLogger.log("Error importing in " + (System.currentTimeMillis() - start) + "ms");
            } else {
                outputLogger.log("Done importing in " + (System.currentTimeMillis() - start) + "ms");
            }
            String transferlogid = "";
            String importlog = outputLogger.getLog();
            PropertyList propertyList = new PropertyList();
            propertyList.setProperty("sdcid", "TransferLog");
            propertyList.setProperty("notes", props.getProperty("lognotesentered"));
            propertyList.setProperty("transferlogdesc", props.getProperty("transferlogdesc"));
            propertyList.setProperty("reason", props.getProperty("esigreasonentered"));
            propertyList.setProperty("transferlog", importlog);
            propertyList.setProperty("lasttransferreddt", "n");
            propertyList.setProperty("lasttransferredby", this.connectionInfo.getSysuserId());
            if (importSuccess) {
                propertyList.setProperty("transferstatus", "Completed");
            } else {
                propertyList.setProperty("transferstatus", "Error");
            }
            if (props.getProperty("transferlogid").length() > 0) {
                transferlogid = props.getProperty("transferlogid");
                propertyList.setProperty("keyid1", transferlogid);
                this.getActionProcessor().processAction("EditSDI", "1", propertyList, true);
            } else if (createtransferlog) {
                propertyList.setProperty("transferlogid", "(auto)");
                propertyList.setProperty("transferlogdesc", "Auto created by ImportSnapshot action");
                propertyList.setProperty("transfertype", this.isFromRemoteCheckIn_Master ? "Remote Check In" : "Import");
                this.getActionProcessor().processAction("AddSDI", "1", propertyList);
                transferlogid = propertyList.getProperty("newkeyid1");
                Attachment attachment = Attachment.getAttachment("TransferLog", transferlogid, null, null);
                attachment.setDescription(props.getProperty(PROPERTY_FILENAME) + ".zip");
                try {
                    attachment.setInputStream(new FileInputStream(file));
                }
                catch (FileNotFoundException e) {
                    throw new SapphireException(e);
                }
                attachment.setSourceFilename(props.getProperty(PROPERTY_FILENAME) + ".zip");
                attachment.setAttachmentType(Attachment.AttachmentType.FILE);
                AttachmentProcessor ap = new AttachmentProcessor(this.getConnectionid());
                ap.addSDIAttachment(attachment, false, false, "Sapphire Custom");
            }
            if (!importSuccess) return;
            if (this.isFromRemoteCheckOut_Slave) return;
            if (importedSDIList.getRowCount() <= 0) return;
            DataSet checkInDs = new DataSet();
            checkInDs.addColumnValues("sdcid", 0, importedSDIList.getColumnValues("sdcid", ";"), ";");
            checkInDs.addColumnValues("keyid1", 0, importedSDIList.getColumnValues("keyid1", ";"), ";");
            checkInDs.addColumnValues("keyid2", 0, importedSDIList.getColumnValues("keyid2", ";"), ";");
            checkInDs.addColumnValues("keyid3", 0, importedSDIList.getColumnValues("keyid3", ";"), ";");
            HashMap<String, String> excludefilterMap = new HashMap<String, String>();
            excludefilterMap.put("sdcid", "Deleted");
            checkInDs = checkInDs.getFilteredDataSet(excludefilterMap, true);
            checkInDs.sort("sdcid");
            ArrayList<DataSet> datasets = checkInDs.getGroupedDataSets("sdcid");
            for (int i = 0; i < datasets.size(); ++i) {
                forsdcDataSets = datasets.get(i);
                PropertyList changelogprops = new PropertyList();
                String sdcid = forsdcDataSets.getValue(0, "sdcid");
                String changeControlledFlag = CMTPolicy.getPolicy(this.getConnectionid(), sdcid).getChangeControlledFlag();
                if (!"Y".equals(changeControlledFlag) && !"T".equals(changeControlledFlag)) continue;
                changelogprops.setProperty("sdcid", sdcid);
                if ("T".equals(changeControlledFlag)) {
                    outputLogger.log("The SDC template is under changecontrol:" + sdcid);
                } else {
                    outputLogger.log("The SDC is under changecontrol:" + sdcid);
                }
                if (forsdcDataSets.getRowCount() <= 0) continue;
                String keyid1 = "";
                String keyid2 = "";
                String keyid3 = "";
                String nodeId = "";
                if ("PropertyTree".equals(sdcid)) {
                    keyid1 = forsdcDataSets.getColumnValues("keyid1", ";");
                    keyid2 = "";
                    keyid3 = "";
                    nodeId = forsdcDataSets.getColumnValues("keyid2", ";");
                } else {
                    keyid1 = forsdcDataSets.getColumnValues("keyid1", ";");
                    keyid2 = forsdcDataSets.getColumnValues("keyid2", ";");
                    keyid3 = forsdcDataSets.getColumnValues("keyid3", ";");
                }
                changelogprops.setProperty("keyid1", keyid1);
                changelogprops.setProperty("keyid2", keyid2);
                changelogprops.setProperty("keyid3", keyid3);
                if ("PropertyTree".equals(sdcid)) {
                    changelogprops.setProperty("propertytreenodeid", nodeId);
                    outputLogger.log("Check In the Imported SDIs:" + sdcid + ": keyid1=" + keyid1 + ". Node id: " + nodeId);
                } else {
                    outputLogger.log("Check In the Imported SDIs:" + sdcid + ": keyid1=" + keyid1 + ": keyid2=" + keyid2 + ": keyid3=" + keyid3);
                }
                changelogprops.setProperty("transferlogid", transferlogid);
                changelogprops.setProperty("notes", props.getProperty("notes"));
                this.getActionProcessor().processAction("CheckInSDI", "1", changelogprops);
            }
            outputLogger.log("Import Complete.");
            this.logger.info("***********Import log" + outputLogger.getLog());
            props.setProperty("transferlogid", transferlogid);
            return;
        }
        catch (Exception e) {
            this.logger.error("Import final tasks failed: " + e.getMessage());
            throw new SapphireException(e);
        }
        finally {
            if (traceLogId != null) {
                try {
                    this.logger.info("Remove TracelogId from Session.");
                    outputLogger.log("Remove TracelogId from Session.");
                    audit.setTracelogIdInDBSession("");
                }
                catch (ServiceException e) {
                    outputLogger.log("Error occurred when trying to remove TracelogId from Session.");
                    this.logger.error("Error occurred when trying to remove TracelogId from Session.");
                }
            }
        }
    }

    private void collectionReferencedItems(SnapshotItem snapshotItem, ArrayList<SnapshotItem> importItemList, ArrayList<SnapshotItem> importPTreeItemList, ArrayList<SnapshotItem> importSDCItemList, SnapshotPackage snapshotPackage, boolean includeToImportList) {
        List<SnapshotItem> linkItems;
        if (includeToImportList && !importItemList.contains(snapshotItem)) {
            importItemList.add(snapshotItem);
            if ("SDC".equals(snapshotItem.getSDCId()) && !importSDCItemList.contains(snapshotItem)) {
                importSDCItemList.add(snapshotItem);
            }
            if ("PropertyTree".equals(snapshotItem.getSDCId()) && !importPTreeItemList.contains(snapshotItem)) {
                importPTreeItemList.add(snapshotItem);
            }
        }
        if ((linkItems = snapshotItem.getLinkItems()) != null && linkItems.size() > 0) {
            ArrayList<SnapshotItem> sortedLinkItems = new ArrayList<SnapshotItem>();
            for (SnapshotItem linkItem : linkItems) {
                if (Snapshot.Type.SDI.equals((Object)snapshotItem.getType()) && this.isVersionedSDC(linkItem.getSDCId())) {
                    this.sortDescending(linkItem, sortedLinkItems);
                    continue;
                }
                sortedLinkItems.add(linkItem);
            }
            for (SnapshotItem linkItem : sortedLinkItems) {
                if (linkItem.isIncludedForTransfer()) {
                    if (snapshotPackage.getSnapshot(linkItem) == null) continue;
                    this.collectionReferencedItems(linkItem, importItemList, importPTreeItemList, importSDCItemList, snapshotPackage, true);
                    continue;
                }
                this.collectionReferencedItems(linkItem, importItemList, importPTreeItemList, importSDCItemList, snapshotPackage, false);
            }
        }
    }

    private void sortDescending(SnapshotItem snapshotItem, List<SnapshotItem> itemList) {
        int processedItemCount = itemList.size();
        for (int index = 0; index < processedItemCount; ++index) {
            SnapshotItem currentsnapshotItem = itemList.get(index);
            if (snapshotItem.getSDCId().equals(currentsnapshotItem.getSDCId()) && snapshotItem.getKeyId1().equals(currentsnapshotItem.getKeyId1()) && snapshotItem.getKeyId3().equals(currentsnapshotItem.getKeyId3())) {
                String versionid = snapshotItem.getKeyId2();
                String currentversionid = currentsnapshotItem.getKeyId2();
                if (Integer.parseInt(versionid) <= Integer.parseInt(currentversionid)) continue;
                itemList.add(index, snapshotItem);
                break;
            }
            if (!snapshotItem.getSDCId().equals("SpecSDC") || !currentsnapshotItem.getSDCId().equals("ParamList")) continue;
            itemList.add(index, snapshotItem);
            break;
        }
        if (processedItemCount == itemList.size()) {
            itemList.add(snapshotItem);
        }
    }

    private boolean isVersionedSDC(String sdcId) {
        if (!this.SDC_VERSIONEDFLAG.containsKey(sdcId)) {
            this.SDC_VERSIONEDFLAG.put(sdcId, "Y".equals(this.getSDCProcessor().getProperty(sdcId, "versionedflag")));
        }
        return this.SDC_VERSIONEDFLAG.get(sdcId);
    }

    private void importSDISnapshot(SDISnapshot sdiSnapshot, boolean isEmbedded, StringLogger outputLogger, ImportInstructions importInstructions, SnapshotItem snapshotItem, DataSet importedSDIList, HashMap<String, HashMap<String, String>> oldNewKeyMap, HashMap<String, HashMap<String, StringBuilder>> fkToValidateMap) throws SapphireException {
        try {
            SDISnapshotItem embeddedItem;
            String[] stringArray;
            boolean isAutoKeyGen;
            SDISnapshotItem sdiSnapshotItem = snapshotItem instanceof SDISnapshotItem ? (SDISnapshotItem)snapshotItem : sdiSnapshot.getSnapshotItem();
            List<SnapshotItem> fklist = sdiSnapshotItem.getLinkItemsByType(SnapshotItem.LinkType.FK);
            List<SnapshotItem> revFKList = sdiSnapshotItem.getLinkItemsByType(SnapshotItem.LinkType.REVFK);
            List<SnapshotItem> softLinkList = sdiSnapshotItem.getLinkItemsByType(SnapshotItem.LinkType.REVSOFTLINK);
            List<SnapshotItem> sqlLinkList = sdiSnapshotItem.getLinkItemsByType(SnapshotItem.LinkType.SQL);
            SDIData sdiData = sdiSnapshotItem.getSDIData();
            DataSet primary = sdiData.getDataset("primary");
            String sdcid = sdiData.getSdcid();
            boolean isDelete = sdiSnapshotItem.isDeleted();
            if (!isDelete) {
                DataSet datasetToUpdate;
                for (int eb = 0; eb < fklist.size(); ++eb) {
                    SDIData embeddedSDIData;
                    SDISnapshotItem embeddedItem2 = (SDISnapshotItem)fklist.get(eb);
                    PropertyList embeddedPL = embeddedItem2.getParentLinkProps();
                    if (embeddedItem2.isIncludedForTransfer() || !Snapshot.Type.SDI.equals((Object)embeddedItem2.getType()) || (embeddedSDIData = fklist.get(eb).getSDIData()) == null) continue;
                    outputLogger.log("Importing FK embedded SDISnapshotItem: " + fklist.get(eb).toString());
                    outputLogger.increaseIndent();
                    this.importSDISnapshot(sdiSnapshot, true, outputLogger, importInstructions, embeddedItem2, importedSDIList, oldNewKeyMap, fkToValidateMap);
                    outputLogger.decreaseIndent();
                }
                ArrayList<SDISnapshotItem> importbeforesoftlinkList = new ArrayList<SDISnapshotItem>();
                if (softLinkList.size() > 0) {
                    for (int eb = 0; eb < softLinkList.size(); ++eb) {
                        SDISnapshotItem embeddedItem3 = (SDISnapshotItem)softLinkList.get(eb);
                        PropertyList embeddedPL = embeddedItem3.getParentLinkProps();
                        if (embeddedItem3.isIncludedForTransfer() || embeddedPL == null || !"Y".equals(embeddedPL.getProperty("importbeforeprimary")) || !Snapshot.Type.SDI.equals((Object)embeddedItem3.getType())) continue;
                        SDIData embeddedSDIData = softLinkList.get(eb).getSDIData();
                        if (embeddedSDIData != null) {
                            outputLogger.log("Importing softlink embedded SDISnapshotItem(Before Primary): " + softLinkList.get(eb).toString());
                            outputLogger.increaseIndent();
                            this.importSDISnapshot(sdiSnapshot, true, outputLogger, importInstructions, embeddedItem3, importedSDIList, oldNewKeyMap, fkToValidateMap);
                            outputLogger.decreaseIndent();
                        }
                        importbeforesoftlinkList.add(embeddedItem3);
                    }
                    softLinkList.removeAll(importbeforesoftlinkList);
                }
                outputLogger.log("Importing " + sdiSnapshotItem.toString(true, true));
                PropertyListCollection links = this.getSDCProcessor().getLinks(sdcid);
                for (int linkno = 0; linkno < links.size(); ++linkno) {
                    PropertyList linkPL = links.getPropertyList(linkno);
                    String linktype = linkPL.getProperty("linktype");
                    String linksdcid = linkPL.getProperty("linksdcid");
                    HashMap<String, String> keyMapForLinkedSDC = oldNewKeyMap.get(linksdcid);
                    boolean isMtoMLink = "M".equals(linktype);
                    if (!"F".equals(linktype) && !isMtoMLink) continue;
                    String linkmtableid = linkPL.getProperty("linktableid");
                    String sdccolumnid = linkPL.getProperty("sdccolumnid");
                    String sdccolumnid2 = linkPL.getProperty("sdccolumnid2");
                    String sdccolumnid3 = linkPL.getProperty("sdccolumnid3");
                    DataSet dataSet = datasetToUpdate = isMtoMLink ? sdiData.getDataset(linkmtableid) : primary;
                    if (datasetToUpdate == null) continue;
                    for (int i = 0; i < datasetToUpdate.getRowCount(); ++i) {
                        String newkeys;
                        if (datasetToUpdate.getValue(i, sdccolumnid).length() <= 0 && !isMtoMLink) continue;
                        String oldkeys = datasetToUpdate.getValue(i, sdccolumnid) + ";" + datasetToUpdate.getValue(i, sdccolumnid2) + ";" + datasetToUpdate.getValue(i, sdccolumnid3);
                        String string = newkeys = keyMapForLinkedSDC != null ? keyMapForLinkedSDC.get(oldkeys) : null;
                        if (newkeys != null && newkeys.length() > 0) {
                            String[] updatekeys = StringUtil.split(newkeys, ";");
                            outputLogger.log("Updated FK fields " + sdccolumnid + ";" + sdccolumnid2 + ";" + sdccolumnid3 + " in " + (isMtoMLink ? linkmtableid : sdcid) + " from " + oldkeys + " to " + newkeys);
                            datasetToUpdate.setValue(i, sdccolumnid, updatekeys[0]);
                            datasetToUpdate.setValue(i, sdccolumnid2, updatekeys[1]);
                            datasetToUpdate.setValue(i, sdccolumnid3, updatekeys[2]);
                            continue;
                        }
                        if (fkToValidateMap.get(linksdcid) == null) {
                            HashMap<String, StringBuilder> valueMap = new HashMap<String, StringBuilder>();
                            valueMap.put("keyid1", new StringBuilder());
                            valueMap.put("keyid2", new StringBuilder());
                            valueMap.put("keyid3", new StringBuilder());
                            fkToValidateMap.put(linksdcid, valueMap);
                        }
                        fkToValidateMap.get(linksdcid).get("keyid1").append(";" + datasetToUpdate.getValue(i, isMtoMLink ? this.getSDCProcessor().getProperty(linksdcid, "keycolid1") : sdccolumnid));
                        fkToValidateMap.get(linksdcid).get("keyid2").append(";" + datasetToUpdate.getValue(i, isMtoMLink ? this.getSDCProcessor().getProperty(linksdcid, "keycolid2") : sdccolumnid2));
                        fkToValidateMap.get(linksdcid).get("keyid3").append(";" + datasetToUpdate.getValue(i, isMtoMLink ? this.getSDCProcessor().getProperty(linksdcid, "keycolid3") : sdccolumnid3));
                    }
                }
                PropertyListCollection detaillinks = this.getSDCProcessor().getDetailLinks(sdcid);
                for (int linkno = 0; linkno < detaillinks.size(); ++linkno) {
                    PropertyList linkPL = detaillinks.getPropertyList(linkno);
                    String linktype = linkPL.getProperty("linktype");
                    String linksdcid = linkPL.getProperty("linksdcid");
                    HashMap<String, String> keyMapForLinkedSDC = oldNewKeyMap.get(linksdcid);
                    if (!"F".equals(linktype) || keyMapForLinkedSDC == null) continue;
                    String sdccolumnid = linkPL.getProperty("sdccolumnid");
                    String sdccolumnid2 = linkPL.getProperty("sdccolumnid2");
                    String sdccolumnid3 = linkPL.getProperty("sdccolumnid3");
                    String linkfromtableid = linkPL.getProperty("linktableid");
                    datasetToUpdate = sdiData.getDataset(linkfromtableid);
                    if (datasetToUpdate == null) continue;
                    this.repopulateFKColumnValues(linksdcid, sdccolumnid, sdccolumnid2, sdccolumnid3, keyMapForLinkedSDC, fkToValidateMap, linkfromtableid, datasetToUpdate, outputLogger);
                }
                String linksdcid = "LV_Calendar";
                HashMap<String, String> keyMapForLinkedSDC = oldNewKeyMap.get(linksdcid);
                DataSet datasetToUpdate2 = sdiData.getDataset("calendar");
                if (keyMapForLinkedSDC != null && datasetToUpdate2 != null && datasetToUpdate2.getRowCount() > 0) {
                    String linkfromtableid = "sdicalendar";
                    String sdccolumnid = "calendarid";
                    String sdccolumnid2 = "";
                    String sdccolumnid3 = "";
                    this.repopulateFKColumnValues(linksdcid, sdccolumnid, sdccolumnid2, sdccolumnid3, keyMapForLinkedSDC, fkToValidateMap, linkfromtableid, datasetToUpdate2, outputLogger);
                }
                linksdcid = "ParamList";
                keyMapForLinkedSDC = oldNewKeyMap.get(linksdcid);
                if ("SpecSDC".equals(sdcid) && keyMapForLinkedSDC != null) {
                    datasetToUpdate2 = sdiData.getDataset("specparamitems");
                    if (datasetToUpdate2 != null) {
                        this.repopulateFKColumnValues(linksdcid, "paramlistid", "paramlistversionid", "variantid", keyMapForLinkedSDC, fkToValidateMap, "specparamitems", datasetToUpdate2, outputLogger);
                    }
                    if ((datasetToUpdate2 = sdiData.getDataset("specparamlimits")) != null) {
                        this.repopulateFKColumnValues(linksdcid, "paramlistid", "paramlistversionid", "variantid", keyMapForLinkedSDC, fkToValidateMap, "specparamlimits", datasetToUpdate2, outputLogger);
                    }
                }
            }
            String tableid = this.getSDCProcessor().getProperty(sdcid, "tableid");
            int keycolumns = Integer.parseInt(this.getSDCProcessor().getProperty(sdcid, "keycolumns"));
            String keycolid1 = this.getSDCProcessor().getProperty(sdcid, "keycolid1");
            String keycolid2 = this.getSDCProcessor().getProperty(sdcid, "keycolid2");
            String keycolid3 = this.getSDCProcessor().getProperty(sdcid, "keycolid3");
            boolean bl = isAutoKeyGen = this.getSDCProcessor().getProperty(sdcid, "keygenerationrule").length() > 0;
            if (keycolumns == 3) {
                String[] stringArray2 = new String[3];
                stringArray2[0] = keycolid1;
                stringArray2[1] = keycolid2;
                stringArray = stringArray2;
                stringArray2[2] = keycolid3;
            } else if (keycolumns == 2) {
                String[] stringArray3 = new String[2];
                stringArray3[0] = keycolid1;
                stringArray = stringArray3;
                stringArray3[1] = keycolid2;
            } else {
                String[] stringArray4 = new String[1];
                stringArray = stringArray4;
                stringArray4[0] = keycolid1;
            }
            String[] keycols = stringArray;
            String keyid1 = primary.getValue(0, keycolid1);
            String keyid2 = primary.getValue(0, keycolid2);
            String keyid3 = primary.getValue(0, keycolid3);
            String importedkeyid1 = keyid1;
            String importedkeyid2 = keyid2;
            String importedkeyid3 = keyid3;
            String policyNodeid = sdiSnapshotItem.getPolicyNodeId();
            PropertyList sourcepolicyPL = sdiSnapshot.getPolicyNodeProps(policyNodeid);
            String sourceimportoption = "";
            String sourceimportversionedoption = "";
            CMTPolicy sourcePolicy = null;
            sourcePolicy = sourcepolicyPL != null && sourcepolicyPL.size() > 0 ? new CMTPolicy(sourcepolicyPL) : CMTPolicy.getPolicy(this.connectionInfo.getConnectionId(), sdcid, "Sapphire Custom");
            boolean doNotImport = "Do Not Import".equals(importInstructions.getImportOption(sdcid, keyid1, keyid2, keyid3));
            if (doNotImport) {
                outputLogger.log("Do Not Import: " + sdcid + ";" + keyid1 + ";" + keyid2 + ";" + keyid3);
                if ("Y".equals(this.getSDCProcessor().getProperty(sdcid, "versionedflag")) || sourcePolicy.getIndentifyColumn().length() == 0 && !isAutoKeyGen) {
                    return;
                }
                outputLogger.log("Do Not Import. Only map target SDI auto key to the SDI in the package as it may be referenced.");
            }
            sourceimportoption = sourcePolicy.getImportOption();
            sourceimportversionedoption = sourcePolicy.getImportVersionedSDIOption();
            CMTPolicy targetPolicy = CMTPolicy.getPolicy(this.getConnectionid(), sdcid, sdiSnapshot != null ? sdiSnapshot.getPolicyNodeId() : "");
            String changecontrolFlag = targetPolicy.getChangeControlledFlag();
            String sdcchangecontrolledFlag = this.getSDCProcessor().getProperty(sdcid, "changecontrolledflag");
            String templateFlag = primary.getValue(0, "templateflag");
            boolean isChangeControlled = "Y".equals(changecontrolFlag) || "T".equals(changecontrolFlag) && "Y".equals(templateFlag) || "R".equals(changecontrolFlag) && this.isFromRemoteCheckOut_Slave && ("Y".equals(sdcchangecontrolledFlag) || "T".equals(sdcchangecontrolledFlag) && "Y".equals(templateFlag));
            ConfigTransferOption transferOption = null;
            if (sourcePolicy != null) {
                transferOption = new ConfigTransferOption(sourcePolicy.getPolicyPropertyList().getPropertyList("primary").getPropertyList("transferoption"));
            }
            String sdilabel = sdcid + ";" + keyid1 + ";" + keyid2 + ";" + keyid3;
            if (!doNotImport && transferOption != null && transferOption.isFlushTarget()) {
                outputLogger.log("Flush target by calling DeleteSDI on " + sdilabel);
                PropertyList actionprops = new PropertyList();
                actionprops.setProperty("sdcid", sdcid);
                actionprops.setProperty("keyid1", keyid1);
                actionprops.setProperty("keyid2", keyid2);
                actionprops.setProperty("keyid3", keyid3);
                actionprops.setProperty("donotvalidate", "Y");
                this.getActionProcessor().processAction("DeleteSDI", "1", actionprops);
                outputLogger.log("Done flushing target.");
            }
            if ("Y".equals(this.getSDCProcessor().getProperty(sdcid, "versionedflag"))) {
                String parentKeyId3;
                String parentKeyId2;
                String parentKeyId1;
                String parentSdcId;
                boolean createNewVersion = true;
                String versionstatus = primary.getValue(0, "versionstatus");
                boolean isTransferExpiredVersion = "E".equals(versionstatus);
                boolean isTransferCurrentVersion = "C".equals(versionstatus);
                if (this.isFromRemoteCheckOut_Slave || this.isFromRemoteCheckIn_Master) {
                    createNewVersion = false;
                } else if (isTransferExpiredVersion) {
                    createNewVersion = false;
                    outputLogger.log("Version status is Expired. Override existing if the same version found.");
                } else if (importInstructions != null && importInstructions.getImportOption(sdcid, keyid1, keyid2, keyid3).length() > 0) {
                    outputLogger.log("Import Instruction:" + importInstructions.getImportOption(sdcid, keyid1, keyid2, keyid3));
                    createNewVersion = importInstructions.isCreateNewVersion(sdcid, keyid1, keyid2, keyid3);
                } else if ("LV_WorksheetSection".equals(sdcid)) {
                    parentSdcId = sdiSnapshotItem.getParent().getSDCId();
                    parentKeyId1 = sdiSnapshotItem.getParent().getKeyId1();
                    parentKeyId2 = sdiSnapshotItem.getParent().getKeyId2();
                    parentKeyId3 = sdiSnapshotItem.getParent().getKeyId3();
                    outputLogger.log("Import Instruction for Parent Worksheet:" + importInstructions.getImportOption(parentSdcId, parentKeyId1, parentKeyId2, parentKeyId3));
                    createNewVersion = importInstructions.isCreateNewVersion(parentSdcId, parentKeyId1, parentKeyId2, parentKeyId3);
                    if (createNewVersion) {
                        outputLogger.log("Import Instruction for " + sdcid + " is set to " + "Create New Version");
                    }
                } else if ("LV_WorksheetItem".equals(sdcid)) {
                    parentSdcId = sdiSnapshotItem.getParent().getParent().getSDCId();
                    parentKeyId1 = sdiSnapshotItem.getParent().getParent().getKeyId1();
                    parentKeyId2 = sdiSnapshotItem.getParent().getParent().getKeyId2();
                    parentKeyId3 = sdiSnapshotItem.getParent().getParent().getKeyId3();
                    outputLogger.log("Import Instruction for Parent Worksheet:" + importInstructions.getImportOption(parentSdcId, parentKeyId1, parentKeyId2, parentKeyId3));
                    createNewVersion = importInstructions.isCreateNewVersion(parentSdcId, parentKeyId1, parentKeyId2, parentKeyId3);
                    if (createNewVersion) {
                        outputLogger.log("Import Instruction for " + sdcid + " is set to " + "Create New Version");
                    }
                } else {
                    outputLogger.log("Using Import Option from Source Policy Node in Snapshot:" + sourceimportversionedoption);
                    createNewVersion = sourceimportversionedoption.equals("Create New Version");
                }
                if (!createNewVersion) {
                    Object[] objectArray;
                    String sql = "SELECT versionstatus from " + tableid + " WHERE " + keycolid1 + "=? AND " + keycolid2 + "=?" + (keycolumns == 3 ? " AND " + keycolid3 + "=?" : "");
                    QueryProcessor queryProcessor = this.getQueryProcessor();
                    if (keycolumns == 3) {
                        Object[] objectArray2 = new Object[3];
                        objectArray2[0] = keyid1;
                        objectArray2[1] = keyid2;
                        objectArray = objectArray2;
                        objectArray2[2] = keyid3;
                    } else {
                        Object[] objectArray3 = new Object[2];
                        objectArray3[0] = keyid1;
                        objectArray = objectArray3;
                        objectArray3[1] = keyid2;
                    }
                    DataSet ds = queryProcessor.getPreparedSqlDataSet(sql, objectArray);
                    if (isTransferExpiredVersion) {
                        if (ds.getRowCount() == 0) {
                            createNewVersion = true;
                            outputLogger.log("Transfer Expired version SDI. Target SDI with same version not found. So import will create the same.");
                        } else {
                            outputLogger.log("Transfer Expired version SDI. Target SDI with same version found. So import will update existing SDIs.");
                        }
                    } else if (ds.getRowCount() == 0 || !"P".equals(ds.getValue(0, "versionstatus"))) {
                        createNewVersion = true;
                        outputLogger.log("Versioned SDC Transfer Option override existing if provisional but target SDI is not provisional or not exist. So import will create new version.");
                    } else {
                        outputLogger.log("Versioned SDC Transfer Option override existing if provisional and target SDI is provisional. So import will update existing SDIs.");
                    }
                } else {
                    outputLogger.log("Versioned SDC Transfer Option always create new version. So import will create new version.");
                }
                if (transferOption.isCreateNewVersion()) {
                    outputLogger.log("Transfer option is Always Create New Version. Regenerate new uuid when Regenerate Key.");
                    primary.setValue(0, "uuid", "");
                }
                PropertyList actionprops = new PropertyList();
                actionprops.setProperty("sdcid", sdcid);
                actionprops.put("sdisnapshot", sdiSnapshot);
                actionprops.put("sdisnapshotitem", snapshotItem);
                actionprops.put("__importSource", this.importSource);
                actionprops.put("oldnewkeymap", oldNewKeyMap);
                actionprops.put("importInstructions", importInstructions);
                actionprops.put("outputLogger", outputLogger);
                if (createNewVersion || "LV_ChildSamplePlan".equals(sdcid) && isEmbedded) {
                    outputLogger.log("Create new version for " + sdcid + ";" + keyid1 + ";" + keyid2 + ";" + keyid3);
                    if (isChangeControlled) {
                        actionprops.setProperty("checkedoutbyuserid", this.connectionInfo.getSysuserId());
                        actionprops.setProperty("changelogreason", "From Import");
                        actionprops.setProperty("changerequestid", this.changerequestid);
                        actionprops.setProperty("cmtchangerequestid", this.changerequestid);
                    }
                    if ("LV_Worksheet".equals(sdcid) || "LV_WorksheetItem".equals(sdcid) || "LV_WorksheetSection".equals(sdcid)) {
                        actionprops.setProperty("importsnapshot", "Y");
                    }
                    actionprops.setProperty("forcesimplecopyattributes", "Y");
                    if (isTransferExpiredVersion) {
                        this.getActionProcessor().processAction("AddSDI", "1", actionprops);
                        outputLogger.log("Source SDI version status Expired. Setting imported SDI versionstatus Expired for version " + actionprops.getProperty("newkeyid2"));
                        actionprops = new PropertyList();
                        actionprops.setProperty("sdcid", sdcid);
                        actionprops.setProperty("keyid1", importedkeyid1);
                        actionprops.setProperty("keyid2", importedkeyid2);
                        actionprops.setProperty("keyid3", importedkeyid3);
                        actionprops.setProperty("versionstatus", "E");
                        if ("LV_Worksheet".equals(sdcid) || "LV_WorksheetItem".equals(sdcid) || "LV_WorksheetSection".equals(sdcid)) {
                            actionprops.setProperty("importsnapshot", "Y");
                        }
                        this.getActionProcessor().processAction("SetSDIVersionStatus", "1", actionprops);
                    } else {
                        this.getActionProcessor().processAction("AddSDIVersion", "1", actionprops);
                        importedkeyid1 = actionprops.getProperty("newkeyid1");
                        importedkeyid2 = actionprops.getProperty("newkeyid2");
                        importedkeyid3 = actionprops.getProperty("newkeyid3");
                    }
                    if (oldNewKeyMap.get(sdcid) == null) {
                        oldNewKeyMap.put(sdcid, new HashMap());
                    }
                    oldNewKeyMap.get(sdcid).put(keyid1 + ";" + keyid2 + ";" + keyid3, importedkeyid1 + ";" + importedkeyid2 + ";" + importedkeyid3);
                    outputLogger.log("Created new version " + actionprops.getProperty("newkeyid2"));
                    if ("C".equals(versionstatus) || "A".equals(versionstatus)) {
                        outputLogger.log("Source SDI version status Current or Active. Setting imported SDI versionstatus current or Active for version " + actionprops.getProperty("newkeyid2"));
                        actionprops = new PropertyList();
                        actionprops.setProperty("sdcid", sdcid);
                        actionprops.setProperty("keyid1", importedkeyid1);
                        actionprops.setProperty("keyid2", importedkeyid2);
                        actionprops.setProperty("keyid3", importedkeyid3);
                        actionprops.setProperty("versionstatus", versionstatus);
                        actionprops.setProperty("overwriteapproveddtflag", "N");
                        if ("LV_Worksheet".equals(sdcid) || "LV_WorksheetItem".equals(sdcid) || "LV_WorksheetSection".equals(sdcid)) {
                            actionprops.setProperty("importsnapshot", "Y");
                        }
                        this.getActionProcessor().processAction("SetSDIVersionStatus", "1", actionprops);
                    }
                } else if (!sdiSnapshotItem.isDeleted() && (!this.isFromRemoteCheckOut_Slave && transferOption != null && transferOption.isIgnoreIfExists() || this.alwaysIgnoreIfExists)) {
                    outputLogger.log("Import ignored as provisional SDI exists for " + sdcid + ";" + keyid1 + ";" + keyid2 + ";" + keyid3);
                } else {
                    outputLogger.log("Update existing provisional SDI for " + sdcid + ";" + keyid1 + ";" + keyid2 + ";" + keyid3);
                    if (isChangeControlled && !this.isUndoCheckoutOperation) {
                        outputLogger.log("The sdi is under changecontrol");
                        PropertyList checkoutProps = new PropertyList();
                        checkoutProps.setProperty("sdcid", sdcid);
                        checkoutProps.setProperty("keyid1", keyid1);
                        checkoutProps.setProperty("keyid2", keyid2);
                        checkoutProps.setProperty("keyid3", keyid3);
                        checkoutProps.setProperty("local", "Y");
                        checkoutProps.setProperty("changelogreason", this.isFromRemoteCheckOut_Slave ? "Check Out From Repository" : "From Import");
                        checkoutProps.setProperty("changerequestid", this.changerequestid);
                        this.getActionProcessor().processAction("CheckOutSDI", "1", checkoutProps);
                        outputLogger.log("Checked out the SDI");
                    }
                    if ("LV_Worksheet".equals(sdcid) || "LV_WorksheetItem".equals(sdcid) || "LV_WorksheetSection".equals(sdcid)) {
                        actionprops.setProperty("importsnapshot", "Y");
                    }
                    if (sdiSnapshotItem.isDeleted()) {
                        outputLogger.log("Delete SDI:" + sdiSnapshotItem.getSDCId() + ";" + sdiSnapshotItem.getKeyId1() + ";" + sdiSnapshotItem.getKeyId2() + ";" + sdiSnapshotItem.getKeyId3());
                        actionprops.setProperty("keyid1", sdiSnapshotItem.getKeyId1());
                        actionprops.setProperty("keyid2", sdiSnapshotItem.getKeyId2());
                        actionprops.setProperty("keyid3", sdiSnapshotItem.getKeyId3());
                        this.getActionProcessor().processAction("DeleteSDI", "1", actionprops);
                        outputLogger.log("Done Delete");
                    } else {
                        this.getActionProcessor().processAction("EditSDI", "1", actionprops);
                        outputLogger.log("Done update");
                        if (isTransferCurrentVersion) {
                            outputLogger.log("Source SDI version status Current. Setting imported SDI versionstatus current for version " + keyid2);
                            actionprops = new PropertyList();
                            actionprops.setProperty("sdcid", sdcid);
                            actionprops.setProperty("keyid1", keyid1);
                            actionprops.setProperty("keyid2", keyid2);
                            actionprops.setProperty("keyid3", keyid3);
                            actionprops.setProperty("versionstatus", "C");
                            actionprops.setProperty("overwriteapproveddtflag", "N");
                            if ("LV_Worksheet".equals(sdcid) || "LV_WorksheetItem".equals(sdcid) || "LV_WorksheetSection".equals(sdcid)) {
                                actionprops.setProperty("importsnapshot", "Y");
                            }
                            this.getActionProcessor().processAction("SetSDIVersionStatus", "1", actionprops);
                        }
                    }
                }
            } else {
                PropertyList linkProps;
                SDISnapshotItem item = (SDISnapshotItem)snapshotItem;
                PropertyList actionprops = new PropertyList();
                actionprops.setProperty("sdcid", sdcid);
                actionprops.put("sdisnapshot", sdiSnapshot);
                actionprops.put("sdisnapshotitem", snapshotItem);
                actionprops.put("oldnewkeymap", oldNewKeyMap);
                actionprops.put("importInstructions", importInstructions);
                actionprops.put("outputLogger", outputLogger);
                PropertyList propertyList = linkProps = item.getParent() != null ? item.getParentLinkProps() : null;
                if (linkProps != null) {
                    transferOption = new CMTPolicy(linkProps).getTransferOption();
                }
                boolean matchedbykeys = false;
                String identifycolumn = sourcePolicy.getIndentifyColumn();
                if (identifycolumn.contains("LV_QUERY")) {
                    identifycolumn = targetPolicy.getIndentifyColumn();
                }
                String matchedkeyid1 = "";
                String matchedkeyid2 = "";
                String matchedkeyid3 = "";
                boolean isRegenerateKey = transferOption.isRegenerateKey();
                if (identifycolumn.length() == 0 && !isRegenerateKey) {
                    Object[] objectArray;
                    String sql = "SELECT " + keycolid1 + " from " + tableid + " WHERE " + keycolid1 + "=?" + (keycolumns > 1 ? " AND " + keycolid2 + "=?" : "") + (keycolumns > 2 ? " AND " + keycolid3 + "=?" : "");
                    QueryProcessor queryProcessor = this.getQueryProcessor();
                    if (keycolumns == 3) {
                        Object[] objectArray4 = new Object[3];
                        objectArray4[0] = keyid1;
                        objectArray4[1] = keyid2;
                        objectArray = objectArray4;
                        objectArray4[2] = keyid3;
                    } else if (keycolumns == 2) {
                        Object[] objectArray5 = new Object[2];
                        objectArray5[0] = keyid1;
                        objectArray = objectArray5;
                        objectArray5[1] = keyid2;
                    } else {
                        Object[] objectArray6 = new Object[1];
                        objectArray = objectArray6;
                        objectArray6[0] = keyid1;
                    }
                    DataSet ds = queryProcessor.getPreparedSqlDataSet(sql, objectArray);
                    boolean bl2 = matchedbykeys = ds.getRowCount() == 1;
                }
                if (!matchedbykeys && isAutoKeyGen && !isRegenerateKey) {
                    if (identifycolumn.length() == 0 && primary.getValue(0, "uuid").length() > 0) {
                        identifycolumn = "uuid";
                    }
                    if (identifycolumn.length() > 0) {
                        String[] idcolumns = RequestParser.parseColItem("primary[" + identifycolumn + "]");
                        SafeSQL safeSQL = new SafeSQL();
                        String sql = "SELECT * FROM " + tableid + " WHERE ";
                        for (int c = 0; c < idcolumns.length; ++c) {
                            String columnid = idcolumns[c];
                            String columnalias = idcolumns[c];
                            boolean isAlias = false;
                            if (idcolumns[c].lastIndexOf(" ") > 0) {
                                columnid = idcolumns[c].substring(0, idcolumns[c].lastIndexOf(" "));
                                columnalias = idcolumns[c].substring(idcolumns[c].lastIndexOf(" ")).trim();
                                isAlias = true;
                            }
                            String colvalue = primary.getValue(0, columnalias);
                            sql = sql + (c == 0 ? "" : " AND ") + "(" + columnid + "=" + safeSQL.addVar(colvalue) + (colvalue.length() == 0 ? " OR " + columnid + " is null" : "") + ")";
                            if (!isAlias) continue;
                            primary.renameColumn(columnalias, "__" + columnalias);
                        }
                        String matchedby = "Alter Identifier Column (" + identifycolumn + ")";
                        outputLogger.log("Find match with " + matchedby);
                        DataSet matchDataSet = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
                        matchedkeyid1 = matchDataSet.getColumnValues(keycolid1, ";");
                        matchedkeyid2 = matchDataSet.getColumnValues(keycolid2, ";");
                        matchedkeyid3 = matchDataSet.getColumnValues(keycolid3, ";");
                        if (matchDataSet.getRowCount() == 0) {
                            outputLogger.log("SDI Match not found");
                        } else if (matchDataSet.getRowCount() == 1) {
                            outputLogger.log("SDI Match found:" + matchedkeyid1 + ";" + matchedkeyid2 + ";" + matchedkeyid3);
                        } else if (matchDataSet.getRowCount() > 1) {
                            throw new SapphireException("Multiple match found: " + matchedkeyid1 + ";" + matchedkeyid2 + ";" + matchedkeyid3);
                        }
                    }
                }
                boolean isIgnoreIfExists = transferOption.isIgnoreIfExists();
                if (importInstructions != null && importInstructions.getImportOption(sdcid, keyid1, keyid2, keyid3).length() > 0) {
                    outputLogger.log("Import Instruction found: " + importInstructions.getImportOption(sdcid, keyid1, keyid2, keyid3));
                    if (importInstructions.getImportOption(sdcid, keyid1, keyid2, keyid3).equals("Ignore If Exists")) {
                        isIgnoreIfExists = true;
                    }
                    if (importInstructions.getImportOption(sdcid, keyid1, keyid2, keyid3).equals("Regenerate Auto Key")) {
                        isRegenerateKey = true;
                    }
                } else {
                    outputLogger.log("Using Import Option from Source Policy Node in Snapshot:" + sourceimportoption);
                }
                if (doNotImport) {
                    outputLogger.log("Import ignored as instructed");
                } else if ((matchedbykeys || matchedkeyid1.length() > 0 || isAutoKeyGen && doNotImport) && !isRegenerateKey) {
                    if (!sdiSnapshotItem.isDeleted() && (!this.isFromRemoteCheckOut_Slave && isIgnoreIfExists || this.alwaysIgnoreIfExists || doNotImport)) {
                        if (doNotImport) {
                            outputLogger.log("Import ignored as instructed");
                        } else {
                            outputLogger.log("Import ignored as SDI exists for " + sdcid + ";" + keyid1 + ";" + keyid2 + ";" + keyid3);
                        }
                        if (!matchedbykeys) {
                            outputLogger.log("Updated SDI to new matched keys:" + sdcid + ";" + matchedkeyid1 + ";" + matchedkeyid2 + ";" + matchedkeyid3 + " matched by identify column " + identifycolumn);
                            if (oldNewKeyMap.get(sdcid) == null) {
                                oldNewKeyMap.put(sdcid, new HashMap());
                            }
                            oldNewKeyMap.get(sdcid).put(keyid1 + ";" + keyid2 + ";" + keyid3, matchedkeyid1 + ";" + matchedkeyid2 + ";" + matchedkeyid3);
                        }
                    } else {
                        if (matchedkeyid1.length() > 0) {
                            outputLogger.log("Matched by identify column " + identifycolumn + ". Update existing SDI for " + sdcid + ";" + matchedkeyid1 + ";" + matchedkeyid2 + ";" + matchedkeyid3);
                        } else {
                            outputLogger.log("Update existing SDI for " + sdcid + ";" + keyid1 + ";" + keyid2 + ";" + keyid3);
                        }
                        if (!isEmbedded && isChangeControlled && !this.isUndoCheckoutOperation) {
                            outputLogger.log(sdcid + " is under changecontrol. Checked out the SDI.");
                            PropertyList checkoutProps = new PropertyList();
                            checkoutProps.setProperty("sdcid", sdcid);
                            checkoutProps.setProperty("keyid1", matchedkeyid1.length() > 0 ? matchedkeyid1 : keyid1);
                            checkoutProps.setProperty("keyid2", matchedkeyid2.length() > 0 ? matchedkeyid2 : keyid2);
                            checkoutProps.setProperty("keyid3", matchedkeyid3.length() > 0 ? matchedkeyid3 : keyid3);
                            checkoutProps.setProperty("local", "Y");
                            checkoutProps.setProperty("changelogreason", this.isFromRemoteCheckOut_Slave ? "From Repository" : "From Import");
                            checkoutProps.setProperty("changerequestid", this.changerequestid);
                            if ("PropertyTree".equals(sdcid)) {
                                PropertyTreeSnapshotItem pTreeSnapshotItem = (PropertyTreeSnapshotItem)snapshotItem;
                                checkoutProps.setProperty("propertytreenodeid", pTreeSnapshotItem.getNodeId());
                            }
                            this.getActionProcessor().processAction("CheckOutSDI", "1", checkoutProps);
                        }
                        if ("LV_Worksheet".equals(sdcid) || "LV_WorksheetItem".equals(sdcid) || "LV_WorksheetSection".equals(sdcid)) {
                            actionprops.setProperty("importsnapshot", "Y");
                        }
                        if (!matchedbykeys) {
                            actionprops.setProperty("keyid1", matchedkeyid1);
                            actionprops.setProperty("keyid2", matchedkeyid2);
                            actionprops.setProperty("keyid3", matchedkeyid3);
                            SDIData sdiData1 = sdiSnapshotItem.getSDIData();
                            sdiData1.setKeys("primary", matchedkeyid1, matchedkeyid2, matchedkeyid3);
                            Set datasets = sdiData1.getDatasets();
                            for (Object key : datasets) {
                                String datasetname = (String)key;
                                if (datasetname.equals("sdiworkitemattribute") || datasetname.equals("datasetattribute") || datasetname.equals("dataitemattribute") || datasetname.equals("attachmentattribute")) continue;
                                if (!"primary".equals(datasetname)) {
                                    if ("".equals(matchedkeyid2)) {
                                        matchedkeyid2 = "(null)";
                                    }
                                    if ("".equals(matchedkeyid3)) {
                                        matchedkeyid3 = "(null)";
                                    }
                                }
                                sdiData1.setKeys(datasetname, matchedkeyid1, matchedkeyid2, matchedkeyid3);
                                DataSet dataSet = sdiData1.getDataset(datasetname);
                                boolean keyReplaced = false;
                                if (!keyReplaced) {
                                    String[] keycolids = new String[]{keycolid1, keycolid2, keycolid3};
                                    String[] matchedkeys = new String[]{matchedkeyid1, matchedkeyid2, matchedkeyid3};
                                    for (int k = 0; k < keycolids.length; ++k) {
                                        if (keycolids[k].length() <= 0 || !dataSet.isValidColumn(keycolids[k])) continue;
                                        for (int row = 0; row < dataSet.getRowCount(); ++row) {
                                            dataSet.setValue(row, keycolids[k], matchedkeys[k]);
                                        }
                                    }
                                }
                                for (int i = 0; i < dataSet.getRowCount(); ++i) {
                                    HashMap rowMap = (HashMap)dataSet.get(i);
                                    if (rowMap == null || !rowMap.containsValue(matchedkeyid1)) continue;
                                    keyReplaced = true;
                                }
                                if (keyReplaced) continue;
                                throw new SapphireException("Key not replaced from " + keyid1 + " to " + matchedkeyid1 + " for " + datasetname + ":" + dataSet.toJSONString());
                            }
                        }
                        if (sdiSnapshotItem.isDeleted()) {
                            outputLogger.log("Delete SDI:" + sdiSnapshotItem.getSDCId() + ";" + sdiSnapshotItem.getKeyId1() + ";" + sdiSnapshotItem.getKeyId2() + ";" + sdiSnapshotItem.getKeyId3());
                            actionprops.setProperty("keyid1", matchedbykeys ? keyid1 : matchedkeyid1);
                            actionprops.setProperty("keyid2", matchedbykeys ? keyid2 : matchedkeyid2);
                            actionprops.setProperty("keyid3", matchedbykeys ? keyid3 : matchedkeyid3);
                            this.getActionProcessor().processAction("DeleteSDI", "1", actionprops);
                            if (!matchedbykeys) {
                                outputLogger.log("Deleted SDI:" + sdcid + ";" + keyid1 + ";" + keyid2 + ";" + keyid3 + " matched by identify column " + identifycolumn);
                            }
                            outputLogger.log("Done Delete");
                        } else {
                            outputLogger.log("Updating " + keyid1 + ";" + keyid2 + ";" + keyid3 + " matched keys " + matchedkeyid1 + ";" + matchedkeyid2 + ";" + matchedkeyid3);
                            this.getActionProcessor().processAction("EditSDI", "1", actionprops);
                            if (!matchedbykeys) {
                                importedkeyid1 = matchedkeyid1;
                                importedkeyid2 = matchedkeyid2;
                                importedkeyid3 = matchedkeyid3;
                                outputLogger.log("Updated SDI:" + sdcid + ";" + matchedkeyid1 + ";" + matchedkeyid2 + ";" + matchedkeyid3 + " matched by identify column " + identifycolumn);
                                if (oldNewKeyMap.get(sdcid) == null) {
                                    oldNewKeyMap.put(sdcid, new HashMap());
                                }
                                oldNewKeyMap.get(sdcid).put(keyid1 + ";" + keyid2 + ";" + keyid3, matchedkeyid1 + ";" + matchedkeyid2 + ";" + matchedkeyid3);
                            }
                            outputLogger.log("Done update");
                        }
                    }
                } else {
                    if (transferOption.isRegenerateKey()) {
                        outputLogger.log("Transfer option is Regenerate Key. Regenerate new uuid when Regenerate Key.");
                        primary.setValue(0, "uuid", "");
                    }
                    outputLogger.log("Add new SDI for " + sdcid + ";" + keyid1 + ";" + keyid2 + ";" + keyid3);
                    if (!isEmbedded && isChangeControlled) {
                        actionprops.setProperty("checkedoutbyuserid", this.connectionInfo.getSysuserId());
                        actionprops.setProperty("changelogreason", "From Import");
                        actionprops.setProperty("changerequestid", this.changerequestid);
                        actionprops.setProperty("cmtchangerequestid", this.changerequestid);
                    }
                    if (isAutoKeyGen) {
                        if (isRegenerateKey) {
                            outputLogger.log(sdcid + ":Transfer Option is Regenerate Key");
                            actionprops.setProperty("keyid1", "");
                        } else if (identifycolumn.length() > 0) {
                            outputLogger.log(sdcid + ":Transfer Option is Override existing. No match by identify column, regenerate key to avoid collision with exiting auto key");
                            actionprops.setProperty("keyid1", "");
                        } else {
                            outputLogger.log(sdcid + ":Transfer Option is Not Regenerate Key. Override auto key gen");
                            actionprops.setProperty("overrideautokey", "Y");
                        }
                    }
                    if ("Y".equals(templateFlag)) {
                        actionprops.setProperty("overrideautokey", "Y");
                    }
                    if ("LV_Worksheet".equals(sdcid) || "LV_WorksheetItem".equals(sdcid) || "LV_WorksheetSection".equals(sdcid)) {
                        actionprops.setProperty("importsnapshot", "Y");
                    }
                    actionprops.setProperty("forcesimplecopyattributes", "Y");
                    this.getActionProcessor().processAction("AddSDI", "1", actionprops);
                    importedkeyid1 = actionprops.getProperty("newkeyid1");
                    importedkeyid2 = actionprops.getProperty("newkeyid2");
                    importedkeyid3 = actionprops.getProperty("newkeyid3");
                    outputLogger.log("New SDI Created:" + sdcid + ";" + importedkeyid1 + ";" + importedkeyid2 + ";" + importedkeyid3);
                    if (oldNewKeyMap.get(sdcid) == null) {
                        oldNewKeyMap.put(sdcid, new HashMap());
                    }
                    oldNewKeyMap.get(sdcid).put(keyid1 + ";" + keyid2 + ";" + keyid3, importedkeyid1 + ";" + importedkeyid2 + ";" + importedkeyid3);
                    outputLogger.log("Done add:" + sdcid + ";" + importedkeyid1 + ";" + importedkeyid2 + ";" + importedkeyid3);
                }
            }
            if (sdiSnapshotItem.isDeleted()) {
                int newRow = importedSDIList.addRow();
                importedSDIList.setString(newRow, "sdcid", "Deleted");
                importedSDIList.setString(newRow, "keyid1", "");
                importedSDIList.setString(newRow, "keyid2", "");
                importedSDIList.setString(newRow, "keyid3", "");
                importedSDIList.setString(newRow, "templateflag", "");
            } else if (!doNotImport) {
                int newRow = importedSDIList.addRow();
                importedSDIList.setString(newRow, "sdcid", sdcid);
                importedSDIList.setString(newRow, "keyid1", importedkeyid1);
                importedSDIList.setString(newRow, "keyid2", importedkeyid2);
                importedSDIList.setString(newRow, "keyid3", importedkeyid3);
                importedSDIList.setString(newRow, "templateflag", templateFlag);
            }
            if (revFKList.size() > 0) {
                for (int eb = 0; eb < revFKList.size(); ++eb) {
                    SDIData embeddedSDIData;
                    embeddedItem = (SDISnapshotItem)revFKList.get(eb);
                    if (embeddedItem.isIncludedForTransfer() || !Snapshot.Type.SDI.equals((Object)embeddedItem.getType()) || (embeddedSDIData = revFKList.get(eb).getSDIData()) == null) continue;
                    outputLogger.log("Importing reverse FK embedded SDISnapshotItem: " + revFKList.get(eb).toString());
                    outputLogger.increaseIndent();
                    this.importSDISnapshot(sdiSnapshot, true, outputLogger, importInstructions, embeddedItem, importedSDIList, oldNewKeyMap, fkToValidateMap);
                    outputLogger.decreaseIndent();
                }
            }
            if (softLinkList.size() > 0) {
                for (int eb = 0; eb < softLinkList.size(); ++eb) {
                    SDIData embeddedSDIData;
                    embeddedItem = (SDISnapshotItem)softLinkList.get(eb);
                    if (embeddedItem.isIncludedForTransfer() || !Snapshot.Type.SDI.equals((Object)embeddedItem.getType()) || (embeddedSDIData = softLinkList.get(eb).getSDIData()) == null) continue;
                    outputLogger.log("Importing softlink embedded SDISnapshotItem(after primary): " + softLinkList.get(eb).toString());
                    if (oldNewKeyMap.get(sdcid) != null && oldNewKeyMap.get(sdcid).get(keyid1 + ";" + keyid2 + ";" + keyid3) != null && !(keyid1 + ";" + keyid2 + ";" + keyid3).equals(oldNewKeyMap.get(sdcid).get(keyid1 + ";" + keyid2 + ";" + keyid3))) {
                        outputLogger.log("Primary key changed from " + keyid1 + ";" + keyid2 + ";" + keyid3 + " to " + oldNewKeyMap.get(sdcid).get(keyid1 + ";" + keyid2 + ";" + keyid3) + ". Updating softlinks to new keys...");
                        PropertyList revsoftlinkprops = embeddedItem.getParentLinkProps().getPropertyList("revsoftlinkprops");
                        String linkkeyid1col = revsoftlinkprops.getProperty("linkkeyid1col");
                        String linkkeyid2col = revsoftlinkprops.getProperty("linkkeyid2col");
                        String linkkeyid3col = revsoftlinkprops.getProperty("linkkeyid3col");
                        DataSet embeddedprimary = embeddedSDIData.getDataset("primary");
                        String[] newkeys = StringUtil.split(oldNewKeyMap.get(sdcid).get(keyid1 + ";" + keyid2 + ";" + keyid3), ";");
                        embeddedprimary.setValue(0, linkkeyid1col, newkeys[0]);
                        embeddedprimary.setValue(0, linkkeyid2col, newkeys[1]);
                        embeddedprimary.setValue(0, linkkeyid3col, newkeys[2]);
                        outputLogger.log("Updated softlink in " + linkkeyid1col + "," + linkkeyid2col + "," + linkkeyid3col + " to " + newkeys[0] + "," + newkeys[1] + "," + newkeys[2]);
                    }
                    outputLogger.increaseIndent();
                    this.importSDISnapshot(sdiSnapshot, true, outputLogger, importInstructions, embeddedItem, importedSDIList, oldNewKeyMap, fkToValidateMap);
                    outputLogger.decreaseIndent();
                }
            }
            if (sqlLinkList.size() > 0) {
                for (int eb = 0; eb < sqlLinkList.size(); ++eb) {
                    SDIData embeddedSDIData;
                    embeddedItem = (SDISnapshotItem)sqlLinkList.get(eb);
                    if (embeddedItem.isIncludedForTransfer() || !Snapshot.Type.SDI.equals((Object)embeddedItem.getType()) || (embeddedSDIData = sqlLinkList.get(eb).getSDIData()) == null) continue;
                    outputLogger.log("Importing sqllink embedded SDISnapshotItem(after primary): " + sqlLinkList.get(eb).toString());
                    outputLogger.increaseIndent();
                    this.importSDISnapshot(sdiSnapshot, true, outputLogger, importInstructions, embeddedItem, importedSDIList, oldNewKeyMap, fkToValidateMap);
                    outputLogger.decreaseIndent();
                }
            }
            if (sdcid.equals("WebPage")) {
                CacheUtil.resetCache(this.getConnectionProcessor().getSapphireConnection().getDatabaseId(), true);
            }
        }
        catch (Exception e) {
            throw new SapphireException(e);
        }
    }

    private void repopulateFKColumnValues(String linksdcid, String sdccolumnid, String sdccolumnid2, String sdccolumnid3, HashMap<String, String> keyMapForLinkedSDC, HashMap<String, HashMap<String, StringBuilder>> fkToValidateMap, String linkfromtableid, DataSet datasetToUpdate, StringLogger outputLogger) {
        for (int i = 0; i < datasetToUpdate.getRowCount(); ++i) {
            if (datasetToUpdate.getValue(i, sdccolumnid).length() <= 0) continue;
            String oldkeys = datasetToUpdate.getValue(i, sdccolumnid) + ";" + datasetToUpdate.getValue(i, sdccolumnid2) + ";" + datasetToUpdate.getValue(i, sdccolumnid3);
            String newkeys = keyMapForLinkedSDC.get(oldkeys);
            if (newkeys != null && newkeys.length() > 0 && !newkeys.equals(oldkeys)) {
                String[] updatekeys = StringUtil.split(newkeys, ";");
                outputLogger.log("Updated FK fields " + sdccolumnid + ";" + sdccolumnid2 + ";" + sdccolumnid3 + " in " + linkfromtableid + " from " + oldkeys + " to " + newkeys);
                datasetToUpdate.setValue(i, sdccolumnid, updatekeys[0]);
                datasetToUpdate.setValue(i, sdccolumnid2, updatekeys[1]);
                datasetToUpdate.setValue(i, sdccolumnid3, updatekeys[2]);
                continue;
            }
            if (fkToValidateMap.get(linksdcid) == null) {
                HashMap<String, StringBuilder> valueMap = new HashMap<String, StringBuilder>();
                valueMap.put("keyid1", new StringBuilder());
                valueMap.put("keyid2", new StringBuilder());
                valueMap.put("keyid3", new StringBuilder());
                fkToValidateMap.put(linksdcid, valueMap);
            }
            fkToValidateMap.get(linksdcid).get("keyid1").append(";" + datasetToUpdate.getValue(i, sdccolumnid));
            fkToValidateMap.get(linksdcid).get("keyid2").append(";" + datasetToUpdate.getValue(i, sdccolumnid2));
            fkToValidateMap.get(linksdcid).get("keyid3").append(";" + datasetToUpdate.getValue(i, sdccolumnid3));
        }
    }

    private void flushUPDTables(StringLogger outputLogger) {
        outputLogger.log("Flushing all UPD Tables.");
        this.getQueryProcessor().execSQL("DELETE FROM updrefcolumn");
        this.getQueryProcessor().execSQL("DELETE FROM updref");
        this.getQueryProcessor().execSQL("DELETE FROM updcolumnproperty");
        this.getQueryProcessor().execSQL("DELETE FROM updcolumn");
        this.getQueryProcessor().execSQL("DELETE FROM updtable");
    }

    private void triggerSyncUpdProc(StringLogger outputLogger) throws SapphireException {
        outputLogger.log("Synchonizing data model...");
        this.database.executePreparedUpdate("{call lv_upd" + (this.database.isOracle() ? "." : "_") + "syncupd( ?, ? ) }", new Object[]{Build.getBuild(), "forceupd"});
        DataSet logText = this.getQueryProcessor().getSqlDataSet("SELECT logtext FROM UPDLOG WHERE logtext = '" + Build.getBuild() + "'");
        if (logText != null && logText.getRowCount() > 0) {
            if (this.database.isOracle()) {
                this.database.executePreparedUpdate("{call lv_util.recomp( ? ) }", new Object[]{"I"});
            }
        } else {
            throw new SapphireException("Upgrade of data model failed - check updlog table for errors.");
        }
    }

    private void triggerSDCAuditTablesProc(String sdcList) throws SapphireException {
        try {
            DataSet updatedSDCs = this.getQueryProcessor().getSqlDataSet("SELECT sdc.sdcid, sdc.tableid, sdc.auditedflag, sdc.auditpromptflag, sdc.accesscontrolledflag FROM sdc WHERE sdcid IN ('" + sdcList.replaceAll(";", "','") + "')");
            for (int i = 0; i < updatedSDCs.getRowCount(); ++i) {
                CallableStatement cs;
                String callstmt;
                String sdcid = updatedSDCs.getString(i, "sdcid");
                String auditedflag = updatedSDCs.getString(i, "auditedflag");
                if (auditedflag == null || auditedflag.length() == 0) {
                    auditedflag = "N";
                }
                if (!auditedflag.equals("N")) {
                    callstmt = "{call lv_audit" + (this.database.isOracle() ? "." : "_") + "sdcaudittables( ?, ? ) }";
                    cs = this.database.prepareCall(callstmt);
                    cs.setString(1, sdcid);
                    cs.setString(2, "Both");
                    cs.executeUpdate();
                    continue;
                }
                callstmt = "{call lv_audit" + (this.database.isOracle() ? "." : "_") + "sdcaudittables( ?, ? ) }";
                cs = this.database.prepareCall(callstmt);
                cs.setString(1, sdcid);
                cs.setString(2, "Off");
                cs.executeUpdate();
            }
        }
        catch (SQLException e) {
            throw new SapphireException("Failed to apply SDC options. Reason: " + e.getMessage(), e);
        }
    }

    private boolean importSDCSnapshot(SDISnapshot sdiSnapshot, boolean isBeforeDataModelUpdate, StringLogger outputLogger, ImportInstructions importInstructions, DataSet importedSDIList) throws SapphireException {
        boolean isImported = false;
        String importOption = importInstructions.getImportOption("SDC", sdiSnapshot.getKeyId1(), "", "");
        outputLogger.log("Import Option: " + importOption);
        if ((this.isFromRemoteCheckIn_Master || this.isFromRemoteCheckOut_Slave) && (importOption == null || importOption.length() == 0)) {
            importOption = this.database.getPreparedCount("SELECT COUNT(sdcid) FROM sdc WHERE sdcid = ?", new Object[]{sdiSnapshot.getKeyId1()}) > 0 ? "Override Existing" : "Import";
        }
        if ("Override Existing".equals(importOption) && this.alwaysIgnoreIfExists) {
            outputLogger.log("SDC Exists. Ignore.");
            return false;
        }
        if ("Do Not Import".equals(importOption)) {
            outputLogger.log("Skip Importing this SDC.");
            return false;
        }
        if ("Import".equals(importOption) || "Override Existing".equals(importOption)) {
            isImported = true;
            CMTPolicy targetPolicy = CMTPolicy.getPolicy(this.getConnectionid(), "SDC");
            boolean isChangeControlled = "Y".equals(targetPolicy.getChangeControlledFlag());
            if (isChangeControlled && "Override Existing".equals(importOption) && isBeforeDataModelUpdate) {
                outputLogger.log("SDC SDC is under Change Control.");
                PropertyList checkoutProps = new PropertyList();
                checkoutProps.setProperty("sdcid", "SDC");
                checkoutProps.setProperty("keyid1", sdiSnapshot.getKeyId1());
                checkoutProps.setProperty("keyid2", "");
                checkoutProps.setProperty("keyid3", "");
                checkoutProps.setProperty("local", "Y");
                checkoutProps.setProperty("changelogreason", "From Import");
                checkoutProps.setProperty("changerequestid", this.changerequestid);
                this.getActionProcessor().processAction("CheckOutSDI", "1", checkoutProps);
                outputLogger.log("Checked out the SDC SDI");
            }
            DataSet updTableColumns = new DataSet();
            if (isBeforeDataModelUpdate) {
                updTableColumns = this.getQueryProcessor().getSqlDataSet("SELECT * FROM syscolumn WHERE tableid IN ('updtable','updcolumn','updcolumnproperty','updref','updrefcolumn')");
            }
            SDIData sdcData = sdiSnapshot.getSDIData();
            Set datasetNames = sdcData.getDatasets();
            block16: for (String datasetName : datasetNames) {
                DataSet dataset = sdcData.getDataset(datasetName);
                if (dataset != null) {
                    dataset.removeColumn("auditsequence");
                    dataset.removeColumn("tracelogid");
                    dataset.removeColumn("createdt");
                    dataset.removeColumn("createby");
                    dataset.removeColumn("createtool");
                    dataset.removeColumn("moddt");
                    dataset.removeColumn("modby");
                    dataset.removeColumn("modtool");
                }
                if (dataset == null || dataset.getRowCount() <= 0) continue;
                switch (datasetName) {
                    case "systable": {
                        if (!isBeforeDataModelUpdate) continue block16;
                        outputLogger.log("Inserting into updtable.");
                        this.removeNonUPDxxxCols(dataset, updTableColumns, "updtable");
                        this.removeDuplicateRows(dataset, "updtable", "tableid");
                        if (dataset.isEmpty()) continue block16;
                        DataSetUtil.insert(this.database, dataset, "updtable");
                        break;
                    }
                    case "syscolumn": {
                        if (!isBeforeDataModelUpdate) continue block16;
                        outputLogger.log("Inserting into updcolumn.");
                        this.removeNonUPDxxxCols(dataset, updTableColumns, "updcolumn");
                        this.removeDuplicateRows(dataset, "updcolumn", "tableid;columnid");
                        if (dataset.isEmpty()) continue block16;
                        DataSetUtil.insert(this.database, dataset, "updcolumn");
                        break;
                    }
                    case "syscolumnproperty": {
                        if (!isBeforeDataModelUpdate) continue block16;
                        outputLogger.log("Inserting into updcolumnproperty.");
                        this.removeNonUPDxxxCols(dataset, updTableColumns, "updcolumnproperty");
                        this.removeDuplicateRows(dataset, "updcolumnproperty", "tableid;columnid;propertyid");
                        if (dataset.isEmpty()) continue block16;
                        DataSetUtil.insert(this.database, dataset, "updcolumnproperty");
                        break;
                    }
                    case "sysref": {
                        if (!isBeforeDataModelUpdate) continue block16;
                        outputLogger.log("Inserting into updref.");
                        this.removeNonUPDxxxCols(dataset, updTableColumns, "updref");
                        this.removeDuplicateRows(dataset, "updref", "refid");
                        if (dataset.isEmpty()) continue block16;
                        DataSetUtil.insert(this.database, dataset, "updref");
                        break;
                    }
                    case "sysrefcolumn": {
                        if (!isBeforeDataModelUpdate) continue block16;
                        outputLogger.log("Inserting into updrefcolumn.");
                        this.removeNonUPDxxxCols(dataset, updTableColumns, "updrefcolumn");
                        this.removeDuplicateRows(dataset, "updrefcolumn", "refid;columnid");
                        if (dataset.isEmpty()) continue block16;
                        DataSetUtil.insert(this.database, dataset, "updrefcolumn");
                        break;
                    }
                    case "primary": {
                        if (isBeforeDataModelUpdate) continue block16;
                        outputLogger.log("Processing dataset: primary");
                        boolean ignoreMissingObjects = importInstructions.isIgnoreMissingObjects();
                        if (ignoreMissingObjects) {
                            this.ignoreMissingColumns(dataset, "sdc");
                        }
                        if ("Import".equals(importOption)) {
                            DataSetUtil.insert(this.database, dataset, "sdc");
                            break;
                        }
                        if (!"Override Existing".equals(importOption)) continue block16;
                        DataSetUtil.update(this.database, dataset, "sdc", new String[]{"sdcid"});
                        break;
                    }
                    default: {
                        String[] keyCols;
                        if (isBeforeDataModelUpdate) continue block16;
                        DataSet dbData = null;
                        outputLogger.log("Processing dataset: " + datasetName);
                        boolean isSDIDataDataSet = Arrays.stream(SDIData.getDatasetNames()).anyMatch(datasetName::equalsIgnoreCase);
                        if (isSDIDataDataSet) {
                            keyCols = sdcData.getDataSetKeys(datasetName);
                            dbData = this.getQueryProcessor().getPreparedSqlDataSet("SELECT * FROM " + SDIData.getDatasetTablename(datasetName) + " WHERE sdcid = 'SDC' AND keyid1 = ?", new Object[]{sdiSnapshot.getKeyId1()});
                        } else {
                            DataSet tablePKCols = this.getQueryProcessor().getPreparedSqlDataSet("SELECT columnid FROM syscolumn WHERE pkflag ='Y' AND tableid = ? ORDER BY columnsequence", new Object[]{datasetName});
                            keyCols = StringUtil.split(tablePKCols.getColumnValues("columnid", ";"), ";");
                            if ("sysextendedcolumn".equalsIgnoreCase(datasetName)) {
                                DataSet sysTables = sdcData.getDataset("systable");
                                dbData = this.getQueryProcessor().getSqlDataSet("SELECT * FROM sysextendedcolumn WHERE tableid IN ('" + sysTables.getColumnValues("tableid", "','") + "')");
                            } else {
                                dbData = this.getQueryProcessor().getPreparedSqlDataSet("SELECT * FROM " + datasetName + " WHERE sdcid = ?", new Object[]{sdiSnapshot.getKeyId1()});
                            }
                        }
                        this.populateAddUpdateFlag(dataset, dbData, keyCols);
                        HashMap<String, String> filterMap = new HashMap<String, String>();
                        filterMap.put("__existsflag", "N");
                        DataSet insertDS = dataset.getFilteredDataSet(filterMap);
                        filterMap.put("__existsflag", "Y");
                        DataSet updateDS = dataset.getFilteredDataSet(filterMap);
                        boolean ignoreMissingObjects = importInstructions.isIgnoreMissingObjects();
                        if (insertDS.getRowCount() > 0) {
                            if (ignoreMissingObjects) {
                                this.ignoreMissingColumns(insertDS, SDIData.getDatasetTablename(datasetName).toLowerCase());
                            }
                            insertDS.removeColumn("__existsflag");
                            DataSetUtil.insert(this.database, insertDS, SDIData.getDatasetTablename(datasetName));
                        }
                        if (updateDS.getRowCount() <= 0) continue block16;
                        if (ignoreMissingObjects) {
                            this.ignoreMissingColumns(updateDS, SDIData.getDatasetTablename(datasetName).toLowerCase());
                        }
                        updateDS.removeColumn("__existsflag");
                        DataSetUtil.update(this.database, updateDS, SDIData.getDatasetTablename(datasetName), keyCols);
                    }
                }
            }
            if (isChangeControlled && "Import".equals(importOption) && !isBeforeDataModelUpdate) {
                outputLogger.log("SDC SDC is under Change Control.");
                PropertyList checkoutProps = new PropertyList();
                checkoutProps.setProperty("sdcid", "SDC");
                checkoutProps.setProperty("keyid1", sdiSnapshot.getKeyId1());
                checkoutProps.setProperty("keyid2", "");
                checkoutProps.setProperty("keyid3", "");
                checkoutProps.setProperty("local", "Y");
                checkoutProps.setProperty("changelogreason", "From Import");
                checkoutProps.setProperty("changerequestid", this.changerequestid);
                checkoutProps.setProperty("mode", "add");
                this.getActionProcessor().processAction("CheckOutSDI", "1", checkoutProps);
                outputLogger.log("Checked out the SDC SDI");
            }
        } else {
            throw new SapphireException("InvalidImportInstruction", "VALIDATION", "Invalid Import Directive for SDC SDC: " + importOption);
        }
        if (!isBeforeDataModelUpdate) {
            int newRow = importedSDIList.addRow();
            importedSDIList.setString(newRow, "sdcid", "SDC");
            importedSDIList.setString(newRow, "keyid1", sdiSnapshot.getSnapshotItem().getKeyId1());
            importedSDIList.setString(newRow, "keyid2", "");
            importedSDIList.setString(newRow, "keyid3", "");
        }
        return isImported;
    }

    private void removeDuplicateRows(DataSet dataset, String updTableId, String pkCols) {
        DataSet insertedRows = this.updTableRows.getOrDefault(updTableId, new DataSet());
        HashMap<String, String> findMap = new HashMap<String, String>();
        for (int i = dataset.getRowCount() - 1; i >= 0; --i) {
            String[] pkColsArr;
            findMap.clear();
            for (String pkColId : pkColsArr = StringUtil.split(pkCols, ";")) {
                findMap.put(pkColId, dataset.getString(i, pkColId, ""));
            }
            if (insertedRows.findRow(findMap) > -1) {
                dataset.deleteRow(i);
                continue;
            }
            insertedRows.copyRow(dataset, i, 1);
        }
        this.updTableRows.put(updTableId, insertedRows);
    }

    private void removeNonUPDxxxCols(DataSet dataset, DataSet updTableColumns, String tableName) {
        HashMap<String, String> filterMap = new HashMap<String, String>();
        for (String col : dataset.getColumns()) {
            filterMap.clear();
            filterMap.put("tableid", tableName);
            filterMap.put("columnid", col.toLowerCase());
            if (updTableColumns == null || updTableColumns.findRow(filterMap) != -1) continue;
            dataset.removeColumn(col);
        }
    }

    private void ignoreMissingColumns(DataSet dataset, String tableId) {
        DataSet dbCols = this.getQueryProcessor().getPreparedSqlDataSet("SELECT tableid, columnid FROM syscolumn WHERE tableid = ?", new Object[]{tableId});
        if (dbCols.getRowCount() > 0) {
            String[] dsCols;
            for (String dsColId : dsCols = dataset.getColumns()) {
                if (dbCols.findRow("columnid", dsColId.toLowerCase()) != -1) continue;
                dataset.removeColumn(dsColId);
            }
        }
    }

    private void populateAddUpdateFlag(DataSet snapshotDS, DataSet dbDS, String[] keyCols) {
        if (snapshotDS == null || dbDS == null) {
            return;
        }
        HashMap<String, String> findMap = new HashMap<String, String>();
        for (int i = 0; i < snapshotDS.getRowCount(); ++i) {
            findMap.clear();
            for (String keyCol : keyCols) {
                findMap.put(keyCol, snapshotDS.getString(i, keyCol));
            }
            int findRow = dbDS.findRow(findMap);
            if (findRow > -1) {
                snapshotDS.setString(i, "__existsflag", "Y");
                continue;
            }
            snapshotDS.setString(i, "__existsflag", "N");
        }
    }

    private ArrayList<SnapshotItem> determinePTreeImportSequence(ArrayList<SnapshotItem> importPTreeItemList, ImportInstructions importInstructions, SnapshotPackage snapshotPackage) throws SapphireException {
        try {
            ArrayList allDBNodes;
            PropertyTree dbPropertyTree;
            HashMap pTreeNodesCache = new HashMap();
            ArrayList<PropertyTreeSnapshotItem> fullOrDefItems_Importable = new ArrayList<PropertyTreeSnapshotItem>();
            for (int i = 0; i < importPTreeItemList.size(); ++i) {
                PropertyTreeSnapshotItem item = (PropertyTreeSnapshotItem)importPTreeItemList.get(i);
                String importOption = importInstructions.getImportOption("PropertyTree", item.getPropertyTreeId(), item.getNodeId(), "");
                if ((this.isFromRemoteCheckIn_Master || this.isFromRemoteCheckOut_Slave) && (importOption == null || importOption.length() == 0)) {
                    importOption = "Import";
                }
                if (!"__FULL".equals(item.getNodeId()) && !"__DEFINITION".equals(item.getNodeId())) continue;
                fullOrDefItems_Importable.add(item);
                if ("Do Not Import".equals(importOption)) continue;
                PropertyTreeSnapshot snapshot = null;
                if (this.isFromRemoteCheckOut_Slave || item.isDeleted()) {
                    snapshot = (PropertyTreeSnapshot)snapshotPackage.getPreSnapshot(item);
                    if (snapshot == null) {
                        snapshot = (PropertyTreeSnapshot)snapshotPackage.getSnapshot(item);
                    }
                    if (snapshot != null) {
                        item = snapshot.getSnapshotItem();
                    }
                } else {
                    snapshot = (PropertyTreeSnapshot)snapshotPackage.getSnapshot(item);
                    item = snapshot.getSnapshotItem();
                }
                HashSet cacheNodesSet = pTreeNodesCache.getOrDefault(item.getPropertyTreeId(), new HashSet());
                pTreeNodesCache.put(item.getPropertyTreeId(), cacheNodesSet);
                dbPropertyTree = null;
                try {
                    dbPropertyTree = PropertyTreeUtil.getPropertyTree(this.database, item.getPropertyTreeId(), false);
                    if (dbPropertyTree != null) {
                        allDBNodes = dbPropertyTree.getAllNodes();
                        allDBNodes.forEach(node -> cacheNodesSet.add(node.getId()));
                    }
                }
                catch (SapphireException allDBNodes2) {
                    // empty catch block
                }
                if (!"__FULL".equals(item.getNodeId()) || item.isDeleted()) continue;
                PropertyTree pkgPropertyTree = item.getPropertyTree();
                ArrayList allPkgNodes = pkgPropertyTree.getAllNodes();
                allPkgNodes.forEach(node -> cacheNodesSet.add(node.getId()));
            }
            ArrayList<String> nodeTreeNodeFound = new ArrayList<String>();
            ArrayList<PropertyTreeSnapshotItem> nodeItems_Importable = new ArrayList<PropertyTreeSnapshotItem>();
            for (int i = 0; i < importPTreeItemList.size(); ++i) {
                PropertyTreeSnapshotItem item = (PropertyTreeSnapshotItem)importPTreeItemList.get(i);
                String importOption = importInstructions.getImportOption("PropertyTree", item.getPropertyTreeId(), item.getNodeId(), "");
                if ((this.isFromRemoteCheckIn_Master || this.isFromRemoteCheckOut_Slave) && (importOption == null || importOption.length() == 0)) {
                    importOption = "Import";
                }
                if ("__DEFINITION".equals(item.getNodeId()) || "__FULL".equals(item.getNodeId())) continue;
                nodeItems_Importable.add(item);
                if ("Do Not Import".equals(importOption) || pTreeNodesCache.containsKey(item.getPropertyTreeId())) continue;
                dbPropertyTree = null;
                try {
                    dbPropertyTree = PropertyTreeUtil.getPropertyTree(this.database, item.getPropertyTreeId(), false);
                    if (dbPropertyTree == null) {
                        throw new SapphireException("Tree Not found.");
                    }
                    allDBNodes = dbPropertyTree.getAllNodes();
                    HashSet cacheNodesSet = pTreeNodesCache.getOrDefault(item.getPropertyTreeId(), new HashSet());
                    pTreeNodesCache.put(item.getPropertyTreeId(), cacheNodesSet);
                    allDBNodes.forEach(node -> cacheNodesSet.add(node.getId()));
                    continue;
                }
                catch (SapphireException e) {
                    if (!importInstructions.isValidateFK()) continue;
                    nodeTreeNodeFound.add(item.getPropertyTreeId() + "|" + item.getNodeId());
                }
            }
            if (nodeTreeNodeFound.size() > 0) {
                StringBuffer allNodes = new StringBuffer();
                nodeTreeNodeFound.forEach(node -> allNodes.append(";").append((String)node));
                allNodes.deleteCharAt(0);
                throw new SapphireException("NoPropertyTreeFound", "VALIDATION", "PropertyTree definition not found in DB or in Package. Can't import the following nodes: " + allNodes);
            }
            ArrayList<PropertyTreeSnapshotItem> deletedNodes = new ArrayList<PropertyTreeSnapshotItem>();
            ArrayList<PropertyTreeSnapshotItem> renamedNodes = new ArrayList<PropertyTreeSnapshotItem>();
            for (int i = nodeItems_Importable.size() - 1; i >= 0; --i) {
                PropertyTreeSnapshotItem item = (PropertyTreeSnapshotItem)nodeItems_Importable.get(i);
                if (item.isDeleted()) {
                    deletedNodes.add(item);
                    nodeItems_Importable.remove(i);
                }
                if (!item.isRenamed()) continue;
                renamedNodes.add(item);
                nodeItems_Importable.remove(i);
            }
            ArrayList<PropertyTreeSnapshotItem> nodeList = new ArrayList<PropertyTreeSnapshotItem>();
            HashSet<String> allNewNodeIds = new HashSet<String>();
            int loopCount = 0;
            while (nodeItems_Importable.size() > 0) {
                ++loopCount;
                int prevSize = nodeItems_Importable.size();
                int currentSize = nodeItems_Importable.size();
                for (int i = currentSize - 1; i >= 0; --i) {
                    PropertyTreeSnapshotItem item = (PropertyTreeSnapshotItem)nodeItems_Importable.get(i);
                    String importOption = importInstructions.getImportOption("PropertyTree", item.getPropertyTreeId(), item.getNodeId(), "");
                    if ((this.isFromRemoteCheckIn_Master || this.isFromRemoteCheckOut_Slave) && (importOption == null || importOption.length() == 0)) {
                        importOption = "Import";
                    }
                    if ("Do Not Import".equals(importOption)) {
                        nodeList.add(item);
                        nodeItems_Importable.remove(i);
                        continue;
                    }
                    if ("__root".equals(item.getNodeId())) {
                        nodeList.add(item);
                        nodeItems_Importable.remove(i);
                        allNewNodeIds.add(item.getNodeId());
                        continue;
                    }
                    PropertyTreeSnapshot snapshot = null;
                    if (this.isFromRemoteCheckOut_Slave) {
                        snapshot = (PropertyTreeSnapshot)snapshotPackage.getPreSnapshot(item);
                        if (snapshot == null) {
                            snapshot = (PropertyTreeSnapshot)snapshotPackage.getSnapshot(item);
                        }
                    } else {
                        snapshot = (PropertyTreeSnapshot)snapshotPackage.getSnapshot(item);
                    }
                    if (snapshot == null && !"Do Not Import".equals(importOption)) {
                        throw new SapphireException("NULLSnapshot", "FAILURE", "NULL Snapshot encountered for: " + item.toString(true));
                    }
                    String extendNodeId = snapshot.getSnapshotItem().getExtendsNodeId();
                    if (loopCount == 1) {
                        HashSet allNodeIds = pTreeNodesCache.getOrDefault(item.getPropertyTreeId(), new HashSet());
                        if (!allNodeIds.contains(extendNodeId) && !"root".equals(extendNodeId)) continue;
                        nodeList.add(item);
                        nodeItems_Importable.remove(i);
                        allNewNodeIds.add(item.getNodeId());
                        continue;
                    }
                    if (!allNewNodeIds.contains(extendNodeId)) continue;
                    nodeList.add(item);
                    nodeItems_Importable.remove(i);
                    allNewNodeIds.add(item.getNodeId());
                }
                currentSize = nodeItems_Importable.size();
                if (prevSize != currentSize) continue;
                if (importInstructions.isValidateFK()) {
                    StringBuffer noExtNodeNodes = new StringBuffer();
                    nodeItems_Importable.forEach(node -> noExtNodeNodes.append(node.getPropertyTreeId() + "|" + node.getNodeId()).append(";"));
                    throw new SapphireException("ExtendNodeNotFound", "VALIDATION", "Extend Node information not found in DB or in Package for following PropertyTree Nodes: " + noExtNodeNodes);
                }
                nodeList.addAll(nodeItems_Importable);
                break;
            }
            ArrayList<SnapshotItem> pTreeImportItems = new ArrayList<SnapshotItem>();
            pTreeImportItems.addAll(fullOrDefItems_Importable);
            pTreeImportItems.addAll(renamedNodes);
            pTreeImportItems.addAll(nodeList);
            pTreeImportItems.addAll(deletedNodes);
            return pTreeImportItems;
        }
        catch (SapphireException e) {
            throw e;
        }
        catch (Exception e) {
            throw new SapphireException("Exception occurred when trying to determine Propertytree Import sequence:" + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
        }
    }

    private void importPropertyTreeSnapshot(SnapshotItem snapshotItem, SnapshotPackage snapshotPackage, StringLogger outputLogger, ImportInstructions importInstructions, DataSet importedSDIList) throws SapphireException {
        try {
            outputLogger.log("Processing PropertyTree Snapshot for save: " + snapshotItem);
            PropertyTreeSnapshotItem pSnapshotItem = (PropertyTreeSnapshotItem)snapshotItem;
            String importOption = importInstructions.getImportOption("PropertyTree", pSnapshotItem.getPropertyTreeId(), pSnapshotItem.getNodeId(), "");
            outputLogger.log("Import Option selected: " + importOption);
            if (this.isFromRemoteCheckOut_Slave && (importOption == null || importOption.length() == 0)) {
                PropertyTree pTree;
                Node node;
                int count = this.database.getPreparedCount("SELECT count(propertytreeid) FROM propertytree WHERE propertytreeid = ?", new Object[]{pSnapshotItem.getPropertyTreeId()});
                if ("__FULL".equals(pSnapshotItem.getNodeId()) || "__DEFINITION".equals(pSnapshotItem.getNodeId()) || "__root".equals(pSnapshotItem.getNodeId())) {
                    if (count > 0 && this.alwaysIgnoreIfExists) {
                        outputLogger.log("PropertyTree exists. Ignore.");
                        return;
                    }
                } else if (count > 0 && (node = (pTree = PropertyTreeUtil.getPropertyTree(this.database, pSnapshotItem.getPropertyTreeId(), false)).getNode(pSnapshotItem.getNodeId())) != null && this.alwaysIgnoreIfExists) {
                    outputLogger.log("Node exists. Ignore.");
                    return;
                }
            }
            if ("Do Not Import".equals(importOption)) {
                outputLogger.log("Skip Save routine.");
            } else {
                CMTPolicy targetPolicy;
                String changecontrolFlag;
                PropertyTreeSnapshot snapshot = null;
                if (this.isFromRemoteCheckOut_Slave) {
                    PropertyTreeSnapshot preSnapshot = (PropertyTreeSnapshot)snapshotPackage.getPreSnapshot(snapshotItem);
                    snapshot = preSnapshot != null ? preSnapshot : (PropertyTreeSnapshot)snapshotPackage.getSnapshot(snapshotItem);
                    if (snapshot == null) {
                        throw new SapphireException("NULLSnapshot", "FAILURE", this.getTranslationProcessor().translate("NULL Snapshot found. Cannot proceed Remote operation. ") + (PropertyTreeSnapshotItem)snapshotItem);
                    }
                } else if (snapshotPackage.getSnapshot(snapshotItem) != null) {
                    snapshot = (PropertyTreeSnapshot)snapshotPackage.getSnapshot(snapshotItem);
                } else if (!((PropertyTreeSnapshotItem)snapshotItem).isDeleted() && !((PropertyTreeSnapshotItem)snapshotItem).isRenamed()) {
                    throw new SapphireException("NULLSnapshot", "FAILURE", this.getTranslationProcessor().translate("NULL Snapshot found. Cannot proceed. ") + (PropertyTreeSnapshotItem)snapshotItem);
                }
                if (snapshot != null) {
                    pSnapshotItem = snapshot.getSnapshotItem();
                }
                boolean isChangeControlled = "Y".equals(changecontrolFlag = (targetPolicy = CMTPolicy.getPolicy(this.getConnectionid(), "PropertyTree", "")).getChangeControlledFlag()) || "T".equals(changecontrolFlag);
                outputLogger.log("PropertyTree SDC Change Control status: " + isChangeControlled);
                WebAdminProcessor wp = new WebAdminProcessor(this.getConnectionid());
                if (!pSnapshotItem.isDeleted()) {
                    int i;
                    String importedNodeId = pSnapshotItem.getNodeId();
                    if ("__DEFINITION".equals(pSnapshotItem.getNodeId()) || "__FULL".equals(pSnapshotItem.getNodeId())) {
                        outputLogger.log("Processing the PropertyTree SDI.");
                        SDIData sdiData = pSnapshotItem.getSDIData();
                        DataSet primary = sdiData.getDataset("primary");
                        if (primary.isValidColumn("valuetree")) {
                            DataSet newPrimary = new DataSet();
                            String[] columns = primary.getColumns();
                            for (i = 0; i < primary.getRowCount(); ++i) {
                                int newRow = newPrimary.addRow();
                                for (String col : columns) {
                                    if (col.equalsIgnoreCase("valuetree") || col.equalsIgnoreCase("definitiontree")) continue;
                                    newPrimary.addColumn(col, primary.getColumnType(col));
                                    newPrimary.setValue(newRow, col, primary.getValue(0, col));
                                }
                            }
                            sdiData.setDataset("primary", newPrimary);
                        }
                        this.importSDISnapshot(snapshot, false, outputLogger, importInstructions, pSnapshotItem, new DataSet(), new HashMap<String, HashMap<String, String>>(), null);
                        sdiData.setDataset("primary", primary);
                        outputLogger.log("Processed the PropertyTree SDI info. Now process the Definition Tree.");
                        PropertyTreeUtil.setPropertyTreeDefinition(this.database, "(import)", pSnapshotItem.getPropertyTreeId(), primary.getString(0, "definitiontree"));
                    }
                    if (!"__DEFINITION".equals(pSnapshotItem.getNodeId()) || "__FULL".equals(pSnapshotItem.getNodeId())) {
                        PropertyTree dbPropertyTree = new PropertyTree();
                        dbPropertyTree.setValueXML(PropertyTreeUtil.getPropertyTreeValue(this.database, pSnapshotItem.getPropertyTreeId(), true));
                        dbPropertyTree.setDefinitionXML(PropertyTreeUtil.getPropertyTreeDefinition(this.database, pSnapshotItem.getPropertyTreeId(), true));
                        if (pSnapshotItem.isRenamed()) {
                            outputLogger.log("Renaming Node to: " + pSnapshotItem.getRenamedNodeId());
                            dbPropertyTree.renameNode(pSnapshotItem.getNodeId(), pSnapshotItem.getRenamedNodeId());
                            outputLogger.log("Re-linking all known linked SDIs (WebPages/Gizmos etc...) refering to the old Node.");
                            wp.renameWebPagePropertyTreeNode(pSnapshotItem.getPropertyTreeId(), pSnapshotItem.getNodeId(), pSnapshotItem.getRenamedNodeId());
                            if (isChangeControlled) {
                                PropertyList extraProps = new PropertyList();
                                extraProps.setProperty("changelogreason", "From Import");
                                extraProps.setProperty("changerequestid", this.changerequestid);
                                CMTUtil.checkOutSDI(this.getConnectionid(), "PropertyTree", pSnapshotItem.getPropertyTreeId(), "", "", pSnapshotItem.getNodeId(), "", "", "edit", extraProps);
                                extraProps.clear();
                                extraProps.setProperty("changelogreason", "From Import");
                                extraProps.setProperty("changerequestid", this.changerequestid);
                                PropertyList checkOutProps = CMTUtil.checkOutSDI(this.getConnectionid(), "PropertyTree", pSnapshotItem.getPropertyTreeId(), "", "", pSnapshotItem.getRenamedNodeId(), "", "", "add", extraProps);
                                String newNodeChangeLogId = checkOutProps.getProperty("changelogid");
                                extraProps.clear();
                                extraProps.setProperty("primarychangelogid", newNodeChangeLogId);
                                extraProps.setProperty("changelogstatus", "Renamed");
                                extraProps.setProperty("auditreason", "Node renamed to: " + pSnapshotItem.getRenamedNodeId() + ", by " + this.getConnectionProcessor().getSapphireConnection().getSysuserId());
                                CMTUtil.updateNodeChangeLogs(pSnapshotItem.getPropertyTreeId(), pSnapshotItem.getNodeId(), this.getQueryProcessor(), this.getActionProcessor(), extraProps);
                            }
                            importedNodeId = pSnapshotItem.getRenamedNodeId();
                        } else {
                            outputLogger.log("Prepare source propertytree for merging into target.");
                            PropertyTree sourcePropertyTree = pSnapshotItem.getPropertyTree();
                            if ("__FULL".equals(pSnapshotItem.getNodeId())) {
                                NodeList straightNodeList = new NodeList();
                                ArrayList nodeList = sourcePropertyTree.getAllNodes();
                                for (i = 0; i < nodeList.size(); ++i) {
                                    Node n = (Node)nodeList.get(i);
                                    this.setNodeAttributes(snapshot, n);
                                    this.setNodeParent(n);
                                    n.setNodeList(new NodeList());
                                    straightNodeList.add(n);
                                }
                                sourcePropertyTree.setNodeList(straightNodeList);
                                this.setPLTAttributes(sourcePropertyTree);
                            } else {
                                if (isChangeControlled) {
                                    outputLogger.log("Checking out the node: " + pSnapshotItem.getNodeId());
                                    PropertyList actionProps = new PropertyList();
                                    actionProps.setProperty("sdcid", "PropertyTree");
                                    actionProps.setProperty("keyid1", pSnapshotItem.getPropertyTreeId());
                                    actionProps.setProperty("propertytreenodeid", pSnapshotItem.getNodeId());
                                    actionProps.setProperty("changelogreason", "From Import");
                                    actionProps.setProperty("changerequestid", this.changerequestid);
                                    if ("Import".equals(importOption)) {
                                        actionProps.setProperty("mode", "add");
                                    }
                                    ActionProcessor actionProcessor = new ActionProcessor(this.getConnectionid());
                                    actionProcessor.processAction("CheckOutSDI", "1", actionProps);
                                }
                                if (!"__root".equals(pSnapshotItem.getNodeId())) {
                                    Node n = sourcePropertyTree.getNode(pSnapshotItem.getNodeId());
                                    this.setNodeAttributes(snapshot, n);
                                    n.setExtendsNodeId(pSnapshotItem.getExtendsNodeId());
                                }
                            }
                            outputLogger.log("Start merging the valuetree.");
                            if ("__FULL".equals(pSnapshotItem.getNodeId()) || "__root".equals(pSnapshotItem.getNodeId())) {
                                outputLogger.log("Root node needs to be handled. Determining course of action...");
                                if (snapshot.isDevMode() || snapshot.getCompCode().length() > 0) {
                                    outputLogger.log("Package created using non-custom mode.");
                                    if ("Import".equals(importOption)) {
                                        outputLogger.log("PropertyTree doesn't exists in target system. Import the Root node.");
                                        dbPropertyTree.merge(sourcePropertyTree, true);
                                    } else {
                                        outputLogger.log("PropertyTree already exists in target system. Skip the Root node.");
                                        dbPropertyTree.merge(sourcePropertyTree, false);
                                    }
                                } else {
                                    outputLogger.log("Package created using custom mode. Import the Root node.");
                                    dbPropertyTree.merge(sourcePropertyTree, true);
                                }
                            } else {
                                if ("Move & Override Existing".equals(importOption)) {
                                    outputLogger.log("Parent node mis-match. Moving node to Parent: " + pSnapshotItem.getExtendsNodeId());
                                    dbPropertyTree.moveNode(pSnapshotItem.getNodeId(), pSnapshotItem.getExtendsNodeId());
                                }
                                dbPropertyTree.merge(sourcePropertyTree, false);
                            }
                        }
                        PropertyTreeUtil.setPropertyTreeValue(this.database, "(import)", pSnapshotItem.getPropertyTreeId(), dbPropertyTree.toXMLString());
                    }
                    int newRow = importedSDIList.addRow();
                    importedSDIList.setString(newRow, "sdcid", "PropertyTree");
                    importedSDIList.setString(newRow, "keyid1", pSnapshotItem.getPropertyTreeId());
                    importedSDIList.setString(newRow, "keyid2", importedNodeId);
                    importedSDIList.setString(newRow, "keyid3", "");
                } else {
                    outputLogger.log("Deleting item: " + pSnapshotItem);
                    if (isChangeControlled) {
                        outputLogger.log("Checking out the node: " + pSnapshotItem.getNodeId());
                        PropertyList actionProps = new PropertyList();
                        actionProps.setProperty("sdcid", "PropertyTree");
                        actionProps.setProperty("keyid1", pSnapshotItem.getPropertyTreeId());
                        actionProps.setProperty("propertytreenodeid", pSnapshotItem.getNodeId());
                        actionProps.setProperty("changelogreason", "From Import");
                        actionProps.setProperty("changerequestid", this.changerequestid);
                        ActionProcessor actionProcessor = new ActionProcessor(this.getConnectionid());
                        actionProcessor.processAction("CheckOutSDI", "1", actionProps);
                    }
                    if ("__FULL".equals(pSnapshotItem.getNodeId())) {
                        wp.deletePropertyTree(pSnapshotItem.getPropertyTreeId());
                    } else if (!("__root".equals(pSnapshotItem.getNodeId()) && "__FULL".equals(pSnapshotItem.getNodeId()) && "__DEFINITION".equals(pSnapshotItem.getNodeId()))) {
                        CMTUtil.deletePropertyTreeNode(pSnapshotItem.getPropertyTreeId(), pSnapshotItem.getNodeId(), true, this.database, wp);
                        if (isChangeControlled) {
                            PropertyList extraProps = new PropertyList();
                            extraProps.setProperty("changelogstatus", "Deleted");
                            CMTUtil.updateNodeChangeLogs(pSnapshotItem.getPropertyTreeId(), pSnapshotItem.getNodeId(), this.getQueryProcessor(), this.getActionProcessor(), extraProps);
                        }
                    }
                }
            }
            try {
                outputLogger.log("Reset Cache for PropertyTree: " + pSnapshotItem.getPropertyTreeId() + ";" + pSnapshotItem.getNodeId());
                WebAdminService webAdminService = new WebAdminService(new SapphireConnection(this.database.getConnection(), this.connectionInfo));
                webAdminService.resetCache("Tree", pSnapshotItem.getPropertyTreeId() + ";" + pSnapshotItem.getNodeId());
            }
            catch (Exception e) {
                outputLogger.log("Unable to reset cache. Server restart required after Import.");
                this.logger.info("Unable to reset cache due to: " + e.getMessage());
                this.logger.stackTrace(e);
            }
        }
        catch (SapphireException e) {
            throw e;
        }
        catch (Exception e) {
            throw new SapphireException(e);
        }
    }

    void setNodeAttributes(PropertyTreeSnapshot snapshot, Node n) {
        if (snapshot.getExists().length() == 0) {
            if (snapshot.isDevMode()) {
                n.setExists(n.isProduct() ? "replace" : "ignore");
            } else if (snapshot.getCompCode().length() > 0) {
                n.setExists(snapshot.getCompCode().equals(n.getCompCode()) ? "replace" : "ignore");
            } else {
                n.setExists(n.isLocked() ? "ignore" : "replace");
            }
        } else if (snapshot.isDevMode()) {
            n.setExists(n.isProduct() ? "replace" : "ignore");
        } else {
            n.setExists(snapshot.getExists());
        }
        if (snapshot.getNotExists().length() == 0) {
            n.setNotexists("add");
        } else {
            n.setNotexists(snapshot.getNotExists());
        }
    }

    private void setNodeParent(Node n) {
        Node parent = n.getParent();
        boolean done = false;
        while (!done && parent != null && !n.getId().endsWith(" Custom") && parent.getCompCode().length() > 0) {
            if ((parent = parent.getParent()) != null && !parent.isCustom()) continue;
            parent = n.getParent();
            done = true;
        }
        n.setExtendsNodeId(parent != null ? parent.getNodeId() : "root");
    }

    private void setPLTAttributes(PropertyTree propertyTree) throws SapphireException {
        ArrayList nodeList = propertyTree.getAllNodes();
        for (int i = 0; i < nodeList.size(); ++i) {
            Node n = (Node)nodeList.get(i);
            PropertyListTransfer prop = (PropertyListTransfer)n.getPropertyList();
            if (prop == null || prop.size() <= 0) continue;
            prop.setId("");
            this.setPLTAttributes(prop, "replace", "add");
        }
    }

    private void setPLTAttributes(PropertyListTransfer propertyListTransfer, String exists, String notExists) throws SapphireException {
        propertyListTransfer.setExists(exists);
        propertyListTransfer.setNotexists(notExists);
        propertyListTransfer.setAttribute("exists", exists);
        propertyListTransfer.setAttribute("notexists", notExists);
        propertyListTransfer.setAttribute("expanded", null, true);
        propertyListTransfer.setAttribute("selectedindex", null, true);
        ArrayList propertyItems = new ArrayList(propertyListTransfer.keySet());
        for (Object propertyItemIdObj : propertyItems) {
            Object propertyItemObj = propertyListTransfer.get(propertyItemIdObj);
            if (propertyItemObj instanceof PropertyList) {
                PropertyListTransfer propertyList1 = (PropertyListTransfer)propertyItemObj;
                if (propertyList1 == null || propertyList1.size() <= 0) continue;
                this.setPLTAttributes(propertyList1, exists, notExists);
                continue;
            }
            if (!(propertyItemObj instanceof PropertyListCollection)) continue;
            PropertyListCollection propertyListCollection = (PropertyListCollection)propertyItemObj;
            for (int i = 0; i < propertyListCollection.size(); ++i) {
                PropertyListTransfer propertyList1 = (PropertyListTransfer)propertyListCollection.getPropertyList(i);
                if (propertyList1 == null || propertyList1.size() <= 0) continue;
                this.setPLTAttributes(propertyList1, exists, notExists);
            }
        }
    }

    public static class ImportInstructions
    extends JSONObject {
        public ImportInstructions() {
        }

        public ImportInstructions(String string) throws JSONException {
            super(string);
        }

        public void setIgnoreMissingObjects(boolean ignore) {
            try {
                if (ignore) {
                    this.put("ignoremissingobjects", "Y");
                } else {
                    this.put("ignoremissingobjects", "N");
                }
            }
            catch (JSONException jSONException) {
                // empty catch block
            }
        }

        public boolean isIgnoreMissingObjects() {
            try {
                return "Y".equals(this.getString("ignoremissingobjects"));
            }
            catch (JSONException jSONException) {
                return false;
            }
        }

        public void setValidateFK(boolean validate) {
            try {
                if (validate) {
                    this.put("validateFK", "Y");
                } else {
                    this.put("validateFK", "N");
                }
            }
            catch (JSONException jSONException) {
                // empty catch block
            }
        }

        public boolean isValidateFK() {
            try {
                return "Y".equals(this.getString("validateFK"));
            }
            catch (JSONException jSONException) {
                return false;
            }
        }

        public void addInstruction(String sdcid, String keyid1, String keyid2, String keyid3, String importoption) {
            this.addInstruction(sdcid, keyid1, keyid2, keyid3, "", "", "", importoption);
        }

        public void addInstruction(String sdcid, String keyid1, String keyid2, String keyid3, String matchedkeyid1, String matchedkeyid2, String matchedkeyid3, String importoption) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("matchedkeyid1", matchedkeyid1);
                jsonObject.put("matchedkeyid2", matchedkeyid2);
                jsonObject.put("matchedkeyid3", matchedkeyid3);
                jsonObject.put("importoption", importoption);
                this.put(sdcid + ";" + keyid1 + ";" + keyid2 + ";" + keyid3, jsonObject);
            }
            catch (JSONException jSONException) {
                // empty catch block
            }
        }

        public void addInstructionsFromJSON(String jsonString) {
            try {
                JSONObject jsonObject = new JSONObject(jsonString);
                Iterator iterator = jsonObject.keys();
                while (iterator.hasNext()) {
                    String sdiKeys = (String)iterator.next();
                    String importOptionString = jsonObject.getString(sdiKeys);
                    String[] keys = StringUtil.split(sdiKeys, "|");
                    if (keys.length <= 1) continue;
                    this.addInstruction(keys[0], keys[1], keys.length > 2 ? keys[2] : "", keys.length > 3 ? keys[3] : "", importOptionString);
                }
            }
            catch (JSONException jSONException) {
                // empty catch block
            }
        }

        public String getImportOption(String sdcid, String keyid1, String keyid2, String keyid3) {
            try {
                JSONObject jsonObject = this.getJSONObject(sdcid + ";" + keyid1 + ";" + keyid2 + ";" + keyid3);
                return jsonObject.getString("importoption");
            }
            catch (JSONException jSONException) {
                return "";
            }
        }

        public boolean isCreateNewVersion(String sdcid, String keyid1, String keyid2, String keyid3) {
            String importoption = this.getImportOption(sdcid, keyid1, keyid2, keyid3);
            return "Create New Version".equals(importoption);
        }

        public boolean isIgnoreIfExists(String sdcid, String keyid1, String keyid2, String keyid3) {
            return "Ignore If Exists".equals(this.getImportOption(sdcid, keyid1, keyid2, keyid3));
        }

        public String getMatchedSDIKeyid1(String sdcid, String keyid1, String keyid2, String keyid3) {
            try {
                JSONObject jsonObject = this.getJSONObject(sdcid + ";" + keyid1 + ";" + keyid2 + ";" + keyid3);
                return jsonObject.getString("matchedkeyid1");
            }
            catch (JSONException jSONException) {
                return null;
            }
        }

        public String getMatchedSDIKeyid2(String sdcid, String keyid1, String keyid2, String keyid3) {
            try {
                JSONObject jsonObject = this.getJSONObject(sdcid + ";" + keyid1 + ";" + keyid2 + ";" + keyid3);
                return jsonObject.getString("matchedkeyid2");
            }
            catch (JSONException jSONException) {
                return null;
            }
        }

        public String getMatchedSDIKeyid3(String sdcid, String keyid1, String keyid2, String keyid3) {
            try {
                JSONObject jsonObject = this.getJSONObject(sdcid + ";" + keyid1 + ";" + keyid2 + ";" + keyid3);
                return jsonObject.getString("matchedkeyid3");
            }
            catch (JSONException jSONException) {
                return null;
            }
        }
    }
}

