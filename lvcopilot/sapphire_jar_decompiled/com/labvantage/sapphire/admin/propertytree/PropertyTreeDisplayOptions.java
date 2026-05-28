/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.propertytree;

import java.util.HashMap;

public class PropertyTreeDisplayOptions {
    public boolean readonly = false;
    public boolean showAdvanced = true;
    public boolean showPropertylistids = false;
    public boolean showExportFlag = false;
    public boolean showDebug = false;
    public boolean showModules = false;
    public boolean cascadeAdvanced = false;
    public boolean containsAdvanced = false;
    public boolean lockAll = false;
    public boolean collectionitemcopy = true;
    public boolean collectionitempaste = true;
    public boolean translate = true;
    public boolean appRolesOnly = false;
    private HashMap<String, String> propertylistsWithError = new HashMap();

    public void setPropertyListHasError(String propertylistid, String message) {
        this.propertylistsWithError.put(propertylistid, message);
    }

    public boolean doesPropertyListHaveError(String propertylistid) {
        return this.propertylistsWithError.get(propertylistid) != null && this.propertylistsWithError.get(propertylistid).length() > 0;
    }

    public String getPropertyListError(String propertylistid) {
        return this.propertylistsWithError.get(propertylistid);
    }
}

