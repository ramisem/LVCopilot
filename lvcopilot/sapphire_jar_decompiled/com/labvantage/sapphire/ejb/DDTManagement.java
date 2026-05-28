/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.ejb;

import com.labvantage.sapphire.ejb.ManagerException;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public interface DDTManagement {
    public PropertyList getSDCProperties(String var1, String var2) throws ManagerException;

    public DataSet getReverseLinksData(String var1, String var2) throws ManagerException;

    public PropertyListCollection getTableColumns(String var1, String var2) throws ManagerException;
}

