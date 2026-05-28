/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.eventmanager.eventobject;

import com.labvantage.sapphire.modules.eventmanager.eventobject.BaseSDCEventObject;
import sapphire.util.SDIData;
import sapphire.xml.PropertyList;

public class PostDataReleaseEventObject
extends BaseSDCEventObject {
    public static final String EVENT_POSTDATARELEASE = "PostDataRelease";

    public PostDataReleaseEventObject(String sdcid, PropertyList sdcProps, SDIData sdiData, PropertyList properties, String releaseFlag, boolean allDataItemsReleased, boolean allMandatoryDataItemsReleased) {
        super(EVENT_POSTDATARELEASE, new Object[]{sdiData, properties, releaseFlag, allDataItemsReleased, allMandatoryDataItemsReleased}, sdcid, sdcProps, properties, sdiData);
    }

    public boolean isRelease() {
        return ((String)this.getEventData()[2]).equals("Y");
    }

    public boolean isUnrelease() {
        return ((String)this.getEventData()[2]).equals("N");
    }

    public void setAllDataItemsReleased(boolean allDataItemsReleased) {
        this.getEventData()[3] = allDataItemsReleased;
    }

    public boolean isAllDataItemsReleased() {
        return (Boolean)this.getEventData()[3];
    }

    public void setAllMandatoryDataItemsReleased(boolean allMandatoryDataItemsReleased) {
        this.getEventData()[4] = allMandatoryDataItemsReleased;
    }

    public boolean isAllMandatoryDataItemsReleased() {
        return (Boolean)this.getEventData()[4];
    }

    @Override
    public int addEventItems() {
        return this.addEventItemsFromDataitems();
    }
}

