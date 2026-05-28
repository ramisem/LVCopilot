/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdi;

import com.labvantage.sapphire.actions.sdi.BaseWorkgroupItem;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;

public class EditWorkgroupItem
extends BaseWorkgroupItem
implements sapphire.action.EditWorkgroupItem {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        this.maintainWorkgroupItem(false, properties);
    }
}

