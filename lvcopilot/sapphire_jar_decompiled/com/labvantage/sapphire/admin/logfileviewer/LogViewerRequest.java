/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.logfileviewer;

import java.util.Date;

public class LogViewerRequest {
    public int index;
    public String type;
    public String startFilename;
    public int startRow;
    public String endFilename;
    public int endRow;
    public String connectionid;
    public String userid;
    public String startDate;
    public Date startDateDate = null;
    public String endDate;
    public long took = -1L;
    public String targetid;
    public String url;
    public String thread;
    public boolean hasException;
    public String exceptionType = "";
    public int indent;
    public int startTotalRow = -1;
    public int endTotalRow = -1;
    public Date endDateDate = null;
}

