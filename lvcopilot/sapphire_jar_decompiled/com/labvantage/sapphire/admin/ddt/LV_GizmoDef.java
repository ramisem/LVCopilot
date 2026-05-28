/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.sapphire.util.cache.CacheUtil;
import sapphire.SapphireException;
import sapphire.accessor.ConnectionProcessor;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.xml.PropertyList;

public class LV_GizmoDef
extends BaseSDCRules {
    @Override
    public boolean requiresBeforeEditImage() {
        return false;
    }

    @Override
    public boolean requiresEditDetailPrimary() {
        return true;
    }

    @Override
    public void preAddDetail(SDIData sdiData, PropertyList actionProps) throws SapphireException {
    }

    @Override
    public void preAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        for (int i = 0; i < primary.size(); ++i) {
            String gizmoid = primary.getValue(i, "gizmodefid");
            this.clearGizmoCache(gizmoid);
        }
    }

    @Override
    public void preEditDetail(SDIData sdiData, PropertyList actionProps) throws SapphireException {
    }

    @Override
    public void preEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        for (int i = 0; i < primary.size(); ++i) {
            String gizmoid = primary.getValue(i, "gizmodefid");
            this.clearGizmoCache(gizmoid);
        }
    }

    @Override
    public void preDeleteDetail(String rsetid, PropertyList actionProps) throws SapphireException {
    }

    @Override
    public void preDelete(String rsetid, PropertyList actionProps) throws SapphireException {
        this.database.createPreparedResultSet("SELECT gizmodefid FROM gizmodef, rsetitems WHERE gizmodef.gizmodefid = rsetitems.keyid1 AND rsetid = ?", new Object[]{rsetid});
        while (this.database.getNext()) {
            String gizmoid = this.database.getString("gizmodefid");
            this.clearOverrides(gizmoid);
            this.clearGizmoCache(gizmoid);
        }
    }

    private void clearOverrides(String gizmoid) {
        ConnectionProcessor cp = this.getConnectionProcessor();
        this.getQueryProcessor().execSQL(20026, new Object[]{gizmoid});
    }

    private void clearGizmoCache(String gizmoid) {
        CacheUtil.remove(this.connectionInfo.getDatabaseId(), "GizmoDefAll", this.connectionInfo.getDatabaseId() + ";" + this.connectionInfo.getSysuserId() + ";" + this.connectionInfo.getConnectionId());
        CacheUtil.remove(this.connectionInfo.getDatabaseId(), "GizmoDef", gizmoid);
        CacheUtil.removeAllStartWith(this.connectionInfo.getDatabaseId(), "GizmoDefSecured", gizmoid + ";");
        CacheUtil.removeAllStartWith(this.connectionInfo.getDatabaseId(), "GizmoDefUser", gizmoid + ";");
        CacheUtil.removeAllStartWith(this.connectionInfo.getDatabaseId(), "GizmoDefAccess", gizmoid + ";");
        CacheUtil.removeAllStartWith(this.connectionInfo.getDatabaseId(), "GizmoDefAssets", gizmoid);
    }
}

