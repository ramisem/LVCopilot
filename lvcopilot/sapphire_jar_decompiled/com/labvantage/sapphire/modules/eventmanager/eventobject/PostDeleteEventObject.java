/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.eventmanager.eventobject;

import com.labvantage.sapphire.modules.eventmanager.eventobject.BaseSDCEventObject;
import sapphire.util.SDIData;
import sapphire.xml.PropertyList;

public class PostDeleteEventObject
extends BaseSDCEventObject {
    public static final String EVENT_POSTDELETE = "PostDelete";

    public PostDeleteEventObject(String sdcid, PropertyList sdcProps, String rsetid, PropertyList properties) {
        super(EVENT_POSTDELETE, new Object[]{rsetid, properties, null}, sdcid, sdcProps, properties, null);
        this.setRsetid(rsetid);
    }

    @Override
    public SDIData getSDIData() {
        return (SDIData)this.getSupplementalData();
    }

    @Override
    public int addEventItems() {
        if (this.getSDIData() != null) {
            return this.addEventItemsFromPrimary();
        }
        return this.addEventItemsFromProperties();
    }
}

