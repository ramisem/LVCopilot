/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.xml;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.FileUtil;
import com.labvantage.sapphire.services.DDTConstants;
import com.labvantage.sapphire.services.DDTService;
import com.labvantage.sapphire.util.file.ZipFileUtil;
import com.labvantage.sapphire.xml.AbstractTransferable;
import com.labvantage.sapphire.xml.Column;
import com.labvantage.sapphire.xml.ExportXML;
import com.labvantage.sapphire.xml.ImportDirective;
import com.labvantage.sapphire.xml.Logger;
import com.labvantage.sapphire.xml.Node;
import com.labvantage.sapphire.xml.NodeList;
import com.labvantage.sapphire.xml.PropertyListTransfer;
import com.labvantage.sapphire.xml.PropertyTree;
import com.labvantage.sapphire.xml.PropertyTreeTransfer;
import com.labvantage.sapphire.xml.PropertyTreeUtil;
import com.labvantage.sapphire.xml.Table;
import com.labvantage.sapphire.xml.TransferConstants;
import com.labvantage.sapphire.xml.TransferPackage;
import com.labvantage.sapphire.xml.Transferable;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipOutputStream;
import sapphire.SapphireException;
import sapphire.util.DBAccess;
import sapphire.util.DataSet;
import sapphire.util.FormatUtil;
import sapphire.util.StringUtil;
import sapphire.xml.DOMUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class TableTransfer
extends AbstractTransferable
implements Transferable,
TransferConstants,
DDTConstants,
Cloneable {
    private String tableid;
    private String from;
    private String where;
    private String orderby;
    private String tablealias;
    private String linkid;
    private Column currentColumn;
    private File dataFile;
    private String data;
    private boolean rowsExist;
    private int rowCount;
    private HashMap preparedStatements;
    private StringBuffer updateKeyCols;
    private DataSet parsedKeys;
    private String defaultFilePattern = "[tableid]" + File.separator + "[rowkey]_[columnid].xml";
    private String defaultForceUpdate = "false";
    private String defaultForceNullUpdate = "false";
    private boolean exportTableDefWithNoData = true;
    private boolean versionStatusCheck;
    private boolean isVersionStatusCurrent = false;
    private boolean versionStatusForceUpdate;
    private boolean tableDef = false;
    private boolean ignoreTable;
    private boolean ignoreColumn;
    private boolean excludeSecurityColumns = true;
    private String sdikeyid = "";
    private ArrayList<String> altkeycolumns = new ArrayList();
    boolean isDevMode = false;
    String compCode = "";

    public TableTransfer(String tableid) {
        this.tableid = tableid;
    }

    public TableTransfer(String tableid, ArrayList columns) {
        this.tableid = tableid;
        this.columns = columns;
    }

    @Override
    public String getId() {
        return this.getTableid();
    }

    public String getTableid() {
        return this.tableid != null ? (!this.tableid.equals("*") ? this.tableid : "tables") : "";
    }

    public void setTableid(String tableid) {
        this.tableid = tableid;
    }

    public String getFrom() {
        return this.from != null && this.from.length() > 0 ? this.from : this.tableid;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getWhere() {
        return this.where != null ? this.where : "";
    }

    public void setWhere(String where) {
        this.where = where;
    }

    public String getOrderby() {
        return this.orderby != null ? this.orderby : "";
    }

    public void setOrderby(String orderby) {
        this.orderby = orderby;
    }

    public String getTablealias() {
        return this.tablealias;
    }

    public void setTablealias(String tablealias) {
        this.tablealias = tablealias;
    }

    public String getLinkid() {
        return this.linkid;
    }

    public void setLinkid(String linkid) {
        this.linkid = linkid;
    }

    public void setDataFile(File dataFile) {
        this.dataFile = dataFile;
    }

    public void setData(String data) {
        this.data = data;
    }

    public void setDefaultFilePattern(String defaultFilePattern) {
        this.defaultFilePattern = defaultFilePattern;
    }

    public void setDefaultForceUpdate(String defaultForceUpdate) {
        this.defaultForceUpdate = defaultForceUpdate;
    }

    public void setDefaultForceNullUpdate(String defaultForceNullUpdate) {
        this.defaultForceNullUpdate = defaultForceNullUpdate;
    }

    public void setExportTableDefWithNoData(boolean exportTableDefWithNoData) {
        this.exportTableDefWithNoData = exportTableDefWithNoData;
    }

    public void setExcludeSecurityColumns(boolean excludeSecurityColumns) {
        this.excludeSecurityColumns = excludeSecurityColumns;
    }

    public boolean isExcludeSecurityColumns() {
        return this.excludeSecurityColumns;
    }

    @Override
    public Object getParsedData() {
        return this.parsedKeys;
    }

    public void setSdikeyid(String sdikeyid) {
        this.sdikeyid = sdikeyid;
    }

    public void setAltkeycolumns(ArrayList<String> altkeycolumns) {
        this.altkeycolumns = altkeycolumns;
    }

    @Override
    public void evalProperties(PropertyList props) {
        this.from = TransferPackage.replaceProperties(this.from, props);
        this.where = TransferPackage.replaceProperties(this.where, props);
        this.orderby = TransferPackage.replaceProperties(this.orderby, props);
        this.filename = TransferPackage.replaceProperties(this.filename, props);
        if (this.filename != null) {
            this.setFile(new File(this.filename));
        }
        this.keyFilename = TransferPackage.replaceProperties(this.keyFilename, props);
        this.keyseparator = TransferPackage.replaceProperties(this.keyseparator, props);
        this.defaultForceUpdate = TransferPackage.replaceProperties(this.defaultForceUpdate, props);
        this.defaultForceNullUpdate = TransferPackage.replaceProperties(this.defaultForceNullUpdate, props);
        for (int i = 0; i < this.columns.size(); ++i) {
            Column column = (Column)this.columns.get(i);
            column.setValue(TransferPackage.replaceProperties(column.getValue(), props));
            column.setForceUpdate(TransferPackage.replaceProperties(column.getForceUpdate(), props));
            column.setForceNullUpdate(TransferPackage.replaceProperties(column.getForceNullUpdate(), props));
        }
    }

    public void exportTables(File exportFile, PrintStream out, ZipOutputStream zipOut, DBAccess database, int level, Logger logger) throws SapphireException, IOException, CloneNotSupportedException, SQLException {
        LinkedHashMap exported = new LinkedHashMap();
        String sql = database.isOracle() ? "SELECT table_name FROM " + (this.from != null && this.from.length() > 0 ? this.from : "user_tables") + (this.where != null && this.where.length() > 0 ? " WHERE " + this.where : "") + (this.orderby != null && this.orderby.length() > 0 ? " ORDER BY " + this.orderby : " ORDER BY table_name") : "SELECT name FROM " + (this.from != null && this.from.length() > 0 ? this.from : "sysobjects") + (this.where != null && this.where.length() > 0 ? " WHERE xtype='U' AND " + this.where : " WHERE xtype='U' ") + (this.orderby != null && this.orderby.length() > 0 ? " ORDER BY " + this.orderby : " ORDER BY name");
        database.createResultSet("tables", sql);
        while (database.getNext("tables")) {
            TableTransfer table = new TableTransfer(database.getString("tables", database.isOracle() ? "table_name" : "name").toLowerCase());
            table.setTransferOption("forcedevmode", this.getTransferOption("forcedevmode"));
            table.setDefaultForceUpdate(this.defaultForceUpdate);
            table.setDefaultForceNullUpdate(this.defaultForceNullUpdate);
            table.setForceLOBExport(this.forceLOBExport);
            table.setExportTableDefinition(this.exportTableDefinition);
            table.setExcludeAuditColumns(this.excludeAuditColumns);
            table.export(exportFile, out, zipOut, database, level, logger, exported);
        }
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public void export(File exportFile, PrintStream out, ZipOutputStream zipOut, DBAccess database, int level, Logger logger, Map exported) throws SapphireException, IOException, CloneNotSupportedException, SQLException {
        Column column;
        int j;
        if (this.tableid == null || this.tableid.length() == 0) {
            throw new SapphireException("Tableid not specified for table export");
        }
        if (this.tableid.equals("*")) {
            this.exportTables(exportFile, out, zipOut, database, level, logger);
            return;
        }
        String level0 = StringUtil.repeat("\t", level);
        String level1 = StringUtil.repeat("\t", level + 1);
        String level2 = StringUtil.repeat("\t", level + 2);
        long start = System.currentTimeMillis();
        logger.log(level0 + "Exporting TABLE " + this.getTableid());
        switch (this.getTransferOption("forcedevmode").length()) {
            case 0: {
                this.isDevMode = database.checkExists("SELECT propertyvalue FROM sysconfig WHERE propertyid = 'devmode' AND propertyvalue = 'Y'");
                database.createResultSet("GetCompCode", "SELECT propertyvalue FROM sysconfig WHERE propertyid='compcode'");
                if (!database.getNext("GetCompCode")) break;
                this.compCode = database.getValue("GetCompCode", "propertyvalue");
                break;
            }
            case 1: {
                this.isDevMode = this.getTransferOption("forcedevmode").equals("Y");
                break;
            }
            default: {
                this.compCode = this.getTransferOption("forcedevmode");
            }
        }
        boolean columnsDefined = true;
        if (this.columns.size() == 0) {
            columnsDefined = false;
            Column newCol = new Column();
            newCol.setColumnid("*");
            newCol.setForceUpdate(this.defaultForceUpdate);
            newCol.setForceNullUpdate(this.defaultForceNullUpdate);
            this.columns.add(newCol);
        }
        for (j = 0; j < this.columns.size(); ++j) {
            column = (Column)this.columns.get(j);
            if (!column.getColumnid().equals("*")) continue;
            this.columns.remove(j);
            StringBuffer columnids = new StringBuffer();
            for (int k = 0; k < this.columns.size(); ++k) {
                columnids.append(";").append(((Column)this.columns.get(k)).getColumnid()).append(";");
            }
            database.createPreparedResultSet("columns", "SELECT columnid, datatype FROM syscolumn WHERE tableid=?" + (this.forceLOBExport ? "" : " AND datatype <> 'T' AND datatype <> 'B'") + " ORDER BY columnsequence desc", new Object[]{this.tableid});
            while (database.getNext("columns")) {
                Column newcol = (Column)column.clone();
                String columnid = database.getString("columns", "columnid");
                if (columnids.indexOf(";" + columnid + ";") != -1) continue;
                newcol.setColumnid(columnid);
                if (database.getString("columns", "datatype").equals("T") || database.getString("columns", "datatype").equals("B")) {
                    newcol.setFile(this.defaultFilePattern);
                }
                this.columns.add(j, newcol);
            }
            break;
        }
        for (j = this.columns.size() - 1; j >= 0; --j) {
            column = (Column)this.columns.get(j);
            if (!(column.isExcluded() || this.excludeAuditColumns && column.isAuditColumn() || this.excludeSecurityColumns && column.isSecurityColumn() || !columnsDefined && this.getTableid().equals("taskdef") && column.getColumnid().equals("taskdefoverride") && this.isDevMode || !columnsDefined && this.getTableid().equals("webpagepropertytree") && column.getColumnid().equals("componentvaluetree") && this.isDevMode) && (columnsDefined || !this.getTableid().equals("webpagepropertytree") || !column.getColumnid().equals("valuetree") || !this.isDevMode) || this.getTableid().equals("tracelog") && column.getColumnid().equals("tracelogid")) continue;
            this.columns.remove(j);
        }
        if (this.columns.size() == 0) {
            throw new SapphireException("No columns defined for exporting table '" + this.tableid + "'");
        }
        StringBuffer tableOutput = new StringBuffer();
        tableOutput.append(level0 + "<table tableid=\"" + (this.getTablealias() != null && this.getTablealias().length() > 0 ? this.getTablealias() : this.getTableid()) + "\">");
        String[] keys = DDTService.getKeyColumns(database, this.tableid);
        int keycols = keys.length;
        if (this.exportTableDefinition) {
            tableOutput.append("\n" + level1 + "<tabledef tableid=\"" + (this.getTablealias() != null && this.getTablealias().length() > 0 ? this.getTablealias() : this.getTableid()) + "\"" + (this.getLinkid() != null && this.getLinkid().length() > 0 ? " linkid=\"" + this.getLinkid() + "\"" : "") + ">\n");
        }
        database.createPreparedResultSet("SELECT * FROM syscolumn WHERE tableid = ? ORDER BY columnsequence", new Object[]{this.getTableid()});
        block8: while (database.getNext()) {
            int i;
            String syscolumnid = database.getString("columnid");
            int keyPos = 0;
            for (i = 0; i < keycols; ++i) {
                if (!keys[i].equals(syscolumnid)) continue;
                keyPos = i + 1;
                break;
            }
            for (i = 0; i < this.columns.size(); ++i) {
                String columnLength;
                Column column2 = (Column)this.columns.get(i);
                if (!column2.getColumnid().equals(syscolumnid)) continue;
                if (keyPos >= 1) {
                    column2.setKeyPos(keyPos);
                }
                if ((columnLength = database.getString("columnlength")) == null || columnLength.equals("") || columnLength.equals("null")) {
                    columnLength = "0";
                }
                column2.setDDTDefinition(database.getString("datatype"), database.getString("pkflag"), database.getString("nnflag"), columnLength, database.getString("columntype"));
                if (!this.exportTableDefinition) continue block8;
                tableOutput.append(level2 + "<coldef columnid=\"" + (column2.getColumnalias() != null && column2.getColumnalias().length() > 0 ? column2.getColumnalias() : syscolumnid) + "\" datatype=\"" + column2.getDatatype() + "\" pkflag=\"" + (column2.isPrimarykey() ? "Y" : "N") + "\" nnflag=\"" + (column2.isNotnull() ? "Y" : "N") + "\" columnlength=\"" + columnLength + "\" columntype=\"" + column2.getColumntype() + "\"/>\n");
                continue block8;
            }
        }
        if (this.exportTableDefinition) {
            tableOutput.append(level1 + "</tabledef>");
        }
        BufferedReader in = null;
        String aLine = "";
        FormatUtil formatUtil = FormatUtil.getInstance();
        boolean moreData = true;
        boolean databaseExtract = true;
        if (this.dataFile == null && this.data == null && this.keyFilename == null) {
            StringBuffer select = new StringBuffer();
            StringBuffer defOrder = new StringBuffer();
            for (int j2 = 0; j2 < this.columns.size(); ++j2) {
                Column column3 = (Column)this.columns.get(j2);
                select.append(", ").append(this.getTableid()).append(".").append(column3.getColumnid());
                if (!column3.isPrimarykey()) continue;
                defOrder.append(", ").append(this.getTableid()).append(".").append(column3.getColumnid());
            }
            String where = this.getWhere();
            String orderby = this.getOrderby();
            String sql = "SELECT " + select.substring(1) + " FROM " + this.getFrom() + (where.length() > 0 ? " WHERE " + where : "") + " ORDER BY " + (orderby.length() > 0 ? orderby : defOrder.substring(1));
            if (this.verbose) {
                logger.log(level1 + "Export SQL: " + sql);
            }
            database.createResultSet(sql);
            moreData = database.getNext();
        } else {
            if (this.data == null) {
                FileReader fr = new FileReader(this.keyFilename != null ? new File(this.keyFilename) : this.dataFile);
                in = new BufferedReader(fr);
            } else {
                StringReader sr = new StringReader(this.data);
                in = new BufferedReader(sr);
            }
            aLine = in.readLine();
            moreData = aLine != null;
            databaseExtract = false;
        }
        boolean headerWritten = false;
        if (moreData || this.exportTableDefWithNoData) {
            out.println(tableOutput);
            headerWritten = true;
        }
        int rows = 0;
        while (moreData) {
            StringBuffer rowKey = new StringBuffer();
            StringBuffer rowWhere = new StringBuffer();
            HashMap<String, Object> rowData = new HashMap<String, Object>();
            if (databaseExtract) {
                this.getDatabaseRowData(rowData, database, rowKey, rowWhere);
            } else if (aLine.trim().length() > 0 && !aLine.trim().startsWith("//")) {
                String[] lineData = StringUtil.split(aLine.trim(), this.keyseparator);
                if (this.keyFilename != null) {
                    StringBuffer select = new StringBuffer();
                    StringBuffer where = new StringBuffer();
                    for (int i = 0; i < this.columns.size(); ++i) {
                        Column col = (Column)this.columns.get(i);
                        String columnid = col.getColumnid();
                        select.append("," + columnid);
                        if (!col.isPrimarykey()) continue;
                        where.append(" AND " + columnid + "='" + lineData[i] + "'");
                    }
                    database.createResultSet("SELECT " + select.substring(1) + " FROM " + this.tableid + " WHERE " + where.substring(5));
                    if (!database.getNext()) throw new SapphireException("Key manifest entry identified by " + where.substring(5) + " not found!");
                    this.getDatabaseRowData(rowData, database, rowKey, rowWhere);
                } else {
                    for (int i = 0; i < this.columns.size(); ++i) {
                        Column col = (Column)this.columns.get(i);
                        String columnid = col.getColumnid();
                        if (i >= lineData.length) continue;
                        if (col.getDatatype().equals("C")) {
                            rowData.put(columnid, lineData[i].trim());
                        } else if (col.getDatatype().equals("N") || col.getDatatype().equals("R")) {
                            rowData.put(columnid, formatUtil.parseBigDecimal(lineData[i].trim()));
                        } else {
                            if (!col.getDatatype().equals("D")) throw new SapphireException(col.getDatatype() + " datatype not supported in this mode");
                            rowData.put(columnid, lineData[i].trim());
                            rowData.put(columnid + "_ts", new DateTimeUtil().getTimestamp(lineData[i].trim()));
                        }
                        if (!col.isPrimarykey()) continue;
                        rowKey.append("_").append(rowData.get(columnid));
                        rowWhere.append(" AND ").append(columnid).append(" = '").append(rowData.get(columnid)).append("'");
                    }
                }
            }
            if (rowKey.length() > 0) {
                if (this.verbose) {
                    logger.log(level1 + "Exporting " + this.tableid + " row keyed by " + rowKey.substring(1));
                }
                this.exportRow(rowData, rowKey, exportFile, out, zipOut, database, level, logger, exported);
                ++rows;
            }
            if (databaseExtract) {
                PropertyTreeTransfer propertytree;
                Node node;
                String extendnodeid;
                String propertytreeid;
                if (this.tableid.equalsIgnoreCase("webpagepropertytree") && this.getTransferOption("includenodes", "N").equals("Y")) {
                    propertytreeid = (String)rowData.get("propertytreeid");
                    extendnodeid = (String)rowData.get("extendnodeid");
                    if (!"Y".equals(this.getTransferOption("excludegenericlayout")) || !propertytreeid.equals("Generic")) {
                        node = new Node(extendnodeid);
                        node.setIncludeAncestors(true);
                        node.setExists(this.getTransferOption("nodeexists", "ignore"));
                        node.setNotexists("add");
                        if (node.getExists().equals("merge")) {
                            PropertyListTransfer plt = new PropertyListTransfer();
                            plt.setExists(this.getTransferOption("nodepropertyexists", "replace"));
                            node.setPropertyList(plt);
                        }
                        NodeList nodes = new NodeList();
                        nodes.add(node);
                        propertytree = new PropertyTreeTransfer(propertytreeid);
                        propertytree.setTransferOption("forcedevmode", this.getTransferOption("forcedevmode"));
                        propertytree.setNodeList(nodes);
                        this.addReferencedItem(propertytree);
                    }
                } else if (this.tableid.equalsIgnoreCase("taskdefstep") && this.getTransferOption("includestepdefs", "N").equals("Y")) {
                    propertytreeid = (String)rowData.get("propertytreeid");
                    extendnodeid = (String)rowData.get("extendnodeid");
                    node = new Node(extendnodeid);
                    node.setIncludeAncestors(true);
                    node.setExists(this.getTransferOption("nodeexists", "ignore"));
                    node.setNotexists("add");
                    NodeList nodes = new NodeList();
                    nodes.add(node);
                    propertytree = new PropertyTreeTransfer(propertytreeid);
                    propertytree.setTransferOption("forcedevmode", this.getTransferOption("forcedevmode"));
                    propertytree.setNodeList(nodes);
                    this.addReferencedItem(propertytree);
                }
                moreData = database.getNext();
                continue;
            }
            aLine = in.readLine();
            moreData = aLine != null;
        }
        if (in != null) {
            in.close();
        }
        if (headerWritten) {
            out.println(level0 + "</table>");
        }
        logger.log(level0 + "Exporting " + rows + " rows from " + this.getTableid() + " took " + (System.currentTimeMillis() - start) + "ms");
    }

    private void getDatabaseRowData(HashMap rowData, DBAccess database, StringBuffer rowKey, StringBuffer rowWhere) throws SapphireException {
        for (int i = 0; i < this.columns.size(); ++i) {
            Column col = (Column)this.columns.get(i);
            String columnid = col.getColumnid();
            if (col.getDatatype().equals("C")) {
                rowData.put(columnid, database.getString(columnid));
            } else if (col.getDatatype().equals("N") || col.getDatatype().equals("R")) {
                rowData.put(columnid, database.getBigDecimal(col.getColumnid()));
            } else if (col.getDatatype().equals("D")) {
                rowData.put(columnid, database.getString(columnid));
                rowData.put(columnid + "_ts", database.getTimestamp(columnid));
            } else if (col.getDatatype().equals("T")) {
                rowData.put(columnid, database.getClob(columnid));
            } else if (col.getDatatype().equals("B")) {
                rowData.put(columnid, database.getBlob(columnid));
            }
            if (!col.isPrimarykey()) continue;
            rowKey.append("_").append(rowData.get(columnid));
            rowWhere.append(" AND ").append(columnid).append(" = '").append(rowData.get(columnid)).append("'");
        }
    }

    private void exportRow(HashMap rowData, StringBuffer rowKey, File exportFile, PrintStream out, ZipOutputStream zipOut, DBAccess database, int level, Logger logger, Map exported) throws CloneNotSupportedException, SapphireException, IOException, SQLException {
        String level1 = StringUtil.repeat("\t", level + 1);
        String level2 = StringUtil.repeat("\t", level + 2);
        boolean forceRowUpdate = false;
        StringBuffer row = new StringBuffer();
        for (int j = 0; j < this.columns.size(); ++j) {
            Blob blob;
            FileOutputStream colFos;
            File colFile;
            String forceNullUpdate;
            Column column = (Column)this.columns.get(j);
            String columnid = column.getColumnid();
            if (column.getDatatype() == null) {
                throw new SapphireException("Column '" + columnid + "' not not found in DDT definition (syscolumn)!");
            }
            String forceUpdate = column.isForceUpdate() ? "Y" : "N";
            String string = forceNullUpdate = column.isForceNullUpdate() ? "Y" : "N";
            if (columnid.equals("auditsequence") || columnid.equals("tracelogid")) {
                forceUpdate = "N";
                forceNullUpdate = "N";
            }
            String value = column.getValue();
            if (column.hasValueTokens()) {
                String[] tokens = column.getValueTokens();
                for (int k = 0; k < tokens.length; ++k) {
                    value = StringUtil.replaceAll(value, "[" + tokens[k] + "]", (String)rowData.get(tokens[k]), false);
                }
            }
            String file = column.getFile();
            if (column.hasFileTokens()) {
                String[] tokens = column.getFileTokens();
                for (int k = 0; k < tokens.length; ++k) {
                    file = tokens[k].equals("columnid") ? StringUtil.replaceAll(file, "[" + tokens[k] + "]", columnid, false) : (tokens[k].equals("tableid") ? StringUtil.replaceAll(file, "[" + tokens[k] + "]", this.tableid, false) : (tokens[k].equals("rowkey") ? StringUtil.replaceAll(file, "[" + tokens[k] + "]", ExportXML.removeIllegalChars(rowKey.substring(1)), false) : StringUtil.replaceAll(file, "[" + tokens[k] + "]", (String)rowData.get(tokens[k]), false)));
                }
            }
            if (forceUpdate.equals("Y") || forceNullUpdate.equals("Y")) {
                forceRowUpdate = true;
            }
            String outputValue = "";
            String filetype = "";
            String filetypeid = "";
            if ("C".equals(column.getDatatype())) {
                String strvalue;
                String string2 = strvalue = value != null ? value : (String)rowData.get(columnid);
                if (strvalue != null) {
                    outputValue = DOMUtil.convertChars(strvalue);
                }
            } else if ("N".equals(column.getDatatype()) || "R".equals(column.getDatatype())) {
                BigDecimal numvalue;
                BigDecimal bigDecimal = numvalue = value != null && value.length() > 0 ? new BigDecimal(value) : (BigDecimal)rowData.get(columnid);
                if (numvalue != null) {
                    outputValue = numvalue.toString();
                }
            } else if ("D".equals(column.getDatatype())) {
                String temptimestamp;
                String string3 = temptimestamp = value != null && value.length() > 0 ? value : (String)rowData.get(columnid);
                if (temptimestamp != null) {
                    DateTimeUtil dtu = new DateTimeUtil();
                    outputValue = value != null && value.length() > 0 ? dtu.getTimestamp(value).toString() : ((Timestamp)rowData.get(columnid + "_ts")).toString();
                }
            } else if ("T".equals(column.getDatatype())) {
                String clobvalue;
                String string4 = clobvalue = value != null && value.length() > 0 ? value : (String)rowData.get(columnid);
                if (clobvalue != null && clobvalue.length() > 0) {
                    if (file != null && file.length() > 0) {
                        outputValue = "[" + file + "]";
                        colFile = new File(exportFile.getParentFile(), file);
                        colFile.getParentFile().mkdirs();
                        colFos = null;
                        PrintStream colOut = null;
                        logger.log(level2 + "Column " + columnid + " written to " + colFile);
                        if (column.getPropertyTreeTransfer() != null) {
                            ExportXML exportXML;
                            String explode;
                            PropertyTreeTransfer propertyTreeTransfer = (PropertyTreeTransfer)column.getPropertyTreeTransfer().clone();
                            String[] tokens = StringUtil.getTokens(propertyTreeTransfer.getPropertyTreeId());
                            if (tokens.length > 0) {
                                String propertyTreeId = propertyTreeTransfer.getPropertyTreeId();
                                for (int k = 0; k < tokens.length; ++k) {
                                    propertyTreeId = StringUtil.replaceAll(propertyTreeId, "[" + tokens[k] + "]", (String)rowData.get(tokens[k]), false);
                                }
                                propertyTreeTransfer.setId(propertyTreeId);
                            }
                            if ((explode = propertyTreeTransfer.getExplode()) == null || explode.length() == 0) {
                                colFos = new FileOutputStream(colFile);
                                colOut = new PrintStream((OutputStream)colFos, true, "UTF-8");
                                propertyTreeTransfer.export(colFile, colOut, zipOut, database, level + 3, logger, exported);
                                filetype = "propertytree";
                                filetypeid = propertyTreeTransfer.getId();
                            } else if (explode.equalsIgnoreCase("nodes")) {
                                PropertyTree pt = PropertyTreeUtil.getPropertyTree(database, propertyTreeTransfer.getPropertyTreeId());
                                exportXML = new ExportXML();
                                exportXML.setDatabase(database);
                                exportXML.setResetDatabase(false);
                                exportXML.setExportLog(logger);
                                exportXML.setExcludeExportAttributes(this.excludeExportAttributes);
                                ArrayList nodes = pt.getAllNodes();
                                for (int i = -1; i < nodes.size(); ++i) {
                                    Node node = i == -1 ? new Node("root") : (Node)nodes.get(i);
                                    String nodeid = node.getNodeId();
                                    NodeList nodeList = new NodeList();
                                    nodeList.add(node);
                                    PropertyTreeTransfer ptTransfer = new PropertyTreeTransfer(propertyTreeTransfer.getPropertyTreeId());
                                    ptTransfer.setTransferOption("forcedevmode", this.getTransferOption("forcedevmode"));
                                    ptTransfer.setNodeList(nodeList);
                                    ptTransfer.setFile(new File(colFile.getParentFile(), nodeid + ".xml"));
                                    if (i == -1) {
                                        ptTransfer.setExplode("root");
                                        ptTransfer.setPropertyDefaultList(pt.getPropertyDefaultList());
                                        ptTransfer.setPropertyDefinitionList(pt.getPropertyDefinitionList());
                                    }
                                    exportXML.addExport(ptTransfer);
                                }
                                if (exportXML.getExports().size() > 0) {
                                    exportXML.export();
                                }
                            } else if (explode.equalsIgnoreCase("definition")) {
                                PropertyTree pt = PropertyTreeUtil.getPropertyTree(database, propertyTreeTransfer.getPropertyTreeId());
                                exportXML = new ExportXML();
                                exportXML.setDatabase(database);
                                exportXML.setResetDatabase(false);
                                exportXML.setExportLog(logger);
                                exportXML.setExcludeExportAttributes(this.excludeExportAttributes);
                                PropertyTreeTransfer ptTransfer = new PropertyTreeTransfer(propertyTreeTransfer.getPropertyTreeId());
                                ptTransfer.setTransferOption("forcedevmode", this.getTransferOption("forcedevmode"));
                                ptTransfer.setPropertyDefinitionList(pt.getPropertyDefinitionList());
                                ptTransfer.setPropertyDependencyList(pt.getPropertyDependencyList());
                                ptTransfer.setFile(new File(colFile.getParentFile(), propertyTreeTransfer.getPropertyTreeId() + ".xml"));
                                ptTransfer.setExplode("definition");
                                exportXML.addExport(ptTransfer);
                                exportXML.export();
                            }
                        } else if (column.getPropertyListTransfer() != null) {
                            colFos = new FileOutputStream(colFile);
                            colOut = new PrintStream((OutputStream)colFos, true, "UTF-8");
                            PropertyListTransfer propertyListTransfer = (PropertyListTransfer)column.getPropertyListTransfer().clone();
                            propertyListTransfer.setId(rowKey.substring(1));
                            propertyListTransfer.export(colFile, colOut, zipOut, database, level + 3, logger, exported, clobvalue);
                            filetype = "propertylist";
                            filetypeid = propertyListTransfer.getId();
                        } else {
                            colFos = new FileOutputStream(colFile);
                            colOut = new PrintStream((OutputStream)colFos, true, "UTF-8");
                            colOut.print(clobvalue);
                        }
                        if (colOut != null) {
                            colOut.close();
                            colFos.close();
                        }
                        if (zipOut != null) {
                            ZipFileUtil.addEntry(exportFile.getParentFile(), colFile, zipOut);
                            colFile.delete();
                            if (colFile.getParentFile().list().length == 0) {
                                colFile.getParentFile().delete();
                            }
                        }
                    } else {
                        outputValue = "<![CDATA[" + clobvalue + "]]>";
                    }
                }
            } else if ("B".equals(column.getDatatype()) && (blob = (Blob)rowData.get(columnid)) != null && blob.length() > 0L) {
                if (file != null && file.length() > 0) {
                    int lengthread;
                    outputValue = "[" + file + "]";
                    colFile = new File(exportFile.getParentFile(), file);
                    colFile.getParentFile().mkdirs();
                    colFos = new FileOutputStream(colFile);
                    InputStream in = blob.getBinaryStream();
                    byte[] bytebuff = new byte[500];
                    while ((lengthread = in.read(bytebuff)) != -1) {
                        colFos.write(bytebuff, 0, lengthread);
                    }
                    in.close();
                    colFos.close();
                    if (zipOut != null) {
                        ZipFileUtil.addEntry(exportFile.getParentFile(), colFile, zipOut);
                        colFile.delete();
                        if (colFile.getParentFile().list().length == 0) {
                            colFile.getParentFile().delete();
                        }
                    }
                } else {
                    throw new SapphireException("File attribute not specified for blob column '" + columnid + "'");
                }
            }
            row.append(level2).append("<col name=\"").append(column.getColumnalias() != null && column.getColumnalias().length() > 0 ? column.getColumnalias() : columnid).append("\"").append(forceUpdate.equals("Y") ? " forceupdate=\"" + forceUpdate + "\"" : "").append(forceNullUpdate.equals("Y") ? " forcenullupdate=\"" + forceNullUpdate + "\"" : "").append(filetype.length() > 0 ? " filetype=\"" + filetype + "\" filetypeid=\"" + filetypeid + "\"" : "").append(">").append(outputValue).append("</col>\n");
        }
        out.println(level1 + "<row" + (forceRowUpdate ? " forceupdate=\"Y\"" : "") + ">");
        out.print(row.toString());
        out.println(level1 + "</row>");
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public boolean startElementImport(DBAccess database, String elementName, Properties attributes, Logger logger) throws SapphireException {
        if (elementName.equalsIgnoreCase("table")) {
            this.ignoreTable = false;
            logger.log((this.parseOnly ? "Parsing" : "Importing") + " TABLE: " + this.tableid + "...");
            this.rowCount = 0;
            if (this.parseOnly) {
                this.parsedKeys = new DataSet();
            }
            if (this.importTarget != 0 && this.importTarget != 2) return true;
            this.preparedStatements = new HashMap();
            database.createPreparedResultSet("select tableid from systable where tableid = ?", new Object[]{this.tableid});
            if (database.getNext()) {
                database.createResultSet("select count(*) \"count\" from " + this.tableid);
                database.getNext();
                this.rowsExist = database.getInt("count") > 0;
                database.closeResultSet();
                return true;
            } else {
                if (!this.ignoreMissingObjects) throw new SapphireException("Table " + this.tableid + " not found in data dictionary");
                this.ignoreTable = true;
                logger.log("Table " + this.tableid + " not found in data dictionary - IGNORING");
            }
            return true;
        } else if (elementName.equalsIgnoreCase("tabledef")) {
            if (this.ignoreTable) return true;
            this.tableDef = true;
            if (this.verbose) {
                logger.log((this.parseOnly ? "Parsing" : "Importing") + " table definition...");
            }
            this.linkid = attributes.getProperty("linkid");
            return true;
        } else if (elementName.equalsIgnoreCase("coldef")) {
            if (this.ignoreTable) return true;
            Column column = new Column(attributes.getProperty("columnid"));
            column.setDDTDefinition(attributes.getProperty("datatype"), attributes.getProperty("pkflag"), attributes.getProperty("nnflag"), attributes.getProperty("columnlength"), attributes.getProperty("columntype"));
            this.columns.add(column);
            return true;
        } else if (elementName.equalsIgnoreCase("row")) {
            if (this.ignoreTable) return true;
            if (!this.tableDef) {
                if (this.columns.size() == 0) {
                    this.processDefinition(database, logger, true);
                }
                this.tableDef = true;
            }
            ++this.rowCount;
            if (this.verbose) {
                logger.log((this.parseOnly ? "Parsing" : "Importing") + " row " + this.rowCount + "...");
            }
            for (int i = 0; i < this.columns.size(); ++i) {
                Column column = (Column)this.columns.get(i);
                column.clearValue();
                if (column.getDefaultvalue() == null || column.getDefaultvalue().length() <= 0) continue;
                column.setValue(column.getDefaultvalue());
            }
            return true;
        } else {
            Column column;
            if (!elementName.equalsIgnoreCase("col")) return false;
            if (this.ignoreTable) return true;
            this.ignoreColumn = false;
            this.currentColumn = column = this.getColumn(attributes.getProperty("name"));
            if (column == null) {
                if (!this.ignoreMissingObjects) throw new SapphireException("Column " + attributes.getProperty("name") + " not defined in COLDEF section for table " + this.tableid);
                this.ignoreColumn = true;
                logger.log("Column " + attributes.getProperty("name") + " not defined in COLDEF section for table " + this.tableid + " - IGNORING");
                return true;
            } else {
                PreparedStatement ps;
                column.setUpdateDefinition(this.importForceUpdate ? "true" : attributes.getProperty("forceupdate"), this.importForceUpdate ? "false" : attributes.getProperty("forcenullupdate"));
                column.setFiletype(attributes.getProperty("filetype"), attributes.getProperty("filetypeid"));
                if (this.importTarget != 0 || !column.isForceNullUpdate() || (ps = (PreparedStatement)this.preparedStatements.get("isnull_" + column.getColumnid())) != null) return true;
                String isnull = "SELECT 1 FROM " + this.tableid + " WHERE " + (database.isOracle() ? "( length( " + column.getColumnid() + ") = 0 or " + column.getColumnid() + " is null ) AND " + this.updateKeyCols : "( datalength(" + column.getColumnid() + ") = 0 or " + column.getColumnid() + " is null ) AND " + this.updateKeyCols);
                ps = database.prepareStatement("isnull_" + column.getColumnid(), isnull);
                this.preparedStatements.put("isnull_" + column.getColumnid(), ps);
                if (!this.verbose) return true;
                logger.log(isnull);
            }
        }
        return true;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public boolean endElementImport(DBAccess database, String elementName, String elementCharacters, boolean isCDATA, Logger logger) throws SapphireException {
        int i;
        int newRow;
        if (this.ignoreTable) return true;
        if (elementName.equalsIgnoreCase("table")) {
            if (this.importTarget != 0) return true;
            if (this.rowCount > 0) {
                try {
                    Iterator it = this.preparedStatements.keySet().iterator();
                    while (it.hasNext()) {
                        ((DBUtil)database).reset((PreparedStatement)this.preparedStatements.get(it.next()));
                    }
                    if (!this.commitScope.equals("table")) return true;
                    logger.log("Committing table: " + this.tableid + " (" + this.rowCount + " rows)");
                    boolean autoCommit = database.getConnection().getAutoCommit();
                    database.getConnection().setAutoCommit(false);
                    database.getConnection().commit();
                    database.getConnection().setAutoCommit(autoCommit);
                    return true;
                }
                catch (SQLException sqle) {
                    throw new SapphireException("Failed to reset prepared statements: SQLException: " + sqle.getMessage());
                }
            } else {
                if (!this.verbose) return true;
                logger.log("No rows found!");
            }
            return true;
        }
        if (elementName.equalsIgnoreCase("tabledef")) {
            this.processDefinition(database, logger, false);
            return true;
        }
        if (elementName.equalsIgnoreCase("coldef")) return true;
        if (elementName.equalsIgnoreCase("col")) {
            if (this.ignoreColumn) return true;
            String value = elementCharacters;
            this.currentColumn.setValue(value, isCDATA);
            if (this.currentColumn.getColumnid().equalsIgnoreCase("versionstatus")) {
                this.isVersionStatusCurrent = this.currentColumn.getValue().equals("C");
                this.versionStatusForceUpdate = this.currentColumn.isForceUpdate();
            }
            if (!this.verbose) return true;
            logger.log((this.parseOnly ? "Parsed" : "Imported") + " column: " + this.currentColumn.getColumnid() + ", Value=" + elementCharacters);
            return true;
        }
        if (!elementName.equalsIgnoreCase("row")) return false;
        StringBuffer keys = new StringBuffer();
        int n = newRow = this.parsedKeys != null ? this.parsedKeys.addRow() : -1;
        if (this.parseOnly || this.verbose) {
            Column column;
            if (this.altkeycolumns == null || this.altkeycolumns.size() == 0) {
                for (i = 0; i < this.columns.size(); ++i) {
                    column = (Column)this.columns.get(i);
                    if (!column.isPrimarykey()) continue;
                    if (this.verbose) {
                        keys.append(";").append(column.getValue());
                    }
                    if (!this.parseOnly) continue;
                    this.parsedKeys.setValue(newRow, column.getColumnid(), column.getValue());
                }
            } else {
                for (i = 0; i < this.altkeycolumns.size(); ++i) {
                    column = this.getColumn(this.altkeycolumns.get(i));
                    if (this.verbose) {
                        keys.append(";").append(column.getValue());
                    }
                    if (!this.parseOnly) continue;
                    this.parsedKeys.setValue(newRow, column.getColumnid(), column.getValue());
                }
            }
            if (this.verbose) {
                logger.log("Parsed row " + this.rowCount + ": " + (keys.length() > 0 ? keys.substring(1) : "no primary keys"));
            }
        }
        if (this.importTarget == 0 || this.importTarget == 2) {
            if (this.tableid.equalsIgnoreCase("sdinote") && this.importDirectives != null) {
                for (i = 0; i < this.importDirectives.size(); ++i) {
                    if (!((ImportDirective)this.importDirectives.get(i)).isSDINoteDirective()) continue;
                    ((ImportDirective)this.importDirectives.get(i)).executeSDINoteDirective(database, this.columns);
                    break;
                }
            }
            int sqlType = 1;
            PreparedStatement ps = null;
            if (!this.rowsExist) {
                if (this.parseOnly) {
                    this.parsedKeys.setNumber(newRow, "__importaction", 1);
                } else {
                    if (this.verbose) {
                        logger.log("Inserting row...");
                    }
                    if (this.executeRowStatement(database, ps = (PreparedStatement)this.preparedStatements.get("insert"), 1, logger) == 1 && this.verbose) {
                        logger.log("Row inserted");
                    }
                }
            } else {
                StringBuffer update = new StringBuffer("update " + this.tableid + " set ");
                boolean forceupdatecols = false;
                int pos = 1;
                for (int i2 = 0; i2 < this.columns.size(); ++i2) {
                    Column column = (Column)this.columns.get(i2);
                    if (this.altkeycolumns != null && this.altkeycolumns.size() != 0 && (this.altkeycolumns.size() <= 0 || column.isPrimarykey()) || this.sdikeyid.length() != 0 && this.sdikeyid.equalsIgnoreCase(column.getColumnid())) continue;
                    boolean isForceNullUpdate = false;
                    if (column.isForceNullUpdate()) {
                        PreparedStatement nullps = (PreparedStatement)this.preparedStatements.get("isnull_" + column.getColumnid());
                        if (this.parseOnly || this.executeRowStatement(database, nullps, 0, logger) == 1) {
                            isForceNullUpdate = true;
                            forceupdatecols = true;
                            update.append(pos > 1 ? "," : "").append(column.getColumnid()).append("=?");
                            column.setForceUpdate("true");
                            column.setUpdatePos(pos++);
                        }
                    }
                    if (isForceNullUpdate || !column.isForceUpdate()) continue;
                    forceupdatecols = true;
                    update.append(pos > 1 ? "," : "").append(column.getColumnid()).append("=?");
                    column.setUpdatePos(pos++);
                }
                if (forceupdatecols) {
                    update.append(" where ").append(this.updateKeyCols);
                    ps = (PreparedStatement)this.preparedStatements.get(update.toString());
                    if (ps == null) {
                        ps = database.prepareStatement(update.toString(), update.toString());
                        this.preparedStatements.put(update.toString(), ps);
                        if (this.verbose) {
                            logger.log(update.toString());
                        }
                    }
                }
                if (ps == null) {
                    ps = (PreparedStatement)this.preparedStatements.get("exists");
                    if (this.executeRowStatement(database, ps, 0, logger) == 1) {
                        if (this.verbose) {
                            logger.log("Row exists");
                        }
                        sqlType = 0;
                        if (this.parseOnly) {
                            this.parsedKeys.setNumber(newRow, "__importaction", 0);
                        }
                    } else if (this.parseOnly) {
                        this.parsedKeys.setNumber(newRow, "__importaction", 1);
                    } else {
                        ps = (PreparedStatement)this.preparedStatements.get("insert");
                        if (this.verbose) {
                            logger.log("Inserting row...");
                        }
                        if (this.executeRowStatement(database, ps, 1, logger) == 1) {
                            if (this.verbose) {
                                logger.log("Row inserted");
                            }
                            sqlType = 1;
                        }
                    }
                } else {
                    int rows = this.executeRowStatement(database, (PreparedStatement)this.preparedStatements.get("exists"), 0, logger);
                    if (rows > 1) {
                        throw new SapphireException("Multiple rows detected in " + this.tableid + " using key columns: " + this.getKeyColumnValues());
                    }
                    if (this.parseOnly) {
                        if (rows == 1) {
                            this.parsedKeys.setNumber(newRow, "__importaction", 2);
                        } else {
                            this.parsedKeys.setNumber(newRow, "__importaction", 1);
                        }
                    } else if (rows == 1) {
                        if (this.verbose) {
                            logger.log("Updating row...");
                        }
                        if (this.executeRowStatement(database, ps, 2, logger) == 1) {
                            if (this.verbose) {
                                logger.log("Row updated");
                            }
                            sqlType = 2;
                        }
                    } else {
                        ps = (PreparedStatement)this.preparedStatements.get("insert");
                        if (this.executeRowStatement(database, ps, 1, logger) == 1) {
                            if (this.verbose) {
                                logger.log("Row inserted");
                            }
                            sqlType = 1;
                        }
                    }
                }
            }
            if (this.verbose) {
                logger.log("Row " + (sqlType == 1 ? (this.parseOnly ? "to be " : "") + "inserted" : (sqlType == 0 ? "exists" : (this.parseOnly ? "to be " : "") + "updated")) + ": " + (keys.length() > 0 ? keys.substring(1) : "no primary key"));
            }
            if (!this.commitScope.equals("row")) return true;
            try {
                logger.log("Committing row: " + this.tableid + " (" + this.rowCount + ")");
                boolean autoCommit = database.getConnection().getAutoCommit();
                database.getConnection().setAutoCommit(false);
                database.getConnection().commit();
                database.getConnection().setAutoCommit(autoCommit);
                return true;
            }
            catch (SQLException e) {
                throw new SapphireException("Failed to commit records. Exception: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionid())));
            }
        } else {
            Table table = (Table)this.importObject;
            int tableRow = table.addRow();
            for (int i3 = 0; i3 < this.columns.size(); ++i3) {
                Column column = (Column)this.columns.get(i3);
                table.setValue(tableRow, column.getColumnid(), column.getValue());
            }
        }
        return true;
    }

    private void processDefinition(DBAccess database, Logger logger, boolean defineCols) throws SapphireException {
        block18: {
            block17: {
                int i;
                if (this.importTarget != 0 && this.importTarget != 2) break block17;
                DataSet tabledef = this.loadTableDef(database, defineCols);
                StringBuffer insertcols = new StringBuffer();
                this.updateKeyCols = new StringBuffer();
                boolean versionStatus = false;
                boolean versioned = false;
                ArrayList<Integer> removeColumns = new ArrayList<Integer>();
                for (i = 0; i < this.columns.size(); ++i) {
                    boolean include = true;
                    Column column = (Column)this.columns.get(i);
                    int findRow = -1;
                    for (int j = 0; findRow == -1 && j < tabledef.size(); ++j) {
                        if (!column.getColumnid().equalsIgnoreCase(tabledef.getValue(j, "columnid"))) continue;
                        findRow = j;
                    }
                    if (findRow == -1 && this.importTarget == 0) {
                        if (this.ignoreMissingObjects) {
                            include = false;
                            removeColumns.add(i);
                        } else {
                            throw new SapphireException("Column '" + column.getColumnid() + "' not found in table '" + this.tableid + "'");
                        }
                    }
                    if (!include) continue;
                    if (!column.getDatatype().equals("C") || column.getColumnlength() > tabledef.getInt(findRow, "columnlength")) {
                        // empty if block
                    }
                    insertcols.append(", ").append(column.getColumnid());
                    if (column.isPrimarykey() && (this.altkeycolumns == null || this.altkeycolumns.size() == 0)) {
                        this.updateKeyCols.append(" and ").append(column.getColumnid()).append(" = ? ");
                        if (column.getColumnid().toLowerCase().endsWith("versionid")) {
                            versioned = true;
                        }
                        if (this.parseOnly) {
                            this.parsedKeys.addColumn(column.getColumnid(), column.getDatatype().equals("C") ? 0 : (column.getDatatype().equals("N") ? 1 : 2));
                        }
                    }
                    if (column.getColumnid().toLowerCase().equals("versionstatus")) {
                        versionStatus = true;
                    }
                    if (!this.verbose) continue;
                    logger.log(("Column Definition: " + column.getColumnid() + StringUtil.repeat(" ", 30)).substring(0, 30) + column.getDatatype() + StringUtil.repeat(" ", 30).substring(0, 30) + column.getColumnlength());
                }
                if (this.altkeycolumns != null && this.altkeycolumns.size() > 0) {
                    for (i = 0; i < this.altkeycolumns.size(); ++i) {
                        Column column = this.getColumn(this.altkeycolumns.get(i));
                        this.updateKeyCols.append(" and ").append(column.getColumnid()).append(" = ? ");
                        if (!this.parseOnly) continue;
                        this.parsedKeys.addColumn(column.getColumnid(), column.getDatatype().equals("C") ? 0 : (column.getDatatype().equals("N") ? 1 : 2));
                    }
                }
                if (removeColumns.size() > 0) {
                    for (i = removeColumns.size() - 1; i >= 0; --i) {
                        this.columns.remove((Integer)removeColumns.get(i));
                    }
                }
                this.versionStatusCheck = versioned && versionStatus;
                this.updateKeyCols.delete(0, 5);
                String insert = "insert into " + this.tableid + " (" + insertcols.substring(1) + ") values (" + StringUtil.repeat(",?", this.columns.size()).substring(1) + ")";
                this.preparedStatements.put("insert", database.prepareStatement("insert", insert));
                if (this.verbose) {
                    logger.log(insert);
                }
                if (!this.rowsExist) break block18;
                String exists = "SELECT 1 FROM " + this.tableid + " WHERE " + this.updateKeyCols.toString();
                this.preparedStatements.put("exists", database.prepareStatement("exists", exists));
                if (!this.verbose) break block18;
                logger.log(exists);
                break block18;
            }
            Table table = (Table)this.importObject;
            table.setLinkid(this.linkid);
            for (int i = 0; i < this.columns.size(); ++i) {
                Column column = (Column)this.columns.get(i);
                table.addColumn(column);
                if (!this.verbose) continue;
                logger.log(("Column Definition: " + column.getColumnid() + StringUtil.repeat(" ", 30)).substring(0, 30) + column.getDatatype() + StringUtil.repeat(" ", 30).substring(0, 30) + column.getColumnlength());
            }
        }
        if (this.verbose) {
            logger.log((this.parseOnly ? "Parsed" : "Imported") + " definition");
        }
    }

    private DataSet loadTableDef(DBAccess database, boolean defineCols) throws SapphireException {
        database.createPreparedResultSet("tablecheck", "SELECT * FROM syscolumn WHERE tableid = ?", new Object[]{this.tableid});
        DataSet tabledef = new DataSet(database.getResultSet("tablecheck"));
        database.closeResultSet("tablecheck");
        if (defineCols) {
            for (int i = 0; i < tabledef.size(); ++i) {
                Column column = new Column(tabledef.getValue(i, "columnid"));
                column.setDDTDefinition(tabledef.getValue(i, "datatype"), tabledef.getValue(i, "pkflag"), tabledef.getValue(i, "nnflag"), tabledef.getValue(i, "columnlength"), tabledef.getValue(i, "columntype"));
                this.columns.add(column);
            }
        }
        this.addRequiredAuditColumns(tabledef);
        return tabledef;
    }

    private int executeRowStatement(DBAccess database, PreparedStatement ps, int sqltype, Logger logger) throws SapphireException {
        FormatUtil formatutil = FormatUtil.getInstance();
        try {
            int ret;
            Column column;
            int i;
            if (!this.parseOnly && this.versionStatusCheck && this.isVersionStatusCurrent && (sqltype == 1 || sqltype == 2 && this.versionStatusForceUpdate)) {
                StringBuffer versionStatusUpdate = new StringBuffer("UPDATE " + this.tableid + " SET versionstatus = 'A' WHERE versionstatus = 'C'");
                for (i = 0; i < this.columns.size(); ++i) {
                    column = (Column)this.columns.get(i);
                    if (!column.isPrimarykey() || column.getColumnid().toLowerCase().endsWith("versionid")) continue;
                    versionStatusUpdate.append(" AND ").append(column.getColumnid()).append("='").append(column.getValue()).append("'");
                }
                database.executeUpdate(versionStatusUpdate.toString());
            }
            boolean hasMSSBlob = false;
            if (database.isSqlServer()) {
                for (i = 0; !hasMSSBlob && i < this.columns.size(); ++i) {
                    column = (Column)this.columns.get(i);
                    if (!column.getDatatype().equals("T") && !column.getDatatype().equals("B")) continue;
                    hasMSSBlob = true;
                }
            }
            int bindVars = 0;
            for (int i2 = 0; i2 < this.columns.size(); ++i2) {
                Column column2 = (Column)this.columns.get(i2);
                if (this.importDirectives != null && this.importDirectives.size() > 0) {
                    for (int j = 0; j < this.importDirectives.size(); ++j) {
                        ImportDirective importDirective = (ImportDirective)this.importDirectives.get(j);
                        if (importDirective.isRegenerateDirective() && importDirective.getColumnid().equals(column2.getColumnid()) && sqltype == 1 && this.altkeycolumns != null && this.altkeycolumns.size() > 0 && column2.isPrimarykey()) {
                            if (this.hasConnection()) {
                                column2.setValue(importDirective.executeRegenerateDirective(this.columns, column2.getValue()));
                                continue;
                            }
                            throw new SapphireException("Alternate key regeneration only supported when import is done with a LabVantage application.");
                        }
                        if (importDirective.isReseqenceDirective() && importDirective.getColumnid().equals(column2.getColumnid()) && sqltype == 1) {
                            column2.setValue(importDirective.executeResequenceDirective(database, column2.getValue()));
                            continue;
                        }
                        if (!importDirective.isReplaceDirective() || !importDirective.getColumnid().equals(column2.getColumnid())) continue;
                        column2.setValue(importDirective.executeReplaceDirective(column2.getValue()));
                    }
                }
                boolean bind = false;
                if (sqltype == 1) {
                    bind = true;
                } else if (sqltype == 2 && column2.isForceUpdate() && !this.sdikeyid.equals(column2.getColumnid())) {
                    if (this.altkeycolumns == null || this.altkeycolumns.size() == 0) {
                        bind = true;
                    } else if (!column2.isPrimarykey()) {
                        bind = true;
                    }
                }
                if (!bind) continue;
                ++bindVars;
                if (column2.getDatatype().equals("C")) {
                    if (column2.getValue() == null || column2.getValue().length() == 0) {
                        ps.setNull(sqltype == 1 ? bindVars : column2.getUpdatePos(), 12);
                        continue;
                    }
                    ps.setString(sqltype == 1 ? bindVars : column2.getUpdatePos(), column2.getValue());
                    continue;
                }
                if (column2.getDatatype().equals("N") || column2.getDatatype().equals("R")) {
                    if (column2.getValue() == null || column2.getValue().length() == 0) {
                        ps.setNull(sqltype == 1 ? bindVars : column2.getUpdatePos(), 2);
                        continue;
                    }
                    if (database.isOracle()) {
                        ps.setBigDecimal(sqltype == 1 ? bindVars : column2.getUpdatePos(), formatutil.parseBigDecimal(column2.getValue()));
                        continue;
                    }
                    if (!hasMSSBlob) {
                        ps.setBigDecimal(sqltype == 1 ? bindVars : column2.getUpdatePos(), formatutil.parseBigDecimal(column2.getValue()));
                        continue;
                    }
                    try {
                        ps.setInt(sqltype == 1 ? bindVars : column2.getUpdatePos(), Integer.parseInt(column2.getValue()));
                    }
                    catch (Exception e) {
                        ps.setBigDecimal(sqltype == 1 ? bindVars : column2.getUpdatePos(), formatutil.parseBigDecimal(column2.getValue()));
                    }
                    continue;
                }
                if (column2.getDatatype().equals("D")) {
                    if (column2.getValue() == null || column2.getValue().length() == 0) {
                        ps.setNull(sqltype == 1 ? bindVars : column2.getUpdatePos(), 93);
                        continue;
                    }
                    ps.setTimestamp(sqltype == 1 ? bindVars : column2.getUpdatePos(), new DateTimeUtil().getTimestamp(column2.getValue()));
                    continue;
                }
                if (!column2.getDatatype().equals("T") && !column2.getDatatype().equals("B")) continue;
                if (column2.getValue() != null && column2.getValue().trim().length() > 0) {
                    this.getFileContents(column2, logger);
                    String filetype = column2.getFiletype();
                    if (filetype != null && filetype.equals("propertytree")) {
                        PropertyTree currentPropertyTree;
                        try {
                            currentPropertyTree = PropertyTreeUtil.getPropertyTree(database, column2.getFiletypeid());
                        }
                        catch (SapphireException e) {
                            currentPropertyTree = new PropertyTree();
                        }
                        PropertyTree filePropertyTree = new PropertyTree();
                        filePropertyTree.setValueXML(column2.getValue(), true);
                        currentPropertyTree.merge(filePropertyTree);
                        column2.setValue(currentPropertyTree.toXMLString());
                    }
                }
                if (this.tableid.equalsIgnoreCase("webpagepropertytree") && column2.getColumnid().equalsIgnoreCase("componentvaluetree") && column2.getValue() != null && column2.getValue().trim().length() > 0) {
                    String compvaluetree;
                    Object[] keycols = StringUtil.split(this.getKeyColumnValues(), ";");
                    database.createPreparedResultSet("fetchcomponentvaluetree", "SELECT componentvaluetree FROM webpagepropertytree WHERE " + this.updateKeyCols, keycols);
                    if (database.getNext("fetchcomponentvaluetree") && (compvaluetree = database.getString("fetchcomponentvaluetree", "componentvaluetree")) != null && compvaluetree.length() > 0) {
                        PropertyList currentValueTree = new PropertyList();
                        currentValueTree.setPropertyList(compvaluetree);
                        PropertyListCollection currentComponents = currentValueTree.getCollectionNotNull("components");
                        PropertyList importValueTree = new PropertyList();
                        importValueTree.setPropertyList(column2.getValue());
                        PropertyListCollection importComponents = importValueTree.getCollectionNotNull("components");
                        for (int j = 0; j < importComponents.size(); ++j) {
                            PropertyList importPL = importComponents.getPropertyList(j);
                            String importCompCode = importPL.getProperty("compcode");
                            PropertyList importValue = importPL.getPropertyList("value");
                            PropertyList component = currentComponents.find("compcode", importCompCode);
                            if (component == null) {
                                component = new PropertyList();
                                component.setProperty("compcode", importCompCode);
                                currentComponents.add(component);
                            }
                            component.setProperty("value", importValue);
                        }
                        column2.setValue(currentValueTree.toXMLString());
                    }
                }
                if (column2.getDatatype().equals("B")) {
                    byte[] myBuf = column2.getByteArray();
                    if (myBuf != null) {
                        ByteArrayInputStream baiStream = new ByteArrayInputStream(myBuf);
                        ps.setBinaryStream(sqltype == 1 ? bindVars : column2.getUpdatePos(), (InputStream)baiStream, myBuf.length);
                        continue;
                    }
                    ps.setNull(sqltype == 1 ? bindVars : column2.getUpdatePos(), 2004);
                    continue;
                }
                String clobString = column2.getValue();
                if (clobString != null) {
                    StringReader sr = new StringReader(clobString);
                    ps.setCharacterStream(sqltype == 1 ? bindVars : column2.getUpdatePos(), (Reader)sr, clobString.length());
                    continue;
                }
                ps.setNull(sqltype == 1 ? bindVars : column2.getUpdatePos(), 2005);
            }
            if (sqltype != 1) {
                this.setKeyColumnValues(ps, bindVars);
            }
            if (sqltype == 0) {
                ret = 0;
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    ++ret;
                }
            } else if (System.getProperty("sapphire.mssinstall", "false").equals("true") && sqltype == 2) {
                ret = ps.executeUpdate();
                if (ret == -1) {
                    ret = 1;
                }
            } else {
                ret = ps.executeUpdate();
            }
            return ret;
        }
        catch (SQLException sqle) {
            throw new SapphireException("ERROR: SQL Exception occured " + ErrorUtil.extractMessageFromException(sqle, ErrorUtil.isUserAdmin(this.getConnectionid())), sqle);
        }
    }

    private void setKeyColumnValues(PreparedStatement ps, int bindVar) throws SQLException, SapphireException {
        FormatUtil formatutil = FormatUtil.getInstance();
        if (this.altkeycolumns == null || this.altkeycolumns.size() == 0) {
            for (int i = 0; i < this.columns.size(); ++i) {
                Column column = (Column)this.columns.get(i);
                if (!column.isPrimarykey()) continue;
                ++bindVar;
                if (column.getDatatype().equals("C")) {
                    ps.setString(bindVar, column.getValue());
                    continue;
                }
                if (column.getDatatype().equals("N") || column.getDatatype().equals("R")) {
                    ps.setBigDecimal(bindVar, formatutil.parseBigDecimal(column.getValue()));
                    continue;
                }
                if (column.getDatatype().equals("D")) {
                    ps.setTimestamp(bindVar, new DateTimeUtil().getTimestamp(column.getValue()));
                    continue;
                }
                throw new SapphireException("Blob or clob cloumns defined as keys - this should not occur!");
            }
        } else {
            for (int i = 0; i < this.altkeycolumns.size(); ++i) {
                Column column = this.getColumn(this.altkeycolumns.get(i));
                ++bindVar;
                if (column.getDatatype().equals("C")) {
                    ps.setString(bindVar, column.getValue());
                    continue;
                }
                if (column.getDatatype().equals("N") || column.getDatatype().equals("R")) {
                    ps.setBigDecimal(bindVar, formatutil.parseBigDecimal(column.getValue()));
                    continue;
                }
                if (column.getDatatype().equals("D")) {
                    ps.setTimestamp(bindVar, new DateTimeUtil().getTimestamp(column.getValue()));
                    continue;
                }
                throw new SapphireException("Blob or clob cloumns defined as keys - this should not occur!");
            }
        }
    }

    private String getKeyColumnValues() {
        StringBuffer keycolumns = new StringBuffer();
        if (this.altkeycolumns == null || this.altkeycolumns.size() == 0) {
            for (int i = 0; i < this.columns.size(); ++i) {
                Column column = (Column)this.columns.get(i);
                if (!column.isPrimarykey()) continue;
                keycolumns.append(";").append(column.getValue());
            }
        } else {
            for (int i = 0; i < this.altkeycolumns.size(); ++i) {
                Column column = this.getColumn(this.altkeycolumns.get(i));
                keycolumns.append(";").append(column.getValue());
            }
        }
        return keycolumns.substring(1);
    }

    private void getFileContents(Column column, Logger logger) throws SapphireException {
        String value = column.getValue();
        if (value.startsWith("[") && value.endsWith("]")) {
            String filename = value.substring(1, value.indexOf("]"));
            try {
                if (this.zipFile != null && this.zipFileEntry != null) {
                    if (column.getDatatype().equals("B")) {
                        column.setByteArray(ZipFileUtil.extractByteArray(this.zipFile, filename));
                    } else {
                        column.setValue(ZipFileUtil.extractString(this.zipFile, filename));
                    }
                } else {
                    File xmlFile = new File(this.file.getParentFile(), filename).getCanonicalFile();
                    if (!xmlFile.exists()) {
                        throw new SapphireException("XML file '" + xmlFile.getAbsolutePath() + "' does not exist");
                    }
                    if (xmlFile.canRead()) {
                        if (this.verbose) {
                            logger.log("Loading column '" + column.getColumnid() + "' from file '" + xmlFile.getAbsolutePath() + "'");
                        }
                        if (column.getDatatype().equals("B")) {
                            column.setByteArray(FileUtil.getFileByteArray(xmlFile));
                        } else {
                            column.setValue(FileUtil.getFileString(xmlFile));
                        }
                    }
                }
            }
            catch (IOException ioe) {
                throw new SapphireException(ErrorUtil.extractMessageFromException(ioe, ErrorUtil.isUserAdmin(this.getConnectionid())), ioe);
            }
        }
    }

    private void addRequiredAuditColumns(DataSet tabledef) throws SapphireException {
        Column column;
        HashMap<String, String> findMap = new HashMap<String, String>();
        findMap.put("columnid", "createdt");
        if (tabledef.findRow(findMap) >= 0 && !this.columnExists("createdt")) {
            column = new Column("createdt");
            column.setDDTDefinition("D", "N", "N", "0", "S");
            column.setDefaultvalue("N");
            this.columns.add(column);
        }
        findMap.put("columnid", "createby");
        if (tabledef.findRow(findMap) >= 0 && !this.columnExists("createby")) {
            column = new Column("createby");
            column.setDDTDefinition("C", "N", "N", "20", "S");
            column.setDefaultvalue("(system)");
            this.columns.add(column);
        }
        findMap.put("columnid", "createtool");
        if (tabledef.findRow(findMap) >= 0 && !this.columnExists("createtool")) {
            column = new Column("createtool");
            column.setDDTDefinition("C", "N", "N", "20", "S");
            column.setDefaultvalue("(import)");
            this.columns.add(column);
        }
        findMap.put("columnid", "moddt");
        if (tabledef.findRow(findMap) >= 0 && !this.columnExists("moddt")) {
            column = new Column("moddt");
            column.setDDTDefinition("D", "N", "N", "0", "S");
            column.setDefaultvalue("N");
            this.columns.add(column);
        }
        findMap.put("columnid", "modby");
        if (tabledef.findRow(findMap) >= 0 && !this.columnExists("modby")) {
            column = new Column("modby");
            column.setDDTDefinition("C", "N", "N", "20", "S");
            column.setDefaultvalue("(system)");
            this.columns.add(column);
        }
        findMap.put("columnid", "modtool");
        if (tabledef.findRow(findMap) >= 0 && !this.columnExists("modtool")) {
            column = new Column("modtool");
            column.setDDTDefinition("C", "N", "N", "20", "S");
            column.setDefaultvalue("(import)");
            column.setForceUpdate("Y");
            this.columns.add(column);
        }
    }

    private boolean columnExists(String columnid) {
        for (int i = 0; i < this.columns.size(); ++i) {
            if (!columnid.equals(((Column)this.columns.get(i)).getColumnid())) continue;
            return true;
        }
        return false;
    }

    @Override
    public void generateAntTask(PrintStream out, int level) {
        String level0 = StringUtil.repeat("\t", level);
        out.println(level0 + "<table tableid=\"" + this.tableid + "\"" + (this.forceLOBExport ? " forcelobexport=\"true\"" : "") + (this.from != null ? " from=\"" + this.from + "\"" : "") + (this.where != null ? " where=\"" + this.where + "\"" : "") + (this.orderby != null ? " orderby=\"" + this.orderby + "\"" : "") + (this.file != null ? " file=\"" + this.file.getName() + "\"" : "") + (this.keyFilename != null ? " keyfile=\"" + this.keyFilename + "\"" : "") + (this.columns.size() == 0 && this.data == null ? "/>" : ">"));
        if (this.columns.size() > 0) {
            for (int j = 0; j < this.columns.size(); ++j) {
                Column column = (Column)this.columns.get(j);
                out.println(column.toXML(level + 1));
            }
            out.println(level0 + "</table>");
        }
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public boolean isValid() {
        return this.tableid != null && this.tableid.length() > 0 && this.getFrom() != null && this.getFrom().length() > 0;
    }
}

