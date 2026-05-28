/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.JspWriter
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.stability;

import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.I18nUtil;
import com.labvantage.sapphire.services.ConnectionInfo;
import com.labvantage.sapphire.stability.EventTaskMapper;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import sapphire.accessor.QueryProcessor;
import sapphire.util.DataSet;
import sapphire.util.HttpUtil;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;

public class CalendarView {
    EventTaskMapper map = null;
    Calendar today = null;
    Calendar currentday = null;
    Calendar currentMonth = null;
    Calendar currentWeek = null;
    Calendar firstday = null;
    SimpleDateFormat monformatter = null;
    SimpleDateFormat weekformatter = null;
    String year = null;
    String cellClass = "calendarCell";
    String cellTopClass = "calendarCellTop";
    String cellRightClass = "calendarCellRight";
    String cellTopRightClass = "calendarCellTopRight";
    String monthHeaderClass = "monthHeadler";
    String weekHeaderClass = "weekHeader";
    String monthTableClass = "monthTable";
    int monthsPerRow = 3;
    String dateCallBack = "";
    String monthCallBack = "";
    DateFormat sdf = DateFormat.getDateInstance(3);
    DateFormat localesdf = null;
    StringBuffer dCellIdArray = new StringBuffer();
    ArrayList hasCountIdList = new ArrayList();
    ArrayList countNames = new ArrayList();
    ArrayList countArrays = new ArrayList();
    Set excludesSet = new HashSet();
    PageContext pageContext = null;
    JspWriter out = null;

    public CalendarView(PageContext pageContext) {
        Locale locale = I18nUtil.getSessionLocale(pageContext);
        TimeZone timezone = I18nUtil.getSessionTimeZone(pageContext);
        this.today = Calendar.getInstance(timezone, locale);
        this.year = pageContext.getRequest().getParameter("year") != null ? pageContext.getRequest().getParameter("year") : String.valueOf(this.today.get(1));
        this.pageContext = pageContext;
        this.out = pageContext.getOut();
        this.currentday = Calendar.getInstance(timezone, locale);
        this.currentMonth = Calendar.getInstance(timezone, locale);
        this.currentWeek = Calendar.getInstance(timezone, locale);
        this.firstday = Calendar.getInstance(timezone, locale);
        this.monformatter = new SimpleDateFormat("MMMM", locale);
        this.weekformatter = new SimpleDateFormat("EEE", locale);
        ConnectionInfo connectionInfo = HttpUtil.getConnectionInfo(pageContext);
        DateTimeUtil dtu = new DateTimeUtil(connectionInfo);
        this.localesdf = dtu.getDefaultDateOnlyFormat();
    }

    public void setEventTaskMapper(EventTaskMapper map) {
        this.map = map;
    }

    public void writeIdArray() throws IOException {
        if (this.dCellIdArray.length() > 3) {
            this.out.write("<script>var dCellIdArray = new Array(" + this.dCellIdArray.substring(2) + "')</script>");
        }
    }

    public void writeExcludeArray(String scheduleexcludeid) throws IOException {
        QueryProcessor qp = new QueryProcessor(this.pageContext);
        this.out.print("<script>var excludeidArray=null;");
        if (scheduleexcludeid == null || scheduleexcludeid.length() == 0) {
            scheduleexcludeid = "(none)";
        } else {
            SafeSQL safeSQL = new SafeSQL();
            String sql = "select * from scheduleexcludeitem where scheduleexcludeid = " + safeSQL.addVar(scheduleexcludeid);
            DataSet seiDataSet = qp.getPreparedSqlDataSet(sql, safeSQL.getValues());
            DateFormat sdf = DateFormat.getDateInstance(3);
            int excluderow = seiDataSet.getRowCount();
            if (excluderow > 0) {
                this.out.print("excludeidArray = new Array(");
                for (int i = 0; i < excluderow; ++i) {
                    if (seiDataSet.getCalendar(i, "excludedt") != null) {
                        String td = sdf.format(seiDataSet.getCalendar(i, "excludedt").getTime());
                        if (i == 0) {
                            this.out.print("'" + td);
                        } else {
                            this.out.print("','" + td);
                        }
                        this.excludesSet.add(td);
                        continue;
                    }
                    if (i == 0) {
                        this.out.print("'" + seiDataSet.getValue(i, "excluderule"));
                    } else {
                        this.out.print("','" + seiDataSet.getValue(i, "excluderule"));
                    }
                    this.excludesSet.add(seiDataSet.getValue(i, "excluderule"));
                }
                this.out.print("');");
            }
        }
        this.out.print("</script>");
    }

    String printDayCell(Calendar currentday, boolean isFirstRow, boolean isLastCell) throws IOException {
        if (this.localesdf == null) {
            this.localesdf = DateFormat.getDateInstance(3, HttpUtil.getSessionLocale(this.pageContext));
        }
        String localedayformat = this.localesdf.format(currentday.getTime());
        String dayformat = this.sdf.format(currentday.getTime());
        String cellClass = isFirstRow && isLastCell ? this.cellTopRightClass : (isFirstRow ? this.cellTopClass : (isLastCell ? this.cellRightClass : this.cellClass));
        this.dCellIdArray.append("','" + dayformat);
        int count = this.map.getCount(dayformat);
        String countSpan = count == 0 ? "" : "<br/><span class=\"CountField\">(" + count + ")</span>";
        String detail = this.map.getDetail(dayformat);
        String excluded = "N";
        if (this.excludesSet.contains(dayformat)) {
            excluded = "Y";
        } else {
            int dayofweek = currentday.get(7);
            switch (dayofweek) {
                case 1: {
                    excluded = this.excludesSet.contains("Sun") ? "Y" : "N";
                    break;
                }
                case 2: {
                    excluded = this.excludesSet.contains("Mon") ? "Y" : "N";
                    break;
                }
                case 3: {
                    excluded = this.excludesSet.contains("Tue") ? "Y" : "N";
                    break;
                }
                case 4: {
                    excluded = this.excludesSet.contains("Wed") ? "Y" : "N";
                    break;
                }
                case 5: {
                    excluded = this.excludesSet.contains("Thu") ? "Y" : "N";
                    break;
                }
                case 6: {
                    excluded = this.excludesSet.contains("Fri") ? "Y" : "N";
                    break;
                }
                case 7: {
                    String string = excluded = this.excludesSet.contains("Sat") ? "Y" : "N";
                }
            }
        }
        if (currentday.equals(this.today)) {
            this.out.println("<td id=\"" + dayformat + "\" ex=\"" + excluded + "\" style=\"font-size:13;color:black;\" class=\"" + cellClass + "\" title=\"" + detail + "\" onclick=\"dateClicked( this, '" + localedayformat + "' );\" ondoubleclick=\"dateClicked( this, '" + localedayformat + "' );\">" + currentday.get(5) + countSpan + "</td>");
        } else {
            this.out.println("<td id=\"" + dayformat + "\" ex=\"" + excluded + "\" class=\"" + cellClass + "\" " + (detail.length() > 0 ? "title=\"" + detail + "\" " : "") + "onclick=\"dateClicked( this, '" + localedayformat + "' );\">" + currentday.get(5) + countSpan + "</td>");
        }
        return "";
    }

    String printMonthGrid(int month) throws IOException {
        this.currentMonth.set(2, month);
        this.currentday.set(2, month);
        this.currentday.set(1, Integer.parseInt(this.year));
        this.firstday.set(2, month);
        this.firstday.set(1, Integer.parseInt(this.year));
        this.firstday.set(5, 1);
        int lastday = this.firstday.getActualMaximum(5);
        if (month % this.monthsPerRow == 0) {
            this.out.print("<tr>");
        }
        this.out.print("<td valign=\"top\" >");
        this.out.print("<table cellspacing=\"0\" cellpadding=\"1\" class=\"" + this.monthTableClass + "\">");
        this.out.print("<tr>");
        this.out.print("<td class=\"" + this.monthHeaderClass + "\" colspan=\"7\" onclick=\"monthClicked('" + this.currentMonth.get(2) + "')\" title=\"Click to list event details for the month.\"><b>" + this.monformatter.format(this.currentMonth.getTime()) + "</b></td>");
        this.out.print("</tr>");
        this.out.print("<tr class=\"" + this.weekHeaderClass + "\">");
        for (int w = 1; w <= 7; ++w) {
            this.currentWeek.set(7, w);
            String localweek = this.weekformatter.format(this.currentWeek.getTime());
            this.out.print("<td class=\"" + this.weekHeaderClass + "\">" + localweek + "</td>");
        }
        this.out.print("</tr>");
        this.out.print("<tr>");
        for (int i = 1; i < this.firstday.get(7); ++i) {
            this.out.println("<td class=\"calendarEmptyCell\">&nbsp;</td>");
        }
        int daynumber = 1;
        boolean isFirstRow = true;
        int i = this.firstday.get(7) - 1;
        while (daynumber <= lastday) {
            this.currentday.set(5, daynumber);
            boolean isLastCell = false;
            if (this.currentday.get(7) == 7 || daynumber == lastday) {
                isLastCell = true;
            }
            if (this.currentday.get(7) == 1 && !this.currentday.equals(this.firstday)) {
                this.out.println("</tr><tr>");
                isFirstRow = false;
            }
            this.printDayCell(this.currentday, isFirstRow, isLastCell);
            isLastCell = false;
            ++daynumber;
            ++i;
        }
        this.out.print("</tr></table></td>");
        return "";
    }

    String printMonthGridWithDetail() {
        return "";
    }

    public String printCalendar(String studyid) throws IOException {
        this.out.print("<table width=\"100%\">");
        this.out.print("<tr><td width=\"140px\">&nbsp;</td>");
        this.out.print("<td align=\"center\">");
        this.out.print("<table width=\"400px\" border=\"0\" align=\"left\" bgcolor=\"#FFEBCD\" style=\"text-align=center;font-weight:bold\">");
        this.out.print("<tr><td align=\"center\">");
        if ("All Studies".equals(studyid)) {
            this.out.print("<a href=\"\" onclick=\"document.getElementById('year').value=" + this.year + " - 1;document.getElementById('displayoptions').submit();return false;\"><img src=\"WEB-CORE/lookup/images/cal_arw_lf.gif\" border=\"0\"></a>");
            this.out.print("&nbsp;" + this.year + "&nbsp;");
            this.out.print("<a href=\"\" onclick=\"document.getElementById('year').value=" + this.year + " + 1;document.getElementById('displayoptions').submit();return false;\"><img src=\"WEB-CORE/lookup/images/cal_arw_rt.gif\" border=\"0\"></a>");
        } else {
            String[] years = StringUtil.split(this.map.getEventYearList(), ";");
            for (int i = 0; i < years.length; ++i) {
                if (years[i].equals("" + this.year)) {
                    this.out.print("&nbsp;" + this.year);
                    continue;
                }
                this.out.print("&nbsp;<a href=\"\" onclick=\"document.getElementById('year').value=" + years[i] + ";document.getElementById('displayoptions').submit();return false;\">" + years[i] + "</a>");
            }
        }
        this.out.print("</td></tr></table><td></tr></table>");
        this.out.print("<table>");
        for (int m = 0; m < 12; ++m) {
            this.printMonthGrid(m);
        }
        this.out.print("</table>");
        return "";
    }

    public static void writeTaskDropdown(PageContext pageContext, String select, String type) throws IOException {
        QueryProcessor qp = new QueryProcessor(pageContext);
        JspWriter out = pageContext.getOut();
        DataSet taskDS = qp.getSqlDataSet("select propertytreeid, objectname from propertytree where propertytreetype = 'ScheduleTask'");
        for (int i = 0; i < taskDS.getRowCount(); ++i) {
            String tempid = taskDS.getString(i, "propertytreeid");
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
            }
            if (!include) continue;
            out.print("<option value=\"" + tempid + "\"" + selected + ">" + tempid + "</option>");
        }
    }
}

