/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.ejb;

import com.labvantage.sapphire.ejb.ManagerException;
import sapphire.xml.PropertyList;

public interface I18NManagement {
    public boolean isAutoFillTempAllowed(String var1) throws ManagerException;

    public void addToTransmasterTemp(String var1, String var2, String var3) throws ManagerException;

    public void saveTranslation(String var1, String var2, String var3, String var4, String var5) throws ManagerException;

    public PropertyList translateTable(String var1, String var2, PropertyList var3) throws ManagerException;

    public PropertyList getWebTranslations(String var1, String var2) throws ManagerException;
}

