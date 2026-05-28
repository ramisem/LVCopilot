/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.wap.calendar;

import com.labvantage.sapphire.modules.wap.calendar.LVCalendar;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.LinkedHashMap;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.util.DataSet;

public class CalendarItem
implements Comparable {
    private String calendarid;
    private String calendaritemid;
    private String calendarItemLabel;
    private Instant startInstantUTC = null;
    private Instant endInstantUTC = null;
    private boolean fromExternal = false;
    private boolean allDayFlag;
    private boolean overrideFlag = false;
    private String overrideCalendarid;
    private String overrideCalendaritemid;
    private Instant overrideStartInstantUTC = null;
    private boolean overrideDeleteFlag = false;
    private boolean isRepeat = false;
    private String repeatRule;
    private int repeatCount;
    private boolean isInstance = false;
    private LVCalendar calendar;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneOffset.UTC);

    public CalendarItem(LVCalendar calendar) {
        this.calendar = calendar;
    }

    public CalendarItem(LVCalendar calendar, JSONObject jso) throws SapphireException {
        this.calendar = calendar;
        this.calendarid = jso.optString("calendarid");
        this.calendarItemLabel = jso.optString("calendaritemlabel", "");
        this.startInstantUTC = this.getJSONInstantUTC(jso, "startdate");
        this.endInstantUTC = this.getJSONInstantUTC(jso, "enddate");
        this.allDayFlag = jso.optString("alldayflag").equals("Y");
        this.overrideFlag = jso.optString("overrideflag").equals("Y");
        this.overrideCalendarid = jso.optString("overridecalendarid");
        this.overrideCalendaritemid = jso.optString("overridecalendaritemdid");
        this.overrideDeleteFlag = jso.optString("overridedeleteflag").equals("Y");
        this.overrideStartInstantUTC = this.getJSONInstantUTC(jso, "overridestartdate");
        this.isRepeat = jso.optString("repeatflag").equals("Y");
        this.repeatRule = jso.optString("repeatrule");
        this.repeatCount = jso.optInt("repeatcount", -1);
        this.isInstance = jso.optString("instance").equals("Y");
    }

    public JSONObject toJSONObject() {
        return this.toJSONObject(null);
    }

    public JSONObject toJSONObject(ZoneId displayTimeZone) {
        LinkedHashMap map = new LinkedHashMap();
        JSONObject jso = new JSONObject(map);
        try {
            jso.put("calendarid", this.calendarid);
            jso.put("calendaritemid", this.calendaritemid);
            jso.put("calendaritemlabel", this.calendarItemLabel);
            if (!this.overrideDeleteFlag) {
                if (displayTimeZone == null || displayTimeZone.getId().equalsIgnoreCase("UTC")) {
                    jso.put("startdate", this.startInstantUTC.getEpochSecond() * 1000L);
                    jso.put("enddate", this.endInstantUTC.getEpochSecond() * 1000L);
                } else {
                    jso.put("startdate_offset", displayTimeZone.getRules().getOffset(this.startInstantUTC).getTotalSeconds() / 60);
                    jso.put("enddate_offset", displayTimeZone.getRules().getOffset(this.endInstantUTC).getTotalSeconds() / 60);
                    ZonedDateTime zonedStart = ZonedDateTime.ofInstant(this.startInstantUTC, displayTimeZone);
                    ZonedDateTime zonedEnd = ZonedDateTime.ofInstant(this.endInstantUTC, displayTimeZone);
                    if (this.isAllDayFlag()) {
                        zonedStart = zonedStart.getHour() <= 12 ? zonedStart.minusHours(zonedStart.getHour()) : zonedStart.plusHours(24 - zonedStart.getHour());
                        zonedEnd = zonedEnd.getHour() <= 12 ? zonedEnd.minusHours(zonedEnd.getHour()) : zonedEnd.plusHours(24 - zonedEnd.getHour());
                        zonedStart = zonedStart.with(ChronoField.MINUTE_OF_HOUR, 0L);
                        zonedEnd = zonedEnd.with(ChronoField.MINUTE_OF_HOUR, 0L);
                    }
                    jso.put("startdate", zonedStart.toEpochSecond() * 1000L);
                    jso.put("enddate", zonedEnd.toEpochSecond() * 1000L);
                }
            }
            jso.put("alldayflag", this.allDayFlag ? "Y" : "N");
            jso.put("overrideflag", this.overrideFlag ? "Y" : "N");
            jso.put("overridecalendarid", this.overrideCalendarid);
            jso.put("overridecalendaritemdid", this.overrideCalendaritemid);
            jso.put("overridedeleteflag", this.overrideDeleteFlag ? "Y" : "N");
            jso.put("overridestartdate", this.overrideStartInstantUTC == null ? -1L : this.overrideStartInstantUTC.getEpochSecond());
            jso.put("repeatflag", this.isRepeat ? "Y" : "N");
            jso.put("repeatrule", this.repeatRule);
            jso.put("repeatcount", this.repeatCount);
            jso.put("instance", this.isInstance ? "Y" : "N");
            jso.put("fromexternal", this.fromExternal ? "Y" : "N");
        }
        catch (JSONException e) {
            return null;
        }
        return jso;
    }

    public static CalendarItem getInstanceFromDataSet(LVCalendar calendar, DataSet ds, int i) {
        CalendarItem calendarItem = new CalendarItem(calendar);
        calendarItem.calendarid = ds.getValue(i, "calendarid");
        calendarItem.calendaritemid = ds.getValue(i, "calendaritemid");
        calendarItem.calendarItemLabel = ds.getString(i, "calendaritemlabel");
        calendarItem.startInstantUTC = calendar.getCalendarConverter().convertDatabaseCalendarToInstantUtc(ds.getCalendar(i, "startdt"));
        calendarItem.endInstantUTC = calendar.getCalendarConverter().convertDatabaseCalendarToInstantUtc(ds.getCalendar(i, "enddt"));
        calendarItem.allDayFlag = ds.getValue(i, "alldayflag").equals("Y");
        calendarItem.overrideFlag = "Y".equals(ds.getValue(i, "overrideflag"));
        calendarItem.overrideCalendarid = ds.getValue(i, "overridecalendarid");
        calendarItem.overrideCalendaritemid = ds.getValue(i, "overridecalendaritemid");
        calendarItem.overrideDeleteFlag = "Y".equals(ds.getValue(i, "overridedeleteflag"));
        calendarItem.overrideStartInstantUTC = calendar.getCalendarConverter().convertDatabaseCalendarToInstantUtc(ds.getCalendar(i, "overridestartdt"));
        calendarItem.isRepeat = ds.getValue(i, "repeatflag").equals("Y");
        calendarItem.repeatCount = ds.getInt(i, "repeatcount", -1);
        calendarItem.repeatRule = ds.getValue(i, "repeatrule");
        calendarItem.isInstance = false;
        return calendarItem;
    }

    private Instant getJSONInstantUTC(JSONObject jso, String fieldid) {
        long epochMillis = jso.optLong(fieldid, -1L);
        if (epochMillis == -1L) {
            return null;
        }
        return Instant.ofEpochMilli(epochMillis);
    }

    public void setFromExternal(boolean external) {
        this.fromExternal = external;
    }

    public boolean isFromExternal() {
        return this.fromExternal;
    }

    public String getCalendarItemLabel() {
        return this.calendarItemLabel;
    }

    public void setCalendarItemLabel(String calendarItemLabel) {
        this.calendarItemLabel = calendarItemLabel;
    }

    public Instant getStartDate() {
        return this.startInstantUTC;
    }

    public void setStartDate(Instant startDateUTC) {
        this.startInstantUTC = startDateUTC;
    }

    public Instant getEndDate() {
        return this.endInstantUTC;
    }

    public void setEndDate(Instant endDateUTC) {
        this.endInstantUTC = endDateUTC;
    }

    public String getCalendarId() {
        return this.calendarid;
    }

    public void setCalendarId(String calendarid) {
        this.calendarid = calendarid;
    }

    public String getCalendarItemId() {
        return this.calendaritemid;
    }

    public void setCalendarItemId(String calendaritemid) {
        this.calendaritemid = calendaritemid;
    }

    public boolean isOverrideFlag() {
        return this.overrideFlag;
    }

    public boolean isOverrideDeleteFlag() {
        return this.overrideDeleteFlag;
    }

    public String getOverrideCalendarId() {
        return this.overrideCalendarid;
    }

    public String getOverrideCalendarItemId() {
        return this.overrideCalendaritemid;
    }

    public Instant getOverrideStartDate() {
        return this.overrideStartInstantUTC;
    }

    public boolean isRepeat() {
        return this.isRepeat;
    }

    public void setRepeat(boolean isRepeat) {
        this.isRepeat = isRepeat;
    }

    public String getRepeatRule() {
        return this.repeatRule;
    }

    public boolean isInstance() {
        return this.isInstance;
    }

    public void setIsInstance(boolean isInstance) {
        this.isInstance = isInstance;
    }

    public int compareTo(Object o) {
        return this.startInstantUTC.compareTo(((CalendarItem)o).startInstantUTC);
    }

    public boolean equals(Object obj) {
        return this.calendaritemid.equals(((CalendarItem)obj).calendaritemid);
    }

    public boolean isAllDayFlag() {
        return this.allDayFlag;
    }

    public String toString() {
        return (this.calendarItemLabel != null ? this.calendarItemLabel : "No Label") + (this.getStartDate() != null && this.getEndDate() != null ? " (" + this.formatter.format(this.getStartDate()) + "-" + this.formatter.format(this.getEndDate()) + " UTC)" : "");
    }

    public void setAllDayFlag(boolean allDayFlag) {
        this.allDayFlag = allDayFlag;
    }

    public int getRepeatCount() {
        return this.repeatCount;
    }
}

