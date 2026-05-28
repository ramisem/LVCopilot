/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.tools.ant.Task
 */
package com.labvantage.sapphire.xml.ant;

import com.labvantage.sapphire.xml.SDCTransfer;
import com.labvantage.sapphire.xml.ant.ColumnTask;
import java.io.File;
import java.util.ArrayList;
import org.apache.tools.ant.Task;

public class SDCTask
extends Task {
    private String sdcid;
    private String categoryid;
    private boolean excludelinktables;
    private boolean flushupdtables = true;
    private boolean resetsystables = false;
    private String forceUpdate = "true";
    private String forceNullUpdate;
    private File file;
    private boolean excludeAuditColumns = true;
    private boolean exportTableDefinition = true;
    private boolean forcelobexport;
    private String compcode = "";
    private ArrayList columns = new ArrayList();

    public void setSdcid(String sdcid) {
        this.sdcid = sdcid;
    }

    public void setCategoryid(String categoryid) {
        this.categoryid = categoryid;
    }

    public void setExcludelinktables(boolean excludelinktables) {
        this.excludelinktables = excludelinktables;
    }

    public void setFlushupdtables(boolean flushupdtables) {
        this.flushupdtables = flushupdtables;
    }

    public void setForceUpdate(String forceUpdate) {
        this.forceUpdate = forceUpdate;
    }

    public void setForceNullUpdate(String forceNullUpdate) {
        this.forceNullUpdate = forceNullUpdate;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public void setExcludeAuditColumns(boolean excludeAuditColumns) {
        this.excludeAuditColumns = excludeAuditColumns;
    }

    public void setExportTableDefinition(boolean exportTableDefinition) {
        this.exportTableDefinition = exportTableDefinition;
    }

    public void setForcelobexport(boolean forcelobexport) {
        this.forcelobexport = forcelobexport;
    }

    public void setCompcode(String compcode) {
        this.compcode = compcode;
    }

    public SDCTransfer getSDCTransfer() {
        SDCTransfer sdc = new SDCTransfer(this.sdcid);
        sdc.setCategoryid(this.categoryid);
        sdc.setExcludelinktables(this.excludelinktables);
        sdc.setFlushupdtables(this.flushupdtables);
        sdc.setResetsystables(this.resetsystables);
        sdc.setForceUpdate(this.forceUpdate);
        sdc.setForceNullUpdate(this.forceNullUpdate);
        sdc.setFile(this.file);
        sdc.setExcludeAuditColumns(this.excludeAuditColumns);
        sdc.setExportTableDefinition(this.exportTableDefinition);
        sdc.setForceLOBExport(this.forcelobexport);
        sdc.setCompCode(this.compcode);
        for (int i = 0; i < this.columns.size(); ++i) {
            sdc.addColumn(((ColumnTask)((Object)this.columns.get(i))).getColumn());
        }
        return sdc;
    }

    public void addConfiguredColumn(ColumnTask column) {
        this.columns.add(column);
    }
}

