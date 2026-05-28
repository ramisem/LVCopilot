/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.util.file;

import com.labvantage.sapphire.util.file.BaseFileDetails;

public class TextFileDetails
extends BaseFileDetails {
    private int fromLine = 1;
    private int toLine = 1;
    private int totalRowsAvailable;

    public int getFromLine() {
        return this.fromLine;
    }

    public void setFromLine(int fromLine) {
        this.fromLine = fromLine;
    }

    public int getToLine() {
        return this.toLine;
    }

    public void setToLine(int toLine) {
        this.toLine = toLine;
    }

    public int getTotalLinesAvailable() {
        return this.totalRowsAvailable;
    }

    public void setTotalRowsAvailable(int totalRowsAvailable) {
        this.totalRowsAvailable = totalRowsAvailable;
    }
}

