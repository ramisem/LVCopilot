/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.sapphire.services.ConfigService;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.util.cache.CacheNames;
import com.labvantage.sapphire.util.cache.CacheUtil;
import sapphire.SapphireException;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.xml.PropertyList;

public class LV_ImageRef
extends BaseSDCRules
implements CacheNames {
    @Override
    public void preAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        boolean devMode;
        ConfigService config = new ConfigService(new SapphireConnection(this.database.getConnection(), this.connectionInfo));
        try {
            devMode = config.getSysConfigProperty("devmode", "N").equalsIgnoreCase("Y");
        }
        catch (Exception e) {
            devMode = false;
        }
        String type = devMode ? "C" : "U";
        SapphireConnection sc = new SapphireConnection(this.database.getConnection(), this.connectionInfo);
        DataSet primary = sdiData.getDataset("primary");
        for (int i = 0; i < primary.size(); ++i) {
            primary.setString(i, "imagetypeflag", type);
            this.clearImageListFromCache(sc);
        }
    }

    @Override
    public void preDelete(String rsetid, PropertyList actionProps) throws SapphireException {
        boolean devMode;
        ConfigService config = new ConfigService(new SapphireConnection(this.database.getConnection(), this.connectionInfo));
        try {
            devMode = config.getSysConfigProperty("devmode", "N").equalsIgnoreCase("Y");
        }
        catch (Exception e) {
            devMode = false;
        }
        SapphireConnection s = new SapphireConnection(this.database.getConnection(), this.connectionInfo);
        this.database.createPreparedResultSet("SELECT imagerefid, imagetypeflag FROM imageref, rsetitems WHERE imageref.imagerefid = rsetitems.keyid1 AND rsetid = ?", new Object[]{rsetid});
        while (this.database.getNext()) {
            String imagerefid = this.database.getString("imagerefid");
            String type = this.database.getString("imagetypeflag");
            if (!devMode && (type.equalsIgnoreCase("S") || type.equalsIgnoreCase("C"))) {
                throw new SapphireException("You cannot delete 'Core' or 'System' images");
            }
            this.clearImageFromCache(imagerefid);
        }
        this.clearImageListFromCache(s);
    }

    @Override
    public void preEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        SapphireConnection s = new SapphireConnection(this.database.getConnection(), this.connectionInfo);
        DataSet primary = sdiData.getDataset("primary");
        for (int i = 0; i < primary.size(); ++i) {
            String imagerefid = primary.getValue(i, "imagerefid");
            this.clearImageListFromCache(s);
            this.clearImageFromCache(imagerefid);
        }
    }

    private void clearImageListFromCache(SapphireConnection sc) {
        CacheUtil.remove(this.connectionInfo.getDatabaseId(), "ImageRef", "$LIST$");
    }

    private void clearImageFromCache(String imagerefid) {
        CacheUtil.removeAllStartWith(this.connectionInfo.getDatabaseId(), "ImageRef", imagerefid + ";");
    }
}

