/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.wap;

import com.labvantage.sapphire.modules.wap.activity.WAPCommands;
import com.labvantage.sapphire.modules.wap.activity.WAPConstants;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.xml.PropertyList;

public class SetActivityStatus
extends BaseAction
implements WAPConstants,
sapphire.action.SetActivityStatus {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        WAPCommands commands = new WAPCommands(this.getConnectionid());
        String activitylist = properties.getProperty("activityid");
        String status = properties.getProperty("status");
        String tracelogid = this.createDatabaseTransactionLogId("LV_Activity", activitylist, "", "", properties);
        if ("Draft".equals(status)) {
            commands.unactivateActivities(activitylist, tracelogid);
        } else if ("Activated".equals(status)) {
            commands.activateActivities(activitylist, tracelogid);
        } else if ("In Progress".equals(status)) {
            commands.startActivities(activitylist, tracelogid);
        } else if ("Completed".equals(status)) {
            commands.stopActivities(activitylist, tracelogid, properties);
        } else if ("Cancelled".equals(status)) {
            commands.cancelActivities(activitylist, tracelogid);
        }
    }
}

