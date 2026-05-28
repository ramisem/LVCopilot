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
import com.labvantage.sapphire.scheduler.SchedulerUtil;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
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

public class ExecuteScheduleEvent
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest req, HttpServletResponse resp, ServletContext sc) throws ServletException {
        AjaxResponse ar = new AjaxResponse(req, resp);
        String schedulePlanId = ar.getRequestParameter("scheduleplanid", "");
        String schedulePlanItemId = ar.getRequestParameter("scheduleplanitemid", "");
        String eventDtStr = ar.getRequestParameter("eventdt", "");
        String timezoneStr = ar.getRequestParameter("timezone", "");
        boolean adhocMode = ar.getRequestParameter("adhocmode", "N").startsWith("Y");
        boolean useUserTimeZone = ar.getRequestParameter("dateinusertimezone", "").startsWith("Y");
        String[] schedulePlanIds = StringUtil.split(schedulePlanId, ";");
        String[] schedulePlanItemIds = StringUtil.split(schedulePlanItemId, ";");
        String[] timeZoneStrs = StringUtil.split(timezoneStr, ";");
        String eventNum = ar.getRequestParameter("eventnum", "");
        String eventStatus = ar.getRequestParameter("eventstatus", "");
        String adhocExecution = ar.getRequestParameter("adhocexecution", "");
        String newEventdtSrt = ar.getRequestParameter("neweventdt", "");
        String refresh = ar.getRequestParameter("refresh", "false");
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
        if (schedulePlanIds.length != schedulePlanItemIds.length || schedulePlanIds.length != timeZoneStrs.length) {
            throw new IllegalArgumentException("Number of Plan Items does not match!");
        }
        String msg = "";
        boolean status = false;
        int successCnt = 0;
        HashMap newKeyidMap = new HashMap();
        for (int i = 0; i < schedulePlanIds.length; ++i) {
            schedulePlanId = schedulePlanIds[i];
            schedulePlanItemId = schedulePlanItemIds[i];
            timezoneStr = timeZoneStrs[i];
            M18NUtil m18n = new M18NUtil(this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()));
            TimeZone planTimeZone = timezoneStr.isEmpty() ? TimeZone.getDefault() : TimeZone.getTimeZone(timezoneStr);
            Calendar eventdt = m18n.parseCalendar(eventDtStr);
            if (!useUserTimeZone) {
                int timeZoneCorrection = SchedulerUtil.getTimeZoneDiffForDate(m18n.getTimezone(), planTimeZone, eventdt);
                eventdt.add(14, -timeZoneCorrection);
                eventDtStr = m18n.format(eventdt);
            }
            PropertyList executeProps = new PropertyList();
            executeProps.setProperty("scheduleplanid", schedulePlanId);
            executeProps.setProperty("scheduleplanitemid", schedulePlanItemId);
            executeProps.setProperty("eventfromdate", eventDtStr);
            executeProps.setProperty("eventtodate", eventDtStr);
            executeProps.setProperty("eventstatus", eventStatus);
            executeProps.setProperty("eventnum", eventNum);
            executeProps.put("newkeyidmap", newKeyidMap);
            if (eventStatus.equals("P")) {
                executeProps.setProperty("noevent", "true");
                executeProps.setProperty("eventdate", eventDtStr);
            }
            if (adhocExecution.startsWith("Y")) {
                executeProps.setProperty("adhocexecution", "true");
                executeProps.setProperty("noevent", "true");
                if (newEventdtSrt.isEmpty()) {
                    executeProps.setProperty("eventdate", eventDtStr);
                } else {
                    Calendar newEventdt = m18n.parseCalendar(newEventdtSrt);
                    if (!useUserTimeZone) {
                        int timeZoneCorrection = SchedulerUtil.getTimeZoneDiffForDate(m18n.getTimezone(), planTimeZone, newEventdt);
                        newEventdt.add(14, -timeZoneCorrection);
                    }
                    executeProps.setProperty("eventdate", m18n.format(newEventdt));
                }
            }
            SchedulerAdminProcessor scheduler = new SchedulerAdminProcessor(this.getConnectionid());
            try {
                scheduler.executeEvent(executeProps);
                ++successCnt;
                status = true;
                continue;
            }
            catch (Exception e) {
                HashMap<String, String> transMap = new HashMap<String, String>();
                transMap.put("eventnumber", String.valueOf(i + 1));
                transMap.put("scheduleplanid", schedulePlanId);
                transMap.put("scheduleplanitemid", schedulePlanItemId);
                transMap.put("nofevents", String.valueOf(schedulePlanIds.length));
                String sql = "select scheduleplanitemdesc, schedulerule from scheduleplanitem where scheduleplanid = ? and scheduleplanitemid = ?";
                DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])new String[]{schedulePlanId, schedulePlanItemId});
                if (ds.getRowCount() == 1) {
                    transMap.put("scheduleplanitemdesc", ds.getValue(0, "scheduleplanitemdesc"));
                    transMap.put("schedulerule", ds.getValue(0, "schedulerule"));
                }
                Exception rootCause = (Exception)this.getCause(e);
                String error = "";
                if (rootCause != null && ((error = rootCause.getMessage()) == null || error.isEmpty())) {
                    int l = rootCause.getStackTrace().length;
                    if (l > 0) {
                        error = rootCause.getStackTrace()[0].getClassName();
                    }
                    error = error + ":" + rootCause.toString();
                }
                transMap.put("error", error);
                msg = msg + this.getTranslationProcessor().translate("Event [eventnumber] ([scheduleplanid], [scheduleplanitemdesc] / [schedulerule]) execution failed, Error: [error]\nSee logs for more information.", transMap);
                msg = msg + "\n\n";
            }
        }
        if (status) {
            HashMap<String, String> transMap = new HashMap<String, String>();
            transMap.put("successcnt", String.valueOf(successCnt));
            transMap.put("nofevents", String.valueOf(schedulePlanIds.length));
            msg = msg + this.getTranslationProcessor().translate("[successcnt]/[nofevents] event(s) executed successfully", transMap);
            msg = msg + "\n";
        }
        ar.addCallbackArgument("status", status);
        ar.addCallbackArgument("msg", msg);
        ar.addCallbackArgument("refresh", refresh);
        for (Map.Entry sdcSet : newKeyidMap.entrySet()) {
            StringBuilder keyid1s = new StringBuilder();
            String newSdcId = (String)sdcSet.getKey();
            ArrayList newKeyid1Arr = (ArrayList)sdcSet.getValue();
            for (int i = 0; i < newKeyid1Arr.size(); ++i) {
                keyid1s.append(";").append((String)newKeyid1Arr.get(i));
            }
            String newKeyids = keyid1s.toString();
            if (!newKeyids.isEmpty()) {
                newKeyids = newKeyids.substring(1);
            }
            ar.addCallbackArgument(this.getSDCProcessor().getProperty(newSdcId, "keycolid1"), newKeyids);
        }
        ar.print();
    }

    private Throwable getCause(Throwable e) {
        Throwable cause = null;
        Throwable result = e;
        while (null != (cause = result.getCause()) && result != cause) {
            result = cause;
        }
        return result;
    }
}

