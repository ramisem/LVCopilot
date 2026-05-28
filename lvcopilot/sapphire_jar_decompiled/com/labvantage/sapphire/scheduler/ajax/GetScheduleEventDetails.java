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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.HttpUtil;
import sapphire.util.M18NUtil;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class GetScheduleEventDetails
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest req, HttpServletResponse resp, ServletContext sc) throws ServletException {
        PropertyList taskEventProps;
        AjaxResponse ar = new AjaxResponse(req, resp);
        String schedulePlanId = ar.getRequestParameter("scheduleplanid", "");
        String schedulePlanItemId = ar.getRequestParameter("scheduleplanitemid", "");
        String eventDt = ar.getRequestParameter("eventdt", "");
        String eventNum = ar.getRequestParameter("eventnum", "");
        String eventStatus = ar.getRequestParameter("eventstatus", "");
        String taskEventPropsStr = ar.getRequestParameter("taskeventprops", "");
        String timezoneStr = ar.getRequestParameter("timezone", "");
        boolean stability = ar.getRequestParameter("stability", "N").equalsIgnoreCase("Y");
        boolean useUserTimeZone = ar.getRequestParameter("dateinusertimezone", "").startsWith("Y");
        try {
            taskEventProps = new PropertyList(new JSONObject(HttpUtil.decodeURIComponent(taskEventPropsStr)));
        }
        catch (JSONException e) {
            throw new ServletException("Could not read task event properties");
        }
        if (schedulePlanId.isEmpty()) {
            throw new IllegalArgumentException("Schedule Plan Id is missing");
        }
        if (schedulePlanItemId.isEmpty()) {
            throw new IllegalArgumentException("Schedule Plan Item Id is missing");
        }
        if (eventDt.isEmpty()) {
            throw new IllegalArgumentException("Event Date is missing");
        }
        if (eventNum.isEmpty()) {
            throw new IllegalArgumentException("Event Number is missing");
        }
        StringBuilder eventText = new StringBuilder();
        String sql = "select propertytreeid from scheduleplanitem where scheduleplanid = ? and scheduleplanitemid = ?";
        Object[] params = new String[]{schedulePlanId, schedulePlanItemId};
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, params);
        String task = "";
        if (ds.getRowCount() == 1) {
            task = ds.getValue(0, "propertytreeid");
        }
        String schedulePlanIdCol = "";
        String schedulePlanItemIdCol = "";
        String eventDateCol = "";
        String eventNumCol = "";
        String sdcid = "";
        String linkUrl = "";
        if (!task.isEmpty()) {
            PropertyListCollection collection = taskEventProps.getCollectionNotNull("taskevents");
            for (int i = 0; i < collection.size(); ++i) {
                String taskId;
                PropertyList taskProps = collection.getPropertyList(i);
                if (taskProps == null || !task.equals(taskId = taskProps.getProperty("taskid"))) continue;
                schedulePlanIdCol = taskProps.getProperty("scheduleplanidcol");
                schedulePlanItemIdCol = taskProps.getProperty("scheduleplanitemidcol");
                eventDateCol = taskProps.getProperty("eventdatecol");
                eventNumCol = taskProps.getProperty("eventnumcol");
                sdcid = taskProps.getProperty("sdcid");
                linkUrl = taskProps.getProperty("linkurl");
                break;
            }
        }
        if (!(schedulePlanIdCol.isEmpty() || schedulePlanItemIdCol.isEmpty() || eventDateCol.isEmpty() || sdcid.isEmpty() || !stability && eventNumCol.isEmpty())) {
            M18NUtil m18NTimezone;
            M18NUtil m18NUtil = new M18NUtil(this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()));
            TimeZone planTimeZone = timezoneStr.isEmpty() ? TimeZone.getDefault() : TimeZone.getTimeZone(timezoneStr);
            if (useUserTimeZone) {
                m18NTimezone = m18NUtil;
            } else {
                m18NTimezone = new M18NUtil(this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()));
                m18NTimezone.setTimeZone(planTimeZone);
            }
            Calendar eventCal = m18NTimezone.parseCalendar(eventDt);
            String queryEventDt = "";
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            queryEventDt = this.getConnectionProcessor().isOra() ? "to_date('" + sdf.format(eventCal.getTime()) + "','yyyy-mm-dd hh24:mi:ss') " : "cast('" + sdf.format(eventCal.getTime()) + "' as datetime) ";
            StringBuilder querywhere = new StringBuilder();
            boolean isOracle = this.getConnectionProcessor().isOra();
            querywhere.append(" ").append(schedulePlanIdCol).append(" = '").append(SafeSQL.encodeForSQL(schedulePlanId, isOracle)).append("' AND");
            querywhere.append(" ").append(schedulePlanItemIdCol).append(" = '").append(SafeSQL.encodeForSQL(schedulePlanItemId, isOracle)).append("' AND");
            querywhere.append(" ").append(eventDateCol).append(" = ").append(queryEventDt).append(" ");
            if (!eventNumCol.isEmpty()) {
                querywhere.append(" ").append(" AND ").append(eventNumCol).append(" = ").append(eventNum).append(" ");
            }
            String keyid1col = this.getSDCProcessor().getProperty(sdcid, "keycolid1");
            String keyid2col = this.getSDCProcessor().getProperty(sdcid, "keycolid2");
            String keyid3col = this.getSDCProcessor().getProperty(sdcid, "keycolid3");
            SDIRequest r = new SDIRequest();
            r.setSDCid(sdcid);
            r.setQueryFrom(this.getSDCProcessor().getProperty(sdcid, "tableid"));
            r.setQueryWhere(querywhere.toString());
            r.setRequestItem("primary");
            r.setRetainRsetid(true);
            SDIData sdi = this.getSDIProcessor().getSDIData(r);
            DataSet primary = sdi.getDataset("primary");
            for (int i = 0; i < primary.getRowCount(); ++i) {
                String keyid1 = primary.getValue(i, keyid1col);
                if (linkUrl.isEmpty()) {
                    eventText.append(keyid1);
                    eventText.append("<br/>");
                    continue;
                }
                String link = linkUrl.replaceAll("\\[keyid1\\]", keyid1);
                link = link.replaceAll("\\[keyid2\\]", primary.getValue(i, keyid2col));
                link = link.replaceAll("\\[keyid3\\]", primary.getValue(i, keyid3col));
                link = link.replaceAll("\\[sdcid\\]", sdcid);
                eventText.append("<a onClick=\"JavaScript:sapphire.lookup.open('").append(link).append("')\" >").append(keyid1).append("</a>");
                eventText.append("<br/>");
            }
            if (primary.getRowCount() == 0) {
                eventText.append(this.getTranslationProcessor().translate("No records found."));
            }
        }
        ar.addCallbackArgument("text", eventText.toString());
        ar.print();
    }
}

