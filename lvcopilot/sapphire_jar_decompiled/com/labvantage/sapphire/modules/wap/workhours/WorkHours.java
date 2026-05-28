/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.wap.workhours;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TimeZone;
import org.json.JSONObject;
import sapphire.SapphireException;

public class WorkHours
implements Serializable {
    private int starthour = -1;
    private int startminute = -1;
    private int endhour = -1;
    private int endminute = -1;
    private boolean sunday = false;
    private boolean monday = false;
    private boolean tuesday = false;
    private boolean wednesday = false;
    private boolean thursday = false;
    private boolean friday = false;
    private boolean saturday = false;
    private HashMap<String, LocalTime[]> dailyRangeCache = new HashMap();
    private ZoneId timezone = TimeZone.getDefault().toZoneId();
    private OverrideType overrideType = OverrideType.DEFAULT;

    public void setOverride(OverrideType overrideType) {
        this.overrideType = overrideType;
    }

    public OverrideType getOverride() {
        return this.overrideType;
    }

    public int getStartHour() {
        return this.starthour;
    }

    public void setStartHour(int starthour) {
        this.starthour = starthour;
    }

    public int getStartMinute() {
        return this.startminute;
    }

    public void setStartMinute(int startminute) {
        this.startminute = startminute;
    }

    public int getEndHour() {
        return this.endhour;
    }

    public void setEndHour(int endhour) {
        this.endhour = endhour;
    }

    public int getEndMinute() {
        return this.endminute;
    }

    public void setEndMinute(int endminute) {
        this.endminute = endminute;
    }

    public boolean isSunday() {
        return this.sunday;
    }

    public void setSunday(boolean sunday) {
        this.sunday = sunday;
    }

    public boolean isMonday() {
        return this.monday;
    }

    public void setMonday(boolean monday) {
        this.monday = monday;
    }

    public boolean isTuesday() {
        return this.tuesday;
    }

    public void setTuesday(boolean tuesday) {
        this.tuesday = tuesday;
    }

    public boolean isWednesday() {
        return this.wednesday;
    }

    public void setWednesday(boolean wednesday) {
        this.wednesday = wednesday;
    }

    public boolean isThursday() {
        return this.thursday;
    }

    public void setThursday(boolean thursday) {
        this.thursday = thursday;
    }

    public boolean isFriday() {
        return this.friday;
    }

    public void setFriday(boolean friday) {
        this.friday = friday;
    }

    public boolean isSaturday() {
        return this.saturday;
    }

    public void setSaturday(boolean saturday) {
        this.saturday = saturday;
    }

    public ArrayList<ZonedDateTime[]> getWorkingRanges(ZonedDateTime rangeFrom) {
        rangeFrom = rangeFrom.truncatedTo(ChronoUnit.DAYS);
        ZonedDateTime rangeTo = rangeFrom.plusDays(1L);
        ZonedDateTime userRangeFrom = rangeFrom.withZoneSameInstant(this.timezone);
        ArrayList<ZonedDateTime[]> tempRanges = new ArrayList<ZonedDateTime[]>();
        this.addUsersWorkhourRange(tempRanges, userRangeFrom.minusDays(2L));
        this.addUsersWorkhourRange(tempRanges, userRangeFrom.minusDays(1L));
        this.addUsersWorkhourRange(tempRanges, userRangeFrom);
        this.addUsersWorkhourRange(tempRanges, userRangeFrom.plusDays(1L));
        this.addUsersWorkhourRange(tempRanges, userRangeFrom.plusDays(2L));
        ArrayList<ZonedDateTime[]> outRanges = new ArrayList<ZonedDateTime[]>();
        ZoneId originalTimezone = rangeFrom.getZone();
        for (ZonedDateTime[] range : tempRanges) {
            ZonedDateTime from = range[0].withZoneSameInstant(originalTimezone);
            ZonedDateTime to = range[1].withZoneSameInstant(originalTimezone);
            if (from.isBefore(rangeFrom) && to.isBefore(rangeFrom) || from.isAfter(rangeTo) && to.isAfter(rangeTo)) continue;
            if (!from.isAfter(rangeFrom) && !to.isBefore(rangeTo)) {
                outRanges.add(new ZonedDateTime[]{rangeFrom, rangeTo});
                continue;
            }
            if (!from.isBefore(rangeFrom) && !to.isAfter(rangeTo)) {
                outRanges.add(new ZonedDateTime[]{from, to});
                continue;
            }
            if (from.isBefore(rangeFrom)) {
                outRanges.add(new ZonedDateTime[]{rangeFrom, to});
                continue;
            }
            if (!to.isAfter(rangeTo)) continue;
            outRanges.add(new ZonedDateTime[]{from, rangeTo});
        }
        return outRanges;
    }

    private void addUsersWorkhourRange(ArrayList<ZonedDateTime[]> ranges, ZonedDateTime today) {
        int START = 0;
        int END = 1;
        LocalTime[] todayStartEndTimes = this.dailyRangeCache.get(today.toString());
        if (todayStartEndTimes == null) {
            todayStartEndTimes = this.getWorkHoursUsersDay(today);
            this.dailyRangeCache.put(today.toString(), todayStartEndTimes);
        }
        if (todayStartEndTimes != null) {
            boolean isTwentyFourHour;
            boolean bl = isTwentyFourHour = todayStartEndTimes[START].getHour() == todayStartEndTimes[END].getHour() && todayStartEndTimes[START].getMinute() == todayStartEndTimes[END].getMinute() && todayStartEndTimes[START].getSecond() == todayStartEndTimes[END].getSecond();
            if (isTwentyFourHour) {
                ZonedDateTime start = today.with(todayStartEndTimes[START]);
                ZonedDateTime end = start.plusDays(1L);
                this.addWorkingRange(ranges, start, end);
            } else if (todayStartEndTimes[START].isBefore(todayStartEndTimes[END])) {
                ZonedDateTime start = today.with(todayStartEndTimes[START]);
                ZonedDateTime end = today.with(todayStartEndTimes[END]);
                this.addWorkingRange(ranges, start, end);
            } else {
                ZonedDateTime tomorrow = today.plusDays(1L);
                ZonedDateTime start = today.with(todayStartEndTimes[START]);
                ZonedDateTime end = tomorrow.with(todayStartEndTimes[END]);
                this.addWorkingRange(ranges, start, end);
            }
        }
    }

    private void addWorkingRange(ArrayList<ZonedDateTime[]> ranges, ZonedDateTime start, ZonedDateTime end) {
        if (ranges.size() > 0 && ranges.get(ranges.size() - 1)[1].compareTo(start) == 0) {
            ZonedDateTime[] lastRange = ranges.get(ranges.size() - 1);
            lastRange[1] = end;
        } else {
            ranges.add(new ZonedDateTime[]{start, end});
        }
    }

    public LocalTime[] getWorkHoursUsersDay(ZonedDateTime usersDay) {
        if (this.monday && usersDay.getDayOfWeek() == DayOfWeek.MONDAY || this.tuesday && usersDay.getDayOfWeek() == DayOfWeek.TUESDAY || this.wednesday && usersDay.getDayOfWeek() == DayOfWeek.WEDNESDAY || this.thursday && usersDay.getDayOfWeek() == DayOfWeek.THURSDAY || this.friday && usersDay.getDayOfWeek() == DayOfWeek.FRIDAY || this.saturday && usersDay.getDayOfWeek() == DayOfWeek.SATURDAY || this.sunday && usersDay.getDayOfWeek() == DayOfWeek.SUNDAY) {
            LocalTime start = LocalTime.of(this.starthour, this.startminute);
            LocalTime end = LocalTime.of(this.endhour, this.endminute);
            return new LocalTime[]{start, end};
        }
        return null;
    }

    public LocalTime getStartOfWorkingDay(ZonedDateTime date) {
        ZonedDateTime t = this.getWorkdayStartForDay(date.toInstant());
        return t != null ? t.toLocalTime() : null;
    }

    public LocalTime getEndOfWorkingDay(ZonedDateTime date) {
        ZonedDateTime t = this.getWorkdayEndForDay(date.toInstant());
        return t != null ? t.toLocalTime() : null;
    }

    public ZonedDateTime getWorkdayStartForDay(Instant currentTime) {
        ZonedDateTime usersCurrentTime = ZonedDateTime.ofInstant(currentTime, ZoneOffset.UTC);
        usersCurrentTime = usersCurrentTime.withZoneSameInstant(this.timezone);
        if (this.monday && usersCurrentTime.getDayOfWeek() == DayOfWeek.MONDAY || this.tuesday && usersCurrentTime.getDayOfWeek() == DayOfWeek.TUESDAY || this.wednesday && usersCurrentTime.getDayOfWeek() == DayOfWeek.WEDNESDAY || this.thursday && usersCurrentTime.getDayOfWeek() == DayOfWeek.THURSDAY || this.friday && usersCurrentTime.getDayOfWeek() == DayOfWeek.FRIDAY || this.saturday && usersCurrentTime.getDayOfWeek() == DayOfWeek.SATURDAY || this.sunday && usersCurrentTime.getDayOfWeek() == DayOfWeek.SUNDAY) {
            ZonedDateTime time = ZonedDateTime.of(usersCurrentTime.getYear(), usersCurrentTime.getMonthValue(), usersCurrentTime.getDayOfMonth(), this.starthour, this.startminute, 0, 0, this.timezone);
            return time;
        }
        return null;
    }

    public ZonedDateTime getWorkdayEndForDay(Instant currentTime) {
        ZonedDateTime usersCurrentTime = ZonedDateTime.ofInstant(currentTime, this.timezone);
        if (this.starthour > this.endhour) {
            usersCurrentTime = usersCurrentTime.plusDays(1L);
        }
        if (this.monday && usersCurrentTime.getDayOfWeek() == DayOfWeek.MONDAY || this.tuesday && usersCurrentTime.getDayOfWeek() == DayOfWeek.TUESDAY || this.wednesday && usersCurrentTime.getDayOfWeek() == DayOfWeek.WEDNESDAY || this.thursday && usersCurrentTime.getDayOfWeek() == DayOfWeek.THURSDAY || this.friday && usersCurrentTime.getDayOfWeek() == DayOfWeek.FRIDAY || this.saturday && usersCurrentTime.getDayOfWeek() == DayOfWeek.SATURDAY || this.sunday && usersCurrentTime.getDayOfWeek() == DayOfWeek.SUNDAY) {
            ZonedDateTime time = ZonedDateTime.of(usersCurrentTime.getYear(), usersCurrentTime.getDayOfMonth(), usersCurrentTime.getDayOfMonth(), this.endhour, this.endminute, 0, 0, this.timezone);
            return time;
        }
        return null;
    }

    public ZonedDateTime getNextWorkdayStart(Instant currentTime) {
        for (int i = 1; i < 7; ++i) {
            OffsetDateTime tomorrow = currentTime.atOffset(ZoneOffset.UTC).plusDays(i);
            ZonedDateTime startTomorrow = this.getWorkdayStartForDay(tomorrow.toInstant());
            if (startTomorrow == null) continue;
            return startTomorrow;
        }
        return null;
    }

    public static WorkHours getInstance() throws SapphireException {
        WorkHours workHours = new WorkHours(-1, -1, -1, -1, false, false, false, false, false, false, false);
        return workHours;
    }

    public static WorkHours getInstance(String definition, ZoneId timezone) throws SapphireException {
        WorkHours workHours = new WorkHours(-1, -1, -1, -1, false, false, false, false, false, false, false);
        workHours.timezone = timezone;
        workHours.fromJSONString(definition);
        return workHours;
    }

    public static WorkHours getInstance(String definition) throws SapphireException {
        return WorkHours.getInstance(definition, TimeZone.getDefault().toZoneId());
    }

    public WorkHours() {
        this.starthour = 0;
        this.startminute = 0;
        this.endhour = 0;
        this.endminute = 0;
        this.sunday = true;
        this.monday = true;
        this.tuesday = true;
        this.wednesday = true;
        this.thursday = true;
        this.friday = true;
        this.saturday = true;
        this.overrideType = OverrideType.DEFAULT;
    }

    public WorkHours(int starthour, int startminute, int endhour, int endminute, boolean sunday, boolean monday, boolean tuesday, boolean wednesday, boolean thursday, boolean friday, boolean saturday) {
        this.starthour = starthour;
        this.startminute = startminute;
        this.endhour = endhour;
        this.endminute = endminute;
        this.sunday = sunday;
        this.monday = monday;
        this.tuesday = tuesday;
        this.wednesday = wednesday;
        this.thursday = thursday;
        this.friday = friday;
        this.saturday = saturday;
        this.overrideType = OverrideType.DEFAULT;
    }

    public JSONObject toJSONObject() throws SapphireException {
        return this.toJSONObject(null, null);
    }

    public JSONObject toJSONObject(ZonedDateTime workhoursDate, ZoneId displayTimeZone) throws SapphireException {
        JSONObject job = new JSONObject();
        if (workhoursDate == null) {
            workhoursDate = ZonedDateTime.of(2022, 2, 14, 0, 0, 0, 0, this.timezone);
        }
        if (!(workhoursDate = workhoursDate.withZoneSameInstant(this.timezone)).getDayOfWeek().equals(DayOfWeek.MONDAY)) {
            workhoursDate = workhoursDate.with(TemporalAdjusters.previous(DayOfWeek.MONDAY));
        }
        if (this.overrideType == OverrideType.OVERRIDE) {
            ZonedDateTime sunStart;
            ZonedDateTime orgMondayStart = workhoursDate.withHour(this.starthour > -1 ? this.starthour : 0).withMinute(this.startminute > -1 ? this.startminute : 0).withSecond(0).withNano(0);
            ZonedDateTime orgMondayEnd = workhoursDate.withHour(this.endhour > -1 ? this.endhour : 0).withMinute(this.endminute > -1 ? this.endminute : 0).withSecond(0).withNano(0);
            ZonedDateTime mondayStart = this.monday ? orgMondayStart : null;
            ZonedDateTime tuesdayStart = this.tuesday ? orgMondayStart.plusDays(1L) : null;
            ZonedDateTime wedStart = this.wednesday ? orgMondayStart.plusDays(2L) : null;
            ZonedDateTime thursStart = this.thursday ? orgMondayStart.plusDays(3L) : null;
            ZonedDateTime friStart = this.friday ? orgMondayStart.plusDays(4L) : null;
            ZonedDateTime satStart = this.saturday ? orgMondayStart.plusDays(5L) : null;
            ZonedDateTime zonedDateTime = sunStart = this.sunday ? orgMondayStart.plusDays(6L) : null;
            if (displayTimeZone != null) {
                orgMondayStart = orgMondayStart.withZoneSameInstant(ZoneOffset.UTC);
                orgMondayEnd = orgMondayEnd.withZoneSameInstant(ZoneOffset.UTC);
                mondayStart = mondayStart == null ? null : mondayStart.withZoneSameInstant(ZoneOffset.UTC);
                tuesdayStart = tuesdayStart == null ? null : tuesdayStart.withZoneSameInstant(ZoneOffset.UTC);
                wedStart = wedStart == null ? null : wedStart.withZoneSameInstant(ZoneOffset.UTC);
                thursStart = thursStart == null ? null : thursStart.withZoneSameInstant(ZoneOffset.UTC);
                friStart = friStart == null ? null : friStart.withZoneSameInstant(ZoneOffset.UTC);
                satStart = satStart == null ? null : satStart.withZoneSameInstant(ZoneOffset.UTC);
                sunStart = sunStart == null ? null : sunStart.withZoneSameInstant(ZoneOffset.UTC);
                orgMondayStart = orgMondayStart.withZoneSameInstant(displayTimeZone);
                orgMondayEnd = orgMondayEnd.withZoneSameInstant(displayTimeZone);
                mondayStart = mondayStart == null ? null : mondayStart.withZoneSameInstant(displayTimeZone);
                tuesdayStart = tuesdayStart == null ? null : tuesdayStart.withZoneSameInstant(displayTimeZone);
                wedStart = wedStart == null ? null : wedStart.withZoneSameInstant(displayTimeZone);
                thursStart = thursStart == null ? null : thursStart.withZoneSameInstant(displayTimeZone);
                friStart = friStart == null ? null : friStart.withZoneSameInstant(displayTimeZone);
                satStart = satStart == null ? null : satStart.withZoneSameInstant(displayTimeZone);
                sunStart = sunStart == null ? null : sunStart.withZoneSameInstant(displayTimeZone);
            }
            try {
                job.put("override", this.overrideType.toString());
                job.put("starthour", orgMondayStart.getHour());
                job.put("startminute", orgMondayStart.getMinute());
                job.put("endhour", orgMondayEnd.getHour());
                job.put("endminute", orgMondayEnd.getMinute());
                ArrayList<DayOfWeek> days = new ArrayList<DayOfWeek>();
                for (int i = 0; i < DayOfWeek.values().length; ++i) {
                    DayOfWeek day = DayOfWeek.values()[i];
                    if (mondayStart != null && mondayStart.getDayOfWeek() == day && !days.contains(day)) {
                        days.add(day);
                    }
                    if (tuesdayStart != null && tuesdayStart.getDayOfWeek() == day && !days.contains(day)) {
                        days.add(day);
                    }
                    if (wedStart != null && wedStart.getDayOfWeek() == day && !days.contains(day)) {
                        days.add(day);
                    }
                    if (thursStart != null && thursStart.getDayOfWeek() == day && !days.contains(day)) {
                        days.add(day);
                    }
                    if (friStart != null && friStart.getDayOfWeek() == day && !days.contains(day)) {
                        days.add(day);
                    }
                    if (satStart != null && satStart.getDayOfWeek() == day && !days.contains(day)) {
                        days.add(day);
                    }
                    if (sunStart == null || sunStart.getDayOfWeek() != day || days.contains(day)) continue;
                    days.add(day);
                }
                job.put("sunday", days.contains(DayOfWeek.SUNDAY));
                job.put("monday", days.contains(DayOfWeek.MONDAY));
                job.put("tuesday", days.contains(DayOfWeek.TUESDAY));
                job.put("wednesday", days.contains(DayOfWeek.WEDNESDAY));
                job.put("thursday", days.contains(DayOfWeek.THURSDAY));
                job.put("friday", days.contains(DayOfWeek.FRIDAY));
                job.put("saturday", days.contains(DayOfWeek.SATURDAY));
            }
            catch (Exception e) {
                throw new SapphireException("Failed to create json.", e);
            }
        }
        try {
            job.put("override", this.overrideType.toString());
        }
        catch (Exception e) {
            throw new SapphireException("Failed to create json.", e);
        }
        return job;
    }

    public String toJSONString() throws SapphireException {
        return this.toJSONObject().toString();
    }

    public void fromJSONString(String definition) throws SapphireException {
        block4: {
            try {
                if (definition == null || definition.length() <= 0) break block4;
                JSONObject job = new JSONObject(definition);
                try {
                    this.overrideType = job.has("override") ? OverrideType.valueOf(job.getString("override").toUpperCase()) : OverrideType.DEFAULT;
                }
                catch (Exception e) {
                    this.overrideType = OverrideType.DEFAULT;
                }
                this.starthour = job.has("starthour") && job.get("starthour") instanceof Integer ? job.getInt("starthour") : -1;
                this.startminute = job.has("startminute") && job.get("startminute") instanceof Integer ? job.getInt("startminute") : -1;
                this.endhour = job.has("endhour") && job.get("endhour") instanceof Integer ? job.getInt("endhour") : -1;
                this.endminute = job.has("endminute") && job.get("endminute") instanceof Integer ? job.getInt("endminute") : -1;
                this.sunday = job.has("sunday") && job.get("sunday") instanceof Boolean ? job.getBoolean("sunday") : false;
                this.monday = job.has("monday") && job.get("monday") instanceof Boolean ? job.getBoolean("monday") : false;
                this.tuesday = job.has("tuesday") && job.get("tuesday") instanceof Boolean ? job.getBoolean("tuesday") : false;
                this.wednesday = job.has("wednesday") && job.get("wednesday") instanceof Boolean ? job.getBoolean("wednesday") : false;
                this.thursday = job.has("thursday") && job.get("thursday") instanceof Boolean ? job.getBoolean("thursday") : false;
                this.friday = job.has("friday") && job.get("friday") instanceof Boolean ? job.getBoolean("friday") : false;
                this.saturday = job.has("saturday") && job.get("saturday") instanceof Boolean ? job.getBoolean("saturday") : false;
            }
            catch (Exception e) {
                throw new SapphireException("Failed to parse definition.", e);
            }
        }
    }

    public String toString() {
        return "WorkHours [starthour=" + this.starthour + ", startminute=" + this.startminute + ", endhour=" + this.endhour + ", endminute=" + this.endminute + ", sunday=" + this.sunday + ", monday=" + this.monday + ", tuesday=" + this.tuesday + ", wednesday=" + this.wednesday + ", thursday=" + this.thursday + ", friday=" + this.friday + ", saturday=" + this.saturday + ", override=" + this.overrideType.toString() + "]";
    }

    public boolean isWorkingDayOfWeek(int dayOfWeek) {
        switch (dayOfWeek) {
            case 7: {
                return this.isSaturday();
            }
            case 1: {
                return this.isSunday();
            }
            case 2: {
                return this.isMonday();
            }
            case 3: {
                return this.isTuesday();
            }
            case 4: {
                return this.isWednesday();
            }
            case 5: {
                return this.isThursday();
            }
            case 6: {
                return this.isFriday();
            }
        }
        return false;
    }

    public boolean isWorkingDayOfWeek(DayOfWeek dayOfWeek) {
        switch (dayOfWeek) {
            case SATURDAY: {
                return this.isSaturday();
            }
            case SUNDAY: {
                return this.isSunday();
            }
            case MONDAY: {
                return this.isMonday();
            }
            case TUESDAY: {
                return this.isTuesday();
            }
            case WEDNESDAY: {
                return this.isWednesday();
            }
            case THURSDAY: {
                return this.isThursday();
            }
            case FRIDAY: {
                return this.isFriday();
            }
        }
        return false;
    }

    public void setTimezone(ZoneId timezone) {
        this.timezone = timezone;
    }

    public static enum OverrideType {
        DEFAULT,
        OVERRIDE,
        INHERIT;

    }
}

