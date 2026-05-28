/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.eventmanager.eventobject;

import com.labvantage.sapphire.modules.eventmanager.eventobject.BaseEventItem;
import java.util.ArrayList;

public class EventItem
extends BaseEventItem {
    private String eventitemid;

    public EventItem(String eventitemid) {
        this.eventitemid = eventitemid;
    }

    public String getEventitemid() {
        return this.eventitemid;
    }

    @Override
    public void logEvent(String eventplanid, String eventplanversionid, String eventitemid, String action, String actionitemid, String summary, String log) {
    }

    @Override
    public ArrayList getLogEvents(String eventplanid, String eventplanversionid, String eventplanitemid) {
        return null;
    }

    @Override
    public String toString() {
        return this.eventitemid;
    }
}

