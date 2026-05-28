/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 *  org.joda.time.DateTime
 *  org.joda.time.ReadableInstant
 *  org.joda.time.format.DateTimeFormat
 *  org.joda.time.format.DateTimeFormatter
 */
package com.labvantage.sapphire.pageelements.list;

import com.labvantage.sapphire.BaseCustom;
import com.labvantage.sapphire.RequestParser;
import com.labvantage.sapphire.pageelements.ElementUtil;
import com.labvantage.sapphire.pageelements.datachart.chart.plot.ConfigurableDrawingSupplier;
import com.labvantage.sapphire.tagext.JavaScriptAPITag;
import java.awt.Color;
import java.awt.Paint;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import javax.servlet.jsp.PageContext;
import org.joda.time.DateTime;
import org.joda.time.ReadableInstant;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.M18NUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class CalendarHelper
extends BaseCustom {
    private static final String PROPERTY_COLUMNID = "columnid";
    private static final String PROPERTY_SELECTVALUE = "selectvalue";
    private static final String PROPERTY_TEXT = "text";
    private static final String PROPERTY_STARTDATE = "startdatecolumn";
    private static final String PROPERTY_EVENTCOLORMAPPINGCOLUMNID = "eventcolormappingcolumnid";
    private static final String PROPERTY_ENDDATE = "enddatecolumn";
    private static final String ELEMENTCONFIG_CALENDARPROPS = "calendarprops";
    private static final String ELEMENTCONFIG_DEFAULTSELECTVALUE = "selectordefault";
    private static final String ELEMENTCONFIG_EVENTPROPS = "eventpropscollection";
    private static final String DEFAULT_FIRST_DAY_OF_WEEK = "Sunday";
    private PageContext pageContext;

    public CalendarHelper(PageContext pageContext) {
        this.pageContext = pageContext;
    }

    private CalendarHelper() {
    }

    public void forceCalendarColumns(PropertyList element, PropertyListCollection columns) {
        HashSet<String> requiredColumns = new HashSet<String>();
        PropertyList calendarProps = element.getPropertyListNotNull(ELEMENTCONFIG_CALENDARPROPS);
        PropertyListCollection eventPropsCollection = calendarProps.getCollectionNotNull(ELEMENTCONFIG_EVENTPROPS);
        if (!calendarProps.getProperty(PROPERTY_EVENTCOLORMAPPINGCOLUMNID).isEmpty()) {
            requiredColumns.add(calendarProps.getProperty(PROPERTY_EVENTCOLORMAPPINGCOLUMNID));
        }
        for (int i = 0; i < eventPropsCollection.size(); ++i) {
            PropertyList eventProps = eventPropsCollection.getPropertyList(i);
            requiredColumns.add(eventProps.getProperty(PROPERTY_TEXT));
            if (!eventProps.getProperty(PROPERTY_STARTDATE).isEmpty()) {
                requiredColumns.add(eventProps.getProperty(PROPERTY_STARTDATE));
            }
            if (!eventProps.getProperty(PROPERTY_ENDDATE).isEmpty()) {
                requiredColumns.add(eventProps.getProperty(PROPERTY_ENDDATE));
            }
            if (eventProps.getProperty(PROPERTY_EVENTCOLORMAPPINGCOLUMNID).isEmpty()) continue;
            requiredColumns.add(eventProps.getProperty(PROPERTY_EVENTCOLORMAPPINGCOLUMNID));
        }
        ArrayList<String> foundColumns = new ArrayList<String>();
        for (int index = 0; index < columns.size(); ++index) {
            String columnName = RequestParser.parseAlias(columns.getPropertyList(index).getProperty(PROPERTY_COLUMNID, ""));
            if (columnName.length() <= 0 || !requiredColumns.contains(columnName)) continue;
            foundColumns.add(columnName);
        }
        for (String calendarField : requiredColumns) {
            if (foundColumns.contains(calendarField)) continue;
            PropertyList column = new PropertyList();
            column.setId(calendarField);
            column.setProperty(PROPERTY_COLUMNID, calendarField);
            column.setProperty("mode", "hidden");
            columns.add(column);
        }
    }

    void insertColumnCalendarField(PropertyList element, PropertyList column, JSONObject columnObject, Object value, boolean isTimeZoneIndependent) throws JSONException {
        PropertyListCollection eventPropsCollection = element.getPropertyListNotNull(ELEMENTCONFIG_CALENDARPROPS).getCollectionNotNull(ELEMENTCONFIG_EVENTPROPS);
        for (int i = 0; i < eventPropsCollection.size(); ++i) {
            DateTimeFormatter dtf;
            PropertyList eventProps = eventPropsCollection.getPropertyList(i);
            if (!eventProps.values().contains(column.getProperty(PROPERTY_COLUMNID)) || !(value instanceof Calendar)) continue;
            if (isTimeZoneIndependent) {
                dtf = DateTimeFormat.forPattern((String)"yyyy-MM-dd'T'00:00");
                columnObject.put("calendarvalue", dtf.print((ReadableInstant)new DateTime(value)));
                columnObject.put("istimzoneindependent", "Y");
                continue;
            }
            dtf = DateTimeFormat.forPattern((String)"yyyy-MM-dd'T'HH:mm");
            columnObject.put("calendarvalue", dtf.print((ReadableInstant)new DateTime(value)));
            columnObject.put("istimzoneindependent", "N");
        }
    }

    public String getHtml(PropertyList element) {
        PropertyList calendarProps = element.getPropertyListNotNull(ELEMENTCONFIG_CALENDARPROPS);
        try {
            PropertyList dateFormatPolicy = new ConfigurationProcessor(this.pageContext).getPolicy("DateFormatPolicy", "Sapphire Custom");
            String firstDayOfWeek = dateFormatPolicy.getProperty("firstdayofweek", DEFAULT_FIRST_DAY_OF_WEEK);
            M18NUtil m18NUtil = new M18NUtil(this.pageContext);
            if (firstDayOfWeek.equals("User Locale")) {
                calendarProps.setProperty("weekstartonmonday", m18NUtil.getNowCalendar().getFirstDayOfWeek() == 2 ? "Y" : "N");
            } else {
                calendarProps.setProperty("weekstartonmonday", firstDayOfWeek.equals("Monday") ? "Y" : "N");
            }
        }
        catch (SapphireException dateFormatPolicy) {
            // empty catch block
        }
        StringBuilder html = new StringBuilder();
        PropertyList colorset = new PropertyList();
        PropertyListCollection eventPropsCollection = calendarProps.getCollectionNotNull(ELEMENTCONFIG_EVENTPROPS);
        for (int i = 0; i < eventPropsCollection.size(); ++i) {
            Paint[] paintArray;
            PropertyList eventProps = eventPropsCollection.getPropertyList(i);
            String colorScheme = eventProps.getProperty("autoassignedcolorscheme", "");
            PropertyListCollection colors = new PropertyListCollection();
            for (Paint paint : paintArray = ConfigurableDrawingSupplier.getStandardPaintArray(colorScheme)) {
                Color color = (Color)paint;
                PropertyList colorList = new PropertyList();
                colorList.setProperty("colorvalue", String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue()));
                colorList.setProperty("highlightcolorvalue", String.format("#%02x%02x%02x", color.brighter().getRed(), color.brighter().getGreen(), color.brighter().getBlue()));
                colors.add(colorList);
            }
            colorset.setProperty(colorScheme, colors);
        }
        PropertyListCollection plugins = new PropertyListCollection();
        PropertyList plugin = new PropertyList();
        plugin.setProperty("pluginid", "perfect-scrollbar");
        plugin.setProperty("css", "Y");
        plugin.setProperty("allowminimized", "Y");
        plugins.add(plugin);
        html.append(JavaScriptAPITag.getJQueryAPI(true, false, plugins, this.pageContext));
        html.append("<link rel=\"stylesheet\" href=\"WEB-CORE/extscripts/dhtmlx/scheduler/dhtmlxscheduler_flat.css\" type=\"text/css\" media=\"screen\"  title=\"no title\" charset=\"utf-8\">");
        html.append("<script>var CALENDAR = CALENDAR || {};CALENDAR.calendarprops = ").append(calendarProps.toJSONString(true, true)).append(";</script>");
        html.append("<script>var CALENDAR = CALENDAR || {};CALENDAR.colors = ").append(colorset.toJSONString(true, true)).append(";</script>");
        html.append("<script src=\"WEB-CORE/pagetypes/list/scripts/calendar.js\"></script>");
        html.append("<script src=\"WEB-CORE/extscripts/dhtmlx/scheduler/dhtmlxscheduler.js\" type=\"text/javascript\" charset=\"utf-8\"></script>\n");
        html.append("<script src=\"WEB-CORE/extscripts/dhtmlx/scheduler/ext/dhtmlxscheduler_grid_view.js\"></script>\n");
        html.append("<script src=\"WEB-CORE/extscripts/dhtmlx/scheduler/ext/dhtmlxscheduler_year_view.js\"></script>\n");
        html.append("<script src=\"WEB-CORE/extscripts/dhtmlx/scheduler/ext/dhtmlxscheduler_timeline.js\"></script>\n");
        html.append("<script src=\"WEB-CORE/extscripts/dhtmlx/scheduler/ext/dhtmlxscheduler_treetimeline.js\"></script>\n");
        html.append("<script src=\"WEB-CORE/extscripts/dhtmlx/scheduler/ext/dhtmlxscheduler_daytimeline.js\"></script>\n");
        html.append("<script src=\"WEB-CORE/extscripts/dhtmlx/scheduler/ext/dhtmlxscheduler_limit.js\"></script>\n");
        html.append("<script src=\"WEB-CORE/extscripts/dhtmlx/scheduler/ext/dhtmlxscheduler_week_agenda.js\"></script>\n");
        html.append("<script src=\"WEB-CORE/extscripts/dhtmlx/scheduler/ext/dhtmlxscheduler_timeline.js\"></script>");
        html.append("<script src=\"WEB-CORE/extscripts/dhtmlx/scheduler/ext/dhtmlxscheduler_minical.js\" type=\"text/javascript\"></script>\n");
        html.append("<script src=\"WEB-CORE/extscripts/dhtmlx/scheduler/ext/dhtmlxscheduler_active_links.js\" type=\"text/javascript\" ></script>\n");
        html.append("<script src=\"rc?command=ajax&ajaxclass=com.labvantage.sapphire.ajax.operations.DHTMLXSchedulerTranslation\" type=\"text/javascript\"></script>");
        html.append("<table><tr id=\"calendar_checkboxes\"></tr></table>");
        html.append("<div id=\"scheduler_here\" class=\"dhx_cal_container\"  style='width:800px; height:600px;'>\n");
        html.append("  <div class=\"dhx_cal_navline\">\n");
        html.append("    <div class=\"dhx_cal_prev_button\">&nbsp;</div>\n");
        html.append("    <div class=\"dhx_cal_next_button\">&nbsp;</div>\n");
        html.append("    <div class=\"dhx_cal_today_button\"></div>\n");
        html.append("    <div class=\"dhx_cal_date\"></div>\n");
        html.append("    <div class=\"dhx_cal_tab\" name=\"day_tab\" style=\"right:204px;\"></div>\n");
        html.append("    <div class=\"dhx_cal_tab\" name=\"week_tab\" style=\"right:140px;\"></div>\n");
        html.append("    <div class=\"dhx_cal_tab\" name=\"month_tab\" style=\"right:76px;\"></div>\n");
        html.append("    <div class=\"dhx_cal_tab\" name=\"year_tab\" style=\"right:280px;\"></div>\n");
        html.append("  </div>");
        html.append("  <div class=\"dhx_cal_header\"></div>");
        html.append("  <div class=\"dhx_cal_data\"></div>");
        html.append("</div>");
        return html.toString();
    }

    StringBuffer getDatesSelector(PropertyList element, TranslationProcessor tp) {
        StringBuffer html = new StringBuffer();
        PropertyListCollection eventPropsCollection = element.getPropertyListNotNull(ELEMENTCONFIG_CALENDARPROPS).getCollectionNotNull(ELEMENTCONFIG_EVENTPROPS);
        String defaultselected = element.getPropertyListNotNull(ELEMENTCONFIG_CALENDARPROPS).getProperty(ELEMENTCONFIG_DEFAULTSELECTVALUE, "");
        if (eventPropsCollection.size() < 2) {
            html.append("<div style=\"display:none\">");
        }
        html.append(ElementUtil.getText(element, "selectdates", "Select mode", tp)).append("&nbsp;<select id=\"selectdates\" name=\"selectdates\"  onchange=\"calendar.updateSelectedDates(this.value);\">\n");
        for (int i = 0; i < eventPropsCollection.size(); ++i) {
            PropertyList eventProps = eventPropsCollection.getPropertyList(i);
            String selectValue = eventProps.getProperty(PROPERTY_SELECTVALUE, Integer.toString(i));
            html.append("<option value=\"").append(i).append("\" ").append(selectValue.equalsIgnoreCase(defaultselected) ? "selected" : "").append(">").append(selectValue).append("</option>");
        }
        html.append("</select>");
        if (eventPropsCollection.size() < 2) {
            html.append("</div>");
        }
        return html;
    }

    boolean calendarConfigured(PropertyList element) {
        PropertyList eventProps = new PropertyList();
        PropertyListCollection eventPropsCollection = element.getPropertyListNotNull(ELEMENTCONFIG_CALENDARPROPS).getCollectionNotNull(ELEMENTCONFIG_EVENTPROPS);
        if (eventPropsCollection.size() > 0) {
            eventProps = eventPropsCollection.getPropertyList(0);
        }
        return !eventProps.getProperty(PROPERTY_TEXT, "").isEmpty() && (!eventProps.getProperty(PROPERTY_STARTDATE, "").isEmpty() || !eventProps.getProperty(PROPERTY_ENDDATE, "").isEmpty());
    }

    public void setPageContext(PageContext pageContext) {
        this.pageContext = pageContext;
    }
}

