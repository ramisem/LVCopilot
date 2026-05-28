/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.xml;

import com.labvantage.sapphire.SDI;
import com.labvantage.sapphire.actions.eln.BaseELNAction;
import com.labvantage.sapphire.services.DDTService;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.SequenceService;
import com.labvantage.sapphire.xml.AbstractTransferable;
import com.labvantage.sapphire.xml.Column;
import com.labvantage.sapphire.xml.ExportXML;
import com.labvantage.sapphire.xml.ImportDirective;
import com.labvantage.sapphire.xml.Logger;
import com.labvantage.sapphire.xml.SDCTransfer;
import com.labvantage.sapphire.xml.SDIDetail;
import com.labvantage.sapphire.xml.Table;
import com.labvantage.sapphire.xml.TableTransfer;
import com.labvantage.sapphire.xml.TransferConstants;
import com.labvantage.sapphire.xml.TransferPackage;
import com.labvantage.sapphire.xml.Transferable;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipOutputStream;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.SequenceProcessor;
import sapphire.util.DBAccess;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.DOMUtil;
import sapphire.xml.PropertyList;

public class SDITransfer
extends AbstractTransferable
implements Transferable,
TransferConstants,
Cloneable {
    private String label = "";
    private String sdcid;
    private String keyid1;
    private String keyid2;
    private String keyid3;
    private String queryfrom;
    private String querywhere;
    private String queryorderby;
    private String categoryid;
    private String rsetid;
    private String exportid;
    private String primaryTableid;
    private String keygenRule;
    private HashSet linktableids;
    private DataSet parsedKeys;
    private String primaryForceUpdate = "false";
    private String primaryForceNullUpdate = "false";
    private String detailForceUpdate = "false";
    private String detailForceNullUpdate = "false";
    private String exportDetails = "true";
    private String exportSDIDetails = "true";
    private String flushDetails = "false";
    private String flushSDI = "false";
    private String flushSDIDetails = "false";
    private String flushChildSDI = "false";
    private String exportFKDetails = "false";
    private String exportSecurityDetails = "false";
    private String flushRoles = "false";
    private String flushCategories = "false";
    private String syncDataModel = "false";
    private String data;
    private boolean multifile = false;
    private boolean regenKeys = false;
    private boolean returnOnNoMatch = false;
    private boolean ignoreExportedTest = false;
    private boolean ignoreSequenceCheck = false;
    private boolean ignoreFlush = false;
    ArrayList sdiDetails = new ArrayList();
    private TableTransfer currentTable;
    private HashSet<String> exportedSDIs;
    private HashMap<String, HashMap<String, String>> regeneratedKeys;
    private String keycolid1;
    private String keycolid2;
    private String keycolid3;
    private ArrayList<String> altkeycols = new ArrayList();

    public SDITransfer(String sdcid) {
        this.sdcid = sdcid;
    }

    @Override
    public String getId() {
        return this.getSdcid();
    }

    public String getSdcid() {
        return this.sdcid;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setSdcid(String sdcid) {
        this.sdcid = sdcid;
    }

    public String getKeyid1() {
        return this.keyid1;
    }

    public void setKeyid1(String keyid1) {
        this.keyid1 = keyid1;
    }

    public String getKeyid2() {
        return this.keyid2;
    }

    public void setKeyid2(String keyid2) {
        this.keyid2 = keyid2;
    }

    public String getKeyid3() {
        return this.keyid3;
    }

    public void setKeyid3(String keyid3) {
        this.keyid3 = keyid3;
    }

    public String toString() {
        return this.sdcid + " " + this.keyid1 + (this.keyid2 != null && this.keyid2.length() > 0 ? ", " + this.keyid2 : "") + (this.keyid3 != null && this.keyid3.length() > 0 ? ", " + this.keyid3 : "");
    }

    public String getQueryfrom() {
        return this.queryfrom != null ? this.queryfrom : "";
    }

    public void setQueryfrom(String queryfrom) {
        this.queryfrom = queryfrom;
    }

    public String getQuerywhere() {
        return this.querywhere != null ? this.querywhere : "";
    }

    public void setQuerywhere(String querywhere) {
        this.querywhere = querywhere;
    }

    public String getQueryorderby() {
        return this.queryorderby != null ? this.queryorderby : "";
    }

    public void setQueryorderby(String queryorderby) {
        this.queryorderby = queryorderby;
    }

    public String getCategoryid() {
        return this.categoryid;
    }

    public void setCategoryid(String categoryid) {
        this.categoryid = categoryid;
    }

    public String getRsetid() {
        return this.rsetid;
    }

    public void setRsetid(String rsetid) {
        this.rsetid = rsetid;
    }

    public String getExportid() {
        return this.exportid;
    }

    public void setExportid(String exportid) {
        this.exportid = exportid;
    }

    public void setAltkeycols(String altkeycols) {
        this.altkeycols.clear();
        if (altkeycols != null && altkeycols.length() > 0) {
            Collections.addAll(this.altkeycols, StringUtil.split(altkeycols, ";"));
        }
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getPrimaryTableid() {
        return this.primaryTableid;
    }

    @Override
    public Object getParsedData() {
        return this.parsedKeys;
    }

    public void addSDIDetail(SDIDetail sdiDetail) {
        SDIDetail existing = this.getSDIDetail(sdiDetail.getDetailid());
        if (existing == null) {
            this.sdiDetails.add(sdiDetail);
        }
    }

    public SDIDetail getSDIDetail(String detailid) {
        for (int i = 0; i < this.sdiDetails.size(); ++i) {
            SDIDetail sdiDetail = (SDIDetail)this.sdiDetails.get(i);
            if (!sdiDetail.getDetailid().equals(detailid)) continue;
            return sdiDetail;
        }
        return null;
    }

    public void removeSDIDetail(String detailid) {
        for (int i = 0; i < this.sdiDetails.size(); ++i) {
            SDIDetail sdiDetail = (SDIDetail)this.sdiDetails.get(i);
            if (!sdiDetail.getDetailid().equals(detailid)) continue;
            this.sdiDetails.remove(i);
            break;
        }
    }

    public SDIDetail getLatestSDIDetail() {
        return this.sdiDetails.size() > 0 ? (SDIDetail)this.sdiDetails.get(this.sdiDetails.size() - 1) : null;
    }

    public void setExportDetails(String exportDetails) {
        this.exportDetails = exportDetails;
    }

    public boolean isExportDetail() {
        return this.exportDetails != null && (this.exportDetails.equals("true") || this.exportDetails.equals("Y"));
    }

    public void setFlushDetails(String flushDetails) {
        this.flushDetails = flushDetails;
    }

    public boolean isFlushDetail() {
        return this.flushDetails != null && (this.flushDetails.equals("true") || this.flushDetails.equals("Y"));
    }

    public void setExportSDIDetails(String exportSDIDetails) {
        this.exportSDIDetails = exportSDIDetails;
    }

    public boolean isExportSDIDetail() {
        return this.exportSDIDetails != null && (this.exportSDIDetails.equals("true") || this.exportSDIDetails.equals("Y"));
    }

    public void setFlushSDI(String flushSDI) {
        this.flushSDI = flushSDI;
    }

    public boolean isFlushSDI() {
        return this.flushSDI != null && (this.flushSDI.equals("true") || this.flushSDI.equals("Y"));
    }

    public void setFlushSDIDetails(String flushSDIDetails) {
        this.flushSDIDetails = flushSDIDetails;
    }

    public boolean isFlushSDIDetail() {
        return this.flushSDIDetails != null && (this.flushSDIDetails.equals("true") || this.flushSDIDetails.equals("Y"));
    }

    public void setFlushChildSDI(String flushChildSDI) {
        this.flushChildSDI = flushChildSDI;
    }

    public boolean isFlushChildSDI() {
        return this.flushChildSDI != null && (this.flushChildSDI.equals("true") || this.flushChildSDI.equals("Y"));
    }

    public boolean getFlushChildSDI() {
        return this.flushChildSDI != null && (this.flushChildSDI.equals("true") || this.flushChildSDI.equals("Y"));
    }

    public void setExportFKDetails(String exportFKDetails) {
        this.exportFKDetails = exportFKDetails;
    }

    public boolean isExportFKDetail() {
        return this.exportFKDetails != null && (this.exportFKDetails.equals("true") || this.exportFKDetails.equals("Y"));
    }

    public void setExportSecurityDetails(String exportSecurityDetails) {
        this.exportSecurityDetails = exportSecurityDetails;
    }

    public boolean isExportSecurityDetail() {
        return this.exportSecurityDetails != null && (this.exportSecurityDetails.equals("true") || this.exportSecurityDetails.equals("Y"));
    }

    public void setFlushRoles(String flushRoles) {
        this.flushRoles = flushRoles;
    }

    public boolean isExportRoles() {
        SDIDetail roles = this.getSDIDetail("sdirole");
        return roles != null;
    }

    public boolean isFlushRoles() {
        SDIDetail roles = this.getSDIDetail("sdirole");
        return roles != null && roles.isFlush() || this.flushRoles != null && (this.flushRoles.equals("true") || this.flushRoles.equals("Y"));
    }

    public void setFlushCategories(String flushCategories) {
        this.flushCategories = flushCategories;
    }

    public boolean isExportCategories() {
        SDIDetail categories = this.getSDIDetail("categoryitem");
        return categories != null;
    }

    public boolean isFlushCategories() {
        SDIDetail categories = this.getSDIDetail("categoryitem");
        return categories != null && categories.isFlush() || this.flushCategories != null && (this.flushCategories.equals("true") || this.flushCategories.equals("Y"));
    }

    public void setPrimaryForceUpdate(String primaryForceUpdate) {
        this.primaryForceUpdate = primaryForceUpdate;
    }

    public boolean isPrimaryForceUpdate() {
        return this.primaryForceUpdate != null && (this.primaryForceUpdate.equals("true") || this.primaryForceUpdate.equals("Y"));
    }

    public void setPrimaryForceNullUpdate(String primaryForceNullUpdate) {
        this.primaryForceNullUpdate = primaryForceNullUpdate;
    }

    public boolean isPrimaryForceNullUpdate() {
        return this.primaryForceNullUpdate != null && (this.primaryForceNullUpdate.equals("true") || this.primaryForceNullUpdate.equals("Y"));
    }

    public void setDetailForceUpdate(String detailForceUpdate) {
        this.detailForceUpdate = detailForceUpdate;
    }

    public void setDetailForceNullUpdate(String detailForceNullUpdate) {
        this.detailForceNullUpdate = detailForceNullUpdate;
    }

    public void setSyncDataModel(String syncDataModel) {
        this.syncDataModel = syncDataModel;
    }

    public String getSyncDataModel() {
        return this.syncDataModel;
    }

    public boolean isSyncDataModel() {
        return this.syncDataModel != null && (this.syncDataModel.equals("true") || this.syncDataModel.equals("Y"));
    }

    public void setMultifile(boolean multifile) {
        this.multifile = multifile;
    }

    public void setRegenKeys(boolean regenKeys) {
        this.regenKeys = regenKeys;
    }

    public void setIgnoreSequenceCheck(boolean ignoreSequenceCheck) {
        this.ignoreSequenceCheck = ignoreSequenceCheck;
    }

    public void setIgnoreFlush(boolean ignoreFlush) {
        this.ignoreFlush = ignoreFlush;
    }

    public ArrayList<String> getAltkeycols() {
        return this.altkeycols;
    }

    @Override
    public void evalProperties(PropertyList props) {
        this.keyid1 = TransferPackage.replaceProperties(this.keyid1, props);
        this.keyid2 = TransferPackage.replaceProperties(this.keyid2, props);
        this.keyid3 = TransferPackage.replaceProperties(this.keyid3, props);
        this.categoryid = TransferPackage.replaceProperties(this.categoryid, props);
        this.queryfrom = TransferPackage.replaceProperties(this.queryfrom, props);
        this.querywhere = TransferPackage.replaceProperties(this.querywhere, props);
        this.queryorderby = TransferPackage.replaceProperties(this.queryorderby, props);
        this.rsetid = TransferPackage.replaceProperties(this.rsetid, props);
        this.exportid = TransferPackage.replaceProperties(this.exportid, props);
        this.keyseparator = TransferPackage.replaceProperties(this.keyseparator, props);
        this.syncDataModel = TransferPackage.replaceProperties(this.syncDataModel, props);
        this.filename = TransferPackage.replaceProperties(this.filename, props);
        if (this.filename != null) {
            this.setFile(new File(this.filename));
        }
        this.keyFilename = TransferPackage.replaceProperties(this.keyFilename, props);
        this.primaryForceUpdate = TransferPackage.replaceProperties(this.primaryForceUpdate, props);
        this.primaryForceNullUpdate = TransferPackage.replaceProperties(this.primaryForceNullUpdate, props);
        this.detailForceUpdate = TransferPackage.replaceProperties(this.detailForceUpdate, props);
        this.detailForceNullUpdate = TransferPackage.replaceProperties(this.detailForceNullUpdate, props);
        this.exportDetails = TransferPackage.replaceProperties(this.exportDetails, props);
        this.flushDetails = TransferPackage.replaceProperties(this.flushDetails, props);
        this.exportFKDetails = TransferPackage.replaceProperties(this.exportFKDetails, props);
        this.exportSecurityDetails = TransferPackage.replaceProperties(this.exportSecurityDetails, props);
        this.exportSDIDetails = TransferPackage.replaceProperties(this.exportSDIDetails, props);
        this.flushSDI = TransferPackage.replaceProperties(this.flushSDI, props);
        this.flushChildSDI = TransferPackage.replaceProperties(this.flushChildSDI, props);
        this.flushSDIDetails = TransferPackage.replaceProperties(this.flushSDIDetails, props);
        for (int i = 0; i < this.columns.size(); ++i) {
            Column column = (Column)this.columns.get(i);
            column.setValue(TransferPackage.replaceProperties(column.getValue(), props));
            column.setForceUpdate(TransferPackage.replaceProperties(column.getForceUpdate(), props));
            column.setForceNullUpdate(TransferPackage.replaceProperties(column.getForceNullUpdate(), props));
        }
    }

    @Override
    public void export(File exportFile, PrintStream out, ZipOutputStream zipOut, DBAccess database, int level, Logger logger, Map exported) throws CloneNotSupportedException, SQLException, IOException, SapphireException {
        if (this.sdcid == null || this.sdcid.length() == 0) {
            throw new SapphireException("Sdcid not specified for SDI export");
        }
        String level0 = StringUtil.repeat("\t", level);
        String level1 = StringUtil.repeat("\t", level + 1);
        String level2 = StringUtil.repeat("\t", level + 2);
        long start = System.currentTimeMillis();
        logger.log(level0 + "Exporting " + this.sdcid + " SDIs...");
        database.createPreparedResultSet("SELECT * FROM sdc WHERE sdcid = ?", new Object[]{this.sdcid});
        if (!database.getNext()) {
            throw new SapphireException("Invalid sdcid '" + this.sdcid + "'");
        }
        String tableid = database.getString("tableid");
        String keygenrule = database.getValue("keygenerationrule");
        if (this.isExportSDIDetail()) {
            this.addSDIDetail(new SDIDetail("sdialias"));
            this.addSDIDetail(new SDIDetail("sdiapproval"));
            this.addSDIDetail(new SDIDetail("sdiapprovalstep"));
            this.addSDIDetail(new SDIDetail("sdiattribute"));
            this.addSDIDetail(new SDIDetail("sdieventplan"));
            this.addSDIDetail(new SDIDetail("sdieventplanitem"));
            this.addSDIDetail(new SDIDetail("sdieventplanitemproperty"));
            this.addSDIDetail(new SDIDetail("sdiformrule"));
            this.addSDIDetail(new SDIDetail("sdiworkflowrule"));
            this.addSDIDetail(new SDIDetail("sdiworksheetrule"));
            this.addSDIDetail(new SDIDetail("sdinote"));
            if (database.getString("addressesflag") != null && database.getString("addressesflag").equals("Y")) {
                this.addSDIDetail(new SDIDetail("sdiaddress"));
            }
            if (database.getString("dataentryflag") != null && database.getString("dataentryflag").equals("Y")) {
                this.addSDIDetail(new SDIDetail("sdidata"));
                this.addSDIDetail(new SDIDetail("sdidataitem"));
                this.addSDIDetail(new SDIDetail("sdidataitemlimits"));
                this.addSDIDetail(new SDIDetail("sdidataitemspec"));
                this.addSDIDetail(new SDIDetail("sdidataapproval"));
            }
            if (database.getString("specflag") != null && database.getString("specflag").equals("Y")) {
                this.addSDIDetail(new SDIDetail("sdispec"));
                this.addSDIDetail(new SDIDetail("sdispecrule"));
            }
            if (database.getString("workitemflag") != null && database.getString("workitemflag").equals("Y")) {
                this.addSDIDetail(new SDIDetail("sdiworkitem"));
                this.addSDIDetail(new SDIDetail("sdiworkitemitem"));
            }
        }
        if (this.isExportSecurityDetail()) {
            this.addSDIDetail(new SDIDetail("sdisecuritydepartment"));
            this.addSDIDetail(new SDIDetail("sdisecurityset"));
        }
        String[] keys = DDTService.getKeyColumns(database, tableid);
        int keycols = keys.length;
        SDITransfer.defineQueryParams(database, this, tableid, keys);
        int count = database.getCount("SELECT count(*) FROM " + this.queryfrom + (this.querywhere != null && this.querywhere.length() > 0 ? " WHERE " + this.querywhere : ""));
        if (this.exportid != null && this.exportid.length() > 0) {
            if (count > 0) {
                logger.log(level0 + "Exporting " + this.sdcid + " SDIs using export script: " + this.exportid);
                TransferPackage transferPackage = new TransferPackage();
                transferPackage.loadExportScript(database, this.sdcid, this.exportid);
                PropertyList exportProps = new PropertyList();
                exportProps.setProperty("export.sdcid", this.sdcid);
                exportProps.setProperty("export.keyid1", this.keyid1);
                exportProps.setProperty("export.keyid2", this.keyid2);
                exportProps.setProperty("export.keyid3", this.keyid3);
                String rsetid = SDITransfer.createExportScriptRSet(database, this.sdcid, tableid, this.queryfrom, this.querywhere, keys);
                exportProps.setProperty("export.rsetid", rsetid);
                ArrayList exports = transferPackage.getExportScriptExportXML().getExports();
                for (int i = 0; i < exports.size(); ++i) {
                    Transferable exportItem = (Transferable)exports.get(i);
                    exportItem.evalProperties(exportProps);
                    ExportXML.exportItem(exportItem, exportFile, out, zipOut, database, logger, exported, this.multifile, false, this.verbose);
                }
            }
        } else {
            logger.log(level0 + "Exporting " + this.sdcid + (this.label.length() > 0 ? " (" + this.label + ")" : "") + " SDIs using " + this.getOptionsText());
            if (!this.ignoreExportedTest) {
                StringBuffer exclusions = new StringBuffer();
                database.createResultSet("SELECT " + tableid + "." + keys[0] + (keycols > 1 ? "," + tableid + "." + keys[1] : "") + (keycols > 2 ? "," + tableid + "." + keys[2] : "") + " FROM " + this.queryfrom + (this.querywhere != null && this.querywhere.length() > 0 ? " WHERE " + this.querywhere : "") + (this.queryorderby != null && this.queryorderby.length() > 0 ? " ORDER BY " + this.queryorderby : ""));
                boolean match = false;
                while (database.getNext()) {
                    match = true;
                    String exportitem = this.sdcid + ":" + database.getString(keys[0]) + (keycols > 1 ? ";" + database.getString(keys[1]) : "") + (keycols > 2 ? ";" + database.getString(keys[2]) : "");
                    if (exported.containsKey(exportitem)) {
                        exclusions.append(" OR ( ").append(tableid).append(".").append(keys[0]).append(" = '").append(database.getString(keys[0])).append("' ");
                        exclusions.append(keycols >= 2 ? "AND " + tableid + "." + keys[1] + " = '" + database.getString(keys[1]) + "' " : " )");
                        exclusions.append(keycols >= 3 ? "AND " + tableid + "." + keys[2] + " = '" + database.getString(keys[2]) + "' )" : (keycols >= 2 ? " )" : ""));
                        continue;
                    }
                    exported.put(exportitem, Boolean.TRUE);
                }
                if (this.returnOnNoMatch && !match) {
                    return;
                }
                if (exclusions.length() > 0) {
                    this.querywhere = this.querywhere != null && this.querywhere.length() > 0 ? this.querywhere + " AND " : " ";
                    this.querywhere = this.querywhere + (database.isOracle() ? "( " + tableid + "." + keys[0] + (keycols > 1 ? "," + tableid + "." + keys[1] : "") + (keycols > 2 ? "," + tableid + "." + keys[2] : "") + ") NOT IN ( SELECT " + tableid + "." + keys[0] + (keycols > 1 ? "," + tableid + "." + keys[1] : "") + (keycols > 2 ? "," + tableid + "." + keys[2] : "") + " FROM " + tableid + " WHERE " + exclusions.substring(3) + " )" : "( " + tableid + "." + keys[0] + (keycols > 1 ? " + ';' + " + tableid + "." + keys[1] : "") + (keycols > 2 ? " + ';' + " + tableid + "." + keys[2] : "") + ") NOT IN ( SELECT " + tableid + "." + keys[0] + (keycols > 1 ? " + ';' + " + tableid + "." + keys[1] : "") + (keycols > 2 ? " + ';' + " + tableid + "." + keys[2] : "") + " FROM " + tableid + " WHERE " + exclusions.substring(3) + " )");
                    database.createResultSet("SELECT " + tableid + "." + keys[0] + (keycols > 1 ? "," + tableid + "." + keys[1] : "") + (keycols > 2 ? "," + tableid + "." + keys[2] : "") + " FROM " + this.queryfrom + (this.querywhere != null && this.querywhere.length() > 0 ? " WHERE " + this.querywhere : "") + (this.queryorderby != null && this.queryorderby.length() > 0 ? " ORDER BY " + this.queryorderby : ""));
                    if (!database.getNext()) {
                        return;
                    }
                }
            }
            ArrayList<TableTransfer> tablelist = new ArrayList<TableTransfer>();
            ArrayList<SDITransfer> sdilist = new ArrayList<SDITransfer>();
            ArrayList<SDITransfer> childsdilist = new ArrayList<SDITransfer>();
            logger.log(level0 + "Default export using " + this.getOptionsText());
            TableTransfer primary = new TableTransfer(tableid, this.columns);
            primary.setTransferOption("forcedevmode", this.getTransferOption("forcedevmode"));
            primary.setForceLOBExport(true);
            primary.setFrom(this.queryfrom);
            primary.setWhere(this.querywhere);
            primary.setOrderby(this.queryorderby);
            primary.setDefaultForceUpdate(this.primaryForceUpdate);
            primary.setDefaultForceNullUpdate(this.primaryForceNullUpdate);
            primary.setExcludeAuditColumns(this.excludeAuditColumns);
            primary.setExcludeSecurityColumns(!this.isExportSecurityDetail());
            primary.setExcludeExportAttributes(this.excludeExportAttributes);
            primary.setExportTableDefinition(this.exportTableDefinition);
            primary.setVerbose(this.verbose);
            primary.setData(this.data);
            tablelist.add(primary);
            if (!this.multifile) {
                int i;
                SDITransfer sdi;
                int loopCheckPos = -1;
                ArrayList<Column> loopCheckColumns = new ArrayList<Column>();
                database.createPreparedResultSet("SELECT * FROM sdclink WHERE sdcid = ? AND (linksdcid IS NULL OR linksdcid <> 'SDC') ORDER BY linksequence", new Object[]{this.sdcid});
                while (database.getNext()) {
                    String fkquerywhere;
                    String fktablekeyidlist;
                    String linkid = database.getValue("linkid");
                    String linktype = database.getValue("linktype");
                    if (this.isExportDetail() && (linktype.equals("D") || linktype.equals("M"))) {
                        String linktableid = database.getString("linktableid");
                        if (this.getSDIDetail(linktableid) != null || this.sdcid.equals("SDC") && (linktableid.equals("systable") || linktableid.equals("syscolumn") || linktableid.equals("syscolumnproperty") || linktableid.equals("sysref") || linktableid.equals("sysrefcolumn")) || this.sdcid.equals("LV_Worksheet") && (linktableid.equals("worksheetactivitylog") || linktableid.equals("worksheetsdi") || linktableid.equals("worksheetcontributor")) || this.sdcid.equals("LV_WorksheetItem") && (linktableid.equals("worksheetitemsdi") || linktableid.equals("worksheetitemreference"))) continue;
                        TableTransfer linktable = new TableTransfer(linktableid);
                        linktable.setTransferOption("forcedevmode", this.getTransferOption("forcedevmode"));
                        linktable.setLinkid(linkid);
                        linktable.setForceLOBExport(true);
                        linktable.setFrom(linktableid);
                        if (database.isOracle()) {
                            linktable.setWhere("( " + linktableid + "." + keys[0] + (keycols >= 2 ? ", " + linktableid + "." + keys[1] + (keycols >= 3 ? ", " + linktableid + "." + keys[2] : "") : "") + " ) IN ( SELECT " + tableid + "." + keys[0] + (keycols >= 2 ? ", " + tableid + "." + keys[1] + (keycols >= 3 ? ", " + tableid + "." + keys[2] : "") : "") + "  FROM " + primary.getFrom() + (primary.getWhere() != null && primary.getWhere().length() > 0 ? " WHERE " + primary.getWhere() : "") + ")");
                        } else {
                            linktable.setWhere(linktableid + "." + keys[0] + (keycols >= 2 ? " + ';' + " + linktableid + "." + keys[1] + (keycols >= 3 ? " + ';' + " + linktableid + "." + keys[2] : "") : "") + " IN ( SELECT " + tableid + "." + keys[0] + (keycols >= 2 ? " + ';' + " + tableid + "." + keys[1] + (keycols >= 3 ? " + ';' + " + tableid + "." + keys[2] : "") : "") + "  FROM " + primary.getFrom() + (primary.getWhere() != null && primary.getWhere().length() > 0 ? " WHERE " + primary.getWhere() : "") + ")");
                        }
                        linktable.setOrderby(this.queryorderby);
                        linktable.setDefaultForceUpdate(this.detailForceUpdate);
                        linktable.setDefaultForceNullUpdate(this.detailForceNullUpdate);
                        linktable.setExcludeAuditColumns(this.excludeAuditColumns);
                        linktable.setExcludeSecurityColumns(!this.isExportSecurityDetail());
                        linktable.setExportTableDefinition(this.exportTableDefinition);
                        linktable.setVerbose(this.verbose);
                        tablelist.add(linktable);
                        continue;
                    }
                    if (!this.isExportFKDetail() || !linktype.equals("F") || !this.isExportSecurityDetail() && (this.isExportSecurityDetail() || linkid.equalsIgnoreCase("securityset link") || linkid.equalsIgnoreCase("securityuser link") || linkid.equalsIgnoreCase("securitydepartment link"))) continue;
                    boolean loopCheck = false;
                    database.createPreparedResultSet("LoopCheck", "SELECT linkid, linktype, linksdcid, sdccolumnid FROM   sdclink s1 WHERE  sdcid = ? AND sdccolumnid IS NOT NULL AND        exists ( SELECT linksdcid FROM sdclink s2 WHERE s2.sdcid = s1.linksdcid AND s2.linksdcid = s1.sdcid )", new Object[]{database.getString("linksdcid")});
                    if (database.getNext("LoopCheck")) {
                        Column exclude = new Column(database.getString("sdccolumnid"));
                        exclude.setExcluded(true);
                        loopCheckColumns.add(exclude);
                        if (loopCheckPos == -1) {
                            SDITransfer sdi2 = new SDITransfer(this.sdcid);
                            sdi2.setLabel("Loop check");
                            sdi2.setReferencedItems(this.referencedItems);
                            sdi2.setQueryfrom(primary.getFrom());
                            sdi2.setQuerywhere(primary.getWhere());
                            sdi2.setVerbose(this.verbose);
                            sdi2.setExportDetails("false");
                            sdi2.setExportSDIDetails("false");
                            sdi2.setExportFKDetails("false");
                            sdi2.setExportSecurityDetails(this.exportSecurityDetails);
                            sdi2.ignoreExportedTest = true;
                            ArrayList<Column> columnlist = new ArrayList<Column>();
                            columnlist.add(new Column("*"));
                            columnlist.add(exclude);
                            sdi2.setColumns(columnlist);
                            sdilist.add(sdi2);
                            loopCheckPos = sdilist.size() - 1;
                            loopCheck = true;
                        } else {
                            SDITransfer sdiTransfer = (SDITransfer)sdilist.get(loopCheckPos);
                            sdiTransfer.getColumns().add(exclude);
                        }
                    }
                    String fksdcid = database.getString("linksdcid");
                    database.createPreparedResultSet("fktableid", "SELECT tableid FROM sdc WHERE sdcid = ?", new Object[]{fksdcid});
                    database.getNext("fktableid");
                    String fktableid = database.getValue("fktableid", "tableid");
                    String[] fktablekeys = DDTService.getKeyColumns(database, fktableid);
                    String string = database.isOracle() ? "(" + fktablekeys[0] + (fktablekeys.length > 1 ? "," + fktablekeys[1] : "") + (fktablekeys.length > 2 ? "," + fktablekeys[2] : "") + ")" : (fktablekeyidlist = "(" + fktablekeys[0] + (fktablekeys.length > 1 ? " + ';' + " + fktablekeys[1] : "") + (fktablekeys.length > 2 ? " + ';' + " + fktablekeys[2] : "") + ")");
                    String string2 = database.isOracle() ? fktablekeyidlist + " in ( SELECT " + database.getString("sdccolumnid") + (database.getString("sdccolumnid2") != null && database.getString("sdccolumnid2").length() > 0 ? ", " + database.getString("sdccolumnid2") : "") + (database.getString("sdccolumnid3") != null && database.getString("sdccolumnid3").length() > 0 ? ", " + database.getString("sdccolumnid3") : "") + " FROM " + primary.getFrom() + (primary.getWhere() != null && primary.getWhere().length() > 0 ? " WHERE " + primary.getWhere() : "") + ")" : (fkquerywhere = fktablekeyidlist + " in ( SELECT " + database.getString("sdccolumnid") + (database.getString("sdccolumnid2") != null && database.getString("sdccolumnid2").length() > 0 ? " + ';' + " + database.getString("sdccolumnid2") : "") + (database.getString("sdccolumnid3") != null && database.getString("sdccolumnid3").length() > 0 ? " + ';' + " + database.getString("sdccolumnid3") : "") + " FROM " + primary.getFrom() + (primary.getWhere() != null && primary.getWhere().length() > 0 ? " WHERE " + primary.getWhere() : "") + ")");
                    String countsql = "SELECT count(*) FROM " + fktableid + " WHERE " + fkquerywhere;
                    if (database.getCount(countsql) <= 0) continue;
                    SDITransfer sdi3 = new SDITransfer(fksdcid);
                    sdi3.setLabel("Primary FK - " + database.getString("sdccolumnid"));
                    sdi3.setReferencedItems(this.referencedItems);
                    sdi3.setQueryfrom(fktableid);
                    sdi3.setQuerywhere(fkquerywhere);
                    sdi3.setPrimaryForceUpdate(this.detailForceUpdate);
                    sdi3.setPrimaryForceNullUpdate(this.detailForceNullUpdate);
                    sdi3.setExcludeAuditColumns(this.excludeAuditColumns);
                    sdi3.setVerbose(this.verbose);
                    sdi3.setExportFKDetails(loopCheck ? "false" : "true");
                    sdi3.setExportSecurityDetails(this.exportSecurityDetails);
                    if (this.getSDIDetail("sdirole") != null) {
                        sdi3.addSDIDetail(new SDIDetail("sdirole"));
                    }
                    if (this.getSDIDetail("categoryitem") != null) {
                        sdi3.addSDIDetail(new SDIDetail("categoryitem"));
                    }
                    sdilist.add(sdi3);
                }
                database.createPreparedResultSet("SELECT * FROM sdcdetaillink WHERE sdcid = ? AND linktype = 'D'  ORDER BY linksequence", new Object[]{this.sdcid});
                while (database.getNext()) {
                    String linktableid;
                    if (!this.isExportDetail() || this.getSDIDetail(linktableid = database.getString("linktableid")) != null) continue;
                    TableTransfer linktable = new TableTransfer(linktableid);
                    linktable.setTransferOption("forcedevmode", this.getTransferOption("forcedevmode"));
                    linktable.setLinkid(database.getString("linkid"));
                    linktable.setForceLOBExport(true);
                    linktable.setFrom(linktableid);
                    if (database.isOracle()) {
                        linktable.setWhere("( " + linktableid + "." + keys[0] + (keycols >= 2 ? ", " + linktableid + "." + keys[1] + (keycols >= 3 ? ", " + linktableid + "." + keys[2] : "") : "") + " ) IN ( SELECT " + tableid + "." + keys[0] + (keycols >= 2 ? ", " + tableid + "." + keys[1] + (keycols >= 3 ? ", " + tableid + "." + keys[2] : "") : "") + "  FROM " + primary.getFrom() + (primary.getWhere() != null && primary.getWhere().length() > 0 ? " WHERE " + primary.getWhere() : "") + ")");
                    } else {
                        linktable.setWhere(linktableid + "." + keys[0] + (keycols >= 2 ? " + ';' + " + linktableid + "." + keys[1] + (keycols >= 3 ? " + ';' + " + linktableid + "." + keys[2] : "") : "") + " IN ( SELECT " + tableid + "." + keys[0] + (keycols >= 2 ? " + ';' + " + tableid + "." + keys[1] + (keycols >= 3 ? " + ';' + " + tableid + "." + keys[2] : "") : "") + "  FROM " + primary.getFrom() + (primary.getWhere() != null && primary.getWhere().length() > 0 ? " WHERE " + primary.getWhere() : "") + ")");
                    }
                    linktable.setOrderby(this.queryorderby);
                    linktable.setDefaultForceUpdate(this.detailForceUpdate);
                    linktable.setDefaultForceNullUpdate(this.detailForceNullUpdate);
                    linktable.setExcludeAuditColumns(this.excludeAuditColumns);
                    linktable.setExcludeSecurityColumns(!this.isExportSecurityDetail());
                    linktable.setExportTableDefinition(this.exportTableDefinition);
                    linktable.setVerbose(this.verbose);
                    tablelist.add(linktable);
                }
                if (this.isExportFKDetail()) {
                    database.createPreparedResultSet("SELECT sdcdetaillink.linkid, sdcdetaillink.detaillinkid, sdcdetaillink.linksdcid, sdcdetaillink.sdccolumnid, sdcdetaillink.linktableid, sdc2.tableid FROM   sdcdetaillink, sdc, sdclink, sdc sdc2 WHERE  sdc.sdcid = ? AND        sdclink.sdcid = sdc.sdcid AND        sdcdetaillink.sdcid = sdc.sdcid AND        sdcdetaillink.linkid = sdclink.linkid AND        sdclink.linktype IN ( 'D', 'M' ) AND        sdcdetaillink.linktype = 'F' AND        sdcdetaillink.linksdcid = sdc2.sdcid AND       coalesce (sdcdetaillink.linksdcid,'X') <> 'SDC'   ORDER BY sdclink.linksequence, sdcdetaillink.detaillinkid", new Object[]{this.sdcid});
                    while (database.getNext()) {
                        String detailtableid = database.getString("linktableid");
                        sdi = new SDITransfer(database.getString("linksdcid"));
                        sdi.label = "Detail FK - " + detailtableid + "." + database.getString("sdccolumnid");
                        sdi.setReferencedItems(this.referencedItems);
                        sdi.setQueryfrom(database.getString("tableid"));
                        if (database.isOracle()) {
                            sdi.setQuerywhere("[keyid1] in ( SELECT " + database.getString("sdccolumnid") + " FROM " + detailtableid + " WHERE ( " + detailtableid + "." + keys[0] + (keycols >= 2 ? ", " + detailtableid + "." + keys[1] + (keycols >= 3 ? ", " + detailtableid + "." + keys[2] : "") : "") + " ) IN ( SELECT " + tableid + "." + keys[0] + (keycols >= 2 ? ", " + tableid + "." + keys[1] + (keycols >= 3 ? ", " + tableid + "." + keys[2] : "") : "") + " FROM " + primary.getFrom() + (primary.getWhere() != null && primary.getWhere().length() > 0 ? " WHERE " + primary.getWhere() : "") + ") )");
                        } else {
                            sdi.setQuerywhere("[keyid1] in ( SELECT " + database.getString("sdccolumnid") + " FROM " + detailtableid + " WHERE ( " + detailtableid + "." + keys[0] + (keycols >= 2 ? " + ';' + " + detailtableid + "." + keys[1] + (keycols >= 3 ? " + ';' + " + detailtableid + "." + keys[2] : "") : "") + " ) IN ( SELECT " + tableid + "." + keys[0] + (keycols >= 2 ? " + ';' + " + tableid + "." + keys[1] + (keycols >= 3 ? " + ';' + " + tableid + "." + keys[2] : "") : "") + " FROM " + primary.getFrom() + (primary.getWhere() != null && primary.getWhere().length() > 0 ? " WHERE " + primary.getWhere() : "") + ") )");
                        }
                        sdi.setQueryorderby("1");
                        sdi.setPrimaryForceUpdate(this.detailForceUpdate);
                        sdi.setPrimaryForceNullUpdate(this.detailForceNullUpdate);
                        sdi.setExcludeAuditColumns(this.excludeAuditColumns);
                        sdi.setVerbose(this.verbose);
                        sdi.returnOnNoMatch = true;
                        sdi.setExportFKDetails(sdi.getSdcid().equals("LV_TaskDef") ? "false" : "true");
                        sdi.setExportSecurityDetails(this.exportSecurityDetails);
                        if (this.getSDIDetail("sdirole") != null) {
                            sdi.addSDIDetail(new SDIDetail("sdirole"));
                        }
                        if (this.getSDIDetail("categoryitem") != null) {
                            sdi.addSDIDetail(new SDIDetail("categoryitem"));
                        }
                        sdilist.add(sdi);
                    }
                }
                for (int i2 = 0; i2 < this.sdiDetails.size(); ++i2) {
                    TableTransfer detailtableParent;
                    SDIDetail sdiDetail = (SDIDetail)this.sdiDetails.get(i2);
                    if (sdiDetail.isExcluded()) continue;
                    TableTransfer detailtable = new TableTransfer(sdiDetail.getDetailid());
                    detailtable.setTransferOption("forcedevmode", this.getTransferOption("forcedevmode"));
                    detailtable.setForceLOBExport(true);
                    detailtable.setFrom(sdiDetail.getDetailid());
                    detailtable.setDefaultForceUpdate(this.detailForceUpdate);
                    detailtable.setDefaultForceNullUpdate(this.detailForceNullUpdate);
                    detailtable.setExcludeAuditColumns(this.excludeAuditColumns);
                    detailtable.setExcludeSecurityColumns(!this.isExportSecurityDetail());
                    detailtable.setExportTableDefinition(this.exportTableDefinition);
                    detailtable.setVerbose(this.verbose);
                    if (sdiDetail.isSDIXXXDetail()) {
                        detailtable.setExportTableDefWithNoData(false);
                        if (database.isOracle()) {
                            detailtable.setWhere("sdcid = '" + this.sdcid + "' AND ( " + sdiDetail.getDetailid() + ".keyid1" + (keycols >= 2 ? ", " + sdiDetail.getDetailid() + ".keyid2" + (keycols >= 3 ? ", " + sdiDetail.getDetailid() + ".keyid3" : "") : "") + " ) IN ( SELECT " + tableid + "." + keys[0] + (keycols >= 2 ? ", " + tableid + "." + keys[1] + (keycols >= 3 ? ", " + tableid + "." + keys[2] : "") : "") + "  FROM " + primary.getFrom() + (primary.getWhere() != null && primary.getWhere().length() > 0 ? " WHERE " + primary.getWhere() : "") + ")" + (sdiDetail.getExtendedwhere().length() > 0 ? " AND " + sdiDetail.getExtendedwhere() : ""));
                        } else {
                            detailtable.setWhere("sdcid = '" + this.sdcid + "' AND ( " + sdiDetail.getDetailid() + ".keyid1" + (keycols >= 2 ? " + ';' +  " + sdiDetail.getDetailid() + ".keyid2" + (keycols >= 3 ? " + ';' + " + sdiDetail.getDetailid() + ".keyid3" : "") : "") + " ) IN ( SELECT " + tableid + "." + keys[0] + (keycols >= 2 ? " + ';' + " + tableid + "." + keys[1] + (keycols >= 3 ? " + ';' + " + tableid + "." + keys[2] : "") : "") + "  FROM " + primary.getFrom() + (primary.getWhere() != null && primary.getWhere().length() > 0 ? " WHERE " + primary.getWhere() : "") + ")" + (sdiDetail.getExtendedwhere().length() > 0 ? " AND " + sdiDetail.getExtendedwhere() : ""));
                        }
                    } else if (sdiDetail.isRole() && !this.sdcid.equals("Role")) {
                        detailtableParent = new TableTransfer("role");
                        detailtableParent.setTransferOption("forcedevmode", this.getTransferOption("forcedevmode"));
                        detailtableParent.setForceLOBExport(true);
                        detailtableParent.setFrom("role");
                        detailtableParent.setOrderby("roleid");
                        detailtableParent.setDefaultForceUpdate(this.detailForceUpdate);
                        detailtableParent.setDefaultForceNullUpdate(this.detailForceNullUpdate);
                        detailtableParent.setExcludeAuditColumns(this.excludeAuditColumns);
                        detailtableParent.setExcludeSecurityColumns(!this.isExportSecurityDetail());
                        detailtableParent.setExportTableDefinition(this.exportTableDefinition);
                        detailtableParent.setVerbose(this.verbose);
                        if (this.propagateAuditColumns) {
                            this.propagateAuditColumns(detailtableParent);
                        }
                        String primarySelect = "( SELECT " + tableid + "." + keys[0] + "  FROM " + primary.getFrom() + (primary.getWhere() != null && primary.getWhere().length() > 0 ? " WHERE " + primary.getWhere() : "") + ")";
                        if (database.isOracle()) {
                            detailtableParent.setWhere("roleid IN ( SELECT roleid FROM sdirole WHERE sdcid = '" + this.sdcid + "' AND keyid1 IN " + primarySelect + ")");
                            detailtable.setWhere("sdcid = '" + this.sdcid + "' AND sdirole.keyid1  IN " + primarySelect + (sdiDetail.getExtendedwhere().length() > 0 ? " AND " + sdiDetail.getExtendedwhere() : ""));
                        } else {
                            detailtableParent.setWhere("roleid IN ( SELECT roleid FROM sdirole WHERE sdcid = '" + this.sdcid + "' AND keyid1 IN " + primarySelect + ")");
                            detailtable.setWhere("sdcid = '" + this.sdcid + "' AND sdirole.keyid1  IN " + primarySelect + (sdiDetail.getExtendedwhere().length() > 0 ? " AND " + sdiDetail.getExtendedwhere() : ""));
                        }
                        tablelist.add(detailtableParent);
                    } else if (sdiDetail.isCategory()) {
                        detailtableParent = new TableTransfer("category");
                        detailtableParent.setTransferOption("forcedevmode", this.getTransferOption("forcedevmode"));
                        detailtableParent.setForceLOBExport(true);
                        detailtableParent.setFrom("category");
                        detailtableParent.setOrderby("sdcid, categoryid");
                        detailtableParent.setDefaultForceUpdate(this.detailForceUpdate);
                        detailtableParent.setDefaultForceNullUpdate(this.detailForceNullUpdate);
                        detailtableParent.setExcludeAuditColumns(this.excludeAuditColumns);
                        detailtableParent.setExcludeSecurityColumns(!this.isExportSecurityDetail());
                        detailtableParent.setExportTableDefinition(this.exportTableDefinition);
                        detailtableParent.setVerbose(this.verbose);
                        if (this.propagateAuditColumns) {
                            this.propagateAuditColumns(detailtableParent);
                        }
                        String primarySelect = "( SELECT " + tableid + "." + keys[0] + "  FROM " + primary.getFrom() + (primary.getWhere() != null && primary.getWhere().length() > 0 ? " WHERE " + primary.getWhere() : "") + ")";
                        if (database.isOracle()) {
                            detailtableParent.setWhere("( categoryid, sdcid ) IN ( SELECT categoryid, sdcid FROM categoryitem WHERE sdcid = '" + this.sdcid + "' AND keyid1 IN " + primarySelect + ")");
                            detailtable.setWhere("sdcid = '" + this.sdcid + "' AND ( categoryitem.keyid1 ) IN " + primarySelect + (sdiDetail.getExtendedwhere().length() > 0 ? " AND " + sdiDetail.getExtendedwhere() : ""));
                        } else {
                            detailtableParent.setWhere("categoryid + ';' + sdcid IN ( SELECT categoryid + ';' + sdcid FROM categoryitem WHERE sdcid = '" + this.sdcid + "' AND keyid1 IN " + primarySelect + ")");
                            detailtable.setWhere("sdcid = '" + this.sdcid + "' AND ( categoryitem.keyid1 ) IN " + primarySelect + (sdiDetail.getExtendedwhere().length() > 0 ? " AND " + sdiDetail.getExtendedwhere() : ""));
                        }
                        tablelist.add(detailtableParent);
                    } else {
                        ArrayList columns = (ArrayList)sdiDetail.getColumns();
                        for (int j = 0; j < columns.size(); ++j) {
                            detailtable.addColumn((Column)columns.get(j));
                        }
                        if (database.isOracle()) {
                            detailtable.setWhere("( " + sdiDetail.getDetailid() + "." + keys[0] + (keycols >= 2 ? ", " + sdiDetail.getDetailid() + "." + keys[1] + (keycols >= 3 ? ", " + sdiDetail.getDetailid() + "." + keys[2] : "") : "") + " ) IN ( SELECT " + tableid + "." + keys[0] + (keycols >= 2 ? ", " + tableid + "." + keys[1] + (keycols >= 3 ? ", " + tableid + "." + keys[2] : "") : "") + "  FROM " + primary.getFrom() + (primary.getWhere() != null && primary.getWhere().length() > 0 ? " WHERE " + primary.getWhere() : "") + ")" + (sdiDetail.getExtendedwhere().length() > 0 ? " AND " + sdiDetail.getExtendedwhere() : ""));
                        } else {
                            detailtable.setWhere(sdiDetail.getDetailid() + "." + keys[0] + (keycols >= 2 ? " + ';' + " + sdiDetail.getDetailid() + "." + keys[1] + (keycols >= 3 ? " + ';' + " + sdiDetail.getDetailid() + "." + keys[2] : "") : "") + " IN ( SELECT " + tableid + "." + keys[0] + (keycols >= 2 ? " + ';' + " + tableid + "." + keys[1] + (keycols >= 3 ? " + ';' + " + tableid + "." + keys[2] : "") : "") + "  FROM " + primary.getFrom() + (primary.getWhere() != null && primary.getWhere().length() > 0 ? " WHERE " + primary.getWhere() : "") + ")" + (sdiDetail.getExtendedwhere().length() > 0 ? " AND " + sdiDetail.getExtendedwhere() : ""));
                        }
                    }
                    tablelist.add(detailtable);
                }
                if (this.sdcid.equals("LV_Worksheet")) {
                    this.setFlushDetails("true");
                    this.setFlushChildSDI("true");
                    if (this.columns.size() == 0) {
                        Column all = new Column("*");
                        all.setForceUpdate("true");
                        this.addColumn(all);
                        Column authorid = new Column("authorid");
                        authorid.setExcluded(true);
                        this.addColumn(authorid);
                        Column authordt = new Column("authordt");
                        authordt.setExcluded(true);
                        this.addColumn(authordt);
                        Column templateid = new Column("templateid");
                        templateid.setExcluded(true);
                        this.addColumn(templateid);
                        Column templateversionid = new Column("templateversionid");
                        templateversionid.setExcluded(true);
                        this.addColumn(templateversionid);
                    }
                    SDITransfer worksheetsection = new SDITransfer("LV_WorksheetSection");
                    worksheetsection.setLabel("Child worksheet section");
                    worksheetsection.setQueryfrom("worksheetsection");
                    if (database.isOracle()) {
                        worksheetsection.setQuerywhere("( worksheetsection.worksheetid, worksheetsection.worksheetversionid ) IN ( SELECT worksheet.worksheetid, worksheet.worksheetversionid FROM " + primary.getFrom() + (primary.getWhere() != null && primary.getWhere().length() > 0 ? " WHERE " + primary.getWhere() : "") + ")");
                    } else {
                        worksheetsection.setQuerywhere("worksheetsection.worksheetid + ';' + worksheetsection.worksheetversionid IN ( SELECT worksheet.worksheetid + ';' + worksheet.worksheetversionid FROM " + primary.getFrom() + (primary.getWhere() != null && primary.getWhere().length() > 0 ? " WHERE " + primary.getWhere() : "") + ")");
                    }
                    worksheetsection.setExcludeAuditColumns(this.excludeAuditColumns);
                    worksheetsection.setVerbose(this.verbose);
                    worksheetsection.returnOnNoMatch = true;
                    worksheetsection.setExportFKDetails("false");
                    worksheetsection.setExportSecurityDetails("false");
                    worksheetsection.setPrimaryForceUpdate("false");
                    worksheetsection.setReferencedItems(this.referencedItems);
                    if (this.getSDIDetail("sdirole") != null) {
                        worksheetsection.addSDIDetail(new SDIDetail("sdirole"));
                    }
                    if (this.getSDIDetail("categoryitem") != null) {
                        worksheetsection.addSDIDetail(new SDIDetail("categoryitem"));
                    }
                    Column wssall = new Column("*");
                    worksheetsection.addColumn(wssall);
                    Column wsstemplateid = new Column("templateid");
                    wsstemplateid.setExcluded(true);
                    worksheetsection.addColumn(wsstemplateid);
                    Column wsstemplateversionid = new Column("templateversionid");
                    wsstemplateversionid.setExcluded(true);
                    worksheetsection.addColumn(wsstemplateversionid);
                    childsdilist.add(worksheetsection);
                    SDITransfer worksheetitem = new SDITransfer("LV_WorksheetItem");
                    worksheetitem.setLabel("Child worksheet item");
                    worksheetitem.setQueryfrom("worksheetitem");
                    if (database.isOracle()) {
                        worksheetitem.setQuerywhere("( worksheetitem.worksheetid, worksheetitem.worksheetversionid ) IN ( SELECT worksheet.worksheetid, worksheet.worksheetversionid FROM " + primary.getFrom() + (primary.getWhere() != null && primary.getWhere().length() > 0 ? " WHERE " + primary.getWhere() : "") + ")");
                    } else {
                        worksheetitem.setQuerywhere("worksheetitem.worksheetid + ';' + worksheetitem.worksheetversionid IN ( SELECT worksheet.worksheetid + ';' + worksheet.worksheetversionid FROM " + primary.getFrom() + (primary.getWhere() != null && primary.getWhere().length() > 0 ? " WHERE " + primary.getWhere() : "") + ")");
                    }
                    worksheetitem.setExcludeAuditColumns(this.excludeAuditColumns);
                    worksheetitem.setVerbose(this.verbose);
                    worksheetitem.returnOnNoMatch = true;
                    worksheetitem.setExportFKDetails("false");
                    worksheetitem.setExportSecurityDetails("false");
                    worksheetitem.setPrimaryForceUpdate("false");
                    worksheetitem.setReferencedItems(this.referencedItems);
                    if (this.getSDIDetail("sdirole") != null) {
                        worksheetitem.addSDIDetail(new SDIDetail("sdirole"));
                    }
                    if (this.getSDIDetail("categoryitem") != null) {
                        worksheetitem.addSDIDetail(new SDIDetail("categoryitem"));
                    }
                    Column wsiall = new Column("*");
                    worksheetitem.addColumn(wsiall);
                    Column wsitemplateid = new Column("templateid");
                    wsitemplateid.setExcluded(true);
                    worksheetitem.addColumn(wsitemplateid);
                    Column wsitemplateversionid = new Column("templateversionid");
                    wsitemplateversionid.setExcluded(true);
                    worksheetitem.addColumn(wsitemplateversionid);
                    childsdilist.add(worksheetitem);
                }
                if (this.sdcid.equals("LV_TaskDef") && "Y".equals(this.getTransferOption("includeconnectortypes"))) {
                    SDITransfer sdi4 = new SDITransfer("LV_ConnectorType");
                    sdi4.setLabel("Child connectortype");
                    sdi4.setQueryfrom("connectortype");
                    if (database.isOracle()) {
                        sdi4.setQuerywhere("[keyid1] in ( SELECT DISTINCT connectortypeid FROM taskdefio WHERE ( taskdefio.taskdefid, taskdefio.taskdefversionid, taskdefio.taskdefvariantid ) IN ( SELECT taskdef.taskdefid, taskdef.taskdefversionid, taskdef.taskdefvariantid FROM taskdef " + (primary.getWhere() != null && primary.getWhere().length() > 0 ? " WHERE " + primary.getWhere() : "") + ") )");
                    } else {
                        sdi4.setQuerywhere("[keyid1] in ( SELECT DISTINCT connectortypeid FROM taskdefio WHERE taskdefio.taskdefid + ';' + taskdefio.taskdefversionid + ';' + taskdefio.taskdefvariantid IN ( SELECT taskdef.taskdefid + ';' + taskdef.taskdefversionid + ';' + taskdef.taskdefvariantid FROM taskdef " + (primary.getWhere() != null && primary.getWhere().length() > 0 ? " WHERE " + primary.getWhere() : "") + ") )");
                    }
                    sdi4.setExcludeAuditColumns(this.excludeAuditColumns);
                    sdi4.setVerbose(this.verbose);
                    sdi4.returnOnNoMatch = true;
                    sdi4.setExportFKDetails("false");
                    sdi4.setExportSecurityDetails("false");
                    sdi4.setPrimaryForceUpdate("false");
                    sdi4.setReferencedItems(this.referencedItems);
                    if (this.getSDIDetail("sdirole") != null) {
                        sdi4.addSDIDetail(new SDIDetail("sdirole"));
                    }
                    if (this.getSDIDetail("categoryitem") != null) {
                        sdi4.addSDIDetail(new SDIDetail("categoryitem"));
                    }
                    sdilist.add(sdi4);
                }
                if (this.sdcid.equals("LV_WorkflowDef") && "Y".equals(this.getTransferOption("includetaskdefs"))) {
                    SDITransfer sdi5 = new SDITransfer("LV_TaskDef");
                    sdi5.setLabel("Child Task Definition");
                    sdi5.setQueryfrom("taskdef");
                    if (database.isOracle()) {
                        sdi5.setQuerywhere("( [keyid1], [keyid2], [keyid3] ) in ( SELECT taskdefid, taskdefversionid, taskdefvariantid FROM workflowdeftask WHERE ( workflowdeftask.workflowdefid, workflowdeftask.workflowdefversionid ) IN ( SELECT workflowdef.workflowdefid, workflowdef.workflowdefversionid FROM workflowdef " + (primary.getWhere() != null && primary.getWhere().length() > 0 ? " WHERE " + primary.getWhere() : "") + ") )");
                    } else {
                        sdi5.setQuerywhere("[keyid1]+';'+[keyid2]+';'+[keyid3] in ( SELECT taskdefid + ';' + taskdefversionid + ';' + taskdefvariantid FROM workflowdeftask WHERE workflowdeftask.workflowdefid + ';' + workflowdeftask.workflowdefversionid IN ( SELECT workflowdef.workflowdefid + ';' + workflowdef.workflowdefversionid FROM workflowdef " + (primary.getWhere() != null && primary.getWhere().length() > 0 ? " WHERE " + primary.getWhere() : "") + ") )");
                    }
                    if ("replace".equals(this.getTransferOption("taskexists"))) {
                        sdi5.setDetailForceUpdate("true");
                        sdi5.setPrimaryForceUpdate("true");
                    }
                    sdi5.setTransferOption("includestepdefs", this.getTransferOption("includestepdefs"));
                    sdi5.setTransferOption("nodeexists", this.getTransferOption("nodeexists"));
                    sdi5.setTransferOption("forcedevmode", this.getTransferOption("forcedevmode"));
                    sdi5.setTransferOption("includeconnectortypes", this.getTransferOption("includeconnectortypes"));
                    if (this.getSDIDetail("sdirole") != null) {
                        sdi5.addSDIDetail(new SDIDetail("sdirole"));
                    }
                    if (this.getSDIDetail("categoryitem") != null) {
                        sdi5.addSDIDetail(new SDIDetail("categoryitem"));
                    }
                    sdi5.setExcludeAuditColumns(this.excludeAuditColumns);
                    sdi5.setVerbose(this.verbose);
                    sdi5.returnOnNoMatch = true;
                    sdi5.setReferencedItems(this.referencedItems);
                    if (this.getSDIDetail("sdirole") != null) {
                        sdi5.addSDIDetail(new SDIDetail("sdirole"));
                    }
                    if (this.getSDIDetail("categoryitem") != null) {
                        sdi5.addSDIDetail(new SDIDetail("categoryitem"));
                    }
                    sdilist.add(sdi5);
                }
                if (this.sdcid.equals("LV_TaskDef") && "Y".equals(this.getTransferOption("includestepdefs")) || this.sdcid.equals("LV_GizmoDef") && "Y".equals(this.getTransferOption("includegizmotypes")) || this.sdcid.equals("WebPage") && "Y".equals(this.getTransferOption("includenodes"))) {
                    SDITransfer sdi6 = new SDITransfer("PropertyTree");
                    sdi6.setLabel("Child PropertyTree");
                    ArrayList<Column> columns = new ArrayList<Column>();
                    Column all = new Column();
                    all.setColumnid("*");
                    all.setForceUpdate("false");
                    all.setForceNullUpdate("false");
                    columns.add(all);
                    Column valuetree = new Column();
                    valuetree.setColumnid("valuetree");
                    valuetree.setExcluded(true);
                    columns.add(valuetree);
                    sdi6.setColumns(columns);
                    sdi6.setReferencedItems(this.referencedItems);
                    sdi6.setQueryfrom("propertytree");
                    if (this.sdcid.equals("LV_TaskDef")) {
                        if (database.isOracle()) {
                            sdi6.setQuerywhere("[keyid1] in ( SELECT propertytreeid FROM taskdefstep WHERE ( taskdefstep.taskdefid, taskdefstep.taskdefversionid, taskdefstep.taskdefvariantid ) IN ( SELECT taskdef.taskdefid, taskdef.taskdefversionid, taskdef.taskdefvariantid FROM taskdef" + (primary.getWhere() != null && primary.getWhere().length() > 0 ? " WHERE " + primary.getWhere() : "") + ") )");
                        } else {
                            sdi6.setQuerywhere("[keyid1] in ( SELECT propertytreeid FROM taskdefstep WHERE taskdefstep.taskdefid + ';' + taskdefstep.taskdefversionid + ';' + taskdefstep.taskdefvariantid IN ( SELECT taskdef.taskdefid + ';' + taskdef.taskdefversionid + ';' + taskdef.taskdefvariantid FROM taskdef" + (primary.getWhere() != null && primary.getWhere().length() > 0 ? " WHERE " + primary.getWhere() : "") + ") )");
                        }
                    } else if (this.sdcid.equals("LV_GizmoDef")) {
                        sdi6.setQuerywhere("[keyid1] in ( SELECT propertytreeid FROM gizmodef WHERE gizmodef.gizmodefid IN ( SELECT gizmodef.gizmodefid FROM gizmodef" + (primary.getWhere() != null && primary.getWhere().length() > 0 ? " WHERE " + primary.getWhere() : "") + ") )");
                    } else {
                        String where = "[keyid1] in ( SELECT propertytreeid FROM webpagepropertytree WHERE ";
                        if ("Y".equals(this.getTransferOption("excludegenericlayout"))) {
                            where = where + " propertytreeid <> 'Generic' AND ";
                        }
                        where = where + " webpagepropertytree.webpageid IN ( SELECT webpage.webpageid FROM " + (primary.getFrom() != null && primary.getFrom().length() > 0 ? primary.getFrom() : "webpage") + (primary.getWhere() != null && primary.getWhere().length() > 0 ? " WHERE " + primary.getWhere() : "") + ") )";
                        sdi6.setQuerywhere(where);
                    }
                    sdi6.setQueryorderby("1");
                    sdi6.setPrimaryForceUpdate("false");
                    sdi6.setPrimaryForceNullUpdate("false");
                    sdi6.setExcludeAuditColumns(this.excludeAuditColumns);
                    sdi6.setVerbose(this.verbose);
                    sdi6.returnOnNoMatch = true;
                    sdi6.setExportFKDetails("false");
                    sdi6.setExportSecurityDetails("false");
                    if (this.getSDIDetail("sdirole") != null) {
                        sdi6.addSDIDetail(new SDIDetail("sdirole"));
                    }
                    if (this.getSDIDetail("categoryitem") != null) {
                        sdi6.addSDIDetail(new SDIDetail("categoryitem"));
                    }
                    sdilist.add(sdi6);
                }
                for (int i3 = 0; i3 < sdilist.size(); ++i3) {
                    sdi = (SDITransfer)sdilist.get(i3);
                    sdi.setReferencedItems(this.referencedItems);
                    sdi.export(exportFile, out, zipOut, database, level + 1, logger, exported);
                }
                if (this.isSyncDataModel()) {
                    SDCTransfer sdc = new SDCTransfer(this.getSdcid());
                    sdc.export(exportFile, out, zipOut, database, level + 1, logger, exported);
                }
                if (this.sdcid.equals("LV_Worksheet")) {
                    database.createResultSet("SELECT worksheet.worksheetid, worksheet.worksheetversionid, templateflag, templateprivacyflag FROM " + primary.getFrom() + (primary.getWhere().length() > 0 ? " WHERE " + primary.getWhere() : "") + (primary.getOrderby().length() > 0 ? " ORDER BY " + primary.getOrderby() : ""));
                } else if (this.sdcid.equals("LV_WorksheetSection")) {
                    database.createResultSet("SELECT worksheetsection.worksheetsectionid, worksheetsection.worksheetsectionversionid, worksheet.templateflag, worksheet.templateprivacyflag FROM worksheet, " + primary.getFrom() + " WHERE worksheet.worksheetid = worksheetsection.worksheetid AND worksheet.worksheetversionid = worksheetsection.worksheetversionid " + (primary.getWhere().length() > 0 ? " AND " + primary.getWhere() : "") + (primary.getOrderby().length() > 0 ? " ORDER BY " + primary.getOrderby() : ""));
                } else if (this.sdcid.equals("LV_WorksheetItem")) {
                    database.createResultSet("SELECT worksheetitem.worksheetitemid, worksheetitem.worksheetitemversionid, worksheet.templateflag, worksheet.templateprivacyflag FROM worksheet, " + primary.getFrom() + " WHERE worksheet.worksheetid = worksheetitem.worksheetid AND worksheet.worksheetversionid = worksheetitem.worksheetversionid " + (primary.getWhere().length() > 0 ? " AND " + primary.getWhere() : "") + (primary.getOrderby().length() > 0 ? " ORDER BY " + primary.getOrderby() : ""));
                } else {
                    database.createResultSet("SELECT " + tableid + "." + keys[0] + (keycols > 1 ? "," + tableid + "." + keys[1] : "") + (keycols > 2 ? "," + tableid + "." + keys[2] : "") + " FROM " + primary.getFrom() + (primary.getWhere().length() > 0 ? " WHERE " + primary.getWhere() : "") + (primary.getOrderby().length() > 0 ? " ORDER BY " + primary.getOrderby() : ""));
                }
                int exportcount = 0;
                StringBuffer exportitems = new StringBuffer("");
                String exportItem = "";
                while (database.getNext()) {
                    ++exportcount;
                    exportItem = DOMUtil.convertChars(database.getString(keys[0]) + (keycols > 1 ? ";" + database.getString(keys[1]) : "") + (keycols > 2 ? ";" + database.getString(keys[2]) : ""));
                    exportitems.append(level2 + "<exportitem>" + exportItem + "</exportitem>\n");
                    logger.log(level0 + "--> " + exportItem);
                    if (this.sdcid.equals("LV_Worksheet") && database.getValue("templateflag").equals("Y") && database.getValue("templateprivacyflag").equals("G")) {
                        keygenrule = "";
                        continue;
                    }
                    if (!this.sdcid.equals("LV_WorksheetSection") && !this.sdcid.equals("LV_WorksheetItem") || !database.getValue("templateflag").equals("Y") || !database.getValue("templateprivacyflag").equals("G")) continue;
                    keygenrule = BaseELNAction.TEMPLATE_KEYGENRULE;
                }
                out.println(level0 + "<sdi sdcid=\"" + this.getSdcid() + "\" primarytableid=\"" + tableid + "\"" + (keygenrule != null && keygenrule.length() > 0 ? " keygenrule=\"" + keygenrule + "\"" : "") + " keycolid1=\"" + keys[0] + "\" keycolid2=\"" + (keycols > 1 ? keys[1] : "") + "\" keycolid3=\"" + (keycols > 2 ? keys[2] : "") + "\"" + (this.altkeycols.size() > 0 ? " altkeycols=\"" + StringUtil.arrayToString(this.altkeycols.toArray(new String[this.altkeycols.size()]), ";") + "\"" : "") + ">");
                out.println(level1 + "<export queryfrom=\"" + DOMUtil.convertChars(this.getQueryfrom()) + "\" querywhere=\"" + DOMUtil.convertChars(this.getQuerywhere()) + "\" exportdetails=\"" + this.isExportDetail() + "\" flushsdi=\"" + this.isFlushSDI() + "\" flushdetails=\"" + this.isFlushDetail() + "\" exportsdidetails=\"" + this.isExportSDIDetail() + "\" flushsdidetails=\"" + this.isFlushSDIDetail() + "\" flushchildsdi=\"" + this.isFlushChildSDI() + "\" exportfkdetails=\"" + this.isExportFKDetail() + "\" exportsecuritydetails=\"" + this.isExportSecurityDetail() + "\" exportroles=\"" + this.isExportRoles() + "\" flushroles=\"" + this.isFlushRoles() + "\" exportcategories=\"" + this.isExportCategories() + "\" flushcategories=\"" + this.isFlushCategories() + "\" >");
                out.println(exportitems);
                out.println(level1 + "</export>");
                for (i = 0; i < tablelist.size(); ++i) {
                    TableTransfer table = (TableTransfer)tablelist.get(i);
                    if (table.getTableid().equals(tableid) && loopCheckColumns.size() > 0) {
                        if (table.getColumns().size() == 0) {
                            table.addColumn(new Column("*"));
                        }
                        for (Column loopCheckColumn : loopCheckColumns) {
                            Column tableColumn = table.getColumn(loopCheckColumn.getColumnid());
                            if (tableColumn != null) {
                                tableColumn.setForceUpdate("true");
                                continue;
                            }
                            loopCheckColumn.setExcluded(false);
                            loopCheckColumn.setForceUpdate("true");
                            table.addColumn(loopCheckColumn);
                        }
                    }
                    table.getTransferOptions().putAll(this.getTransferOptions());
                    table.export(exportFile, out, zipOut, database, level, logger, exported);
                    this.referencedItems.addAll(table.getReferencedItems());
                }
                out.println(level0 + "</sdi>");
                for (i = 0; i < childsdilist.size(); ++i) {
                    SDITransfer sdi7 = (SDITransfer)childsdilist.get(i);
                    sdi7.setReferencedItems(this.referencedItems);
                    sdi7.export(exportFile, out, zipOut, database, level + 1, logger, exported);
                }
            } else {
                ExportXML exportXML = new ExportXML();
                exportXML.setDatabase(database);
                exportXML.setExportLog(logger);
                exportXML.setVerbose(this.verbose);
                exportXML.setExcludeExportAttributes(this.excludeExportAttributes);
                database.createResultSet("SELECT " + tableid + "." + keys[0] + (keys.length > 1 ? "," + keys[1] : "") + (keys.length > 2 ? "," + keys[2] : "") + " FROM " + primary.getFrom() + (primary.getWhere().length() > 0 ? " WHERE " + primary.getWhere() : ""));
                while (database.getNext()) {
                    String keyid1 = database.getString(keys[0]);
                    String keyid2 = keys.length > 1 ? database.getString(keys[1]) : "";
                    String keyid3 = keys.length > 2 ? database.getString(keys[2]) : "";
                    SDITransfer sdiTransfer = new SDITransfer(this.sdcid);
                    sdiTransfer.setReferencedItems(this.referencedItems);
                    sdiTransfer.setKeyid1(keyid1);
                    if (keys.length > 1) {
                        sdiTransfer.setKeyid2(keyid2);
                    }
                    if (keys.length > 2) {
                        sdiTransfer.setKeyid3(keyid3);
                    }
                    String keyvalues = ExportXML.removeIllegalChars(keyid1 + (keys.length > 1 ? "_" + keyid2 : "") + (keys.length > 2 ? "_" + keyid3 : ""));
                    sdiTransfer.setFile(new File(exportFile.getAbsolutePath() + "/" + (this.file == null ? keyvalues + ".xml" : this.file.getName())));
                    sdiTransfer.setExportDetails(this.isExportDetail() ? "true" : "false");
                    sdiTransfer.setExportSDIDetails(this.isExportSDIDetail() ? "true" : "false");
                    sdiTransfer.setExportFKDetails(this.isExportFKDetail() ? "true" : "false");
                    sdiTransfer.setExportSecurityDetails(this.isExportSecurityDetail() ? "true" : "false");
                    sdiTransfer.setExcludeAuditColumns(this.isExcludeAuditColumns());
                    sdiTransfer.setExportTableDefinition(this.exportTableDefinition);
                    sdiTransfer.setColumns((ArrayList)this.getColumns());
                    for (int i = 0; i < this.sdiDetails.size(); ++i) {
                        sdiTransfer.addSDIDetail((SDIDetail)this.sdiDetails.get(i));
                    }
                    exportXML.addExport(sdiTransfer);
                }
                if (exportXML.getExports().size() > 0) {
                    exportXML.export();
                } else {
                    logger.log(level0 + "No entries found in table " + tableid);
                }
            }
        }
        logger.log(level0 + "SDI export (for " + this.sdcid + ") took " + (System.currentTimeMillis() - start) + "ms");
        logger.log("");
    }

    private void propagateAuditColumns(TableTransfer detailTable) {
        Column all = new Column("*");
        all.setForceUpdate(this.detailForceUpdate);
        all.setForceNullUpdate(this.detailForceNullUpdate);
        detailTable.addColumn(all);
        for (int j = 0; j < this.columns.size(); ++j) {
            if (!((Column)this.columns.get(j)).isAuditColumn()) continue;
            detailTable.addColumn((Column)this.columns.get(j));
        }
    }

    public static void defineQueryParams(DBAccess database, SDITransfer sdi, String tableid, String[] keys) throws SapphireException {
        int i;
        int keycols = keys.length;
        if (sdi.keyFilename != null && sdi.keyFilename.length() > 0) {
            sdi.rsetid = SDITransfer.createManifestRSet(database, sdi, tableid, keys);
        }
        if (sdi.rsetid != null && sdi.rsetid.length() > 0) {
            if (sdi.keyid1 != null && sdi.keyid1.length() > 0 || sdi.queryfrom != null && sdi.queryfrom.length() > 0 || sdi.querywhere != null && sdi.querywhere.length() > 0 || sdi.queryorderby != null && sdi.queryorderby.length() > 0) {
                throw new SapphireException("RSet attribute cannot be specified with keyid or other query attributes");
            }
            sdi.queryfrom = tableid + ", rsetitems";
            sdi.querywhere = "rsetitems.rsetid = '" + sdi.rsetid + "' AND rsetitems.sdcid = '" + sdi.sdcid + "' AND rsetitems.keyid1 = " + tableid + "." + keys[0] + (keycols > 1 ? " AND rsetitems.keyid2 = " + tableid + "." + keys[1] : "") + (keycols > 2 ? " AND rsetitems.keyid3 = " + tableid + "." + keys[2] : "");
        } else if (sdi.categoryid != null && sdi.categoryid.length() > 0) {
            if (sdi.keyid1 != null && sdi.keyid1.length() > 0 || sdi.queryfrom != null && sdi.queryfrom.length() > 0 || sdi.querywhere != null && sdi.querywhere.length() > 0 || sdi.queryorderby != null && sdi.queryorderby.length() > 0) {
                throw new SapphireException("Category attribute cannot be specified with keyid or other query attributes");
            }
            sdi.queryfrom = tableid + ", categoryitem";
            sdi.querywhere = sdi.categoryid.indexOf(";") > -1 ? "categoryitem.categoryid IN ('" + StringUtil.replaceAll(sdi.categoryid, ";", "','") + "') AND categoryitem.sdcid = '" + sdi.sdcid + "' AND categoryitem.keyid1 = " + tableid + "." + keys[0] : "categoryitem.categoryid = '" + sdi.categoryid + "' AND categoryitem.sdcid = '" + sdi.sdcid + "' AND categoryitem.keyid1 = " + tableid + "." + keys[0];
        } else if (sdi.keyid1 != null && sdi.keyid1.length() > 0) {
            if (sdi.queryfrom != null && sdi.queryfrom.length() > 0 || sdi.querywhere != null && sdi.querywhere.length() > 0 || sdi.queryorderby != null && sdi.queryorderby.length() > 0) {
                throw new SapphireException("Keyid1,2,3 attributes cannot be specified with other query attributes");
            }
            sdi.queryfrom = tableid;
            StringBuffer where = new StringBuffer();
            String[] keyid1split = StringUtil.split(sdi.keyid1, ";");
            String[] keyid2split = StringUtil.split(sdi.keyid2, ";");
            String[] keyid3split = StringUtil.split(sdi.keyid3, ";");
            if (keycols >= 2 && (keyid2split.length != keyid1split.length || sdi.keyid2.length() == 0)) {
                throw new SapphireException("Multi-key SDC '" + sdi.sdcid + "' requires matching key identifiers for keyid1, keyid2 and keyid3");
            }
            if (keycols >= 3 && (keyid3split.length != keyid1split.length || sdi.keyid3.length() == 0)) {
                throw new SapphireException("Multi-key SDC '" + sdi.sdcid + "' requires matching key identifiers for keyid1, keyid2 and keyid3");
            }
            for (int i2 = 0; i2 < keyid1split.length; ++i2) {
                where.append(" OR ( ").append(tableid).append(".").append(keys[0]).append(" = '").append(keyid1split[i2]).append("' ");
                where.append(keycols >= 2 ? "AND " + tableid + "." + keys[1] + " = '" + keyid2split[i2] + "' " : " )");
                where.append(keycols >= 3 ? "AND " + tableid + "." + keys[2] + " = '" + keyid3split[i2] + "' )" : (keycols >= 2 ? " )" : ""));
            }
            sdi.querywhere = where.substring(3);
        } else if (sdi.queryfrom == null || sdi.queryfrom.length() == 0) {
            sdi.queryfrom = tableid;
        }
        String[] tokens = StringUtil.getTokens(sdi.queryfrom);
        for (i = 0; i < tokens.length; ++i) {
            if (tokens[i].equalsIgnoreCase("keyid1")) {
                sdi.queryfrom = StringUtil.replaceAll(sdi.queryfrom, "[" + tokens[i] + "]", keys[0]);
            }
            if (tokens[i].equalsIgnoreCase("keyid2")) {
                sdi.queryfrom = StringUtil.replaceAll(sdi.queryfrom, "[" + tokens[i] + "]", keys[1]);
            }
            if (!tokens[i].equalsIgnoreCase("keyid3")) continue;
            sdi.queryfrom = StringUtil.replaceAll(sdi.queryfrom, "[" + tokens[i] + "]", keys[2]);
        }
        tokens = StringUtil.getTokens(sdi.querywhere);
        for (i = 0; i < tokens.length; ++i) {
            if (tokens[i].equalsIgnoreCase("keyid1")) {
                sdi.querywhere = StringUtil.replaceAll(sdi.querywhere, "[" + tokens[i] + "]", keys[0]);
            }
            if (tokens[i].equalsIgnoreCase("keyid2")) {
                sdi.querywhere = StringUtil.replaceAll(sdi.querywhere, "[" + tokens[i] + "]", keys[1]);
            }
            if (tokens[i].equalsIgnoreCase("keyid3")) {
                sdi.querywhere = StringUtil.replaceAll(sdi.querywhere, "[" + tokens[i] + "]", keys[2]);
            }
            if (!tokens[i].equalsIgnoreCase("keyidlist")) continue;
            sdi.querywhere = database.isOracle() ? StringUtil.replaceAll(sdi.querywhere, "[" + tokens[i] + "]", "(" + keys[0] + (keycols > 1 ? "," + keys[1] : "") + (keycols > 2 ? "," + keys[2] : "") + ")") : StringUtil.replaceAll(sdi.querywhere, "[" + tokens[i] + "]", "(" + keys[0] + (keycols > 1 ? " + ';' + " + keys[1] : "") + (keycols > 2 ? " + ';' + " + keys[2] : "") + ")");
        }
    }

    private static String createExportScriptRSet(DBAccess database, String sdcid, String tableid, String from, String where, String[] keys) throws SapphireException {
        DataSet sdilist = new DataSet();
        database.createResultSet("SELECT " + tableid + "." + keys[0] + (keys.length > 1 ? "," + tableid + "." + keys[1] : "") + (keys.length > 2 ? "," + tableid + "." + keys[2] : "") + " FROM " + (from != null && from.length() > 0 ? from : tableid) + (where != null && where.length() > 0 ? " WHERE " + where : ""));
        while (database.getNext()) {
            int row = sdilist.addRow();
            sdilist.setString(row, keys[0], database.getString(keys[0]));
            if (keys.length > 1) {
                sdilist.setString(row, keys[1], database.getString(keys[1]));
            }
            if (keys.length <= 2) continue;
            sdilist.setString(row, keys[2], database.getString(keys[2]));
        }
        return SDITransfer.createRSet(database, sdcid, keys, sdilist);
    }

    private static String createManifestRSet(DBAccess database, SDITransfer sdi, String tableid, String[] keys) throws SapphireException {
        DataSet sdilist = new DataSet();
        try {
            FileReader fr = new FileReader(new File(sdi.keyFilename));
            BufferedReader in = new BufferedReader(fr);
            String aLine = "";
            while ((aLine = in.readLine()) != null) {
                if (aLine.trim().length() <= 0 || aLine.trim().startsWith("//")) continue;
                String[] lineKeys = StringUtil.split(aLine, sdi.keyseparator);
                if (keys.length == 1) {
                    database.createPreparedResultSet("existscheck", "SELECT 1 FROM " + tableid + " WHERE " + keys[0] + " = ?", new Object[]{lineKeys[0].trim()});
                } else if (keys.length == 2) {
                    database.createPreparedResultSet("existscheck", "SELECT 1 FROM " + tableid + " WHERE " + keys[0] + " = ? AND " + keys[1] + " = ? ", new Object[]{lineKeys[0].trim(), lineKeys[1].trim()});
                } else {
                    database.createPreparedResultSet("existscheck", "SELECT 1 FROM " + tableid + " WHERE " + keys[0] + " = ? AND " + keys[1] + " = ? AND " + keys[2] + " = ?", new Object[]{lineKeys[0].trim(), lineKeys[1].trim(), lineKeys[2].trim()});
                }
                if (database.getNext("existscheck")) {
                    int row = sdilist.addRow();
                    sdilist.setString(row, keys[0], lineKeys[0].trim());
                    if (keys.length > 1) {
                        sdilist.setString(row, keys[1], lineKeys.length > 1 ? lineKeys[1].trim() : "(null)");
                    }
                    if (keys.length <= 2) continue;
                    sdilist.setString(row, keys[2], lineKeys.length > 2 ? lineKeys[2].trim() : "(null)");
                    continue;
                }
                throw new SapphireException("Key manifest entry " + new SDI(sdi.sdcid, lineKeys[0].trim(), lineKeys.length > 1 ? lineKeys[1].trim() : "", lineKeys.length > 2 ? lineKeys[2].trim() : "").getKeyText() + " not found!");
            }
            in.close();
        }
        catch (IOException e) {
            throw new SapphireException("Failed to create manifest rset. Reason: " + e.getMessage(), e);
        }
        return SDITransfer.createRSet(database, sdi.sdcid, keys, sdilist);
    }

    private static String createRSet(DBAccess database, String sdcid, String[] keys, DataSet sdilist) throws SapphireException {
        String rsetid = String.valueOf(System.currentTimeMillis());
        if (rsetid.length() > 20) {
            rsetid = rsetid.substring(rsetid.length() - 20);
        }
        try {
            database.executePreparedUpdate("INSERT INTO RSET ( rsetid ) VALUES ( ? )", new Object[]{rsetid});
            for (int i = 0; i < sdilist.size(); ++i) {
                database.executePreparedUpdate("INSERT INTO RSETITEMS ( rsetid, sdcid, keyid1, keyid2, keyid3, rsetseq ) VALUES ( ?, ?, ?, ?, ?, ? )", new Object[]{rsetid, sdcid, sdilist.getValue(i, keys[0]), keys.length > 1 ? sdilist.getValue(i, keys[1]) : "(null)", keys.length > 2 ? sdilist.getValue(i, keys[2]) : "(null)", i});
            }
        }
        catch (Exception e) {
            throw new SapphireException("Failed to create rset. Reason:" + e.getMessage(), e);
        }
        return rsetid;
    }

    public void addExportItem(String key) {
        this.exportedSDIs.add(key);
    }

    public void setRegeneratedKeys(HashMap<String, HashMap<String, String>> regeneratedKeys) {
        this.regeneratedKeys = regeneratedKeys;
    }

    public HashMap<String, HashMap<String, String>> getRegeneratedKeys() {
        return this.regeneratedKeys;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public boolean startElementImport(DBAccess database, String elementName, Properties attributes, Logger logger) throws SapphireException {
        if (elementName.equalsIgnoreCase("sdi")) {
            logger.log((this.parseOnly ? "Parsing" : "Importing") + " SDI(s) of type: " + this.sdcid + "...");
            this.primaryTableid = attributes.getProperty("primarytableid");
            this.keygenRule = attributes.getProperty("keygenrule", "");
            if (database != null) {
                if (this.primaryTableid == null || this.primaryTableid.length() == 0 || this.keygenRule == null) {
                    database.createPreparedResultSet("SELECT tableid, keygenerationrule FROM sdc WHERE sdcid = ?", new Object[]{this.sdcid});
                    if (!database.getNext()) {
                        throw new SapphireException("Invalid sdcid '" + this.sdcid + "'");
                    }
                    this.primaryTableid = database.getString("tableid");
                    this.keygenRule = database.getValue("keygenerationrule");
                }
                this.linktableids = new HashSet();
                database.createPreparedResultSet("SELECT linktableid FROM sdclink WHERE sdcid = ?", new Object[]{this.sdcid});
                while (database.getNext()) {
                    this.linktableids.add(database.getValue("linktableid"));
                }
            }
            this.exportedSDIs = new HashSet();
            this.keycolid1 = attributes.getProperty("keycolid1");
            this.keycolid2 = attributes.getProperty("keycolid2");
            this.keycolid3 = attributes.getProperty("keycolid3");
            this.setAltkeycols(attributes.getProperty("altkeycols"));
            String regenkey = attributes.getProperty("regenkey");
            boolean bl = this.regenKeys = regenkey != null && (regenkey.equals("Y") || regenkey.equals("true"));
            if (this.regenKeys && !this.hasConnection()) {
                throw new SapphireException("Key regeneration is only supported when import is done with a LabVantage application.");
            }
            if (!this.isFlushDetail() && !this.isFlushSDIDetail() && !this.isFlushRoles() && !this.isFlushCategories() || !this.commitScope.equals("table") && !this.commitScope.equals("row")) return true;
            this.commitScope = "sdi";
            return true;
        }
        if (elementName.equalsIgnoreCase("table")) {
            String tableid = attributes.getProperty("tableid");
            this.currentTable = new TableTransfer(tableid);
            this.currentTable.setConnectionid(this.getConnectionid());
            this.currentTable.setImportObject(new Table(tableid));
            if (tableid.equals(this.primaryTableid)) {
                this.currentTable.setAltkeycolumns(this.altkeycols);
            }
            if (!this.parseOnly && this.exportedSDIs.size() > 0) {
                if (tableid.equals(this.primaryTableid)) {
                    if (this.isFlushSDI() && !this.ignoreFlush) {
                        if (!this.hasConnection()) throw new SapphireException("Flush SDI option only supported when import is done with a LabVantage application.");
                        StringBuffer deletekeyid1 = new StringBuffer();
                        StringBuffer deletekeyid2 = new StringBuffer();
                        StringBuffer deletekeyid3 = new StringBuffer();
                        for (String key : this.exportedSDIs) {
                            String[] keyids = StringUtil.split(key, ";");
                            deletekeyid1.append(";").append(keyids[0]);
                            if (keyids.length > 1) {
                                deletekeyid2.append(";").append(keyids[1]);
                            }
                            if (keyids.length > 2) {
                                deletekeyid3.append(";").append(keyids[2]);
                            }
                            logger.log("Deleting " + keyids[0] + (keyids.length > 1 ? ";" + keyids[1] : "") + (keyids.length > 2 ? ";" + keyids[2] : "") + " before reimporting...");
                        }
                        if (deletekeyid1.length() > 0) {
                            PropertyList deleteProps = new PropertyList();
                            deleteProps.setProperty("sdcid", this.sdcid);
                            deleteProps.setProperty("keyid1", deletekeyid1.substring(1));
                            if (deletekeyid2.length() > 0) {
                                deleteProps.setProperty("keyid2", deletekeyid2.substring(1));
                            }
                            if (deletekeyid3.length() > 0) {
                                deleteProps.setProperty("keyid3", deletekeyid3.substring(1));
                            }
                            ActionProcessor actionProcessor = new ActionProcessor(this.getConnectionid());
                            actionProcessor.processAction("DeleteSDI", "1", deleteProps);
                        }
                    }
                    if (this.keygenRule.contains(";Q^") && !this.ignoreSequenceCheck) {
                        if (this.altkeycols == null || this.altkeycols.size() == 0) {
                            SequenceProcessor sequenceProcessor = new SequenceProcessor(this.getConnectionid());
                            for (String key : this.exportedSDIs) {
                                String[] keyids = StringUtil.split(key, ";");
                                if (this.sdcid.equals("LV_WorksheetSection") || this.sdcid.equals("LV_WorksheetItem")) {
                                    if (!this.hasConnection()) throw new SapphireException("Sequence updates only supported when import is done with a LabVantage application.");
                                    String worksheetid = keyids[0].substring(0, keyids[0].length() - 5);
                                    int seqnum = Integer.parseInt(keyids[0].substring(keyids[0].length() - 5));
                                    int currentseq = sequenceProcessor.getSequence("LV_Worksheet", worksheetid, 0);
                                    if (currentseq >= seqnum) continue;
                                    int seq = sequenceProcessor.getSequence("LV_Worksheet", worksheetid, seqnum - currentseq);
                                    seq = 0;
                                    continue;
                                }
                                if (this.regenKeys) {
                                    String newKeyid1;
                                    HashMap<String, String> sdcRegeneratedKeys = this.regeneratedKeys.get(this.sdcid);
                                    if (sdcRegeneratedKeys == null) {
                                        sdcRegeneratedKeys = new HashMap();
                                        this.regeneratedKeys.put(this.sdcid, sdcRegeneratedKeys);
                                    }
                                    if (sdcRegeneratedKeys.containsKey(keyids[0])) {
                                        newKeyid1 = sdcRegeneratedKeys.get(keyids[0]);
                                    } else {
                                        newKeyid1 = SequenceService.updateSequences(sequenceProcessor, this.sdcid, keyids[0], this.keygenRule, true);
                                        sdcRegeneratedKeys.put(keyids[0], newKeyid1);
                                        this.currentTable.addImportDirective(ImportDirective.getReplaceValueDirective(this.primaryTableid, this.keycolid1, sdcRegeneratedKeys));
                                    }
                                    logger.log("Regenerated key for " + keyids[0] + " to " + newKeyid1);
                                    continue;
                                }
                                if (!this.hasConnection()) throw new SapphireException("Sequence updates only supported when import is done with a LabVantage application.");
                                SequenceService.updateSequences(sequenceProcessor, this.sdcid, keyids[0], this.keygenRule, false);
                            }
                        } else {
                            if (!this.hasConnection()) throw new SapphireException("Alternate key regeneration only supported when import is done with a LabVantage application.");
                            SapphireConnection sapphireConnection = new ConnectionProcessor(this.getConnectionid()).getSapphireConnection();
                            this.currentTable.addImportDirective(ImportDirective.getRegenerateDirective(new SequenceService(sapphireConnection), this.sdcid, this.keygenRule, this.keycolid1));
                        }
                    }
                    database.createPreparedResultSet("SELECT linksdcid, sdccolumnid FROM sdclink WHERE sdcid = ? AND linktype='F'", new Object[]{this.sdcid});
                    while (database.getNext()) {
                        String linksdcid = database.getValue("linksdcid");
                        HashMap<String, String> sdcRegeneratedKeys = this.regeneratedKeys.get(linksdcid);
                        if (sdcRegeneratedKeys == null) continue;
                        this.currentTable.addImportDirective(ImportDirective.getReplaceValueDirective(this.primaryTableid, database.getValue("sdccolumnid"), sdcRegeneratedKeys));
                    }
                    if (this.getFlushChildSDI() && !this.ignoreFlush) {
                        if (!this.hasConnection()) throw new SapphireException("Flush child SDIs only supported when import is done with a LabVantage application.");
                        ActionProcessor actionProcessor = new ActionProcessor(this.getConnectionid());
                        database.createPreparedResultSet("childsdcs", "SELECT sdclink.sdcid, sdccolumnid, sdccolumnid2, sdccolumnid3, tableid FROM sdclink, sdc WHERE sdc.sdcid = sdclink.sdcid AND linksdcid = ? AND linktype='F' AND deleteflag = 'D'", new Object[]{this.sdcid});
                        while (database.getNext("childsdcs")) {
                            String childsdcid = database.getValue("childsdcs", "sdcid");
                            String childtableid = database.getValue("childsdcs", "tableid");
                            String sdccolumnid1 = database.getValue("childsdcs", "sdccolumnid");
                            String sdccolumnid2 = database.getValue("childsdcs", "sdccolumnid2");
                            String sdccolumnid3 = database.getValue("childsdcs", "sdccolumnid3");
                            String[] childKeys = DDTService.getKeyColumns(database, childtableid);
                            if (childKeys == null) continue;
                            for (String key : this.exportedSDIs) {
                                String[] keyids = StringUtil.split(key, ";");
                                database.createResultSet("childsdis", "SELECT " + childKeys[0] + (childKeys.length > 1 ? "," + childKeys[1] : "") + (childKeys.length > 2 ? "," + childKeys[2] : "") + " FROM " + childtableid + " WHERE " + sdccolumnid1 + "='" + keyids[0] + "'" + (sdccolumnid2.length() > 0 ? " AND " + sdccolumnid2 + "='" + keyids[1] + "'" : "") + (sdccolumnid3.length() > 0 ? " AND " + sdccolumnid3 + "='" + keyids[2] + "'" : ""));
                                StringBuffer deletekeyid1 = new StringBuffer();
                                StringBuffer deletekeyid2 = new StringBuffer();
                                StringBuffer deletekeyid3 = new StringBuffer();
                                while (database.getNext("childsdis")) {
                                    deletekeyid1.append(";").append(database.getValue("childsdis", childKeys[0]));
                                    if (childKeys.length > 1) {
                                        deletekeyid2.append(";").append(database.getValue("childsdis", childKeys[1]));
                                    }
                                    if (childKeys.length > 2) {
                                        deletekeyid3.append(";").append(database.getValue("childsdis", childKeys[2]));
                                    }
                                    logger.log("Deleting child SDC " + childsdcid + " " + database.getValue("childsdis", childKeys[0]) + (childKeys.length > 1 ? ";" + database.getValue("childsdis", childKeys[1]) : "") + (childKeys.length > 2 ? ";" + database.getValue("childsdis", childKeys[2]) : "") + "...");
                                }
                                if (deletekeyid1.length() <= 0) continue;
                                PropertyList deleteProps = new PropertyList();
                                deleteProps.setProperty("sdcid", childsdcid);
                                deleteProps.setProperty("keyid1", deletekeyid1.substring(1));
                                if (childKeys.length > 1) {
                                    deleteProps.setProperty("keyid2", deletekeyid2.substring(1));
                                }
                                if (childKeys.length > 2) {
                                    deleteProps.setProperty("keyid3", deletekeyid3.substring(1));
                                }
                                actionProcessor.processAction("DeleteSDI", "1", deleteProps);
                            }
                        }
                    }
                } else {
                    database.createPreparedResultSet("SELECT sdikeyid FROM sdc WHERE lower( tableid )=? AND sdctype='D'", new Object[]{tableid.toLowerCase()});
                    if (database.getNext() && database.getValue("sdikeyid").length() > 0 && !this.ignoreSequenceCheck) {
                        this.currentTable.addImportDirective(ImportDirective.getResequenceDirective(tableid, database.getValue("sdikeyid")));
                        this.currentTable.setSdikeyid(database.getValue("sdikeyid"));
                    }
                    if (tableid.equalsIgnoreCase("SDINote") && !this.ignoreSequenceCheck) {
                        if (!this.hasConnection()) throw new SapphireException("SDINote import only supported when import is done with a LabVantage application.");
                        this.currentTable.addImportDirective(ImportDirective.getSDINoteDirective(this.getConnectionid()));
                    }
                    for (String key : this.exportedSDIs) {
                        String[] keyids = StringUtil.split(key, ";");
                        SafeSQL safeSQL = new SafeSQL();
                        if (this.linktableids.contains(tableid)) {
                            if (this.isFlushDetail()) {
                                logger.log("Deleting rows in detail table " + tableid + "...");
                                database.executePreparedUpdate("DELETE FROM " + tableid + " WHERE " + this.keycolid1 + "=" + safeSQL.addVar(keyids[0]) + (keyids.length > 1 ? " AND " + this.keycolid2 + "=" + safeSQL.addVar(keyids[1]) : "") + (keyids.length > 2 ? " AND " + this.keycolid3 + "=" + safeSQL.addVar(keyids[2]) : ""), safeSQL.getValues());
                            }
                            if (!this.regeneratedKeys.containsKey(this.sdcid) || !this.regeneratedKeys.get(this.sdcid).containsKey(keyids[0])) continue;
                            this.currentTable.addImportDirective(ImportDirective.getReplaceValueDirective(tableid, this.keycolid1, this.regeneratedKeys.get(this.sdcid)));
                            continue;
                        }
                        if (tableid.toLowerCase().startsWith("sdi") && !tableid.equalsIgnoreCase("sdirole")) {
                            if (this.isFlushSDIDetail()) {
                                logger.log("Deleting rows in sdidetail table " + tableid + "...");
                                database.executePreparedUpdate("DELETE FROM " + tableid + " WHERE sdcid=" + safeSQL.addVar(this.sdcid) + " AND keyid1=" + safeSQL.addVar(keyids[0]) + (keyids.length > 1 ? " AND keyid2=" + safeSQL.addVar(keyids[1]) : "") + (keyids.length > 2 ? " AND keyid3=" + safeSQL.addVar(keyids[2]) : ""), safeSQL.getValues());
                            }
                            if (!this.regeneratedKeys.containsKey(this.sdcid) || !this.regeneratedKeys.get(this.sdcid).containsKey(keyids[0])) continue;
                            this.currentTable.addImportDirective(ImportDirective.getReplaceValueDirective(tableid, "keyid1", this.regeneratedKeys.get(this.sdcid)));
                            continue;
                        }
                        if (tableid.equalsIgnoreCase("sdirole")) {
                            if (this.isFlushRoles()) {
                                logger.log("Deleting rows in role table " + tableid + "...");
                                database.executePreparedUpdate("DELETE FROM sdirole WHERE sdcid=" + safeSQL.addVar(this.sdcid) + " AND keyid1=" + safeSQL.addVar(keyids[0]), safeSQL.getValues());
                            }
                            if (!this.regeneratedKeys.containsKey(this.sdcid) || !this.regeneratedKeys.get(this.sdcid).containsKey(keyids[0])) continue;
                            this.currentTable.addImportDirective(ImportDirective.getReplaceValueDirective(tableid, "keyid1", this.regeneratedKeys.get(this.sdcid)));
                            continue;
                        }
                        if (!tableid.equalsIgnoreCase("categoryitem")) continue;
                        if (this.isFlushCategories()) {
                            logger.log("Deleting rows in category table " + tableid + "...");
                            database.executePreparedUpdate("DELETE FROM categoryitem WHERE sdcid=" + safeSQL.addVar(this.sdcid) + " AND keyid1=" + safeSQL.addVar(keyids[0]), safeSQL.getValues());
                        }
                        if (!this.regeneratedKeys.containsKey(this.sdcid) || !this.regeneratedKeys.get(this.sdcid).containsKey(keyids[0])) continue;
                        this.currentTable.addImportDirective(ImportDirective.getReplaceValueDirective(tableid, "keyid1", this.regeneratedKeys.get(this.sdcid)));
                    }
                }
            }
            this.currentTable.setImportTarget(this.importTarget);
            this.currentTable.setParseOnly(this.parseOnly);
            this.currentTable.setVerbose(this.verbose || attributes.getProperty("verbose") != null && attributes.getProperty("verbose").equals("true"));
            this.currentTable.setCommitScope(this.commitScope);
            this.currentTable.setIgnoreMissingObjects(this.ignoreMissingObjects);
            this.currentTable.setFile(this.file);
            this.currentTable.setZipFile(this.zipFile);
            this.currentTable.setZipFileEntry(this.zipFileEntry);
            this.currentTable.setImportForceUpdate(this.importForceUpdate);
            return this.currentTable.startElementImport(database, elementName, attributes, logger);
        }
        if (this.currentTable != null && this.currentTable.startElementImport(database, elementName, attributes, logger)) return true;
        return false;
    }

    @Override
    public boolean endElementImport(DBAccess database, String elementName, String elementCharacters, boolean isCDATA, Logger logger) throws SapphireException {
        if (elementName.equalsIgnoreCase("sdi")) {
            logger.log((this.parseOnly ? "Parsed" : "Imported") + " SDI(s)");
        } else {
            if (elementName.equalsIgnoreCase("table")) {
                if (this.currentTable.endElementImport(database, elementName, elementCharacters, isCDATA, logger)) {
                    if (this.currentTable.getTableid().equals(this.primaryTableid)) {
                        this.parsedKeys = (DataSet)this.currentTable.getParsedData();
                        if (this.importTarget == 1) {
                            String[] primaryKeys = ((Table)this.currentTable.importObject).getKeyColumns();
                            ((SDIData)this.importObject).setDataset("primary", (DataSet)this.currentTable.importObject);
                            ((SDIData)this.importObject).setPrimaryKeyCols(primaryKeys[0], primaryKeys.length > 1 ? primaryKeys[1] : "", primaryKeys.length > 2 ? primaryKeys[2] : "");
                        }
                    } else {
                        if (this.importTarget == 1) {
                            ((SDIData)this.importObject).setDataset(this.currentTable.getTableid(), (DataSet)this.currentTable.importObject);
                        }
                        ArrayList<ImportDirective> importDirectives = this.currentTable.getImportDirectives();
                        for (int i = 0; i < importDirectives.size(); ++i) {
                            ImportDirective importDirective = importDirectives.get(i);
                            if (!importDirective.isReseqenceDirective()) continue;
                            HashMap<String, String> sdcRegeneratedKeys = this.regeneratedKeys.get(importDirective.getTableid());
                            if (sdcRegeneratedKeys == null) {
                                sdcRegeneratedKeys = new HashMap();
                                this.regeneratedKeys.put(importDirective.getTableid(), sdcRegeneratedKeys);
                            }
                            sdcRegeneratedKeys.put(importDirective.getOldvalue(), importDirective.getNewvalue());
                        }
                    }
                    return true;
                }
                return false;
            }
            if (this.currentTable == null || !this.currentTable.endElementImport(database, elementName, elementCharacters, isCDATA, logger)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void generateAntTask(PrintStream out, int level) {
        int i;
        String level0 = StringUtil.repeat("\t", level);
        StringBuffer buffer = new StringBuffer("<sdi sdcid=\"" + this.sdcid + "\"" + (this.file != null ? " file=\"" + this.file.getName() + "\" " : " "));
        if (this.keyFilename != null && this.keyFilename.length() > 0) {
            buffer.append("keyfile=\"").append(this.keyFilename).append("\"");
            if (this.keyseparator != null && !this.keyseparator.equals("\t")) {
                buffer.append(" keyseparator=\"").append(this.keyseparator).append("\"");
            }
        } else if (this.rsetid != null && this.rsetid.length() > 0) {
            buffer.append("rsetid=\"").append(this.rsetid).append("\"");
        } else if (this.categoryid != null && this.categoryid.length() > 0) {
            buffer.append("categoryid=\"").append(this.categoryid).append("\"");
        } else if (this.keyid1 != null && this.keyid1.length() > 0) {
            buffer.append("keyid1=\"").append(this.keyid1).append("\"");
            if (this.keyid2 != null && this.keyid2.length() > 0) {
                buffer.append(" keyid2=\"").append(this.keyid2).append("\"");
            }
            if (this.keyid3 != null && this.keyid3.length() > 0) {
                buffer.append(" keyid3=\"").append(this.keyid3).append("\"");
            }
        } else {
            buffer.append("queryfrom=\"").append(this.queryfrom != null && this.queryfrom.length() > 0 ? this.queryfrom : "").append("\" querywhere=\"").append(this.querywhere != null && this.querywhere.length() > 0 ? this.querywhere : "").append("\" queryorderby=\"").append(this.queryorderby != null && this.queryorderby.length() > 0 ? this.queryorderby : "").append("\"");
        }
        if (this.exportid != null && this.exportid.length() > 0) {
            buffer.append(" exportid=\"").append(this.exportid != null && this.exportid.length() > 0 ? this.exportid : "").append("\"");
        } else {
            buffer.append(" exportdetails=\"").append(this.exportDetails).append("\" flushdetails=\"").append(this.flushDetails).append("\" exportfkdetails=\"").append(this.exportFKDetails).append("\" exportsecuritydetails=\"").append(this.exportSecurityDetails).append("\" exportsdidetails=\"").append(this.exportSDIDetails).append("\" flushsdi=\"").append(this.flushSDI).append("\" flushsdidetails=\"").append(this.flushSDIDetails).append("\" flushchildsdi=\"").append(this.flushChildSDI).append("\" exportroles=\"").append(this.getSDIDetail("sdirole") != null ? "true" : "false").append("\" flushroles=\"").append(this.isFlushRoles() ? "true" : "false").append("\" exportcategories=\"").append(this.getSDIDetail("categoryitem") != null ? "true" : "false").append("\" flushcategories=\"").append(this.isFlushCategories() ? "true" : "false").append("\"");
            buffer.append(" primaryforceupdate=\"").append(this.primaryForceUpdate).append("\" primaryforcenullupdate=\"").append(this.primaryForceNullUpdate).append("\" detailforceupdate=\"").append(this.detailForceUpdate).append("\" detailforcenullupdate=\"").append(this.detailForceNullUpdate).append("\"");
            buffer.append(" syncdatamodel=\"").append(this.syncDataModel).append("\"");
            buffer.append(" excludeauditcolumns=\"").append(this.excludeAuditColumns ? "true" : "false").append("\"");
        }
        buffer.append(" altkeycols=\"").append(StringUtil.arrayToString(this.altkeycols.toArray(new String[this.altkeycols.size()]), ";")).append("\"");
        buffer.append(this.columns.size() == 0 && this.sdiDetails.size() == 0 ? "/>" : ">");
        out.println(level0 + buffer.toString());
        if (this.columns.size() > 0) {
            for (i = 0; i < this.columns.size(); ++i) {
                Column column = (Column)this.columns.get(i);
                out.println(column.toXML(level + 1));
            }
        }
        if (this.sdiDetails.size() > 0) {
            for (i = 0; i < this.sdiDetails.size(); ++i) {
                SDIDetail sdiDetail = (SDIDetail)this.sdiDetails.get(i);
                if (sdiDetail.getDetailid().equals("sdirole") || sdiDetail.getDetailid().equals("categoryitem")) continue;
                out.println(sdiDetail.toXML(level + 1));
            }
        }
        if (this.columns.size() > 0 || this.sdiDetails.size() > 0) {
            out.println(level0 + "</sdi>");
        }
    }

    public String getOptionsText() {
        StringBuffer options = new StringBuffer();
        if (this.exportid != null && this.exportid.length() > 0) {
            options.append(", exportscript: ").append(this.exportid);
        } else {
            options.append(this.isExportDetail() ? ", exportdetails" : "");
            options.append(this.isFlushSDI() ? ", flushsdi" : "");
            options.append(this.isFlushDetail() ? ", flushdetails" : "");
            options.append(this.isFlushSDIDetail() ? ", flushsdidetails" : "");
            options.append(this.isFlushChildSDI() ? ", flushchildsdi" : "");
            options.append(this.isExportFKDetail() ? ", exportfkdetails" : "");
            options.append(this.isExportSecurityDetail() ? ", exportsecuritydetails" : "");
            options.append(this.isExportSDIDetail() ? ", exportsdidetails" : "");
            options.append(this.getSDIDetail("sdirole") != null ? ", exportroles" : "");
            options.append(this.isFlushRoles() ? ", flushroles" : "");
            options.append(this.getSDIDetail("categoryitem") != null ? ", exportcategories" : "");
            options.append(this.isFlushCategories() ? ", flushcategories" : "");
            options.append(this.isExcludeAuditColumns() ? ", excludeauditcolumns" : "");
            options.append(this.isPrimaryForceUpdate() ? ", forceupdate" : "");
            options.append(this.isPrimaryForceNullUpdate() ? ", forcenullupdate" : "");
            options.append(this.isSyncDataModel() ? ", syncdatamodel" : "");
        }
        return "options (" + (options.length() > 0 ? options.substring(2) : "") + ")";
    }

    public String getTransferType() {
        if (this.keyFilename != null && this.keyFilename.length() > 0) {
            return "keyfile";
        }
        if (this.rsetid != null && this.rsetid.length() > 0) {
            return "rset";
        }
        if (this.categoryid != null && this.categoryid.length() > 0) {
            return "category";
        }
        if (this.keyid1 != null && this.keyid1.length() > 0) {
            return "keyid";
        }
        return "query";
    }

    public boolean isValid() {
        String type = this.getTransferType();
        if (type.equals("keyfile")) {
            return this.keyFilename != null && this.keyFilename.length() > 0;
        }
        if (type.equals("rset")) {
            return this.rsetid != null && this.rsetid.length() > 0;
        }
        if (type.equals("category")) {
            return this.categoryid != null && this.categoryid.length() > 0;
        }
        if (type.equals("keyid")) {
            return this.keyid1 != null && this.keyid1.length() > 0;
        }
        return type.equals("query");
    }

    public void reset() {
        this.sdcid = "";
        this.keyid1 = "";
        this.keyid2 = "";
        this.keyid3 = "";
        this.categoryid = "";
        this.rsetid = "";
        this.queryfrom = "";
        this.querywhere = "";
        this.queryorderby = "";
        this.exportid = "";
        this.altkeycols.clear();
        this.keyFilename = "";
        this.keyseparator = "\t";
        this.primaryForceUpdate = "false";
        this.primaryForceNullUpdate = "false";
        this.detailForceUpdate = "false";
        this.detailForceNullUpdate = "false";
        this.exportDetails = "true";
        this.flushDetails = "false";
        this.flushSDI = "false";
        this.flushSDIDetails = "false";
        this.flushChildSDI = "false";
        this.flushRoles = "false";
        this.flushCategories = "false";
        this.exportSDIDetails = "true";
        this.exportFKDetails = "false";
        this.exportSecurityDetails = "false";
        this.syncDataModel = "false";
    }
}

