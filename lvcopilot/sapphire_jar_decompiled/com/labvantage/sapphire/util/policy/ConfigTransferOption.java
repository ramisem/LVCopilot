/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.util.policy;

import java.io.Serializable;
import sapphire.xml.PropertyList;

public class ConfigTransferOption
implements Serializable {
    private PropertyList transferOptionPL;

    public ConfigTransferOption(PropertyList transferOptionPL) {
        this.transferOptionPL = transferOptionPL != null ? transferOptionPL : new PropertyList();
    }

    public boolean isExcludedColumn(String columnid) {
        String excludecolumnlist = this.transferOptionPL.getProperty("excludecolumnlist");
        return (";" + excludecolumnlist + ";").indexOf(";" + columnid + ";") > 0;
    }

    public boolean isFlushTarget() {
        return "Y".equals(this.transferOptionPL.getProperty("flush", "N"));
    }

    public boolean isExcludeSecurityDetails() {
        return "Y".equals(this.transferOptionPL.getProperty("excludesecuritydetails", "N"));
    }

    public boolean isExcludeAuditColumns() {
        return "Y".equals(this.transferOptionPL.getProperty("excludeauditcolumn", "N"));
    }

    public boolean isCreateNewVersion() {
        String importversionedsdioption = this.transferOptionPL.getProperty("importversionedsdioption");
        return "Create New Version".equals(importversionedsdioption);
    }

    public boolean isUpdateSameVersion() {
        return "Override If Provisional".equals(this.transferOptionPL.getProperty("importversionedsdioption"));
    }

    public boolean promptImporterForVersionOption() {
        return "Allow User To Choose".equals(this.transferOptionPL.getProperty("importversionedsdioption"));
    }

    public boolean isRegenerateKey() {
        return "Regenerate Auto Key".equals(this.transferOptionPL.getProperty("importoption"));
    }

    public boolean isUpdateIfExists() {
        return "Override Existing".equals(this.transferOptionPL.getProperty("importoption"));
    }

    public boolean isIgnoreIfExists() {
        return "Ignore If Exists".equals(this.transferOptionPL.getProperty("importoption"));
    }

    public void setImportVersionedSDIOption(String option) {
        this.transferOptionPL.setProperty("importversionedsdioption", option);
    }

    public void setImportSDIOption(String option) {
        this.transferOptionPL.setProperty("importoption", option);
    }
}

