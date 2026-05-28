/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.wap;

import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.modules.wap.activity.Activity;
import com.labvantage.sapphire.modules.wap.activity.ActivityClassHandler;
import com.labvantage.sapphire.modules.wap.activity.NewWorkDetails;
import com.labvantage.sapphire.modules.wap.activity.WAPCommands;
import com.labvantage.sapphire.modules.wap.activity.WAPConstants;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class CreateActivity
extends BaseAction
implements WAPConstants,
sapphire.action.CreateActivity {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        WAPCommands commands = new WAPCommands(this.getConnectionid());
        PropertyList wapPolicy = this.getConfigurationProcessor().getPolicy("WAPPolicy", "Sapphire Custom");
        Activity createActivity = new Activity();
        createActivity.setActivityClass(properties.getProperty("activityclass", "Batch Testing"));
        String worksdcid = properties.getProperty("worksdcid");
        ActivityClassHandler activityClassHandler = ActivityClassHandler.getInstance(this.getConnectionid(), wapPolicy, createActivity.getActivityClass(), worksdcid);
        createActivity.setLabel(properties.getProperty("label", activityClassHandler == null ? "New Activity" : activityClassHandler.getLabel()));
        createActivity.setTimeMode(properties.getProperty("timemode", "None"));
        DateTimeUtil dtu = new DateTimeUtil(this.connectionInfo);
        createActivity.setStartDateInstantUTC(dtu.getInstant(properties.getProperty("startdt")));
        if (properties.getProperty("enddtfixed").equals("Y")) {
            createActivity.setEndDateInstantUTC(dtu.getInstant(properties.getProperty("enddt")));
        }
        createActivity.setStartRangeInstantUTC(dtu.getInstant(properties.getProperty("startrangedt")));
        createActivity.setEndRangeInstantUTC(dtu.getInstant(properties.getProperty("endrangedt")));
        createActivity.setStatus("Draft");
        String workkeyid1 = properties.getProperty("workkeyid1");
        String workkeyid2 = properties.getProperty("workkeyid2");
        String workkeyid3 = properties.getProperty("workkeyid3");
        String[] workkeyid1array = StringUtil.split(workkeyid1, ";");
        int workCount = workkeyid1array.length;
        createActivity.setMaxActivitySize(1000);
        createActivity.setWorksdcid(worksdcid);
        if (createActivity.getLabel().contains("[") && activityClassHandler != null) {
            String label = createActivity.getLabel();
            label = activityClassHandler.replaceDateTokens(label);
            label = activityClassHandler.replaceTokensFromActivity(label, createActivity);
            createActivity.setLabel(label);
        }
        NewWorkDetails newWorkDetails = null;
        if (worksdcid.length() > 0 && workkeyid1.length() > 0) {
            newWorkDetails = commands.getNewWorkDetails(activityClassHandler, worksdcid, workkeyid1);
            createActivity.setWorkContext(newWorkDetails.getWorkContext());
            createActivity.setTestingDepartmentid(newWorkDetails.getTestingDepartmentid());
            createActivity.setMaxActivitySize(newWorkDetails.getMaxActivitySize());
            createActivity.setActivitySize(workCount);
            commands.setContextSDI(createActivity, worksdcid, workkeyid1array[0], workkeyid2.length() > 0 ? StringUtil.split(workkeyid2, ";")[0] : "", workkeyid3.length() > 0 ? StringUtil.split(workkeyid3, ";")[0] : "", wapPolicy);
            if (createActivity.getLabel().contains("[")) {
                createActivity.setLabel(activityClassHandler.replaceTokensFromWorkSDI(createActivity.getLabel(), workkeyid1array[0]));
            }
        }
        if (newWorkDetails != null && createActivity.getActivitySize() > createActivity.getMaxActivitySize()) {
            if (!properties.getProperty("multibysize", "Y").equals("Y")) {
                throw new ActionException("Maximum number of worksdis (" + createActivity.getMaxActivitySize() + ") exceeded.");
            }
            DataSet temp = new DataSet();
            temp.addColumnValues("workkeyid1", 0, workkeyid1, ";");
            temp.addColumnValues("workkeyid2", 0, workkeyid2, ";", "__null");
            temp.addColumnValues("workkeyid3", 0, workkeyid3, ";", "__null");
            temp.padColumns();
            int maxActivitySize = newWorkDetails.getMaxActivitySize();
            String returnActivityList = "";
            for (int start = 0; start < temp.size(); start += maxActivitySize) {
                int end = start + maxActivitySize > temp.size() ? temp.size() : start + maxActivitySize;
                String workkeyid1Temp = temp.getColumnValues("workkeyid1", start, end, ";");
                String workkeyid2Temp = temp.getColumnValues("workkeyid2", start, end, ";");
                String workkeyid3Temp = temp.getColumnValues("workkeyid3", start, end, ";");
                int workCountTemp = StringUtil.split(workkeyid1Temp, ";").length;
                createActivity.setActivitySize(workCountTemp);
                String activityid = commands.createActivity(createActivity);
                returnActivityList = returnActivityList + ";" + activityid;
                Activity activity = commands.getActivityDetails(activityid);
                newWorkDetails.setWorkCount(workCountTemp);
                commands.addWorkSDI(activity, worksdcid, workkeyid1Temp, workkeyid2Temp, workkeyid3Temp, newWorkDetails);
                this.addResources(properties, commands, newWorkDetails, activityid, activity);
            }
            String string = returnActivityList = returnActivityList.length() > 0 ? returnActivityList.substring(1) : "";
            if (properties.getProperty("autoactivate").equals("Y")) {
                commands.activateActivities(returnActivityList, null);
            }
            properties.setProperty("activityid", returnActivityList);
        } else {
            String activityid = commands.createActivity(createActivity);
            properties.setProperty("activityid", activityid);
            if (newWorkDetails != null) {
                Activity activity = commands.getActivityDetails(activityid);
                commands.addWorkSDI(activity, worksdcid, workkeyid1, workkeyid2, workkeyid3, newWorkDetails);
                this.addResources(properties, commands, newWorkDetails, activityid, activity);
            }
            if (properties.getProperty("autoactivate").equals("Y")) {
                commands.activateActivities(activityid, null);
            }
        }
    }

    public void addResources(PropertyList properties, WAPCommands commands, NewWorkDetails newWorkDetails, String activityid, Activity activity) throws SapphireException {
        DataSet resourceRequirements = newWorkDetails.getResourceRequirements();
        if (resourceRequirements != null && resourceRequirements.size() > 0) {
            String resourceNum = properties.getProperty("resourcenum");
            String resourceId = properties.getProperty("resourcekeyid1");
            String resourceSDCid = properties.getProperty("resourcesdcid");
            String analystid = properties.getProperty("analystid");
            String analystType = properties.getProperty("analysttype");
            String analystworkareaid = properties.getProperty("analystworkareaid");
            String instrumentid = properties.getProperty("instrumentid");
            String instrumentmodelid = properties.getProperty("instrumentmodelid");
            String instrumenttypeid = properties.getProperty("instrumenttypeid");
            String instrumentworkareaid = properties.getProperty("instrumentworkareaid");
            if (resourceNum.length() > 0) {
                commands.updateResourceSDIByNum(activityid, resourceRequirements, resourceNum, resourceId, resourceSDCid);
            } else if (analystid.length() > 0 || instrumentid.length() > 0 || analystworkareaid.length() > 0 || instrumentworkareaid.length() > 0) {
                commands.updateResourceSDIsByInferring(activityid, resourceRequirements, analystid, analystType, analystworkareaid, instrumentid, instrumentworkareaid, instrumenttypeid, instrumentmodelid);
            }
        }
        commands.syncMaxDurationEndDateDueDateCompleteCount(activity);
    }
}

