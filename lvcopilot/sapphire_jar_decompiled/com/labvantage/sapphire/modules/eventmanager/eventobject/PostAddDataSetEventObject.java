/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.eventmanager.eventobject;

import com.labvantage.sapphire.modules.eventmanager.eventobject.BaseSDCEventObject;
import sapphire.util.SDIData;
import sapphire.xml.PropertyList;

public class PostAddDataSetEventObject
extends BaseSDCEventObject {
    public static final String EVENT_POSTADDDATASET = "PostAddDataSet";

    public PostAddDataSetEventObject(String sdcid, PropertyList sdcProps, SDIData sdiData, PropertyList properties) {
        super(EVENT_POSTADDDATASET, new Object[]{sdiData, properties, null}, sdcid, sdcProps, properties, sdiData);
    }

    @Override
    public int addEventItems() {
        return this.addEventItemsFromDatasets();
    }
}

