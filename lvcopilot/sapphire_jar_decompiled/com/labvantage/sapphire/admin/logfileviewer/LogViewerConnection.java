/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.logfileviewer;

public class LogViewerConnection {
    public String connectionid;
    public String userid;

    public boolean equals(Object object) {
        if (object != null && object instanceof LogViewerConnection) {
            LogViewerConnection thing = (LogViewerConnection)object;
            if (this.connectionid == null) {
                return thing.connectionid == null;
            }
            return this.connectionid.equals(thing.connectionid);
        }
        return false;
    }
}

