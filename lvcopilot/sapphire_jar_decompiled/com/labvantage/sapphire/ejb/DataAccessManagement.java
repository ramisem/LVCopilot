/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.ejb;

import com.labvantage.sapphire.RSet;
import com.labvantage.sapphire.ejb.ManagerException;
import sapphire.util.SDIList;

public interface DataAccessManagement {
    public void clearRSet(String var1, RSet var2) throws ManagerException;

    public void clearRSets(String var1, String var2) throws ManagerException;

    public void touchRSet(String var1, RSet var2) throws ManagerException;

    public RSet createRSet(String var1, String var2, String var3, String var4, String var5) throws ManagerException;

    public RSet createRSet(String var1, String var2, String var3, String var4, String var5, boolean var6, int var7) throws ManagerException;

    public RSet createRSetQ(String var1, String var2, String var3, String[] var4) throws ManagerException;

    public RSet createLockedRSet(String var1, String var2, String var3, String var4, String var5, String var6) throws ManagerException;

    public RSet createRSetDS(String var1, String var2, String var3, String var4, String var5, String var6, String var7, String var8, String var9, boolean var10, boolean var11) throws ManagerException;

    public RSet createRSetDSNP(String var1, String var2, String var3, String var4, String var5, String var6, String var7, String var8, String var9, boolean var10, boolean var11) throws ManagerException;

    public RSet createLockedRSetDS(String var1, String var2, String var3, String var4, String var5, String var6, String var7, String var8, String var9, String var10, boolean var11) throws ManagerException;

    public RSet createLockedRSetDSNP(String var1, String var2, String var3, String var4, String var5, String var6, String var7, String var8, String var9, String var10, boolean var11) throws ManagerException;

    public RSet createRSetWI(String var1, String var2, String var3, String var4, String var5, String var6, String var7, boolean var8) throws ManagerException;

    public RSet lockRSet(String var1, RSet var2, String var3, int var4) throws ManagerException;

    public SDIList checkSDIAccess(String var1, String var2, String var3, String var4, String var5, boolean var6, String var7) throws ManagerException;

    public boolean setGlobalLock(String var1, boolean var2) throws ManagerException;

    public boolean isGlobalLock(String var1) throws ManagerException;

    public boolean checkRESTAccess(String var1, String var2, String var3, String var4, String var5, String var6, String var7) throws ManagerException;
}

