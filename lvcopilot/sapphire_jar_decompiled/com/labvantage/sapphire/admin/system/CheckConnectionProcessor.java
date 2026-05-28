/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.system;

import sapphire.accessor.ConnectionProcessor;

public class CheckConnectionProcessor
extends ConnectionProcessor {
    public CheckConnectionProcessor() {
    }

    public CheckConnectionProcessor(String connectionid) {
        super(connectionid);
    }

    public boolean isConnectionValid(String connectionid) {
        try {
            return connectionid != null && connectionid.length() > 0 && this.getLocalAccessManager().checkConnection(connectionid, true);
        }
        catch (Exception e) {
            return false;
        }
    }
}

