/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.logfileviewer;

public class LogViewerThread
implements Comparable {
    public String threadid;
    public String startdate;
    public int count = 1;

    public String toString() {
        return this.threadid;
    }

    public int compareTo(Object o) {
        return this.threadid.compareTo(((LogViewerThread)o).threadid);
    }
}

