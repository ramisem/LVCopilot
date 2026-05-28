/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joda.time.DateTimeZone
 *  org.joda.time.LocalDate
 *  org.joda.time.LocalDateTime
 *  org.joda.time.ReadablePartial
 */
package com.labvantage.sapphire.scheduler;

import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.modules.wap.calendar.CalendarFactory;
import com.labvantage.sapphire.modules.wap.calendar.CalendarItem;
import com.labvantage.sapphire.modules.wap.calendar.LVCalendar;
import com.labvantage.sapphire.services.SapphireConnection;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.ReadablePartial;
import sapphire.SapphireException;
import sapphire.util.DataSet;
import sapphire.util.Logger;

public class ScheduleExclude {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";
    public static final String EXCLUDEMODE_NEXTDAY = "N";
    public static final String EXCLUDEMODE_PREVIOUSDAY = "P";
    public static final String EXCLUDEMODE_SKIP = "S";
    public static final String EXCLUDEMODE_FLAG = "F";
    private TimeZone timeZone;
    private LVCalendar calendar;
    private List<CalendarItem> calendarItems;
    private Calendar startdt;
    private Calendar enddt;
    private String planId;
    private SapphireConnection connection;
    private Logger logger;
    private File rakFile = null;

    public ScheduleExclude(SapphireConnection sapphireConnection, Logger logger) {
        this.logger = logger;
        this.connection = sapphireConnection;
        this.planId = "";
        this.calendarItems = null;
    }

    public ScheduleExclude(SapphireConnection sapphireConnection, Logger logger, File rakFile) {
        this.rakFile = rakFile;
        this.logger = logger;
        this.connection = sapphireConnection;
        this.planId = "";
        this.calendarItems = null;
    }

    public void setSchedulePlanId(String schedulePlanId) {
        this.planId = schedulePlanId;
        try {
            if (this.timeZone == null) {
                this.getSchedulePlanTimeZone();
            }
            CalendarFactory factory = this.rakFile == null ? new CalendarFactory(this.connection.getConnectionId()) : new CalendarFactory(this.connection.getConnectionId(), this.rakFile);
            this.calendar = factory.getSchedulePlanCalendar(this.planId, true, true);
        }
        catch (SapphireException e) {
            this.logger.info("Could not fetch Schedule Excludes for schedule plan " + this.planId);
        }
        this.calendarItems = null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void getSchedulePlanTimeZone() {
        DBUtil db = new DBUtil(this.connection.getConnectionId());
        db.setConnection(this.connection);
        try {
            String sql = "select timezone FROM scheduleplan where scheduleplanid = ?";
            Object[] params = new Object[]{this.planId};
            db.createPreparedResultSet(sql, params);
            DataSet timeZoneDs = new DataSet(db.getResultSet());
            if (timeZoneDs.getRowCount() == 1) {
                String timeZoneStr = timeZoneDs.getString(0, "timezone", "");
                this.timeZone = timeZoneStr.isEmpty() ? TimeZone.getDefault() : TimeZone.getTimeZone(timeZoneStr);
            }
        }
        catch (SapphireException e) {
            this.timeZone = TimeZone.getDefault();
        }
        finally {
            db.reset();
        }
    }

    public void setCalendar(String calendarid) {
        try {
            CalendarFactory factory = new CalendarFactory(this.connection.getConnectionId());
            this.calendar = factory.getCalendar(calendarid);
            if (this.timeZone == null) {
                this.timeZone = TimeZone.getDefault();
            }
        }
        catch (SapphireException e) {
            this.logger.info("Could not fetch Schedule Excludes Calendar " + calendarid);
        }
        this.calendarItems = null;
    }

    public void setStartdt(Calendar newStartDt) {
        if (this.startdt == null || this.startdt.after(newStartDt)) {
            this.startdt = newStartDt;
            this.startdt.add(5, -1);
            this.calendarItems = null;
        }
    }

    public void setEnddt(Calendar newEndDt) {
        if (this.enddt == null || this.enddt.before(newEndDt)) {
            this.enddt = newEndDt;
            this.enddt.add(5, 1);
            this.calendarItems = null;
        }
    }

    private void loadExcludes() {
        if (this.startdt == null) {
            throw new IllegalArgumentException("Start date not defined!");
        }
        if (this.enddt == null) {
            throw new IllegalArgumentException("End date not defined!");
        }
        this.calendarItems = new ArrayList<CalendarItem>();
        try {
            this.calendarItems.addAll(this.calendar.getCalendarItemsBetween(this.calendar.getCalendarConverter().convertDatabaseCalendarToInstantUtc(this.startdt), this.calendar.getCalendarConverter().convertDatabaseCalendarToInstantUtc(this.enddt), true));
        }
        catch (SapphireException e) {
            this.logger.info("Failed to load Exclude Calendar Items for Calendar " + this.calendar.getId());
        }
    }

    public boolean isExcluded(Calendar eventDt) {
        boolean tmpIsExcluded = false;
        LocalDateTime eventDateTime = new LocalDateTime((Object)eventDt, DateTimeZone.forTimeZone((TimeZone)this.timeZone));
        if (this.calendarItems == null || eventDt.before(this.startdt) || eventDt.after(this.enddt)) {
            if (this.startdt == null || eventDt.before(this.startdt)) {
                this.startdt = (Calendar)eventDt.clone();
            }
            if (this.enddt == null || eventDt.after(this.enddt)) {
                this.enddt = (Calendar)eventDt.clone();
            }
            this.loadExcludes();
        }
        for (CalendarItem item : this.calendarItems) {
            Calendar startDt;
            if (item.isAllDayFlag()) {
                startDt = this.calendar.getCalendarConverter().convertInstantUtcToDatabaseCalendar(item.getStartDate());
                LocalDate excludeDate = new LocalDate((Object)startDt);
                if (excludeDate != null && eventDateTime.getYear() == excludeDate.getYear() && eventDateTime.getMonthOfYear() == excludeDate.getMonthOfYear() && eventDateTime.getDayOfMonth() == excludeDate.getDayOfMonth()) {
                    tmpIsExcluded = true;
                }
            } else {
                startDt = this.calendar.getCalendarConverter().convertInstantUtcToDatabaseCalendar(item.getStartDate());
                LocalDateTime excludeStartDateTime = new LocalDateTime((Object)startDt, DateTimeZone.forTimeZone((TimeZone)this.timeZone));
                Calendar endDt = this.calendar.getCalendarConverter().convertInstantUtcToDatabaseCalendar(item.getEndDate());
                LocalDateTime excludeEndDateTime = new LocalDateTime((Object)endDt, DateTimeZone.forTimeZone((TimeZone)this.timeZone));
                boolean bl = tmpIsExcluded = !eventDateTime.isBefore((ReadablePartial)excludeStartDateTime) && eventDateTime.isBefore((ReadablePartial)excludeEndDateTime);
            }
            if (!tmpIsExcluded) continue;
            break;
        }
        return tmpIsExcluded;
    }

    public DataSet getExcludesDs() throws SapphireException {
        if ((this.planId == null || this.planId.isEmpty()) && this.calendar == null) {
            throw new SapphireException("Exclude is not defined correctly");
        }
        DataSet excludes = new DataSet();
        excludes.addColumn("scheduleplanid", 0);
        excludes.addColumn("scheduleexcludeid", 0);
        excludes.addColumn("eventdt", 2);
        excludes.addColumn("eventenddt", 2);
        excludes.addColumn("eventstatus", 0);
        excludes.addColumn("eventdesc", 0);
        excludes.addColumn("eventid", 0);
        this.loadExcludes();
        for (CalendarItem item : this.calendarItems) {
            int row = excludes.addRow();
            excludes.setString(row, "scheduleexcludeid", item.getCalendarId());
            excludes.setDate(row, "eventdt", this.calendar.getCalendarConverter().convertInstantUtcToDatabaseCalendar(item.getStartDate()));
            excludes.setDate(row, "eventenddt", this.calendar.getCalendarConverter().convertInstantUtcToDatabaseCalendar(item.getEndDate()));
            excludes.setString(row, "eventdesc", item.getCalendarItemLabel());
            excludes.setString(row, "eventstatus", "Holiday");
            excludes.setString(row, "timezone", this.timeZone.getID());
            excludes.setString(row, "allday", item.isAllDayFlag() ? "true" : "false");
            excludes.setString(row, "eventid", item.getCalendarItemId() + item.getStartDate().toEpochMilli());
        }
        return excludes;
    }

    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }
}

