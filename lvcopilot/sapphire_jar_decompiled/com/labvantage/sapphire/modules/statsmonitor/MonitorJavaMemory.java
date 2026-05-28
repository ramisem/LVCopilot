/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.statsmonitor;

import com.labvantage.sapphire.modules.statsmonitor.BaseMonitor;
import sapphire.SapphireException;
import sapphire.util.DBAccess;

public class MonitorJavaMemory
extends BaseMonitor {
    private Runtime r;

    @Override
    public void init(String databaseid, DBAccess database) {
        super.init(databaseid, database);
        this.r = Runtime.getRuntime();
    }

    @Override
    public double getValue(String itemid, String args) throws SapphireException {
        if ("Total Memory".equals(itemid)) {
            return this.r.totalMemory() / 0x100000L;
        }
        if ("Free Memory".equals(itemid)) {
            return this.r.freeMemory() / 0x100000L;
        }
        if ("Maximum Memory".equals(itemid)) {
            return this.r.maxMemory() / 0x100000L;
        }
        throw new SapphireException("Item " + itemid + " not recognized");
    }
}

