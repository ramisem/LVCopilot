/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.wap;

import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.modules.wap.activity.Activity;
import com.labvantage.sapphire.modules.wap.activity.WAPCommands;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.xml.PropertyList;

public class EditActivity
extends BaseAction
implements sapphire.action.EditActivity {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        WAPCommands commands = new WAPCommands(this.getConnectionid());
        Activity activity = new Activity();
        activity.setActivityid(properties.getProperty("activityid"));
        if (properties.containsKey("label")) {
            activity.setLabel(properties.getProperty("label"));
        }
        DateTimeUtil dtu = new DateTimeUtil(this.connectionInfo);
        if (properties.containsKey("startdt")) {
            activity.setStartDateInstantUTC(dtu.getInstant(properties.getProperty("startdt")));
        }
        if (properties.getProperty("enddtfixed").equals("Y")) {
            activity.setEndDateInstantUTC(dtu.getInstant(properties.getProperty("enddt")));
        }
        activity.setStartRangeInstantUTC(dtu.getInstant(properties.getProperty("startrangedt")));
        activity.setEndRangeInstantUTC(dtu.getInstant(properties.getProperty("endrangedt")));
        commands.editActivity(activity, true);
    }
}

