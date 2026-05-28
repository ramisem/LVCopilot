/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.ejb;

import com.labvantage.sapphire.ejb.ManagerException;

public interface SequenceManagement {
    public int getSequence(String var1, String var2, String var3, int var4, int var5) throws ManagerException;

    public String getUUID(String var1) throws ManagerException;
}

