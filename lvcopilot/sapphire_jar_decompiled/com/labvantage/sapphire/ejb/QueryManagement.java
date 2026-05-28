/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.ejb;

import com.labvantage.sapphire.ejb.ManagerException;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;

public interface QueryManagement {
    public DataSet getSQLDataSet(String var1, String var2, String var3, boolean var4, int var5, boolean var6) throws ManagerException;

    public DataSet getSQLDataSet(String var1, String var2, String var3, boolean var4, int var5) throws ManagerException;

    public DataSet getSQLDataSet(String var1, int var2, Object[] var3, boolean var4) throws ManagerException;

    public DataSet getPreparedSqlDataSet(String var1, String var2, String var3, Object[] var4, boolean var5, int var6) throws ManagerException;

    public DataSet getPreparedSqlDataSet(String var1, int var2, Object[] var3, boolean var4) throws ManagerException;

    public DataSet getRefTypeDataSet(String var1, String var2) throws ManagerException;

    public int execPreparedUpdate(String var1, String var2, Object[] var3) throws ManagerException;

    public int execSQL(String var1, String var2) throws ManagerException;

    public int execSQL(String var1, int var2, Object[] var3) throws ManagerException;

    public String getQueryKeyid1List(String var1, String var2, String var3, String[] var4) throws ManagerException;

    public SDIData getSDIData(String var1, SDIRequest var2) throws ManagerException;

    public int getSDICount(String var1, SDIRequest var2, boolean var3) throws ManagerException;

    public int getSDICount(String var1, SDIRequest var2) throws ManagerException;

    public String getSecurityFilterWhere(String var1, String var2) throws ManagerException;
}

