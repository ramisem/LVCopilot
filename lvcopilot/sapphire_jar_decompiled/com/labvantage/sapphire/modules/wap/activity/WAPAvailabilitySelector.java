/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.wap.activity;

import com.labvantage.sapphire.BaseCustom;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.modules.wap.CalendarConverter;
import com.labvantage.sapphire.modules.wap.activity.Activity;
import com.labvantage.sapphire.modules.wap.activity.WAPAvailability;
import com.labvantage.sapphire.modules.wap.activity.WAPAvailabilityOptions;
import com.labvantage.sapphire.modules.wap.activity.WAPCommands;
import com.labvantage.sapphire.modules.wap.activity.WAPConstants;
import com.labvantage.sapphire.modules.wap.activity.WAPSelector;
import com.labvantage.sapphire.modules.wap.activity.WAPSelectorOptions;
import com.labvantage.sapphire.modules.wap.calendar.CalendarFactory;
import com.labvantage.sapphire.modules.wap.calendar.CalendarItem;
import com.labvantage.sapphire.modules.wap.calendar.LVCalendar;
import com.labvantage.sapphire.modules.wap.workhours.WorkHours;
import java.io.File;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import sapphire.SapphireException;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.StringUtil;

public class WAPAvailabilitySelector
extends BaseCustom
implements WAPConstants {
    private DateTimeUtil dtu;
    private WAPSelector selector;
    private WAPCommands wapCommands;
    private TranslationProcessor translationProcessor = null;
    public final CalendarConverter calendarConverter;

    public WAPAvailabilitySelector(String connectionid) {
        this.setConnectionId(connectionid);
        ConnectionProcessor cp = this.getConnectionProcessor();
        this.dtu = new DateTimeUtil(cp.getConnectionInfo(connectionid));
        this.selector = new WAPSelector(connectionid);
        this.wapCommands = new WAPCommands(connectionid);
        this.calendarConverter = new CalendarConverter(this.dtu);
    }

    public WAPAvailabilitySelector(String connectionid, File rakFile) {
        this.setConnectionId(connectionid);
        this.setRakFile(rakFile);
        ConnectionProcessor cp = this.getConnectionProcessor();
        this.dtu = new DateTimeUtil(cp.getConnectionInfo(connectionid));
        this.selector = new WAPSelector(connectionid, rakFile);
        this.wapCommands = new WAPCommands(connectionid, rakFile);
        this.calendarConverter = new CalendarConverter(this.dtu);
    }

    @Override
    public TranslationProcessor getTranslationProcessor() {
        return this.translationProcessor;
    }

    public void setTranslationProcessor(TranslationProcessor translationProcessor) {
        this.translationProcessor = translationProcessor;
    }

    public List<WAPAvailability> getUserWorkareaAvailabilityBetween(String workareaid, String rangeFromString, String rangeToString, WAPAvailabilityOptions options, ZoneId dailySegmentTimezone) throws SapphireException {
        Instant rangeFrom = this.calendarConverter.getIsoInstant(rangeFromString);
        Instant rangeTo = this.calendarConverter.getIsoInstant(rangeToString);
        return this.getUserWorkareaAvailabilityBetween(workareaid, rangeFrom, rangeTo, options, dailySegmentTimezone);
    }

    public List<WAPAvailability> getUserWorkareaAvailabilityBetween(String workareaid, Instant rangeFrom, Instant rangeTo, WAPAvailabilityOptions options, ZoneId dailySegmentTimezone) throws SapphireException {
        WAPAvailability userAvail;
        int i;
        List<WAPAvailability> userAvailabilities;
        String[] permanent;
        List<WAPAvailability> baseWorkareaAvailability = this.getBaseWorkareaAvailability(workareaid, rangeFrom, rangeTo, options, dailySegmentTimezone);
        for (String userid : permanent = StringUtil.split(this.wapCommands.getPermanentWorkareaUsers(workareaid), ";")) {
            options.setType("Analyst");
            options.setResourcekeyid1(userid);
            userAvailabilities = this.getAvailabilityBetween(rangeFrom, rangeTo, options, dailySegmentTimezone);
            for (i = 0; i < userAvailabilities.size(); ++i) {
                userAvail = userAvailabilities.get(i);
                WAPAvailability workareaAvail = baseWorkareaAvailability.get(i);
                if (this.getTranslationProcessor() != null) {
                    workareaAvail.setTranslationProcessor(this.getTranslationProcessor());
                }
                workareaAvail.addExtraWorkingMinutes(userAvail.getWorkingMinutes());
                workareaAvail.addAppointmenMinutes(userAvail.getAppointmentMinutes());
                if (userAvail.getAppointmentMinutes() > 0) {
                    workareaAvail.appendAppointmentLog(userid, "<table>" + StringUtil.replaceAll(userAvail.getAppointmentLog().toString(), "<td>Appointment</td>", "") + "</table>", userAvail.getAppointmentMinutes());
                }
                workareaAvail.addActivityMinutes(userAvail.getActivityMinutes());
                if (userAvail.getActivityMinutes() > 0) {
                    workareaAvail.appendActivityLog(userid, "<table>" + userAvail.getActivityLog() + "</table>", userAvail.getActivityMinutes());
                }
                workareaAvail.addWidestRange(userAvail.getWidestWorkingRange());
            }
        }
        Map<String, Set<String>> dates = this.selector.getScheduledWorkAreaUsers(workareaid, rangeFrom, rangeTo);
        HashSet<String> scheduledUsers = new HashSet<String>();
        for (Set<String> value : dates.values()) {
            scheduledUsers.addAll(value);
        }
        for (String userid : scheduledUsers) {
            options.setType("Analyst");
            options.setResourcekeyid1(userid);
            userAvailabilities = this.getAvailabilityBetween(rangeFrom, rangeTo, options, dailySegmentTimezone);
            for (i = 0; i < userAvailabilities.size(); ++i) {
                userAvail = userAvailabilities.get(i);
                Set<String> users = dates.get(userAvail.getStartdt().toInstant().toString());
                if (!users.contains(userid)) continue;
                WAPAvailability workareaAvail = baseWorkareaAvailability.get(i);
                workareaAvail.addExtraWorkingMinutes(userAvail.getWorkingMinutes());
                workareaAvail.addAppointmenMinutes(userAvail.getAppointmentMinutes());
                workareaAvail.addActivityMinutes(userAvail.getActivityMinutes());
                if (userAvail.getAppointmentMinutes() > 0) {
                    workareaAvail.appendActivityLog("(" + userid + ")", userAvail.getAppointmentMinutes());
                }
                if (userAvail.getActivityMinutes() > 0) {
                    workareaAvail.appendActivityLog("(" + userid + ")", userAvail.getActivityMinutes());
                }
                workareaAvail.addWidestRange(userAvail.getWidestWorkingRange());
            }
        }
        return baseWorkareaAvailability;
    }

    public List<WAPAvailability> getBaseWorkareaAvailability(String workareaid, Instant rangeFrom, Instant rangeTo, WAPAvailabilityOptions options, ZoneId dailySegmentTimezone) throws SapphireException {
        options.setType("AnalystWorkArea");
        options.setResourcekeyid1(workareaid);
        List<WAPAvailability> workareaAvailabilities = this.getAvailabilityBetween(rangeFrom, rangeTo, options, dailySegmentTimezone);
        for (int i = 0; i < workareaAvailabilities.size(); ++i) {
            WAPAvailability tempAvail = workareaAvailabilities.get(i);
            tempAvail.setWorkingRanges(new ArrayList<ZonedDateTime[]>());
        }
        return workareaAvailabilities;
    }

    public List<WAPAvailability> getInstrumentWorkareaAvailabilityBetween(String workareaid, String rangeFromString, String rangeToString, WAPAvailabilityOptions options, String instrumenttypeid, String instrumentmodelid, ZoneId dailySegmentTimezone) throws SapphireException {
        Instant rangeFrom = this.calendarConverter.getIsoInstant(rangeFromString);
        Instant rangeTo = this.calendarConverter.getIsoInstant(rangeToString);
        return this.getInstrumentWorkareaAvailabilityBetween(workareaid, rangeFrom, rangeTo, options, instrumenttypeid, instrumentmodelid, dailySegmentTimezone);
    }

    public List<WAPAvailability> getInstrumentWorkareaAvailabilityBetween(String workareaid, Instant rangeFrom, Instant rangeTo, WAPAvailabilityOptions options, String instrumenttypeid, String instrumentmodelid, ZoneId dailySegmentTimezone) throws SapphireException {
        String[] instruments;
        List<WAPAvailability> availability = this.getBaseWorkareaAvailability(workareaid, rangeFrom, rangeTo, options, dailySegmentTimezone);
        for (String instrumentid : instruments = StringUtil.split(this.wapCommands.getDepartmentInstruments("", workareaid, instrumenttypeid, instrumentmodelid), ";")) {
            if (instrumentid.length() <= 0) continue;
            options.setType("Instrument");
            options.setResourcekeyid1(instrumentid);
            List<WAPAvailability> instAvailabilities = this.getAvailabilityBetween(rangeFrom, rangeTo, options, dailySegmentTimezone);
            for (int i = 0; i < instAvailabilities.size(); ++i) {
                WAPAvailability instAvail = instAvailabilities.get(i);
                WAPAvailability workareaAvail = availability.get(i);
                workareaAvail.addExtraWorkingMinutes(instAvail.getWorkingMinutes());
                workareaAvail.addAppointmenMinutes(instAvail.getAppointmentMinutes());
                workareaAvail.addActivityMinutes(instAvail.getActivityMinutes());
                workareaAvail.addWidestRange(instAvail.getWidestWorkingRange());
            }
        }
        return availability;
    }

    public List<WAPAvailability> getUserAvailabilityBetween(String analystid, String rangeFromString, String rangeToString, WAPAvailabilityOptions options, ZoneId dailySegmentTimezone) throws SapphireException {
        Instant rangeFrom = this.calendarConverter.getIsoInstant(rangeFromString);
        Instant rangeTo = this.calendarConverter.getIsoInstant(rangeToString);
        options.setType("Analyst");
        options.setResourcekeyid1(analystid);
        return this.getAvailabilityBetween(rangeFrom, rangeTo, options, dailySegmentTimezone);
    }

    public List<WAPAvailability> getUserByTypeAvailabilityBetween(String analystid, String analysttype, Instant rangeFrom, Instant rangeTo, WAPAvailabilityOptions options, ZoneId dailySegmentTimezone) throws SapphireException {
        options.setType("Analyst");
        options.setResourcekeyid1(analystid);
        options.setAnalystType(analysttype);
        return this.getAvailabilityBetween(rangeFrom, rangeTo, options, dailySegmentTimezone);
    }

    public List<WAPAvailability> getUserByTypeAvailabilityBetween(String analystid, Instant rangeFrom, Instant rangeTo, WAPAvailabilityOptions options, ZoneId dailySegmentTimezone) throws SapphireException {
        options.setType("Analyst");
        options.setResourcekeyid1(analystid);
        return this.getAvailabilityBetween(rangeFrom, rangeTo, options, dailySegmentTimezone);
    }

    public List<WAPAvailability> getUserAvailabilityBetween(String analystid, Instant rangeFrom, Instant rangeTo, WAPAvailabilityOptions options, ZoneId dailySegmentTimezone) throws SapphireException {
        options.setType("Analyst");
        options.setResourcekeyid1(analystid);
        return this.getAvailabilityBetween(rangeFrom, rangeTo, options, dailySegmentTimezone);
    }

    public List<WAPAvailability> getInstrumentAvailabilityBetween(String instrumentid, String rangeFromString, String rangeToString, WAPAvailabilityOptions options, ZoneId dailySegmentTimezone) throws SapphireException {
        Instant rangeFrom = this.calendarConverter.getIsoInstant(rangeFromString);
        Instant rangeTo = this.calendarConverter.getIsoInstant(rangeToString);
        return this.getInstrumentAvailabilityBetween(instrumentid, rangeFrom, rangeTo, options, dailySegmentTimezone);
    }

    public List<WAPAvailability> getInstrumentAvailabilityBetween(String instrumentid, Instant rangeFrom, Instant rangeTo, WAPAvailabilityOptions options, ZoneId dailySegmentTimezone) throws SapphireException {
        options.setType("Instrument");
        options.setResourcekeyid1(instrumentid);
        return this.getAvailabilityBetween(rangeFrom, rangeTo, options, dailySegmentTimezone);
    }

    private List<WAPAvailability> getAvailabilityBetween(Instant rangeFromInstantUTC, Instant rangeToInstantUTC, WAPAvailabilityOptions options, ZoneId dailySegmentTimezone) throws SapphireException {
        ArrayList<WAPAvailability> availabilityList = new ArrayList<WAPAvailability>();
        HashMap<String, WAPAvailability> availabilityMap = new HashMap<String, WAPAvailability>();
        CalendarFactory factory = this.getRakFile() == null ? new CalendarFactory(this.getConnectionid()) : new CalendarFactory(this.getConnectionid(), this.getRakFile());
        LVCalendar calendar = null;
        calendar = options.getType().equals("Analyst") ? factory.getUserCalendar(options.getResourcekeyid1(), true, true) : (options.getType().equals("Instrument") ? factory.getInstrumentCalendar(options.getResourcekeyid1(), true, true) : factory.getDepartmentCalendar(options.getResourcekeyid1(), true, true));
        ZonedDateTime fullStart = ZonedDateTime.ofInstant(rangeFromInstantUTC, dailySegmentTimezone);
        ZonedDateTime fullEnd = ZonedDateTime.ofInstant(rangeToInstantUTC, dailySegmentTimezone);
        fullStart = fullStart.truncatedTo(ChronoUnit.DAYS);
        ZonedDateTime appointmentsFullEnd = fullEnd = fullEnd.truncatedTo(ChronoUnit.DAYS);
        if (options.isLog()) {
            appointmentsFullEnd = appointmentsFullEnd.plus(30L, ChronoUnit.DAYS);
        }
        ZonedDateTime tempStartDayLoop = fullStart;
        while (tempStartDayLoop.isBefore(appointmentsFullEnd)) {
            WAPAvailability availability = this.getWapAvailability(availabilityMap, calendar, tempStartDayLoop);
            if (tempStartDayLoop.isBefore(fullEnd)) {
                availabilityList.add(availability);
            }
            tempStartDayLoop = tempStartDayLoop.plus(1L, ChronoUnit.DAYS);
        }
        List<CalendarItem> appointments = calendar.getCalendarItemsBetween(fullStart.toInstant(), appointmentsFullEnd.toInstant(), true);
        for (CalendarItem appointment : appointments) {
            ZonedDateTime appointmentStartDt = ZonedDateTime.ofInstant(appointment.getStartDate(), dailySegmentTimezone);
            ZonedDateTime appointmentEndDt = ZonedDateTime.ofInstant(appointment.getEndDate(), dailySegmentTimezone);
            tempStartDayLoop = appointmentStartDt.truncatedTo(ChronoUnit.DAYS);
            while (tempStartDayLoop.isBefore(appointmentEndDt)) {
                WAPAvailability availability = this.getWapAvailability(availabilityMap, calendar, tempStartDayLoop);
                if (appointment.isAllDayFlag()) {
                    availability.setForceOOO(true);
                } else {
                    ArrayList<ZonedDateTime[]> workingRanges = availability.getWorkingRanges();
                    if (workingRanges.size() > 0) {
                        int overlapMinutes = WAPAvailability.getWorkingOverlapMinutes(workingRanges, appointmentStartDt, appointmentEndDt);
                        if (overlapMinutes > 0) {
                            availability.addAppointment(appointment);
                            availability.addAppointmenMinutes(overlapMinutes);
                            availability.appendLog(appointment, overlapMinutes);
                        }
                    }
                }
                tempStartDayLoop = tempStartDayLoop.plus(1L, ChronoUnit.DAYS);
            }
        }
        WAPSelectorOptions selectorOptions = new WAPSelectorOptions();
        if (options.getType().equals("Analyst")) {
            selectorOptions.setAssignedUser(options.getResourcekeyid1());
            selectorOptions.setAnalysttype(options.getAnalystType());
        } else if (options.getType().equals("Instrument")) {
            selectorOptions.setAssignedInstrument(options.getResourcekeyid1());
        } else if (options.getType().equals("AnalystWorkArea")) {
            selectorOptions.setAssignedUserWorkarea(options.getResourcekeyid1());
        } else if (options.getType().equals("InstrumentWorkArea")) {
            selectorOptions.setAssignedInstrumentWorkarea(options.getResourcekeyid1(), options.getInstrumenttypeid(), options.getInstrumentmodelid());
        }
        selectorOptions.setIncludeFixed(true);
        selectorOptions.setIncludeFixedOverlaps(true);
        selectorOptions.setIncludeFloating(true);
        selectorOptions.setIncludeFloatingOverlaps(true);
        selectorOptions.setIncludeTimemodeNone(false);
        selectorOptions.setActivityStatusList(options.getActivityStatusList());
        selectorOptions.setTreatFixedFloatingAsFixed(options.isTreatFixedFloatingAsFixed());
        List<Activity> activities = this.selector.getActivitiesBetween(rangeFromInstantUTC, rangeToInstantUTC, selectorOptions);
        for (Activity activity : activities) {
            int mins;
            String dayStr2;
            Instant activityStartRangeDt = activity.getTimeMode().equals("Fixed") || activity.getTimeMode().equals("Floating") && options.isTreatFixedFloatingAsFixed() && activity.getStartDateInstantUTC() != null ? activity.getStartDateInstantUTC() : activity.getStartRangeInstantUTC();
            Instant activityEndRangeDt = activity.getTimeMode().equals("Fixed") || activity.getTimeMode().equals("Floating") && options.isTreatFixedFloatingAsFixed() && activity.getEndDateInstantUTC() != null ? activity.getEndDateInstantUTC() : activity.getEndRangeInstantUTC();
            ZonedDateTime activityStartDt = ZonedDateTime.ofInstant(activityStartRangeDt, dailySegmentTimezone);
            ZonedDateTime activityEndDt = ZonedDateTime.ofInstant(activityEndRangeDt, dailySegmentTimezone);
            int daysWhereAvailableToWorkOnActivity = 0;
            ArrayList<WAPAvailability> availabletoWorkDays = new ArrayList<WAPAvailability>();
            int daysActivitySpans = 0;
            ArrayList<WAPAvailability> activitySpanDays = new ArrayList<WAPAvailability>();
            tempStartDayLoop = activityStartDt.truncatedTo(ChronoUnit.DAYS);
            while (tempStartDayLoop.isBefore(activityEndDt)) {
                int overlapMinutes;
                ArrayList<ZonedDateTime[]> workingRanges;
                WAPAvailability availability = this.getWapAvailability(availabilityMap, calendar, tempStartDayLoop);
                ++daysActivitySpans;
                activitySpanDays.add(availability);
                if (!availability.isOoo() && (workingRanges = availability.getWorkingRanges()).size() > 0 && (overlapMinutes = WAPAvailability.getWorkingOverlapMinutes(workingRanges, activityStartDt, activityEndDt)) > 0) {
                    ++daysWhereAvailableToWorkOnActivity;
                    availabletoWorkDays.add(availability);
                }
                tempStartDayLoop = tempStartDayLoop.plus(1L, ChronoUnit.DAYS);
            }
            String dayStr1 = this.getTranslationProcessor() == null ? "not working days" : this.getTranslationProcessor().translate("not working days");
            String string = dayStr2 = this.getTranslationProcessor() == null ? "days" : this.getTranslationProcessor().translate("days");
            if (daysWhereAvailableToWorkOnActivity == 0) {
                if (daysActivitySpans == 0) {
                    daysActivitySpans = 1;
                }
                mins = activity.getExtra_resourceduration() / daysActivitySpans;
                for (WAPAvailability availability : activitySpanDays) {
                    availability.addActivityMinutes(mins);
                    availability.addActivity(activity);
                    availability.appendActivityLog(activity.getLabel() + " (" + activity.getExtra_resourceduration() + "/" + daysActivitySpans + " " + dayStr1 + ")", mins);
                }
                continue;
            }
            mins = activity.getExtra_resourceduration() / daysWhereAvailableToWorkOnActivity;
            for (WAPAvailability availability : availabletoWorkDays) {
                availability.addActivityMinutes(mins);
                availability.addActivity(activity);
                String activityLabel = this.getTranslationProcessor() == null ? activity.getLabel() : this.getTranslationProcessor().translate(activity.getLabel());
                availability.appendActivityLog(activityLabel + " (" + activity.getExtra_resourceduration() + "/" + daysWhereAvailableToWorkOnActivity + " " + dayStr2 + ")", mins);
            }
        }
        return availabilityList;
    }

    private WAPAvailability getWapAvailability(Map<String, WAPAvailability> availabilityMap, LVCalendar calendar, ZonedDateTime tempStartDay) {
        WAPAvailability availability = availabilityMap.get(tempStartDay.toInstant().toString());
        if (availability == null) {
            availability = new WAPAvailability();
            availabilityMap.put(tempStartDay.toInstant().toString(), availability);
            availability.setStartdt(tempStartDay);
            this.setWorkingDetails(availability, calendar);
        }
        return availability;
    }

    public void setWorkingDetails(WAPAvailability availability, LVCalendar calendar) {
        WorkHours coreHours;
        WorkHours workHours = coreHours = calendar == null ? null : calendar.getCoreHours();
        if (coreHours == null) {
            return;
        }
        ArrayList<ZonedDateTime[]> workingRanges = coreHours.getWorkingRanges(availability.getStartdt());
        availability.setWorkingRanges(workingRanges);
    }
}

