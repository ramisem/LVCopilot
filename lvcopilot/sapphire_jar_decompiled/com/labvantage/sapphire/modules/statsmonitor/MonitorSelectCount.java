/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.statsmonitor;

import com.labvantage.sapphire.modules.statsmonitor.BaseMonitor;
import sapphire.SapphireException;

public class MonitorSelectCount
extends BaseMonitor {
    @Override
    public double getValue(String itemid, String args) throws SapphireException {
        return this.database.getCount(args);
    }
}

