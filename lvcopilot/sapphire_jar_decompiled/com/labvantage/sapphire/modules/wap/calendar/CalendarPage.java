/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.modules.wap.calendar;

import com.labvantage.opal.elements.advancedtoolbar.AdvancedToolbar;
import com.labvantage.sapphire.I18nUtil;
import com.labvantage.sapphire.modules.wap.calendar.CalendarFactory;
import com.labvantage.sapphire.modules.wap.calendar.LVCalendar;
import com.labvantage.sapphire.services.SapphireConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.TimeZone;
import javax.servlet.jsp.PageContext;
import sapphire.SapphireException;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.pageelements.BaseElement;
import sapphire.servlet.RequestContext;
import sapphire.tagext.PageTagInfo;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.util.M18NUtil;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class CalendarPage
extends BaseElement {
    private String devMode = null;
    private String sdcid = "";
    private boolean workarea = false;
    private boolean testinglab = false;
    private String keyid1 = "";
    private String shiftid = "";
    private LVCalendar lvCalendar = null;
    private String title = "Calendar";
    private String description = "Calendar";
    private Mode mode = Mode.USER;
    private View view = View.WEEK;
    private boolean viewOnly = false;
    protected boolean inLookup = false;
    private int firstDayOfWeek = 2;
    private String userConfigPrefix = "calendar_";
    private PropertyList userConfig;
    private ZoneId displayTimeZone = null;

    public CalendarPage(PageContext pageContext) {
        this.setPageContext(pageContext);
        this.userConfig = RequestContext.getRequestContext(pageContext).getPropertyList("userconfig");
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

    public void setViewOnly(boolean viewOnly) {
        this.viewOnly = viewOnly;
    }

    public String getScript() {
        PropertyList pagedata = this.requestContext.getPropertyList().getPropertyListNotNull("pagedata");
        StringBuilder script = new StringBuilder();
        script.append("calendar.mode='").append(this.mode.toString()).append("';");
        script.append("calendar.calView='").append(this.view.toString().toLowerCase()).append("';");
        script.append("calendar.viewonly=").append(this.viewOnly).append(";");
        script.append("calendar.sdcid='").append(this.sdcid).append("';");
        script.append("calendar.keyid1='").append(this.keyid1).append("';");
        script.append("calendar.shiftid='").append(this.shiftid).append("';");
        script.append("calendar.title='").append(StringUtil.replaceAll(this.title, "'", "\\'")).append("';");
        script.append("calendar.description='").append(StringUtil.replaceAll(this.description, "'", "\\'")).append("';");
        script.append("calendar.inLookup=").append(this.inLookup).append(";");
        script.append("calendar.firstDayOfWeek=").append(this.firstDayOfWeek).append(";");
        script.append("calendar.userConfigPrefix='").append(this.userConfigPrefix).append("';");
        script.append("calendar.timezone='").append(TimeZone.getTimeZone(this.displayTimeZone).getID()).append("';");
        script.append("calendar.ownertimezone='").append(this.lvCalendar != null ? this.lvCalendar.getTimeZone().getId() : TimeZone.getDefault().toZoneId().getId()).append("';");
        script.append("calendar.usertimezone='" + (this.connectionInfo.getTimeZone() != null ? TimeZone.getTimeZone(this.connectionInfo.getTimeZone()).toZoneId().getId() : TimeZone.getDefault().toZoneId().getId()) + "';");
        script.append("calendar.time_step=" + pagedata.getProperty("timestep", "5") + ";");
        if (this.lvCalendar != null) {
            try {
                Calendar startrange = Calendar.getInstance();
                startrange.set(11, 0);
                startrange.set(12, 0);
                startrange.set(13, 0);
                startrange.set(14, 0);
                Calendar endrange = Calendar.getInstance();
                endrange.set(11, 23);
                endrange.set(12, 59);
                endrange.set(13, 59);
                endrange.set(14, 99);
                switch (this.view) {
                    case DAY: {
                        break;
                    }
                    case WEEK: {
                        startrange.add(6, -7);
                        endrange.add(6, 7);
                        break;
                    }
                    case MONTH: {
                        startrange.add(2, -1);
                        endrange.add(2, 1);
                        break;
                    }
                    case YEAR: {
                        startrange.set(6, startrange.getActualMinimum(6));
                        endrange.set(6, endrange.getActualMaximum(6));
                    }
                }
                try {
                    M18NUtil m18;
                    DateFormat currentFormat;
                    String p;
                    if (this.connectionInfo == null) {
                        this.connectionInfo = this.getConnectionProcessor().getConnectionInfo(this.getConnectionId());
                    }
                    if ((p = ((SimpleDateFormat)(currentFormat = (m18 = new M18NUtil(this.connectionInfo)).getDefaultDateFormat())).toPattern()).contains("a") && p.contains("h")) {
                        script.append("calendar.formats.hour_date='").append("%h:%i %A").append("';");
                    } else {
                        script.append("calendar.formats.hour_date='").append("%H:%i").append("';");
                    }
                    String js = this.lvCalendar.toJSONObject(null, null, this.displayTimeZone).toString();
                    script.append("calendar.calendar = sapphire.util.calendar.create(").append(js).append(");");
                }
                catch (Exception e) {
                    script.append("calendar.calendar = sapphire.util.calendar.create();");
                    script.append("sapphire.alert('").append("Unable to render calendar.<br>Error: ").append(e.getMessage()).append("');");
                    this.logger.error("Unable to get JSON calendar.", e);
                }
                script.append("calendar.initDynamic();");
            }
            catch (Exception e) {
                this.logger.error("Failed to obtain calendar items.");
            }
        }
        return script.toString();
    }

    public String getSDCId() {
        return this.sdcid;
    }

    public void setSDCId(String sdcid) {
        this.sdcid = sdcid;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public void setView(View view) {
        this.view = view;
    }

    private String getToolbar() {
        PropertyList userbuttonprops;
        PropertyList commonprops;
        PropertyList button;
        AdvancedToolbar advancedToolbar = new AdvancedToolbar();
        advancedToolbar.setPageContext(this.pageContext);
        PropertyList properties = new PropertyList();
        properties.setProperty("displaystyle", "");
        properties.setProperty("showtitle", "N");
        PropertyListCollection buttons = new PropertyListCollection();
        if (this.inLookup) {
            button = new PropertyList();
            commonprops = new PropertyList();
            userbuttonprops = new PropertyList();
            button.setProperty("id", "closeButton");
            button.setProperty("type", "User");
            commonprops.setProperty("text", this.getTranslationProcessor().translate("Close"));
            commonprops.setProperty("image", "rc?command=image&image=FlatBlackClose");
            commonprops.setProperty("imagelarge", "rc?command=image&image=FlatBlackClose");
            commonprops.setProperty("show", "Y");
            commonprops.setProperty("ribbonstyle", "Large");
            commonprops.setProperty("appearance", "standard");
            commonprops.setProperty("tip", this.getTranslationProcessor().translate("Close dialog"));
            commonprops.setProperty("mode", "Button");
            userbuttonprops.setProperty("action", "calendar.close()");
            button.setProperty("commonprops", commonprops);
            button.setProperty("userbuttonprops", userbuttonprops);
            buttons.add(button);
        } else {
            button = new PropertyList();
            commonprops = new PropertyList();
            userbuttonprops = new PropertyList();
            button.setProperty("id", "returnButton");
            button.setProperty("type", "User");
            commonprops.setProperty("text", this.getTranslationProcessor().translate("Return"));
            commonprops.setProperty("image", "rc?command=image&image=FlatBlackArrowLeft");
            commonprops.setProperty("imagelarge", "rc?command=image&image=FlatBlackArrowLeft");
            commonprops.setProperty("show", "Y");
            commonprops.setProperty("ribbonstyle", "Large");
            commonprops.setProperty("appearance", "standard");
            commonprops.setProperty("tip", this.getTranslationProcessor().translate("Return to last page"));
            commonprops.setProperty("mode", "Button");
            userbuttonprops.setProperty("action", "top.modernLayout.navigation.goBack()");
            button.setProperty("commonprops", commonprops);
            button.setProperty("userbuttonprops", userbuttonprops);
            buttons.add(button);
        }
        if (!this.viewOnly) {
            button = new PropertyList();
            commonprops = new PropertyList();
            userbuttonprops = new PropertyList();
            button.setProperty("id", "undoButton");
            button.setProperty("type", "User");
            commonprops.setProperty("text", this.getTranslationProcessor().translate("Undo"));
            commonprops.setProperty("image", "rc?command=image&image=FlatBlackUndo2");
            commonprops.setProperty("imagelarge", "rc?command=image&image=FlatBlackUndo2");
            commonprops.setProperty("show", "Y");
            commonprops.setProperty("ribbonstyle", "Large");
            commonprops.setProperty("appearance", "standard");
            commonprops.setProperty("tip", "Undo last action");
            commonprops.setProperty("mode", "Button");
            userbuttonprops.setProperty("action", "calendar.undo()");
            button.setProperty("commonprops", commonprops);
            button.setProperty("userbuttonprops", userbuttonprops);
            buttons.add(button);
            if (this.mode == Mode.ADMIN && this.lvCalendar != null && this.lvCalendar.isShared()) {
                button = new PropertyList();
                commonprops = new PropertyList();
                userbuttonprops = new PropertyList();
                button.setProperty("id", "renameButton");
                button.setProperty("type", "User");
                commonprops.setProperty("text", this.getTranslationProcessor().translate("Change Description"));
                commonprops.setProperty("image", "rc?command=image&image=FlatBlackEditBox");
                commonprops.setProperty("imagelarge", "rc?command=image&image=FlatBlackEditBox");
                commonprops.setProperty("show", "Y");
                commonprops.setProperty("ribbonstyle", "Large");
                commonprops.setProperty("appearance", "standard");
                commonprops.setProperty("tip", "Rename the description of the calendar");
                commonprops.setProperty("mode", "Button");
                userbuttonprops.setProperty("action", "calendar.rename()");
                button.setProperty("commonprops", commonprops);
                button.setProperty("userbuttonprops", userbuttonprops);
                buttons.add(button);
            }
            if (!(this.mode != Mode.ADMIN || this.lvCalendar == null || this.lvCalendar.isShared() || this.workarea && !this.testinglab || this.sdcid.equalsIgnoreCase("SchedulePlan"))) {
                button = new PropertyList();
                commonprops = new PropertyList();
                userbuttonprops = new PropertyList();
                button.setProperty("id", "corehoursbutton");
                button.setProperty("type", "User");
                commonprops.setProperty("text", this.getTranslationProcessor().translate("Manage Core Hours"));
                commonprops.setProperty("image", "rc?command=image&image=FlatBlackClock");
                commonprops.setProperty("imagelarge", "rc?command=image&image=FlatBlackClock");
                commonprops.setProperty("show", "Y");
                commonprops.setProperty("ribbonstyle", "Large");
                commonprops.setProperty("appearance", "standard");
                commonprops.setProperty("tip", "Change the core hours for this calendar");
                commonprops.setProperty("mode", "Button");
                userbuttonprops.setProperty("action", "calendar.coreHours()");
                button.setProperty("commonprops", commonprops);
                button.setProperty("userbuttonprops", userbuttonprops);
                buttons.add(button);
            }
        } else if (this.lvCalendar != null && this.lvCalendar.isExternal()) {
            button = new PropertyList();
            commonprops = new PropertyList();
            userbuttonprops = new PropertyList();
            button.setProperty("id", "clearCache");
            button.setProperty("type", "User");
            commonprops.setProperty("text", this.getTranslationProcessor().translate("Refresh"));
            commonprops.setProperty("image", "rc?command=image&image=FlatBlackRefresh");
            commonprops.setProperty("imagelarge", "rc?command=image&image=FlatBlackRefresh");
            commonprops.setProperty("show", "Y");
            commonprops.setProperty("ribbonstyle", "Large");
            commonprops.setProperty("appearance", "standard");
            commonprops.setProperty("tip", this.getTranslationProcessor().translate("Refresh External Calendar"));
            commonprops.setProperty("mode", "Button");
            userbuttonprops.setProperty("action", "calendar.refresh(true,true)");
            button.setProperty("commonprops", commonprops);
            button.setProperty("userbuttonprops", userbuttonprops);
            buttons.add(button);
            button = new PropertyList();
            commonprops = new PropertyList();
            userbuttonprops = new PropertyList();
            button.setProperty("id", "import");
            button.setProperty("type", "User");
            commonprops.setProperty("text", this.getTranslationProcessor().translate("Import"));
            commonprops.setProperty("image", "rc?command=image&image=FlatBlackDownload2");
            commonprops.setProperty("imagelarge", "rc?command=image&image=FlatBlackDownload2");
            commonprops.setProperty("show", "Y");
            commonprops.setProperty("ribbonstyle", "Large");
            commonprops.setProperty("appearance", "standard");
            commonprops.setProperty("tip", this.getTranslationProcessor().translate("Import External Calendar"));
            commonprops.setProperty("mode", "Button");
            userbuttonprops.setProperty("action", "calendar.import()");
            button.setProperty("commonprops", commonprops);
            button.setProperty("userbuttonprops", userbuttonprops);
            buttons.add(button);
        }
        properties.setProperty("buttons", buttons);
        properties.setProperty("rendermode", "Button");
        advancedToolbar.setElementProperties(properties);
        return advancedToolbar.getHtml();
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
        html.append("<div id=\"header\">");
        html.append(this.getToolbar());
        html.append("<div id=\"calendar_timezoneDIV\">");
        TimeZone usertimeZone = this.connectionInfo.getTimeZone() != null ? TimeZone.getTimeZone(this.connectionInfo.getTimeZone()) : TimeZone.getDefault();
        TimeZone displayTimeZone = TimeZone.getTimeZone(this.displayTimeZone);
        TimeZone ownerTimeZone = this.lvCalendar != null && this.lvCalendar.getTimeZone() != null ? TimeZone.getTimeZone(this.lvCalendar.getTimeZone()) : TimeZone.getDefault();
        html.append(this.getTranslationProcessor().translate("Timezone")).append(": ");
        html.append("<select name=\"calendar_timezone\" id=\"calendar_timezone\" value=\"").append(displayTimeZone.getDisplayName()).append("\" onChange=\"calendar.toggleTimewarp(this)\">");
        if (usertimeZone.toZoneId().getRules().equals(ownerTimeZone.toZoneId().getRules())) {
            html.append("<option ").append(usertimeZone.toZoneId().getRules().equals(displayTimeZone.toZoneId().getRules()) ? "selected " : "").append("value=\"").append(usertimeZone.getID()).append("\">").append(usertimeZone.toZoneId().getId()).append(this.getTranslationProcessor().translate(" (You & Owner)")).append("</option>");
        } else {
            html.append("<option ").append(usertimeZone.toZoneId().getRules().equals(displayTimeZone.toZoneId().getRules()) ? "selected " : "").append("value=\"").append(usertimeZone.getID()).append("\">").append(usertimeZone.toZoneId().getId()).append(this.getTranslationProcessor().translate(" (You)")).append("</option>");
            html.append("<option ").append(ownerTimeZone.toZoneId().getRules().equals(displayTimeZone.toZoneId().getRules()) ? "selected " : "").append("value=\"").append(ownerTimeZone.getID()).append("\">").append(ownerTimeZone.toZoneId().getId()).append(this.getTranslationProcessor().translate(" (Owner)")).append("</option>");
        }
        html.append("</select>");
        html.append("</div>");
        html.append("<div id=\"").append("calendar").append("\" style=\"top:45px;\">");
        if (this.workarea && !this.testinglab) {
            html.append("<div class=\"warn\">").append(this.getTranslationProcessor().translate("You cannot update the calendar for a workarea")).append("</div>");
        } else if (this.lvCalendar != null) {
            html.append(this.getCalendarHTML());
        } else {
            html.append("<div class=\"error\">").append(this.getTranslationProcessor().translate("Unable to load calendar")).append("</div>");
        }
        html.append("<div>");
        return html.toString();
    }

    private String getCalendarHTML() {
        StringBuilder html = new StringBuilder();
        html.append("<title>").append(this.title).append("</title>");
        html.append("<div id=\"scheduler_here\" class=\"dhx_cal_container\" style='width:100%; height:100%;'>");
        html.append("<div class=\"dhx_cal_navline\">");
        html.append("<div class=\"dhx_cal_prev_button\">&nbsp;</div>");
        html.append("<div class=\"dhx_cal_next_button\">&nbsp;</div>");
        html.append("<div class=\"dhx_cal_today_button\"></div>");
        html.append("<div class=\"dhx_cal_date\"></div>");
        html.append("<div class=\"dhx_cal_tab\" name=\"day_tab\" style=\"right:204px;\"></div>");
        html.append("<div class=\"dhx_cal_tab\" name=\"week_tab\" style=\"right:140px;\"></div>");
        html.append("<div class=\"dhx_cal_tab\" name=\"month_tab\" style=\"right:76px;\"></div>");
        html.append("<div class=\"dhx_cal_tab\" name=\"year_tab\" style=\"right:280px;\"></div>");
        html.append("</div>");
        html.append("<div class=\"dhx_cal_header\"></div>");
        html.append("<div class=\"dhx_cal_data\"></div>");
        html.append("</div>");
        return html.toString();
    }

    public void setShiftId(String shiftId) {
        this.shiftid = shiftId;
    }

    public void setKeyId1(String keyid1) {
        this.keyid1 = keyid1;
    }

    public void setDepartmentType(String departmentid) {
        DataSet dep = this.getQueryProcessor().getSqlDataSet("SELECT workassignmentflag, testingflag, parentdepartmentid FROM department WHERE departmentid='" + departmentid + "'");
        if (dep != null && dep.getRowCount() > 0) {
            this.workarea = dep.getValue(0, "workassignmentflag", "N").equalsIgnoreCase("Y");
            this.testinglab = dep.getValue(0, "testingflag", "N").equalsIgnoreCase("Y");
        } else {
            this.workarea = false;
            this.testinglab = false;
        }
    }

    public static boolean isWorkarea(String departmentid, QueryProcessor qp) {
        DataSet dep = qp.getSqlDataSet("SELECT workassignmentflag, parentdepartmentid FROM department WHERE departmentid='" + departmentid + "' AND workassignmentflag='Y'");
        return dep != null && dep.getRowCount() > 0;
    }

    public static boolean isTestingLab(String departmentid, QueryProcessor qp) {
        DataSet dep = qp.getSqlDataSet("SELECT testingflag, parentdepartmentid FROM department WHERE departmentid='" + departmentid + "' AND testingflag='Y'");
        return dep != null && dep.getRowCount() > 0;
    }

    public boolean isWorkarea(String departmentid) {
        return CalendarPage.isWorkarea(departmentid, this.getQueryProcessor());
    }

    public boolean isTestingLab(String departmentid) {
        return CalendarPage.isTestingLab(departmentid, this.getQueryProcessor());
    }

    public void setUpData() {
        StringBuilder title = new StringBuilder();
        StringBuilder desc = new StringBuilder();
        if (this.sdcid.equalsIgnoreCase("department")) {
            this.setDepartmentType(this.keyid1);
        }
        this.lvCalendar = this.workarea && !this.testinglab ? null : CalendarPage.getCalendar(this.mode, this.sdcid, this.keyid1, this.shiftid, this.workarea, this.testinglab, title, desc, this.getConnectionProcessor().getSapphireConnection(), this.getQueryProcessor(), this.getTranslationProcessor(), this.logger);
        this.title = title.toString();
        this.description = desc.toString();
        if (this.lvCalendar != null && !this.lvCalendar.isInternal()) {
            this.viewOnly = true;
        }
    }

    public static LVCalendar getCalendar(Mode mode, String sdcid, String keyid1, String shiftid, boolean isWorkarea, boolean isTestingLab, StringBuilder titleOut, StringBuilder descriptionOut, SapphireConnection sapphireConnection, QueryProcessor qp, TranslationProcessor tp, Logger logger) {
        LVCalendar lvCalendar;
        block36: {
            CalendarFactory calendarFactory = new CalendarFactory(sapphireConnection.getConnectionId());
            lvCalendar = null;
            if (mode == Mode.ADMIN) {
                if (sdcid.length() > 0 && keyid1.length() > 0) {
                    try {
                        if (sdcid.equalsIgnoreCase("User")) {
                            lvCalendar = calendarFactory.getUserCalendar(keyid1, true, true);
                            if (titleOut != null) {
                                titleOut.append(tp.translate("Calendar For User")).append(" ").append(keyid1);
                            }
                            break block36;
                        }
                        if (sdcid.equalsIgnoreCase("Instrument")) {
                            lvCalendar = calendarFactory.getInstrumentCalendar(keyid1, true, true);
                            if (titleOut != null) {
                                titleOut.append(tp.translate("Calendar For Instrument")).append(" ").append(keyid1);
                            }
                            break block36;
                        }
                        if (sdcid.equalsIgnoreCase("SchedulePlan")) {
                            lvCalendar = calendarFactory.getSchedulePlanCalendar(keyid1, true, true);
                            if (titleOut != null) {
                                titleOut.append(tp.translate("Calendar For Schedule Plan")).append(" ").append(keyid1);
                            }
                            break block36;
                        }
                        if (sdcid.equalsIgnoreCase("Department")) {
                            if (shiftid.length() > 0 && !shiftid.equalsIgnoreCase("(null)")) {
                                lvCalendar = calendarFactory.getShiftCalendar(keyid1, shiftid, true, true);
                                if (titleOut != null) {
                                    titleOut.append(tp.translate("Calendar For Department")).append(" ").append(keyid1).append(" ").append(tp.translate("Shift")).append(" ").append(shiftid);
                                }
                            } else if (isWorkarea && !isTestingLab) {
                                lvCalendar = calendarFactory.getDepartmentCalendar(keyid1, true, true);
                                if (titleOut != null) {
                                    titleOut.append(tp.translate("Calendar For Workarea")).append(" ").append(keyid1);
                                }
                            } else if (isTestingLab) {
                                lvCalendar = calendarFactory.getDepartmentCalendar(keyid1, true, true);
                                if (titleOut != null) {
                                    titleOut.append(tp.translate("Calendar For Testing Lab")).append(" ").append(keyid1);
                                }
                            } else {
                                lvCalendar = calendarFactory.getDepartmentCalendar(keyid1, true, true);
                                if (titleOut != null) {
                                    titleOut.append(tp.translate("Calendar For Department")).append(" ").append(keyid1);
                                }
                            }
                            break block36;
                        }
                        if (sdcid.equalsIgnoreCase("LV_Calendar")) {
                            lvCalendar = calendarFactory.getCalendar(keyid1);
                            if (lvCalendar != null && !lvCalendar.isShared()) {
                                if (titleOut != null) {
                                    titleOut.append(tp.translate("Private Calendar")).append(" ").append(keyid1);
                                }
                            } else {
                                DataSet ds = qp.getPreparedSqlDataSet("SELECT calendardesc, sourceflag FROM calendar WHERE calendarid=?", new Object[]{keyid1});
                                if (ds != null && ds.getRowCount() > 0) {
                                    String desc = ds.getValue(0, "calendardesc", tp.translate("No Description"));
                                    if (descriptionOut != null) {
                                        descriptionOut.append(desc);
                                    }
                                    if (titleOut != null) {
                                        boolean isExternal = ds.getValue(0, "sourceflag", "I").equalsIgnoreCase("E");
                                        titleOut.append(isExternal ? tp.translate("External") : tp.translate("Shared")).append(" ").append(tp.translate("Calendar")).append(" ").append("(").append(desc).append(")");
                                    }
                                } else if (titleOut != null) {
                                    titleOut.append(tp.translate("Shared Calendar")).append(" ").append(keyid1);
                                }
                            }
                            break block36;
                        }
                        logger.error("Invalid SDC Id provided for Admin mode.");
                    }
                    catch (SapphireException e) {
                        logger.error("Failed to obtain calendar.");
                    }
                } else {
                    logger.error("No SDC Id and/or Keyid1 provided for Admin mode.");
                }
            } else if (mode == Mode.USER) {
                try {
                    String s = sapphireConnection.getSysuserId();
                    lvCalendar = calendarFactory.getUserCalendar(s, true, true);
                    if (titleOut != null) {
                        titleOut.append(tp.translate("My Calendar")).append(" ");
                    }
                }
                catch (SapphireException e) {
                    logger.error("Failed to obtain user calendar.");
                }
            }
        }
        return lvCalendar;
    }

    public void loadProperties(PageTagInfo pageTagInfo) {
        this.viewOnly = pageTagInfo.getProperty("viewonly", pageTagInfo.getPropertyList("pagedata").getProperty("viewonly", "N")).equalsIgnoreCase("Y");
        this.inLookup = pageTagInfo.getProperty("dialog", "N").equalsIgnoreCase("Y");
        this.sdcid = pageTagInfo.getProperty("sdcid", pageTagInfo.getPropertyList("pagedata").getProperty("sdcid"));
        this.keyid1 = pageTagInfo.getProperty("keyid1", pageTagInfo.getPropertyList("pagedata").getProperty("keyid1"));
        this.shiftid = pageTagInfo.getProperty("shiftid", pageTagInfo.getPropertyList("pagedata").getProperty("shiftid", pageTagInfo.getProperty("shift", pageTagInfo.getPropertyList("pagedata").getProperty("shift"))));
        this.mode = this.sdcid.length() > 0 && this.keyid1.length() > 0 ? Mode.ADMIN : Mode.USER;
        String m = pageTagInfo.getProperty("mode", pageTagInfo.getPropertyList("pagedata").getProperty("mode"));
        if (m.length() > 0) {
            try {
                this.mode = Mode.valueOf(m.toUpperCase());
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        String v = pageTagInfo.getProperty("view", pageTagInfo.getPropertyList("pagedata").getProperty("view"));
        if (this.userConfig != null && v.length() == 0) {
            v = this.userConfig.getProperty(this.userConfigPrefix + "calview");
        }
        if (v.length() > 0) {
            try {
                this.view = View.valueOf(v.toUpperCase());
            }
            catch (Exception exception) {
                // empty catch block
            }
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
        this.setUpData();
    }

    public static enum View {
        YEAR{

            @Override
            public Calendar getStartRange(Calendar cal) {
                cal.set(6, -7);
                return cal;
            }

            @Override
            public Calendar getEndRange(Calendar cal) {
                cal.set(6, 375);
                return cal;
            }
        }
        ,
        MONTH{

            @Override
            public Calendar getStartRange(Calendar cal) {
                cal.set(5, -7);
                return cal;
            }

            @Override
            public Calendar getEndRange(Calendar cal) {
                cal.set(5, 40);
                return cal;
            }
        }
        ,
        WEEK{

            @Override
            public Calendar getStartRange(Calendar cal) {
                cal.set(7, -7);
                return cal;
            }

            @Override
            public Calendar getEndRange(Calendar cal) {
                cal.set(7, 14);
                return cal;
            }
        }
        ,
        DAY{

            @Override
            public Calendar getStartRange(Calendar cal) {
                cal.add(6, -1);
                return cal;
            }

            @Override
            public Calendar getEndRange(Calendar cal) {
                cal.add(6, 1);
                return cal;
            }
        };


        public abstract Calendar getStartRange(Calendar var1);

        public abstract Calendar getEndRange(Calendar var1);
    }

    public static enum Mode {
        ADMIN,
        USER;

    }
}

