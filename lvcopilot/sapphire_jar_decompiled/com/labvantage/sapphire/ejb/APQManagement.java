/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.ejb;

import com.labvantage.sapphire.ejb.ManagerException;
import sapphire.util.DataSet;

public interface APQManagement {
    public void scheduleTasks(String var1) throws ManagerException;

    public void scheduleEvents(String var1) throws ManagerException;

    public void executeScheduleEvents(String var1, String var2, String var3, String var4, String var5, String var6) throws ManagerException;

    public int setPlanItemInProcessingFlag(String var1, String var2, String var3, String var4) throws ManagerException;

    public void setScheduleEventError(String var1, String var2, String var3, String var4) throws ManagerException;

    public DataSet getExecutePlanItems(String var1) throws ManagerException;
}

