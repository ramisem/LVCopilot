/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.ejb;

import com.labvantage.sapphire.ejb.ManagerException;
import java.util.HashMap;
import java.util.Set;
import sapphire.xml.PropertyList;

public interface RequestManagement {
    public PropertyList getConnectionProperties(String var1) throws ManagerException;

    public PropertyList getWebPageProperties(String var1, String var2, PropertyList var3) throws ManagerException;

    public PropertyList getWebPageProperties(String var1, String var2, String var3, PropertyList var4, boolean var5) throws ManagerException;

    public String getDefaultWebPageEdition(String var1, String var2) throws ManagerException;

    public PropertyList getBulletinProperties(String var1, String var2, String var3, PropertyList var4) throws ManagerException;

    public PropertyList getHistoryProperties(String var1, String var2, PropertyList var3) throws ManagerException;

    public PropertyList getFavoriteProperties(String var1, String var2, PropertyList var3) throws ManagerException;

    public PropertyList addPropertyData(String var1, PropertyList var2) throws ManagerException;

    public HashMap processRequest(String var1, String var2, HashMap var3) throws ManagerException;

    public void logPageAccess(String var1, String var2, String var3, String var4, HashMap var5) throws ManagerException;

    public String logPageAccess(String var1, String var2, String var3, String var4, HashMap var5, boolean var6) throws ManagerException;

    public String getPropertyTreeValue(String var1, String var2) throws ManagerException;

    public Set<String> getInactiveRoleList(String var1) throws ManagerException;

    public String processFileCommand(String var1, String var2) throws ManagerException;
}

