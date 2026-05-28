/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.logfileviewer;

import java.util.Date;

public interface LogViewerParser {
    public boolean isContentLine(String var1);

    public String getLineContent(String var1);

    public String getLineDateString(String var1);

    public int getLineContentPos(String var1);

    public String getLineLevel(String var1);

    public String getLineContext(String var1);

    public Date getLineDate(String var1);

    public String getLineThread(String var1);

    public boolean isContentStartupStart(String var1);

    public boolean isContentStartupEnd(String var1);

    public boolean isContentRequestStart(String var1);

    public boolean isContentRequestEnd(String var1);

    public boolean isContentActionStart(String var1);

    public boolean isContentActionEnd(String var1);
}

