/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.sapphire.util.cache.CacheUtil;
import sapphire.SapphireException;
import sapphire.action.BaseSDCRules;
import sapphire.util.SDIData;
import sapphire.xml.PropertyList;

public class LV_EditorStyle
extends BaseSDCRules {
    static final String LABVANTAGE_CVS_ID = "$Revision: 60907 $";

    @Override
    public void preAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
    }

    @Override
    public void preEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        CacheUtil.clear(this.connectionInfo.getDatabaseId(), "EditorStyle");
    }

    @Override
    public void postEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
    }

    @Override
    public void preDelete(String rsetid, PropertyList actionProps) throws SapphireException {
    }
}

