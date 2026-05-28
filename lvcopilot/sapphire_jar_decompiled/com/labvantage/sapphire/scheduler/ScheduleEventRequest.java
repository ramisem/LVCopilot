/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 *  javax.servlet.http.HttpSession
 *  org.joda.time.DateTimeZone
 */
package com.labvantage.sapphire.scheduler;

import com.labvantage.sapphire.RequestParser;
import com.labvantage.sapphire.scheduler.SchedulerAdminProcessor;
import com.labvantage.sapphire.servlet.command.BaseRequest;
import java.io.PrintWriter;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.joda.time.DateTimeZone;
import org.json.JSONArray;
import org.json.JSONObject;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.DataSet;
import sapphire.util.M18NUtil;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class ScheduleEventRequest
extends BaseRequest {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        try {
            String endDateAsISO;
            Calendar endDt;
            String dateAsISO;
            SimpleDateFormat df;
            Calendar eventdt;
            SimpleDateFormat sdf;
            M18NUtil m18n = new M18NUtil(this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()));
            String localfromdate = request.getParameter("fromdate");
            String localtodate = request.getParameter("todate");
            String status = request.getParameter("status");
            String scheduleplanid = request.getParameter("scheduleplanid");
            String scheduleplanitemid = request.getParameter("scheduleplanitemid");
            String selectednodeid = request.getParameter("selectednodeid");
            String rsetId = request.getParameter("rsetid");
            String childnodes = request.getParameter("childnodes");
            String pageId = request.getParameter("page");
            String showdisabled = request.getParameter("showdisabled");
            String linksdcid = request.getParameter("linksdcid");
            String linkkeyid1 = request.getParameter("linkkeyid1");
            String stability = request.getParameter("stability");
            boolean isStability = stability.startsWith("Y");
            String tasktype = request.getParameter("tasktype");
            String studyidParam = request.getParameter("studyid");
            String studyid1 = request.getParameter("studyid1");
            String studyid2 = request.getParameter("studyid2");
            String studyid3 = request.getParameter("studyid3");
            String studyid4 = request.getParameter("studyid4");
            String activeOnly = request.getParameter("activeonly");
            String showexcludeid = request.getParameter("showexcludeid");
            if (linkkeyid1 == null) {
                linkkeyid1 = request.getParameter("keyid1");
            }
            String maxrowsstring = request.getParameter("maxrows");
            int maxrows = 1000;
            if (maxrowsstring != null && maxrowsstring.length() > 0) {
                try {
                    maxrows = Integer.parseInt(maxrowsstring);
                }
                catch (NumberFormatException numberFormatException) {
                    // empty catch block
                }
            }
            String datefrom = request.getParameter("from");
            String dateto = request.getParameter("to");
            String userTimeZoneStr = request.getParameter("dateinusertimezone");
            if (userTimeZoneStr == null) {
                userTimeZoneStr = "N";
            }
            if (scheduleplanitemid == null) {
                scheduleplanitemid = "";
            }
            if (scheduleplanid == null) {
                scheduleplanid = "";
            }
            if (selectednodeid == null) {
                selectednodeid = "";
            }
            if (selectednodeid.equals("root")) {
                selectednodeid = "";
            }
            if (childnodes == null) {
                childnodes = "Y";
            }
            if (showdisabled == null) {
                showdisabled = "N";
            }
            if (status == null) {
                status = "";
            }
            if (stability == null) {
                stability = "N";
            }
            if (studyidParam == null) {
                studyidParam = "";
            }
            if (studyid1 == null) {
                studyid1 = "";
            }
            if (studyid2 == null) {
                studyid2 = "";
            }
            if (studyid3 == null) {
                studyid3 = "";
            }
            if (studyid4 == null) {
                studyid4 = "";
            }
            if (showexcludeid == null) {
                showexcludeid = "";
            }
            if (tasktype == null || tasktype.length() == 0) {
                tasktype = "";
            }
            boolean userTimeZone = userTimeZoneStr.startsWith("Y");
            Calendar todate = null;
            Calendar fromdate = null;
            if (localfromdate != null && localfromdate.length() > 0) {
                fromdate = m18n.parseCalendar(localfromdate);
            } else if (datefrom != null && datefrom.length() > 0) {
                fromdate = Calendar.getInstance();
                sdf = new SimpleDateFormat("yyyy-MM-dd");
                fromdate.setTime(sdf.parse(datefrom));
                localfromdate = m18n.formatDateOnly(fromdate);
            } else {
                fromdate = Calendar.getInstance();
                fromdate.set(14, 0);
                fromdate.set(12, 0);
                fromdate.set(10, 0);
                fromdate.set(5, 1);
                localfromdate = m18n.formatDateOnly(fromdate);
            }
            if (localtodate != null && localtodate.length() > 0) {
                Calendar includetodate = Calendar.getInstance();
                includetodate.setTime(m18n.parseCalendar(localtodate).getTime());
                includetodate.add(5, 1);
                todate = includetodate;
            } else if (dateto != null && dateto.length() > 0) {
                todate = Calendar.getInstance();
                sdf = new SimpleDateFormat("yyyy-MM-dd");
                todate.setTime(sdf.parse(dateto));
                localtodate = m18n.formatDateOnly(todate);
            } else {
                todate = Calendar.getInstance();
                todate.set(14, 0);
                todate.set(12, 0);
                todate.set(10, 0);
                todate.set(5, fromdate.getActualMaximum(5));
                localtodate = m18n.formatDateOnly(todate);
            }
            String onlyExisting = "N";
            if (status.equals("D")) {
                onlyExisting = "Y";
            } else if (status.equals("S")) {
                onlyExisting = "Y";
            } else if (status.equals("E")) {
                onlyExisting = "Y";
            }
            if (stability.equals("Y")) {
                onlyExisting = "Y";
            }
            SchedulerAdminProcessor scheduler = new SchedulerAdminProcessor(this.getConnectionid());
            HttpSession session = request.getSession(true);
            PropertyList pageProps = (PropertyList)session.getAttribute(pageId);
            PropertyList properties = new PropertyList();
            properties.setProperty("scheduleplanid", scheduleplanid);
            properties.setProperty("schduleplanitemid", scheduleplanitemid);
            properties.setProperty("selectednodeid", selectednodeid);
            properties.setProperty("status", status);
            properties.setProperty("fromdate", localfromdate);
            properties.setProperty("todate", localtodate);
            properties.setProperty("showdisabled", showdisabled);
            properties.setProperty("onlyexisting", onlyExisting);
            properties.setProperty("childnodes", childnodes);
            properties.setProperty("calendarmode", "Y");
            properties.setProperty("dateinusertimezone", userTimeZoneStr);
            properties.setProperty("linksdcid", linksdcid);
            properties.setProperty("linkkeyid1", linkkeyid1);
            properties.setProperty("pageprops", pageProps);
            properties.setProperty("stability", stability);
            properties.setProperty("studyid", studyidParam);
            properties.setProperty("studyid1", studyid1);
            properties.setProperty("studyid2", studyid2);
            properties.setProperty("studyid3", studyid3);
            properties.setProperty("studyid4", studyid4);
            properties.setProperty("tasktype", tasktype);
            properties.setProperty("activeonly", activeOnly);
            properties.setProperty("mode", "getevents");
            DataSet eventsDs = scheduler.getEvents(properties);
            int totalrows = eventsDs.getRowCount();
            if (totalrows > maxrows) {
                eventsDs.sort("eventdt");
                for (int j = totalrows - 1; j >= maxrows; --j) {
                    eventsDs.deleteRow(j);
                }
            }
            JSONArray calendarEventsJSON = new JSONArray();
            PropertyListCollection columns = pageProps.getPropertyListNotNull("extracolprops").getCollectionNotNull("columns");
            ArrayList<String> schedulePlanIds = new ArrayList<String>();
            M18NUtil m18NTimezone = m18n;
            boolean l = false;
            for (int k = 0; k < eventsDs.getRowCount(); ++k) {
                TimeZone timeZone;
                String timeZoneStr = eventsDs.getString(k, "timezone", TimeZone.getDefault().getID());
                if (userTimeZone) {
                    m18NTimezone = m18n;
                    timeZone = m18n.getTimezone();
                } else {
                    timeZone = TimeZone.getTimeZone(timeZoneStr);
                    m18NTimezone = new M18NUtil(m18n.getLocale(), timeZone);
                }
                JSONObject event = new JSONObject();
                String eventId = eventsDs.getValue(k, "eventid");
                event.put("id", eventId);
                eventdt = eventsDs.getCalendar(k, "eventdt");
                df = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm");
                df.setTimeZone(timeZone);
                dateAsISO = df.format(new Date(eventdt.getTimeInMillis()));
                event.put("start_date", dateAsISO);
                event.put("start_date_str", dateAsISO);
                endDt = (Calendar)eventdt.clone();
                endDateAsISO = df.format(new Date(endDt.getTimeInMillis()));
                event.put("end_date", endDateAsISO);
                event.put("end_date_str", dateAsISO);
                String eventStatus = eventsDs.getValue(k, "eventstatus");
                String schedulePlanId = eventsDs.getValue(k, "scheduleplanid", "");
                if (!schedulePlanIds.contains(schedulePlanId)) {
                    schedulePlanIds.add(schedulePlanId);
                }
                String schedulePlanItemId = eventsDs.getValue(k, "scheduleplanitemid", "");
                String eventNum = eventsDs.getValue(k, "eventnum", "");
                String scheduleRule = eventsDs.getValue(k, "schedulerule", "");
                String adhocFlag = eventsDs.getValue(k, "adhocflag", "");
                String randomizationType = eventsDs.getString(k, "randomizationtype", "");
                String randomizationMode = eventsDs.getString(k, "randomizationmode", "");
                String randomizationCount = eventsDs.getValue(k, "randomizationcount", "");
                String scheduleTemplateSdcId = eventsDs.getString(k, "scheduletemplatesdcid", "");
                String scheduleTemplateKeyId1 = eventsDs.getString(k, "scheduletemplatekeyid1", "");
                String scheduleTemplateKeyId2 = eventsDs.getString(k, "scheduletemplatekeyid2", "");
                String scheduleTemplateKeyId3 = eventsDs.getString(k, "scheduletemplatekeyid3", "");
                String propertytreeid = eventsDs.getString(k, "propertytreeid", "");
                String studyidstr = eventsDs.getString(k, "studyid", "");
                String stabilitydepartmentid = eventsDs.getString(k, "stabilitydepartmentid", "");
                String qtypull = eventsDs.getValue(k, "qtypull", "");
                String qtypulltype = eventsDs.getString(k, "qtypulltype", "");
                String qtypullunits = eventsDs.getString(k, "qtypullunits", "");
                String conditionlabel = eventsDs.getString(k, "conditionlabel", "");
                String scheduletimerule = eventsDs.getString(k, "scheduletimerule", "");
                String text = "";
                if (isStability) {
                    text = studyidstr + ": " + conditionlabel + " " + scheduletimerule;
                }
                for (int m = 0; m < columns.size(); ++m) {
                    String[] s;
                    String value;
                    PropertyList columnProps = columns.getPropertyList(m);
                    String column = RequestParser.parseAlias(columnProps.getProperty("columnid"));
                    String location = columnProps.getProperty("calendarlocation", "Tooltip");
                    String link = columnProps.getProperty("link", "");
                    if (!location.equals("Header") && !location.equals("Header and Tooltip") || (value = eventsDs.getValue(k, column, "")).isEmpty()) continue;
                    if (link.isEmpty()) {
                        text = text + " " + value;
                        continue;
                    }
                    for (String str : s = StringUtil.getTokens(link)) {
                        if (!eventsDs.isValidColumn(str)) continue;
                        link = StringUtil.replaceAll(link, "[" + str + "]", eventsDs.getValue(k, str, ""));
                    }
                    text = text + "<a href=\"#\" onclick=\"sapphire.lookup.open('" + link + "')\" >  " + value + "</a>";
                }
                String itemDesc = eventsDs.getValue(k, "scheduleplanitemdesc", "");
                if (text.isEmpty()) {
                    text = itemDesc;
                }
                if (text.isEmpty()) {
                    text = schedulePlanId + " " + scheduleRule;
                }
                event.put("text", text);
                event.put("schedulerule", scheduleRule);
                event.put("scheduleplanitemdesc", itemDesc);
                event.put("eventnum", eventNum);
                event.put("eventdt", eventsDs.getValue(k, "eventdtstr"));
                event.put("executedt", eventsDs.getValue(k, "executedtstr"));
                event.put("originaleventdt", eventsDs.getValue(k, "originaleventdtstr"));
                event.put("propertytreeid", propertytreeid);
                Calendar originalEventDt = eventsDs.getCalendar(k, "originaleventdt");
                event.put("scheduleplanid", schedulePlanId);
                event.put("scheduleplanitemid", schedulePlanItemId);
                event.put("adhocflag", adhocFlag);
                event.put("eventstatus", eventStatus);
                event.put("eventstatus_str", this.translateEventStatus(this.getTranslationProcessor(), eventStatus));
                event.put("scheduletemplatesdcid", scheduleTemplateSdcId);
                event.put("scheduletemplatekeyid1", scheduleTemplateKeyId1);
                event.put("timezone", timeZoneStr);
                eventdt.setTimeZone(TimeZone.getTimeZone(timeZoneStr));
                event.put("timezone_short", DateTimeZone.forID((String)timeZoneStr).getShortName(eventdt.getTimeInMillis()));
                event.put("randomizationtype", randomizationType);
                event.put("randomizationmode", randomizationMode);
                event.put("randomizationcount", randomizationCount);
                event.put("studyid", studyidstr);
                event.put("conditionlabel", conditionlabel);
                event.put("scheduletimerule", scheduletimerule);
                event.put("qtypull", qtypull);
                event.put("qtypulltype", qtypulltype);
                event.put("qtypullunits", qtypullunits);
                event.put("windowstartdt", eventsDs.getValue(k, "windowstartdtstr"));
                event.put("windowenddt", eventsDs.getValue(k, "windowenddtstr"));
                event.put("summarymode", properties.getProperty("summarymode"));
                for (int m = 0; m < columns.size(); ++m) {
                    PropertyList columnProps = columns.getPropertyList(m);
                    String column = RequestParser.parseAlias(columnProps.getProperty("columnid"));
                    event.put(column, eventsDs.getValue(k, column, ""));
                }
                if (eventdt.compareTo(originalEventDt) != 0) {
                    event.put("movedflag", "Y");
                }
                if (eventStatus.equals("D")) {
                    event.put("readonly", "true");
                }
                String sectionIdStr = eventsDs.getValue(k, "scheduleplanid", "") + "|" + eventsDs.getValue(k, "scheduleplanitemid", "");
                event.put("section_planitem", sectionIdStr);
                String sectionIdSource = eventsDs.getValue(k, "linksdcid", "") + "|" + eventsDs.getValue(k, "linkkeyid1", "");
                event.put("section_source", sectionIdSource);
                String source = this.getTranslationProcessor().translate(eventsDs.getValue(k, "linksdcid", "")) + ": " + eventsDs.getValue(k, "linkkeyid1", "");
                event.put("source", source);
                calendarEventsJSON.put(event);
            }
            if (schedulePlanIds.size() == 1 && !userTimeZone || isStability) {
                properties = new PropertyList();
                if (isStability) {
                    properties.setProperty("calendarid", showexcludeid);
                } else {
                    properties.setProperty("scheduleplanid", (String)schedulePlanIds.get(0));
                }
                properties.setProperty("fromdate", localfromdate);
                properties.setProperty("todate", localtodate);
                DataSet excludeDs = scheduler.getExcludes(properties);
                if (excludeDs != null && excludeDs.getRowCount() > 0) {
                    for (int k = 0; k < excludeDs.getRowCount(); ++k) {
                        JSONObject eventJSON = new JSONObject();
                        String eventId = excludeDs.getValue(k, "eventid");
                        eventJSON.put("id", eventId);
                        boolean isAllDay = excludeDs.getValue(k, "allday", "").equals("true");
                        eventdt = excludeDs.getCalendar(k, "eventdt");
                        df = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm");
                        if (!isAllDay) {
                            df.setTimeZone(m18n.getTimezone());
                        }
                        dateAsISO = df.format(eventdt.getTime());
                        eventJSON.put("start_date", dateAsISO);
                        eventJSON.put("start_date_str", dateAsISO);
                        if (isAllDay) {
                            endDt = (Calendar)eventdt.clone();
                            endDt.add(5, 1);
                        } else {
                            endDt = excludeDs.getCalendar(k, "eventenddt");
                        }
                        endDateAsISO = df.format(endDt.getTime());
                        eventJSON.put("end_date", endDateAsISO);
                        eventJSON.put("end_date_str", endDateAsISO);
                        eventJSON.put("readonly", "true");
                        eventJSON.put("eventdt", m18n.format(eventdt));
                        eventJSON.put("eventenddt", m18n.format(endDt));
                        String timeZoneStr = excludeDs.getString(k, "timezone", TimeZone.getDefault().getID());
                        eventJSON.put("timezone", timeZoneStr);
                        eventJSON.put("timezone_short", DateTimeZone.forID((String)timeZoneStr).getShortName(eventdt.getTimeInMillis()));
                        eventJSON.put("text", excludeDs.getValue(k, "eventdesc", ""));
                        eventJSON.put("all_day", excludeDs.getValue(k, "allday", ""));
                        eventJSON.put("exclusion", "true");
                        eventJSON.put("textColor", "black");
                        eventJSON.put("color", "red");
                        calendarEventsJSON.put(eventJSON);
                    }
                }
            }
            String jsOn2 = calendarEventsJSON.toString();
            PrintWriter pw = response.getWriter();
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            response.setHeader("Pragma", "no-cache");
            response.setDateHeader("Expires", 0L);
            pw.println(jsOn2);
        }
        catch (Exception e) {
            this.logger.error(e.getMessage());
        }
    }

    private String translateEventStatus(TranslationProcessor tp, String eventStatus) {
        String text = "";
        if (eventStatus.equals("P")) {
            text = "Planned";
        } else if (eventStatus.equals("E")) {
            text = "Error";
        } else if (eventStatus.equals("D")) {
            text = "Done";
        } else if (eventStatus.equals("I")) {
            text = "Inactive";
        } else if (eventStatus.equals("S")) {
            text = "Scheduled";
        } else if (eventStatus.equals("F")) {
            text = "Flagged";
        }
        return tp.translate(text);
    }
}

