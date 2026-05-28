/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.ejb;

import com.labvantage.sapphire.ejb.ManagerException;
import java.util.List;
import sapphire.util.DataSet;

public interface StatusManagement {
    public DataSet getStats(String var1, int var2) throws ManagerException;

    public void resetStats(String var1) throws ManagerException;

    public DataSet getCacheSizes(String var1, boolean var2) throws ManagerException;

    public DataSet getClassLoaderStats(String var1, boolean var2) throws ManagerException;

    public List<String> getLSMExceptions(String var1) throws ManagerException;

    public DataSet getTableSizes(String var1) throws ManagerException;

    public DataSet getMemoryStats(String var1) throws ManagerException;

    public double getStatsMonitoringValue(String var1, String var2, String var3) throws ManagerException;
}

