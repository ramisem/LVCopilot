/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.wap.activity;

import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.modules.wap.activity.Activity;
import com.labvantage.sapphire.modules.wap.calendar.CalendarItem;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import sapphire.accessor.TranslationProcessor;

public class WAPAvailability {
    public static final int DAILY = 0;
    private ZonedDateTime startdt;
    private int appointmentMinutes = 0;
    private ArrayList<CalendarItem> appointments = new ArrayList();
    private int activityMinutes = 0;
    private ArrayList<Activity> activities = new ArrayList();
    StringBuilder appointmentLog = new StringBuilder();
    StringBuilder activityLog = new StringBuilder();
    private TranslationProcessor translationProcessor;
    private ZonedDateTime[] widestRange = null;
    private boolean forceOOO = false;
    private ArrayList<ZonedDateTime[]> workingRanges = new ArrayList();
    private int extraWorkingMinutes = 0;

    public boolean isOoo() {
        return this.forceOOO || this.workingRanges.size() == 0 || this.appointmentMinutes >= this.getWorkingMinutes();
    }

    public int getWorkingMinutes() {
        return WAPAvailability.getWorkingMinutes(this.workingRanges) + this.extraWorkingMinutes;
    }

    public ZonedDateTime getStartdt() {
        return this.startdt;
    }

    public void setForceOOO(boolean forceOOO) {
        this.forceOOO = forceOOO;
    }

    public void setStartdt(ZonedDateTime startdt) {
        this.startdt = startdt;
    }

    public int getAppointmentMinutes() {
        return this.appointmentMinutes;
    }

    public void addAppointmenMinutes(int minutesAppointments) {
        this.appointmentMinutes += minutesAppointments;
    }

    public String toString() {
        return this.startdt.toString();
    }

    public List<CalendarItem> getAppointments() {
        return this.appointments;
    }

    public void addAppointment(CalendarItem appointment) {
        this.appointments.add(appointment);
    }

    public List<Activity> getActivites() {
        return this.activities;
    }

    public void addActivity(Activity activity) {
        this.activities.add(activity);
    }

    public int getActivityMinutes() {
        return this.activityMinutes;
    }

    public void addActivityMinutes(int activityMinutes) {
        this.activityMinutes += activityMinutes;
    }

    public int getAvailableMinutes() {
        return this.getWorkingMinutes() - this.appointmentMinutes - this.activityMinutes;
    }

    public void appendLog(CalendarItem appointment, int value) {
        TranslationProcessor tp = this.getTranslationProcessor();
        String appointmentStr = tp == null ? "Appointment" : tp.translate("Appointment");
        String appointmentCalendarLabel = appointment.getCalendarItemLabel() + (appointment.isAllDayFlag() ? " (All Day)" : "");
        appointmentCalendarLabel = tp == null ? appointmentCalendarLabel : tp.translate(appointmentCalendarLabel);
        this.appointmentLog.append("<tr><td>" + appointmentStr + "</td><td>" + appointmentCalendarLabel + "</td><td>" + value + "</td><td>&nbsp;</td></tr>");
    }

    public void appendLog(Activity activity, int value) {
        TranslationProcessor tp = this.getTranslationProcessor();
        String activityStr = tp == null ? "Activity" : tp.translate("Activity");
        String activityLabel = tp == null ? activity.getLabel() : tp.translate(activity.getLabel());
        this.activityLog.append("<tr><td>" + activityStr + "</td><td>" + activityLabel + "</td><td>" + value + "</td><td>&nbsp;</td></tr>");
    }

    public void appendActivityLog(String message, int value) {
        TranslationProcessor tp = this.getTranslationProcessor();
        String activityStr = tp == null ? "Activity" : tp.translate("Activity");
        this.activityLog.append("<tr><td>" + activityStr + "</td><td>" + message + "</td><td>" + value + "</td><td>&nbsp;</td></tr>");
    }

    public void appendActivityLog(String userid, String message, int value) {
        TranslationProcessor tp = this.getTranslationProcessor();
        String activitiesStr = tp == null ? "Activities" : tp.translate("Activities");
        this.activityLog.append("<tr><td>" + activitiesStr + ": " + userid + "</td><td>" + message + "</td><td>" + value + "</td><td>&nbsp;</td></tr>");
    }

    public void appendActivityLog(StringBuilder row) {
        this.activityLog.append((CharSequence)row);
    }

    public void appendAppointmentLog(String userid, String message, int value) {
        TranslationProcessor tp = this.getTranslationProcessor();
        String appointmentsStr = tp == null ? "Appointments" : tp.translate("Appointments");
        this.appointmentLog.append("<tr><td>" + appointmentsStr + ": " + userid + "</td><td>" + message + "</td><td>" + value + "</td><td>&nbsp;</td></tr>");
    }

    public StringBuilder getActivityLog() {
        return this.activityLog;
    }

    public StringBuilder getAppointmentLog() {
        return this.appointmentLog;
    }

    public TranslationProcessor getTranslationProcessor() {
        return this.translationProcessor;
    }

    public void setTranslationProcessor(TranslationProcessor translationProcessor) {
        this.translationProcessor = translationProcessor;
    }

    public void setWorkingRanges(ArrayList<ZonedDateTime[]> workingRanges) {
        this.workingRanges = workingRanges;
    }

    public ArrayList<ZonedDateTime[]> getWorkingRanges() {
        return this.workingRanges;
    }

    public void addExtraWorkingMinutes(int workingMinutes) {
        this.extraWorkingMinutes += workingMinutes;
    }

    public ZonedDateTime[] getWidestWorkingRange() {
        if (this.widestRange == null) {
            this.widestRange = new ZonedDateTime[]{null, null};
            for (ZonedDateTime[] range : this.workingRanges) {
                if (this.widestRange[0] == null) {
                    this.widestRange[0] = range[0];
                    this.widestRange[1] = range[1];
                    continue;
                }
                if (range[0].isBefore(this.widestRange[0])) {
                    this.widestRange[0] = range[0];
                }
                if (!range[1].isAfter(this.widestRange[1])) continue;
                this.widestRange[1] = range[1];
            }
        }
        return this.widestRange;
    }

    public void addWidestRange(ZonedDateTime[] extraRange) {
        if (extraRange == null || extraRange[0] == null) {
            return;
        }
        if (this.widestRange == null || this.widestRange[0] == null) {
            this.widestRange = extraRange;
        } else {
            if (extraRange[0].isBefore(this.widestRange[0])) {
                this.widestRange[0] = extraRange[0];
            }
            if (extraRange[1].isAfter(this.widestRange[1])) {
                this.widestRange[1] = extraRange[1];
            }
        }
    }

    public static int getWorkingOverlapMinutes(List<ZonedDateTime[]> workingRanges, ZonedDateTime appointmentStartDt, ZonedDateTime appointmentEndDt) {
        if (workingRanges.size() == 0) {
            return 0;
        }
        int overlap = 0;
        for (ZonedDateTime[] range : workingRanges) {
            overlap += DateTimeUtil.getOverlappingMinutes(appointmentStartDt.toInstant(), appointmentEndDt.toInstant(), range[0].toInstant(), range[1].toInstant());
        }
        return overlap;
    }

    public static int getWorkingMinutes(List<ZonedDateTime[]> workingRanges) {
        int workingMinutes = 0;
        for (ZonedDateTime[] range : workingRanges) {
            workingMinutes = (int)((long)workingMinutes + ChronoUnit.MINUTES.between(range[0], range[1]));
        }
        return workingMinutes;
    }

    public static int getWorkingMinutesAfterDataTime(List<ZonedDateTime[]> workingRanges, ZonedDateTime onlyMinutesAfterThisDate) {
        int workingMinutes = 0;
        for (ZonedDateTime[] range : workingRanges) {
            if (!range[1].isAfter(onlyMinutesAfterThisDate)) continue;
            if (range[0].isBefore(onlyMinutesAfterThisDate)) {
                range[0] = onlyMinutesAfterThisDate;
            }
            workingMinutes = (int)((long)workingMinutes + ChronoUnit.MINUTES.between(range[0], range[1]));
        }
        return workingMinutes;
    }
}

