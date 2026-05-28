/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.eventmanager;

import sapphire.SapphireException;
import sapphire.xml.PropertyList;

public interface NotifyListener {
    public void setSystemPassword(String var1);

    public void resetDatabaseFilterOptions(String var1);

    public boolean notify(PropertyList var1) throws SapphireException;

    public boolean preNotify(PropertyList var1) throws SapphireException;
}

