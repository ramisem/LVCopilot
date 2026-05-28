/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.util.dhtmlxscheduler;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.M18NUtil;

public class DHTMLXScheduler {
    public static StringBuilder getSchedulerTranslationScript(M18NUtil m18NUtil, TranslationProcessor tp) {
        StringBuilder html = new StringBuilder();
        String[] month_full = new String[]{"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
        String[] month_short = new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        String[] day_full = new String[]{"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        String[] day_short = new String[]{"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        DHTMLXScheduler.translateArray(month_full, tp);
        DHTMLXScheduler.translateArray(month_short, tp);
        DHTMLXScheduler.translateArray(day_full, tp);
        DHTMLXScheduler.translateArray(day_short, tp);
        ArrayList<String> months_full = new ArrayList<String>(Arrays.asList(month_full));
        ArrayList<String> months_short = new ArrayList<String>(Arrays.asList(month_short));
        ArrayList<String> days_full = new ArrayList<String>(Arrays.asList(day_full));
        ArrayList<String> days_short = new ArrayList<String>(Arrays.asList(day_short));
        JSONArray monthArr = new JSONArray(months_full);
        JSONArray monthShortArr = new JSONArray(months_short);
        JSONArray dayArr = new JSONArray(days_full);
        JSONArray dayShortArr = new JSONArray(days_short);
        SimpleDateFormat defaultDateFormat = (SimpleDateFormat)m18NUtil.getDefaultDateFormat();
        String datePattern = defaultDateFormat.toPattern();
        String dateStr = DHTMLXScheduler.getCalendarDateFormatString(datePattern);
        defaultDateFormat = (SimpleDateFormat)DateFormat.getTimeInstance(3, m18NUtil.getLocale());
        datePattern = defaultDateFormat.toPattern();
        String timeStr = DHTMLXScheduler.getCalendarDateFormatString(datePattern);
        html.append("<script>");
        try {
            JSONObject locale = new JSONObject();
            JSONObject date = new JSONObject();
            locale.put("date", date);
            date.put("month_full", monthArr);
            date.put("month_short", monthShortArr);
            date.put("day_full", dayArr);
            date.put("day_short", dayShortArr);
            html.append("scheduler.locale.date=").append(date.toString()).append(";\n");
        }
        catch (JSONException e) {
            html.append(tp.translate("Could not parse locale information"));
        }
        html.append("scheduler.locale.labels.grid_tab =\"").append(tp.translate("Grid")).append("\";\n");
        html.append("scheduler.locale.labels.week_agenda_tab = \"").append(tp.translate("Week Agenda")).append("\";\n");
        html.append("scheduler.locale.labels.timeline_tab = \"").append(tp.translate("Timeline")).append("\";\n");
        html.append("scheduler.locale.labels.dhx_cal_today_button = \"").append(tp.translate("Today")).append("\";\n");
        html.append("scheduler.locale.labels.day_tab = \"").append(tp.translate("Day")).append("\";\n");
        html.append("scheduler.locale.labels.month_tab = \"").append(tp.translate("Month")).append("\";\n");
        html.append("scheduler.locale.labels.week_tab = \"").append(tp.translate("Week")).append("\";\n");
        html.append("scheduler.locale.labels.year_tab = \"").append(tp.translate("Year")).append("\";\n");
        html.append("scheduler.config.move_date = \"").append(dateStr).append("\";\n");
        html.append("scheduler.config.hour_date_month  = \"").append(timeStr).append("\";\n");
        html.append("scheduler.config.hour_date = \"").append(timeStr).append("\";\n");
        html.append("</script>\n\n");
        return html;
    }

    private static String getCalendarDateFormatString(String datePattern) {
        datePattern = datePattern.replaceAll("^mm", "%i");
        datePattern = datePattern.replaceAll("^MMM", "%M");
        datePattern = datePattern.replaceAll("^yyyy", "%Y");
        datePattern = datePattern.replaceAll("^yy", "%y");
        datePattern = datePattern.replaceAll("^MM", "%m");
        datePattern = datePattern.replaceAll("^M", "%n");
        datePattern = datePattern.replaceAll("^w", "%W");
        datePattern = datePattern.replaceAll("^dd", "%d");
        datePattern = datePattern.replaceAll("^d", "%j");
        datePattern = datePattern.replaceAll("^D", "%d");
        datePattern = datePattern.replaceAll("^HH", "%H");
        datePattern = datePattern.replaceAll("^H", "%G");
        datePattern = datePattern.replaceAll("^hh", "%h");
        datePattern = datePattern.replaceAll("^h", "%g");
        datePattern = datePattern.replaceAll("^ss", "%s");
        datePattern = datePattern.replaceAll("^a", "%A");
        datePattern = datePattern.replaceAll("([^%])mm", "$1%i");
        datePattern = datePattern.replaceAll("([^%])yyyy", "$1%Y");
        datePattern = datePattern.replaceAll("([^%])yy", "$1%y");
        datePattern = datePattern.replaceAll("([^%])MMM", "$1%M");
        datePattern = datePattern.replaceAll("([^%])MM", "$1%m");
        datePattern = datePattern.replaceAll("([^%])M", "$1%n");
        datePattern = datePattern.replaceAll("([^%])w", "$1%W");
        datePattern = datePattern.replaceAll("([^%])dd", "$1%d");
        datePattern = datePattern.replaceAll("([^%])d", "$1%j");
        datePattern = datePattern.replaceAll("([^%])D", "$1%d");
        datePattern = datePattern.replaceAll("([^%])HH", "$1%H");
        datePattern = datePattern.replaceAll("([^%])H", "$1%G");
        datePattern = datePattern.replaceAll("([^%])h", "$1%g");
        datePattern = datePattern.replaceAll("([^%])ss", "$1%s");
        datePattern = datePattern.replaceAll("([^%])a", "$1%A");
        return datePattern;
    }

    private static void translateArray(String[] array, TranslationProcessor tp) {
        for (int i = 0; i < array.length; ++i) {
            array[i] = tp.translate(array[i]);
        }
    }
}

