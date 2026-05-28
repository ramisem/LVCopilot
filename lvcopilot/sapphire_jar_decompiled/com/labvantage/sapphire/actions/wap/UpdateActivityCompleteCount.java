/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.wap;

import com.labvantage.sapphire.actions.wap.SetActivityStatus;
import com.labvantage.sapphire.modules.wap.activity.Activity;
import com.labvantage.sapphire.modules.wap.activity.ActivityClassHandler;
import com.labvantage.sapphire.modules.wap.activity.WAPCommands;
import com.labvantage.sapphire.modules.wap.activity.WAPConstants;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.action.BaseAction;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class UpdateActivityCompleteCount
extends BaseAction
implements WAPConstants,
sapphire.action.UpdateActivityCompleteCount {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        WAPCommands commands = new WAPCommands(this.getConnectionid());
        PropertyList wapPolicy = commands.getWapPolicy();
        String[] activityies = null;
        String worksdcid = properties.getProperty("worksdcid");
        String workkeyid1 = properties.getProperty("workkeyid1");
        String rsetid = properties.getProperty("workrsetid");
        String activitylist = properties.getProperty("activityid");
        if (activitylist.length() > 0) {
            activityies = StringUtil.split(activitylist, ";");
        } else if (worksdcid.length() > 0 && workkeyid1.length() > 0) {
            if (workkeyid1.contains(";")) {
                throw new ActionException("Only a single SDI can be passed into the action in this mode. Try passing an rsetid instead");
            }
            activitylist = commands.getActivityForWorkSDI(worksdcid, workkeyid1, properties.getProperty("workkeyid2"), properties.getProperty("workkeyid3"));
            if (activitylist.length() > 0) {
                activityies = StringUtil.split(activitylist, ";");
            }
        } else if (worksdcid.length() > 0 && rsetid.length() > 0 && (activitylist = commands.getActivitiesForWorkRset(worksdcid, rsetid)).length() > 0) {
            activityies = StringUtil.split(activitylist, ";");
        }
        if (activityies != null) {
            StringBuffer startActivities = new StringBuffer();
            for (String activityid : activityies) {
                Activity activity = commands.getActivityDetails(activityid);
                String status = activity.getStatus();
                if (!status.equals("Draft") && !status.equals("Activated") && !status.equals("In Progress")) continue;
                ActivityClassHandler activityClassHandler = ActivityClassHandler.getInstance(this.getConnectionid(), wapPolicy, activity.getActivityClass(), activity.getWorksdcid());
                int completeCount = commands.getWorkCompleteCount(activity);
                String sql = "UPDATE activity SET workcompletecount=? WHERE activityid=? AND (workcompletecount <> ? OR workcompletecount is null)";
                this.database.executePreparedUpdate(sql, new Object[]{completeCount, activityid, completeCount});
                if (completeCount == activity.getActivitySize()) {
                    PropertyList setstatus;
                    int cancelCount = -1;
                    if (activityClassHandler.isAutoCancel()) {
                        cancelCount = commands.getWorkCancelledCount(activity);
                    }
                    if (activityClassHandler.isAutoCancel() && cancelCount == activity.getActivitySize()) {
                        setstatus = new PropertyList();
                        setstatus.setProperty("activityid", activityid);
                        setstatus.setProperty("status", "Cancelled");
                        this.getActionProcessor().processActionClass(SetActivityStatus.class.getName(), setstatus);
                        continue;
                    }
                    if (activityClassHandler.isAutoComplete()) {
                        setstatus = new PropertyList();
                        setstatus.setProperty("activityid", activityid);
                        setstatus.setProperty("status", "Completed");
                        this.getActionProcessor().processActionClass(SetActivityStatus.class.getName(), setstatus);
                        continue;
                    }
                    if (!status.equals("Activated")) continue;
                    startActivities.append(";").append(activityid);
                    continue;
                }
                if (completeCount <= 0 || !status.equals("Activated")) continue;
                startActivities.append(";").append(activityid);
            }
            if (startActivities.length() > 0) {
                PropertyList setstatus = new PropertyList();
                setstatus.setProperty("activityid", startActivities.substring(1));
                setstatus.setProperty("status", "In Progress");
                setstatus.setProperty("actualstartdt", "n");
                this.getActionProcessor().processActionClass(SetActivityStatus.class.getName(), setstatus);
            }
        }
    }
}

