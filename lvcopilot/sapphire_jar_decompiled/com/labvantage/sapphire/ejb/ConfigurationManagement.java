/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.ejb;

import com.labvantage.sapphire.ejb.ManagerException;
import java.util.List;

public interface ConfigurationManagement {
    public String getConfigProperty(String var1, String var2, String var3) throws ManagerException;

    public void setConfigProperty(String var1, String var2, String var3) throws ManagerException;

    public String getSysConfigProperty(String var1, String var2, String var3) throws ManagerException;

    public void setSysConfigProperty(String var1, String var2, String var3) throws ManagerException;

    public String getProfileProperty(String var1, String var2, String var3, String var4) throws ManagerException;

    public void setProfileProperty(String var1, String var2, String var3, String var4) throws ManagerException;

    public String getLicenseProperty(String var1, String var2) throws ManagerException;

    public List getSapphireDatabases() throws ManagerException;
}

