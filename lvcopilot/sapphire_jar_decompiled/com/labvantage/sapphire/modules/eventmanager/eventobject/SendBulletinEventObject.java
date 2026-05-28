/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.eventmanager.eventobject;

import com.labvantage.sapphire.modules.eventmanager.eventobject.BaseEventObject;
import com.labvantage.sapphire.modules.eventmanager.eventobject.EventItem;

public class SendBulletinEventObject
extends BaseEventObject {
    public static final String EVENT_SENDBULLETIN = "SendBulletin";

    public SendBulletinEventObject(String[] usernames, String description, String body, String priority, String url) {
        super(EVENT_SENDBULLETIN, new Object[]{usernames, description, body, priority, url});
    }

    public String[] getUsernames() {
        return (String[])this.getEventData()[0];
    }

    public String getDescription() {
        return (String)this.getEventData()[1];
    }

    public String getBody() {
        return (String)this.getEventData()[2];
    }

    public String getPriority() {
        return (String)this.getEventData()[3];
    }

    public String getUrl() {
        return (String)this.getEventData()[4];
    }

    @Override
    public int addEventItems() {
        String[] usernames = this.getUsernames();
        for (int i = 0; i < usernames.length; ++i) {
            this.addEventItem(new EventItem(usernames[i]));
        }
        return usernames.length;
    }
}

