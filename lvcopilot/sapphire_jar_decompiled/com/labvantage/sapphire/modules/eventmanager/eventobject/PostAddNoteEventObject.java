/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.eventmanager.eventobject;

import com.labvantage.sapphire.modules.eventmanager.eventobject.BaseSDCEventObject;
import sapphire.util.SDIData;
import sapphire.xml.PropertyList;

public class PostAddNoteEventObject
extends BaseSDCEventObject {
    public static final String EVENT_POSTADDNOTE = "PostAddNote";

    public PostAddNoteEventObject(String sdcid, PropertyList sdcProps, SDIData sdiData, PropertyList properties) {
        super(EVENT_POSTADDNOTE, new Object[]{sdiData, properties, null}, sdcid, sdcProps, properties, sdiData);
    }

    @Override
    public int addEventItems() {
        return this.addEventItemsFromNotes();
    }
}

