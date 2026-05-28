/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.wap.activity;

import com.labvantage.sapphire.modules.wap.CalendarConverter;
import com.labvantage.sapphire.modules.wap.activity.WAPConstants;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import sapphire.util.DataSet;

public class Activity
implements WAPConstants {
    private String activityid = "";
    private String label = "";
    private String worksdcid = "";
    private int activitySize = -1;
    private int maxActivitySize = -999999;
    private String timeMode = "None";
    private String status = "";
    private int maxDurationMinutes = -9999999;
    private boolean isEndDateFixed = false;
    private String workContext = "";
    private String testingDepartmentid = "";
    private Instant startDateInstantUTC;
    private Instant endDateInstantUTC;
    private Instant startRangeInstantUTC;
    private Instant endRangeInstantUTC;
    private int workCompleteCount;
    private Instant workDuedt;
    private Calendar actualStartDate;
    private Calendar actualEndDate;
    private Calendar cancelledDate;
    private Calendar createDate;
    private Calendar activatedDate;
    private String activityClass = "";
    private String activityContextSdcid = "";
    private String activityContextKeyid1 = "";
    private String activityContextKeyid2 = "";
    private String activityContextKeyid3 = "";
    private String isReservation = "";
    private String reservationType = "";
    private String reservationContext = "";
    private String extra_resourcetype = "";
    private int extra_resourceduration = -9999999;
    private CalendarConverter calendarConverter;

    public Activity() {
    }

    public Activity(CalendarConverter calendarConverter, DataSet ds, int row) {
        this.calendarConverter = calendarConverter;
        this.setActivityid(ds.getString(row, "activityid"));
        this.setLabel(ds.getValue(row, "activitydesc"));
        this.setWorksdcid(ds.getValue(row, "worksdcid"));
        this.setActivitySize(ds.getInt(row, "activitysize"));
        this.setWorkCompleteCount(ds.getInt(row, "workcompletecount"));
        this.setMaxActivitySize(ds.getInt(row, "maxactivitysize"));
        this.setWorkContext(ds.getValue(row, "workcontext"));
        this.setTestingDepartmentid(ds.getValue(row, "testingdepartmentid"));
        this.setTimeMode(ds.getValue(row, "timemode"));
        this.setStatus(ds.getValue(row, "activitystatus"));
        this.startDateInstantUTC = calendarConverter.convertDatabaseCalendarToInstantUtc(ds.getCalendar(row, "startdt"));
        this.endDateInstantUTC = calendarConverter.convertDatabaseCalendarToInstantUtc(ds.getCalendar(row, "enddt"));
        this.startRangeInstantUTC = calendarConverter.convertDatabaseCalendarToInstantUtc(ds.getCalendar(row, "startrangedt"));
        this.endRangeInstantUTC = calendarConverter.convertDatabaseCalendarToInstantUtc(ds.getCalendar(row, "endrangedt"));
        this.setWorkDuedt(calendarConverter.convertDatabaseCalendarToInstantUtc(ds.getCalendar(row, "workduedt")));
        this.setCancelledDate(ds.getCalendar(row, "cancelleddt"));
        this.setCreateDate(ds.getCalendar(row, "createdt"));
        this.setActualStartDate(ds.getCalendar(row, "actualstartdt"));
        this.setActualEndDate(ds.getCalendar(row, "actualenddt"));
        this.setActivatedDate(ds.getCalendar(row, "activateddt"));
        this.setMaxDurationMinutes(ds.getInt(row, "maxduration"));
        this.setIsEndDateFixed("Y".equals(ds.getString(row, "enddtfixedflag")));
        this.setActivityClass(ds.getValue(row, "activityclass"));
        this.setActivityContextSdcid(ds.getValue(row, "activitycontextsdcid"));
        this.setActivityContextKeyid1(ds.getValue(row, "activitycontextkeyid1"));
        this.setActivityContextKeyid2(ds.getValue(row, "activitycontextkeyid2"));
        this.setActivityContextKeyid3(ds.getValue(row, "activitycontextkeyid3"));
        this.setIsReservation("Y".equals(ds.getString(row, "reservationflag")));
        this.setReservationType(ds.getValue(row, "reservationtype"));
        this.setReservationContext(ds.getValue(row, "reservationcontext"));
        if (ds.isValidColumn("extra_resourcetype")) {
            this.setExtra_resourcetype(ds.getValue(row, "extra_resourcetype"));
            this.setExtra_resourceduration(ds.getInt(row, "extra_resourceduration", -99999));
        }
    }

    public Instant getStartDateInstantUTC() {
        return this.startDateInstantUTC;
    }

    public void setStartDateInstantUTC(Instant startDateInstantUTC) {
        this.startDateInstantUTC = startDateInstantUTC;
    }

    public Instant getEndDateInstantUTC() {
        return this.endDateInstantUTC;
    }

    public void setEndDateInstantUTC(Instant endDateInstantUTC) {
        this.endDateInstantUTC = endDateInstantUTC;
    }

    public Instant getStartRangeInstantUTC() {
        return this.startRangeInstantUTC;
    }

    public void setStartRangeInstantUTC(Instant startRangeInstantUTC) {
        this.startRangeInstantUTC = startRangeInstantUTC;
    }

    public Instant getEndRangeInstantUTC() {
        return this.endRangeInstantUTC;
    }

    public void setEndRangeInstantUTC(Instant endRangeInstantUTC) {
        this.endRangeInstantUTC = endRangeInstantUTC;
    }

    public void setExtra_resourcetype(String extra_resourcetype) {
        this.extra_resourcetype = extra_resourcetype;
    }

    public String getExtra_resourcetype() {
        return this.extra_resourcetype;
    }

    public void setExtra_resourceduration(int extra_resourceduration) {
        this.extra_resourceduration = extra_resourceduration;
    }

    public int getExtra_resourceduration() {
        return this.extra_resourceduration >= 0 ? this.extra_resourceduration : this.getMaxDurationMinutes();
    }

    public Calendar getCancelledDate() {
        return this.cancelledDate;
    }

    public void setCancelledDate(Calendar cancelledDate) {
        this.cancelledDate = cancelledDate;
    }

    public Calendar getCreateDate() {
        return this.createDate;
    }

    public void setCreateDate(Calendar createDate) {
        this.createDate = createDate;
    }

    public Calendar getActualStartDate() {
        return this.actualStartDate;
    }

    public void setActualStartDate(Calendar actualStartDate) {
        this.actualStartDate = actualStartDate;
    }

    public Calendar getActualEndDate() {
        return this.actualEndDate;
    }

    public void setActualEndDate(Calendar actualEndDate) {
        this.actualEndDate = actualEndDate;
    }

    public Calendar getActivatedDate() {
        return this.activatedDate;
    }

    public void setActivatedDate(Calendar activatedDate) {
        this.activatedDate = activatedDate;
    }

    public int getWorkCompleteCount() {
        return this.workCompleteCount < 0 ? 0 : this.workCompleteCount;
    }

    public void setWorkCompleteCount(int workCompleteCount) {
        this.workCompleteCount = workCompleteCount;
    }

    public Instant getWorkDuedt() {
        return this.workDuedt;
    }

    public void setWorkDuedt(Instant workDuedt) {
        this.workDuedt = workDuedt;
    }

    public String getActivityClass() {
        return this.activityClass;
    }

    public void setActivityClass(String contextClass) {
        this.activityClass = contextClass;
    }

    public String getTestingDepartmentid() {
        return this.testingDepartmentid;
    }

    public void setTestingDepartmentid(String testingDepartmentid) {
        this.testingDepartmentid = testingDepartmentid;
    }

    public String getWorkContext() {
        return this.workContext;
    }

    public void setWorkContext(String workContext) {
        this.workContext = workContext;
    }

    public boolean isReservation() {
        return "Y".equals(this.isReservation);
    }

    public boolean isReservationChange() {
        return !"".equals(this.isReservation);
    }

    public void setIsReservation(boolean isReservation) {
        this.isReservation = isReservation ? "Y" : "N";
    }

    public String getReservationType() {
        return this.reservationType;
    }

    public void setReservationType(String reservationType) {
        this.reservationType = reservationType;
    }

    public String getActivityContextSdcid() {
        return this.activityContextSdcid;
    }

    public void setActivityContextSdcid(String activityContextSdcid) {
        this.activityContextSdcid = activityContextSdcid;
    }

    public String getActivityContextKeyid1() {
        return this.activityContextKeyid1;
    }

    public void setActivityContextKeyid1(String activityContextKeyid1) {
        this.activityContextKeyid1 = activityContextKeyid1;
    }

    public String getActivityContextKeyid2() {
        return this.activityContextKeyid2;
    }

    public void setActivityContextKeyid2(String activityContextKeyid2) {
        this.activityContextKeyid2 = activityContextKeyid2;
    }

    public String getActivityContextKeyid3() {
        return this.activityContextKeyid3;
    }

    public void setActivityContextKeyid3(String activityContextKeyid3) {
        this.activityContextKeyid3 = activityContextKeyid3;
    }

    public String getReservationContext() {
        return this.reservationContext;
    }

    public void setReservationContext(String reservationContext) {
        this.reservationContext = reservationContext;
    }

    public int getActivitySize() {
        return this.activitySize < 0 ? 0 : this.activitySize;
    }

    public int getEditActivitySize() {
        return this.activitySize;
    }

    public void setActivitySize(int activitySize) {
        this.activitySize = activitySize;
    }

    public int getMaxActivitySize() {
        return this.maxActivitySize < 0 ? 1000 : this.maxActivitySize;
    }

    public int getEditMaxActivitySize() {
        return this.maxActivitySize;
    }

    public void setMaxActivitySize(int maxActivitySize) {
        this.maxActivitySize = maxActivitySize;
    }

    public String getWorksdcid() {
        return this.worksdcid;
    }

    public void setWorksdcid(String worksdcid) {
        this.worksdcid = worksdcid;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getActivityid() {
        return this.activityid;
    }

    public void setActivityid(String activityid) {
        this.activityid = activityid;
    }

    public String getLabel() {
        return this.label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getTimeMode() {
        return this.timeMode;
    }

    public void setTimeMode(String timeMode) {
        this.timeMode = timeMode;
    }

    public void setStartRangeDate(String startRangeDate) {
        this.startRangeInstantUTC = Instant.from(DateTimeFormatter.ISO_INSTANT.parse(startRangeDate));
    }

    public void setEndRangeDate(String endRangeDate) {
        this.endRangeInstantUTC = Instant.from(DateTimeFormatter.ISO_INSTANT.parse(endRangeDate));
    }

    public int getMaxDurationMinutes() {
        return this.maxDurationMinutes < 0 ? 0 : this.maxDurationMinutes;
    }

    public void setMaxDurationMinutes(int maxDurationMinutes) {
        this.maxDurationMinutes = maxDurationMinutes;
    }

    public boolean isEndDateFixed() {
        return this.isEndDateFixed;
    }

    public void setIsEndDateFixed(boolean isEndDateFixed) {
        this.isEndDateFixed = isEndDateFixed;
    }
}

