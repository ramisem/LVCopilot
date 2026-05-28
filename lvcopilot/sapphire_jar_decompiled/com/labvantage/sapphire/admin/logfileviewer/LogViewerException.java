/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.logfileviewer;

public class LogViewerException {
    public int index;
    public String exception;
    public String firstAt = null;
    public StringBuffer content = new StringBuffer();
    public String filename;
    public long startRow;
    public long startTotalRow;
    public String thread;
    public String startDate;
    public int requestIndex = -1;
}

