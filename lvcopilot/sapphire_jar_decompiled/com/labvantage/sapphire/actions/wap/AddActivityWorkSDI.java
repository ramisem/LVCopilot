/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.wap;

import com.labvantage.sapphire.modules.wap.activity.Activity;
import com.labvantage.sapphire.modules.wap.activity.NewWorkDetails;
import com.labvantage.sapphire.modules.wap.activity.WAPCommands;
import com.labvantage.sapphire.modules.wap.activity.WAPConstants;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.xml.PropertyList;

public class AddActivityWorkSDI
extends BaseAction
implements WAPConstants,
sapphire.action.AddActivityWorkSDI {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        WAPCommands commands = new WAPCommands(this.getConnectionid());
        String activityid = properties.getProperty("activityid");
        String worksdcid = properties.getProperty("worksdcid");
        String workkeyid1 = properties.getProperty("workkeyid1");
        String workkeyid2 = properties.getProperty("workkeyid2");
        String workkeyid3 = properties.getProperty("workkeyid3");
        boolean updateActivityContextSDI = false;
        PropertyList wapPolicy = this.getConfigurationProcessor().getPolicy("WAPPolicy", "Sapphire Custom");
        if (worksdcid.length() > 0 && workkeyid1.length() > 0) {
            Activity activity = commands.getActivityDetails(activityid);
            if (activity.getActivitySize() == 0) {
                commands.setContextSDI(activity, worksdcid, workkeyid1, workkeyid2, workkeyid3, wapPolicy);
                updateActivityContextSDI = true;
            }
            NewWorkDetails newWorkDetails = commands.addWorkSDI(activity, worksdcid, workkeyid1, workkeyid2, workkeyid3, null);
            commands.updateResourceDurations(activityid);
            commands.syncActivityWorkDetails(activity, newWorkDetails, updateActivityContextSDI);
            commands.syncMaxDurationEndDateDueDateCompleteCount(activity);
            if (activity.getStatus().equals("Activated") || activity.getStatus().equals("In Progress")) {
                commands.pushDownPlannedStartDt(activityid, true);
            }
        }
    }
}

