/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.ejb;

import com.labvantage.sapphire.ejb.ManagerException;
import com.labvantage.sapphire.services.SapphireConnection;

public interface SecurityManagement {
    public String getConnectionId(SapphireConnection var1) throws ManagerException;

    public SapphireConnection getSapphireConnection(String var1) throws ManagerException;

    public void changePassword(String var1, String var2, String var3, String var4) throws ManagerException;

    public boolean isValidPassword(String var1, String var2, String var3) throws ManagerException;

    public void clearConnection(String var1, String var2) throws ManagerException;

    public void prepareToDeleteConnection(String var1, String var2) throws ManagerException;

    public boolean checkUser(String var1, String var2, String var3) throws ManagerException;

    public boolean checkConnection(String var1, boolean var2) throws ManagerException;

    public void disableUser(String var1, String var2, String var3) throws ManagerException;

    public void enableUser(String var1, String var2) throws ManagerException;

    public void forcePasswordChange(String var1, String var2) throws ManagerException;
}

