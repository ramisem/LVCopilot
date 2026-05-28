/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.wap.calendar;

import com.labvantage.sapphire.BaseCustom;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.I18nUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.admin.system.SystemProcessor;
import com.labvantage.sapphire.modules.wap.calendar.LVCalendar;
import com.labvantage.sapphire.modules.wap.calendar.LVCalendarCache;
import com.labvantage.sapphire.modules.wap.workhours.WorkHours;
import com.labvantage.sapphire.services.AutomationService;
import com.labvantage.sapphire.util.cache.CacheUtil;
import com.labvantage.sapphire.util.junit.ConnectionDetails;
import java.io.File;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.TimeZone;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class CalendarFactory
extends BaseCustom {
    private ConnectionInfo connectionInfo;
    private BroadcastHandler broadcastHandler;

    public CalendarFactory(String connectionid) {
        this.setConnectionId(connectionid);
        ConnectionProcessor cp = this.getConnectionProcessor();
        this.connectionInfo = cp.getConnectionInfo(connectionid);
        this.broadcastHandler = new BroadcastHandlerLocal();
    }

    public CalendarFactory(String connectionid, File rakFile) {
        this.setConnectionId(connectionid);
        this.setRakFile(rakFile);
        ConnectionProcessor cp = this.getConnectionProcessor();
        this.connectionInfo = cp.getConnectionInfo(connectionid);
        this.broadcastHandler = new BroadcastHandlerJUnit();
    }

    public static Object[] getUserCalendarDetails(QueryProcessor queryProcessor, String sysuserid) throws SapphireException {
        if (sysuserid == null || sysuserid.length() == 0) {
            throw new SapphireException("Unable to find calendar details for User as no user was provided.");
        }
        DataSet ds = queryProcessor.getPreparedSqlDataSet("SELECT calendarid, timezone FROM sysuser WHERE sysuserid=?", (Object[])new String[]{sysuserid});
        if (ds.size() == 0) {
            throw new SapphireException("Unable to find calendar details for User " + sysuserid);
        }
        String calendarid = ds.getValue(0, "calendarid");
        TimeZone timeZone = ds.getValue(0, "timezone").length() > 0 ? TimeZone.getTimeZone(ds.getValue(0, "timezone")) : TimeZone.getDefault();
        return new Object[]{calendarid, timeZone};
    }

    public static Object[] getActivityCalendarDetails(QueryProcessor queryProcessor, String activityid) throws SapphireException {
        DataSet ds;
        if (activityid == null || activityid.length() == 0) {
            throw new SapphireException("Unable to find calendar details for Activity as no activity was provided.");
        }
        if (activityid.indexOf(";") > -1) {
            Trace.logInfo("Multiple activities provided to get calendar details, thus use first activity.");
            activityid = StringUtil.split(activityid, ";")[0];
        }
        if ((ds = queryProcessor.getPreparedSqlDataSet("SELECT calendarid, parentdepartmentid, timezone FROM department WHERE departmentid=(SELECT testingdepartmentid FROM activity WHERE activityid=?)", (Object[])new String[]{activityid})).size() == 0) {
            throw new SapphireException("Unable to find calendar details for Activity " + activityid);
        }
        String calendarid = ds.getValue(0, "calendarid");
        String parentdepartmentid = ds.getValue(0, "parentdepartmentid");
        TimeZone timeZone = ds.getValue(0, "timezone").length() > 0 ? TimeZone.getTimeZone(ds.getValue(0, "timezone")) : TimeZone.getDefault();
        return new Object[]{calendarid, timeZone, parentdepartmentid};
    }

    public static Object[] getDepartmentCalendarDetails(QueryProcessor queryProcessor, String departmentid) throws SapphireException {
        if (departmentid == null || departmentid.length() == 0) {
            throw new SapphireException("Unable to find calendar details for Department as no department was provided.");
        }
        DataSet ds = queryProcessor.getPreparedSqlDataSet("SELECT calendarid, parentdepartmentid, timezone FROM department WHERE departmentid=?", (Object[])new String[]{departmentid});
        if (ds.size() == 0) {
            throw new SapphireException("Unable to find calendar details for Department " + departmentid);
        }
        String calendarid = ds.getValue(0, "calendarid");
        String parentdepartmentid = ds.getValue(0, "parentdepartmentid");
        TimeZone timeZone = ds.getValue(0, "timezone").length() > 0 ? TimeZone.getTimeZone(ds.getValue(0, "timezone")) : TimeZone.getDefault();
        return new Object[]{calendarid, timeZone, parentdepartmentid};
    }

    public static Object[] getShiftCalendarDetails(QueryProcessor queryProcessor, String departmentid, String shiftid) throws SapphireException {
        TimeZone timeZone;
        String sql = "select calendarid, timezone from calendar where calendarid in (select calendarid from departmentshift where shiftid=? and departmentid=?)";
        DataSet ds = queryProcessor.getPreparedSqlDataSet(sql, (Object[])new String[]{shiftid, departmentid});
        if (ds.size() == 0) {
            throw new SapphireException("Unable to find calendar details for Department shift " + departmentid + ", " + shiftid);
        }
        String calendarid = ds.getValue(0, "calendarid");
        TimeZone timeZone2 = timeZone = ds.getValue(0, "timezone").length() > 0 ? TimeZone.getTimeZone(ds.getValue(0, "timezone")) : null;
        if (timeZone == null) {
            ds = queryProcessor.getPreparedSqlDataSet("SELECT timezone FROM department WHERE departmentid=?", (Object[])new String[]{departmentid});
            if (ds.size() == 0) {
                throw new SapphireException("Unable to find calendar details for Department " + departmentid);
            }
            timeZone = ds.getValue(0, "timezone").length() > 0 ? TimeZone.getTimeZone(ds.getValue(0, "timezone")) : TimeZone.getDefault();
        }
        return new Object[]{calendarid, timeZone};
    }

    public static Object[] getInstrumentCalendarDetails(QueryProcessor queryProcessor, String instrumentid) throws SapphireException {
        if (instrumentid == null || instrumentid.length() == 0) {
            throw new SapphireException("Unable to find calendar details for Instrument as no instrument was provided.");
        }
        DataSet ds = queryProcessor.getPreparedSqlDataSet("SELECT instrument.calendarid, instrument.timezone, instrument.testingdepartmentid, department.timezone departmenttimezone FROM instrument LEFT OUTER JOIN department ON instrument.testingdepartmentid=department.departmentid WHERE instrumentid=?", (Object[])new String[]{instrumentid});
        if (ds == null || ds.size() == 0) {
            throw new SapphireException("Unable to find calendar details for Instrument " + instrumentid);
        }
        String calendarid = ds.getValue(0, "calendarid");
        String testingdepartmentid = ds.getValue(0, "testingdepartmentid");
        String tz = ds.getValue(0, "timezone", ds.getValue(0, "departmenttimezone"));
        TimeZone timeZone = tz.length() > 0 ? TimeZone.getTimeZone(tz) : TimeZone.getDefault();
        return new Object[]{calendarid, timeZone, testingdepartmentid};
    }

    public LVCalendar getUserCalendar(String userid, boolean createIfMissing, boolean includeHierarchy) throws SapphireException {
        QueryProcessor queryProcessor = this.getQueryProcessor();
        Object[] out = CalendarFactory.getUserCalendarDetails(queryProcessor, userid);
        String calendarid = (String)out[0];
        TimeZone timeZone = (TimeZone)out[1];
        if (calendarid.length() == 0) {
            if (createIfMissing) {
                ActionProcessor ap = this.getActionProcessor();
                calendarid = CalendarFactory.createNewUserCalendar(ap, userid);
            } else {
                throw new SapphireException("User " + userid + " does not have a default calendar");
            }
        }
        LVCalendar calendar = this.getCalendar(calendarid, timeZone.toZoneId());
        DataSet extras = queryProcessor.getPreparedSqlDataSet("SELECT * FROM sdicalendar WHERE sdcid='User' AND keyid1=? ORDER BY usersequence DESC", (Object[])new String[]{userid});
        for (int i = 0; i < extras.size(); ++i) {
            String extraCalendarid = extras.getValue(i, "calendarid");
            LVCalendar extraCalendar = this.getCalendar(extraCalendarid, timeZone.toZoneId());
            calendar.addExtraCalendar(extraCalendar);
        }
        if (includeHierarchy) {
            DataSet department = queryProcessor.getPreparedSqlDataSet("SELECT departmentid, shiftid FROM departmentsysuser WHERE defaulttestinglabflag='Y' AND sysuserid=?", (Object[])new String[]{userid});
            String departmentid = department.getValue(0, "departmentid");
            String shiftid = department.getValue(0, "shiftid");
            if (departmentid.length() > 0) {
                if (shiftid.length() > 0) {
                    LVCalendar deptCalendar = this.getShiftCalendar(departmentid, shiftid, true, true);
                    calendar.addParent(deptCalendar);
                } else {
                    LVCalendar deptCalendar = this.getDepartmentCalendar(departmentid, true, true);
                    calendar.addParent(deptCalendar);
                }
            }
        }
        return calendar;
    }

    public LVCalendar getDepartmentCalendar(String departmentid, boolean createIfMissing, boolean includeHierarchy) throws SapphireException {
        QueryProcessor queryProcessor = this.getQueryProcessor();
        Object[] out = CalendarFactory.getDepartmentCalendarDetails(queryProcessor, departmentid);
        String calendarid = (String)out[0];
        TimeZone timeZone = (TimeZone)out[1];
        String parentdepartmentid = (String)out[2];
        if (calendarid.length() == 0) {
            if (createIfMissing) {
                ActionProcessor ap = this.getActionProcessor();
                calendarid = CalendarFactory.createNewDepartmentCalendar(ap, departmentid);
            } else {
                throw new SapphireException("Department " + departmentid + " does not have a default calendar");
            }
        }
        LVCalendar calendar = this.getCalendar(calendarid, timeZone.toZoneId());
        DataSet extras = this.getQueryProcessor().getPreparedSqlDataSet("SELECT * FROM sdicalendar WHERE sdcid='Department' AND keyid1=? ORDER BY usersequence DESC", (Object[])new String[]{departmentid});
        for (int i = 0; i < extras.size(); ++i) {
            String extraCalendarid = extras.getValue(i, "calendarid");
            LVCalendar extraCalendar = this.getCalendar(extraCalendarid);
            calendar.addExtraCalendar(extraCalendar);
        }
        if (includeHierarchy && parentdepartmentid.length() > 0) {
            LVCalendar deptCalendar = this.getDepartmentCalendar(parentdepartmentid, true, true);
            calendar.addParent(deptCalendar);
        }
        return calendar;
    }

    public LVCalendar getInstrumentCalendar(String instrumentid, boolean createIfMissing, boolean includeHierarchy) throws SapphireException {
        QueryProcessor queryProcessor = this.getQueryProcessor();
        Object[] out = CalendarFactory.getInstrumentCalendarDetails(queryProcessor, instrumentid);
        String calendarid = (String)out[0];
        TimeZone timeZone = (TimeZone)out[1];
        String testingdepartmentid = (String)out[2];
        if (calendarid.length() == 0) {
            if (createIfMissing) {
                ActionProcessor ap = this.getActionProcessor();
                calendarid = CalendarFactory.createNewInstrumentCalendar(ap, instrumentid);
            } else {
                throw new SapphireException("Instrumentid " + instrumentid + " does not have a default calendar");
            }
        }
        LVCalendar calendar = this.getCalendar(calendarid, timeZone.toZoneId());
        if (includeHierarchy && (calendar.getCoreHours() == null || calendar.getCoreHours().getOverride() != WorkHours.OverrideType.OVERRIDE) && testingdepartmentid.length() > 0) {
            LVCalendar deptCalendar = this.getDepartmentCalendar(testingdepartmentid, true, true);
            WorkHours workHours = deptCalendar.getCoreHours();
            LVCalendar empty = this.getCalendar("", deptCalendar.getTimeZone());
            empty.setCoreHours(workHours);
            calendar.addParent(empty);
        }
        return calendar;
    }

    public LVCalendar getSchedulePlanCalendar(String scheduleplanid, boolean createIfMissing, boolean includeHierarchy) throws SapphireException {
        QueryProcessor queryProcessor = this.getQueryProcessor();
        DataSet ds = queryProcessor.getPreparedSqlDataSet("SELECT calendarid, sitedepartmentid FROM scheduleplan WHERE scheduleplanid=?", (Object[])new String[]{scheduleplanid});
        if (ds == null) {
            throw new SapphireException("Could not obtain calendar");
        }
        if (ds.size() == 0) {
            throw new SapphireException("Schedule Plan " + scheduleplanid + " not recognized");
        }
        String calendarid = ds.getValue(0, "calendarid");
        String siteid = ds.getValue(0, "sitedepartmentid");
        if (calendarid.length() == 0) {
            if (createIfMissing) {
                ActionProcessor ap = this.getActionProcessor();
                calendarid = CalendarFactory.createNewSchedulePlanCalendar(ap, scheduleplanid);
            } else {
                throw new SapphireException("Schedule Plan " + scheduleplanid + " does not have a default calendar");
            }
        }
        LVCalendar calendar = this.getCalendar(calendarid);
        DataSet extras = this.getQueryProcessor().getPreparedSqlDataSet("SELECT * FROM sdicalendar WHERE sdcid='SchedulePlan' AND keyid1=? ORDER BY usersequence DESC", (Object[])new String[]{scheduleplanid});
        for (int i = 0; i < extras.size(); ++i) {
            String extraCalendarid = extras.getValue(i, "calendarid");
            LVCalendar extraCalendar = this.getCalendar(extraCalendarid);
            calendar.addExtraCalendar(extraCalendar);
        }
        if (includeHierarchy && siteid.length() > 0) {
            LVCalendar deptCalendar = this.getDepartmentCalendar(siteid, true, true);
            calendar.addParent(deptCalendar);
        }
        return calendar;
    }

    public LVCalendar getShiftCalendar(String departmentid, String shiftid, boolean createIfMissing, boolean includeHierarchy) throws SapphireException {
        TimeZone departmentTimezone;
        QueryProcessor queryProcessor = this.getQueryProcessor();
        DataSet ds = queryProcessor.getPreparedSqlDataSet("SELECT departmentshift.calendarid, department.timezone FROM department, departmentshift WHERE department.departmentid = departmentshift.departmentid AND departmentshift.departmentid=? AND shiftid=?", (Object[])new String[]{departmentid, shiftid});
        if (ds.size() == 0) {
            throw new SapphireException("Shift " + shiftid + "  in Department " + departmentid + " not recognized");
        }
        String calendarid = ds.getValue(0, "calendarid");
        TimeZone timeZone = departmentTimezone = ds.getValue(0, "timezone").length() > 0 ? TimeZone.getTimeZone(ds.getValue(0, "timezone")) : TimeZone.getDefault();
        if (calendarid.length() == 0) {
            if (createIfMissing) {
                ActionProcessor ap = this.getActionProcessor();
                calendarid = CalendarFactory.createNewDepartmentShiftCalendar(ap, departmentid, shiftid);
            } else {
                throw new SapphireException("Department " + departmentid + " shift " + shiftid + " does not have a default calendar");
            }
        }
        LVCalendar calendar = this.getCalendar(calendarid, departmentTimezone.toZoneId());
        if (includeHierarchy) {
            LVCalendar deptCalendar = this.getDepartmentCalendar(departmentid, true, true);
            calendar.addParent(deptCalendar);
        }
        return calendar;
    }

    public LVCalendar getCalendar(String calendarid) throws SapphireException {
        return this.getCalendar(calendarid, null);
    }

    public LVCalendar getCalendar(String calendarid, ZoneId timeZone) throws SapphireException {
        DataSet ds;
        LVCalendar calendar;
        LVCalendarCache cachedCalendar = calendarid.length() > 0 ? (LVCalendarCache)CacheUtil.get(this.connectionInfo.getDatabaseId(), "Calendar", calendarid) : null;
        DateTimeUtil dtu = new DateTimeUtil(this.connectionInfo);
        if (cachedCalendar == null) {
            cachedCalendar = new LVCalendarCache(calendarid);
            CacheUtil.put(this.connectionInfo.getDatabaseId(), "Calendar", calendarid, cachedCalendar);
            calendar = new LVCalendar(this, cachedCalendar, this.getActionProcessor(), this.getQueryProcessor(), dtu, this.broadcastHandler);
            if (timeZone != null) {
                calendar.setTimeZone(timeZone);
            }
            if (calendarid.length() > 0) {
                calendar.loadCalendar(calendarid);
            }
        } else {
            calendar = new LVCalendar(this, cachedCalendar, this.getActionProcessor(), this.getQueryProcessor(), dtu, this.broadcastHandler);
            if (timeZone != null) {
                calendar.setTimeZone(timeZone);
            }
        }
        if (!calendar.isShared() && timeZone == null && (ds = this.getQueryProcessor().getPreparedSqlDataSet("SELECT timezone FROM department WHERE calendarid = ?  UNION SELECT timezone FROM instrument WHERE calendarid = ?  UNION SELECT timezone FROM sysuser WHERE calendarid = ? ", (Object[])new String[]{calendarid, calendarid, calendarid})).size() >= 0 && ds.getValue(0, "timezone").length() > 0) {
            timeZone = I18nUtil.getZoneIdFromString(ds.getValue(0, "timezone"));
            calendar.setTimeZone(timeZone);
        }
        return calendar;
    }

    private static String createNewUserCalendar(ActionProcessor actionProcessor, String userid) throws ActionException {
        PropertyList props = new PropertyList();
        props.setProperty("sdcid", "LV_Calendar");
        props.setProperty("copies", "1");
        props.setProperty("sharedflag", "N");
        props.setProperty("sourceflag", "I");
        props.setProperty("calendardesc", "User " + userid + " Calendar");
        actionProcessor.processAction("AddSDI", "1", props);
        String calenderid = props.getProperty("newkeyid1");
        props.clear();
        props.setProperty("sdcid", "User");
        props.setProperty("keyid1", userid);
        props.setProperty("calendarid", calenderid);
        actionProcessor.processAction("EditSDI", "1", props);
        return calenderid;
    }

    private static String createNewInstrumentCalendar(ActionProcessor actionProcessor, String instrumentid) throws ActionException {
        PropertyList props = new PropertyList();
        props.setProperty("sdcid", "LV_Calendar");
        props.setProperty("copies", "1");
        props.setProperty("sharedflag", "N");
        props.setProperty("sourceflag", "I");
        props.setProperty("calendardesc", "Instrument " + instrumentid + " Calendar");
        actionProcessor.processAction("AddSDI", "1", props);
        String calenderid = props.getProperty("newkeyid1");
        props.clear();
        props.setProperty("sdcid", "Instrument");
        props.setProperty("keyid1", instrumentid);
        props.setProperty("calendarid", calenderid);
        actionProcessor.processAction("EditSDI", "1", props);
        return calenderid;
    }

    private static String createNewSchedulePlanCalendar(ActionProcessor actionProcessor, String scheduleplanid) throws ActionException {
        PropertyList props = new PropertyList();
        props.setProperty("sdcid", "LV_Calendar");
        props.setProperty("copies", "1");
        props.setProperty("sharedflag", "N");
        props.setProperty("sourceflag", "I");
        props.setProperty("calendardesc", "SchedulePlan " + scheduleplanid + " Calendar");
        actionProcessor.processAction("AddSDI", "1", props);
        String calenderid = props.getProperty("newkeyid1");
        props.clear();
        props.setProperty("sdcid", "SchedulePlan");
        props.setProperty("keyid1", scheduleplanid);
        props.setProperty("calendarid", calenderid);
        actionProcessor.processAction("EditSDI", "1", props);
        return calenderid;
    }

    public static String createNewSharedCalendar(ActionProcessor actionProcessor, String description) throws ActionException {
        PropertyList props = new PropertyList();
        props.setProperty("sdcid", "LV_Calendar");
        props.setProperty("copies", "1");
        props.setProperty("sharedflag", "Y");
        props.setProperty("sourceflag", "I");
        props.setProperty("calendardesc", description);
        actionProcessor.processAction("AddSDI", "1", props);
        String calenderid = props.getProperty("newkeyid1");
        return calenderid;
    }

    private static String createNewDepartmentCalendar(ActionProcessor actionProcessor, String departmentid) throws ActionException {
        PropertyList props = new PropertyList();
        props.setProperty("sdcid", "LV_Calendar");
        props.setProperty("copies", "1");
        props.setProperty("sharedflag", "N");
        props.setProperty("sourceflag", "I");
        props.setProperty("calendardesc", "Department " + departmentid + " Calendar");
        actionProcessor.processAction("AddSDI", "1", props);
        String calenderid = props.getProperty("newkeyid1");
        props.clear();
        props.setProperty("sdcid", "Department");
        props.setProperty("keyid1", departmentid);
        props.setProperty("calendarid", calenderid);
        actionProcessor.processAction("EditSDI", "1", props);
        return calenderid;
    }

    private static String createNewDepartmentShiftCalendar(ActionProcessor actionProcessor, String departmentid, String shiftid) throws ActionException {
        PropertyList props = new PropertyList();
        props.setProperty("sdcid", "LV_Calendar");
        props.setProperty("copies", "1");
        props.setProperty("sharedflag", "N");
        props.setProperty("sourceflag", "I");
        props.setProperty("calendardesc", "Department " + departmentid + " " + shiftid + " Calendar");
        actionProcessor.processAction("AddSDI", "1", props);
        String calenderid = props.getProperty("newkeyid1");
        props.clear();
        props.setProperty("sdcid", "Department");
        props.setProperty("keyid1", departmentid);
        props.setProperty("linkid", "Department Shifts");
        props.setProperty("shiftid", shiftid);
        props.setProperty("calendarid", calenderid);
        actionProcessor.processAction("EditSDIDetail", "1", props);
        return calenderid;
    }

    class BroadcastHandlerJUnit
    implements BroadcastHandler {
        BroadcastHandlerJUnit() {
        }

        @Override
        public void resetCache(String calendarid) throws SapphireException {
            SystemProcessor systemProcessor = new SystemProcessor(CalendarFactory.this.getRakFile(), CalendarFactory.this.connectionInfo.getConnectionId());
            HashMap<String, String> commandProps = new HashMap<String, String>();
            commandProps.put("command", "flushcalendar");
            commandProps.put("databaseid", ConnectionDetails.TESTDB_DATABASEID);
            commandProps.put("userid", ConnectionDetails.SAPPHIRE_USERNAME);
            commandProps.put("password", ConnectionDetails.SAPPHIRE_PASSWORD);
            commandProps.put("calendarid", calendarid);
            systemProcessor.processCommand(commandProps);
        }
    }

    class BroadcastHandlerLocal
    implements BroadcastHandler {
        BroadcastHandlerLocal() {
        }

        @Override
        public void resetCache(String calendarid) {
            PropertyList props = new PropertyList();
            props.setProperty("name", "Calendar");
            props.setProperty("key", calendarid);
            AutomationService.broadcastServerCommand(CalendarFactory.this.connectionInfo.getDatabaseId(), "RemoveFromCache", props.toXMLString());
        }
    }

    static interface BroadcastHandler {
        public void resetCache(String var1) throws SapphireException;
    }
}

