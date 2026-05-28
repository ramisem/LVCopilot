/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.eventmanager.eventobject;

import com.labvantage.sapphire.modules.eventmanager.eventobject.BaseSDCEventObject;
import sapphire.util.SDIData;
import sapphire.xml.PropertyList;

public class PostAddEventObject
extends BaseSDCEventObject {
    public static final String EVENT_POSTADD = "PostAdd";

    public PostAddEventObject(String sdcid, PropertyList sdcProps, SDIData sdiData, PropertyList properties) {
        super(EVENT_POSTADD, new Object[]{sdiData, properties}, sdcid, sdcProps, properties, sdiData);
    }

    @Override
    public int addEventItems() {
        return this.addEventItemsFromPrimary();
    }
}

