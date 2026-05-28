/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.sapphire.modules.eventmanager.EventManager;
import com.labvantage.sapphire.services.SapphireConnection;
import sapphire.SapphireException;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.xml.PropertyList;

public class LV_EventType
extends BaseSDCRules {
    @Override
    public void preAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        if (primary.getValue(0, "basedoneventtypeid").length() == 0) {
            throw new SapphireException("You cannot add or copy a primitive event type, i.e. one that is not based on another event type!");
        }
    }

    @Override
    public void preDelete(String rsetid, PropertyList actionProps) throws SapphireException {
        this.database.createPreparedResultSet("SELECT basedoneventtypeid, typeflag FROM eventtype, rsetitems WHERE rsetitems.sdcid = ? AND rsetitems.keyid1 = eventtype.eventtypeid AND rsetitems.rsetid = ?", new Object[]{"LV_EventType", rsetid});
        this.database.getNext();
        if (this.database.getValue("typeflag").equals("C")) {
            throw new SapphireException("You cannot delete core event types!");
        }
        if (this.database.getValue("basedoneventtypeid").length() == 0) {
            throw new SapphireException("You cannot delete primitive event types, i.e. one that is not based on another event type!");
        }
        String eventplanCheck = "SELECT eventplanitem.eventplanid, eventplanitem.eventplanversionid FROM   eventplanitem, rsetitems WHERE  rsetitems.rsetid = ? AND    eventplanitem.eventtypeid = rsetitems.keyid1 ORDER BY 1, 2";
        this.database.createPreparedResultSet(eventplanCheck, new Object[]{rsetid});
        StringBuffer eventplanRefs = new StringBuffer();
        for (int i = 0; i < 10 && this.database.getNext(); ++i) {
            eventplanRefs.append("<br/>").append(this.database.getString("eventplanid")).append(": ").append(this.database.getString("eventplanversionid"));
        }
        if (eventplanRefs.length() > 0) {
            boolean more = this.database.getNext();
            this.throwError("EventTypeUsed", "VALIDATION", "Event type cannot be deleted because of " + (more ? "at least" : "") + " the following event plan references:" + eventplanRefs + (more ? "<br/>..." : ""));
        }
    }

    @Override
    public void postDelete(String rsetid, PropertyList actionProps) throws SapphireException {
        EventManager.loadEventPlans(new SapphireConnection(this.database.getConnection(), this.connectionInfo));
    }
}

