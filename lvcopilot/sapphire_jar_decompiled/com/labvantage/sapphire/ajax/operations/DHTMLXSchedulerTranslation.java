/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.ajax.operations;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.accessor.TranslationProcessor;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;

public class DHTMLXSchedulerTranslation
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";

    @Override
    public boolean acceptContentType(String contentType) {
        return contentType == null || contentType.equalsIgnoreCase("application/x-www-form-urlencoded") || contentType.equalsIgnoreCase("application/json");
    }

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        response.setHeader("Cache-Control", "max-age=2592000");
        response.setDateHeader("Last-Modified", System.currentTimeMillis() - 10000000000L);
        response.setHeader("Pragma", "");
        response.setContentType("text/javascript");
        String translationJSON = this.generateSchedulerTranslations();
        this.write(translationJSON);
    }

    public String generateSchedulerTranslations() {
        StringBuffer sb = new StringBuffer();
        TranslationProcessor tp = this.getTranslationProcessor();
        sb.append("Scheduler.plugin(function(scheduler){scheduler.locale = {");
        sb.append("date:{");
        sb.append("month_full:[\"").append(tp.translate("January")).append("\", \"").append(tp.translate("February")).append("\", \"").append(tp.translate("March")).append("\", \"").append(tp.translate("April")).append("\", \"").append(tp.translate("May")).append("\", \"").append(tp.translate("June")).append("\", \"").append(tp.translate("July")).append("\", \"").append(tp.translate("August")).append("\", \"").append(tp.translate("September")).append("\", \"").append(tp.translate("October")).append("\", \"").append(tp.translate("November")).append("\", \"").append(tp.translate("December")).append("\"],");
        sb.append("month_short:[\"").append(tp.translate("Jan")).append("\", \"").append(tp.translate("Feb")).append("\", \"").append(tp.translate("Mar")).append("\", \"").append(tp.translate("Apr")).append("\", \"").append(tp.translate("May")).append("\", \"").append(tp.translate("Jun")).append("\", \"").append(tp.translate("Jul")).append("\", \"").append(tp.translate("Aug")).append("\", \"").append(tp.translate("Sep")).append("\", \"").append(tp.translate("Oct")).append("\", \"").append(tp.translate("Nov")).append("\", \"").append(tp.translate("Dec")).append("\"],");
        sb.append("day_full:[\"").append(tp.translate("Sunday")).append("\", \"").append(tp.translate("Monday")).append("\", \"").append(tp.translate("Tuesday")).append("\", \"").append(tp.translate("Wednesday")).append("\", \"").append(tp.translate("Thursday")).append("\", \"").append(tp.translate("Friday")).append("\", \"").append(tp.translate("Saturday")).append("\"],");
        sb.append("day_short:[\"").append(tp.translate("Sun")).append("\", \"").append(tp.translate("Mon")).append("\", \"").append(tp.translate("Tue")).append("\", \"").append(tp.translate("Wed")).append("\", \"").append(tp.translate("Thu")).append("\", \"").append(tp.translate("Fri")).append("\", \"").append(tp.translate("Sat")).append("\"]");
        sb.append("},");
        sb.append("labels:{");
        sb.append("dhx_cal_today_button:\"").append(tp.translate("Today")).append("\",");
        sb.append("day_tab:\"").append(tp.translate("Day")).append("\",");
        sb.append("week_tab:\"").append(tp.translate("Week")).append("\",");
        sb.append("month_tab:\"").append(tp.translate("Month")).append("\",");
        sb.append("new_event:\"").append(tp.translate("New event")).append("\",");
        sb.append("icon_save:\"").append(tp.translate("Save")).append("\",");
        sb.append("icon_cancel:\"").append(tp.translate("Cancel")).append("\",");
        sb.append("icon_details:\"").append(tp.translate("Details")).append("\",");
        sb.append("icon_edit:\"").append(tp.translate("Edit")).append("\",");
        sb.append("icon_delete:\"").append(tp.translate("Delete")).append("\",");
        sb.append("confirm_closing:\"").append(tp.translate("")).append("\",");
        sb.append("confirm_deleting:\"").append(tp.translate("Event will be deleted permanently, are you sure?")).append("\",");
        sb.append("section_description:\"").append(tp.translate("Description")).append("\",");
        sb.append("section_time:\"").append(tp.translate("Time period")).append("\",");
        sb.append("full_day:\"").append(tp.translate("Full day")).append("\",");
        sb.append("confirm_recurring:\"").append(tp.translate("Do you want to edit the whole set of repeated events?")).append("\",");
        sb.append("section_recurring:\"").append(tp.translate("Repeat event")).append("\",");
        sb.append("button_recurring:\"").append(tp.translate("Disabled")).append("\",");
        sb.append("button_recurring_open:\"").append(tp.translate("Enabled")).append("\",");
        sb.append("button_edit_series:\"").append(tp.translate("Edit series")).append("\",");
        sb.append("button_edit_occurrence:\"").append(tp.translate("Edit occurrence")).append("\",");
        sb.append("agenda_tab:\"").append(tp.translate("Agenda")).append("\",");
        sb.append("date:\"").append(tp.translate("Date")).append("\",");
        sb.append("description:\"").append(tp.translate("Description")).append("\",");
        sb.append("year_tab:\"").append(tp.translate("Year")).append("\",");
        sb.append("week_agenda_tab:\"").append(tp.translate("Agenda")).append("\",");
        sb.append("grid_tab:\"").append(tp.translate("Grid")).append("\",");
        sb.append("drag_to_create:\"").append(tp.translate("Drag to create")).append("\",");
        sb.append("drag_to_move:\"").append(tp.translate("Drag to move")).append("\",");
        sb.append("message_ok:\"").append(tp.translate("OK")).append("\",");
        sb.append("message_cancel:\"").append(tp.translate("Cancel")).append("\",");
        sb.append("next:\"").append(tp.translate("Next")).append("\",");
        sb.append("prev:\"").append(tp.translate("Previous")).append("\",");
        sb.append("year:\"").append(tp.translate("Year")).append("\",");
        sb.append("month:\"").append(tp.translate("Month")).append("\",");
        sb.append("day:\"").append(tp.translate("Day")).append("\",");
        sb.append("hour:\"").append(tp.translate("Hour")).append("\",");
        sb.append("minute:\"").append(tp.translate("Minute")).append("\",");
        sb.append("repeat_radio_day:\"").append(tp.translate("Daily")).append("\",");
        sb.append("repeat_radio_week:\"").append(tp.translate("Weekly")).append("\",");
        sb.append("repeat_radio_month:\"").append(tp.translate("Monthly")).append("\",");
        sb.append("repeat_radio_year:\"").append(tp.translate("Yearly")).append("\",");
        sb.append("repeat_radio_day_type:\"").append(tp.translate("Every")).append("\",");
        sb.append("repeat_text_day_count:\"").append(tp.translate("day")).append("\",");
        sb.append("repeat_radio_day_type2:\"").append(tp.translate("Every workday")).append("\",");
        sb.append("repeat_week:\"").append(tp.translate(" Repeat every")).append("\",");
        sb.append("repeat_text_week_count:\"").append(tp.translate("week next days:")).append("\",");
        sb.append("repeat_radio_month_type:\"").append(tp.translate("Repeat")).append("\",");
        sb.append("repeat_radio_month_start:\"").append(tp.translate("On")).append("\",");
        sb.append("repeat_text_month_day:\"").append(tp.translate("day every")).append("\",");
        sb.append("repeat_text_month_count:\"").append(tp.translate("month")).append("\",");
        sb.append("repeat_text_month_count2_before:\"").append(tp.translate("every")).append("\",");
        sb.append("repeat_text_month_count2_after:\"").append(tp.translate("month")).append("\",");
        sb.append("repeat_year_label:\"").append(tp.translate("On")).append("\",");
        sb.append("select_year_day2:\"").append(tp.translate("of")).append("\",");
        sb.append("repeat_text_year_day:\"").append(tp.translate("day")).append("\",");
        sb.append("select_year_month:\"").append(tp.translate("month")).append("\",");
        sb.append("repeat_radio_end:\"").append(tp.translate("No end date")).append("\",");
        sb.append("repeat_text_occurences_count:\"").append(tp.translate("occurrences")).append("\",");
        sb.append("repeat_radio_end2:\"").append(tp.translate("After")).append("\",");
        sb.append("repeat_radio_end3:\"").append(tp.translate("End by")).append("\",");
        sb.append("month_for_recurring:[\"").append(tp.translate("January")).append("\", \"").append(tp.translate("February")).append("\", \"").append(tp.translate("March")).append("\", \"").append(tp.translate("April")).append("\", \"").append(tp.translate("May")).append("\", \"").append(tp.translate("June")).append("\", \"").append(tp.translate("July")).append("\", \"").append(tp.translate("August")).append("\", \"").append(tp.translate("September")).append("\", \"").append(tp.translate("October")).append("\", \"").append(tp.translate("November")).append("\", \"").append(tp.translate("December")).append("\"],");
        sb.append("day_for_recurring:[\"").append(tp.translate("Sunday")).append("\", \"").append(tp.translate("Monday")).append("\", \"").append(tp.translate("Tuesday")).append("\", \"").append(tp.translate("Wednesday")).append("\", \"").append(tp.translate("Thursday")).append("\", \"").append(tp.translate("Friday")).append("\", \"").append(tp.translate("Saturday")).append("\"]");
        sb.append("}");
        sb.append("};");
        sb.append("});");
        return sb.toString();
    }
}

