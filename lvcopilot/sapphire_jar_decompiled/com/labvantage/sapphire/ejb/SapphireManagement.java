/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.ejb;

import com.labvantage.sapphire.ejb.ManagerException;
import com.labvantage.sapphire.platform.SapphireDatabase;
import java.util.ArrayList;
import java.util.HashMap;

public interface SapphireManagement {
    public ArrayList<String> startup(HashMap var1) throws ManagerException;

    public ArrayList<String> startupDatabase(SapphireDatabase var1) throws ManagerException;

    public void shutdown() throws ManagerException;

    public void restart() throws ManagerException;

    public void resetCache() throws ManagerException;

    public void resetSingleton() throws ManagerException;

    public void stopAutomation() throws ManagerException;

    public void startAutomation() throws ManagerException;

    public void startDatabaseAutomation(SapphireDatabase var1) throws ManagerException;

    public HashMap processCommand(HashMap<String, Object> var1) throws ManagerException;

    public void processCommands(ArrayList<HashMap<String, Object>> var1) throws ManagerException;
}

