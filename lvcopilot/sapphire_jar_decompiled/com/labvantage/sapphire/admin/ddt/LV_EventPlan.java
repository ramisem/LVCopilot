/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.sapphire.util.cache.CacheUtil;
import sapphire.SapphireException;
import sapphire.action.BaseSDCRules;
import sapphire.util.SDIData;
import sapphire.xml.PropertyList;

public class LV_EventPlan
extends BaseSDCRules {
    @Override
    public void postAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        this.clearCache();
    }

    @Override
    public void postDelete(String rsetid, PropertyList actionProps) throws SapphireException {
        this.clearCache();
    }

    @Override
    public void postEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        this.clearCache();
    }

    @Override
    public void postAddDetail(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        this.clearCache();
    }

    @Override
    public void postEditDetail(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        this.clearCache();
    }

    @Override
    public void postDeleteDetail(String rsetid, PropertyList actionProps) throws SapphireException {
        this.clearCache();
    }

    private void clearCache() {
        CacheUtil.remove(this.connectionInfo.getDatabaseId(), "EventManager", "EventTypes");
        CacheUtil.remove(this.connectionInfo.getDatabaseId(), "EventManager", "EventPlans");
        CacheUtil.remove(this.connectionInfo.getDatabaseId(), "EventManager", "GlobalPlans");
        CacheUtil.remove(this.connectionInfo.getDatabaseId(), "EventManager", "GlobalSDCPlans");
        CacheUtil.remove(this.connectionInfo.getDatabaseId(), "EventManager", "HasPlans");
        CacheUtil.remove(this.connectionInfo.getDatabaseId(), "EventManager", "RequiresSupplementalData");
        CacheUtil.remove(this.connectionInfo.getDatabaseId(), "EventManager", "EventTypeImplementations");
    }
}

