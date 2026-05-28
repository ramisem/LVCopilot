/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.wap;

import com.labvantage.sapphire.modules.wap.activity.Activity;
import com.labvantage.sapphire.modules.wap.activity.WAPCommands;
import com.labvantage.sapphire.modules.wap.activity.WAPConstants;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.xml.PropertyList;

public class RemoveActivityWorkSDI
extends BaseAction
implements WAPConstants,
sapphire.action.RemoveActivityWorkSDI {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        WAPCommands commands = new WAPCommands(this.getConnectionid());
        String activityid = properties.getProperty("activityid");
        String worksdcid = properties.getProperty("worksdcid");
        String workkeyid1 = properties.getProperty("workkeyid1");
        String workkeyid2 = properties.getProperty("workkeyid2");
        String workkeyid3 = properties.getProperty("workkeyid3");
        if (worksdcid.length() > 0 && workkeyid1.length() > 0) {
            Activity activity = commands.getActivityDetails(activityid);
            commands.removeWorkSDI(activityid, worksdcid, workkeyid1, workkeyid2, workkeyid3);
            commands.updateResourceDurations(activityid);
            int currentWorkCount = this.getQueryProcessor().getPreparedCount("SELECT count(*) FROM activityworksdi WHERE activityid=?", new String[]{activity.getActivityid()});
            Activity editActivity = new Activity();
            editActivity.setActivityid(activityid);
            editActivity.setActivitySize(activity.getActivitySize() != currentWorkCount ? currentWorkCount : -1);
            commands.editActivity(editActivity, false);
            if (activity.getStatus().equals("Draft") || activity.getStatus().equals("Activated") || activity.getStatus().equals("In Progress")) {
                commands.updateResourceDurations(activityid);
                commands.syncMaxDurationEndDateDueDateCompleteCount(activity);
            }
            if (properties.getProperty("setcancelled").equals("Y")) {
                commands.setWAPStatus(worksdcid, workkeyid1, workkeyid2, workkeyid3, "Cancelled", false);
            } else if (properties.getProperty("setpending").equals("Y")) {
                commands.setWAPStatus(worksdcid, workkeyid1, workkeyid2, workkeyid3, "Pending", false);
            }
        }
    }
}

