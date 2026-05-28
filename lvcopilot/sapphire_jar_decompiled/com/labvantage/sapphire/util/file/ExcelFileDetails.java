/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.util.file;

import com.labvantage.sapphire.util.file.BaseFileDetails;

public class ExcelFileDetails
extends BaseFileDetails {
    private String sheetName = "1";
    private String allSheets = "";
    private boolean showGridLines = true;

    public boolean isShowGridLines() {
        return this.showGridLines;
    }

    public void setShowGridLines(boolean showGridLines) {
        this.showGridLines = showGridLines;
    }

    public String getSheetName() {
        return this.sheetName;
    }

    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }

    public String getAllSheets() {
        return this.allSheets;
    }

    public void setAllSheets(String allSheets) {
        this.allSheets = allSheets;
    }
}

