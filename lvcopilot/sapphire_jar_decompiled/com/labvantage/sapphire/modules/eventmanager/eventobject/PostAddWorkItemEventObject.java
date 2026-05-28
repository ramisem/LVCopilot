/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.eventmanager.eventobject;

import com.labvantage.sapphire.modules.eventmanager.eventobject.BaseSDCEventObject;
import sapphire.util.SDIData;
import sapphire.xml.PropertyList;

public class PostAddWorkItemEventObject
extends BaseSDCEventObject {
    public static final String EVENT_POSTADDWORKITEM = "PostAddWorkItem";

    public PostAddWorkItemEventObject(String sdcid, PropertyList sdcProps, SDIData sdiData, PropertyList properties) {
        super(EVENT_POSTADDWORKITEM, new Object[]{sdiData, properties, null}, sdcid, sdcProps, properties, sdiData);
    }

    @Override
    public int addEventItems() {
        return this.addEventItemsFromWorkitems();
    }
}

