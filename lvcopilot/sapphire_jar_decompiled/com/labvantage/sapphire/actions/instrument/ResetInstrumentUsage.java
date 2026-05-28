/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.instrument;

import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.ScheduleService;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.ActionBlock;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class ResetInstrumentUsage
extends BaseAction
implements sapphire.action.ResetInstrumentUsage {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String[] instruments = StringUtil.split(properties.getProperty("instrumentid", ""), ";");
        String[] partsReset = StringUtil.split(properties.getProperty("resetinstrumentparts", ""), ";");
        String[] planitemReset = StringUtil.split(properties.getProperty("resetplanitemcurrentusage", ""), ";");
        String[] countReset = StringUtil.split(properties.getProperty("resettotalusagecount", ""), ";");
        String[] timeReset = StringUtil.split(properties.getProperty("resettotalusagetime", ""), ";");
        HashMap<String, String> totalUsageCount = new HashMap<String, String>();
        totalUsageCount.put("sdcid", "Instrument");
        totalUsageCount.put("keyid1", "");
        totalUsageCount.put("totalusagecount", "");
        HashMap<String, String> totalUsageTime = new HashMap<String, String>();
        totalUsageTime.put("sdcid", "Instrument");
        totalUsageTime.put("keyid1", "");
        totalUsageTime.put("totalusagetime", "");
        HashMap<String, String> planitemUsageTime = new HashMap<String, String>();
        planitemUsageTime.put("scheduleplanid", "");
        planitemUsageTime.put("scheduleplanitemid", "");
        planitemUsageTime.put("resettozero", "");
        PreparedStatement childPlanItems = this.database.prepareStatement("childPlanItems", "SELECT scheduleplanid, scheduleplanitemid FROM scheduleplanitem WHERE linksdcid = 'Instrument' AND linkkeyid1=?");
        HashMap<String, String> partUsageMap = new HashMap<String, String>();
        partUsageMap.put("instrumentid", "");
        partUsageMap.put("resetinstrumentparts", "");
        partUsageMap.put("resettotalusagecount", "");
        partUsageMap.put("resettotalusagetime", "");
        PreparedStatement instrumentParts = this.database.prepareStatement("instrumentParts", "SELECT instrumentid FROM instrument WHERE parentinstrumentid = ?");
        for (int i = 0; i < instruments.length; ++i) {
            int j;
            boolean countResetFlag = true;
            boolean timeResetFlag = true;
            boolean planitemResetFlag = true;
            boolean partsResetFlag = true;
            if (countReset.length > i && "N".equals(countReset[i])) {
                countResetFlag = false;
            }
            if (countResetFlag) {
                totalUsageCount.put("keyid1", (String)totalUsageCount.get("keyid1") + ";" + instruments[i]);
                totalUsageCount.put("totalusagecount", (String)totalUsageCount.get("totalusagecount") + ";0");
            }
            if (timeReset.length > i && "N".equals(timeReset[i])) {
                timeResetFlag = false;
            }
            if (timeResetFlag) {
                totalUsageTime.put("keyid1", (String)totalUsageTime.get("keyid1") + ";" + instruments[i]);
                totalUsageTime.put("totalusagetime", (String)totalUsageTime.get("totalusagetime") + ";0");
            }
            if (planitemReset.length > i && "N".equals(planitemReset[i])) {
                planitemResetFlag = false;
            }
            if (planitemResetFlag) {
                try {
                    childPlanItems.setString(1, instruments[i]);
                    DataSet childPlanItemsDs = new DataSet(childPlanItems.executeQuery());
                    for (j = 0; j < childPlanItemsDs.getRowCount(); ++j) {
                        planitemUsageTime.put("scheduleplanid", (String)planitemUsageTime.get("scheduleplanid") + ";" + childPlanItemsDs.getValue(j, "scheduleplanid"));
                        planitemUsageTime.put("scheduleplanitemid", planitemUsageTime.get("scheduleplanitemid") + ";" + childPlanItemsDs.getValue(j, "scheduleplanitemid"));
                        planitemUsageTime.put("resettozero", planitemUsageTime.get("resettozero") + ";Y");
                    }
                }
                catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (partsReset.length > i && "N".equals(partsReset[i])) {
                partsResetFlag = false;
            }
            if (!partsResetFlag) continue;
            try {
                instrumentParts.setString(1, instruments[i]);
                DataSet instrumentPartsDs = new DataSet(instrumentParts.executeQuery());
                for (j = 0; j < instrumentPartsDs.getRowCount(); ++j) {
                    partUsageMap.put("instrumentid", (String)partUsageMap.get("instrumentid") + ";" + instrumentPartsDs.getValue(j, "instrumentid"));
                    partUsageMap.put("resetinstrumentparts", (String)partUsageMap.get("resetinstrumentparts") + ";N");
                    partUsageMap.put("resettotalusagecount", (String)partUsageMap.get("resettotalusagecount") + (countResetFlag ? ";Y" : ";N"));
                    partUsageMap.put("resettotalusagetime", (String)partUsageMap.get("resettotalusagetime") + (timeResetFlag ? ";Y" : ";N"));
                }
                continue;
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
        this.database.closeStatement("childPlanItems");
        this.database.closeStatement("instrumentParts");
        ActionBlock actionBlock = new ActionBlock();
        if (((String)totalUsageCount.get("keyid1")).length() > 0) {
            totalUsageCount.put("keyid1", ((String)totalUsageCount.get("keyid1")).substring(1));
            totalUsageCount.put("totalusagecount", ((String)totalUsageCount.get("totalusagecount")).substring(1));
            actionBlock.setAction("totalUsageCount", "EditSDI", "1", totalUsageCount);
        }
        if (((String)totalUsageTime.get("keyid1")).length() > 0) {
            totalUsageTime.put("keyid1", ((String)totalUsageTime.get("keyid1")).substring(1));
            totalUsageTime.put("totalusagetime", ((String)totalUsageTime.get("totalusagetime")).substring(1));
            actionBlock.setAction("totalUsageTime", "EditSDI", "1", totalUsageTime);
        }
        if (((String)planitemUsageTime.get("scheduleplanid")).length() > 0) {
            planitemUsageTime.put("scheduleplanid", ((String)planitemUsageTime.get("scheduleplanid")).substring(1));
            planitemUsageTime.put("scheduleplanitemid", ((String)planitemUsageTime.get("scheduleplanitemid")).substring(1));
            planitemUsageTime.put("resettozero", ((String)planitemUsageTime.get("resettozero")).substring(1));
            ScheduleService ss = new ScheduleService((SapphireConnection)this.connectionInfo);
            ss.resetCounter(planitemUsageTime);
        }
        if (((String)partUsageMap.get("instrumentid")).length() > 0) {
            partUsageMap.put("instrumentid", ((String)partUsageMap.get("instrumentid")).substring(1));
            partUsageMap.put("resetinstrumentparts", ((String)partUsageMap.get("resetinstrumentparts")).substring(1));
            partUsageMap.put("resettotalusagecount", ((String)partUsageMap.get("resettotalusagecount")).substring(1));
            partUsageMap.put("resettotalusagetime", ((String)partUsageMap.get("resettotalusagetime")).substring(1));
            PropertyList pl = new PropertyList(partUsageMap);
            this.processAction(pl);
        }
        this.getActionProcessor().processActionBlock(actionBlock);
    }
}

