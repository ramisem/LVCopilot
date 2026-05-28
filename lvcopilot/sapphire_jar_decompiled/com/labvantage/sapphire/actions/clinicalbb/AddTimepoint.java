/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.clinicalbb;

import com.labvantage.sapphire.actions.clinicalbb.AddEvent;
import sapphire.SapphireException;
import sapphire.accessor.SDCProcessor;
import sapphire.action.BaseAction;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class AddTimepoint
extends BaseAction
implements sapphire.action.AddTimepoint {
    static final String LABVANTAGE_CVS_ID = "$Revision: 67311 $";

    @Override
    public void processAction(PropertyList actionProps) throws SapphireException {
        PropertyList props = new PropertyList();
        props.setProperty("visiteventid", actionProps.getProperty("visiteventid", ""));
        props.setProperty("eventlabel", actionProps.getProperty("eventlabel", ""));
        props.setProperty("eventdt", actionProps.getProperty("eventdt", ""));
        props.setProperty("eventstatus", actionProps.getProperty("eventstatus", ""));
        props.setProperty("adhocevtflag", actionProps.getProperty("adhocevtflag", ""));
        props.setProperty("auditreason", actionProps.getProperty("auditreason", ""));
        props.setProperty("auditactivity", actionProps.getProperty("auditactivity", ""));
        props.setProperty("eventdefid", actionProps.getProperty("eventdefid", ""));
        props.setProperty("auditsignedflag", actionProps.getProperty("auditsignedflag", "N"));
        props.setProperty("eventtype", "Timepoint");
        props.setProperty("addeventpolicy", actionProps.getProperty("addeventpolicy"));
        SDCProcessor sdcProcessor = this.getSDCProcessor();
        PropertyListCollection columns = sdcProcessor.getColumns("LV_ParticipantEvent");
        for (int col = 0; col < columns.size(); ++col) {
            PropertyList column = columns.getPropertyList(col);
            String colid = column.getProperty("columnid").toLowerCase();
            if (!actionProps.containsKey("timepoint_" + colid)) continue;
            props.setProperty("event_" + colid, actionProps.getProperty("timepoint_" + colid, ""));
        }
        this.getActionProcessor().processActionClass(AddEvent.class.getName(), props);
        String eventId = props.getProperty("neweventid");
        actionProps.setProperty("neweventid", eventId);
    }
}

