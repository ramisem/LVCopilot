/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.ejb;

import com.labvantage.sapphire.ejb.ManagerException;

public interface TDQManagement {
    public boolean processToDoListEntry(String var1, String var2) throws ManagerException;

    public void deleteToDoListEntry(String var1, String var2) throws ManagerException;

    public void setToDoListError(String var1, String var2, String var3) throws ManagerException;

    public String getNextToDoListItems(String var1, int var2) throws ManagerException;

    public void writeConnectionCache(String var1) throws ManagerException;

    public void prepareTimeoutConnections(String var1, int var2) throws ManagerException;

    public void timeoutConnections(String var1) throws ManagerException;

    public void timeoutConnection(String var1, String var2) throws ManagerException;

    public void timeoutRSets(String var1, int var2) throws ManagerException;

    public void performHouseKeeping(String var1) throws ManagerException;

    public void performStatsMonitoringHourly(String var1) throws ManagerException;

    public void performStatsMonitoringDaily(String var1) throws ManagerException;

    public void importDataCaptureFolders(String var1) throws ManagerException;

    public String sendServerCommands(String var1) throws ManagerException;

    public String processServerCommands(String var1) throws ManagerException;
}

