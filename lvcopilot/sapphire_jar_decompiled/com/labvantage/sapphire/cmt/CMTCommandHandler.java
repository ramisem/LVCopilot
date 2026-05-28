/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.cmt;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.RequestParser;
import com.labvantage.sapphire.actions.cmt.ImportSnapshot;
import com.labvantage.sapphire.admin.webadmin.WebAdminProcessor;
import com.labvantage.sapphire.cmt.SnapshotFactory;
import com.labvantage.sapphire.cmt.SnapshotPackage;
import com.labvantage.sapphire.util.policy.CMTPolicy;
import com.labvantage.sapphire.xml.Node;
import com.labvantage.sapphire.xml.PropertyTree;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.servlet.BaseExternalHandler;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class CMTCommandHandler
extends BaseExternalHandler {
    public static final String COMMAND_CHECKOUT = "COMMAND_CHECK_OUT";
    public static final String COMMAND_CHECKIN = "COMMAND_CHECK_IN";
    public static final String COMMAND_EXPORT = "COMMAND_EXPORT";
    public static final String COMMAND_IMPORT = "COMMAND_IMPORT";
    public static final String COMMAND_JUNITTEST = "COMMAND_JUNITTEST";
    public static final String COMMAND_SDIREQUEST = "COMMAND_SDIREQUEST";
    public static final String COMMAND_INSPECTSDI = "COMMAND_INSPECTSDI";
    public static final String COMMAND_DATASETREQUEST = "COMMAND_DATASETREQUEST";
    public static final String COMMAND_CHECK_CONNECTION = "COMMAND_CHECK_CONNECTION";

    @Override
    public PropertyList processCommand(String command, PropertyList commandRequest) throws SapphireException {
        PropertyList commandResponse = new PropertyList();
        if (command.equals(COMMAND_DATASETREQUEST)) {
            String sqlcode = commandRequest.getProperty("sqlcode");
            String bindvars = commandRequest.getProperty("bindvars");
            Object[] vars = StringUtil.split(bindvars, ";");
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sqlcode, vars);
            commandResponse.setProperty("datasetxml", ds.toXML());
        } else if (command.equals(COMMAND_CHECK_CONNECTION)) {
            commandResponse.setProperty("databaseid", this.getDatabaseId());
            commandResponse.setProperty("externalappid", this.getExternalAppid());
            commandResponse.setProperty("externaluserid", this.getExternalUserid());
            commandResponse.setProperty("processasuserid", this.getProcessAsUserId());
            commandResponse.setProperty("dbms", this.getConnectionProcessor().isMSS() ? "MSS" : "ORA");
            CMTPolicy cmtPolicy = CMTPolicy.getPolicy(this.getConnectionid(), "");
            commandResponse.setProperty("changerequestmandatory", cmtPolicy.isChangeRequestMandatory() ? "Y" : "N");
        } else if (command.equals(COMMAND_CHECKOUT)) {
            String sdcid = commandRequest.getProperty("sdcid");
            String keyid1 = commandRequest.getProperty("keyid1");
            String keyid2 = commandRequest.getProperty("keyid2");
            String keyid3 = commandRequest.getProperty("keyid3");
            String propertyTreeNodeId = commandRequest.getProperty("propertytreenodeid");
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", sdcid);
            props.setProperty("keyid1", keyid1);
            props.setProperty("keyid2", keyid2);
            props.setProperty("keyid3", keyid3);
            if ("PropertyTree".equals(sdcid)) {
                props.setProperty("propertytreenodeid", propertyTreeNodeId);
            }
            this.getActionProcessor().processAction("CheckOutSDI", "1", props);
            String changelogidlist = props.getProperty("changelogid");
            SafeSQL safeSQL = new SafeSQL();
            DataSet changelogdataset = this.getQueryProcessor().getPreparedSqlDataSet("SELECT * FROM changelog WHERE changelogid in (" + safeSQL.addIn(changelogidlist, ";") + ")", safeSQL.getValues(), true);
            commandResponse.setProperty("changelogdataset", changelogdataset.toJSONString(true, true));
        } else if (command.equals(COMMAND_CHECKIN)) {
            String snapshotpackagexml = commandRequest.getProperty("snapshotpackagexml");
            SnapshotPackage snapshotPackage = SnapshotPackage.fromXML(snapshotpackagexml);
        } else if (COMMAND_INSPECTSDI.equals(command)) {
            this.inspectSDI(commandRequest, commandResponse);
        } else if (command.equals(COMMAND_JUNITTEST)) {
            commandResponse.setProperty(COMMAND_JUNITTEST, "COMMAND_JUNITTEST Command succeeded");
        }
        return commandResponse;
    }

    private void inspectSDI(PropertyList props, PropertyList commandResponse) {
        block19: {
            String sdcId = props.getProperty("sdcid");
            String keyId1 = props.getProperty("keyid1");
            String keyId2 = props.getProperty("keyid2");
            String keyId3 = props.getProperty("keyid3");
            String propertyTreeNodeId = props.getProperty("propertytreenodeid");
            if ("PropertyTree".equals(sdcId)) {
                WebAdminProcessor wp = new WebAdminProcessor(this.getConnectionid());
                try {
                    PropertyTree tree = wp.getPropertyTree(keyId1);
                    if ("__FULL".equals(propertyTreeNodeId) || "__DEFINITION".equals(propertyTreeNodeId) || "__root".equals(propertyTreeNodeId)) {
                        commandResponse.setProperty("found", "Y");
                        break block19;
                    }
                    try {
                        Node node = tree.getNode(propertyTreeNodeId);
                        if (node == null) {
                            commandResponse.setProperty("found", "N");
                        } else {
                            commandResponse.setProperty("found", "Y");
                        }
                    }
                    catch (Exception e) {
                        this.logger.error("Exception occurred when trying retrieve PropertyTree Node to inspectSDI: " + keyId1 + "," + propertyTreeNodeId, e);
                        commandResponse.setProperty("found", "N");
                    }
                }
                catch (Exception e) {
                    this.logger.error("Exception occurred when trying retrieve PropertyTree to inspectSDI: " + keyId1, e);
                    commandResponse.setProperty("found", "N");
                }
            } else if (sdcId.equals("LV_AttributeDef")) {
                if ("LV_WorksheetItem".equals(props.getProperty("attributesdcid"))) {
                    SafeSQL safeSQL = new SafeSQL();
                    String sql = "select attributeid from sdiattribute";
                    sql = sql + " where sdcid = " + safeSQL.addVar(props.getProperty("parentsdcid"));
                    sql = sql + " and keyid1 = " + safeSQL.addVar(keyId1);
                    if (keyId2 != null && !keyId2.trim().isEmpty()) {
                        sql = sql + " and keyid2 = " + safeSQL.addVar(keyId2);
                        if (keyId3 != null && !keyId3.trim().isEmpty()) {
                            sql = sql + " and keyid3 = " + safeSQL.addVar(keyId3);
                        }
                    }
                    sql = sql + " and attributeid = " + safeSQL.addVar(props.getProperty("attributeid"));
                    sql = sql + " and attributesdcid = " + safeSQL.addVar(props.getProperty("attributesdcid"));
                    sql = sql + " and attributeinstance = " + safeSQL.addVar(props.getProperty("attributeinstance"));
                    DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
                    commandResponse.setProperty("found", ds != null && ds.size() > 0 ? "Y" : "N");
                } else {
                    String sql = "select attributedef.attributedefid from attributedef where attributedef.attributedefid = ? and attributedef.basedonid = ?";
                    DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])new String[]{keyId1, keyId2});
                    commandResponse.setProperty("found", ds != null && ds.size() > 0 ? "Y" : "N");
                }
            } else {
                PropertyList sdcProps = this.getSDCProcessor().getPropertyList(sdcId);
                boolean isVersionedSDC = "Y".equals(sdcProps.getProperty("versionedflag"));
                boolean isCurrentRef = isVersionedSDC && (keyId2.length() == 0 || "C".equals(keyId2));
                SDIRequest sdiRequest = new SDIRequest();
                sdiRequest.setSDCid(sdcId);
                if (isCurrentRef) {
                    int keyCols = Integer.parseInt(sdcProps.getProperty("keycolumns"));
                    if (keyCols == 2) {
                        sdiRequest.setQueryWhere(sdcProps.getProperty("keycolid1") + " = '" + keyId1 + "' AND versionstatus IN ('P','C')");
                    } else {
                        sdiRequest.setQueryWhere(sdcProps.getProperty("keycolid1") + " = '" + keyId1 + "' AND " + sdcProps.getProperty("keycolid3") + " = '" + keyId3 + "' AND versionstatus IN ('P','C')");
                    }
                    sdiRequest.setQueryFrom(sdcProps.getProperty("tableid"));
                } else {
                    sdiRequest.setKeyid1List(keyId1);
                    sdiRequest.setKeyid2List(keyId2);
                    sdiRequest.setKeyid3List(keyId3);
                }
                sdiRequest.setRequestItem("primary");
                sdiRequest.setShowHiddenRecords(true);
                sdiRequest.setSecurityBypassCode(1);
                SDIData sdiData = this.getSDIProcessor().getSDIData(sdiRequest);
                DataSet primary = sdiData.getDataset("primary");
                commandResponse.setProperty("found", primary.getRowCount() > 0 ? "Y" : "N");
            }
        }
    }

    @Override
    public JSONObject processFileCommand(String command, Path file, JSONObject commandRequest) throws SapphireException {
        PropertyList commandResponse = new PropertyList();
        if (command.equals(COMMAND_JUNITTEST)) {
            commandResponse.setProperty(COMMAND_JUNITTEST, "COMMAND_JUNITTEST Command succeeded");
        }
        return commandResponse.toJSONObject(false, false);
    }

    @Override
    public JSONObject processFileCommand(String command, String filename, InputStream inputStream, JSONObject commandRequest) throws SapphireException {
        PropertyList commandResponse = new PropertyList();
        try {
            File tempFile = File.createTempFile(filename, "zip");
            tempFile.deleteOnExit();
            CMTCommandHandler.copyInputStreamToFile(inputStream, tempFile);
            if (command.equals(COMMAND_IMPORT)) {
                PropertyList props = new PropertyList();
                props.put("tempFile", tempFile);
                props.put("remotecheckin", "Y");
                props.put("notes", commandRequest.getString("notes"));
                props.put("changerequestid", commandRequest.getString("changerequestid"));
                this.getActionProcessor().processActionClass(ImportSnapshot.class.getName(), props);
                commandResponse.setProperty(COMMAND_IMPORT, "COMMAND_IMPORT Command succeeded");
                commandResponse.setProperty("importlog", props.getProperty("importlog"));
            }
        }
        catch (Exception e) {
            throw new SapphireException(e);
        }
        return commandResponse.toJSONObject(false, false);
    }

    @Override
    public File processFileDownloadCommand(String command, JSONObject commandRequest) throws SapphireException {
        File zipFile = null;
        if (command.equals(COMMAND_CHECKOUT)) {
            try {
                String sdcid = commandRequest.getString("sdcid");
                String keyid1 = commandRequest.getString("keyid1");
                String keyid2 = commandRequest.getString("keyid2");
                String keyid3 = commandRequest.getString("keyid3");
                boolean keyfromremoteLookup = commandRequest.has("keyfromremotelookup") && "Y".equals(commandRequest.getString("keyfromremotelookup"));
                CMTPolicy cmtPolicy = CMTPolicy.getPolicy(this.getConnectionid(), sdcid);
                String identifycolumn = cmtPolicy.getIndentifyColumn();
                if (identifycolumn.length() > 0 && !keyfromremoteLookup) {
                    DataSet keyidDataSet = new DataSet();
                    keyidDataSet.addColumnValues("keyid1", 0, keyid1, ";");
                    keyidDataSet.addColumnValues("keyid2", 0, keyid2, ";");
                    keyidDataSet.addColumnValues("keyid3", 0, keyid3, ";");
                    String[] idcolumns = RequestParser.parseColItem("primary[" + identifycolumn + "]");
                    String table = this.getSDCProcessor().getProperty(sdcid, "tableid");
                    String keycolid1 = this.getSDCProcessor().getProperty(sdcid, "keycolid1");
                    String keycolid2 = this.getSDCProcessor().getProperty(sdcid, "keycolid2");
                    String keycolid3 = this.getSDCProcessor().getProperty(sdcid, "keycolid3");
                    for (int c = 0; c < idcolumns.length; ++c) {
                        String columnid = idcolumns[c];
                        String columnalias = idcolumns[c];
                        if (idcolumns[c].lastIndexOf(" ") > 0) {
                            columnid = idcolumns[c].substring(0, idcolumns[c].lastIndexOf(" "));
                            columnalias = idcolumns[c].substring(idcolumns[c].lastIndexOf(" ")).trim();
                        }
                        idcolumns[c] = columnid;
                        keyidDataSet.addColumnValues(columnid, 0, commandRequest.getString(columnalias), ";");
                    }
                    for (int i = 0; i < keyidDataSet.getRowCount(); ++i) {
                        SafeSQL safeSQL = new SafeSQL();
                        String sql = "SELECT " + keycolid1 + (keycolid2.length() > 0 ? "," + keycolid2 : "") + (keycolid3.length() > 0 ? "," + keycolid3 : "") + " FROM " + table + " WHERE ";
                        for (int c = 0; c < idcolumns.length; ++c) {
                            String colvalue = keyidDataSet.getValue(i, idcolumns[c]);
                            sql = sql + (c == 0 ? "" : " AND ") + "(" + idcolumns[c] + "=" + safeSQL.addVar(colvalue) + (colvalue.length() == 0 ? " OR " + idcolumns[c] + " is null" : "") + ")";
                        }
                        DataSet matchingDataSet = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
                        if (matchingDataSet.getRowCount() != 1) continue;
                        keyidDataSet.setValue(i, "keyid1", matchingDataSet.getValue(0, keycolid1));
                        if (keycolid2.length() <= 0) continue;
                        keyidDataSet.setValue(i, "keyid2", matchingDataSet.getValue(0, keycolid2));
                        if (keycolid3.length() <= 0) continue;
                        keyidDataSet.setValue(i, "keyid3", matchingDataSet.getValue(0, keycolid3));
                    }
                    keyid1 = keyidDataSet.getColumnValues("keyid1", ";");
                    if (keycolid2.length() > 0) {
                        keyid2 = keyidDataSet.getColumnValues("keyid2", ";");
                        if (keycolid3.length() > 0) {
                            keyid3 = keyidDataSet.getColumnValues("keyid3", ";");
                        }
                    }
                }
                SnapshotFactory snapshotUtil = new SnapshotFactory(this.getConnectionId());
                SnapshotPackage snapshotPackage = null;
                if ("N".equals(commandRequest.getString("checkout"))) {
                    snapshotPackage = "PropertyTree".equals(sdcid) ? snapshotUtil.packagePropertyTreeSnapshot(keyid1, commandRequest.getString("propertytreenodeid")) : snapshotUtil.packageSnapshot(sdcid, keyid1, keyid2, keyid3, sdcid, false);
                } else {
                    String changerequestid = commandRequest.has("changerequestid") ? commandRequest.getString("changerequestid") : "";
                    PropertyList props = new PropertyList();
                    props.setProperty("sdcid", sdcid);
                    props.setProperty("keyid1", keyid1);
                    props.setProperty("keyid2", keyid2);
                    props.setProperty("keyid3", keyid3);
                    if ("PropertyTree".equals(sdcid)) {
                        props.setProperty("propertytreenodeid", commandRequest.getString("propertytreenodeid"));
                    }
                    props.setProperty("changerequestid", changerequestid);
                    props.setProperty("fromremotecheckout", "Y");
                    this.getActionProcessor().processAction("CheckOutSDI", "1", props);
                    String changelogidlist = props.getProperty("changelogid");
                    if (changelogidlist.length() > 0) {
                        snapshotPackage = snapshotUtil.packageFromChangeLog(changelogidlist, changelogidlist);
                    } else {
                        throw new Exception(sdcid + ";" + keyid1 + ";" + keyid2 + ";" + keyid3 + " Not Exist In Master Repository request SDI");
                    }
                }
                String fileName = "checkoutpackage" + System.currentTimeMillis();
                zipFile = File.createTempFile(fileName, ".zip");
                zipFile = snapshotPackage.toFile(zipFile.getParentFile().getAbsolutePath(), fileName + ".zip", this.getConnectionId(), this.getRakFile());
                zipFile.deleteOnExit();
            }
            catch (Exception e) {
                throw new SapphireException(ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
            }
        }
        return zipFile;
    }

    private static void copyInputStreamToFile(InputStream inputStream, File file) throws IOException {
        try (FileOutputStream outputStream = new FileOutputStream(file);){
            int read;
            byte[] bytes = new byte[1024];
            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
            outputStream.close();
        }
    }
}

