/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.modules.wap.activity;

import com.labvantage.opal.elements.advancedtoolbar.AdvancedToolbar;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.I18nUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.layouts.modern.ModernLayout;
import com.labvantage.sapphire.modules.wap.CalendarConverter;
import com.labvantage.sapphire.modules.wap.activity.Activity;
import com.labvantage.sapphire.modules.wap.activity.ActivityClassHandler;
import com.labvantage.sapphire.modules.wap.activity.AssignmentPageButtonType;
import com.labvantage.sapphire.modules.wap.activity.AssignmentPageResourceContainer;
import com.labvantage.sapphire.modules.wap.activity.AssignmentPageResourceData;
import com.labvantage.sapphire.modules.wap.activity.AssignmentPageUtil;
import com.labvantage.sapphire.modules.wap.activity.WAPAvailability;
import com.labvantage.sapphire.modules.wap.activity.WAPAvailabilityOptions;
import com.labvantage.sapphire.modules.wap.activity.WAPAvailabilitySelector;
import com.labvantage.sapphire.modules.wap.activity.WAPCommands;
import com.labvantage.sapphire.modules.wap.activity.WAPConstants;
import com.labvantage.sapphire.modules.wap.activity.WAPSelector;
import com.labvantage.sapphire.modules.wap.activity.WAPSelectorOptions;
import com.labvantage.sapphire.modules.wap.calendar.CalendarFactory;
import com.labvantage.sapphire.modules.wap.calendar.CalendarItem;
import com.labvantage.sapphire.modules.wap.calendar.CalendarPage;
import com.labvantage.sapphire.modules.wap.calendar.LVCalendar;
import com.labvantage.sapphire.modules.wap.workhours.WorkHours;
import com.labvantage.sapphire.pageelements.controls.Button;
import com.labvantage.sapphire.pageelements.controls.Image;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import javax.servlet.jsp.PageContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.SDIProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.pageelements.BaseElement;
import sapphire.servlet.RequestContext;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.util.M18NUtil;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.SafeHTML;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class AssignmentPage
extends BaseElement
implements WAPConstants {
    private static DateTimeFormatter schedulerControlFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm");
    public static final String ACTIVITY_ACTIVITYCLASSID = "__LV_Activity";
    public static final String ACTIVITY_ACTIVITYCLASSLABEL = "Unassigned Actvities";
    public static final String DEPARTMENTUSERS_COLUMN = "_sysusers";
    private boolean viewOnly = false;
    private boolean fullContent = true;
    private SelectorMode selectorMode = SelectorMode.NONE;
    private String devMode = null;
    private ActivityClassHandler activityClassHandler = null;
    private PropertyList wapPolicy = null;
    private String keyid1 = "";
    private ViewMode viewMode = null;
    private CalendarView calendarView = null;
    private ZonedDateTime clientFromDate = null;
    private ZonedDateTime clientToDate = null;
    private PropertyList userConfig;
    private String userConfigPrefix = "";
    private String pageName = "";
    private String returnButtonId = "";
    private ColorScheme colorScheme = null;
    private String departmentid = "";
    private ArrayList<String> usersdepartments = null;
    private String shiftid = "";
    private String focusid = "";
    private AssignmentPageResourceContainer focusedResource = null;
    private ResourceSDC resourceSDC = null;
    private String sdcid = "";
    private JSONObject selection = null;
    private boolean mywork = false;
    private boolean pageRender = false;
    private AssignmentPageResourceData resourceData = null;
    private String activityQueryWhere = "";
    private String activityQuery = "";
    private ZoneId displayTimeZone = ZoneOffset.UTC;
    private int clientOffsetMinutes;
    private OperatingMode operatingMode = OperatingMode.ASSIGNMENT;
    private int firstDayOfWeek = 2;

    public AssignmentPage(PageContext pageContext) {
        this.setPageContext(pageContext);
        this.userConfig = RequestContext.getRequestContext(pageContext).getPropertyList("userconfig");
    }

    public void setUserConfigPrefix(String userconfigpre) {
        this.userConfigPrefix = userconfigpre;
    }

    public void setTimeZone(ZoneId timezone) {
        this.displayTimeZone = timezone;
    }

    private void buildUserConfigPrefix() {
        if (this.userConfigPrefix == null || this.userConfigPrefix.length() == 0) {
            this.userConfigPrefix = (this.operatingMode == OperatingMode.ASSIGNMENT ? "A" : "W") + (this.pageName.length() > 0 ? "_" + this.pageName : "") + "_";
        }
    }

    public void setViewOnly(boolean vo) {
        this.viewOnly = vo;
    }

    public boolean getViewOnly() {
        return this.viewOnly;
    }

    public void setOperatingMode(OperatingMode operatingMode) {
        this.operatingMode = operatingMode;
        if (operatingMode == OperatingMode.WORK) {
            this.setViewMode(ViewMode.CALENDAR);
        }
    }

    public void setClientOffsetMinutes(int clientOffsetMinutes) {
        this.clientOffsetMinutes = clientOffsetMinutes;
    }

    private static void setEventStartEnd(JSONObject event, LocalDateTime startDateOutputZone, LocalDateTime endDateOutputZone, M18NUtil m18, ZoneId displayTimeZone) throws JSONException {
        if (startDateOutputZone != null) {
            event.put("start_date", schedulerControlFormatter.format(startDateOutputZone));
            event.put("start_date_formatted", m18.getDateTimeFormatter(displayTimeZone).format(startDateOutputZone));
        }
        if (endDateOutputZone != null) {
            event.put("end_date", schedulerControlFormatter.format(endDateOutputZone));
            event.put("end_date_formatted", m18.getDateTimeFormatter(displayTimeZone).format(endDateOutputZone));
        }
    }

    private static void handleConsolidate(JSONArray eventData, int styleNum, String keyValue, String focusSDC, boolean consolidateFullday, ZoneId displayTimeZone, TranslationProcessor tp, M18NUtil m18, StringBuilder consolidatedText, int consolidates, Instant consolStart, Instant consolEnd, String resourceSDC) {
        if (consolidateFullday && consolidates > 0) {
            JSONObject event = new JSONObject();
            try {
                event.put("text", consolidates + (consolidates > 1 ? " " + tp.translate("allday activities") : " " + tp.translate("allday activity")) + " " + tp.translate("for") + " " + keyValue);
                event.put("type", "ACTIVITY");
                if (consolStart != null) {
                    LocalDateTime localConsolidateStart = LocalDateTime.ofInstant(consolStart, displayTimeZone);
                    event.put("start_date", schedulerControlFormatter.format(localConsolidateStart));
                    event.put("start_date_formatted", m18.getDateTimeFormatter(displayTimeZone).format(localConsolidateStart));
                }
                if (consolEnd != null) {
                    LocalDateTime localConsolidateEnd = LocalDateTime.ofInstant(consolEnd, displayTimeZone);
                    event.put("end_date", schedulerControlFormatter.format(localConsolidateEnd));
                    event.put("end_date_formatted", m18.getDateTimeFormatter(displayTimeZone).format(localConsolidateEnd));
                }
                event.put("resourcesdc", resourceSDC);
                event.put("resourceid", keyValue);
                if (focusSDC.equalsIgnoreCase(ResourceSDC.INSTRUMENT.getName())) {
                    event.put("styleclass", "activity_instrument_" + styleNum);
                } else if (focusSDC.equalsIgnoreCase("Department")) {
                    event.put("styleclass", "activity_workarea_" + styleNum);
                } else {
                    event.put("styleclass", "activity_user_" + styleNum);
                }
                event.put("styleclass", "activity_user_" + styleNum);
                event.put("consolidatedtext", consolidatedText.toString());
                eventData.put(event);
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
    }

    public void setActivityClassHandler(String activityClassHandler) throws SapphireException {
        this.setActivityClassHandler(activityClassHandler, "");
    }

    public void setActivityClassHandler(String activityClass, String sdcid) throws SapphireException {
        this.activityClassHandler = this.getActivityClassHandler(activityClass, sdcid);
    }

    public void setActivityClassHandler(String activityClass, String sdcid, String testinglabdepartmentid, String testinglabtype) throws SapphireException {
        this.activityClassHandler = this.getActivityClassHandler(activityClass, sdcid, testinglabdepartmentid, testinglabtype);
    }

    public ActivityClassHandler getActivityClassHandler(String activityClass, String sdcid) throws SapphireException {
        if (sdcid.equalsIgnoreCase("LV_Activity") || activityClass.equalsIgnoreCase(ACTIVITY_ACTIVITYCLASSID)) {
            return new ActivityClassHandler(this.getConnectionId(), ACTIVITY_ACTIVITYCLASSID, ACTIVITY_ACTIVITYCLASSLABEL, "LV_Activity");
        }
        return ActivityClassHandler.getInstance(this.getConnectionid(), this.getWapPolicy(), activityClass, sdcid);
    }

    public ActivityClassHandler getActivityClassHandler(String activityClass, String sdcid, String testinglabdepartmentid, String testinglabtype) throws SapphireException {
        if (sdcid.equalsIgnoreCase("LV_Activity") || activityClass.equalsIgnoreCase(ACTIVITY_ACTIVITYCLASSID)) {
            return new ActivityClassHandler(this.getConnectionId(), ACTIVITY_ACTIVITYCLASSID, ACTIVITY_ACTIVITYCLASSLABEL, "LV_Activity", testinglabdepartmentid, testinglabtype);
        }
        return ActivityClassHandler.getInstance(this.getConnectionid(), this.getWapPolicy(), activityClass, sdcid, testinglabdepartmentid, testinglabtype);
    }

    public ActivityClassHandler getActivityClassHandler() {
        return this.activityClassHandler;
    }

    public PropertyList getWapPolicy() {
        if (this.wapPolicy == null) {
            this.wapPolicy = AssignmentPageUtil.getWapPolicy(this.connectionInfo.getConnectionId());
        }
        return this.wapPolicy;
    }

    private boolean drawResourceHtml(StringBuilder html, WAPAvailabilitySelector wapAvailabilitySelector, WAPAvailabilityOptions wapAvailabilityOptions, WAPSelector wapSelector, DataSet data, DataSet attachments, String sdcid, AssignmentPageResourceContainer focusedResource) {
        JSONArray jsonArray = this.getFocusedResource() != null ? this.getSelectedResource(this.getFocusedResource().getId()) : null;
        return AssignmentPageUtil.drawResourceHtml(html, wapAvailabilitySelector, wapAvailabilityOptions, wapSelector, this.clientFromDate.toInstant(), this.clientToDate.toInstant(), this.displayTimeZone, data, attachments, sdcid, focusedResource, jsonArray, 128, "assignment", this.getColorScheme(), this.logger, this.getTranslationProcessor(), this.pageContext);
    }

    public String getColorSchemeClass() {
        return "colorscheme_" + this.getColorScheme().toString().toLowerCase();
    }

    public ColorScheme getColorScheme() {
        if (this.colorScheme == null) {
            PropertyList wap = this.getWapPolicy();
            if (wap != null) {
                try {
                    this.colorScheme = ColorScheme.valueOf(wap.getPropertyListNotNull("colors").getProperty("scheme", ColorScheme.DEFAULT.toString()).toUpperCase());
                }
                catch (Exception e) {
                    this.colorScheme = ColorScheme.DEFAULT;
                }
            } else {
                this.colorScheme = ColorScheme.DEFAULT;
            }
            return this.colorScheme;
        }
        return this.colorScheme;
    }

    public OperatingMode getOperatingMode() {
        return this.operatingMode;
    }

    public void setFullContent(boolean full) {
        this.fullContent = full;
    }

    public void setDepartmentId(String departmentid) {
        this.departmentid = departmentid;
    }

    public String getDepartmentId() {
        return this.departmentid;
    }

    public String getKeyId1() {
        return this.keyid1;
    }

    public String getActivityQuery() {
        return this.activityQuery;
    }

    public void setShiftId(String shiftid) {
        this.shiftid = shiftid;
    }

    public void setSelection(JSONObject selection) {
        this.selection = selection;
    }

    public JSONObject getSelection() {
        return this.selection;
    }

    public JSONArray getSelectedResource(String resourceid) {
        return AssignmentPageUtil.getSelectedResource(resourceid, this.selection);
    }

    public AssignmentPageResourceData getResourceData() {
        return this.resourceData;
    }

    public AssignmentPageResourceContainer getFocusedResource() {
        if (this.focusedResource == null && this.operatingMode == OperatingMode.WORK && this.resourceData != null && this.resourceData.getResources() != null && this.resourceData.getResources().size() > 0) {
            this.focusedResource = this.resourceData.getResources().get(0);
        }
        return this.focusedResource;
    }

    public void setFocusedResource(AssignmentPageResourceContainer focusedResource) {
        this.focusedResource = focusedResource;
    }

    public void setFocusedResource(String resourceid) {
        AssignmentPageResourceData resourceData = this.getResourceData();
        this.focusedResource = AssignmentPageUtil.getFocusedResource(resourceid, resourceData);
    }

    public AssignmentPageResourceContainer getResourceData(String resourceId) {
        return AssignmentPageUtil.getResourceData(resourceId, this.getResourceData());
    }

    public AssignmentPageResourceContainer getResourceData(ResourceSDC resourceSDC, int typeCount, String keyid1, String keyid2, String keyid3, String workareaId) {
        return AssignmentPageUtil.getResourceData(resourceSDC, 0, "", "", "", "", "", typeCount, keyid1, keyid2, keyid3, workareaId, this.departmentid, true, this.getSDIProcessor());
    }

    public void setResourceData(AssignmentPageResourceData resourceData) {
        this.resourceData = resourceData;
    }

    public void generateResources() {
        String s;
        String k = this.getKeyId1();
        if (k.length() == 0) {
            k = this.getFocusId();
        }
        if ((s = this.getSDCId()).length() == 0) {
            s = this.getResourceSDC().getName();
        }
        String rt = "";
        String rm = "";
        if (this.getResourceSDC() == ResourceSDC.INSTRUMENT && this.element != null && this.element.containsKey("pagedata")) {
            rt = this.element.getPropertyList("pagedata").getProperty("instrumenttype", this.element.getPropertyList("pagedata").getProperty("instrumenttypeid", ""));
            rm = this.element.getPropertyList("pagedata").getProperty("instrumentmodel", this.element.getPropertyList("pagedata").getProperty("instrumentmodelid", ""));
        } else if (this.getResourceSDC() == ResourceSDC.USER && this.element != null && this.element.containsKey("pagedata")) {
            rt = "";
        }
        this.setResourceData(AssignmentPageUtil.generateResources(s, k, this.getActivityQuery(), this.getDepartmentId(), this.getResourceSDC(), this.clientFromDate.toInstant(), this.clientToDate.toInstant(), rt, rm, this.getSDIProcessor(), this.getQueryProcessor(), this.getConnectionId()));
    }

    public boolean isDevMode() {
        if (this.devMode == null) {
            com.labvantage.sapphire.admin.system.ConfigurationProcessor config = new com.labvantage.sapphire.admin.system.ConfigurationProcessor(this.pageContext);
            try {
                this.devMode = config.getSysConfigProperty("devmode", "N");
            }
            catch (Exception e) {
                this.devMode = "N";
            }
        }
        return this.devMode.equalsIgnoreCase("Y");
    }

    public void setViewMode(ViewMode viewMode) {
        this.viewMode = viewMode;
    }

    public String getListPageUrl() {
        PropertyList found;
        String sdc = this.getSDCId();
        String out = "";
        PropertyListCollection plans = this.element.getPropertyList("pagedata").getCollection("planables");
        if (plans != null && plans.size() > 0 && (found = plans.find("sdcid", sdc)) != null) {
            out = found.getProperty("listpage");
        }
        return out.length() > 0 ? "rc?command=page&page=" + out + "&sdcid=" + sdc : "rc?command=file&file=WEB-OPAL/pagetypes/list/maintenance_list.jsp";
    }

    private void processAvailability(List<WAPAvailability> availability, JSONObject availabilities, boolean isUser) {
        if (availability != null) {
            for (int a = 0; a < availability.size(); ++a) {
                WAPAvailability av = availability.get(a);
                String key = "A-" + av.getStartdt();
                if (availabilities.length() == 0 || !availabilities.has(key)) {
                    JSONObject availData = new JSONObject();
                    try {
                        ZonedDateTime end;
                        ZonedDateTime start;
                        if (av.isOoo()) continue;
                        ZonedDateTime zonedDateTime = av.getWorkingRanges() != null && av.getWorkingRanges().size() > 0 ? av.getWorkingRanges().get(0)[0] : (start = isUser ? null : av.getStartdt());
                        ZonedDateTime zonedDateTime2 = av.getWorkingRanges() != null && av.getWorkingRanges().size() > 0 ? av.getWorkingRanges().get(0)[1] : (end = isUser ? null : av.getStartdt().plusDays(1L));
                        if (start == null || end == null) continue;
                        availData.put("startdateutc", start.toInstant().toEpochMilli());
                        availData.put("enddateutc", end.toInstant().toEpochMilli());
                        availData.put("availablemin", av.getAvailableMinutes());
                        availData.put("workingmin", av.getWorkingMinutes());
                        availabilities.put(key, availData);
                    }
                    catch (Exception start) {}
                    continue;
                }
                try {
                    ZonedDateTime end;
                    ZonedDateTime start;
                    ZonedDateTime zonedDateTime = av.getWorkingRanges() != null && av.getWorkingRanges().size() > 0 ? av.getWorkingRanges().get(0)[0] : (start = isUser ? null : av.getStartdt());
                    ZonedDateTime zonedDateTime3 = av.getWorkingRanges() != null && av.getWorkingRanges().size() > 0 ? av.getWorkingRanges().get(0)[1] : (end = isUser ? null : av.getStartdt().plusDays(1L));
                    if (start == null || end == null) continue;
                    JSONObject availData = availabilities.getJSONObject(key);
                    int amin = availData.getInt("availablemin");
                    int wmin = availData.getInt("workingmin");
                    availData.put("availablemin", amin += av.getAvailableMinutes());
                    availData.put("workingmin", wmin += av.getWorkingMinutes());
                    continue;
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
        }
    }

    private void buildWorkloadData(JSONObject availabilities, JSONArray workloadData, String displayOption) {
        Iterator it = availabilities.keys();
        while (it.hasNext()) {
            String key = it.next().toString();
            try {
                JSONObject availData = availabilities.getJSONObject(key);
                int amin = availData.getInt("availablemin");
                int wmin = availData.getInt("workingmin");
                int percent = AssignmentPageUtil.getPercentAvailability(amin, wmin);
                availData.put("text", "" + percent + "% Workload");
                availData.put("percent", percent);
                availData.put("display", displayOption);
                availData.put("readonly", true);
                workloadData.put(availData);
            }
            catch (Exception exception) {}
        }
    }

    public PropertyList getListPageDirectives() {
        return this.activityClassHandler != null ? this.activityClassHandler.getListPageDirectives(this.departmentid, this.keyid1, this.activityQueryWhere, this.getSDCProcessor(), this.getTranslationProcessor(), this.pageContext) : new PropertyList();
    }

    public void setPageRender(boolean pagerender) {
        this.pageRender = pagerender;
    }

    public void setMyWork(boolean mywork) {
        this.mywork = mywork;
    }

    public boolean isMyWork() {
        return this.mywork;
    }

    public String getScript() {
        StringBuilder script = new StringBuilder();
        if (this.pageRender) {
            PropertyList pagedata = this.element.getPropertyListNotNull("pagedata");
            script.append("assignment.viewonly = ").append(this.viewOnly).append(";");
            script.append("assignment.colorScheme = '").append(this.getColorScheme().toString()).append("';");
            script.append("assignment.setReturnButton('").append(this.returnButtonId).append("');");
            script.append("assignment.timezone = '").append(TimeZone.getTimeZone(this.displayTimeZone).getID()).append("';");
            script.append("assignment.plannertimezone = '").append(this.connectionInfo.getTimeZone() != null ? TimeZone.getTimeZone(this.connectionInfo.getTimeZone()).toZoneId().getId() : TimeZone.getDefault().toZoneId().getId()).append("';");
            script.append("assignment.timestep = '").append(this.connectionInfo.getTimeZone() != null ? TimeZone.getTimeZone(this.connectionInfo.getTimeZone()).toZoneId().getId() : TimeZone.getDefault().toZoneId().getId()).append("';");
            script.append("assignment.time_step=" + pagedata.getProperty("timestep", "5") + ";");
            JSONObject job = new JSONObject();
            if (this.operatingMode == OperatingMode.WORK) {
                PropertyListCollection operations = pagedata.getCollectionNotNull("operations");
                for (int o = 0; o < operations.size(); ++o) {
                    PropertyList operation = operations.getPropertyList(o);
                    if (!operation.getProperty("enable", "Y").equalsIgnoreCase("Y")) continue;
                    try {
                        if (operation.getProperty("operation").equalsIgnoreCase("Reassign")) {
                            job.put("reassign", true);
                            continue;
                        }
                        if (!operation.getProperty("operation").equalsIgnoreCase("Change Date")) continue;
                        job.put("changedate", true);
                        continue;
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                }
                if (!job.has("reassign")) {
                    try {
                        job.put("reassign", false);
                    }
                    catch (Exception o) {
                        // empty catch block
                    }
                }
                if (!job.has("changedate")) {
                    try {
                        job.put("changedate", false);
                    }
                    catch (Exception o) {}
                }
            } else {
                try {
                    job.put("reassign", true);
                    job.put("changedate", true);
                }
                catch (Exception operations) {
                    // empty catch block
                }
            }
            script.append("assignment.operations=").append(job).append(";");
            script.append("assignment.operatingmode = '").append(this.operatingMode.toString()).append("';");
            script.append("assignment.userConfigPrefix = '").append(this.userConfigPrefix).append("';");
            script.append("assignment.firstDayOfWeek = ").append(this.firstDayOfWeek).append(";");
            M18NUtil m18 = new M18NUtil(this.connectionInfo);
            DateFormat currentFormat = m18.getDefaultDateFormat();
            String p = ((SimpleDateFormat)currentFormat).toPattern();
            if (p.contains("a") && p.contains("h")) {
                script.append("assignment.formats.hour_date='").append("%h:%i %A").append("';");
            } else {
                script.append("assignment.formats.hour_date='").append("%H:%i").append("';");
            }
            script.append("assignment.properties = sapphire.util.propertyList.create(").append(this.getElementProperties() != null && this.getElementProperties().getPropertyList("pagedata") != null ? this.getElementProperties().getPropertyList("pagedata").toJSONString() : "{}").append(");");
        }
        if (this.viewMode != null) {
            script.append(this.viewMode.getScript(this));
        }
        if (this.operatingMode == OperatingMode.ASSIGNMENT) {
            script.append("assignment.focusid = '").append(this.focusid).append("';");
            script.append("assignment.focusedresource = '").append(this.focusedResource != null ? this.focusedResource.getId() : "").append("';");
            script.append("assignment.focusedresourcesdc = '").append(this.focusedResource != null ? this.focusedResource.getResourceSDC().toString() : "").append("';");
            script.append("assignment.shiftid = '").append(this.shiftid).append("';");
            script.append("assignment.departmentid = '").append(this.departmentid).append("';");
            script.append("_lvtop.modernLayout.navigation.updateTitle('").append(this.getTranslationProcessor().translate("Work Assignment")).append("', false);");
        } else {
            if (this.getResourceSDC() != null) {
                String title = "";
                if (this.getResourceSDC() == ResourceSDC.USER && this.mywork) {
                    title = this.getTranslationProcessor().translate("My Work");
                } else if (this.getSDCId().equalsIgnoreCase("LV_Activity")) {
                    title = this.departmentid.length() > 0 ? this.getTranslationProcessor().translate("Work for Testing Lab") + " " + this.departmentid : this.getTranslationProcessor().translate("Work by Activties");
                } else {
                    title = title + this.getResourceSDC().singulartext + " " + this.getTranslationProcessor().translate("work");
                    if (this.keyid1.length() == 0) {
                        if (this.departmentid.length() > 0) {
                            title = title + " " + this.getTranslationProcessor().translate("for") + " " + this.getTranslationProcessor().translate("Testing Lab") + " " + this.departmentid;
                        }
                    } else {
                        title = title + " " + this.getTranslationProcessor().translate("for") + " " + (this.keyid1.length() > 25 ? this.keyid1.substring(0, 23) + "..." : this.keyid1);
                    }
                }
                script.append("_lvtop.modernLayout.navigation.updateTitle('").append(title).append("', false);");
            }
            if (this.pageRender) {
                script.append("$('#sidebar_accordion > .active').accordion({heightStyle:'content',header:'h3',active:0,collapsible:true,activate:function(event,ui){assignment.sidebarSectionChange(event, ui)}});");
                script.append("$('#sidebar_accordion > .unactive').accordion({heightStyle:'content',header:'h3',active:false,collapsible:true,activate:function(event,ui){assignment.sidebarSectionChange(event, ui)}});");
                script.append("$('[name=filterstatus]').chosen({width:'180px'});");
            }
        }
        return script.toString();
    }

    public String getPlannableSDC() {
        return this.activityClassHandler != null ? this.activityClassHandler.getSDC() : null;
    }

    public String getSDCId() {
        if (this.operatingMode == OperatingMode.ASSIGNMENT) {
            return this.getPlannableSDC() != null ? this.getPlannableSDC() : "";
        }
        return this.sdcid != null && this.sdcid.length() > 0 ? this.sdcid : this.getResourceSDC().getName();
    }

    public int getLeftPanel() {
        int out = -1;
        if (this.userConfig != null) {
            try {
                String prop = this.userConfigPrefix + "leftpanel";
                out = Integer.parseInt(this.userConfig.getProperty(prop, "-1"));
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        return out;
    }

    public String getWorkToolbar() throws SapphireException {
        Object[] details;
        AdvancedToolbar tb = new AdvancedToolbar();
        tb.setPageContext(this.pageContext);
        tb.setElementid("advancedtoolbar");
        PropertyList properties = new PropertyList();
        properties.setProperty("displaystyle", "");
        properties.setProperty("showtitle", "N");
        PropertyListCollection buttons = new PropertyListCollection();
        PropertyListCollection pagebuttons = this.getElementProperties().getPropertyList("pagedata").getCollection("buttons");
        TranslationProcessor translationProcessor = this.getTranslationProcessor();
        if (pagebuttons != null) {
            for (int i = 0; i < pagebuttons.size(); ++i) {
                PropertyList button = AssignmentPageButtonType.getButton(pagebuttons.getPropertyList(i), this.viewOnly, translationProcessor);
                if (button.getProperty("assignmentbuttontype").equalsIgnoreCase(AssignmentPageButtonType.RETURN.toString())) {
                    this.returnButtonId = button.getProperty("id", "");
                }
                button.setProperty("buttontype", "User");
                buttons.add(button);
                if (button.getProperty("text").length() != 0) continue;
                button.getPropertyListNotNull("commonprops").setProperty("showtext", "N");
            }
        }
        properties.setProperty("buttons", buttons);
        properties.setProperty("rendermode", "Button");
        tb.setElementProperties(properties);
        StringBuilder html = new StringBuilder();
        html.append(tb.getHtml());
        html.append("<div id=\"selectallcontainer\">");
        TimeZone resourceTimeZone = null;
        String sdcid = this.getSDCId();
        QueryProcessor queryProcessor = new QueryProcessor(this.connectionInfo.getConnectionId());
        String resourceTypeName = "Resource";
        if (this.focusedResource != null && this.focusid.length() > 0) {
            resourceTimeZone = this.focusedResource.getTimeZone(this.focusid);
        } else if (sdcid.equals("User")) {
            details = CalendarFactory.getUserCalendarDetails(queryProcessor, this.getKeyId1());
            resourceTimeZone = (TimeZone)details[1];
        } else if (sdcid.equals("Department")) {
            details = CalendarFactory.getDepartmentCalendarDetails(queryProcessor, this.getKeyId1());
            resourceTimeZone = (TimeZone)details[1];
            resourceTypeName = "WorkArea";
        } else if (sdcid.equals("Instrument")) {
            details = CalendarFactory.getInstrumentCalendarDetails(queryProcessor, this.getKeyId1());
            resourceTimeZone = (TimeZone)details[1];
        } else if (sdcid.equals("LV_Activity")) {
            if (this.getDepartmentId().length() > 0) {
                details = CalendarFactory.getDepartmentCalendarDetails(queryProcessor, this.getDepartmentId());
                resourceTimeZone = (TimeZone)details[1];
            } else {
                details = CalendarFactory.getActivityCalendarDetails(queryProcessor, this.getKeyId1());
                resourceTimeZone = (TimeZone)details[1];
            }
            resourceTypeName = "Department";
        }
        TimeZone displayTimeZone = TimeZone.getTimeZone(this.displayTimeZone);
        DataSet plannerDS = queryProcessor.getPreparedSqlDataSet("SELECT sysuserid, timezone FROM sysuser WHERE sysuserid=?", (Object[])new String[]{this.connectionInfo.getSysuserId()});
        TimeZone plannerTimeZone = plannerDS.getValue(0, "timezone").length() > 0 ? TimeZone.getTimeZone(plannerDS.getValue(0, "timezone")) : TimeZone.getDefault();
        html.append("<div>").append(translationProcessor.translate("Timezone")).append(": &nbsp;</div>");
        html.append("<div><select name=\"assignment_timezone\" id=\"assignment_timezone\" value=\"").append(displayTimeZone.getDisplayName()).append("\" onChange=\"assignment.toggleTimewarp(this)\">");
        if (resourceTimeZone != null && plannerTimeZone.toZoneId().getRules().equals(resourceTimeZone.toZoneId().getRules())) {
            html.append("<option ").append(plannerTimeZone.toZoneId().getRules().equals(displayTimeZone.toZoneId().getRules()) ? "selected " : "").append("value=\"").append(plannerTimeZone.getID()).append("\">").append(plannerTimeZone.toZoneId().getId()).append(translationProcessor.translate(" (You & " + resourceTypeName + ")")).append("</option>");
        } else {
            html.append("<option ").append(plannerTimeZone.toZoneId().getRules().equals(displayTimeZone.toZoneId().getRules()) ? "selected " : "").append("value=\"").append(plannerTimeZone.getID()).append("\">").append(plannerTimeZone.toZoneId().getId()).append(translationProcessor.translate(" (You)")).append("</option>");
            if (resourceTimeZone != null) {
                html.append("<option ").append(resourceTimeZone.toZoneId().getRules().equals(displayTimeZone.toZoneId().getRules()) ? "selected " : "").append("value=\"").append(resourceTimeZone.getID()).append("\">").append(resourceTimeZone.toZoneId().getId()).append(translationProcessor.translate(" (" + resourceTypeName + ")")).append("</option>");
            }
        }
        html.append("</select></div>&nbsp;");
        html.append("<div>Select&nbsp;&nbsp;</div><div><select style=\"width:98px;\" id=\"selectalldropdown\" onchange=\"assignment.selectAllChange(this)\">");
        html.append("<option value=\"draft\">").append(translationProcessor.translate("Draft")).append("</option>");
        html.append("<option value=\"started\">").append(translationProcessor.translate("Started")).append("</option>");
        html.append("<option value=\"mywork\">").append(translationProcessor.translate("My Work")).append("</option>");
        html.append("<option value=\"pastduedate\">").append(translationProcessor.translate("Past Due Date")).append("</option>");
        html.append("<option value=\"unassigned\">").append(translationProcessor.translate("Unassigned")).append("</option>");
        html.append("<option value=\"all\">").append(translationProcessor.translate("All")).append("</option>");
        html.append("<option value=\"\" SELECTED>").append(translationProcessor.translate("Nothing")).append("</option>");
        html.append("</select></div></div>");
        return html.toString();
    }

    public void setClientFromDate(ZonedDateTime clientFromDate) {
        this.clientFromDate = clientFromDate;
    }

    public void setClientToDate(ZonedDateTime clientToDate) {
        this.clientToDate = clientToDate;
    }

    public void setClientDateRange(AssignmentPageTimeRange timeRange) {
        this.clientFromDate = timeRange.from;
        this.clientToDate = timeRange.to;
    }

    public void setCalendarView(CalendarView calendarView) {
        this.calendarView = calendarView;
    }

    public String getFrameTop() {
        if (this.selectorMode != SelectorMode.NONE) {
            return this.selectorMode == SelectorMode.TABS || this.selectorMode == SelectorMode.LINKS ? "70px" : "35px";
        }
        return "35px";
    }

    private void renderSelectionOption(StringBuilder menu, ActivityClassHandler currentActivityClass, int count) {
        if (this.selectorMode == SelectorMode.DROPDOWN) {
            menu.append("<option value=\"").append(currentActivityClass.id).append("\"").append(currentActivityClass.getId().equalsIgnoreCase(this.activityClassHandler.getId()) ? " selected" : "").append(">");
            String s = currentActivityClass.getLabel();
            menu.append(s).append(" (").append(count).append(")");
            menu.append("</option>");
        } else {
            menu.append("<span id=\"tab_").append(currentActivityClass.getId()).append("\" onclick=\"assignment.changePlanList('").append(currentActivityClass.getId()).append("');\" class=\"");
            menu.append("selectormode_").append(this.selectorMode.toString().toLowerCase());
            if (this.selectorMode == SelectorMode.TABS) {
                menu.append(" tab_text");
                if (currentActivityClass.getId().equalsIgnoreCase(this.activityClassHandler.getId())) {
                    menu.append(" _selected");
                }
            }
            menu.append("\">");
            String s = currentActivityClass.getLabel();
            menu.append(s).append(" (").append(count).append(")");
            menu.append("</span>");
        }
    }

    public String getLeftSelector() throws SapphireException {
        String depid;
        int r;
        StringBuilder html = new StringBuilder();
        StringBuilder details = new StringBuilder();
        StringBuilder dephtml = new StringBuilder();
        PropertyList pagedata = this.element.getPropertyList("pagedata");
        try {
            this.selectorMode = SelectorMode.valueOf(pagedata.getProperty("activityclassselectormode", "None").toUpperCase());
        }
        catch (Exception exception) {
            // empty catch block
        }
        boolean selectDep = !pagedata.getProperty("selectdepartment", "Y").equalsIgnoreCase("N");
        String testinglabtype = "";
        dephtml.append("<div class=\"titletext\">");
        dephtml.append(this.getTranslationProcessor().translate("Testing Lab")).append(":");
        dephtml.append("&nbsp;");
        dephtml.append("<select name=\"departmentsel\" id=\"departmentsel\" style=\"width:130px;\" onchange=\"assignment.selectorsChange(this)\"").append(!selectDep ? " disabled" : "").append(">");
        if (this.keyid1.length() > 0) {
            WAPCommands wapCommands = new WAPCommands(this.getConnectionId());
            DataSet deps = wapCommands.getNewWorkTestingDepartments(this.getPlannableSDC().equals("LV_Activity") ? "LV_Activity" : this.getPlannableSDC(), this.keyid1);
            if (deps != null) {
                for (r = 0; r < deps.getRowCount(); ++r) {
                    depid = deps.getValue(r, "testingdepartmentid", "");
                    if (this.usersdepartments != null && !this.usersdepartments.contains(depid)) continue;
                    boolean selected = false;
                    if (this.departmentid == null || this.departmentid.length() == 0) {
                        this.departmentid = depid;
                        selected = true;
                        testinglabtype = deps.getValue(r, "testinglabtype", "");
                    } else if (this.departmentid.equals(depid)) {
                        testinglabtype = deps.getValue(r, "testinglabtype", "");
                        selected = true;
                    }
                    dephtml.append("<option value=\"").append(SafeHTML.encodeForHTMLAttribute(depid)).append("\"").append(selected ? " selected" : "").append(">");
                    dephtml.append(SafeHTML.encodeForHTML(depid));
                    dephtml.append("</option>");
                }
            }
        } else {
            SDIRequest departmentReq = new SDIRequest();
            departmentReq.setSDCid("Department");
            departmentReq.setRequestItem("primary");
            departmentReq.setRetainRsetid(false);
            departmentReq.setQueryFrom("department");
            departmentReq.setQueryWhere("testingflag='Y'");
            SDIData depData = this.getSDIProcessor().getSDIData(departmentReq);
            if (depData != null && depData.getDataset("primary") != null) {
                for (r = 0; r < depData.getDataset("primary").getRowCount(); ++r) {
                    depid = depData.getDataset("primary").getValue(r, "departmentid", "");
                    if (this.usersdepartments != null && !this.usersdepartments.contains(depid)) continue;
                    String desc = depData.getDataset("primary").getValue(r, "departmentdesc", "");
                    if (desc.length() == 0) {
                        desc = depid;
                    }
                    boolean selected = false;
                    if (this.departmentid == null || this.departmentid.length() == 0) {
                        this.departmentid = depid;
                        selected = true;
                        testinglabtype = depData.getDataset("primary").getValue(r, "testinglabtype", "");
                    } else if (this.departmentid.equals(depid)) {
                        selected = true;
                        testinglabtype = depData.getDataset("primary").getValue(r, "testinglabtype", "");
                    }
                    dephtml.append("<option value=\"").append(SafeHTML.encodeForHTMLAttribute(depid)).append("\"").append(selected ? " selected" : "").append(">");
                    dephtml.append(SafeHTML.encodeForHTML(desc));
                    dephtml.append("</option>");
                }
            }
        }
        dephtml.append("</select>");
        dephtml.append("</div>");
        if (!(this.selectorMode == SelectorMode.NONE || this.keyid1 != null && this.keyid1.length() != 0 || this.activityQueryWhere != null && this.activityQueryWhere.length() != 0)) {
            boolean hideZeros = this.element.getPropertyList("pagedata").getProperty("hidezerocount", "N").equalsIgnoreCase("Y");
            boolean hideOne = false;
            PropertyList wapPolicy = this.getWapPolicy();
            List<String> allClasses = ActivityClassHandler.getAllClasses(wapPolicy);
            if (allClasses.size() > 0) {
                int processed = 0;
                StringBuilder menu = new StringBuilder();
                for (String activityClass : allClasses) {
                    if (activityClass.length() <= 0) continue;
                    try {
                        int c;
                        ActivityClassHandler currentActivityClass = ActivityClassHandler.getInstance(this.getConnectionid(), wapPolicy, activityClass);
                        boolean continueGoing = false;
                        if (currentActivityClass.getTestingLab().length() > 0) {
                            if (this.departmentid.length() > 0 && this.departmentid.equalsIgnoreCase(currentActivityClass.getTestingLab())) {
                                continueGoing = true;
                            }
                        } else if (currentActivityClass.getTestingLabType().length() > 0) {
                            if (testinglabtype.length() > 0 && testinglabtype.equalsIgnoreCase(currentActivityClass.getTestingLabType())) {
                                continueGoing = true;
                            }
                        } else {
                            continueGoing = true;
                        }
                        if (!continueGoing || (c = currentActivityClass.getCount(this.departmentid, this.keyid1)) <= 0 && hideZeros) continue;
                        this.renderSelectionOption(menu, currentActivityClass, c);
                        ++processed;
                    }
                    catch (Exception e) {
                        this.logger.warn("Invalid plannable SDC provided.");
                    }
                }
                try {
                    ActivityClassHandler activityActClass = this.getActivityClassHandler(ACTIVITY_ACTIVITYCLASSID, "LV_Activity");
                    int ac = activityActClass.getCount(this.departmentid, this.keyid1);
                    if (ac > 0 || !hideZeros) {
                        this.renderSelectionOption(menu, activityActClass, ac);
                    }
                }
                catch (SapphireException e) {
                    this.logger.warn(e.getMessage());
                }
                if (!hideOne || processed > 1) {
                    if (this.selectorMode == SelectorMode.DROPDOWN) {
                        details.append(this.getTranslationProcessor().translate("Plannable Work")).append(": <select name=\"planableitemsselector\" id=\"planableitemsselector\" onchange=\"assignment.selectorChange(this)\">");
                        details.append((CharSequence)menu);
                        details.append("</select>");
                    } else {
                        details.append((CharSequence)menu);
                    }
                }
            }
        }
        if (dephtml.length() > 0 && (this.selectorMode == SelectorMode.LINKS || this.selectorMode == SelectorMode.TABS)) {
            if (this.fullContent) {
                html.append("<div id=\"leftheader\">");
            }
            html.append((CharSequence)dephtml);
            if (this.fullContent) {
                html.append("</div>");
            }
        }
        if (details.length() > 0) {
            if (this.fullContent) {
                html.append("<div id=\"leftheader\">");
            }
            if (dephtml.length() > 0 && (this.selectorMode == SelectorMode.DROPDOWN || this.selectorMode == SelectorMode.NONE)) {
                html.append((CharSequence)dephtml).append("&nbsp;&nbsp;");
            }
            html.append("<div class=\"titletext\">");
            html.append((CharSequence)details);
            html.append("</div>");
            this.addButtons(html);
            if (this.fullContent) {
                html.append("</div>");
            }
        } else {
            this.selectorMode = SelectorMode.NONE;
            pagedata.setProperty("activityclassselectormode", SelectorMode.NONE.toString());
            if (dephtml.length() > 0 && (this.selectorMode == SelectorMode.DROPDOWN || this.selectorMode == SelectorMode.NONE)) {
                if (this.fullContent) {
                    html.append("<div id=\"leftheader\">");
                }
                html.append((CharSequence)dephtml);
                this.addButtons(html);
                if (this.fullContent) {
                    html.append("</div>");
                }
            }
        }
        return html.toString();
    }

    private void addButtons(StringBuilder html) {
        html.append("<div class=\"titletext\">");
        Button refreshButton = new Button(this.pageContext);
        refreshButton.setId("btRefresh");
        refreshButton.setImg("rc?command=image&image=FlatBlackRefresh1");
        refreshButton.setText("");
        refreshButton.setTip(this.getTranslationProcessor().translate("Refresh the list"));
        refreshButton.setAction("assignment.refreshPlanList()");
        html.append(refreshButton.getHtml());
        html.append("&nbsp;");
        Button backBtn = new Button(this.pageContext);
        this.returnButtonId = "btBack";
        backBtn.setId(this.returnButtonId);
        backBtn.setImg("rc?command=image&image=FlatBlackArrowLeft");
        backBtn.setText("");
        backBtn.setTip(this.getTranslationProcessor().translate("Return to previous page"));
        backBtn.setAction("_lvtop.modernLayout.navigation.goBack()");
        html.append(backBtn.getHtml());
        if (this.element.getPropertyListNotNull("pagedata").getProperty("showplanpage").length() > 0) {
            html.append("&nbsp;");
            Button showPlanButton = new Button(this.pageContext);
            showPlanButton.setId("btShowPlan");
            showPlanButton.setImg("rc?command=image&image=FlatBlackPeopleMultipleMagnify");
            showPlanButton.setText(this.getTranslationProcessor().translate("Show Plan"));
            showPlanButton.setTip(this.getTranslationProcessor().translate("Show draft activities for this Testing Lab"));
            showPlanButton.setAction("assignment.showPlan('" + this.element.getPropertyListNotNull("pagedata").getProperty("showplanpage") + "','" + this.element.getPropertyListNotNull("pagedata").getProperty("showplantarget") + "')");
            html.append(showPlanButton.getHtml());
        }
        html.append("</div>");
    }

    private String getAreaSelector() {
        StringBuilder html = new StringBuilder();
        html.append("<div id=\"rightsubheader\">");
        if (!this.fullContent) {
            if (this.clientFromDate == null || this.clientToDate == null) {
                this.clientToDate = this.clientFromDate = ZonedDateTime.now(this.displayTimeZone);
            }
            String selectedSDC = "";
            try {
                selectedSDC = this.selection.getString(this.focusedResource.getId() + "_sdcid");
            }
            catch (Exception exception) {
                // empty catch block
            }
            html.append(AssignmentPageUtil.getAreaSelector(this.viewMode, this.clientFromDate, this.clientToDate, this.displayTimeZone, this.departmentid, "", this.focusedResource, this.getResourceData(), this.getFocusId() != null ? this.getFocusId() : "", this.getSelectedResource(this.focusedResource.getId()), selectedSDC, this.getSDCId(), this.userConfig, this.userConfigPrefix, this.getElementProperties(), this.getOperatingMode(), this.getColorScheme(), this.getSDIProcessor(), this.getTranslationProcessor(), this.getConnectionProcessor().getSapphireConnection(), this.pageContext, this.displayTimeZone));
        }
        html.append("</div>");
        return html.toString();
    }

    public String getLoaderHtml() {
        String image = ModernLayout.getLoaderImage(new ConfigurationProcessor(this.pageContext));
        return "<div id=\"loader\" style=\"display:none;background-image:url('" + image + "');\"></div>";
    }

    @Override
    public String getHtml() {
        StringBuilder html = new StringBuilder();
        this.displayTimeZone = ZoneOffset.UTC;
        if (this.connectionInfo.getTimeZone() != null && this.connectionInfo.getTimeZone().length() > 0) {
            try {
                try {
                    this.displayTimeZone = TimeZone.getTimeZone(this.connectionInfo.getTimeZone()).toZoneId();
                }
                catch (Exception e1) {
                    this.displayTimeZone = I18nUtil.getZoneIdFromString(this.connectionInfo.getTimeZone());
                }
            }
            catch (Exception e) {
                this.logger.error("Failed to find timezone", e);
            }
        } else {
            this.displayTimeZone = TimeZone.getDefault().toZoneId();
        }
        if (this.operatingMode == OperatingMode.WORK && this.viewMode != null) {
            this.viewMode = ViewMode.CALENDAR;
            html.append(this.viewMode.getHTML(this));
        }
        return html.toString();
    }

    public CalendarView getCalendarView() {
        if (this.calendarView == null) {
            String prop = this.userConfigPrefix + "calendarview";
            if (this.userConfig != null && this.userConfig.getProperty(prop).length() > 0) {
                CalendarView out = CalendarView.MONTH;
                try {
                    out = CalendarView.valueOf(this.userConfig.getProperty(prop).toUpperCase());
                }
                catch (Exception exception) {
                    // empty catch block
                }
                this.setCalendarView(out);
                return out;
            }
            return this.operatingMode == OperatingMode.ASSIGNMENT ? CalendarView.MONTH : CalendarView.RESOURCE_TIMELINE;
        }
        return this.calendarView;
    }

    public ViewMode getViewMode() {
        if (this.viewMode == null) {
            String prop = this.userConfigPrefix + "viewmode";
            if (this.userConfig != null && this.userConfig.getProperty(prop).length() > 0) {
                ViewMode out = ViewMode.RESOURCE;
                try {
                    out = ViewMode.valueOf(this.userConfig.getProperty(prop).toUpperCase());
                }
                catch (Exception exception) {
                    // empty catch block
                }
                this.setViewMode(out);
                return out;
            }
            return ViewMode.RESOURCE;
        }
        return this.viewMode;
    }

    public boolean isShowingSidebar() {
        String prop = this.userConfigPrefix + "sidebar";
        return this.userConfig != null && this.userConfig.getProperty(prop, "Y").equalsIgnoreCase("Y");
    }

    public String getSidebarHtml() {
        String rsdc;
        StringBuilder html = new StringBuilder();
        html.append("<div id=\"sidebar_accordion\">");
        PropertyList filters = null;
        if (this.getElementProperties() != null && this.getElementProperties().getPropertyList("pagedata") != null && this.getElementProperties().getPropertyList("pagedata").getPropertyList("filters") != null) {
            filters = this.getElementProperties().getPropertyList("pagedata").getPropertyList("filters");
        }
        if (this.getElementProperties() != null && this.getElementProperties().getPropertyList("pagedata") != null && (rsdc = this.element.getProperty("resourcesdcid", this.element.getPropertyList("pagedata").getProperty("resourcesdcid", this.element.getProperty("resourcetype", this.element.getPropertyList("pagedata").getProperty("resourcetype", ""))))).length() == 0 && !this.getSDCId().equalsIgnoreCase(ResourceSDC.USER.getName()) && !this.getSDCId().equalsIgnoreCase(ResourceSDC.INSTRUMENT.getName())) {
            html.append("<div class=\"").append(this.userConfig != null && this.userConfig.getProperty(this.userConfigPrefix + "sidebar_section_resource").equalsIgnoreCase("Y") ? "active" : "unactive").append("\">");
            html.append("<h3 data-section=\"resource\">").append(this.getTranslationProcessor().translate("Resource Selection")).append("</h3>");
            html.append("<div class=\"acordian_content\">");
            html.append("<div id=\"resourceSection\">");
            html.append("</div>");
            html.append("</div>");
            html.append("</div>");
        }
        html.append("<div class=\"").append(this.userConfig != null && this.userConfig.getProperty(this.userConfigPrefix + "sidebar_section_filter").equalsIgnoreCase("Y") ? "active" : "unactive").append("\">");
        html.append("<h3 data-section=\"filter\">").append(this.getTranslationProcessor().translate("Activity Filter")).append("</h3>");
        html.append("<div class=\"acordian_content\">");
        String statusFilter = this.userConfig.getProperty(this.userConfigPrefix + "sidebar_status");
        if (statusFilter.length() == 0) {
            statusFilter = filters != null ? filters.getProperty("status") : "";
        }
        statusFilter = statusFilter.equalsIgnoreCase("All") ? "" : statusFilter.toLowerCase();
        ArrayList<String> statusFilters = statusFilter.length() > 0 ? new ArrayList<String>(Arrays.asList(StringUtil.split(statusFilter, ";"))) : new ArrayList();
        html.append("<div>");
        html.append(this.getTranslationProcessor().translate("Filter Status")).append(" ").append("<br>");
        html.append("<select name=\"filterstatus\" onchange=\"assignment.sidebarChange(this);\" multiple>");
        html.append("<option value=\"Draft\"").append(statusFilters.contains("draft") ? " selected" : "").append(">").append("Draft").append("</option>");
        html.append("<option value=\"Activated\"").append(statusFilters.contains("activated") ? " selected" : "").append(">").append("Activated").append("</option>");
        html.append("<option value=\"In Progress\"").append(statusFilters.contains("in progress") ? " selected" : "").append(">").append("In Progress").append("</option>");
        html.append("<option value=\"Completed\"").append(statusFilters.contains("completed") ? " selected" : "").append(">").append("Completed").append("</option>");
        html.append("<option value=\"Cancelled\"").append(statusFilters.contains("cancelled") ? " selected" : "").append(">").append("Cancelled").append("</option>");
        html.append("</select>");
        html.append("</div>");
        String timemodeFilter = this.userConfig.getProperty(this.userConfigPrefix + "sidebar_timemode");
        if (timemodeFilter.length() == 0) {
            timemodeFilter = filters != null ? filters.getProperty("timemode") : "";
        }
        timemodeFilter = timemodeFilter.equalsIgnoreCase("All") ? "" : timemodeFilter;
        html.append("<div>");
        html.append(this.getTranslationProcessor().translate("Filter Timemode")).append(" ").append("<br>");
        html.append("<select name=\"filtertimemode\" onchange=\"assignment.sidebarChange(this);\">");
        html.append("<option value=\"All\"").append(timemodeFilter.length() == 0 ? " selected" : "").append(">").append(this.getTranslationProcessor().translate("All")).append("</option>");
        html.append("<option value=\"Fixed\"").append(timemodeFilter.equalsIgnoreCase("fixed") ? " selected" : "").append(">").append(this.getTranslationProcessor().translate("Fixed")).append("</option>");
        html.append("<option value=\"Floating\"").append(timemodeFilter.equalsIgnoreCase("floating") ? " selected" : "").append(">").append(this.getTranslationProcessor().translate("Floating")).append("</option>");
        html.append("</select>");
        html.append("</div>");
        String duedateFilter = this.userConfig.getProperty(this.userConfigPrefix + "sidebar_duedate");
        if (duedateFilter.length() == 0) {
            duedateFilter = filters != null ? filters.getProperty("duedate") : "";
        }
        duedateFilter = duedateFilter.equalsIgnoreCase("All") ? "" : duedateFilter;
        html.append("<div>");
        html.append(this.getTranslationProcessor().translate("Filter Due Dates")).append(" ").append("<br>");
        html.append("<select name=\"filterduedate\" onchange=\"assignment.sidebarChange(this);\">");
        html.append("<option value=\"All\"").append(duedateFilter.length() == 0 ? " selected" : "").append(">").append(this.getTranslationProcessor().translate("All")).append("</option>");
        html.append("<option value=\"Past Due Date\"").append(duedateFilter.equalsIgnoreCase("Past Due Date") ? " selected" : "").append(">").append(this.getTranslationProcessor().translate("Past Due Date")).append("</option>");
        html.append("<option value=\"In Due Date\"").append(duedateFilter.equalsIgnoreCase("In Due Date") ? " selected" : "").append(">").append(this.getTranslationProcessor().translate("In Due Date")).append("</option>");
        html.append("<option value=\"Has Due Date\"").append(duedateFilter.equalsIgnoreCase("Has Due Date") ? " selected" : "").append(">").append(this.getTranslationProcessor().translate("Has Due Date")).append("</option>");
        html.append("<option value=\"Has No Due Date\"").append(duedateFilter.equalsIgnoreCase("Has No Due Date") ? " selected" : "").append(">").append(this.getTranslationProcessor().translate("Has No Due Date")).append("</option>");
        html.append("</select>");
        html.append("</div>");
        html.append("</div>");
        html.append("</div>");
        PropertyList displayoptions = null;
        if (this.getElementProperties() != null && this.getElementProperties().getPropertyList("pagedata") != null && this.getElementProperties().getPropertyList("pagedata").getPropertyList("displayoptions") != null) {
            displayoptions = this.getElementProperties().getPropertyList("pagedata").getPropertyList("displayoptions");
        }
        html.append("<div class=\"").append(this.userConfig != null && this.userConfig.getProperty(this.userConfigPrefix + "sidebar_section_display").equalsIgnoreCase("Y") ? "active" : "unactive").append("\">");
        html.append("<h3 data-section=\"display\">").append(this.getTranslationProcessor().translate("Display Options")).append("</h3>");
        html.append("<div class=\"acordian_content\">");
        html.append(AssignmentPageUtil.getDisplayOptions(displayoptions, this.getSDCId(), this.userConfig, this.userConfigPrefix, this.getOperatingMode(), this.getColorScheme(), true, this.getTranslationProcessor()));
        html.append("</div>");
        html.append("</div>");
        html.append("<div class=\"").append(this.userConfig != null && this.userConfig.getProperty(this.userConfigPrefix + "sidebar_section_legend").equalsIgnoreCase("Y") ? "active" : "unactive").append("\">");
        html.append("<h3 data-section=\"legend\">").append(this.getTranslationProcessor().translate("Legend")).append("</h3>");
        html.append("<div class=\"acordian_content\">");
        if (this.getColorScheme() != ColorScheme.NONE) {
            String colorbyDisplayOp = this.userConfig.getProperty(this.userConfigPrefix + "sidebar_colorby");
            if (colorbyDisplayOp.length() == 0) {
                colorbyDisplayOp = displayoptions != null ? displayoptions.getProperty("colorby", "Resource") : "Resource";
            }
            html.append(this.getTranslationProcessor().translate("Event Colors"));
            html.append("<div id=\"eventColors\" class=\"eventColors\">");
            html.append(AssignmentPageUtil.getEventColorsKey(colorbyDisplayOp, this.getFocusedResource(), this.getTranslationProcessor()));
            html.append("</div>");
        }
        if (this.getColorScheme() != ColorScheme.NONE) {
            html.append(this.getTranslationProcessor().translate("Workload Colors"));
            html.append("<div>");
            html.append(AssignmentPageUtil.getWorkloadColorsKey(this.getTranslationProcessor()));
            html.append("</div>");
        }
        html.append(this.getTranslationProcessor().translate("Icons"));
        html.append("<div>");
        html.append(AssignmentPageUtil.getEventIconsKey(this.getTranslationProcessor()));
        html.append("</div>");
        html.append("</div>");
        html.append("</div>");
        html.append("</div>");
        return html.toString();
    }

    private String getCalendarHTML(boolean hide) {
        StringBuilder html = new StringBuilder();
        String viewmodecss = this.viewMode != null ? " viewmode_" + this.viewMode.toString().toLowerCase() : "";
        String calmodecss = this.calendarView != null ? " dhx_scheduler_" + this.calendarView.toString().toLowerCase() : "";
        html.append("<div id=\"scheduler_here\" class=\"dhx_cal_container").append(viewmodecss).append(calmodecss).append("\" style='width:100%; height:100%;'>");
        html.append("<div class=\"dhx_cal_navline\">");
        html.append("<div class=\"dhx_cal_prev_button\">&nbsp;</div>");
        html.append("<div class=\"dhx_cal_next_button\">&nbsp;</div>");
        html.append("<div class=\"dhx_cal_today_button\"></div>");
        html.append("<div class=\"dhx_cal_date\"></div>");
        html.append("<div class=\"dhx_cal_tab\" name=\"day_tab\" style=\"right:auto;left:0px;\"></div>");
        html.append("<div class=\"dhx_cal_tab\" name=\"week_tab\" style=\"right:auto;left:50px;\"></div>");
        html.append("<div class=\"dhx_cal_tab\" name=\"month_tab\" style=\"right:auto;left:100px;\"></div>");
        html.append("<div class=\"dhx_cal_tab\" id=\"resource_week_tab\" name=\"resource_week_tab\" style=\"right:auto;left:150px;display:none;\"></div>");
        html.append("<div class=\"dhx_cal_tab\" id=\"resource_timeline_tab\" name=\"resource_timeline_tab\" style=\"right:auto;left:200px;display:none;\"></div>");
        if (this.operatingMode == OperatingMode.WORK) {
            html.append("<div class=\"dhx_cal_tab\" id=\"agenda_tab\" name=\"agenda_tab\" style=\"right:auto;left:250px;\"></div>");
        }
        html.append("</div>");
        if (hide) {
            html.append("<div class=\"dhx_cal_header\" style=\"display:none;\"></div>");
            html.append("<div class=\"dhx_cal_data\" style=\"display:none;\"></div>");
        } else {
            html.append("<div class=\"dhx_cal_header\"></div>");
            html.append("<div class=\"dhx_cal_data\"></div>");
        }
        html.append("</div>");
        return html.toString();
    }

    public String getSelectionIcons() {
        return this.getSelectionIcons(true, true);
    }

    public String getSelectionIcons(boolean renderContainer, boolean renderContent) {
        boolean showText = !this.element.getPropertyList("pagedata").getProperty("resourceindicator").equalsIgnoreCase("Icons");
        return this.getSelectionIcons(renderContainer, renderContent, showText);
    }

    public String getSelectionIcons(boolean renderContainer, boolean renderContent, boolean showText) {
        StringBuilder html = new StringBuilder();
        if (renderContainer) {
            html.append("<div id=\"selectionicons\">");
        }
        if (renderContent) {
            boolean found = false;
            AssignmentPageResourceData resourceData = this.getResourceData();
            if (resourceData != null && resourceData.getResources() != null && resourceData.getResources().size() > 0) {
                if (this.getPlannableSDC() != null && !this.getPlannableSDC().equals("LV_Activity")) {
                    String caltext;
                    boolean hasDateSelection;
                    Image calimage = new Image(this.pageContext);
                    calimage.setImageId("FlatBlackCalendarWeek");
                    String caltitle = "";
                    calimage.setTitle(caltitle);
                    calimage.setDimensions(16, 16);
                    if (this.viewMode == ViewMode.CALENDAR) {
                        calimage.setColor("#255d92");
                    }
                    JSONObject selection = this.getSelection();
                    String startms = selection.optString("startms", "");
                    ZonedDateTime selectionStart = !selection.has("start") || selection.isNull("start") || startms.length() == 0 ? null : CalendarConverter.getZonedDateTimeFromClientIso(startms, this.displayTimeZone, this.clientOffsetMinutes);
                    String endms = selection.optString("endms", "");
                    ZonedDateTime selectionEnd = !selection.has("end") || selection.isNull("end") || endms.length() == 0 ? null : CalendarConverter.getZonedDateTimeFromClientIso(endms, this.displayTimeZone, this.clientOffsetMinutes);
                    boolean bl = hasDateSelection = selectionStart != null || selectionEnd != null;
                    if (!hasDateSelection) {
                        selectionStart = this.clientFromDate;
                        selectionEnd = this.clientToDate;
                    }
                    if (showText) {
                        if (this.connectionInfo == null) {
                            this.connectionInfo = this.getConnectionProcessor().getConnectionInfo(this.getConnectionId());
                        }
                        M18NUtil m18 = new M18NUtil(this.connectionInfo);
                        m18.setTimeZone(TimeZone.getDefault());
                        DateTimeFormatter dateTimeFormatter = m18.getDateTimeFormatter(this.displayTimeZone);
                        DateTimeFormatter timeFormatter = m18.getTimeFormatter(this.displayTimeZone);
                        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMMM").withZone(this.displayTimeZone).withLocale(m18.getLocale());
                        DateTimeFormatter dayMonthFormatter = DateTimeFormatter.ofPattern("d MMM").withZone(this.displayTimeZone).withLocale(m18.getLocale());
                        if (selectionStart != null && selectionEnd != null) {
                            if (selectionStart.compareTo(selectionEnd) == 0) {
                                AssignmentPageTimeRange timeRange = new AssignmentPageTimeRange(selectionStart, selectionEnd);
                                AssignmentPageUtil.adjustRangeToCalendarView(this.calendarView, timeRange, true, m18);
                                selectionStart = timeRange.from;
                                selectionEnd = timeRange.to;
                            }
                            String preDateRange = " (" + dayMonthFormatter.format(selectionStart) + " - " + dayMonthFormatter.format(selectionEnd) + ")";
                            if (selectionEnd.get(ChronoField.HOUR_OF_DAY) == 23 && selectionEnd.get(ChronoField.MINUTE_OF_HOUR) == 59) {
                                selectionEnd = selectionEnd.with(LocalTime.of(0, 0, 0)).plus(1L, ChronoUnit.DAYS);
                            }
                            int daysBetween = (int)ChronoUnit.DAYS.between(selectionStart, selectionEnd);
                            String timeRange = ": (" + dateTimeFormatter.format(selectionStart) + "-" + timeFormatter.format(selectionEnd) + ")";
                            int weekNumber = selectionStart.get(WeekFields.of(m18.getLocale()).weekOfWeekBasedYear());
                            if ((daysBetween == 7 || daysBetween == 5) && selectionStart.getDayOfWeek() == WeekFields.of(m18.getLocale()).getFirstDayOfWeek()) {
                                caltext = this.getTranslationProcessor().translate("Week") + " " + weekNumber;
                                caltitle = caltitle + caltext + (hasDateSelection ? " " + this.getTranslationProcessor().translate("Selected") : "") + preDateRange;
                            } else if (daysBetween == 14 && selectionStart.getDayOfWeek() == WeekFields.of(m18.getLocale()).getFirstDayOfWeek()) {
                                caltext = this.getTranslationProcessor().translate("Week") + " " + weekNumber + "-" + (weekNumber + 1);
                                caltitle = caltitle + caltext + (hasDateSelection ? " " + this.getTranslationProcessor().translate("Selected") : "") + preDateRange;
                            } else if (selectionStart.getYear() == selectionEnd.getYear() && selectionStart.getDayOfMonth() == 1 && selectionEnd.getDayOfMonth() == 1 && selectionStart.getMonth().getValue() == selectionEnd.getMonth().getValue() - 1 && selectionStart.getHour() == 0 && selectionStart.getMinute() == 0 && selectionEnd.getHour() == 0 && selectionEnd.getMinute() == 0) {
                                caltext = monthFormatter.format(selectionStart);
                                caltitle = caltitle + caltext + (hasDateSelection ? " " + this.getTranslationProcessor().translate("Selected") : "");
                            } else if (daysBetween == 1 && selectionStart.getHour() == 0 && selectionStart.getMinute() == 0 && selectionEnd.getHour() == 0 && selectionEnd.getMinute() == 0) {
                                caltext = dayMonthFormatter.format(selectionStart);
                                caltitle = caltitle + caltext + (hasDateSelection ? " " + this.getTranslationProcessor().translate("Selected") : "");
                            } else if (daysBetween == 0) {
                                caltext = dayMonthFormatter.format(selectionStart) + " (" + timeFormatter.format(selectionStart) + "-" + timeFormatter.format(selectionEnd) + ")";
                                caltitle = caltitle + (hasDateSelection ? " " + this.getTranslationProcessor().translate("Range Selected") : "") + timeRange;
                            } else if (daysBetween > 1) {
                                caltext = this.getTranslationProcessor().translate("Range");
                                caltitle = caltitle + (hasDateSelection ? " " + this.getTranslationProcessor().translate("Range Selected") : "") + preDateRange;
                            } else {
                                caltext = this.getTranslationProcessor().translate("Range");
                                caltitle = caltitle + (hasDateSelection ? " " + this.getTranslationProcessor().translate("Range Selected") : "") + timeRange;
                            }
                        } else if (selectionStart != null) {
                            caltext = this.getTranslationProcessor().translate("Single Day");
                            caltitle = caltitle + caltext + " " + this.getTranslationProcessor().translate("Selected") + ": " + dayMonthFormatter.format(selectionStart);
                        } else {
                            caltext = this.getTranslationProcessor().translate("Current Range");
                            caltitle = caltitle + this.getTranslationProcessor().translate("No Dates") + " " + this.getTranslationProcessor().translate("Selected");
                        }
                    } else {
                        caltext = selectionStart != null && selectionEnd != null ? "1" : (selectionStart != null ? "1" : "0");
                    }
                    calimage.setTitle(caltitle);
                    html.append("<div id=\"sel_").append("calendar").append("\" class=\"selicon").append("\" title=\"").append(caltitle).append("\" onclick=\"assignment.changeView('").append((Object)ViewMode.CALENDAR).append("'").append(")\">");
                    html.append(calimage.getHtml());
                    html.append("<span style=\"margin-left: 4px;\">");
                    html.append(caltext);
                    html.append("</span>");
                    html.append("</div>");
                }
                for (int i = 0; i < resourceData.getResources().size(); ++i) {
                    AssignmentPageResourceContainer resource = resourceData.getResources().get(i);
                    if (resource.getData() == null || resource.getResourceSDC() != ResourceSDC.INSTRUMENT && !resource.getType().equalsIgnoreCase("Analyst")) continue;
                    found = true;
                    int selected = 0;
                    JSONArray sel = this.getSelectedResource(resource.getId());
                    if (sel != null) {
                        selected = sel.length();
                    }
                    String selSDC = "";
                    try {
                        selSDC = this.selection != null && this.selection.has(resource.getId() + "_sdcid") ? this.selection.getString(resource.getId() + "_sdcid") : "";
                    }
                    catch (Exception selectionEnd) {
                        // empty catch block
                    }
                    String iconid = resource.getResourceSDC() == ResourceSDC.INSTRUMENT ? (selSDC.equalsIgnoreCase(ResourceSDC.INSTRUMENT.getName()) || selected == 0 ? "FlatBlackThermometer2" : "FlatBlackThermometerGroup") : (selSDC.equalsIgnoreCase(ResourceSDC.USER.getName()) || selected == 0 ? "FlatBlackPeople" : "FlatBlackGroup");
                    Image image = new Image(this.pageContext);
                    image.setImageId(iconid);
                    if (this.viewMode == ViewMode.RESOURCE && this.focusedResource.getId().equals(resource.getId())) {
                        image.setColor("#255d92");
                    }
                    String title = resource.getLabel();
                    if (resource.getType().length() > 0 && resource.getModel().length() > 0) {
                        title = title + " " + this.getTranslationProcessor().translate("of type");
                        title = title + " " + resource.getType();
                        title = title + " " + this.getTranslationProcessor().translate("and model");
                        title = title + " " + resource.getModel();
                    } else if (resource.getType().length() > 0) {
                        title = title + " " + this.getTranslationProcessor().translate("of type");
                        title = title + " " + resource.getType();
                    } else if (resource.getModel().length() > 0) {
                        title = title + " " + this.getTranslationProcessor().translate("of model");
                        title = title + " " + resource.getModel();
                    }
                    if (selected > 0) {
                        title = selSDC.equalsIgnoreCase(resource.getResourceSDC().getName()) ? title + " " + this.getTranslationProcessor().translate("has") + " " + selected + " " + (selected == 1 ? this.getTranslationProcessor().translate(resource.getResourceSDC().singulartext) : this.getTranslationProcessor().translate(resource.getResourceSDC().pluraltext)) + " " + this.getTranslationProcessor().translate("Selected") + ": " : title + " " + this.getTranslationProcessor().translate("has") + " " + selected + " " + (selected == 1 ? "Workarea" : "Workareas") + " " + this.getTranslationProcessor().translate("Selected") + ": ";
                        StringBuilder out = new StringBuilder();
                        for (int k = 0; k < sel.length(); ++k) {
                            if (out.length() > 0) {
                                out.append(", ");
                            }
                            try {
                                String t = sel.getString(k);
                                if (resource.getResourceSDC() == ResourceSDC.USER) {
                                    t = t.substring(0, 1).toUpperCase() + t.substring(1);
                                }
                                out.append(t);
                                continue;
                            }
                            catch (Exception exception) {
                                // empty catch block
                            }
                        }
                        title = title + out.toString();
                    } else {
                        title = title + " " + this.getTranslationProcessor().translate("has no") + " " + this.getTranslationProcessor().translate(resource.getResourceSDC().pluraltext) + " " + this.getTranslationProcessor().translate("Selected");
                    }
                    image.setTitle(title);
                    image.setDimensions(16, 16);
                    String focusId = resource != null ? resource.getId() : "";
                    html.append("<div id=\"sel_").append(resource.getId()).append("\" class=\"selicon").append("\" title=\"").append(title).append("\" onclick=\"assignment.changeView('").append((Object)ViewMode.RESOURCE).append("'").append(",'").append(focusId).append("'").append(")\">");
                    html.append(image.getHtml());
                    html.append("<span>");
                    if (showText) {
                        if (selected > 0) {
                            if (selected == 1) {
                                try {
                                    String t = sel.getString(0);
                                    if (resource.getResourceSDC() == ResourceSDC.USER) {
                                        t = t.substring(0, 1).toUpperCase() + t.substring(1);
                                    }
                                    html.append(t);
                                }
                                catch (Exception exception) {}
                            } else {
                                html.append(selected).append(" ").append(this.getTranslationProcessor().translate(resource.getResourceSDC().pluraltext));
                            }
                        } else if (resource.getType().length() > 0) {
                            html.append(resource.getType()).append(": ").append(this.getTranslationProcessor().translate("None"));
                        } else if (resource.getModel().length() > 0) {
                            html.append(resource.getModel()).append(": ").append(this.getTranslationProcessor().translate("None"));
                        } else {
                            html.append(this.getTranslationProcessor().translate(resource.getResourceSDC().singulartext)).append(": ").append(this.getTranslationProcessor().translate("None"));
                        }
                    } else {
                        html.append(selected);
                    }
                    html.append("</span>");
                    html.append("</div>");
                }
            }
            if (!found) {
                html.append(this.getTranslationProcessor().translate("No Resources Found"));
            }
        }
        if (renderContainer) {
            html.append("</div>");
        }
        return html.toString();
    }

    public String getViewIcons() {
        StringBuilder html = new StringBuilder();
        Image image = new Image(this.pageContext);
        image.setImageId("FlatBlackGroup");
        image.setTitle(this.getTranslationProcessor().translate("Resource View"));
        image.setDimensions(24, 24);
        html.append("<div class=\"viewicon").append(this.viewMode == ViewMode.RESOURCE ? " viewicon_sel" : "").append("\" title=\"").append(this.getTranslationProcessor().translate("Resource View")).append("\" onclick=\"assignment.changeView('").append((Object)ViewMode.RESOURCE).append("'").append(")\">").append(image.getHtml()).append("</div>");
        image = new Image(this.pageContext);
        image.setImageId("FlatBlackCalendar31");
        image.setTitle(this.getTranslationProcessor().translate("Calendar View"));
        image.setDimensions(24, 24);
        html.append("<div class=\"viewicon").append(this.viewMode == ViewMode.CALENDAR ? " viewicon_sel" : "").append("\" title=\"").append(this.getTranslationProcessor().translate("Calendar View")).append("\" onclick=\"assignment.changeView('").append((Object)ViewMode.CALENDAR).append("'").append(")\">").append(image.getHtml()).append("</div>");
        return html.toString();
    }

    public void setFocusId(String focusid) {
        this.focusid = focusid;
    }

    public String getFocusId() {
        return this.focusid;
    }

    public ResourceSDC getResourceSDC() {
        return this.resourceSDC != null ? this.resourceSDC : (this.getFocusedResource() != null ? this.getFocusedResource().getResourceSDC() : ResourceSDC.USER);
    }

    public void setResourceSDC(ResourceSDC resourceSDC) {
        this.resourceSDC = resourceSDC;
    }

    public void setSDCId(String sdcid) {
        this.sdcid = sdcid;
    }

    public void setKeyId1(String keyid1) {
        this.keyid1 = keyid1;
    }

    public void setActivityQuery(String activityQuery) {
        this.activityQuery = activityQuery = StringUtil.replaceAll(activityQuery, "\\'", "'");
    }

    public JSONArray getEventData(JSONObject metadata, M18NUtil m18) {
        return this.viewMode != null ? this.viewMode.getEventData(this, metadata, m18) : new JSONArray();
    }

    @Override
    public void setElementProperties(PropertyList props) {
        super.setElementProperties(props);
        this.pageName = this.element.getProperty("page", "");
        this.buildUserConfigPrefix();
    }

    public void loadProperties() throws SapphireException {
        String keyid1;
        this.buildUserConfigPrefix();
        this.setViewOnly(this.element.getProperty("viewonly", this.element.getPropertyList("pagedata").getProperty("viewonly", "N")).equalsIgnoreCase("Y"));
        if (!(";" + this.requestContext.getProperty("modulelist")).contains(";WAP")) {
            this.setViewOnly(true);
        }
        String sdcid = this.element.getProperty("sdcid", this.element.getPropertyList("pagedata").getProperty("sdcid", ""));
        boolean depforce = false;
        this.departmentid = this.element.getProperty("departmentid", this.element.getPropertyList("pagedata").getProperty("departmentid", this.element.getProperty("testingdepartment", this.element.getPropertyList("pagedata").getProperty("testingdepartment", ""))));
        if (this.departmentid.length() == 0) {
            this.departmentid = this.userConfig.getProperty(this.userConfigPrefix + "testingdepartmentid", "");
        } else {
            depforce = true;
        }
        if (this.element.getPropertyList("pagedata").getProperty("requiredepartmentmember", "Y").equalsIgnoreCase("Y")) {
            SafeSQL safede = new SafeSQL();
            String depsql = "SELECT departmentid from departmentsysuser WHERE sysuserid = ?";
            if (this.connectionInfo == null) {
                this.connectionInfo = this.getConnectionProcessor().getConnectionInfo(this.getConnectionId());
            }
            safede.addVar(this.connectionInfo.getSysuserId());
            DataSet deps = this.getQueryProcessor().getPreparedSqlDataSet(depsql, safede.getValues());
            if (deps != null && deps.size() > 0) {
                this.usersdepartments = new ArrayList<String>(Arrays.asList(StringUtil.split(deps.getColumnValues("departmentid", ";"), ";")));
                if (!this.usersdepartments.contains(this.departmentid)) {
                    this.logger.warn("Invalid department id provided. User not member of department.");
                    this.departmentid = depforce ? this.usersdepartments.get(0) : "";
                }
            } else {
                this.usersdepartments = new ArrayList();
                this.departmentid = "";
                this.logger.warn("Invalid department id provided. User not a member of any department.");
            }
        } else {
            this.usersdepartments = null;
        }
        String contextsdcid = this.element.getProperty("contextsdcid", this.element.getPropertyList("pagedata").getProperty("contextsdcid", this.element.getProperty("activitycontextsdcid", this.element.getPropertyList("pagedata").getProperty("activitycontextsdcid", ""))));
        String worksdcid = this.element.getProperty("worksdcid", this.element.getPropertyList("pagedata").getProperty("worksdcid", ""));
        if (this.operatingMode == OperatingMode.ASSIGNMENT) {
            String activityClass = this.element.getProperty("activityclass", this.element.getPropertyList("pagedata").getProperty("activityclass", ""));
            if (activityClass.length() == 0 && this.userConfig != null) {
                activityClass = this.userConfig.getProperty(this.userConfigPrefix + "activityclass");
            }
            if (activityClass.length() == 0 && sdcid.length() == 0) {
                sdcid = "SDIWorkItem";
            }
            String testinglabtype = AssignmentPageUtil.getTestingLabType(this.departmentid, this.getQueryProcessor());
            try {
                this.setActivityClassHandler(activityClass, sdcid, this.departmentid, testinglabtype);
            }
            catch (Exception e) {
                this.logger.warn(e.getMessage());
                this.logger.debug("Invalid saved activity class " + activityClass + " defaulting...");
                sdcid = "SDIWorkItem";
                activityClass = "";
                this.setActivityClassHandler(activityClass, sdcid, this.departmentid, testinglabtype);
            }
        } else {
            String rsdc = this.element.getProperty("resourcesdcid", this.element.getPropertyList("pagedata").getProperty("resourcesdcid", this.element.getProperty("resourcetype", this.element.getPropertyList("pagedata").getProperty("resourcetype", ""))));
            if (rsdc.length() > 0) {
                try {
                    this.resourceSDC = ResourceSDC.valueOf(rsdc.toUpperCase());
                }
                catch (Exception e) {
                    this.logger.warn("Invalid resource provided");
                    this.resourceSDC = ResourceSDC.USER;
                }
                this.sdcid = sdcid.length() > 0 ? sdcid : (this.departmentid.length() > 0 || worksdcid.length() > 0 || contextsdcid.length() > 0 ? (sdcid = "LV_Activity") : this.resourceSDC.getName());
            } else if (sdcid.length() > 0) {
                this.sdcid = sdcid;
                try {
                    this.resourceSDC = ResourceSDC.valueOf(sdcid.toUpperCase());
                }
                catch (Exception e) {
                    this.logger.warn("Invalid resource provided");
                    this.resourceSDC = ResourceSDC.USER;
                }
            } else if (this.departmentid.length() > 0 || worksdcid.length() > 0 || contextsdcid.length() > 0) {
                sdcid = "LV_Activity";
                this.resourceSDC = ResourceSDC.USER;
                this.sdcid = sdcid;
            } else {
                this.resourceSDC = ResourceSDC.USER;
                this.sdcid = this.resourceSDC.getName();
            }
        }
        this.keyid1 = keyid1 = this.element.getProperty("keyid1", this.element.getPropertyList("pagedata").getProperty("keyid1", ""));
        if (this.operatingMode == OperatingMode.WORK) {
            if (keyid1.length() > 0 && !keyid1.contains(";") && sdcid.equalsIgnoreCase(this.resourceSDC.getName())) {
                this.setFocusId(keyid1);
            } else if (this.resourceSDC == ResourceSDC.USER && this.departmentid.length() == 0 && (sdcid.length() == 0 || sdcid.equalsIgnoreCase(this.resourceSDC.getName())) && !keyid1.contains(";")) {
                if (this.connectionInfo == null) {
                    this.connectionInfo = this.getConnectionProcessor().getConnectionInfo(this.getConnectionId());
                }
                this.setFocusId(this.connectionInfo.getSysuserId());
                this.keyid1 = this.focusid;
                this.mywork = true;
            } else {
                this.focusid = this.element.getProperty("itemid", this.element.getPropertyList("pagedata").getProperty("itemid"));
            }
        } else {
            this.focusid = this.element.getProperty("itemid", this.element.getPropertyList("pagedata").getProperty("itemid"));
        }
        if (this.userConfig != null) {
            try {
                this.viewMode = ViewMode.valueOf(this.userConfig.getProperty(this.userConfigPrefix + "viewmode", this.viewMode.toString()));
            }
            catch (Exception e) {
                // empty catch block
            }
            if (this.userConfig.getProperty(this.userConfigPrefix + "focusedresource", "").length() > 0) {
                this.setFocusedResource(this.userConfig.getProperty(this.userConfigPrefix + "focusedresource"));
            }
            try {
                this.focusid = this.userConfig.getProperty(this.userConfigPrefix + "focusid", "");
            }
            catch (Exception e) {
                // empty catch block
            }
        }
        if (this.operatingMode == OperatingMode.WORK) {
            String startDate;
            if (!this.getSDCId().equalsIgnoreCase(ResourceSDC.USER.getName()) && !this.getSDCId().equalsIgnoreCase(ResourceSDC.INSTRUMENT.getName())) {
                DataSet instrumentmodels;
                DataSet instrumenttypes;
                if (this.element.getPropertyList("pagedata").getProperty("instrumenttype", this.element.getPropertyList("pagedata").getProperty("instrumenttypeid", "")).length() == 0 && (instrumenttypes = this.getQueryProcessor().getSqlDataSet("SELECT DISTINCT instrumenttypeid FROM activityresource WHERE instrumenttypeid IS NOT null")) != null && instrumenttypes.getRowCount() > 0) {
                    this.element.getPropertyList("pagedata").setProperty("instrumenttype", instrumenttypes.getValue(0, "instrumenttypeid", ""));
                }
                if (this.element.getPropertyList("pagedata").getProperty("instrumenttype", this.element.getPropertyList("pagedata").getProperty("instrumenttypeid", "")).length() == 0 && this.element.getPropertyList("pagedata").getProperty("instrumentmodel", this.element.getPropertyList("pagedata").getProperty("instrumentmodelid", "")).length() == 0 && (instrumentmodels = this.getQueryProcessor().getSqlDataSet("SELECT DISTINCT instrumentmodelid FROM activityresource WHERE instrumentmodelid IS NOT null")) != null && instrumentmodels.getRowCount() > 0) {
                    this.element.getPropertyList("pagedata").setProperty("instrumentmodel", instrumentmodels.getValue(0, "instrumentmodelid", ""));
                }
                if (this.element.getPropertyList("pagedata").getProperty("analysttype", "").length() == 0) {
                    // empty if block
                }
            }
            DateTimeUtil dtu = new DateTimeUtil(this.getConnectionProcessor().getConnectionInfo(this.getConnectionId()));
            CalendarConverter calendarConverter = new CalendarConverter(dtu);
            Instant startPos = null;
            if (sdcid.equalsIgnoreCase("LV_Activity") && keyid1.length() == 0) {
                String wkeyid;
                StringBuilder qw = new StringBuilder();
                qw.append(this.element.getProperty("activitywhere", this.element.getPropertyList("pagedata").getProperty("activitywhere", "")));
                String qf = this.element.getProperty("activityfrom", this.element.getPropertyList("pagedata").getProperty("activityfrom", ""));
                if (this.departmentid.length() > 0) {
                    if (qw.length() > 0) {
                        qw.insert(0, "(");
                        qw.append(") AND (").append("activity.testingdepartmentid = '").append(this.departmentid).append("')");
                    } else {
                        qw.append("activity.testingdepartmentid = '").append(this.departmentid).append("'");
                    }
                }
                if (contextsdcid.length() > 0) {
                    String ckeyid = this.element.getProperty("contextkeyid1", this.element.getPropertyList("pagedata").getProperty("contextkeyid1", this.element.getProperty("activitycontextkeyid1", this.element.getPropertyList("pagedata").getProperty("activitycontextkeyid1", ""))));
                    if (ckeyid.length() > 0) {
                        if (qw.length() > 0) {
                            qw.insert(0, "(");
                            qw.append(" AND (activity.activitycontextsdcid='").append(contextsdcid).append("' AND activity.activitycontextkeyid1='").append(ckeyid).append("')");
                        } else {
                            qw.append(" activity.activitycontextsdcid='").append(contextsdcid).append("' AND activity.activitycontextkeyid1='").append(ckeyid).append("'");
                        }
                    }
                } else if (worksdcid.length() > 0 && (wkeyid = this.element.getProperty("workkeyid1", this.element.getPropertyList("pagedata").getProperty("workkeyid1", ""))).length() > 0) {
                    if (qw.length() > 0) {
                        qw.insert(0, "(");
                        qw.append(" AND (activity.activityid IN (SELECT activityworksdi.activityid FROM activityworksdi WHERE activityworksdi.worksdcid='").append(worksdcid).append("' AND activityworksdi.workkeyid1 IN ('").append(StringUtil.replaceAll(wkeyid, ";", "','")).append("')))");
                    } else {
                        qw.append(" activity.activityid IN (SELECT activityworksdi.activityid FROM activityworksdi WHERE activityworksdi.worksdcid='").append(worksdcid).append("' AND activityworksdi.workkeyid1 IN ('").append(StringUtil.replaceAll(wkeyid, ";", "','")).append("'))");
                    }
                }
                if (qw.length() > 0) {
                    String sql = "SELECT activity.activityid, activity.startdt, activity.startrangedt, activity.timemode FROM activity" + (qf.length() > 0 ? "," + qf : "") + " WHERE " + qw;
                    StringBuilder sqlw = new StringBuilder();
                    sqlw.append("SELECT activity.activityid FROM activity").append(qf.length() > 0 ? "," + qf : "").append(" WHERE ").append((CharSequence)qw);
                    DataSet ds = this.getQueryProcessor().getSqlDataSet(sql);
                    if (ds != null && ds.getRowCount() > 0) {
                        String startDate2;
                        this.keyid1 = ds.getColumnValues("activityid", ";");
                        if (ds.getRowCount() > 500) {
                            this.activityQuery = sqlw.toString();
                        }
                        if ((startDate2 = this.element.getProperty("startdate", this.element.getPropertyList("pagedata").getProperty("startdate", ""))).length() > 0) {
                            Instant chosen = null;
                            if (startDate2.equalsIgnoreCase("start")) {
                                for (int a = 0; a < ds.getRowCount(); ++a) {
                                    String tm = ds.getValue(a, "timemode", "Floating");
                                    Calendar dt = tm.equalsIgnoreCase("Fixed") ? ds.getCalendar(a, "startdt") : ds.getCalendar(a, "startrangedt");
                                    Instant dtInstant = calendarConverter.convertDatabaseCalendarToInstantUtc(dt);
                                    if (dt == null || chosen != null && chosen.compareTo(dtInstant) <= 0) continue;
                                    chosen = dtInstant;
                                }
                            } else if (startDate2.equalsIgnoreCase("end")) {
                                for (int a = 0; a < ds.getRowCount(); ++a) {
                                    String tm = ds.getValue(a, "timemode", "Floating");
                                    Calendar dt = tm.equalsIgnoreCase("Fixed") ? ds.getCalendar(a, "startdt") : ds.getCalendar(a, "startrangedt");
                                    Instant dtInstant = calendarConverter.convertDatabaseCalendarToInstantUtc(dt);
                                    if (dt == null || chosen != null && chosen.compareTo(dtInstant) >= 0) continue;
                                    chosen = dtInstant;
                                }
                            } else {
                                try {
                                    chosen = calendarConverter.convertDatabaseCalendarToInstantUtc(dtu.getCalendar(startDate2));
                                }
                                catch (Exception e) {
                                    chosen = null;
                                }
                            }
                            if (chosen != null) {
                                startPos = chosen;
                            }
                        }
                    }
                }
            } else {
                startDate = this.element.getProperty("startdate", this.element.getPropertyList("pagedata").getProperty("startdate", ""));
                if (startDate.length() > 0 && !startDate.equalsIgnoreCase("start") && !startDate.equalsIgnoreCase("end")) {
                    try {
                        startPos = calendarConverter.convertDatabaseCalendarToInstantUtc(dtu.getCalendar(startDate));
                    }
                    catch (Exception e) {
                        startPos = null;
                    }
                }
            }
            if (startPos != null) {
                this.logger.debug("ASSIGNMENTPAGE CALENDAR 7535 - startPos - " + startPos);
            } else {
                this.logger.debug("ASSIGNMENTPAGE CALENDAR 7545 - startPos is null");
            }
            if (this.clientFromDate == null) {
                if (startPos != null) {
                    AssignmentPageTimeRange timeRange = new AssignmentPageTimeRange(ZonedDateTime.ofInstant(startPos, this.displayTimeZone), ZonedDateTime.ofInstant(startPos, this.displayTimeZone));
                    AssignmentPageUtil.adjustRangeToCalendarView(CalendarView.MONTH, timeRange, new M18NUtil(this.connectionInfo));
                    this.setClientDateRange(timeRange);
                } else {
                    Instant startPosInstant;
                    startDate = this.userConfig.getProperty(this.userConfigPrefix + "startdate", "");
                    if (startDate.length() > 0 && (startPosInstant = Instant.ofEpochMilli(Long.parseLong(startDate))) != null) {
                        this.logger.debug("ASSIGNMENTPAGE CALENDAR 75485 - startPosInstant - " + startPosInstant.getEpochSecond() * 1000L + " - " + DateTimeFormatter.ISO_INSTANT.format(startPosInstant));
                        AssignmentPageTimeRange timeRange = new AssignmentPageTimeRange(ZonedDateTime.ofInstant(startPosInstant, this.displayTimeZone), ZonedDateTime.ofInstant(startPosInstant, this.displayTimeZone));
                        AssignmentPageUtil.adjustRangeToCalendarView(CalendarView.MONTH, timeRange, new M18NUtil(this.connectionInfo));
                        this.setClientDateRange(timeRange);
                    }
                }
            }
        } else if (this.activityClassHandler != null && this.activityClassHandler.getSDC().equals("LV_Activity")) {
            this.activityQueryWhere = this.element.getProperty("activitywhere", this.element.getPropertyList("pagedata").getProperty("activitywhere", ""));
        }
        ConfigurationProcessor cp = new ConfigurationProcessor(this.connectionInfo.getConnectionId());
        try {
            PropertyList dateFormatPolicy = cp.getPolicy("DateFormatPolicy", "Sapphire Custom");
            String s = dateFormatPolicy.getProperty("firstdayofweek", "Monday");
            if (s.equalsIgnoreCase("Sunday")) {
                this.firstDayOfWeek = 1;
            } else if (s.equalsIgnoreCase("User Locale")) {
                M18NUtil m18 = new M18NUtil(this.pageContext);
                Calendar cal = Calendar.getInstance(m18.getLocale());
                this.firstDayOfWeek = cal.getFirstDayOfWeek();
            } else {
                this.firstDayOfWeek = 2;
            }
        }
        catch (Exception e) {
            this.firstDayOfWeek = 2;
        }
    }

    public String getOperationsMenu(String activityId, String activityClass) throws SapphireException {
        if (activityClass.length() > 0) {
            return AssignmentPageUtil.getOperationsMenu(activityId, ActivityClassHandler.getInstance(this.getConnectionId(), this.getWapPolicy(), activityClass), this.getWapPolicy(), this.getTranslationProcessor(), this.getConnectionId());
        }
        return AssignmentPageUtil.getOperationsMenu(activityId, new ActivityClassHandler(this.getConnectionId(), "__ac", "", ""), this.getWapPolicy(), this.getTranslationProcessor(), this.getConnectionId());
    }

    private void renderRightHeader(StringBuilder html, boolean hasResources) {
        Button assignButton = new Button(this.pageContext);
        assignButton.setId("btAssign");
        assignButton.setImg("rc?command=image&image=FlatBlackPeopleDown");
        assignButton.setText(this.getTranslationProcessor().translate("Assign Work"));
        assignButton.setTip(this.getTranslationProcessor().translate("Assign Work"));
        assignButton.setAction("assignment.assignWork()");
        if (!hasResources) {
            assignButton.setDisabled(true);
        }
        html.append(assignButton.getHtml());
        html.append("&nbsp;");
        Button recentButton = new Button(this.pageContext);
        recentButton.setId("btRecent");
        recentButton.setImg("rc?command=image&image=FlatBlackClock");
        recentButton.setText("");
        recentButton.setTip(this.getTranslationProcessor().translate("Show recent assignments"));
        recentButton.setAction("assignment.showRecentAssignments()");
        recentButton.setDisabled(true);
        html.append(recentButton.getHtml());
        html.append("&nbsp;");
        Button undoButton = new Button(this.pageContext);
        undoButton.setId("btUndo");
        undoButton.setImg("rc?command=image&image=FlatBlackUndo2");
        undoButton.setText("");
        undoButton.setTip(this.getTranslationProcessor().translate("Undo last assignment"));
        undoButton.setAction("assignment.undoAssignment()");
        undoButton.setDisabled(true);
        html.append(undoButton.getHtml());
        html.append("&nbsp;");
        Button showButton = new Button(this.pageContext);
        showButton.setId("btShowRec");
        showButton.setImg("rc?command=image&image=FlatBlackPeopleMultipleMagnify");
        showButton.setText("");
        showButton.setTip(this.getTranslationProcessor().translate("Show recent in plan"));
        showButton.setAction("assignment.showPlan('" + this.element.getPropertyListNotNull("pagedata").getProperty("showplanpage") + "','" + this.element.getPropertyListNotNull("pagedata").getProperty("showplantarget") + "', true)");
        showButton.setDisabled(true);
        html.append(showButton.getHtml());
        html.append("&nbsp;");
        Button editButton = new Button(this.pageContext);
        editButton.setId("btEditSel");
        editButton.setImg("rc?command=image&image=FlatBlackEditBox");
        editButton.setText("");
        editButton.setTip(this.getTranslationProcessor().translate("Edit selected item"));
        editButton.setAction("assignment.editActivity()");
        editButton.setDisabled(false);
        html.append(editButton.getHtml());
        html.append("&nbsp;");
        html.append(this.getViewIcons());
    }

    static /* synthetic */ Logger access$3500(AssignmentPage x0) {
        return x0.logger;
    }

    static /* synthetic */ Logger access$3600(AssignmentPage x0) {
        return x0.logger;
    }

    static /* synthetic */ Logger access$3700(AssignmentPage x0) {
        return x0.logger;
    }

    static /* synthetic */ Logger access$3800(AssignmentPage x0) {
        return x0.logger;
    }

    static /* synthetic */ void access$3900(AssignmentPage x0, List x1, JSONObject x2, boolean x3) {
        x0.processAvailability(x1, x2, x3);
    }

    static /* synthetic */ void access$4000(AssignmentPage x0, JSONObject x1, JSONArray x2, String x3) {
        x0.buildWorkloadData(x1, x2, x3);
    }

    static /* synthetic */ TranslationProcessor access$4200(AssignmentPage x0) {
        return x0.getTranslationProcessor();
    }

    static /* synthetic */ PageContext access$4300(AssignmentPage x0) {
        return x0.pageContext;
    }

    static /* synthetic */ TranslationProcessor access$4400(AssignmentPage x0) {
        return x0.getTranslationProcessor();
    }

    static /* synthetic */ SDCProcessor access$4500(AssignmentPage x0) {
        return x0.getSDCProcessor();
    }

    static /* synthetic */ TranslationProcessor access$4600(AssignmentPage x0) {
        return x0.getTranslationProcessor();
    }

    static /* synthetic */ SDCProcessor access$4700(AssignmentPage x0) {
        return x0.getSDCProcessor();
    }

    static /* synthetic */ String access$4800(AssignmentPage x0) {
        return x0.departmentid;
    }

    static /* synthetic */ Logger access$4900(AssignmentPage x0) {
        return x0.logger;
    }

    static /* synthetic */ TranslationProcessor access$5000(AssignmentPage x0) {
        return x0.getTranslationProcessor();
    }

    static /* synthetic */ PageContext access$5100(AssignmentPage x0) {
        return x0.pageContext;
    }

    static /* synthetic */ TranslationProcessor access$5200(AssignmentPage x0) {
        return x0.getTranslationProcessor();
    }

    static /* synthetic */ SDCProcessor access$5300(AssignmentPage x0) {
        return x0.getSDCProcessor();
    }

    static /* synthetic */ TranslationProcessor access$5400(AssignmentPage x0) {
        return x0.getTranslationProcessor();
    }

    static /* synthetic */ SDCProcessor access$5500(AssignmentPage x0) {
        return x0.getSDCProcessor();
    }

    static /* synthetic */ Logger access$5600(AssignmentPage x0) {
        return x0.logger;
    }

    static /* synthetic */ TranslationProcessor access$5700(AssignmentPage x0) {
        return x0.getTranslationProcessor();
    }

    static /* synthetic */ PageContext access$5800(AssignmentPage x0) {
        return x0.pageContext;
    }

    static /* synthetic */ TranslationProcessor access$5900(AssignmentPage x0) {
        return x0.getTranslationProcessor();
    }

    static /* synthetic */ SDCProcessor access$6000(AssignmentPage x0) {
        return x0.getSDCProcessor();
    }

    static /* synthetic */ TranslationProcessor access$6100(AssignmentPage x0) {
        return x0.getTranslationProcessor();
    }

    static /* synthetic */ SDCProcessor access$6200(AssignmentPage x0) {
        return x0.getSDCProcessor();
    }

    static /* synthetic */ Logger access$6300(AssignmentPage x0) {
        return x0.logger;
    }

    static /* synthetic */ TranslationProcessor access$6400(AssignmentPage x0) {
        return x0.getTranslationProcessor();
    }

    static /* synthetic */ PageContext access$6500(AssignmentPage x0) {
        return x0.pageContext;
    }

    static /* synthetic */ TranslationProcessor access$6600(AssignmentPage x0) {
        return x0.getTranslationProcessor();
    }

    static /* synthetic */ SDCProcessor access$6700(AssignmentPage x0) {
        return x0.getSDCProcessor();
    }

    static /* synthetic */ TranslationProcessor access$6800(AssignmentPage x0) {
        return x0.getTranslationProcessor();
    }

    static /* synthetic */ SDCProcessor access$6900(AssignmentPage x0) {
        return x0.getSDCProcessor();
    }

    static /* synthetic */ Logger access$7000(AssignmentPage x0) {
        return x0.logger;
    }

    static /* synthetic */ TranslationProcessor access$7100(AssignmentPage x0) {
        return x0.getTranslationProcessor();
    }

    static /* synthetic */ PageContext access$7200(AssignmentPage x0) {
        return x0.pageContext;
    }

    static /* synthetic */ TranslationProcessor access$7300(AssignmentPage x0) {
        return x0.getTranslationProcessor();
    }

    static /* synthetic */ SDCProcessor access$7400(AssignmentPage x0) {
        return x0.getSDCProcessor();
    }

    static /* synthetic */ TranslationProcessor access$7500(AssignmentPage x0) {
        return x0.getTranslationProcessor();
    }

    static /* synthetic */ SDCProcessor access$7600(AssignmentPage x0) {
        return x0.getSDCProcessor();
    }

    public static class AssignmentPageTimeRange {
        ZonedDateTime from;
        ZonedDateTime to;

        AssignmentPageTimeRange(ZonedDateTime from, ZonedDateTime to) {
            this.from = from;
            this.to = to;
        }

        public ZonedDateTime getFromZonedDateTime() {
            return this.from;
        }

        public ZonedDateTime getToZonedDateTime() {
            return this.to;
        }
    }

    public static class AssignmentPageResourceRequirement {
        private AssignmentPageResourceData resourceData;
        private int maxActivitySize;
        private String durationText;
        private String descriptionText;

        AssignmentPageResourceRequirement(AssignmentPageResourceData resourceData, String durationText, String desciptionText, int maxActivitySize) {
            this.resourceData = resourceData;
            this.durationText = durationText;
            this.descriptionText = desciptionText;
            this.maxActivitySize = maxActivitySize;
        }

        public String getDuration() {
            return this.durationText;
        }

        public String getDescription() {
            return this.descriptionText;
        }

        public int getMaxSize() {
            return this.maxActivitySize;
        }

        public AssignmentPageResourceData getResourceData() {
            return this.resourceData;
        }
    }

    public static enum ResourceSDC {
        INSTRUMENT("Instrument", "instrumentid", "FlatBlackThermometer2", "Instrument Workload", "Instrument", "Instruments"){

            @Override
            public String getHTML(AssignmentPage page) {
                AssignmentPageResourceContainer instData;
                StringBuilder html = new StringBuilder();
                html.append("<div id=\"resourcecontent\" class=\"viewmode_").append(page.viewMode.toString().toLowerCase()).append("_").append(this.toString().toLowerCase()).append("\">");
                AssignmentPageResourceContainer assignmentPageResourceContainer = instData = page.getFocusedResource() != null && page.getFocusedResource().getResourceSDC() == this ? page.getFocusedResource() : null;
                if (instData != null && instData.getData() != null) {
                    WAPAvailabilitySelector wapAvailabilitySelector = new WAPAvailabilitySelector(page.getConnectionId());
                    WAPSelector wapSelector = new WAPSelector(page.getConnectionId());
                    WAPAvailabilityOptions wapAvailabilityOptions = new WAPAvailabilityOptions();
                    html.append("<div class=\"resourcetype_instruments\">");
                    DataSet instruments = instData.getData();
                    boolean primrendered = page.drawResourceHtml(html, wapAvailabilitySelector, wapAvailabilityOptions, wapSelector, instruments, null, this.getName(), page.getFocusedResource());
                    html.append("</div>");
                    html.append("<div class=\"resourcetype_instrumentworkareas\">");
                    DataSet workareas = instData.getWorkareas();
                    boolean warendered = workareas != null && page.drawResourceHtml(html, wapAvailabilitySelector, wapAvailabilityOptions, wapSelector, workareas, null, "Department", page.getFocusedResource());
                    html.append("</div>");
                    if (!primrendered && !warendered) {
                        if (page.getFocusedResource().isShowAll()) {
                            html.append("<div class=\"message\">").append(page.getTranslationProcessor().translate("No ")).append(" ").append(this.pluraltext).append(" ").append(page.getTranslationProcessor().translate(" found.")).append("<br>").append(page.getTranslationProcessor().translate("Try adding the " + this.singulartext + " to testing lab")).append(" ").append(page.getDepartmentId()).append(".</div>");
                        } else {
                            html.append("<div class=\"message\">").append(page.getTranslationProcessor().translate("No preferred")).append(" ").append(this.pluraltext).append(" ").append(page.getTranslationProcessor().translate(" found.")).append("<br>").append(page.getTranslationProcessor().translate("Try selecting Show All to show all available " + this.pluraltext)).append(".</div>");
                        }
                    }
                } else {
                    html.append("<div class=\"error\">").append(page.getTranslationProcessor().translate("Unable to find")).append(" ").append(this.pluraltext).append(".").append("</div>");
                }
                html.append("</div>");
                return html.toString();
            }

            @Override
            public String getAssignmentHTML(ActivityClassHandler activityClassHandler, String selectedsdis, CalendarPage.View dateRange, Instant[] selectedDates, String departmentid, String shiftid, JSONObject selection, AssignmentPageResourceData resourceData, SDIProcessor sdiProcessor, TranslationProcessor tp, ZoneId displayTimeZone, M18NUtil m18) throws SapphireException {
                return AssignmentPageUtil.getAssignmentHTML(activityClassHandler, selectedsdis, dateRange, selectedDates, departmentid, shiftid, selection, resourceData, sdiProcessor, tp, displayTimeZone, m18);
            }

            @Override
            protected String getScript(AssignmentPage page) {
                return "";
            }
        }
        ,
        USER("User", "sysuserid", "FlatBlackPeople", "User Workload", "User", "Users"){

            @Override
            public String getHTML(AssignmentPage page) {
                AssignmentPageResourceContainer userData;
                StringBuilder html = new StringBuilder();
                html.append("<div id=\"resourcecontent\" class=\"viewmode_").append(page.viewMode.toString().toLowerCase()).append("_").append(this.toString().toLowerCase()).append("\">");
                AssignmentPageResourceContainer assignmentPageResourceContainer = userData = page.focusedResource != null && page.focusedResource.getResourceSDC() == this ? page.focusedResource : null;
                if (userData != null && userData.getData() != null) {
                    WAPAvailabilitySelector wapAvailabilitySelector = new WAPAvailabilitySelector(page.getConnectionId());
                    WAPAvailabilityOptions wapAvailabilityOptions = new WAPAvailabilityOptions();
                    WAPSelector wapSelector = new WAPSelector(page.getConnectionId());
                    html.append("<div class=\"resourcetype_analysts\">");
                    DataSet users = userData.getData();
                    boolean primrendered = page.drawResourceHtml(html, wapAvailabilitySelector, wapAvailabilityOptions, wapSelector, users, null, this.getName(), page.getFocusedResource());
                    html.append("</div>");
                    html.append("<div class=\"resourcetype_analystworkareas\">");
                    DataSet workareas = userData.getWorkareas();
                    boolean warendered = workareas != null && page.drawResourceHtml(html, wapAvailabilitySelector, wapAvailabilityOptions, wapSelector, workareas, null, "Department", page.getFocusedResource());
                    html.append("</div>");
                    if (!primrendered && !warendered) {
                        if (page.getFocusedResource().isShowAll()) {
                            html.append("<div class=\"message\">").append(page.getTranslationProcessor().translate("No ")).append(" ").append(this.pluraltext).append(" ").append(page.getTranslationProcessor().translate(" found.")).append("</div>");
                        } else {
                            html.append("<div class=\"message\">").append(page.getTranslationProcessor().translate("No preferred")).append(" ").append(this.pluraltext).append(" ").append(page.getTranslationProcessor().translate(" found.")).append("</div>");
                        }
                    }
                } else {
                    html.append("<div class=\"error\">").append(page.getTranslationProcessor().translate("Unable to find")).append(" ").append(this.pluraltext).append(".").append("</div>");
                }
                html.append("</div>");
                return html.toString();
            }

            @Override
            public String getAssignmentHTML(ActivityClassHandler activityClassHandler, String selectedsdis, CalendarPage.View dateRange, Instant[] selectedDates, String departmentid, String shiftid, JSONObject selection, AssignmentPageResourceData resourceData, SDIProcessor sdiProcessor, TranslationProcessor tp, ZoneId displayTimeZone, M18NUtil m18) throws SapphireException {
                return AssignmentPageUtil.getAssignmentHTML(activityClassHandler, selectedsdis, dateRange, selectedDates, departmentid, shiftid, selection, resourceData, sdiProcessor, tp, displayTimeZone, m18);
            }

            @Override
            protected String getScript(AssignmentPage page) {
                return "";
            }
        };

        protected String keycolid;
        protected String name;
        protected String imageid;
        protected String viewtitle;
        protected String singulartext;
        protected String pluraltext;

        private ResourceSDC(String name, String keycolid, String imageid, String viewtitle, String singulartext, String pluraltext) {
            this.keycolid = keycolid;
            this.name = name;
            this.imageid = imageid;
            this.viewtitle = viewtitle;
            this.singulartext = singulartext;
            this.pluraltext = pluraltext;
        }

        public String getName() {
            return this.name;
        }

        protected abstract String getHTML(AssignmentPage var1);

        protected abstract String getScript(AssignmentPage var1);

        protected abstract String getAssignmentHTML(ActivityClassHandler var1, String var2, CalendarPage.View var3, Instant[] var4, String var5, String var6, JSONObject var7, AssignmentPageResourceData var8, SDIProcessor var9, TranslationProcessor var10, ZoneId var11, M18NUtil var12) throws SapphireException;
    }

    public static enum SelectorMode {
        NONE,
        TABS,
        LINKS,
        DROPDOWN;

    }

    public static enum ViewMode {
        RESOURCE{

            @Override
            protected String getHTML(AssignmentPage page) {
                this.focus = page.getFocusedResource();
                StringBuilder html = new StringBuilder();
                if (page.fullContent) {
                    html.append("<div id=\"rightheader\">");
                    html.append("<div class=\"titletext\">");
                    html.append("</div>");
                    boolean hasResources = page.getResourceData() != null && page.getResourceData().getResources() != null && page.getResourceData().getResources().size() > 0;
                    page.renderRightHeader(html, hasResources);
                    html.append("&nbsp;");
                    html.append(page.getSelectionIcons(true, false));
                    html.append("</div>");
                    if (hasResources) {
                        html.append(page.getAreaSelector());
                        html.append(page.getCalendarHTML(true));
                    } else if (page.operatingMode == OperatingMode.ASSIGNMENT) {
                        html.append("<div id=\"noresourcesmsg\">").append(page.getTranslationProcessor().translate("No Resources Found. Please select an SDI with available resources.")).append("</div>");
                    }
                    html.append("<div id=\"").append("assignmentarea").append("\" class=\"viewmode_").append(this.toString().toLowerCase()).append("\">");
                    html.append("</div>");
                } else if (page.operatingMode == OperatingMode.WORK || page.getResourceData() != null && page.getResourceData().getResources() != null && page.getResourceData().getResources().size() > 0) {
                    if (page.clientFromDate == null || page.clientToDate == null) {
                        ZonedDateTime now = ZonedDateTime.now(page.displayTimeZone);
                        AssignmentPageTimeRange timeRange = new AssignmentPageTimeRange(now, now);
                        AssignmentPageUtil.adjustRangeToCalendarView(CalendarView.MONTH, timeRange, new M18NUtil(page.connectionInfo));
                        page.setClientDateRange(timeRange);
                    }
                    html.append(this.focus != null ? this.focus.getResourceSDC().getHTML(page) : "");
                } else {
                    html.append("<div id=\"noresourcesmsg\">").append(page.getTranslationProcessor().translate("No Resources Found. Please select an SDI with available resources.")).append("</div>");
                }
                return html.toString();
            }

            @Override
            protected String getAssignmentHTML(ActivityClassHandler activityClassHandler, String selectedsdis, CalendarPage.View dateRange, Instant[] selectedDates, String departmentid, String shiftid, JSONObject selection, AssignmentPageResourceData resourceData, SDIProcessor sdiProcessor, TranslationProcessor tp, ZoneId displayTimeZone, M18NUtil m18) throws SapphireException {
                return this.focus != null ? this.focus.getResourceSDC().getAssignmentHTML(activityClassHandler, selectedsdis, dateRange, selectedDates, departmentid, shiftid, selection, resourceData, sdiProcessor, tp, displayTimeZone, m18) : "";
            }

            @Override
            protected String getAreaSelector(TranslationProcessor tp) {
                return "<div id=\"selectallcontainer\"><div>" + tp.translate("Select All") + "</div><div><input type=\"checkbox\" id=\"selectallcheckbox\" onclick=\"assignment.selectAllItems(this)\"></div></div>";
            }

            @Override
            protected String getScript(AssignmentPage page) {
                StringBuilder script = new StringBuilder();
                if (page.fullContent) {
                    String d = page.userConfig.getProperty(page.userConfigPrefix + "date");
                    long date = page.clientFromDate.toInstant().toEpochMilli();
                    if (d.length() > 0) {
                        try {
                            date = Long.parseLong(d);
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                    }
                    script.append("assignment.initDynamic('").append((Object)this).append("', '").append(page.getCalendarView().toString()).append("',").append(date).append(");");
                }
                script.append(this.focus != null ? this.focus.getResourceSDC().getScript(page) : "");
                return script.toString();
            }

            @Override
            protected JSONArray getEventData(AssignmentPage page, JSONObject metadata, M18NUtil m18) {
                return null;
            }
        }
        ,
        CALENDAR{

            @Override
            protected String getHTML(AssignmentPage page) {
                StringBuilder html = new StringBuilder();
                if (page.fullContent) {
                    if (page.operatingMode == OperatingMode.ASSIGNMENT) {
                        html.append("<div id=\"rightheader\">");
                        html.append("<div class=\"titletext\">");
                        html.append("</div>");
                        boolean hasResources = page.getResourceData() != null && page.getResourceData().getResources() != null && page.getResourceData().getResources().size() > 0;
                        page.renderRightHeader(html, hasResources);
                        html.append("&nbsp;");
                        html.append(page.getSelectionIcons());
                        html.append("</div>");
                        if (hasResources) {
                            html.append(page.getAreaSelector());
                            html.append("<div id=\"").append("calendarplan").append("\" class=\"viewmode_").append(this.toString().toLowerCase()).append("\">");
                            html.append(page.getCalendarHTML(false));
                            html.append("<div>");
                        } else if (page.operatingMode == OperatingMode.ASSIGNMENT) {
                            html.append("<div id=\"noresourcesmsg\">").append(page.getTranslationProcessor().translate("No Resources Found. Please select an SDI with available resources.")).append("</div>");
                        }
                    } else if (page.operatingMode == OperatingMode.WORK || page.getResourceData() != null && page.getResourceData().getResources() != null && page.getResourceData().getResources().size() > 0) {
                        html.append("<div id=\"").append("calendarplan").append("\" class=\"viewmode_").append(this.toString().toLowerCase()).append("\">");
                        html.append(page.getCalendarHTML(false));
                        html.append("<div>");
                    } else {
                        html.append("<div id=\"noresourcesmsg\">").append(page.getTranslationProcessor().translate("No Resources Found. Please select an SDI with available resources.")).append("</div>");
                    }
                }
                return html.toString();
            }

            @Override
            protected String getAssignmentHTML(ActivityClassHandler activityClassHandler, String selectedsdis, CalendarPage.View dateRange, Instant[] selectedDates, String departmentid, String shiftid, JSONObject selection, AssignmentPageResourceData resourceData, SDIProcessor sdiProcessor, TranslationProcessor tp, ZoneId displayTimeZone, M18NUtil m18) throws SapphireException {
                return AssignmentPageUtil.getAssignmentHTML(activityClassHandler, selectedsdis, dateRange, selectedDates, departmentid, shiftid, selection, resourceData, sdiProcessor, tp, displayTimeZone, m18);
            }

            @Override
            protected String getScript(AssignmentPage page) {
                StringBuilder script = new StringBuilder();
                if (page.fullContent) {
                    if (page.clientFromDate == null) {
                        page.clientFromDate = ZonedDateTime.now(page.displayTimeZone);
                    }
                    String d = page.userConfig.getProperty(page.userConfigPrefix + "date");
                    long date = page.clientFromDate.toInstant().toEpochMilli();
                    if (d.length() > 0) {
                        try {
                            date = Long.parseLong(d);
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                    }
                    script.append("assignment.initDynamic('").append((Object)this).append("', '").append(page.getCalendarView().toString()).append("',").append(date).append(", function(){});");
                } else if (page.getFocusId() != null && page.getFocusId().length() > 0) {
                    String fSdc;
                    String fId;
                    if (page.getFocusId().contains("|")) {
                        String[] t = StringUtil.split(page.getFocusId(), "|");
                        fId = t[0];
                        fSdc = t[1];
                    } else {
                        fId = page.getFocusId();
                        String string = fSdc = page.focusedResource != null ? page.focusedResource.getResourceSDC().getName() : "";
                    }
                    if (fSdc.equalsIgnoreCase(ResourceSDC.USER.getName())) {
                        CalendarFactory calendarFactory = new CalendarFactory(page.getConnectionId());
                        try {
                            LVCalendar lvCalendar = calendarFactory.getUserCalendar(fId, true, true);
                            WorkHours wh = lvCalendar.getCoreHours();
                            if (wh != null) {
                                script.append("assignment.viewmodes." + this.toString().toLowerCase() + ".setUpCoreHours(").append(wh.toJSONObject(page.clientFromDate, page.displayTimeZone).toString()).append(");");
                            }
                        }
                        catch (Exception exception) {}
                    }
                } else {
                    script.append("assignment.viewmodes." + this.toString().toLowerCase() + ".setUpCoreHours(").append(");");
                }
                return script.toString();
            }

            private void addActivityWorkResourceDetails(JSONObject event, Activity activity, WAPCommands wapCommands) throws JSONException {
                if (wapCommands != null) {
                    DataSet ds = wapCommands.getActivityWorkSDIs(activity.getActivityid());
                    if (ds != null) {
                        event.put("workcount", ds.getRowCount());
                    }
                    DataSet resources = wapCommands.getActivityResources(activity.getActivityid());
                    if (ds != null) {
                        JSONArray assigned = new JSONArray();
                        JSONArray unassigned = new JSONArray();
                        int unCount = 0;
                        for (int i = 0; i < resources.getRowCount(); ++i) {
                            String type = resources.getValue(i, "resourcetypeflag", "A");
                            String name = resources.getValue(i, type.equalsIgnoreCase("I") ? "instrumenttypeid" : "analysttype", type.equalsIgnoreCase("I") ? resources.getValue(i, "instrumentmodelid", "Instrument") : "Analyst");
                            String rid = resources.getValue(i, type.equalsIgnoreCase("I") ? "instrumentid" : "analystid", "");
                            if (rid.length() == 0) {
                                rid = resources.getValue(i, "workareadepartmentid", "");
                            }
                            if (rid.length() > 0) {
                                assigned.put(name + ";" + rid);
                                continue;
                            }
                            unassigned.put(name);
                            ++unCount;
                        }
                        event.put("unassignedcount", unCount);
                        event.put("resourcecount", ds.getRowCount());
                        event.put("assignedresources", assigned);
                        event.put("unassignedresources", unassigned);
                    }
                }
            }

            private void generateEvents(List<WAPAvailability> availability, JSONArray eventData, int styleNum, String keyValue, AssignmentPageResourceContainer resource, String focusSDC, String departmentid, String shiftid, int sectionId, boolean fullDayOnly, boolean consolidateFullday, boolean convertToFullDay, PropertyList filters, PropertyList displayOptions, ZoneId displayTimeZone, M18NUtil m18, PropertyList userConfig, String userConfigPrefix, OperatingMode operatingMode, List<Activity> unassignedActivities, List<Activity> prefetchedActivities, List<String> processedActivities, TranslationProcessor tp, SDCProcessor sdcProcessor, WAPCommands wapCommands) {
                ArrayList<String> deps = new ArrayList<String>();
                if (departmentid != null && departmentid.length() > 0) {
                    deps.add(departmentid);
                }
                this.generateEvents(availability, eventData, styleNum, keyValue, resource, focusSDC, deps, shiftid, sectionId, fullDayOnly, consolidateFullday, convertToFullDay, filters, displayOptions, displayTimeZone, m18, userConfig, userConfigPrefix, operatingMode, unassignedActivities, prefetchedActivities, processedActivities, tp, sdcProcessor, wapCommands);
            }

            private void generateEvents(List<WAPAvailability> availability, JSONArray eventData, int styleNum, String keyValue, AssignmentPageResourceContainer resource, String resourceSDC, ArrayList<String> departmentids, String shiftid, int sectionId, boolean fullDayOnly, boolean consolidateFullday, boolean convertToFullDay, PropertyList filters, PropertyList displayOptions, ZoneId displayTimeZone, M18NUtil m18, PropertyList userConfig, String userConfigPrefix, OperatingMode operatingMode, List<Activity> unassignedActivities, List<Activity> prefetchedActivities, List<String> processedActivities, TranslationProcessor tp, SDCProcessor sdcProcessor, WAPCommands wapCommands) {
                ArrayList<String> activitiesfound = new ArrayList<String>();
                StringBuilder consolidatedText = new StringBuilder();
                int consolidates = 0;
                Instant consolStart = null;
                Instant consolEnd = null;
                ArrayList<String> statusFilters = new ArrayList<String>();
                String timemodeFilter = "";
                String reservationFilter = "";
                String duedateFilter = "";
                if (filters != null) {
                    String statusFilter = userConfig.getProperty(userConfigPrefix + "sidebar_status");
                    if (statusFilter.length() == 0) {
                        statusFilter = filters.getProperty("status");
                    }
                    statusFilter = statusFilter.equalsIgnoreCase("All") ? "" : statusFilter.toLowerCase();
                    statusFilters = statusFilter.length() > 0 ? new ArrayList<String>(Arrays.asList(StringUtil.split(statusFilter, ";"))) : new ArrayList();
                    timemodeFilter = userConfig.getProperty(userConfigPrefix + "sidebar_timemode");
                    if (timemodeFilter.length() == 0) {
                        timemodeFilter = filters.getProperty("timemode");
                    }
                    timemodeFilter = timemodeFilter.equalsIgnoreCase("All") ? "" : timemodeFilter;
                    duedateFilter = userConfig.getProperty(userConfigPrefix + "sidebar_duedate");
                    if (duedateFilter.length() == 0) {
                        duedateFilter = filters.getProperty("duedate");
                    }
                    duedateFilter = duedateFilter.equalsIgnoreCase("All") ? "" : duedateFilter;
                }
                String colorbyDisplayOp = "Resource";
                String activityLabelDisplayOp = "[activitylabel]";
                if (displayOptions != null) {
                    String activityLabelDisplayOpSel;
                    colorbyDisplayOp = userConfig.getProperty(userConfigPrefix + "sidebar_colorby");
                    if (colorbyDisplayOp.length() == 0) {
                        colorbyDisplayOp = displayOptions.getProperty("colorby", "Resource");
                    }
                    if ((activityLabelDisplayOpSel = userConfig.getProperty(userConfigPrefix + "sidebar_activitylabel")).length() == 0) {
                        activityLabelDisplayOpSel = displayOptions.getProperty("activitylabel", "");
                    }
                    if (activityLabelDisplayOpSel.length() > 0 && displayOptions.getCollection("activitylabels") != null) {
                        for (int i = 0; i < displayOptions.getCollection("activitylabels").size(); ++i) {
                            if (!displayOptions.getCollection("activitylabels").getPropertyList(i).getProperty("id").equalsIgnoreCase(activityLabelDisplayOpSel)) continue;
                            activityLabelDisplayOp = displayOptions.getCollection("activitylabels").getPropertyList(i).getProperty("label");
                            break;
                        }
                        if (activityLabelDisplayOp.trim().length() == 0) {
                            activityLabelDisplayOp = "[activitylabel]";
                        }
                    }
                }
                for (WAPAvailability av : availability) {
                    List<Activity> activities = av.getActivites();
                    for (Activity activity : activities) {
                        Object ca;
                        if (!this.validateDepartment(activity.getTestingDepartmentid(), departmentids)) continue;
                        String key = activity.getActivityid();
                        if (unassignedActivities != null && unassignedActivities.size() > 0) {
                            for (int b = 0; b < unassignedActivities.size(); ++b) {
                                String cKey;
                                if (unassignedActivities.get(b) == null || !key.equals(cKey = ((Activity)(ca = unassignedActivities.get(b))).getActivityid())) continue;
                                unassignedActivities.remove(b);
                                break;
                            }
                        }
                        if (processedActivities != null) {
                            String pkey;
                            String string = operatingMode == OperatingMode.WORK ? (keyValue.length() > 0 ? keyValue + ";" : "") + activity.getActivityid() : (pkey = activity.getActivityid());
                            if (processedActivities.size() > 0 && processedActivities.contains(pkey)) continue;
                            processedActivities.add(pkey);
                        }
                        if (prefetchedActivities != null) {
                            if (prefetchedActivities.size() <= 0) continue;
                            boolean found = false;
                            ca = prefetchedActivities.iterator();
                            while (ca.hasNext()) {
                                String cKey;
                                Activity prefetchedActivity = ca.next();
                                if (prefetchedActivity == null || !key.equals(cKey = prefetchedActivity.getActivityid())) continue;
                                found = true;
                                break;
                            }
                            if (!found) continue;
                        }
                        if (activitiesfound.contains(key)) continue;
                        try {
                            boolean singleday;
                            Instant endInstantUTC;
                            Instant startInstantUTC;
                            String resourcetype;
                            activitiesfound.add(key);
                            JSONObject event = new JSONObject();
                            String string = resourcetype = activity.getExtra_resourcetype() != null ? activity.getExtra_resourcetype() : "";
                            if (resource.getType().length() > 0 && resourcetype.length() > 0 && !resource.getType().equalsIgnoreCase(resourcetype) || statusFilters.size() > 0 && !statusFilters.contains(activity.getStatus().toLowerCase()) || timemodeFilter.length() > 0 && !timemodeFilter.equalsIgnoreCase(activity.getTimeMode()) || reservationFilter.length() > 0 && (reservationFilter.equalsIgnoreCase("Reservations Only") && !activity.isReservation() || !reservationFilter.equalsIgnoreCase("Reservations Only") && activity.isReservation())) continue;
                            if (duedateFilter.length() > 0) {
                                Instant end;
                                Instant duedate = activity.getWorkDuedt();
                                Instant instant = end = activity.getEndDateInstantUTC() != null ? activity.getEndDateInstantUTC() : activity.getEndRangeInstantUTC();
                                if (duedateFilter.equalsIgnoreCase("Has No Due Date") && duedate != null || duedateFilter.equalsIgnoreCase("Has Due Date") && duedate == null || duedateFilter.equalsIgnoreCase("In Due Date") && (duedate == null || duedate.compareTo(end) <= 0) || duedateFilter.equalsIgnoreCase("Past Due Date") && (duedate == null || duedate.compareTo(end) > 0)) continue;
                            }
                            String text = activity.getLabel();
                            if (activityLabelDisplayOp.length() > 0) {
                                text = AssignmentPageUtil.getEventDisplayLabel(wapCommands, sdcProcessor, activityLabelDisplayOp, activity, displayTimeZone, m18);
                            }
                            event.put("text", text);
                            event.put("activityid", activity.getActivityid());
                            event.put("type", "ACTIVITY");
                            event.put("activitystatus", activity.getStatus());
                            event.put("activityclass", activity.getActivityClass());
                            event.put("resourcetype", resourcetype);
                            if (activity.getTimeMode().equalsIgnoreCase("Fixed")) {
                                startInstantUTC = activity.getStartDateInstantUTC();
                                endInstantUTC = activity.getEndDateInstantUTC();
                                event.put("timemode", "Fixed");
                            } else {
                                Instant startdateUtc = activity.getStartDateInstantUTC();
                                if (startdateUtc != null) {
                                    startInstantUTC = activity.getStartDateInstantUTC();
                                    endInstantUTC = activity.getEndDateInstantUTC();
                                    event.put("fixedfloater", true);
                                } else {
                                    startInstantUTC = activity.getStartRangeInstantUTC();
                                    endInstantUTC = activity.getEndRangeInstantUTC();
                                    event.put("fixedfloater", false);
                                }
                                event.put("timemode", "Floating");
                            }
                            event.put("reservation", activity.isReservation());
                            LocalDateTime startLDT = LocalDateTime.ofInstant(startInstantUTC, displayTimeZone);
                            LocalDateTime endLDT = LocalDateTime.ofInstant(endInstantUTC, displayTimeZone);
                            int daysBetween = (int)ChronoUnit.DAYS.between(startInstantUTC, endInstantUTC);
                            boolean fullday = daysBetween >= 1;
                            boolean bl = singleday = daysBetween == 1;
                            if (!fullday && convertToFullDay) {
                                fullday = true;
                                event.put("real_start_formatted", m18.getDateTimeFormatter(displayTimeZone).format(startLDT));
                                event.put("real_end_formatted", m18.getDateTimeFormatter(displayTimeZone).format(endLDT));
                                event.put("real_start_time_formatted", m18.getTimeFormatter(displayTimeZone).format(startLDT));
                                event.put("real_end_time_formatted", m18.getTimeFormatter(displayTimeZone).format(endLDT));
                                event.put("real_start", startInstantUTC.getEpochSecond() * 1000L);
                                event.put("real_end", endInstantUTC.getEpochSecond() * 1000L);
                                startLDT = CalendarConverter.rewindToStartOfThisDay(startLDT);
                                endLDT = CalendarConverter.forwardToStartOfNextDayAfter(endLDT, startLDT);
                            }
                            if (fullDayOnly && !fullday) continue;
                            if (fullday && consolidateFullday) {
                                if (consolStart == null || consolStart.compareTo(startInstantUTC) > 0) {
                                    consolStart = startInstantUTC;
                                }
                                if (consolEnd == null || consolEnd.compareTo(endInstantUTC) < 0) {
                                    consolEnd = endInstantUTC;
                                }
                                if (consolidatedText.length() > 0) {
                                    consolidatedText.append(", ");
                                }
                                consolidatedText.append(text);
                                ++consolidates;
                                continue;
                            }
                            if (displayTimeZone == null) {
                                displayTimeZone = TimeZone.getDefault().toZoneId();
                            }
                            if (fullday) {
                                AssignmentPage.setEventStartEnd(event, startLDT, endLDT, m18, displayTimeZone);
                            } else {
                                AssignmentPage.setEventStartEnd(event, startLDT, endLDT, m18, displayTimeZone);
                            }
                            event.put("resourcesdc", resource.getResourceSDC().toString());
                            event.put("resourceid", keyValue);
                            event.put("departmentid", "");
                            event.put("shiftid", "");
                            event.put("worksdcid", activity.getWorksdcid());
                            if (activity.getWorkDuedt() != null) {
                                event.put("workduedt", activity.getWorkDuedt());
                                event.put("workduedt_formatted", m18.getDateTimeFormatter(displayTimeZone).format(activity.getWorkDuedt()));
                            }
                            this.addActivityWorkResourceDetails(event, activity, wapCommands);
                            if (colorbyDisplayOp.equalsIgnoreCase("resource")) {
                                if (resourceSDC.equalsIgnoreCase(ResourceSDC.INSTRUMENT.getName())) {
                                    event.put("styleclass", "activity_instrument_" + styleNum);
                                } else if (resourceSDC.equalsIgnoreCase("Department")) {
                                    event.put("styleclass", "activity_workarea_" + styleNum);
                                } else {
                                    event.put("styleclass", "activity_user_" + styleNum);
                                }
                            } else if (colorbyDisplayOp.equalsIgnoreCase("status")) {
                                event.put("styleclass", "activity_status_" + StringUtil.replaceAll(activity.getStatus().toLowerCase(), " ", "_"));
                            } else if (colorbyDisplayOp.equalsIgnoreCase("timemode")) {
                                event.put("styleclass", "activity_timemode_" + activity.getTimeMode().toLowerCase());
                            } else if (colorbyDisplayOp.equalsIgnoreCase("reservation")) {
                                if (activity.isReservation()) {
                                    event.put("styleclass", "activity_reservation");
                                }
                            } else if (colorbyDisplayOp.equalsIgnoreCase("work sdc")) {
                                if (wapCommands.getActivityWorkSDICount(activity.getActivityid()) > 0) {
                                    event.put("styleclass", "activity_worksdc_" + (activity.getWorksdcid().length() > 0 ? activity.getWorksdcid().toLowerCase() : "noworksdcid"));
                                } else {
                                    event.put("styleclass", "activity_worksdc_noworksdcid");
                                }
                            } else if (colorbyDisplayOp.equalsIgnoreCase("due date")) {
                                if (activity.getWorkDuedt() != null) {
                                    Instant enddt;
                                    Instant instant = enddt = activity.getEndDateInstantUTC() != null ? activity.getEndDateInstantUTC() : activity.getEndRangeInstantUTC();
                                    if (enddt.compareTo(activity.getWorkDuedt()) <= 0) {
                                        event.put("styleclass", "activity_duedt_in");
                                    } else {
                                        event.put("styleclass", "activity_duedt_out");
                                    }
                                } else {
                                    event.put("styleclass", "activity_duedt_none");
                                }
                            } else {
                                event.put("styleclass", "activity_nocolor");
                            }
                            event.put("section_id", sectionId);
                            eventData.put(event);
                        }
                        catch (Exception e) {
                            Trace.logError("Failed to process entry: " + e.getMessage(), e);
                        }
                    }
                }
                AssignmentPage.handleConsolidate(eventData, styleNum, keyValue, resourceSDC, consolidateFullday, displayTimeZone, tp, m18, consolidatedText, consolidates, consolStart, consolEnd, resourceSDC);
            }

            private void generateEvents(List<Activity> activities, JSONArray eventData, int styleNum, String keyValue, ResourceSDC resourceSDC, String focusSDC, String departmentid, String shiftid, int sectionId, boolean fullDayOnly, boolean consolidateFullday, boolean convertToFullDay, PropertyList filters, PropertyList displayOptions, ZoneId displayTimeZone, M18NUtil m18, PropertyList userConfig, String userConfigPrefix, OperatingMode operatingMode, TranslationProcessor tp, SDCProcessor sdcProcessor, WAPCommands wapCommands) {
                ArrayList<String> deps = new ArrayList<String>();
                if (departmentid != null && departmentid.length() > 0) {
                    deps.add(departmentid);
                }
                this.generateEvents(activities, eventData, styleNum, keyValue, resourceSDC, focusSDC, deps, shiftid, sectionId, fullDayOnly, consolidateFullday, convertToFullDay, filters, displayOptions, displayTimeZone, m18, userConfig, userConfigPrefix, operatingMode, tp, sdcProcessor, wapCommands);
            }

            private void generateEvents(List<Activity> activities, JSONArray eventData, int styleNum, String keyValue, ResourceSDC resourceSDC, String focusSDC, ArrayList<String> departmentids, String shiftid, int sectionId, boolean fullDayOnly, boolean consolidateFullday, boolean convertToFullDay, PropertyList filters, PropertyList displayOptions, ZoneId displayTimeZone, M18NUtil m18, PropertyList userConfig, String userConfigPrefix, OperatingMode operatingMode, TranslationProcessor tp, SDCProcessor sdcProcessor, WAPCommands wapCommands) {
                ArrayList<String> activitiesfound = new ArrayList<String>();
                Locale viewerLocale = m18.getLocale();
                StringBuilder consolidatedText = new StringBuilder();
                int consolidates = 0;
                Instant consolStart = null;
                Instant consolEnd = null;
                ArrayList<String> statusFilters = new ArrayList<String>();
                String timemodeFilter = "";
                String reservationFilter = "";
                String duedateFilter = "";
                if (filters != null) {
                    String statusFilter = userConfig.getProperty(userConfigPrefix + "sidebar_status");
                    if (statusFilter.length() == 0) {
                        statusFilter = filters.getProperty("status");
                    }
                    statusFilter = statusFilter.equalsIgnoreCase("All") ? "" : statusFilter.toLowerCase();
                    statusFilters = statusFilter.length() > 0 ? new ArrayList<String>(Arrays.asList(StringUtil.split(statusFilter, ";"))) : new ArrayList();
                    timemodeFilter = userConfig.getProperty(userConfigPrefix + "sidebar_timemode");
                    if (timemodeFilter.length() == 0) {
                        timemodeFilter = filters.getProperty("timemode");
                    }
                    timemodeFilter = timemodeFilter.equalsIgnoreCase("All") ? "" : timemodeFilter;
                    duedateFilter = userConfig.getProperty(userConfigPrefix + "sidebar_duedate");
                    if (duedateFilter.length() == 0) {
                        duedateFilter = filters.getProperty("duedate");
                    }
                    duedateFilter = duedateFilter.equalsIgnoreCase("All") ? "" : duedateFilter;
                }
                String colorbyDisplayOp = "Resource";
                String activityLabelDisplayOp = "[activitylabel]";
                if (displayOptions != null) {
                    String activityLabelDisplayOpSel;
                    colorbyDisplayOp = userConfig.getProperty(userConfigPrefix + "sidebar_colorby");
                    if (colorbyDisplayOp.length() == 0) {
                        colorbyDisplayOp = displayOptions.getProperty("colorby", "Resource");
                    }
                    if ((activityLabelDisplayOpSel = userConfig.getProperty(userConfigPrefix + "sidebar_activitylabel")).length() == 0) {
                        activityLabelDisplayOpSel = displayOptions.getProperty("activitylabel", "");
                    }
                    if (activityLabelDisplayOpSel.length() > 0 && displayOptions.getCollection("activitylabels") != null) {
                        for (int i = 0; i < displayOptions.getCollection("activitylabels").size(); ++i) {
                            if (!displayOptions.getCollection("activitylabels").getPropertyList(i).getProperty("id").equalsIgnoreCase(activityLabelDisplayOpSel)) continue;
                            activityLabelDisplayOp = displayOptions.getCollection("activitylabels").getPropertyList(i).getProperty("label");
                            break;
                        }
                        if (activityLabelDisplayOp.trim().length() == 0) {
                            activityLabelDisplayOp = "[activitylabel]";
                        }
                    }
                }
                for (Activity activity : activities) {
                    String key = activity.getActivityid();
                    if (!this.validateDepartment(activity.getTestingDepartmentid(), departmentids) || activitiesfound.contains(key)) continue;
                    try {
                        boolean singleday;
                        Instant endInstantUTC;
                        Instant startInstantUTC;
                        activitiesfound.add(key);
                        JSONObject event = new JSONObject();
                        if (statusFilters.size() > 0 && !statusFilters.contains(activity.getStatus().toLowerCase()) || timemodeFilter.length() > 0 && !timemodeFilter.equalsIgnoreCase(activity.getTimeMode()) || reservationFilter.length() > 0 && (reservationFilter.equalsIgnoreCase("Reservations Only") && !activity.isReservation() || !reservationFilter.equalsIgnoreCase("Reservations Only") && activity.isReservation())) continue;
                        if (duedateFilter.length() > 0) {
                            Instant end;
                            Instant duedate = activity.getWorkDuedt();
                            Instant instant = end = activity.getEndDateInstantUTC() != null ? activity.getEndDateInstantUTC() : activity.getEndRangeInstantUTC();
                            if (duedateFilter.equalsIgnoreCase("Has No Due Date") && duedate != null || duedateFilter.equalsIgnoreCase("Has Due Date") && duedate == null || duedateFilter.equalsIgnoreCase("In Due Date") && (duedate == null || duedate.compareTo(end) <= 0) || duedateFilter.equalsIgnoreCase("Past Due Date") && (duedate == null || duedate.compareTo(end) > 0)) continue;
                        }
                        String text = activity.getLabel();
                        if (activityLabelDisplayOp.length() > 0) {
                            text = AssignmentPageUtil.getEventDisplayLabel(wapCommands, sdcProcessor, activityLabelDisplayOp, activity, displayTimeZone, m18);
                        }
                        event.put("text", text);
                        event.put("activityid", activity.getActivityid());
                        event.put("type", "ACTIVITY");
                        event.put("activitystatus", activity.getStatus());
                        event.put("activityclass", activity.getActivityClass());
                        event.put("resourcetype", "");
                        if (activity.getTimeMode().equalsIgnoreCase("Fixed")) {
                            startInstantUTC = activity.getStartDateInstantUTC();
                            endInstantUTC = activity.getEndDateInstantUTC();
                            event.put("timemode", "Fixed");
                        } else {
                            Instant startdateUtc = activity.getStartDateInstantUTC();
                            if (startdateUtc != null) {
                                startInstantUTC = activity.getStartDateInstantUTC();
                                endInstantUTC = activity.getEndDateInstantUTC();
                                event.put("fixedfloater", true);
                            } else {
                                startInstantUTC = activity.getStartRangeInstantUTC();
                                endInstantUTC = activity.getEndRangeInstantUTC();
                                event.put("fixedfloater", false);
                            }
                            event.put("timemode", "Floating");
                        }
                        event.put("reservation", activity.isReservation());
                        LocalDateTime startLDT = LocalDateTime.ofInstant(startInstantUTC, displayTimeZone);
                        LocalDateTime endLDT = LocalDateTime.ofInstant(endInstantUTC, displayTimeZone);
                        int daysBetween = (int)ChronoUnit.DAYS.between(startInstantUTC, endInstantUTC);
                        boolean fullday = daysBetween >= 1;
                        boolean bl = singleday = daysBetween == 1;
                        if (!fullday && convertToFullDay) {
                            fullday = true;
                            event.put("real_start_formatted", m18.getDateTimeFormatter(displayTimeZone).format(startLDT));
                            event.put("real_end_formatted", m18.getDateTimeFormatter(displayTimeZone).format(endLDT));
                            event.put("real_start_time_formatted", m18.getTimeFormatter(displayTimeZone).format(startLDT));
                            event.put("real_end_time_formatted", m18.getTimeFormatter(displayTimeZone).format(endLDT));
                            event.put("real_start", startInstantUTC.getEpochSecond() * 1000L);
                            event.put("real_end", endInstantUTC.getEpochSecond() * 1000L);
                            startLDT = CalendarConverter.rewindToStartOfThisDay(startLDT);
                            endLDT = CalendarConverter.forwardToStartOfNextDayAfter(endLDT, startLDT);
                        }
                        if (fullDayOnly && !fullday) continue;
                        if (fullday && consolidateFullday) {
                            if (consolStart == null || consolStart.compareTo(startInstantUTC) > 0) {
                                consolStart = startInstantUTC;
                            }
                            if (consolEnd == null || consolEnd.compareTo(endInstantUTC) < 0) {
                                consolEnd = endInstantUTC;
                            }
                            if (consolidatedText.length() > 0) {
                                consolidatedText.append(", ");
                            }
                            consolidatedText.append(text);
                            ++consolidates;
                            continue;
                        }
                        if (displayTimeZone == null) {
                            displayTimeZone = TimeZone.getDefault().toZoneId();
                        }
                        if (fullday) {
                            AssignmentPage.setEventStartEnd(event, startLDT, endLDT, m18, displayTimeZone);
                        } else {
                            AssignmentPage.setEventStartEnd(event, startLDT, endLDT, m18, displayTimeZone);
                        }
                        event.put("resourcesdc", (Object)resourceSDC);
                        event.put("resourceid", keyValue);
                        event.put("departmentid", "");
                        event.put("shiftid", "");
                        event.put("worksdcid", activity.getWorksdcid());
                        if (activity.getWorkDuedt() != null) {
                            event.put("workduedt", activity.getWorkDuedt().getEpochSecond() * 1000L);
                            event.put("workduedt_formatted", m18.getDateFormatter(displayTimeZone).format(activity.getWorkDuedt()));
                        }
                        this.addActivityWorkResourceDetails(event, activity, wapCommands);
                        if (colorbyDisplayOp.equalsIgnoreCase("resource")) {
                            if (focusSDC.equalsIgnoreCase(ResourceSDC.INSTRUMENT.getName())) {
                                event.put("styleclass", "activity_instrument_" + styleNum);
                            } else if (focusSDC.equalsIgnoreCase("Department")) {
                                event.put("styleclass", "activity_workarea_" + styleNum);
                            } else {
                                event.put("styleclass", "activity_user_" + styleNum);
                            }
                        } else if (colorbyDisplayOp.equalsIgnoreCase("status")) {
                            event.put("styleclass", "activity_status_" + StringUtil.replaceAll(activity.getStatus().toLowerCase(), " ", "_"));
                        } else if (colorbyDisplayOp.equalsIgnoreCase("timemode")) {
                            event.put("styleclass", "activity_timemode_" + activity.getTimeMode().toLowerCase());
                        } else if (colorbyDisplayOp.equalsIgnoreCase("reservation")) {
                            if (activity.isReservation()) {
                                event.put("styleclass", "activity_reservation");
                            }
                        } else if (colorbyDisplayOp.equalsIgnoreCase("work sdc")) {
                            if (wapCommands.getActivityWorkSDICount(activity.getActivityid()) > 0) {
                                event.put("styleclass", "activity_worksdc_" + (activity.getWorksdcid().length() > 0 ? activity.getWorksdcid().toLowerCase() : "noworksdcid"));
                            } else {
                                event.put("styleclass", "activity_worksdc_noworksdcid");
                            }
                            event.put("styleclass", "activity_worksdc_" + (activity.getWorksdcid().length() > 0 ? activity.getWorksdcid().toLowerCase() : "noworksdcid"));
                        } else if (colorbyDisplayOp.equalsIgnoreCase("due date")) {
                            if (activity.getWorkDuedt() != null) {
                                Instant enddt;
                                Instant instant = enddt = activity.getEndDateInstantUTC() != null ? activity.getEndDateInstantUTC() : activity.getEndRangeInstantUTC();
                                if (enddt.compareTo(activity.getWorkDuedt()) <= 0) {
                                    event.put("styleclass", "activity_duedt_in");
                                } else {
                                    event.put("styleclass", "activity_duedt_out");
                                }
                            } else {
                                event.put("styleclass", "activity_duedt_none");
                            }
                        } else {
                            event.put("styleclass", "activity_nocolor");
                        }
                        event.put("section_id", sectionId);
                        eventData.put(event);
                    }
                    catch (Exception e) {
                        Trace.logError("Failed to process entry: " + e.getMessage(), e);
                    }
                }
                AssignmentPage.handleConsolidate(eventData, styleNum, keyValue, focusSDC, consolidateFullday, displayTimeZone, tp, m18, consolidatedText, consolidates, consolStart, consolEnd, resourceSDC.toString());
            }

            @Override
            protected JSONArray getEventData(AssignmentPage page, JSONObject metadata, M18NUtil m18) {
                String fSdc;
                String fId;
                String temp;
                JSONArray perE = new JSONArray();
                if (metadata != null) {
                    try {
                        metadata.put("workload", perE);
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                }
                JSONArray eventData = new JSONArray();
                if (page.clientFromDate == null || page.clientToDate == null) {
                    ZonedDateTime now = ZonedDateTime.now(page.displayTimeZone);
                    AssignmentPageTimeRange timeRange = new AssignmentPageTimeRange(now, now);
                    AssignmentPageUtil.adjustRangeToCalendarView(CalendarView.MONTH, timeRange, new M18NUtil(page.connectionInfo));
                    page.setClientDateRange(timeRange);
                }
                PropertyList filters = new PropertyList();
                if (page.getElementProperties() != null && page.getElementProperties().getPropertyList("pagedata") != null && page.getElementProperties().getPropertyList("pagedata").getPropertyList("filters") != null) {
                    filters = page.getElementProperties().getPropertyList("pagedata").getPropertyList("filters");
                }
                PropertyList displayoptions = null;
                if (page.getElementProperties() != null && page.getElementProperties().getPropertyList("pagedata") != null && page.getElementProperties().getPropertyList("pagedata").getPropertyList("displayoptions") != null) {
                    displayoptions = page.getElementProperties().getPropertyList("pagedata").getPropertyList("displayoptions");
                }
                boolean appointmentsDisplayOp = (temp = page.userConfig.getProperty(page.userConfigPrefix + "sidebar_appointments")).length() == 0 ? displayoptions == null || displayoptions.getProperty("appointments", "Y").equalsIgnoreCase("Y") : temp.equalsIgnoreCase("Y");
                temp = page.userConfig.getProperty(page.userConfigPrefix + "sidebar_unassigned");
                boolean unassignedDisplayOp = temp.length() == 0 ? displayoptions == null || displayoptions.getProperty("unassigned", "Y").equalsIgnoreCase("Y") : temp.equalsIgnoreCase("Y");
                String workloadsDisplayOp = page.userConfig.getProperty(page.userConfigPrefix + "sidebar_workload");
                if (workloadsDisplayOp.length() == 0) {
                    String string = workloadsDisplayOp = displayoptions != null ? displayoptions.getProperty("workload", "Percentage Workload") : "Percentage Workload";
                }
                if (page.getFocusId().contains("|")) {
                    String[] t = StringUtil.split(page.getFocusId(), "|");
                    fId = t[0];
                    fSdc = t[1];
                } else {
                    fId = page.getFocusId();
                    fSdc = page.getResourceSDC() != null ? page.getResourceSDC().getName() : "";
                }
                WAPCommands wapCommands = new WAPCommands(page.getConnectionId());
                ArrayList<String> processedActivities = new ArrayList<String>();
                if (page.operatingMode == OperatingMode.WORK) {
                    String timemodeFilter;
                    WAPSelector wapSelector = new WAPSelector(page.getConnectionId());
                    WAPSelectorOptions wapoptions = new WAPSelectorOptions();
                    wapoptions.setIncludeFixed(true);
                    String statusFilter = page.userConfig.getProperty(page.userConfigPrefix + "sidebar_status");
                    if (statusFilter.length() == 0) {
                        statusFilter = filters.getProperty("status");
                    }
                    if (statusFilter.length() > 0 && !statusFilter.equalsIgnoreCase("All")) {
                        wapoptions.setActivityStatusList(statusFilter);
                    }
                    if ((timemodeFilter = page.userConfig.getProperty(page.userConfigPrefix + "sidebar_timemode")).length() == 0) {
                        timemodeFilter = filters.getProperty("timemode");
                    }
                    if (timemodeFilter.length() > 0 && !timemodeFilter.equalsIgnoreCase("All")) {
                        if (timemodeFilter.equalsIgnoreCase("fixed")) {
                            wapoptions.setIncludeFixed(true);
                            wapoptions.setIncludeFloating(false);
                        } else {
                            wapoptions.setIncludeFixed(false);
                            wapoptions.setIncludeFloating(true);
                        }
                    }
                    if (page.getFocusedResource() != null) {
                        if (page.getFocusedResource().getResourceSDC() == ResourceSDC.USER) {
                            wapoptions.setPendingInstrumentAssignment(false);
                            wapoptions.setPendingUserAssignment(true);
                        } else {
                            wapoptions.setPendingInstrumentAssignment(true);
                            wapoptions.setPendingUserAssignment(false);
                        }
                    }
                    wapoptions.setIncludeFixedOverlaps(true);
                    wapoptions.setIncludeFloatingOverlaps(true);
                    if (page.getSDCId().equalsIgnoreCase("LV_Activity") && page.getKeyId1().length() > 0) {
                        String rt;
                        StringBuilder ew = new StringBuilder();
                        if (page.getActivityQuery().length() > 0) {
                            ew.append("a.activityid IN (").append(page.getActivityQuery()).append(")");
                        } else {
                            ew.append("a.activityid IN ('").append(StringUtil.replaceAll(page.getKeyId1(), ";", "','")).append("')");
                        }
                        if (page.getResourceSDC() == ResourceSDC.INSTRUMENT && page.element != null && page.element.containsKey("pagedata")) {
                            rt = page.element.getPropertyList("pagedata").getProperty("instrumenttype", page.element.getPropertyList("pagedata").getProperty("instrumenttypeid", ""));
                            String rm = page.element.getPropertyList("pagedata").getProperty("instrumentmodel", page.element.getPropertyList("pagedata").getProperty("instrumentmodelid", ""));
                            if (rt.length() > 0) {
                                wapoptions.setInstrumenttypeid(rt);
                            }
                            if (rm.length() > 0) {
                                wapoptions.setInstrumentmodelid(rm);
                            }
                        } else if (page.getResourceSDC() == ResourceSDC.USER && page.element != null && page.element.containsKey("pagedata") && (rt = page.element.getPropertyList("pagedata").getProperty("analysttype", page.element.getPropertyList("pagedata").getProperty("analysttypeid", ""))).length() > 0) {
                            wapoptions.setAnalysttype(rt);
                        }
                        wapoptions.setExtraActiviytWhere(ew.toString());
                    }
                    boolean showUnassigned = unassignedDisplayOp && page.getSDCId().equalsIgnoreCase("LV_Activity");
                    try {
                        List<Activity> unassignedActivities = null;
                        if (showUnassigned) {
                            unassignedActivities = wapSelector.getActivitiesBetween(page.clientFromDate.toInstant(), page.clientToDate.toInstant(), wapoptions);
                        }
                        List<Activity> prefetchedActivities = null;
                        if (page.getSDCId().equalsIgnoreCase("LV_Activity") && page.getKeyId1().length() > 0) {
                            wapoptions.setPendingUserAssignment(false);
                            wapoptions.setPendingInstrumentAssignment(false);
                            prefetchedActivities = wapSelector.getActivitiesBetween(page.clientFromDate.toInstant(), page.clientToDate.toInstant(), wapoptions);
                        }
                        this.generateAssignedEvents(page, fSdc, fId, appointmentsDisplayOp, workloadsDisplayOp, filters, displayoptions, eventData, metadata, perE, page.displayTimeZone, m18, unassignedActivities, prefetchedActivities, processedActivities, wapCommands);
                        if (unassignedActivities != null && unassignedActivities.size() > 0 && metadata != null) {
                            JSONArray timelinesections = metadata.getJSONArray("timelinesections");
                            if (timelinesections == null) {
                                timelinesections = new JSONArray();
                            }
                            JSONObject timelineSection = new JSONObject();
                            timelineSection.put("key", "-99");
                            timelineSection.put("resourceid", "");
                            timelineSection.put("resourceisworkarea", false);
                            timelineSection.put("resourcesdcid", (Object)page.getResourceSDC());
                            timelineSection.put("label", page.getTranslationProcessor().translate("Unassigned"));
                            StringBuilder h = new StringBuilder();
                            h.append("<div class=\"icon\"><div class=\"icon_img\">");
                            Image image = new Image(page.pageContext);
                            image.setTitle(page.getTranslationProcessor().translate("Unassigned activities"));
                            image.setImageId("FlatBlackQuestion");
                            h.append(image.getHtml());
                            h.append("</div></div>");
                            timelineSection.put("icon", h.toString());
                            timelinesections.put(timelineSection);
                            this.generateEvents(unassignedActivities, eventData, 99, "", page.getResourceSDC(), page.getResourceSDC().getName(), "", "", -99, page.getCalendarView().isTimeline(), page.getCalendarView() == CalendarView.DAY || page.getCalendarView() == CalendarView.WEEK, page.getCalendarView().isTimeline(), filters, displayoptions, page.displayTimeZone, m18, page.userConfig, page.userConfigPrefix, page.operatingMode, page.getTranslationProcessor(), page.getSDCProcessor(), wapCommands);
                            metadata.put("timelinesections", timelinesections);
                        }
                    }
                    catch (Exception exception) {}
                } else {
                    this.generateAssignedEvents(page, fSdc, fId, appointmentsDisplayOp, workloadsDisplayOp, filters, displayoptions, eventData, metadata, perE, page.displayTimeZone, m18, null, null, processedActivities, wapCommands);
                }
                return eventData;
            }

            private void generateCalendarItems(List<CalendarItem> calendarItems, int sectionKey, JSONArray eventData, ZoneId outputTimeZone, M18NUtil m18, ZoneId displayTimeZone) {
                for (CalendarItem item : calendarItems) {
                    try {
                        JSONObject event = new JSONObject();
                        event.put("text", item.getCalendarItemLabel());
                        LocalDateTime startDateOutputZone = LocalDateTime.ofInstant(item.getStartDate(), outputTimeZone);
                        LocalDateTime endDateOutputZone = LocalDateTime.ofInstant(item.getEndDate(), outputTimeZone);
                        if (item.isAllDayFlag()) {
                            AssignmentPage.setEventStartEnd(event, startDateOutputZone, endDateOutputZone, m18, displayTimeZone);
                            event.put("section_id", sectionKey);
                        } else {
                            AssignmentPage.setEventStartEnd(event, startDateOutputZone, endDateOutputZone, m18, displayTimeZone);
                            event.put("section_id", sectionKey);
                        }
                        event.put("type", "CALENDARITEM");
                        eventData.put(event);
                    }
                    catch (Exception exception) {}
                }
            }

            private boolean validateDepartment(String departmentid, ArrayList<String> departments) {
                boolean departmentValidated = false;
                if (departmentid == null || departmentid.length() == 0 || departments == null || departments.size() == 0) {
                    departmentValidated = true;
                } else if (departmentid.length() > 0 && departments != null && departments.size() > 0 && departments.contains(departmentid)) {
                    departmentValidated = true;
                }
                return departmentValidated;
            }

            /*
             * Unable to fully structure code
             * Enabled aggressive block sorting
             * Enabled unnecessary exception pruning
             * Enabled aggressive exception aggregation
             */
            private void generateAssignedEvents(AssignmentPage page, String fSdc, String fId, boolean appointmentsDisplayOp, String workloadsDisplayOp, PropertyList filters, PropertyList displayoptions, JSONArray eventData, JSONObject metadata, JSONArray perE, ZoneId displayTimeZone, M18NUtil m18, List<Activity> unassigneddActivities, List<Activity> preFetchedActivities, List<String> processedActivities, WAPCommands wapCommands) {
                block111: {
                    block112: {
                        block113: {
                            block114: {
                                block110: {
                                    block107: {
                                        block108: {
                                            block109: {
                                                calendarFactory = new CalendarFactory(page.getConnectionId());
                                                try {
                                                    block116: {
                                                        calendarItems = null;
                                                        if (appointmentsDisplayOp) {
                                                            if (fSdc.equalsIgnoreCase(ResourceSDC.USER.getName()) && fId.length() > 0) {
                                                                lvCalendar = calendarFactory.getUserCalendar(fId, true, true);
                                                                calendarItems = lvCalendar.getCalendarItemsBetween(AssignmentPage.access$700(page).toInstant(), AssignmentPage.access$800(page).toInstant(), true);
                                                            } else if (fSdc.equalsIgnoreCase(ResourceSDC.INSTRUMENT.getName()) && fId.length() > 0) {
                                                                lvCalendar = calendarFactory.getInstrumentCalendar(fId, true, true);
                                                                calendarItems = lvCalendar.getCalendarItemsBetween(AssignmentPage.access$700(page).toInstant(), AssignmentPage.access$800(page).toInstant(), true);
                                                            } else if (fSdc.equalsIgnoreCase("Department") && fId.length() > 0) {
                                                                lvCalendar = calendarFactory.getDepartmentCalendar(fId, true, true);
                                                                calendarItems = lvCalendar.getCalendarItemsBetween(AssignmentPage.access$700(page).toInstant(), AssignmentPage.access$800(page).toInstant(), true);
                                                            }
                                                        }
                                                        wapAvailabilitySelector = new WAPAvailabilitySelector(page.getConnectionId());
                                                        wapAvailabilityOptions = new WAPAvailabilityOptions();
                                                        wapAvailabilityOptions.setActivityStatusList("Draft;Activated;In Progress");
                                                        availability = null;
                                                        if (fId != null && fId.length() > 0) {
                                                            if (fSdc.equalsIgnoreCase(ResourceSDC.INSTRUMENT.getName())) {
                                                                try {
                                                                    availability = wapAvailabilitySelector.getInstrumentAvailabilityBetween(fId, AssignmentPage.access$700(page).toInstant(), AssignmentPage.access$800(page).toInstant(), wapAvailabilityOptions, displayTimeZone);
                                                                }
                                                                catch (Exception e) {
                                                                    AssignmentPage.access$3500(page).warn("Failed to find instrument availability.");
                                                                }
                                                            } else if (fSdc.equalsIgnoreCase("Department") && page.getFocusedResource().getResourceSDC() == ResourceSDC.INSTRUMENT) {
                                                                try {
                                                                    availability = wapAvailabilitySelector.getInstrumentWorkareaAvailabilityBetween(fId, AssignmentPage.access$700(page).toInstant(), AssignmentPage.access$800(page).toInstant(), wapAvailabilityOptions, page.getFocusedResource().resourceType, page.getFocusedResource().resourceModel, displayTimeZone);
                                                                }
                                                                catch (Exception e) {
                                                                    AssignmentPage.access$3600(page).warn("Failed to find workarea availability.");
                                                                }
                                                            } else if (fSdc.equalsIgnoreCase("Department") && page.getFocusedResource().getResourceSDC() == ResourceSDC.USER) {
                                                                try {
                                                                    availability = wapAvailabilitySelector.getUserWorkareaAvailabilityBetween(fId, AssignmentPage.access$700(page).toInstant(), AssignmentPage.access$800(page).toInstant(), wapAvailabilityOptions, displayTimeZone);
                                                                }
                                                                catch (Exception e) {
                                                                    AssignmentPage.access$3700(page).warn("Failed to find instrument workarea availability.");
                                                                }
                                                            } else {
                                                                try {
                                                                    availability = wapAvailabilitySelector.getUserAvailabilityBetween(fId, AssignmentPage.access$700(page).toInstant(), AssignmentPage.access$800(page).toInstant(), wapAvailabilityOptions, displayTimeZone);
                                                                }
                                                                catch (Exception e) {
                                                                    AssignmentPage.access$3800(page).warn("Failed to find user availability.");
                                                                }
                                                            }
                                                            sectionKey = 10;
                                                            availabilities = new JSONObject();
                                                            if (availability == null) break block107;
                                                            timelinesections = new JSONArray();
                                                            if (!fSdc.equalsIgnoreCase(ResourceSDC.USER.getName())) break block108;
                                                            transientUserArray = page.getFocusedResource().transientSDI != null && page.getFocusedResource().transientSDI.length() > 0 ? StringUtil.split(page.getFocusedResource().transientSDI, ";") : new String[]{};
                                                            transientItem = false;
                                                            var27_45 = transientUserArray;
                                                            var28_48 = var27_45.length;
                                                            break block109;
                                                        }
                                                        availabilities = new JSONObject();
                                                        sectionKey = 0;
                                                        if (page.getResourceSDC() != ResourceSDC.INSTRUMENT) break block116;
                                                        v0 = instrumentData = page.getFocusedResource() != null && page.getFocusedResource().getResourceSDC() == ResourceSDC.INSTRUMENT ? page.getFocusedResource() : null;
                                                        if (instrumentData != null && instrumentData.getData() != null) {
                                                            instruments = instrumentData.getData();
                                                            timelinesections = new JSONArray();
                                                            uc = 0;
                                                            wapHiddenSelector = new WAPSelector(page.getConnectionId());
                                                            break block110;
                                                        }
                                                        ** GOTO lbl138
                                                    }
                                                    v1 = userData = page.getFocusedResource() != null && page.getFocusedResource().getResourceSDC() == ResourceSDC.USER ? page.getFocusedResource() : null;
                                                    if (userData == null || userData.getData() == null) ** GOTO lbl138
                                                    users = userData.getData();
                                                    timelinesections = new JSONArray();
                                                    uc = 0;
                                                    wapSelector = null;
                                                    permWAUsersList = null;
                                                    var30_74 = new WAPSelector(page.getConnectionId());
                                                    transientUserArray = null;
                                                    workAreaSelector = null;
                                                    i = 0;
lbl80:
                                                    // 2 sources

                                                    while (true) {
                                                        if (i < users.getRowCount()) {
                                                            basedepartment = users.getValue(i, "basedepartment", "");
                                                            departments = new ArrayList<String>();
                                                            if (basedepartment != null && basedepartment.length() > 0) {
                                                                departments.add(basedepartment);
                                                            }
                                                            if (userData != null && userData.getDetail() != null) {
                                                                for (d = 0; d < userData.getDetail().getRowCount(); ++d) {
                                                                    if (userData.getDetail().getValue(d, "departmentid", "").length() <= 0) continue;
                                                                    departments.add(userData.getDetail().getValue(d, "departmentid", ""));
                                                                }
                                                            }
                                                            if (!this.validateDepartment(page.getDepartmentId(), departments)) break block111;
                                                            sysuserid = users.getValue(i, "sysuserid", "");
                                                            try {
                                                                analysttype = userData.getType();
                                                                wapAvailabilityOptions.setAnalystType(analysttype);
                                                                availability = wapAvailabilitySelector.getUserByTypeAvailabilityBetween(sysuserid, analysttype, AssignmentPage.access$700(page).toInstant(), AssignmentPage.access$800(page).toInstant(), wapAvailabilityOptions, displayTimeZone);
                                                            }
                                                            catch (Exception e) {
                                                                AssignmentPage.access$6300(page).warn("Failed to find users availability.");
                                                            }
                                                            AssignmentPage.access$3900(page, availability, availabilities, true);
                                                            sectionKey = (i + 1) * 10;
                                                            if (!page.getCalendarView().isTimeline()) break block112;
                                                            timelineSection = new JSONObject();
                                                            timelineSection.put("key", sectionKey);
                                                            timelineSection.put("resourceid", sysuserid);
                                                            timelineSection.put("resourcesdcid", ResourceSDC.USER.toString());
                                                            timelineSection.put("resourceisworkarea", false);
                                                            timelinesectionLabel = users.getValue(i, "sysuserdesc", sysuserid);
                                                            resHtml = new StringBuilder();
                                                            AssignmentPageUtil.renderWorkloadIcon(resHtml, AssignmentPage.access$4100(page), AssignmentPage.access$1600(page), sysuserid + "|" + ResourceSDC.USER.getName(), true, AssignmentPage.access$700(page), AssignmentPage.access$800(page), availability, page.getColorScheme(), page.getConnectionId(), AssignmentPage.access$6400(page), AssignmentPage.access$6500(page), displayTimeZone);
                                                            timelineSection.put("icon", resHtml.toString());
                                                            addTimelineSection = true;
                                                            if (page.getSDCId().equalsIgnoreCase("Department") && page.getKeyId1().length() > 0 && page.getOperatingMode() == OperatingMode.WORK) {
                                                                if (permWAUsersList == null) {
                                                                    perm = wapCommands.getPermanentWorkareaUsers(page.getKeyId1());
                                                                    v2 = permWAUsersList = perm.length() > 0 ? new ArrayList(Arrays.asList(StringUtil.split(perm, ";"))) : new ArrayList<E>();
                                                                }
                                                                if (!permWAUsersList.contains(sysuserid)) {
                                                                    if (wapSelector == null) {
                                                                        wapSelector = new WAPSelector(page.getConnectionId());
                                                                    }
                                                                    if ((dates = wapSelector.getScheduledWorkAreaUsers(page.getKeyId1(), AssignmentPage.access$700(page).toInstant(), AssignmentPage.access$800(page).toInstant())) != null && dates.size() <= 0) {
                                                                        // empty if block
                                                                    }
                                                                }
                                                                break block113;
                                                            }
                                                            if (transientUserArray == null) {
                                                                transientUserArray = page.getFocusedResource().transientSDI != null && page.getFocusedResource().transientSDI.length() > 0 ? StringUtil.split(page.getFocusedResource().transientSDI, ";") : new String[]{};
                                                            }
                                                            transientItem = false;
                                                            var42_104 = transientUserArray;
                                                            var43_105 = var42_104.length;
                                                            break block114;
                                                        }
                                                        metadata.put("timelinesections", timelinesections);
lbl138:
                                                        // 4 sources

                                                        while (true) {
                                                            v3 = workareas = page.getFocusedResource() != null ? page.getFocusedResource().getWorkareas() : null;
                                                            if (workareas != null) {
                                                                timelinesections = metadata.has("timelinesections") != false ? metadata.getJSONArray("timelinesections") : new JSONArray();
                                                                uc = 0;
                                                                wapHiddenSelector = new WAPSelector(page.getConnectionId());
                                                                for (i = 0; i < workareas.getRowCount(); ++i) {
                                                                    parentdepartment = workareas.getValue(i, "parentdepartmentid", "");
                                                                    if (AssignmentPage.access$4800(page) != null && AssignmentPage.access$4800(page).length() != 0 && parentdepartment != null && parentdepartment.length() != 0 && !parentdepartment.equals(AssignmentPage.access$4800(page))) continue;
                                                                    try {
                                                                        if (page.getResourceSDC() == ResourceSDC.INSTRUMENT) {
                                                                            availability = wapAvailabilitySelector.getInstrumentWorkareaAvailabilityBetween(workareas.getValue(i, "departmentid", ""), AssignmentPage.access$700(page).toInstant(), AssignmentPage.access$800(page).toInstant(), wapAvailabilityOptions, page.getFocusedResource().resourceType, page.getFocusedResource().resourceModel, displayTimeZone);
                                                                        } else {
                                                                            var30_77 = page.getFocusedResource().getType();
                                                                            wapAvailabilityOptions.setAnalystType(var30_77);
                                                                            availability = wapAvailabilitySelector.getUserWorkareaAvailabilityBetween(workareas.getValue(i, "departmentid", ""), AssignmentPage.access$700(page).toInstant(), AssignmentPage.access$800(page).toInstant(), wapAvailabilityOptions, displayTimeZone);
                                                                        }
                                                                    }
                                                                    catch (Exception var30_78) {
                                                                        AssignmentPage.access$7000(page).warn("Failed to find department availability.");
                                                                    }
                                                                    AssignmentPage.access$3900(page, availability, availabilities, false);
                                                                    sectionKey += (i + 1) * 10;
                                                                    if (page.getCalendarView().isTimeline()) {
                                                                        var30_79 = new JSONObject();
                                                                        var30_79.put("key", sectionKey);
                                                                        var30_79.put("resourceid", workareas.getValue(i, "departmentid", ""));
                                                                        var30_79.put("resourcesdcid", page.getResourceSDC().toString());
                                                                        var30_79.put("resourceisworkarea", true);
                                                                        var30_79.put("label", workareas.getValue(i, "departmentdesc", workareas.getValue(i, "departmentid", "")));
                                                                        resHtml = new StringBuilder();
                                                                        AssignmentPageUtil.renderWorkloadIcon(resHtml, AssignmentPage.access$4100(page), AssignmentPage.access$1600(page), workareas.getValue(i, "departmentid", "") + "|Department", true, AssignmentPage.access$700(page), AssignmentPage.access$800(page), availability, page.getColorScheme(), page.getConnectionId(), AssignmentPage.access$7100(page), AssignmentPage.access$7200(page), displayTimeZone);
                                                                        var30_79.put("icon", resHtml.toString());
                                                                        timelinesections.put(var30_79);
                                                                        resourceAvailabilities = new JSONObject();
                                                                        AssignmentPage.access$3900(page, availability, resourceAvailabilities, false);
                                                                        if (!workloadsDisplayOp.equalsIgnoreCase("None")) {
                                                                            workload = new JSONArray();
                                                                            AssignmentPage.access$4000(page, resourceAvailabilities, workload, workloadsDisplayOp);
                                                                            metadata.put("workload-" + sectionKey, workload);
                                                                        }
                                                                        if (appointmentsDisplayOp && (currentCalItems = (lvCalendar = calendarFactory.getDepartmentCalendar(workareas.getValue(i, "departmentid", ""), true, true)).getCalendarItemsBetween(AssignmentPage.access$700(page).toInstant(), AssignmentPage.access$800(page).toInstant(), true)) != null) {
                                                                            this.generateCalendarItems(currentCalItems, sectionKey, eventData, displayTimeZone, m18, displayTimeZone);
                                                                        }
                                                                    }
                                                                    this.generateEvents(availability, eventData, uc, workareas.getValue(i, "departmentid", ""), page.getFocusedResource(), "Department", workareas.getValue(i, "parentdepartmentid", ""), "", sectionKey, page.getCalendarView().isTimeline(), page.getCalendarView() == CalendarView.DAY || page.getCalendarView() == CalendarView.WEEK, page.getCalendarView().isTimeline(), filters, displayoptions, displayTimeZone, m18, AssignmentPage.access$1300(page), AssignmentPage.access$1200(page), AssignmentPage.access$500(page), unassigneddActivities, preFetchedActivities, processedActivities, AssignmentPage.access$7300(page), AssignmentPage.access$7400(page), wapCommands);
                                                                    if (filters == null || filters.getProperty("status").length() == 0 || filters.getProperty("status").equalsIgnoreCase("All") || filters.getProperty("status").contains("Cancelled") || filters.getProperty("status").contains("Completed")) {
                                                                        var30_80 = new WAPSelectorOptions();
                                                                        var30_80.setActivityStatusList("Cancelled;Completed");
                                                                        if (page.getFocusedResource().getResourceSDC() == ResourceSDC.USER) {
                                                                            var30_80.setAssignedUserWorkarea(workareas.getValue(i, "departmentid", ""));
                                                                        } else {
                                                                            var30_80.setAssignedInstrumentWorkarea(workareas.getValue(i, "departmentid", ""), "", "");
                                                                        }
                                                                        hiddenActivities = wapHiddenSelector.getActivitiesBetween(AssignmentPage.access$700(page).toInstant(), AssignmentPage.access$800(page).toInstant(), var30_80);
                                                                        if (hiddenActivities != null && hiddenActivities.size() > 0) {
                                                                            this.generateEvents(hiddenActivities, eventData, uc, workareas.getValue(i, "departmentid", ""), page.getFocusedResource().getResourceSDC(), fSdc, workareas.getValue(i, "parentdepartmentid", ""), "", sectionKey, page.getCalendarView().isTimeline(), page.getCalendarView() == CalendarView.DAY || page.getCalendarView() == CalendarView.WEEK, page.getCalendarView().isTimeline(), filters, displayoptions, displayTimeZone, m18, AssignmentPage.access$1300(page), AssignmentPage.access$1200(page), AssignmentPage.access$500(page), AssignmentPage.access$7500(page), AssignmentPage.access$7600(page), wapCommands);
                                                                        }
                                                                    }
                                                                    if (uc > 5) {
                                                                        uc = 0;
                                                                        continue;
                                                                    }
                                                                    ++uc;
                                                                }
                                                                metadata.put("timelinesections", timelinesections);
                                                            }
                                                            if (page.getCalendarView().isTimeline() != false) return;
                                                            if (workloadsDisplayOp.equalsIgnoreCase("None") != false) return;
                                                            AssignmentPage.access$4000(page, availabilities, perE, workloadsDisplayOp);
                                                            return;
                                                        }
                                                        break;
                                                    }
                                                }
                                                catch (Exception var18_19) {
                                                    // empty catch block
                                                }
                                                return;
                                            }
                                            for (var29_52 = 0; var29_52 < var28_48; ++var29_52) {
                                                var30_60 = var27_45[var29_52];
                                                if (!fId.equalsIgnoreCase(var30_60)) continue;
                                                transientItem = true;
                                                break;
                                            }
                                            if (transientItem) {
                                                schedWorkareas = null;
                                                workAreaSelector = new WAPSelector(page.getConnectionId());
                                                try {
                                                    schedWorkareas = workAreaSelector != null ? workAreaSelector.getScheduledUserWorkAreas(fId, AssignmentPage.access$700(page).toInstant(), AssignmentPage.access$800(page).toInstant()) : null;
                                                }
                                                catch (Exception var29_53) {
                                                    // empty catch block
                                                }
                                                if (schedWorkareas != null && page.getFocusedResource().getWorkareas() != null && page.getFocusedResource().getWorkareas().getRowCount() > 0) {
                                                    for (a = 0; a < availability.size(); ++a) {
                                                    }
                                                } else {
                                                    for (WAPAvailability var30_62 : availability) {
                                                    }
                                                }
                                            }
                                        }
                                        AssignmentPage.access$3900(page, availability, availabilities, fSdc.equalsIgnoreCase(ResourceSDC.USER.getName()));
                                        if (!workloadsDisplayOp.equalsIgnoreCase("None")) {
                                            if (!page.getCalendarView().isTimeline()) {
                                                AssignmentPage.access$4000(page, availabilities, perE, workloadsDisplayOp);
                                            } else {
                                                workloaddata = new JSONArray();
                                                AssignmentPage.access$4000(page, availabilities, workloaddata, workloadsDisplayOp);
                                                metadata.put("workload-" + sectionKey, workloaddata);
                                            }
                                        }
                                        isWorkarea = fSdc.equalsIgnoreCase("Department");
                                        if (page.getCalendarView().isTimeline()) {
                                            timelineSection = new JSONObject();
                                            timelineSection.put("key", sectionKey);
                                            timelineSection.put("resourceid", fId);
                                            timelineSection.put("resourceisworkarea", isWorkarea);
                                            timelineSection.put("resourcesdcid", (Object)page.getResourceSDC());
                                            timelineSection.put("label", fId);
                                            resHtml = new StringBuilder();
                                            AssignmentPageUtil.renderWorkloadIcon(resHtml, AssignmentPage.access$4100(page), AssignmentPage.access$1600(page), page.getFocusId(), true, AssignmentPage.access$700(page), AssignmentPage.access$800(page), availability, page.getColorScheme(), page.getConnectionId(), AssignmentPage.access$4200(page), AssignmentPage.access$4300(page), displayTimeZone);
                                            timelineSection.put("icon", resHtml.toString());
                                            timelinesections.put(timelineSection);
                                        }
                                        this.generateEvents(availability, eventData, 0, fId, page.getFocusedResource(), fSdc, "", "", sectionKey, page.getCalendarView().isTimeline(), page.getCalendarView() == CalendarView.DAY || page.getCalendarView() == CalendarView.WEEK, page.getCalendarView().isTimeline(), filters, displayoptions, displayTimeZone, m18, AssignmentPage.access$1300(page), AssignmentPage.access$1200(page), AssignmentPage.access$500(page), unassigneddActivities, preFetchedActivities, processedActivities, AssignmentPage.access$4400(page), AssignmentPage.access$4500(page), wapCommands);
                                        metadata.put("timelinesections", timelinesections);
                                    }
                                    if (filters == null || filters.getProperty("status").length() == 0 || filters.getProperty("status").equalsIgnoreCase("All") || filters.getProperty("status").contains("Cancelled") || filters.getProperty("status").contains("Completed")) {
                                        wapSelector = new WAPSelector(page.getConnectionId());
                                        wapSelectorOptions = new WAPSelectorOptions();
                                        wapSelectorOptions.setActivityStatusList("Cancelled;Completed");
                                        if (page.getFocusedResource().getResourceSDC() == ResourceSDC.USER) {
                                            wapSelectorOptions.setAssignedUser(fId);
                                        } else {
                                            wapSelectorOptions.setAssignedInstrument(fId);
                                        }
                                        hiddenActivities = wapSelector.getActivitiesBetween(AssignmentPage.access$700(page).toInstant(), AssignmentPage.access$800(page).toInstant(), wapSelectorOptions);
                                        if (hiddenActivities != null && hiddenActivities.size() > 0) {
                                            this.generateEvents(hiddenActivities, eventData, 0, fId, page.getFocusedResource().getResourceSDC(), fSdc, "", "", sectionKey, page.getCalendarView().isTimeline(), page.getCalendarView() == CalendarView.DAY || page.getCalendarView() == CalendarView.WEEK, page.getCalendarView().isTimeline(), filters, displayoptions, displayTimeZone, m18, AssignmentPage.access$1300(page), AssignmentPage.access$1200(page), AssignmentPage.access$500(page), AssignmentPage.access$4600(page), AssignmentPage.access$4700(page), wapCommands);
                                        }
                                    }
                                    if (calendarItems != null) {
                                        this.generateCalendarItems(calendarItems, 10, eventData, displayTimeZone, m18, displayTimeZone);
                                    }
                                    workareas = page.getFocusedResource() != null ? page.getFocusedResource().getWorkareas() : null;
                                    if (workareas == null) return;
                                    if (AssignmentPage.access$500(page) != OperatingMode.WORK) return;
                                    if (page.getCalendarView().isTimeline() == false) return;
                                    timelinesections = metadata.has("timelinesections") != false ? metadata.getJSONArray("timelinesections") : new JSONArray();
                                    uc = 0;
                                    wapHiddenSelector = new WAPSelector(page.getConnectionId());
                                    i = 0;
                                    while (true) {
                                        if (i >= workareas.getRowCount()) {
                                            metadata.put("timelinesections", timelinesections);
                                            return;
                                        }
                                        parentdepartment = workareas.getValue(i, "parentdepartmentid", "");
                                        if (AssignmentPage.access$4800(page) == null || AssignmentPage.access$4800(page).length() == 0 || parentdepartment == null || parentdepartment.length() == 0 || parentdepartment.equals(AssignmentPage.access$4800(page))) {
                                            try {
                                                availability = page.getResourceSDC() == ResourceSDC.INSTRUMENT ? wapAvailabilitySelector.getInstrumentWorkareaAvailabilityBetween(workareas.getValue(i, "departmentid", ""), AssignmentPage.access$700(page).toInstant(), AssignmentPage.access$800(page).toInstant(), wapAvailabilityOptions, page.getFocusedResource().resourceType, page.getFocusedResource().resourceType, displayTimeZone) : wapAvailabilitySelector.getUserWorkareaAvailabilityBetween(workareas.getValue(i, "departmentid", ""), AssignmentPage.access$700(page).toInstant(), AssignmentPage.access$800(page).toInstant(), wapAvailabilityOptions, displayTimeZone);
                                            }
                                            catch (Exception var30_64) {
                                                AssignmentPage.access$4900(page).warn("Failed to find department availability.");
                                            }
                                            AssignmentPage.access$3900(page, availability, availabilities, false);
                                            sectionKey += (i + 1) * 10;
                                            if (page.getCalendarView().isTimeline()) {
                                                var30_65 = new JSONObject();
                                                var30_65.put("key", sectionKey);
                                                var30_65.put("resourceid", workareas.getValue(i, "departmentid", ""));
                                                var30_65.put("resourcesdcid", page.getResourceSDC().toString());
                                                var30_65.put("resourceisworkarea", true);
                                                var30_65.put("label", workareas.getValue(i, "departmentdesc", workareas.getValue(i, "departmentid", "")));
                                                resHtml = new StringBuilder();
                                                AssignmentPageUtil.renderWorkloadIcon(resHtml, AssignmentPage.access$4100(page), AssignmentPage.access$1600(page), workareas.getValue(i, "departmentid", "") + "|" + "Department", true, AssignmentPage.access$700(page), AssignmentPage.access$800(page), availability, page.getColorScheme(), page.getConnectionId(), AssignmentPage.access$5000(page), AssignmentPage.access$5100(page), displayTimeZone);
                                                var30_65.put("icon", resHtml.toString());
                                                timelinesections.put(var30_65);
                                                resourceAvailabilities = new JSONObject();
                                                AssignmentPage.access$3900(page, availability, resourceAvailabilities, false);
                                                if (!workloadsDisplayOp.equalsIgnoreCase("None")) {
                                                    workload = new JSONArray();
                                                    AssignmentPage.access$4000(page, resourceAvailabilities, workload, workloadsDisplayOp);
                                                    metadata.put("workload-" + sectionKey, workload);
                                                }
                                                if (appointmentsDisplayOp && (currentCalItems = (lvCalendar = calendarFactory.getDepartmentCalendar(workareas.getValue(i, "departmentid", ""), true, true)).getCalendarItemsBetween(AssignmentPage.access$700(page).toInstant(), AssignmentPage.access$800(page).toInstant(), true)) != null) {
                                                    this.generateCalendarItems(currentCalItems, sectionKey, eventData, displayTimeZone, m18, displayTimeZone);
                                                }
                                            }
                                            this.generateEvents(availability, eventData, uc, workareas.getValue(i, "departmentid", ""), page.getFocusedResource(), "Department", workareas.getValue(i, "parentdepartmentid", ""), "", sectionKey, page.getCalendarView().isTimeline(), page.getCalendarView() == CalendarView.DAY || page.getCalendarView() == CalendarView.WEEK, page.getCalendarView().isTimeline(), filters, displayoptions, displayTimeZone, m18, AssignmentPage.access$1300(page), AssignmentPage.access$1200(page), AssignmentPage.access$500(page), unassigneddActivities, preFetchedActivities, processedActivities, AssignmentPage.access$5200(page), AssignmentPage.access$5300(page), wapCommands);
                                            if (filters == null || filters.getProperty("status").length() == 0 || filters.getProperty("status").equalsIgnoreCase("All") || filters.getProperty("status").contains("Cancelled") || filters.getProperty("status").contains("Completed")) {
                                                var30_66 = new WAPSelectorOptions();
                                                var30_66.setActivityStatusList("Cancelled;Completed");
                                                if (page.getFocusedResource().getResourceSDC() == ResourceSDC.USER) {
                                                    var30_66.setAssignedUserWorkarea(workareas.getValue(i, "departmentid", ""));
                                                } else {
                                                    var30_66.setAssignedInstrumentWorkarea(workareas.getValue(i, "departmentid", ""), "", "");
                                                }
                                                hiddenActivities = wapHiddenSelector.getActivitiesBetween(AssignmentPage.access$700(page).toInstant(), AssignmentPage.access$800(page).toInstant(), var30_66);
                                                if (hiddenActivities != null && hiddenActivities.size() > 0) {
                                                    this.generateEvents(hiddenActivities, eventData, uc, workareas.getValue(i, "departmentid", ""), page.getFocusedResource().getResourceSDC(), fSdc, workareas.getValue(i, "parentdepartmentid", ""), "", sectionKey, page.getCalendarView().isTimeline(), page.getCalendarView() == CalendarView.DAY || page.getCalendarView() == CalendarView.WEEK, page.getCalendarView().isTimeline(), filters, displayoptions, displayTimeZone, m18, AssignmentPage.access$1300(page), AssignmentPage.access$1200(page), AssignmentPage.access$500(page), AssignmentPage.access$5400(page), AssignmentPage.access$5500(page), wapCommands);
                                                }
                                            }
                                            uc = uc > 5 ? 0 : ++uc;
                                        }
                                        ++i;
                                    }
                                }
                                for (i = 0; i < instruments.getRowCount(); ++i) {
                                    try {
                                        var30_68 = instrumentData.getType();
                                        instrumentmodel = instrumentData.getModel();
                                        wapAvailabilityOptions.setInstrumenttypeid(var30_68);
                                        wapAvailabilityOptions.setInstrumentmodelid(instrumentmodel);
                                        availability = wapAvailabilitySelector.getInstrumentAvailabilityBetween(instruments.getValue(i, "instrumentid", ""), AssignmentPage.access$700(page).toInstant(), AssignmentPage.access$800(page).toInstant(), wapAvailabilityOptions, displayTimeZone);
                                    }
                                    catch (Exception var30_69) {
                                        AssignmentPage.access$5600(page).warn("Failed to find instrument availability.");
                                    }
                                    sectionKey = (i + 1) * 10;
                                    if (page.getCalendarView().isTimeline()) {
                                        var30_71 = new JSONObject();
                                        var30_71.put("key", sectionKey);
                                        var30_71.put("resourceid", instruments.getValue(i, "instrumentid", ""));
                                        var30_71.put("resourcesdcid", ResourceSDC.INSTRUMENT.toString());
                                        var30_71.put("resourceisworkarea", false);
                                        var30_71.put("label", instruments.getValue(i, "instrumentdesc", instruments.getValue(i, "instrumentid", "")));
                                        resHtml = new StringBuilder();
                                        AssignmentPageUtil.renderWorkloadIcon(resHtml, AssignmentPage.access$4100(page), AssignmentPage.access$1600(page), instruments.getValue(i, "instrumentid", "") + "|" + ResourceSDC.INSTRUMENT.getName(), true, AssignmentPage.access$700(page), AssignmentPage.access$800(page), availability, page.getColorScheme(), page.getConnectionId(), AssignmentPage.access$5700(page), AssignmentPage.access$5800(page), displayTimeZone);
                                        var30_71.put("icon", resHtml.toString());
                                        timelinesections.put(var30_71);
                                        resourceAvailabilities = new JSONObject();
                                        AssignmentPage.access$3900(page, availability, resourceAvailabilities, false);
                                        if (!workloadsDisplayOp.equalsIgnoreCase("None")) {
                                            workload = new JSONArray();
                                            AssignmentPage.access$4000(page, resourceAvailabilities, workload, workloadsDisplayOp);
                                            metadata.put("workload-" + sectionKey, workload);
                                        }
                                        if (appointmentsDisplayOp && (currentCalItems = (lvCalendar = calendarFactory.getInstrumentCalendar(instruments.getValue(i, "instrumentid", ""), true, true)).getCalendarItemsBetween(AssignmentPage.access$700(page).toInstant(), AssignmentPage.access$800(page).toInstant(), true)) != null) {
                                            this.generateCalendarItems(currentCalItems, sectionKey, eventData, displayTimeZone, m18, displayTimeZone);
                                        }
                                    } else {
                                        AssignmentPage.access$3900(page, availability, availabilities, false);
                                    }
                                    this.generateEvents(availability, eventData, uc, instruments.getValue(i, "instrumentid", ""), page.getFocusedResource(), ResourceSDC.INSTRUMENT.getName(), "", "", sectionKey, page.getCalendarView().isTimeline(), page.getCalendarView() == CalendarView.DAY || page.getCalendarView() == CalendarView.WEEK, page.getCalendarView().isTimeline(), filters, displayoptions, displayTimeZone, m18, AssignmentPage.access$1300(page), AssignmentPage.access$1200(page), AssignmentPage.access$500(page), unassigneddActivities, preFetchedActivities, processedActivities, AssignmentPage.access$5900(page), AssignmentPage.access$6000(page), wapCommands);
                                    if (filters == null || filters.getProperty("status").length() == 0 || filters.getProperty("status").equalsIgnoreCase("All") || filters.getProperty("status").contains("Cancelled") || filters.getProperty("status").contains("Completed")) {
                                        var30_73 = new WAPSelectorOptions();
                                        var30_73.setActivityStatusList("Cancelled;Completed");
                                        var30_73.setAssignedInstrument(instruments.getValue(i, "instrumentid", ""));
                                        hiddenActivities = wapHiddenSelector.getActivitiesBetween(AssignmentPage.access$700(page).toInstant(), AssignmentPage.access$800(page).toInstant(), var30_73);
                                        if (hiddenActivities != null && hiddenActivities.size() > 0) {
                                            this.generateEvents(hiddenActivities, eventData, uc, instruments.getValue(i, "instrumentid", ""), page.getFocusedResource().getResourceSDC(), fSdc, "", "", sectionKey, page.getCalendarView().isTimeline(), page.getCalendarView() == CalendarView.DAY || page.getCalendarView() == CalendarView.WEEK, page.getCalendarView().isTimeline(), filters, displayoptions, displayTimeZone, m18, AssignmentPage.access$1300(page), AssignmentPage.access$1200(page), AssignmentPage.access$500(page), AssignmentPage.access$6100(page), AssignmentPage.access$6200(page), wapCommands);
                                        }
                                    }
                                    if (uc > 5) {
                                        uc = 0;
                                        continue;
                                    }
                                    ++uc;
                                }
                                metadata.put("timelinesections", timelinesections);
                                ** while (true)
                            }
                            for (var44_110 = 0; var44_110 < var43_105; ++var44_110) {
                                s = var42_104[var44_110];
                                if (!sysuserid.equalsIgnoreCase(s)) continue;
                                transientItem = true;
                                break;
                            }
                            if (page.getFocusedResource().autoresource.length() > 0 && page.getFocusedResource().autoresource.equals(sysuserid)) {
                                timelinesectionLabel = timelinesectionLabel + " (P)";
                            }
                            if (transientItem) {
                                timelinesectionLabel = timelinesectionLabel + " (T)";
                                schedWorkareas = null;
                                if (workAreaSelector == null) {
                                    workAreaSelector = new WAPSelector(page.getConnectionId());
                                }
                                try {
                                    schedWorkareas = workAreaSelector != null ? workAreaSelector.getScheduledUserWorkAreas(sysuserid, AssignmentPage.access$700(page).toInstant(), AssignmentPage.access$800(page).toInstant()) : null;
                                }
                                catch (Exception var43_106) {
                                    // empty catch block
                                }
                                if (schedWorkareas != null && page.getFocusedResource().getWorkareas() != null && page.getFocusedResource().getWorkareas().getRowCount() > 0) {
                                    hasAvailability = false;
                                    for (a = 0; a < availability.size(); ++a) {
                                    }
                                    if (!hasAvailability) {
                                        addTimelineSection = false;
                                    }
                                } else {
                                    for (WAPAvailability var44_111 : availability) {
                                    }
                                    addTimelineSection = false;
                                }
                            }
                        }
                        timelineSection.put("label", timelinesectionLabel);
                        if (addTimelineSection) {
                            timelinesections.put(timelineSection);
                        }
                        resourceAvailabilities = new JSONObject();
                        AssignmentPage.access$3900(page, availability, resourceAvailabilities, true);
                        if (!workloadsDisplayOp.equalsIgnoreCase("None")) {
                            workload = new JSONArray();
                            AssignmentPage.access$4000(page, resourceAvailabilities, workload, workloadsDisplayOp);
                            metadata.put("workload-" + sectionKey, workload);
                        }
                        if (appointmentsDisplayOp && (currentCalItems = (lvCalendar = calendarFactory.getUserCalendar(sysuserid, true, true)).getCalendarItemsBetween(AssignmentPage.access$700(page).toInstant(), AssignmentPage.access$800(page).toInstant(), true)) != null) {
                            this.generateCalendarItems(currentCalItems, sectionKey, eventData, displayTimeZone, m18, displayTimeZone);
                        }
                    }
                    this.generateEvents(availability, eventData, uc, sysuserid, page.getFocusedResource(), ResourceSDC.USER.getName(), departments, "", sectionKey, page.getCalendarView().isTimeline(), page.getCalendarView() == CalendarView.DAY || page.getCalendarView() == CalendarView.WEEK, page.getCalendarView().isTimeline(), filters, displayoptions, displayTimeZone, m18, AssignmentPage.access$1300(page), AssignmentPage.access$1200(page), AssignmentPage.access$500(page), unassigneddActivities, preFetchedActivities, processedActivities, AssignmentPage.access$6600(page), AssignmentPage.access$6700(page), wapCommands);
                    if (filters == null || filters.getProperty("status").length() == 0 || filters.getProperty("status").equalsIgnoreCase("All") || filters.getProperty("status").contains("Cancelled") || filters.getProperty("status").contains("Completed")) {
                        wapSelectorOptions = new WAPSelectorOptions();
                        wapSelectorOptions.setActivityStatusList("Cancelled;Completed");
                        wapSelectorOptions.setAssignedUser(sysuserid);
                        hiddenActivities = var30_74.getActivitiesBetween(AssignmentPage.access$700(page).toInstant(), AssignmentPage.access$800(page).toInstant(), wapSelectorOptions);
                        if (hiddenActivities != null && hiddenActivities.size() > 0) {
                            this.generateEvents(hiddenActivities, eventData, uc, sysuserid, page.getFocusedResource().getResourceSDC(), fSdc, departments, "", sectionKey, page.getCalendarView().isTimeline(), page.getCalendarView() == CalendarView.DAY || page.getCalendarView() == CalendarView.WEEK, page.getCalendarView().isTimeline(), filters, displayoptions, displayTimeZone, m18, AssignmentPage.access$1300(page), AssignmentPage.access$1200(page), AssignmentPage.access$500(page), AssignmentPage.access$6800(page), AssignmentPage.access$6900(page), wapCommands);
                        }
                    }
                    uc = uc > 5 ? 0 : ++uc;
                }
                ++i;
                ** while (true)
            }
        };

        protected boolean showFocusSelector = false;
        protected boolean showWorkSelector = false;
        protected AssignmentPageResourceContainer focus = null;

        protected abstract String getHTML(AssignmentPage var1);

        protected abstract String getAssignmentHTML(ActivityClassHandler var1, String var2, CalendarPage.View var3, Instant[] var4, String var5, String var6, JSONObject var7, AssignmentPageResourceData var8, SDIProcessor var9, TranslationProcessor var10, ZoneId var11, M18NUtil var12) throws SapphireException;

        protected abstract String getScript(AssignmentPage var1);

        protected abstract JSONArray getEventData(AssignmentPage var1, JSONObject var2, M18NUtil var3);

        protected String getAreaSelector(TranslationProcessor translationProcessor) {
            return "";
        }

        protected PropertyList getProperties(AssignmentPage page) {
            return page.getElementProperties().getPropertyList("pagedata") != null ? (page.getElementProperties().getPropertyList("pagedata").getPropertyList(this.toString().toLowerCase()) != null ? page.getElementProperties().getPropertyList("pagedata").getPropertyList(this.toString().toLowerCase()) : new PropertyList()) : new PropertyList();
        }

        static {
            ViewMode.RESOURCE.showFocusSelector = true;
            ViewMode.RESOURCE.showWorkSelector = false;
            ViewMode.RESOURCE.focus = null;
            ViewMode.CALENDAR.showFocusSelector = true;
            ViewMode.CALENDAR.showWorkSelector = true;
            ViewMode.CALENDAR.focus = null;
        }
    }

    public static enum OperatingMode {
        ASSIGNMENT,
        WORK;

    }

    public static enum CalendarView {
        WEEK(false),
        MONTH(false),
        DAY(false),
        AGENDA(false),
        RESOURCE_TIMELINE(true),
        RESOURCE_WEEK(true);

        private boolean timeline;

        private CalendarView(boolean timeline) {
            this.timeline = timeline;
        }

        public boolean isTimeline() {
            return this.timeline;
        }
    }

    public static enum ResourceMode {
        ALL,
        SUGGESTED,
        ASSIGNED;

    }

    public static enum ColorScheme {
        DEFAULT,
        NONE;

    }
}

