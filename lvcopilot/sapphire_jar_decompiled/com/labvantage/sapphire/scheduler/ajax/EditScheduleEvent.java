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
import java.util.TimeZone;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.M18NUtil;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class EditScheduleEvent
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest req, HttpServletResponse resp, ServletContext sc) throws ServletException {
        AjaxResponse ar = new AjaxResponse(req, resp);
        String schedulePlanId = ar.getRequestParameter("scheduleplanid", "");
        String schedulePlanItemId = ar.getRequestParameter("scheduleplanitemid", "");
        String eventDtStr = ar.getRequestParameter("eventdt", "");
        String newEventdtStr = ar.getRequestParameter("neweventdt", "");
        String eventNumStr = ar.getRequestParameter("eventnum", "");
        String executeDtStr = ar.getRequestParameter("executedt", "");
        String eventStatusStr = ar.getRequestParameter("eventstatus", "");
        String timezoneStr = ar.getRequestParameter("timezone", "");
        String mode = ar.getRequestParameter("mode", "move");
        String auditreason = ar.getRequestParameter("auditreason", "");
        String auditactivity = ar.getRequestParameter("auditactivity", "");
        String auditsignedflag = ar.getRequestParameter("auditsignedflag", "");
        boolean useUserTimeZone = ar.getRequestParameter("dateinusertimezone", "").startsWith("Y");
        if (schedulePlanId.isEmpty()) {
            throw new IllegalArgumentException("Schedule Plan Id is missing");
        }
        if (schedulePlanItemId.isEmpty()) {
            throw new IllegalArgumentException("Schedule Plan Item Id is missing");
        }
        if (eventDtStr.isEmpty()) {
            throw new IllegalArgumentException("Event Date is missing");
        }
        if (newEventdtStr.isEmpty()) {
            throw new IllegalArgumentException("New Event Date is missing");
        }
        if (eventNumStr.isEmpty()) {
            throw new IllegalArgumentException("Event Number is missing");
        }
        if (executeDtStr.isEmpty()) {
            throw new IllegalArgumentException("Execute Date is missing");
        }
        ArrayList<ScheduleEvent> events = new ArrayList<ScheduleEvent>();
        String[] plansArr = StringUtil.split(schedulePlanId, ";");
        String[] plansItemsArr = StringUtil.split(schedulePlanItemId, ";");
        String[] eventDatesArr = StringUtil.split(eventDtStr, ";");
        String[] executeDatesArr = StringUtil.split(executeDtStr, ";");
        String[] eventStatusesArr = StringUtil.split(eventStatusStr, ";");
        String[] eventNumsArr = StringUtil.split(eventNumStr, ";");
        String[] timeZonesArr = StringUtil.split(timezoneStr, ";");
        M18NUtil m18n = new M18NUtil(this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()));
        SchedulerAdminProcessor scheduler = new SchedulerAdminProcessor(this.getConnectionid());
        String msg = "";
        boolean status = false;
        try {
            for (int i = 0; i < plansArr.length; ++i) {
                Calendar eventDt;
                Calendar executeDt;
                Calendar newEventDt;
                M18NUtil m18NTimezone;
                TimeZone planTimeZone = timeZonesArr[i].isEmpty() ? TimeZone.getDefault() : TimeZone.getTimeZone(timeZonesArr[i]);
                if (useUserTimeZone) {
                    m18NTimezone = m18n;
                } else {
                    m18NTimezone = new M18NUtil(this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()));
                    m18NTimezone.setTimeZone(planTimeZone);
                }
                if (useUserTimeZone) {
                    newEventDt = m18n.parseCalendar(newEventdtStr);
                    executeDt = m18n.parseCalendar(executeDatesArr[i]);
                    eventDt = m18n.parseCalendar(eventDatesArr[i]);
                } else {
                    newEventDt = m18NTimezone.parseCalendar(newEventdtStr);
                    executeDt = m18NTimezone.parseCalendar(executeDatesArr[i]);
                    eventDt = m18NTimezone.parseCalendar(eventDatesArr[i]);
                }
                ScheduleEvent event = new ScheduleEvent(plansArr[i], plansItemsArr[i], eventDt, planTimeZone);
                event.setEventNum(Integer.valueOf(eventNumsArr[i]));
                String eventStatus = eventStatusesArr[i];
                boolean addEvent = false;
                if (mode.equals("move")) {
                    if (eventStatus.equals("P")) {
                        addEvent = true;
                        event.setEventDt(newEventDt);
                    }
                    long executeahead = eventDt.getTime().getTime() - executeDt.getTime().getTime();
                    executeDt.setTimeInMillis(newEventDt.getTime().getTime() - executeahead);
                } else if (mode.equals("copy")) {
                    addEvent = true;
                    eventStatus = "S";
                    event.setAdHocFlag(true);
                    event.setEventDt(newEventDt);
                    event.setOriginalEventDt(newEventDt);
                }
                event.setEventStatus(eventStatus);
                events.add(event);
                PropertyList props = new PropertyList();
                props.setProperty("mode", "movescheduleeventwithevents");
                props.put("event", event);
                props.put("executedt", executeDt);
                props.put("movetodt", newEventDt);
                props.put("auditreason", auditreason);
                props.put("auditactivity", auditactivity);
                props.put("auditsignedflag", auditsignedflag);
                if (addEvent) {
                    props.put("addevent", "Y");
                }
                try {
                    scheduler.moveEvent(props);
                    status = true;
                    continue;
                }
                catch (Exception e) {
                    msg = e.getMessage();
                }
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        ar.addCallbackArgument("status", status);
        ar.addCallbackArgument("msg", msg);
        ar.print();
    }
}

