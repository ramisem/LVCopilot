/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.eventmanager.eventobject;

import com.labvantage.sapphire.modules.eventmanager.eventobject.BaseEventObject;
import com.labvantage.sapphire.modules.eventmanager.eventobject.EventItem;

public class AlertRaisedEventObject
extends BaseEventObject {
    public static final String EVENT_ALERTRAISED = "AlertRaised";

    public AlertRaisedEventObject(String incidentid, String incidenttype, String severity, String description, String explanation, String causalSDCid, String causalkeyid1, String causalkeyid2, String causalkeyid3) {
        super(EVENT_ALERTRAISED, new Object[]{incidentid, incidenttype, severity, description, explanation, causalSDCid, causalkeyid1, causalkeyid2, causalkeyid3});
    }

    public String getIncidentId() {
        return (String)this.getEventData()[0];
    }

    public String getIncidentType() {
        return (String)this.getEventData()[1];
    }

    public String getSeverity() {
        return (String)this.getEventData()[2];
    }

    public String getDescription() {
        return (String)this.getEventData()[3];
    }

    public String getExplanation() {
        return (String)this.getEventData()[4];
    }

    public String getCausalSDCid() {
        return (String)this.getEventData()[5];
    }

    public String getCausalKeyid1() {
        return (String)this.getEventData()[6];
    }

    public String getCausalKeyid2() {
        return (String)this.getEventData()[7];
    }

    public String getCausalKeyid3() {
        return (String)this.getEventData()[8];
    }

    @Override
    public int addEventItems() {
        this.addEventItem(new EventItem(this.getIncidentId()));
        return 1;
    }
}

