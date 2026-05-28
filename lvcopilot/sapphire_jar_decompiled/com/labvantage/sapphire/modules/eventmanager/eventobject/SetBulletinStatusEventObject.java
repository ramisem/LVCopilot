/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.eventmanager.eventobject;

import com.labvantage.sapphire.modules.eventmanager.eventobject.BaseEventObject;
import com.labvantage.sapphire.modules.eventmanager.eventobject.EventItem;

public class SetBulletinStatusEventObject
extends BaseEventObject {
    public static final String EVENT_SETBULLETINSTATUS = "SetBulletinStatus";

    public SetBulletinStatusEventObject(String username, String status) {
        super(EVENT_SETBULLETINSTATUS, new Object[]{username, status});
    }

    public String getUsername() {
        return (String)this.getEventData()[0];
    }

    public String getStatus() {
        return (String)this.getEventData()[1];
    }

    @Override
    public int addEventItems() {
        this.addEventItem(new EventItem(this.getUsername()));
        return 1;
    }
}

