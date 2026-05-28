/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.statsmonitor;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.modules.statsmonitor.BaseMonitor;
import sapphire.SapphireException;

public class MonitorStatsRollups
extends BaseMonitor {
    @Override
    public double getValue(String itemid, String args) throws SapphireException {
        int traceType = Integer.parseInt(args.substring(0, args.indexOf(";")));
        String filter = args.length() > args.indexOf(";") ? args.substring(args.indexOf(";") + 1) : "";
        return Trace.getMonitoringTotalCounter(traceType, filter);
    }
}

