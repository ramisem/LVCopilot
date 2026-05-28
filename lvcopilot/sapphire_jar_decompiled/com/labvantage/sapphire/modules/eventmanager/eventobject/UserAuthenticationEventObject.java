/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.eventmanager.eventobject;

import com.labvantage.sapphire.modules.eventmanager.eventobject.BaseEventObject;
import com.labvantage.sapphire.modules.eventmanager.eventobject.EventItem;

public class UserAuthenticationEventObject
extends BaseEventObject {
    public static final String EVENT_USERAUTHENTICATION = "UserAuthentication";

    public UserAuthenticationEventObject(String username, String databaseid, String activity, String disableReason, int logonAttempts) {
        super(EVENT_USERAUTHENTICATION, new Object[]{username, databaseid, activity, disableReason, logonAttempts, "", ""});
    }

    public UserAuthenticationEventObject(String username, String databaseid, String activity, String errorid, String errormesage) {
        super(EVENT_USERAUTHENTICATION, new Object[]{username, databaseid, activity, "", 0, errorid, errormesage});
    }

    public String getUsername() {
        return (String)this.getEventData()[0];
    }

    public String getDatabaseid() {
        return (String)this.getEventData()[1];
    }

    public String getActivity() {
        return (String)this.getEventData()[2];
    }

    public String getDisableReason() {
        return (String)this.getEventData()[3];
    }

    public int getLogonAttempts() {
        return (Integer)this.getEventData()[4];
    }

    public String getErrorid() {
        return (String)this.getEventData()[5];
    }

    public String getErrormessage() {
        return (String)this.getEventData()[6];
    }

    public String getLogonSuccess() {
        return this.getDisableReason().length() == 0 && this.getLogonAttempts() == 0 ? "Y" : "N";
    }

    @Override
    public int addEventItems() {
        this.addEventItem(new EventItem(this.getUsername()));
        return 1;
    }
}

