/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.Servlet
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.modules.wap.workhours;

import com.labvantage.sapphire.modules.wap.calendar.CalendarFactory;
import com.labvantage.sapphire.modules.wap.calendar.LVCalendar;
import com.labvantage.sapphire.modules.wap.workhours.GetWorkHours;
import com.labvantage.sapphire.modules.wap.workhours.WorkHours;
import com.labvantage.sapphire.modules.wap.workhours.WorkHoursPage;
import java.util.TimeZone;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;
import org.json.JSONObject;
import sapphire.accessor.ConnectionProcessor;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.ConnectionInfo;

public class WorkHoursAjaxHandler
extends BaseAjaxRequest {
    private void doRefresh(AjaxResponse ajaxresponse, PageContext pageContext, LVCalendar calendar) {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        block29: {
            AjaxResponse ajaxresponse = new AjaxResponse(request, response);
            ConnectionProcessor cp = this.getConnectionProcessor();
            ConnectionInfo info = cp.getConnectionInfo(cp.getConnectionid());
            try {
                Mode mode = Mode.REFRESH;
                try {
                    mode = Mode.valueOf(ajaxresponse.getRequestParameter("mode", mode.toString()).toUpperCase());
                }
                catch (Exception exception) {
                    // empty catch block
                }
                WorkHoursPage.Mode pagemode = WorkHoursPage.Mode.USER;
                try {
                    pagemode = WorkHoursPage.Mode.valueOf(ajaxresponse.getRequestParameter("pagemode", pagemode.toString()).toUpperCase());
                }
                catch (Exception exception) {
                    // empty catch block
                }
                String calendarid = ajaxresponse.getRequestParameter("calendarid");
                CalendarFactory factory = new CalendarFactory(this.getConnectionid());
                LVCalendar calendar = null;
                if (pagemode == WorkHoursPage.Mode.ADMIN) {
                    Object[] details;
                    String sdcid = ajaxresponse.getRequestParameter("sdcid");
                    String keyid1 = ajaxresponse.getRequestParameter("keyid1");
                    TimeZone timeZone = TimeZone.getDefault();
                    if (sdcid.equalsIgnoreCase("user")) {
                        details = CalendarFactory.getUserCalendarDetails(this.getQueryProcessor(), keyid1);
                        calendarid = (String)details[0];
                        timeZone = (TimeZone)details[1];
                    } else if (sdcid.equalsIgnoreCase("instrument")) {
                        details = CalendarFactory.getInstrumentCalendarDetails(this.getQueryProcessor(), keyid1);
                        calendarid = (String)details[0];
                        timeZone = (TimeZone)details[1];
                    } else if (sdcid.equalsIgnoreCase("department")) {
                        if (ajaxresponse.getRequestParameter("shiftid").length() > 0) {
                            details = CalendarFactory.getShiftCalendarDetails(this.getQueryProcessor(), keyid1, ajaxresponse.getRequestParameter("shiftid"));
                            calendarid = (String)details[0];
                            timeZone = (TimeZone)details[1];
                        } else {
                            details = CalendarFactory.getDepartmentCalendarDetails(this.getQueryProcessor(), keyid1);
                            calendarid = (String)details[0];
                            timeZone = (TimeZone)details[1];
                        }
                    }
                    calendar = factory.getCalendar(calendarid, timeZone.toZoneId());
                } else if (pagemode == WorkHoursPage.Mode.USER) {
                    calendar = factory.getUserCalendar(this.getConnectionProcessor().getSapphireConnection().getSysuserId(), true, true);
                }
                if (calendar != null) {
                    if (mode == Mode.REFRESH) {
                        WorkHoursPage workHoursPage = new WorkHoursPage(ajaxresponse.getPageContext((Servlet)this.getServlet(), servletContext, request, response));
                        workHoursPage.setMode(pagemode);
                        if (pagemode == WorkHoursPage.Mode.ADMIN) {
                            workHoursPage.setSDCId(ajaxresponse.getRequestParameter("sdcid"));
                            workHoursPage.setKeyId1(ajaxresponse.getRequestParameter("keyid1"));
                            workHoursPage.setShiftId(ajaxresponse.getRequestParameter("shiftid"));
                        }
                        workHoursPage.setUpData();
                        boolean override = ajaxresponse.getRequestParameter("override", "N").equalsIgnoreCase("Y");
                        if (override) {
                            workHoursPage.setOverride(WorkHours.OverrideType.OVERRIDE);
                        } else {
                            workHoursPage.setOverride(WorkHours.OverrideType.INHERIT);
                        }
                        String html = workHoursPage.getWorkHoursHTML();
                        String data = workHoursPage.getWorkHours() != null ? workHoursPage.getWorkHours().toJSONString() : "{}";
                        ajaxresponse.addCallbackArgument("html", html);
                        ajaxresponse.addCallbackArgument("data", data);
                        break block29;
                    }
                    if (mode != Mode.UPDATE) break block29;
                    String wh = ajaxresponse.getRequestParameter("workhours");
                    if (wh.length() > 0) {
                        try {
                            WorkHours workHours = WorkHours.getInstance(wh, calendar.getTimeZone());
                            calendar.setCoreHours(workHours);
                            try {
                                calendar.saveCalendar();
                                JSONObject job = new JSONObject();
                                job.put("coreHours", GetWorkHours.getShifts(workHours));
                                job.put("coreHourJson", wh);
                                job.put("shiftid", ajaxresponse.getRequestParameter("shiftid"));
                                ajaxresponse.addCallbackArgument("retvalue", job.toString());
                                break block29;
                            }
                            catch (Exception e2) {
                                ajaxresponse.setError("Failed to save workhours.");
                            }
                        }
                        catch (Exception e) {
                            ajaxresponse.setError("Failed to load workhours definition.");
                        }
                        break block29;
                    }
                    ajaxresponse.setError("No workhour data provided.");
                    break block29;
                }
                this.logger.error("Unable to obtain Calendar");
                ajaxresponse.setError("Unable to obtain Calendar");
            }
            catch (Exception e) {
                this.logger.error("Unable to execute WorkHours command", e);
                ajaxresponse.setError("Unable to execute WorkHours command");
            }
            finally {
                ajaxresponse.print();
            }
        }
    }

    public static enum Mode {
        UPDATE,
        REFRESH;

    }
}

