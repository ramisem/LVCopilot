/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.scheduler.ajax;

import com.labvantage.sapphire.scheduler.ScheduleEvent;
import com.labvantage.sapphire.scheduler.SchedulerAdminProcessor;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TimeZone;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.M18NUtil;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class ChangeScheduleEventStatus
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest req, HttpServletResponse resp, ServletContext sc) throws ServletException {
        AjaxResponse ar = new AjaxResponse(req, resp);
        String schedulePlanId = ar.getRequestParameter("scheduleplanid", "");
        String schedulePlanItemId = ar.getRequestParameter("scheduleplanitemid", "");
        String eventDtStr = ar.getRequestParameter("eventdt", "");
        String eventNum = ar.getRequestParameter("eventnum", "");
        String eventStatus = ar.getRequestParameter("eventstatus", "");
        String newStatus = ar.getRequestParameter("newstatus", "");
        String timezones = ar.getRequestParameter("timezone", "");
        boolean userTimeZone = ar.getRequestParameter("dateinusertimezone", "").startsWith("Y");
        if (schedulePlanId.isEmpty()) {
            throw new IllegalArgumentException("Schedule Plan Id is missing");
        }
        if (schedulePlanItemId.isEmpty()) {
            throw new IllegalArgumentException("Schedule Plan Item Id is missing");
        }
        if (eventDtStr.isEmpty()) {
            throw new IllegalArgumentException("Event Date is missing");
        }
        if (eventNum.isEmpty()) {
            throw new IllegalArgumentException("Event Number is missing");
        }
        if (newStatus.isEmpty()) {
            throw new IllegalArgumentException("New Event Status is missing");
        }
        PropertyList statusProps = new PropertyList();
        ArrayList<ScheduleEvent> events = new ArrayList<ScheduleEvent>();
        String[] plansArr = StringUtil.split(schedulePlanId, ";");
        String[] plansItemsArr = StringUtil.split(schedulePlanItemId, ";");
        String[] eventDatesArr = StringUtil.split(eventDtStr, ";");
        String[] eventStatusesArr = StringUtil.split(eventStatus, ";");
        String[] eventNumsArr = StringUtil.split(eventNum, ";");
        String[] timeZonesArr = StringUtil.split(timezones, ";");
        M18NUtil m18n = new M18NUtil(this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()));
        boolean planItemsOk = true;
        String msg = "";
        try {
            for (int i = 0; i < plansArr.length; ++i) {
                M18NUtil m18NTimezone;
                TimeZone planTimeZone = timeZonesArr[i].isEmpty() ? TimeZone.getDefault() : TimeZone.getTimeZone(timeZonesArr[i]);
                if (userTimeZone) {
                    m18NTimezone = m18n;
                } else {
                    m18NTimezone = new M18NUtil(this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()));
                    m18NTimezone.setTimeZone(planTimeZone);
                }
                Calendar eventDt = userTimeZone ? m18n.parseCalendar(eventDatesArr[i]) : m18NTimezone.parseCalendar(eventDatesArr[i]);
                eventDt.setTimeZone(planTimeZone);
                ScheduleEvent event = new ScheduleEvent(plansArr[i], plansItemsArr[i], eventDt, planTimeZone);
                event.setEventNum(Integer.valueOf(eventNumsArr[i]));
                event.setEventStatus(eventStatusesArr[i]);
                events.add(event);
                String sql = "SELECT spi.scheduleplanid, spi.scheduleplanitemid, spn.nodestatus, sp.planstatus, spi.planitemstatus, spi.scheduleconditionid, spi.reviewdispositionflag,       s.studyid, s.studystatus, sc.conditionstatus, sc.conditionlabel  FROM scheduleplanitem spi    left join scheduleplan sp on sp.scheduleplanid = spi.scheduleplanid    left join scheduleplannode spn on spn.scheduleplanid = spi.scheduleplanid and spn.scheduleplannodeid = spi.scheduleplannodeid    left join study_scheduleplan ss on ss.scheduleplanid = sp.scheduleplanid     left join study s on ss.studyid = s.studyid    left join schedulecondition sc on sc.scheduleplanid = sp.scheduleplanid and sc.scheduleconditionid = spi.scheduleconditionid WHERE spi.scheduleplanid = ? and spi.scheduleplanitemid = ?";
                DataSet checkStatusDs = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])new String[]{plansArr[i], plansItemsArr[i]});
                if (checkStatusDs.getRowCount() == 1) {
                    HashMap<String, String> transmap;
                    String studyId = checkStatusDs.getValue(0, "studyid", "");
                    boolean planActive = checkStatusDs.getValue(0, "planstatus", studyId.isEmpty() ? "" : "A").equals("A");
                    boolean planItemActive = checkStatusDs.getValue(0, "planitemstatus", "").equals("A");
                    boolean planNodeDisabled = checkStatusDs.getValue(0, "nodestatus", "").startsWith("X");
                    String studystatus = checkStatusDs.getValue(0, "studystatus", "");
                    String conditionstatus = checkStatusDs.getValue(0, "conditionstatus", "");
                    String conditionLabel = checkStatusDs.getValue(0, "conditionlabel", "");
                    String reviewdispositionFlag = checkStatusDs.getValue(0, "reviewdispositionflag ", "");
                    if (!(studyId.isEmpty() || eventStatusesArr[i].equals("E") || eventStatusesArr[i].equals("I"))) {
                        planItemsOk = false;
                        msg = this.getTranslationProcessor().translate("Changing event status allowed only for Error and Inactive events within Stability Studies.");
                    }
                    if (!reviewdispositionFlag.isEmpty()) {
                        planItemsOk = false;
                        msg = this.getTranslationProcessor().translate("Event status change not allowed - At least one item has been reviewed");
                        continue;
                    }
                    if (studystatus.equals("C") || studystatus.equals("X")) {
                        planItemsOk = false;
                        transmap = new HashMap<String, String>();
                        transmap.put("studyid", studyId);
                        transmap.put("studystatus", studystatus);
                        msg = this.getTranslationProcessor().translate("Event status change not allowed - Study [studyid] is [studystatus]", transmap);
                        continue;
                    }
                    if (conditionstatus.equals("X") || conditionstatus.equals("C")) {
                        planItemsOk = false;
                        transmap = new HashMap();
                        transmap.put("studyid", studyId);
                        transmap.put("conditionlabel", conditionLabel);
                        msg = this.getTranslationProcessor().translate("Event status change not allowed - Study [studyid], Condition [conditionlabel] is cancelled or complete", transmap);
                        continue;
                    }
                    if (planActive && planItemActive && !planNodeDisabled || !eventStatusesArr[i].equals("E") || !newStatus.equals("S")) continue;
                    planItemsOk = false;
                    msg = this.getTranslationProcessor().translate("Event status change not allowed - Schedule not active!");
                    continue;
                }
                planItemsOk = false;
                msg = this.getTranslationProcessor().translate("Could not find schedule event and details.");
            }
        }
        catch (Exception e) {
            planItemsOk = false;
        }
        statusProps.put("events", events);
        statusProps.put("neweventstatus", newStatus);
        SchedulerAdminProcessor scheduler = new SchedulerAdminProcessor(this.getConnectionid());
        boolean status = false;
        if (planItemsOk) {
            try {
                scheduler.changeEventStatus(statusProps);
                status = true;
                msg = this.getTranslationProcessor().translate("Event status changed successfully");
            }
            catch (Exception e) {
                msg = e.getMessage();
            }
        }
        ar.addCallbackArgument("status", status);
        ar.addCallbackArgument("msg", msg);
        ar.print();
    }
}

