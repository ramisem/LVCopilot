/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.statsmonitor;

import com.labvantage.sapphire.modules.statsmonitor.BaseMonitor;
import com.labvantage.sapphire.util.cache.CacheUtil;
import sapphire.SapphireException;

public class MonitorCacheSizes
extends BaseMonitor {
    @Override
    public double getValue(String itemid, String args) throws SapphireException {
        return CacheUtil.getCacheSize(this.databaseid, args);
    }
}

