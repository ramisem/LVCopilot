/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.statsmonitor;

import com.labvantage.sapphire.modules.statsmonitor.MonitorConstants;
import sapphire.SapphireException;
import sapphire.util.DBAccess;

public abstract class BaseMonitor
implements MonitorConstants {
    DBAccess database;
    String databaseid;

    public void init(String databaseid, DBAccess database) {
        this.databaseid = databaseid;
        this.database = database;
    }

    public abstract double getValue(String var1, String var2) throws SapphireException;
}

