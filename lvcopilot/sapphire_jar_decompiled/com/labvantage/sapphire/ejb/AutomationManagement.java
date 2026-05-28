/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.ejb;

import com.labvantage.sapphire.admin.system.automation.LAM;
import com.labvantage.sapphire.admin.system.automation.LAMException;
import com.labvantage.sapphire.admin.system.automation.Server;
import com.labvantage.sapphire.ejb.ManagerException;
import sapphire.xml.PropertyList;

public interface AutomationManagement {
    public PropertyList getToDoListProperties(String var1, String var2) throws ManagerException;

    public void loadWebPageCache(String var1) throws ManagerException;

    public LAM createLAM(String var1) throws LAMException;

    public void destroyLAM(String var1) throws LAMException;

    public void pausePoller(String var1, int var2);

    public void resumePoller(String var1, int var2);

    public boolean pingServer(String var1, Server var2);

    public void killRedundantServers(String var1, int var2);

    public void scanServerList(String var1);

    public void startStopCollectors(String var1);

    public void reallocateCollectors(String var1);
}

