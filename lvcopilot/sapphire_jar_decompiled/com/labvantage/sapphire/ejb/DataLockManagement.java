/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.ejb;

import com.labvantage.sapphire.RSet;
import com.labvantage.sapphire.ejb.ManagerException;

public interface DataLockManagement {
    public RSet createRSet(String var1, RSet var2) throws ManagerException;

    public RSet lockRSet(String var1, RSet var2, String var3, int var4) throws ManagerException;

    public RSet lockRSet(String var1, RSet var2, String var3, int var4, boolean var5) throws ManagerException;

    public RSet lockRSet(String var1, RSet var2, String var3, int var4, boolean var5, boolean var6) throws ManagerException;

    public void clearRSet(String var1, RSet var2) throws ManagerException;

    public void clearLocks(String var1, RSet var2) throws ManagerException;
}

