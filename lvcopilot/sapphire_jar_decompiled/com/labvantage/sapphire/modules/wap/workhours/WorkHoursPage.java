/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.modules.wap.workhours;

import com.labvantage.opal.elements.advancedtoolbar.AdvancedToolbar;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.admin.system.ConfigurationProcessor;
import com.labvantage.sapphire.modules.wap.calendar.CalendarFactory;
import com.labvantage.sapphire.modules.wap.calendar.LVCalendar;
import com.labvantage.sapphire.modules.wap.workhours.WorkHours;
import java.text.DateFormatSymbols;
import javax.servlet.jsp.PageContext;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.pageelements.BaseElement;
import sapphire.tagext.PageTagInfo;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class WorkHoursPage
extends BaseElement {
    private String devMode = null;
    private String sdcid = "";
    private String keyid1 = "";
    private String shiftid = "";
    private LVCalendar lvCalendar = null;
    private String title = "Work Hours";
    private Mode mode = Mode.USER;
    private boolean viewOnly = false;
    private boolean inLookup = false;
    private boolean workarea = false;
    private boolean testinglab = false;
    private WorkHours workHours = null;

    public WorkHoursPage(PageContext pageContext) {
        this.setPageContext(pageContext);
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

    public boolean isTestingLab(String departmentid) {
        return WorkHoursPage.isTestingLab(departmentid, this.getQueryProcessor());
    }

    public boolean isWorkarea(String departmentid) {
        return WorkHoursPage.isWorkarea(departmentid, this.getQueryProcessor());
    }

    public boolean isDevMode() {
        if (this.devMode == null) {
            ConfigurationProcessor config = new ConfigurationProcessor(this.pageContext);
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
        StringBuilder script = new StringBuilder();
        script.append("workhours.mode='").append(this.mode.toString()).append("';");
        script.append("workhours.viewonly=").append(this.viewOnly).append(";");
        script.append("workhours.sdcid='").append(this.sdcid).append("';");
        script.append("workhours.keyid1='").append(this.keyid1).append("';");
        script.append("workhours.shiftid='").append(this.shiftid).append("';");
        script.append("workhours.title='").append(StringUtil.replaceAll(this.title, "'", "\\'")).append("';");
        script.append("workhours.inLookup=").append(this.inLookup).append(";");
        if (this.lvCalendar != null) {
            script.append("workhours.calendarid='").append(this.lvCalendar.getId()).append("';");
            try {
                WorkHours workHours = this.getWorkHours();
                script.append("workhours.data=").append(workHours != null ? workHours.toJSONString() : "{}").append(";");
                script.append("workhours.initDynamic();");
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

    private String getToolbar() {
        AdvancedToolbar advancedToolbar = new AdvancedToolbar();
        advancedToolbar.setPageContext(this.pageContext);
        PropertyList properties = new PropertyList();
        properties.setProperty("displaystyle", "");
        properties.setProperty("showtitle", "N");
        PropertyListCollection buttons = new PropertyListCollection();
        if (!this.inLookup) {
            PropertyList button = new PropertyList();
            PropertyList commonprops = new PropertyList();
            PropertyList userbuttonprops = new PropertyList();
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
            // empty if block
        }
        properties.setProperty("buttons", buttons);
        properties.setProperty("rendermode", "Button");
        advancedToolbar.setElementProperties(properties);
        return advancedToolbar.getHtml();
    }

    @Override
    public String getHtml() {
        StringBuilder html = new StringBuilder();
        if (!this.inLookup) {
            html.append("<div id=\"header\">");
            html.append(this.getToolbar());
            html.append("</div>");
        }
        html.append("<div id=\"").append("workhours").append("\" style=\"top:").append(this.inLookup ? "0" : "45px").append(";\">");
        if (this.workarea && !this.testinglab) {
            html.append("<div class=\"warn\">").append(this.getTranslationProcessor().translate("You cannot update the core hours for a workarea")).append("</div>");
        } else if (this.lvCalendar != null) {
            html.append(this.getWorkHoursHTML());
        } else {
            html.append("<div class=\"error\">").append(this.getTranslationProcessor().translate("Unable to load core hours")).append("</div>");
        }
        html.append("<div>");
        return html.toString();
    }

    private String padNum(int num) {
        String out = "" + num;
        if (out.length() == 1) {
            out = "0" + out;
        }
        return out;
    }

    private StringBuilder getHourOptions(int selectedhour) {
        StringBuilder options = new StringBuilder();
        for (int i = 0; i < 24; ++i) {
            options.append("<option value=\"").append(i).append("\"").append(i == selectedhour ? " selected" : "").append(">").append(this.padNum(i)).append("</option>");
        }
        return options;
    }

    private StringBuilder getMinuteOptions(int selectedmin) {
        StringBuilder options = new StringBuilder();
        for (int i = 0; i < 60; i += 5) {
            options.append("<option value=\"").append(i).append("\"").append(i == selectedmin ? " selected" : "").append(">").append(this.padNum(i)).append("</option>");
        }
        return options;
    }

    public void setCalendar(LVCalendar calendar) {
        this.lvCalendar = calendar;
    }

    public void setOverride(WorkHours.OverrideType overrideType) {
        if (this.getWorkHours() != null) {
            this.getWorkHours().setOverride(overrideType);
        }
    }

    public WorkHours getWorkHours() {
        if (this.workHours == null) {
            WorkHours workHours = this.workHours = this.lvCalendar != null ? this.lvCalendar.getCoreHours(false) : null;
            if (this.workHours.getOverride() != WorkHours.OverrideType.OVERRIDE) {
                try {
                    WorkHours clone = WorkHours.getInstance(this.lvCalendar.getCoreHours(true).toJSONString());
                    clone.setOverride(this.workHours.getOverride());
                    this.workHours = clone;
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
        }
        return this.workHours;
    }

    protected String getWorkHoursHTML() {
        StringBuilder html = new StringBuilder();
        html.append("<title>").append(this.title).append("</title>");
        if (this.lvCalendar != null) {
            WorkHours workHours = this.getWorkHours();
            boolean hasDefinition = false;
            if (workHours == null) {
                this.logger.debug("Workhours not set....");
                workHours = new WorkHours(0, 0, 0, 0, false, true, true, true, true, true, false);
            } else {
                if (workHours.getOverride() == WorkHours.OverrideType.OVERRIDE) {
                    hasDefinition = true;
                } else if (workHours.getOverride() == WorkHours.OverrideType.INHERIT || workHours.getOverride() == WorkHours.OverrideType.DEFAULT) {
                    hasDefinition = false;
                    workHours = this.lvCalendar.isChild() && this.lvCalendar.getParent().getCoreHours() != null ? this.lvCalendar.getParent().getCoreHours() : workHours;
                }
                this.logger.debug("Workhours set....");
            }
            if (workHours.getStartHour() == -1 && workHours.getEndHour() == -1) {
                workHours.setMonday(true);
                workHours.setTuesday(true);
                workHours.setWednesday(true);
                workHours.setThursday(true);
                workHours.setFriday(true);
                workHours.setSaturday(true);
                workHours.setSunday(true);
            }
            if (workHours.getStartHour() == -1) {
                workHours.setStartHour(0);
            }
            if (workHours.getStartMinute() == -1) {
                workHours.setStartMinute(0);
            }
            if (workHours.getEndHour() == -1) {
                workHours.setEndHour(0);
            }
            if (workHours.getEndMinute() == -1) {
                workHours.setEndMinute(0);
            }
            html.append("<div id=\"workhoursheader\">");
            html.append("<input type=\"checkbox\" id=\"corehours\" name=\"corehours\"").append(hasDefinition ? " checked" : "").append(" onchange=\"workhours.doChange(this)\">");
            html.append("<label for=\"corehours\">").append(this.lvCalendar.isChild() ? this.getTranslationProcessor().translate("Override Core Hours") : this.getTranslationProcessor().translate("Set Core Hours")).append("</label>");
            html.append("</div>");
            html.append("<div class=\"workhourscover\" style=\"display:").append(hasDefinition ? "none" : "block").append(";\">");
            html.append("</div>");
            if (workHours != null) {
                html.append("<div class=\"row\">");
                html.append("<div class=\"label\">");
                html.append(this.getTranslationProcessor().translate("Start time")).append(":");
                html.append("</div>");
                html.append("<div class=\"field\">");
                html.append("<select id=\"starthour\" name=\"starthour\" value=\"").append(workHours.getStartHour()).append("\" onchange=\"workhours.doChange()\">");
                html.append((CharSequence)this.getHourOptions(workHours.getStartHour()));
                html.append("</select>");
                html.append("&nbsp;");
                html.append("<select id=\"startminute\" name=\"startminute\" value=\"").append(workHours.getStartMinute()).append("\" onchange=\"workhours.doChange()\">");
                html.append((CharSequence)this.getMinuteOptions(workHours.getStartMinute()));
                html.append("</select>");
                html.append("</div>");
                html.append("</div>");
                html.append("<div class=\"row\">");
                html.append("<div class=\"label\">");
                html.append(this.getTranslationProcessor().translate("End time")).append(":");
                html.append("</div>");
                html.append("<div class=\"field\">");
                html.append("<select id=\"endhour\" name=\"endhour\" value=\"").append(workHours.getEndHour()).append("\" onchange=\"workhours.doChange()\">");
                html.append((CharSequence)this.getHourOptions(workHours.getEndHour()));
                html.append("</select>");
                html.append("&nbsp;");
                html.append("<select id=\"endminute\" name=\"endminute\" value=\"").append(workHours.getEndMinute()).append("\" onchange=\"workhours.doChange()\">");
                html.append((CharSequence)this.getMinuteOptions(workHours.getEndMinute()));
                html.append("</select>");
                html.append("</div>");
                html.append("</div>");
                html.append("<div class=\"row\">");
                html.append("<div class=\"label\">");
                html.append(this.getTranslationProcessor().translate("Work week")).append(":");
                html.append("</div>");
                html.append("<div class=\"field\">");
                DateFormatSymbols symbols = new DateFormatSymbols(new DateTimeUtil(this.getConnectionProcessor().getConnectionInfo(this.getConnectionId())).getLocale());
                String[] dayNames = symbols.getShortWeekdays();
                html.append("<input type=\"checkbox\" id=\"sunday\" name=\"sunday\"").append(workHours.isSunday() ? " checked" : "").append(" onchange=\"workhours.doChange()\">");
                html.append("<label for=\"sunday\">").append(dayNames[1]).append("</label>");
                html.append("<input type=\"checkbox\" id=\"monday\" name=\"monday\"").append(workHours.isMonday() ? " checked" : "").append(" onchange=\"workhours.doChange()\">");
                html.append("<label for=\"monday\">").append(dayNames[2]).append("</label>");
                html.append("<input type=\"checkbox\" id=\"tuesday\" name=\"tuesday\"").append(workHours.isTuesday() ? " checked" : "").append(" onchange=\"workhours.doChange()\">");
                html.append("<label for=\"tuesday\">").append(dayNames[3]).append("</label>");
                html.append("<input type=\"checkbox\" id=\"wednesday\" name=\"wednesday\"").append(workHours.isWednesday() ? " checked" : "").append(" onchange=\"workhours.doChange()\">");
                html.append("<label for=\"wednesday\">").append(dayNames[4]).append("</label>");
                html.append("<input type=\"checkbox\" id=\"thursday\" name=\"thursday\"").append(workHours.isThursday() ? " checked" : "").append(" onchange=\"workhours.doChange()\">");
                html.append("<label for=\"thursday\">").append(dayNames[5]).append("</label>");
                html.append("<input type=\"checkbox\" id=\"friday\" name=\"friday\"").append(workHours.isFriday() ? " checked" : "").append(" onchange=\"workhours.doChange()\">");
                html.append("<label for=\"friday\">").append(dayNames[6]).append("</label>");
                html.append("<input type=\"checkbox\" id=\"saturday\" name=\"saturday\"").append(workHours.isSaturday() ? " checked" : "").append(" onchange=\"workhours.doChange()\">");
                html.append("<label for=\"saturday\">").append(dayNames[7]).append("</label>");
                html.append("</div>");
                html.append("</div>");
            }
        }
        html.append("<div id=\"workhoursfooter\">");
        html.append("</div>");
        return html.toString();
    }

    public void setShiftId(String shiftId) {
        this.shiftid = shiftId;
    }

    public void setKeyId1(String keyid1) {
        this.keyid1 = keyid1;
    }

    public void setUpData() {
        block20: {
            CalendarFactory calendarFactory = new CalendarFactory(this.getConnectionId());
            if (this.mode == Mode.ADMIN) {
                if (this.sdcid.length() > 0 && this.keyid1.length() > 0) {
                    try {
                        if (this.sdcid.equalsIgnoreCase("User")) {
                            this.lvCalendar = calendarFactory.getUserCalendar(this.keyid1, true, true);
                            this.title = this.getTranslationProcessor().translate("Calendar For User") + " " + this.keyid1 + "";
                            break block20;
                        }
                        if (this.sdcid.equalsIgnoreCase("Instrument")) {
                            this.lvCalendar = calendarFactory.getInstrumentCalendar(this.keyid1, true, true);
                            this.title = this.getTranslationProcessor().translate("Calendar For Instrument") + " " + this.keyid1 + "";
                            break block20;
                        }
                        if (this.sdcid.equalsIgnoreCase("Department")) {
                            this.setDepartmentType(this.keyid1);
                            if (this.workarea && !this.testinglab) {
                                this.lvCalendar = null;
                            } else if (this.shiftid.length() > 0 && !this.shiftid.equalsIgnoreCase("(null)")) {
                                this.lvCalendar = calendarFactory.getShiftCalendar(this.keyid1, this.shiftid, true, true);
                                this.title = this.getTranslationProcessor().translate("Calendar For Department") + " " + this.keyid1 + " " + this.getTranslationProcessor().translate("Shift") + " " + this.shiftid;
                            } else {
                                this.lvCalendar = calendarFactory.getDepartmentCalendar(this.keyid1, true, true);
                                this.title = this.getTranslationProcessor().translate("Calendar For Department") + " " + this.keyid1 + "";
                            }
                            break block20;
                        }
                        if (this.sdcid.equalsIgnoreCase("LV_Calendar")) {
                            this.lvCalendar = calendarFactory.getCalendar(this.keyid1);
                            if (this.lvCalendar != null && !this.lvCalendar.isShared()) {
                                this.viewOnly = true;
                                this.title = this.getTranslationProcessor().translate("Private Calendar") + " " + this.keyid1;
                            } else {
                                DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("SELECT calendardesc FROM calendar WHERE calendarid=?", new Object[]{this.keyid1});
                                this.title = ds != null && ds.getRowCount() > 0 ? ds.getValue(0, "calendardesc", this.getTranslationProcessor().translate("Shared Calendar") + " (" + this.getTranslationProcessor().translate("No Description") + ")") : this.getTranslationProcessor().translate("Shared Calendar") + " " + this.keyid1;
                                if (this.lvCalendar != null && !this.lvCalendar.isInternal()) {
                                    this.viewOnly = true;
                                }
                            }
                            break block20;
                        }
                        this.logger.error("Invalid SDC Id provided for Admin mode.");
                    }
                    catch (SapphireException e) {
                        this.logger.error("Failed to obtain calendar.");
                    }
                } else {
                    this.logger.error("No SDC Id and/or Keyid1 provided for Admin mode.");
                }
            } else if (this.mode == Mode.USER) {
                try {
                    String s = this.getConnectionProcessor().getSapphireConnection().getSysuserId();
                    this.lvCalendar = calendarFactory.getUserCalendar(s, true, true);
                    this.title = this.getTranslationProcessor().translate("My Calendar") + " ";
                }
                catch (SapphireException e) {
                    this.logger.error("Failed to obtain user calendar.");
                }
            }
        }
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
        this.setUpData();
    }

    public static enum Mode {
        ADMIN,
        USER;

    }
}

