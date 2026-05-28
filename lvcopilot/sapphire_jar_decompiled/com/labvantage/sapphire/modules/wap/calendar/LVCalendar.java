/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  biweekly.Biweekly
 *  biweekly.ICalendar
 *  biweekly.component.VEvent
 *  biweekly.property.RecurrenceId
 *  biweekly.property.RecurrenceRule
 *  biweekly.util.ICalDate
 *  biweekly.util.com.google.ical.compat.javautil.DateIterator
 */
package com.labvantage.sapphire.modules.wap.calendar;

import biweekly.Biweekly;
import biweekly.ICalendar;
import biweekly.component.VEvent;
import biweekly.property.RecurrenceId;
import biweekly.property.RecurrenceRule;
import biweekly.util.ICalDate;
import biweekly.util.com.google.ical.compat.javautil.DateIterator;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.I18nUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.modules.wap.CalendarConverter;
import com.labvantage.sapphire.modules.wap.calendar.CalendarFactory;
import com.labvantage.sapphire.modules.wap.calendar.CalendarItem;
import com.labvantage.sapphire.modules.wap.calendar.LVCalendarCache;
import com.labvantage.sapphire.modules.wap.workhours.WorkHours;
import com.labvantage.sapphire.scheduler.ScheduleEvent;
import com.labvantage.sapphire.scheduler.ScheduleRule;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.Stack;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class LVCalendar
extends LVCalendarCache {
    private final CalendarConverter calendarConverter;
    private CalendarFactory factory;
    private final ActionProcessor actionProcessor;
    private final QueryProcessor queryProcessor;
    private final CalendarFactory.BroadcastHandler broadcastHandler;
    private final UndoHandler undoHandler;
    private final LVCalendarCache cache;
    private LVCalendar parent = null;
    private ArrayList<LVCalendar> extraCalendars = new ArrayList();

    public LVCalendar(CalendarFactory factory, LVCalendarCache cache, ActionProcessor actionProcessor, QueryProcessor queryProcessor, DateTimeUtil dtu, CalendarFactory.BroadcastHandler broadcastHandler) {
        super(cache);
        this.factory = factory;
        this.actionProcessor = actionProcessor;
        this.queryProcessor = queryProcessor;
        this.broadcastHandler = broadcastHandler;
        this.cache = cache;
        this.calendarConverter = new CalendarConverter(dtu);
        this.undoHandler = new UndoHandler();
    }

    public void setTimeZone(ZoneId zone) {
        this.timezone = zone;
    }

    public void setTimeZone(TimeZone timezone) {
        this.timezone = timezone.toZoneId();
    }

    public ZoneId getTimeZone() {
        return this.timezone;
    }

    public WorkHours getCoreHours() {
        return this.getCoreHours(true);
    }

    public WorkHours getCoreHours(boolean inherit) {
        if (inherit) {
            if (this.corehours != null && this.corehours.getOverride() == WorkHours.OverrideType.OVERRIDE) {
                return this.corehours;
            }
            if (this.parent != null && this.corehours.getOverride() != WorkHours.OverrideType.OVERRIDE) {
                WorkHours workHours = this.parent.getCoreHours();
                return workHours;
            }
            WorkHours.OverrideType ot = this.corehours.getOverride();
            this.corehours = new WorkHours();
            this.corehours.setTimezone(this.timezone);
            this.corehours.setOverride(ot);
            return this.corehours;
        }
        if (this.corehours != null) {
            return this.corehours;
        }
        this.corehours = new WorkHours();
        this.corehours.setTimezone(this.timezone);
        return this.corehours;
    }

    public LVCalendar getParent() {
        return this.parent;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setCoreHours(WorkHours coreHours) {
        if (coreHours != null) {
            LVCalendarCache lVCalendarCache = this.cache;
            synchronized (lVCalendarCache) {
                this.corehours = coreHours;
                this.cache.corehours = coreHours;
            }
        }
    }

    public void overrideCoreHours(WorkHours coreHours) {
        if (coreHours != null) {
            coreHours.setOverride(WorkHours.OverrideType.OVERRIDE);
            this.setCoreHours(coreHours);
        }
    }

    public boolean isChild() {
        return this.parent != null;
    }

    public void addParent(LVCalendar parent) {
        this.parent = parent;
    }

    public void addExtraCalendar(LVCalendar extra) {
        this.extraCalendars.add(extra);
    }

    private static String readStringFromURL(String requestURL) {
        StringBuilder out = new StringBuilder();
        try (Scanner scanner = new Scanner(new URL(requestURL).openStream(), StandardCharsets.UTF_8.toString());){
            scanner.useDelimiter("\\A");
            while (scanner.hasNext()) {
                out.append(scanner.next());
            }
        }
        catch (Exception e) {
            Trace.logWarn(e.getMessage());
        }
        return out.toString();
    }

    public void saveCalendar() throws SapphireException {
        PropertyList editsdi = new PropertyList();
        editsdi.setProperty("sdcid", "LV_Calendar");
        editsdi.setProperty("keyid1", this.calendarid);
        editsdi.setProperty("corehours", this.corehours.toJSONString());
        editsdi.setProperty("sourceflag", this.sourceflag.length() > 0 ? this.sourceflag : "I");
        editsdi.setProperty("sharedflag", this.sharedflag.length() > 0 ? this.sharedflag : "Y");
        editsdi.setProperty("externaloptions", this.externalOptions == null ? "" : this.externalOptions);
        try {
            this.actionProcessor.processAction("EditSDI", "1", editsdi);
        }
        catch (SapphireException e) {
            throw new SapphireException("Failed to update Calendar", e);
        }
    }

    public void loadCalendar(String calendarid) throws SapphireException {
        String sql = this.timezone == null ? "SELECT COALESCE( sysuser.timezone, department.timezone, instrument.timezone, calendar.timezone ) owner_timezone, calendar.*  FROM calendar  LEFT OUTER JOIN sysuser ON sysuser.calendarid=calendar.calendarid  LEFT OUTER JOIN department ON department.calendarid=calendar.calendarid  LEFT OUTER JOIN instrument ON instrument.calendarid=calendar.calendarid WHERE calendar.calendarid=?" : "SELECT calendar.* FROM calendar WHERE calendarid=?";
        DataSet primary = this.queryProcessor.getPreparedSqlDataSet(sql, (Object[])new String[]{calendarid}, true);
        if (primary.size() == 0) {
            throw new SapphireException("Calendar " + calendarid + " not recognized");
        }
        boolean bl = this.isSharedAndNoTimezone = primary.getValue(0, "sharedflag").equals("Y") && primary.getValue(0, "owner_timezone").length() == 0;
        if (this.timezone == null) {
            this.timezone = I18nUtil.getZoneIdFromString(primary.getValue(0, "owner_timezone", TimeZone.getDefault().getID()));
        }
        this.calendarid = primary.getString(0, "calendarid");
        this.sharedflag = primary.getString(0, "sharedflag");
        this.sourceflag = primary.getString(0, "sourceflag");
        this.externalOptions = primary.getClob(0, "externaloptions");
        this.corehours = WorkHours.getInstance(primary.getString(0, "corehours"), this.timezone);
        if (this.cache != null) {
            this.cache.sharedflag = this.sharedflag;
            this.cache.sourceflag = this.sourceflag;
            this.cache.externalOptions = this.externalOptions;
            this.cache.corehours = this.corehours;
            this.cache.timezone = this.timezone;
            this.cache.isSharedAndNoTimezone = this.isSharedAndNoTimezone;
        }
    }

    public void loadCalendarItems(String rangeFromStringUTC, String rangeToStringUTC) throws SapphireException {
        Instant rangeFromInstantUTC = this.calendarConverter.getIsoInstant(rangeFromStringUTC);
        Instant rangeToInstantUTC = this.calendarConverter.getIsoInstant(rangeToStringUTC);
        this.loadCalendarItems(rangeFromInstantUTC, rangeToInstantUTC);
    }

    public static ICalendar getICalendar(String externalOptions) throws SapphireException {
        ICalendar iCalendar = null;
        if (externalOptions != null && externalOptions.length() > 0) {
            PropertyList externalOps = null;
            try {
                if (externalOptions.startsWith("{") && externalOptions.endsWith("}")) {
                    externalOps = new PropertyList(new JSONObject(externalOptions));
                } else {
                    externalOps = new PropertyList();
                    externalOps.setProperty("url", externalOptions);
                }
                String u = externalOps.getProperty("url");
                if (u.length() > 0) {
                    String icalstring = "";
                    if (u.startsWith("BEGIN:VCALENDAR")) {
                        icalstring = u;
                    } else {
                        if (u.startsWith("webcal://")) {
                            u = "http://" + u.substring("webcal://".length());
                        }
                        icalstring = LVCalendar.readStringFromURL(u);
                    }
                    if (icalstring.length() > 0) {
                        try {
                            iCalendar = Biweekly.parse((String)icalstring).first();
                            return iCalendar;
                        }
                        catch (Exception e) {
                            throw new SapphireException("Unable to parse iCal.", e);
                        }
                    }
                    throw new SapphireException("Unable to load ical from url.");
                }
                throw new SapphireException("No ical URL provided.");
            }
            catch (Exception e) {
                throw new SapphireException("Unable to load ical.");
            }
        }
        throw new SapphireException("No external options provided.");
    }

    public boolean isExternal() {
        return this.sourceflag != null && this.sourceflag.equalsIgnoreCase("E");
    }

    public void clearCache() {
        if (this.isExternal()) {
            this.expireExternalCalendarCache();
        }
        try {
            this.broadcastHandler.resetCache(this.calendarid);
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void expireExternalCalendarCache() {
        LVCalendarCache lVCalendarCache = this.cache;
        synchronized (lVCalendarCache) {
            if (!this.isCalendarCacheExpired()) {
                this.cache.setLoadedTime(null);
            }
        }
    }

    private boolean isCalendarCacheExpired() {
        if (this.cache.getLoadedTime() == null) {
            return true;
        }
        Calendar now = Calendar.getInstance();
        Calendar expired = (Calendar)this.cache.getLoadedTime().clone();
        expired.add(12, 60);
        return expired.compareTo(now) < 1;
    }

    public void importExternal() throws SapphireException {
        if (this.isExternal()) {
            CalendarItem ci;
            int i;
            this.sourceflag = "I";
            this.externalOptions = "";
            if (this.cache != null) {
                this.cache.sourceflag = this.sourceflag;
                this.cache.externalOptions = this.externalOptions;
            }
            ArrayList<CalendarItem> temp = new ArrayList<CalendarItem>();
            for (i = 0; i < this.calendarItems.size(); ++i) {
                ci = (CalendarItem)this.calendarItems.get(i);
                temp.add(ci);
            }
            this.calendarItems.clear();
            this.clearCache();
            this.undoHandler.steps.clear();
            for (i = 0; i < temp.size(); ++i) {
                ci = (CalendarItem)temp.get(i);
                this.createCalendarItem(ci.getCalendarItemLabel(), ci.getStartDate(), ci.getEndDate(), ci.isAllDayFlag());
            }
        } else {
            throw new SapphireException("Not an external calendar.");
        }
        this.saveCalendar();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void loadCalendarItems(Instant rangeFromUTC, Instant rangeToUTC) throws SapphireException {
        if (this.isExternal()) {
            LVCalendarCache lVCalendarCache = this.cache;
            synchronized (lVCalendarCache) {
                if (this.isCalendarCacheExpired()) {
                    try {
                        this.calendarItems.clear();
                        ICalendar iCalendar = LVCalendar.getICalendar(this.externalOptions);
                        if (iCalendar.getEvents().size() > 0) {
                            ArrayList<Long> overrides = new ArrayList<Long>();
                            for (int e = 0; e < iCalendar.getEvents().size(); ++e) {
                                RecurrenceId id = ((VEvent)iCalendar.getEvents().get(e)).getRecurrenceId();
                                if (id == null) continue;
                                Calendar c = Calendar.getInstance();
                                c.setTimeInMillis(((ICalDate)id.getValue()).getTime());
                                overrides.add(((ICalDate)id.getValue()).getTime());
                            }
                            for (int i = 0; i < iCalendar.getEvents().size(); ++i) {
                                String uid;
                                VEvent iEvent = (VEvent)iCalendar.getEvents().get(i);
                                long startMillisUTC = ((ICalDate)iEvent.getDateStart().getValue()).getTime();
                                long endMillisUTC = ((ICalDate)iEvent.getDateEnd().getValue()).getTime();
                                Instant startInstantUTC = Instant.ofEpochMilli(startMillisUTC);
                                Instant endInstantUTC = Instant.ofEpochMilli(endMillisUTC);
                                if (iEvent.getRecurrenceRule() == null) {
                                    String uid2;
                                    CalendarItem calendarItem = new CalendarItem(this);
                                    String string = uid2 = iEvent.getUid() != null ? (String)iEvent.getUid().getValue() : "";
                                    if (uid2.length() == 0) {
                                        uid2 = "EX-" + this.calendarid + "-" + i;
                                    }
                                    calendarItem.setCalendarItemId(uid2);
                                    calendarItem.setCalendarId(this.calendarid);
                                    calendarItem.setFromExternal(true);
                                    calendarItem.setStartDate(startInstantUTC);
                                    calendarItem.setEndDate(endInstantUTC);
                                    calendarItem.setCalendarItemLabel(iEvent.getSummary() != null ? (String)iEvent.getSummary().getValue() : "No Label");
                                    this.calendarItems.add(calendarItem);
                                    continue;
                                }
                                RecurrenceRule reoccurance = iEvent.getRecurrenceRule();
                                Date startDateUTC = new Date(startMillisUTC);
                                DateIterator dateIterator = reoccurance.getDateIterator(startDateUTC, TimeZone.getTimeZone("UTC"));
                                long duration = endMillisUTC - startMillisUTC;
                                String string = uid = iEvent.getUid() != null ? (String)iEvent.getUid().getValue() : "";
                                if (uid.length() == 0) {
                                    uid = "EX-" + this.calendarid + "-" + i;
                                }
                                boolean cont = true;
                                int count = 0;
                                while (dateIterator.hasNext() && cont) {
                                    ++count;
                                    Date dateNext = (Date)dateIterator.next();
                                    Instant nextStartUTC = dateNext.toInstant();
                                    if (!overrides.contains(dateNext.getTime())) {
                                        CalendarItem calendarItem = new CalendarItem(this);
                                        calendarItem.setCalendarId(uid + "-" + count);
                                        calendarItem.setCalendarId(this.calendarid);
                                        calendarItem.setStartDate(nextStartUTC);
                                        Instant nextEndUTC = nextStartUTC.plusMillis(duration);
                                        calendarItem.setEndDate(nextEndUTC);
                                        calendarItem.setCalendarItemLabel(iEvent.getSummary() != null ? (String)iEvent.getSummary().getValue() : "No Label");
                                        calendarItem.setIsInstance(true);
                                        this.calendarItems.add(calendarItem);
                                    }
                                    cont = (int)ChronoUnit.DAYS.between(startDateUTC.toInstant(), nextStartUTC) < 366;
                                }
                            }
                        }
                        this.cache.setLoadedTime();
                    }
                    catch (Exception e) {
                        throw new SapphireException("Unable to parse iCal.", e);
                    }
                }
            }
        }
        if (this.parent != null) {
            this.parent.loadCalendarItems(rangeFromUTC, rangeToUTC);
        }
        if (!this.isFullyLoaded()) {
            if (rangeFromUTC == null || rangeToUTC == null) {
                throw new SapphireException("Unrecognized dates from " + rangeFromUTC + " to " + rangeToUTC);
            }
            if (rangeFromUTC.isAfter(rangeToUTC)) {
                throw new SapphireException("The From must be before the To");
            }
            if (this.loadedFromInstantUTC == null || !rangeFromUTC.isAfter(this.loadedFromInstantUTC) || !rangeToUTC.isBefore(this.loadedToInstantUTC)) {
                LVCalendarCache lVCalendarCache = this.cache;
                synchronized (lVCalendarCache) {
                    Instant queryToInstantUTC;
                    Instant queryFromInstantUTC;
                    if (this.loadedFromInstantUTC == null) {
                        queryFromInstantUTC = rangeFromUTC;
                        queryToInstantUTC = rangeToUTC;
                    } else {
                        Instant instant = queryFromInstantUTC = rangeFromUTC.isBefore(this.loadedFromInstantUTC) ? rangeFromUTC : this.loadedToInstantUTC;
                        queryToInstantUTC = rangeToUTC.isBefore(this.loadedFromInstantUTC) ? this.loadedFromInstantUTC : (rangeToUTC.isAfter(this.loadedToInstantUTC) ? rangeToUTC : this.loadedFromInstantUTC);
                    }
                    LocalDateTime queryToInstantUTCTimestamp = LocalDateTime.ofInstant(queryToInstantUTC, ZoneOffset.UTC);
                    queryToInstantUTCTimestamp = queryToInstantUTCTimestamp.plus(1L, ChronoUnit.MONTHS);
                    queryToInstantUTC = queryToInstantUTCTimestamp.atZone(ZoneOffset.UTC).toInstant();
                    Timestamp queryFrom = this.calendarConverter.convertInstantUtcToDatabaseTimestamp(queryFromInstantUTC);
                    Timestamp queryTo = this.calendarConverter.convertInstantUtcToDatabaseTimestamp(queryToInstantUTC);
                    DataSet items = this.queryProcessor.getPreparedSqlDataSet("SELECT * FROM calendaritem WHERE calendarid=? AND enddt>=? AND startdt<=? AND (repeatflag is null or repeatflag='N')  ORDER BY startdt", new Object[]{this.calendarid, queryFrom, queryTo});
                    for (int i = 0; i < items.size(); ++i) {
                        CalendarItem calendarItem = CalendarItem.getInstanceFromDataSet(this, items, i);
                        if (this.calendarItems.contains(calendarItem)) continue;
                        this.calendarItems.add(calendarItem);
                    }
                    Instant instant = this.loadedFromInstantUTC == null ? queryFromInstantUTC : (this.loadedFromInstantUTC = queryFromInstantUTC.isBefore(this.loadedFromInstantUTC) ? queryFromInstantUTC : this.loadedFromInstantUTC);
                    this.loadedToInstantUTC = this.loadedToInstantUTC == null ? queryToInstantUTC : (queryToInstantUTC.isAfter(this.loadedToInstantUTC) ? queryToInstantUTC : this.loadedToInstantUTC);
                }
                if (!this.isExternal()) {
                    if (!this.isOverridesLoaded()) {
                        this.loadAllOverrides();
                    }
                    if (!this.isRepeatsLoaded()) {
                        this.loadAllRepeats();
                    }
                }
                this.cache.setLoadedTime();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void loadAllCalendarItems() throws SapphireException {
        if (this.parent != null) {
            this.parent.loadAllCalendarItems();
        }
        LVCalendarCache lVCalendarCache = this.cache;
        synchronized (lVCalendarCache) {
            DataSet items = this.queryProcessor.getPreparedSqlDataSet("SELECT * FROM calendaritem WHERE calendarid=? AND (repeatflag is null or repeatflag='N')", new Object[]{this.calendarid});
            for (int i = 0; i < items.size(); ++i) {
                CalendarItem calendarItem = CalendarItem.getInstanceFromDataSet(this, items, i);
                if (this.calendarItems.contains(calendarItem)) continue;
                this.calendarItems.add(calendarItem);
            }
            this.setFullyLoaded(true);
        }
        if (!this.isOverridesLoaded()) {
            this.loadAllOverrides();
        }
        if (!this.isRepeatsLoaded()) {
            this.loadAllRepeats();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void loadAllOverrides() throws SapphireException {
        DataSet overrides = this.queryProcessor.getPreparedSqlDataSet("SELECT * FROM calendaritem WHERE calendarid=? AND overrideflag='Y'", new Object[]{this.calendarid});
        this.allOverrides = new HashMap();
        LVCalendarCache lVCalendarCache = this.cache;
        synchronized (lVCalendarCache) {
            for (int i = 0; i < overrides.size(); ++i) {
                CalendarItem override = CalendarItem.getInstanceFromDataSet(this, overrides, i);
                this.allOverrides.put(override.getOverrideCalendarId() + ";" + override.getOverrideCalendarItemId() + ";" + override.getOverrideStartDate(), override);
            }
        }
        this.setOverridesLoaded(true);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void loadAllRepeats() throws SapphireException {
        DataSet repeats = this.queryProcessor.getPreparedSqlDataSet("SELECT * FROM calendaritem WHERE calendarid=? AND repeatflag='Y'", new Object[]{this.calendarid});
        this.allRepeats = new ArrayList();
        LVCalendarCache lVCalendarCache = this.cache;
        synchronized (lVCalendarCache) {
            for (int i = 0; i < repeats.size(); ++i) {
                CalendarItem repeat = CalendarItem.getInstanceFromDataSet(this, repeats, i);
                this.allRepeats.add(repeat);
            }
        }
        this.setRepeatsLoaded(true);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private CalendarItem loadCalendarItem(String calendaritemid) {
        DataSet ds = this.queryProcessor.getPreparedSqlDataSet("SELECT * FROM calendaritem WHERE calendarid=? AND calendaritemid=?", new Object[]{this.calendarid, calendaritemid});
        CalendarItem calendarItem = CalendarItem.getInstanceFromDataSet(this, ds, 0);
        LVCalendarCache lVCalendarCache = this.cache;
        synchronized (lVCalendarCache) {
            if (calendarItem.isRepeat()) {
                this.allRepeats.remove(calendarItem);
                this.allRepeats.add(calendarItem);
            } else {
                this.calendarItems.remove(calendarItem);
                this.calendarItems.add(calendarItem);
            }
        }
        return calendarItem;
    }

    public String getId() {
        return this.calendarid;
    }

    public boolean isShared() {
        return this.sharedflag == null || this.sharedflag.length() == 0 || this.sharedflag.equalsIgnoreCase("Y");
    }

    public boolean isInternal() {
        return this.sourceflag == null || !this.sourceflag.equalsIgnoreCase("E");
    }

    public void editCalendarItem(String calendaritemid, String calendarItemLabel, String startStringUTC, String endStringUTC) throws SapphireException {
        Instant startInstantUTC = this.calendarConverter.getIsoInstant(startStringUTC);
        Instant endInstantUTC = this.calendarConverter.getIsoInstant(endStringUTC);
        this.editCalendarItem(calendaritemid, calendarItemLabel, startInstantUTC, endInstantUTC, false, false, 0);
    }

    public void editCalendarItem(String calendaritemid, String calendarItemLabel, Instant startUTC, Instant endUTC) throws SapphireException {
        this.editCalendarItem(calendaritemid, calendarItemLabel, startUTC, endUTC, false, false, 0);
    }

    public void editCalendarItem(String calendaritemid, String calendarItemLabel, String startStringUTC, String endStringUTC, ZoneOffset allDayOffset) throws SapphireException {
        Instant startInstantUTC = this.calendarConverter.getIsoInstant(startStringUTC);
        Instant endInstantUTC = this.calendarConverter.getIsoInstant(endStringUTC);
        this.editCalendarItem(calendaritemid, calendarItemLabel, startInstantUTC, endInstantUTC, allDayOffset);
    }

    public void editCalendarItem(String calendaritemid, String calendarItemLabel, Instant startInstantUTC, Instant endInstantUTC, ZoneOffset allDayOffset) throws SapphireException {
        ZonedDateTime startZoneDatetime = ZonedDateTime.ofInstant(startInstantUTC, allDayOffset);
        ZonedDateTime endZonedDateTime = ZonedDateTime.ofInstant(endInstantUTC, allDayOffset);
        this.editCalendarItem(calendaritemid, calendarItemLabel, startZoneDatetime, endZonedDateTime, true);
    }

    public void editCalendarItem(String calendaritemid, String calendarItemLabel, ZonedDateTime startZoneDatetime, ZonedDateTime endZonedDateTime, boolean allDayFlag) throws SapphireException {
        this.editCalendarItem(calendaritemid, calendarItemLabel, startZoneDatetime, endZonedDateTime, allDayFlag, false);
    }

    public void editCalendarItem(String calendaritemid, String calendarItemLabel, ZonedDateTime startZoneDatetime, ZonedDateTime endZonedDateTime, boolean allDayFlag, boolean recurring) throws SapphireException {
        if (allDayFlag) {
            ZonedDateTime[] back = this.adjustToAllDay(startZoneDatetime, endZonedDateTime);
            startZoneDatetime = back[0];
            endZonedDateTime = back[1];
        }
        this.editCalendarItem(calendaritemid, calendarItemLabel, startZoneDatetime.toInstant(), endZonedDateTime.toInstant(), allDayFlag, recurring, 0);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void editCalendarItem(String calendaritemid, String calendarItemLabel, Instant startInstantUTC, Instant endInstantUTC, boolean allDayFlag, boolean reoccurring, int daysBetween) throws SapphireException {
        CalendarItem item = this.loadCalendarItem(calendaritemid);
        if (!endInstantUTC.isAfter(startInstantUTC)) {
            throw new SapphireException("End date must come after the start date.");
        }
        UndoStep step = new UndoStep("EDITCALENDARITEM");
        step.calendaritemid = calendaritemid;
        step.contents = item.toJSONObject();
        LVCalendarCache lVCalendarCache = this.cache;
        synchronized (lVCalendarCache) {
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", "LV_Calendar");
            props.setProperty("keyid1", this.calendarid);
            props.setProperty("linkid", "Calendar Items");
            props.setProperty("calendaritemid", calendaritemid);
            if (reoccurring) {
                props.setProperty("repeatrule", item.getRepeatRule().substring(0, item.getRepeatRule().lastIndexOf("@")) + "@ " + new SimpleDateFormat("HH:mm").format(Date.from(startInstantUTC)));
            }
            props.setProperty("calendaritemlabel", calendarItemLabel);
            props.setProperty("startdt", this.calendarConverter.convertInstantUtcToUserActionDateString(startInstantUTC));
            props.setProperty("enddt", this.calendarConverter.convertInstantUtcToUserActionDateString(endInstantUTC));
            props.setProperty("alldayflag", allDayFlag ? "Y" : "N");
            this.actionProcessor.processAction("EditSDIDetail", "1", props);
        }
        this.undoHandler.addUndo(step);
        this.loadCalendarItem(calendaritemid);
        this.broadcastHandler.resetCache(this.calendarid);
    }

    public String overrideInstance(String overrideCalendarid, String overrideCalendaritemid, String overrideStartDtUTC, String calendarItemLabel, String startStringUTC, String endStringUTC) throws SapphireException {
        Instant overrideStartDtInstantUTC = this.calendarConverter.getIsoInstant(overrideStartDtUTC);
        Instant startInstantUTC = this.calendarConverter.getIsoInstant(startStringUTC);
        Instant endInstantUTC = this.calendarConverter.getIsoInstant(endStringUTC);
        return this.doOverrideCalendarItem(overrideCalendarid, overrideCalendaritemid, overrideStartDtInstantUTC, false, calendarItemLabel, startInstantUTC, endInstantUTC, false);
    }

    public String overrideInstance(String overrideCalendarid, String overrideCalendaritemid, Instant overrideStartDtInstantUTC, String calendarItemLabel, Instant startInstantUTC, Instant endInstantUTC) throws SapphireException {
        return this.doOverrideCalendarItem(overrideCalendarid, overrideCalendaritemid, overrideStartDtInstantUTC, false, calendarItemLabel, startInstantUTC, endInstantUTC, false);
    }

    public String overrideInstance(String overrideCalendarid, String overrideCalendaritemid, Instant overrideStartDtInstantUTC, String calendarItemLabel, String startStringUTC, String endStringUTC, ZoneOffset allDayOffset) throws SapphireException {
        Instant startInstantUTC = this.calendarConverter.getIsoInstant(startStringUTC);
        Instant endInstantUTC = this.calendarConverter.getIsoInstant(endStringUTC);
        return this.overrideInstance(overrideCalendarid, overrideCalendaritemid, overrideStartDtInstantUTC, calendarItemLabel, startInstantUTC, endInstantUTC, allDayOffset);
    }

    public String overrideInstance(String overrideCalendarid, String overrideCalendaritemid, Instant overrideStartDtInstantUTC, String calendarItemLabel, Instant startInstantUTC, Instant endInstantUTC, ZoneOffset allDayOffset) throws SapphireException {
        ZonedDateTime startZoneDatetime = ZonedDateTime.ofInstant(startInstantUTC, allDayOffset);
        ZonedDateTime endZonedDateTime = ZonedDateTime.ofInstant(endInstantUTC, allDayOffset);
        return this.overrideInstance(overrideCalendarid, overrideCalendaritemid, overrideStartDtInstantUTC, calendarItemLabel, startZoneDatetime, endZonedDateTime, true);
    }

    public String overrideInstance(String overrideCalendarid, String overrideCalendaritemid, Instant overrideStartDtInstantUTC, String calendarItemLabel, ZonedDateTime startZoneDatetime, ZonedDateTime endZonedDateTime, boolean allDayFlag) throws SapphireException {
        if (allDayFlag) {
            ZonedDateTime[] back = this.adjustToAllDay(startZoneDatetime, endZonedDateTime);
            startZoneDatetime = back[0];
            endZonedDateTime = back[1];
        }
        return this.doOverrideCalendarItem(overrideCalendarid, overrideCalendaritemid, overrideStartDtInstantUTC, false, calendarItemLabel, startZoneDatetime.toInstant(), endZonedDateTime.toInstant(), allDayFlag);
    }

    public String overrideInstanceAsDelete(String overrideCalendarid, String overrideCalendaritemid, String overrideStartDtUTC) throws SapphireException {
        Instant overrideStartDtInstantUTC = this.calendarConverter.getIsoInstant(overrideStartDtUTC);
        return this.overrideInstanceAsDelete(overrideCalendarid, overrideCalendaritemid, overrideStartDtInstantUTC);
    }

    public String overrideInstanceAsDelete(String overrideCalendarid, String overrideCalendaritemid, Instant overrideStartDtInstantUTC) throws SapphireException {
        return this.doOverrideCalendarItem(overrideCalendarid, overrideCalendaritemid, overrideStartDtInstantUTC, true, "", null, null, false);
    }

    public String overrideCalendarItem(String overrideCalendarid, String overrideCalendaritemid, String calendarItemLabel, String startStringUTC, String endStringUTC) throws SapphireException {
        Instant startInstantUTC = this.calendarConverter.getIsoInstant(startStringUTC);
        Instant endInstantUTC = this.calendarConverter.getIsoInstant(endStringUTC);
        return this.doOverrideCalendarItem(overrideCalendarid, overrideCalendaritemid, null, false, calendarItemLabel, startInstantUTC, endInstantUTC, false);
    }

    public String overrideCalendarItem(String overrideCalendarid, String overrideCalendaritemid, String calendarItemLabel, Instant startInstantUTC, Instant endInstantUTC) throws SapphireException {
        return this.doOverrideCalendarItem(overrideCalendarid, overrideCalendaritemid, null, false, calendarItemLabel, startInstantUTC, endInstantUTC, false);
    }

    public String overrideCalendarItem(String overrideCalendarid, String overrideCalendaritemid, String calendarItemLabel, String startStringUTC, String endStringUTC, boolean allDayFlag) throws SapphireException {
        Instant startInstantUTC = this.calendarConverter.getIsoInstant(startStringUTC);
        Instant endInstantUTC = this.calendarConverter.getIsoInstant(endStringUTC);
        return this.doOverrideCalendarItem(overrideCalendarid, overrideCalendaritemid, null, false, calendarItemLabel, startInstantUTC, endInstantUTC, allDayFlag);
    }

    public String overrideCalendarItem(String overrideCalendarid, String overrideCalendaritemid, String calendarItemLabel, Instant startInstantUTC, Instant endInstantUTC, ZoneOffset allDayOffset) throws SapphireException {
        ZonedDateTime startZoneDatetime = ZonedDateTime.ofInstant(startInstantUTC, allDayOffset);
        ZonedDateTime endZonedDateTime = ZonedDateTime.ofInstant(endInstantUTC, allDayOffset);
        return this.overrideCalendarItem(overrideCalendarid, overrideCalendaritemid, calendarItemLabel, startZoneDatetime, endZonedDateTime, true);
    }

    public String overrideCalendarItem(String overrideCalendarid, String overrideCalendaritemid, String calendarItemLabel, ZonedDateTime startZoneDatetime, ZonedDateTime endZonedDateTime, boolean allDayFlag) throws SapphireException {
        if (allDayFlag) {
            ZonedDateTime[] back = this.adjustToAllDay(startZoneDatetime, endZonedDateTime);
            startZoneDatetime = back[0];
            endZonedDateTime = back[1];
        }
        return this.doOverrideCalendarItem(overrideCalendarid, overrideCalendaritemid, null, false, calendarItemLabel, startZoneDatetime.toInstant(), endZonedDateTime.toInstant(), allDayFlag);
    }

    public String overrideCalendarItemAsDelete(String overrideCalendarid, String overrideCalendaritemid) throws SapphireException {
        return this.doOverrideCalendarItem(overrideCalendarid, overrideCalendaritemid, null, true, "", null, null, false);
    }

    private String doOverrideCalendarItem(String overrideCalendarid, String overrideCalendaritemid, Instant overrideStartDtInstantUTC, boolean delete, String calendarItemLabel, Instant startInstantUTC, Instant endInstantUTC, boolean allDayFlag) throws SapphireException {
        CalendarItem overriddenCalendarItem;
        if (overrideCalendarid.equals(this.getId())) {
            overriddenCalendarItem = this.loadCalendarItem(overrideCalendaritemid);
        } else {
            overriddenCalendarItem = this.findParentOrExtraCalendarItem(overrideCalendarid, overrideCalendaritemid);
            if (overriddenCalendarItem == null) {
                for (LVCalendar lVCalendar : this.extraCalendars) {
                }
                throw new SapphireException("Requested calendaritems not found in parent chain");
            }
        }
        if (overriddenCalendarItem == null) {
            throw new SapphireException("Requested calendaritems not found in parent chain");
        }
        PropertyList props = this.getBaseCreateCalendarItemPropertyList();
        props.setProperty("overrideflag", "Y");
        props.setProperty("overridecalendarid", overrideCalendarid);
        props.setProperty("overridecalendaritemid", overrideCalendaritemid);
        if (overrideStartDtInstantUTC == null) {
            overrideStartDtInstantUTC = overriddenCalendarItem.getStartDate();
        }
        props.setProperty("overridestartdt", this.calendarConverter.convertInstantUtcToUserActionDateString(overrideStartDtInstantUTC));
        if (delete) {
            props.setProperty("overridedeleteflag", "Y");
        } else {
            if (!delete && !endInstantUTC.isAfter(startInstantUTC)) {
                throw new SapphireException("End date must come after the start date.");
            }
            props.setProperty("calendaritemlabel", calendarItemLabel);
            props.setProperty("startdt", this.calendarConverter.convertInstantUtcToUserActionDateString(startInstantUTC));
            props.setProperty("enddt", this.calendarConverter.convertInstantUtcToUserActionDateString(endInstantUTC));
            props.setProperty("alldayflag", allDayFlag ? "Y" : "N");
        }
        props.setProperty("returngeneratedkey", "Y");
        this.actionProcessor.processAction("AddSDIDetail", "1", props);
        String calendaritemid = props.getProperty("calendaritemid");
        CalendarItem override = this.loadCalendarItem(calendaritemid);
        UndoStep step = new UndoStep("OVERRIDECALENDARITEM");
        step.calendaritemid = calendaritemid;
        this.undoHandler.addUndo(step);
        this.allOverrides.put(overrideCalendarid + ";" + overrideCalendaritemid + ";" + overrideStartDtInstantUTC, override);
        this.broadcastHandler.resetCache(this.calendarid);
        return calendaritemid;
    }

    public List<CalendarItem> getCalendarItemsBetween(String rangeFromStringUTC, String rangeToStringUTC, boolean includeOverlaps) throws SapphireException {
        Instant rangeFromInstantUTC = this.calendarConverter.getIsoInstant(rangeFromStringUTC);
        Instant rangeToInstantUTC = this.calendarConverter.getIsoInstant(rangeToStringUTC);
        return this.getCalendarItemsBetween(rangeFromInstantUTC, rangeToInstantUTC, includeOverlaps);
    }

    public List<CalendarItem> getCalendarItemsBetween(Instant rangeFromInstantUTC, Instant rangeToInstantUTC, boolean includeOverlaps) throws SapphireException {
        this.loadCalendarItems(rangeFromInstantUTC, rangeToInstantUTC);
        List<CalendarItem> instances = this.getRepeatInstances(rangeFromInstantUTC, rangeToInstantUTC);
        ArrayList<CalendarItem> candidateList = new ArrayList<CalendarItem>();
        candidateList.addAll(this.calendarItems);
        for (CalendarItem calendarItem : instances) {
            CalendarItem calendarItem2 = (CalendarItem)this.allOverrides.get(this.getId() + ";" + calendarItem.getCalendarItemId() + ";" + calendarItem.getStartDate());
            if (calendarItem2 != null && calendarItem2.getOverrideStartDate() != null && calendarItem2.getOverrideStartDate().compareTo(calendarItem.getStartDate()) == 0) continue;
            candidateList.add(calendarItem);
        }
        ArrayList<CalendarItem> matching = new ArrayList<CalendarItem>();
        for (CalendarItem calendarItem : candidateList) {
            if (calendarItem == null || calendarItem.getStartDate() == null || calendarItem.isOverrideFlag() && calendarItem.isOverrideDeleteFlag()) continue;
            if (includeOverlaps) {
                if (calendarItem.getEndDate().isBefore(rangeFromInstantUTC) || calendarItem.getStartDate().isAfter(rangeToInstantUTC)) continue;
                matching.add(calendarItem);
                continue;
            }
            if (calendarItem.getStartDate().isBefore(rangeFromInstantUTC) || calendarItem.getEndDate().isAfter(rangeToInstantUTC)) continue;
            matching.add(calendarItem);
        }
        if (this.extraCalendars.size() > 0) {
            for (LVCalendar lVCalendar : this.extraCalendars) {
                List<CalendarItem> extraItems = lVCalendar.getCalendarItemsBetween(rangeFromInstantUTC, rangeToInstantUTC, includeOverlaps);
                for (CalendarItem extraItem : extraItems) {
                    if (extraItem == null || extraItem.getStartDate() == null) continue;
                    CalendarItem override = (CalendarItem)this.allOverrides.get(extraItem.getCalendarId() + ";" + extraItem.getCalendarItemId() + ";" + extraItem.getStartDate());
                    if (override == null) {
                        matching.add(extraItem);
                        continue;
                    }
                    if (override.getOverrideStartDate() == null || override.getOverrideStartDate().equals(extraItem.getStartDate())) continue;
                    matching.add(extraItem);
                }
            }
        }
        if (this.parent != null) {
            List<CalendarItem> list = this.parent.getCalendarItemsBetween(rangeFromInstantUTC, rangeToInstantUTC, includeOverlaps);
            for (CalendarItem parentCalendarItem : list) {
                if (parentCalendarItem == null || parentCalendarItem.getStartDate() == null) continue;
                CalendarItem override = (CalendarItem)this.allOverrides.get(parentCalendarItem.getCalendarId() + ";" + parentCalendarItem.getCalendarItemId() + ";" + parentCalendarItem.getStartDate());
                if (override == null) {
                    matching.add(parentCalendarItem);
                    continue;
                }
                if (override.getOverrideStartDate() == null || override.getOverrideStartDate().compareTo(parentCalendarItem.getStartDate()) == 0) continue;
                matching.add(parentCalendarItem);
            }
        }
        Collections.sort(matching);
        return matching;
    }

    private List<CalendarItem> getRepeatInstances(Instant rangeFromInstantUTC, Instant rangeToInstantUTC) throws SapphireException {
        Calendar rangeFromUTC = this.calendarConverter.convertUtcInstantToUtcCalendar(rangeFromInstantUTC);
        Calendar rangeToUTC = this.calendarConverter.convertUtcInstantToUtcCalendar(rangeToInstantUTC);
        ArrayList<CalendarItem> instances = new ArrayList<CalendarItem>();
        ScheduleEvent scheduleEvent = new ScheduleEvent(this.calendarid, "", rangeFromUTC, TimeZone.getTimeZone(this.timezone));
        for (CalendarItem repeat : this.allRepeats) {
            long durationMillis = repeat.getEndDate().toEpochMilli() - repeat.getStartDate().toEpochMilli();
            int daysBetween = (int)ChronoUnit.DAYS.between(repeat.getStartDate(), repeat.getEndDate());
            int repeatCount = repeat.getRepeatCount();
            ScheduleRule rule = new ScheduleRule(repeat.getRepeatRule());
            int ruleOffset = rule.getRuleType() == 4 ? 31 : (rule.getRuleType() == 3 ? 7 : 1);
            Calendar eventRangeFrom = (Calendar)rangeFromUTC.clone();
            if (repeat.isAllDayFlag()) {
                eventRangeFrom.add(6, -(daysBetween + 1));
            } else {
                eventRangeFrom.setTimeInMillis(rangeFromUTC.getTimeInMillis() - durationMillis);
            }
            eventRangeFrom.add(6, -ruleOffset);
            if (repeatCount >= 0) {
                eventRangeFrom.setTimeInMillis(repeat.getStartDate().toEpochMilli());
            }
            ZonedDateTime zdtStart = ZonedDateTime.ofInstant(repeat.getStartDate(), this.timezone);
            Calendar tempStartDate = (Calendar)rangeFromUTC.clone();
            tempStartDate.setTimeInMillis(repeat.getStartDate().toEpochMilli());
            try {
                repeat.getStartDate().toEpochMilli();
                ArrayList events = rule.getEvents(scheduleEvent, tempStartDate, rangeToUTC, eventRangeFrom, true);
                for (int i = 0; i < events.size() && (repeatCount == -1 || i < repeatCount); ++i) {
                    ScheduleEvent event = (ScheduleEvent)events.get(i);
                    CalendarItem instance = new CalendarItem(this);
                    instance.setCalendarId(repeat.getCalendarId());
                    instance.setCalendarItemId(repeat.getCalendarItemId());
                    instance.setCalendarItemLabel(repeat.getCalendarItemLabel());
                    instance.setIsInstance(true);
                    ZonedDateTime zdt = ZonedDateTime.ofInstant(event.getEventDt().toInstant(), this.timezone);
                    zdt = zdt.withHour(zdtStart.getHour()).withMinute(zdtStart.getMinute());
                    instance.setStartDate(zdt.toInstant());
                    instance.setAllDayFlag(repeat.isAllDayFlag());
                    if (repeat.isAllDayFlag()) {
                        instance.setEndDate(instance.getStartDate().plus((long)daysBetween, ChronoUnit.DAYS));
                    } else {
                        instance.setEndDate(instance.getStartDate().plusMillis(durationMillis));
                    }
                    instance.setRepeat(true);
                    instances.add(instance);
                }
            }
            catch (SapphireException e) {
                Trace.logWarn("Failed to parse rule " + repeat.getRepeatRule() + ". Ignoring this rule.");
            }
        }
        return instances;
    }

    public List<CalendarItem> getCalendarItemsAt(String date) throws SapphireException {
        return this.getCalendarItemsBetween(date, date, true);
    }

    public CalendarItem getCalendarItem(String calendaritemid) throws SapphireException {
        CalendarItem found = null;
        for (CalendarItem item : this.calendarItems) {
            if (!item.getCalendarItemId().equals(calendaritemid) || item.isInstance()) continue;
            found = item;
        }
        if (found == null) {
            DataSet ds = this.queryProcessor.getPreparedSqlDataSet("SELECT * FROM calendaritem WHERE calendarid=? AND calendaritemid=? ORDER BY startdt", new Object[]{this.calendarid, calendaritemid});
            if (ds.size() == 0) {
                throw new SapphireException("Unable to find calendar item " + calendaritemid);
            }
            found = CalendarItem.getInstanceFromDataSet(this, ds, 0);
        }
        return found;
    }

    public boolean hasOverrides(String calendaritemid) throws SapphireException {
        return this.queryProcessor.getPreparedCount("SELECT count(*) FROM calendaritem WHERE overridecalendarid=? AND overridecalendaritemid=?  ORDER BY startdt", new Object[]{this.calendarid, calendaritemid}) > 0;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     * Converted monitor instructions to comments
     * Lifted jumps to return sites
     */
    public void deleteCalendarItem(String calendaritemid) throws SapphireException {
        DataSet overrideMe;
        CalendarItem calendarItem = this.getCalendarItem(calendaritemid);
        if (calendarItem.isInstance()) {
            throw new SapphireException("You cannot delete an instance of a repeating calendar item. Try overrideing instead.");
        }
        UndoStep step = new UndoStep("DELETECALENDARITEM");
        step.calendaritemid = calendaritemid;
        step.contents = calendarItem.toJSONObject();
        this.undoHandler.addUndo(step);
        if (!calendarItem.isOverrideFlag() && (overrideMe = this.queryProcessor.getPreparedSqlDataSet("SELECT * FROM calendaritem WHERE overridecalendarid=? AND overridecalendaritemid=?", new Object[]{this.calendarid, calendaritemid})).size() > 0) {
            Class<LVCalendar> clazz = LVCalendar.class;
            // MONITORENTER : com.labvantage.sapphire.modules.wap.calendar.LVCalendar.class
            for (int i = 0; i < overrideMe.size(); ++i) {
                String subCalendarid = overrideMe.getString(i, "calendarid");
                String subCalendaritemid = overrideMe.getString(i, "calendaritemid");
                LVCalendar subCalendar = this.factory.getCalendar(subCalendarid);
                subCalendar.deleteCalendarItem(subCalendaritemid);
            }
            // MONITOREXIT : clazz
        }
        LVCalendarCache lVCalendarCache = this.cache;
        // MONITORENTER : lVCalendarCache
        PropertyList props = new PropertyList();
        props.setProperty("sdcid", "LV_Calendar");
        props.setProperty("keyid1", this.calendarid);
        props.setProperty("linkid", "Calendar Items");
        props.setProperty("calendaritemid", calendaritemid);
        this.actionProcessor.processAction("DeleteSDIDetail", "1", props);
        this.calendarItems.remove(calendarItem);
        this.broadcastHandler.resetCache(this.calendarid);
        if (calendarItem.isOverrideFlag()) {
            this.allOverrides.remove(calendarItem.getOverrideCalendarId() + ";" + calendarItem.getOverrideCalendarItemId() + ";" + calendarItem.getOverrideStartDate());
        }
        // MONITOREXIT : lVCalendarCache
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void deleteAllCalendarItems() throws SapphireException {
        LVCalendarCache lVCalendarCache = this.cache;
        synchronized (lVCalendarCache) {
            if (this.sourceflag == null || !this.sourceflag.equalsIgnoreCase("E")) {
                this.loadAllCalendarItems();
                PropertyList props = new PropertyList();
                for (CalendarItem calendarItem : this.calendarItems) {
                    props.setProperty("sdcid", "LV_Calendar");
                    props.setProperty("keyid1", this.calendarid);
                    props.setProperty("linkid", "Calendar Items");
                    props.setProperty("calendaritemid", calendarItem.getCalendarItemId());
                    this.actionProcessor.processAction("DeleteSDIDetail", "1", props);
                }
                for (CalendarItem calendarItem : this.allRepeats) {
                    props.setProperty("sdcid", "LV_Calendar");
                    props.setProperty("keyid1", this.calendarid);
                    props.setProperty("linkid", "Calendar Items");
                    props.setProperty("calendaritemid", calendarItem.getCalendarItemId());
                    this.actionProcessor.processAction("DeleteSDIDetail", "1", props);
                }
                this.broadcastHandler.resetCache(this.calendarid);
                DataSet overrideMe = this.queryProcessor.getPreparedSqlDataSet("SELECT DISTINCT calendarid FROM calendaritem WHERE overridecalendarid=?", new Object[]{this.calendarid});
                for (int i = 0; i < overrideMe.size(); ++i) {
                    this.broadcastHandler.resetCache(overrideMe.getString(i, "calendarid"));
                }
            }
            this.calendarItems.clear();
            this.allOverrides.clear();
            this.allRepeats.clear();
            this.loadedFromInstantUTC = null;
            this.loadedToInstantUTC = null;
            this.setRepeatsLoaded(false);
            this.setFullyLoaded(false);
            this.setOverridesLoaded(false);
        }
    }

    public String createCalendarItem(String calendarItemLabel, String startStringUTC, String endStringUTC) throws SapphireException {
        Instant startInstantUTC = this.calendarConverter.getIsoInstant(startStringUTC);
        Instant endInstantUTC = this.calendarConverter.getIsoInstant(endStringUTC);
        return this.createCalendarItem(calendarItemLabel, startInstantUTC, endInstantUTC, false);
    }

    public String createCalendarItem(String calendarItemLabel, Instant startInstantUTC, Instant endInstantUTC) throws SapphireException {
        return this.createCalendarItem(calendarItemLabel, startInstantUTC, endInstantUTC, false);
    }

    public String createCalendarItem(String calendarItemLabel, String startStringUTC, String endStringUTC, ZoneOffset allDayOffset) throws SapphireException {
        Instant startInstantUTC = this.calendarConverter.getIsoInstant(startStringUTC);
        Instant endInstantUTC = this.calendarConverter.getIsoInstant(endStringUTC);
        return this.createCalendarItem(calendarItemLabel, startInstantUTC, endInstantUTC, allDayOffset);
    }

    public String createCalendarItem(String calendarItemLabel, Instant startInstantUTC, Instant endInstantUTC, ZoneOffset allDayOffset) throws SapphireException {
        ZonedDateTime startZoneDatetime = ZonedDateTime.ofInstant(startInstantUTC, allDayOffset);
        ZonedDateTime endZonedDateTime = ZonedDateTime.ofInstant(endInstantUTC, allDayOffset);
        return this.createCalendarItem(calendarItemLabel, startZoneDatetime, endZonedDateTime, true);
    }

    public String createCalendarItem(String calendarItemLabel, ZonedDateTime startZoneDatetime, ZonedDateTime endZonedDateTime, boolean allDayFlag) throws SapphireException {
        if (allDayFlag) {
            ZonedDateTime[] back = this.adjustToAllDay(startZoneDatetime, endZonedDateTime);
            startZoneDatetime = back[0];
            endZonedDateTime = back[1];
        }
        return this.createCalendarItem(calendarItemLabel, startZoneDatetime.toInstant(), endZonedDateTime.toInstant(), allDayFlag);
    }

    private ZonedDateTime[] adjustToAllDay(ZonedDateTime startZoneDatetime, ZonedDateTime endZonedDateTime) {
        if (startZoneDatetime.getHour() != 0 || startZoneDatetime.getMinute() != 0 || startZoneDatetime.getSecond() != 0) {
            startZoneDatetime = startZoneDatetime.with(LocalTime.of(0, 0, 0));
        }
        if (endZonedDateTime.getHour() != 0 || endZonedDateTime.getMinute() != 0 || endZonedDateTime.getSecond() != 0) {
            endZonedDateTime = endZonedDateTime.plus(1L, ChronoUnit.DAYS);
            endZonedDateTime = endZonedDateTime.with(LocalTime.of(0, 0, 0));
        }
        return new ZonedDateTime[]{startZoneDatetime, endZonedDateTime};
    }

    private String createCalendarItem(String calendarItemLabel, Instant startInstantUTC, Instant endInstantUTC, boolean allDayFlag) throws SapphireException {
        if (!endInstantUTC.isAfter(startInstantUTC)) {
            throw new SapphireException("End date must come after the start date.");
        }
        PropertyList props = this.getBaseCreateCalendarItemPropertyList();
        props.setProperty("calendaritemlabel", calendarItemLabel);
        props.setProperty("startdt", this.calendarConverter.convertInstantUtcToUserActionDateString(startInstantUTC));
        props.setProperty("enddt", this.calendarConverter.convertInstantUtcToUserActionDateString(endInstantUTC));
        props.setProperty("alldayflag", allDayFlag ? "Y" : "N");
        props.setProperty("returngeneratedkey", "Y");
        this.actionProcessor.processAction("AddSDIDetail", "1", props);
        String calendaritemid = props.getProperty("calendaritemid");
        this.loadCalendarItem(calendaritemid);
        this.broadcastHandler.resetCache(this.calendarid);
        UndoStep step = new UndoStep("ADDCALENDARITEM");
        step.calendaritemid = calendaritemid;
        this.undoHandler.addUndo(step);
        return calendaritemid;
    }

    public String createRecurringCalendarItem(String calendarItemLabel, String startStringUTC, String endStringUTC, String rule, int repeatCount) throws SapphireException {
        Instant startInstantUTC = this.calendarConverter.getIsoInstant(startStringUTC);
        Instant endInstantUTC = this.calendarConverter.getIsoInstant(endStringUTC);
        return this.createRecurringCalendarItem(calendarItemLabel, startInstantUTC, endInstantUTC, false, rule, repeatCount);
    }

    public String createRecurringCalendarItem(String calendarItemLabel, Instant startInstantUTC, Instant endInstantUTC, String rule, int repeatCount) throws SapphireException {
        return this.createRecurringCalendarItem(calendarItemLabel, startInstantUTC, endInstantUTC, false, rule, repeatCount);
    }

    public String createRecurringCalendarItem(String calendarItemLabel, String startStringUTC, String endStringUTC, ZoneOffset allDayOffset, String rule, int repeatCount) throws SapphireException {
        Instant startInstantUTC = this.calendarConverter.getIsoInstant(startStringUTC);
        Instant endInstantUTC = this.calendarConverter.getIsoInstant(endStringUTC);
        return this.createRecurringCalendarItem(calendarItemLabel, startInstantUTC, endInstantUTC, allDayOffset, rule, repeatCount);
    }

    public String createRecurringCalendarItem(String calendarItemLabel, Instant startInstantUTC, Instant endInstantUTC, ZoneOffset allDayOffset, String rule, int repeatCount) throws SapphireException {
        ZonedDateTime startZoneDatetime = ZonedDateTime.ofInstant(startInstantUTC, allDayOffset);
        ZonedDateTime endZonedDateTime = ZonedDateTime.ofInstant(endInstantUTC, allDayOffset);
        return this.createRecurringCalendarItem(calendarItemLabel, startZoneDatetime, endZonedDateTime, false, rule, repeatCount);
    }

    public String createRecurringCalendarItem(String calendarItemLabel, ZonedDateTime startZoneDatetime, ZonedDateTime endZonedDateTime, boolean allDayFlag, String rule, int repeatCount) throws SapphireException {
        if (allDayFlag) {
            ZonedDateTime[] back = this.adjustToAllDay(startZoneDatetime, endZonedDateTime);
            startZoneDatetime = back[0];
            endZonedDateTime = back[1];
        }
        return this.createRecurringCalendarItem(calendarItemLabel, startZoneDatetime.toInstant(), endZonedDateTime.toInstant(), allDayFlag, rule, repeatCount);
    }

    private String createRecurringCalendarItem(String calendarItemLabel, Instant startInstantUTC, Instant endInstantUTC, boolean allDayFlag, String rule, int repeatCount) throws SapphireException {
        if (rule == null || rule.isEmpty()) {
            throw new SapphireException("No rule specified");
        }
        if (!endInstantUTC.isAfter(startInstantUTC)) {
            throw new SapphireException("End date must come after the start date.");
        }
        PropertyList props = this.getBaseCreateCalendarItemPropertyList();
        props.setProperty("calendaritemlabel", calendarItemLabel);
        props.setProperty("startdt", this.calendarConverter.convertInstantUtcToUserActionDateString(startInstantUTC));
        props.setProperty("enddt", this.calendarConverter.convertInstantUtcToUserActionDateString(endInstantUTC));
        props.setProperty("alldayflag", allDayFlag ? "Y" : "N");
        props.setProperty("repeatflag", "Y");
        props.setProperty("repeatrule", rule);
        props.setProperty("repeatcount", Integer.toString(repeatCount));
        props.setProperty("returngeneratedkey", "Y");
        this.actionProcessor.processAction("AddSDIDetail", "1", props);
        String calendaritemid = props.getProperty("calendaritemid");
        this.loadCalendarItem(calendaritemid);
        this.broadcastHandler.resetCache(this.calendarid);
        return calendaritemid;
    }

    private CalendarItem findParentOrExtraCalendarItem(String calendarid, String calendaritemid) {
        for (LVCalendar extra : this.extraCalendars) {
            if (!extra.getId().equals(calendarid)) continue;
            try {
                return extra.getCalendarItem(calendaritemid);
            }
            catch (SapphireException e) {
                return null;
            }
        }
        if (this.parent == null) {
            return null;
        }
        if (this.parent.getId().equals(calendarid)) {
            try {
                return this.parent.getCalendarItem(calendaritemid);
            }
            catch (SapphireException e) {
                return null;
            }
        }
        return this.parent.findParentOrExtraCalendarItem(calendarid, calendaritemid);
    }

    private PropertyList getBaseCreateCalendarItemPropertyList() {
        PropertyList props = new PropertyList();
        props.setProperty("sdcid", "LV_Calendar");
        props.setProperty("keyid1", this.calendarid);
        props.setProperty("linkid", "Calendar Items");
        return props;
    }

    public JSONObject toJSONObject(ZonedDateTime zonedFrom, ZonedDateTime zonedTo, ZoneId displayTimeZone) throws JSONException {
        List<CalendarItem> repeatsIn;
        List<CalendarItem> calItems;
        JSONObject jso = new JSONObject();
        jso.put("calendarid", this.getId());
        if (displayTimeZone == null) {
            displayTimeZone = ZoneOffset.UTC;
        }
        jso.put("disableRepeating", !this.isSharedAndNoTimezone && (this.getTimeZone() == null || !TimeZone.getTimeZone(displayTimeZone).toZoneId().getRules().equals(this.getTimeZone().getRules())));
        try {
            WorkHours ch = this.getCoreHours();
            jso.put("corehours", ch != null ? ch.toJSONObject(zonedFrom, displayTimeZone) : new JSONObject());
        }
        catch (Exception e) {
            jso.put("corehours", new JSONObject());
        }
        JSONArray items = new JSONArray();
        jso.put("calendaritems", items);
        try {
            calItems = zonedFrom != null && zonedTo != null ? this.getCalendarItemsBetween(zonedFrom.toInstant(), zonedTo.toInstant(), true) : null;
        }
        catch (Exception e) {
            throw new JSONException(e);
        }
        if (calItems != null) {
            for (CalendarItem item : calItems) {
                items.put(item.toJSONObject(displayTimeZone));
            }
        }
        JSONArray repeats = new JSONArray();
        jso.put("calendaritemrepeats", repeats);
        try {
            repeatsIn = zonedFrom != null && zonedTo != null ? this.getRepeatInstances(zonedFrom.toInstant(), zonedTo.toInstant()) : null;
        }
        catch (Exception e) {
            throw new JSONException(e);
        }
        if (repeatsIn != null) {
            for (CalendarItem repeat : repeatsIn) {
                repeats.put(repeat.toJSONObject(displayTimeZone));
            }
        }
        return jso;
    }

    public JSONObject toJSONObject() throws JSONException {
        JSONObject jso = new JSONObject();
        jso.put("calendarid", this.getId());
        JSONArray items = new JSONArray();
        jso.put("calendaritems", items);
        for (CalendarItem item : this.calendarItems) {
            items.put(item.toJSONObject());
        }
        JSONArray repeats = new JSONArray();
        jso.put("calendaritemrepeats", repeats);
        for (CalendarItem repeat : this.allRepeats) {
            repeats.put(repeat.toJSONObject());
        }
        return jso;
    }

    public boolean canUndo() {
        return this.undoHandler.steps.size() > 0 && this.undoHandler.steps.get(0) != null;
    }

    public JSONArray getUndoStack() throws JSONException {
        return this.undoHandler.toJSONArray();
    }

    public void setUndoStack(JSONArray stack) throws JSONException {
        this.undoHandler.setJSONArray(stack);
    }

    public void undo() throws SapphireException {
        UndoStep step = this.undoHandler.getUndo();
        if (step == null) {
            throw new SapphireException("Nothing to undo");
        }
        this.undoHandler.startUndo();
        if (step.command.equals("ADDCALENDARITEM") || step.command.equals("OVERRIDECALENDARITEM")) {
            this.deleteCalendarItem(step.calendaritemid);
        } else if (step.command.equals("EDITCALENDARITEM")) {
            CalendarItem ci = new CalendarItem(this, step.contents);
            this.editCalendarItem(step.calendaritemid, ci.getCalendarItemLabel(), ci.getStartDate(), ci.getEndDate());
        } else if (step.command.equals("DELETECALENDARITEM")) {
            CalendarItem ci = new CalendarItem(this, step.contents);
            if (!ci.isOverrideFlag()) {
                this.editCalendarItem(step.calendaritemid, ci.getCalendarItemLabel(), ci.getStartDate(), ci.getEndDate());
            } else {
                this.overrideCalendarItem(ci.getOverrideCalendarId(), ci.getOverrideCalendarItemId(), ci.getCalendarItemLabel(), ci.getStartDate(), ci.getEndDate());
            }
        }
        this.undoHandler.endUndo();
    }

    public CalendarConverter getCalendarConverter() {
        return this.calendarConverter;
    }

    public class UndoStep {
        public static final String ADDCALENDARITEM = "ADDCALENDARITEM";
        public static final String EDITCALENDARITEM = "EDITCALENDARITEM";
        public static final String DELETECALENDARITEM = "DELETECALENDARITEM";
        public static final String OVERRIDECALENDARITEM = "OVERRIDECALENDARITEM";
        public String command;
        public String calendaritemid;
        public JSONObject contents;

        public UndoStep(String command) {
            this.command = command;
        }

        public UndoStep(JSONObject jso) {
            this.command = jso.optString("command");
            this.calendaritemid = jso.optString("calendaritemid");
            this.contents = jso.optJSONObject("contents");
        }

        public JSONObject toJSONObject() throws JSONException {
            JSONObject jso = new JSONObject();
            jso.put("command", this.command);
            jso.put("calendaritemid", this.calendaritemid);
            jso.put("contents", this.contents);
            return jso;
        }
    }

    class UndoHandler {
        private boolean undoing = false;
        Stack<UndoStep> steps = new Stack();

        UndoHandler() {
        }

        public void startUndo() {
            this.undoing = true;
        }

        public void endUndo() {
            this.undoing = false;
        }

        public void addUndo(UndoStep step) {
            if (!this.undoing) {
                this.steps.push(step);
            }
        }

        public UndoStep getUndo() {
            if (this.steps.size() == 0) {
                return null;
            }
            UndoStep step = this.steps.pop();
            return step;
        }

        public JSONArray toJSONArray() throws JSONException {
            JSONArray jsonArray = new JSONArray();
            for (UndoStep step : this.steps) {
                if (step == null) continue;
                jsonArray.put(step.toJSONObject());
            }
            return jsonArray;
        }

        public void setJSONArray(JSONArray array) throws JSONException {
            this.steps.clear();
            for (int i = 0; i < array.length(); ++i) {
                UndoStep step = new UndoStep((JSONObject)array.get(i));
                this.steps.push(step);
            }
        }
    }
}

