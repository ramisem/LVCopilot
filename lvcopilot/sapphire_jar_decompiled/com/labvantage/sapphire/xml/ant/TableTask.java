/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.tools.ant.Task
 */
package com.labvantage.sapphire.xml.ant;

import com.labvantage.sapphire.xml.TableTransfer;
import com.labvantage.sapphire.xml.ant.ColumnTask;
import com.labvantage.sapphire.xml.ant.DataTask;
import java.io.File;
import java.util.ArrayList;
import org.apache.tools.ant.Task;

public class TableTask
extends Task {
    private String tableid;
    private String from;
    private String where;
    private String orderby;
    private File keyFile;
    private String keyseparator = "\t";
    private String tablealias;
    private File file;
    private File datafile;
    private String data;
    private ArrayList columns = new ArrayList();
    private boolean forcelobexport;
    private String lobfilepattern;
    private boolean forceupdate;
    private boolean forcenullupdate;
    private boolean verbose;
    private boolean excludeAuditColumns;
    private boolean exportTableDefinition = true;
    private boolean exportTableDefWithNoData = true;

    public void setTableid(String tableid) {
        this.tableid = tableid;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public void setWhere(String where) {
        this.where = where;
    }

    public void setOrderby(String orderby) {
        this.orderby = orderby;
    }

    public void setKeyFile(File keyFile) {
        this.keyFile = keyFile;
    }

    public void setKeyseparator(String keyseparator) {
        this.keyseparator = keyseparator;
    }

    public void setTablealias(String tablealias) {
        this.tablealias = tablealias;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public void setDatafile(File datafile) {
        this.datafile = datafile;
    }

    public void setForceupdate(boolean forceupdate) {
        this.forceupdate = forceupdate;
    }

    public void setForcenullupdate(boolean forcenullupdate) {
        this.forcenullupdate = forcenullupdate;
    }

    public void setForcelobexport(boolean forcelobexport) {
        this.forcelobexport = forcelobexport;
    }

    public void setLobfilepattern(String lobfilepattern) {
        this.lobfilepattern = lobfilepattern;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public void setExcludeAuditColumns(boolean excludeAuditColumns) {
        this.excludeAuditColumns = excludeAuditColumns;
    }

    public void setExportTableDefinition(boolean exportTableDefinition) {
        this.exportTableDefinition = exportTableDefinition;
    }

    public void setExportTableDefWithNoData(boolean exportTableDefWithNoData) {
        this.exportTableDefWithNoData = exportTableDefWithNoData;
    }

    public TableTransfer getTableTransfer() {
        TableTransfer table = new TableTransfer(this.tableid);
        table.setTablealias(this.tablealias);
        table.setFrom(this.from);
        table.setWhere(this.where);
        table.setOrderby(this.orderby);
        if (this.keyFile != null) {
            table.setKeyFilename(this.keyFile.getAbsolutePath());
        }
        table.setKeyseparator(this.keyseparator);
        table.setFile(this.file);
        table.setDefaultForceUpdate(this.forceupdate ? "true" : "false");
        table.setDefaultForceNullUpdate(this.forcenullupdate ? "true" : "false");
        table.setForceLOBExport(this.forcelobexport);
        if (this.lobfilepattern != null && this.lobfilepattern.length() > 0) {
            table.setDefaultFilePattern(this.lobfilepattern);
        }
        table.setVerbose(this.verbose);
        table.setExcludeAuditColumns(this.excludeAuditColumns);
        table.setExportTableDefinition(this.exportTableDefinition);
        table.setExportTableDefWithNoData(this.exportTableDefWithNoData);
        table.setDataFile(this.datafile);
        table.setData(this.data);
        for (int i = 0; i < this.columns.size(); ++i) {
            table.addColumn(((ColumnTask)((Object)this.columns.get(i))).getColumn());
        }
        return table;
    }

    public void addConfiguredColumn(ColumnTask column) {
        this.columns.add(column);
    }

    public void addConfiguredData(DataTask data) {
        this.data = data.getData();
    }
}

