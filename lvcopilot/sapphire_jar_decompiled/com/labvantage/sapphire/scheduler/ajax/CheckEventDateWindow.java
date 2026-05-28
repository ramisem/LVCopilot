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

public class CheckEventDateWindow
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest req, HttpServletResponse resp, ServletContext sc) throws ServletException {
        AjaxResponse ar = new AjaxResponse(req, resp);
        String schedulePlanIdStr = ar.getRequestParameter("scheduleplanid", "");
        String schedulePlanItemIdStr = ar.getRequestParameter("scheduleplanitemid", "");
        String eventDtStr = ar.getRequestParameter("eventdt", "");
        String newEventdtStr = ar.getRequestParameter("neweventdt", "");
        String eventNumStr = ar.getRequestParameter("eventnum", "");
        String timezoneStr = ar.getRequestParameter("timezone", "");
        String windowStartDtStr = ar.getRequestParameter("windowstartdt", "");
        String windowEndDtStr = ar.getRequestParameter("windowenddt", "");
        String originaleventdtStr = ar.getRequestParameter("originaleventdt", "");
        boolean isSummaryMode = StringUtil.getYN(ar.getRequestParameter("summarymode", ""), "N").equals("Y");
        boolean isStability = StringUtil.getYN(ar.getRequestParameter("stability", ""), "N").equals("Y");
        boolean useUserTimeZone = ar.getRequestParameter("dateinusertimezone", "").startsWith("Y");
        if (schedulePlanIdStr.isEmpty()) {
            throw new IllegalArgumentException("Schedule Plan Id is missing");
        }
        if (schedulePlanItemIdStr.isEmpty()) {
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
        ArrayList events = new ArrayList();
        String[] plansArr = StringUtil.split(schedulePlanIdStr, ";");
        String[] plansItemArr = StringUtil.split(schedulePlanItemIdStr, ";");
        String[] timeZonesArr = StringUtil.split(timezoneStr, ";");
        String[] originalEventDtStrArr = StringUtil.split(originaleventdtStr, ";");
        String[] eventDtArr = StringUtil.split(eventDtStr, ";");
        String[] windowStartDtArr = StringUtil.split(windowStartDtStr, ";");
        String[] windowEndDtArr = StringUtil.split(windowEndDtStr, ";");
        if (isSummaryMode) {
            windowEndDtStr = "";
            windowStartDtStr = "";
        }
        M18NUtil m18n = new M18NUtil(this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()));
        String msg = "";
        String status = "ok";
        for (int i = 0; i < plansArr.length; ++i) {
            M18NUtil m18NTimezone;
            TimeZone planTimeZone = timeZonesArr[i].isEmpty() ? TimeZone.getDefault() : TimeZone.getTimeZone(timeZonesArr[i]);
            if (useUserTimeZone) {
                m18NTimezone = m18n;
            } else {
                m18NTimezone = new M18NUtil(this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()));
                m18NTimezone.setTimeZone(planTimeZone);
            }
            String scheduleplanid = plansArr[i];
            String scheduleplanitemid = plansItemArr[i];
            String sql = "select * from scheduleplanitem where scheduleplanid = ? and scheduleplanitemid = ?";
            DataSet planItemDs = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])new String[]{scheduleplanid, scheduleplanitemid});
            if (planItemDs.getRowCount() != 1) {
                throw new IllegalArgumentException("Schedule Plan Item Not Found!");
            }
            Calendar newEventDt = null;
            Calendar windowStartDt = null;
            Calendar windowEndDt = null;
            Calendar originalEventDt = null;
            String newEventDtStrConverted = "";
            String windowStartDtStrThisPlanItem = "";
            String windowEndDtStrThisPlanItem = "";
            if (isStability) {
                windowStartDtStrThisPlanItem = windowStartDtArr[i];
                windowEndDtStrThisPlanItem = windowEndDtArr[i];
            }
            try {
                if (useUserTimeZone) {
                    newEventDt = m18n.parseCalendar(newEventdtStr);
                    newEventDtStrConverted = m18n.format(newEventDt);
                    windowStartDt = m18n.parseCalendar(windowStartDtStrThisPlanItem);
                    windowEndDt = m18n.parseCalendar(windowEndDtStrThisPlanItem);
                    originalEventDt = m18n.parseCalendar(originalEventDtStrArr[i]);
                } else {
                    newEventDt = m18NTimezone.parseCalendar(newEventdtStr);
                    newEventDtStrConverted = m18NTimezone.format(newEventDt);
                    windowStartDt = m18NTimezone.parseCalendar(windowStartDtStrThisPlanItem);
                    windowEndDt = m18NTimezone.parseCalendar(windowEndDtStrThisPlanItem);
                    originalEventDt = m18NTimezone.parseCalendar(originalEventDtStrArr[i]);
                }
            }
            catch (Exception e) {
                status = "invalid";
                msg = this.getTranslationProcessor().translate("Invalid date given");
            }
            if (isSummaryMode) {
                SchedulerAdminProcessor scheduler = new SchedulerAdminProcessor(this.getConnectionid());
                PropertyList gracePerioidMap = new PropertyList();
                gracePerioidMap.put("scheduleplanid", scheduleplanid);
                gracePerioidMap.put("scheduleplanitemid", scheduleplanitemid);
                gracePerioidMap.put("propertytreeid", planItemDs.getValue(0, "propertytreeid"));
                gracePerioidMap.put("scheduletasknodeid", planItemDs.getValue(0, "scheduleplannodeid"));
                gracePerioidMap.put("startdt", planItemDs.getCalendar(0, "startdt"));
                gracePerioidMap.put("originaleventdt", originalEventDt);
                try {
                    scheduler.getPlanItemGracePerioid(gracePerioidMap);
                }
                catch (Exception e) {
                    status = "invalid";
                    msg = this.getTranslationProcessor().translate("Could not calculate Grace period");
                }
                windowStartDt = (Calendar)gracePerioidMap.get("windowstartdt");
                if (windowStartDt != null) {
                    windowStartDtStrThisPlanItem = m18NTimezone.format(windowStartDt);
                    windowStartDtStr = windowStartDtStr + (windowStartDtStr.isEmpty() ? "" : ";") + windowEndDtStrThisPlanItem;
                }
                if ((windowEndDt = (Calendar)gracePerioidMap.get("windowenddt")) != null) {
                    windowEndDtStrThisPlanItem = m18NTimezone.format(windowEndDt);
                    windowEndDtStr = windowEndDtStr + (windowEndDtStr.isEmpty() ? "" : ";") + windowEndDtStrThisPlanItem;
                }
            }
            if (!status.equals("ok")) continue;
            HashMap<String, String> transMap = new HashMap<String, String>();
            transMap.put("neweventdt", newEventDtStrConverted);
            transMap.put("windowstartdt", StringUtil.replaceAll(windowEndDtStr, ";", " / "));
            transMap.put("windowenddt", StringUtil.replaceAll(windowEndDtStr, ";", " / "));
            if (!windowStartDtStrThisPlanItem.isEmpty() && newEventDt.compareTo(windowStartDt) < 0) {
                msg = this.getTranslationProcessor().translate("The new event date [neweventdt] is before window start date [windowstartdt]. Do you want to continue?", transMap);
                status = "false";
            }
            if (!status.equals("ok") || windowEndDtStrThisPlanItem.isEmpty() || newEventDt.compareTo(windowEndDt) <= 0) continue;
            msg = this.getTranslationProcessor().translate("The new event date [neweventdt] is after window end date [windowenddt]. Do you want to continue?", transMap);
            status = "false";
        }
        ar.addCallbackArgument("status", status);
        ar.addCallbackArgument("msg", msg);
        ar.print();
    }
}

