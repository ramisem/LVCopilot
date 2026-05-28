/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.Servlet
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.pageelements.maint;

import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.pageelements.maint.MaintCalendar;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.M18NUtil;
import sapphire.xml.PropertyList;

public class MaintCalendarAjaxHandler
extends BaseAjaxRequest {
    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        block27: {
            AjaxResponse ajaxresponse = new AjaxResponse(request, response);
            try {
                Mode mode = Mode.REFRESH;
                try {
                    mode = Mode.valueOf(ajaxresponse.getRequestParameter("mode", Mode.REFRESH.toString()).toUpperCase());
                }
                catch (Exception exception) {
                    // empty catch block
                }
                if (mode == Mode.REFRESH) {
                    String data;
                    String keyid1 = ajaxresponse.getRequestParameter("keyid1");
                    String keyid2 = ajaxresponse.getRequestParameter("keyid2");
                    String keyid3 = ajaxresponse.getRequestParameter("keyid3");
                    String sdcid = ajaxresponse.getRequestParameter("sdcid");
                    boolean viewonly = ajaxresponse.getRequestParameter("viewonly", "N").equalsIgnoreCase("Y");
                    PropertyList properties = null;
                    String props2 = ajaxresponse.getRequestParameter("properties");
                    if (props2.length() > 0) {
                        try {
                            properties = new PropertyList(new JSONObject(props2));
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                    }
                    DataSet calendardata = null;
                    if (!ajaxresponse.getRequestParameter("full").equalsIgnoreCase("Y") && (data = ajaxresponse.getRequestParameter("data")).length() > 0) {
                        try {
                            calendardata = new DataSet(new JSONObject(data));
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                    }
                    String elementid = ajaxresponse.getRequestParameter("elementid", "attachments");
                    MaintCalendar calendar = new MaintCalendar(ajaxresponse.getPageContext((Servlet)this.getServlet(), servletContext, request, response));
                    calendar.setElementid(elementid);
                    calendar.setElementProperties(properties);
                    calendar.setPrimary(sdcid, keyid1, keyid2, keyid3);
                    if (calendardata != null) {
                        calendar.setCalendarData(calendardata);
                    }
                    if (viewonly) {
                        calendar.setViewOnly(viewonly);
                    }
                    ajaxresponse.addCallbackArgument("elementid", elementid);
                    ajaxresponse.addCallbackArgument("html", calendar.getHtml());
                    ajaxresponse.addCallbackArgument("script", calendar.getScript());
                    if (ajaxresponse.getRequestParameter("full").equalsIgnoreCase("Y") && (calendardata = calendar.getCalendarData()) != null) {
                        ajaxresponse.addCallbackArgument("data", calendardata.toJSONString(true, true));
                    }
                    break block27;
                }
                if (mode == Mode.CREATE) {
                    String keyid1 = ajaxresponse.getRequestParameter("keyid1");
                    String keyid2 = ajaxresponse.getRequestParameter("keyid2");
                    String keyid3 = ajaxresponse.getRequestParameter("keyid3");
                    String sdcid = ajaxresponse.getRequestParameter("sdcid");
                    PropertyList properties = null;
                    String props = ajaxresponse.getRequestParameter("properties");
                    if (props.length() > 0) {
                        try {
                            properties = new PropertyList(new JSONObject(props));
                        }
                        catch (Exception props2) {
                            // empty catch block
                        }
                    }
                    String elementid = ajaxresponse.getRequestParameter("elementid", "attachments");
                    MaintCalendar calendar = new MaintCalendar(ajaxresponse.getPageContext((Servlet)this.getServlet(), servletContext, request, response));
                    calendar.setAjaxCreate(true);
                    calendar.setElementid(elementid);
                    calendar.setElementProperties(properties);
                    calendar.setPrimary(sdcid, keyid1, keyid2, keyid3);
                    JSONObject calendarElement = calendar.getCalendarElement();
                    String script = calendar.getScript();
                    ajaxresponse.addCallbackArgument("elementid", elementid);
                    ajaxresponse.addCallbackArgument("html", calendar.getHtml());
                    ajaxresponse.addCallbackArgument("script", script);
                    ajaxresponse.addCallbackArgument("fileElement", calendarElement.toString());
                    ajaxresponse.addCallbackArgument("properties", calendar.getElementProperties().toJSONString());
                    DataSet calendarData = calendar.getCalendarData();
                    if (calendarData != null) {
                        ajaxresponse.addCallbackArgument("data", calendarData.toJSONString(true, true));
                    }
                    break block27;
                }
                if (mode == Mode.PARSEDATE) {
                    SimpleDateFormat sdf;
                    String enteredDate = ajaxresponse.getRequestParameter("enteredDate");
                    String dateFormat = ajaxresponse.getRequestParameter("format");
                    DateTimeUtil dtu = new DateTimeUtil();
                    String result = "Fail";
                    if (dateFormat.indexOf("yy") == -1) {
                        String[] dateFormatArr = dateFormat.split(" ");
                        sdf = (SimpleDateFormat)dtu.getDateFormatFromSMLString(dateFormatArr[0]);
                    } else {
                        if (dateFormat.indexOf("yy") >= 0) {
                            dateFormat = dateFormat.substring(0, dateFormat.lastIndexOf("y") + 1);
                        }
                        sdf = new SimpleDateFormat();
                        sdf.applyPattern(dateFormat);
                    }
                    try {
                        M18NUtil m18NUtil = new M18NUtil(this.getConnectionProcessor().getConnectionInfo(this.getConnectionId()));
                        Calendar cal1 = m18NUtil.parseCalendar(enteredDate, true);
                        String tempDateString = sdf.format(cal1.getTime());
                        sdf.setTimeZone(m18NUtil.getTimezone());
                        Date tempDate = sdf.parse(tempDateString);
                        GregorianCalendar cal2 = new GregorianCalendar();
                        cal2.setTime(tempDate);
                        if (cal1.get(1) == cal2.get(1)) {
                            result = "Pass";
                        }
                        ajaxresponse.addCallbackArgument("Result", result);
                    }
                    catch (Exception e) {
                        result = "Fail";
                        ajaxresponse.addCallbackArgument("Result", result);
                    }
                }
            }
            finally {
                ajaxresponse.print();
            }
        }
    }

    private static enum Mode {
        REFRESH,
        CREATE,
        PARSEDATE;

    }
}

