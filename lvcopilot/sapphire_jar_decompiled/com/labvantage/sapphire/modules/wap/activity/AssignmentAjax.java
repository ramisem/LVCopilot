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
package com.labvantage.sapphire.modules.wap.activity;

import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.EncryptDecrypt;
import com.labvantage.sapphire.I18nUtil;
import com.labvantage.sapphire.actions.wap.DeleteActivity;
import com.labvantage.sapphire.actions.wap.SetActivityResource;
import com.labvantage.sapphire.actions.wap.SetActivityStatus;
import com.labvantage.sapphire.modules.wap.CalendarConverter;
import com.labvantage.sapphire.modules.wap.activity.ActivityClassHandler;
import com.labvantage.sapphire.modules.wap.activity.AssignmentPage;
import com.labvantage.sapphire.modules.wap.activity.AssignmentPageResourceContainer;
import com.labvantage.sapphire.modules.wap.activity.AssignmentPageResourceData;
import com.labvantage.sapphire.modules.wap.activity.AssignmentPageUtil;
import com.labvantage.sapphire.modules.wap.activity.WAPAvailability;
import com.labvantage.sapphire.modules.wap.activity.WAPAvailabilityOptions;
import com.labvantage.sapphire.modules.wap.activity.WAPAvailabilitySelector;
import com.labvantage.sapphire.modules.wap.calendar.CalendarPage;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.TranslationProcessor;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.servlet.RequestContext;
import sapphire.util.Browser;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.util.M18NUtil;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class AssignmentAjax
extends BaseAjaxRequest {
    private M18NUtil m18;
    ZoneId displayTimeZone = ZoneOffset.UTC;
    CalendarConverter calendarConverter;
    int clientOffsetMinutes = 0;
    AssignmentPage.CalendarView calendarView;
    AssignmentPage.ViewMode viewMode;
    AssignmentPage.OperatingMode operatingMode;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        try {
            Mode mode;
            AssignmentPage assignmentPage = new AssignmentPage(ajaxResponse.getPageContext((Servlet)this.getServlet(), servletContext, request, response));
            ConnectionInfo connectionInfo = this.getConnectionProcessor().getConnectionInfo(this.getConnectionid());
            this.m18 = new M18NUtil(connectionInfo);
            DateTimeUtil dtu = new DateTimeUtil(connectionInfo);
            this.calendarConverter = new CalendarConverter(dtu);
            PropertyList elementp = new PropertyList();
            try {
                PropertyList propertyList = new PropertyList(new JSONObject(ajaxResponse.getRequestParameter("element")));
                elementp.setProperty("pagedata", propertyList);
            }
            catch (Exception e) {
                elementp.setProperty("pagedata", new PropertyList());
            }
            assignmentPage.setElementProperties(elementp);
            try {
                mode = Mode.valueOf(ajaxResponse.getRequestParameter("mode").toUpperCase());
            }
            catch (Exception e) {
                mode = Mode.VIEW;
            }
            String displayTZ = ajaxResponse.getRequestParameter("timezone");
            if (displayTZ.length() > 0) {
                try {
                    try {
                        this.displayTimeZone = TimeZone.getTimeZone(displayTZ).toZoneId();
                    }
                    catch (Exception e1) {
                        this.displayTimeZone = I18nUtil.getZoneIdFromString(displayTZ);
                    }
                }
                catch (Exception e) {
                    this.logger.error("Failed to find timezone", e);
                }
            } else if (connectionInfo.getTimeZone() != null && connectionInfo.getTimeZone().length() > 0) {
                try {
                    try {
                        this.displayTimeZone = TimeZone.getTimeZone(connectionInfo.getTimeZone()).toZoneId();
                    }
                    catch (Exception e1) {
                        this.displayTimeZone = I18nUtil.getZoneIdFromString(displayTZ);
                    }
                }
                catch (Exception e) {
                    this.logger.error("Failed to find timezone", e);
                }
            } else {
                this.displayTimeZone = TimeZone.getDefault().toZoneId();
            }
            try {
                this.clientOffsetMinutes = Integer.parseInt(ajaxResponse.getRequestParameter("timezoneoffset"));
            }
            catch (Exception e) {
                // empty catch block
            }
            String userConfigPrefix = ajaxResponse.getRequestParameter("userconfigprefix");
            if (userConfigPrefix.length() > 0) {
                assignmentPage.setUserConfigPrefix(userConfigPrefix);
            }
            if (ajaxResponse.getRequestParameter("mywork").length() > 0 && ajaxResponse.getRequestParameter("mywork").equalsIgnoreCase("Y")) {
                assignmentPage.setMyWork(true);
            }
            try {
                this.operatingMode = AssignmentPage.OperatingMode.valueOf(ajaxResponse.getRequestParameter("operatingmode").toUpperCase());
            }
            catch (Exception e) {
                this.operatingMode = AssignmentPage.OperatingMode.ASSIGNMENT;
            }
            assignmentPage.setOperatingMode(this.operatingMode);
            if (ajaxResponse.getRequestParameter("view").length() == 0) {
                this.viewMode = assignmentPage.getViewMode();
            } else {
                try {
                    this.viewMode = AssignmentPage.ViewMode.valueOf(ajaxResponse.getRequestParameter("view").toUpperCase());
                }
                catch (Exception e) {
                    this.viewMode = AssignmentPage.ViewMode.RESOURCE;
                }
            }
            if (ajaxResponse.getRequestParameter("calendarview").length() > 0) {
                try {
                    this.calendarView = AssignmentPage.CalendarView.valueOf(ajaxResponse.getRequestParameter("calendarview").toUpperCase());
                }
                catch (Exception e) {
                    this.calendarView = AssignmentPage.CalendarView.MONTH;
                }
            } else {
                this.calendarView = assignmentPage.getCalendarView();
            }
            if (this.viewMode == AssignmentPage.ViewMode.RESOURCE && this.calendarView.isTimeline()) {
                this.calendarView = AssignmentPage.CalendarView.MONTH;
            }
            assignmentPage.setTimeZone(this.displayTimeZone);
            assignmentPage.setClientOffsetMinutes(this.clientOffsetMinutes);
            assignmentPage.setCalendarView(this.calendarView);
            assignmentPage.setViewMode(this.viewMode);
            if (mode == Mode.VIEW) {
                this.handleModeView(ajaxResponse, assignmentPage);
            } else if (mode == Mode.SELECTION) {
                this.handleModeSelection(ajaxResponse, assignmentPage);
            } else if (mode == Mode.EVENTCOLORSKEY) {
                this.handleModeEventColorsKey(request, ajaxResponse, elementp, userConfigPrefix);
            } else if (mode == Mode.RESOURCESECTION) {
                this.handleModeResourceSection(ajaxResponse, elementp);
            } else if (mode == Mode.SELECTORS) {
                this.handleModeSelectors(request, response, servletContext, ajaxResponse, assignmentPage, userConfigPrefix);
            } else if (mode == Mode.LIST) {
                this.handleModeList(ajaxResponse, assignmentPage);
            } else if (mode == Mode.RESOURCES) {
                this.handleModeResources(ajaxResponse, assignmentPage);
            } else if (mode == Mode.REFRESHRSOURCE) {
                this.handleModeRefreshSource(ajaxResponse);
            } else if (mode == Mode.TIME) {
                this.handleModeTime(ajaxResponse, assignmentPage);
            } else if (mode == Mode.ASSIGNDIALOG) {
                this.handleModeAssignDialog(ajaxResponse, assignmentPage);
            } else if (mode == Mode.ASSIGN) {
                this.handleModeAssign(ajaxResponse, assignmentPage);
            } else if (mode == Mode.MOVE) {
                this.handleModeMove(ajaxResponse);
            } else if (mode == Mode.FIXEDTIME) {
                this.handleModeFixedTime(ajaxResponse);
            } else if (mode == Mode.OPERATIONSMENU) {
                this.handleModeOperationsMenu(ajaxResponse, assignmentPage);
            } else if (mode == Mode.WORKLOADINFO) {
                this.handleModeWorkloadInfo(request, ajaxResponse, dtu);
            } else if (mode == Mode.ACTIVATE || mode == Mode.UNACTIVATE || mode == Mode.DELETE || mode == Mode.UNDO || mode == Mode.CANCEL || mode == Mode.START || mode == Mode.STOP || mode == Mode.RESETRESOURCES) {
                this.handleModeActivityOperations(ajaxResponse, mode);
            }
        }
        catch (SapphireException e) {
            this.logger.error(e.getMessage(), e);
        }
        finally {
            ajaxResponse.print();
        }
    }

    private void handleModeView(AjaxResponse ajaxResponse, AssignmentPage assignmentPage) throws SapphireException {
        String focus;
        String shiftid;
        boolean fullContent;
        boolean bl = fullContent = !ajaxResponse.getRequestParameter("fullcontent", "Y").equalsIgnoreCase("N");
        if (this.calendarView == AssignmentPage.CalendarView.AGENDA) {
            AssignmentPage.AssignmentPageTimeRange timeRange = new AssignmentPage.AssignmentPageTimeRange(ZonedDateTime.now(this.displayTimeZone), ZonedDateTime.now(this.displayTimeZone));
            AssignmentPageUtil.adjustRangeToCalendarView(this.calendarView, timeRange, this.m18);
            assignmentPage.setClientDateRange(timeRange);
        } else {
            JSONObject selection = null;
            try {
                selection = new JSONObject(ajaxResponse.getRequestParameter("selection"));
            }
            catch (Exception exception) {
                // empty catch block
            }
            if (selection != null) {
                assignmentPage.setSelection(selection);
            }
            AssignmentPage.AssignmentPageTimeRange clientTimeRange = this.getClientTimeRange(ajaxResponse);
            AssignmentPageUtil.adjustRangeToCalendarView(this.calendarView, clientTimeRange, true, this.m18);
            assignmentPage.setClientDateRange(clientTimeRange);
        }
        String departmentid = ajaxResponse.getRequestParameter("departmentid");
        if (departmentid.length() > 0) {
            assignmentPage.setDepartmentId(departmentid);
        }
        if ((shiftid = ajaxResponse.getRequestParameter("shiftid")).length() > 0) {
            assignmentPage.setShiftId(shiftid);
        }
        if (assignmentPage.getOperatingMode() == AssignmentPage.OperatingMode.ASSIGNMENT) {
            String testinglabtype = AssignmentPageUtil.getTestingLabType(departmentid, this.getQueryProcessor());
            assignmentPage.setActivityClassHandler(ajaxResponse.getRequestParameter("activityclass"), ajaxResponse.getRequestParameter("sdcid"), departmentid, testinglabtype);
            assignmentPage.setFocusId(ajaxResponse.getRequestParameter("focusid"));
        } else {
            assignmentPage.setActivityClassHandler("__LV_Activity", "Unassigned Actvities");
            assignmentPage.setFocusId(ajaxResponse.getRequestParameter("focusid"));
        }
        if (assignmentPage.getOperatingMode() == AssignmentPage.OperatingMode.ASSIGNMENT) {
            assignmentPage.setFullContent(fullContent);
            if (fullContent) {
                ajaxResponse.addCallbackArgument("viewMode", this.viewMode.toString());
            }
            try {
                String rs = ajaxResponse.getRequestParameter("resourcedata", "");
                if (rs.length() > 0) {
                    AssignmentPageResourceData resourceData = new AssignmentPageResourceData(new JSONObject(rs));
                    assignmentPage.setResourceData(resourceData);
                }
            }
            catch (Exception e) {
                this.logError(e.getMessage(), e);
            }
            AssignmentPageResourceContainer focus2 = AssignmentPageUtil.getFocusedResource(ajaxResponse.getRequestParameter("focusedresource"), assignmentPage.getResourceData());
            if (focus2 == null && fullContent && assignmentPage.getResourceData() != null && assignmentPage.getResourceData().getResources() != null && assignmentPage.getResourceData().getResources().size() > 0) {
                focus2 = assignmentPage.getResourceData().getResources().get(0);
            }
            assignmentPage.setFocusedResource(focus2);
        } else {
            assignmentPage.setFullContent(false);
            if (fullContent) {
                assignmentPage.loadProperties();
            } else {
                AssignmentPage.ResourceSDC resourcesdc = AssignmentPage.ResourceSDC.USER;
                try {
                    resourcesdc = AssignmentPage.ResourceSDC.valueOf(ajaxResponse.getRequestParameter("resourcesdc").toUpperCase());
                }
                catch (Exception resourceData) {
                    // empty catch block
                }
                assignmentPage.setResourceSDC(resourcesdc);
                assignmentPage.setSDCId(ajaxResponse.getRequestParameter("sdcid").length() > 0 ? ajaxResponse.getRequestParameter("sdcid") : resourcesdc.getName());
                assignmentPage.setKeyId1(ajaxResponse.getRequestParameter("keyid1"));
                String s = ajaxResponse.getRequestParameter("activityquery");
                if (EncryptDecrypt.isObfuscating()) {
                    if (EncryptDecrypt.isObfuscated(s)) {
                        assignmentPage.setActivityQuery(EncryptDecrypt.unobfsql(s));
                    } else {
                        this.logWarn("Invalid SQL sent through request.");
                    }
                } else {
                    assignmentPage.setActivityQuery(s);
                }
            }
            assignmentPage.generateResources();
        }
        ajaxResponse.addCallbackArgument("html", this.viewMode.getHTML(assignmentPage));
        ajaxResponse.addCallbackArgument("script", assignmentPage.getScript());
        JSONObject meta = new JSONObject();
        JSONArray jay = assignmentPage.getEventData(meta, this.m18);
        ajaxResponse.addCallbackArgument("events", jay != null ? jay.toString() : "");
        ajaxResponse.addCallbackArgument("data", meta != null ? meta.toString() : "");
        ajaxResponse.addCallbackArgument("focusedresource", assignmentPage.getFocusedResource() != null ? assignmentPage.getFocusedResource().getId() : "");
        ajaxResponse.addCallbackArgument("focusedresourcesdc", assignmentPage.getFocusedResource() != null ? assignmentPage.getFocusedResource().getResourceSDC().toString() : "");
        TimeZone timeZone = TimeZone.getDefault();
        if (assignmentPage.getFocusedResource() != null && (focus = assignmentPage.getFocusId()).length() > 0) {
            timeZone = assignmentPage.getFocusedResource().getTimeZone(focus);
        }
        ajaxResponse.addCallbackArgument("focusedresourcetimezone", timeZone.getID());
        if (this.operatingMode == AssignmentPage.OperatingMode.WORK) {
            ajaxResponse.addCallbackArgument("resourcedata", assignmentPage.getResourceData().toJSONString());
            if (fullContent) {
                ajaxResponse.addCallbackArgument("sdcid", assignmentPage.getSDCId());
                ajaxResponse.addCallbackArgument("keyid1", assignmentPage.getKeyId1());
            }
        } else {
            ajaxResponse.addCallbackArgument("resourcedata", "");
        }
    }

    private void handleModeSelection(AjaxResponse ajaxResponse, AssignmentPage assignmentPage) {
        AssignmentPage.AssignmentPageTimeRange clientTimeRange = this.getClientTimeRange(ajaxResponse);
        assignmentPage.setClientDateRange(clientTimeRange);
        JSONObject selection = null;
        try {
            selection = new JSONObject(ajaxResponse.getRequestParameter("selection"));
            AssignmentAjax.convertDateSelectionFromClient(selection, this.displayTimeZone, this.clientOffsetMinutes);
        }
        catch (Exception exception) {
            // empty catch block
        }
        String departmentid = ajaxResponse.getRequestParameter("departmentid");
        String testinglabtype = AssignmentPageUtil.getTestingLabType(departmentid, this.getQueryProcessor());
        try {
            assignmentPage.setActivityClassHandler(ajaxResponse.getRequestParameter("activityclass"), ajaxResponse.getRequestParameter("sdcid"), departmentid, testinglabtype);
        }
        catch (SapphireException e) {
            this.logger.info("Could not find activity class handler for activity class '" + ajaxResponse.getRequestParameter("activityclass") + "' or sdcid '" + ajaxResponse.getRequestParameter("sdcid") + "'.");
        }
        assignmentPage.setSelection(selection);
        try {
            String rs = ajaxResponse.getRequestParameter("resourcedata", "");
            if (rs.length() > 0) {
                AssignmentPageResourceData resourceData = new AssignmentPageResourceData(new JSONObject(rs));
                assignmentPage.setResourceData(resourceData);
            }
        }
        catch (Exception e) {
            this.logError(e.getMessage(), e);
        }
        if (assignmentPage.getResourceData() != null) {
            assignmentPage.setFocusedResource(AssignmentPageUtil.getFocusedResource(ajaxResponse.getRequestParameter("focusedresource"), assignmentPage.getResourceData()));
        }
        ajaxResponse.addCallbackArgument("html", assignmentPage.getSelectionIcons(false, true, !ajaxResponse.getRequestParameter("iconsonly", "N").equalsIgnoreCase("Y")));
        ajaxResponse.addCallbackArgument("selectionchanged", ajaxResponse.getRequestParameter("selectionchanged", "Y"));
    }

    private void handleModeEventColorsKey(HttpServletRequest request, AjaxResponse ajaxResponse, PropertyList elementp, String userConfigPrefix) {
        String colorByOp;
        AssignmentPageResourceContainer focusedResource = null;
        try {
            AssignmentPageResourceData resourceData;
            String rs = ajaxResponse.getRequestParameter("resourcedata", "");
            if (rs.length() > 0 && (resourceData = new AssignmentPageResourceData(new JSONObject(rs))) != null) {
                focusedResource = AssignmentPageUtil.getFocusedResource(ajaxResponse.getRequestParameter("focusedresource"), resourceData);
            }
        }
        catch (Exception e) {
            this.logError(e.getMessage(), e);
        }
        PropertyList userConfig = RequestContext.getRequestContext(request).getPropertyList("userconfig");
        String string = colorByOp = userConfig != null ? userConfig.getProperty(userConfigPrefix + "sidebar_colorby") : "";
        if (colorByOp.length() == 0) {
            colorByOp = elementp != null && elementp.getPropertyList("pagedata") != null && elementp.getPropertyList("pagedata").getPropertyList("displayoptions") != null ? elementp.getPropertyList("pagedata").getPropertyList("displayoptions").getProperty("colorby", "Resource") : "Resource";
        }
        ajaxResponse.addCallbackArgument("html", AssignmentPageUtil.getEventColorsKey(colorByOp, focusedResource, this.getTranslationProcessor()));
    }

    private void handleModeResourceSection(AjaxResponse ajaxResponse, PropertyList elementp) {
        String html = "";
        try {
            AssignmentPageResourceData resourceData;
            String rs = ajaxResponse.getRequestParameter("resourcedata", "");
            if (rs.length() > 0 && (resourceData = new AssignmentPageResourceData(new JSONObject(rs))) != null) {
                AssignmentPageResourceContainer focusedResource = AssignmentPageUtil.getFocusedResource(ajaxResponse.getRequestParameter("focusedresource"), resourceData);
                html = AssignmentPageUtil.getResourceSidebarSection(focusedResource, elementp, this.getTranslationProcessor(), this.getQueryProcessor());
            }
        }
        catch (Exception e) {
            this.logError(e.getMessage(), e);
        }
        ajaxResponse.addCallbackArgument("html", html);
    }

    private void handleModeSelectors(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext, AjaxResponse ajaxResponse, AssignmentPage assignmentPage, String userConfigPrefix) {
        String departmentid = ajaxResponse.getRequestParameter("departmentid");
        String shiftid = ajaxResponse.getRequestParameter("shiftid");
        String focusid = ajaxResponse.getRequestParameter("focusid");
        AssignmentPage.AssignmentPageTimeRange clientTimeRange = this.getClientTimeRange(ajaxResponse);
        JSONObject selection = null;
        try {
            selection = new JSONObject(ajaxResponse.getRequestParameter("selection"));
        }
        catch (Exception exception) {
            // empty catch block
        }
        AssignmentPageResourceData resourceData = null;
        try {
            String rs = ajaxResponse.getRequestParameter("resourcedata", "");
            if (rs.length() > 0) {
                resourceData = new AssignmentPageResourceData(new JSONObject(rs));
            }
        }
        catch (Exception e) {
            this.logError(e.getMessage(), e);
        }
        AssignmentPageResourceContainer focusedResource = null;
        if (resourceData != null) {
            focusedResource = AssignmentPageUtil.getFocusedResource(ajaxResponse.getRequestParameter("focusedresource"), resourceData);
        }
        String selectedSDC = "";
        try {
            selectedSDC = selection.getString(focusedResource.getId() + "_sdcid");
        }
        catch (Exception exception) {
            // empty catch block
        }
        PropertyList userConfig = RequestContext.getRequestContext(request).getPropertyList("userconfig");
        String html = AssignmentPageUtil.getAreaSelector(this.viewMode, clientTimeRange.from, clientTimeRange.to, this.displayTimeZone, departmentid, shiftid, focusedResource, resourceData, focusid, focusedResource != null ? AssignmentPageUtil.getSelectedResource(focusedResource.getId(), selection) : null, selectedSDC, ajaxResponse.getRequestParameter("sdcid"), userConfig, userConfigPrefix, assignmentPage.getElementProperties(), this.operatingMode, assignmentPage.getColorScheme(), this.getSDIProcessor(), this.getTranslationProcessor(), this.getConnectionProcessor().getSapphireConnection(), ajaxResponse.getPageContext((Servlet)this.getServlet(), servletContext, request, response), this.displayTimeZone);
        ajaxResponse.addCallbackArgument("html", html);
    }

    private AssignmentPage.AssignmentPageTimeRange getClientTimeRange(AjaxResponse ajaxResponse) {
        String from = ajaxResponse.getRequestParameter("from");
        String to = ajaxResponse.getRequestParameter("to");
        String date = ajaxResponse.getRequestParameter("date");
        if (from.length() > 0 && to.length() > 0) {
            ZonedDateTime clientFrom = CalendarConverter.getZonedDateTimeFromClientValue(ajaxResponse.getRequestParameter("from"), this.displayTimeZone, this.clientOffsetMinutes);
            ZonedDateTime clientTo = CalendarConverter.getZonedDateTimeFromClientValue(ajaxResponse.getRequestParameter("to"), this.displayTimeZone, this.clientOffsetMinutes);
            return new AssignmentPage.AssignmentPageTimeRange(clientFrom, clientTo);
        }
        if (date.length() > 0) {
            ZonedDateTime zonedDate = CalendarConverter.getZonedDateTimeFromClientValue(ajaxResponse.getRequestParameter("date"), this.displayTimeZone, this.clientOffsetMinutes);
            AssignmentPage.AssignmentPageTimeRange timeRange = new AssignmentPage.AssignmentPageTimeRange(zonedDate, zonedDate);
            AssignmentPageUtil.adjustRangeToCalendarView(this.calendarView, timeRange, this.m18);
            return timeRange;
        }
        ZonedDateTime now = ZonedDateTime.now(this.displayTimeZone);
        return new AssignmentPage.AssignmentPageTimeRange(now, now);
    }

    private void handleModeList(AjaxResponse ajaxResponse, AssignmentPage assignmentPage) throws SapphireException {
        assignmentPage.loadProperties();
        String departmentid = ajaxResponse.getRequestParameter("departmentid");
        String testinglabtype = AssignmentPageUtil.getTestingLabType(departmentid, this.getQueryProcessor());
        assignmentPage.setActivityClassHandler(ajaxResponse.getRequestParameter("activityclass"), ajaxResponse.getRequestParameter("sdcid"), departmentid, testinglabtype);
        if (departmentid.length() > 0) {
            assignmentPage.setDepartmentId(departmentid);
        }
        assignmentPage.setFullContent(false);
        ajaxResponse.addCallbackArgument("activityclass", assignmentPage.getActivityClassHandler().getId());
        ajaxResponse.addCallbackArgument("sdcid", assignmentPage.getSDCId());
        ajaxResponse.addCallbackArgument("url", assignmentPage.getListPageUrl());
        ajaxResponse.addCallbackArgument("pagedirectives", assignmentPage.getListPageDirectives().toJSONString());
        ajaxResponse.addCallbackArgument("leftheader", assignmentPage.getLeftSelector());
    }

    private void handleModeResources(AjaxResponse ajaxResponse, AssignmentPage assignmentPage) {
        try {
            AssignmentPageResourceData resourceData = null;
            try {
                String rs = ajaxResponse.getRequestParameter("resourcedata", "");
                if (rs.length() > 0 && rs.startsWith("{")) {
                    resourceData = new AssignmentPageResourceData(new JSONObject(rs));
                }
            }
            catch (Exception e) {
                this.logError(e.getMessage(), e);
            }
            JSONObject selection = null;
            try {
                selection = ajaxResponse.getRequestParameter("selection").length() > 0 ? new JSONObject(ajaxResponse.getRequestParameter("selection")) : new JSONObject("{start: null, startms:'', end: null, endms:'', activity:null}");
            }
            catch (Exception exception) {
                // empty catch block
            }
            String departmentid = ajaxResponse.getRequestParameter("departmentid");
            AssignmentPageResourceData outData = null;
            ActivityClassHandler activityClass = assignmentPage.getActivityClassHandler(ajaxResponse.getRequestParameter("activityclass"), ajaxResponse.getRequestParameter("sdcid"));
            if (this.operatingMode == AssignmentPage.OperatingMode.ASSIGNMENT) {
                AssignmentPage.AssignmentPageResourceRequirement resourceRequirement = AssignmentPageUtil.loadResources(activityClass, ajaxResponse.getRequestParameter("keyid1"), null, departmentid, AssignmentPage.ResourceMode.SUGGESTED, this.getConnectionId(), this.getSDIProcessor(), this.getTranslationProcessor(), false, selection);
                outData = resourceRequirement.getResourceData();
            }
            if (!(outData == null || resourceData != null && outData.getWorkContext().equals(resourceData.getWorkContext()) && outData.getTestingDepartmentId().equals(resourceData.getTestingDepartmentId()) && resourceData.resources.size() == outData.resources.size())) {
                ajaxResponse.addCallbackArgument("data", outData.toJSONString());
                if (selection != null) {
                    ajaxResponse.addCallbackArgument("selection", selection.toString());
                } else {
                    ajaxResponse.addCallbackArgument("selection", "{}");
                }
            } else if (outData == null) {
                AssignmentPageResourceData empty = new AssignmentPageResourceData("", "");
                ajaxResponse.addCallbackArgument("data", empty.toJSONString());
            }
        }
        catch (Exception e) {
            this.logError(e.getMessage(), e);
        }
    }

    private void handleModeRefreshSource(AjaxResponse ajaxResponse) {
        try {
            AssignmentPageResourceData resourceData = null;
            try {
                String rs = ajaxResponse.getRequestParameter("resourcedata", "");
                if (rs.length() > 0) {
                    resourceData = new AssignmentPageResourceData(new JSONObject(rs));
                }
            }
            catch (Exception e) {
                this.logError(e.getMessage(), e);
            }
            String focusedResource = ajaxResponse.getRequestParameter("focusedresource");
            String departmentid = ajaxResponse.getRequestParameter("departmentid");
            AssignmentPage.ResourceMode resourceMode = ajaxResponse.getRequestParameter("showall", "N").equalsIgnoreCase("Y") ? AssignmentPage.ResourceMode.ALL : AssignmentPage.ResourceMode.SUGGESTED;
            AssignmentPageUtil.refreshFocusedResource(focusedResource, resourceData, departmentid, resourceMode, this.getSDIProcessor(), this.getConnectionId());
            ajaxResponse.addCallbackArgument("data", resourceData.toJSONString());
        }
        catch (Exception e) {
            ajaxResponse.setError(e.getMessage());
        }
    }

    private void handleModeTime(AjaxResponse ajaxResponse, AssignmentPage assignmentPage) {
        try {
            AssignmentPageResourceData resourceData = null;
            try {
                String rs = ajaxResponse.getRequestParameter("resourcedata", "");
                if (rs.length() > 0) {
                    resourceData = new AssignmentPageResourceData(new JSONObject(rs));
                }
            }
            catch (Exception e) {
                this.logError(e.getMessage(), e);
            }
            JSONObject selection = null;
            try {
                selection = ajaxResponse.getRequestParameter("selection").length() > 0 ? new JSONObject(ajaxResponse.getRequestParameter("selection")) : new JSONObject("{start: null, startms:'', end: null, endms:'', activity:null}");
            }
            catch (Exception exception) {
                // empty catch block
            }
            String departmentid = ajaxResponse.getRequestParameter("departmentid");
            ActivityClassHandler activityClass = assignmentPage.getActivityClassHandler(ajaxResponse.getRequestParameter("activityclass"), ajaxResponse.getRequestParameter("sdcid"));
            AssignmentPage.AssignmentPageResourceRequirement resourceRequirement = AssignmentPageUtil.loadResources(activityClass, ajaxResponse.getRequestParameter("keyid1"), resourceData, departmentid, AssignmentPage.ResourceMode.SUGGESTED, this.getConnectionId(), this.getSDIProcessor(), this.getTranslationProcessor(), true, selection);
            AssignmentPageResourceData outData = resourceRequirement.getResourceData();
            String duration = resourceRequirement.getDuration();
            ajaxResponse.addCallbackArgument("result", duration);
            if (!(outData == null || resourceData.getWorkContext().length() != 0 && outData.getWorkContext().equalsIgnoreCase(resourceData.getWorkContext()) && outData.getTestingDepartmentId().equals(resourceData.getTestingDepartmentId()) && resourceData.resources.size() == outData.resources.size())) {
                ajaxResponse.addCallbackArgument("data", outData != null ? outData.toJSONString() : "");
                if (selection != null) {
                    ajaxResponse.addCallbackArgument("selection", selection.toString());
                } else {
                    ajaxResponse.addCallbackArgument("selection", "{}");
                }
            } else {
                ajaxResponse.addCallbackArgument("data", "");
            }
        }
        catch (Exception e) {
            ajaxResponse.setError(e.getMessage());
        }
    }

    private void handleModeAssignDialog(AjaxResponse ajaxResponse, AssignmentPage assignmentPage) throws SapphireException {
        CalendarPage.View dateRange;
        try {
            dateRange = CalendarPage.View.valueOf(ajaxResponse.getRequestParameter("daterange").toUpperCase());
        }
        catch (Exception e) {
            dateRange = CalendarPage.View.MONTH;
        }
        String selectedsdis = StringUtil.replaceAll(ajaxResponse.getRequestParameter("selectedsdis"), "%3B", ";");
        Instant[] selectedDates = AssignmentAjax.convertDatesFromClient(ajaxResponse.getRequestParameter("selecteddates"), this.displayTimeZone, this.clientOffsetMinutes);
        String shiftid = ajaxResponse.getRequestParameter("shiftid");
        String departmentid = ajaxResponse.getRequestParameter("departmentid");
        JSONObject selection = null;
        try {
            selection = new JSONObject(ajaxResponse.getRequestParameter("selection"));
        }
        catch (Exception exception) {
            // empty catch block
        }
        AssignmentPageResourceData resourceData = null;
        try {
            String rs = ajaxResponse.getRequestParameter("resourcedata", "");
            if (rs.length() > 0) {
                resourceData = new AssignmentPageResourceData(new JSONObject(rs));
            }
        }
        catch (Exception e) {
            this.logError(e.getMessage(), e);
        }
        ActivityClassHandler activityClass = assignmentPage.getActivityClassHandler(ajaxResponse.getRequestParameter("activityclass"), ajaxResponse.getRequestParameter("sdcid"));
        String html = "";
        String message = "";
        try {
            html = this.viewMode.getAssignmentHTML(activityClass, selectedsdis, dateRange, selectedDates, departmentid, shiftid, selection, resourceData, this.getSDIProcessor(), this.getTranslationProcessor(), this.displayTimeZone, this.m18);
        }
        catch (Exception e) {
            message = e.getMessage();
        }
        ajaxResponse.addCallbackArgument("html", html);
        ajaxResponse.addCallbackArgument("message", message);
    }

    private void handleModeAssign(AjaxResponse ajaxResponse, AssignmentPage assignmentPage) throws SapphireException {
        CalendarPage.View calendarView;
        try {
            calendarView = CalendarPage.View.valueOf(ajaxResponse.getRequestParameter("daterange").toUpperCase());
        }
        catch (Exception e) {
            calendarView = CalendarPage.View.MONTH;
        }
        String sdcid = ajaxResponse.getRequestParameter("sdcid");
        ActivityClassHandler activityClass = assignmentPage.getActivityClassHandler(ajaxResponse.getRequestParameter("activityclass"), ajaxResponse.getRequestParameter("sdcid"));
        String[] selectedsdis = ajaxResponse.getRequestParameter("selectedsdis").length() > 0 ? StringUtil.split(StringUtil.replaceAll(ajaxResponse.getRequestParameter("selectedsdis"), "%3B", ";"), ";") : new String[]{};
        String activitydesc = ajaxResponse.getRequestParameter("activitydesc");
        boolean autoactivate = ajaxResponse.getRequestParameter("autoactivate", "N").equalsIgnoreCase("Y");
        String shiftid = ajaxResponse.getRequestParameter("shiftid");
        String departmentid = ajaxResponse.getRequestParameter("departmentid");
        ZonedDateTime[] selectedDates = null;
        if (ajaxResponse.getRequestParameter("selecteddates").length() > 0) {
            String[] parts = StringUtil.split(ajaxResponse.getRequestParameter("selecteddates"), ";");
            selectedDates = new ZonedDateTime[parts.length];
            for (int i = 0; i < parts.length; ++i) {
                selectedDates[i] = CalendarConverter.getZonedDateTimeFromClientInstantUTC(parts[i], this.displayTimeZone);
            }
        }
        String dateassignment = ajaxResponse.getRequestParameter("dateassignment");
        int fixedmins = 0;
        try {
            fixedmins = Integer.parseInt(ajaxResponse.getRequestParameter("fixeddate"));
        }
        catch (Exception exception) {
            // empty catch block
        }
        JSONObject resourceassignment = null;
        try {
            resourceassignment = ajaxResponse.getRequestParameter("resourceassignment").length() > 0 ? new JSONObject(ajaxResponse.getRequestParameter("resourceassignment")) : new JSONObject();
        }
        catch (Exception exception) {
            // empty catch block
        }
        JSONObject selection = null;
        try {
            selection = new JSONObject(ajaxResponse.getRequestParameter("selection"));
        }
        catch (Exception exception) {
            // empty catch block
        }
        AssignmentPageResourceData resourceData = null;
        try {
            String rs = ajaxResponse.getRequestParameter("resourcedata", "");
            if (rs.length() > 0) {
                resourceData = new AssignmentPageResourceData(new JSONObject(rs));
            }
        }
        catch (Exception e) {
            this.logError(e.getMessage(), e);
        }
        String timemode = "Floating";
        if (dateassignment.equalsIgnoreCase("Fixed")) {
            timemode = "Fixed";
        }
        try {
            String activityid = AssignmentPageUtil.assignWork(sdcid, selectedsdis, departmentid, shiftid, selection, resourceassignment, timemode, calendarView, selectedDates, fixedmins, activitydesc, autoactivate, activityClass, resourceData, this.displayTimeZone, this.getSDIProcessor(), this.getTranslationProcessor(), this.getActionProcessor(), this.m18);
            ajaxResponse.addCallbackArgument("activityid", activityid);
            ajaxResponse.addCallbackArgument("message", "");
        }
        catch (Throwable e) {
            ajaxResponse.addCallbackArgument("activityid", "");
            ajaxResponse.addCallbackArgument("message", e.getMessage());
        }
    }

    private void handleModeMove(AjaxResponse ajaxResponse) {
        String activityid = ajaxResponse.getRequestParameter("activityid");
        String timemode = ajaxResponse.getRequestParameter("timemode");
        if (timemode.length() == 0) {
            timemode = "Floating";
        }
        try {
            ZonedDateTime newFrom = CalendarConverter.getZonedDateTimeFromClientValue(ajaxResponse.getRequestParameter("fromdate"), this.displayTimeZone, this.clientOffsetMinutes);
            ZonedDateTime newTo = CalendarConverter.getZonedDateTimeFromClientValue(ajaxResponse.getRequestParameter("todate"), this.displayTimeZone, this.clientOffsetMinutes);
            if (ajaxResponse.getRequestParameter("resourceid").length() > 0 && ajaxResponse.getRequestParameter("resourcedata").length() > 0 && ajaxResponse.getRequestParameter("focusedresource").length() > 0) {
                AssignmentPageResourceData resourceData = null;
                String rs = ajaxResponse.getRequestParameter("resourcedata", "");
                if (rs.length() > 0) {
                    resourceData = new AssignmentPageResourceData(new JSONObject(rs));
                }
                AssignmentPageResourceContainer focus = AssignmentPageUtil.getFocusedResource(ajaxResponse.getRequestParameter("focusedresource"), resourceData);
                String resourcesdc = ajaxResponse.getRequestParameter("resourceisworkarea").equalsIgnoreCase("Y") ? "Department" : focus.getResourceSDC().getName();
                try {
                    TimeZone currentTimeZone = this.getConnectionProcessor().getSapphireConnection().getTimeZone() != null ? TimeZone.getTimeZone(this.getConnectionProcessor().getSapphireConnection().getTimeZone()) : null;
                    String message = AssignmentPageUtil.moveWork(activityid, timemode, newFrom != null ? newFrom.toInstant() : null, newTo != null ? newTo.toInstant() : null, resourceData, focus, ajaxResponse.getRequestParameter("resourceid"), resourcesdc, currentTimeZone, this.getActionProcessor(), this.getTranslationProcessor(), this.m18);
                    ajaxResponse.addCallbackArgument("message", message);
                    ajaxResponse.addCallbackArgument("messagetype", "warning");
                }
                catch (SapphireException e) {
                    ajaxResponse.addCallbackArgument("message", e.getMessage());
                    ajaxResponse.addCallbackArgument("messagetype", "error");
                }
            } else {
                try {
                    TimeZone currentTimeZone = this.getConnectionProcessor().getSapphireConnection().getTimeZone() != null ? TimeZone.getTimeZone(this.getConnectionProcessor().getSapphireConnection().getTimeZone()) : null;
                    String message = AssignmentPageUtil.moveWork(activityid, timemode, newFrom != null ? newFrom.toInstant() : null, newTo != null ? newTo.toInstant() : null, currentTimeZone, this.getActionProcessor(), this.getTranslationProcessor(), this.m18);
                    ajaxResponse.addCallbackArgument("message", message);
                    ajaxResponse.addCallbackArgument("messagetype", "warning");
                }
                catch (SapphireException e) {
                    ajaxResponse.addCallbackArgument("message", e.getMessage());
                    ajaxResponse.addCallbackArgument("messagetype", "error");
                }
            }
        }
        catch (Exception e) {
            ajaxResponse.addCallbackArgument("message", e.getMessage());
        }
    }

    private void handleModeFixedTime(AjaxResponse ajaxResponse) {
        String activityid = ajaxResponse.getRequestParameter("activityid");
        if (activityid.length() > 0) {
            try {
                AssignmentPageUtil.toggleTimemode(activityid, this.getConnectionId());
                ajaxResponse.addCallbackArgument("message", "");
            }
            catch (SapphireException e) {
                ajaxResponse.addCallbackArgument("message", e.getMessage());
            }
            catch (Exception e) {
                Logger.logError("Failed to toggle time mode.", e);
                ajaxResponse.addCallbackArgument("message", "Failed to toggle time mode.");
            }
        } else {
            ajaxResponse.addCallbackArgument("message", this.getTranslationProcessor().translate("No activity selected."));
        }
    }

    private void handleModeOperationsMenu(AjaxResponse ajaxResponse, AssignmentPage assignmentPage) throws SapphireException {
        String activityid = ajaxResponse.getRequestParameter("activityid");
        String activityclass = ajaxResponse.getRequestParameter("activityclass");
        String html = assignmentPage.getOperationsMenu(activityid, activityclass);
        ajaxResponse.addCallbackArgument("activityid", activityid);
        ajaxResponse.addCallbackArgument("activityclass", activityclass);
        ajaxResponse.addCallbackArgument("html", html);
    }

    private void handleModeWorkloadInfo(HttpServletRequest request, AjaxResponse ajaxResponse, DateTimeUtil dtu) throws SapphireException {
        AssignmentPage.AssignmentPageTimeRange clientTimeRange = this.getClientTimeRange(ajaxResponse);
        AssignmentPageUtil.adjustRangeToCalendarView(this.calendarView, clientTimeRange, true, this.m18);
        AssignmentPageResourceData resourceData = null;
        String rs = ajaxResponse.getRequestParameter("resourcedata", "");
        if (rs.length() > 0) {
            try {
                resourceData = new AssignmentPageResourceData(new JSONObject(rs));
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        AssignmentPageResourceContainer focus = AssignmentPageUtil.getFocusedResource(ajaxResponse.getRequestParameter("focusedresource"), resourceData);
        DataSet resources = focus.getData();
        DataSet workareas = focus.getWorkareas();
        String focusid = ajaxResponse.getRequestParameter("focusid");
        boolean isWorkarea = ajaxResponse.getRequestParameter("focusworkarea", "N").equalsIgnoreCase("Y");
        if (focusid.contains("|")) {
            String[] fa = StringUtil.split(focusid, "|");
            focusid = fa[0];
            isWorkarea = fa[1].equalsIgnoreCase("Department");
        }
        StringBuilder out = new StringBuilder();
        Browser browser = new Browser(request);
        if (browser.isIE()) {
            out.append("<div style=\"width:auto;height:auto;overflow:scroll;position:absolute;top:41px;bottom:43px;left:10px;right:10px\" id=\"wapcontent\">");
        } else {
            out.append("<div style=\"width:100%;height:100%;overflow:scroll;position:relative\" id=\"wapcontent\">");
        }
        WAPAvailabilitySelector selector = new WAPAvailabilitySelector(this.getConnectionid());
        selector.setTranslationProcessor(this.getTranslationProcessor());
        DateFormat dateOnlyFormat = dtu.getDefaultDateOnlyFormat();
        if (focusid.length() > 0) {
            DataSet use;
            DataSet dataSet = use = isWorkarea ? workareas : resources;
            int row = use.findRow(isWorkarea ? "departmentid" : (focus.getResourceSDC() == AssignmentPage.ResourceSDC.USER ? "sysuserid" : "instrumentid"), focusid);
            if (row >= 0) {
                WAPAvailabilityOptions options = new WAPAvailabilityOptions();
                options.setIsLog(true);
                List<WAPAvailability> availabilityList = isWorkarea ? (focus.getResourceSDC() == AssignmentPage.ResourceSDC.USER ? selector.getUserWorkareaAvailabilityBetween(focusid, clientTimeRange.from.toInstant(), clientTimeRange.to.toInstant(), options, this.displayTimeZone) : selector.getInstrumentWorkareaAvailabilityBetween(focusid, clientTimeRange.from.toInstant(), clientTimeRange.to.toInstant(), options, "", "", this.displayTimeZone)) : (focus.getResourceSDC() == AssignmentPage.ResourceSDC.USER ? selector.getUserAvailabilityBetween(focusid, clientTimeRange.from.toInstant(), clientTimeRange.to.toInstant(), options, this.displayTimeZone) : selector.getInstrumentAvailabilityBetween(focusid, clientTimeRange.from.toInstant(), clientTimeRange.to.toInstant(), options, this.displayTimeZone));
                if (availabilityList.size() > 0) {
                    this.writeLoadingTable(out, focusid, dateOnlyFormat, availabilityList, this.getTranslationProcessor(), isWorkarea);
                } else {
                    out.append("No Details Found");
                }
            }
        } else {
            List<WAPAvailability> availabilityList;
            WAPAvailabilityOptions options;
            String resourceid;
            int i;
            if (resources != null) {
                for (i = 0; i < resources.size(); ++i) {
                    resourceid = resources.getValue(i, focus.getResourceSDC() == AssignmentPage.ResourceSDC.USER ? "sysuserid" : "instrumentid");
                    if (resourceid.length() <= 0) continue;
                    options = new WAPAvailabilityOptions();
                    options.setIsLog(true);
                    availabilityList = focus.getResourceSDC() == AssignmentPage.ResourceSDC.USER ? selector.getUserAvailabilityBetween(resourceid, clientTimeRange.from.toInstant(), clientTimeRange.to.toInstant(), options, this.displayTimeZone) : selector.getInstrumentAvailabilityBetween(resourceid, clientTimeRange.from.toInstant(), clientTimeRange.to.toInstant(), options, this.displayTimeZone);
                    if (availabilityList.size() > 0) {
                        this.writeLoadingTable(out, resourceid, dateOnlyFormat, availabilityList, this.getTranslationProcessor(), false);
                        continue;
                    }
                    out.append("No Details Found");
                }
            }
            if (workareas != null) {
                for (i = 0; i < workareas.size(); ++i) {
                    resourceid = workareas.getValue(i, "departmentid");
                    if (resourceid.length() <= 0) continue;
                    options = new WAPAvailabilityOptions();
                    options.setIsLog(true);
                    availabilityList = focus.getResourceSDC() == AssignmentPage.ResourceSDC.USER ? selector.getUserWorkareaAvailabilityBetween(resourceid, clientTimeRange.from.toInstant(), clientTimeRange.to.toInstant(), options, this.displayTimeZone) : selector.getInstrumentWorkareaAvailabilityBetween(resourceid, clientTimeRange.from.toInstant(), clientTimeRange.to.toInstant(), options, "", "", this.displayTimeZone);
                    if (availabilityList.size() > 0) {
                        this.writeLoadingTable(out, resourceid, dateOnlyFormat, availabilityList, this.getTranslationProcessor(), true);
                        continue;
                    }
                    out.append("No Details Found");
                }
            }
        }
        out.append("</div>");
        ajaxResponse.addCallbackArgument("html", out.toString());
    }

    private void handleModeActivityOperations(AjaxResponse ajaxResponse, Mode mode) {
        block24: {
            String activityid;
            StringBuilder activitylist = new StringBuilder();
            JSONObject selection = null;
            try {
                selection = new JSONObject(ajaxResponse.getRequestParameter("selection"));
            }
            catch (Exception exception) {
                // empty catch block
            }
            if (selection != null && selection.has("activity")) {
                try {
                    JSONArray acts = selection.getJSONArray("activity");
                    for (int i = 0; i < acts.length(); ++i) {
                        if (activitylist.length() > 0) {
                            activitylist.append(";");
                        }
                        activitylist.append(acts.getString(i));
                    }
                }
                catch (Exception acts) {
                    // empty catch block
                }
            }
            if (activitylist.length() == 0 && (activityid = ajaxResponse.getRequestParameter("activityid")).length() > 0) {
                activitylist.append(activityid);
            }
            if (activitylist.length() > 0) {
                try {
                    PropertyList props;
                    if (mode == Mode.DELETE || mode == Mode.UNDO) {
                        props = new PropertyList();
                        props.setProperty("activityid", activitylist.toString());
                        this.getActionProcessor().processActionClass(DeleteActivity.class.getName(), props);
                        break block24;
                    }
                    if (mode == Mode.RESETRESOURCES) {
                        props = new PropertyList();
                        props.setProperty("activityid", activitylist.toString());
                        props.setProperty("reset", "Y");
                        this.getActionProcessor().processActionClass(SetActivityResource.class.getName(), props);
                        break block24;
                    }
                    props = new PropertyList();
                    props.setProperty("activityid", activitylist.toString());
                    if (mode == Mode.CANCEL) {
                        props.setProperty("status", "Cancelled");
                    } else if (mode == Mode.START) {
                        props.setProperty("status", "In Progress");
                    } else if (mode == Mode.STOP) {
                        if (ajaxResponse.getRequestParameter("date", "").length() > 0) {
                            props.setProperty("actualenddt", ajaxResponse.getRequestParameter("date", "n"));
                        }
                        props.setProperty("status", "Completed");
                    } else if (mode == Mode.ACTIVATE) {
                        props.setProperty("status", "Activated");
                    } else if (mode == Mode.UNACTIVATE) {
                        props.setProperty("status", "Draft");
                    }
                    this.getActionProcessor().processActionClass(SetActivityStatus.class.getName(), props);
                }
                catch (Exception e) {
                    ajaxResponse.addCallbackArgument("message", e.getMessage());
                }
            } else {
                ajaxResponse.addCallbackArgument("message", this.getTranslationProcessor().translate("No activity selected."));
            }
        }
    }

    public static void convertDateSelectionFromClient(JSONObject selection, ZoneId displayTimeZone, int clientOffsetMinutes) {
        try {
            if (selection.optString("startms", "").equals("")) {
                selection.put("startms", DateTimeFormatter.ISO_INSTANT.format(Instant.now()));
                selection.put("endms", DateTimeFormatter.ISO_INSTANT.format(Instant.now()));
            } else {
                if (selection.has("startms")) {
                    selection.put("startms", DateTimeFormatter.ISO_INSTANT.format(CalendarConverter.getZonedDateTimeFromClientValue(selection.optString("startms"), displayTimeZone, clientOffsetMinutes).toInstant()));
                }
                if (selection.has("endms")) {
                    selection.put("endms", DateTimeFormatter.ISO_INSTANT.format(CalendarConverter.getZonedDateTimeFromClientValue(selection.optString("endms"), displayTimeZone, clientOffsetMinutes).toInstant()));
                }
            }
        }
        catch (JSONException jSONException) {
            // empty catch block
        }
    }

    public static long getAdjustedTime(long input, int clientTimeZoneOffsetToUTC) {
        long l = input + -1L * (long)clientTimeZoneOffsetToUTC * 60000L;
        return l;
    }

    public static Calendar getTimeZoneCal(Calendar input, TimeZone displayTimeZone) {
        if (input != null) {
            if (displayTimeZone != null) {
                Date date = input.getTime();
                TimeZone tz = input.getTimeZone();
                long msFromEpochGmt = date.getTime();
                int inputOffsetFromUTC = tz.getOffset(msFromEpochGmt);
                long msUTC = msFromEpochGmt - (long)inputOffsetFromUTC;
                int outputOffsetFromUTC = displayTimeZone.getOffset(msFromEpochGmt);
                long msOut = msUTC + (long)outputOffsetFromUTC;
                Calendar output = Calendar.getInstance(displayTimeZone);
                output.setTimeInMillis(msOut);
                return output;
            }
            return input;
        }
        return null;
    }

    public static Instant[] convertDatesFromClient(String selecteddates, ZoneId displayTimeZone, int clientOffsetMinutes) {
        try {
            String[] sel = StringUtil.split(selecteddates, ";");
            if (sel.length == 1) {
                if (displayTimeZone == null) {
                    return new Instant[]{Instant.ofEpochMilli(Long.parseLong(sel[0]))};
                }
                ZonedDateTime from = CalendarConverter.getZonedDateTimeFromClientValue(sel[0], displayTimeZone, clientOffsetMinutes);
                return new Instant[]{from.toInstant()};
            }
            if (displayTimeZone == null) {
                return new Instant[]{Instant.ofEpochMilli(Long.parseLong(sel[0])), Instant.ofEpochMilli(Long.parseLong(sel[1]))};
            }
            ZonedDateTime from = CalendarConverter.getZonedDateTimeFromClientValue(sel[0], displayTimeZone, clientOffsetMinutes);
            ZonedDateTime to = CalendarConverter.getZonedDateTimeFromClientValue(sel[1], displayTimeZone, clientOffsetMinutes);
            return new Instant[]{from.toInstant(), to.toInstant()};
        }
        catch (NumberFormatException numberFormatException) {
            return new Instant[]{Instant.now(), Instant.now()};
        }
    }

    public void writeLoadingTable(StringBuilder out, String title, DateFormat dateOnlyFormat, List<WAPAvailability> availabilityList, TranslationProcessor translationProcessor, boolean isWorkarea) {
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        DateTimeFormatter dateOnlyFormatter = DateTimeFormatter.ofPattern(((SimpleDateFormat)dateOnlyFormat).toPattern());
        out.append("<table border=\"0\" cellspacing=\"0\" cellpadding=\"2\">");
        if (title.length() > 0) {
            out.append("<tr><td class=\"maintform_fieldtitle\" colspan=\"3\">" + title + "</td></tr>");
        }
        int totalpercent = 0;
        int size = 0;
        for (WAPAvailability availability : availabilityList) {
            int workingMinutes;
            int n = workingMinutes = availability.isOoo() && !isWorkarea ? 0 : availability.getWorkingMinutes();
            if (workingMinutes > 0) {
                ++size;
            }
            out.append("<tr>");
            out.append("<td class=\"maintform_fieldtitle\">" + dateOnlyFormatter.format(availability.getStartdt()) + "</td>");
            out.append("<td style=\"padding:0\">");
            out.append("<table style=\"border: 0;width:100%\" border=\"1\" cellspacing=\"0\" cellpadding=\"2\">");
            out.append("<tr><td>" + translationProcessor.translate("Working Time (mins)") + "</td>");
            StringBuilder workingTime = new StringBuilder();
            if (availability.isOoo() && !isWorkarea) {
                workingTime.append(translationProcessor.translate("Out-Of-Office"));
            } else {
                ArrayList<ZonedDateTime[]> workingRanges = availability.getWorkingRanges();
                if (workingRanges.size() == 0) {
                    if (isWorkarea) {
                        workingTime.append("&nbsp;");
                    } else {
                        workingTime.append(translationProcessor.translate("Unknown"));
                    }
                } else if (workingRanges.size() == 1) {
                    ZonedDateTime[] range = workingRanges.get(0);
                    if (ChronoUnit.MINUTES.between(range[0], range[1]) == 1440L) {
                        workingTime.append(translationProcessor.translate("All day"));
                    } else {
                        workingTime.append(timeFormatter.format(range[0])).append("-").append(timeFormatter.format(range[1]));
                    }
                } else {
                    for (ZonedDateTime[] range : workingRanges) {
                        workingTime.append(workingTime.length() > 0 ? ", " : "").append(timeFormatter.format(range[0])).append("-").append(timeFormatter.format(range[1]));
                    }
                }
            }
            out.append("<td colspan=\"2\">" + workingTime + "</td>");
            out.append("<td >" + workingMinutes + "</td></tr>");
            out.append((CharSequence)availability.getAppointmentLog());
            out.append("<tr><td colspan=\"3\">" + translationProcessor.translate("Total Appointments") + "</td><td>" + availability.getAppointmentMinutes() + "</td></tr>");
            out.append((CharSequence)availability.getActivityLog());
            out.append("<tr><td style=\"border-bottom: 1px solid #BDCCD4\" colspan=\"3\">" + translationProcessor.translate("Total Activity") + "</td><td style=\"border-bottom: 1px solid #BDCCD4\" >" + availability.getActivityMinutes() + "</td></tr>");
            int percent = availability.getAppointmentMinutes() + availability.getActivityMinutes() > 0 && workingMinutes == 0 ? -1 : Math.round(100.0f * (float)(availability.getAppointmentMinutes() + availability.getActivityMinutes()) / (float)workingMinutes);
            totalpercent += Math.max(percent, 0);
            out.append("</table>");
            out.append("</td>");
            out.append("<td class=\"maintform_fieldtitle\">");
            out.append("<table>");
            out.append("<tr><td rowspan=\"2\" valign=\"middle\">" + translationProcessor.translate("Percent Load") + " = </td><td style=\"border-bottom:1px solid black\">" + availability.getAppointmentMinutes() + " + " + availability.getActivityMinutes() + "</td><td rowspan=\"2\" valign=\"middle\"> = <b>" + (percent == -1 ? "??" : Integer.valueOf(percent)) + "%</b></td></tr>");
            out.append("<tr><td align=\"center\">" + workingMinutes + "</td></tr>");
            out.append("</table>");
            out.append("</td></tr>");
        }
        if (size > 0) {
            int percent = (int)Math.round((double)totalpercent / (double)size);
            out.append("<tr><td class=\"maintform_fieldtitle\"colspan=\"2\"><b>" + translationProcessor.translate("TOTAL") + ": </b></td><td class=\"maintform_fieldtitle\">" + translationProcessor.translate("Percent Load") + " = " + percent + "%</td></tr>");
        }
        out.append("</table>");
    }

    public static enum Mode {
        VIEW,
        LIST,
        SELECTORS,
        TIME,
        ASSIGNDIALOG,
        ASSIGN,
        MOVE,
        ACTIVATE,
        UNACTIVATE,
        DELETE,
        UNDO,
        RESOURCES,
        REFRESHRSOURCE,
        FIXEDTIME,
        RESETRESOURCES,
        CANCEL,
        START,
        STOP,
        SELECTION,
        EVENTCOLORSKEY,
        RESOURCESECTION,
        OPERATIONSMENU,
        WORKLOADINFO;

    }
}

