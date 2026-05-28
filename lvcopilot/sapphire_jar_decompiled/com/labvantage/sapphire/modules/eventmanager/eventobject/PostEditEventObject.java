/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.eventmanager.eventobject;

import com.labvantage.sapphire.modules.eventmanager.eventobject.BaseSDCEventObject;
import sapphire.util.SDIData;
import sapphire.xml.PropertyList;

public class PostEditEventObject
extends BaseSDCEventObject {
    public static final String EVENT_POSTEDIT = "PostEdit";

    public PostEditEventObject(String sdcid, PropertyList sdcProps, SDIData sdiData, PropertyList properties) {
        super(EVENT_POSTEDIT, new Object[]{sdiData, properties, null}, sdcid, sdcProps, properties, sdiData);
    }

    public SDIData getBeforeSDIData() {
        return (SDIData)this.getSupplementalData();
    }

    @Override
    public int addEventItems() {
        return this.addEventItemsFromPrimary();
    }
}

