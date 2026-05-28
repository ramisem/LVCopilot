/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.wap;

import com.labvantage.sapphire.modules.wap.activity.WAPCommands;
import com.labvantage.sapphire.modules.wap.activity.WAPConstants;
import java.util.ArrayList;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class SetActivityResource
extends BaseAction
implements WAPConstants,
sapphire.action.SetActivityResource {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        WAPCommands commands = new WAPCommands(this.getConnectionid());
        String activityProp = properties.getProperty("activityid");
        boolean reset = properties.getProperty("reset").equals("Y");
        if (reset) {
            String[] activities;
            for (String activityid : activities = StringUtil.split(activityProp, ";")) {
                commands.resetResources(activityid);
            }
        } else {
            String resourceNumProp = properties.getProperty("resourcenum");
            if (resourceNumProp.length() > 0) {
                DataSet propsDS = new DataSet();
                String resourceIdProp = properties.getProperty("resourcekeyid1");
                String resourceSdcidProp = properties.getProperty("resourcesdcid");
                if (activityProp.contains(";") && resourceNumProp.contains(";") && StringUtil.split(activityProp, ";").length != StringUtil.split(resourceNumProp, ";").length) {
                    throw new ActionException("Mismatch detected between the number of activites and the number of resource nums");
                }
                propsDS.addColumnValues("activityid", 0, activityProp, ";");
                propsDS.addColumnValues("resourcenum", 0, resourceNumProp, ";");
                propsDS.addColumnValues("resourcekeyid1", 0, resourceIdProp, ";");
                propsDS.addColumnValues("resourcesdcid", 0, resourceSdcidProp, ";");
                propsDS.padColumns();
                propsDS.sort("activityid");
                ArrayList<DataSet> eachActivity = propsDS.getGroupedDataSets("activityid");
                for (DataSet thisActivity : eachActivity) {
                    String activityid = thisActivity.getValue(0, "activityid");
                    DataSet resources = commands.getActivityResources(activityid);
                    if (resources.size() > 0) {
                        String resourceNum = thisActivity.getColumnValues("resourcenum", ";");
                        String resourceSDI = thisActivity.getColumnValues("resourcekeyid1", ";");
                        String resourceSDCID = thisActivity.getColumnValues("resourcesdcid", ";");
                        commands.updateResourceSDIByNum(activityid, resources, resourceNum, resourceSDI, resourceSDCID);
                        continue;
                    }
                    this.logger.warn("No resources found for activity " + activityid + ".");
                }
            } else {
                String[] activities = StringUtil.split(activityProp, ";");
                String analystid = properties.getProperty("analystid");
                String analysttype = properties.getProperty("analysttype");
                String instrumentid = properties.getProperty("instrumentid");
                String analystworkareaid = properties.getProperty("analystworkareaid");
                String instrumentworkareaid = properties.getProperty("instrumentworkareaid");
                String instrumentmodelid = properties.getProperty("instrumentmodelid");
                String instrumenttypeid = properties.getProperty("instrumenttypeid");
                for (int i = 0; i < activities.length; ++i) {
                    String activityid = activities[i];
                    DataSet resources = commands.getActivityResources(activityid);
                    if (resources.size() > 0) {
                        commands.updateResourceSDIsByInferring(activityid, resources, analystid, analysttype, analystworkareaid, instrumentid, instrumentworkareaid, instrumenttypeid, instrumentmodelid);
                        continue;
                    }
                    this.logger.warn("No resources found for activity " + activityid + ".");
                }
            }
        }
    }
}

