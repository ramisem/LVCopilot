/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpSession
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.scheduler;

import com.labvantage.sapphire.RequestParser;
import com.labvantage.sapphire.pageelements.controls.Button;
import com.labvantage.sapphire.pageelements.maint.DataView;
import com.labvantage.sapphire.pageelements.maint.EditorStyleField;
import com.labvantage.sapphire.scheduler.SchedulerAdminProcessor;
import com.labvantage.sapphire.tagext.JavaScriptAPITag;
import com.labvantage.sapphire.util.dhtmlxscheduler.DHTMLXScheduler;
import java.util.ArrayList;
import java.util.Calendar;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.pageelements.BaseElement;
import sapphire.util.DataSet;
import sapphire.util.HttpUtil;
import sapphire.util.M18NUtil;
import sapphire.util.SafeHTML;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class ScheduleEventList
extends BaseElement {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";

    public static String writeTaskDropdown(PageContext pageContext, String select, String type) {
        QueryProcessor qp = new QueryProcessor(pageContext);
        StringBuilder out = new StringBuilder();
        TranslationProcessor tp = new TranslationProcessor(pageContext);
        DataSet taskDS = qp.getSqlDataSet("select propertytreeid, objectname from propertytree where propertytreetype = 'ScheduleTask'");
        for (int i = 0; i < taskDS.getRowCount(); ++i) {
            String tempid = taskDS.getString(i, "propertytreeid");
            String translatedId = tp.translate(tempid);
            String selected = tempid.equals(select) ? "selected" : "";
            boolean include = false;
            if ("Stability".equals(type)) {
                String objectname = taskDS.getString(i, "objectname");
                try {
                    Class<?> c = Class.forName(objectname);
                    Class<?>[] interfaces = c.getInterfaces();
                    if (interfaces != null && interfaces.length > 0) {
                        for (int j = 0; j < interfaces.length; ++j) {
                            if (!"com.labvantage.sapphire.stability.task.GridTask".equals(interfaces[j].getName())) continue;
                            include = true;
                            break;
                        }
                    }
                    if (!include && "sapphire.action.BaseAdvancedPullSample".equals(c.getSuperclass().getName())) {
                        include = true;
                    }
                }
                catch (Exception cnfe) {
                    include = false;
                }
            } else {
                include = true;
            }
            if (!include) continue;
            out.append("<option value=\"" + tempid + "\"" + selected + ">" + translatedId + "</option>");
        }
        return out.toString();
    }

    @Override
    public String getHtml() {
        PropertyList properties;
        String usertimezone;
        String allowStudySearchStr;
        String orgk;
        StringBuilder html = new StringBuilder();
        SchedulerAdminProcessor scheduler = new SchedulerAdminProcessor(this.pageContext);
        DataSet ds = new DataSet();
        boolean isSummaryMode = false;
        PropertyList calendarProps = this.element.getPropertyListNotNull("calendarprops");
        PropertyList eventProps = calendarProps.getPropertyListNotNull("eventprops");
        PropertyList operationsProps = this.element.getPropertyListNotNull("operations");
        String pageId = this.pageContext.getRequest().getParameter("page");
        HttpSession session = this.pageContext.getSession();
        session.setAttribute(pageId, (Object)this.element);
        PropertyList taskeventprops = this.element.getPropertyListNotNull("taskeventprops");
        PropertyList extraColProps = this.element.getPropertyListNotNull("extracolprops").copy();
        PropertyListCollection columns = extraColProps.getCollectionNotNull("columns");
        for (int m = 0; m < columns.size(); ++m) {
            PropertyList columnProps = columns.getPropertyList(m);
            String column = RequestParser.parseAlias(columnProps.getProperty("columnid"));
            columnProps.setProperty("columnid", column);
        }
        String stability = this.pageContext.getRequest().getParameter("stability");
        String errormode = this.pageContext.getRequest().getParameter("errormode");
        if (errormode == null) {
            errormode = "N";
        }
        boolean isErrorMode = errormode.equals("Y");
        if (stability == null) {
            stability = "N";
        }
        if (this.element.getProperty("mode", "").equals("stability")) {
            stability = "Y";
        }
        boolean isStabilityMode = stability.startsWith("Y");
        M18NUtil m18n = new M18NUtil(this.pageContext);
        String scheduleplanid = this.pageContext.getRequest().getParameter("scheduleplanid");
        String scheduleplanitemid = this.pageContext.getRequest().getParameter("scheduleplanitemid");
        String rsetid = this.pageContext.getRequest().getParameter("rsetid");
        String selectednodeid = this.pageContext.getRequest().getParameter("selectednodeid");
        String groupby = this.pageContext.getRequest().getParameter("groupby");
        String sortby = this.pageContext.getRequest().getParameter("sortby");
        String sortbydirection = this.pageContext.getRequest().getParameter("sortbydirection");
        String maxrowsstring = this.pageContext.getRequest().getParameter("maxrows");
        String status = this.pageContext.getRequest().getParameter("status");
        String pageNo = this.pageContext.getRequest().getParameter("pageno");
        int currentpage = 1;
        try {
            currentpage = Integer.parseInt(pageNo);
        }
        catch (NumberFormatException numberFormatException) {
            // empty catch block
        }
        String localfromdate = this.pageContext.getRequest().getParameter("fromdate");
        String localtodate = this.pageContext.getRequest().getParameter("todate");
        String linksdcid = this.pageContext.getRequest().getParameter("linksdcid");
        String linkkeyid1 = this.pageContext.getRequest().getParameter("linkkeyid1");
        if (linkkeyid1 == null && !isStabilityMode) {
            linkkeyid1 = this.pageContext.getRequest().getParameter("keyid1");
        }
        String childnodes = this.pageContext.getRequest().getParameter("childnodes");
        String getevents = this.pageContext.getRequest().getParameter("getevents");
        String pagemode = this.pageContext.getRequest().getParameter("pagemode");
        String showdisabled = this.pageContext.getRequest().getParameter("showdisabled");
        String dateinusertimezone = this.pageContext.getRequest().getParameter("dateinusertimezone");
        String selectedview = this.pageContext.getRequest().getParameter("selectedview");
        String selecteddate = this.pageContext.getRequest().getParameter("selecteddate");
        String showexcludeid = this.pageContext.getRequest().getParameter("showexcludeid");
        String studyid = this.pageContext.getRequest().getParameter("keyid1");
        String keyid1 = this.pageContext.getRequest().getParameter("keyid1");
        if (keyid1 == null || keyid1.length() == 0) {
            keyid1 = "";
        }
        if ((orgk = this.pageContext.getRequest().getParameter("orgk")) == null || orgk.equals("")) {
            orgk = this.pageContext.getRequest().getParameter("keyid1");
        }
        if (studyid == null || studyid.length() == 0) {
            studyid = this.pageContext.getRequest().getParameter("studyid");
        }
        if (studyid == null || studyid.length() == 0) {
            studyid = "";
        }
        String returntolistpage = this.pageContext.getRequest().getParameter("returntolistpage") == null ? "" : this.pageContext.getRequest().getParameter("returntolistpage");
        String nextpage = this.pageContext.getRequest().getParameter("nextpage") == null ? "" : this.pageContext.getRequest().getParameter("nextpage");
        String studytype = this.pageContext.getRequest().getParameter("studytype");
        if (studytype == null || studytype.length() == 0) {
            studytype = "";
        }
        String studyid1 = this.pageContext.getRequest().getParameter("SingleStudy_lookup");
        String studyid2 = this.pageContext.getRequest().getParameter("StudyByDept_lookup");
        String studyid3 = this.pageContext.getRequest().getParameter("StudyByOwner_lookup");
        String studyid4 = this.pageContext.getRequest().getParameter("StudyBySuite_lookup");
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
        if (studytype.isEmpty() && studyid1.isEmpty() && studyid2.isEmpty() && studyid3.isEmpty() && studyid3.isEmpty()) {
            if (!studyid.isEmpty()) {
                studyid1 = studyid;
                studytype = "SingleStudy";
            } else {
                studytype = "AllStudies";
            }
        }
        boolean allowStudySearch = (allowStudySearchStr = this.pageContext.getRequest().getParameter("allowstudysearch")) == null || allowStudySearchStr.equals("Y");
        PropertyList lookupProps = this.element.getPropertyListNotNull("stabilityprops").getPropertyListNotNull("lookuppages");
        String singleStudyEditorStyle = lookupProps.getProperty("singlestudy", "Study");
        String studyByDeptEditorStyle = lookupProps.getProperty("studybydept", "Department");
        String studyByOwnerEditorStyle = lookupProps.getProperty("studybyowner", "Users");
        String studyBySuiteEditorStyle = lookupProps.getProperty("studybysuite", "StudySuite");
        String activeOnly = this.pageContext.getRequest().getParameter("activeonly");
        if (activeOnly == null) {
            activeOnly = "";
        }
        String startdatejs = this.pageContext.getRequest().getParameter("startdatejs");
        String enddatejs = this.pageContext.getRequest().getParameter("enddatejs");
        String tasktype = this.pageContext.getRequest().getParameter("tasktype");
        if (tasktype == null || tasktype.length() == 0) {
            tasktype = "";
        }
        Calendar todate = null;
        Calendar fromdate = null;
        if (localfromdate != null && localfromdate.length() > 0) {
            fromdate = m18n.parseCalendar(localfromdate);
        } else if (startdatejs != null && startdatejs.length() > 0) {
            fromdate = Calendar.getInstance();
            fromdate.setTimeInMillis(Long.valueOf(startdatejs));
            localfromdate = m18n.formatDateOnly(fromdate);
        } else if (!isErrorMode) {
            fromdate = Calendar.getInstance();
            fromdate.set(14, 0);
            fromdate.set(12, 0);
            fromdate.set(10, 0);
            fromdate.set(5, 1);
            localfromdate = m18n.formatDateOnly(fromdate);
        }
        if (enddatejs != null && enddatejs.length() > 0) {
            todate = Calendar.getInstance();
            todate.setTimeInMillis(Long.valueOf(enddatejs) - 1L);
            localtodate = m18n.formatDateOnly(todate);
        } else if ((localtodate == null || localtodate.isEmpty()) && !isErrorMode) {
            todate = Calendar.getInstance();
            todate.set(14, 0);
            todate.set(12, 0);
            todate.set(10, 0);
            todate.set(5, fromdate.getActualMaximum(5));
            localtodate = m18n.formatDateOnly(todate);
        }
        String includeLocalToDate = localtodate;
        Calendar includetodate = Calendar.getInstance();
        includetodate.setTime(m18n.parseCalendar(localtodate).getTime());
        includetodate.add(5, 1);
        includeLocalToDate = m18n.formatDateOnly(includetodate);
        if (dateinusertimezone == null) {
            dateinusertimezone = "N";
        }
        if (rsetid == null) {
            rsetid = "";
        }
        if (selectedview == null) {
            selectedview = "";
        }
        if (selecteddate == null) {
            selecteddate = "";
        }
        if (getevents == null) {
            getevents = "";
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
        if (linkkeyid1 == null) {
            linkkeyid1 = "";
        }
        if (linksdcid == null) {
            linksdcid = "";
        }
        if (childnodes == null) {
            childnodes = "Y";
        }
        if (showexcludeid == null) {
            showexcludeid = "";
        }
        if (groupby == null) {
            groupby = (String)this.pageContext.getSession().getAttribute("eventlist_groupby");
        }
        if (sortby == null) {
            sortby = (String)this.pageContext.getSession().getAttribute("eventlist_sortby");
        }
        if (sortbydirection == null) {
            sortbydirection = (String)this.pageContext.getSession().getAttribute("eventlist_sortbydirection");
        }
        if (maxrowsstring == null) {
            maxrowsstring = (String)this.pageContext.getSession().getAttribute("eventlist_maxrowsstring");
        }
        if (status == null) {
            status = (String)this.pageContext.getSession().getAttribute("eventlist_status");
        }
        if (pagemode == null) {
            pagemode = (String)this.pageContext.getSession().getAttribute("eventlist_pagemode");
        }
        if (showdisabled == null) {
            showdisabled = "N";
        }
        if (pagemode == null) {
            pagemode = this.element.getProperty("defaultview", "list");
        }
        if (stability == null) {
            stability = "N";
        }
        if (groupby == null) {
            groupby = isStabilityMode ? "studyid" : "source";
        }
        if (sortby == null || sortby.length() == 0) {
            sortby = "eventdt";
        }
        if (sortbydirection == null || sortbydirection.length() == 0) {
            sortbydirection = "a";
        }
        if (status == null && (status = this.element.getProperty("defaultfilter", "S")).equals("Any")) {
            status = "";
        }
        int maxrows = 1000;
        if (isStabilityMode) {
            maxrows = 20000;
        }
        if (maxrowsstring != null && maxrowsstring.length() > 0) {
            try {
                maxrows = Integer.parseInt(maxrowsstring);
            }
            catch (NumberFormatException numberFormatException) {
                // empty catch block
            }
        }
        maxrowsstring = "" + maxrows;
        String loadUrl = "rc?command=operation&operationclass=com.labvantage.sapphire.scheduler.ScheduleEventRequest";
        if (!scheduleplanid.isEmpty()) {
            loadUrl = loadUrl + "&scheduleplanid=" + scheduleplanid;
        }
        if (!scheduleplanitemid.isEmpty()) {
            loadUrl = loadUrl + "&scheduleplanitemid=" + scheduleplanitemid;
        }
        if (!selectednodeid.isEmpty()) {
            loadUrl = loadUrl + "&selectednodeid=" + HttpUtil.encodeURIComponent(selectednodeid);
        }
        if (!childnodes.isEmpty()) {
            loadUrl = loadUrl + "&childnodes=" + childnodes;
        }
        if (!status.isEmpty()) {
            loadUrl = loadUrl + "&status=" + status;
        }
        if (!showdisabled.isEmpty()) {
            loadUrl = loadUrl + "&showdisabled=" + showdisabled;
        }
        if (!dateinusertimezone.isEmpty()) {
            loadUrl = loadUrl + "&dateinusertimezone=" + dateinusertimezone;
        }
        if (!isStabilityMode) {
            if (!linksdcid.isEmpty()) {
                loadUrl = loadUrl + "&linksdcid=" + linksdcid;
            }
            if (!linkkeyid1.isEmpty()) {
                loadUrl = loadUrl + "&linkkeyid1=" + linkkeyid1;
            }
        }
        if (!pageId.isEmpty()) {
            loadUrl = loadUrl + "&page=" + pageId;
        }
        if (!showexcludeid.isEmpty()) {
            loadUrl = loadUrl + "&showexcludeid=" + showexcludeid;
        }
        if (!tasktype.isEmpty()) {
            loadUrl = loadUrl + "&tasktype=" + tasktype;
        }
        if (!maxrowsstring.isEmpty()) {
            loadUrl = loadUrl + "&maxrows=" + maxrowsstring;
        }
        if (!stability.isEmpty()) {
            loadUrl = loadUrl + "&stability=" + stability;
            if (!studyid.isEmpty()) {
                loadUrl = loadUrl + "&studyid=" + studyid;
            }
            if (!studyid1.isEmpty()) {
                loadUrl = loadUrl + "&studyid1=" + studyid1;
            }
            if (!studyid2.isEmpty()) {
                loadUrl = loadUrl + "&studyid2=" + studyid2;
            }
            if (!studyid3.isEmpty()) {
                loadUrl = loadUrl + "&studyid3=" + studyid3;
            }
            if (!studyid4.isEmpty()) {
                loadUrl = loadUrl + "&studyid4=" + studyid4;
            }
            if (!activeOnly.isEmpty()) {
                loadUrl = loadUrl + "&activeonly=" + activeOnly;
            }
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
        if (!isErrorMode) {
            this.pageContext.getSession().setAttribute("eventlist_groupby", (Object)groupby);
            this.pageContext.getSession().setAttribute("eventlist_sortby", (Object)sortby);
            this.pageContext.getSession().setAttribute("eventlist_sortbydirection", (Object)sortbydirection);
            this.pageContext.getSession().setAttribute("eventlist_maxrowsstring", (Object)maxrowsstring);
            this.pageContext.getSession().setAttribute("eventlist_status", (Object)status);
            this.pageContext.getSession().setAttribute("eventlist_pagemode", (Object)pagemode);
        }
        if ((usertimezone = this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()).getTimeZone()) == null) {
            usertimezone = m18n.getTimezone().getID();
        }
        html.append("<style>");
        html.append("    body{\n        background-color: rgba(255, 253, 253, 0.82);\n    }");
        html.append("</style>");
        html.append("<div id='options_div'>");
        html.append("<input type='hidden' id='stability' value='").append(stability).append("'/>");
        html.append("<input type='hidden' id='dateinusertimezone' value='").append(dateinusertimezone).append("'/>");
        html.append("<input type='hidden' id='usertimezone' value='").append(usertimezone).append("'/>");
        html.append("<input type='hidden' id='taskeventprops' value='").append(HttpUtil.encodeURIComponent(taskeventprops.toJSONString(false))).append("'/>");
        html.append("<input type='hidden' id='calendarprops' value='").append(HttpUtil.encodeURIComponent(calendarProps.toJSONString(false))).append("'/>");
        html.append("<input type='hidden' id='extracolprops' value='").append(HttpUtil.encodeURIComponent(extraColProps.toJSONString(false))).append("'/>");
        html.append("<input type='hidden' id='pagemode' value='").append(pagemode).append("'/>");
        html.append("<input type='hidden' id='loadurl' value='").append(loadUrl).append("'/>");
        boolean mondayFirst = false;
        try {
            ConfigurationProcessor cp = new ConfigurationProcessor(this.pageContext);
            PropertyList dateformatpolicy = cp.getPolicy("DateFormatPolicy", "Sapphire Custom");
            String firstDayOfWeek = dateformatpolicy.getProperty("firstdayofweek", "");
            if (firstDayOfWeek.equals("User Locale")) {
                if (m18n.getNowCalendar().getFirstDayOfWeek() == 2) {
                    mondayFirst = true;
                }
            } else {
                mondayFirst = firstDayOfWeek.equals("Monday");
            }
        }
        catch (SapphireException cp) {
            // empty catch block
        }
        html.append("<input type='hidden' id='start_on_monday' value='").append(HttpUtil.encodeURIComponent(mondayFirst ? "true" : "false")).append("'/>");
        html.append(JavaScriptAPITag.getJQueryAPI(true, false, null, this.pageContext));
        if (pagemode.equals("calendar")) {
            html.append("<script src=\"WEB-CORE/extscripts/dhtmlx/scheduler/dhtmlxscheduler.js\" type=\"text/javascript\" charset=\"utf-8\"></script>\n");
            html.append("<script src=\"WEB-CORE/extscripts/dhtmlx/scheduler/ext/dhtmlxscheduler_grid_view.js\"></script>\n");
            html.append("<script src=\"WEB-CORE/extscripts/dhtmlx/scheduler/ext/dhtmlxscheduler_year_view.js\"></script>\n");
            html.append("<script src=\"WEB-CORE/extscripts/dhtmlx/scheduler/ext/dhtmlxscheduler_timeline.js\"></script>\n");
            html.append("<script src=\"WEB-CORE/extscripts/dhtmlx/scheduler/ext/dhtmlxscheduler_treetimeline.js\"></script>\n");
            html.append("<script src=\"WEB-CORE/extscripts/dhtmlx/scheduler/ext/dhtmlxscheduler_daytimeline.js\"></script>\n");
            html.append("<script src=\"WEB-CORE/extscripts/dhtmlx/scheduler/ext/dhtmlxscheduler_limit.js\"></script>\n");
            html.append("<script src=\"WEB-CORE/extscripts/dhtmlx/scheduler/ext/dhtmlxscheduler_tooltip.js\"></script>\n");
            html.append("<script src=\"WEB-CORE/extscripts/dhtmlx/scheduler/ext/dhtmlxscheduler_week_agenda.js\"></script>\n");
            html.append("<script src=\"WEB-CORE/extscripts/dhtmlx/scheduler/ext/dhtmlxscheduler_timeline.js\"></script>");
            html.append("<script src=\"WEB-CORE/extscripts/dhtmlx/scheduler/ext/dhtmlxscheduler_minical.js\" type=\"text/javascript\"></script>\n");
            html.append("<script src=\"WEB-CORE/extscripts/dhtmlx/scheduler/ext/dhtmlxscheduler_active_links.js\" type=\"text/javascript\" ></script>\n");
            html.append("<script src=\"WEB-CORE/extscripts/dhtmlx/scheduler/ext/dhtmlxscheduler_expand.js\" type=\"text/javascript\"></script>\n");
            html.append("<link rel=\"stylesheet\" href=\"WEB-CORE/extscripts/dhtmlx/scheduler/dhtmlxscheduler_flat.css\" type=\"text/css\" media=\"screen\"  title=\"no title\" charset=\"utf-8\">\n");
            html.append("<script src=\"rc?command=ajax&ajaxclass=com.labvantage.sapphire.ajax.operations.DHTMLXSchedulerTranslation\" type=\"text/javascript\"></script>");
        }
        html.append("<form name=\"displayoptions\" id=\"diplayoptions\" action=\"\" method=\"post\">\n");
        html.append("<input name=\"page\" type=\"hidden\" value=\"").append(this.requestContext.getProperty("page")).append("\">\n");
        html.append("<input name=\"orgk\" type=\"hidden\" value=\"").append(orgk).append("\">\n");
        html.append("<input name=\"keyid1\" type=\"hidden\" value=\"").append(keyid1).append("\">\n");
        html.append("<input name=\"scheduleplanid\" type=\"hidden\" value=\"").append(scheduleplanid).append("\">\n");
        html.append("<input name=\"scheduleplanitemid\" type=\"hidden\" value=\"").append(scheduleplanitemid).append("\">\n");
        html.append("<input name=\"linksdcid\" type=\"hidden\" value=\"").append(linksdcid).append("\">\n");
        html.append("<input name=\"linkkeyid1\" type=\"hidden\" value=\"").append(linkkeyid1).append("\">\n");
        html.append("<input name=\"rsetid\" type=\"hidden\" value=\"").append(rsetid).append("\">\n");
        html.append("<input name=\"selectednodeid\" type=\"hidden\" value=\"").append(selectednodeid).append("\">\n");
        html.append("<input name=\"sortby\" type=\"hidden\" value=\"").append(sortby).append("\">\n");
        html.append("<input name=\"sortbydirection\" type=\"hidden\" value=\"").append(sortbydirection).append("\">\n");
        html.append("<input name=\"childnodes\" type=\"hidden\" value=\"").append(childnodes).append("\"\">\n");
        html.append("<input name=\"selectedview\" id=\"selectedview\"  type=\"hidden\" value=\"").append(selectedview).append("\"\">\n");
        html.append("<input name=\"selecteddate\" id=\"selecteddate\"  type=\"hidden\" value=\"").append(selecteddate).append("\"\">\n");
        html.append("<input type=\"hidden\" id=\"keyid1\" name=\"keyid1\" value=\"").append(keyid1).append("/>\n");
        html.append("<input type=\"hidden\" id=\"orgk\" name=\"orgk\" value=\"").append(orgk).append("\"/>\n");
        html.append("<input name=\"returntolistpage\" type=\"hidden\" value=\"").append(returntolistpage).append("\">\n");
        html.append("<input name=\"nextpage\" type=\"hidden\" value=\"").append(nextpage).append("\">\n");
        html.append("<input name=\"startdatejs\" type=\"hidden\" value=\"\">\n");
        html.append("<input name=\"enddatejs\" type=\"hidden\" value=\"\">\n");
        html.append("<input name=\"errormode\" type=\"hidden\" value=\"").append(errormode).append("\">\n");
        html.append("\n");
        html.append("\n");
        html.append("<table cellspacing=0 cellpadding=0 border=0 bordercolor=\"#b0c4de\" width=\"100%\">");
        html.append("<tbody>");
        html.append("<tr>");
        html.append("<td align=center>");
        html.append(this.getTranslationProcessor().translate("Mode")).append("&nbsp;");
        html.append("</td>");
        html.append("<td>");
        html.append(this.getTranslationProcessor().translate("Events"));
        html.append("</td>");
        if (!isStabilityMode) {
            html.append("<td>");
            html.append(this.getTranslationProcessor().translate("Options"));
            html.append("</td>");
        }
        html.append("<td>");
        html.append(this.getTranslationProcessor().translate("Task"));
        html.append("</td>");
        if (isStabilityMode) {
            if (allowStudySearch) {
                html.append("<td>");
                html.append(this.getTranslationProcessor().translate("Query Studies By"));
                html.append("</td>");
            }
            if (!pagemode.equals("list")) {
                html.append("<td>");
                html.append(this.getTranslationProcessor().translate("Show Exclusion Calendar"));
                html.append("</td>");
            }
        }
        if (pagemode.equals("list")) {
            html.append("<td>");
            html.append(this.getTranslationProcessor().translate("Date Range"));
            html.append("</td>");
            html.append("<td>");
            html.append(this.getTranslationProcessor().translate("Group by")).append(":&nbsp;");
            html.append("</td>");
        }
        html.append("<td>");
        if (pagemode.equals("calendar")) {
            html.append(this.getTranslationProcessor().translate("Row limit"));
        }
        html.append("</td>");
        html.append("</tr><tr>");
        html.append("<td>");
        html.append("<input type=\"radio\" onchange=\"refreshEventList()\" name=\"pagemode\" id=\"pagemode\" value=\"calendar\" ").append(pagemode.equals("calendar") ? "checked=\"checked\"" : "").append(">").append(this.getTranslationProcessor().translate("Calendar"));
        html.append("<br/>");
        html.append("<input type=\"radio\" onchange=\"setCalendarDates();refreshEventList()\" name=\"pagemode\" id=\"pagemode\" value=\"list\" ").append(pagemode.equals("list") ? "checked=\"checked\"" : "").append(">").append(this.getTranslationProcessor().translate("Event List"));
        html.append("</td>");
        html.append("<td>");
        if (!isErrorMode) {
            html.append("\n<select name=\"status\" id=\"status\" onchange=\"refreshEventList()\">\n");
            html.append("<option value=\"S\"").append("S".equals(status) ? "selected" : "").append(">").append(this.getTranslationProcessor().translate("Scheduled")).append("</option>\n");
            html.append("<option value=\"D\"").append("D".equals(status) ? "selected" : "").append(">").append(this.getTranslationProcessor().translate("Done")).append("</option>\n");
            html.append("<option value=\"F\"").append("F".equals(status) ? "selected" : "").append(">").append(this.getTranslationProcessor().translate("Flagged")).append("</option>\n");
            html.append("<option value=\"E\"").append("E".equals(status) ? "selected" : "").append(" >").append(this.getTranslationProcessor().translate("Error")).append("</option>\n");
            html.append("<option value=\"\"").append("".equals(status) ? "selected" : "").append(">").append(this.getTranslationProcessor().translate("Any")).append("</option>\n                        </select>\n");
        } else {
            html.append("<input name=\"status\" type=\"hidden\" value=\"").append(status).append("\"/>\n");
        }
        html.append("</td>");
        if (!isStabilityMode) {
            html.append("<td>");
            html.append(" <input type=\"checkbox\" name=\"showdisabled\" id=\"showdisabled_chexbox\" value=\"").append(showdisabled).append("\"");
            if (showdisabled.equals("Y")) {
                html.append(" checked ");
            }
            html.append("onchange=\"eventList.setYNCheckboxValue(event);refreshEventList()\">").append(this.getTranslationProcessor().translate("Show Plan Items Turned Off")).append("\n");
            html.append("<br/>\n");
            html.append("\n");
            html.append(" <input type=\"checkbox\" name=\"dateinusertimezone\" id=\"dateinusertimezone_chexbox\" value=\"").append(dateinusertimezone).append("\"");
            if (dateinusertimezone.equals("Y")) {
                html.append(" checked ");
            }
            html.append("onchange=\"eventList.setYNCheckboxValue(event);refreshEventList()\">").append(this.getTranslationProcessor().translate("Show events in user timezone")).append("\n");
            html.append("\n");
            html.append("\n");
        }
        html.append("</td>");
        html.append("<td>");
        html.append("<select name=\"tasktype\" id=\"tasktype\" onchange=\"refreshEventList()\">\n\n").append("<option value=\"\"" + (tasktype.equals("") ? "selected" : "") + ">" + this.getTranslationProcessor().translate("Select Task") + "</option>").append(ScheduleEventList.writeTaskDropdown(this.pageContext, tasktype, isStabilityMode ? "Stability" : "")).append("        </select>\n");
        html.append("</td>");
        if (isStabilityMode) {
            if (allowStudySearch) {
                html.append("<td nowrap class=\"displayOption\">");
                html.append("");
                html.append("<select name=\"studytype\" id=\"studytype\" onload=\"displayDiv(this.value);\" onchange=\"displayDiv(this.value);\">");
                html.append("<option value=\"\"" + (studytype.equals("") ? "selected" : "") + ">" + this.getTranslationProcessor().translate("Select one item") + "</option>");
                html.append("<option value=\"AllStudies\" " + (studytype.equals("AllStudies") ? "selected" : "") + ">" + this.getTranslationProcessor().translate("All Studies") + "</option>");
                html.append("<option value=\"SingleStudy\" " + (studytype.equals("SingleStudy") ? "selected" : "") + ">" + this.getTranslationProcessor().translate("By Study") + "</option>");
                html.append("<option value=\"StudyByDept\" " + (studytype.equals("StudyByDept") ? "selected" : "") + ">" + this.getTranslationProcessor().translate("Study By Dept") + "</option>");
                html.append("<option value=\"StudyByOwner\"" + (studytype.equals("StudyByOwner") ? "selected" : "") + ">" + this.getTranslationProcessor().translate("Study By Owner") + "</option>");
                html.append("<option value=\"StudyBySuite\"" + (studytype.equals("StudyBySuite") ? "selected" : "") + ">" + this.getTranslationProcessor().translate("Study By Suite") + "</option>");
                html.append("</select>");
                String SingleStudy_lookup = studyid1;
                if (SingleStudy_lookup == null) {
                    SingleStudy_lookup = "";
                }
                SingleStudy_lookup = SingleStudy_lookup.replaceAll("%3B", ";");
                String StudyByDept_lookup = studyid2;
                if (StudyByDept_lookup == null) {
                    StudyByDept_lookup = "";
                }
                StudyByDept_lookup = StudyByDept_lookup.replaceAll("%3B", ";");
                String StudyByOwner_lookup = studyid3;
                if (StudyByOwner_lookup == null) {
                    StudyByOwner_lookup = "";
                }
                StudyByOwner_lookup = StudyByOwner_lookup.replaceAll("%3B", ";");
                String StudyBySuite_lookup = studyid4;
                if (StudyBySuite_lookup == null) {
                    StudyBySuite_lookup = "";
                }
                StudyBySuite_lookup = StudyBySuite_lookup.replaceAll("%3B", ";");
                html.append("<div name=\"AllStudies_div\" id=\"AllStudies_div\" style=\"display:" + (studytype.equals("AllStudies") ? "block" : "none") + "\">&nbsp;</div>");
                html.append("<div name=\"SingleStudy_div\" id=\"SingleStudy_div\" style=\"display:" + (studytype.equals("SingleStudy") ? "block" : "none") + "\">");
                html.append(this.getEditorStyleHtml(singleStudyEditorStyle, SingleStudy_lookup, "SingleStudy_lookup"));
                html.append("</div>");
                html.append("<div name=\"StudyByDept_div\" id=\"StudyByDept_div\" style=\"display:" + (studytype.equals("StudyByDept") ? "block" : "none") + "\">");
                html.append(this.getEditorStyleHtml(studyByDeptEditorStyle, StudyByDept_lookup, "StudyByDept_lookup"));
                html.append("</div>");
                html.append("<div name=\"StudyByOwner_div\" id=\"StudyByOwner_div\" style=\"display:" + (studytype.equals("StudyByOwner") ? "block" : "none") + "\">");
                html.append(this.getEditorStyleHtml(studyByOwnerEditorStyle, StudyByOwner_lookup, "StudyByOwner_lookup"));
                html.append("</div>");
                html.append("<div name=\"StudyBySuite_div\" id=\"StudyBySuite_div\" style=\"display:" + (studytype.equals("StudyBySuite") ? "block" : "none") + "\">");
                html.append(this.getEditorStyleHtml(studyBySuiteEditorStyle, StudyBySuite_lookup, "StudyBySuite_lookup"));
                html.append("</div>");
                html.append("<div name=\"activeonly_div\" id=\"activeonly_div\" style=\"display:" + (!studytype.equals("SingleStudy") ? "block" : "none") + "\">");
                html.append("<input type = \"checkbox\" name=\"activeonly\" id=\"activeonly\"" + (activeOnly.equals("on") ? "checked" : "") + " onchange=\"refreshEventList()\"> <b>" + this.getTranslationProcessor().translate("Only Active Studies") + "</b>");
                html.append("</div>");
                html.append("</td>");
                html.append("<script language=\"JavaScript\">var displayedDiv = \"").append(SafeHTML.encodeForJavaScript(studytype)).append("\";</script>");
                html.append("");
                html.append("");
            }
            html.append("</td>");
            if (!pagemode.equals("list")) {
                html.append("<td>");
                String editorStyle = "SharedCalendars";
                html.append(this.getEditorStyleHtml(editorStyle, showexcludeid, "showexcludeid", true));
                html.append("</td>");
            }
        }
        if (pagemode.equals("list")) {
            html.append("<td>");
            String startDate = localfromdate != null ? localfromdate : (isErrorMode ? "" : m18n.formatDateOnly(m18n.getNowCalendar()));
            html.append("<input type=\"text\" name=\"fromdate\" id=\"fromdate\" value=\"").append(startDate);
            html.append("\" size=\"12px\"/><a href=\"/Lookup a date\" onClick=\"lookupdate( 'fromdate','','','','','O' );return false\" tabindex=\"0\"><img title=\"Lookup a date\" border=\"0\" src=\"WEB-CORE/elements/images/lookup_date.gif\"></a>\n");
            html.append(this.getTranslationProcessor().translate(" to "));
            html.append("<br/>");
            html.append("<input type=\"text\" name=\"todate\" id=\"todate\" value=\"").append(localtodate != null ? localtodate : "");
            html.append("\"size=\"12px\"/><a href=\"/Lookup a date\" onClick=\"lookupdate( 'todate','','','','','O' );return false\" tabindex=\"0\"><img title=\"Lookup a date\" border=\"0\" src=\"WEB-CORE/elements/images/lookup_date.gif\"></a>\n");
            html.append("\n");
            html.append("</td>");
            html.append("<td>");
            html.append("<select name=\"groupby\" id=\"groupby\" onchange=\"refreshEventList()\">\n");
            if (!isStabilityMode) {
                html.append("<option value=\"source\" ").append(groupby.equals("source") ? "selected" : "").append(">").append(this.getTranslationProcessor().translate("Source")).append("</option>\n");
            }
            html.append("<option value=\"eventdt\" ").append(groupby.equals("eventdt") ? "selected" : "").append(">").append(this.getTranslationProcessor().translate("Event Date")).append("</option>\n");
            html.append("<option value=\"scheduleplanitemdesc\" ").append(groupby.equals("scheduleplanitemdesc") ? "selected" : "").append(">").append(this.getTranslationProcessor().translate("Description")).append("</option>\n");
            html.append("<option value=\"eventstatus\" ").append(groupby.equals("eventstatus") ? "selected" : "").append(">").append(this.getTranslationProcessor().translate("Status")).append("</option>\n");
            html.append("<option value=\"propertytreeid\" ").append(groupby.equals("propertytreeid") ? "selected" : "").append(">").append(this.getTranslationProcessor().translate("Task")).append("</option>\n");
            if (isStabilityMode) {
                html.append("<option value=\"studyid\" ").append(groupby.equals("studyid") ? "selected" : "").append(">").append(this.getTranslationProcessor().translate("Study")).append("</option>\n");
                html.append("<option value=\"scheduletimerule\" ").append(groupby.equals("scheduletimerule") ? "selected" : "").append(">").append(this.getTranslationProcessor().translate("Time Point")).append("</option>\n");
                html.append("<option value=\"conditionlabel\" ").append(groupby.equals("conditionlabel") ? "selected" : "").append(">").append(this.getTranslationProcessor().translate("Condition")).append("</option>\n");
            }
            html.append("<option value=\"\" ").append(groupby.equals("") ? "selected" : "").append(">").append("</option></select>\n");
            html.append("</td>");
            properties = new PropertyList();
            properties.setProperty("scheduleplanid", scheduleplanid);
            properties.setProperty("schduleplanitemid", scheduleplanitemid);
            properties.setProperty("selectednodeid", selectednodeid);
            properties.setProperty("status", status);
            properties.setProperty("linksdcid", linksdcid);
            properties.setProperty("linkkeyid1", linkkeyid1);
            properties.setProperty("fromdate", localfromdate);
            properties.setProperty("todate", includeLocalToDate);
            properties.setProperty("showdisabled", showdisabled);
            properties.setProperty("onlyexisting", onlyExisting);
            properties.setProperty("childnodes", childnodes);
            properties.setProperty("stability", stability);
            properties.setProperty("tasktype", tasktype);
            properties.setProperty("mode", "getevents");
            properties.setProperty("dateinusertimezone", dateinusertimezone);
            properties.setProperty("studyid", studyid);
            properties.setProperty("studyid1", studyid1);
            properties.setProperty("studyid2", studyid2);
            properties.setProperty("studyid3", studyid3);
            properties.setProperty("studyid4", studyid4);
            properties.setProperty("activeonly", activeOnly);
            properties.setProperty("errormode", isErrorMode ? "Y" : "N");
            properties.setProperty("pageprops", this.element);
            try {
                ds = scheduler.getEvents(properties);
            }
            catch (Exception e) {
                html.append(this.getTranslationProcessor().translate("Could not fetch events"));
            }
            ds.sort(sortby);
            isSummaryMode = properties.getProperty("summarymode", "N").equals("Y");
            if (isSummaryMode) {
                html.append("<td style='width: 200px'>");
                html.append(this.getTranslationProcessor().translate("Note: Window dates and full details shown only when in single study mode"));
            }
        }
        if (pagemode.equals("calendar")) {
            html.append("<td>");
            html.append("<input name=\"maxrows\" id=\"maxrows\" size=\"5\" value=\"").append(maxrows).append("\">").append(" \n");
        }
        html.append("</td>");
        html.append("<td>");
        html.append("</td>");
        html.append("<td>");
        Button ok = new Button(this.pageContext);
        ok.setId("ok");
        ok.setAction("refreshEventList()");
        ok.setText(this.getTranslationProcessor().translate("Refresh"));
        ok.setTip(this.getTranslationProcessor().translate("Refresh the event list"));
        html.append(ok.getHtml());
        html.append("</td>");
        html.append("<td>");
        html.append("</tr></tbody></table>");
        html.append("<input name=\"pageno\" type=\"hidden\" value=\"\"/>\n");
        if (pagemode.equals("calendar")) {
            html.append("</form>");
        }
        html.append("\n");
        html.append("</div>");
        if (isSummaryMode) {
            html.append("<script language='JavaScript'>var isSummaryMode = true;</script>");
        } else {
            html.append("<script language='JavaScript'>var isSummaryMode = false;</script>");
        }
        if (pagemode.equals("list")) {
            // empty if block
        }
        if (pagemode.equals("list")) {
            int totalrows = ds.getRowCount();
            int maxRowsPerPage = 1000;
            PropertyList dataviewProps = this.element.getPropertyListNotNull("listprops");
            dataviewProps.setProperty("dataset", "eventlist");
            dataviewProps.setProperty("showgroupby", "Y");
            PropertyListCollection listColumns = dataviewProps.getCollectionNotNull("columns");
            listColumns.index("columnid");
            if (isSummaryMode) {
                this.hideColumn(listColumns, "s_sampleid");
                this.hideColumn(listColumns, "windowenddtstr");
                this.hideColumn(listColumns, "windowstartdtstr");
            }
            PropertyListCollection groupByCollection = new PropertyListCollection();
            PropertyList groupBy = new PropertyList();
            groupBy.setProperty("id", "default");
            groupBy.setProperty("columnid", groupby);
            if (groupby.equals("eventstatus")) {
                groupBy.setProperty("displayvalue", "S=Scheduled;D=Done;E=Error;X=Inactivated;P=Planned;I=Cancelled;F=Flagged;A=Done (Adhoc)");
                groupBy.setProperty("translatevalue", "Y");
            }
            groupByCollection.add(groupBy);
            dataviewProps.setProperty("groupby", groupByCollection);
            PropertyListCollection sortByCollection = new PropertyListCollection();
            PropertyList sortBy = new PropertyList();
            sortBy.setProperty("columnid", sortby);
            sortBy.setProperty("asc-desc", sortbydirection);
            sortBy.setProperty("callback", "Y");
            sortByCollection.add(sortBy);
            dataviewProps.setProperty("sortby", sortByCollection);
            dataviewProps.setProperty("rowsperpage", String.valueOf(maxRowsPerPage));
            dataviewProps.setProperty("selectiontype", "Hidden");
            int qualifiedrows = ds.getRowCount();
            if (currentpage > 1 && qualifiedrows > maxRowsPerPage) {
                int j;
                int startNo = 0;
                startNo = (currentpage - 1) * maxRowsPerPage;
                int endNo = currentpage * maxRowsPerPage;
                if (endNo > qualifiedrows) {
                    endNo = qualifiedrows;
                }
                for (j = qualifiedrows - 1; j > endNo; --j) {
                    ds.deleteRow(j);
                }
                for (j = 0; j < startNo - 1; ++j) {
                    ds.deleteRow(0);
                }
            }
            DataView dataView = new DataView(this.pageContext, ds, this.getConnectionid());
            dataView.setElementid("eventlist");
            dataView.setSDCId("SchedulePlanItem");
            dataView.setElementProperties(dataviewProps);
            dataView.setRenderTagsJS(true);
            boolean isPagingRequest = false;
            if (pageNo != null && pageNo.length() > 0) {
                isPagingRequest = true;
            }
            html.append("<input name=\"listmode\" type=\"hidden\" value=\"list\"/>\n");
            html.append("<input name=\"rowsperpage\" type=\"hidden\" value=\"").append("500").append("\"/>\n");
            html.append("<input name=\"selectionmode\" type=\"hidden\" value=\"").append("").append("\"/>\n");
            html.append("<input name=\"currentpage\" id=\"currentpage\" type=\"hidden\" value=\"").append(currentpage).append("\"/>\n");
            if (!isPagingRequest || isPagingRequest && currentpage == 0) {
                html.append("<input name=\"totalrows\" type=\"hidden\" value=\"").append(totalrows).append("\"/>\n");
                html.append("<input name=\"qualifiedrows\" type=\"hidden\" value=\"").append(qualifiedrows).append("\"/>\n");
            } else {
                html.append("<input name=\"totalrows\" type=\"hidden\" value=\"").append(totalrows).append("\">\n");
                html.append("<input name=\"qualifiedrows\" type=\"hidden\" value=\"").append(qualifiedrows).append("\">\n");
                html.append("<input name=\"keyid1\" type=\"hidden\" value=\"").append(this.pageContext.getRequest().getParameter("keyid1")).append("\">\n");
            }
            html.append("</form>");
            html.append(dataView.getHtml());
        } else if (pagemode.equals("calendar")) {
            html.append((CharSequence)this.getCalendarHtml(selectedview, calendarProps, m18n));
            properties = new PropertyList();
            properties.setProperty("scheduleplanid", scheduleplanid);
            properties.setProperty("schduleplanitemid", scheduleplanitemid);
            properties.setProperty("selectednodeid", selectednodeid);
            properties.setProperty("stability", stability);
            properties.setProperty("tasktype", tasktype);
            properties.setProperty("status", status);
            properties.setProperty("linksdcid", linksdcid);
            properties.setProperty("linkkeyid1", linkkeyid1);
            properties.setProperty("fromdate", localfromdate);
            properties.setProperty("todate", includeLocalToDate);
            properties.setProperty("showdisabled", showdisabled);
            properties.setProperty("onlyexisting", onlyExisting);
            properties.setProperty("childnodes", childnodes);
            properties.setProperty("mode", "getevents");
            properties.setProperty("sectionsonly", "Y");
            properties.setProperty("studyid", studyid);
            properties.setProperty("studyid1", studyid1);
            properties.setProperty("studyid2", studyid2);
            properties.setProperty("studyid3", studyid3);
            properties.setProperty("studyid4", studyid4);
            DataSet sectionsDs = null;
            try {
                sectionsDs = scheduler.getEvents(properties);
            }
            catch (Exception e) {
                html.append(this.getTranslationProcessor().translate("Could not fetch sections data!"));
            }
            ArrayList<String> sourceSections = new ArrayList<String>();
            ArrayList<String> planItemSections = new ArrayList<String>();
            ArrayList<String> studySections = new ArrayList<String>();
            try {
                JSONArray sectionSourceJSON = new JSONArray();
                JSONArray sectionPlanItemJSON = new JSONArray();
                JSONArray sectionStudyJSON = new JSONArray();
                for (int i = 0; i < sectionsDs.getRowCount(); ++i) {
                    String studyId;
                    String planItemSection;
                    JSONObject obj;
                    String sourceSection;
                    String planId = sectionsDs.getString(i, "scheduleplanid");
                    String planItemId = sectionsDs.getString(i, "scheduleplanitemid");
                    String sourceSdcid = sectionsDs.getString(i, "linksdcid", "");
                    String sourceKeyid1 = sectionsDs.getString(i, "linkkeyid1", "");
                    String scheduleRule = sectionsDs.getString(i, "schedulerule", "");
                    String schedulePlanItemDesc = sectionsDs.getString(i, "scheduleplanitemdesc", "");
                    if (!(sourceSdcid.isEmpty() || sourceKeyid1.isEmpty() || sourceSections.contains(sourceSection = sourceSdcid + "|" + sourceKeyid1))) {
                        sourceSections.add(sourceSection);
                        obj = new JSONObject();
                        obj.put("key", sourceSection);
                        obj.put("label", this.getTranslationProcessor().translate(sourceSdcid) + ": " + sourceKeyid1);
                        sectionSourceJSON.put(obj);
                    }
                    if (!planItemSections.contains(planItemSection = planId + "|" + planItemId)) {
                        planItemSections.add(planItemSection);
                        obj = new JSONObject();
                        obj.put("key", planItemSection);
                        if (schedulePlanItemDesc.isEmpty()) {
                            schedulePlanItemDesc = planId + " " + scheduleRule;
                        }
                        obj.put("label", schedulePlanItemDesc);
                        sectionPlanItemJSON.put(obj);
                    }
                    if (studySections.contains(studyId = sectionsDs.getString(i, "studyid", ""))) continue;
                    studySections.add(studyId);
                    JSONObject obj2 = new JSONObject();
                    obj2.put("key", studyId);
                    obj2.put("label", studyId);
                    sectionStudyJSON.put(obj2);
                }
                html.append("<input type='hidden' id='section_source' value='").append(HttpUtil.encodeURIComponent(sectionSourceJSON.toString())).append("'/>");
                html.append("<input type='hidden' id='section_planitem' value='").append(HttpUtil.encodeURIComponent(sectionPlanItemJSON.toString())).append("'/>");
                html.append("<input type='hidden' id='section_study' value='").append(HttpUtil.encodeURIComponent(sectionStudyJSON.toString())).append("'/>");
            }
            catch (JSONException e) {
                html.append(this.getTranslationProcessor().translate("Could not parse locale information"));
            }
        }
        html.append("<script src=\"WEB-CORE/modules/scheduler/scripts/eventlist.js\"></script>\n");
        html.append("<script language=\"JavaScript\">");
        boolean requireMoveEsig = operationsProps.getProperty("moveesig", "N").startsWith("Y");
        html.append("var requireMoveEsig = ").append(requireMoveEsig ? "true" : "false").append(";");
        html.append("</script>");
        return html.toString();
    }

    private void hideColumn(PropertyListCollection listColumns, String id) {
        PropertyList col = listColumns.getIndexedPropertyList(id);
        if (col != null) {
            col.setProperty("mode", "Hidden Value");
        }
    }

    private String getEditorStyleHtml(String editorStyleId, String value, String id) {
        return this.getEditorStyleHtml(editorStyleId, value, id, false);
    }

    private String getEditorStyleHtml(String editorStyleId, String value, String id, boolean refresh) {
        StringBuilder html = new StringBuilder();
        try {
            EditorStyleField editorStyleField = new EditorStyleField(this.pageContext);
            editorStyleField.setEditorStyleId(editorStyleId);
            editorStyleField.setFieldName(id);
            editorStyleField.setFieldValue(value);
            PropertyList column = new PropertyList();
            column.setProperty("columnid", id);
            String cssclass = "modern_search_mandatoryfield";
            column.setProperty("class", cssclass);
            PropertyListCollection events = new PropertyListCollection();
            PropertyList oninput = new PropertyList();
            oninput.setProperty("event", "oninput");
            oninput.setProperty("js", "this.value=this.value");
            events.add(oninput);
            if (refresh) {
                PropertyList oninput2 = new PropertyList();
                oninput2.setProperty("event", "onchange");
                oninput2.setProperty("js", "refreshEventList();");
                events.add(oninput2);
            }
            column.setProperty("events", events);
            editorStyleField.setReadonly(false);
            editorStyleField.setColumn(column);
            html.append(editorStyleField.getHtml());
        }
        catch (SapphireException e) {
            html.append("Invalid editor style " + editorStyleId);
        }
        return html.toString();
    }

    private void getEventStyles(StringBuilder html, String status, String color) {
        html.append(".dhx_cal_event.status_").append(status).append(" div { \n");
        if (!color.isEmpty()) {
            html.append("  color:").append(color).append(";\n");
        }
        html.append("}\n");
        html.append(".dhx_cal_event_line.status_").append(status).append(" { \n");
        if (!color.isEmpty()) {
            html.append("  color:").append(color).append(";\n");
        }
        html.append("}\n");
        html.append(".dhx_cal_event_clear.status_").append(status).append(" { \n");
        if (!color.isEmpty()) {
            html.append("  color:").append(color).append(";\n");
        }
        html.append("}\n");
        html.append(".dhx_wa_ev_body.status_").append(status).append(" { \n");
        if (!color.isEmpty()) {
            html.append("  color:").append(color).append(";\n");
        }
        html.append("}\n");
        html.append(".dhx_wa_ev_body.status_").append(status).append(" { \n");
        if (!color.isEmpty()) {
            html.append("  color:").append(color).append(";\n");
        }
        html.append("}\n");
        html.append(".lv_year_tooltip.status_").append(status).append(" { \n");
        if (!color.isEmpty()) {
            html.append("  color:").append(color).append(";\n");
        }
        html.append("}\n");
        html.append(".dhx_cal_event_line.status_").append(status).append(" { \n");
        if (!color.isEmpty()) {
            html.append("  background-color:").append(color).append(";\n");
        }
        html.append("}\n");
    }

    private String arrayToString(String[] array) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (String text : array) {
            sb.append("\"").append(text).append("\"").append(",");
        }
        if (sb.length() > 1) {
            sb.deleteCharAt(sb.length() - 1);
        }
        sb.append("]");
        return sb.toString();
    }

    private StringBuilder getCalendarHtml(String selectedView, PropertyList calendarProps, M18NUtil m18NUtil) {
        String color;
        StringBuilder html = new StringBuilder();
        html.append("<div id=\"scheduler_here\" class=\"dhx_cal_container\" style='width:100%; height:800px;' >\n");
        html.append("<div class=\"dhx_cal_navline\">\n");
        html.append("<div class=\"dhx_cal_prev_button\">&nbsp;</div>\n");
        html.append("<div class=\"dhx_cal_next_button\">&nbsp;</div>\n");
        html.append("<div class=\"dhx_cal_today_button\"></div>\n");
        html.append("<div class=\"dhx_cal_date\"></div>\n");
        boolean viewsAsDropdown = calendarProps.getProperty("showviewsasdropdown", "N").startsWith("Y");
        String calendarview = calendarProps.getProperty("defaultview", "month");
        if (selectedView.length() > 0) {
            calendarview = selectedView;
        }
        if (viewsAsDropdown) {
            html.append("<div class=\"dhx_cal_tab\"  style=\"top:14px\"><select  name=\"calendarview\" id=\"calendarview\"  onchange=\"changeCalendarView()\">\n");
            html.append("<option value=\"day\" ").append(calendarview.equals("day") ? "selected" : "").append(">").append(this.getTranslationProcessor().translate("Day")).append("</option>\n");
            html.append("<option value=\"week\" ").append(calendarview.equals("week") ? "selected" : "").append(">").append(this.getTranslationProcessor().translate("Week")).append("</option>\n");
            html.append("<option value=\"month\" ").append(calendarview.equals("month") ? "selected" : "").append(">").append(this.getTranslationProcessor().translate("Month")).append("</option>\n");
            html.append("<option value=\"year\" ").append(calendarview.equals("year") ? "selected" : "").append(">").append(this.getTranslationProcessor().translate("Year")).append("</option>\n");
            html.append("<option value=\"week_agenda\" ").append(calendarview.equals("week_agenda") ? "selected" : "").append(">").append(this.getTranslationProcessor().translate("Week Agenda")).append("</option>\n");
            html.append("<option value=\"timeline\" ").append(calendarview.equals("timeline") ? "selected" : "").append(">").append(this.getTranslationProcessor().translate("Timeline")).append("</option></select></div>\n");
        } else {
            html.append("<div class=\"dhx_cal_tab\" name=\"day_tab\" style=\"right:100px;\"></div>\n");
            html.append("<div class=\"dhx_cal_tab\" name=\"week_tab\" style=\"right:100px;\"></div>\n");
            html.append("<div class=\"dhx_cal_tab\" name=\"month_tab\" style=\"right:100px;\"></div>\n");
            html.append("<div class=\"dhx_cal_tab\" id=\"year_tab\" name=\"year_tab\" style=\"right:100px;\"></div>\n");
            html.append("<div class=\"dhx_cal_tab\" name=\"week_agenda_tab\" style=\"right:100px;\"></div>\n");
            html.append("<div class=\"dhx_cal_tab\" name=\"timeline_tab\"  style=\"right:100px;\"></div>\n");
        }
        html.append("<div class=\"dhx_minical_icon\" id=\"dhx_minical_icon\" onclick=\"show_minical()\" style=\"right:100px;\">&nbsp;</div>\n");
        html.append("</div>\n");
        html.append("");
        html.append("<div class=\"dhx_cal_header\"></div>\n");
        html.append("<div class=\"dhx_cal_data\"></div>\n");
        html.append("");
        html.append((CharSequence)DHTMLXScheduler.getSchedulerTranslationScript(m18NUtil, this.getTranslationProcessor()));
        html.append("<style>\n");
        PropertyList eventProps = calendarProps.getPropertyListNotNull("eventprops");
        PropertyListCollection statusCollection = eventProps.getCollectionNotNull("statuscollection");
        for (int m = 0; m < statusCollection.size(); ++m) {
            PropertyList statusProps = statusCollection.getPropertyList(m);
            String status = statusProps.getProperty("eventstatus");
            String color2 = statusProps.getProperty("color", "");
            this.getEventStyles(html, status, color2);
        }
        String defaultColor = eventProps.getProperty("defaultcolor", "");
        if (!defaultColor.isEmpty()) {
            this.getEventStyles(html, "default", defaultColor);
        }
        html.append(".dhx_cal_header { z-index: 0; }\n");
        html.append(".search2 { z-index: 1; }\n");
        html.append(".dhx_year_event.status_exclusion").append(" { \n");
        html.append("  box-shadow:inset 0px 3px 0px 0px blue;").append("\n } \n");
        html.append(".dhx_year_event.status_exclusion_event").append(" { \n");
        html.append("  box-shadow:inset 0px 0px 0px 3px blue;").append("\n } \n");
        html.append(".dhx_now").append(" { \n");
        html.append("     box-shadow:inset 0px 0px 0px 1px ;").append("\n } \n");
        html.append(".dhx_month_head.dhx_year_event").append(" { \n");
        html.append("  background-color: white; !important;").append("\n } \n");
        PropertyList eventcountprops = this.element.getPropertyListNotNull("calendarprops").getPropertyListNotNull("eventcountprops");
        String selectedCountDef = eventcountprops.getProperty("selectedcountdef");
        PropertyListCollection colorDefinitions = eventcountprops.getCollectionNotNull("countdefinitions");
        colorDefinitions.index("definitionid");
        PropertyList selectedDef = colorDefinitions.getIndexedPropertyList(selectedCountDef);
        PropertyListCollection colorDef = selectedDef.getCollectionNotNull("definition");
        for (int i = 0; i < colorDef.size(); ++i) {
            PropertyList definition = colorDef.getPropertyList(i);
            String classId = definition.getProperty("classid");
            color = definition.getProperty("color");
            html.append(".dhx_year_event.events_" + classId + " ").append(" { \n");
            html.append("  background-color: " + color + ";").append("\n } \n");
        }
        html.append(".countlegend ").append(" { \n");
        html.append("  width: 70px; display: inline-block; text-align: center;").append("\n } \n");
        for (int m = 0; m < statusCollection.size(); ++m) {
            PropertyList statusProps = statusCollection.getPropertyList(m);
            String status = statusProps.getProperty("eventstatus");
            color = statusProps.getProperty("color", "");
            if (!status.equals("E")) continue;
            html.append(".dhx_month_head.dhx_year_event.status_").append(status).append("  { \n");
            if (!color.isEmpty()) {
                html.append("  background-color:").append(color).append(";\n");
                html.append("  color: black").append(";\n");
            }
            html.append("}\n");
        }
        html.append("</style>\n");
        String mode = selectedDef.getProperty("definitiontype");
        JSONObject countObj = new JSONObject();
        try {
            JSONArray countArr = new JSONArray();
            countObj.put("counts", countArr);
            countObj.put("mode", mode);
            String classes = "";
            String numCounts = "";
            for (int i = 0; i < colorDef.size(); ++i) {
                PropertyList definition = colorDef.getPropertyList(i);
                String classId = definition.getProperty("classid");
                String maxCount = definition.getProperty("maxcount", "0");
                JSONObject countClass = new JSONObject();
                countClass.put("class", "events_" + classId);
                countClass.put("numCount", maxCount);
                countArr.put(countClass);
            }
        }
        catch (JSONException jSONException) {
            // empty catch block
        }
        html.append("<input type='hidden' id='countprops' value='").append(HttpUtil.encodeURIComponent(countObj.toString())).append("'/>");
        return html;
    }
}

