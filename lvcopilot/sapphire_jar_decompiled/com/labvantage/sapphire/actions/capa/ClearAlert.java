/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.capa;

import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.accessor.ActionProcessor;
import sapphire.action.BaseAction;
import sapphire.xml.PropertyList;

public class ClearAlert
extends BaseAction
implements sapphire.action.ClearAlert {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String incidentid = properties.getProperty("incidentid");
        String resolution = properties.getProperty("resolution");
        String triage = properties.getProperty("triage");
        String suppressDuration = properties.getProperty("suppressduration");
        ActionProcessor ap = this.getActionProcessor();
        PropertyList props = new PropertyList();
        props.put("sdcid", "LV_Incdt");
        props.put("keyid1", incidentid);
        props.put("triage", triage);
        props.put("resolution", resolution);
        if (suppressDuration.length() > 0) {
            props.put("suppressuntildt", suppressDuration.startsWith("n+") ? suppressDuration : "n+" + suppressDuration);
        }
        props.setProperty("incidentstatus", "Completed");
        try {
            ap.processAction("EditSDI", "1", props);
        }
        catch (ActionException e) {
            this.logger.error("Error running EditSDI. Could not set the initial status on the incident.", e);
        }
    }
}

