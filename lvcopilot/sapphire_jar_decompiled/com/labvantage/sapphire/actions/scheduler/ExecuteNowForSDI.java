/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.scheduler;

import com.labvantage.sapphire.gwt.shared.util.StringUtil;
import com.labvantage.sapphire.modules.em.MonitorGroup;
import com.labvantage.sapphire.scheduler.SchedulerAdminProcessor;
import com.labvantage.sapphire.scheduler.SchedulerUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.M18NUtil;
import sapphire.xml.PropertyList;

public class ExecuteNowForSDI
extends BaseAction
implements sapphire.action.ExecuteNowForSDI {
    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String sdcid = properties.getProperty("sdcid");
        String keyid1 = properties.getProperty("keyid1");
        String keyid2 = properties.getProperty("keyid2");
        String keyid3 = properties.getProperty("keyid3");
        boolean honorPlanItemStatus = properties.getProperty("honorplanitemstatus", "Y").startsWith("Y");
        String monitorGroupFlag = properties.getProperty("monitorgroupflag", "R");
        String monitorGroupLabel = properties.getProperty("monitorgrouplabel", "");
        String monitorGroupType = properties.getProperty("monitorgrouptype", "AdHoc");
        String existingMonitorGroupId = properties.getProperty("monitorgroupid", "");
        String instanceCount = properties.getProperty("instancecount", "");
        String planitemClass = properties.getProperty("planitemclass", "");
        List<String> planItemClasses = Arrays.asList(StringUtil.split(planitemClass, ";"));
        String eventDtStr = properties.getProperty("eventdt", "n");
        String msg = "";
        String monitorGroupBy = "";
        if (sdcid.isEmpty()) {
            throw new SapphireException("SDC id not given!");
        }
        if (keyid1.isEmpty()) {
            throw new SapphireException("KeyID 1 not given!");
        }
        PropertyList sdcProps = this.getSDCProcessor().getProperties(sdcid);
        boolean haskeyid2 = !sdcProps.getProperty("keycolid2", "").isEmpty();
        boolean haskeyid3 = !sdcProps.getProperty("keycolid3", "").isEmpty();
        String[] keyid1Arr = StringUtil.split(keyid1, ";");
        String[] keyid2Arr = StringUtil.split(keyid2, ";");
        String[] keyid3Arr = StringUtil.split(keyid3, ";");
        if (haskeyid2) {
            if (keyid2.isEmpty()) throw new SapphireException("Keyid2 not given!");
            if (keyid1Arr.length != keyid2Arr.length) {
                throw new SapphireException("Number of keyid1 and keyid2 arguments does not match!");
            }
            if (haskeyid3) {
                if (keyid3.isEmpty()) throw new SapphireException("Keyid3 not given!");
                if (keyid1Arr.length != keyid3Arr.length) {
                    throw new SapphireException("Number of keyid1 and keyid3 arguments does not match!");
                }
            }
        } else {
            keyid2 = null;
            keyid3 = null;
        }
        DataSet planItemsDs = null;
        String scheduleGroupId = null;
        if (sdcid.equals("LV_ScheduleGroup")) {
            String scheduleGroupMonitorGroupType;
            if (keyid1Arr.length > 1) {
                throw new SapphireException("Only one Schedule Group can be executed at once!");
            }
            String sql = "SELECT schedulegroupid, monitorgrouptype, monitorgroupflag, monitorgrouplabel, honorplanitemstatusflag FROM schedulegroup WHERE schedulegroupid = ? ";
            DataSet settingsDs = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])new String[]{keyid1});
            String monitorGroupFlagStr = settingsDs.getString(0, "monitorgroupflag", "");
            if (!monitorGroupFlagStr.isEmpty()) {
                monitorGroupFlag = monitorGroupFlagStr.equals("R") ? "R" : (monitorGroupFlagStr.equals("S") ? "S" : "N");
            }
            String honorPlanItemStatusFlag = settingsDs.getString(0, "honorplanitemstatusflag", "Y");
            honorPlanItemStatus = honorPlanItemStatusFlag.startsWith("Y");
            monitorGroupType = scheduleGroupMonitorGroupType = settingsDs.getString(0, "monitorgrouptype", monitorGroupType);
            String schedulegroupid = settingsDs.getString(0, "schedulegroupid", "");
            if (monitorGroupLabel.isEmpty()) {
                monitorGroupLabel = settingsDs.getString(0, "monitorgrouplabel", schedulegroupid);
            }
            scheduleGroupId = keyid1;
            String rsetId = this.getDAMProcessor().createRSet("LV_ScheduleGroup", keyid1, keyid2, null);
            sql = "SELECT sp.scheduleplanid, sp.timezone, COALESCE(sp.planstatus, 'X') planstatus, spi.scheduleplanitemid, spi.planitemclass, COALESCE(spi.planitemstatus, 'X') planitemstatus , COALESCE(spn.nodestatus, 'A') nodestatus FROM schedulegroupitem sgi left join scheduleplanitem spi on spi.scheduleplanid = sgi.scheduleplanid and spi.scheduleplanitemid = sgi.scheduleplanitemid LEFT JOIN scheduleplan sp ON spi.scheduleplanid = sp.scheduleplanid LEFT JOIN rsetitems ON rsetitems.keyid1 = sgi.schedulegroupid LEFT JOIN scheduleplannode spn ON spn.scheduleplanid = spi.scheduleplanid AND spn.scheduleplannodeid = spi.scheduleplannodeid WHERE rsetitems.rsetid = ?";
            planItemsDs = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])new String[]{rsetId});
            this.getDAMProcessor().clearRSet(rsetId);
        } else if (sdcid.equals("SchedulePlanItem")) {
            scheduleGroupId = properties.getProperty("schedulegroupid", "");
            String rsetId = this.getDAMProcessor().createRSet("SchedulePlanItem", keyid1, keyid2, null);
            String sql = "SELECT sp.scheduleplanid, sp.timezone, coalesce(sp.planstatus, 'X') planstatus, spi.scheduleplanitemid, spi.planitemclass, coalesce(spi.planitemstatus, 'X') planitemstatus , coalesce(spn.nodestatus, 'A') nodestatus FROM scheduleplanitem spi LEFT JOIN scheduleplan sp ON spi.scheduleplanid = sp.scheduleplanid LEFT JOIN rsetitems ON rsetitems.keyid1 = spi.scheduleplanid and rsetitems.keyid2 = spi.scheduleplanitemid LEFT JOIN scheduleplannode spn ON spn.scheduleplanid = spi.scheduleplanid AND spn.scheduleplannodeid = spi.scheduleplannodeid WHERE rsetitems.rsetid = ?";
            planItemsDs = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])new String[]{rsetId});
            this.getDAMProcessor().clearRSet(rsetId);
        } else {
            boolean isScheduleable = sdcProps.getProperty("scheduleableflag", "N").startsWith("Y");
            if (!isScheduleable) {
                throw new SapphireException("The SDC is not scheduleable!");
            }
            String rsetId = this.getDAMProcessor().createRSet(sdcid, keyid1, keyid2, keyid3);
            String sql = "SELECT sp.scheduleplanid, sp.timezone, coalesce(sp.planstatus, 'X') planstatus, spi.scheduleplanitemid, spi.planitemclass, coalesce(spi.planitemstatus, 'X') planitemstatus , coalesce(spn.nodestatus, 'A') nodestatus FROM  scheduleplanitem spi LEFT JOIN rsetitems ON rsetitems.sdcid = spi.linksdcid AND rsetitems.keyid1 = linkkeyid1 ";
            if (haskeyid2) {
                sql = sql + " and rsetitems.keyid2 = spi.linkkeyid2 ";
            }
            if (haskeyid3) {
                sql = sql + " and rsetitems.keyid3 = spi.linkkeyid3 ";
            }
            sql = sql + "left join scheduleplan sp on spi.scheduleplanid = sp.scheduleplanid left join scheduleplannode spn on spn.scheduleplanid = spi.scheduleplanid and spn.scheduleplannodeid = spi.scheduleplannodeid WHERE rsetitems.rsetid = ?";
            planItemsDs = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])new String[]{rsetId});
            this.getDAMProcessor().clearRSet(rsetId);
        }
        DataSet scheduleData = planItemsDs.copy();
        scheduleData.clear();
        if (planitemClass.trim().isEmpty()) {
            scheduleData = planItemsDs;
        } else {
            for (String planClass : planItemClasses) {
                if (planClass.isEmpty()) continue;
                HashMap<String, String> filterMap = new HashMap<String, String>();
                filterMap.put("planitemclass", planClass);
                DataSet classPlanItems = planItemsDs.getFilteredDataSet(filterMap);
                scheduleData.addAll(classPlanItems);
            }
        }
        if (honorPlanItemStatus) {
            HashMap<String, String> filterMap = new HashMap<String, String>();
            filterMap.put("planstatus", "A");
            filterMap.put("planitemstatus", "A");
            filterMap.put("nodestatus", "A");
            scheduleData = scheduleData.getFilteredDataSet(filterMap);
        }
        if (scheduleData.getRowCount() == 0) {
            properties.setProperty("msg", "");
            throw new SapphireException("No Schedule Plan Items to Execute");
        }
        if (monitorGroupFlag.equals("S")) {
            String monitorGroupByKeyid = keyid1;
            if (keyid1.contains(";") && !sdcid.equals("LV_ScheduleGroup")) {
                monitorGroupByKeyid = this.getSDCProcessor().getProperty(sdcid, "plural", sdcid);
            }
            monitorGroupBy = monitorGroupByKeyid + ";" + Calendar.getInstance().getTime().getTime();
        }
        String monitorGroupId = "";
        if (monitorGroupFlag.equals("E")) {
            monitorGroupId = existingMonitorGroupId;
        } else if (scheduleData.getRowCount() > 0 && monitorGroupFlag.equals("S")) {
            MonitorGroup monitorGroup = new MonitorGroup(this.getConnectionId());
            if (sdcid.equals("LV_ScheduleGroup")) {
                monitorGroup.setParent(sdcid, keyid1, keyid2, keyid3);
            }
            monitorGroupId = monitorGroup.createMonitorGroup(monitorGroupType, monitorGroupBy, monitorGroupLabel, scheduleGroupId);
        }
        HashSet<String> schdulePlans = new HashSet<String>();
        schdulePlans.addAll(Arrays.asList(StringUtil.split(scheduleData.getColumnValues("scheduleplanid", ";"), ";")));
        int successCnt = 0;
        boolean success = false;
        HashMap newKeyidMap = new HashMap();
        for (String schedulePlanId : schdulePlans) {
            M18NUtil m18nTimezone;
            HashMap<String, String> filterMap = new HashMap<String, String>();
            filterMap.put("scheduleplanid", schedulePlanId);
            DataSet planDs = scheduleData.getFilteredDataSet(filterMap);
            String timezoneStr = planDs.getValue(0, "timezone");
            M18NUtil m18n = new M18NUtil(this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()));
            TimeZone planTimeZone = timezoneStr.isEmpty() ? TimeZone.getDefault() : TimeZone.getTimeZone(timezoneStr);
            boolean useUserTimeZone = false;
            if (useUserTimeZone) {
                m18nTimezone = m18n;
            } else {
                m18nTimezone = new M18NUtil(this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()));
                m18nTimezone.setTimeZone(planTimeZone);
            }
            Calendar eventdt = m18n.parseCalendar(eventDtStr);
            if (useUserTimeZone) {
                int timeZoneCorrection = SchedulerUtil.getTimeZoneDiffForDate(m18n.getTimezone(), planTimeZone, eventdt);
                eventdt.add(14, timeZoneCorrection);
                eventDtStr = m18n.format(eventdt);
            } else {
                eventdt = m18nTimezone.parseCalendar(eventDtStr);
                eventDtStr = m18n.format(eventdt);
            }
            for (int i = 0; i < planDs.getRowCount(); ++i) {
                String schedulePlanItemId = planDs.getValue(i, "scheduleplanitemid");
                PropertyList executeProps = new PropertyList();
                executeProps.setProperty("scheduleplanid", schedulePlanId);
                executeProps.setProperty("scheduleplanitemid", schedulePlanItemId);
                executeProps.setProperty("eventfromdate", eventDtStr);
                executeProps.setProperty("eventtodate", eventDtStr);
                executeProps.setProperty("eventstatus", "P");
                executeProps.setProperty("eventnum", "");
                executeProps.setProperty("noevent", "true");
                executeProps.setProperty("eventdate", eventDtStr);
                executeProps.setProperty("adhocexecution", "true");
                executeProps.setProperty("noevent", "true");
                executeProps.setProperty("monitorgroupflag", monitorGroupFlag);
                executeProps.setProperty("instancecount", instanceCount);
                executeProps.put("newkeyidmap", newKeyidMap);
                if (monitorGroupFlag.equals("S") || monitorGroupFlag.equals("E")) {
                    executeProps.setProperty("monitorgroupid", monitorGroupId);
                    executeProps.setProperty("monitorgrouplabel", monitorGroupLabel);
                }
                SchedulerAdminProcessor scheduler = new SchedulerAdminProcessor(this.getConnectionid());
                try {
                    scheduler.executeEvent(executeProps);
                    ++successCnt;
                    success = true;
                    continue;
                }
                catch (Exception e) {
                    HashMap<String, String> transMap = new HashMap<String, String>();
                    transMap.put("eventnumber", String.valueOf(i + 1));
                    transMap.put("scheduleplanid", schedulePlanId);
                    transMap.put("scheduleplanitemid", schedulePlanItemId);
                    String sql = "select scheduleplanitemdesc, schedulerule from scheduleplanitem where scheduleplanid = ? and scheduleplanitemid = ?";
                    DataSet ds = null;
                    try {
                        ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])new String[]{schedulePlanId, schedulePlanItemId});
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                    if (ds != null && ds.getRowCount() == 1) {
                        transMap.put("scheduleplanitemdesc", ds.getValue(0, "scheduleplanitemdesc"));
                        transMap.put("schedulerule", ds.getValue(0, "schedulerule"));
                    } else {
                        transMap.put("scheduleplanitemdesc", schedulePlanItemId);
                        transMap.put("schedulerule", "");
                    }
                    Exception rootCause = new Exception(this.getCause(e));
                    String error = "";
                    if (rootCause != null && ((error = rootCause.getMessage()) == null || error.isEmpty())) {
                        int l = rootCause.getStackTrace().length;
                        if (l > 0) {
                            error = rootCause.getStackTrace()[0].getClassName();
                        }
                        error = error + ":" + rootCause.toString();
                    }
                    transMap.put("error", error);
                    msg = msg + this.getTranslationProcessor().translate("Schedule Plan Item ([scheduleplanid], [scheduleplanitemdesc] / [schedulerule]) execution failed, Error: [error]\nSee logs for more information.", transMap);
                    msg = msg + "\n\n";
                }
            }
        }
        if (success) {
            HashMap<String, String> transMap = new HashMap<String, String>();
            transMap.put("successcnt", String.valueOf(successCnt));
            transMap.put("nofevents", String.valueOf(scheduleData.getRowCount()));
            transMap.put("monitorgroupid", monitorGroupId);
            msg = msg + this.getTranslationProcessor().translate("[successcnt]/[nofevents] Plan Item(s) executed successfully.", transMap);
            msg = msg + "\n";
            if (!monitorGroupId.isEmpty()) {
                msg = msg + this.getTranslationProcessor().translate("Monitor group [monitorgroupid]", transMap);
            }
        }
        for (Map.Entry sdcSet : newKeyidMap.entrySet()) {
            StringBuilder keyid1s = new StringBuilder();
            String newSdcId = (String)sdcSet.getKey();
            ArrayList newKeyid1Arr = (ArrayList)sdcSet.getValue();
            for (int i = 0; i < newKeyid1Arr.size(); ++i) {
                keyid1s.append(";").append((String)newKeyid1Arr.get(i));
            }
            String newKeyids = keyid1s.toString();
            if (!newKeyids.isEmpty()) {
                newKeyids = newKeyids.substring(1);
            }
            properties.setProperty(this.getSDCProcessor().getProperty(newSdcId, "keycolid1"), newKeyids);
        }
        properties.setProperty("newkeyid1", monitorGroupId);
        properties.setProperty("msg", msg);
    }

    private Throwable getCause(Throwable e) {
        Throwable cause = null;
        Throwable result = e;
        while (null != (cause = result.getCause()) && result != cause) {
            result = cause;
        }
        return result;
    }
}

