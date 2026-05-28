/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.console.diagnostics.database;

import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.console.diagnostics.BaseDiagnostic;
import com.labvantage.sapphire.console.diagnostics.DiagnosticException;
import com.labvantage.sapphire.scheduler.ScheduleRule;
import java.sql.Timestamp;
import java.util.Calendar;
import sapphire.SapphireException;
import sapphire.util.ConnectionInfo;
import sapphire.util.DBAccess;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.xml.PropertyList;

public class MigrateScheduleExcludeToCalendar
extends BaseDiagnostic {
    public MigrateScheduleExcludeToCalendar(DBAccess database, ConnectionInfo conenctionInfo) {
        super(database, conenctionInfo);
    }

    @Override
    public String getTitle() {
        return "Migrate ScheduleExcludes to LV Calendars";
    }

    @Override
    public String getDescription() {
        return "Move Schedule Excludes to LV Calendar";
    }

    @Override
    public String runDiagnostic(PropertyList properties) throws DiagnosticException {
        return "";
    }

    @Override
    public String runRepair(PropertyList properties) throws DiagnosticException {
        String msg = "";
        try {
            if (this.database.getCount("SELECT count(*) FROM sysconfig WHERE propertyid='scheduleexcludesconverted' AND propertyvalue='Y'") == 1) {
                msg = "Schedule Excludes have already been transferred";
            } else {
                msg = this.createExcludeSharedCalendars();
                this.database.executeUpdate("INSERT INTO sysconfig( propertyid, propertyvalue ) VALUES ( 'scheduleexcludesconverted', 'Y' )");
            }
        }
        catch (SapphireException e) {
            throw new DiagnosticException(e);
        }
        return msg;
    }

    private String createExcludeSharedCalendars() throws SapphireException {
        StringBuilder msg = new StringBuilder();
        String insertCalendarSQL = "insert into calendar (calendarid, calendardesc, sharedflag, sourceflag, createdt, createtool, moddt, modtool ) values (?, ?, ?, ?, ?, ?, ?, ?)";
        String insertCalendarItemSQL = "insert into calendaritem (calendarid, calendaritemid, calendaritemdescription, calendaritemlabel, repeatflag, repeatrule, startdt, enddt, alldayflag, createdt, createtool, moddt, modtool) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        this.database.createResultSet("SELECT * FROM scheduleexclude");
        DataSet excludes = new DataSet(this.database.getResultSet());
        this.database.closeResultSet();
        if (this.database.isOracle()) {
            try {
                this.database.executeSQL("ALTER TRIGGER biu_calendar DISABLE");
                this.database.executeSQL("ALTER TRIGGER ad_calendar DISABLE");
                this.database.executeSQL("ALTER TRIGGER biu_calendaritem DISABLE");
                this.database.executeSQL("ALTER TRIGGER ad_calendaritem DISABLE");
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        int excludeIndex = 0;
        int excludeItemIndex = 0;
        boolean allOk = true;
        for (int i = 0; i < excludes.getRowCount(); ++i) {
            boolean excludeTransferOk = true;
            String excludeId = excludes.getString(i, "scheduleexcludeid");
            msg.append("Transfering exclude:" + excludeId);
            String excludeCalendarID = "EXC" + String.format("%05d", ++excludeIndex);
            try {
                this.database.executePreparedUpdate(insertCalendarSQL, new Object[]{excludeCalendarID, "Migrated from Schedule Exclude " + excludeId, "Y", "I", DateTimeUtil.getNowTimestamp(), "Upgrade", DateTimeUtil.getNowTimestamp(), "Upgrade"});
            }
            catch (SapphireException e) {
                excludeTransferOk = false;
                String message = "ERROR inserting calendar " + excludeCalendarID + " from Schedule Exclude " + excludeId;
                Logger.logDebug(message);
                msg.append(message).append("\n<br>");
                e.printStackTrace();
            }
            this.database.createPreparedResultSet("SELECT * FROM scheduleexcludeitem WHERE SCHEDULEEXCLUDEID = ?", new Object[]{excludeId});
            DataSet excludeItemsDs = new DataSet(this.database.getResultSet());
            this.database.closeResultSet();
            boolean excludeItemsOk = true;
            if (excludeTransferOk) {
                for (int j = 0; j < excludeItemsDs.getRowCount(); ++j) {
                    String message;
                    String calendarItemId = "EXC" + String.format("%05d", excludeItemIndex);
                    String itemId = excludeItemsDs.getString(j, "scheduleexcludeitemid", "");
                    String desc = excludeItemsDs.getString(j, "scheduleexcludeitemdesc", itemId);
                    String repeatFlag = "N";
                    String rule = excludeItemsDs.getString(j, "excluderule", "");
                    Calendar startDt = excludeItemsDs.getCalendar(j, "excludedt");
                    Calendar endDt = null;
                    String ruleStr = null;
                    boolean itemOk = false;
                    if (rule != null && !rule.isEmpty()) {
                        startDt = Calendar.getInstance();
                        startDt.setTimeInMillis(0L);
                        startDt.set(14, 0);
                        startDt.set(13, 0);
                        startDt.set(12, 0);
                        startDt.set(11, 0);
                        endDt = (Calendar)startDt.clone();
                        startDt.set(13, 0);
                        startDt.set(12, 59);
                        startDt.set(11, 23);
                        ruleStr = "Every Week on " + rule + " @ 00:00";
                        ScheduleRule scheduleRule = new ScheduleRule(ruleStr);
                        if (scheduleRule.isValidRule()) {
                            repeatFlag = "Y";
                            itemOk = true;
                        } else {
                            message = "Rule not recognized:" + ruleStr;
                            Logger.logDebug(message);
                            msg.append(message).append("\n<br>");
                            excludeItemsOk = false;
                        }
                    } else if (startDt != null) {
                        endDt = (Calendar)startDt.clone();
                        endDt.add(11, 23);
                        endDt.add(12, 59);
                        itemOk = true;
                    }
                    if (itemOk) {
                        try {
                            this.database.executePreparedUpdate(insertCalendarItemSQL, new Object[]{excludeCalendarID, calendarItemId, desc, desc, repeatFlag, ruleStr, new Timestamp(startDt.getTime().getTime()), new Timestamp(endDt.getTime().getTime()), "Y", DateTimeUtil.getNowTimestamp(), "Upgrade", DateTimeUtil.getNowTimestamp(), "Upgrade"});
                        }
                        catch (SapphireException e) {
                            message = "ERROR inserting Calendar item " + calendarItemId + " for Schedule Exclude " + excludeId;
                            Logger.logDebug(message);
                            msg.append(message).append("\n<br>");
                            e.printStackTrace();
                            excludeTransferOk = false;
                            excludeItemsOk = false;
                        }
                    }
                    ++excludeItemIndex;
                }
            }
            if (excludeTransferOk) {
                String usageSql = "select * from scheduleplanexclude where scheduleexcludeid = ?";
                this.database.createPreparedResultSet(usageSql, new Object[]{excludeId});
                DataSet usageDs = new DataSet(this.database.getResultSet());
                this.database.closeResultSet();
                for (int j = 0; j < usageDs.getRowCount(); ++j) {
                    String insertSdiCalendar = "insert into sdicalendar (sdcid, keyid1, keyid2, keyid3, calendarid, createby, createtool, moddt, modtool) values (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                    String planId = usageDs.getString(j, "scheduleplanid");
                    try {
                        this.database.executePreparedUpdate(insertSdiCalendar, new Object[]{"SchedulePlan", planId, "(null)", "(null)", excludeCalendarID, DateTimeUtil.getNowTimestamp(), "Upgrade", DateTimeUtil.getNowTimestamp(), "Upgrade"});
                        continue;
                    }
                    catch (SapphireException e) {
                        String message = "ERROR inserting sdicalendar " + excludeCalendarID + " from Schedule Exclude " + excludeId + " to Schedule plan " + planId;
                        Logger.logDebug(message);
                        msg.append(message);
                        e.printStackTrace();
                    }
                }
            }
            if (excludeTransferOk && excludeItemsOk) {
                String message = "Schedule Exclude " + excludeId + " migrated successfully to calendar: " + excludeCalendarID;
                msg.append(message).append("\n<br>");
                continue;
            }
            allOk = false;
        }
        if (!allOk) {
            msg.append("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!").append("\n<br>");
            msg.append("\n<br>").append("NOTE: There were errors while converting schedule Excludes to Calendars. It is necessary to do manual conversion to make sure the Schedule Plans work correctly!");
        }
        if (this.database.isOracle()) {
            try {
                this.database.executeSQL("ALTER TRIGGER biu_calendar ENABLE");
                this.database.executeSQL("ALTER TRIGGER ad_calendar ENABLE");
                this.database.executeSQL("ALTER TRIGGER biu_calendaritem ENABLE");
                this.database.executeSQL("ALTER TRIGGER ad_calendaritem ENABLE");
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        return msg.toString();
    }

    @Override
    public boolean canBeRepaired() {
        return true;
    }

    @Override
    public boolean canAutoRepair() {
        return true;
    }
}

