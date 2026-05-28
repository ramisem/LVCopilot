/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.xml;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.Build;
import com.labvantage.sapphire.xml.AbstractTransferable;
import com.labvantage.sapphire.xml.Column;
import com.labvantage.sapphire.xml.Logger;
import com.labvantage.sapphire.xml.TableTransfer;
import com.labvantage.sapphire.xml.TransferPackage;
import com.labvantage.sapphire.xml.Transferable;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.CallableStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipOutputStream;
import sapphire.SapphireException;
import sapphire.util.DBAccess;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class SDCTransfer
extends AbstractTransferable
implements Transferable,
Cloneable {
    private String sdcid;
    private String categoryid;
    private boolean excludelinktables;
    private boolean flushupdtables = true;
    private boolean resetsystables = false;
    private String forceUpdate = "true";
    private String forceNullUpdate;
    private TableTransfer currentTable;
    private boolean importedUpdTable;
    private boolean importedUpdColumn;
    private boolean importedUpdColumnProperty;
    private boolean importedUpdRef;
    private boolean importedUpdRefColumn;
    private boolean syncDDTDone;
    private String parsedData;
    private String compCode = "";

    public SDCTransfer(String sdcid) {
        this.sdcid = sdcid;
    }

    @Override
    public String getId() {
        return this.getSdcid();
    }

    public String getSdcid() {
        return !this.sdcid.equals("*") ? this.sdcid : "sdcs";
    }

    public void setSdcid(String sdcid) {
        this.sdcid = sdcid;
    }

    public void setCategoryid(String categoryid) {
        this.categoryid = categoryid;
    }

    public String getCategoryid() {
        return this.categoryid;
    }

    public void setExcludelinktables(boolean excludelinktables) {
        this.excludelinktables = excludelinktables;
    }

    public boolean isExcludelinktables() {
        return this.excludelinktables;
    }

    public void setFlushupdtables(boolean flushupdtables) {
        this.flushupdtables = flushupdtables;
    }

    public boolean isFlushupdtables() {
        return this.flushupdtables;
    }

    public boolean isResetsystables() {
        return this.resetsystables;
    }

    public void setResetsystables(boolean resetsystables) {
        this.resetsystables = resetsystables;
    }

    public void setForceUpdate(String forceUpdate) {
        this.forceUpdate = forceUpdate;
    }

    public boolean isForceUpdate() {
        return this.forceUpdate != null && (this.forceUpdate.equals("true") || this.forceUpdate.equals("Y"));
    }

    public void setForceNullUpdate(String forceNullUpdate) {
        this.forceNullUpdate = forceNullUpdate;
    }

    public boolean isForceNullUpdate() {
        return this.forceNullUpdate != null && (this.forceNullUpdate.equals("true") || this.forceNullUpdate.equals("Y"));
    }

    @Override
    public Object getParsedData() {
        return this.parsedData;
    }

    public void setCompCode(String compCode) {
        this.compCode = compCode;
    }

    @Override
    public void evalProperties(PropertyList props) {
        this.sdcid = TransferPackage.replaceProperties(this.sdcid, props);
        this.filename = TransferPackage.replaceProperties(this.filename, props);
        if (this.filename != null) {
            this.setFile(new File(this.filename));
        }
    }

    @Override
    public void export(File exportFile, PrintStream out, ZipOutputStream zipOut, DBAccess database, int level, Logger logger, Map exported) throws CloneNotSupportedException, SQLException, IOException, SapphireException {
        String[] sdclist;
        if (!(this.sdcid != null && this.sdcid.length() != 0 || this.categoryid != null && this.categoryid.length() != 0)) {
            throw new SapphireException("Sdcid or categoryid not specified for SDC export");
        }
        String level0 = StringUtil.repeat("\t", level);
        StringBuffer sdcInclause = new StringBuffer();
        if (this.sdcid != null && this.sdcid.length() > 0) {
            if (this.sdcid.equals("*")) {
                sdclist = new StringBuffer();
                database.createResultSet("SELECT sdcid FROM sdc");
                while (database.getNext()) {
                    sdcInclause.append(",'").append(database.getString("sdcid")).append("'");
                    sdclist.append(";").append(database.getString("sdcid"));
                }
                this.sdcid = sdclist.length() > 0 ? sdclist.substring(1) : "";
            } else {
                sdclist = StringUtil.split(this.sdcid, ";");
                for (int i = 0; i < sdclist.length; ++i) {
                    sdcInclause.append(",'").append(sdclist[i]).append("'");
                }
            }
        } else {
            sdclist = new StringBuffer();
            database.createResultSet("SELECT DISTINCT sdc.sdcid FROM sdc, categoryitem WHERE sdc.sdcid = categoryitem.keyid1 AND categoryitem.sdcid = 'SDC' AND categoryitem.categoryid IN ( '" + StringUtil.replaceAll(this.categoryid, ";", "','") + "' ) ORDER BY 1");
            while (database.getNext()) {
                sdcInclause.append(",'").append(database.getString("sdcid")).append("'");
                sdclist.append(";").append(database.getString("sdcid"));
            }
            String string = this.sdcid = sdclist.length() > 0 ? sdclist.substring(1) : "";
        }
        if (sdcInclause.length() > 0) {
            StringBuffer linktableInclause = new StringBuffer();
            if (!this.excludelinktables) {
                database.createResultSet("select linktableid from sdclink where sdcid IN (" + sdcInclause.substring(1) + ") AND linktype IN ( 'D', 'M' )");
                while (database.getNext()) {
                    linktableInclause.append(",'").append(database.getString("linktableid")).append("'");
                }
                database.createResultSet("select linktableid from sdcdetaillink where sdcid IN (" + sdcInclause.substring(1) + ") AND linktype IN ( 'D' )");
                while (database.getNext()) {
                    linktableInclause.append(",'").append(database.getString("linktableid")).append("'");
                }
            }
            logger.log("Exporting SDC" + (this.sdcid.indexOf(";") > -1 ? " list" : "") + ": " + this.sdcid + "...");
            out.println(level0 + "<sdc sdcid=\"" + this.sdcid + "\">");
            TableTransfer systableTable = new TableTransfer("systable");
            systableTable.setTablealias("updtable");
            systableTable.setFrom("systable");
            systableTable.setWhere("( systable.tableid IN ( SELECT sdc.tableid FROM sdc WHERE sdcid IN (" + sdcInclause.substring(1) + ") ) )" + (linktableInclause.length() > 0 ? " OR ( systable.tableid IN (" + linktableInclause.substring(1) + ") )" : ""));
            systableTable.setDefaultForceUpdate("true");
            systableTable.setVerbose(this.verbose);
            systableTable.setExportTableDefinition(this.exportTableDefinition);
            systableTable.setExcludeAuditColumns(true);
            systableTable.export(exportFile, out, zipOut, database, level + 1, logger, exported);
            TableTransfer syscolumnTable = new TableTransfer("syscolumn");
            syscolumnTable.setTablealias("updcolumn");
            syscolumnTable.setFrom("syscolumn");
            syscolumnTable.setWhere("( ( syscolumn.tableid IN ( SELECT sdc.tableid FROM sdc WHERE sdcid IN (" + sdcInclause.substring(1) + ") ) ) " + (linktableInclause.length() > 0 ? " OR ( syscolumn.tableid IN (" + linktableInclause.substring(1) + ") ) )" : ")") + (this.compCode.length() > 0 ? " AND ( LOWER( syscolumn.columnid ) LIKE '" + this.compCode.toLowerCase() + "_%' OR syscolumn.columnid NOT LIKE '___\\_%' ESCAPE '\\' )" : ""));
            syscolumnTable.setDefaultForceUpdate("true");
            syscolumnTable.setVerbose(this.verbose);
            syscolumnTable.setExportTableDefinition(this.exportTableDefinition);
            syscolumnTable.setExcludeAuditColumns(true);
            syscolumnTable.export(exportFile, out, zipOut, database, level + 1, logger, exported);
            TableTransfer syscolumnpropertyTable = new TableTransfer("syscolumnproperty");
            syscolumnpropertyTable.setTablealias("updcolumnproperty");
            syscolumnpropertyTable.setFrom("syscolumnproperty");
            syscolumnpropertyTable.setWhere("( ( syscolumnproperty.tableid IN ( SELECT sdc.tableid FROM sdc WHERE sdcid IN (" + sdcInclause.substring(1) + ") ) )" + (linktableInclause.length() > 0 ? " OR ( syscolumnproperty.tableid IN (" + linktableInclause.substring(1) + ") ) )" : ")") + (this.compCode.length() > 0 ? " AND ( LOWER( syscolumnproperty.columnid ) LIKE '" + this.compCode.toLowerCase() + "_%' OR syscolumnproperty.columnid NOT LIKE '___\\_%' ESCAPE '\\' )" : ""));
            syscolumnpropertyTable.setDefaultForceUpdate("true");
            syscolumnpropertyTable.setVerbose(this.verbose);
            syscolumnpropertyTable.setExportTableDefinition(this.exportTableDefinition);
            syscolumnpropertyTable.setExcludeAuditColumns(true);
            syscolumnpropertyTable.export(exportFile, out, zipOut, database, level + 1, logger, exported);
            TableTransfer sysrefTable = new TableTransfer("sysref");
            sysrefTable.setTablealias("updref");
            sysrefTable.setFrom("sysref");
            sysrefTable.setWhere("( sysref.tableid IN ( SELECT sdc.tableid FROM sdc WHERE sdcid IN (" + sdcInclause.substring(1) + ") ) )" + (linktableInclause.length() > 0 ? " OR ( sysref.tableid IN (" + linktableInclause.substring(1) + ") )" : ""));
            sysrefTable.setDefaultForceUpdate("true");
            sysrefTable.setVerbose(this.verbose);
            sysrefTable.setExportTableDefinition(this.exportTableDefinition);
            sysrefTable.export(exportFile, out, zipOut, database, level + 1, logger, exported);
            TableTransfer sysrefcolumnTable = new TableTransfer("sysrefcolumn");
            sysrefcolumnTable.setTablealias("updrefcolumn");
            sysrefcolumnTable.setFrom("sysrefcolumn");
            sysrefcolumnTable.setWhere("( ( sysrefcolumn.refid IN ( SELECT sysref.refid FROM sysref, sdc WHERE sdc.tableid = sysref.tableid AND sdcid IN (" + sdcInclause.substring(1) + ") ) )" + (linktableInclause.length() > 0 ? " OR ( sysrefcolumn.refid IN ( SELECT sysref.refid FROM sysref, sysrefcolumn WHERE sysref.refid = sysrefcolumn.refid AND sysref.tableid in (" + linktableInclause.substring(1) + ") ) ) )" : ")") + (this.compCode.length() > 0 ? " AND ( LOWER( sysrefcolumn.columnid ) LIKE '" + this.compCode.toLowerCase() + "_%' OR sysrefcolumn.columnid NOT LIKE '___\\_%' ESCAPE '\\' )" : ""));
            sysrefcolumnTable.setDefaultForceUpdate("true");
            sysrefcolumnTable.setVerbose(this.verbose);
            sysrefcolumnTable.setExportTableDefinition(this.exportTableDefinition);
            sysrefcolumnTable.export(exportFile, out, zipOut, database, level + 1, logger, exported);
            TableTransfer sysextendedcolumnTable = new TableTransfer("sysextendedcolumn");
            sysextendedcolumnTable.setFrom("sysextendedcolumn");
            sysextendedcolumnTable.setWhere("( ( sysextendedcolumn.tableid IN ( SELECT sdc.tableid FROM sdc WHERE sdcid IN (" + sdcInclause.substring(1) + ") ) ) " + (linktableInclause.length() > 0 ? " OR ( sysextendedcolumn.tableid IN (" + linktableInclause.substring(1) + ") ) )" : ")") + (this.compCode.length() > 0 ? " AND ( LOWER( sysextendedcolumn.columnid ) LIKE '" + this.compCode.toLowerCase() + "_%' OR sysextendedcolumn.columnid NOT LIKE '___\\_%' ESCAPE '\\' )" : ""));
            sysextendedcolumnTable.setDefaultForceUpdate("true");
            sysextendedcolumnTable.setVerbose(this.verbose);
            sysextendedcolumnTable.setExportTableDefinition(this.exportTableDefinition);
            sysextendedcolumnTable.export(exportFile, out, zipOut, database, level + 1, logger, exported);
            TableTransfer sdcTable = new TableTransfer("sdc");
            sdcTable.setWhere("sdcid IN (" + sdcInclause.substring(1) + ")");
            if (this.columns.size() > 0) {
                sdcTable.setColumns(this.columns);
            } else {
                sdcTable.setForceLOBExport(this.forceLOBExport);
                Column sdcTableColumn = new Column("*");
                sdcTableColumn.setForceUpdate(this.forceUpdate);
                sdcTableColumn.setForceNullUpdate(this.forceNullUpdate);
                sdcTable.addColumn(sdcTableColumn);
            }
            sdcTable.setExcludeAuditColumns(this.excludeAuditColumns);
            sdcTable.setVerbose(this.verbose);
            sdcTable.setExportTableDefinition(this.exportTableDefinition);
            sdcTable.export(exportFile, out, zipOut, database, level + 1, logger, exported);
            TableTransfer reftypeTable = new TableTransfer("reftype");
            reftypeTable.setFrom("reftype");
            reftypeTable.setWhere("reftypeid IN ( SELECT reftypeid FROM sdc WHERE sdcid IN ( " + sdcInclause.substring(1) + " ) AND reftypeid IS NOT NULL UNION SELECT reftypeid FROM sdclink WHERE sdcid IN ( " + sdcInclause.substring(1) + " ) UNION SELECT editreftypeid FROM attributedef WHERE basedonid IN ( " + sdcInclause.substring(1) + " ) )");
            reftypeTable.setDefaultForceUpdate(this.forceUpdate);
            reftypeTable.setExcludeAuditColumns(this.excludeAuditColumns);
            reftypeTable.setVerbose(this.verbose);
            reftypeTable.setExportTableDefinition(this.exportTableDefinition);
            reftypeTable.export(exportFile, out, zipOut, database, level + 1, logger, exported);
            TableTransfer refvalueTable = new TableTransfer("refvalue");
            refvalueTable.setFrom("refvalue");
            refvalueTable.setWhere("reftypeid IN ( SELECT reftypeid FROM sdc WHERE sdcid IN ( " + sdcInclause.substring(1) + " ) AND reftypeid IS NOT NULL UNION SELECT reftypeid FROM sdclink WHERE sdcid IN ( " + sdcInclause.substring(1) + " ) UNION SELECT editreftypeid FROM attributedef WHERE basedonid IN ( " + sdcInclause.substring(1) + " ) )");
            refvalueTable.setDefaultForceUpdate(this.forceUpdate);
            refvalueTable.setExcludeAuditColumns(this.excludeAuditColumns);
            refvalueTable.setVerbose(this.verbose);
            refvalueTable.setExportTableDefinition(this.exportTableDefinition);
            refvalueTable.export(exportFile, out, zipOut, database, level + 1, logger, exported);
            TableTransfer sdclinkTable = new TableTransfer("sdclink");
            sdclinkTable.setWhere("sdcid IN (" + sdcInclause.substring(1) + ")");
            sdclinkTable.setForceLOBExport(this.forceLOBExport);
            Column sdclinkTableColumn = new Column("*");
            sdclinkTableColumn.setForceUpdate(this.forceUpdate);
            sdclinkTableColumn.setForceNullUpdate(this.forceNullUpdate);
            sdclinkTable.addColumn(sdclinkTableColumn);
            sdclinkTable.setExcludeAuditColumns(this.excludeAuditColumns);
            sdclinkTable.setVerbose(this.verbose);
            sdclinkTable.setExportTableDefinition(this.exportTableDefinition);
            sdclinkTable.export(exportFile, out, zipOut, database, level + 1, logger, exported);
            TableTransfer sdcdetaillinkTable = new TableTransfer("sdcdetaillink");
            sdcdetaillinkTable.setWhere("sdcid IN (" + sdcInclause.substring(1) + ")");
            sdcdetaillinkTable.setForceLOBExport(this.forceLOBExport);
            Column sdcdetaillinkTableColumn = new Column("*");
            sdcdetaillinkTableColumn.setForceUpdate(this.forceUpdate);
            sdcdetaillinkTableColumn.setForceNullUpdate(this.forceNullUpdate);
            sdcdetaillinkTable.addColumn(sdclinkTableColumn);
            sdcdetaillinkTable.setExcludeAuditColumns(this.excludeAuditColumns);
            sdcdetaillinkTable.setVerbose(this.verbose);
            sdcdetaillinkTable.setExportTableDefinition(this.exportTableDefinition);
            sdcdetaillinkTable.export(exportFile, out, zipOut, database, level + 1, logger, exported);
            TableTransfer sdcpropertyTable = new TableTransfer("sdcproperty");
            sdcpropertyTable.setWhere("sdcid IN (" + sdcInclause.substring(1) + ")");
            sdcpropertyTable.setForceLOBExport(this.forceLOBExport);
            Column sdcpropertyTableColumn = new Column("*");
            sdcpropertyTableColumn.setForceUpdate(this.forceUpdate);
            sdcpropertyTableColumn.setForceNullUpdate(this.forceNullUpdate);
            sdcpropertyTable.addColumn(sdcpropertyTableColumn);
            sdcpropertyTable.setExcludeAuditColumns(this.excludeAuditColumns);
            sdcpropertyTable.setVerbose(this.verbose);
            sdcpropertyTable.setExportTableDefinition(this.exportTableDefinition);
            sdcpropertyTable.export(exportFile, out, zipOut, database, level + 1, logger, exported);
            TableTransfer sdcexportTable = new TableTransfer("sdcexport");
            sdcexportTable.setWhere("sdcid IN (" + sdcInclause.substring(1) + ")");
            sdcexportTable.setForceLOBExport(true);
            Column sdcexportTableColumn = new Column("*");
            sdcexportTableColumn.setForceUpdate(this.forceUpdate);
            sdcexportTableColumn.setForceNullUpdate(this.forceNullUpdate);
            sdcexportTable.addColumn(sdcexportTableColumn);
            sdcexportTable.setExcludeAuditColumns(this.excludeAuditColumns);
            sdcexportTable.setVerbose(this.verbose);
            sdcexportTable.setExportTableDefinition(this.exportTableDefinition);
            sdcexportTable.export(exportFile, out, zipOut, database, level + 1, logger, exported);
            TableTransfer sdcoperationTable = new TableTransfer("sdcoperation");
            sdcoperationTable.setWhere("sdcid IN (" + sdcInclause.substring(1) + ")");
            sdcoperationTable.setForceLOBExport(this.forceLOBExport);
            Column sdcoperationTableColumn = new Column("*");
            sdcoperationTableColumn.setForceUpdate(this.forceUpdate);
            sdcoperationTableColumn.setForceNullUpdate(this.forceNullUpdate);
            sdcoperationTable.addColumn(sdcoperationTableColumn);
            sdcoperationTable.setExcludeAuditColumns(this.excludeAuditColumns);
            sdcoperationTable.setVerbose(this.verbose);
            sdcoperationTable.setExportTableDefinition(this.exportTableDefinition);
            sdcoperationTable.export(exportFile, out, zipOut, database, level + 1, logger, exported);
            TableTransfer editorstyleTable = new TableTransfer("editorstyle");
            editorstyleTable.setFrom("editorstyle");
            editorstyleTable.setWhere("editorstyleid IN ( SELECT editorstyleid FROM attributedef WHERE basedonid IN ( " + sdcInclause.substring(1) + " ) AND editorstyleid IS NOT NULL )");
            editorstyleTable.setDefaultForceUpdate(this.forceUpdate);
            editorstyleTable.setExcludeAuditColumns(this.excludeAuditColumns);
            editorstyleTable.setVerbose(this.verbose);
            editorstyleTable.setExportTableDefinition(this.exportTableDefinition);
            editorstyleTable.export(exportFile, out, zipOut, database, level + 1, logger, exported);
            TableTransfer attributedefTable = new TableTransfer("attributedef");
            attributedefTable.setWhere("basedonid IN (" + sdcInclause.substring(1) + ")");
            attributedefTable.setForceLOBExport(true);
            Column attributedefTableColumn = new Column("*");
            attributedefTableColumn.setForceUpdate(this.forceUpdate);
            attributedefTableColumn.setForceNullUpdate(this.forceNullUpdate);
            attributedefTable.addColumn(attributedefTableColumn);
            attributedefTable.setExcludeAuditColumns(this.excludeAuditColumns);
            attributedefTable.setVerbose(this.verbose);
            attributedefTable.setExportTableDefinition(this.exportTableDefinition);
            attributedefTable.export(exportFile, out, zipOut, database, level + 1, logger, exported);
            TableTransfer categoryTable = new TableTransfer("category");
            categoryTable.setFrom("category");
            categoryTable.setOrderby("sdcid, categoryid");
            categoryTable.setDefaultForceUpdate(this.forceUpdate);
            categoryTable.setDefaultForceNullUpdate(this.forceNullUpdate);
            categoryTable.setExcludeAuditColumns(this.excludeAuditColumns);
            categoryTable.setVerbose(this.verbose);
            categoryTable.setExportTableDefinition(this.exportTableDefinition);
            TableTransfer categoryitemTable = new TableTransfer("categoryitem");
            categoryitemTable.setFrom("categoryitem");
            categoryitemTable.setOrderby("sdcid, categoryid");
            categoryitemTable.setDefaultForceUpdate(this.forceUpdate);
            categoryitemTable.setDefaultForceNullUpdate(this.forceNullUpdate);
            categoryitemTable.setExcludeAuditColumns(this.excludeAuditColumns);
            categoryitemTable.setVerbose(this.verbose);
            categoryitemTable.setExportTableDefinition(this.exportTableDefinition);
            if (database.isOracle()) {
                categoryTable.setWhere("sdcid = 'SDC' AND categoryid IN ( SELECT categoryid FROM categoryitem WHERE sdcid = 'SDC' AND keyid1 IN (" + sdcInclause.substring(1) + ") )");
                categoryitemTable.setWhere("sdcid = 'SDC' AND keyid1 IN (" + sdcInclause.substring(1) + ")");
            } else {
                categoryTable.setWhere("categoryid + ';' + sdcid IN ( SELECT categoryid + ';' + sdcid FROM categoryitem WHERE sdcid ='SDC' )");
                categoryitemTable.setWhere("sdcid = '" + this.sdcid + "' AND ( categoryitem.keyid1 ) IN (" + sdcInclause.substring(1) + ")");
            }
            categoryTable.export(exportFile, out, zipOut, database, level + 1, logger, exported);
            categoryitemTable.export(exportFile, out, zipOut, database, level + 1, logger, exported);
            out.println(level0 + "</sdc>");
        } else {
            logger.log("No SDCs match criteria!");
        }
    }

    @Override
    public boolean startElementImport(DBAccess database, String elementName, Properties attributes, Logger logger) throws SapphireException {
        if (elementName.equalsIgnoreCase("sdc")) {
            logger.log((this.parseOnly ? "Parsing" : "Importing") + " SDC: " + this.sdcid + "...");
            if (this.flushupdtables) {
                database.executeUpdate("DELETE FROM updrefcolumn");
                database.executeUpdate("DELETE FROM updref");
                database.executeUpdate("DELETE FROM updcolumnproperty");
                database.executeUpdate("DELETE FROM updcolumn");
                database.executeUpdate("DELETE FROM updtable");
            }
            if (!this.parseOnly && this.resetsystables) {
                SafeSQL safeSQL = new SafeSQL();
                database.executePreparedUpdate("UPDATE syscolumn SET columndesc = NULL, searchableflag = NULL WHERE tableid IN ( SELECT tableid FROM sdc WHERE sdcid in (" + safeSQL.addIn(this.sdcid, ";") + ") )", safeSQL.getValues());
            }
        } else {
            if (elementName.equalsIgnoreCase("table")) {
                this.currentTable = new TableTransfer(attributes.getProperty("tableid"));
                this.currentTable.setParseOnly(this.parseOnly);
                this.currentTable.setVerbose(this.verbose || attributes.getProperty("verbose") != null && attributes.getProperty("verbose").equals("true"));
                this.currentTable.setCommitScope(this.commitScope);
                this.currentTable.setIgnoreMissingObjects(this.ignoreMissingObjects);
                this.currentTable.setFile(this.file);
                this.currentTable.setZipFile(this.zipFile);
                this.currentTable.setZipFileEntry(this.zipFileEntry);
                this.currentTable.setImportForceUpdate(this.importForceUpdate);
                if (this.currentTable.getTableid().equalsIgnoreCase("updtable")) {
                    this.importedUpdTable = true;
                } else if (this.currentTable.getTableid().equalsIgnoreCase("updcolumn")) {
                    this.importedUpdColumn = true;
                } else if (this.currentTable.getTableid().equalsIgnoreCase("updcolumnproperty")) {
                    this.importedUpdColumnProperty = true;
                } else if (this.currentTable.getTableid().equalsIgnoreCase("updref")) {
                    this.importedUpdRef = true;
                } else if (this.currentTable.getTableid().equalsIgnoreCase("updrefcolumn")) {
                    this.importedUpdRefColumn = true;
                }
                return this.currentTable.startElementImport(database, elementName, attributes, logger);
            }
            if (this.currentTable == null || !this.currentTable.startElementImport(database, elementName, attributes, logger)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean endElementImport(DBAccess database, String elementName, String elementCharacters, boolean isCDATA, Logger logger) throws SapphireException {
        if (elementName.equalsIgnoreCase("sdc")) {
            if (!this.parseOnly) {
                try {
                    database.createResultSet("SELECT sdcid, tableid, auditedflag, auditpromptflag, accesscontrolledflag FROM sdc WHERE sdcid in ('" + StringUtil.replaceAll(this.sdcid, ";", "','") + "')");
                    while (database.getNext()) {
                        CallableStatement cs;
                        String callstmt;
                        String sdcid = database.getString("sdcid");
                        String auditedflag = database.getString("auditedflag");
                        if (auditedflag == null || auditedflag.length() == 0) {
                            auditedflag = "N";
                        }
                        if (!auditedflag.equals("N")) {
                            callstmt = "{call lv_audit" + (database.isOracle() ? "." : "_") + "sdcaudittables( ?, ? ) }";
                            cs = database.prepareCall(callstmt);
                            cs.setString(1, sdcid);
                            cs.setString(2, "Both");
                            cs.executeUpdate();
                            continue;
                        }
                        callstmt = "{call lv_audit" + (database.isOracle() ? "." : "_") + "sdcaudittables( ?, ? ) }";
                        cs = database.prepareCall(callstmt);
                        cs.setString(1, sdcid);
                        cs.setString(2, "Off");
                        cs.executeUpdate();
                    }
                    if (this.commitScope.equals("table")) {
                        logger.log("Committing imported sdcs");
                        boolean autoCommit = database.getConnection().getAutoCommit();
                        database.getConnection().setAutoCommit(false);
                        database.getConnection().commit();
                        database.getConnection().setAutoCommit(autoCommit);
                    }
                }
                catch (Exception e) {
                    throw new SapphireException("Failed to apply SDC options. Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionid())), e);
                }
            }
            logger.log((this.parseOnly ? "Parsed" : "Imported") + " SDC");
            this.parsedData = this.sdcid;
        } else {
            if (elementName.equalsIgnoreCase("table")) {
                if (this.currentTable.endElementImport(database, elementName, elementCharacters, isCDATA, logger)) {
                    if (!this.parseOnly && this.importedUpdTable && this.importedUpdColumn && this.importedUpdColumnProperty && this.importedUpdRef && this.importedUpdRefColumn && !this.syncDDTDone) {
                        this.syncUpd(database, logger);
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
        String level0 = StringUtil.repeat("\t", level);
        out.println(level0 + "<sdc" + (this.sdcid != null ? " sdcid=\"" + this.sdcid + "\"" : "") + (this.categoryid != null ? " categoryid=\"" + this.categoryid + "\"" : "") + (this.file != null ? " file=\"" + this.file.getName() + "\"" : "") + "/>");
    }

    private void syncUpd(DBAccess database, Logger logger) throws SapphireException {
        logger.log("Synchonizing data model...");
        database.executePreparedUpdate("{call lv_upd" + (database.isOracle() ? "." : "_") + "syncupd( ?, ? ) }", new Object[]{Build.getBuild(), "forceupd"});
        database.createResultSet("SELECT logtext FROM UPDLOG WHERE logtext = '" + Build.getBuild() + "'");
        if (database.getNext()) {
            if (database.isOracle()) {
                database.executePreparedUpdate("{call lv_util.recomp( ? ) }", new Object[]{"I"});
            }
        } else {
            throw new SapphireException("Upgrade of data model failed - check updlog table for errors.");
        }
        this.syncDDTDone = true;
    }

    public boolean isValid() {
        return this.sdcid != null && this.sdcid.length() > 0 || this.categoryid != null && this.categoryid.length() > 0;
    }
}

