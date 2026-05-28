/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.tools.ant.Task
 */
package com.labvantage.sapphire.xml.ant;

import com.labvantage.sapphire.xml.SDIDetail;
import com.labvantage.sapphire.xml.SDITransfer;
import com.labvantage.sapphire.xml.ant.ColumnTask;
import com.labvantage.sapphire.xml.ant.DataTask;
import com.labvantage.sapphire.xml.ant.SDIDetailTask;
import java.io.File;
import java.util.ArrayList;
import org.apache.tools.ant.Task;

public class SDITask
extends Task {
    private String sdcid;
    private String keyid1;
    private String keyid2;
    private String keyid3;
    private String queryfrom;
    private String querywhere;
    private String queryorderby;
    private String categoryid;
    private File keyFile;
    private String keyseparator = "\t";
    private String primaryForceUpdate = "false";
    private String primaryForceNullUpdate = "false";
    private String detailForceUpdate = "false";
    private String detailForceNullUpdate = "false";
    private String exportDetails = "true";
    private String flushDetails = "false";
    private String exportSDIDetails = "true";
    private String flushSDI = "false";
    private String flushSDIDetails = "false";
    private String flushChildSDI = "false";
    private String exportFKDetails = "false";
    private String exportSecurityDetails = "false";
    private String exportRoles = "true";
    private String flushRoles = "false";
    private String exportCategories = "true";
    private String flushCategories = "false";
    private String syncDataModel = "false";
    private String altkeycols = "";
    private boolean ignoreSequenceCheck = false;
    private boolean excludeAuditColumns = true;
    private boolean propagateAuditColumns = false;
    private String exportid;
    private File file;
    private String data;
    ArrayList columns = new ArrayList();
    ArrayList sdiDetails = new ArrayList();
    private boolean exportTableDefinition = true;

    public void setSdcid(String sdcid) {
        this.sdcid = sdcid;
    }

    public void setKeyid1(String keyid1) {
        this.keyid1 = keyid1;
    }

    public void setKeyid2(String keyid2) {
        this.keyid2 = keyid2;
    }

    public void setKeyid3(String keyid3) {
        this.keyid3 = keyid3;
    }

    public void setQueryfrom(String queryfrom) {
        this.queryfrom = queryfrom;
    }

    public void setQuerywhere(String querywhere) {
        this.querywhere = querywhere;
    }

    public void setQueryorderby(String queryorderby) {
        this.queryorderby = queryorderby;
    }

    public void setCategoryid(String categoryid) {
        this.categoryid = categoryid;
    }

    public void setExportDetails(String exportDetails) {
        this.exportDetails = exportDetails;
    }

    public void setFlushDetails(String flushDetails) {
        this.flushDetails = flushDetails;
    }

    public void setExportSDIDetails(String exportSDIDetails) {
        this.exportSDIDetails = exportSDIDetails;
    }

    public void setFlushSDIDetails(String flushSDIDetails) {
        this.flushSDIDetails = flushSDIDetails;
    }

    public void setFlushSDI(String flushSDI) {
        this.flushSDI = flushSDI;
    }

    public void setFlushChildSDI(String flushChildSDI) {
        this.flushChildSDI = flushChildSDI;
    }

    public void setExportFKDetails(String exportFKDetails) {
        this.exportFKDetails = exportFKDetails;
    }

    public void setExportSecurityDetails(String exportSecurityDetails) {
        this.exportSecurityDetails = exportSecurityDetails;
    }

    public void setPrimaryForceUpdate(String primaryForceUpdate) {
        this.primaryForceUpdate = primaryForceUpdate;
    }

    public void setPrimaryForceNullUpdate(String primaryForceNullUpdate) {
        this.primaryForceNullUpdate = primaryForceNullUpdate;
    }

    public void setDetailForceUpdate(String detailForceUpdate) {
        this.detailForceUpdate = detailForceUpdate;
    }

    public void setDetailForceNullUpdate(String detailForceNullUpdate) {
        this.detailForceNullUpdate = detailForceNullUpdate;
    }

    public void setExportRoles(String exportRoles) {
        this.exportRoles = exportRoles;
    }

    public void setFlushRoles(String flushRoles) {
        this.flushRoles = flushRoles;
    }

    public void setExportCategories(String exportCategories) {
        this.exportCategories = exportCategories;
    }

    public void setFlushCategories(String flushCategories) {
        this.flushCategories = flushCategories;
    }

    public void setSyncDataModel(String syncDataModel) {
        this.syncDataModel = syncDataModel;
    }

    public void setAltkeycols(String altkeycols) {
        this.altkeycols = altkeycols;
    }

    public void setIgnoreSequenceCheck(boolean ignoreSequenceCheck) {
        this.ignoreSequenceCheck = ignoreSequenceCheck;
    }

    public void setExportid(String exportid) {
        this.exportid = exportid;
    }

    public void setKeyFile(File keyFile) {
        this.keyFile = keyFile;
    }

    public void setKeyseparator(String keyseparator) {
        this.keyseparator = keyseparator;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public void setExcludeAuditColumns(boolean excludeAuditColumns) {
        this.excludeAuditColumns = excludeAuditColumns;
    }

    public void setPropagateAuditColumns(boolean propagateAuditColumns) {
        this.propagateAuditColumns = propagateAuditColumns;
    }

    public void setExportTableDefinition(boolean exportTableDefinition) {
        this.exportTableDefinition = exportTableDefinition;
    }

    public SDITransfer getSDITransfer() {
        int i;
        SDITransfer sdi = new SDITransfer(this.sdcid);
        sdi.setKeyid1(this.keyid1);
        sdi.setKeyid2(this.keyid2);
        sdi.setKeyid3(this.keyid3);
        sdi.setQueryfrom(this.queryfrom);
        sdi.setQuerywhere(this.querywhere);
        sdi.setQueryorderby(this.queryorderby);
        sdi.setCategoryid(this.categoryid);
        if (this.keyFile != null) {
            sdi.setKeyFilename(this.keyFile.getAbsolutePath());
        }
        sdi.setKeyseparator(this.keyseparator);
        sdi.setExportDetails(this.exportDetails);
        sdi.setFlushDetails(this.flushDetails);
        sdi.setExportSDIDetails(this.exportSDIDetails);
        sdi.setFlushSDI(this.flushSDI);
        sdi.setFlushSDIDetails(this.flushSDIDetails);
        sdi.setFlushChildSDI(this.flushChildSDI);
        sdi.setExportFKDetails(this.exportFKDetails);
        sdi.setExportSecurityDetails(this.exportSecurityDetails);
        sdi.setPrimaryForceUpdate(this.primaryForceUpdate);
        sdi.setPrimaryForceNullUpdate(this.primaryForceNullUpdate);
        sdi.setDetailForceUpdate(this.detailForceUpdate);
        sdi.setDetailForceNullUpdate(this.detailForceNullUpdate);
        sdi.setSyncDataModel(this.syncDataModel);
        sdi.setAltkeycols(this.altkeycols);
        sdi.setExcludeAuditColumns(this.excludeAuditColumns);
        sdi.setPropagateAuditColumns(this.propagateAuditColumns);
        sdi.setExportTableDefinition(this.exportTableDefinition);
        sdi.setExportid(this.exportid);
        sdi.setIgnoreSequenceCheck(this.ignoreSequenceCheck);
        sdi.setFile(this.file);
        sdi.setData(this.data);
        for (i = 0; i < this.columns.size(); ++i) {
            sdi.addColumn(((ColumnTask)((Object)this.columns.get(i))).getColumn());
        }
        for (i = 0; i < this.sdiDetails.size(); ++i) {
            sdi.addSDIDetail(((SDIDetailTask)((Object)this.sdiDetails.get(i))).getSDIDetail());
        }
        if (this.exportRoles != null && (this.exportRoles.equals("Y") || this.exportRoles.equals("true"))) {
            sdi.addSDIDetail(new SDIDetail("sdirole"));
        }
        sdi.setFlushRoles(this.flushRoles);
        if (this.exportCategories != null && (this.exportCategories.equals("Y") || this.exportCategories.equals("true"))) {
            sdi.addSDIDetail(new SDIDetail("categoryitem"));
        }
        sdi.setFlushCategories(this.flushCategories);
        return sdi;
    }

    public void addConfiguredColumn(ColumnTask column) {
        this.columns.add(column);
    }

    public void addConfiguredSDIDetail(SDIDetailTask sdiDetail) {
        this.sdiDetails.add(sdiDetail);
    }

    public void addConfiguredData(DataTask data) {
        this.data = data.getData();
    }
}

