/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.ejb;

import com.labvantage.sapphire.ejb.ManagerException;
import java.util.ArrayList;
import sapphire.xml.PropertyList;

public interface WebAdminManagement {
    public String getPropertyTreeDef(String var1, String var2) throws ManagerException;

    public void setPropertyTreeDef(String var1, String var2, String var3) throws ManagerException;

    public String getPropertyTreeValue(String var1, String var2) throws ManagerException;

    public void setPropertyTreeValue(String var1, String var2, String var3) throws ManagerException;

    public String getPropertyTreeObject(String var1, String var2) throws ManagerException;

    public void savePropertyTree(String var1, String var2, String var3, String var4, String[] var5, String[] var6, String[] var7) throws ManagerException;

    public String getPageValueTree(String var1, String var2, String var3, String var4, String var5) throws ManagerException;

    public boolean isProductPage(String var1, String var2, String var3) throws ManagerException;

    public String getPageProductValueTree(String var1, String var2, String var3, String var4, String var5) throws ManagerException;

    public void setPageValueTree(String var1, String var2, String var3, String var4, String var5, String var6, ArrayList var7) throws ManagerException;

    public void addPropertyTree(String var1, String var2, String var3, String var4, String var5) throws ManagerException;

    public void editPropertyTree(String var1, String var2, String var3, String var4, String var5) throws ManagerException;

    public void setPropertyTreeCategories(String var1, String var2, String var3) throws ManagerException;

    public void setPropertyTreeRoles(String var1, String var2, String var3) throws ManagerException;

    public void setWebPage(String var1, String var2, String var3, String var4, String var5, String var6, String var7, String var8, String var9) throws ManagerException;

    public void setWebPageDefaultFlag(String var1, String var2, String var3) throws ManagerException;

    public void setUserOverrides(String var1, String var2, String var3, String var4, String var5, PropertyList var6) throws ManagerException;

    public void clearUserOverrides(String var1, String var2, String var3, String var4, String var5) throws ManagerException;

    public void setWebPageProperties(String var1, String var2, String var3, PropertyList var4) throws ManagerException;

    public void setWebPageCategories(String var1, String var2, String var3) throws ManagerException;

    public void setWebPageRoles(String var1, String var2, String var3) throws ManagerException;

    public void addWebPagePropertyTree(String var1, String var2, String var3, String var4, String var5, String var6, int var7) throws ManagerException;

    public void deleteWebPagePropertyTree(String var1, String var2, String var3, String var4, String var5) throws ManagerException;

    public void renameWebPagePropertyTreeNode(String var1, String var2, String var3, String var4) throws ManagerException;

    public void moveWebPagePropertyTreePage(String var1, String var2, String var3, String var4, String var5, String var6) throws ManagerException;

    public void setComponentPageOverride(String var1, String var2, String var3, String var4, String var5, String var6, String var7) throws ManagerException;

    public void removeComponentPageOverride(String var1, String var2, String var3, String var4, String var5, String var6) throws ManagerException;

    public String getComponentPageOverrides(String var1, String var2, String var3, String var4, String var5) throws ManagerException;

    public void deletePropertyTree(String var1, String var2) throws ManagerException;

    public void synchronizePropertyTree(String var1, String var2) throws ManagerException;
}

