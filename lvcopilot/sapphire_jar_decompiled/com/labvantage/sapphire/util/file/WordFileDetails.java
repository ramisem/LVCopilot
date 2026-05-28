/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.util.file;

import com.labvantage.sapphire.util.file.BasePagedFileDetails;

public class WordFileDetails
extends BasePagedFileDetails {
    private boolean exportPageMargins = true;
    private boolean exportPageSetup = true;

    public void setExportPageMargins(boolean exportPageMargins) {
        this.exportPageMargins = exportPageMargins;
    }

    public boolean isExportPageMargins() {
        return this.exportPageMargins;
    }

    public void setExportPageSetup(boolean exportPageSetup) {
        this.exportPageSetup = exportPageSetup;
    }

    public boolean isExportPageSetup() {
        return this.exportPageSetup;
    }
}

