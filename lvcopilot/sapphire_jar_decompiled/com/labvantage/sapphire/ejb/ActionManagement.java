/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.ejb;

import com.labvantage.sapphire.ejb.ManagerException;
import sapphire.util.ActionBlock;

public interface ActionManagement {
    public ActionBlock processActionBlock(String var1, ActionBlock var2, boolean var3, boolean var4) throws ManagerException;
}

